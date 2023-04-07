package ch.micha.automation.room.light.configuration;

import ch.micha.automation.room.errorhandling.exceptions.UnexpectedSqlException;
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

/**
 * This class provides an API to the "light_configuration" table.
 */
@ApplicationScoped
public class LightConfigProvider {
    public static final String DEFAULT_CONFIG_NAME = "_default";
    private final Logger logger = Logger.getLogger(getClass().getSimpleName());
    private final SQLService sql;

    @Inject
    public LightConfigProvider(SQLService sql) {
        this.sql = sql;
    }

    public Optional<LightConfig> findConfig(int configId) {
        final String query = "SELECT * FROM light_configuration WHERE id = ?";

        try (PreparedStatement statement = sql.getConnection().prepareStatement(query)) {
            statement.setInt(1, configId);
            ResultSet result = statement.executeQuery();

            if(!result.next()) {
                logger.log(Level.INFO, "did not find LightConfig by id:{0}", configId);
                return Optional.empty();
            }
            LightConfig lightConfig = createConfigFromCurrent(result);

            logger.log(Level.INFO, "selected light_configuration id:{0}", lightConfig.id());
            return Optional.of(lightConfig);
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }

    public Optional<LightConfig> findConfigByName(String configName) {
        final String query = "SELECT * FROM light_configuration WHERE name = ?";

        try (PreparedStatement statement = sql.getConnection().prepareStatement(query)) {
            statement.setString(1, configName);
            ResultSet result = statement.executeQuery();

            if(!result.next()) {
                logger.log(Level.INFO, "did not find LightConfig by name:{0}", configName);
                return Optional.empty();
            }
            LightConfig lightConfig = createConfigFromCurrent(result);

            logger.log(Level.INFO, "selected light_configuration id:{0}", lightConfig.id());
            return Optional.of(lightConfig);
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }

    public Map<Integer, LightConfig> findConfigsToMap(Integer... configIds) {
        Map<Integer, LightConfig> lightConfigs = new HashMap<>();
        StringBuilder query = new StringBuilder("SELECT * FROM light_configuration ");

        if(configIds.length > 0) {
            query.append("WHERE ");
            for (int i = 0; i < configIds.length; i++) { // add or statement to include the given configIds
                query.append("id = ?");
                if(i != configIds.length -1) query.append(" OR ");
            }
        }

        try (PreparedStatement statement = sql.getConnection().prepareStatement(query.toString())) {
            for (int i = 0; i < configIds.length; i++) { // prepare the generated OR statement
                statement.setInt(i + 1, configIds[i]);
            }
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                LightConfig config = createConfigFromCurrent(result);
                lightConfigs.put(config.id(), config);
            }

            logger.log(Level.INFO, "selected {0} light_configurations", lightConfigs.size());
            return lightConfigs;
        }catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }

    /**
     * can be used to select multiple LightConfigDTOs from the DB. (executes only one statement)
     * @param configIds a list of the configs to be selected
     * @return the selected and parsed configs (if id was not found, then nothing happens but the id won't be included in the result)
     */
    public Collection<LightConfig> findConfigs(Integer... configIds) {
        return findConfigsToMap(configIds).values();
    }

    public LightConfig createDefaultConfig() {
        logger.log(Level.INFO, "creating default light configuration");
        return createConfig(DEFAULT_CONFIG_NAME, 255, 255, 255, 100);
    }

    public LightConfig createConfig(String name, int red, int green, int blue, int brightnes) {
        logger.log(Level.INFO, "creating light configuration");

        try (PreparedStatement statement = sql.getConnection().prepareStatement(
                "INSERT INTO light_configuration (name, red, green, blue, brightness) " +
                        "VALUES (?, ?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS
        )) {
            statement.setString(1, name);
            statement.setInt(2, red);
            statement.setInt(3, green);
            statement.setInt(4, blue);
            statement.setInt(5, brightnes);

            statement.execute();

            ResultSet generatedIdKeys = statement.getGeneratedKeys();
            generatedIdKeys.next();
            int generatedId = generatedIdKeys.getInt("id");

            final LightConfig lightConfigEntity = new LightConfig(
                    generatedId,
                    name,
                    red,
                    green,
                    blue,
                    brightnes
            );

            logger.log(Level.INFO, "created light configuration: {0}", lightConfigEntity);
            return lightConfigEntity;
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e); // todo handle unique constraint exception (some good generic error handling)
        }
    }

    public void updateConfig(int id, String name, int red, int green, int blue, int brightness) {
        logger.log(Level.INFO, "updating light configuration {0}", id);


        try (PreparedStatement statement = sql.getConnection().prepareStatement(
                "UPDATE light_configuration " +
                        "SET name = ?, red = ?, green = ?, blue = ?, brightness = ? " +
                        "WHERE id = ?;"
        )) {
            statement.setString(1, name);
            statement.setInt(2, red);
            statement.setInt(3, green);
            statement.setInt(4, blue);
            statement.setInt(5, brightness);
            statement.setInt(6, id);

            statement.executeUpdate();
            logger.log(Level.INFO, "updated configs with id {0}", id);
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }

    public void deleteConfig(int id) {
        logger.log(Level.INFO, "deleting light config {0}", id);

        final int defaultConfigId = findConfigByName(DEFAULT_CONFIG_NAME).orElseThrow().id();

        try (PreparedStatement statement = sql.getConnection().prepareStatement(
                "UPDATE device_light_scene SET configuration_id = ? WHERE configuration_id = ?;"
        )) {
            statement.setInt(1, defaultConfigId);
            statement.setInt(2, id);

            statement.executeUpdate();
            logger.log(Level.INFO, "changed all usages of light config {0} to default config", id);
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }

        try (PreparedStatement statement = sql.getConnection().prepareStatement(
                "DELETE FROM light_configuration WHERE id = ?"
        )) {
            statement.setInt(1, id);

            statement.execute();
            logger.log(Level.INFO, "delete config with id {0}", id);
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }

    private LightConfig createConfigFromCurrent(ResultSet result) throws SQLException {
        return new LightConfig(
                result.getInt("id"),
                result.getString("name"),
                result.getInt("red"),
                result.getInt("green"),
                result.getInt("blue"),
                result.getInt("brightness")
        );
    }
}
