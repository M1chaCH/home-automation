package ch.micha.automation.room.scene;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.logging.Logger;

@ApplicationScoped
public class SceneRestService {
    private final Logger logger = Logger.getLogger(SceneRestService.class.getSimpleName());

    private final SceneProvider provider;

    @Inject
    public SceneRestService(SceneProvider provider) {
        this.provider = provider;
    }
}
