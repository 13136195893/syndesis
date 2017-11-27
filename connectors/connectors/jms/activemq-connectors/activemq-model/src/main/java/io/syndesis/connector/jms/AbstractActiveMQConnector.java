/*
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
package io.syndesis.connector.jms;

import java.net.URISyntaxException;
import java.util.Map;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.component.connector.DefaultConnectorComponent;
import org.apache.camel.component.sjms2.Sjms2Component;
import org.apache.camel.spi.Metadata;
import org.apache.camel.util.ObjectHelper;

/**
 * Base class for ActiveMQ connectors
 */
public abstract class AbstractActiveMQConnector extends DefaultConnectorComponent {

    @Metadata(label = "basic", description = "ActiveMQ Broker URL")
    private String brokerUrl;

    @Metadata(label = "basic", description = "User name for authorization credential")
    private String username;

    @Metadata(label = "basic", description = "Password for authorization credential")
    private String password;

    public AbstractActiveMQConnector(String componentName, String componentSchema, String className) {
        super(componentName, componentSchema, className);
    }

    @Override
    public String createEndpointUri(String scheme, Map<String, String> options) throws URISyntaxException {

        // validate url
        if (ObjectHelper.isEmpty(this.brokerUrl)) {
            throw new IllegalArgumentException("Missing required property brokerUrl");
        }

        // create ActiveMQ Connection Factory
        ActiveMQConnectionFactory connectionFactory = ObjectHelper.isEmpty(username) ?
                new ActiveMQConnectionFactory(this.brokerUrl) : new ActiveMQConnectionFactory(username, password, this.brokerUrl);
        Sjms2Component delegate = getCamelContext().getComponent(getComponentName() + "-component", Sjms2Component.class);
        delegate.setConnectionFactory(connectionFactory);

        return super.createEndpointUri(scheme, options);
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    /**
     * ActiveMQ Broker URL.
     */
    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public String getUsername() {
        return username;
    }

    /**
     * ActiveMQ Broker username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    /**
     * ActiveMQ Broker password.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
