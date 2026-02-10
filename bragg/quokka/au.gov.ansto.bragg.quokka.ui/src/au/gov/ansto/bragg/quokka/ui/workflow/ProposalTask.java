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

package au.gov.ansto.bragg.quokka.ui.workflow;

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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.gumtree.widgets.swt.util.UIResources;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITaskView;

/**
 * Proposal task handles the input of general experiment details. This includes
 * user information, experiment title and associated instrument statistic files.
 * 
 */
public class ProposalTask extends AbstractExperimentTask {

	@Override
	protected ITaskView createViewInstance() {
		return new ProposalTaskView();
	}
	
	class ProposalTaskView extends AbstractTaskView {
		
		public void createPartControl(final Composite parent) {
			parent.setLayout(new GridLayout(3, false));
			
			/*****************************************************************
			 * User details
			 *****************************************************************/
			Label label = getToolkit().createLabel(parent, "Experiment Title: ");
			Font boldFont = UIResources.getDefaultFont(SWT.BOLD);
			label.setFont(boldFont);
			GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).applyTo(label);
			
			final Text titleText = getToolkit().createText(parent, null, SWT.LEFT);
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).hint(350, SWT.DEFAULT).span(2, 1).applyTo(titleText);
			
			label = getToolkit().createLabel(parent, "User: ");
			label.setFont(boldFont);
			GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).applyTo(label);
			
			final Text userText = getToolkit().createText(parent, null, SWT.LEFT);
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).hint(350, SWT.DEFAULT).span(2, 1).applyTo(userText);
			
			label = getToolkit().createLabel(parent, "Email: ");
			label.setFont(boldFont);
			GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).applyTo(label);
			
			final Text emailText = getToolkit().createText(parent, null, SWT.LEFT);
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).hint(350, SWT.DEFAULT).span(2, 1).applyTo(emailText);
			
			label = getToolkit().createLabel(parent, "Phone: ");
			label.setFont(boldFont);
			GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).applyTo(label);
			
			final Text phoneText = getToolkit().createText(parent, null, SWT.LEFT);
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).hint(350, SWT.DEFAULT).span(2, 1).applyTo(phoneText);
			
			/*****************************************************************
			 * Separator
			 *****************************************************************/
			label = getToolkit().createLabel(parent, "");
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).hint(550, SWT.DEFAULT).span(3, 1).applyTo(label);
			label = getToolkit().createSeparator(parent, SWT.HORIZONTAL);
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).hint(550, SWT.DEFAULT).span(3, 1).applyTo(label);
			label = getToolkit().createLabel(parent, "");
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).hint(550, SWT.DEFAULT).span(3, 1).applyTo(label);
			
			/*****************************************************************
			 * Supporting files
			 *****************************************************************/
			label = getToolkit().createLabel(parent, "Dark current File: ");
			label.setFont(boldFont);
			GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).applyTo(label);
			
			final Text darkCurrentText = getToolkit().createText(parent, null, SWT.LEFT | SWT.READ_ONLY);
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).hint(350, SWT.DEFAULT).applyTo(darkCurrentText);
			
			Button darkCurrentButton = getToolkit().createButton(parent, "Browse", SWT.PUSH);
			darkCurrentButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					FileDialog dialog = new FileDialog(parent.getShell());
					String filename = dialog.open();
					if (filename != null) {
						darkCurrentText.setText(filename);
					}
				}
			});
			
			label = getToolkit().createLabel(parent, "Sensitivity File: ");
			label.setFont(boldFont);
			GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).applyTo(label);
			
			final Text sensitivityText = getToolkit().createText(parent, null, SWT.LEFT | SWT.READ_ONLY);
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).hint(350, SWT.DEFAULT).applyTo(sensitivityText);

			Button sensitivityButton = getToolkit().createButton(parent, "Browse", SWT.PUSH);
			sensitivityButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					FileDialog dialog = new FileDialog(parent.getShell());
					String filename = dialog.open();
					if (filename != null) {
						sensitivityText.setText(filename);
					}
				}
			});
			
			/*****************************************************************
			 * Separator
			 *****************************************************************/
			label = getToolkit().createLabel(parent, "");
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).hint(550, SWT.DEFAULT).span(3, 1).applyTo(label);
			label = getToolkit().createSeparator(parent, SWT.HORIZONTAL);
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).hint(550, SWT.DEFAULT).span(3, 1).applyTo(label);
			label = getToolkit().createLabel(parent, "");
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).hint(550, SWT.DEFAULT).span(3, 1).applyTo(label);
			
			/*****************************************************************
			 * Report file
			 *****************************************************************/
			label = getToolkit().createLabel(parent, "Report directory: ");
			label.setFont(boldFont);
			GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).applyTo(label);
			
			final Text reportDirectoryText = getToolkit().createText(parent, null, SWT.LEFT | SWT.READ_ONLY);
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).hint(350, SWT.DEFAULT).applyTo(reportDirectoryText);
			
			Button reportDirectoryButton = getToolkit().createButton(parent, "Browse", SWT.PUSH);
			reportDirectoryButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					DirectoryDialog dialog = new DirectoryDialog(parent.getShell());
					String directoryName = dialog.open();
					if (directoryName != null) {
						reportDirectoryText.setText(directoryName);
					}
				}
			});
			
			/*****************************************************************
			 * Data binding
			 *****************************************************************/
			Realm.runWithDefault(DisplayRealm.getRealm(Display.getDefault()), new Runnable() {
				public void run() {
					DataBindingContext bindingContext = new DataBindingContext();
					bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(titleText), 
							BeanProperties.value("title").observe(getExperiment()), 
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(userText), 
							BeanProperties.value("name").observe(getExperiment().getUser()), 
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(emailText), 
							BeanProperties.value("email").observe(getExperiment().getUser()), 
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(phoneText), 
							BeanProperties.value("phone").observe(getExperiment().getUser()), 
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(darkCurrentText), 
							BeanProperties.value("darkCurrentFile").observe(getExperiment()), 
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(sensitivityText), 
							BeanProperties.value("sensitivityFile").observe(getExperiment()), 
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(reportDirectoryText), 
							BeanProperties.value("userReportDirectory").observe(getExperiment()), 
							new UpdateValueStrategy(), new UpdateValueStrategy());
				}
			});
			
		}

	}
	
}