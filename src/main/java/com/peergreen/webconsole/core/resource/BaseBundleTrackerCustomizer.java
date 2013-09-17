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

package com.peergreen.webconsole.core.resource;

import com.peergreen.webconsole.notifier.INotifierService;
import com.peergreen.webconsole.resource.CssHandle;
import com.peergreen.webconsole.resource.CssInjectorService;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Bundle tracker for css contribution
 *
 * @author Mohammed Boukada
 */
public class BaseBundleTrackerCustomizer implements BundleTrackerCustomizer<List<CssHandle>> {

    private CssInjectorService cssInjectorService;
    private INotifierService notifierService;

    public BaseBundleTrackerCustomizer(CssInjectorService cssInjectorService, INotifierService notifierService) {
        this.cssInjectorService = cssInjectorService;
        this.notifierService = notifierService;
    }

    /**
     * {@inheritDoc} <br />
     * <p/>
     * If bundle contains css/ folder, adds all *.css file as a css contribution
     */
    @Override
    public List<CssHandle> addingBundle(Bundle bundle, BundleEvent bundleEvent) {
        BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
        Collection<String> cssFiles = bundleWiring.listResources("/css", "*.css", BundleWiring.LISTRESOURCES_RECURSE);
        List<CssHandle> cssHandles = new ArrayList<>();
        if (cssFiles.size() > 0) {
            for (String cssFile : cssFiles) {
                try {
                    cssHandles.add(cssInjectorService.inject(bundle.getResource(cssFile).openStream()));
                } catch (IOException e) {
                    notifierService.addNotification(String.format("Cannot add css file '%s' from '%s'", cssFile, bundle.getSymbolicName()));
                }
            }
        }
        return cssHandles;
    }

    @Override
    public void modifiedBundle(Bundle bundle, BundleEvent bundleEvent, List<CssHandle> cssHandles) {
        // do nothing
    }

    /**
     * {@inheritDoc} <br />
     * <p/>
     * Remove css contribution when bundle is stopping.
     */
    @Override
    public void removedBundle(Bundle bundle, BundleEvent bundleEvent, List<CssHandle> cssHandles) {
        for (CssHandle cssHandle : cssHandles) {
            cssHandle.remove();
        }
    }
}
