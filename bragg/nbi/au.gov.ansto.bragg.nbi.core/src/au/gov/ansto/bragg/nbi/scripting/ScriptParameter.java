/**
 * 
 */
package au.gov.ansto.bragg.nbi.scripting;

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
		LABEL,
		SPACE,
		DEFAULT
	};
	
	public ScriptParameter() {
	}
	
	private PType type = PType.STRING;
	private Object value;
	private List<Object> options;
	private String command;
	private boolean dirtyFlag = false;

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
			if ("str".equalsIgnoreCase(typeString)){
				type = PType.STRING;
			} else {
				type = PType.DEFAULT;
			}
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
	
	public boolean getDirtyFlag() {
		return dirtyFlag;
	}
	
	public void setDirtyFlag(){
		dirtyFlag = true;
	}
	
	public void resetDirtyFlag() {
		dirtyFlag = false;
	}
	
	public String getHtml(){
		String html;
		if (getOptions() != null) {
			String valueString;
			switch (type) {
			case INT:
				valueString = "int(' + this.value + ')";
				break;
			case FLOAT:
				valueString = "float(' + this.value + ')";
				break;
			case STRING:
				valueString = "str(\\\'' + this.value + '\\\')";
				break;
			default:
				valueString = "";
				break;
			}
			html = "<td colspan=\"" + getColSpan() + "\" rowspan=\"" + getRowSpan() + "\"><div class=\"div_jython_label\">" + getTitle() + "</div></td><td colspan=\"" + getColSpan() + "\" rowspan=\"" + getRowSpan() + "\"><div class=\"div_jython_input\">" +
					"<select name=\"" + getName() + "\" onchange=\"sendJython('" + getName() + ".value=" + valueString + (getCommand() != null ? ";" + getCommand() : "") + "')\" class=\"input_jython_text\" id=\"" 
					+ getName() + "\" value=\"" + value + "\" style=\"min-width:50%;min-height:12px;\">";
			for (Object option : getOptions()){
				boolean isSelected = false;
				switch (type) {
				case INT:
				case STRING:
					isSelected = getValue().equals(option);
					break;
				case FLOAT:
					isSelected = getValue().equals(Float.valueOf(option.toString()));
					break;
				default:
					break;
				}
				html += "<option value=\"" + option + "\"" + (isSelected ? " selected" : "") + ">" + option + "</option>";
			}
			html += "</select></div></td>";
			return html;
		}
		switch (type) {
		case INT:
			html = "<td colspan=\"" + getColSpan() + "\" rowspan=\"" + getRowSpan() + "\"><div class=\"div_jython_label\">" + getTitle() + "</div></td><td colspan=\"" + getColSpan() + "\" rowspan=\"" + getRowSpan() + "\"><div class=\"div_jython_input\"><input type=\"text\" name=\"" 
					+ getName() + "\" onchange=\"sendJython('" + getName() + ".value=int(' + this.value + ')" + (getCommand() != null ? ";" + getCommand() : "") + "')\" class=\"input_jython_text\" id=\"" 
					+ getName() + "\" value=\"" + value + "\" style=\"min-width:50%;min-height:12px;\" autocomplete=\"off\"></div></td>";
			break;
		case FLOAT:
			html = "<td colspan=\"" + getColSpan() + "\" rowspan=\"" + getRowSpan() + "\"><div class=\"div_jython_label\">" + getTitle() + "</div></td><td colspan=\"" + getColSpan() + "\" rowspan=\"" + getRowSpan() + "\"><div class=\"div_jython_input\"><input type=\"text\" name=\"" 
					+ getName() + "\" onchange=\"sendJython('" + getName() + ".value=float(' + this.value + ')" + (getCommand() != null ? ";" + getCommand() : "") + "')\" class=\"input_jython_text\" id=\"" 
					+ getName() + "\" value=\"" + value + "\" style=\"min-width:50%;min-height:12px;\" autocomplete=\"off\"></div></td>";
			break;
		case STRING:
			html = "<td colspan=\"" + getColSpan() + "\" rowspan=\"" + getRowSpan() + "\"><div class=\"div_jython_label\">" + getTitle() + "</div></td><td colspan=\"" + getColSpan() + "\" rowspan=\"" + getRowSpan() + "\"><div class=\"div_jython_input\"><input type=\"text\" name=\"" 
					+ getName() + "\" onchange=\"sendJython('" + getName() + ".value=str(\\\'' + this.value + '\\\')" + (getCommand() != null ? ";" + getCommand() : "") + "')\" class=\"input_jython_text\" id=\"" 
					+ getName() + "\" value=\"" + value + "\" style=\"min-width:50%;min-height:12px;\" autocomplete=\"off\"></div></td>";
			break;
		case BOOL:
			html = "<td colspan=\"" + getColSpan() + "\" rowspan=\"" + getRowSpan() + "\"><div class=\"div_jython_label\">" + getTitle() + "</div></td><td colspan=\"" + getColSpan() + "\" rowspan=\"" + getRowSpan() + "\"><div class=\"div_jython_input\"><input type=\"checkbox\" name=\"" 
					+ getName() + "\" onclick=\"sendJython('" + getName() + ".value=' + getBool(this.checked)" + (getCommand() != null ? ";" + getCommand() : "") + ")\" class=\"input_jython_check\" id=\"" 
					+ getName() + "\"" + ((Boolean) value ? "checked" : "") + " ></div></td>";
			break;
		case LABEL:
			html = "<td colspan=\"" + getColSpan() * 2 + "\" rowspan=\"" + getRowSpan() + "\"><div class=\"div_jython_label\" id=\"" + getName() + "\">" + getValue() + "</div></td>";
			break;
		case PROGRESS:
			html = "<td colspan=\"" + getColSpan() * 2 + "\" rowspan=\"" + getRowSpan() + "\"><div class=\"div_jython_progress_wrap\"><div class=\"div_jython_label div_jython_label_progress\">" 
					+ getTitle() + "</div><div id=\"" + getName() + "\" class=\"div_jython_progress\"></div></div></td>";
			break;
		case FILE:
		case SPACE:
			html = "<td colspan=\"" + getColSpan() * 2 + "\" rowspan=\"" + getRowSpan() + "\">&nbsp;</td>";
			break;
		default:
			html = "<td colspan=\"" + getColSpan()* 2 + "\" rowspan=\"" + getRowSpan() + "\"><div class=\"div_jython_blank\">" + getValue() + "</div></td>";
		}
		return html;
	}
	
	public String getInitJs(){
		String js = null;
		switch (type) {
		case PROGRESS:
			js = "$('#" + getName() + "').progressbar({value: " + getValue() + ", max: 100});";
			break;
		default:
			break;
		}
		return js;
	}
	
	public String getEventJs(String property){
		String js = "";
		if ("value".equals(property)) {
			switch (type) {
			case INT:
				js = "$('#" + getName() + "').val(" + getValue() + ");";
				break;
			case FLOAT:
				js = "$('#" + getName() + "').val(" + (Float) getValue() + ");";
				break;
			case STRING:
				js = "$('#" + getName() + "').val('" + getValue() + "');";
				break;
			case BOOL:
				js = "$('#" + getName() + "').prop('checked', " + getValue() + ");";
				break;
			case LABEL:
				js = "$('#" + getName() + "').html('" + getValue() + "');";
				break;
			case PROGRESS:
				js = "$('#" + getName() + "').progressbar('option', 'value', " + getValue() + ");";
				break;
			case FILE:
			case SPACE:
				js = "";
				break;
			default:
				js = "";
			}
//			if (getCommand() != null) {
//				js += "sendJython(\"" + getCommand() + "\");";
//			}
		} else if ("enabled".equals(property)){
			switch (type) {
			case INT:
			case FLOAT:
			case BOOL:
			case STRING:
				if (getProperty("enabled") != null){
					if (getProperty("enabled").toLowerCase().equals("false")){
						js = "$('#" + getName() + "').attr('disabled', 'disabled');";
					} else {
						js = "$('#" + getName() + "').removeAttr('disabled');";
					}
				}
				break;
			case PROGRESS:
				if (getProperty("enabled") != null){
					js = "$('#" + getName() + "').progressbar('option', 'disabled', " + getProperty("enabled").toLowerCase().equals("false") + ");";
				}
				break;
			default:
				js = "";
			}
		} else if ("options".equals(property)){
			String optionString = "";
			switch (type) {
			case INT:
				for (Object option : getOptions()){
					optionString += option + ",";
				}
				break;
			case FLOAT:
				for (Object option : getOptions()){
					optionString += Float.valueOf(option.toString()) + ",";
				}
				break;
			case STRING:
				for (Object option : getOptions()){
					optionString += "'" + option + "',";
				}
				break;
			default:
				break;
			}
			js = "changeOptions($('#" + getName() + "'), [" + optionString + "]);";
		} else if ("selection".equals(property)){
			switch (type) {
			case PROGRESS:
				int max = 100;
				if (getProperty("max") != null) {
					max = Integer.valueOf(getProperty("max"));
				}
				int value = Integer.valueOf(getProperty(property)) * 100 / max;
				js = "$('#" + getName() + "').progressbar('option', 'value', " + value + ");";
				break;

			default:
				break;
			}
		}
		return js;
	}
}
