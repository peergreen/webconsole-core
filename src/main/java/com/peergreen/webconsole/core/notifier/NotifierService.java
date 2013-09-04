package com.peergreen.webconsole.core.notifier;

import com.peergreen.webconsole.NotificationOverlay;
import com.peergreen.webconsole.core.notifier.utils.Notification;
import com.peergreen.webconsole.core.notifier.utils.NotificationButton;
import com.peergreen.webconsole.core.notifier.utils.ScopeButton;
import com.peergreen.webconsole.core.notifier.utils.Task;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Notifier service implementation
 * @author Mohammed Boukada
 */
@Component
@Provides
@Instantiate
public class NotifierService implements InternalNotifierService, Serializable {

    private static final long serialVersionUID = 1L;

    private List<NotificationOverlay> overlays = new ArrayList<>();
    private Map<com.vaadin.ui.Component, ScopeButton> scopesButtons = new ConcurrentHashMap<>();
    private Map<UI, NotificationButton> notificationButtons = new ConcurrentHashMap<>();
    private Map<UI, HorizontalLayout> tasksBars = new ConcurrentHashMap<>();
    private ConcurrentLinkedDeque<Notification> notifications = new ConcurrentLinkedDeque<>();
    private Queue<Task> tasks = new ConcurrentLinkedQueue<>();
    private Task currentTask;

    /**
     * Close all overlays
     */
    public void closeAll() {
        for (NotificationOverlay overlay : overlays) {
            overlay.close();
        }
        overlays.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addNotification(String notification) {
        notifications.add(new Notification(notification, System.currentTimeMillis()));
        for (final Map.Entry<UI, NotificationButton> notificationButtonEntry : notificationButtons.entrySet()) {
            UI ui = notificationButtonEntry.getKey();
            if (!ui.isClosing()) {
                ui.access(new Runnable() {
                    @Override
                    public void run() {
                        notificationButtonEntry.getValue().incrementBadge();
                        updateNotificationBadge(notificationButtonEntry.getValue());
                    }
                });
            } else {
                clearComponentsForUI(ui);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startTask(Object worker, String message, Long contentLength) {
        tasks.add(new Task(worker, message, contentLength));
        showTask();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTask(final Object worker, final Long bytesReceived) {
        for (final Map.Entry<UI, HorizontalLayout> taskBar : tasksBars.entrySet()) {
            UI ui = taskBar.getKey();
            if (ui.isClosing()) {
                clearComponentsForUI(ui);
            } else {
                getTask(worker).updateTask(bytesReceived);
                ui.push();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopTask(Object worker) {
        if (currentTask.equals(getTask(worker))) {
            currentTask = null;
        }
        removeTask(worker);
        showTask();
        for (final Map.Entry<UI, HorizontalLayout> taskBar : tasksBars.entrySet()) {
            UI ui = taskBar.getKey();
            if (!ui.isClosing()) {
                ui.access(new Runnable() {
                    @Override
                    public void run() {
                        taskBar.getValue().removeAllComponents();
                    }
                });
            } else {
                clearComponentsForUI(ui);
            }
        }
    }

    /**
     * Show current task
     */
    private void showTask() {
        if (currentTask == null && tasks.size() > 0) {
            final Task task = tasks.peek();
            currentTask = task;
            for (final Map.Entry<UI, HorizontalLayout> taskBar : tasksBars.entrySet()) {
                UI ui = taskBar.getKey();
                if (ui.isClosing()) {
                    clearComponentsForUI(ui);
                } else {
                    ui.access(new Runnable() {
                        @Override
                        public void run() {
                            taskBar.getValue().removeAllComponents();
                            taskBar.getValue().addComponent(new Label(task.getMessage()));
                            ProgressIndicator progressIndicator = new ProgressIndicator();
                            task.addProgressIndicator(progressIndicator);
                            taskBar.getValue().addComponent(progressIndicator);
                        }
                    });
                }
            }
        }
    }

    /**
     * Get task
     * @param worker task worker
     * @return task descriptor
     */
    private Task getTask(Object worker) {
        for (Task task : tasks) {
            if (worker.equals(task.getWorker())) {
                return task;
            }
        }
        return null;
    }

    /**
     * Remove task
     * @param worker task worker
     */
    private void removeTask(Object worker) {
        for (Task task : tasks) {
            if (worker.equals(task.getWorker())) {
                tasks.remove(task);
                return;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearComponentsForUI(UI ui) {
        notificationButtons.remove(ui);
        tasksBars.remove(ui);
        List<com.vaadin.ui.Component> scopesButtonsToRemove = new ArrayList<>();
        for (Map.Entry<com.vaadin.ui.Component, ScopeButton> scopeButtonEntry : scopesButtons.entrySet()) {
            if (scopeButtonEntry.getValue().getButtonUi().equals(ui)) {
                scopesButtonsToRemove.add(scopeButtonEntry.getKey());
            }
        }
        for (com.vaadin.ui.Component scopeButtonToRemove : scopesButtonsToRemove) {
            scopesButtons.remove(scopeButtonToRemove);
        }
    }


    /**
     * {@inheritDoc}
     */
    public NotificationOverlay addOverlay(String caption, String text, String style) {
        NotificationOverlay o = new NotificationOverlay();
        o.setCaption(caption);
        o.addComponent(new Label(text, ContentMode.HTML));
        o.setStyleName(style);
        overlays.add(o);
        return o;
    }

    /**
     * {@inheritDoc}
     */
    public void addScopeButton(com.vaadin.ui.Component scope, Button button, UI ui, boolean notify) {
        scopesButtons.put(scope, new ScopeButton(button, ui, 0));
        if (notify) {
            setBadgeAsNew(button, ui);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeScopeButton(com.vaadin.ui.Component scope) {
        if (scopesButtons.containsKey(scope)) {
            scopesButtons.remove(scope);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addNotificationsButton(Button button, final Window window, final UI ui) {
        final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);
        window.setContent(layout);

        notificationButtons.put(ui, new NotificationButton(button, 0));

        button.addClickListener(new NotificationClickListener(layout));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addTasksBar(HorizontalLayout tasksBar, UI ui) {
        tasksBars.put(ui, tasksBar);
    }

    /** {@inheritDoc}
     */
    public void hideScopeButton(com.vaadin.ui.Component scope) {
        if (scopesButtons.containsKey(scope)) {
            scopesButtons.get(scope).getButton().setVisible(false);
        }
    }

    /**
     * {@inheritDoc}
     * @param scope
     */
    public void removeBadge(com.vaadin.ui.Component scope) {
        updateBadge(scope, 0);
        scopesButtons.get(scope).getButton().setHtmlContentAllowed(true);
        setCaption(scopesButtons.get(scope).getButton(),
                   scopesButtons.get(scope).getButtonUi(),
                   getInitialCaption(scopesButtons.get(scope).getButton()));
    }

    /**
     * {@inheritDoc}
     */
    public void incrementBadge(com.vaadin.ui.Component scope) {
        if (scopesButtons.containsKey(scope) && scopesButtons.get(scope).getButton().isAttached()) {
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

    /**
     * {@inheritDoc}
     */
    public void decrementBadge(com.vaadin.ui.Component scope) {
        if (scopesButtons.containsKey(scope) && scopesButtons.get(scope).getButton().isAttached()) {
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
     * @param button button
     * @param ui button UI
     */
    private void setBadgeAsNew(final Button button, UI ui) {
        button.setHtmlContentAllowed(true);
        final String newCaption = getInitialCaption(button) +
                "<span class=\"badge\">new</span>";
        setCaption(button, ui, newCaption);

    }

    /**
     * Set button caption
     * @param button button
     * @param ui button UI
     * @param caption new button caption
     */
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
     * @param scope scope
     * @param op operation (+1, -1, set to 0)
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
     * @param button button
     * @return button caption
     */
    private String getInitialCaption(Button button) {
        if (button.getCaption().contains("<span")) {
            return button.getCaption().substring(0, button.getCaption().indexOf("<span"));
        }
        return button.getCaption();
    }

    /**
     * Update notification button, make it as unread.
     * @param notificationButton notification button
     */
    private void updateNotificationBadge(NotificationButton notificationButton) {
        int badge = notificationButton.getBadge();
        notificationButton.getButton().addStyleName("unread");
        notificationButton.getButton().setCaption(String.valueOf(badge));
        notificationButton.getButton().setDescription("Notifications (" + badge + " unread)");
    }

    /**
     * Format time
     * @param t timestamp
     * @return time string
     */
    private String formatTime(Long t) {
        Long days = TimeUnit.MILLISECONDS.toDays(t);
        Long months = days / 30;
        Long years = months / 12;

        StringBuilder date = new StringBuilder();
        if (years > 0) {
            date.append(years);
            if (years == 1) {
                date.append(" year");
            } else {
                date.append(" years");
            }
        } else {
            if (months > 0) {
                date.append(months);
                if (months == 1) {
                    date.append(" month");
                } else {
                    date.append(" months");
                }
            } else {
                if (days > 0) {
                    date.append(days);
                    if (days == 1) {
                        date.append(" day");
                    } else {
                        date.append(" days");
                    }
                } else {
                    Long hours = TimeUnit.MILLISECONDS.toHours(t - days * 24 * 60 * 60 * 1000);
                    if (hours > 0) {
                        date.append(hours);
                        if (hours == 1) {
                            date.append(" hour");
                        } else {
                            date.append(" hours");
                        }
                    } else {
                        Long minutes = TimeUnit.MILLISECONDS.toMinutes(t - hours * 60 * 60 * 1000);
                        if (minutes > 0) {
                            date.append(minutes);
                            if (minutes == 1) {
                                date.append(" minute");
                            } else {
                                date.append(" minutes");
                            }
                        } else {
                            date.append("a few seconds");
                        }
                    }
                }
            }
        }
        date.append(" ago");
        return date.toString();
    }

    /**
     * @author Mohammed Boukada
     */
    private class NotificationClickListener implements Button.ClickListener {
        private boolean opened = false;

        private VerticalLayout layout;

        public NotificationClickListener(VerticalLayout layout) {
            this.layout = layout;
        }

        @Override
        public void buttonClick(Button.ClickEvent event) {
            for (Map.Entry<UI, NotificationButton> notificationButtonEntry : notificationButtons.entrySet()) {
                notificationButtonEntry.getValue().setBadge(0);
            }
            if (opened) {
                opened = false;
            } else {
                opened = true;
                layout.removeAllComponents();
                int i = 0;
                Iterator<Notification> iterator = notifications.descendingIterator();
                while (iterator.hasNext() && i < 50) {
                    Notification notification = iterator.next();
                    layout.addComponent(new Label("<hr>", ContentMode.HTML));
                    Label message = new Label("<b>" + notification.getMessage() + "</b>", ContentMode.HTML);
                    message.setWidth("280px");
                    layout.addComponent(message);
                    Label date = new Label("<span>" + formatTime(System.currentTimeMillis() - notification.getDate()) +
                            "</span>", ContentMode.HTML);
                    layout.addComponent(date);
                    i++;
                }
            }
        }
    }

    /**
     * @author Mohammed Boukada
     */
    private class CleanupThread extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(2000);
                for (final Map.Entry<UI, HorizontalLayout> taskBar : tasksBars.entrySet()) {
                    UI ui = taskBar.getKey();
                    if (ui.isClosing()) {
                        clearComponentsForUI(ui);
                    } else {
                        ui.access(new Runnable() {
                            @Override
                            public void run() {
                                taskBar.getValue().removeAllComponents();
                            }
                        });
                    }
                }
            } catch (InterruptedException e) {
                // do nothing
            }
        }
    }
}
