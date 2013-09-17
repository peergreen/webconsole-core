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

/**
 * Notification descriptor
 *
 * @author Mohammed Boukada
 */
public class Notification {

    /**
     * Notification message
     */
    private String message;

    /**
     * Notification date
     */
    private Long date;

    /**
     * Create a notification descriptor
     *
     * @param message notification message
     * @param date    notification date
     */
    public Notification(String message, Long date) {
        this.message = message;
        this.date = date;
    }

    /**
     * Get notification message
     *
     * @return notification message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get notification date
     *
     * @return notification date
     */
    public Long getDate() {
        return date;
    }
}
