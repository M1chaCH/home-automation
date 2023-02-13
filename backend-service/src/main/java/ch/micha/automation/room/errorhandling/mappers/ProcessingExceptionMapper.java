package ch.micha.automation.room.errorhandling.mappers;

import ch.micha.automation.room.errorhandling.ErrorMessageDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
@ApplicationScoped
public class ProcessingExceptionMapper implements ExceptionMapper<ProcessingException> {
    private final Logger logger = Logger.getLogger(ProcessingExceptionMapper.class.getSimpleName());

    @Override
    public Response toResponse(ProcessingException exception) {
        logger.log(Level.INFO, "got request with invalid body: {0}", exception.getMessage());

        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(new ErrorMessageDTO("bad request, a given JSON value didn't match the expected schema",
                        exception.getClass().getSimpleName() + ": " + exception.getMessage(), "not created"))
                .build();
    }
}
