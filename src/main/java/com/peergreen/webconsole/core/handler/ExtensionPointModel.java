package com.peergreen.webconsole.core.handler;

import java.lang.reflect.Method;

/**
 * Defines an extension point
 * @author Mohammed Boukada
 */
public class ExtensionPointModel {
    private Method bindMethod;
    private Method unbindMethod;
    private String filter;

    /**
     * Create an extension point model
     * @param bindMethod callback bind method
     * @param unbindMethod callback unbind method
     * @param filter binding filter
     */
    public ExtensionPointModel(Method bindMethod, Method unbindMethod, String filter) {
        this.bindMethod = bindMethod;
        this.unbindMethod = unbindMethod;
        this.filter = filter;
    }

    /**
     * Get bind method to callback
     * @return bind method
     */
    public Method getBindMethod() {
        return bindMethod;
    }

    /**
     * Get unbind method to callback
     * @return unbind method
     */
    public Method getUnbindMethod() {
        return unbindMethod;
    }

    /**
     * Get bindings filter
     * @return filter
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Set unbind method to callback
     * @param unbindMethod unbind method
     */
    public void setUnbindMethod(Method unbindMethod) {
        this.unbindMethod = unbindMethod;
    }

    /**
     * Set bind method to callback
     * @param bindMethod bind method
     */
    public void setBindMethod(Method bindMethod) {
        this.bindMethod = bindMethod;
    }
}
