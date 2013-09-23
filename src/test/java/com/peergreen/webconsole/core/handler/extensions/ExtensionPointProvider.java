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

package com.peergreen.webconsole.core.handler.extensions;

import org.osgi.framework.BundleContext;

import com.peergreen.webconsole.Extension;
import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.Inject;
import com.peergreen.webconsole.Link;
import com.peergreen.webconsole.UIContext;
import com.peergreen.webconsole.Unlink;
import com.peergreen.webconsole.notifier.INotifierService;
import com.peergreen.webconsole.security.ISecurityManager;
import com.vaadin.ui.Button;

/**
 * @author Mohammed Boukada
 */
@Extension
@ExtensionPoint("my.extension")
public class ExtensionPointProvider extends Button implements TestInterface {

    @Inject
    private ISecurityManager securityManager;

    @Inject
    private BundleContext bundleContext;

    @Inject
    private UIContext uiContext;

    @Inject
    private INotifierService notifierService;

    public ISecurityManager getSecurityManager() {
        return securityManager;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public UIContext getUiContext() {
        return uiContext;
    }

    public INotifierService getNotifierService() {
        return notifierService;
    }

    @Link("TestInterface")
    public TestInterface addElement(TestInterface testInterface) {
        return testInterface;
    }

    @Unlink("TestInterface")
    public TestInterface removeElement(TestInterface testInterface) {
        return testInterface;
    }

    @Link("Button")
    public Button addButton(Button button) {
        return button;
    }

    @Unlink("Button")
    public Button removeButton(Button button) {
        return button;
    }
}
