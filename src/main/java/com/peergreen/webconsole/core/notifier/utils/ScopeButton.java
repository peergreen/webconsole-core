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

package com.peergreen.webconsole.core.notifier.utils;

import com.vaadin.ui.Button;
import com.vaadin.ui.UI;

/**
 * Scope button descriptor
 *
 * @author Mohammed Boukada
 */
public class ScopeButton {
    /**
     * Scope button
     */
    private Button button;

    /**
     * Scope button UI
     */
    private UI buttonUi;

    /**
     * Scope badge
     */
    private int badge;

    /**
     * Create a scope button descriptor
     *
     * @param button   vaadin button
     * @param buttonUi vaadin UI (where button is attached)
     * @param badge    badge value
     */
    public ScopeButton(Button button, UI buttonUi, int badge) {
        this.button = button;
        this.buttonUi = buttonUi;
        this.badge = badge;
    }

    /**
     * Get scope button
     *
     * @return scope button
     */
    public Button getButton() {
        return button;
    }

    /**
     * Get scope button UI
     *
     * @return UI
     */
    public UI getButtonUi() {
        return buttonUi;
    }

    /**
     * Get scope badge
     *
     * @return badge value
     */
    public int getBadge() {
        return badge;
    }

    /**
     * Set scope badge
     *
     * @param badge new badge value
     */
    public void setBadge(int badge) {
        this.badge = badge;
    }
}
