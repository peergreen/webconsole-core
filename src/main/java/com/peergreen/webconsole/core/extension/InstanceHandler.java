package com.peergreen.webconsole.core.extension;

/**
 * @author Mohammed Boukada
 */
public interface InstanceHandler {
    String getInstanceName();
    void stop();
    InstanceState getState();
}
