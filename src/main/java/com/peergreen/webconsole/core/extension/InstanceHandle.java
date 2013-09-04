package com.peergreen.webconsole.core.extension;

/**
 * Extension Instance handle
 * @author Mohammed Boukada
 */
public interface InstanceHandle {
    /**
     * Get instance name
     * @return instance name
     */
    String getInstanceName();

    /**
     * Stop instance
     */
    void stop();

    /**
     * Get instance state
     * @return instance state
     */
    InstanceState getState();
}
