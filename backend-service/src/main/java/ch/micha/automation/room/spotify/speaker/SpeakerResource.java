package ch.micha.automation.room.spotify.speaker;

import ch.micha.automation.room.events.Logged;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Logged
@Path("/automation/speaker")
@RequestScoped
public class SpeakerResource {
    private final SpeakerService service;

    @Inject
    public SpeakerResource(SpeakerService service) {
        this.service = service;
    }

    @PUT
    public Response restartSpeaker() {
        service.restartSpeaker();
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
