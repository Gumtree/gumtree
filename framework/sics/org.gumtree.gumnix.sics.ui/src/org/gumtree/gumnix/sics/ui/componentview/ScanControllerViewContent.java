package org.gumtree.gumnix.sics.ui.componentview;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.IScanController;
import org.gumtree.gumnix.sics.control.events.IScanControllerListener;
import org.gumtree.gumnix.sics.control.events.ScanControllerListenerAdapter;

public class ScanControllerViewContent implements IComponentViewContent {

	private IScanController controller;

	private FormToolkit toolkit;

	private DataBindingContext bindingContext;

	private Text npText;

	private Text presetText;

	private Text scanStartText;

	private Text scanIncrementText;

	private Text variableText;

	private ComboViewer modeComboViewer;

	private ListViewer listViewer;

	private IScanControllerListener controllerListener;

	public void createPartControl(Composite parent, IComponentController controller) {
		Assert.isTrue(controller instanceof IScanController);
		this.controller = (IScanController) controller;
		toolkit = new FormToolkit(parent.getDisplay());
		parent.setLayout(new FillLayout());
		ScrolledForm form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(new GridLayout(2, false));
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);

		getToolkit().createLabel(form.getBody(), "Preset: ");
		presetText = getToolkit().createText(form.getBody(), "", SWT.BORDER);
		presetText.setLayoutData(gridData);

		bindingContext = new DataBindingContext();
		bindingContext.bindValue(
				WidgetProperties.text(SWT.Modify).observe(presetText),
				BeanProperties.value("preset").observe(getController().config()), 
				new UpdateValueStrategy(), new UpdateValueStrategy());

		controllerListener = new ScanControllerListenerAdapter() {
			@Override
			public void scanConfigUpdated() {
			}
			@Override
			public void scanStatusUpdated() {
			}
		};

		getController().addComponentListener(controllerListener);
	}

	public void dispose() {
		if (bindingContext != null) {
			bindingContext.dispose();
		}
		if (getController() != null && controllerListener != null) {
			getController().removeComponentListener(controllerListener);
			controllerListener = null;
		}
		if (toolkit != null) {
			toolkit.dispose();
			toolkit = null;
		}
	}

	public IScanController getController() {
		return controller;
	}

	private FormToolkit getToolkit() {
		return toolkit;
	}

}
