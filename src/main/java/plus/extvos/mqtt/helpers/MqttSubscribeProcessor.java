package plus.extvos.mqtt.helpers;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import plus.extvos.mqtt.annotation.TopicSubscribe;
import plus.extvos.mqtt.subscribe.MqttSubscriber;

import java.lang.reflect.Method;
import java.util.LinkedList;

/**
 * When Bean is initialized, filter out the methods annotated with @TopicSubscribe, and create MqttSubscriber
 *
 * @author tocrhz
 * @see TopicSubscribe
 * @see MqttSubscriber
 */
@Component
public class MqttSubscribeProcessor implements BeanPostProcessor {

    // subscriber cache
    public static final LinkedList<MqttSubscriber> SUBSCRIBERS = new LinkedList<>();

    @Value("${spring.mqtt.disable:false}")
    private Boolean disable;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (disable == null || !disable) {
            Method[] methods = bean.getClass().getMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(TopicSubscribe.class)) {
                    SUBSCRIBERS.add(MqttSubscriber.of(bean, method));
                }
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
