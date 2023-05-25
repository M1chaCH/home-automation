package ch.micha.automation.room.errorhandling.exceptions;

import ch.micha.automation.room.errorhandling.ErrorMessageDTO;
import jakarta.ws.rs.core.Response;

public class ResourceAlreadyExistsException extends AppException{
    private final String resourceName;
    private final String resourceValue;

    public ResourceAlreadyExistsException(String resourceName, String resourceValue) {
        super(String.format("resource already exists: %s -> %s", resourceName, resourceValue), null, false);
        this.resourceName = resourceName;
        this.resourceValue = resourceValue;
    }

    @Override
    public Response getResponse() {
        return Response.status(Response.Status.BAD_REQUEST).entity(getErrorMessage()).build();
    }

    @Override
    public ErrorMessageDTO getErrorMessage() {
        return new ErrorMessageDTO(
            String.format("given %s already exists", resourceName),
            String.format("%s like '%s' already exists", resourceName, resourceValue),
            ""
        );
    }
}
