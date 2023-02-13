package ch.micha.automation.room.scene;

import ch.micha.automation.room.errorhandling.exceptions.UnexpectedSqlException;
import ch.micha.automation.room.light.*;
import ch.micha.automation.room.sql.SQLService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class SceneProvider {
    private final Logger logger = Logger.getLogger(getClass().getSimpleName());
    private final SQLService sql;
    private final YeelightDeviceProvider deviceProvider;
    private final LightConfigProvider lightConfigProvider;

    private int defaultSceneId;

    @Inject
    public SceneProvider(SQLService sql, YeelightDeviceProvider deviceProvider, LightConfigProvider lightConfigProvider) {
        this.sql = sql;
        this.deviceProvider = deviceProvider;
        this.lightConfigProvider = lightConfigProvider;
    }

    public Optional<SceneEntity> findSceneById(int id) {
        logger.log(Level.SEVERE, "@micha, du hesch das noni implementiert");
        return Optional.empty();
    }

    /**
     * tries to load the default scene from the DB.
     * if no default scene is found it creates a new one.
     * if multiple default scenes are found, it will log a warning.
     * @return always! a default scene (except if unexpected sql exception)
     */
    public SceneEntity loadDefaultScene() {
        try (PreparedStatement statement = sql.getConnection().prepareStatement(
                "select * from scene where default_scene = true;"
        )) {
            ResultSet result = statement.executeQuery();

            if(result.getFetchSize() > 1) {
                logger.log(Level.WARNING, "Found multiple default scenes. Using first.");
            }

            if(result.next()) {
                final int sceneId = result.getInt("id");
                final SceneEntity scene = new SceneEntity(
                        sceneId,
                        result.getString("name"),
                        result.getBoolean("default_scene"),
                        loadDeviceLightConfigs(sceneId)
                );

                defaultSceneId = sceneId;
                logger.log(Level.INFO, "default scene selected");
                return scene;
            } else {
                logger.log(Level.INFO, "no default scene found, creating default scene");
                return createDefaultScene();
            }
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }

    /**
     * inserts a new scene to the DB.
     * <br><br>
     * IMPORTANT! if default scene is true, the old default scene will be changed to no longer be default.
     * @param name the name of the new scene, must be unique. todo implement errorhandling for unique constraint violation stuff.
     * @param defaultScene this new scene will be the default
     * @return the created scene with the correct generated id.
     */
    public SceneEntity createNewScene(String name, boolean defaultScene, Map<YeelightDeviceEntity, LightConfigDTO> newLightConfigs) {
        try (PreparedStatement statement = sql.getConnection().prepareStatement(
                "INSERT INTO scene (name, default_scene) VALUES (?, ?);", Statement.RETURN_GENERATED_KEYS
        )) {
            statement.setString(1, name);
            statement.setBoolean(2, defaultScene);

            statement.execute();

            ResultSet generatedIdKeys = statement.getGeneratedKeys();
            generatedIdKeys.next();
            int generatedId = generatedIdKeys.getInt("id");
            logger.log(Level.INFO, "created new scene: {0}-{1}", new Object[]{generatedId, name});

            if(defaultScene)
                unsetOldDefaultScene(generatedId);

            saveChangedLightConfigs(generatedId, newLightConfigs);
            return new SceneEntity(generatedId, name, defaultScene, new HashMap<>());
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }

    /**
     * updates the light configs for a scene. (deletes the complete ond config and inserts the new one)
     * @param sceneId the scene that should be changed
     * @param newLightConfigs the new light configuration
     */
    public void saveChangedLightConfigs(int sceneId, Map<YeelightDeviceEntity, LightConfigDTO> newLightConfigs) {
        deleteAllLightConfigs(sceneId);

        StringBuilder query = new StringBuilder("INSERT INTO device_light_scene (scene_id, device_id, configuration_id) VALUES ");
        for (int i = 0; i < newLightConfigs.size(); i++) {
            query.append("(?, ?, ?)");

            if(i != newLightConfigs.size() - 1) query.append(", ");
            else query.append(";");
        }

        try (PreparedStatement statement = sql.getConnection().prepareStatement(query.toString())) {
            int i = 0;
            for (Map.Entry<YeelightDeviceEntity, LightConfigDTO> entry : newLightConfigs.entrySet()) {
                i++;
                statement.setInt(i, sceneId);
                i++;
                statement.setInt(i, entry.getKey().id());
                i++;
                statement.setInt(i, entry.getValue().id());
            }

            statement.execute();
            logger.log(Level.INFO, "updated light configs for scene {0} with {1} new configs",
                    new Object[]{sceneId, newLightConfigs.size()});
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }

    private Map<YeelightDeviceEntity, LightConfigDTO> loadDeviceLightConfigs(int sceneId) {
        Map<YeelightDeviceEntity, LightConfigDTO> deviceLightConfigs = new HashMap<>();
        String query = "SELECT * FROM device_light_scene WHERE scene_id=?;";

        try (PreparedStatement statement = sql.getConnection().prepareStatement(query)) {
            statement.setInt(1, sceneId);
            ResultSet result = statement.executeQuery();

            List<Integer> deviceIds = new ArrayList<>();
            List<Integer> configurationIds = new ArrayList<>();

            while (result.next()) {
                deviceIds.add(result.getInt("device_id"));
                configurationIds.add(result.getInt("configuration_id"));
            }

            Integer[] deviceIdsArray = deviceIds.toArray(new Integer[0]);
            List<YeelightDeviceEntity> devices = deviceProvider.findByIds(deviceIdsArray);
            Integer[] configurationIdsArray = configurationIds.toArray(new Integer[0]);
            Map<Integer, LightConfigDTO> lightConfigs = lightConfigProvider.findConfigsToMap(configurationIdsArray);

            for (int i = 0; i < devices.size(); i++) {
                deviceLightConfigs.put(devices.get(i), lightConfigs.get(configurationIds.get(i)));
            }
            logger.log(Level.INFO, "successfully loaded {0} device_light_configurations", deviceLightConfigs.size()); 
            return deviceLightConfigs;
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }

    /**
     * expects the new default scene to be saved already (with default scene set to true)
     */
    private void unsetOldDefaultScene(int newDefaultSceneId) {
        try (PreparedStatement statement = sql.getConnection().prepareStatement(
                "UPDATE scene " +
                        "SET default_scene = false " +
                        "WHERE id = ?;"
        )) {
            statement.setInt(1, defaultSceneId);
            statement.execute();

            logger.log(Level.INFO, "saved new default scene: {0}", newDefaultSceneId);
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }

    private SceneEntity createDefaultScene() {
        Map<YeelightDeviceEntity, LightConfigDTO> defaultLightConfigs = new HashMap<>();
        Optional<LightConfigDTO> existingLightConfig = lightConfigProvider
                .findConfigByName(LightConfigProvider.DEFAULT_CONFIG_NAME);

        LightConfigDTO defaultLightConfig = existingLightConfig.orElseGet(lightConfigProvider::createDefaultConfig);

        deviceProvider.getDevices().forEach(device -> defaultLightConfigs.put(device, defaultLightConfig));

        return createNewScene("Default", true, defaultLightConfigs);
    }

    private void deleteAllLightConfigs(int sceneId) {
        final String query = "DELETE FROM device_light_scene WHERE scene_id = ?;";

        try (PreparedStatement statement = sql.getConnection().prepareStatement(query)) {
            statement.setInt(1, sceneId);
            statement.execute();

            logger.log(Level.INFO, "deleted all device_light_scenes for scene: {0}", sceneId);
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }
}
