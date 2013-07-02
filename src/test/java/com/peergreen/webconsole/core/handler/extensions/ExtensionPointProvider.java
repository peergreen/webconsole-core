package com.peergreen.webconsole.core.handler.extensions;

import com.peergreen.webconsole.Extension;
import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.ISecurityManager;
import com.peergreen.webconsole.Inject;
import com.peergreen.webconsole.Link;
import com.peergreen.webconsole.UIContext;
import com.peergreen.webconsole.Unlink;
import com.peergreen.webconsole.INotifierService;
import com.vaadin.ui.Button;
import org.osgi.framework.BundleContext;

/**
 * @author Mohammed Boukada
 */
@Extension
@ExtensionPoint("my.extension")
public class ExtensionPointProvider extends Button implements TestInterface {

    @Inject
    ISecurityManager securityManager;

    @Inject
    BundleContext bundleContext;

    @Inject
    UIContext uiContext;

    @Inject
    INotifierService notifierService;

    public ISecurityManager getSecurityManager() {
        return securityManager;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public UIContext getUiContext() {
        return uiContext;
    }

    public INotifierService getNotifierService() {
        return notifierService;
    }

    @Link("TestInterface")
    public TestInterface addElement(TestInterface testInterface) {
        return testInterface;
    }

    @Unlink("TestInterface")
    public TestInterface removeElement(TestInterface testInterface) {
        return testInterface;
    }

    @Link("Button")
    public Button addButton(Button button) {
        return button;
    }

    @Unlink("Button")
    public Button removeButton(Button button) {
        return button;
    }
}
