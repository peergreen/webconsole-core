package com.peergreen.webconsole.core.navigator;

import com.peergreen.webconsole.navigator.NavigationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mohammed Boukada
 */
public class BaseNavigationContext implements NavigationContext {

    private String path;
    private Map<String, Object> properties;

    public BaseNavigationContext(String path) {
        this.path = path;
        this.properties = new HashMap<>();
    }

    public BaseNavigationContext(String path, Map<String, Object> properties) {
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
