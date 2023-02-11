package ch.micha.automation.room.light;

import com.moppletop.yeelight.api.model.YeeLight;

public record YeelightDeviceEntity (
        int id, // id of YeeLight
        String name,
        YeeLight light
        ){

    public boolean isOnline() {
        return light != null;
    }
}
