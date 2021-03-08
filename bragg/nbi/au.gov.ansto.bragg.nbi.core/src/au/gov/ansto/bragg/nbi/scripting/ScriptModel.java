/**
 * 
 */
package au.gov.ansto.bragg.nbi.scripting;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import au.gov.ansto.bragg.nbi.scripting.IPyObject.PyObjectType;


/**
 * @author nxi
 *
 */
public class ScriptModel {

//	private final static Map<Integer, ScriptModel> modelRegistry = new HashMap<Integer, ScriptModel>();
	private List<IPyObject> controls = new ArrayList<IPyObject>();
	private List<IModelChangeListener> listeners = new ArrayList<IModelChangeListener>();
	private int id;
	private String title;
	private String version;
	private int numColumns = 1;
	private boolean isEqualWidth = false;
	private boolean isDirty = false;
	private long lastModifiedTimestamp;
	
//	public static ScriptModel getModel(int id) {
//		return modelRegistry.get(id);
//	}
	
//	public static void registerModel(int id, ScriptModel model) {
//		modelRegistry.put(id, model);
//	}

	public ScriptModel(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	public void addControl(IPyObject control) {
		this.controls.add(control);
	}

	public void removeControl(IPyObject control) {
		this.controls.remove(control);
	}

	public void moveObject1BeforeObject2(IPyObject obj1, IPyObject obj2) {
		int idx1 = controls.indexOf(obj1);
		if (idx1 < 0) {
			throw new NoSuchElementException("movable object not found");
		}
		int idx2 = controls.indexOf(obj2);
		if (idx2 < 0) {
			throw new NoSuchElementException("target object not found");
		}
		if (controls.remove(obj1)) {
			if (idx1 < idx2) {
				controls.add(idx2 - 1, obj1);
			} else {
				controls.add(idx2, obj1);
			}
		}
	}
	
	public void moveObject1AfterObject2(IPyObject obj1, IPyObject obj2) {
		int idx1 = controls.indexOf(obj1);
		if (idx1 < 0) {
			throw new NoSuchElementException("movable object not found");
		}
		int idx2 = controls.indexOf(obj2);
		if (idx2 < 0) {
			throw new NoSuchElementException("target object not found");
		}
		if (controls.remove(obj1)) {
			if (idx1 < idx2) {
				controls.add(idx2, obj1);
			} else {
				controls.add(idx2 + 1, obj1);
			}
		} else {
			throw new IllegalAccessError("failed to remove object from list");
		}
	}
	
	public List<IPyObject> getControlList() {
		return this.controls;
	}
	
	public interface IModelChangeListener{
		public void modelChanged();
	}
	
	public void addChangeListener(IModelChangeListener listener) {
		this.listeners.add(listener);
	}
	
	public void removeChangeListener(IModelChangeListener listener) {
		this.listeners.remove(listener);
	}
	
	public void fireModelChanged() {
		for (IModelChangeListener listener : listeners) {
			listener.modelChanged();
		}
	}
	
	public List<ScriptObjectGroup> getGroups() {
		List<ScriptObjectGroup> list = new ArrayList<ScriptObjectGroup>();
		for (IPyObject obj : controls) {
			if (obj.getObjectType() == PyObjectType.GROUP) {
				list.add((ScriptObjectGroup) obj);
			}
		}
		return list;
	}

	public List<ScriptObjectTab> getTabs() {
		List<ScriptObjectTab> list = new ArrayList<ScriptObjectTab>();
		for (IPyObject obj : controls) {
			if (obj.getObjectType() == PyObjectType.TAB) {
				list.add((ScriptObjectTab) obj);
			}
		}
		return list;
	}

	/**
	 * @return the numColumns
	 */
	public int getNumColumns() {
		return numColumns;
	}

	/**
	 * @param numColumns the numColumns to set
	 */
	public void setNumColumns(int numColumns) {
		this.numColumns = numColumns;
	}

	/**
	 * @return the isDirty
	 */
	public boolean isDirty() {
		return isDirty;
	}

	/**
	 * @param isDirty the isDirty to set
	 */
	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}

	public boolean isEqualWidth() {
		return isEqualWidth;
	}

	public void setEqualWidth(boolean isEqualWidth) {
		this.isEqualWidth = isEqualWidth;
	}

	public long getLastModifiedTimestamp() {
		return lastModifiedTimestamp;
	}

	public void setLastModifiedTimestamp(long lastModifiedTimestamp) {
		this.lastModifiedTimestamp = lastModifiedTimestamp;
	}
		
}
