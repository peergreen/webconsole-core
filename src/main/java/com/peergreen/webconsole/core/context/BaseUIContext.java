package com.peergreen.webconsole.core.context;

import com.peergreen.webconsole.ISecurityManager;
import com.peergreen.webconsole.UIContext;
import com.peergreen.webconsole.navigator.ViewNavigator;
import com.vaadin.ui.UI;

/**
 * @author Mohammed Boukada
 */
public class BaseUIContext implements UIContext {

    private UI ui;
    private ViewNavigator navigator;
    private ISecurityManager securityManager;
    private String uiId;

    public BaseUIContext(UI ui, ViewNavigator navigator, ISecurityManager securityManager, String uiId) {
        this.ui = ui;
        this.navigator = navigator;
        this.securityManager = securityManager;
        this.uiId = uiId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ISecurityManager getSecurityManager() {
        return securityManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UI getUI() {
        return ui;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ViewNavigator getViewNavigator() {
        return navigator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUIId() {
        return uiId;
    }
}
