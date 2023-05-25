package ch.micha.automation.room.errorhandling.exceptions;

import ch.micha.automation.room.errorhandling.ErrorMessageDTO;
import jakarta.ws.rs.core.Response;

public class SpotifyAlreadyAuthorizedException extends AppException{

    public SpotifyAlreadyAuthorizedException() {
        super("tried to add a second auth to spotify", null, false);
    }

    @Override
    public Response getResponse() {
        return Response
                .status(Response.Status.FORBIDDEN)
                .entity(getErrorMessage())
                .build();
    }

    @Override
    public ErrorMessageDTO getErrorMessage() {
        return new ErrorMessageDTO("spotify already authorized",
            "You already have access to spotify, this step is redundant",
            "");
    }
}
