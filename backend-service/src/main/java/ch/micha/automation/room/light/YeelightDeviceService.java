package ch.micha.automation.room.light;

import ch.micha.automation.room.errorhandling.exceptions.YeeLightOfflineException;
import com.moppletop.yeelight.api.YeeApi;
import com.moppletop.yeelight.api.model.YeeDuration;
import com.moppletop.yeelight.api.model.YeeLight;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class YeelightDeviceService {
    public static final int DEFAULT_CHANGE_DURATION = 500;

    protected final Logger logger = Logger.getLogger(getClass().getSimpleName());
    private final YeelightDeviceProvider provider;
    private final YeeApi yeeApi;

    @Inject
    public YeelightDeviceService(YeelightDeviceProvider provider) {
        this.provider = provider;
        yeeApi = provider.getYeeApi();
    }

    /**
     * applies a config to the given light
     * @param lightEntity the light to change
     * @param config the config to apply
     * @throws YeeLightOfflineException if the light is offline
     */
    public void applyConfigToLight(YeelightDeviceEntity lightEntity, LightConfigDTO config) {
        final int changeDuration = config.changeDurationMillis() == 0 ? DEFAULT_CHANGE_DURATION : config.changeDurationMillis();
        final YeeDuration duration = YeeDuration.millis(changeDuration);
        YeeLight light = lightEntity.light();
        if(light == null)
            throw new YeeLightOfflineException(lightEntity.id(), lightEntity.name());
        int lightId = lightEntity.light().getId();

        if(config.power()) {
            yeeApi.setBrightness(lightId, config.brightness(), duration);
            yeeApi.setRgb(lightId, config.red(), config.green(), config.blue(), duration);
        }

        yeeApi.setPower(lightId, config.power(), duration);
        logger.log(Level.INFO, "applied config to light: [ light:{0}-{1} | power:{2} | color:r-{3} g-{4} b-{5} | brightness:{6} ] }",
                new Object[]{ lightEntity.light().getLocation(), lightEntity.name(), config.power(), config.red(),
                        config.green(), config.blue(), config.brightness() });
    }

    public void powerAllOff() {
        List<YeelightDeviceEntity> onlineDevices = provider.getOnlineDevices();
        for (YeelightDeviceEntity device : onlineDevices) {
            yeeApi.setPower(device.id(), false, YeeDuration.millis(DEFAULT_CHANGE_DURATION));
        }
        logger.log(Level.INFO, "powered {0} online devices off", onlineDevices.size());
    }
}
