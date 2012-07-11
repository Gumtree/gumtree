/**
 * 
 */
package au.gov.ansto.bragg.wombat.ui.script.pyobj;

/**
 * @author nxi
 *
 */
public class ScriptAction implements IPyObject {

	private String text;
	private String command;
	private String name;
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

}
