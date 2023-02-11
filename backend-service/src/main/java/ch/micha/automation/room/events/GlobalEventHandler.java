package ch.micha.automation.room.events;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.BeforeDestroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Listens for application events and calls the custom listeners.
 * Executes the handler methods of the listener classes with respect to their annotated priority.
 */
@ApplicationScoped
public class GlobalEventHandler {
    private final Logger logger = Logger.getLogger(getClass().getSimpleName());

    private final Instance<OnAppStartupListener> onAppStartupListeners;
    private final Instance<OnAppShutdownListener> onAppShutdownListeners;

    @Inject
    public GlobalEventHandler(Instance<OnAppStartupListener> onAppStartupListeners,
                              Instance<OnAppShutdownListener> onAppShutdownListeners) {
        this.onAppStartupListeners = onAppStartupListeners;
        this.onAppShutdownListeners = onAppShutdownListeners;
    }

    public void onAppStartup(@Observes @Initialized(ApplicationScoped.class) Object init) {
        logger.log(Level.INFO, "calling application startup event");

        onAppStartupListeners
                .stream()
                .sorted(Comparator.comparingInt(l -> getHandlerPriority(l.getClass(), "onAppStartup")))
                .forEachOrdered(listener -> {
                    logger.log(Level.INFO, "running startup listener of {0}", listener.getClass().getSimpleName());
                    listener.onAppStartup();
                });

        logger.log(Level.INFO, "executed all startup listeners");
    }

    public void onAppShutdown(@Observes @BeforeDestroyed(ApplicationScoped.class) Object destroy) {
        logger.log(Level.INFO, "calling application shutdown event");

        onAppShutdownListeners
                .stream()
                .sorted(Comparator.comparingInt(l -> getHandlerPriority(l.getClass(), "onAppShutdown")))
                .forEachOrdered(listener -> {
                    logger.log(Level.INFO, "running shutdown listener of {0}", listener.getClass().getSimpleName());
                    listener.onAppShutdown();
                });

        logger.log(Level.INFO, "executed all shutdown listeners");
    }

    private int getHandlerPriority(Class<?> clazz, String methodName) {
        try {
            EventHandlerPriority annotation = clazz.getMethod(methodName).getAnnotation(EventHandlerPriority.class);
            if(annotation == null)
                return 0;

            return annotation.value().getPriority();
        } catch (NoSuchMethodException | NullPointerException e) {
            logger.log(Level.SEVERE, "unexpected exception during event handling annotation parsing | class {0} | method {1}",
                    new String[]{ clazz.getName(), methodName });
            e.printStackTrace();
        }

        return 0;
    }
}
