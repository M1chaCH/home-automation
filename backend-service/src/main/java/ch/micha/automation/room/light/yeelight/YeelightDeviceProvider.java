package ch.micha.automation.room.light.yeelight;

import ch.micha.automation.room.errorhandling.exceptions.ResourceAlreadyExistsException;
import ch.micha.automation.room.errorhandling.exceptions.ResourceNotFoundException;
import ch.micha.automation.room.errorhandling.exceptions.UnexpectedSqlException;
import ch.micha.automation.room.sql.SQLService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.postgresql.util.PSQLException;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * fetches stored devices from the DB and provides them combined with the online found Yeelight devices.
 */
@ApplicationScoped
public class YeelightDeviceProvider {
    private final Logger logger = Logger.getLogger(getClass().getSimpleName());
    private final SQLService sqlService;

    private Map<Integer, YeelightDeviceEntity> devices;

    @Inject
    public YeelightDeviceProvider(SQLService sqlService) {
        this.sqlService = sqlService;
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
     * @throws NullPointerException if one of the IDs was not found
     */
    public List<YeelightDeviceEntity> findByIds(Integer... deviceIds) {
        List<YeelightDeviceEntity> foundDevices = new ArrayList<>();

        for (int deviceId : deviceIds) {
            foundDevices.add(devices.get(deviceId));
        }

        return foundDevices;
    }

    /**
     * (only gets devices from RAM, no DB access)
     * @param name the device name to query for
     * @return the locally stored YeelightDeviceEntity by the given id
     * @throws ResourceNotFoundException if the id could not be found
     */
    public YeelightDeviceEntity findYeelightDevice(String name) {
        for (Map.Entry<Integer, YeelightDeviceEntity> entry : devices.entrySet()) {
            if(entry.getValue().name().toLowerCase(Locale.ROOT).equals(name.toLowerCase(Locale.ROOT)))
                return entry.getValue();
        }

        throw new ResourceNotFoundException("device", name);
    }

    /**
     * selects all lights from the DB and tried to connect to them. Online AND offline devices will be returned
     * @return all devices stored in the DB.
     */
    public List<YeelightDeviceEntity> loadYeelightDeviceEntities() {
        final List<YeelightDeviceEntity> entities = new ArrayList<>();

        // loads all lights from the db and maps them to an online light.
        // if no online light with the same id is found then null is mapped
        try(PreparedStatement statement = sqlService.getConnection().prepareStatement("select * from yeelight_devices;")){
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                final String ip = result.getString("device_ip");

                YeelightDeviceEntity entity = new YeelightDeviceEntity(
                        result.getInt("id"),
                        result.getString("name"),
                        ip,
                        null
                );
                entities.add(entity);
            }
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }

        logger.log(Level.INFO, "selected {0} yeelight_devices", entities.size());
        return entities;
    }

    public void setDevices(Map<Integer, YeelightDeviceEntity> devices) {
        this.devices = devices;
    }

    public void putDevice(YeelightDeviceEntity device) {
        devices.put(device.id(), device);
    }

    /**
     * inserts a device to the DB, also inserts it to the local devices list
     * @param entity the device to insert in the DB
     * @return the inserted device with the generated ID
     */
    public YeelightDeviceEntity saveNewEntity(YeelightDeviceEntity entity) {

        try (PreparedStatement statement = sqlService.getConnection().prepareStatement(
                "insert into yeelight_devices (name, device_ip) values (?, ?);", Statement.RETURN_GENERATED_KEYS
        )) {
            statement.setString(1, entity.name().toLowerCase(Locale.ROOT));
            statement.setString(2, entity.ip());
            statement.execute();
            logger.log(Level.INFO, "inserted into yeelight_devices: {0}", entity);

            ResultSet generatedIdKeys = statement.getGeneratedKeys();
            generatedIdKeys.next();
            int generatedId = generatedIdKeys.getInt("id");

            YeelightDeviceEntity createdDevice = new YeelightDeviceEntity(generatedId, entity.name(), entity.ip(), entity.light());
            this.devices.put(createdDevice.id(), createdDevice);
            return createdDevice;
        } catch (PSQLException e) {
            throwKnownAlreadyExists(e, entity.name(), entity.ip());
            return null;
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }

    public void updateDeviceName(String oldName, String newName) {
        try (PreparedStatement statement = sqlService.getConnection().prepareStatement(
                "update yeelight_devices set name = ? where name = ?", Statement.RETURN_GENERATED_KEYS
        )) {
            statement.setString(1, newName.toLowerCase(Locale.ROOT));
            statement.setString(2, oldName.toLowerCase(Locale.ROOT));
            statement.executeUpdate();

            for (Map.Entry<Integer, YeelightDeviceEntity> entry : devices.entrySet()) {
                if(entry.getValue().name().toLowerCase(Locale.ROOT).equals(oldName.toLowerCase(Locale.ROOT))) {
                    devices.put(entry.getKey(), new YeelightDeviceEntity(
                            entry.getValue().id(),
                            newName,
                            entry.getValue().ip(),
                            entry.getValue().light()
                    ));
                    break;
                }
            }

            logger.log(Level.INFO, "successfully renamed device {0} -> {1}", new Object[]{oldName, newName});
        } catch (PSQLException e) {
            throwKnownAlreadyExists(e, newName, "");
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }

    public void deleteDevice(String name) {
        try (PreparedStatement statement = sqlService.getConnection().prepareStatement(
                "delete from yeelight_devices where name = ?", Statement.RETURN_GENERATED_KEYS
        )) {
            statement.setString(1, name.toLowerCase(Locale.ROOT));
            statement.execute();

            for (Map.Entry<Integer, YeelightDeviceEntity> entry : devices.entrySet()) {
                if(entry.getValue().name().toLowerCase(Locale.ROOT).equals(name.toLowerCase(Locale.ROOT))){
                    devices.remove(entry.getKey());
                    break;
                }
            }

            logger.log(Level.INFO, "successfully deleted device {0}", name);
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }

    private void throwKnownAlreadyExists(PSQLException e, String name, String ip) {
        if(e.getMessage().contains("yeelight_devices_name_key")) {
            throw new ResourceAlreadyExistsException("device name", name);
        } else if(e.getMessage().contains("yeelight_devices_device_ip_key")) {
            throw new ResourceAlreadyExistsException("device ip", ip);
        }
        throw new UnexpectedSqlException(e);
    }
}
