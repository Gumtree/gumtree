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
public class ScriptAction implements IPyObject {

	public enum ActionStatus {RUNNING, INTERRUPT, DONE, ERROR, DEFAULT}
	private List<IActionStatusListener> listeners = new ArrayList<IActionStatusListener>();
	
	private String text;
	private String command;
	private String name;
	private ActionStatus status;
	
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

	public ActionStatus getStatus() {
		return status;
	}

	public void setStatus(ActionStatus status) {
		this.status = status;
		setStatusChanged(status);
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
}
