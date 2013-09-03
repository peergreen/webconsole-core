package com.peergreen.webconsole.core.osgi;

import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.UIProvider;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletService;

import javax.servlet.annotation.WebServlet;

/**
 * Vaadin OSGi Servlet
 * to add Vaadin UI provider dynamically
 * @author Mohammed Boukada
 */
@WebServlet(asyncSupported=true)
public class VaadinOSGiServlet extends VaadinServlet {

    private static final long serialVersionUID = 1L;

    /**
     * Vaadin UI provider
     */
    private UIProvider provider;

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
        service.addSessionInitListener(new BaseSessionInitListener(provider));

        return service;
    }
}
