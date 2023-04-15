package ch.micha.automation.room.light.yeelight;

import ch.micha.automation.room.errorhandling.exceptions.UnexpectedYeeLightException;
import ch.micha.automation.room.errorhandling.exceptions.YeeLightOfflineException;
import ch.micha.automation.room.events.EventHandlerPriority;
import ch.micha.automation.room.events.HandlerPriority;
import ch.micha.automation.room.events.OnAppStartupListener;
import ch.micha.automation.room.light.configuration.LightConfig;
import ch.micha.automation.room.light.yeelight.dtos.YeelightDeviceDTO;
import ch.micha.automation.room.scene.SceneService;
import com.mollin.yapi.YeelightDevice;
import com.mollin.yapi.enumeration.YeelightEffect;
import com.mollin.yapi.enumeration.YeelightProperty;
import com.mollin.yapi.exception.YeelightResultErrorException;
import com.mollin.yapi.exception.YeelightSocketException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class YeelightDeviceService implements OnAppStartupListener {

    protected final Logger logger = Logger.getLogger(getClass().getSimpleName());
    private final YeelightDeviceProvider provider;
    private final SceneService sceneService;

    @Inject
    public YeelightDeviceService(YeelightDeviceProvider provider, SceneService sceneService) {
        this.provider = provider;
        this.sceneService = sceneService;
    }

    @Override
    @EventHandlerPriority(HandlerPriority.NOT_APPLICABLE)
    public void onAppStartup() {
        loadYeelightDevices();
    }

    /**
     * applies a config to the given light
     * @param lightEntity the light to change
     * @param config the config to apply
     * @throws YeeLightOfflineException if the light is offline
     */
    public void applyConfigToLight(YeelightDeviceEntity lightEntity, LightConfig config) {
        YeelightDevice light = lightEntity.light();
        if(light == null)
            throw new YeeLightOfflineException(lightEntity.ip(), lightEntity.name());

        try {
            light.setPower(true);
            light.setBrightness(config.brightness());
            light.setRGB(config.red(), config.green(), config.blue());

            logger.log(Level.INFO, "applied config to light: [ light:{0}-{1} | color:r-{2} g-{3} b-{4} | brightness:{5} ] }",
                    new Object[]{ lightEntity.ip(), lightEntity.name(), config.red(),
                            config.green(), config.blue(), config.brightness() });
        } catch (YeelightSocketException e) {
            throw new YeeLightOfflineException(lightEntity.ip(), lightEntity.name());
        } catch (YeelightResultErrorException e) {
            throw new UnexpectedYeeLightException(lightEntity.ip(), e);
        }
    }

    public void togglePower(String name) {
        YeelightDeviceEntity entity = provider.findYeelightDevice(name);
        YeelightDevice device = entity.light();
        if(device == null)
            throw new YeeLightOfflineException(entity.ip(), entity.name());

        try {
            boolean on = isDeviceOn(device);
            device.setPower(!on);
            logger.log(Level.INFO, "toggled power of {0} to {1}", new Object[]{name, !on});
        } catch (YeelightResultErrorException | YeelightSocketException e) {
            throw new UnexpectedYeeLightException(entity.ip(), e);
        }

    }

    public void powerAllOff() {
        List<YeelightDeviceEntity> onlineDevices = provider.getOnlineDevices();
        for (YeelightDeviceEntity device : onlineDevices) {
            try {
                device.light().setPower(false);
            } catch (YeelightSocketException e) {
                throw new YeeLightOfflineException(device.ip(), device.name());
            } catch (YeelightResultErrorException e) {
                throw new UnexpectedYeeLightException(device.ip(), e);
            }
        }
        logger.log(Level.INFO, "powered {0} online devices off", onlineDevices.size());
    }

    public List<YeelightDeviceDTO> getAllDevices() {
        List<YeelightDeviceEntity> entities = new ArrayList<>(provider.getDevices());
        return entities.stream().map(e -> new YeelightDeviceDTO(e.name(), e.ip(), e.isOnline())).toList();
    }

    public YeelightDeviceDTO addNewDevice(String name, String ip) {
        YeelightDeviceEntity createdDevice = provider.saveNewEntity(new YeelightDeviceEntity(0, name, ip, null));
        YeelightDeviceEntity connectedDevice = new YeelightDeviceEntity(
                createdDevice.id(),
                createdDevice.name(),
                createdDevice.ip(),
                tryToConnect(createdDevice.ip())
        );

        provider.putDevice(connectedDevice); // if the light has been updated it also needs to be updated in the provider
        sceneService.addDeviceToDefaultScene(connectedDevice.id());
        return new YeelightDeviceDTO(connectedDevice.name(), connectedDevice.ip(), connectedDevice.isOnline());
    }

    public void renameDevice(String oldName, String newName) {
        provider.updateDeviceName(oldName, newName);
    }

    public void removeDevice(String name) {
        provider.deleteDevice(name);
    }

    private void loadYeelightDevices() {
        List<YeelightDeviceEntity> storedDevices = provider.loadYeelightDeviceEntities();
        Map<Integer, YeelightDeviceEntity> devices = new HashMap<>();

        for (YeelightDeviceEntity storedDevice : storedDevices) {
            devices.put(storedDevice.id(), new YeelightDeviceEntity(
                    storedDevice.id(),
                    storedDevice.name(),
                    storedDevice.ip(),
                    tryToConnect(storedDevice.ip())
            ));
        }

        provider.setDevices(devices);
        logger.log(Level.INFO, "connected to {0} devices & found {1} offline devices",
                new Object[]{provider.getOnlineDevices().size(), provider.getOfflineDevices().size()});
    }

    private YeelightDevice tryToConnect(String ip) {
        try {
            YeelightDevice device = new YeelightDevice(
                    ip,
                    YeelightDeviceEntity.DEFAULT_PORT,
                    YeelightEffect.SMOOTH,
                    YeelightDeviceEntity.DEFAULT_DURATION);
            logger.log(Level.INFO, "successfully connected to device at {0}", ip);
            return device;
        } catch (YeelightSocketException e) {
            logger.log(Level.INFO, "no connection available for device at {0}", ip);
            return null;
        }
    }

    private boolean isDeviceOn(YeelightDevice device) throws YeelightSocketException, YeelightResultErrorException {
        return "on".equals(device.getProperties(YeelightProperty.POWER).get(YeelightProperty.POWER));
    }
}
