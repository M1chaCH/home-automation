package ch.micha.automation.room.exceptions;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {
    private final Logger logger = Logger.getLogger(RuntimeExceptionMapper.class.getSimpleName());

    @Override
    public Response toResponse(RuntimeException exception) {
        logger.log(Level.SEVERE, "got UNKNOWN internal server error!", exception);

        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorMessageDTO("something went wrong on our end",
                        exception.getClass().getSimpleName() + ": " + exception.getMessage(), "not created"))
                .build();
    }
}
