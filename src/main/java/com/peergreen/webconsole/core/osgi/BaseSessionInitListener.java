package com.peergreen.webconsole.core.osgi;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.peergreen.webconsole.core.exception.VaadinErrorHandler;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.server.BootstrapFragmentResponse;
import com.vaadin.server.BootstrapListener;
import com.vaadin.server.BootstrapPageResponse;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.UIProvider;

/**
 * Session init listener
 *
 * @author Mohammed Boukada
 */
public class BaseSessionInitListener implements SessionInitListener {

    private static final long serialVersionUID = -3430847247361456116L;
    private UIProvider provider;

    public BaseSessionInitListener(UIProvider provider) {
        this.provider = provider;
    }

    @Override
    public void sessionInit(SessionInitEvent e) throws ServiceException {
        // Add Vaadin UI provider to the Vaadin session
        e.getSession().addUIProvider(provider);
        e.getSession().setErrorHandler(new VaadinErrorHandler());
        e.getSession().addBootstrapListener(new BaseBootstrapListener());
    }

    private class BaseBootstrapListener implements BootstrapListener {
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
    }
}
