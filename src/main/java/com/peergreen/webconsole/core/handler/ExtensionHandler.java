package com.peergreen.webconsole.core.handler;

import com.peergreen.webconsole.Constants;
import com.peergreen.webconsole.ISecurityManager;
import com.peergreen.webconsole.Inject;
import com.peergreen.webconsole.Link;
import com.peergreen.webconsole.Qualifier;
import com.peergreen.webconsole.UIContext;
import com.peergreen.webconsole.Unlink;
import com.peergreen.webconsole.core.extension.ExtensionFactory;
import com.peergreen.webconsole.core.extension.InstanceHandler;
import com.peergreen.webconsole.core.extension.InstanceState;
import com.peergreen.webconsole.INotifierService;
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Mohammed Boukada
 */
@Handler(name = "extension",
         namespace = "com.peergreen.webconsole")
public class ExtensionHandler extends DependencyHandler {

    private Class<?> extensionType;
    private Map<String, ExtensionPoint> bindings;
    private List<Field> fieldsToBind;
    private Map<ExtensionFactory, InstanceHandler> instances = new HashMap<>();
    private List<LinkDependencyCallback> dependencyCallbacks = new ArrayList<>();
    private UIContext uiContext;
    private InstanceManager ownInstanceManager = new OwnInstanceManager();
    private INotifierService notifierService;
    private List<String> notifications = new LinkedList<>();

    public void setOwnInstanceManager(InstanceManager instanceManager) {
        this.ownInstanceManager = instanceManager;
    }

    public void setUiContext(UIContext uiContext) {
        this.uiContext = uiContext;
    }

    @Override
    public void configure(Element metadata, Dictionary configuration) throws ConfigurationException {
        uiContext = (UIContext) configuration.get(Constants.UI_CONTEXT);
        configuration.remove(Constants.UI_CONTEXT);
        configuration.put(Constants.UI_ID, uiContext.getUIId());

        setExtensionType();
        setExtensionProperties(configuration);
        bindInjections();

        // Add instance state listener to (un)register component specifications as services
        ownInstanceManager.addInstanceStateListener(new ExtensionInstanceStateListener(getSpecifications(), configuration));

        super.configure(createBindings(metadata), configuration);
    }

    public void setExtensionType() {
        extensionType = ownInstanceManager.getPojoObject().getClass();
    }

    @Override
    public void stop() {
        super.stop();
        for (Map.Entry<ExtensionFactory, InstanceHandler> instance : instances.entrySet()) {
            instance.getValue().stop();
        }
    }

    @Override
    public void start() {
        super.start();
    }

    @Bind(aggregate = true, optional = true)
    public void bindExtensionFactory(ExtensionFactory extensionFactory, Dictionary properties) {
        if (canBindExtensionFactory(properties)) {
            InstanceHandler instanceHandler;
            boolean failed = false;
            try {
                instanceHandler = extensionFactory.create(uiContext);
                if (InstanceState.STOPPED.equals(instanceHandler.getState())) failed = true;
                instances.put(extensionFactory, instanceHandler);
            } catch (MissingHandlerException e) {
                e.printStackTrace();
                failed = true;
            } catch (UnacceptableConfiguration unacceptableConfiguration) {
                unacceptableConfiguration.printStackTrace();
                failed = true;
            } catch (ConfigurationException e) {
                e.printStackTrace();
                failed = true;
            }
            if (failed) {
                String error = "Fail to add an extension to '" + properties.get("extension.point") +"'";
                if(notifierService != null) notifierService.addNotification(error);
                else notifications.add(error);
            }
        }
    }

    @Unbind
    public void unbindExtensionFactory(ExtensionFactory extensionFactory) {
        if (instances.containsKey(extensionFactory)) {
            instances.get(extensionFactory).stop();
            instances.remove(extensionFactory);
        }
    }

    protected Dictionary setExtensionProperties(Dictionary configuration) {
        Annotation[] annotations = extensionType.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
                Qualifier qualifier = annotation.annotationType().getAnnotation(Qualifier.class);
                String propertiesPrefix = "".equals(qualifier.value()) ? annotation.annotationType().getName() : qualifier.value();
                for (Method method : annotation.annotationType().getDeclaredMethods()) {
                    Object value = null;
                    try {
                        value = method.invoke(annotation);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    configuration.put(propertiesPrefix + "." + method.getName(), method.getReturnType().cast(value));
                }
            }
        }
        return configuration;
    }

    protected List<Field> bindInjections() {
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
                    e.printStackTrace();
                }
            }
        }
        return fieldsToBind;
    }

    protected String[] getSpecifications() {
        Class<?>[] interfaces = extensionType.getInterfaces();
        Class<?> superClass = extensionType.getSuperclass();
        Class<?> superSuperClass = null;
        int specificationsArrayLength = interfaces.length + 1;
        if (Component.class.isAssignableFrom(superClass)) {
            superSuperClass = Component.class;
            specificationsArrayLength++;
        }
        Class<?>[] classes = new Class[specificationsArrayLength];
        System.arraycopy(interfaces, 0, classes, 0, interfaces.length);
        classes[interfaces.length] = superClass;
        if (superSuperClass != null) classes[interfaces.length + 1] = superSuperClass;

        List<String> specificationsList = new ArrayList<>();
        for (Class<?> clazz : classes) {
            if (!Pojo.class.equals(clazz) && !Object.class.equals(clazz)) {
                specificationsList.add(clazz.getName());
            }
        }

        return specificationsList.toArray(new String[specificationsList.size()]);
    }

    private boolean canBindExtensionFactory(Dictionary properties) {
        String extensionId = (String) properties.get(Constants.EXTENSION_POINT);
        return extensionId != null && bindings.containsKey(extensionId) && isInRoles(properties);
    }

    private boolean isInRoles(Dictionary properties) {
        if (uiContext.getSecurityManager() == null) return true;
        String[] extensionRoles = (String[]) properties.get(Constants.EXTENSION_ROLES);
        return uiContext.getSecurityManager().isUserInRoles(extensionRoles);
    }

    protected Element createBindings(Element metadata) {
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
                    filter = "(&(" + Constants.UI_ID + "=" + uiContext.getUIId() + ")(" +
                            Constants.EXTENSION_POINT + "=" + extensionPointId + "))";
                }

                if (bindings.containsKey(extensionPointId)) {
                    if (bindings.get(extensionPointId).getBindMethod() == null) {
                        bindings.get(extensionPointId).setBindMethod(method);
                    } else {
                        // throw exception
                    }
                } else {
                    bindings.put(extensionPointId, new ExtensionPoint(method, null, filter));
                }
            }
            if (method.isAnnotationPresent(Unlink.class)) {
                Unlink unlink = method.getAnnotation(Unlink.class);
                String extensionName = unlink.value();
                String extensionPointId = extensionType.getName() + "." + extensionName;
                String filter = "";
                if (uiContext != null) {
                    filter = "(&(" + Constants.UI_ID + "=" + uiContext.getUIId() + ")(" +
                            Constants.EXTENSION_POINT + "=" + extensionPointId + "))";
                }
                if (bindings.containsKey(extensionPointId)) {
                    if (bindings.get(extensionPointId).getUnbindMethod() == null) {
                        bindings.get(extensionPointId).setUnbindMethod(method);
                    } else {
                        // throw exception
                    }
                } else {
                    bindings.put(extensionPointId, new ExtensionPoint(null, method, filter));
                }
            }
        }

        for (Map.Entry<String, ExtensionPoint> binding : bindings.entrySet()) {
            String extensionPointId = binding.getKey();
            ExtensionPoint extensionPoint = binding.getValue();

            Element requires = new Element("requires", null);
            if (!"".equals(extensionPoint.getFilter())) {
                requires.addAttribute(new Attribute("filter", extensionPoint.getFilter()));
            }
            requires.addAttribute(new Attribute("optional", "true"));
            requires.addAttribute(new Attribute("aggregate", "true"));
            requires.addAttribute(new Attribute("id", extensionPointId));

            if (extensionPoint.getBindMethod() != null) {
                Element bindCallback = new Element("callback", null);
                bindCallback.addAttribute(new Attribute("method", extensionPoint.getBindMethod().getName()));
                bindCallback.addAttribute(new Attribute("type", "bind"));
                requires.addElement(bindCallback);
            }

            if (extensionPoint.getUnbindMethod() != null) {
                Element unbindCallback = new Element("callback", null);
                unbindCallback.addAttribute(new Attribute("method", extensionPoint.getUnbindMethod().getName()));
                unbindCallback.addAttribute(new Attribute("type", "unbind"));
                requires.addElement(unbindCallback);
            }
            newMetadata.addElement(requires);
        }

        for (Field field : fieldsToBind) {
            Element requires = new Element("requires", null);
            requires.addAttribute(new Attribute("field", field.getName()));
            newMetadata.addElement(requires);
        }
        return newMetadata;
    }

    @Override
    public void initializeComponentFactory(ComponentTypeDescription typeDesc, Element metadata) throws ConfigurationException {
        super.initializeComponentFactory(typeDesc, metadata);
        // Tag this component as extension
        typeDesc.addProperty(new PropertyDescription(Constants.WEBCONSOLE_EXTENSION, "java.lang.String", "true", true));
    }

    @Override
    public HandlerDescription getDescription() {
        return super.getDescription();
    }

    public class ExtensionInstanceStateListener implements InstanceStateListener {

        String[] specifications;
        Dictionary configuration;
        ServiceRegistration serviceRegistration;

        public ExtensionInstanceStateListener(String[] specifications, Dictionary configuration) {
            this.specifications = specifications;
            this.configuration = configuration;
        }

        @Override
        public void stateChanged(ComponentInstance instance, int newState) {
            BundleContext bc = ownInstanceManager.getBundleContext();

            if (!SecurityHelper.hasPermissionToRegisterServices(specifications, bc)) {
                throw new SecurityException("The bundle "
                        + bc.getBundle().getBundleId()
                        + " does not have the"
                        + " permission to register the services "
                        + specifications);
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
    public void bindNotifierService(INotifierService notifierService) {
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
