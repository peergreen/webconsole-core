package com.peergreen.webconsole.core.notifier;

import com.peergreen.webconsole.notifier.Task;

/**
 * @author Mohammed Boukada
 */
public class BaseTask implements Task {

    private String message;
    private NotifierService notifierService;

    public BaseTask(String message, NotifierService notifierService) {
        this.message = message;
        this.notifierService = notifierService;
    }

    @Override
    public void stop() {
        notifierService.hideTask(this);
    }

    public String getMessage() {
        return message;
    }
}
