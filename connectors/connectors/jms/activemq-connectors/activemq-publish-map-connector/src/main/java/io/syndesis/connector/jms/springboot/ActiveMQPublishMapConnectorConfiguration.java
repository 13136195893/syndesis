package io.syndesis.connector.jms.springboot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Generated("org.apache.camel.maven.connector.SpringBootAutoConfigurationMojo")
@ConfigurationProperties(prefix = "activemq-publish-map")
public class ActiveMQPublishMapConnectorConfiguration
        extends
            ActiveMQPublishMapConnectorConfigurationCommon {

    /**
     * Define additional configuration definitions
     */
    private Map<String, ActiveMQPublishMapConnectorConfigurationCommon> configurations = new HashMap<>();

    public Map<String, ActiveMQPublishMapConnectorConfigurationCommon> getConfigurations() {
        return configurations;
    }
}