package ch.micha.automation.room.scene;

import ch.micha.automation.room.scene.dtos.SceneDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class SceneRestService {
    private final SceneProvider provider;

    @Inject
    public SceneRestService(SceneProvider provider) {
        this.provider = provider;
    }

    public List<SceneDTO> loadScenes() {
        return provider.loadScenesAsDto();
    }
}
