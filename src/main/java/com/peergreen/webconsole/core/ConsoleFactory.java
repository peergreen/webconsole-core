package com.peergreen.webconsole.core;

import javax.servlet.ServletException;

import com.peergreen.webconsole.Constants;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import com.peergreen.webconsole.IConsole;
import com.peergreen.webconsole.core.osgi.VaadinOSGiServlet;
import com.vaadin.server.UIProvider;

import java.util.Dictionary;

/**
 * BaseConsole factory
 * @author Mohammed Boukada
 */
@Component
@Instantiate
public class ConsoleFactory {

    /**
     * Http Service
     */
    @Requires
    HttpService httpService;

    /**
     * UI provider factory
     */
    @Requires
    IUIProviderFactory uiProviderFactory;

    /**
     * Bind a console
     * @param console
     */
    @Bind(aggregate = true, optional = true)
    public void bindConsole(IConsole console, Dictionary properties) {

        // Create an UI provider for the console UI
        UIProvider uiProvider = uiProviderFactory.createUIProvider(properties);
        // Create a servlet
        VaadinOSGiServlet servlet = new VaadinOSGiServlet(uiProvider);

        try {
            // Register the servlet with the console alias
            httpService.registerServlet((String) properties.get(Constants.CONSOLE_ALIAS), servlet, null, null);
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (NamespaceException e) {
            // ignore update
        }
    }

    /**
     * Unbind a console
     * @param console
     */
    @Unbind
    public void unbindConsole(IConsole console, Dictionary properties) {
        // Unregister its servlet
        uiProviderFactory.stopUIProvider(properties);
        httpService.unregister((String) properties.get(Constants.CONSOLE_ALIAS));
    }
}
