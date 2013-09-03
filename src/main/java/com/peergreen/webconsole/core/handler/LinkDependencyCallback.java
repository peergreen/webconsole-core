package com.peergreen.webconsole.core.handler;

import com.peergreen.webconsole.Link;
import com.peergreen.webconsole.Unlink;
import com.peergreen.webconsole.core.notifier.InternalNotifierService;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.handlers.dependency.Dependency;
import org.apache.felix.ipojo.handlers.dependency.DependencyCallback;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Mohammed Boukada
 */
public class LinkDependencyCallback extends DependencyCallback {

    private UI ui;
    private InternalNotifierService notifierService;
    private InstanceManager manager;

    /**
     * Constructor.
     *
     * @param dep        : the dependency attached to this dependency callback
     * @param method     : the method to call
     * @param methodType : is the method to call a bind method or an unbind
     *                   method
     */
    public LinkDependencyCallback(Dependency dep, String method, int methodType, UI ui, InternalNotifierService notifierService) {
        super(dep, method, methodType);
        this.ui = ui;
        this.notifierService = notifierService;
        this.manager = dep.getHandler().getInstanceManager();
    }

    @Override
    public Object call(final Object[] arg) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (m_methodObj == null) {
            searchMethod();
        }
        final Object[] newObject = {null};
        // Two cases :
        // - if instances already exists : call on each instances
        // - if no instance exists : create an instance
        if (manager.getPojoObjects() == null) {
            ui.access(new Runnable() {
                @Override
                public void run() {
                    try {
                        newObject[0] =  m_methodObj.invoke(manager.getPojoObject(), arg);
                        updateNotifier();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            for (int i = 0; i < manager.getPojoObjects().length; i++) {
                final int finalI = i;
                ui.access(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            newObject[0] = m_methodObj.invoke(manager.getPojoObjects()[finalI], arg);
                            updateNotifier();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
        return newObject[0];
    }

    @Override
    public Object call(final Object instance, final Object[] arg) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (m_methodObj == null) {
            searchMethod();
        }
        final Object[] newObject = {null};
        ui.access(new Runnable() {
            @Override
            public void run() {
                try {
                    newObject[0] = m_methodObj.invoke(instance, arg);
                    updateNotifier();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        });
        return newObject[0];
    }

    public void setNotifierService(InternalNotifierService notifierService) {
        this.notifierService = notifierService;
    }

    private void updateNotifier() {
        if (notifierService != null) {
            if (m_methodObj.isAnnotationPresent(Link.class)) {
                notifierService.incrementBadge((Component) manager.getPojoObject());
            } else if (m_methodObj.isAnnotationPresent(Unlink.class)) {
                notifierService.decrementBadge((Component) manager.getPojoObject());
            }
        }
    }
}
