package com.peergreen.webconsole.core.exception;

import com.vaadin.server.ErrorEvent;
import com.vaadin.server.ErrorHandler;

/**
 * @author Mohammed Boukada
 */
public class VaadinErrorHandler implements ErrorHandler {

    @Override
    public void error(ErrorEvent event) {
        System.err.println(event.getThrowable().getMessage());
    }
}
