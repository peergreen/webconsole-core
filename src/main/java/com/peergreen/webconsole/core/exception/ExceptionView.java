package com.peergreen.webconsole.core.exception;

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * Exception view
 * @author Mohammed Boukada
 */
public class ExceptionView extends VerticalLayout {

    public ExceptionView(Exception ex) {

        setSizeFull();
        addStyleName("dashboard-view");

        HorizontalLayout top = new HorizontalLayout();
        top.setWidth("100%");
        top.setSpacing(true);
        top.addStyleName("toolbar");
        addComponent(top);
        final Label title = new Label("Oops ! A problem occurred when drawing this view");
        title.setSizeUndefined();
        title.addStyleName("h1");
        top.addComponent(title);
        top.setComponentAlignment(title, Alignment.MIDDLE_LEFT);
        top.setExpandRatio(title, 1);

        HorizontalLayout row = new HorizontalLayout();
        row.setSizeFull();
        row.setMargin(new MarginInfo(true, true, false, true));
        row.setSpacing(true);
        addComponent(row);
        setExpandRatio(row, 1.5f);

        Table t = new Table();
        t.setCaption("Stack trace");
        t.addContainerProperty("<p style=\"display:none\">Stack</p>", String.class, null);
        t.setWidth("100%");
        t.setImmediate(true);
        t.addStyleName("plain");
        t.addStyleName("borderless");
        t.setSortEnabled(false);
        t.setImmediate(true);
        t.setSizeFull();

        int i = 1;
        t.addItem(new Object[] {ex.toString()}, i++);
        for (StackTraceElement element : ex.getStackTrace()) {
            t.addItem(new Object[] {element.toString()}, i++);
        }
        CssLayout panel = new CssLayout();
        panel.addStyleName("layout-panel");
        panel.setSizeFull();

        panel.addComponent(t);

        row.addComponent(panel);
    }
}
