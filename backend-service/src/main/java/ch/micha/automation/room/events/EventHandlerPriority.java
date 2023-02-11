package ch.micha.automation.room.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Lets the EventHandler know at what priority this listener should be handled. Can be used to order the listeners for
 * one event. Needs to be put above the handlers Method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandlerPriority {
    HandlerPriority value() default HandlerPriority.NOT_APPLICABLE;
}
