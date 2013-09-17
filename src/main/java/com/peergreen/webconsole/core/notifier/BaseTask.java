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

package com.peergreen.webconsole.core.notifier;

import com.peergreen.webconsole.notifier.Task;

/**
 * @author Mohammed Boukada
 */
public class BaseTask implements Task {

    private String message;
    private NotifierService notifierService;

    public BaseTask(String message, NotifierService notifierService) {
        this.message = message;
        this.notifierService = notifierService;
    }

    @Override
    public void stop() {
        notifierService.hideTask(this);
    }

    public String getMessage() {
        return message;
    }
}
