package ch.micha.automation.room.sql;

import ch.micha.automation.room.events.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class SQLService implements OnAppStartupListener, OnAppShutdownListener {
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    private Connection dbConnection;

    @Inject
    public SQLService(
            @ConfigProperty(name = "room.automation.db.url") String dbUrl,
            @ConfigProperty(name = "room.automation.db.user") String dbUser,
            @ConfigProperty(name = "room.automation.db.password") String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    @Override
    @EventHandlerPriority(HandlerPriority.FIRST)
    public void onAppStartup() {
        try {
            DriverManager.registerDriver(new org.postgresql.Driver());
            dbConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            logger.log(Level.INFO, "successfully initialized datasource at {0}", dbUrl);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to initialize datasource at {0}", dbUrl);
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    @EventHandlerPriority(HandlerPriority.LAST)
    public void onAppShutdown() {
        try {
            dbConnection.close();
            logger.info("disconnected db");
        } catch (SQLException e) {
            logger.log(Level.WARNING, "failed to close db connection", e);
        }
    }
}
