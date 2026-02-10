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

package org.gumtree.control.ui.batch.taskeditor;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.gumtree.control.ui.batch.command.AbstractSicsCommandView;
import org.gumtree.control.ui.batch.command.ScriptCommand;

public class ScriptView extends AbstractSicsCommandView<ScriptCommand> {

private DataBindingContext bindingContext;
	
	private static final int HEIGHT_TEXT = 100;
	private static final String PROP_TEXT_HEIGHT = "gumtree.workflow.scriptTextHeight";
	
	private Text text;
	
	
	@Override
	public void createPartControl(Composite parent, ScriptCommand command) {
		int textHeight = HEIGHT_TEXT;
		try {
			textHeight = Integer.valueOf(System.getProperty(PROP_TEXT_HEIGHT));
		} catch (Exception e) {
		}
		GridLayoutFactory.fillDefaults().margins(0, 0).applyTo(parent);
		text = getToolkit().createText(parent, "", SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		text.setToolTipText("Enter script");
		GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, textHeight).applyTo(text);

		/*********************************************************************
		 * Data binding
		 *********************************************************************/
		Realm.runWithDefault(DisplayRealm.getRealm(Display.getDefault()), new Runnable() {
			public void run() {
				bindingContext = new DataBindingContext();
				bindingContext.bindValue(
						WidgetProperties.text(SWT.Modify).observe(text),
						BeanProperties.value(ScriptCommand.class, "text").observe(getCommand()),
						new UpdateValueStrategy(),
						new UpdateValueStrategy()
				);
			}
		});
	}

	public void dispose() {
		if (bindingContext != null) {
			bindingContext.dispose();
			bindingContext = null;
		}
		text = null;
		super.dispose();
	}
	
}