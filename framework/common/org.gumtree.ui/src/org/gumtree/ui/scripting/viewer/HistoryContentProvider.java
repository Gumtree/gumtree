package org.gumtree.ui.scripting.viewer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.gumtree.ui.util.SafeUIRunner;

public class HistoryContentProvider implements IStructuredContentProvider, ICommandHistoryList {

	private List<CommandHistory> commandList;
	
	private StructuredViewer viewer;
	
	public Object[] getElements(Object inputElement) {
		if (inputElement == this) {
			return getCommandList().toArray(new CommandHistory[getCommandList().size()]);
		}
		return new Object[0];
	}

	public void dispose() {
		if (commandList != null) {
			clearCommands();
		}
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (viewer instanceof StructuredViewer) {
			this.viewer = (StructuredViewer) viewer;
		}
	}
	
	private List<CommandHistory> getCommandList() {
		if (commandList == null) {
			synchronized (this) {
				if (commandList == null) {
					commandList = new ArrayList<CommandHistory>();
				}
			}
		}
		return commandList;
	}
	
	public void appendCommand(String command) {
		getCommandList().add(new CommandHistory(command));
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (viewer != null && !viewer.getControl().isDisposed()) {
					viewer.refresh();
				}
			}			
		});
	}
	
	public void clearCommands() {
		getCommandList().clear();
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (viewer != null && !viewer.getControl().isDisposed()) {
					viewer.refresh();
				}
			}			
		});
	}

	public class CommandHistory {
		
		private Date timestamp;
		
		private String command;
		
		private CommandHistory(String command) {
			timestamp = new Date(System.currentTimeMillis());
			this.command = command;
		}

		public Date getTimestamp() {
			return timestamp;
		}

		public String getCommand() {
			return command;
		}
		
	}

	public String get(int index) {
		CommandHistory history = (CommandHistory) getCommandList().get(index);
		if (history != null) {
			return history.getCommand();
		}
		return null;
	}

	public int size() {
		return getCommandList().size();
	}

	public boolean isEmpty() {
		return getCommandList().isEmpty();
	}
	
	public String[] getCommands() {
		List<String> buffer = new ArrayList<String>();
		// Synchronized to avoid concurrent access on the command list
		synchronized (commandList) {
			for (int i = 0; i < commandList.size(); i++) {
				buffer.add(commandList.get(i).getCommand());
			}
		}
		return buffer.toArray(new String[buffer.size()]);
	}
	
}
