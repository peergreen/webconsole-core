package com.peergreen.webconsole.core.vaadin7;

import com.peergreen.webconsole.Constants;
import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.ui.UI;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

/**
 * Vaadin Base console UI provider
 * @author Mohammed Boukada
 */
@Component
@Provides(specifications = UIProvider.class)
public class UIProviderBase extends UIProvider {

    private String consoleName;
    private String consoleAlias;
    private Boolean enableSecurity;

    List<ComponentInstance> uis = new ArrayList<>();

    /**
     * Base console UI ipojo component factory
     */
    @Requires(from = "com.peergreen.webconsole.core.vaadin7.BaseUI")
    private Factory factory;

    /**
     * Bundle context
     */
    private BundleContext bundleContext;

    private int i = 0;

    /**
     * Vaadin base UI provider constructor
     * @param bundleContext
     */
    public UIProviderBase(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /**
     * Set console
     * @param consoleName
     */
    public void setConsoleName(String consoleName) {
        this.consoleName = consoleName;
    }

    public void setConsoleAlias(String consoleAlias) {
        this.consoleAlias = consoleAlias.substring(1);
    }

    public void setEnableSecurity(Boolean enableSecurity) {
        this.enableSecurity = enableSecurity;
    }



    /** {@inheritDoc}
     */
    @Override
    public Class<? extends UI> getUIClass(UIClassSelectionEvent event) {
        return BaseUI.class;
    }

    /** {@inheritDoc}
     */
    @Override
    public UI createInstance(final UICreateEvent e) {
        BaseUI ui = null;
        try {
            // Create an instance of baseUI
            String scopeExtensionPoint = "com.peergreen.webconsole.scope";
            String uiId = consoleAlias + "-" + i;
            ui = new BaseUI(consoleName, scopeExtensionPoint, uiId, enableSecurity);

            // Configuration properties for ipojo component
            Dictionary<String, Object> props = new Hashtable<>();
            props.put("instance.object", ui);
            Dictionary<String, Object> bindFilters = new Hashtable<>();
            bindFilters.put("ScopeView", "(&(" + Constants.UI_ID + "=" + uiId + ")(" +
                    Constants.EXTENSION_POINT + "=" + scopeExtensionPoint + "))");
            props.put(Constants.REQUIRES_FILTER, bindFilters);

            // Create ipojo component from its factory
            uis.add(factory.createComponentInstance(props));
            i++;
        } catch (UnacceptableConfiguration unacceptableConfiguration) {
            unacceptableConfiguration.printStackTrace();
        } catch (MissingHandlerException ex) {
            ex.printStackTrace();
        } catch (ConfigurationException ex) {
            ex.printStackTrace();
        }

        return ui;
    }

    @Invalidate
    public void stop() {
        for (ComponentInstance instance : uis) {
            instance.stop();
            instance.dispose();
        }
    }
}
