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

import com.peergreen.webconsole.Extension;
import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.Scope;
import com.vaadin.ui.Button;

import javax.annotation.security.RolesAllowed;

/**
 * @author Mohammed Boukada
 */
@Extension
@ExtensionPoint("com.peergreen.webconsole.core.handler.extensions.ExtensionPointProvider.Button")
@RolesAllowed({"admin", "peergreen"})
@TestQualifier(attr1 = "My Awesome Extension", attr3 = "Really an awesome extension")
@Scope("testScopeAnnotationWithoutIconPath")
public class ExtensionExample extends Button {
}
