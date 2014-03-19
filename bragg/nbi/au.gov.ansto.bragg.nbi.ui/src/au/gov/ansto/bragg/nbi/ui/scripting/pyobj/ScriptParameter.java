/**
 * 
 */
package au.gov.ansto.bragg.nbi.ui.scripting.pyobj;

import java.util.List;



/**
 * @author nxi
 *
 */
public class ScriptParameter extends PyObjectImp {

	public enum PType{
		STRING,
		INT,
		FLOAT,
		BOOL,
		FILE,
		PROGRESS,
		DEFAULT
	};
	
	public ScriptParameter() {
	}
	
	private PType type = PType.STRING;
	private Object value;
	private List<Object> options;
	private String command;

	public PType getType() {
		return type;
	}

	public void setType(PType type) {
		this.type = type;
	}

	public void setTypeName(String typeString) {
		try {
			type = PType.valueOf(typeString.toUpperCase());
		} catch (Exception e) {
			type = PType.DEFAULT;
		}
	}
	
	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		Object oldValue = this.value;
		switch (type) {
		case INT:
			this.value = Integer.valueOf(String.valueOf(value));
			break;
		case FLOAT:
			this.value = Float.valueOf(String.valueOf(value));
			break;
		case BOOL:
			this.value = Boolean.valueOf(String.valueOf(value));
			break;
		default:
			this.value = value;
			break;
		}
		if (oldValue != this.value) {
			firePropertyChange("value", oldValue, value);
		}
	}

	public List<Object> getOptions() {
		return options;
	}
	
	public void setOptions(List<Object> options) {
		List<Object> oldValue = this.options;
		this.options = options;
		firePropertyChange("options", oldValue, options);
	}
	
	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}
	
}
