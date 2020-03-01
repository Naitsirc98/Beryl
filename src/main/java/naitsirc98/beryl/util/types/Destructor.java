package naitsirc98.beryl.util.types;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Indicates that the type annotated with this annotation has a destructor that needs to be called whenever the object
 * is no longer needed
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Destructor {

    /**
     * Destructor method name
     *
     * @return the method name to be called to release this object
     */
    String method() default "free";

}
