package com.peergreen.webconsole.core.notifier;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;

/**
 * @author Mohammed Boukada
 */
public class ScopeButton {
    Button scopeButton;
    UI buttonUi;
    int badge;

    public ScopeButton(Button scopeButton, UI buttonUi, int badge) {
        this.scopeButton = scopeButton;
        this.buttonUi = buttonUi;
        this.badge = badge;
    }

    public Button getScopeButton() {
        return scopeButton;
    }

    public void setScopeButton(Button scopeButton) {
        this.scopeButton = scopeButton;
    }

    public UI getButtonUi() {
        return buttonUi;
    }

    public void setButtonUi(UI buttonUi) {
        this.buttonUi = buttonUi;
    }

    public int getBadge() {
        return badge;
    }

    public void setBadge(int badge) {
        this.badge = badge;
    }
}
