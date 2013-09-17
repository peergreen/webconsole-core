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

package com.peergreen.webconsole.core.handler;

import java.lang.reflect.Method;

/**
 * Defines an extension point
 *
 * @author Mohammed Boukada
 */
public class ExtensionPointModel {
    private Method bindMethod;
    private Method unbindMethod;
    private String filter;

    /**
     * Create an extension point model
     *
     * @param bindMethod   callback bind method
     * @param unbindMethod callback unbind method
     * @param filter       binding filter
     */
    public ExtensionPointModel(Method bindMethod, Method unbindMethod, String filter) {
        this.bindMethod = bindMethod;
        this.unbindMethod = unbindMethod;
        this.filter = filter;
    }

    /**
     * Get bind method to callback
     *
     * @return bind method
     */
    public Method getBindMethod() {
        return bindMethod;
    }

    /**
     * Get unbind method to callback
     *
     * @return unbind method
     */
    public Method getUnbindMethod() {
        return unbindMethod;
    }

    /**
     * Get bindings filter
     *
     * @return filter
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Set unbind method to callback
     *
     * @param unbindMethod unbind method
     */
    public void setUnbindMethod(Method unbindMethod) {
        this.unbindMethod = unbindMethod;
    }

    /**
     * Set bind method to callback
     *
     * @param bindMethod bind method
     */
    public void setBindMethod(Method bindMethod) {
        this.bindMethod = bindMethod;
    }
}
