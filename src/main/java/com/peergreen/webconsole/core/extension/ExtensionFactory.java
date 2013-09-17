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

package com.peergreen.webconsole.core.extension;

import com.peergreen.webconsole.UIContext;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;

/**
 * Extension factory
 *
 * @author Mohammed Boukada
 */
public interface ExtensionFactory {
    /**
     * Create an instance of the extension
     *
     * @param context UI context
     * @return extension instance handle
     * @throws MissingHandlerException
     * @throws UnacceptableConfiguration
     * @throws ConfigurationException
     */
    InstanceHandle create(UIContext context) throws MissingHandlerException, UnacceptableConfiguration, ConfigurationException;
}
