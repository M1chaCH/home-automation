package ch.micha.automation.room.scene;

import ch.micha.automation.room.scene.presets.Scene;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class SceneProvider {
    private final Logger logger = Logger.getLogger(getClass().getName());

    @Inject
    Instance<Scene> scenes;

    public Scene findSceneById(String sceneId) {
        for (Scene scene : scenes) {
            if(scene.getSceneId().equals(sceneId))
                return scene;
        }

        logger.log(Level.WARNING, "could not find scene ({0}), returning null", sceneId);
        return null;
    }
}
