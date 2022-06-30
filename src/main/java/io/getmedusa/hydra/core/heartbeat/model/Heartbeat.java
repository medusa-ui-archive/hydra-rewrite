package io.getmedusa.hydra.core.heartbeat.model;

//combined model for heartbeat channel
//works for both hydra and medusa
//is always only partially filled up

import io.getmedusa.hydra.core.discovery.model.meta.ActiveService;

import java.util.List;
import java.util.Map;

public class Heartbeat {
    private String name;
    private Map<String, List<ActiveService>> serviceMap;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, List<ActiveService>> getServiceMap() {
        return serviceMap;
    }

    public void setServiceMap(Map<String, List<ActiveService>> serviceMap) {
        this.serviceMap = serviceMap;
    }

}
