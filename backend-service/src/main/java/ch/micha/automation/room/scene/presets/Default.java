package ch.micha.automation.room.scene.presets;

import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class Default extends Scene {

    @Override
    protected void runScene() {

    }

    @Override
    public String getSceneId() {
        return "default";
    }
}
