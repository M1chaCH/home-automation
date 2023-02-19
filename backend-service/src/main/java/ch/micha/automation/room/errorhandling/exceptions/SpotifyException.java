package ch.micha.automation.room.errorhandling.exceptions;

import ch.micha.automation.room.errorhandling.ErrorMessageDTO;
import jakarta.ws.rs.core.Response;

public class SpotifyException extends AppException{
    private final String message;

    public SpotifyException(String message) {
        super("got expected spotify error: " + message, null, false);
        this.message = message;
    }

    @Override
    public Response getResponse() {
        return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorMessageDTO(
                "failed to call spotify",
                String.format("got with message %s", message),
                ""
        )).build();
    }
}
