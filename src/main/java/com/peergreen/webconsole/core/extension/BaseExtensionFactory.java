package com.peergreen.webconsole.core.extension;

import com.peergreen.webconsole.Constants;
import com.peergreen.webconsole.UIContext;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Extension factory. <br />
 * This factory creates an instance of given extension for each client (UI)
 * @author Mohammed Boukada
 */
@Component
@Provides
public class BaseExtensionFactory implements ExtensionFactory {

    @ServiceProperty(name = Constants.EXTENSION_POINT, mandatory = true)
    private String extensionPoint;
    @ServiceProperty(name = Constants.EXTENSION_ROLES, mandatory = true)
    private String[] roles;
    private Factory factory;

    public BaseExtensionFactory(Factory factory) {
        this.factory = factory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InstanceHandle create(UIContext context) throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException {
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put(Constants.EXTENSION_POINT, extensionPoint);
        properties.put(Constants.UI_CONTEXT, context);
        return new BaseInstanceHandle(factory.createComponentInstance(properties));
    }

    /**
     * {@inheritDoc}
     * @author Mohamme Boukada
     */
    public class BaseInstanceHandle implements InstanceHandle {

        private ComponentInstance iPOJOComponentInstance;

        public BaseInstanceHandle(ComponentInstance iPOJOComponentInstance) {
            this.iPOJOComponentInstance = iPOJOComponentInstance;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getInstanceName() {
            return iPOJOComponentInstance.getInstanceName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void stop() {
            iPOJOComponentInstance.stop();
            iPOJOComponentInstance.dispose();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public InstanceState getState() {
            switch (iPOJOComponentInstance.getState()) {
                case ComponentInstance.DISPOSED:
                    return InstanceState.DISPOSED;
                case ComponentInstance.STOPPED:
                    return InstanceState.STOPPED;
                case ComponentInstance.INVALID:
                    return InstanceState.INVALID;
                case ComponentInstance.VALID:
                    return InstanceState.VALID;
                default:
                    return null;
            }
        }
    }
}
