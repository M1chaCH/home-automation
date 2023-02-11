package ch.micha.automation.room.errorhandling.mappers;

import ch.micha.automation.room.errorhandling.exceptions.AppException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Listens for all thrown AppExceptions. And since all custom exceptions should inherit AppException, this is some sort
 * of REST custom exception handler.
 */
@Provider
@ApplicationScoped
public class AppExceptionMapper implements ExceptionMapper<AppException> {
    private final Logger logger = Logger.getLogger(AppExceptionMapper.class.getSimpleName());

    @Override
    public Response toResponse(AppException exception) {
        if(exception.isServerError())
            logger.log(Level.SEVERE, "got internal server error", exception);
        else
            logger.log(Level.INFO, "caught exception --> {0}", exception.getMessage());

        return exception.getResponse();
    }
}
