package com.peergreen.webconsole.core.notifier;

import com.peergreen.webconsole.NotificationOverlay;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;

/**
 * Notifier service
 * @author Mohammed Boukada
 */
public interface INotifierService {

    /**
     * Add an overlay
     * @param caption
     * @param text
     * @param style
     * @return
     */
    NotificationOverlay addOverlay(String caption, String text, String style);

    /**
     * Add scope button reference
     * @param scope
     * @param button
     * @param notify
     */
    void addScopeButton(Component scope, Button button, UI ui, boolean notify);

    /**
     * Remove scope button reference
     * @param scope
     */
    void removeScopeButton(Component scope);

    /**
     * Hide scope button from menu
     * @param scope
     */
    void hideScopeButton(Component scope);

    /**
     * Remove badge from scope button in menu
     * @param scope
     */
    void removeBadge(Component scope);

    /**
     * Increment badge in scope button in menu
     * @param scope
     */
    void incrementBadge(Component scope);

    /**
     * Decrement badge in scope button in menu
     * @param scope
     */
    void decrementBadge(Component scope);

    /**
     * Close all overlays
     */
    void closeAll();

    /**
     * Add a notification
     * @param notification
     */
    void addNotification(String notification);
}
