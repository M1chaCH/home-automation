package ch.micha.automation.room.exceptions;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class AppExceptionMapper implements ExceptionMapper<AppException> {
    private final Logger logger = Logger.getLogger(AppExceptionMapper.class.getSimpleName());

    @Override
    public Response toResponse(AppException exception) {
        if(exception.isServerError())
            logger.log(Level.SEVERE, "got internal server error", exception);

        return exception.getResponse();
    }
}
