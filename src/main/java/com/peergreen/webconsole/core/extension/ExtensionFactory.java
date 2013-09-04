package com.peergreen.webconsole.core.extension;

import com.peergreen.webconsole.UIContext;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;

/**
 * Extension factory
 * @author Mohammed Boukada
 */
public interface ExtensionFactory {
    /**
     * Create an instance of the extension
     * @param context UI context
     * @return extension instance handle
     * @throws MissingHandlerException
     * @throws UnacceptableConfiguration
     * @throws ConfigurationException
     */
    InstanceHandle create(UIContext context) throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException;
}
