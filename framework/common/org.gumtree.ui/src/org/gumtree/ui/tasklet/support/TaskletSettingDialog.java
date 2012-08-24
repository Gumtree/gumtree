package org.gumtree.ui.tasklet.support;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.gumtree.ui.tasklet.ITasklet;
import org.gumtree.ui.tasklet.ITaskletManager;

public class TaskletSettingDialog extends Dialog {

	private String contributionUri;

	private ITaskletManager taskletRegistry;

	private UIContext context;
	
	private ITasklet tasklet;

	public TaskletSettingDialog(Shell parentShell) {
		super(parentShell);
		context = new UIContext();
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(composite);

		// Label
		Label label = new Label(composite, SWT.NONE);
		label.setText("Label: ");
		context.labelText = new Text(composite, SWT.BORDER);
		if (getTasklet() != null  && getTasklet().getLabel() != null) {
			context.labelText.setText(getTasklet().getLabel());
		} else {
			context.labelText.setText("New Task");
			context.labelText.selectAll();
		}
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).hint(350, SWT.DEFAULT).span(2, 1)
				.applyTo(context.labelText);

		// Tags
		label = new Label(composite, SWT.NONE);
		label.setText("Tags: ");
		context.tagsText = new Text(composite, SWT.BORDER);
		if (getTasklet() != null && getTasklet().getTags() != null) {
			context.tagsText.setText(getTasklet().getTags());
		} else {
			context.tagsText.setText("");
		}
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(2, 1).applyTo(context.tagsText);

		// Script
		label = new Label(composite, SWT.NONE);
		label.setText("Script: ");
		context.scriptText = new Text(composite, SWT.BORDER);
		if (getTasklet() != null) {
			context.scriptText.setText(getTasklet().getContributionURI());
		} else {
			if (getContributionUri() != null) {
				context.scriptText.setText(getContributionUri());
			}
		}
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(context.scriptText);
		Button browseButton = new Button(composite, SWT.PUSH);
		browseButton.setText("Broswe...");
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell());
				String filename = dialog.open();
				if (filename != null) {
					File file = new File(filename);
					context.scriptText.setText(file.toURI().toString());
				}
			}
		});

		// Layout option
		Group layoutGroup = new Group(composite, SWT.NONE);
		layoutGroup.setText("Layout");
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, true).span(3, 1).applyTo(layoutGroup);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(layoutGroup);
		context.simpleLayoutButton = new Button(layoutGroup, SWT.RADIO);
		context.simpleLayoutButton.setText("Simple");
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(context.simpleLayoutButton);
		context.advancedLayoutButton = new Button(layoutGroup, SWT.RADIO);
		context.advancedLayoutButton.setText("Advanced");
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(context.advancedLayoutButton);
		if (getTasklet() != null) {
			if (getTasklet().isSimpleLayout()) {
				context.simpleLayoutButton.setSelection(true);
			} else {
				context.advancedLayoutButton.setSelection(true);
			}
		} else {
			context.simpleLayoutButton.setSelection(true);
		}
		
		// New window option
		context.startNewWindowButton = new Button(composite, SWT.CHECK);
		context.startNewWindowButton
				.setText(" Always start task in new window");
		if (getTasklet() != null) {
			context.startNewWindowButton.setSelection(getTasklet().isNewWindow());
		}
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, true).span(3, 1)
				.applyTo(context.startNewWindowButton);

		// Title
		getShell().setText("Add new tasklet");

		return composite;
	}

	// Override to set focus
	@Override
	public void create() {
		super.create();
		context.labelText.setFocus();
	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	public boolean close() {
		context = null;
		return super.close();
	}

	@Override
	protected void okPressed() {
		// Prepare new tasklet
		if (getTaskletRegistry() != null) {
			ITasklet tasklet = getTasklet();
			if (tasklet == null) {
				tasklet = new Tasklet();
			}
			tasklet.setLabel(context.labelText.getText());
			tasklet.setTags(context.tagsText.getText());
			tasklet.setContributionURI(context.scriptText.getText());
			tasklet.setSimpleLayout(context.simpleLayoutButton.getSelection());
			tasklet.setNewWindow(context.startNewWindowButton.getSelection());
			if (getTasklet() != null) {
				getTaskletRegistry().updateTasklet(tasklet);	
			} else {
				getTaskletRegistry().addTasklet(tasklet);
			}
		}
		super.okPressed();
	}

	/*************************************************************************
	 * Properties
	 *************************************************************************/

	public String getContributionUri() {
		return contributionUri;
	}

	public void setContributionUri(String contributionUri) {
		this.contributionUri = contributionUri;
	}

	public ITasklet getTasklet() {
		return tasklet;
	}
	
	public void setTasklet(ITasklet tasklet) {
		this.tasklet = tasklet;
	}
	
	public ITaskletManager getTaskletRegistry() {
		return taskletRegistry;
	}

	public void setTaskletRegistry(ITaskletManager taskletRegistry) {
		this.taskletRegistry = taskletRegistry;
	}

	/*************************************************************************
	 * Utilities
	 *************************************************************************/

	private class UIContext {
		Text labelText;
		Text tagsText;
		Text scriptText;
		Button simpleLayoutButton;
		Button advancedLayoutButton;
		Button startNewWindowButton;
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		TaskletSettingDialog dialog = new TaskletSettingDialog(shell);
		dialog.open();

		display.dispose();
	}
}
