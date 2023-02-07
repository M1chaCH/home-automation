package ch.micha.automation.room.scene.presets;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Scene {
    protected final Logger logger = Logger.getLogger(getClass().getSimpleName());

    protected abstract void runScene();
    public abstract String getSceneId();

    public void apply() {
        logger.log(Level.INFO, "applying scene {0}", getSceneId());
        runScene();
        logger.log(Level.INFO, "applied scene");
    }
}
