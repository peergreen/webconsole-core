package com.peergreen.webconsole.core.scope;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;

/**
 * Build a scope view for Navigator
 * @author Mohammed Boukada
 */
public class NavigatorView extends CssLayout implements View {

    public NavigatorView(Component component) {
        setSizeFull();
        addComponent(component);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

    }
}
