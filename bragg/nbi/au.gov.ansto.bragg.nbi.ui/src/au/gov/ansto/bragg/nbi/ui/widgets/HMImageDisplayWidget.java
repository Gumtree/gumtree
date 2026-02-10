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

package au.gov.ansto.bragg.nbi.ui.widgets;

import javax.inject.Inject;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.ui.widgets.ScalableImageDisplayWidget;

import au.gov.ansto.bragg.nbi.ui.internal.Activator;

public class HMImageDisplayWidget extends ScalableImageDisplayWidget {
	
	private final static String[] SCALE_INPUT = new String[]{"enable", "disable"};
	private static final String IS_SCALE_ENABLED = "gumtree.hm.isScaleEnabled";
	private HMImageMode imageMode;
	private boolean isScaleEnabled;
	private ComboViewer comboViewer;
	
	public HMImageDisplayWidget(Composite parent, int style) {
		super(parent, style);
	}
	
	protected void widgetDispose() {
		imageMode = null;
		super.widgetDispose();
	}
	
	protected Composite createImageArea() {
		GridLayoutFactory.swtDefaults().numColumns(9).margins(0, 0).applyTo(this);
		
		// Mode
		Label label = getToolkit().createLabel(this, "Mode: ");
		label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).grab(true, false).applyTo(label);
		comboViewer = new ComboViewer(this, SWT.READ_ONLY);
		comboViewer.setContentProvider(new ArrayContentProvider());
		comboViewer.setLabelProvider(new LabelProvider());
		comboViewer.setInput(getImageMode().getValues());
		comboViewer.setSelection(new StructuredSelection(getImageMode()));
		comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				// Change display mode
				setImageMode((HMImageMode) ((IStructuredSelection) event.getSelection()).getFirstElement());
				// Update NOW
				Job job = new Job(HMImageDisplayWidget.class.getName()) {
					protected IStatus run(IProgressMonitor monitor) {
						try {
							// Get data (one off)
							pullData();
						} catch (Exception e) {
							return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
									"Failed to fetch data.", e);
						}
						return Status.OK_STATUS;
					}			
				};
				job.setSystem(true);
				job.schedule();
			}
		});
		
		// Separator
		label = getToolkit().createLabel(this, "");
		GridDataFactory.swtDefaults().hint(8, SWT.DEFAULT).applyTo(label);
		
		Label scaleLabel = getToolkit().createLabel(this, "Scale: ");
		scaleLabel.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).grab(true, false).applyTo(scaleLabel);
		ComboViewer scaleViewer = new ComboViewer(this, SWT.READ_ONLY);
		scaleViewer.setContentProvider(new ArrayContentProvider());
		scaleViewer.setLabelProvider(new LabelProvider());
		scaleViewer.setInput(SCALE_INPUT);
		isScaleEnabled = true;
		try {
			isScaleEnabled = Boolean.valueOf(System.getProperty(IS_SCALE_ENABLED));
		} catch (Exception e) {
		}
		scaleViewer.setSelection(new StructuredSelection(SCALE_INPUT[isScaleEnabled ? 0 : 1]));
		scaleViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				// Change display mode
				setScaleEnabled(SCALE_INPUT[0].equals(((IStructuredSelection) event.getSelection()).getFirstElement()));
				// Update NOW
				Job job = new Job(HMImageDisplayWidget.class.getName()) {
					protected IStatus run(IProgressMonitor monitor) {
						try {
							// Get data (one off)
							pullData();
						} catch (Exception e) {
							return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
									"Failed to fetch data.", e);
						}
						return Status.OK_STATUS;
					}			
				};
				job.setSystem(true);
				job.schedule();
			}
		});
				
		label = getToolkit().createLabel(this, "");
		GridDataFactory.swtDefaults().hint(8, SWT.DEFAULT).applyTo(label);
		
		// Refresh
		label = getToolkit().createLabel(this, "Refresh: ");
		label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		final Text refreshText = getToolkit().createText(this, "");
		Realm.runWithDefault(DisplayRealm.getRealm(Display.getDefault()), new Runnable() {
			public void run() {
				DataBindingContext bindingContext = new DataBindingContext();
				bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(refreshText), 
						BeanProperties.value("refreshDelay").observe(HMImageDisplayWidget.this), 
						new UpdateValueStrategy(), new UpdateValueStrategy());
			}
		});
		GridDataFactory.swtDefaults().hint(20, SWT.DEFAULT).applyTo(refreshText);
		label = getToolkit().createLabel(this, "sec ");
		label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		
		Composite imageArea = getToolkit().createComposite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(0, 0).spacing(0, 0).applyTo(imageArea);
		GridDataFactory.fillDefaults().grab(true, true).span(9, 1).applyTo(imageArea);	
		
		return imageArea;
	}

	public HMImageMode getImageMode() {
		if (imageMode == null) {
			setImageMode(DefaultHMImageMode.values()[0]);
		}
		return imageMode;
	}
	
	public void setImageMode(HMImageMode imageMode) {
		this.imageMode = imageMode;
	}
	
	public String getDataURI() {
		return super.getDataURI() + getImageMode().getQuery() + getScaleEnabledQuery();
	}
	
	@Inject
	public void setDataAccessManager(IDataAccessManager dataAccessManager) {
		super.setDataAccessManager(dataAccessManager);
	}
	
	@Override
	public void update() {
		super.update();
	}
	
	@Override
	public void redraw() {
		// TODO Auto-generated method stub
		super.redraw();
	}
	
	@Override
	public void layout() {
		// TODO Auto-generated method stub
		super.layout();
	}

	public boolean isScaleEnabled() {
		return isScaleEnabled;
	}

	public void setScaleEnabled(boolean isScaleEnabled) {
		this.isScaleEnabled = isScaleEnabled;
	}
	
	public String getScaleEnabledQuery() {
		return "&open_annotations=" + SCALE_INPUT[isScaleEnabled ? 0 : 1];
	}
	
	public void setImageModeOptions(HMImageMode[] modes) {
		if (comboViewer != null) {
			comboViewer.setInput(modes);
		}
	}
}
