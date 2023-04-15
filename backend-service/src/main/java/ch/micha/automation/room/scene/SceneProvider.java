package ch.micha.automation.room.scene;

import ch.micha.automation.room.errorhandling.exceptions.ResourceAlreadyExistsException;
import ch.micha.automation.room.errorhandling.exceptions.UnexpectedSqlException;
import ch.micha.automation.room.light.configuration.LightConfig;
import ch.micha.automation.room.light.configuration.LightConfigProvider;
import ch.micha.automation.room.light.yeelight.YeelightDeviceEntity;
import ch.micha.automation.room.light.yeelight.YeelightDeviceProvider;
import ch.micha.automation.room.sql.SQLService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.postgresql.util.PSQLException;

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

    private static final String ID_COLUMN = "id";
    private static final String NAME_COLUMN = "name";
    private static final String DEFAULT_SCENE_COLUMN = "default_scene";
    private static final String SPOTIFY_RESOURCE_COLUMN = "spotify_resource";
    private static final String SPOTIFY_VOLUME_COLUMN = "spotify_volume";

    private int defaultSceneId = -1;

    @Inject
    public SceneProvider(SQLService sql, YeelightDeviceProvider deviceProvider, LightConfigProvider lightConfigProvider) {
        this.sql = sql;
        this.deviceProvider = deviceProvider;
        this.lightConfigProvider = lightConfigProvider;
    }

    /**
     * @param id of the scene to search
     * @return an optional with the selected scene or an empty optional if scene was not found
     */
    public Optional<SceneEntity> findSceneById(int id) {
        try (PreparedStatement statement = sql.getConnection().prepareStatement(
                "select * from scene where id = ?"
        )) {
            statement.setInt(1, id);
            ResultSet result = statement.executeQuery();

            if(!result.next())
                return Optional.empty();

            Map<YeelightDeviceEntity, LightConfig> lights = loadDeviceLightConfigs(id);

            SceneEntity scene = new SceneEntity(
                    id,
                    result.getString(NAME_COLUMN),
                    result.getBoolean(DEFAULT_SCENE_COLUMN),
                    lights,
                    result.getString(SPOTIFY_RESOURCE_COLUMN),
                    result.getInt(SPOTIFY_VOLUME_COLUMN)
            );
            return Optional.of(scene);
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
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
                final int sceneId = result.getInt(ID_COLUMN);
                final SceneEntity scene = new SceneEntity(
                        sceneId,
                        result.getString(NAME_COLUMN),
                        result.getBoolean(DEFAULT_SCENE_COLUMN),
                        loadDeviceLightConfigs(sceneId),
                        result.getString(SPOTIFY_RESOURCE_COLUMN),
                        result.getInt(SPOTIFY_VOLUME_COLUMN)
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
     * @param name the name of the new scene, must be unique.
     * @param defaultScene this new scene will be the default
     * @param spotifyResource the URI to a spotify resource
     * @param spotifyVolume the volume to start the spotify resource at
     * @return the created scene with the correct generated id.
     */
    public SceneEntity createNewScene(String name, boolean defaultScene, String spotifyResource, int spotifyVolume, Map<YeelightDeviceEntity, LightConfig> newLightConfigs) {
        try (PreparedStatement statement = sql.getConnection().prepareStatement(
                "INSERT INTO scene (name, default_scene, spotify_resource, spotify_volume) VALUES (?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS
        )) {
            statement.setString(1, name.toLowerCase(Locale.ROOT));
            statement.setBoolean(2, defaultScene);
            statement.setString(3, spotifyResource);
            statement.setInt(4, spotifyVolume);

            statement.execute();

            ResultSet generatedIdKeys = statement.getGeneratedKeys();
            generatedIdKeys.next();
            int generatedId = generatedIdKeys.getInt(ID_COLUMN);
            logger.log(Level.INFO, "created new scene: {0}-{1}", new Object[]{generatedId, name});

            if(defaultScene)
                unsetOldDefaultScene(generatedId);

            saveChangedLightConfigs(generatedId, newLightConfigs);
            return new SceneEntity(generatedId, name, defaultScene, newLightConfigs, spotifyResource, spotifyVolume);
        } catch (PSQLException e) {
            throwKnownAlreadyExists(e, name);
            return null;
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }

    public void updateScene(int id, String name, String spotifyResource, int spotifyVolume, Map<YeelightDeviceEntity, LightConfig> lightConfigs) {
        try (PreparedStatement statement = sql.getConnection().prepareStatement(
                "UPDATE scene SET name = ?, spotify_resource = ?, spotify_volume = ? where id = ?;"
        )) {
            statement.setString(1, name);
            statement.setString(2, spotifyResource);
            statement.setInt(3, spotifyVolume);
            statement.setInt(4, id);

            statement.executeUpdate();

            logger.log(Level.INFO, "updated scene {0} -> now updating dependant light configs", id);
            saveChangedLightConfigs(id, lightConfigs);
        } catch (PSQLException e) {
            throwKnownAlreadyExists(e, name);
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }

    public void deleteScene(int id) {
        try (PreparedStatement statement = sql.getConnection().prepareStatement(
                "DELETE FROM scene where id = ?;"
        )) {
            statement.setInt(1, id);
            statement.execute();

            logger.log(Level.INFO, "deleted scene {0}", id);
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }

    /**
     * updates the light configs for a scene. (deletes the complete ond config and inserts the new one)
     * @param sceneId the scene that should be changed
     * @param newLightConfigs the new light configuration
     */
    public void saveChangedLightConfigs(int sceneId, Map<YeelightDeviceEntity, LightConfig> newLightConfigs) {
        deleteAllLightConfigs(sceneId);
        if(newLightConfigs.size() < 1) {
            logger.log(Level.INFO, "updated light configs for scene {0} with {1} new configs",
                    new Object[]{sceneId, 0});
            return;
        }

        StringBuilder query = new StringBuilder("INSERT INTO device_light_scene (scene_id, device_id, configuration_id) VALUES ");
        for (int i = 0; i < newLightConfigs.size(); i++) {
            query.append("(?, ?, ?)");

            if(i != newLightConfigs.size() - 1) query.append(", ");
            else query.append(";");
        }

        try (PreparedStatement statement = sql.getConnection().prepareStatement(query.toString())) {
            int i = 0;
            for (Map.Entry<YeelightDeviceEntity, LightConfig> entry : newLightConfigs.entrySet()) {
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

    public void addDeviceToScene(int deviceId, int configId, int sceneId){
        logger.log(Level.INFO, "adding device {0} to default scene", deviceId);
        if(configId < 1) {
            Optional<LightConfig> config = lightConfigProvider.findConfigByName(LightConfigProvider.DEFAULT_CONFIG_NAME);
            configId = config.orElseGet(lightConfigProvider::createDefaultConfig).id();
        }

        if(sceneId < 1) {
            if(defaultSceneId < 1) {
                createDefaultScene();
                return; // creating a new scene already adds all devices.
            }
            sceneId = defaultSceneId;
        }

        try (PreparedStatement statement = sql.getConnection().prepareStatement(
                "INSERT INTO device_light_scene (device_id, configuration_id, scene_id) VALUES (?, ?, ?);"
        )) {
            statement.setInt(1, deviceId);
            statement.setInt(2, configId);
            statement.setInt(3, sceneId);
            statement.execute();

            logger.log(Level.INFO, "added device {0} to scene {1} with config {2}", new Object[]{deviceId, sceneId, configId});
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }

    public List<SceneEntity> loadScenes() {
        final String query = """
                 SELECT
                     s.id as scene_id, s.name as scene_name, s.default_scene, s.spotify_resource, s.spotify_volume,
                     lc.id as config_id, lc.name as config_name, lc.red, lc.green, lc.blue, lc.brightness,
                     dls.device_id as device_id
                 FROM scene AS s
                          LEFT JOIN device_light_scene AS dls ON s.id = dls.scene_id
                          LEFT JOIN light_configuration AS lc ON dls.configuration_id = lc.id
                 ORDER BY scene_id;""";

        try (PreparedStatement statement = sql.getConnection().prepareStatement(query)) {
            ResultSet result = statement.executeQuery();

            List<SceneEntity> scenes = new ArrayList<>();
            Map<YeelightDeviceEntity, LightConfig> lightsToFill = new HashMap<>();
            int sceneIdToFill = -1;
            while (result.next()) {
                int currentSceneId = result.getInt("scene_id");

                if(sceneIdToFill != currentSceneId) {
                    sceneIdToFill = currentSceneId;
                    lightsToFill = new HashMap<>();
                    scenes.add(new SceneEntity(
                            result.getInt("scene_id"),
                            result.getString("scene_name"),
                            result.getBoolean(DEFAULT_SCENE_COLUMN),
                            lightsToFill,
                            result.getString(SPOTIFY_RESOURCE_COLUMN),
                            result.getInt(SPOTIFY_VOLUME_COLUMN)
                    ));
                }

                YeelightDeviceEntity currentDevice = deviceProvider.findByIds(result.getInt("device_id")).get(0);
                if(currentDevice != null)
                    lightsToFill.put(currentDevice, new LightConfig(
                            result.getInt("config_id"),
                            result.getString("config_name"),
                            result.getInt("red"),
                            result.getInt("green"),
                            result.getInt("blue"),
                            result.getInt("brightness")
                    ));
            }

            logger.log(Level.INFO, "selected {0} scenes", scenes.size());
            return scenes;
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }

    private Map<YeelightDeviceEntity, LightConfig> loadDeviceLightConfigs(int sceneId) {
        Map<YeelightDeviceEntity, LightConfig> deviceLightConfigs = new HashMap<>();
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
            Map<Integer, LightConfig> lightConfigs = lightConfigProvider.findConfigsToMap(configurationIdsArray);

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
        Map<YeelightDeviceEntity, LightConfig> defaultLightConfigs = new HashMap<>();
        Optional<LightConfig> existingLightConfig = lightConfigProvider
                .findConfigByName(LightConfigProvider.DEFAULT_CONFIG_NAME);

        LightConfig defaultLightConfig = existingLightConfig.orElseGet(lightConfigProvider::createDefaultConfig);

        deviceProvider.getDevices().forEach(device -> defaultLightConfigs.put(device, defaultLightConfig));

        SceneEntity defaultScene = createNewScene("Default", true, "spotify:playlist:4kh5kepkkxFgrlUqFkcMfm", 30, defaultLightConfigs);
        this.defaultSceneId = defaultScene.id();
        return defaultScene;
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

    private void throwKnownAlreadyExists(PSQLException e, String name) {
        if(e.getMessage().contains("scene_name_key")) {
            throw new ResourceAlreadyExistsException("scene name", name);
        }
        throw new UnexpectedSqlException(e);
    }
}
