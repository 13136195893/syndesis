/**
 * Copyright (C) 2017 Red Hat, Inc.
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
package io.syndesis.connector.salesforce;

import java.net.URISyntaxException;
import java.util.Map;

import org.apache.camel.component.connector.DefaultConnectorComponent;
import org.apache.camel.component.salesforce.SalesforceEndpointConfig;

public class SalesforceOnUpdateComponent extends DefaultConnectorComponent {

    public SalesforceOnUpdateComponent() {
        super("salesforce-on-update", SalesforceOnUpdateComponent.class.getName());
    }

    @Override
    public String createEndpointUri(final String scheme, final Map<String, String> options) throws URISyntaxException {
        final String sObjectName = options.get(SalesforceEndpointConfig.SOBJECT_NAME);

        final String query = "SELECT Id FROM " + sObjectName;
        options.put("topicName", topicNameFor(options));
        options.put(SalesforceEndpointConfig.SOBJECT_QUERY, query);
        options.remove(SalesforceEndpointConfig.SOBJECT_NAME);

        return super.createEndpointUri(scheme, options);
    }

    private static String topicNameFor(final Map<String, String> options) {
        final String sObjectName = options.get(SalesforceEndpointConfig.SOBJECT_NAME);

        return "syndesis_" + sObjectName + "_update";
    }
}
