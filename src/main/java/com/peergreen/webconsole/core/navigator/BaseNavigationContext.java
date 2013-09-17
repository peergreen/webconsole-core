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

package com.peergreen.webconsole.core.navigator;

import com.peergreen.webconsole.navigator.NavigationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Navigation context implementation
 *
 * @author Mohammed Boukada
 */
public class BaseNavigationContext implements NavigationContext {

    private String path;
    private Map<String, Object> properties;

    public BaseNavigationContext(String path) {
        this.path = path;
        this.properties = new HashMap<>();
    }

    public BaseNavigationContext(String path, Map<String, Object> properties) {
        this.path = path;
        this.properties = properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath() {
        return path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }
}
