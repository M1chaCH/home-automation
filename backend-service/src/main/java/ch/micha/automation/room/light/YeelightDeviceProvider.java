package ch.micha.automation.room.light;

import ch.micha.automation.room.events.OnAppStartupListener;
import ch.micha.automation.room.sql.SQLService;
import com.moppletop.yeelight.api.YeeApi;
import com.moppletop.yeelight.api.YeeApiBuilder;
import com.moppletop.yeelight.api.model.YeeLight;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class YeelightDeviceProvider implements OnAppStartupListener {
    private final Logger logger = Logger.getLogger(getClass().getName());
    private final SQLService sqlService;

    private List<YeelightDeviceEntity> devices;

    @Inject
    public YeelightDeviceProvider(SQLService sqlService) {
        this.sqlService = sqlService;
    }

    @Override
    public void onAppStartup() {
        logger.info("init lights");

        final YeeApi api = new YeeApiBuilder().build();
        this.devices = mapEntitiesWithDevices(api.getLights().stream().toList());
    }

    private List<YeelightDeviceEntity> mapEntitiesWithDevices(List<YeeLight> devices) {
        final List<YeelightDeviceEntity> storedDevices = getEntitiesFromDB();
        return null;
    }

    private List<YeelightDeviceEntity> getEntitiesFromDB() {


        return null;
    }
}
