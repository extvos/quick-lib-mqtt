package plus.extvos.mqtt.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author shenmc
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface TopicVariable {
    /**
     * Parameter name.
     *
     * @return Parameter name.
     */
    String value();

    /**
     * if required is true and value is null, method does not execute.
     *
     * @return boolean
     */
    boolean required() default false;
}
