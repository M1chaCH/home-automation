package ch.micha.automation.room.alarm.websocket;

import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@SuppressWarnings("unused") // intellij does not know that this is called by jakarta
@ServerEndpoint("/automation/alarm/notifications")
public class AlarmNotificationsResource {

    private final AlarmNotificationService service;

    @Inject
    public AlarmNotificationsResource(AlarmNotificationService service) {
        this.service = service;
    }

    @OnOpen
    public void onOpen(Session session) {
        service.addSession(session);
    }

    @OnClose
    public void onClose(Session session) {
        service.removeSession(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        service.sessionHasError(session, throwable);
    }
}
