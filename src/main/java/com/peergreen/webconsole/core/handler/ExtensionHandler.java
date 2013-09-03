package com.peergreen.webconsole.core.handler;

import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.INotifierService;
import com.peergreen.webconsole.ISecurityManager;
import com.peergreen.webconsole.Inject;
import com.peergreen.webconsole.Link;
import com.peergreen.webconsole.Qualifier;
import com.peergreen.webconsole.Scope;
import com.peergreen.webconsole.UIContext;
import com.peergreen.webconsole.Unlink;
import com.peergreen.webconsole.core.extension.ExtensionFactory;
import com.peergreen.webconsole.core.extension.InstanceHandler;
import com.peergreen.webconsole.core.extension.InstanceState;
import com.peergreen.webconsole.core.notifier.InternalNotifierService;
import com.peergreen.webconsole.navigator.Navigable;
import com.peergreen.webconsole.navigator.NavigableModel;
import com.peergreen.webconsole.navigator.Navigate;
import com.peergreen.webconsole.navigator.NavigationContext;
import com.peergreen.webconsole.navigator.ViewNavigator;
import com.vaadin.ui.Component;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.InstanceStateListener;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.Pojo;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Handler;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.architecture.ComponentTypeDescription;
import org.apache.felix.ipojo.architecture.HandlerDescription;
import org.apache.felix.ipojo.architecture.PropertyDescription;
import org.apache.felix.ipojo.handlers.dependency.Dependency;
import org.apache.felix.ipojo.handlers.dependency.DependencyCallback;
import org.apache.felix.ipojo.handlers.dependency.DependencyHandler;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.util.SecurityHelper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.peergreen.webconsole.Constants.EXTENSION_ALIAS;
import static com.peergreen.webconsole.Constants.EXTENSION_POINT;
import static com.peergreen.webconsole.Constants.EXTENSION_ROLES;
import static com.peergreen.webconsole.Constants.UI_CONTEXT;
import static com.peergreen.webconsole.Constants.UI_ID;
import static com.peergreen.webconsole.Constants.WEBCONSOLE_EXTENSION;
import static java.lang.String.format;

/**
 * @author Mohammed Boukada
 */
@Handler(name = "extension",
         namespace = "com.peergreen.webconsole")
public class ExtensionHandler extends DependencyHandler {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(ExtensionHandler.class);

    private Class<?> extensionType;
    private UIContext uiContext;
    private InstanceManager ownInstanceManager = new OwnInstanceManager();
    private InternalNotifierService notifierService;
    private List<Field> fieldsToBind;
    private List<LinkDependencyCallback> dependencyCallbacks = new ArrayList<>();
    private List<String> notifications = new LinkedList<>();
    private Map<String, ExtensionPointModel> bindings;
    private Map<ExtensionFactory, InstanceHandler> extensionFactories = new HashMap<>();

    public void setOwnInstanceManager(InstanceManager instanceManager) {
        this.ownInstanceManager = instanceManager;
    }

    public void setUiContext(UIContext uiContext) {
        this.uiContext = uiContext;
    }

    @Override
    public void configure(Element metadata, Dictionary configuration) throws ConfigurationException {
        uiContext = (UIContext) configuration.get(UI_CONTEXT);
        if (uiContext == null) {
            throw new ConfigurationException("Missing UI Context");
        }

        configuration.remove(UI_CONTEXT);
        configuration.put(UI_ID, uiContext.getUIId());

        setExtensionType();
        setExtensionProperties(configuration);
        setExtensionNavigationModel(configuration);
        bindInjections();

        // Add instance state listener to (un)register component specifications as services
        ownInstanceManager.addInstanceStateListener(new ExtensionInstanceStateListener(configuration));

        super.configure(createBindings(metadata), configuration);
    }

    public void setExtensionType() {
        extensionType = ownInstanceManager.getPojoObject().getClass();
    }

    @Override
    public void stop() {
        super.stop();
        for (Map.Entry<ExtensionFactory, InstanceHandler> instance : extensionFactories.entrySet()) {
            instance.getValue().stop();
        }
        uiContext.getViewNavigator().unregisterNavigableModel((Component) getInstanceManager().getPojoObject());
    }

    @Bind(aggregate = true, optional = true)
    public void bindExtensionFactory(ExtensionFactory extensionFactory, Dictionary properties) {
        if (canBindExtensionFactory(properties)) {
            InstanceHandler instanceHandler;
            boolean failed = false;
            try {
                instanceHandler = extensionFactory.create(uiContext);
                if (InstanceState.STOPPED.equals(instanceHandler.getState())) {
                    failed = true;
                }
                extensionFactories.put(extensionFactory, instanceHandler);
            } catch (MissingHandlerException | UnacceptableConfiguration | ConfigurationException e) {
                failed = true;
            }

            if (failed) {
                String error = "Fail to add an extension to '" + properties.get("extension.point") +"'";
                if (notifierService != null) {
                    notifierService.addNotification(error);
                } else {
                    notifications.add(error);
                }
            }
        }
    }

    @Unbind
    public void unbindExtensionFactory(ExtensionFactory extensionFactory) {
        if (extensionFactories.containsKey(extensionFactory)) {
            extensionFactories.get(extensionFactory).stop();
            extensionFactories.remove(extensionFactory);
        }
    }

    protected Dictionary setExtensionProperties(Dictionary configuration) throws ConfigurationException {
        Annotation[] annotations = extensionType.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
                Qualifier qualifier = annotation.annotationType().getAnnotation(Qualifier.class);
                String propertiesPrefix = "".equals(qualifier.value()) ? annotation.annotationType().getName() : qualifier.value();
                for (Method method : annotation.annotationType().getDeclaredMethods()) {
                    Object value = null;
                    try {
                        value = method.invoke(annotation);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new ConfigurationException(e.getMessage(), e);
                    }
                    configuration.put(propertiesPrefix + "." + method.getName(), method.getReturnType().cast(value));
                }
            }
        }
        return configuration;
    }

    protected List<Field> bindInjections() throws ConfigurationException {
        fieldsToBind = new ArrayList<>();
        Field[] fields = extensionType.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Class<?> type = field.getType();
                Object pojo = ownInstanceManager.getPojoObject();
                Object fieldValue = null;
                if (type.equals(BundleContext.class)) {
                    fieldValue = ownInstanceManager.getBundleContext();
                } else if (type.equals(ISecurityManager.class)) {
                    fieldValue = uiContext.getSecurityManager();
                } else if (type.equals(ViewNavigator.class)) {
                    fieldValue = uiContext.getViewNavigator();
                } else if (type.equals(UIContext.class)) {
                    fieldValue = uiContext;
                }

                try {
                    if (fieldValue != null) {
                        field.setAccessible(true);
                        field.set(pojo, fieldValue);
                    } else {
                        fieldsToBind.add(field);
                    }
                } catch (IllegalAccessException e) {
                    throw new ConfigurationException(format("Fail to set the field '%s' type of '%s'", field.getName(), field.getType().getName()), e);
                }
            }
        }
        return fieldsToBind;
    }

    protected List<String> getSpecifications(Class<?> clazz) {
        List<String> specifications = new ArrayList<>();
        specifications.add(clazz.getName());
        for (Class<?> itf : clazz.getInterfaces()) {
            if (!Pojo.class.equals(itf)) {
                specifications.add(itf.getName());
            }
        }
        if (clazz.getSuperclass() != null && !Object.class.equals(clazz.getSuperclass())) {
            specifications.addAll(getSpecifications(clazz.getSuperclass()));
        }
        return specifications;
    }

    private boolean canBindExtensionFactory(Dictionary properties) {
        String extensionId = (String) properties.get(EXTENSION_POINT);
        return extensionId != null && bindings.containsKey(extensionId) && isInRoles(properties);
    }

    private boolean isInRoles(Dictionary properties) {
        if (uiContext.getSecurityManager() == null) {
            return true;
        }
        String[] extensionRoles = (String[]) properties.get(EXTENSION_ROLES);
        return uiContext.getSecurityManager().isUserInRoles(extensionRoles);
    }

    protected Element createBindings(Element metadata) throws ConfigurationException {
        Method[] methods = extensionType.getDeclaredMethods();
        bindings = new HashMap<>();

        Element newMetadata = new Element("component", null);
        for (Element element : metadata.getElements()) {
            if (!element.getName().equals("requires")) {
                newMetadata.addElement(element);
            }
        }
        for (Attribute attribute : metadata.getAttributes()) {
            newMetadata.addAttribute(attribute);
        }

        for (Method method : methods) {
            if (method.isAnnotationPresent(Link.class)) {
                Link link = method.getAnnotation(Link.class);
                String extensionName = link.value();
                String extensionPointId = extensionType.getName() + "." + extensionName;
                String filter = "";
                if (uiContext != null) {
                    filter = String.format("(&(%s=%s)(%s=%s))", UI_ID, uiContext.getUIId(), EXTENSION_POINT, extensionPointId);
                }

                if (bindings.containsKey(extensionPointId)) {
                    if (bindings.get(extensionPointId).getBindMethod() == null) {
                        bindings.get(extensionPointId).setBindMethod(method);
                    } else {
                        LOGGER.error("Bind method already defined for ''{0}''", extensionPointId);
                    }
                } else {
                    bindings.put(extensionPointId, new ExtensionPointModel(method, null, filter));
                }
            }
            if (method.isAnnotationPresent(Unlink.class)) {
                Unlink unlink = method.getAnnotation(Unlink.class);
                String extensionName = unlink.value();
                String extensionPointId = extensionType.getName() + "." + extensionName;
                String filter = "";
                if (uiContext != null) {
                    filter = String.format("(&(%s=%s)(%s=%s))", UI_ID, uiContext.getUIId(), EXTENSION_POINT, extensionPointId);
                }
                if (bindings.containsKey(extensionPointId)) {
                    if (bindings.get(extensionPointId).getUnbindMethod() == null) {
                        bindings.get(extensionPointId).setUnbindMethod(method);
                    } else {
                        LOGGER.error("Unbind method already defined for ''{0}''", extensionPointId);
                    }
                } else {
                    bindings.put(extensionPointId, new ExtensionPointModel(null, method, filter));
                }
            }
        }

        for (Map.Entry<String, ExtensionPointModel> binding : bindings.entrySet()) {
            String extensionPointId = binding.getKey();
            ExtensionPointModel extensionPointModel = binding.getValue();

            Element requires = new Element("requires", null);
            if (!"".equals(extensionPointModel.getFilter())) {
                requires.addAttribute(new Attribute("filter", extensionPointModel.getFilter()));
            }
            requires.addAttribute(new Attribute("optional", "true"));
            requires.addAttribute(new Attribute("aggregate", "true"));
            requires.addAttribute(new Attribute("id", extensionPointId));

            if (extensionPointModel.getBindMethod() != null) {
                Element bindCallback = new Element("callback", null);
                bindCallback.addAttribute(new Attribute("method", extensionPointModel.getBindMethod().getName()));
                bindCallback.addAttribute(new Attribute("type", "bind"));
                requires.addElement(bindCallback);
            }

            if (extensionPointModel.getUnbindMethod() != null) {
                Element unbindCallback = new Element("callback", null);
                unbindCallback.addAttribute(new Attribute("method", extensionPointModel.getUnbindMethod().getName()));
                unbindCallback.addAttribute(new Attribute("type", "unbind"));
                requires.addElement(unbindCallback);
            }
            newMetadata.addElement(requires);
        }

        for (Field field : fieldsToBind) {
            Element requires = new Element("requires", null);
            requires.addAttribute(new Attribute("field", field.getName()));
            requires.addAttribute(new Attribute("filter", String.format("(|(%s=%s)(!(%s=*)))", UI_ID, uiContext.getUIId(), UI_ID)));
            newMetadata.addElement(requires);
        }
        return newMetadata;
    }

    private void setExtensionNavigationModel(Dictionary configuration) throws ConfigurationException {
        ExtensionPoint extension = extensionType.getAnnotation(ExtensionPoint.class);
        String alias = "";
        Method callbackMethod = null;

        if (extensionType.isAnnotationPresent(Navigable.class) || extensionType.isAnnotationPresent(Scope.class)) {
            Navigable navigable = extensionType.getAnnotation(Navigable.class);
            Scope scope = extensionType.getAnnotation(Scope.class);

            if (navigable != null && !"".equals(navigable.value())) {
                alias = navigable.value();
            } else if (scope != null) {
                alias = scope.value();
            } else if ("".equals(alias)) {
                alias = extensionType.getName();
            }
            if (alias.charAt(0) != '/') {
                alias = '/' + alias;
            }

            boolean found = false;
            for (Method method : extensionType.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Navigate.class)) {
                    if (found) {
                        LOGGER.error("Webconsole extension should have a unique method annotated with ''{0}''", Navigate.class.getName());
                        continue;
                    }

                    // default alias is class name
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length != 1){
                        LOGGER.error("Method annotated with @Navigate should have one parameter");
                        continue;
                    } else if (parameterTypes[0] != NavigationContext.class) {
                        LOGGER.error("The parameter for the method annotated with @Navigate should be instance of ''{0}''", NavigationContext.class.getName());
                        continue;
                    }
                    callbackMethod = method;
                    found = true;
                }
            }
        }

        if (!"".equals(alias)) {
            NavigableModel parent = uiContext.getViewNavigator().getNavigableModel(extension.value());
            NavigableModel navigableModel = new NavigableModel(parent, alias, getInstanceManager().getPojoObject(), callbackMethod);
            uiContext.getViewNavigator().registerNavigableModel((Component) getInstanceManager().getPojoObject(), navigableModel);
            configuration.put(EXTENSION_ALIAS, alias);
        }
    }

    @Override
    public void initializeComponentFactory(ComponentTypeDescription typeDesc, Element metadata) throws ConfigurationException {
        super.initializeComponentFactory(typeDesc, metadata);
        // Tag this component as extension
        typeDesc.addProperty(new PropertyDescription(WEBCONSOLE_EXTENSION, "java.lang.String", "true", true));
    }

    @Override
    public HandlerDescription getDescription() {
        return super.getDescription();
    }

    public class ExtensionInstanceStateListener implements InstanceStateListener {

        private String[] specifications;
        private Dictionary<String, ?> configuration;
        private ServiceRegistration serviceRegistration;

        public ExtensionInstanceStateListener(Dictionary<String, ?> configuration) {
            List<String> listSpecifications = getSpecifications(extensionType);
            this.specifications = listSpecifications.toArray(new String[listSpecifications.size()]);
            this.configuration = configuration;
        }

        @Override
        public void stateChanged(ComponentInstance instance, int newState) {
            BundleContext bc = ownInstanceManager.getBundleContext();

            if (!SecurityHelper.hasPermissionToRegisterServices(specifications, bc)) {
                throw new SecurityException(
                        format("The bundle %d does not have the permission to register the services %s",
                                bc.getBundle().getBundleId(),
                                Arrays.asList(specifications)));
            } else {
                switch (newState) {
                    case ComponentInstance.VALID :
                        if (specifications.length >= 1) {
                            serviceRegistration =
                                    bc.registerService(specifications, getInstanceManager().getPojoObject(), configuration);
                        }
                        break;
                    case ComponentInstance.INVALID :
                    case ComponentInstance.DISPOSED :
                    case ComponentInstance.STOPPED :
                        if (serviceRegistration != null) {
                            serviceRegistration.unregister();
                            serviceRegistration = null;
                        }
                        break;
                }
            }
        }
    }

    @Override
    protected DependencyCallback createDependencyHandler(Dependency dep, String method, int type) {
        LinkDependencyCallback linkDependencyCallback = new LinkDependencyCallback(dep, method, type, uiContext.getUI(), notifierService);
        dependencyCallbacks.add(linkDependencyCallback);
        return linkDependencyCallback;
    }

    @Bind(optional = true)
    public void bindNotifierService(InternalNotifierService notifierService) {
        this.notifierService = notifierService;
        for (LinkDependencyCallback dependencyCallback : dependencyCallbacks) {
            dependencyCallback.setNotifierService(notifierService);
        }
        for (String notification : notifications) {
            notifierService.addNotification(notification);
        }
        notifications.clear();
    }

    @Unbind
    public void unbindNotifierService(INotifierService notifierService) {
        this.notifierService = null;
        for (LinkDependencyCallback dependencyCallback : dependencyCallbacks) {
            dependencyCallback.setNotifierService(null);
        }
    }

    public static interface InstanceManager {
        Object getPojoObject();
        BundleContext getBundleContext();
        void addInstanceStateListener(InstanceStateListener listener);
    }

    public class OwnInstanceManager implements InstanceManager {

        @Override
        public Object getPojoObject() {
            return ExtensionHandler.this.getInstanceManager().getPojoObject();
        }

        @Override
        public BundleContext getBundleContext() {
            return ExtensionHandler.this.getInstanceManager().getContext();
        }

        @Override
        public void addInstanceStateListener(InstanceStateListener listener) {
            ExtensionHandler.this.getInstanceManager().addInstanceStateListener(listener);
        }
    }
}
