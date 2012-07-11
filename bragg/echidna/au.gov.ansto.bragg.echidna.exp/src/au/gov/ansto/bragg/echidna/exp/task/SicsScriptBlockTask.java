/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package au.gov.ansto.bragg.echidna.exp.task;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.gumtree.gumnix.sics.batch.ui.CommandBlockTask;
import org.gumtree.gumnix.sics.batch.ui.commands.ScriptCommand;
import org.gumtree.gumnix.sics.batch.ui.views.ScriptView;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITaskView;

public class SicsScriptBlockTask extends CommandBlockTask {

	public static final String TITLE = "Sics Script";
//	private ScriptCommand scriptCommand;
	
	public SicsScriptBlockTask() {
		super();
//		if (getDataModel() != null && getDataModel().getCommands().length == 1){
//			ISicsCommandElement[] commands = getDataModel().getCommands();
//			scriptCommand = (ScriptCommand) commands[0];
//		}else{
//			scriptCommand = new ScriptCommand();
//			getDataModel().addCommand(scriptCommand);
//		}
	}

	protected ITaskView createViewInstance() {
		return new SicsScriptBlockTaskView();
	}
		
	private class SicsScriptBlockTaskView extends AbstractTaskView {

		public void createPartControl(Composite parent) {
			GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(2).applyTo(parent);
			Label scriptLabel = getToolkit().createLabel(parent, "Script ");
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).indent(2, 2).
				applyTo(scriptLabel);
			
			ScriptCommand scriptCommand = null;
			if (getDataModel().getCommands().length == 0){
				scriptCommand = new ScriptCommand();
				getDataModel().addCommand(scriptCommand);
			} else {
				scriptCommand = (ScriptCommand) getDataModel().getCommands()[0];
			}
			
			Composite scriptComposite = getToolkit().createComposite(parent);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(scriptComposite);
			ScriptView scriptView = new ScriptView();
			scriptView.setTaskView(this);
			scriptView.setCommand(scriptCommand);
			scriptView.createPartControl(scriptComposite);
		}
		
	}
	
}
