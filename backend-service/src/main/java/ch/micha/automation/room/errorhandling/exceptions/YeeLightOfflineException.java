package ch.micha.automation.room.errorhandling.exceptions;

import ch.micha.automation.room.errorhandling.ErrorMessageDTO;
import jakarta.ws.rs.core.Response;

public class YeeLightOfflineException extends AppException{
    private final int deviceId;
    private final String deviceName;

    public YeeLightOfflineException(int id, String name) {
        super(String.format("tried to call to offline device { id:%s, name:%s }", id, name), null, false);
        this.deviceId = id;
        this.deviceName = name;
    }

    @Override
    public Response getResponse() {
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(new ErrorMessageDTO("can't change offline light",
                        String.format("tried to call to offline device { id:%s, name:%s }", deviceId, deviceName), ""))
                .build();
    }
}
