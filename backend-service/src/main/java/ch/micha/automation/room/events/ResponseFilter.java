package ch.micha.automation.room.events;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.*;

import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

@Logged
@Provider
public class ResponseFilter implements ContainerResponseFilter {
    private final Logger logger = Logger.getLogger(ResponseFilter.class.getSimpleName());

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Instant startTime = (Instant) requestContext.getProperty(RequestFilter.REQUEST_START_TIME);

        logger.log(Level.INFO, "responding to {0}:{1} with {2} - {3} after {4}ms", new Object[]{
                requestContext.getMethod(),
                requestContext.getUriInfo().getAbsolutePath().getPath(),
                responseContext.getStatus(),
                responseContext.getStatusInfo().getReasonPhrase(),
                Instant.now().toEpochMilli() - startTime.toEpochMilli()
        });
    }
}
