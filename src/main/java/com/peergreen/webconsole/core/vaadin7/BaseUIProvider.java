/**
 * Peergreen S.A.S. All rights reserved.
 * Proprietary and confidential.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.webconsole.core.vaadin7;

import static com.peergreen.webconsole.Constants.CONSOLE_ID;
import static com.peergreen.webconsole.Constants.EXTENSION_POINT;
import static com.peergreen.webconsole.Constants.REQUIRES_FILTER;
import static com.peergreen.webconsole.Constants.SCOPE_EXTENSION_POINT;
import static com.peergreen.webconsole.Constants.UI_ID;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import com.vaadin.server.ClientConnector;
import com.vaadin.server.SessionDestroyEvent;
import com.vaadin.server.SessionDestroyListener;
import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.ui.UI;

/**
 * Vaadin Base console UI provider
 *
 * @author Mohammed Boukada
 */
@Component
@Provides(specifications = UIProvider.class)
public class BaseUIProvider extends UIProvider {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(BaseUIProvider.class);

    private String consoleId;
    private String consoleName;
    private String consoleAlias;
    private String securityServicePid;
    private List<String> defaultRoles;
    private List<String> domains;

    private List<ComponentInstance> uis = new ArrayList<>();

    /**
     * Base console UI ipojo component factory
     */
    @Requires(from = "com.peergreen.webconsole.core.vaadin7.BaseUI")
    private Factory factory;

    private int i = 0;

    /**
     * Set console Id
     * @param consoleId console Id
     */
    public void setConsoleId(String consoleId) {
        this.consoleId = consoleId;
    }

    /**
     * Set console name
     *
     * @param consoleName console name
     */
    public void setConsoleName(String consoleName) {
        this.consoleName = consoleName;
    }

    /**
     * Set console alias
     * @param consoleAlias console alias
     */
    public void setConsoleAlias(String consoleAlias) {
        this.consoleAlias = consoleAlias.substring(1);
    }

    /**
     * Enable security
     * @param securityServicePid security service pid
     */
    public void setSecurityServicePid(String securityServicePid) {
        this.securityServicePid = securityServicePid;
    }

    /**
     * Default roles for default user (development mode)
     * @param defaultRoles default roles
     */
    public void setDefaultRoles(List<String> defaultRoles) {
        this.defaultRoles = defaultRoles;
    }

    /**
     * Set console domains
     * @param domains console domains
     */
    public void setDomains(List<String> domains) {
        this.domains = domains;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends UI> getUIClass(UIClassSelectionEvent event) {
        return BaseUI.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UI createInstance(final UICreateEvent e) {
        BaseUI ui = null;
        try {
            // Create an instance of baseUI
            String uiId = consoleAlias + "-" + i;
            ui = new BaseUI(SCOPE_EXTENSION_POINT, uiId, securityServicePid != null);
            ui.setConsoleId(consoleId);
            ui.setConsoleName(consoleName);
            ui.setDefaultRoles(defaultRoles);
            ui.setDomains(domains);
            // Configuration properties for ipojo component
            Dictionary<String, Object> props = new Hashtable<>();
            props.put("instance.object", ui);
            Dictionary<String, Object> bindFilters = new Hashtable<>();
            bindFilters.put("ScopeView", String.format("(&(%s=%s)(%s=%s))", UI_ID, uiId, EXTENSION_POINT, SCOPE_EXTENSION_POINT));
            bindFilters.put("NotifierService", String.format("(%s=%s)", CONSOLE_ID, consoleId));
            if (securityServicePid != null) {
                bindFilters.put("AuthenticateService", String.format("(%s=%s)", "factory.name", securityServicePid));
            }
            props.put(REQUIRES_FILTER, bindFilters);

            // Create ipojo component from its factory
            final ComponentInstance instance = factory.createComponentInstance(props);
            ui.addDetachListener(new ClientConnector.DetachListener() {
                @Override
                public void detach(ClientConnector.DetachEvent event) {
                    stop(instance);
                }
            });
            e.getService().addSessionDestroyListener(new SessionDestroyListener() {
                @Override
                public void sessionDestroy(SessionDestroyEvent event) {
                    stop(instance);
                }
            });
            uis.add(instance);
            i++;
        } catch (UnacceptableConfiguration | MissingHandlerException | ConfigurationException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        return ui;
    }

    private void stop(ComponentInstance instance) {
        if (uis.contains(instance)) {
            instance.stop();
            instance.dispose();
            uis.remove(instance);
        }
    }

    @Invalidate
    public void stop() {
        for (ComponentInstance instance : uis) {
            instance.stop();
            instance.dispose();
        }
        uis.clear();
    }
}
