package ch.micha.automation.room.events;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;

import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

@Logged
@Provider
public class RequestFilter implements ContainerRequestFilter {
    private final Logger logger = Logger.getLogger(RequestFilter.class.getSimpleName());

    @Override
    public void filter(ContainerRequestContext requestContext) {
        MultivaluedMap<String, String> queries = requestContext.getUriInfo().getQueryParameters();
        StringJoiner queryString = new StringJoiner(",");
        queries.forEach((query, values) -> {
            StringJoiner valueString = new StringJoiner(",");
            values.forEach(valueString::add);
            queryString.add(String.format("%s=%s", query, valueString));
        });

        logger.log(Level.INFO, "received request to {0}:{1}?{2}",
                new Object[]{requestContext.getMethod(), requestContext.getUriInfo().getAbsolutePath().getPath(), queryString.toString()});
    }
}
