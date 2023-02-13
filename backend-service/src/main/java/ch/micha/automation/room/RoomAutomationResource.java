package ch.micha.automation.room;

import ch.micha.automation.room.events.Logged;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Logged
@Path("/automation")
@RequestScoped()
public class RoomAutomationResource {

    private final RoomAutomationService service;

    @Inject
    public RoomAutomationResource(RoomAutomationService service) {
        this.service = service;
    }

    @PUT()
    @Produces(MediaType.APPLICATION_JSON)
    public Response toggleRoom() {
        service.toggleRoom();
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
