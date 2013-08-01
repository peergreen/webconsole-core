package com.peergreen.webconsole.core.resource;

import com.peergreen.webconsole.INotifierService;
import com.peergreen.webconsole.resource.CssHandler;
import com.peergreen.webconsole.resource.CssInjectorService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Mohammed Boukada
 */
public class BaseBundleTrackerCustomizer implements BundleTrackerCustomizer<List<CssHandler>> {

    private CssInjectorService cssInjectorService;
    private INotifierService notifierService;

    public BaseBundleTrackerCustomizer(CssInjectorService cssInjectorService, INotifierService notifierService) {
        this.cssInjectorService = cssInjectorService;
        this.notifierService = notifierService;
    }

    @Override
    public List<CssHandler> addingBundle(Bundle bundle, BundleEvent bundleEvent) {
        BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
        Collection<String> cssFiles = bundleWiring.listResources("/css", "*.css", BundleWiring.LISTRESOURCES_RECURSE);
        List<CssHandler> cssHandlers = new ArrayList<>();
        if (cssFiles.size() > 0) {
            for (String cssFile : cssFiles) {
                try {
                    cssHandlers.add(cssInjectorService.inject(bundle.getResource(cssFile).openStream()));
                } catch (IOException e) {
                    notifierService.addNotification(String.format("Cannot add css file '%s' from '%s'", cssFile, bundle.getSymbolicName()));
                }
            }
        }
        return cssHandlers;
    }

    @Override
    public void modifiedBundle(Bundle bundle, BundleEvent bundleEvent, List<CssHandler> cssHandlers) {
        // do nothing
    }

    @Override
    public void removedBundle(Bundle bundle, BundleEvent bundleEvent, List<CssHandler> cssHandlers) {
        for (CssHandler cssHandler : cssHandlers) {
            cssInjectorService.remove(cssHandler);
        }
    }
}
