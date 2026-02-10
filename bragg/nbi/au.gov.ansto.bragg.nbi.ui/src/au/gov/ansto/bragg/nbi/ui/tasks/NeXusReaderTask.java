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

package au.gov.ansto.bragg.nbi.ui.tasks;

import java.io.File;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;
import org.gumtree.data.Factory;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.workflow.ui.AbstractTask;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.WorkflowException;
import org.gumtree.workflow.ui.models.SingleStringDataModel;

public class NeXusReaderTask extends AbstractTask {

	@Override
	protected Object createModelInstance() {
		return new SingleStringDataModel();
	}

	@Override
	protected ITaskView createViewInstance() {
		return new NeXusReaderTaskView();
	}

	@Override
	protected Object run(Object input) throws WorkflowException {
		if (getFileLocation() != null) {
			try {
				return Factory.createDatasetInstance(new File(getFileLocation()).toURI());
			} catch (FileAccessException e) {
				new WorkflowException("Failed to load NeXus file from " + getFileLocation(), e);
			} catch (Exception e) {
				new WorkflowException("Failed to load NeXus file from " + getFileLocation(), e);
			}
		}
		return null;
	}

	public String getFileLocation() {
		return getDataModel().getString();
	}
	
	public void setFileLocation(String fileLocation) {
		getDataModel().setString(fileLocation);
	}
	
	public SingleStringDataModel getDataModel() {
		return (SingleStringDataModel) super.getDataModel();
	}
	
	public Class<?>[] getInputTypes() {
		return null;
	}
	
	public Class<?>[] getOutputTypes() {
		return new Class[] { IDataset.class };
	}
	
	private class NeXusReaderTaskView extends AbstractTaskView {

		@Override
		public void createPartControl(final Composite parent) {
			parent.setLayout(new GridLayout(3, false));
			// Label
			getToolkit().createLabel(parent, "File: ");
			// Text - location
			final Text fileLocationText = getToolkit().createText(parent, "", SWT.READ_ONLY );
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(fileLocationText);
			// Button
			Button browseButton = getToolkit().createButton(parent, "Browse", SWT.PUSH);
			browseButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					FileDialog fileDialog = new FileDialog(parent.getShell(), SWT.SINGLE);
					String filename = fileDialog.open();
					if (filename != null) {
						setFileLocation(filename);
					}
				}
			});
			// Data binding
			Realm.runWithDefault(DisplayRealm.getRealm(Display.getDefault()), new Runnable() {
				public void run() {
					DataBindingContext bindingContext = new DataBindingContext();
					bindingContext.bindValue(
							WidgetProperties.text(SWT.Modify).observe(fileLocationText),
							BeanProperties.value("string").observe(getDataModel()),
							new UpdateValueStrategy(), new UpdateValueStrategy());
				}
			});
		}
		
	}
	
}
