package ch.micha.automation.room.errorhandling.exceptions;

import ch.micha.automation.room.errorhandling.ErrorMessageDTO;
import jakarta.ws.rs.core.Response;

public class InvalidAlarmState extends AppException{

    public InvalidAlarmState() {
        super("tried to modify alarm, when no current alarm exists", null, false);
    }

    @Override
    public Response getResponse() {
        return Response.status(Response.Status.BAD_REQUEST).entity(getErrorMessage()).build();
    }

    @Override
    public ErrorMessageDTO getErrorMessage() {
        return new ErrorMessageDTO(
            "can not modify alarm",
            "there is no current alarm -> you can't modify it",
            ""
        );
    }
}
