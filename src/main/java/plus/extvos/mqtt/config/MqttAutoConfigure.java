package plus.extvos.mqtt.config;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import plus.extvos.mqtt.helpers.MqttAsyncClientAdapter;
import plus.extvos.mqtt.helpers.MqttConnectOptionsAdapter;
import plus.extvos.mqtt.helpers.MqttConnector;
import plus.extvos.mqtt.helpers.MqttConversionService;
import plus.extvos.mqtt.properties.MqttProperties;
import plus.extvos.mqtt.publish.MqttPublisher;

/**
 * mqtt auto configuration
 *
 * @author tocrhz
 */
@Order(1010)
@AutoConfigureAfter(PayloadJacksonAutoConfigure.class)
@ConditionalOnClass(MqttAsyncClient.class)
@ConditionalOnProperty(prefix = "spring.mqtt", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(MqttProperties.class)
@Configuration
public class MqttAutoConfigure {

    private static final Logger log = LoggerFactory.getLogger(MqttAutoConfigure.class);

    public MqttAutoConfigure(ListableBeanFactory beanFactory) {
        // register converters
        MqttConversionService.addBeans(MqttConversionService.getSharedInstance(), beanFactory);
    }

    /**
     * default MqttConnectOptionsAdapter, nothing to do.
     *
     * @return MqttConnectOptionsAdapter
     */
    @Bean
    @Order(1010)
    @ConditionalOnMissingBean(MqttConnectOptionsAdapter.class)
    public MqttConnectOptionsAdapter mqttConnectOptionsAdapter() {
        log.debug("mqttConnectOptionsAdapter:> ...");
        return (clientId, options) -> {
            log.debug("mqttConnectOptionsAdapter:> ... {}, {}", clientId,options);
        };
    }


    /**
     * default MqttClientAdapter
     *
     * @return MqttAsyncClientAdapter
     */
    @Bean
    @Order(1010)
    @ConditionalOnMissingBean(MqttAsyncClientAdapter.class)
    public MqttAsyncClientAdapter mqttAsyncClientAdapter() {
        log.debug("mqttAsyncClientAdapter:> ...");
        return (clientId, serverURI) -> {
            log.debug("mqttAsyncClientAdapter:> ...{}, {}", clientId, serverURI);
            return new MqttAsyncClient(serverURI[0], clientId, new MemoryPersistence());
        };
    }

    /**
     * default MqttPublisher
     *
     * @return MqttPublisher
     */
    @Bean
    @Order(1013)
    @ConditionalOnMissingBean(MqttPublisher.class)
    public MqttPublisher mqttPublisher() {
        log.debug("mqttPublisher:> ...");
        return new MqttPublisher();
    }

    /**
     * default MqttConnector.
     * <p>
     * Ensure the final initialization, the order is {@link org.springframework.core.Ordered#LOWEST_PRECEDENCE}
     *
     * @param adapter       MqttConnectOptionsAdapter
     * @param properties    MqttProperties
     * @param clientAdapter MqttAsyncClientAdapter
     * @return MqttConnector
     */
    @Bean
    @Order // Ordered.LOWEST_PRECEDENCE
    public MqttConnector mqttConnector(MqttAsyncClientAdapter clientAdapter, MqttProperties properties, MqttConnectOptionsAdapter adapter) {
        log.debug("mqttConnector:> ... {}, {}, {}", clientAdapter, properties, adapter);
        MqttConnector connector = new MqttConnector();
        connector.start(clientAdapter, properties, adapter);
        return connector;
    }
}
