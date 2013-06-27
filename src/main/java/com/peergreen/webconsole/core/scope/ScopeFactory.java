package com.peergreen.webconsole.core.scope;


import com.peergreen.webconsole.core.extension.InstanceHandler;

/**
 * @author Mohammed Boukada
 */
public class ScopeFactory {
    private String[] roles;
    private InstanceHandler instance;

    public ScopeFactory(String[] roles) {
        this.roles = roles;
    }

    public String[] getRoles() {
        return roles;
    }

    public InstanceHandler getInstance() {
        return instance;
    }

    public void setInstance(InstanceHandler instance) {
        this.instance = instance;
    }
}
