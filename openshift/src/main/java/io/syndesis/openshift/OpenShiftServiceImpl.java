/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.openshift;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DeploymentConfigStatus;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.syndesis.core.Names;

public class OpenShiftServiceImpl implements OpenShiftService {

    private final NamespacedOpenShiftClient openShiftClient;
    private final OpenShiftConfigurationProperties config;

    public OpenShiftServiceImpl(NamespacedOpenShiftClient openShiftClient, OpenShiftConfigurationProperties config) {
        this.openShiftClient = openShiftClient;
        this.config = config;
    }

    @Override
    public void ensureSetup(String name, DeploymentData deploymentData) {
        String sName = Names.sanitize(name);
        ensureImageStreams(sName);
        ensureDeploymentConfig(sName, deploymentData, this.config.getIntegrationServiceAccount());
        ensureSecret(sName, deploymentData);
        ensureBuildConfig(sName, deploymentData, this.config.getBuilderImageStreamTag());
    }

    @Override
    public void build(String name, InputStream tarInputStream) throws IOException {
        String sName = Names.sanitize(name);
        openShiftClient.buildConfigs().withName(sName)
                       .instantiateBinary()
                       .fromInputStream(tarInputStream);
    }

    @Override
    public boolean delete(String name) {
        String sName = Names.sanitize(name);
        return
            removeImageStreams(sName) &&
            removeDeploymentConfig(sName) &&
            removeSecret(sName) &&
            removeBuildConfig(sName);
    }

    @Override
    public boolean exists(String name) {
        String sName = Names.sanitize(name);
        return openShiftClient.deploymentConfigs().withName(sName).get() != null;
    }

    @Override
    public void scale(String name, int desiredReplicas) {
        String sName = Names.sanitize(name);
        openShiftClient.deploymentConfigs().withName(sName).edit()
                       .editSpec()
                       .withReplicas(desiredReplicas)
                       .endSpec()
                       .done();
    }


    @Override
    public boolean isScaled(String name, int desiredReplicas) {
        String sName = Names.sanitize(name);
        DeploymentConfig dc = openShiftClient.deploymentConfigs().withName(sName).get();

        int allReplicas = 0;
        int readyReplicas = 0;
        if (dc != null && dc.getStatus() != null) {
            DeploymentConfigStatus status = dc.getStatus();
            allReplicas = nullSafe(status.getReplicas());
            readyReplicas = nullSafe(status.getReadyReplicas());
        }
        return desiredReplicas == allReplicas && desiredReplicas == readyReplicas;
    }

    @Override
    public List<DeploymentConfig> getDeploymentsByLabel(Map<String, String> labels) {
        return openShiftClient.deploymentConfigs().withLabels(labels).list().getItems();
    };

    private int nullSafe(Integer nr) {
        return nr != null ? nr : 0;
    }

//==================================================================================================

    private void ensureImageStreams(String name) {
        openShiftClient.imageStreams().withName(name).createOrReplaceWithNew()
                       .withNewMetadata().withName(name).endMetadata().done();
    }

    private boolean removeImageStreams(String name) {
        return openShiftClient.imageStreams().withName(name).delete();
    }

    private void ensureDeploymentConfig(String name, DeploymentData deploymentData, String serviceAccount) {
        openShiftClient.deploymentConfigs().withName(name).createOrReplaceWithNew()
            .withNewMetadata()
            .withName(name)
            .addToAnnotations(deploymentData.getAnnotations())
            .addToLabels(deploymentData.getLabels())
            .endMetadata()
            .withNewSpec()
            .withReplicas(1)
            .addToSelector("integration", name)
            .withNewTemplate()
            .withNewMetadata().addToLabels("integration", name).endMetadata()
            .withNewSpec()
            .withServiceAccount(serviceAccount)
            .withServiceAccountName(serviceAccount)
            .addNewContainer()
            .withImage(" ").withImagePullPolicy("Always").withName(name)
            .addNewPort().withName("jolokia").withContainerPort(8778).endPort()
            .addNewVolumeMount()
                .withName("secret-volume")
                .withMountPath("/deployments/config")
                .withReadOnly(false)
            .endVolumeMount()
            .endContainer()
            .addNewVolume()
                .withName("secret-volume")
                .withNewSecret()
                    .withSecretName(name)
                .endSecret()
            .endVolume()
            .endSpec()
            .endTemplate()
            .addNewTrigger().withType("ConfigChange").endTrigger()
            .addNewTrigger().withType("ImageChange")
            .withNewImageChangeParams()
            .withAutomatic(true).addToContainerNames(name)
            .withNewFrom().withKind("ImageStreamTag").withName(name + ":latest").endFrom()
            .endImageChangeParams()
            .endTrigger()
            .endSpec()
            .done();
    }


    private boolean removeDeploymentConfig(String projectName) {
        return openShiftClient.deploymentConfigs().withName(projectName).delete();
    }

    private void ensureBuildConfig(String name, DeploymentData deploymentData, String builderStreamTag) {
        openShiftClient.buildConfigs().withName(name).createOrReplaceWithNew()
            .withNewMetadata()
                .withName(name)
                .addToAnnotations(deploymentData.getAnnotations())
                .addToLabels(deploymentData.getLabels())
            .endMetadata()
            .withNewSpec()
            .withRunPolicy("SerialLatestOnly")
            .withNewSource().withType("Binary").endSource()
            .withNewStrategy()
              .withType("Source")
              .withNewSourceStrategy()
                .withNewFrom().withKind("ImageStreamTag").withName(builderStreamTag).endFrom()
                .withIncremental(true)
                .withEnv(new EnvVar("MAVEN_OPTS","-XX:+UseG1GC -XX:+UseStringDeduplication -Xmx500m", null))
              .endSourceStrategy()
            .endStrategy()
            .withNewOutput().withNewTo().withKind("ImageStreamTag").withName(name + ":latest").endTo().endOutput()
            .endSpec()
            .done();
    }

    private boolean removeBuildConfig(String projectName) {
        return openShiftClient.buildConfigs().withName(projectName).delete();
    }

    private void ensureSecret(String name, DeploymentData deploymentData) {
        openShiftClient.secrets().withName(name).createOrReplaceWithNew()
            .withNewMetadata()
                .withName(name)
                .addToAnnotations(deploymentData.getAnnotations())
                .addToLabels(deploymentData.getLabels())
            .endMetadata()
            .withStringData(deploymentData.getSecret())
            .done();
    }


    private boolean removeSecret(String projectName) {
       return openShiftClient.secrets().withName(projectName).delete();
    }

}
