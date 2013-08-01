package com.peergreen.webconsole.core.resource;

import com.peergreen.webconsole.resource.CssHandler;

/**
 * @author Mohammed Boukada
 */
public class BaseCssHandler implements CssHandler {
    private String cssContent;

    public BaseCssHandler(String cssContent) {
        this.cssContent = cssContent;
    }

    @Override
    public String getCss() {
        return cssContent;
    }

    @Override
    public void setCss(String cssContent) {
        this.cssContent = cssContent;
    }

    @Override
    public void removeCss() {
        this.cssContent = "";
    }
}
