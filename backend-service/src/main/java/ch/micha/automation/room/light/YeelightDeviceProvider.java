package ch.micha.automation.room.light;

import ch.micha.automation.room.events.OnAppStartupListener;
import ch.micha.automation.room.exceptions.UnexpectedSqlException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class YeelightDeviceProvider implements OnAppStartupListener {
    private final Logger logger = Logger.getLogger(getClass().getSimpleName());
    private final SQLService sqlService;
    private final UnknownNameGenerator nameGenerator = new UnknownNameGenerator("device");

    private List<YeelightDeviceEntity> devices;

    @Inject
    public YeelightDeviceProvider(SQLService sqlService) {
        this.sqlService = sqlService;
    }

    @Override
    public void onAppStartup() {
        final YeeApi api = new YeeApiBuilder().build();
        this.devices = mapEntitiesWithDevices(api.getLights().stream().toList());

        logger.log(Level.INFO, "connected to {0} devices & found {1} offline devices",
                new Integer[]{getOnlineDevices().size(), getOfflineDevices().size()});
    }

    public List<YeelightDeviceEntity> getDevices() {
        return devices;
    }

    /**
     * @return all online devices as <strong>immutable</strong> list
     */
    public List<YeelightDeviceEntity> getOnlineDevices() {
        return devices.stream().filter(YeelightDeviceEntity::isOnline).toList();
    }


    /**
     * @return all offline devices as <strong>immutable</strong> list
     */
    public List<YeelightDeviceEntity> getOfflineDevices() {
        return devices.stream().filter(d -> !d.isOnline()).toList();
    }

    private List<YeelightDeviceEntity> mapEntitiesWithDevices(List<YeeLight> devices) {
        final List<YeelightDeviceEntity> storedDevices = getEntitiesFromDB();

        for (YeeLight onlineDevice : devices) {
            Optional<YeelightDeviceEntity> storedDevice = storedDevices.stream()
                    .filter(device -> device.getId().equals(onlineDevice.getLocation()))
                    .findAny();

            if(storedDevice.isPresent()) {
                storedDevice.get().setLight(onlineDevice);
            } else {
                YeelightDeviceEntity createdDevice = saveNewEntity(onlineDevice.getLocation(), nameGenerator.nextString());
                createdDevice.setLight(onlineDevice);
                storedDevices.add(createdDevice);
            }
        }

        return storedDevices;
    }

    private List<YeelightDeviceEntity> getEntitiesFromDB() {
        final List<YeelightDeviceEntity> entities = new ArrayList<>();
        try(PreparedStatement statement = sqlService.getConnection().prepareStatement("select * from yeelight_devices")){
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                YeelightDeviceEntity entity = new YeelightDeviceEntity();
                entity.setId(result.getString("id"));
                entity.setName(result.getString("name"));
                entities.add(entity);
            }

        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }

        logger.log(Level.FINE, "selected {0} yeelight_devices", entities.size());
        return entities;
    }

    private YeelightDeviceEntity saveNewEntity(String id, String name) {
        YeelightDeviceEntity entity = new YeelightDeviceEntity();
        entity.setId(id);
        entity.setName(name);

        try (PreparedStatement statement = sqlService.getConnection().prepareStatement(
                "insert into yeelight_devices values (?, ?)"
        )) {
            statement.setString(1, entity.getId());
            statement.setString(2, entity.getName());
            statement.execute();
            logger.log(Level.FINE, "inserted into yeelight_devices: {0}", entity);
            return entity;
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }
}
