package io.getmedusa.hydra.core.heartbeat.model;

import io.getmedusa.hydra.core.heartbeat.model.meta.RenderedFragment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RenderedFragmentsWrapper {

    private List<RenderedFragment> fragments;

    private Map<String, Object> attributesAfterRender = new HashMap<>();

    public List<RenderedFragment> getFragments() {
        return fragments;
    }

    public void setFragments(List<RenderedFragment> fragments) {
        this.fragments = fragments;
    }

    public Map<String, Object> getAttributesAfterRender() {
        return attributesAfterRender;
    }

    public void setAttributesAfterRender(Map<String, Object> attributesAfterRender) {
        this.attributesAfterRender = attributesAfterRender;
    }

    @Override
    public String toString() {
        return "RenderedFragmentsWrapper{" +
                "fragments=" + fragments +
                ", attributesAfterRender=" + attributesAfterRender +
                '}';
    }
}
