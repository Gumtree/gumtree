/**
 * 
 */
package org.gumtree.data.nexus.ui.io;

import java.io.File;
import java.net.URI;

import org.gumtree.vis.interfaces.IExporter;


/**
 * @author nxi
 *
 */
public abstract class AbstractExporter implements IExporter {
	
	
	protected File getFile(URI uri, String extensionName){
		if (uri == null || uri.getPath().isEmpty())
			return null;
		String filename = uri.getPath();
		if (!filename.endsWith(extensionName))
			if (extensionName.startsWith("."))
				filename = filename + extensionName;
			else 
				filename = filename + "." + extensionName;
		return new File(filename);
	}
	

}
