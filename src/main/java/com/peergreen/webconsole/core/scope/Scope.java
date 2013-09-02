package com.peergreen.webconsole.core.scope;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mohammed Boukada
 */
public class Scope {
    private String scopeName;
    private String scopeAlias;
    private String scopeIconClass;
    private Component scopeView;
    private Button scopeMenuButton;
    private Map<String, Method> navigatorCallbacks = new HashMap<>();

    public Scope(String scopeName, String scopeAlias, String scopeIconClass, Component scopeView) {
        this.scopeName = scopeName;
        this.scopeAlias = scopeAlias;
        this.scopeIconClass = scopeIconClass;
        this.scopeView = scopeView;
    }

    public String getScopeName() {
        return scopeName;
    }

    public String getScopeAlias() {
        return scopeAlias;
    }

    public String getScopeIconClass() {
        return scopeIconClass;
    }

    public void setScopeName(String scopeName) {
        this.scopeName = scopeName;
    }

    public Component getScopeView() {
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

    public Map<String, Method> getNavigatorCallbacks() {
        return navigatorCallbacks;
    }
}
