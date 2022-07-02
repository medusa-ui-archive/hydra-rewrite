package io.getmedusa.hydra.core.repository.meta;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.getmedusa.hydra.core.discovery.model.meta.ActiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class InMemoryStorage {

    private String overallRouteHashKey = null;

    private final Cache<String, List<ActiveService>> serviceMap = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build();

    private static final Logger logger = LoggerFactory.getLogger(InMemoryStorage.class);

    public String getOverallRouteHashKey() {
        return overallRouteHashKey;
    }

    public void setOverallRouteHashKey(String overallRouteHashKey) {
        this.overallRouteHashKey = overallRouteHashKey;
    }

    public void storeActiveService(String key, ActiveService service) {
        List<ActiveService> activeServices = serviceMap.get(key, s -> new ArrayList<>());
        activeServices.add(service);
        logger.info("Registering service: {}", service.toString());
        serviceMap.put(key, activeServices);
    }

    public void removeActiveService(String key) {
        serviceMap.invalidate(key);
    }

    public Map<String, List<ActiveService>> retrieveServiceMap() {
        return serviceMap.asMap();
    }

    public ActiveService findService(String key) {
        final List<ActiveService> services = serviceMap.getIfPresent(key);
        if(services != null) {
            return services.get(0);
        } else {
            return null;
        }
    }

    public void updateAlive(String key) {
        final List<ActiveService> services = serviceMap.getIfPresent(key);
        if(null != services) {
            serviceMap.put(key, services);
        } else {
            throw new IllegalStateException("Received request for alive to be updated for non-registered service, must register first");
        }
    }
}
