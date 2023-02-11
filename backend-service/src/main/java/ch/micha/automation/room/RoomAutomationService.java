package ch.micha.automation.room;

import ch.micha.automation.room.scene.SceneService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class RoomAutomationService {
    private final Logger logger = Logger.getLogger(getClass().getSimpleName());
    private final SceneService sceneService;

    private boolean roomOn = false;

    @Inject
    public RoomAutomationService(SceneService sceneService) {
        this.sceneService = sceneService;
    }

    public void toggleRoom() {
        if(!roomOn) {
            logger.log(Level.INFO, "toggling room ON");
            sceneService.applyDefaultScene();
        } else {
            logger.log(Level.INFO, "toggling room OFF");
            sceneService.shutdown();
        }
        roomOn = !roomOn;
    }
}
