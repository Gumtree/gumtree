/**
 * 
 */
package org.gumtree.vis.awt;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gumtree.vis.interfaces.IHelpProvider;

/**
 * @author nxi
 *
 */
public class AbstractHelpProvider implements IHelpProvider {

	private Map<String, HelpObject> helpMap = new LinkedHashMap<String, HelpObject>();
	
	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IHelpProvider#getHelpMap()
	 */
	@Override
	public Map<String, HelpObject> getHelpMap() {
		// TODO Auto-generated method stub
		return helpMap;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IHelpProvider#add(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void add(String name, String key, String discription) {
		HelpObject help = new HelpObject(name, key, discription);
		helpMap.put(help.getName(), help);
	}

}
