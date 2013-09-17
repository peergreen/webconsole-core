package com.peergreen.webconsole.core.navigator;

import com.peergreen.webconsole.Constants;
import com.peergreen.webconsole.notifier.INotifierService;
import com.peergreen.webconsole.core.exception.ExceptionView;
import com.peergreen.webconsole.core.scope.NavigatorView;
import com.peergreen.webconsole.core.scope.Scope;
import com.peergreen.webconsole.core.vaadin7.BaseUI;
import com.peergreen.webconsole.navigator.NavigableModel;
import com.peergreen.webconsole.navigator.ViewNavigator;
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
 * Vaadin view navigator implementation
 *
 * @author Mohammed Boukada
 */
public class BaseViewNavigator implements ViewNavigator {

    private Navigator nav;
    private INotifierService notifierService;
    private NavigableModel root;
    private Map<Component, NavigableModel> navigableModels = new ConcurrentHashMap<>();
    private Map<String, Scope> scopes = new ConcurrentHashMap<>();

    /**
     * Create new view navigator
     *
     * @param nav                Vaadin navigator
     * @param rootNavigableModel root (UI) navigable model
     */
    public BaseViewNavigator(Navigator nav, NavigableModel rootNavigableModel) {
        this.nav = nav;
        this.root = rootNavigableModel;
        configure();
    }

    /**
     * Configure navigator
     */
    private void configure() {
        this.nav.addView("", new NavigatorView(new CssLayout()));
        this.nav.addView("/", new NavigatorView(new CssLayout()));
        this.nav.addViewChangeListener(new NavigatorViewChangeListener());
    }

    public void setNotifierService(INotifierService notifierService) {
        this.notifierService = notifierService;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void navigateTo(String path) {
        nav.navigateTo(path);
    }

    /**
     * Navigate to the given path by calling all callback methods annotated by
     * {@link com.peergreen.webconsole.navigator.Navigate} of each {@link com.peergreen.webconsole.navigator.NavigableModel}
     * through the path.
     *
     * @param path path to extension
     */
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
        } else {
            BaseNavigationContext context = new BaseNavigationContext(UrlFragment.subFirstFragment(path));
            try {
                Method callbackMethod = navigableModel.getCallbackMethod();
                Component nextComponent = null;
                if (callbackMethod != null) {
                    nextComponent = (Component) callbackMethod.invoke(navigableModel.getExtension(), context);
                }
                while (nextComponent != null
                        && navigableModels.containsKey(nextComponent)
                        && navigableModels.get(nextComponent).getCallbackMethod() != null
                        && !"".equals(context.getPath())) {
                    NavigableModel model = navigableModels.get(nextComponent);
                    nextComponent = (Component) model.getCallbackMethod().invoke(model.getExtension(), context);
                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                notifierService.addNotification(String.format("Cannot navigate to '%s'", path));
            }
        }
    }

    /**
     * Add scope alias to navigator
     *
     * @param scope given scope
     */
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

        if (BaseUI.HOME_SCOPE.equals(scope.getScopeName().toLowerCase())) {
            nav.addView("", view);
            nav.addView("/", view);
        }
    }

    /**
     * Remove scope alias from navigator
     *
     * @param scope given scope
     */
    public void removeRoute(Scope scope) {
        nav.removeView(scope.getScopeAlias());
        if (BaseUI.HOME_SCOPE.equals(scope.getScopeName().toLowerCase())) {
            nav.removeView("");
            nav.removeView("/");
        }
        scopes.remove(scope.getScopeAlias());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLocation(String extension) {
        NavigableModel navigableModel = getNavigableModel(extension);
        if (navigableModel != null) {
            return navigableModel.getFullPath();
        }
        return null;
    }

    /**
     * {@inheritDoc }
     */
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

    /**
     * {@inheritDoc }
     */
    @Override
    public void registerNavigableModel(Component component, NavigableModel navigableModel) {
        navigableModels.put(component, navigableModel);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void unregisterNavigableModel(Component component) {
        navigableModels.remove(component);
    }

    /**
     * Test event given extension id matches extension className.
     *
     * @param extension extension id. <br/>
     *                  This parameter could be the extension class name or one of its own extension point.
     * @param className className
     * @return True if extension matches className, else returns False.
     */
    private boolean extensionMatchClassName(String extension, String className) {
        return extension.matches(className + ".*");
    }

    /**
     * View change listener.
     *
     * @author Mohammed Boukada
     */
    public class NavigatorViewChangeListener implements ViewChangeListener {

        /**
         * {@inheritDoc} <br />
         * <p/>
         * Navigate to the next view.
         * Remove style from the previous.
         * Update context.
         */
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

        /**
         * {@inheritDoc} <br />
         * <p/>
         * Update style for the selected view
         */
        @Override
        public void afterViewChange(ViewChangeEvent event) {
            if (event.getViewName().equals(UrlFragment.getFirstFragment(event.getNavigator().getState()))) {
                String alias = event.getViewName();
                if ("".equals(alias) || "/".equals(alias)) {
                    alias = BaseUI.HOME_ALIAS;
                }
                if (scopes.containsKey(alias)) {
                    scopes.get(alias).getScopeMenuButton().addStyleName("selected");
                }
            }
        }
    }
}
