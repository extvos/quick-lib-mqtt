package plus.extvos.mqtt.annotation;

import org.springframework.core.annotation.AliasFor;

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

    @AliasFor("name")
    String value();

    /**
     * Parameter name.
     *
     * @return Parameter name.
     */
    String name();

    /**
     * if required is true and value is null, method does not execute.
     *
     * @return boolean
     */
    boolean required() default false;
}
