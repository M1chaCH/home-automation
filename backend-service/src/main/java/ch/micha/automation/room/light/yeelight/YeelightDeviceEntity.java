package ch.micha.automation.room.light.yeelight;

import com.mollin.yapi.YeelightDevice;

public record YeelightDeviceEntity (
        int id,
        String name,
        String ip, // this is the primary key to identify a yeelight device
        YeelightDevice light
        ){

    public static final int DEFAULT_PORT = 55443;
    /** (in ms) */
    public static final int DEFAULT_DURATION = 500;

    public boolean isOnline() {
        return light != null;
    }
}
