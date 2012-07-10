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
package au.gov.ansto.bragg.kakadu.ui.plot;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.gumtree.vis.interfaces.IPlot;
import org.gumtree.vis.interfaces.IPlot1D;

import au.gov.ansto.bragg.kakadu.core.data.DataType;

/**
 * The composite for view properties section of plot.
 * 
 * @author Danil Klimontov (dak)
 */
public class ViewPlotPropertiesComposite extends Composite {

	private final Plot plot;
	private Button logXAxisButton;
	private Button logYAxisButton;
	private Button flipXAxisButton;
	private Button showErrorButton;
	private Button schemaModeButton;
	private Button showMetadataButton;
	private Button intensityModeButton;
	private Button offsetModeButton;
	private Button overlayModeButton;
	private Button surfaceModeButton;
	private Label offsetLabel;
	private OffsetValueEditorComposite offsetEditorComposite;
	private Combo intensityPlotListCombo;
	private List<PlotDataItem> visiblePlotDataItems;
	private Button generatedTitleModeButton;
	private Button includeReferenceButton;
	private Button customTitleModeButton;
	private Text customTitleText;

	/**
	 * Creates new instance of <code>ViewPlotPropertiesComposite</code>. 
	 * @param parent
	 * @param style
	 * @param plot linked plot.
	 */
	public ViewPlotPropertiesComposite(Composite parent, int style, Plot plot) {
		super(parent, style);
		this.plot = plot;
		
		initialise();
	}

	protected void initialise() {
		GridLayout gridLayout = new GridLayout ();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 3;
		gridLayout.marginWidth = 3;
		gridLayout.horizontalSpacing = 3;
		gridLayout.verticalSpacing = 3;
		setLayout (gridLayout);

		/*********************************************************************
		 * View mode group
		 *********************************************************************/
		final Group viewModeGroup = new Group(this, SWT.NONE);
		viewModeGroup.setText("View mode");
		GridData data = new GridData ();
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		viewModeGroup.setLayoutData (data);
		GridLayout viewModeGroupGridLayout = new GridLayout ();
		viewModeGroupGridLayout.numColumns = 2;
		viewModeGroupGridLayout.marginHeight = 2;
		viewModeGroupGridLayout.marginWidth = 2;
		viewModeGroupGridLayout.horizontalSpacing = 2;
		viewModeGroupGridLayout.verticalSpacing = 2;
		viewModeGroup.setLayout(viewModeGroupGridLayout);
		
		offsetModeButton = new Button(viewModeGroup, SWT.RADIO);
		offsetModeButton.setText("Offset");
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 2;
		offsetModeButton.setLayoutData (data);

		overlayModeButton = new Button(viewModeGroup, SWT.RADIO);
		overlayModeButton.setText("Overlay");
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 2;
		overlayModeButton.setLayoutData (data);

		intensityModeButton = new Button(viewModeGroup, SWT.RADIO);
		intensityModeButton.setText("Intensity");
		data = new GridData ();
//		data.horizontalAlignment = GridData.FILL;
//		data.grabExcessHorizontalSpace = true;
		intensityModeButton.setLayoutData (data);
		
		intensityPlotListCombo = new Combo(viewModeGroup, SWT.READ_ONLY);
		intensityPlotListCombo.setVisibleItemCount(10);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		intensityPlotListCombo.setLayoutData (data);
		
		surfaceModeButton = new Button(viewModeGroup, SWT.RADIO);
		surfaceModeButton.setText("Surface");
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 2;
		surfaceModeButton.setLayoutData (data);
		
		/*********************************************************************
		 * Log axis group
		 *********************************************************************/
		Group logAxisGroup = new Group(this, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1).applyTo(logAxisGroup);
		logAxisGroup.setText("Log Axis");
		logAxisGroup.setLayout(new GridLayout(2, true));
		logYAxisButton = new Button(logAxisGroup, SWT.CHECK);
		logYAxisButton.setText("Y Axis");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(logYAxisButton);
		logXAxisButton = new Button(logAxisGroup, SWT.CHECK);
		logXAxisButton.setText("X Axis");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(logXAxisButton);
		IPlot plotWidget = plot.getCurrentPlotWidget();
		if (plotWidget != null && plotWidget instanceof IPlot1D) {
			logYAxisButton.setSelection(((IPlot1D) plotWidget).isLogarithmYEnabled());
			logXAxisButton.setSelection(((IPlot1D) plotWidget).isLogarithmXEnabled());
		}
		
		/*********************************************************************
		 * View options
		 *********************************************************************/
		flipXAxisButton = new Button(this, SWT.CHECK);
		flipXAxisButton.setText("Flip X-Axis");
		data = new GridData ();
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		flipXAxisButton.setLayoutData (data);

		showErrorButton = new Button(this, SWT.CHECK);
		showErrorButton.setText("Show Error Data");
		data = new GridData ();
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		showErrorButton.setLayoutData (data);

		
		schemaModeButton = new Button(this, SWT.CHECK);
		schemaModeButton.setText("Table mode");
		data = new GridData ();
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		schemaModeButton.setLayoutData (data);
		
		showMetadataButton = new Button(this, SWT.CHECK);
		showMetadataButton.setText("Statistic Data");
		data = new GridData ();
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		showMetadataButton.setLayoutData (data);

		//Offset value editors
		offsetLabel = new Label(this, SWT.NONE);
		offsetLabel.setText("Offset");
		
		
		offsetEditorComposite = new OffsetValueEditorComposite(this, SWT.NONE);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		offsetEditorComposite.setLayoutData (data);

		/*********************************************************************
		 * Title group
		 *********************************************************************/
		final Group titleGroup = new Group(this, SWT.NONE);
		titleGroup.setText("Title mode");
		data = new GridData ();
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		titleGroup.setLayoutData (data);
		GridLayout titleGroupGridLayout = new GridLayout ();
		titleGroupGridLayout.numColumns = 2;
		titleGroupGridLayout.marginHeight = 2;
		titleGroupGridLayout.marginWidth = 2;
		titleGroupGridLayout.horizontalSpacing = 2;
		titleGroupGridLayout.verticalSpacing = 2;
		titleGroup.setLayout(titleGroupGridLayout);
				
		
		generatedTitleModeButton = new Button(titleGroup, SWT.RADIO);
		generatedTitleModeButton.setText("Generated");
		data = new GridData ();
		generatedTitleModeButton.setLayoutData (data);
		generatedTitleModeButton.setSelection(true);
		
		includeReferenceButton = new Button(titleGroup, SWT.CHECK);
		includeReferenceButton.setText("Include reference");
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		includeReferenceButton.setLayoutData (data);
		includeReferenceButton.setSelection(true);

		customTitleModeButton = new Button(titleGroup, SWT.RADIO);
		customTitleModeButton.setText("Custom");
		data = new GridData ();
		customTitleModeButton.setLayoutData (data);
		
		customTitleText = new Text(titleGroup, SWT.BORDER);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		customTitleText.setLayoutData (data);
		
		
		initListeners();
		
	}
	
	private void initListeners() {
		logYAxisButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IPlot plotWidget = plot.getCurrentPlotWidget();
				if (plotWidget != null && plotWidget instanceof IPlot1D) {
					try {
						((IPlot1D) plotWidget).setLogarithmYEnabled(logYAxisButton.getSelection());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		
		logXAxisButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IPlot plotWidget = plot.getCurrentPlotWidget();
				if (plotWidget != null && plotWidget instanceof IPlot1D) {
					try {
						((IPlot1D) plotWidget).setLogarithmXEnabled(logXAxisButton.getSelection());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		
		flipXAxisButton.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				setXAxisFlip(flipXAxisButton.getSelection());
			}
		});
		showErrorButton.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				setShowError(showErrorButton.getSelection());
			}
		});

		schemaModeButton.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				plot.setSchemaMode(schemaModeButton.getSelection());
			}
		});
		
		showMetadataButton.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				plot.setMetadataMode(showMetadataButton.getSelection());
			}
		});

		final SelectionListener viewModeSelectionListener = new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource() == offsetModeButton && offsetModeButton.getSelection()) {
					plot.setPlotType(PlotType.OffsetPlot);
				} else if (e.getSource() == overlayModeButton && overlayModeButton.getSelection()) {
					plot.setPlotType(PlotType.OverlayPlot);
				} else if (e.getSource() == intensityModeButton && intensityModeButton.getSelection()) {
					plot.setPlotType(PlotType.IntensityPlot);
				} else if (e.getSource() == surfaceModeButton && surfaceModeButton.getSelection()) {
					plot.setPlotType(PlotType.SurfacePlot);
				}
			}
		};
		
		offsetModeButton.addSelectionListener(viewModeSelectionListener);
		overlayModeButton.addSelectionListener(viewModeSelectionListener);
		intensityModeButton.addSelectionListener(viewModeSelectionListener);
		surfaceModeButton.addSelectionListener(viewModeSelectionListener);

		intensityPlotListCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setSelectedIntensityPlot(intensityPlotListCombo.getSelectionIndex());
			}
		});

		plot.getMultiPlotDataManager().addMultiPlotDataListener(new MultiPlotDataListener() {
			public void itemAdded(PlotDataItem plotDataItem) {
				updateUI();
			}
			public void itemRemoved(PlotDataItem plotDataItem) {
				updateUI();
			}
			public void itemUpdated(PlotDataItem plotDataItem) {
				updateUI();
			}
		});
		
		final SelectionAdapter titleSelectionListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateTitle();
			}
		};

		generatedTitleModeButton.addSelectionListener(titleSelectionListener);
		includeReferenceButton.addSelectionListener(titleSelectionListener);
		customTitleModeButton.addSelectionListener(titleSelectionListener);
		
		customTitleText.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				updateTitle();
			}
		});
	}

	protected void updateTitle() {
		customTitleText.setEnabled(customTitleModeButton.getSelection());
		includeReferenceButton.setEnabled(generatedTitleModeButton.getSelection());
		
		plot.updateTitle();
	}
	
	public boolean isTitleIncludeReference() {
		return includeReferenceButton.getSelection();
	}
	
	public String getCustomTitle() {
		return customTitleText.getText();
	}
	
	public boolean isTitleGenerated() {
		return generatedTitleModeButton.getSelection();
	}

	protected void setSchemaMode(boolean isSchemaMode) {
		schemaModeButton.setSelection(isSchemaMode);
	}

	public void setXAxisFlip(boolean isFlipXAxis) {
		flipXAxisButton.setSelection(isFlipXAxis);
		updatePlot();
	}
	
	public boolean isXAxisFlip() {
		return flipXAxisButton.getSelection();
	}

	public void setShowError(boolean isShowError) {
		showErrorButton.setSelection(isShowError);
		updatePlot();
	}
	
	public boolean isShowError() {
		return showErrorButton.getSelection();
	}

	
	private void updatePlot() {
		try {
			IPlot plotWidget = plot.getCurrentPlotWidget();
			if (plotWidget != null && plotWidget instanceof IPlot1D) {
				((IPlot1D) plotWidget).setHorizontalAxisFlipped(flipXAxisButton.getSelection());
				((IPlot1D) plotWidget).setErrorBarEnabled(showErrorButton.getSelection());
			}
			plot.getCurrentPlotWidget().repaint();
		} catch (Exception e) {
			plot.handleException(e);
		}
		
	}

	public void setFlipAxiesEnabled(boolean enabled) {
		flipXAxisButton.setEnabled(enabled);
	}
	
	public void setSchemaModeEnabled(boolean enabled) {
		schemaModeButton.setEnabled(enabled);
	}

	public void setShowErrorEnabled(boolean enabled) {
		showErrorButton.setEnabled(enabled);
	}

	public void setLogYEnabled(boolean enabled){
		logYAxisButton.setEnabled(enabled);
	}
	
	public void setLogXEnabled(boolean enabled){
		logXAxisButton.setEnabled(enabled);
	}

	public void updateUI() {
		IPlot plotWidget = plot.getCurrentPlotWidget();
		switch (plot.getCurrentPlotType()) {
		default:
		case IntensityPlot:
			intensityModeButton.setSelection(true);
			offsetModeButton.setSelection(false);
			overlayModeButton.setSelection(false);
			surfaceModeButton.setSelection(false);
			intensityPlotListCombo.setEnabled(true);
			updateIntensityPlotList();
			break;
		case OffsetPlot:
			intensityModeButton.setSelection(false);
			offsetModeButton.setSelection(true);
			overlayModeButton.setSelection(false);
			surfaceModeButton.setSelection(false);
			intensityPlotListCombo.setEnabled(false);
			logYAxisButton.setSelection(((IPlot1D) plotWidget).isLogarithmYEnabled());
			logXAxisButton.setSelection(((IPlot1D) plotWidget).isLogarithmXEnabled());
			showErrorButton.setSelection(((IPlot1D) plotWidget).isErrorBarEnabled());
			clearIntensitiPlotList();
			break;
		case OverlayPlot:
			intensityModeButton.setSelection(false);
			offsetModeButton.setSelection(false);
			overlayModeButton.setSelection(true);
			surfaceModeButton.setSelection(false);
			intensityPlotListCombo.setEnabled(false);
			logYAxisButton.setSelection(((IPlot1D) plotWidget).isLogarithmYEnabled());
			logXAxisButton.setSelection(((IPlot1D) plotWidget).isLogarithmXEnabled());
			showErrorButton.setSelection(((IPlot1D) plotWidget).isErrorBarEnabled());
			clearIntensitiPlotList();
			break;
		case SurfacePlot:
			intensityModeButton.setSelection(false);
			offsetModeButton.setSelection(false);
			overlayModeButton.setSelection(false);
			surfaceModeButton.setSelection(true);
			intensityPlotListCombo.setEnabled(false);
			clearIntensitiPlotList();
			break;
		}
		
		switch (plot.getCurrentDataType()) {
		case Pattern:
		case PatternSet:
			offsetModeButton.setEnabled(true);
			overlayModeButton.setEnabled(true);
			intensityModeButton.setEnabled(false);
			surfaceModeButton.setEnabled(false);
			break;
		case Map:
		case MapSet:
			offsetModeButton.setEnabled(false);
			overlayModeButton.setEnabled(false);
			intensityModeButton.setEnabled(true);
			surfaceModeButton.setEnabled(false);
			break;
		case Calculation:
		case Undefined:
		default:
			offsetModeButton.setEnabled(false);
			overlayModeButton.setEnabled(false);
			intensityModeButton.setEnabled(false);
			surfaceModeButton.setEnabled(false);
			break;
		}
		
		flipXAxisButton.setSelection(plotWidget.isHorizontalAxisFlipped());
		updateOffsetValueFields();
		

	}

	/**
	 * Sets selected Intensity plot from the list of available plots.
	 */
	public void setSelectedIntensityPlot(int index) {
		PlotDataItem plotDataItem = null;
		if (index >= 0 && visiblePlotDataItems != null) {
			plotDataItem = visiblePlotDataItems.get(index);
			intensityPlotListCombo.select(index);
		}
		plot.getMultiPlotDataManager().setSingleItemVisible(plotDataItem);
		plot.updateTitle();
	}


	private void updateIntensityPlotList() {
		//remember selected item
		final int selectionIndex = intensityPlotListCombo.getSelectionIndex();
		PlotDataItem selectedPlotDataItem = null;
		if (selectionIndex >= 0 && visiblePlotDataItems != null) {
			selectedPlotDataItem = visiblePlotDataItems.get(selectionIndex);
		}
		
		//remove all and update enabling
		intensityPlotListCombo.removeAll();
		intensityPlotListCombo.setEnabled(
				(plot.getCurrentDataType() == DataType.Map || plot.getCurrentDataType() == DataType.MapSet)
				&& plot.getCurrentPlotType() == PlotType.IntensityPlot);

		
		if (intensityPlotListCombo.isEnabled()) {
			//load visible items
			visiblePlotDataItems = plot.getMultiPlotDataManager().getDisplayablePlotDataItems();
			
			for (PlotDataItem plotDataItem : visiblePlotDataItems) {
				if(plotDataItem.getChildrenCount() > 0) {
					continue;
				}
				String title = plotDataItem.getTitle();
				if (plotDataItem.getParent() != null) {
					//this is a child item
					PlotDataItem parentItem = plotDataItem.getParent();
					title += "[" + parentItem.getChildren().indexOf(plotDataItem) + "]";
				}
				intensityPlotListCombo.add(title + " - " + plotDataItem.getReferenceString());
			}
			//update selection
			if (selectedPlotDataItem != null) {
				final int indexToSelect = visiblePlotDataItems.indexOf(selectedPlotDataItem);
				if (indexToSelect >= 0) {
					setSelectedIntensityPlot(indexToSelect);
				} else {
					if (visiblePlotDataItems.size() > 0) {
						setSelectedIntensityPlot(0);
					}
				}
			} else {
				if (visiblePlotDataItems.size() > 0) {
					setSelectedIntensityPlot(0);
				}
			}
		} else {
			visiblePlotDataItems = null;
		}

	}
	
	private void clearIntensitiPlotList() {
		intensityPlotListCombo.removeAll();
		intensityPlotListCombo.setEnabled(false);
		visiblePlotDataItems = null;
	}

	/**
	 * Updates UI for Offset values.
	 */
	private void updateOffsetValueFields() {
		//update Offset value fields
		final boolean isOffsetModeSelected = offsetModeButton.getSelection();
		offsetEditorComposite.setEnabled(isOffsetModeSelected);

		if (plot.getCurrentPlotWidget() != null && isOffsetModeSelected) {
			offsetEditorComposite.loadLastOffsetValues();
			offsetEditorComposite.applyOffsetValues();
		} else if (plot.getCurrentPlotWidget() != null && overlayModeButton.getSelection()) {
			offsetEditorComposite.loadOverlayValues();
			offsetEditorComposite.applyOffsetValues();
		} else {
			offsetEditorComposite.clearValues();
		}
	}

	protected class OffsetValueEditorComposite extends Composite {
		private Text zOffsetValueText;
		private Text yOffsetValueText;
		private Text xOffsetValueText;

		protected boolean isChanged = false;
		
		final VerifyListener verifyListener = new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				try {
					final String text = ((Text)e.getSource()).getText();
					if (!"".equals(text)) {
						Integer.parseInt(text);
					}
					e.doit = true;
				} catch (NumberFormatException ex) {
					e.doit = false;
				}
			}
		};
		
		protected ModifyListener textModifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				isChanged = true;
				updateButtons();
			}
		};
		private Button applyButton;
		private int xOffsetValue;
		private int yOffsetValue;
		private int zOffsetValue;

		public OffsetValueEditorComposite(Composite parent, int style) {
			super(parent, style);
			initialize();
			initListeners();
		}

		private void initialize() {
			GridLayout offsetEditorCompositeGridLayout = new GridLayout ();
			offsetEditorCompositeGridLayout.numColumns = 7;
			offsetEditorCompositeGridLayout.marginHeight = 3;
			offsetEditorCompositeGridLayout.marginWidth = 3;
			offsetEditorCompositeGridLayout.horizontalSpacing = 3;
			offsetEditorCompositeGridLayout.verticalSpacing = 3;
			setLayout (offsetEditorCompositeGridLayout);

			Label label0 = new Label (this, SWT.NONE);
			label0.setText ("X");
			
			xOffsetValueText = new Text (this, SWT.BORDER);
			GridData data = new GridData ();
			data.horizontalAlignment = GridData.FILL;
			data.grabExcessHorizontalSpace = true;
			xOffsetValueText.setLayoutData (data);

			Label label2 = new Label (this, SWT.NONE);
			label2.setText ("Y");
			data = new GridData ();
			data.horizontalIndent = 5;
			label2.setLayoutData (data);

			yOffsetValueText = new Text (this, SWT.BORDER);
			data = new GridData ();
			data.horizontalAlignment = GridData.FILL;
			data.grabExcessHorizontalSpace = true;
			yOffsetValueText.setLayoutData (data);

			Label label4 = new Label (this, SWT.NONE);
			label4.setText ("Z");
			data = new GridData ();
			data.horizontalIndent = 5;
			label4.setLayoutData (data);

			zOffsetValueText = new Text (this, SWT.BORDER);
			data = new GridData ();
			data.horizontalAlignment = GridData.FILL;
			data.grabExcessHorizontalSpace = true;
			zOffsetValueText.setLayoutData (data);
			
			applyButton = new Button(this, SWT.PUSH);
			applyButton.setText("Apply");
			applyButton.setEnabled(false);

		}
		private void initListeners() {
			final SelectionAdapter applyOffsetListener = new SelectionAdapter(){
				public void widgetDefaultSelected(SelectionEvent e) {
					applyOffsetValues();
				}
				public void widgetSelected(SelectionEvent e) {
					applyOffsetValues();
				}
			};
			xOffsetValueText.addSelectionListener(applyOffsetListener);
			yOffsetValueText.addSelectionListener(applyOffsetListener);
			zOffsetValueText.addSelectionListener(applyOffsetListener);
			
			xOffsetValueText.addModifyListener(textModifyListener);
			yOffsetValueText.addModifyListener(textModifyListener);
			zOffsetValueText.addModifyListener(textModifyListener);

			xOffsetValueText.addVerifyListener(verifyListener);
			yOffsetValueText.addVerifyListener(verifyListener);
			zOffsetValueText.addVerifyListener(verifyListener);
			
			applyButton.addSelectionListener(applyOffsetListener);
			
		}
		
		public void applyOffsetValues() {
//			final IPlot currentPlotWidget = plot.getCurrentPlotWidget();
//			if (currentPlotWidget != null) {
//				final VisualisationDataManager multiPlotManager = currentPlotWidget.getMultiPlotManager();
//				multiPlotManager.setXOffset(getXOffsetValue());
//				multiPlotManager.setYOffset(getYOffsetValue());
//				multiPlotManager.setZOffset(getZOffsetValue());
//				
//				try {
//					currentPlotWidget.refreshPlot();
//				} catch (KurandaException e) {
//					plot.handleException(e);
//					return;
//				}
//				
//				if (isEnabled()) {
//					//this is offset mode
//					xOffsetValue = getXOffsetValue();
//					yOffsetValue = getYOffsetValue();
//					zOffsetValue = getZOffsetValue();
//					
//				}
//				
//				isChanged = false;
//				updateButtons();
////				updateOffsetValueFields();
//			}
		}
		
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			xOffsetValueText.setEnabled(enabled);
			yOffsetValueText.setEnabled(enabled);
			zOffsetValueText.setEnabled(enabled);
			
			applyButton.setEnabled(false);
			
			if (enabled) {
				updateButtons();
			}
		}

		public void clearValues() {
			xOffsetValueText.setText("");
			yOffsetValueText.setText("");
			zOffsetValueText.setText("");
			isChanged = false;
			updateButtons();
		}
		
		public void loadLastOffsetValues() {
			setXOffsetValue(xOffsetValue);
			setYOffsetValue(yOffsetValue);
			setZOffsetValue(zOffsetValue);
		}
		
		public void loadOverlayValues() {
			xOffsetValueText.setText("" + 0);
			yOffsetValueText.setText("" + 0);
			zOffsetValueText.setText("" + 0);
			isChanged = false;
			updateButtons();
		}
		
		protected void updateButtons() {
			applyButton.setEnabled(isChanged);
		}
		
		public int getXOffsetValue() {
			return parseOffsetValue(xOffsetValueText.getText());
		}
		
		public void setXOffsetValue(int xOffsetValue) {
			this.xOffsetValue = xOffsetValue;
			xOffsetValueText.setText("" + xOffsetValue);
			isChanged = false;
			updateButtons();
		}
		
		public int getYOffsetValue() {
			return parseOffsetValue(yOffsetValueText.getText());
		}
		
		public void setYOffsetValue(int yOffsetValue) {
			this.yOffsetValue = yOffsetValue;
			yOffsetValueText.setText("" + yOffsetValue);
			isChanged = false;
			updateButtons();
		}
		
		public int getZOffsetValue() {
			return parseOffsetValue(zOffsetValueText.getText());
		}

		/**
		 * @param text
		 * @return
		 * @throws NumberFormatException
		 */
		private int parseOffsetValue(final String text)
				throws NumberFormatException {
			if ("".equals(text)) {
				return 0;
			}
			return Integer.parseInt(text);
		}
		
		public void setZOffsetValue(int zOffsetValue) {
			this.zOffsetValue = zOffsetValue;
			zOffsetValueText.setText("" + zOffsetValue);
			isChanged = false;
			updateButtons();
		}
	}
}
