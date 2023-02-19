package ch.micha.automation.room.errorhandling.exceptions;

import ch.micha.automation.room.errorhandling.ErrorMessageDTO;
import jakarta.ws.rs.core.Response;

public class SpotifyNotAuthorizedException extends AppException{

    public SpotifyNotAuthorizedException() {
        super("tried to access spotify auth, but was never configured", null, false);
    }

    @Override
    public Response getResponse() {
        return Response
                .status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorMessageDTO("spotify not authorized",
                        "you need to authorize spotify using the POST endpoint or the UI.",
                        ""))
                .build();
    }
}
