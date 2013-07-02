package com.peergreen.webconsole.core.extension;

import com.peergreen.webconsole.UIContext;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;

/**
 * @author Mohammed Boukada
 */
public interface ExtensionFactory {
    InstanceHandler create(UIContext context) throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException;
}
