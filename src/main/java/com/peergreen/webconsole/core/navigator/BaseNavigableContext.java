package com.peergreen.webconsole.core.navigator;

import com.peergreen.webconsole.navigator.NavigableContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mohammed Boukada
 */
public class BaseNavigableContext implements NavigableContext {

    private String path;
    private Map<String, Object> properties;

    public BaseNavigableContext(String path) {
        this.path = path;
        this.properties = new HashMap<>();
    }

    public BaseNavigableContext(String path, Map<String, Object> properties) {
        this.path = path;
        this.properties = properties;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }
}
