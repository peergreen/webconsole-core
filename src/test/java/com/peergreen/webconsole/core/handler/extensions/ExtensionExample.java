package com.peergreen.webconsole.core.handler.extensions;

import com.peergreen.webconsole.Extension;
import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.Scope;
import com.vaadin.ui.Button;

import javax.annotation.security.RolesAllowed;

/**
 * @author Mohammed Boukada
 */
@Extension
@ExtensionPoint("com.peergreen.webconsole.core.handler.extensions.ExtensionPointProvider.Button")
@RolesAllowed({"admin", "peergreen"})
@TestQualifier(attr1 = "My Awesome Extension", attr3 = "Really an awesome extension")
@Scope("testScopeAnnotationWithoutIconPath")
public class ExtensionExample extends Button {
}
