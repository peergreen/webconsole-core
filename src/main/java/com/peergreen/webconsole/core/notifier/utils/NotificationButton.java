package com.peergreen.webconsole.core.notifier.utils;

import com.vaadin.ui.Button;

/**
 * @author Mohammed Boukada
 */
public class NotificationButton {
    private Button button;
    private int badge;

    public NotificationButton(Button button, int badge) {
        this.button = button;
        this.badge = badge;
    }

    public Button getButton() {
        return button;
    }

    public int getBadge() {
        return badge;
    }

    public void setBadge(int badge) {
        this.badge = badge;
    }

    public void incrementBadge() {
        this.badge++;
    }
}
