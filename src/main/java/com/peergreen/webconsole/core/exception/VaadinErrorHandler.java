package com.peergreen.webconsole.core.exception;

import com.vaadin.server.ErrorEvent;
import com.vaadin.server.ErrorHandler;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * @author Mohammed Boukada
 */
public class VaadinErrorHandler implements ErrorHandler {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(VaadinErrorHandler.class);

    @Override
    public void error(ErrorEvent event) {
        LOGGER.error(event.getThrowable().getMessage(), event.getThrowable());
    }
}
