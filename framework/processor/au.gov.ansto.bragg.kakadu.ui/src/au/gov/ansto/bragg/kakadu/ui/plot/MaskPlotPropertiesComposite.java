/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *     Norman Xiong (Bragg Institute) - implementation and bug fixing
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.ui.plot;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import au.gov.ansto.bragg.kakadu.core.AlgorithmTask;
import au.gov.ansto.bragg.kakadu.core.data.Operation;
import au.gov.ansto.bragg.kakadu.ui.ProjectManager;
import au.gov.ansto.bragg.kakadu.ui.SWTResourceManager;
import au.gov.ansto.bragg.kakadu.ui.region.RegionParameter;
import au.gov.ansto.bragg.kakadu.ui.region.RegionParameterManager;

/**
 * UI controls for mask properties for plot.
 * 
 * @author Danil Klimontov (dak)
 * @author nxi
 */
public class MaskPlotPropertiesComposite extends Composite {

	private Button maskEnabledButton;
	private Combo operationParameterCombo;
	private final List<RegionParameter> regionParameterList = new ArrayList<RegionParameter>();
	private final Plot plot;
	private boolean isInitialised = false;


	/**
	 * @param parent
	 * @param style
	 */
	public MaskPlotPropertiesComposite(Composite parent, int style, Plot plot) {
		super(parent, style);
		this.plot = plot;
		SWTResourceManager.registerResourceUser(parent);
		initialise();
		initListeners();

		plot.getMultiPlotDataManager().addMultiPlotDataListener(new MultiPlotDataListener() {
			public void itemAdded(PlotDataItem plotDataItem) {
				loadMaskParameterManagers();
			}
			public void itemRemoved(PlotDataItem plotDataItem) {
				loadMaskParameterManagers();
			}
			public void itemUpdated(PlotDataItem plotDataItem) {
				loadMaskParameterManagers();
			}
		});
	}

	protected void initialise() {

		GridLayout gridLayout = new GridLayout ();
		gridLayout.marginHeight = 3;
		gridLayout.marginWidth = 3;
		gridLayout.horizontalSpacing = 3;
		gridLayout.verticalSpacing = 3;
		setLayout (gridLayout);

		maskEnabledButton = new Button(this, SWT.CHECK);
		maskEnabledButton.setText("Mask enabled");
		GridData data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		data.grabExcessHorizontalSpace = true;
		maskEnabledButton.setLayoutData (data);

		operationParameterCombo = new Combo(this, SWT.READ_ONLY);
		operationParameterCombo.setEnabled(false);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		data.grabExcessHorizontalSpace = true;
		operationParameterCombo.setLayoutData (data);
	}

	protected void initListeners() {
		maskEnabledButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				setMaskEnabled(maskEnabledButton.getSelection());
			}
		});
		operationParameterCombo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				final int selectionIndex = operationParameterCombo.getSelectionIndex();
				processParameterRegionManagerSelection(selectionIndex);
			}
		});
	}

	private void loadParametersList() {
		operationParameterCombo.removeAll();

		for (RegionParameter parameter: regionParameterList) {
			Operation operation = parameter.getOperation();
			operationParameterCombo.add("T" + operation.getAlgorithmTaskId() 
					+ "." + operation.getUILabel()
					+ " - " + parameter.getName());
		}

		final int selectedManager = regionParameterList.indexOf(plot.getRegionParameter());

		if (selectedManager >= 0) {
			operationParameterCombo.select(selectedManager);
		}
		final boolean maskingPossible = operationParameterCombo.getItemCount() > 0;
		maskEnabledButton.setEnabled(maskingPossible);
//		setMaskEnabled(maskingPossible);
//		if (maskingPossible) enableMask();
	}

	public void initMaskEnabled(){
		if (! isInitialised){
			final boolean maskingPossible = operationParameterCombo.getItemCount() > 0;
			setMaskEnabled(maskingPossible);
			isInitialised = true;
		}
	}

	public void enableMask(){
		if (regionParameterList.size() > 0) {
			operationParameterCombo.setEnabled(true);
			final int selectionIndex = operationParameterCombo.getSelectionIndex();
			if (selectionIndex == -1) {
				operationParameterCombo.select(0);
				processParameterRegionManagerSelection(0);
			}
			maskEnabledButton.setSelection(true);
			plot.enableMask();
		} else {
			operationParameterCombo.setEnabled(false);
			operationParameterCombo.setToolTipText("<no mask parameters available>");
		}
	}
	/**
	 * Sets mask managing enable status.
	 * @param maskEnabled <code>true</code> if enabled or <code>false</code> otherwise.
	 */
	public void setMaskEnabled(boolean maskEnabled) {
		maskEnabledButton.setSelection(maskEnabled);
		if (maskEnabled) {
			if (regionParameterList.size() > 0) {
				operationParameterCombo.setEnabled(true);
				final int selectionIndex = operationParameterCombo.getSelectionIndex();
				if (selectionIndex == -1) {
					operationParameterCombo.select(0);
					processParameterRegionManagerSelection(0);
				}
				plot.setMaskEnabled(true);
			} else {
				operationParameterCombo.setEnabled(false);
				operationParameterCombo.setToolTipText("<no mask parameters available>");
			}
		} else {
			operationParameterCombo.setEnabled(false);
			plot.setMaskEnabled(false);
		}
	}

	public void loadMaskParameterManagers() {
		regionParameterList.clear();

		final MultiPlotDataManager multiPlotDataManager = plot.getMultiPlotDataManager();
		for (PlotDataItem plotDataItem : multiPlotDataManager.getPlotDataItems()) {
			final PlotDataReference plotDataReference = plotDataItem.getPlotDataReference();
			if (plotDataReference != null && plotDataItem.isLinked() &&  plotDataItem.isLinkEnabled()) {
				final AlgorithmTask algorithmTask = ProjectManager.getAlgorithmTask(plotDataReference.getTaskId());
				if (algorithmTask != null) {
//					final RegionManager rManager = algorithmTask.getRegionManager();
//					for (ParameterRegionManager parameterRegionManager : rManager.getParameterRegionManagerList()) {
//						if (!parameterRegionManagerList.contains(parameterRegionManager)) {
//							parameterRegionManagerList.add(parameterRegionManager);
//						}
//					}
					Object manager = algorithmTask.getRegionParameterManager();
					if (manager != null && manager instanceof RegionParameterManager) {
						RegionParameterManager regionParameterManager = (RegionParameterManager) manager;
						for (RegionParameter parameter : regionParameterManager.getParameterList()) {
							regionParameterList.add(parameter);
						}
					}
				}
			}
		}
		loadParametersList();
	}

	/**
	 * @param selectionIndex
	 */
	private void processParameterRegionManagerSelection(final int selectionIndex) {
		operationParameterCombo.setToolTipText(operationParameterCombo.getItem(selectionIndex));
//		final ParameterRegionManager parameterRegionManager = parameterRegionManagerList.get(selectionIndex);
		RegionParameter parameter = regionParameterList.get(selectionIndex);
		plot.setRegionParameter(parameter);
	}

}
