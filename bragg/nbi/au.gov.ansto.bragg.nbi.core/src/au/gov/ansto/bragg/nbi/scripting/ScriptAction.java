/**
 * 
 */
package au.gov.ansto.bragg.nbi.scripting;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nxi
 *
 */
public class ScriptAction extends PyObjectImp {

	public enum ActionStatus {RUNNING, INTERRUPT, DONE, ERROR, DEFAULT}
	private List<IActionStatusListener> listeners = new ArrayList<IActionStatusListener>();
	
	private String text;
	private String command;
	private ActionStatus status;
	
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

	public ActionStatus getStatus() {
		return status;
	}

	public void setStatus(ActionStatus status) {
		this.status = status;
		setStatusChanged(status);
	}

	public void clearStatus() {
		this.status = ActionStatus.DEFAULT;
	}
	
	public void setBusyStatus() {
		setStatus(ActionStatus.RUNNING);
	}
	
	public void setDoneStatus() {
		setStatus(ActionStatus.DONE);
	}
	
	public void setErrorStatus() {
		setStatus(ActionStatus.ERROR);
	}
	
	public void setInterruptStatus() {
		setStatus(ActionStatus.INTERRUPT);
	}
	
	public void addStatusListener(IActionStatusListener listener) {
		listeners.add(listener);
	}

	public void removeStatusListener(IActionStatusListener listener) {
		listeners.remove(listener);
	}

	private void setStatusChanged(ActionStatus newStatus) {
		for (IActionStatusListener listener : listeners) {
			listener.statusChanged(newStatus);
		}
	}
	
	public interface IActionStatusListener{
		public void statusChanged(ActionStatus newStatus);
	}
	
	public String getHtml(){
		return "<td colspan=\"" + getColSpan() * 2 + "\" rowspan=\"" + getRowSpan() + "\"><div class=\"div_jython_action\"><input type=\"button\" class=\"buttonAction buttonStyle\" data-role=\"button\" id=\"" + getName() + "\" value=\"" 
				+ text + "\" onclick=\"sendJython('" + command + "')\"/></div></td>";
	}

	@Override
	public String getInitJs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEventJs(String property) {
		// TODO Auto-generated method stub
		return null;
	}
}
