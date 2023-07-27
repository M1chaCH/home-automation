package ch.micha.automation.room.light.yeelight;

import ch.micha.automation.room.errorhandling.exceptions.UnexpectedYeeLightException;
import ch.micha.automation.room.errorhandling.exceptions.YeeLightOfflineException;
import ch.micha.automation.room.events.EventHandlerPriority;
import ch.micha.automation.room.events.HandlerPriority;
import ch.micha.automation.room.events.OnAppStartupListener;
import ch.micha.automation.room.light.configuration.LightConfig;
import ch.micha.automation.room.light.yeelight.dtos.DeviceWithStateDTO;
import ch.micha.automation.room.light.yeelight.dtos.YeelightDeviceDTO;
import ch.micha.automation.room.light.yeelight.interceptor.RequireLightConnection;
import ch.micha.automation.room.scene.SceneService;
import com.mollin.yapi.YeelightDevice;
import com.mollin.yapi.enumeration.YeelightEffect;
import com.mollin.yapi.enumeration.YeelightProperty;
import com.mollin.yapi.exception.YeelightResultErrorException;
import com.mollin.yapi.exception.YeelightSocketException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.awt.*;
import java.time.Instant;
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
    private final int deviceConnectionLifetime;

    private Instant devicesLoaded;


    @Inject
    public YeelightDeviceService(YeelightDeviceProvider provider, SceneService sceneService,
                                 @ConfigProperty(name = "room.automation.device.connection.lifetime", defaultValue = "60")
                                 int deviceConnectionLifetimeMin) {
        this.provider = provider;
        this.sceneService = sceneService;
        this.deviceConnectionLifetime = deviceConnectionLifetimeMin > 0 ? deviceConnectionLifetimeMin : 60;
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
    @RequireLightConnection
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

    /**
     * loads the device by its name, checks the power state and toggles it
     * @param name the device name to use
     * @return the config of the light (if powered on, else if powered of returns "null")
     */
    @RequireLightConnection
    public LightConfig togglePower(String name) {
        YeelightDeviceEntity entity = provider.findYeelightDevice(name);
        YeelightDevice device = entity.light();
        if(device == null)
            throw new YeeLightOfflineException(entity.ip(), entity.name());

        try {
            boolean on = isDeviceOn(device);
            device.setPower(!on);
            logger.log(Level.INFO, "toggled power of {0} to {1}", new Object[]{name, !on});
            return !on ? loadConfig(entity) : null;
        } catch (YeelightResultErrorException | YeelightSocketException e) {
            throw new UnexpectedYeeLightException(entity.ip(), e);
        }
    }

    @RequireLightConnection
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

    /**
     * @return a map of the online devices with their current configs
     */
    @RequireLightConnection
    public Map<YeelightDeviceEntity, LightConfig> loadCurrentOnlineConfigs() {
        Map<YeelightDeviceEntity, LightConfig> lights = new HashMap<>();

        provider.getOnlineDevices().forEach(d -> lights.put(d, loadConfig(d)));

        return lights;
    }

    public List<YeelightDeviceDTO> getAllDevices() {
        List<YeelightDeviceEntity> entities = new ArrayList<>(provider.getDevices());
        return entities.stream().map(e -> new YeelightDeviceDTO(e.name(), e.ip(), e.isOnline())).toList();
    }

    /**
     * loads all devices and additionally loads the state of the online devices
     * @return all devices and if they are online, then their state is included
     */
    @RequireLightConnection
    public List<DeviceWithStateDTO> getAllDevicesWithState() {
        Map<YeelightDeviceEntity, LightConfig> onlineDevices = loadCurrentOnlineConfigs();
        List<YeelightDeviceEntity> allDevices = new ArrayList<>(provider.getDevices());
        List<DeviceWithStateDTO> response = new ArrayList<>();

        for (YeelightDeviceEntity device : allDevices) {
            LightConfig config = onlineDevices.get(device);
            response.add(new DeviceWithStateDTO(
                    device.name(),
                    device.ip(),
                    device.isOnline(),
                    config
            ));
        }

        return response;
    }

    /**
     * add a new device to the DB and the default scene
     * @param name of the new device
     * @param ip of the new device
     * @return the created device DTO
     */
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

    /**
     * loads all device from the db and tries to connect to all of them
     * (also resets the device cache in the provider)
     * @return the loaded devices
     */
    public Map<Integer, YeelightDeviceEntity> loadYeelightDevices() {
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
        devicesLoaded = Instant.now();
        logger.log(Level.INFO, "connected to {0} devices & found {1} offline devices",
                new Object[]{provider.getOnlineDevices().size(), provider.getOfflineDevices().size()});
        return devices;
    }

    public boolean isDeviceConnectionExpired() {
        return devicesLoaded.plusSeconds(deviceConnectionLifetime * 60L).isBefore(Instant.now());
    }

    /**
     * tries to connect to a yeelight device (throws nothing)
     * @param ip to connect to
     * @return the connection YeelightDevice or null if connection failed
     */
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

    /**
     * @param device the device to load from
     * @return the current config, if device is powered of null is returned
     */
    @RequireLightConnection
    private LightConfig loadConfig(YeelightDeviceEntity device) {
        try {
            Map<YeelightProperty, String> properties = device.light()
                    .getProperties(YeelightProperty.POWER, YeelightProperty.BRIGHTNESS, YeelightProperty.RGB);

            int brightness = Integer.parseInt(properties.get(YeelightProperty.BRIGHTNESS));
            Color color = new Color(Integer.parseInt(properties.get(YeelightProperty.RGB)));

            if("on".equals(properties.get(YeelightProperty.POWER))) {
                logger.log(Level.INFO, "loaded config of online device {0}", device.name());
                return new LightConfig(
                        -1,
                        "generated",
                        color.getRed(),
                        color.getGreen(),
                        color.getBlue(),
                        brightness
                );
            }

            logger.log(Level.INFO, "device {0} is powered of -> did not load state", device.name());
            return null;
        } catch (YeelightResultErrorException | NullPointerException | NumberFormatException e) {
            logger.log(Level.WARNING, "could not load properties of light at {0}: {1}",
                    new Object[]{ device.ip(), e.getMessage() });
            return null;
        } catch (YeelightSocketException e) {
            throw new UnexpectedYeeLightException(device.ip(), e);
        }
    }

    private boolean isDeviceOn(YeelightDevice device) throws YeelightSocketException, YeelightResultErrorException {
        return "on".equals(device.getProperties(YeelightProperty.POWER).get(YeelightProperty.POWER));
    }
}
