package com.peergreen.webconsole.core.scope;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;

/**
 * @author Mohammed Boukada
 */
public class Scope {
    private String scopeName;
    private Component scopeView;
    private Button scopeMenuButton;

    public Scope(String scopeName, Component scopeView) {
        this.scopeName = scopeName;
        this.scopeView = scopeView;
    }

    public String getScopeName() {
        return scopeName;
    }

    public void setScopeName(String scopeName) {
        this.scopeName = scopeName;
    }

    public Object getScopeView() {
        return scopeView;
    }

    public void setScopeView(Component scopeView) {
        this.scopeView = scopeView;
    }

    public Button getScopeMenuButton() {
        return scopeMenuButton;
    }

    public void setScopeMenuButton(Button scopeMenuButton) {
        this.scopeMenuButton = scopeMenuButton;
    }
}
