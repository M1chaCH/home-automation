package ch.micha.automation.room.errorhandling.mappers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
@ApplicationScoped
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {
    private final Logger logger = Logger.getLogger(NotFoundExceptionMapper.class.getSimpleName());

    @Override
    public Response toResponse(NotFoundException exception) {
        logger.log(Level.INFO, "got request to invalid path");

        return Response
                .status(Response.Status.NOT_FOUND)
                .build();
    }
}
