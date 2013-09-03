package com.peergreen.webconsole.core.resource;

import com.peergreen.webconsole.resource.CssHandle;

/**
 * Css contribution handle implementation
 * @author Mohammed Boukada
 */
public class BaseCssHandle implements CssHandle {
    private String cssContent;
    private BaseCssInjectorService cssInjectorService;

    /**
     * Create new css contribution handle
     * @param cssContent css content
     * @param cssInjectorService css contribution service
     */
    public BaseCssHandle(String cssContent, BaseCssInjectorService cssInjectorService) {
        this.cssContent = cssContent;
        this.cssInjectorService = cssInjectorService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String get() {
        return cssContent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(String cssContent) {
        this.cssContent = cssContent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove() {
        cssInjectorService.remove(this);
    }
}
