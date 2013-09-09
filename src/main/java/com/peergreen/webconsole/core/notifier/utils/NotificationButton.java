package com.peergreen.webconsole.core.notifier.utils;

import com.vaadin.ui.Button;

/**
 * Notification button descriptor
 *
 * @author Mohammed Boukada
 */
public class NotificationButton {

    /**
     * Notification button
     */
    private Button button;

    /**
     * Button badge
     */
    private int badge;

    /**
     * Create notification button descriptor
     *
     * @param button vaadin button
     * @param badge  badge value
     */
    public NotificationButton(Button button, int badge) {
        this.button = button;
        this.badge = badge;
    }

    /**
     * Get notification button
     *
     * @return notification button
     */
    public Button getButton() {
        return button;
    }

    /**
     * Get notification badge
     *
     * @return notification badge
     */
    public int getBadge() {
        return badge;
    }

    /**
     * Set notification badge
     *
     * @param badge new badge value
     */
    public void setBadge(int badge) {
        this.badge = badge;
    }

    /**
     * Increment badge value
     */
    public void incrementBadge() {
        this.badge++;
    }
}
