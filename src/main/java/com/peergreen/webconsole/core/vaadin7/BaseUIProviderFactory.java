package com.peergreen.webconsole.core.vaadin7;

import com.peergreen.webconsole.Constants;
import com.vaadin.server.UIProvider;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.osgi.framework.BundleContext;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Vaadin UI provider factory
 * @author Mohammed Boukada
 */
@Component
@Instantiate
@Provides
public class BaseUIProviderFactory implements UIProviderFactory {

    /**
     * Base console UI provider ipojo component factory
     */
    @Requires(from = "com.peergreen.webconsole.core.vaadin7.BaseUIProvider")
    private Factory factory;

    /**
     * Bundle context
     */
    private BundleContext bundleContext;

    private Map<String, ComponentInstance> providers = new ConcurrentHashMap<>();

    /**
     * Vaadin UI provider factory constructor
     * @param bundleContext
     */
    public BaseUIProviderFactory(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /** {@inheritDoc}
     */
    @Override
    public UIProvider createUIProvider(Dictionary properties) {
        BaseUIProvider provider = null;
        String consoleName = (String) properties.get(Constants.CONSOLE_NAME);
        String consoleAlias = (String) properties.get(Constants.CONSOLE_ALIAS);
        Boolean enableSecurity = (Boolean) properties.get(Constants.ENABLE_SECURITY);
        String[] defaultRoles = (String[]) properties.get(Constants.DEFAULT_ROLES);
        if (defaultRoles == null) {
            defaultRoles = new String[0];
        }

        try {
            // Create an instance of base console UI provider
            provider = new BaseUIProvider(bundleContext);
            provider.setConsoleName(consoleName);
            provider.setConsoleAlias(consoleAlias);
            provider.setEnableSecurity(enableSecurity);
            provider.setDefaultRoles(Arrays.asList(defaultRoles));

            // Configuration properties for ipojo component
            Properties props = new Properties();
            // Use the instance of baseUI an pojo instance for ipojo component
            props.put("instance.object", provider);

            // Create ipojo component from its factory
            String instanceName = (String) properties.get("instance.name");
            providers.put(instanceName, factory.createComponentInstance(props));
        } catch (UnacceptableConfiguration | MissingHandlerException | ConfigurationException unacceptableConfiguration) {
            unacceptableConfiguration.printStackTrace();
        }
        return provider;
    }

    @Override
    public void stopUIProvider(Dictionary properties) {
        String instanceName = (String) properties.get("instance.name");
        providers.get(instanceName).stop();
        providers.get(instanceName).dispose();
        providers.remove(instanceName);
    }
}
