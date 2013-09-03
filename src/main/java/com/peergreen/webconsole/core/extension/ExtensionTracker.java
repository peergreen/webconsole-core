package com.peergreen.webconsole.core.extension;

import com.peergreen.webconsole.Constants;
import com.peergreen.webconsole.ExtensionPoint;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.ipojo.util.Tracker;
import org.apache.felix.ipojo.util.TrackerCustomizer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import javax.annotation.security.RolesAllowed;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static java.lang.String.format;

/**
 * @author Mohammed Boukada
 */
@Component
@Instantiate
public class ExtensionTracker implements TrackerCustomizer {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(ExtensionTracker.class);

    private Tracker tracker;
    private BundleContext bundleContext;
    private Map<ServiceReference, ComponentInstance> instances = new HashMap<>();

    @Requires(from = "com.peergreen.webconsole.core.extension.BaseExtensionFactory")
    private Factory extensionFactory;

    public ExtensionTracker(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setExtensionFactory(Factory extensionFactory) {
        this.extensionFactory = extensionFactory;
    }

    @Validate
    public void start() {
        String filter = format("(&(%s=true)(factory.state=1))", Constants.WEBCONSOLE_EXTENSION);
        try {
            tracker = new Tracker(bundleContext, bundleContext.createFilter(filter), this);
            tracker.open();
        } catch (InvalidSyntaxException e) {
            LOGGER.warn("Fail to create filter ''{0}''", filter);
        }
    }

    @Invalidate
    public void stop() {
        tracker.close();
    }

    @Override
    public boolean addingService(ServiceReference reference) {
        return true;
    }


    @Override
    public void addedService(ServiceReference reference) {
        // get extension factory
        Factory factory = (Factory) bundleContext.getService(reference);
        String className = factory.getClassName();
        Class<?> extensionClass;
        try {
            extensionClass = factory.getBundleContext().getBundle().loadClass(className);
            String extensionPoint = "";
            String[] roles = {};
            if (extensionClass.isAnnotationPresent(ExtensionPoint.class)) {
                ExtensionPoint properties = extensionClass.getAnnotation(ExtensionPoint.class);
                extensionPoint = properties.value();
            }
            if (extensionClass.isAnnotationPresent(RolesAllowed.class)) {
                RolesAllowed rolesAllowed = extensionClass.getAnnotation(RolesAllowed.class);
                roles = rolesAllowed.value();
            }

            BaseExtensionFactory baseExtensionFactory = new BaseExtensionFactory(factory);
            Dictionary<String, Object> properties = new Hashtable<>();
            properties.put("instance.object", baseExtensionFactory);
            properties.put(Constants.EXTENSION_POINT, extensionPoint);
            properties.put(Constants.EXTENSION_ROLES, roles);
            instances.put(reference, extensionFactory.createComponentInstance(properties));
        } catch (ClassNotFoundException | MissingHandlerException | ConfigurationException | UnacceptableConfiguration e) {
            e.printStackTrace();
        }
    }

    @Override
    public void modifiedService(ServiceReference reference, Object service) {

    }

    @Override
    public void removedService(ServiceReference reference, Object service) {
        instances.get(reference).stop();
        instances.get(reference).dispose();
        instances.remove(reference);
    }

}
