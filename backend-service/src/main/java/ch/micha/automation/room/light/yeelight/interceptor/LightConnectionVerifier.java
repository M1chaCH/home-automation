package ch.micha.automation.room.light.yeelight.interceptor;

import ch.micha.automation.room.light.yeelight.YeelightDeviceService;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.util.logging.Level;
import java.util.logging.Logger;

@RequireLightConnection
@Interceptor
public class LightConnectionVerifier {
    private final Logger logger = Logger.getLogger(getClass().getSimpleName());

    private final YeelightDeviceService deviceService;

    @Inject
    public LightConnectionVerifier(YeelightDeviceService deviceService) {
        this.deviceService = deviceService;
    }

    /**
     * executed before all methods annotated with @RequireLightConnection
     * checks if the device connection is expired, if so reloads all devices
     * @param invocationContext context with information about the invocation
     * @return the return value of the annotated method
     * @throws Exception the exceptions to the annotated method
     */
    @AroundInvoke
    public Object intercept(InvocationContext invocationContext) throws Exception {
        if(deviceService.isDeviceConnectionExpired()) {
            logger.log(Level.INFO, "light connection expired");
            deviceService.loadYeelightDevices();
        }

        return invocationContext.proceed();
    }
}
