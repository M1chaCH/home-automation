package ch.micha.automation.room.cache;

import java.time.LocalDateTime;

public class EntityCache<E> {
    private final int cacheLifetimeMin;
    private E cachedEntity;
    private LocalDateTime expiresAt;

    public EntityCache(int cacheLifetimeMin, E toCache) {
        this.cacheLifetimeMin = cacheLifetimeMin;
        this.cachedEntity = toCache;
    }

    public E load() {
        if(isValid())
            return cachedEntity;
        return null;
    }

    public void store(E toStore) {
        cachedEntity = toStore;
        expiresAt = LocalDateTime.now().plusMinutes(cacheLifetimeMin);
    }

    public boolean isValid() {
        if(expiresAt == null || cachedEntity == null)
            return false;

        return LocalDateTime.now().isBefore(expiresAt);
    }
}
