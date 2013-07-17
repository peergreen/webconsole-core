package com.peergreen.webconsole.core.notifier.utils;

/**
 * @author Mohammed Boukada
 */
public class Notification {
    private String message;
    private Long date;

    public Notification(String message, Long date) {
        this.message = message;
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public Long getDate() {
        return date;
    }
}
