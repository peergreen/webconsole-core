package com.peergreen.webconsole.core.resource;

import com.peergreen.webconsole.resource.CssHandle;

/**
 * @author Mohammed Boukada
 */
public class BaseCssHandle implements CssHandle {
    private String cssContent;
    private BaseCssInjectorService cssInjectorService;

    public BaseCssHandle(String cssContent, BaseCssInjectorService cssInjectorService) {
        this.cssContent = cssContent;
        this.cssInjectorService = cssInjectorService;
    }

    @Override
    public String get() {
        return cssContent;
    }

    @Override
    public void update(String cssContent) {
        this.cssContent = cssContent;
    }

    @Override
    public void remove() {
        cssInjectorService.remove(this);
    }
}
