package ch.micha.automation.room.errorhandling.exceptions;

import ch.micha.automation.room.errorhandling.ErrorMessageDTO;
import jakarta.ws.rs.core.Response;

public class UnexpectedAlarmException extends AppException{
    private final Exception exception;

    public UnexpectedAlarmException(Exception exception) {
        super("got unexpected error while executing alarm", exception, true);
        this.exception = exception;
    }

    public UnexpectedAlarmException(String message) {
        super("got unexpected error while executing alarm: " + message, null, true);
        this.exception = this;
    }

    @Override
    public Response getResponse() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(getErrorMessage()).build();
    }

    @Override
    public ErrorMessageDTO getErrorMessage() {
        return new ErrorMessageDTO(
            "failed to execute alarm",
            String.format("got %s with message %s", exception.getClass().getSimpleName(), exception.getMessage()),
            super.formatStackTraceToHtml(exception.getStackTrace())
        );
    }
}
