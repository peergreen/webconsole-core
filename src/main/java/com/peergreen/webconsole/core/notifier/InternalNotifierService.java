package com.peergreen.webconsole.core.notifier;

import com.peergreen.webconsole.INotifierService;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Internal notifier service
 * @author Mohammed Boukada
 */
public interface InternalNotifierService extends INotifierService {
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
     * Add notifications buttons
     * @param button
     * @param window
     */
    void addNotificationsButton(Button button, Window window, UI ui);

    /**
     * Add task bar
     * @param tasksBar
     * @param ui
     */
    void addTasksBar(HorizontalLayout tasksBar, UI ui);

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
     * Clear UI component in notifier service
     * @param ui
     */
    void clearComponentsForUI(UI ui);
}
