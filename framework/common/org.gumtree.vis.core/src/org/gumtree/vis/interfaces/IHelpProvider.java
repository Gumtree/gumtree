/**
 * 
 */
package org.gumtree.vis.interfaces;

import java.util.Map;

import org.gumtree.vis.awt.HelpObject;

/**
 * @author nxi
 *
 */
public interface IHelpProvider {

	public Map<String, HelpObject> getHelpMap();
	
	public void add(String name, String key, String discription);
}
