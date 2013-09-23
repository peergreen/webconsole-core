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

package com.peergreen.webconsole.core.security;

import javax.security.auth.Subject;
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.peergreen.webconsole.security.ISecurityManager;

/**
 * Web Console security manager implementation
 *
 * @author Mohammed Boukada
 */
public class SecurityManager implements ISecurityManager {

    private String principalName;

    private List<String> principalRoles = new ArrayList<>();

    private boolean userLogged = false;

    public SecurityManager(Subject subject) {
        for (Principal principal : subject.getPrincipals()) {
            if (!(principal instanceof Group)) {
                principalName = principal.getName();
            } else {
                Enumeration e = ((Group) principal).members();
                while (e.hasMoreElements()) {
                    principalRoles.add(((Principal) e.nextElement()).getName());
                }
            }
        }
        userLogged = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUserInRole(String role) {
        return "all".equals(role.toLowerCase()) || principalRoles.contains(role);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUserInRoles(List<String> roles) {
        for (String role : roles) {
            if ("all".equals(role.toLowerCase()) || principalRoles.contains(role)) {
                return true;
            }
        }
        return roles.size() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserName() {
        return principalName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUserLogged() {
        return userLogged;
    }

    public void setUserLogged(boolean userLogged) {
        this.userLogged = userLogged;
    }
}
