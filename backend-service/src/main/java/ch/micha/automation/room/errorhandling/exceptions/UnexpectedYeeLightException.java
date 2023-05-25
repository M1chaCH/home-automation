package ch.micha.automation.room.errorhandling.exceptions;

import ch.micha.automation.room.errorhandling.ErrorMessageDTO;
import jakarta.ws.rs.core.Response;

public class UnexpectedYeeLightException extends AppException{
    private final Throwable exception;

    public UnexpectedYeeLightException(String deviceIp, Throwable exception) {
        super("unexpected yeelight exception ond device: " + deviceIp, exception, true);
        this.exception = exception;
    }

    @Override
    public Response getResponse() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(getErrorMessage()).build();
    }

    @Override
    public ErrorMessageDTO getErrorMessage() {
        return new ErrorMessageDTO(
            "failed to access YeeLight",
            String.format("got %s with message %s", exception.getClass().getSimpleName(), exception.getMessage()),
            super.formatStackTraceToHtml(exception.getStackTrace())
        );
    }
}
