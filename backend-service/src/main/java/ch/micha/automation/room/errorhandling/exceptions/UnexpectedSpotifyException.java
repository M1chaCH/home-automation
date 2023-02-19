package ch.micha.automation.room.errorhandling.exceptions;

import ch.micha.automation.room.errorhandling.ErrorMessageDTO;
import jakarta.ws.rs.core.Response;

public class UnexpectedSpotifyException extends AppException{
    private final Exception exception;

    public UnexpectedSpotifyException(Exception exception) {
        super("got unexpected error while calling spotify", exception, true);
        this.exception = exception;
    }

    public UnexpectedSpotifyException(String message) {
        super("got unexpected error while calling spotify: " + message, null, true);
        this.exception = this;
    }

    @Override
    public Response getResponse() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorMessageDTO(
                "failed to call spotify",
                String.format("got %s with message %s", exception.getClass().getSimpleName(), exception.getMessage()),
                super.formatStackTraceToHtml(exception.getStackTrace())
        )).build();
    }
}
