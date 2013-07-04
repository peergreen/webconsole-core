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
     * @param properties
     * @return
     */
    UIProvider createUIProvider(Dictionary properties);

    void stopUIProvider(Dictionary properties);
}
