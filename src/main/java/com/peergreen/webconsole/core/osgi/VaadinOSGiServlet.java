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

package com.peergreen.webconsole.core.osgi;

import javax.servlet.annotation.WebServlet;

import com.peergreen.webconsole.core.vaadin7.BaseUI;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.UIProvider;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletService;

/**
 * Vaadin OSGi Servlet
 * to add Vaadin UI provider dynamically
 *
 * @author Mohammed Boukada
 */
@WebServlet(asyncSupported = true)
@VaadinServletConfiguration(ui = BaseUI.class, productionMode = true)
public class VaadinOSGiServlet extends VaadinServlet {

    private static final long serialVersionUID = 1L;

    /**
     * Vaadin UI provider
     */
    private UIProvider provider;

    /**
     * Vaadin OSGi Servlet constructor
     *
     * @param provider
     */
    public VaadinOSGiServlet(UIProvider provider) {
        this.provider = provider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected VaadinServletService createServletService(DeploymentConfiguration deploymentConfiguration) throws ServiceException {

        final VaadinServletService service = super.createServletService(deploymentConfiguration);
        service.addSessionInitListener(new BaseSessionInitListener(provider));

        return service;
    }
}
