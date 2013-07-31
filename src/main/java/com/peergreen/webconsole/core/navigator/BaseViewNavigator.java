package com.peergreen.webconsole.core.navigator;

import com.peergreen.webconsole.Constants;
import com.peergreen.webconsole.INotifierService;
import com.peergreen.webconsole.navigator.ViewNavigator;
import com.peergreen.webconsole.navigator.NavigableModel;
import com.peergreen.webconsole.utils.UrlFragment;
import com.vaadin.navigator.Navigator;
import com.vaadin.ui.Component;

import java.lang.reflect.InvocationTargetException;
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

    public void setNav(Navigator nav) {
        this.nav = nav;
    }

    public void setNotifierService(INotifierService notifierService) {
        this.notifierService = notifierService;
    }

    public void setRootNavigableModel(NavigableModel navigableModel) {
        this.root = navigableModel;
    }

    @Override
    public void navigateTo(String path) {
        navigateTo(path, true);
    }

    public void navigateTo(String path, boolean navigate) {
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
                Component nextComponent = (Component) navigableModel.getCallbackMethod().invoke(navigableModel.getObject(), context);
                while (nextComponent != null && navigableModels.containsKey(nextComponent) && !"".equals(context.getPath())) {
                    NavigableModel model = navigableModels.get(nextComponent);
                    nextComponent = (Component) model.getCallbackMethod().invoke(model.getObject(), context);
                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                notifierService.addNotification(String.format("Cannot navigate to '%s'", path));
            }
        }
        if (navigate) {
            nav.navigateTo(path);
        }
    }

    @Override
    public String getLocation(String extension) {
        return getNavigableModel(extension).getFullPath();
    }

    @Override
    public NavigableModel getNavigableModel(String extension) {
        if (Constants.SCOPE_EXTENSION_POINT.equals(extension)) {
            return root;
        }
        for (Map.Entry<Component, NavigableModel> navigableModel : navigableModels.entrySet()) {
            if (extension.startsWith(navigableModel.getKey().getClass().getName())) {
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
}
