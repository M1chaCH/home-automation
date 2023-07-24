package ch.micha.automation.room.alarm;

import ch.micha.automation.room.errorhandling.exceptions.UnexpectedSqlException;
import ch.micha.automation.room.sql.SQLService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class AlarmProvider {
    private static final Logger LOGGER = Logger.getLogger(AlarmProvider.class.getSimpleName());
    private final SQLService sql;

    public static final String ID_COLUMN = "id";
    public static final String CRON_SCHEDULE_COLUMN = "cron_schedule";
    public static final String ACTIVE_COLUMN = "active";
    public static final String SCENE_ID_COLUMN = "scene";

    @Inject
    public AlarmProvider(SQLService sql) {
        this.sql = sql;
    }

    public List<AlarmEntity> loadAlarms() {
        final String query = """
                    SELECT * FROM alarm;
                    """;

        try (PreparedStatement statement = sql.getConnection().prepareStatement(query)) {
            ResultSet result = statement.executeQuery();

            List<AlarmEntity> alarms = new ArrayList<>();
            while (result.next()) {
                alarms.add(new AlarmEntity(
                    result.getInt(ID_COLUMN),
                    result.getString(CRON_SCHEDULE_COLUMN),
                    result.getBoolean(ACTIVE_COLUMN),
                    result.getInt(SCENE_ID_COLUMN)
                ));
            }

            LOGGER.log(Level.INFO, "selected {0} alarms", new Object[]{ alarms.size() });
            return alarms;
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }

    public AlarmEntity createAlarm(String cronSchedule, int sceneId) {
        LOGGER.log(Level.INFO, "creating alarm at {0}", new Object[]{ cronSchedule });

        try (PreparedStatement statement = sql.getConnection().prepareStatement(
            "INSERT INTO alarm (cron_schedule, scene) " +
                    "VALUES (?, ?);", Statement.RETURN_GENERATED_KEYS
        )) {
            statement.setString(1, cronSchedule);
            statement.setInt(2, sceneId);

            statement.execute();

            ResultSet generatedIdKeys = statement.getGeneratedKeys();
            generatedIdKeys.next();
            int generatedId = generatedIdKeys.getInt("id");

            final AlarmEntity entity = new AlarmEntity(
                generatedId,
                cronSchedule,
                 true,
                sceneId
            );

            LOGGER.log(Level.INFO, "created alarm {0}", entity);
            return entity;
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }

    public void updateAlarm(int id, String cronSchedule, boolean active, int sceneId) {
        LOGGER.log(Level.INFO, "updating alarm {0}", new Object[]{ id });

        try (PreparedStatement statement = sql.getConnection().prepareStatement(
            "UPDATE alarm " +
                "SET cron_schedule = ?, active = ?, scene = ? " +
                "WHERE id = ?;"
        )) {
            statement.setString(1, cronSchedule);
            statement.setBoolean(2, active);
            statement.setInt(3, sceneId);
            statement.setInt(4, id);
            statement.executeUpdate();
            LOGGER.log(Level.INFO, "updated alarms with id {0}", id);
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }

    public void deleteAlarm(int id) {
        LOGGER.log(Level.INFO, "deleting alarm {0}", new Object[]{ id });

        try (PreparedStatement statement = sql.getConnection().prepareStatement(
            "DELETE FROM alarm WHERE id = ?;"
        )) {
            statement.setInt(1, id);
            statement.execute();
            LOGGER.log(Level.INFO, "deleted alarms with id {0}", id);
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }
}
