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

import java.util.Arrays;
import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import com.peergreen.webconsole.Constants;
import com.vaadin.server.UIProvider;

/**
 * Vaadin UI provider factory
 *
 * @author Mohammed Boukada
 */
@Component
@Instantiate
@Provides
public class BaseUIProviderFactory implements UIProviderFactory {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(BaseUIProviderFactory.class);

    /**
     * Base console UI provider ipojo component factory
     */
    @Requires(from = "com.peergreen.webconsole.core.vaadin7.BaseUIProvider")
    private Factory factory;

    private Map<String, ComponentInstance> providers = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public UIProvider createUIProvider(Dictionary properties) {
        BaseUIProvider provider = null;
        String consoleId = (String) properties.get("instance.name");
        String consoleName = (String) properties.get(Constants.CONSOLE_NAME);
        String consoleAlias = (String) properties.get(Constants.CONSOLE_ALIAS);
        Boolean enableSecurity = (Boolean) properties.get(Constants.ENABLE_SECURITY);
        String[] defaultRoles = (String[]) properties.get(Constants.DEFAULT_ROLES);
        if (defaultRoles == null) {
            defaultRoles = new String[0];
        }
        String[] consoleDomains = (String[]) properties.get(Constants.CONSOLE_DOMAINS);

        try {
            // Create an instance of base console UI provider
            provider = new BaseUIProvider();
            provider.setConsoleId(consoleId);
            provider.setConsoleName(consoleName);
            provider.setConsoleAlias(consoleAlias);
            provider.setEnableSecurity(enableSecurity);
            provider.setDefaultRoles(Arrays.asList(defaultRoles));
            provider.setDomains(Arrays.asList(consoleDomains));

            // Configuration properties for ipojo component
            Properties props = new Properties();
            // Use the instance of baseUI an pojo instance for ipojo component
            props.put("instance.object", provider);

            // Create ipojo component from its factory
            String instanceName = (String) properties.get("instance.name");
            providers.put(instanceName, factory.createComponentInstance(props));
        } catch (UnacceptableConfiguration | MissingHandlerException | ConfigurationException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return provider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopUIProvider(Dictionary properties) {
        String instanceName = (String) properties.get("instance.name");
        providers.get(instanceName).stop();
        providers.get(instanceName).dispose();
        providers.remove(instanceName);
    }
}
