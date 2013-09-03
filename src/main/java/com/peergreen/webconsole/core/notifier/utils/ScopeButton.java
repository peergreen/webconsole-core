package com.peergreen.webconsole.core.notifier.utils;

import com.vaadin.ui.Button;
import com.vaadin.ui.UI;

/**
 * @author Mohammed Boukada
 */
public class ScopeButton {
    private Button button;
    private UI buttonUi;
    private int badge;

    public ScopeButton(Button button, UI buttonUi, int badge) {
        this.button = button;
        this.buttonUi = buttonUi;
        this.badge = badge;
    }

    public Button getButton() {
        return button;
    }

    public void setButton(Button button) {
        this.button = button;
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
