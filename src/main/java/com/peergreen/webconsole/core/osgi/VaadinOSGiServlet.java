package com.peergreen.webconsole.core.osgi;

import javax.servlet.annotation.WebServlet;

import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.UIProvider;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletService;

/**
 * Vaadin OSGi Servlet
 * to add Vaadin UI provider dynamically
 * @author Mohammed Boukada
 */
@WebServlet(asyncSupported=true)
public class VaadinOSGiServlet extends VaadinServlet {

    /**
     * Vaadin UI provider
     */
    UIProvider provider;

    /**
     * Vaadin OSGi Servlet constructor
     * @param provider
     */
    public VaadinOSGiServlet(UIProvider provider) {
        this.provider = provider;
    }

    /** {@inheritDoc}
     */
    @Override
    protected VaadinServletService createServletService(DeploymentConfiguration deploymentConfiguration) throws ServiceException {

        final VaadinServletService service = super.createServletService(deploymentConfiguration);

        service.addSessionInitListener(new SessionInitListener() {
            private static final long serialVersionUID = -3430847247361456116L;

            @Override
            public void sessionInit(SessionInitEvent e) throws ServiceException {
                // Add Vaadin UI provider to the Vaadin session
                e.getSession().addUIProvider(provider);
            }
         });

        return service;
    }
}
