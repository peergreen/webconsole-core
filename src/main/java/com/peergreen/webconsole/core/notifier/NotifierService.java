/**
 * DISCLAIMER
 *
 * The quality of the code is such that you should not copy any of it as best
 * practice how to build Vaadin applications.
 *
 * @author jouni@vaadin.com
 *
 */

package com.peergreen.webconsole.core.notifier;

import com.peergreen.webconsole.NotificationOverlay;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Notifier service
 * @author Mohammed Boukada
 */
@Component
@Provides
@Instantiate
public class NotifierService implements INotifierService {

    /**
     * List of overlays
     */
    private List<NotificationOverlay> overlays = new ArrayList<>();

    /**
     * Scope buttons in each view
     */
    private ConcurrentHashMap<com.vaadin.ui.Component, ScopeButton> scopesButtons = new ConcurrentHashMap<>();
    /**
     * Close all overlays
     */
    public void closeAll() {
        for (NotificationOverlay overlay : overlays) {
            overlay.close();
        }
        overlays.clear();
    }

    /** {@inheritDoc}
     */
    @Override
    public void addNotification(String notification) {
        Notification.show(notification);
    }

    /** {@inheritDoc}
     */
    public NotificationOverlay addOverlay(String caption, String text, String style) {
        NotificationOverlay o = new NotificationOverlay();
        o.setCaption(caption);
        o.addComponent(new Label(text, ContentMode.HTML));
        o.setStyleName(style);
        overlays.add(o);
        return o;
    }

    /** {@inheritDoc}
     */
    public void addScopeButton(com.vaadin.ui.Component scope, Button button, UI ui, boolean notify) {
        scopesButtons.put(scope, new ScopeButton(button, ui, 0));
        if (notify) {
            setBadgeAsNew(button, ui);
        }
    }

    /** {@inheritDoc}
     */
    public void removeScopeButton(com.vaadin.ui.Component scope) {
        if (scopesButtons.containsKey(scope)) {
            scopesButtons.remove(scope);
        }
    }

    /** {@inheritDoc}
     */
    public void hideScopeButton(com.vaadin.ui.Component scope) {
        if (scopesButtons.containsKey(scope)) {
            scopesButtons.get(scope).getScopeButton().setVisible(false);
        }
    }

    /** {@inheritDoc}
     * @param scope
     */
    public void removeBadge(com.vaadin.ui.Component scope) {
        updateBadge(scope, 0);
        scopesButtons.get(scope).getScopeButton().setHtmlContentAllowed(true);
        setCaption(scopesButtons.get(scope).getScopeButton(),
                   scopesButtons.get(scope).getButtonUi(),
                   getInitialCaption(scopesButtons.get(scope).getScopeButton()));
    }

    /** {@inheritDoc}
     */
    public void incrementBadge(com.vaadin.ui.Component scope) {
        if (scopesButtons.containsKey(scope)) {
            updateBadge(scope, +1);
            scopesButtons.get(scope).getScopeButton().setVisible(true);
            scopesButtons.get(scope).getScopeButton().setHtmlContentAllowed(true);
            String newCaption = getInitialCaption(scopesButtons.get(scope).getScopeButton()) +
                    "<span class=\"badge\">" + scopesButtons.get(scope).getBadge() +"</span>";
            setCaption(scopesButtons.get(scope).getScopeButton(),
                       scopesButtons.get(scope).getButtonUi(),
                       newCaption);
        }
    }

    /** {@inheritDoc}
     */
    public void decrementBadge(com.vaadin.ui.Component scope) {
        if (scopesButtons.containsKey(scope)) {
            updateBadge(scope, -1);
            scopesButtons.get(scope).getScopeButton().setHtmlContentAllowed(true);
            String newCaption = getInitialCaption(scopesButtons.get(scope).getScopeButton()) +
                    ((scopesButtons.get(scope).getBadge() == 0) ? "" : "<span class=\"badge\">" +
                            scopesButtons.get(scope).getBadge() +"</span>");
            setCaption(scopesButtons.get(scope).getScopeButton(),
                       scopesButtons.get(scope).getButtonUi(),
                       newCaption);
        }
    }

    /**
     * Set badge as new
     * @param button
     */
    private void setBadgeAsNew(final Button button, UI ui) {
        button.setHtmlContentAllowed(true);
        final String newCaption = getInitialCaption(button) +
                "<span class=\"badge\">new</span>";
        setCaption(button, ui, newCaption);

    }

    private void setCaption(final Button button, UI ui, final String caption) {
        ui.access(new Runnable() {
            @Override
            public void run() {
                button.setCaption(caption);
            }
        });
    }

    /**
     * Update badge when it is changed
     * @param scope
     * @param op
     */
    private void updateBadge(com.vaadin.ui.Component scope, int op) {
        if (scopesButtons.containsKey(scope)) {
            Integer badge = scopesButtons.get(scope).getBadge();
            if (op == 0) {
                badge = 0;
            } else if (op == +1) {
                badge++;
            } else if (op == -1 && badge > 0) {
                badge--;
            }
            scopesButtons.get(scope).setBadge(badge);
        }
    }

    /**
     * Get initial caption of the button
     * @param button
     * @return
     */
    private String getInitialCaption(Button button) {
        if (button.getCaption().contains("<span")) {
            return button.getCaption().substring(0, button.getCaption().indexOf("<span"));
        }
        return button.getCaption();
    }

}
