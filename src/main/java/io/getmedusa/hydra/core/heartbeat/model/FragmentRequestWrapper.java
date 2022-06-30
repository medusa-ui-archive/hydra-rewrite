package io.getmedusa.hydra.core.heartbeat.model;

import io.getmedusa.hydra.core.heartbeat.model.meta.FragmentRequest;

import java.util.List;
import java.util.Map;

public class FragmentRequestWrapper {

    private Map<String, Object> attributes;
    private Map<String, List<FragmentRequest>> requests;

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, List<FragmentRequest>> getRequests() {
        return requests;
    }

    public void setRequests(Map<String, List<FragmentRequest>> requests) {
        this.requests = requests;
    }
}
