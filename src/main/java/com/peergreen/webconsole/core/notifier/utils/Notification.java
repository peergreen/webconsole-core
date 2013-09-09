package com.peergreen.webconsole.core.notifier.utils;

/**
 * Notification descriptor
 *
 * @author Mohammed Boukada
 */
public class Notification {

    /**
     * Notification message
     */
    private String message;

    /**
     * Notification date
     */
    private Long date;

    /**
     * Create a notification descriptor
     *
     * @param message notification message
     * @param date    notification date
     */
    public Notification(String message, Long date) {
        this.message = message;
        this.date = date;
    }

    /**
     * Get notification message
     *
     * @return notification message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get notification date
     *
     * @return notification date
     */
    public Long getDate() {
        return date;
    }
}
