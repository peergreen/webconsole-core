package com.peergreen.webconsole.core.extension;

import com.peergreen.webconsole.UIContext;

/**
 * @author Mohammed Boukada
 */
public interface ExtensionFactory {
    InstanceHandler create(UIContext context);
}
