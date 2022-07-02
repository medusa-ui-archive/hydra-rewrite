package io.getmedusa.hydra.core.discovery.model;

import java.util.List;
import java.util.Map;

public class FragmentRequestWrapper {

    private Map<String, Object> attributes;
    private List<Fragment> requests;

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public List<Fragment> getRequests() {
        return requests;
    }

    public void setRequests(List<Fragment> requests) {
        this.requests = requests;
    }
}
