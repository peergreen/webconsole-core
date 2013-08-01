package com.peergreen.webconsole.core.navigator;

import com.peergreen.webconsole.Constants;
import com.peergreen.webconsole.INotifierService;
import com.peergreen.webconsole.core.exception.ExceptionView;
import com.peergreen.webconsole.core.scope.NavigatorView;
import com.peergreen.webconsole.core.scope.Scope;
import com.peergreen.webconsole.navigator.ViewNavigator;
import com.peergreen.webconsole.navigator.NavigableModel;
import com.peergreen.webconsole.utils.UrlFragment;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Mohammed Boukada
 */
public class BaseViewNavigator implements ViewNavigator {

    private Navigator nav;
    private INotifierService notifierService;
    private NavigableModel root;
    private Map<Component, NavigableModel> navigableModels = new ConcurrentHashMap<>();
    private Map<String, Scope> scopes = new ConcurrentHashMap<>();

    public BaseViewNavigator(Navigator nav, NavigableModel rootNavigableModel) {
        this.nav = nav;
        this.root = rootNavigableModel;
        configure();
    }

    private void configure() {
        this.nav.addView("", new NavigatorView(new CssLayout()));
        this.nav.addView("/", new NavigatorView(new CssLayout()));
        this.nav.addViewChangeListener(new NavigatorViewChangeListener());
    }

    public void setNotifierService(INotifierService notifierService) {
        this.notifierService = notifierService;
    }

    @Override
    public void navigateTo(String path) {
        nav.navigateTo(path);
    }

    public void navigate(String path) {
        String localPath = UrlFragment.getFirstFragment(path);

        NavigableModel navigableModel = null;
        for (Map.Entry<Component, NavigableModel> navigable : navigableModels.entrySet()) {
            if (localPath.equals(navigable.getValue().getFullPath())) {
                navigableModel = navigable.getValue();
            }
        }

        if (navigableModel == null) {
            notifierService.addNotification(String.format("Cannot navigate to '%s'", path));
        }
        else {
            BaseNavigableContext context = new BaseNavigableContext(UrlFragment.subFirstFragment(path));
            try {
                Method callbackMethod = navigableModel.getCallbackMethod();
                Component nextComponent = null;
                if (callbackMethod != null) {
                    nextComponent = (Component) callbackMethod.invoke(navigableModel.getObject(), context);
                }
                while (nextComponent != null
                        && navigableModels.containsKey(nextComponent)
                        && navigableModels.get(nextComponent).getCallbackMethod() != null
                        && !"".equals(context.getPath())) {
                    NavigableModel model = navigableModels.get(nextComponent);
                    nextComponent = (Component) model.getCallbackMethod().invoke(model.getObject(), context);
                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                notifierService.addNotification(String.format("Cannot navigate to '%s'", path));
            }
        }
    }

    public void addRoute(Scope scope) {
        scopes.put(scope.getScopeAlias(), scope);
        nav.removeView(scope.getScopeAlias());
        View view;
        try {
            view = new NavigatorView(scope.getScopeView());
        } catch (Exception e) {
            view = new NavigatorView(new ExceptionView(e));
        }
        nav.addView(scope.getScopeAlias(), view);

        if ("home".equals(scope.getScopeName().toLowerCase())) {
            nav.addView("", view);
            nav.addView("/", view);
        }
    }

    public void removeRoute(Scope scope) {
        nav.removeView(scope.getScopeAlias());
        if ("home".equals(scope.getScopeName().toLowerCase())) {
            nav.removeView("");
            nav.removeView("/");
        }
        scopes.remove(scope.getScopeAlias());
    }

    @Override
    public String getLocation(String extension) {
        NavigableModel navigableModel = getNavigableModel(extension);
        if (navigableModel != null) {
            return navigableModel.getFullPath();
        }
        return null;
    }

    @Override
    public NavigableModel getNavigableModel(String extension) {
        if (Constants.SCOPE_EXTENSION_POINT.equals(extension)) {
            return root;
        }
        for (Map.Entry<Component, NavigableModel> navigableModel : navigableModels.entrySet()) {
            if (extensionMatchClassName(extension, navigableModel.getKey().getClass().getName())) {
                return navigableModel.getValue();
            }
        }
        return null;
    }

    @Override
    public void registerNavigableModel(Component component, NavigableModel navigableModel) {
        navigableModels.put(component, navigableModel);
    }

    @Override
    public void unregisterNavigableModel(Component component) {
        navigableModels.remove(component);
    }

    private boolean extensionMatchClassName(String extension, String className) {
        return extension.matches(className + ".*");
    }

    public class NavigatorViewChangeListener implements ViewChangeListener {
        @Override
        public boolean beforeViewChange(ViewChangeEvent event) {
            notifierService.closeAll();
            for (Map.Entry<String, Scope> scopeEntry : scopes.entrySet()) {
                scopeEntry.getValue().getScopeMenuButton().removeStyleName("selected");
            }
            if (event.getParameters() != null && !"".equals(event.getParameters())) {
                navigate(event.getViewName() + "/" + event.getParameters());
            }
            return true;
        }

        @Override
        public void afterViewChange(ViewChangeEvent event) {
            if (event.getViewName().equals(UrlFragment.getFirstFragment(event.getNavigator().getState()))) {
                String alias = event.getViewName();
                if ("".equals(alias) || "/".equals(alias)) {
                    alias = "/home";
                }
                scopes.get(alias).getScopeMenuButton().addStyleName("selected");
            }
        }
    }
}
