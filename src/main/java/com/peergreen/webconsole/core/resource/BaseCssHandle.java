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

import com.peergreen.webconsole.resource.CssHandle;

/**
 * Css contribution handle implementation
 *
 * @author Mohammed Boukada
 */
public class BaseCssHandle implements CssHandle {
    private String cssContent;
    private BaseCssInjectorService cssInjectorService;

    /**
     * Create new css contribution handle
     *
     * @param cssContent         css content
     * @param cssInjectorService css contribution service
     */
    public BaseCssHandle(String cssContent, BaseCssInjectorService cssInjectorService) {
        this.cssContent = cssContent;
        this.cssInjectorService = cssInjectorService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String get() {
        return cssContent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(String cssContent) {
        this.cssContent = cssContent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove() {
        cssInjectorService.remove(this);
    }
}
