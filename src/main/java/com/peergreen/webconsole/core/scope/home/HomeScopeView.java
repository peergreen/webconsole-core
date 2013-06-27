package com.peergreen.webconsole.core.scope.home;

import com.peergreen.newsfeed.FeedMessage;
import com.peergreen.newsfeed.Rss;
import com.peergreen.newsfeed.RssService;
import com.peergreen.newsfeed.RssServiceException;
import com.vaadin.event.ShortcutAction;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Home scope view
 */
public class HomeScopeView extends VerticalLayout {

    /**
     * Peergreen rss flow url
     */
    private final static String PEERGREEN_RSS_FLOW_URL = "http://www.peergreen.com/Blog/BlogRss?xpage=plain";

    public HomeScopeView(RssService rssService) {
        setSizeFull();
        addStyleName("dashboard-view");


        HorizontalLayout top = new HorizontalLayout();
        top.setWidth("100%");
        top.setSpacing(true);
        top.addStyleName("toolbar");
        addComponent(top);
        final Label title = new Label("Welcome to Peergreen Administration Console");
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

        row.addComponent(createPanel(peergreenNewsFeed(rssService)));

        TextArea notes = new TextArea("Notes");
        notes.setValue("Friday may 17, 2013 : OSGi France user group conf'");
        notes.setSizeFull();
        CssLayout panel = createPanel(notes);
        panel.addStyleName("notes");
        row.addComponent(panel);
    }

    /**
     * Creat a panel
     * @param content
     * @return
     */
    private CssLayout createPanel(Component content) {
        CssLayout panel = new CssLayout();
        panel.addStyleName("layout-panel");
        panel.setSizeFull();

        panel.addComponent(content);
        return panel;
    }

    /**
     * Peergreen news feed panel
     * @param rssService
     * @return
     */
    private Component peergreenNewsFeed(RssService rssService) {
        Rss rss = null;
        try {
            rss = rssService.parse(new URL(PEERGREEN_RSS_FLOW_URL));
        } catch (RssServiceException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        Table t = new Table();
        t.setCaption("Peergreen News");
        t.addContainerProperty("<p style=\"display:none\">Title</p>", Button.class, null);
        t.setWidth("100%");
        t.setPageLength(10);
        t.setImmediate(true);
        t.addStyleName("plain");
        t.addStyleName("borderless");
        t.setSortEnabled(false);
        t.setImmediate(true);

        int i = 1;
        for(final FeedMessage feedMessage : rss.getItems()) {
            Button news = new NativeButton(feedMessage.getTitle());
            news.addStyleName("link");
            news.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    Window w = getNewsDescription(feedMessage);
                    UI.getCurrent().addWindow(w);
                    w.focus();
                }
            });

            t.addItem(new Object[] {
                    news
                    },
                    i++);
        }

        return t;
    }

    /**
     * News popup
     * @param feedMessage
     * @return
     */
    private Window getNewsDescription(FeedMessage feedMessage) {
        final Window w = new Window();
        VerticalLayout l = new VerticalLayout();
        l.setSpacing(true);

        w.setCaption(feedMessage.getTitle());
        w.setContent(l);
        w.center();
        w.setCloseShortcut(ShortcutAction.KeyCode.ESCAPE, null);
        w.setResizable(false);
        w.setClosable(false);

        addStyleName("no-vertical-drag-hints");
        addStyleName("no-horizontal-drag-hints");

        HorizontalLayout details = new HorizontalLayout();
        details.setSpacing(true);
        details.setMargin(true);
        l.addComponent(details);

        FormLayout fields = new FormLayout();
        fields.setWidth("35em");
        fields.setSpacing(true);
        fields.setMargin(true);
        details.addComponent(fields);

        Label label = new Label("<a href=\"" + feedMessage.getLink() + "\">" + feedMessage.getLink().substring(0, 50) + "..." + "</a>");
        label.setContentMode(ContentMode.HTML);
        label.setSizeUndefined();
        label.setCaption("URL");
        fields.addComponent(label);

        Label desc = new Label(feedMessage.getDescription());
        desc.setContentMode(ContentMode.HTML);
        desc.setCaption("Description");
        fields.addComponent(desc);

        HorizontalLayout footer = new HorizontalLayout();
        footer.addStyleName("footer");
        footer.setWidth("100%");
        footer.setMargin(true);

        Button ok = new Button("Close");
        ok.addStyleName("wide");
        ok.addStyleName("default");
        ok.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                w.close();
            }
        });
        footer.addComponent(ok);
        footer.setComponentAlignment(ok, Alignment.TOP_RIGHT);
        l.addComponent(footer);

        return w;
    }

    private Component getMemoryInformation() {
        return null;
    }
}
