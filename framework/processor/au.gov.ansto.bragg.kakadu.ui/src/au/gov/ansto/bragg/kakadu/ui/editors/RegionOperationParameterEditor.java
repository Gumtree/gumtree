/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.ui.editors;


import java.awt.geom.Rectangle2D;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.gumtree.data.Factory;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.vis.mask.Abstract2DMask;
import org.gumtree.vis.mask.AbstractMask;

import au.gov.ansto.bragg.datastructures.core.region.RegionFactory;
import au.gov.ansto.bragg.kakadu.core.data.OperationParameter;
import au.gov.ansto.bragg.kakadu.ui.Activator;
import au.gov.ansto.bragg.kakadu.ui.region.RegionEventListener;
import au.gov.ansto.bragg.kakadu.ui.region.RegionParameter;
import au.gov.ansto.bragg.kakadu.ui.region.RegionParameterManager;
import au.gov.ansto.bragg.kakadu.ui.util.DisplayManager;
import au.gov.ansto.bragg.kakadu.ui.views.MaskPropertiesView;

public class RegionOperationParameterEditor extends TextOperationParameterEditor {

	private Text regionText;
	private String regionString;
	private Object value;
	private Button editButton;
	private RegionEventListener regionListener;
	private RegionParameter regionParameter;
	
	public RegionOperationParameterEditor(OperationParameter operationParameter,
			Composite parentComposite) {
		super(operationParameter, parentComposite);
	}

	protected Control createEditor() {
		Composite editorComposite =	new Composite(parentComposite, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.horizontalSpacing = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		editorComposite.setLayout(gridLayout);
		
		regionText = new Text(editorComposite, SWT.BORDER);
		regionText.setEditable(false);

		//to register data modifications
//		regionText.addModifyListener(this);
		
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.widthHint = 55;
		regionText.setLayoutData (data);

		editButton = new Button(editorComposite, SWT.PUSH);
		editButton.setText(">");
		editButton.setToolTipText("Click to open the window of region control");
		editButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				//show Mask Properties view
				IWorkbenchWindow workbenchWindow = Activator.getDefault()
						.getWorkbench().getActiveWorkbenchWindow();
				final IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
				MaskPropertiesView maskPropertiesView = null;
				try {
					maskPropertiesView = (MaskPropertiesView) workbenchPage.showView("au.gov.ansto.bragg.kakadu.ui.views.MaskPropertiesView");
				} catch (PartInitException ex) {
					ex.printStackTrace();
					return;
				}

//				maskPropertiesView.set(
//						((RegionOperationParameter)operationParameter).getParameterRegionManager());
				
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		return editorComposite;
	}
	
	public void setOperationParameter(OperationParameter operationParameter) {
//		if (this.operationParameter != null) {
//			((RegionOperationParameter) this.operationParameter ).getParameterRegionManager()
//			.removeRegionListener(getRegionListener());
//		}
//		
//		super.setOperationParameter(operationParameter);
//		
//		if (this.operationParameter != null) {
//			((RegionOperationParameter) this.operationParameter ).getParameterRegionManager()
//			.addRegionListener(getRegionListener());
//		}
		
		super.setOperationParameter(operationParameter);
	}


	public RegionEventListener getRegionListener() {
		if (regionListener == null) {
			regionListener = new RegionEventListener() {

				@Override
				public void maskUpdated(AbstractMask mask) {
					dataUpdated();
				}

				@Override
				public void maskAdded(AbstractMask mask) {
					dataUpdated();					
				}

				@Override
				public void maskRemoved(AbstractMask mask) {
					dataUpdated();			
				}
				
			};
		}
		return regionListener;
	}

	@Override
	public void initData() {
		Object newValue = operationParameter.getValue();
		if (value != newValue) {
			value = newValue;
			boolean hasMask = false;
			if (value != null) {
				if (value instanceof IGroup) {
					List<AbstractMask> masks = RegionParameterManager.convertToUIObject((IGroup) value);
					if (masks != null && masks.size() > 0) {
						regionParameter.addMasks(masks);
						regionString = createRegionString(masks);
						hasMask = true;
					}
				}
			} 
			if (!hasMask){
				regionString = "";
			}
			regionText.setText(regionString);
		}
	}
	
	public void loadData() {
//		Object newValue = operationParameter.getValue();
//		if (value != newValue) {
//			value = newValue;
//			if (value != null) {
//				if (value instanceof Group) {
//					List<AbstractMask> masks = RegionParameterManager.convertToUIObject((Group) value);
//					List<AbstractMask> maskList = regionParameter.getMaskList();
//					if (areEqual(masks, maskList2)) {
//
//					}
//				}
//			} else {
//				regionText.setText("");
//			}
//		}
		if (regionParameter.getMaskList() != null && regionParameter.getMaskList().size() > 0) {
			
		regionString = createRegionString(regionParameter.getMaskList());
		regionText.setText(regionString);
		} else {
			initData();
		}
		
	}
	
	private String createRegionString(List<AbstractMask> masks) {
		String text = "";
		for (AbstractMask mask : masks) {
			text += mask.getName();
			if (mask instanceof Abstract2DMask) {
				Rectangle2D region = ((Abstract2DMask) mask).getRectangleFrame();
				text += (mask.isInclusive() ? "[" : "(")
					+ formatString(region.getMinX()) + "," 
					+ formatString(region.getMinY()) + ";" 
				 	+ formatString(region.getMaxX()) + "," 
				 	+ formatString(region.getMaxY()) 
				 	+ (mask.isInclusive() ? "];" : ");");
			}
		}
		if (text.trim().equals("")) {
			text = "<no mask>";
		}
		return text;
	}

	private String formatString(double value) {
		return String.format("%.1f", value);
	}
	
	protected Object getEditorData() {
		try {
			return createRegionSet(regionParameter.getMaskList());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private IGroup createRegionSet(Object value) throws Exception {
		if (value instanceof List) {
			List<AbstractMask> maskList = (List<AbstractMask>) value;

			return RegionParameterManager.createRegionSet(maskList);
		}
		IGroup regionSet = RegionFactory.createRegionSet(
				Factory.createEmptyDatasetInstance().getRootGroup(), "regionSet");
		return regionSet;
	}

	protected void dispose() {
		super.dispose();
		if (regionParameter != null) {
			regionParameter.removeRegionListener(regionListener);
			regionListener = null;
			regionParameter = null;
		}
//		if (this.operationParameter != null) {
//			((RegionOperationParameter) this.operationParameter ).getParameterRegionManager().removeRegionListener(getRegionListener());
//		}
		
		if (operationParameter != null) {
			
		}
	}
	
	public void addApplyParameterListener(SelectionListener selectionListener) {
		regionText.addSelectionListener(selectionListener);
	}

	public void setRegionParameter(RegionParameter regionParameter) {
		this.regionParameter = regionParameter;
		regionParameter.addRegionListener(getRegionListener());
	}

	@Override
	protected void dataUpdated() {
		String newString = createRegionString(regionParameter.getMaskList());
		if (!newString.equals(regionString)) {
			regionString = newString;
			DisplayManager.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					regionText.setText(regionString);
					superDataUpdated();
				}
			});
//			super.dataUpdated();
		}
	}
	
	private void superDataUpdated() {
		super.dataUpdated();
	}
}
