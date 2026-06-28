package org.heartattack.heartattacklibs.dependency;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ProviderRegistry {
    private final Map<DependencyCapability, Object> providers = new ConcurrentHashMap<>();

    public <T> void register(DependencyCapability capability, T provider) {
        providers.put(capability, provider);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(DependencyCapability capability, Class<T> type) {
        Object provider = providers.get(capability);
        if (provider == null || !type.isInstance(provider)) {
            return Optional.empty();
        }
        return Optional.of((T) provider);
    }
}
