package ch.micha.automation.room.alarm.websocket;

import ch.micha.automation.room.alarm.dtos.AlarmDTO;
import ch.micha.automation.room.alarm.dtos.AlarmNotificationDTO;
import ch.micha.automation.room.errorhandling.ErrorMessageDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.websocket.Session;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class AlarmNotificationService {
    public static final int CONNECTION_IDLE_TIMEOUT = 10 * 60 * 1000; // millis (10 Minutes)
    public static final String ALARM_RUNNING = "alarm";
    public static final String ALARM_COMPLETED = "alarm_completed";
    public static final String ERROR_MESSAGE = "error";

    private static final Logger LOGGER = Logger.getLogger(AlarmNotificationService.class.getSimpleName());

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final BlockingQueue<AlarmNotificationDTO> notificationsToProcess = new LinkedBlockingQueue<>();
    private final Jsonb jsonBuilder = JsonbBuilder.create();

    public void addSession(Session session) {
        session.setMaxIdleTimeout(CONNECTION_IDLE_TIMEOUT);
        sessions.put(session.getId(), session);
        LOGGER.log(Level.INFO, "Alarm message listener connected: {0}", new Object[]{ session.getId() });

        if(!notificationsToProcess.isEmpty())
            catchSessionUp(session);
    }

    public void removeSession(Session session) {
        sessions.remove(session.getId());
        LOGGER.log(Level.INFO, "disconnected Alarm message listener: {0}", new Object[]{ session.getId() });
    }

    public void sessionHasError(Session session, Throwable error) {
        sessions.remove(session.getId());
        LOGGER.log(
            Level.WARNING,
            String.format("alarm message listener disconnected with error: %s", session.getId()),
            error
        );
    }

    public void sendAlarmNotification(AlarmDTO runningAlarm) {
        AlarmNotificationDTO notification = new AlarmNotificationDTO(ALARM_RUNNING, runningAlarm);

        if(sessions.isEmpty()) {
            notificationsToProcess.add(notification);
            LOGGER.log(Level.INFO, "tried to notify client about new alarm, but no client connected, added to queue");
        } else
            broadcastNotification(notification);
    }

    public void sendCurrentAlarmDone() {
        AlarmNotificationDTO notification = new AlarmNotificationDTO(ALARM_COMPLETED, null);

        if(sessions.isEmpty()) {
            notificationsToProcess.add(notification);
            LOGGER.log(Level.INFO, "tried to notify client about completed alarm, but no client connected, added to queue");
        } else
            broadcastNotification(notification);
    }

    public void sendError(ErrorMessageDTO error) {
        AlarmNotificationDTO notification = new AlarmNotificationDTO(ERROR_MESSAGE, error);

        if(sessions.isEmpty()) {
            notificationsToProcess.add(notification);
            LOGGER.log(Level.INFO, "tried to send error: {0}, but no client connected, added to queue",
                new Object[]{ error.message() });
        } else
            broadcastNotification(notification);
    }

    private void catchSessionUp(Session session) {
        LOGGER.log(Level.INFO, "catching session {0} up to {1} missed notifications",
            new Object[]{ session.getId(), notificationsToProcess.size() });

        try {
            while (!notificationsToProcess.isEmpty())
                sendNotification(session, notificationsToProcess.take());
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "failed to catch up session, thread interrupted");
            Thread.currentThread().interrupt();
        }
    }

    private void broadcastNotification(AlarmNotificationDTO notification) {
        LOGGER.log(Level.INFO, "sending notifications to {0} sessions", new Object[]{ sessions.size() });
        sessions.values().forEach(session -> sendNotification(session, notification));
    }

    private void sendNotification(Session session, AlarmNotificationDTO notification) {
        session.getAsyncRemote().sendText(jsonBuilder.toJson(notification), result -> {
            if(result.isOK())
                LOGGER.log(Level.INFO, "successfully sent {0} to {1}", new Object[]{ notification.getNotificationId(), session.getId() });
            else
                LOGGER.log(
                    Level.WARNING,
                    String.format("failed to send %s notification to %s. error: ", notification.getNotificationId(), session.getId()),
                    result.getException()
                );
        });
    }
}
