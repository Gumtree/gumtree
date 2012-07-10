// See: http://eclipse-shell.sourceforge.net/scripts.html
//
// Get the GumTree workbench service registry
import java.util.*;

public Object getWBSRegistry() {
 reg = org.eclipse.core.runtime.Platform.getExtensionRegistry();
 point = reg.getExtensionPoint("org.eclipse.eclipsemonkey.dom");
 extensions = point.getExtensions();
 for (int i = 0; i<extensions.length; i++){
  print(i);
  extension = extensions[i];
  configurations = extension.getConfigurationElements();
  for (int c = 0; c<configurations.length; c++){
    element = configurations[c];
    varName = element.getAttribute("variableName");
    if(varName.equals("workbenchServiceRegistry")) {
	    dom = element.createExecutableExtension("class");
    	return domRoot = dom.getDOMroot();
    	}
  }
 } 
 return null;
}

// Launch the first service from the registry
getWBSRegistry().getDescriptors()[0].getService().launch()