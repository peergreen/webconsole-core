package com.peergreen.webconsole.core.resource;

import com.peergreen.webconsole.INotifierService;
import com.peergreen.webconsole.resource.CssHandler;
import com.peergreen.webconsole.resource.CssInjectorService;
import com.vaadin.ui.UI;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.BundleTracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Mohammed Boukada
 */
@Component
@Instantiate
@Provides
public class BaseCssInjectorService implements CssInjectorService {

    private BundleTracker<List<CssHandler>> tracker;
    private List<UI> uis;
    private List<CssHandler> styles;

    public BaseCssInjectorService(BundleContext bundleContext,
                                  @Requires
                                  INotifierService notifierService) {
        uis = new CopyOnWriteArrayList<>();
        styles = new CopyOnWriteArrayList<>();
        tracker = new BundleTracker<List<CssHandler>>(bundleContext, Bundle.ACTIVE, new BaseBundleTrackerCustomizer(this, notifierService));
    }

    @Validate
    public void start() {
        tracker.open();
    }

    @Invalidate
    public void stop() {
        tracker.close();
    }

    @Override
    public CssHandler inject(String cssContent) {
        CssHandler cssHandler = new BaseCssHandler(cssContent);
        styles.add(cssHandler);
        updateStyle(uis, Collections.singletonList(cssHandler));
        return cssHandler;
    }

    @Override
    public void remove(CssHandler cssHandler) {
        styles.remove(cssHandler);
    }

    @Override
    public CssHandler inject(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        String line = bufferedReader.readLine();
        while (line != null) {
            sb.append(line);
            line = bufferedReader.readLine();
        }
        return inject(sb.toString());
    }

    @Bind(optional = true, aggregate = true)
    public void bindUI(UI ui) {
        uis.add(ui);
        updateStyle(Collections.singletonList(ui), styles);
    }

    @Unbind
    public void unbindUI(UI ui) {
        uis.remove(ui);
    }

    private void updateStyle(List<UI> uis, List<CssHandler> cssHandlers) {
        for (UI ui : uis) {
            for (CssHandler cssHandler : cssHandlers) {
                ui.getPage().getStyles().add(cssHandler.getCss());
            }
        }
    }
}