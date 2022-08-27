package io.getmedusa.hydra.core.discovery.model;

import io.getmedusa.hydra.core.discovery.model.meta.ActiveService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActiveServiceOverview {

    private String serviceName;
    private List<ActiveService> activeServices;

    public static List<ActiveServiceOverview> of(Map<String, List<ActiveService>> activeServicesMap) {
        List<ActiveServiceOverview> list = new ArrayList<>();

        for(Map.Entry<String, List<ActiveService>> entry : activeServicesMap.entrySet()) {
            ActiveServiceOverview overview = new ActiveServiceOverview();
            overview.setServiceName(entry.getKey());
            overview.setActiveServices(entry.getValue());
            list.add(overview);
        }

        return list;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<ActiveService> getActiveServices() {
        return activeServices;
    }

    public void setActiveServices(List<ActiveService> activeServices) {
        this.activeServices = activeServices;
    }
}
