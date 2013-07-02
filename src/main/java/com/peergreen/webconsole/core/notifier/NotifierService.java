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
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Notifier service
 * @author Mohammed Boukada
 */
@Component
@Provides
@Instantiate
public class NotifierService implements INotifierService, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * List of overlays
     */
    private List<NotificationOverlay> overlays = new ArrayList<>();

    /**
     * Scope buttons in each view
     */
    private Map<com.vaadin.ui.Component, ScopeButton> scopesButtons = new ConcurrentHashMap<>();
    private List<NotificationButton> notificationButtons = new CopyOnWriteArrayList<>();

    // TODO handle concurrent access
    private Map<String, Long> notifications = new LinkedHashMap<>();

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
        notifications.put(notification, System.currentTimeMillis());
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

    @Override
    public void addNotificationsButton(Button button, final Window window, UI ui) {
        final VerticalLayout l = new VerticalLayout();
        l.setMargin(true);
        l.setSpacing(true);
        window.setContent(l);

        notificationButtons.add(new NotificationButton(button, window, l, 0));

//        for (int j=0; j<15; j++) {
//            notifications.put("Notification " + j, System.currentTimeMillis());
//        }

        button.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                l.removeAllComponents();
                int i = 1;
                for (Map.Entry<String, Long> notification : notifications.entrySet()) {
                    Label notif = new Label("<hr><b>" + notification.getKey() + "</b><br><span>" +
                            TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - notification.getValue()) +
                            " minutes ago</span><br>", ContentMode.HTML);
                    l.addComponentAsFirst(notif);
                    i++;
                    if (i > 5) break;
                }
            }
        });
    }

    /** {@inheritDoc}
     */
    public void hideScopeButton(com.vaadin.ui.Component scope) {
        if (scopesButtons.containsKey(scope)) {
            scopesButtons.get(scope).getButton().setVisible(false);
        }
    }

    /** {@inheritDoc}
     * @param scope
     */
    public void removeBadge(com.vaadin.ui.Component scope) {
        updateBadge(scope, 0);
        scopesButtons.get(scope).getButton().setHtmlContentAllowed(true);
        setCaption(scopesButtons.get(scope).getButton(),
                   scopesButtons.get(scope).getButtonUi(),
                   getInitialCaption(scopesButtons.get(scope).getButton()));
    }

    /** {@inheritDoc}
     */
    public void incrementBadge(com.vaadin.ui.Component scope) {
        if (scopesButtons.containsKey(scope)) {
            updateBadge(scope, +1);
            scopesButtons.get(scope).getButton().setVisible(true);
            scopesButtons.get(scope).getButton().setHtmlContentAllowed(true);
            String newCaption = getInitialCaption(scopesButtons.get(scope).getButton()) +
                    "<span class=\"badge\">" + scopesButtons.get(scope).getBadge() +"</span>";
            setCaption(scopesButtons.get(scope).getButton(),
                       scopesButtons.get(scope).getButtonUi(),
                       newCaption);
        }
    }

    /** {@inheritDoc}
     */
    public void decrementBadge(com.vaadin.ui.Component scope) {
        if (scopesButtons.containsKey(scope)) {
            updateBadge(scope, -1);
            scopesButtons.get(scope).getButton().setHtmlContentAllowed(true);
            String newCaption = getInitialCaption(scopesButtons.get(scope).getButton()) +
                    ((scopesButtons.get(scope).getBadge() == 0) ? "" : "<span class=\"badge\">" +
                            scopesButtons.get(scope).getBadge() +"</span>");
            setCaption(scopesButtons.get(scope).getButton(),
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
