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

import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.metadata.Element;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.peergreen.webconsole.Constants;
import com.peergreen.webconsole.UIContext;
import com.peergreen.webconsole.core.handler.extensions.ExtensionExample;
import com.peergreen.webconsole.core.handler.extensions.ExtensionExtendsProvider;
import com.peergreen.webconsole.core.handler.extensions.ExtensionPointProvider;
import com.peergreen.webconsole.core.handler.extensions.TestInterface;
import com.peergreen.webconsole.security.ISecurityManager;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;

/**
 * @author Mohammed Boukada
 */
public class ExtensionHandlerTestCase {

    @Mock
    UIContext uiContext;
    @Mock
    ISecurityManager securityManager;
    @Mock
    BundleContext bundleContext;
    @Mock
    ExtensionHandler.InstanceManager instanceManager;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testBindInjections() throws ClassNotFoundException, NoSuchFieldException, ConfigurationException {
        ExtensionPointProvider extensionPointProvider = new ExtensionPointProvider();
        when(uiContext.getSecurityManager()).thenReturn(securityManager);
        when(instanceManager.getPojoObject()).thenReturn(extensionPointProvider);
        when(instanceManager.getBundleContext()).thenReturn(bundleContext);

        ExtensionHandler extensionHandler = new ExtensionHandler();
        extensionHandler.setOwnInstanceManager(instanceManager);
        extensionHandler.setUiContext(uiContext);
        extensionHandler.setExtensionType();

        List<Field> fieldsToBind = extensionHandler.getFieldsToBind(extensionPointProvider.getClass());
        Assert.assertTrue(fieldsToBind.contains(extensionPointProvider.getClass().getDeclaredField("notifierService")), "Notifier Service must be added to fields to Bind   ");
        Assert.assertEquals(extensionPointProvider.getBundleContext(), bundleContext, "Wrong bundle context injected");
        Assert.assertEquals(extensionPointProvider.getSecurityManager(), securityManager, "Wrong security manager injected");
        Assert.assertEquals(extensionPointProvider.getUiContext(), uiContext, "Wrong UI Context injected");
    }

    @Test
    public void testBindInjectionsInSuperClass() throws ConfigurationException, NoSuchFieldException {
        ExtensionExtendsProvider extensionExtendsProvider = new ExtensionExtendsProvider();
        when(uiContext.getSecurityManager()).thenReturn(securityManager);
        when(instanceManager.getPojoObject()).thenReturn(extensionExtendsProvider);
        when(instanceManager.getBundleContext()).thenReturn(bundleContext);

        ExtensionHandler extensionHandler = new ExtensionHandler();
        extensionHandler.setOwnInstanceManager(instanceManager);
        extensionHandler.setUiContext(uiContext);
        extensionHandler.setExtensionType();

        List<Field> fieldsToBind = extensionHandler.getFieldsToBind(extensionExtendsProvider.getClass());
        Assert.assertTrue(fieldsToBind.contains(extensionExtendsProvider.getClass().getSuperclass().getDeclaredField("notifierService")), "Notifier Service must be added to fields to Bind   ");
        Assert.assertEquals(extensionExtendsProvider.getBundleContext(), bundleContext, "Wrong bundle context injected");
        Assert.assertEquals(extensionExtendsProvider.getSecurityManager(), securityManager, "Wrong security manager injected");
        Assert.assertEquals(extensionExtendsProvider.getUiContext(), uiContext, "Wrong UI Context injected");
    }

    @Test
    public void testGetSpecifications() {
        ExtensionPointProvider extensionPointProvider = new ExtensionPointProvider();

        when(instanceManager.getPojoObject()).thenReturn(extensionPointProvider);

        ExtensionHandler extensionHandler = new ExtensionHandler();
        extensionHandler.setOwnInstanceManager(instanceManager);
        extensionHandler.setExtensionType();

        List<String> specifications = extensionHandler.getSpecifications(extensionPointProvider.getClass());
        Assert.assertTrue(specifications.contains(ExtensionPointProvider.class.getName()), "ExtensionPointProvider class missing");
        Assert.assertTrue(specifications.contains(Button.class.getName()), "Button class missing");
        Assert.assertTrue(specifications.contains(Component.class.getName()), "Component class missing");
        Assert.assertTrue(specifications.contains(TestInterface.class.getName()), "Test Interface class missing");
    }

    @Test
    public void testCreateBindings() throws ConfigurationException {
        ExtensionPointProvider extensionPointProvider = new ExtensionPointProvider();
        String uiId = "id-1";

        when(uiContext.getSecurityManager()).thenReturn(securityManager);
        when(instanceManager.getPojoObject()).thenReturn(extensionPointProvider);
        when(instanceManager.getBundleContext()).thenReturn(bundleContext);
        when(uiContext.getUIId()).thenReturn(uiId);

        ExtensionHandler extensionHandler = new ExtensionHandler();
        extensionHandler.setOwnInstanceManager(instanceManager);
        extensionHandler.setExtensionType();
        extensionHandler.setUiContext(uiContext);

        Element oldMetadata = new Element("component", null);
        List<Field> fieldsToBind = extensionHandler.getFieldsToBind(extensionPointProvider.getClass());
        Element newMetadata = extensionHandler.createBindings(oldMetadata, fieldsToBind, new ArrayList<Method>());

        Assert.assertTrue(newMetadata.containsElement("requires"), "Requires missing");
        Assert.assertTrue(newMetadata.getElements("requires").length == 3, "Requires must contains 3 requirement");

        boolean fieldRequirementPresent = false;
        for (Element element : newMetadata.getElements("requires")) {
            if (element.containsAttribute("field") && element.getAttribute("field").equals("notifierService")) {
                fieldRequirementPresent = true;
            } else if (element.containsAttribute("filter")) {
                Assert.assertTrue(element.containsAttribute("aggregate"), "aggregate attribute missing");
                Assert.assertEquals(element.getAttribute("aggregate"), "true", "aggregate attribute must be true");

                Assert.assertTrue(element.containsAttribute("optional"), "optional attribute missing");
                Assert.assertEquals(element.getAttribute("optional"), "true", "optional attribute must be true");

                Assert.assertTrue(element.containsElement("callback"), "callback element missing");

                String filterTestInterface = "(&(" + Constants.UI_ID + "=" + uiId + ")(" + Constants.EXTENSION_POINT
                        + "=" + extensionPointProvider.getClass().getName() + ".TestInterface))";
                String filterButton = "(&(" + Constants.UI_ID + "=" + uiId + ")(" + Constants.EXTENSION_POINT
                        + "=" + extensionPointProvider.getClass().getName() + ".Button))";

                if (element.getAttribute("filter").equals(filterTestInterface)) {
                    Assert.assertTrue(element.getElements("callback").length == 2, "callback methods are 2 elements");
                    Assert.assertTrue(element.containsAttribute("id"), "id attribute missing");
                    Assert.assertEquals(element.getAttribute("id"), extensionPointProvider.getClass().getName() + ".TestInterface", "wrong id attribute value");
                    for (Element callback : element.getElements("callback")) {
                        if (callback.containsAttribute("type") && callback.getAttribute("type").equals("bind")) {
                            Assert.assertTrue(callback.getAttribute("method").equals("addElement"), "Wrong TestInterface bind method name");
                        } else if (callback.containsAttribute("type") && callback.getAttribute("type").equals("unbind")) {
                            Assert.assertTrue(callback.getAttribute("method").equals("removeElement"), "Wrong TestInterface unbind method name");
                        }
                    }
                } else if (element.getAttribute("filter").equals(filterButton)) {
                    Assert.assertTrue(element.getElements("callback").length == 2, "callback methods are 2 elements");
                    Assert.assertTrue(element.containsAttribute("id"), "id attribute missing");
                    Assert.assertEquals(element.getAttribute("id"), extensionPointProvider.getClass().getName() + ".Button", "wrong id attribute value");
                    for (Element callback : element.getElements("callback")) {
                        if (callback.containsAttribute("type") && callback.getAttribute("type").equals("bind")) {
                            Assert.assertTrue(callback.getAttribute("method").equals("addButton"), "Wrong TestInterface bind method name");
                        } else if (callback.containsAttribute("type") && callback.getAttribute("type").equals("unbind")) {
                            Assert.assertTrue(callback.getAttribute("method").equals("removeButton"), "Wrong TestInterface unbind method name");
                        }
                    }
                }
            }
        }
        Assert.assertTrue(fieldRequirementPresent, "Field requirement missing");
    }

    @Test
    public void testSetExtensionProperties() throws ConfigurationException {
        ExtensionExample extensionExample = new ExtensionExample();

        when(instanceManager.getPojoObject()).thenReturn(extensionExample);

        ExtensionHandler extensionHandler = new ExtensionHandler();
        extensionHandler.setOwnInstanceManager(instanceManager);
        extensionHandler.setExtensionType();
        Dictionary properties = extensionHandler.setExtensionProperties(new Hashtable());

        Assert.assertTrue(properties.size() == 6);
        Assert.assertEquals(properties.get("test.attr1"), "My Awesome Extension", "Wrong attr1 attribute in Qualifier annotation");
        Assert.assertEquals(properties.get("test.attr2"), "", "Wrong attr2 attribute in Qualifier annotation");
        Assert.assertEquals(properties.get("test.attr3"), "Really an awesome extension", "Wrong attr3 attribute in Qualifier annotation");
        Assert.assertEquals(properties.get("scope.name"), "testScopeAnnotationWithoutIconPath", "Wrong name attribute in Scope annotation");
        Assert.assertEquals(properties.get("scope.domains"), new String[]{"test"}, "Wrong domains attribute in Scope annotation");
        Assert.assertEquals(properties.get("scope.iconClass"), "", "Wrong icon path attribute in Scope annotation");
    }
}
