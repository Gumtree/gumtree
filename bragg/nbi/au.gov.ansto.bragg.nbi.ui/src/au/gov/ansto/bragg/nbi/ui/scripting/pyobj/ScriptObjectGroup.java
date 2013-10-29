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
public class ScriptObjectGroup extends PyObjectImp {

	private List<IPyObject> objectList = new ArrayList<IPyObject>();
	
	public ScriptObjectGroup(String name) {
		setName(name);
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
