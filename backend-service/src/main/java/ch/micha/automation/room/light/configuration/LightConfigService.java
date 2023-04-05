package ch.micha.automation.room.light.configuration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class LightConfigService {
    private final LightConfigProvider provider;

    @Inject
    public LightConfigService(LightConfigProvider provider) {
        this.provider = provider;
    }

    public List<LightConfig> loadLightConfigs() {
        return provider.findConfigs().stream().toList();
    }

    public LightConfig createConfig(LightConfig config) {
        return provider.createConfig(config.name(), config.red(), config.green(), config.blue(), config.brightness());
    }

    public void updateConfig(LightConfig config) {
        provider.updateConfig(config.id(), config.name(), config.red(), config.green(), config.blue(), config.brightness());
    }

    public void deleteConfig(int id) {
        provider.deleteConfig(id);
    }
}
