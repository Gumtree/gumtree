/**
 * 
 */
package au.gov.ansto.bragg.nbi.ui.scripting.pyobj;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nxi
 *
 */
public class ScriptObjectGroup implements IPyObject {

	private List<IPyObject> objectList = new ArrayList<IPyObject>();
	private String name;
	
	public ScriptObjectGroup(String name) {
		this.name = name;
	}
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.wombat.ui.script.pyobj.IPyObject#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.wombat.ui.script.pyobj.IPyObject#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the objectList
	 */
	public List<IPyObject> getObjectList() {
		return objectList;
	}

	public void addObject(IPyObject object) {
		objectList.add(object);
	}

	public void removeObject(IPyObject object) {
		objectList.remove(object);
	}
	
	public IPyObject getObject(String objName) {
		for (IPyObject obj : objectList) {
			if (obj.getName().equals(objName)) {
				return obj;
			}
		}
		return null;
	}
}
