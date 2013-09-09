package com.peergreen.webconsole.core.vaadin7;

import com.peergreen.security.UsernamePasswordAuthenticateService;
import com.peergreen.security.principal.RoleGroup;
import com.peergreen.security.principal.RolePrincipal;
import com.peergreen.security.principal.UserPrincipal;
import com.peergreen.webconsole.Constants;
import com.peergreen.webconsole.ISecurityManager;
import com.peergreen.webconsole.core.context.BaseUIContext;
import com.peergreen.webconsole.core.extension.ExtensionFactory;
import com.peergreen.webconsole.core.extension.InstanceHandle;
import com.peergreen.webconsole.core.extension.InstanceState;
import com.peergreen.webconsole.core.navigator.BaseViewNavigator;
import com.peergreen.webconsole.core.notifier.InternalNotifierService;
import com.peergreen.webconsole.core.scope.Scope;
import com.peergreen.webconsole.core.scope.ScopeFactory;
import com.peergreen.webconsole.core.security.SecurityManager;
import com.peergreen.webconsole.navigator.NavigableModel;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.data.Property;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.UIDetachedException;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.util.CurrentInstance;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import javax.security.auth.Subject;
import javax.servlet.http.Cookie;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base console UI
 *
 * @author Mohammed Boukada
 */
@Theme("dashboard")
@PreserveOnRefresh
@org.apache.felix.ipojo.annotations.Component
@Provides(specifications = UI.class)
@Push(transport = Transport.STREAMING)
public class BaseUI extends UI implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String PEERGREEN_USER_COOKIE_NAME = "peergreen-user";

    private static final String ANONYMOUS_USER = "Anonymous";

    private static final String MAX_WIDTH = "100%";
    private static final String MAX_HEIGHT = "100%";

    public static final String HOME_SCOPE = "home";

    public static final String HOME_ALIAS = "/" + HOME_SCOPE;

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(BaseUI.class);

    /**
     * Root layout
     */
    private CssLayout root = new CssLayout();

    /**
     * Progress indicator layout
     */
    private VerticalLayout progressIndicatorLayout;

    /**
     * Menu layout
     */
    private CssLayout menu;

    /**
     * Content layout
     */
    private CssLayout content = new CssLayout();

    private Window notifications;

    /**
     * Main content layout
     */
    private HorizontalLayout main;

    /**
     * Progress indicator
     */
    private ProgressIndicator progressIndicator = new ProgressIndicator(new Float(0.0));

    private int nbScopesToBind = 0;

    /**
     * Scopes bound
     */
    private Map<ExtensionFactory, ScopeFactory> scopesFactories = new ConcurrentHashMap<>();
    private Map<String, Scope> scopes = new ConcurrentHashMap<>();

    /**
     * SecuredConsole name
     */
    private String consoleName;
    private Boolean enableSecurity;
    private List<String> defaultRoles;

    private String scopeExtensionPoint;

    /**
     * Security manager
     */
    private ISecurityManager securityManager;
    private BaseViewNavigator viewNavigator;

    /**
     * UI id
     */
    private String uiId;

    /**
     * Notifier service
     */
    @Requires
    private InternalNotifierService notifierService;

    /**
     * Authentication service
     */
    @Requires
    private UsernamePasswordAuthenticateService authenticateService;

    /**
     * Base console UI constructor
     */
    public BaseUI(String consoleName, String extensionPoint, String uiId, Boolean enableSecurity, List<String> defaultRoles) {
        this.consoleName = consoleName;
        this.scopeExtensionPoint = extensionPoint;
        this.uiId = uiId;
        this.enableSecurity = enableSecurity;
        this.defaultRoles = defaultRoles;

        NavigableModel rootNavigableModel = new NavigableModel(null, "", null, null);
        this.viewNavigator = new BaseViewNavigator(new Navigator(this, content), rootNavigableModel);
    }

    @Validate
    public void start() {
        viewNavigator.setNotifierService(notifierService);
    }

    @Invalidate
    public void stop() {
        notifierService.clearComponentsForUI(this);
        for (Map.Entry<ExtensionFactory, ScopeFactory> scopeFactoryEntry : scopesFactories.entrySet()) {
            ScopeFactory scopeFactory = scopeFactoryEntry.getValue();
            if (scopeFactory.getInstance() != null) {
                scopeFactory.getInstance().stop();
                scopeFactory.setInstance(null);
            }
        }
    }

    /**
     * Bind a scope factory
     *
     * @param extensionFactory
     */
    @Bind(aggregate = true, optional = true)
    public void bindExtensionFactory(ExtensionFactory extensionFactory, Dictionary props) {
        if (canAddExtensionFactory(props)) {
            scopesFactories.remove(extensionFactory);
            String[] roles = (String[]) props.get(Constants.EXTENSION_ROLES);
            List<String> listRoles = (roles == null) ? new ArrayList<String>() : Arrays.asList(roles);
            ScopeFactory scopeFactory = new ScopeFactory(listRoles);
            if (progressIndicator.getValue() >= 1 && isAllowedToShowScope(listRoles)) {
                boolean failed = false;
                try {
                    InstanceHandle instance = extensionFactory.create(new BaseUIContext(this, viewNavigator, securityManager, uiId));
                    if (InstanceState.STOPPED.equals(instance.getState())) {
                        failed = true;
                    }
                    scopeFactory.setInstance(instance);
                } catch (MissingHandlerException | UnacceptableConfiguration | ConfigurationException e) {
                    LOGGER.error(e.getMessage(), e);
                    failed = true;
                }
                if (failed) {
                    String error = "Fail to add a scope for main UI. Please see logs";
                    if (notifierService != null) {
                        notifierService.addNotification(error);
                    }
                }
            }
            scopesFactories.put(extensionFactory, scopeFactory);
        }
    }

    /**
     * Unbind a scope factory
     *
     * @param extensionFactory
     */
    @Unbind
    public void unbindExtensionFactory(ExtensionFactory extensionFactory) {
        if (scopesFactories.containsKey(extensionFactory)) {
            if (scopesFactories.get(extensionFactory).getInstance() != null) {
                scopesFactories.get(extensionFactory).getInstance().stop();
            }
            scopesFactories.remove(extensionFactory);
        }
    }

    private boolean canAddExtensionFactory(Dictionary props) {
        String extensionId = (String) props.get(Constants.EXTENSION_POINT);
        return extensionId != null && scopeExtensionPoint.equals(extensionId);
    }

    @Bind(aggregate = true, optional = true)
    public void bindScopeView(Component scopeView, Dictionary props) {
        String scopeName = (String) props.get("scope.value");
        Object iconClass = props.get("scope.iconClass");
        String scopeIconClass = (iconClass == null || "".equals(iconClass)) ? "icon-" + scopeName : (String) props.get("scope.iconClass");
        String scopeAlias = (String) props.get(Constants.EXTENSION_ALIAS);
        Scope scope = new Scope(scopeName, scopeAlias, scopeIconClass, scopeView);
        scopes.put(scopeAlias, scope);
        viewNavigator.addRoute(scope);
        addScopeButtonInMenu(scope, progressIndicator.getValue() >= 1);
    }

    @Unbind
    public void unbindScopeView(Component scopeView, Dictionary props) {
        String scopeAlias = (String) props.get(Constants.EXTENSION_ALIAS);
        viewNavigator.removeRoute(scopes.get(scopeAlias));
        removeScopeButtonInMenu(scopes.get(scopeAlias));
        scopes.remove(scopeAlias);
    }

    /**
     * Init UI
     *
     * @param request
     */
    @Override
    protected void init(VaadinRequest request) {
        setLocale(Locale.US);
        getPage().setTitle("Welcome to " + consoleName);
        setContent(root);
        root.addStyleName("root");
        root.setSizeFull();

        Label bg = new Label();
        bg.setSizeUndefined();
        bg.addStyleName("login-bg");
        root.addComponent(bg);

        Boolean isLogged = (Boolean) getSession().getAttribute("is.logged");
        if (!enableSecurity || (isLogged != null && isLogged)) {
            securityManager = (ISecurityManager) getSession().getAttribute("security.manager");
            if (securityManager == null) {
                Subject defaultSubject = new Subject();
                defaultSubject.getPrincipals().add(new UserPrincipal(ANONYMOUS_USER));
                RoleGroup group = new RoleGroup();
                if (defaultRoles != null) {
                    for (String role : defaultRoles) {
                        group.addMember(new RolePrincipal(role));
                    }
                }
                defaultSubject.getPrincipals().add(group);
                defaultSubject.setReadOnly();
                securityManager = new SecurityManager(defaultSubject);
                getSession().setAttribute("security.manager", securityManager);
            }
            buildMainView();
        } else {
//            Cookie userCookie = getCookieByName(PEERGREEN_USER_COOKIE_NAME);
//            if (userCookie != null) {
//                String token = userCookie.getValue();
//                // get user by token and show main view
//            }
            buildLoginView(false);
        }
    }

    /**
     * Build login view
     *
     * @param exit
     */
    private void buildLoginView(final boolean exit) {
        if (exit) {
            root.removeAllComponents();
        }
        notifierService.closeAll();

        addStyleName("login");

        VerticalLayout loginLayout = new VerticalLayout();
        loginLayout.setId("webconsole_loginlayout_id");
        loginLayout.setSizeFull();
        loginLayout.addStyleName("login-layout");
        root.addComponent(loginLayout);

        final CssLayout loginPanel = new CssLayout();
        loginPanel.addStyleName("login-panel");

        HorizontalLayout labels = new HorizontalLayout();
        labels.setWidth(MAX_WIDTH);
        labels.setMargin(true);
        loginPanel.addComponent(labels);

        Label welcome = new Label("Welcome");
        welcome.addStyleName("h4");
        labels.addComponent(welcome);
        labels.setComponentAlignment(welcome, Alignment.MIDDLE_LEFT);

        Label title = new Label(consoleName);
        //title.setSizeUndefined();
        title.addStyleName("h2");
        title.addStyleName("light");
        labels.addComponent(title);
        labels.setComponentAlignment(title, Alignment.MIDDLE_RIGHT);

        HorizontalLayout fields = new HorizontalLayout();
        fields.setSpacing(true);
        fields.setMargin(true);
        fields.addStyleName("fields");

        final TextField username = new TextField("Username");
        username.focus();
        username.setId("webconsole_login_username");
        fields.addComponent(username);

        final PasswordField password = new PasswordField("Password");
        password.setId("webconsole_login_password");
        fields.addComponent(password);

        final Button signin = new Button("Sign In");
        signin.setId("webconsole_login_signin");
        signin.addStyleName("default");
        fields.addComponent(signin);
        fields.setComponentAlignment(signin, Alignment.BOTTOM_LEFT);

        final ShortcutListener enter = new ShortcutListener("Sign In",
                ShortcutAction.KeyCode.ENTER, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                signin.click();
            }
        };

        signin.addShortcutListener(enter);
        loginPanel.addComponent(fields);

        HorizontalLayout bottomRow = new HorizontalLayout();
        bottomRow.setWidth(MAX_WIDTH);
        bottomRow.setMargin(new MarginInfo(false, true, false, true));
        final CheckBox keepLoggedIn = new CheckBox("Keep me logged in");
        bottomRow.addComponent(keepLoggedIn);
        bottomRow.setComponentAlignment(keepLoggedIn, Alignment.MIDDLE_LEFT);
        // Add new error message
        final Label error = new Label(
                "Wrong username or password.",
                ContentMode.HTML);
        error.setId("webconsole_login_error");
        error.addStyleName("error");
        error.setSizeUndefined();
        error.addStyleName("light");
        // Add animation
        error.addStyleName("v-animate-reveal");
        error.setVisible(false);
        bottomRow.addComponent(error);
        bottomRow.setComponentAlignment(error, Alignment.MIDDLE_RIGHT);
        loginPanel.addComponent(bottomRow);

        signin.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (authenticate(username.getValue(), password.getValue())) {
//                    if (keepLoggedIn.getValue()) {
//                        //Cookie userCookie = getCookieByName(PEERGREEN_USER_COOKIE_NAME);
//                       if (getCookieByName(PEERGREEN_USER_COOKIE_NAME) == null) {
//                            // Get a token for this user and create a cooki
//                            Page.getCurrent().getJavaScript().execute( String.format("document.cookie = '%s=%s; path=%s'",
//                                    PEERGREEN_USER_COOKIE_NAME, token, VaadinService.getCurrentRequest().getContextPath()));
//                        } else {
//                            // update token
//                            userCookie.setValue(token);
//                            userCookie.setPath(VaadinService.getCurrentRequest().getContextPath());
//                        }
//                    }

                    buildMainView();
                } else {
                    error.setVisible(true);
                }
            }
        });

        loginLayout.addComponent(loginPanel);
        loginLayout.setComponentAlignment(loginPanel, Alignment.MIDDLE_CENTER);
    }

    private Cookie getCookieByName(String name) {
        // Fetch all cookies from the request
        Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();

        // Iterate to find cookie by its name
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie;
            }
        }

        return null;
    }

    private boolean authenticate(String username, String password) {
        Subject subject = authenticateService.authenticate(username, password);
        if (subject != null) {
            securityManager = new SecurityManager(subject);
            getSession().setAttribute("is.logged", true);
            getSession().setAttribute("security.manager", securityManager);
            return true;
        }
        return false;
    }

    /**
     * Build main view
     */
    private void buildMainView() {
        getPage().setTitle(consoleName);
        menu = new CssLayout();
        menu.setId("webconsole_mainlayout_id");

        notifierService.closeAll();
        final Button notify = new Button("");
        final HorizontalLayout tasksBar = new HorizontalLayout();
        tasksBar.setMargin(true);

        // Build menu layout
        main = new HorizontalLayout() {
            {
                setSizeFull();
                addStyleName("main-view");

                addComponent(new SidebarView());
                VerticalLayout contentRoot = new ConsoleContentView(notify, tasksBar);
                addComponent(contentRoot);
                setExpandRatio(contentRoot, 1);
            }

        };
        notifications = new NotificationWindow();
        notifierService.addNotificationsButton(notify, notifications, this);
        notifierService.addTasksBar(tasksBar, this);

        menu.removeAllComponents();

        //Compute nb scopes to bind
        Map<ExtensionFactory, ScopeFactory> scopesToBind = new HashMap<>();
        for (Map.Entry<ExtensionFactory, ScopeFactory> scopeFactoryEntry : scopesFactories.entrySet()) {
            if (isAllowedToShowScope(scopeFactoryEntry.getValue().getRoles())) {
                scopesToBind.put(scopeFactoryEntry.getKey(), scopeFactoryEntry.getValue());
            }
        }
        nbScopesToBind = scopesToBind.size();

        // Tell scopesFactories view factories to create views
        createScopeViews(scopesToBind);

        // Start progress indicator
        root.removeAllComponents();
        progressIndicatorLayout = new VerticalLayout();
        progressIndicatorLayout.setSizeFull();
        progressIndicatorLayout.addStyleName("login-layout");
        root.addComponent(progressIndicatorLayout);
        buildProgressIndicatorView();

        menu.addStyleName("menu");
        menu.setHeight(MAX_HEIGHT);

        navigateToPageLocation();
    }

    /**
     * Create scopes views
     *
     * @param scopesToBind scope to bound
     */
    private void createScopeViews(Map<ExtensionFactory, ScopeFactory> scopesToBind) {
        for (Map.Entry<ExtensionFactory, ScopeFactory> scopeFactoryEntry : scopesToBind.entrySet()) {
            if (isAllowedToShowScope(scopeFactoryEntry.getValue().getRoles())) {
                ExtensionFactory extensionFactory = scopeFactoryEntry.getKey();
                ScopeFactory scopeFactory = scopeFactoryEntry.getValue();
                boolean failed = false;
                try {
                    InstanceHandle instance = extensionFactory.create(new BaseUIContext(this, viewNavigator, securityManager, uiId));
                    if (InstanceState.STOPPED.equals(instance.getState())) {
                        failed = true;
                    }
                    scopeFactory.setInstance(instance);
                } catch (MissingHandlerException | UnacceptableConfiguration | ConfigurationException e) {
                    LOGGER.error(e.getMessage(), e);
                    failed = true;
                }
                if (failed) {
                    String error = "Fail to add a scope for main UI. Please see logs";
                    if (notifierService != null) {
                        notifierService.addNotification(error);
                    }
                }
            }
        }
    }

    /**
     * Get page location and navigate to
     */
    private void navigateToPageLocation() {
        String f = Page.getCurrent().getUriFragment();
        if (f != null && f.startsWith("!")) {
            f = f.substring(1);
        }

        if (f == null) {
            viewNavigator.navigateTo("/");
        } else {
            viewNavigator.navigateTo(f);
        }
    }

    private void buildProgressIndicatorView() {

        final CssLayout progressPanel = new CssLayout();
        progressPanel.addStyleName("login-panel");

        HorizontalLayout labels = new HorizontalLayout();
        labels.setWidth(MAX_WIDTH);
        labels.setMargin(true);
        progressPanel.addComponent(labels);

        Label welcome = new Label("Welcome " + ((securityManager == null) ? "" : securityManager.getUserName()));
        welcome.addStyleName("h4");
        labels.addComponent(welcome);
        labels.setComponentAlignment(welcome, Alignment.MIDDLE_LEFT);

        Label title = new Label(consoleName);
        title.addStyleName("h2");
        title.addStyleName("light");
        labels.addComponent(title);
        labels.setComponentAlignment(title, Alignment.MIDDLE_RIGHT);

        Float scopesViewsBound = (float) scopes.size();
        final Float stopValue = new Float(1.0);

        if (scopesFactories.isEmpty()) {
            progressIndicator.setValue(stopValue);
        } else {
            progressIndicator.setValue(scopesViewsBound / nbScopesToBind);
        }

        if (stopValue.equals(progressIndicator.getValue())) {
            showMainContent();
        } else {
            TimeOutThread timeOutThread = new TimeOutThread();
            timeOutThread.start();
            progressIndicator.setPollingInterval(200);
            progressIndicator.addValueChangeListener(new Property.ValueChangeListener() {
                @Override
                public void valueChange(Property.ValueChangeEvent event) {
                    if (stopValue.equals(event.getProperty().getValue())) {
                        showMainContent();
                    }
                }
            });
        }

        HorizontalLayout progressBarPanel = new HorizontalLayout();
        progressBarPanel.setWidth(MAX_WIDTH);
        progressBarPanel.setMargin(true);
        progressBarPanel.addComponent(progressIndicator);
        progressBarPanel.setComponentAlignment(progressIndicator, Alignment.MIDDLE_CENTER);
        progressPanel.addComponent(progressBarPanel);

        progressIndicatorLayout.addComponent(progressPanel);
        progressIndicatorLayout.setComponentAlignment(progressPanel, Alignment.MIDDLE_CENTER);
    }

    protected void showMainContent() {
        removeStyleName("login");
        root.removeAllComponents();
        root.addComponent(main);
    }

    /**
     * Format console title
     *
     * @param title
     * @return
     */
    private String formatTitle(String title) {
        String[] words = title.split(" ");
        StringBuilder sb = new StringBuilder();
        sb.append("<center><span>");
        for (String word : words) {
            sb.append(word);
            sb.append("<br />");
        }
        sb.append("</span></center>");
        return sb.toString();
    }

    /**
     * Add scope button in menu
     *
     * @param scope
     * @param notify for notifierService to show badge
     */
    private void addScopeButtonInMenu(final Scope scope, boolean notify) {
        if (menu != null) {
            final Button b = new NativeButton(scope.getScopeName().toUpperCase());

            b.addStyleName(scope.getScopeIconClass());

            b.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    //clearMenuSelection();
                    notifierService.removeBadge(scope.getScopeView());
                    viewNavigator.navigateTo(scope.getScopeAlias());
                }
            });

            sortButtonsInMenu(scope.getScopeAlias(), b);

            notifierService.addScopeButton(scope.getScopeView(), b, this, notify);

            scopes.get(scope.getScopeAlias()).setScopeMenuButton(b);

            if (nbScopesToBind > 0) {
                Float progressIndicatorValue = progressIndicator.getValue();
                progressIndicatorValue += (float) (1.0 / nbScopesToBind);
                progressIndicator.setValue(progressIndicatorValue);
            }
        }
    }

    /**
     * Remove scope button from menu
     *
     * @param scope
     */
    private void removeScopeButtonInMenu(final Scope scope) {
        if (scope.getScopeMenuButton() != null) {
            access(new Runnable() {
                @Override
                public void run() {
                    menu.removeComponent(scope.getScopeMenuButton());
                }
            });
            scope.setScopeMenuButton(null);
            notifierService.removeScopeButton(scope.getScopeView());
        }
    }

    private void sortButtonsInMenu(final String scopeAlias, final Button b) {
        final LinkedList<String> scopesNames = new LinkedList<>();
        for (Map.Entry<String, Scope> scopeEntry : scopes.entrySet()) {
            scopesNames.add(scopeEntry.getValue().getScopeAlias());
        }
        Collections.sort(scopesNames);
        if (scopesNames.contains(HOME_ALIAS)) {
            scopesNames.remove(HOME_ALIAS);
            scopesNames.addFirst(HOME_ALIAS);
        }

        final List<Button> buttonsToShift = new LinkedList<>();
        buttonsToShift.add(b);

        access(new Runnable() {
            @Override
            public void run() {
                for (String scope : scopesNames) {
                    if (HOME_ALIAS.equals(scope)) {
                        continue;
                    }

                    if (HOME_ALIAS.equals(scopeAlias) || scopeAlias.compareTo(scope) < 0) {
                        buttonsToShift.add(scopes.get(scope).getScopeMenuButton());
                        menu.removeComponent(scopes.get(scope).getScopeMenuButton());
                    }
                }

                for (Button button : buttonsToShift) {
                    menu.addComponent(button);
                }
            }
        });
    }

    private boolean isAllowedToShowScope(List<String> rolesAllowed) {
        return securityManager == null || securityManager.isUserInRoles(rolesAllowed);
    }

    @Override
    public void accessSynchronously(Runnable runnable) {
        Map<Class<?>, CurrentInstance> old = null;

        VaadinSession session = getSession();

        if (session == null) {
            throw new UIDetachedException();
        }

        // TODO PGWK-7 - hack to avoid exception when another session had lock
        //VaadinService.verifyNoOtherSessionLocked(session);

        session.lock();
        try {
            if (getSession() == null) {
                // UI was detached after fetching the session but before we
                // acquired the lock.
                throw new UIDetachedException();
            }
            old = CurrentInstance.setCurrent(this);
            runnable.run();
        } finally {
            session.unlock();
            if (old != null) {
                CurrentInstance.restoreInstances(old);
            }
        }
    }

    private class TimeOutThread extends Thread {

        private static final int TWO_SECONDS = 2000;

        @Override
        public void run() {
            try {
                sleep(TWO_SECONDS);
                if (progressIndicator.getValue() < 1) {
                    access(new Runnable() {
                        @Override
                        public void run() {
                            showMainContent();
                        }
                    });
                }
            } catch (InterruptedException e) {
                access(new Runnable() {
                    @Override
                    public void run() {
                        showMainContent();
                    }
                });
            }
        }
    }

    private void setNotificationsWindowPosition(Button.ClickEvent event) {
        notifications.setPositionX(event.getClientX() - event.getRelativeX());
        notifications.setPositionY(event.getClientY() - event.getRelativeY());
    }

    private class SidebarView extends VerticalLayout {

        public SidebarView() {
            addStyleName("sidebar");
            setWidth(null);
            setHeight(MAX_HEIGHT);

            // Branding element
            addComponent(new CssLayout() {
                {
                    addStyleName("branding");
                    Label logo = new Label(
                            formatTitle(consoleName),
                            ContentMode.HTML);
                    logo.setSizeUndefined();
                    addComponent(logo);
                }
            });

            // Main menu
            addComponent(menu);
            setExpandRatio(menu, 1);

            if (securityManager != null) {
                // User menu
                VerticalLayout menuView = new ScopeMenuView();
                addComponent(menuView);
            }
        }
    }

    private final class ScopeMenuView extends VerticalLayout {

        private ScopeMenuView() {
            setSizeUndefined();
            addStyleName("user");
            Image profilePic = new Image(
                    null,
                    new ThemeResource("img/profile-pic.png"));
            profilePic.setWidth("34px");
            addComponent(profilePic);
            Label userName = new Label(securityManager.getUserName());
            userName.setSizeUndefined();
            addComponent(userName);

            if (!ANONYMOUS_USER.equals(securityManager.getUserName())) {
                MenuBar.Command cmd = new MenuBar.Command() {
                    @Override
                    public void menuSelected(
                            MenuBar.MenuItem selectedItem) {
                        Notification
                                .show("Not implemented yet");
                    }
                };
                MenuBar settings = new MenuBar();
                MenuBar.MenuItem settingsMenu = settings.addItem("",
                        null);
                settingsMenu.setStyleName("icon-cog");
                settingsMenu.addItem("Settings", cmd);
                settingsMenu.addItem("Preferences", cmd);
                settingsMenu.addSeparator();
                settingsMenu.addItem("My Account", cmd);
                addComponent(settings);

                Button exit = new NativeButton("Exit");
                exit.addStyleName("icon-cancel");
                exit.setDescription("Sign Out");
                addComponent(exit);
                exit.addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        ((com.peergreen.webconsole.core.security.SecurityManager) securityManager).setUserLogged(false);
                        for (Map.Entry<ExtensionFactory, ScopeFactory> scopeFactoryEntry : scopesFactories.entrySet()) {
                            ScopeFactory scopeFactory = scopeFactoryEntry.getValue();
                            if (scopeFactory.getInstance() != null) {
                                scopeFactory.getInstance().stop();
                                scopeFactory.setInstance(null);
                            }
                        }
                        nbScopesToBind = 0;
                        progressIndicator.setValue((float) 0);
                        getSession().setAttribute("is.logged", false);
                        buildLoginView(true);
                    }
                });
            }
        }
    }

    private final class ConsoleContentView extends VerticalLayout {

        private ConsoleContentView(final Button notify, final HorizontalLayout tasksBar) {
            setSizeFull();
            HorizontalLayout toolbar = new HorizontalLayout();
            toolbar.setWidth(MAX_WIDTH);
            toolbar.setSpacing(true);
            toolbar.addStyleName("toolbar");
            toolbar.addComponent(tasksBar);
            toolbar.setComponentAlignment(tasksBar, Alignment.MIDDLE_LEFT);
            toolbar.setExpandRatio(tasksBar, 1);

            notify.setDescription("Notifications");
            notify.addStyleName("notifications");
            notify.addStyleName("icon-only");
            notify.addStyleName("icon-notification");
            notify.addStyleName("fontello");

            notify.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    event.getButton().removeStyleName("unread");
                    event.getButton().setDescription("Notifications");
                    if (notifications != null && notifications.getUI() != null) {
                        notifications.close();
                    } else {
                        setNotificationsWindowPosition(event);
                        getUI().addWindow(notifications);
                        notifications.focus();
                    }
                }
            });
            toolbar.addComponent(notify);
            toolbar.setComponentAlignment(notify, Alignment.MIDDLE_LEFT);

            addComponent(toolbar);
            addComponent(content);
            content.setSizeFull();
            content.addStyleName("view-content");
            setExpandRatio(content, 1.5f);
        }
    }

    private class NotificationWindow extends Window {

        public NotificationWindow() {
            setCaption("Notifications");
            setHeight("80%");
            setWidth("300px");
            addStyleName("notifications");
            setClosable(false);
            setResizable(false);
            setDraggable(false);
            setCloseShortcut(ShortcutAction.KeyCode.ESCAPE, null);
        }
    }
}