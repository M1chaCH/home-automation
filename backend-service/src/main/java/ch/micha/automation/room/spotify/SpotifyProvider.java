package ch.micha.automation.room.spotify;

import ch.micha.automation.room.errorhandling.exceptions.UnexpectedSqlException;
import ch.micha.automation.room.spotify.dtos.SpotifyAuthorisationDTO;
import ch.micha.automation.room.sql.SQLService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class SpotifyProvider {
    private final Logger logger = Logger.getLogger(getClass().getSimpleName());
    private final SQLService sql;

    @Inject
    public SpotifyProvider(SQLService sql) {
        this.sql = sql;
    }

    public Optional<SpotifyAuthorisationDTO> findAuth() {
        try (PreparedStatement statement = sql.getConnection().prepareStatement(
                "select * from spotify_authorisation limit 1"
        )) {
            ResultSet result = statement.executeQuery();

            if(result.next()) {
                SpotifyAuthorisationDTO dto = new SpotifyAuthorisationDTO(
                        result.getString("access_token"),
                        result.getString("token_type"),
                        result.getString("scope"),
                        result.getInt("generated_at"),
                        result.getInt("expires_in"),
                        result.getString("refresh_token")
                );

                logger.log(Level.INFO, "successfully selected spotify auth");
                return Optional.of(dto);
            } else {
                logger.log(Level.INFO, "did not select any rows in spotify auth");
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new UnexpectedSqlException(e);
        }
    }

    public boolean insertAuth(SpotifyAuthorisationDTO auth) {
        try (PreparedStatement statement = sql.getConnection().prepareStatement(
                "insert into spotify_authorisation (access_token, token_type, scope, generated_at, expires_in, refresh_token) values " +
                        "(?, ?, ?, ?, ?, ?)"
        )) {
            statement.setString(1, auth.getAccessToken());
            statement.setString(2, auth.getTokenType());
            statement.setString(3, auth.getScope());
            statement.setInt(4, auth.getGeneratedAt());
            statement.setInt(5, auth.getExpiresIn());
            statement.setString(6, auth.getRefreshToken());
            statement.execute();

            logger.log(Level.INFO, "successfully stored spotify authorisation");
            return true;
        } catch (SQLException e) {
            if("27000".equals(e.getSQLState()))
                return false;

            throw new UnexpectedSqlException(e);
        }
    }
}
