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
package au.gov.ansto.bragg.kakadu.ui.views.mask;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.vis.mask.Abstract2DMask;
import org.gumtree.vis.mask.AbstractMask;
import org.gumtree.vis.mask.RectangleMask;

import au.gov.ansto.bragg.kakadu.ui.region.RegionEventListener;
import au.gov.ansto.bragg.kakadu.ui.region.RegionParameter;
import au.gov.ansto.bragg.kakadu.ui.region.RegionParameterManager;
import au.gov.ansto.bragg.kakadu.ui.util.DisplayManager;

/**
 * Composite for UI components for Mask Properties managing.
 * 
 * @author Danil Klimontov (dak)
 */
public class MaskPropertiesComposite extends Composite {

	private List maskList;
	private RectangularMaskEditor maskEditor;
	private java.util.List<AbstractMask> regions;
	private AbstractMask selectedRegion;
	private RegionParameter currentRegionParameter;

	private RegionEventListener regionListener = new RegionEventListener() {
		public void maskAdded(final AbstractMask region) {
			
			regions.add(region);
			DisplayManager.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					if (maskList.indexOf(region.getName()) < 0) {
						maskList.add(region.getName());
					}
				}
			});
			fireActionStateChangeListeners();

		}
		public void maskRemoved(final AbstractMask region) {
			if (selectedRegion == region) {
				setSelectedRegion(null);
			}
			DisplayManager.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					if (maskList.indexOf(region.getName()) >= 0) {
						maskList.remove(region.getName());
					}
				}
			});
			
//			int index = RegionUtil.getRegionIndex(regions, region);
////			final int index = regions.indexOf(region);
//			if (index >= 0) {
//				maskList.remove(index);
//				regions.remove(index);
//			}
			
			fireActionStateChangeListeners();
		}
		public void maskUpdated(final AbstractMask region) {
			DisplayManager.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {

					if (selectedRegion == region) {
						//all current changes will be ignored
						setSelectedRegion(region);
					}
					//			int index = RegionUtil.getRegionIndex(regions, region);
					final int index = regions.indexOf(region);
					if (index >= 0) {
						maskList.setItem(index, region.getName());
					}

					fireActionStateChangeListeners();
				}
			});
		}
	};
	private RegionParameterManager regionManager;
	private java.util.List<RegionParameter> regionParameterList;
	private Combo currentParameterCombo;

	public MaskPropertiesComposite(Composite parent, int style) {
		super(parent, style);
		initialise();
	}

	protected void initialise() {
		GridLayout gridLayout = new GridLayout ();
		gridLayout.marginHeight = 3;
		gridLayout.marginWidth = 3;
		gridLayout.horizontalSpacing = 3;
		gridLayout.verticalSpacing = 3;
		gridLayout.numColumns = 2;
		setLayout (gridLayout);

		Label parameterTitleLabel = new Label (this, SWT.NONE);
		parameterTitleLabel.setText("Parameter");
		GridData data = new GridData ();
		parameterTitleLabel.setLayoutData (data);

		currentParameterCombo = new Combo(this, SWT.SINGLE | SWT.READ_ONLY);
		currentParameterCombo.setEnabled(false);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		currentParameterCombo.setLayoutData (data);
		
		Label titleLabel = new Label (this, SWT.NONE);
		titleLabel.setText("Mask list:");
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 2;
		titleLabel.setLayoutData (data);
		
		maskList = new List (this, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.horizontalSpan = 2;
		maskList.setLayoutData (data);

		Composite maskEditorGroup = new Composite (this, SWT.NONE);
//		maskEditorGroup.setText ("Mask Properties");
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 2;
		maskEditorGroup.setLayoutData (data);
		maskEditorGroup.setLayout(new FillLayout());
		
		maskEditor = new RectangularMaskEditor(maskEditorGroup, SWT.NONE);
		
		initListeners();
	}

	private void initListeners() {
		maskList.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				int selectionIndex = maskList.getSelectionIndex();
				if (selectionIndex >= 0 && regions != null) {

					//save current mask changes
					if (maskEditor.isChanged() && cofirmApplyChangesAction()) {
						maskEditor.applyChanges();
					}
					
					AbstractMask mask = (AbstractMask) regions.get(selectionIndex);
					setSelectedRegion(mask);
				}
			}
		});
		
		currentParameterCombo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			
			public void widgetSelected(SelectionEvent e) {
				final RegionParameter parameterRegionManager = regionParameterList.get(currentParameterCombo.getSelectionIndex());
				setRegionParameter(parameterRegionManager);
			}
		});
		
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				disposeUIComonents();
			}
		});
	}
	
	protected void setSelectedRegion(AbstractMask region) {
		selectedRegion = region;
		maskEditor.setRegion(selectedRegion);
		if (selectedRegion != null) {
			//					int index = RegionUtil.getRegionIndex(regions, region);
			int index = regions.indexOf(selectedRegion);
			if (index >= 0) {
				maskList.select(index);
			} else {
				maskList.deselectAll();
			}
		} else {
			maskList.deselectAll();
		}

		fireActionStateChangeListeners();


	}

	public AbstractMask getSelectedRegion() {
		return selectedRegion;
	}
	
	public void setRegions(java.util.List<AbstractMask> regions) {
		this.regions = regions;
		
		AbstractMask oldSelectedRegion = selectedRegion; 
		
		maskList.removeAll();
		setSelectedRegion(null);
		if (regions != null) {
			for (AbstractMask region : regions) {
				maskList.add(region.getName());
			}
			
			//restore selection
			if (regions.indexOf(oldSelectedRegion) >= 0) {
				setSelectedRegion(oldSelectedRegion);
			}
		}

		
	}

	public void setRegionParameter(RegionParameter regionParameter) {
		if (currentRegionParameter == regionParameter) {
			return;
		}

		checkMaskChanged();
		
		if (currentRegionParameter != null) {
			currentRegionParameter.removeRegionListener(regionListener);
		}
		currentRegionParameter = regionParameter;
		
		if (currentRegionParameter != null) {
			currentRegionParameter.addRegionListener(regionListener);
			setRegions(currentRegionParameter.getMaskList());
			
			final int index = regionParameterList.indexOf(currentRegionParameter);
			if (index >= 0) {
				currentParameterCombo.select(index);
			}
		} else {
			setRegions(null);
		}
		
		maskEditor.setRegionParameter(currentRegionParameter);
		

//		if (regionManager != null) {
//			regionManager.setLastEditableParameterRegionManager(parameterRegionManager);
//		}
		
		
		fireActionStateChangeListeners();

	}
	
	public RegionParameter getCurrentParameterRegionManager() {
		return currentRegionParameter;
	}
	
	private boolean cofirmApplyChangesAction() {
		return MessageDialog.openConfirm(
				getShell(),
				"Mask Editor",
				"Mask has been changed. Do you want to save changes before continue? Otherwise all the changes will be ignored.");

	}

	private void disposeUIComonents() {
		if (this.currentRegionParameter != null) {
			this.currentRegionParameter.removeRegionListener(regionListener);
		}
	}

	public void setRegionManager(RegionParameterManager regionManager) {
		this.regionManager = regionManager;
		loadParametersList();
		if (regionManager != null && regionManager.getParameterList().size() > 0) {
			setRegionParameter(regionManager != null ? regionManager.getParameterList().get(0) : null);
		}
		
		
//		fireActionStateChangeListeners();
	}

	private void loadParametersList() {
		currentParameterCombo.removeAll();
		if (regionManager != null) {
			regionParameterList = regionManager.getParameterList();
			for (RegionParameter parameter : regionParameterList) {
				currentParameterCombo.add(parameter.getOperation().getUILabel()
						+ " - " + parameter.getName());
			}
		} else {
			regionParameterList = null;
		}
		currentParameterCombo.setEnabled(currentParameterCombo.getItemCount() > 0);
	}

	private void checkMaskChanged() {
		//save mask changes
		if (maskEditor.isChanged() && cofirmApplyChangesAction()) {
			maskEditor.applyChanges();
		}
	}

	public void createNewMask() {
		final Abstract2DMask region = new RectangleMask(true, 0, 0, 0, 0);
//		region.setName("Mask " + region.getId());
		
		currentRegionParameter.addMask(region);
		setSelectedRegion(region);
	}
	
	public void addNewMask(IGroup regionSet) {
//		final UIRegion region = new UIRegion(true, 0, 0, 0, 0);
		java.util.List<AbstractMask> regionList = RegionParameterManager.convertToUIObject(regionSet);
		for (AbstractMask region : regionList){
//			if (region.isInclusive())
//				region.setName("Region " + UIRegion.nextId());
//			else 
//				region.setName("Mask " + UIRegion.nextId());
			currentRegionParameter.addMask(region);
		}
//		setSelectedRegion(region);
	}

	public void removeSelectedMask() {
		if (selectedRegion != null
				&& confirmMaskRemoval() ) {
			
			currentRegionParameter.removeMask(selectedRegion);
			
			fireActionStateChangeListeners();
		}
	}
	
	public void removeAllMasks() {
		if (regions.size() > 0
				&& currentRegionParameter != null
				&& confirmAllMasksRemoval()) {
			
			currentRegionParameter.clearMask();
			
			fireActionStateChangeListeners();
			
		}
	}


	private boolean confirmAllMasksRemoval() {
		return MessageDialog.openConfirm(getShell(), "Mask Properties", "Are you sure you want to remove ALL masks from the paremeter '" + currentParameterCombo.getText() + "'?");
	}

	private boolean confirmMaskRemoval() {
		return MessageDialog.openConfirm(getShell(), "Mask Properties", "Are you sure you want to remove mask '" + selectedRegion.toString() + "'?");
	}
	
	
	private final java.util.List<ActionStateChangeListener> actionStateChangeListeners = new ArrayList<ActionStateChangeListener>();
	
	public void addActionStateChangeListener(ActionStateChangeListener listener) {
		actionStateChangeListeners.add(listener);
	}
	
	public void removeActionStateChangeListener(ActionStateChangeListener listener) {
		actionStateChangeListeners.remove(listener);
	}

	protected void fireActionStateChangeListeners() {
		for (ActionStateChangeListener listener : actionStateChangeListeners) {
			listener.updateActionsState();
		}
	}
	public static interface ActionStateChangeListener {
		
		void updateActionsState();
	}
	
	public java.util.List<AbstractMask> getRegionList(){
		return regions;
	}
	
	public List getControl(){
		return maskList;
	}
}
