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

package org.gumtree.workflow.ui.views;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.gumtree.util.collection.IParameters;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.models.ParametersObservables;

// TODO: Support user defined editor for each field
// TODO: Support optional spinner for int type
// TODO: Support file type
public class ParametersBasedTaskView extends AbstractTaskView {

	private IParameters parameters;
	
	private Map<String, String> labels;
	
	private Map<String, String> units;
	
	public ParametersBasedTaskView(IParameters parameters) {
		this.parameters = parameters;
		labels = new HashMap<String, String>(2);
		units = new HashMap<String, String>(2);
	}
	
	public void setLabel(String key, String label) {
		labels.put(key, label);
	}
	
	public void setUnit(String key, String unit) {
		units.put(key, unit);
	}

	public void createPartControl(final Composite parent) {
		Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
			public void run() {
				DataBindingContext bindingContext = new DataBindingContext();
				GridLayoutFactory.swtDefaults().numColumns(3).applyTo(parent);
				for (Entry<String, Object> entry : parameters.entrySet()) {
					if (entry.getValue() instanceof Number || entry.getValue() instanceof String) {
						// Support number and text
						createLabel(parent, entry.getKey());
						
						Text text = getToolkit().createText(parent, "");
						
						if (units.containsKey(entry.getKey())) {
							GridDataFactory.fillDefaults().grab(true, false).applyTo(text);
							getToolkit().createLabel(parent, " " + units.get(entry.getKey()));
						} else {
							GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(text);
						}
						
						bindingContext.bindValue(SWTObservables.observeText(text, SWT.Modify),
								ParametersObservables.observeValue(parameters, entry.getKey()),
								new UpdateValueStrategy(), new UpdateValueStrategy());
					} else if (entry.getValue().getClass().isEnum()) {
						// Support enum
						createLabel(parent, entry.getKey());
						
						ComboViewer comboViewer = new ComboViewer(parent, SWT.READ_ONLY);
						comboViewer.setContentProvider(new ArrayContentProvider());
						comboViewer.setLabelProvider(new LabelProvider());
						comboViewer.setInput(entry.getValue().getClass().getEnumConstants());
						
						GridDataFactory.fillDefaults().grab(true, false).span(2, 0).applyTo(comboViewer.getControl());
						bindingContext.bindValue(SWTObservables.observeSelection(comboViewer.getControl()),
								ParametersObservables.observeValue(parameters, entry.getKey()),
								new UpdateValueStrategy(), new UpdateValueStrategy());
					}
				}
			}
		});
	}

	private void createLabel(Composite parent, String key) {
		if (labels.containsKey(key)) {
			getToolkit().createLabel(parent, labels.get(key) + ": ");
		} else {
			getToolkit().createLabel(parent, getLabel(key));
		}
	}
	
	// Transform a key to a label, for example, time -> Time:
	private static String getLabel(String key) {
		if (key == null || key.length() < 1) {
			return "";
		}
		return key.substring(0, 1).toUpperCase() + key.substring(1) + ": ";
	}
	
}
