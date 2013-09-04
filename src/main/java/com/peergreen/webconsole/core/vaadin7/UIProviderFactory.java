package com.peergreen.webconsole.core.vaadin7;

import com.vaadin.server.UIProvider;

import java.util.Dictionary;

/**
 * Vaadin UI provider factory
 * @author Mohammed Boukada
 */
public interface UIProviderFactory {

    /**
     * Create an UI provider for a console
     * @param properties properties
     * @return UI provider
     */
    UIProvider createUIProvider(Dictionary properties);

    /**
     * Stop Ui provider
     * @param properties properties
     */
    void stopUIProvider(Dictionary properties);
}
