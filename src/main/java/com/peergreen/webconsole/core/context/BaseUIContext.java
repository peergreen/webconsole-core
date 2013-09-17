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

package com.peergreen.webconsole.core.context;

import com.peergreen.webconsole.ISecurityManager;
import com.peergreen.webconsole.UIContext;
import com.peergreen.webconsole.navigator.ViewNavigator;
import com.vaadin.ui.UI;

/**
 * @author Mohammed Boukada
 */
public class BaseUIContext implements UIContext {

    private UI ui;
    private ViewNavigator navigator;
    private ISecurityManager securityManager;
    private String uiId;

    public BaseUIContext(UI ui, ViewNavigator navigator, ISecurityManager securityManager, String uiId) {
        this.ui = ui;
        this.navigator = navigator;
        this.securityManager = securityManager;
        this.uiId = uiId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ISecurityManager getSecurityManager() {
        return securityManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UI getUI() {
        return ui;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ViewNavigator getViewNavigator() {
        return navigator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUIId() {
        return uiId;
    }
}
