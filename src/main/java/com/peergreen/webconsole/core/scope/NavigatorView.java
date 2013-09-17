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

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;

/**
 * Build a scope view for Navigator
 *
 * @author Mohammed Boukada
 */
public class NavigatorView extends CssLayout implements View {

    public NavigatorView(Component component) {
        setSizeFull();
        addComponent(component);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

    }
}
