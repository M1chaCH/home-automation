package ch.micha.automation.room.light;

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

    public Optional<LightConfigDTO> findConfig(int configId) {
        final String query = "SELECT * FROM light_configuration WHERE id = ?";

        try (PreparedStatement statement = sql.getConnection().prepareStatement(query)) {
            statement.setInt(1, configId);
            ResultSet result = statement.executeQuery();

            if(!result.next()) {
                logger.log(Level.INFO, "did not find LightConfig by id:{0}", configId);
                return Optional.empty();
            }
            LightConfigDTO lightConfig = createConfigFromCurrent(result);

            logger.log(Level.INFO, "selected light_configuration id:{0}", lightConfig.id());
            return Optional.of(lightConfig);
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }

    public Optional<LightConfigDTO> findConfigByName(String configName) {
        final String query = "SELECT * FROM light_configuration WHERE name = ?";

        try (PreparedStatement statement = sql.getConnection().prepareStatement(query)) {
            statement.setString(1, configName);
            ResultSet result = statement.executeQuery();

            if(!result.next()) {
                logger.log(Level.INFO, "did not find LightConfig by name:{0}", configName);
                return Optional.empty();
            }
            LightConfigDTO lightConfig = createConfigFromCurrent(result);

            logger.log(Level.INFO, "selected light_configuration id:{0}", lightConfig.id());
            return Optional.of(lightConfig);
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }

    public Map<Integer, LightConfigDTO> findConfigsToMap(Integer... configIds) {
        Map<Integer, LightConfigDTO> lightConfigs = new HashMap<>();
        StringBuilder query = new StringBuilder("SELECT * FROM light_configuration WHERE ");

        for (int i = 0; i < configIds.length; i++) { // add or statement to include the given configIds
            query.append("id = ?");
            if(i != configIds.length -1) query.append(" OR ");
        }

        try (PreparedStatement statement = sql.getConnection().prepareStatement(query.toString())) {
            for (int i = 0; i < configIds.length; i++) { // prepare the generated OR statement
                statement.setInt(i + 1, configIds[i]);
            }
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                LightConfigDTO config = createConfigFromCurrent(result);
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
    public Collection<LightConfigDTO> findConfigs(Integer... configIds) {
        return findConfigsToMap(configIds).values();
    }

    public LightConfigDTO createDefaultConfig() {
        final LightConfigDTO defaultConfig = new LightConfigDTO(
                0,
                DEFAULT_CONFIG_NAME,
                true,
                255,
                255,
                255,
                100,
                0
        );

        try (PreparedStatement statement = sql.getConnection().prepareStatement(
                "INSERT INTO light_configuration (name, power, red, green, blue, brightness, change_duration) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS
        )) {
            statement.setString(1, defaultConfig.name());
            statement.setBoolean(2, defaultConfig.power());
            statement.setInt(3, defaultConfig.red());
            statement.setInt(4, defaultConfig.green());
            statement.setInt(5, defaultConfig.blue());
            statement.setInt(6, defaultConfig.brightness());
            statement.setInt(7, defaultConfig.changeDurationMillis());

            statement.execute();

            ResultSet generatedIdKeys = statement.getGeneratedKeys();
            generatedIdKeys.next();
            int generatedId = generatedIdKeys.getInt("id");

            final LightConfigDTO lightConfigDTO = new LightConfigDTO(
                    generatedId,
                    defaultConfig.name(),
                    defaultConfig.power(),
                    defaultConfig.red(),
                    defaultConfig.green(),
                    defaultConfig.blue(),
                    defaultConfig.brightness(),
                    defaultConfig.changeDurationMillis()
            );

            logger.log(Level.INFO, "created default light configuration: {0}", lightConfigDTO);
            return lightConfigDTO;
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }

    private LightConfigDTO createConfigFromCurrent(ResultSet result) throws SQLException {
        return new LightConfigDTO(
                result.getInt("id"),
                result.getString("name"),
                result.getBoolean("power"),
                result.getInt("red"),
                result.getInt("green"),
                result.getInt("blue"),
                result.getInt("brightness"),
                result.getInt("change_duration")
        );
    }
}
