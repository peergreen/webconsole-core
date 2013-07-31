package com.peergreen.webconsole.core.resource;

import com.peergreen.webconsole.INotifierService;
import com.peergreen.webconsole.resource.CssInjectorService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import java.io.IOException;
import java.util.Collection;

/**
 * @author Mohammed Boukada
 */
public class BaseBundleTrackerCustomizer implements BundleTrackerCustomizer {

    CssInjectorService cssInjectorService;
    INotifierService notifierService;

    public BaseBundleTrackerCustomizer(CssInjectorService cssInjectorService, INotifierService notifierService) {
        this.cssInjectorService = cssInjectorService;
        this.notifierService = notifierService;
    }

    @Override
    public Object addingBundle(Bundle bundle, BundleEvent bundleEvent) {
        BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
        Collection<String> cssFiles = bundleWiring.listResources("/css", "*.css", BundleWiring.LISTRESOURCES_RECURSE);
        if (cssFiles.size() > 0) {
            for (String cssFile : cssFiles) {
                try {
                    cssInjectorService.add(bundle, bundle.getResource(cssFile).openStream());
                } catch (IOException e) {
                    notifierService.addNotification(String.format("Cannot add css file '%s' from '%s'", cssFile, bundle.getSymbolicName()));
                }
            }
        }
        return null;
    }

    @Override
    public void modifiedBundle(Bundle bundle, BundleEvent bundleEvent, Object o) {
        // do nothing
    }

    @Override
    public void removedBundle(Bundle bundle, BundleEvent bundleEvent, Object o) {
        cssInjectorService.remove(bundle);
    }
}
