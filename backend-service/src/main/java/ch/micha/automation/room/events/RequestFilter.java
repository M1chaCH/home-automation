package ch.micha.automation.room.events;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

import java.util.logging.Level;
import java.util.logging.Logger;

@Logged
@Provider
public class RequestFilter implements ContainerRequestFilter {
    private final Logger logger = Logger.getLogger(RequestFilter.class.getSimpleName());

    @Override
    public void filter(ContainerRequestContext requestContext) {
        logger.log(Level.INFO, "received request to {0}:{1}",
                new Object[]{requestContext.getMethod(), requestContext.getUriInfo().getAbsolutePath().getPath()});
    }
}
