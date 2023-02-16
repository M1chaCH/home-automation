package ch.micha.automation.room.spotify;

import ch.micha.automation.room.events.Logged;
import ch.micha.automation.room.spotify.dtos.SpotifyAuthorisationDTO;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Logged
@Path("/automation/spotify")
@RequestScoped
public class SpotifyResource {
    private final SpotifyService service;


    @Inject
    public SpotifyResource(SpotifyService service) {
        this.service = service;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response isConnected() {
        return Response
                .status(Response.Status.OK)
                .entity(service.getAccess())
                .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response storeConnection(SpotifyAuthorisationDTO dto) {
        service.addAuthorisation(dto);
        return Response
                .status(Response.Status.CREATED)
                .build();
    }
}
