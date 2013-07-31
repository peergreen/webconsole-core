package com.peergreen.webconsole.core.resource;

import com.peergreen.webconsole.INotifierService;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Mohammed Boukada
 */
@Component
@Instantiate
@Provides
public class BaseCssInjectorService implements CssInjectorService {

    private BundleTracker tracker;
    private List<UI> uis;
    private Map<Bundle, String> styles;

    public BaseCssInjectorService(BundleContext bundleContext,
                                  @Requires
                                  INotifierService notifierService) {
        uis = new CopyOnWriteArrayList<>();
        styles = new ConcurrentHashMap<>();
        tracker = new BundleTracker(bundleContext, Bundle.ACTIVE, new BaseBundleTrackerCustomizer(this, notifierService));
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
    public void add(Bundle bundle, String style) {
        styles.put(bundle, style);
        for (UI ui : uis) {
            ui.getPage().getStyles().add(style);
        }
    }

    @Override
    public void remove(Bundle bundle) {
        styles.remove(bundle);
    }

    @Override
    public void add(Bundle bundle, InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        String line = bufferedReader.readLine();
        while (line != null) {
            sb.append(line);
            line = bufferedReader.readLine();
        }
        add(bundle, sb.toString());
    }

    @Bind(optional = true, aggregate = true)
    public void bindUI(UI ui) {
        uis.add(ui);
        for (Map.Entry<Bundle, String> style : styles.entrySet()) {
            ui.getPage().getStyles().add(style.getValue());
        }
    }

    @Unbind
    public void unbindUI(UI ui) {
        uis.remove(ui);
    }
}
