package com.peergreen.webconsole.core.security;

import com.peergreen.webconsole.ISecurityManager;

import javax.security.auth.Subject;
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * @author Mohammed Boukada
 */
public class SecurityManager implements ISecurityManager {

    private String principalName;

    private List<String> principalRoles;

    private boolean userLogged = false;

    public SecurityManager(Subject subject) {
        Iterator iterator = subject.getPrincipals().iterator();
        while (iterator.hasNext()) {
            Object object = iterator.next();
            if (!(object instanceof Group)) {
                principalName = ((Principal) object).getName();
            } else {
                Enumeration e = ((Group) object).members();
                principalRoles = new ArrayList<>();
                while (e.hasMoreElements()) {
                    principalRoles.add(((Principal) e.nextElement()).getName());
                }
            }
        }
        userLogged = true;
    }

    @Override
    public boolean isUserInRole(String role) {
        return "all".equals(role.toLowerCase()) || principalRoles.contains(role);
    }

    @Override
    public boolean isUserInRoles(String[] roles) {
        for (String role : roles) {
            if ("all".equals(role.toLowerCase()) || principalRoles.contains(role)) {
                return true;
            }
        }
        return roles.length == 0;
    }

    @Override
    public String getUserName() {
        return principalName;
    }

    @Override
    public boolean isUserLogged() {
        return userLogged;
    }

    public void setUserLogged(boolean userLogged) {
        this.userLogged = userLogged;
    }
}
