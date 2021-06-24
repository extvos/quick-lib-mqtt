package plus.extvos.mqtt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author shenmc
 */
@Configuration
public class MqttConfig {
    @Value("${spring.mqtt.qos:1}")
    private int qos;

    @Value("${spring.mqtt.broker}")
    private String broker;

    @Value("${spring.mqtt.client-id}")
    private String clientId;

    @Value("${spring.mqtt.username}")
    private String username;

    @Value("${spring.mqtt.password}")
    private String password;

    @Value("${spring.mqtt.keep-alive-interval:3000}")
    private int keepAliveInterval;

    @Value("${spring.mqtt.clean-session:false}")
    private boolean cleanSession;

    @Value("${spring.mqtt.auto-reconnect:true}")
    private boolean autoReconnect;

    @Value("${spring.mqtt.completion-timeout:30000}")
    private int completionTimeout;
}
