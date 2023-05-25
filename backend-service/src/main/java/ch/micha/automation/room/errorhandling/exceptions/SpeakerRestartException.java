package ch.micha.automation.room.errorhandling.exceptions;

import ch.micha.automation.room.errorhandling.ErrorMessageDTO;
import jakarta.ws.rs.core.Response;

public class SpeakerRestartException extends AppException{
    private final String message;
    private final String host;

    public SpeakerRestartException(String message, String host) {
        super(String.format("could not restart speaker at %s: %s", host, message), null, false);
        this.message = message;
        this.host = host;
    }

    @Override
    public Response getResponse() {
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(getErrorMessage())
                .build();
    }

    @Override
    public ErrorMessageDTO getErrorMessage() {
        return new ErrorMessageDTO(message,
            String.format("could not restart speaker at %s: %s", host, message), "");
    }
}
