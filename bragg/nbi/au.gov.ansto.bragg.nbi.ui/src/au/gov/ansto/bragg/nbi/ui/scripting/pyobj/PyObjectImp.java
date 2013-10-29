/**
 * 
 */
package au.gov.ansto.bragg.nbi.ui.scripting.pyobj;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author nxi
 *
 */
public class PyObjectImp implements IPyObject{

	private String name;
	private Map<String, String> properties;

	private PropertyChangeSupport changeListener = new PropertyChangeSupport(this);
	
	protected void firePropertyChange(String name, Object oldValue, Object newValue) {
		changeListener.firePropertyChange(name, oldValue, newValue);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		changeListener.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		changeListener.removePropertyChangeListener(listener);
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setProperty(String name, Object value){
		if (properties == null) {
			properties = new HashMap<String, String>();
		}
		String oldValue = properties.get(name);
		properties.put(name, String.valueOf(value));
		firePropertyChange(name, oldValue, String.valueOf(value));
	}
	
	public String getProperty(String name) {
		if (properties == null) {
			return null;
		}
		return properties.get(name);
	}
	
	public Set<String> getPropertyNames() {
		if (properties == null) {
			return null;
		}
		return properties.keySet();
	}

}
