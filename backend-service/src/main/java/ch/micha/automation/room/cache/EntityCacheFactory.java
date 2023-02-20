package ch.micha.automation.room.cache;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class EntityCacheFactory {

    private final int configuredCacheLifetime;

    @Inject
    public EntityCacheFactory(@ConfigProperty(name = "room.automation.cache.lifetime") int configuredCacheLifetime) {
        this.configuredCacheLifetime = configuredCacheLifetime;
    }

    public <T> EntityCache<T> build() {
        return new EntityCache<>(configuredCacheLifetime, null);
    }

    public <T> EntityCache<T> build(T initial) {
        return new EntityCache<>(configuredCacheLifetime, initial);
    }
}
