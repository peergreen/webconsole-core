package com.peergreen.webconsole.core.handler;

import java.lang.reflect.Method;

/**
 * @author Mohammed Boukada
 */
public class ExtensionPoint {
    private Method bindMethod;
    private Method unbindMethod;
    private String filter;

    public ExtensionPoint(Method bindMethod, Method unbindMethod, String filter) {
        this.bindMethod = bindMethod;
        this.unbindMethod = unbindMethod;
        this.filter = filter;
    }

    public Method getBindMethod() {
        return bindMethod;
    }

    public Method getUnbindMethod() {
        return unbindMethod;
    }

    public String getFilter() {
        return filter;
    }

    public void setUnbindMethod(Method unbindMethod) {
        this.unbindMethod = unbindMethod;
    }

    public void setBindMethod(Method bindMethod) {
        this.bindMethod = bindMethod;
    }
}
