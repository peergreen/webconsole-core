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

import com.vaadin.server.UIProvider;

import java.util.Dictionary;

/**
 * Vaadin UI provider factory
 *
 * @author Mohammed Boukada
 */
public interface UIProviderFactory {

    /**
     * Create an UI provider for a console
     *
     * @param properties properties
     * @return UI provider
     */
    UIProvider createUIProvider(Dictionary properties);

    /**
     * Stop Ui provider
     *
     * @param properties properties
     */
    void stopUIProvider(Dictionary properties);
}
