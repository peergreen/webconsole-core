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

package com.peergreen.webconsole.core.scope;


import com.peergreen.webconsole.core.extension.InstanceHandle;

import java.util.List;

/**
 * Scope factory descriptor
 *
 * @author Mohammed Boukada
 */
public class ScopeFactory {

    /**
     * Roles allowed
     */
    private List<String> roles;

    /**
     * Scope instance handle
     */
    private InstanceHandle instance;

    /**
     * Create scope factory descriptor
     *
     * @param roles roles allowed
     */
    public ScopeFactory(List<String> roles) {
        this.roles = roles;
    }

    /**
     * Get allowed roles
     *
     * @return allowed roles
     */
    public List<String> getRoles() {
        return roles;
    }

    /**
     * Get scope instance handle
     *
     * @return scope instance handle
     */
    public InstanceHandle getInstance() {
        return instance;
    }

    /**
     * Set scope instance handle
     *
     * @param instance scope instance handle
     */
    public void setInstance(InstanceHandle instance) {
        this.instance = instance;
    }
}
