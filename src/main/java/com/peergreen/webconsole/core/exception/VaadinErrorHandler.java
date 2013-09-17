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

package com.peergreen.webconsole.core.exception;

import com.vaadin.server.ErrorEvent;
import com.vaadin.server.ErrorHandler;

import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * @author Mohammed Boukada
 */
public class VaadinErrorHandler implements ErrorHandler {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(VaadinErrorHandler.class);

    @Override
    public void error(ErrorEvent event) {
        LOGGER.error(event.getThrowable().getMessage(), event.getThrowable());
    }
}
