package com.peergreen.webconsole.core.context;

import com.peergreen.webconsole.ISecurityManager;
import com.peergreen.webconsole.UIContext;
import com.vaadin.ui.UI;

/**
 * @author Mohammed Boukada
 */
public class BaseUIContext implements UIContext {

    private UI ui;
    private ISecurityManager securityManager;
    private String uiId;

    public BaseUIContext(UI ui, ISecurityManager securityManager, String uiId) {
        this.ui = ui;
        this.securityManager = securityManager;
        this.uiId = uiId;
    }

    @Override
    public ISecurityManager getSecurityManager() {
        return securityManager;
    }

    @Override
    public UI getUI() {
        return ui;
    }

    @Override
    public String getUIId() {
        return uiId;
    }
}
