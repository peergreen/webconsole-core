package com.peergreen.webconsole.core.osgi;

import javax.servlet.annotation.WebServlet;

import com.peergreen.webconsole.core.exception.VaadinErrorHandler;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.server.BootstrapFragmentResponse;
import com.vaadin.server.BootstrapListener;
import com.vaadin.server.BootstrapPageResponse;
import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.UIProvider;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletService;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
                e.getSession().setErrorHandler(new VaadinErrorHandler());
                e.getSession().addBootstrapListener(new BootstrapListener() {
                    @Override
                    public void modifyBootstrapFragment(BootstrapFragmentResponse response) {

                    }

                    /*
                     * Ensure window has "stable name", in case PreserveOnRefresh is used.
                     * This is to fool vaadinBootstrap.js so that for example applications
                     * used as ios "home screen webapps", can preserve their state among app
                     * switches, like following links (with browser) and then returning back
                     * to app.
                     */
                    @Override
                    public void modifyBootstrapPage(BootstrapPageResponse response) {
                        Document document = response.getDocument();
                        Element head = document.getElementsByTag("head").get(0);
                        Element element;
                        if (response.getUiClass().getAnnotation(PreserveOnRefresh.class) != null) {
                            element = document.createElement("script");
                            element.attr("type", "text/javascript");
                            element.appendText("\nwindow.name = '" + response.getUiClass().hashCode() + "';\n");
                            head.appendChild(element);
                        }
                    }
                });
            }
         });

        return service;
    }
}
