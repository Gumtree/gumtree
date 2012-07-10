/*****************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *****************************************************************************/

package org.gumtree.gumnix.sics.batch.ui.views;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.gumtree.gumnix.sics.batch.ui.model.ISicsCommandElement;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITaskView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSicsCommandView<T extends ISicsCommandElement> implements ISicsCommandView<T> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractSicsCommandView.class);
	
	protected static final int WIDTH_PARAMETER = 70;
	
	protected static final int WIDTH_COMBO = 54;
	
	private boolean disposed;
	
	private FormToolkit toolkit;
	
	private T command;
	
	private ITaskView taskView;
	
	public T getCommand() {
		return command;
	}
	
	public void setCommand(T command) {
		this.command = command;
	}
	
	public void createPartControl(Composite parent) {
		// Automatic disposal
		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		});
		// Continue to create UI
		createPartControl(parent, getCommand());
	}
	
	protected abstract void createPartControl(Composite parent, T command);
	
	public void setTaskView(ITaskView taskView) {
		this.taskView = taskView;
	}
	protected void fireRefresh() {
		if (taskView != null && taskView instanceof AbstractTaskView) {
			((AbstractTaskView) taskView).fireRefresh();
		}
	}
	public void dispose() {
		if (isDisposed()) {
			logger.info("Unnecessary dispose method call.");
			return;
		}
		disposed = true;
		if (toolkit != null) {
			toolkit.dispose();
			toolkit = null;
		}
		command = null;
	}

	public boolean isDisposed() {
		return disposed;
	}

	public void setFocus() {
	}

	/**
	 * Returns an instance of form toolkit for convenience reason.
	 * 
	 * @return
	 */
	protected FormToolkit getToolkit() {
		if (toolkit == null) {
			toolkit = new FormToolkit(Display.getDefault());
		}
		return toolkit;
	}
	
}
