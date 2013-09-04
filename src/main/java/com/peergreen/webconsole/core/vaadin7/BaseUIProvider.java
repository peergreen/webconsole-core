package com.peergreen.webconsole.core.vaadin7;

import com.vaadin.server.SessionDestroyEvent;
import com.vaadin.server.SessionDestroyListener;
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
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import static com.peergreen.webconsole.Constants.EXTENSION_POINT;
import static com.peergreen.webconsole.Constants.REQUIRES_FILTER;
import static com.peergreen.webconsole.Constants.SCOPE_EXTENSION_POINT;
import static com.peergreen.webconsole.Constants.UI_ID;

/**
 * Vaadin Base console UI provider
 * @author Mohammed Boukada
 */
@Component
@Provides(specifications = UIProvider.class)
public class BaseUIProvider extends UIProvider {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(BaseUIProvider.class);

    private String consoleName;
    private String consoleAlias;
    private Boolean enableSecurity;
    private List<String> defaultRoles;

    private List<ComponentInstance> uis = new ArrayList<>();

    /**
     * Base console UI ipojo component factory
     */
    @Requires(from = "com.peergreen.webconsole.core.vaadin7.BaseUI")
    private Factory factory;

    private int i = 0;

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

    public void setDefaultRoles(List<String> defaultRoles) {
        this.defaultRoles = defaultRoles;
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
            String uiId = consoleAlias + "-" + i;
            ui = new BaseUI(consoleName, SCOPE_EXTENSION_POINT, uiId, enableSecurity, defaultRoles);
            // Configuration properties for ipojo component
            Dictionary<String, Object> props = new Hashtable<>();
            props.put("instance.object", ui);
            Dictionary<String, Object> bindFilters = new Hashtable<>();
            bindFilters.put("ScopeView", String.format("(&(%s=%s)(%s=%s))", UI_ID, uiId, EXTENSION_POINT, SCOPE_EXTENSION_POINT));
            props.put(REQUIRES_FILTER, bindFilters);

            // Create ipojo component from its factory
            final ComponentInstance instance = factory.createComponentInstance(props);
            e.getService().addSessionDestroyListener(new SessionDestroyListener() {
                @Override
                public void sessionDestroy(SessionDestroyEvent event) {
                    instance.stop();
                    instance.dispose();
                }
            });
            uis.add(instance);
            i++;
        } catch (UnacceptableConfiguration | MissingHandlerException | ConfigurationException ex) {
            LOGGER.error(ex.getMessage(), ex);
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
