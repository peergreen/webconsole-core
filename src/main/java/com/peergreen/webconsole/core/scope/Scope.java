package com.peergreen.webconsole.core.scope;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;

/**
 * Scope descriptor
 *
 * @author Mohammed Boukada
 */
public class Scope {

    /**
     * Scope name
     */
    private String scopeName;

    /**
     * Scope alias
     */
    private String scopeAlias;

    /**
     * Scope icon class
     */
    private String scopeIconClass;

    /**
     * Scope view
     */
    private Component scopeView;

    /**
     * Scope menu button
     */
    private Button scopeMenuButton;

    /**
     * Create new scope descriptor
     *
     * @param scopeName      scope name
     * @param scopeAlias     scope alias
     * @param scopeIconClass scope icon class
     * @param scopeView      scope view
     */
    public Scope(String scopeName, String scopeAlias, String scopeIconClass, Component scopeView) {
        this.scopeName = scopeName;
        this.scopeAlias = scopeAlias;
        this.scopeIconClass = scopeIconClass;
        this.scopeView = scopeView;
    }

    /**
     * Get scope name
     *
     * @return scope name
     */
    public String getScopeName() {
        return scopeName;
    }

    /**
     * Get scope alias
     *
     * @return scope alias
     */
    public String getScopeAlias() {
        return scopeAlias;
    }

    /**
     * Get scope icon class
     *
     * @return scope icon class
     */
    public String getScopeIconClass() {
        return scopeIconClass;
    }

    /**
     * Get scope view
     *
     * @return scope view
     */
    public Component getScopeView() {
        return scopeView;
    }

    /**
     * Get scope menu button
     *
     * @return scope menu button
     */
    public Button getScopeMenuButton() {
        return scopeMenuButton;
    }

    /**
     * set scope menu button
     *
     * @param scopeMenuButton scope menu button
     */
    public void setScopeMenuButton(Button scopeMenuButton) {
        this.scopeMenuButton = scopeMenuButton;
    }
}
