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

/**
 * Extension Instance handle
 *
 * @author Mohammed Boukada
 */
public interface InstanceHandle {
    /**
     * Get instance name
     *
     * @return instance name
     */
    String getInstanceName();

    /**
     * Stop instance
     */
    void stop();

    /**
     * Get instance state
     *
     * @return instance state
     */
    InstanceState getState();
}
