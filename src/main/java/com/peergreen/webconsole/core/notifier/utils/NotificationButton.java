package com.peergreen.webconsole.core.notifier.utils;

import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * @author Mohammed Boukada
 */
public class NotificationButton {
    Button button;
    Window window;
    VerticalLayout windowContent;
    int badge;

    public NotificationButton(Button button, Window window, VerticalLayout windowContent, int badge) {
        this.button = button;
        this.window = window;
        this.windowContent = windowContent;
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
