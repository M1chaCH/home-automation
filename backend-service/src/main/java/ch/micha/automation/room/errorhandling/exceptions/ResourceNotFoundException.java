package ch.micha.automation.room.errorhandling.exceptions;

import ch.micha.automation.room.errorhandling.ErrorMessageDTO;
import jakarta.ws.rs.core.Response;

/**
 * This exception can be used to let a client know that a given resource could not be found.
 */
public class ResourceNotFoundException extends AppException{
    private final String resourceName;
    private final String resourceId;

    /**
     * will return 404 to the client with a message that contains the resource name and id.
     * @param resourceName a human-readable identifier for the not found resource
     * @param resourceId the given ID for the resource by the user.
     */
    public ResourceNotFoundException(String resourceName, String resourceId) {
        super(String.format("resource not found { name: %s | given id: %s }", resourceName, resourceId), null, false);
        this.resourceName = resourceName;
        this.resourceId = resourceId;
    }

    @Override
    public Response getResponse() {
        return Response
                .status(Response.Status.NOT_FOUND)
                .entity(new ErrorMessageDTO(String.format("did not find %s with id '%s'", resourceName, resourceId),
                        "", ""))
                .build();
    }
}
