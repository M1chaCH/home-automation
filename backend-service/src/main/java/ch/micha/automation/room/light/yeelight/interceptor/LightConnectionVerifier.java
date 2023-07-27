package ch.micha.automation.room.light.yeelight.interceptor;

import ch.micha.automation.room.light.yeelight.YeelightDeviceEntity;
import ch.micha.automation.room.light.yeelight.YeelightDeviceService;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.util.List;
import java.util.Optional;
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
            List<YeelightDeviceEntity> devices = deviceService.loadYeelightDevices()
                .values()
                .stream()
                .toList();
            // most certainly,
            // the underlying method won't load the device again, so we need
            // to update its device argument to a device that is actually connected
            updateDeviceParameter(invocationContext, devices);
        }

        return invocationContext.proceed();
    }

    /**
     * updates the device parameter if one exists.
     * DOES NOT SUPPORT PARAMETER LISTS
     * @param invocationContext the context to be updated
     * @param devices the newly connected devices
     */
    private void updateDeviceParameter(InvocationContext invocationContext, List<YeelightDeviceEntity> devices) {
        Object[] parameters = invocationContext.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if(parameters[i] instanceof YeelightDeviceEntity deviceParameter) {
                Optional<YeelightDeviceEntity> newDevice = devices
                    .stream()
                    .filter(device -> device.ip().equals(deviceParameter.ip()))
                    .findAny();
                if(newDevice.isPresent()) {
                    parameters[i] = newDevice.get();
                    logger.log(Level.INFO, "updated device parameter for {0} with connected device {1}",
                        new Object[]{ invocationContext.getMethod().getName(), deviceParameter.ip() });
                }
            }
        }
    }
}
