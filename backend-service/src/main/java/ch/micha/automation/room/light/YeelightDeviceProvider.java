package ch.micha.automation.room.light;

import ch.micha.automation.room.errorhandling.exceptions.ResourceNotFoundException;
import ch.micha.automation.room.events.OnAppStartupListener;
import ch.micha.automation.room.errorhandling.exceptions.UnexpectedSqlException;
import ch.micha.automation.room.sql.SQLService;
import ch.micha.automation.room.sql.UnknownNameGenerator;
import com.moppletop.yeelight.api.YeeApi;
import com.moppletop.yeelight.api.YeeApiBuilder;
import com.moppletop.yeelight.api.model.YeeLight;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * fetches stored devices from the DB and provides them combined with the online found Yeelight devices.
 */
@ApplicationScoped
public class YeelightDeviceProvider implements OnAppStartupListener {
    private final Logger logger = Logger.getLogger(getClass().getSimpleName());
    private final SQLService sqlService;
    private final UnknownNameGenerator nameGenerator = new UnknownNameGenerator("device");

    private YeeApi yeeApi;
    private Map<Integer, YeelightDeviceEntity> devices;

    @Inject
    public YeelightDeviceProvider(SQLService sqlService) {
        this.sqlService = sqlService;
    }

    @Override
    public void onAppStartup() {
        yeeApi = new YeeApiBuilder().build();
        this.devices = loadYeelightDeviceEntities(new ArrayList<>(yeeApi.getLights().stream().toList()));

        logger.log(Level.INFO, "connected to {0} devices & found {1} offline devices",
                new Object[]{getOnlineDevices().size(), getOfflineDevices().size()});
    }

    public Collection<YeelightDeviceEntity> getDevices() {
        return devices.values();
    }

    /**
     * @return all online devices as <strong>immutable</strong> list
     */
    public List<YeelightDeviceEntity> getOnlineDevices() {
        return getDevices().stream().filter(YeelightDeviceEntity::isOnline).toList();
    }


    /**
     * @return all offline devices as <strong>immutable</strong> list
     */
    public List<YeelightDeviceEntity> getOfflineDevices() {
        return getDevices().stream().filter(d -> !d.isOnline()).toList();
    }

    /**
     * (only gets devices from RAM, no DB access)
     * @param deviceIds ids of devices to return.
     * @return all found devices by ids, if not found -> null entry in the list.
     */
    public List<YeelightDeviceEntity> findByIds(Integer... deviceIds) {
        List<YeelightDeviceEntity> foundDevices = new ArrayList<>();

        for (int deviceId : deviceIds) {
            foundDevices.add(devices.get(deviceId));
        }

        return foundDevices;
    }

    public YeeApi getYeeApi() {
        return yeeApi;
    }

    /**
     * (only gets devices from RAM, no DB access)
     * @param id the id to query for
     * @return the locally stored YeelightDeviceEntity by the given id
     * @throws ResourceNotFoundException if the id could not be found
     */
    public YeelightDeviceEntity findYeelightDevice(int id) {
        YeelightDeviceEntity device = devices.get(id);
        if(device == null)
            throw new ResourceNotFoundException("device", "" + id);

        return device;
    }

    /**
     * fetches the YeelightDeviceEntities stored in the DB and combines them with the given lights.
     * If given Light could not be paired with a stored entity, new entity is stored with generated name.
     * if entity is found but could not be mapped to a light, entity will be returned but as offline.
     * @param onlineLights the YeeLights that were found online
     * @return a map with YeelightDeviceEntities mapped with their ID.
     */
    private Map<Integer, YeelightDeviceEntity> loadYeelightDeviceEntities(List<YeeLight> onlineLights) {
        final Map<Integer, YeelightDeviceEntity> entities = new HashMap<>();

        // loads all lights from the db and maps them to an online light.
        // if no online light with the same id is found then null is mapped
        try(PreparedStatement statement = sqlService.getConnection().prepareStatement("select * from yeelight_devices")){
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                final int id = result.getInt("id");
                final YeeLight onlineLight = onlineLights.stream().filter(light -> light.getId() == id).findAny().orElse(null);

                YeelightDeviceEntity entity = new YeelightDeviceEntity(
                        result.getInt("id"),
                        result.getString("name"),
                        onlineLight
                );
                entities.put(entity.id(), entity);

                if(onlineLight != null)
                    onlineLights.remove(onlineLight); // to let the next step know that this is not a new light.
            }

            // save all remaining online lights to the db
            for (YeeLight onlineLight : onlineLights) {
                YeelightDeviceEntity entity = new YeelightDeviceEntity(onlineLight.getId(), nameGenerator.nextString(), onlineLight);
                saveNewEntity(entity);
                entities.put(entity.id(), entity);
            }
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }

        logger.log(Level.INFO, "selected {0} yeelight_devices", entities.size());
        return entities;
    }

    private void saveNewEntity(YeelightDeviceEntity entity) {

        try (PreparedStatement statement = sqlService.getConnection().prepareStatement(
                "insert into yeelight_devices values (?, ?)"
        )) {
            statement.setInt(1, entity.id());
            statement.setString(2, entity.name());
            statement.execute();
            logger.log(Level.INFO, "inserted into yeelight_devices: {0}", entity);
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }
}
