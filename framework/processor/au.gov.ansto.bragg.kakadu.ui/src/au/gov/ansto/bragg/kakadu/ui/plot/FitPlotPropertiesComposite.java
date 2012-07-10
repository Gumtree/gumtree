/******************************************************************************* 
 * Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.ui.plot;

import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.vis.interfaces.IPlot1D;
import org.gumtree.vis.listener.XYChartMouseEvent;
import org.gumtree.vis.plot1d.MarkerShape;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;

import au.gov.ansto.bragg.cicada.core.Exporter;
import au.gov.ansto.bragg.cicada.core.Format;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.freehep.jas3.core.Fitter;
import au.gov.ansto.bragg.freehep.jas3.core.StaticField.FunctionType;
import au.gov.ansto.bragg.freehep.jas3.core.UserDefinedFitter;
import au.gov.ansto.bragg.freehep.jas3.exception.DimensionNotSupportedException;
import au.gov.ansto.bragg.freehep.jas3.exception.FitterException;
import au.gov.ansto.bragg.kakadu.core.UIAlgorithmManager;
import au.gov.ansto.bragg.kakadu.core.data.DataType;
import au.gov.ansto.bragg.kakadu.ui.util.DisplayManager;
import au.gov.ansto.bragg.kakadu.ui.util.Util;

/**
 * @author nxi
 * Created on 19/06/2008
 */
public class FitPlotPropertiesComposite extends Composite {

	private Plot plot;
	private Composite parent;
	Fitter fitter;
	Button fitEnabledButton;
	Combo fitFunctionCombo;
	Button doFitButton;
	Button resetButton;
	Group parameterGroup;
	Composite userDefinedArea;
	Button removeButton;
	List<Text> parameterList = new ArrayList<Text>();
	Text resolutionText;
	Button minXButton;
	Button maxXButton;
	Text minXText;
	Text maxXText;
	double minXValue;
	double maxXValue;
	boolean isInitialised = false;
	private ExpandItem expandItem;
	Button inverseButton;
	private PlotDataItem plotDataItem;
	private ControlablePointLocationListener minLocationListener;
	private ControlablePointLocationListener maxLocationListener;
//	Label inverseLabel;
	/**
	 * @param parent
	 * @param style
	 */
	public FitPlotPropertiesComposite(Composite parent, int style, Plot plot) {
		super(parent, style);
		this.plot = plot;
		this.parent = parent;
		initialise();
		initListeners();

	}

	private void initialise() {
		GridLayout gridLayout = new GridLayout ();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 3;
		gridLayout.marginWidth = 3;
		gridLayout.horizontalSpacing = 3;
		gridLayout.verticalSpacing = 3;
		setLayout (gridLayout);

		fitEnabledButton = new Button(this, SWT.CHECK);
		fitEnabledButton.setText("Fitting Enabled");
		GridData data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		data.horizontalSpan = 2;
		data.grabExcessHorizontalSpace = true;
		fitEnabledButton.setLayoutData (data);


		fitFunctionCombo = new Combo(this, SWT.READ_ONLY);
		fitFunctionCombo.setEnabled(false);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		data.verticalIndent = 3;
		data.grabExcessHorizontalSpace = true;
		fitFunctionCombo.setLayoutData (data);

//		inverseLabel = new Label(this, SWT.NONE);
//		inverseLabel.setText("inverse function");
//		data = new GridData ();
//		data.verticalAlignment = GridData.BEGINNING;
//		data.verticalIndent = 3;
//		inverseLabel.setLayoutData(data);
//		inverseLabel.setEnabled(false);

		inverseButton = new Button(this, SWT.CHECK);
		inverseButton.setText("Inverted Model");
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalIndent = 3;
		data.grabExcessHorizontalSpace = true;
		inverseButton.setLayoutData (data);
		inverseButton.setEnabled(false);
//		fitFunctionCombo.setLayoutData (data);

		Composite xComposite = new Composite(this, SWT.NULL);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
//		gridLayout.marginHeight = 3;
		gridLayout.marginWidth = 0;
//		gridLayout.horizontalSpacing = 3;
//		gridLayout.verticalSpacing = 3;
		xComposite.setLayout (gridLayout);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = 2;
		data.grabExcessHorizontalSpace = true;
		xComposite.setLayoutData(data);
		
		minXButton = new Button(xComposite, SWT.TOGGLE);
		minXButton.setText("X min");
		minXButton.setToolTipText("Click to enable grabing a point from the plot as the beginning of fitting data");
		data = new GridData ();
//		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
//		data.grabExcessHorizontalSpace = true;
//		data.grabExcessVerticalSpace = true;
		minXButton.setLayoutData(data);
		minXButton.setEnabled(false);
		
		minXText = new Text(xComposite, SWT.BORDER);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		data.grabExcessHorizontalSpace = true;
//		data.grabExcessVerticalSpace = true;
		minXText.setLayoutData(data);
		minXText.setEnabled(false);
		
		maxXButton = new Button(xComposite, SWT.TOGGLE);
		maxXButton.setText("X max");
		maxXButton.setToolTipText("Click to enable grabing a point from the plot as the end of fitting data");
		data = new GridData ();
//		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
//		data.grabExcessHorizontalSpace = true;
//		data.grabExcessVerticalSpace = true;
		maxXButton.setLayoutData(data);
		maxXButton.setEnabled(false);
		
		maxXText = new Text(xComposite, SWT.BORDER);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		data.grabExcessHorizontalSpace = true;
//		data.grabExcessVerticalSpace = true;
		maxXText.setLayoutData(data);
		maxXText.setEnabled(false);
		
		doFitButton = new Button(this, SWT.PUSH);
		doFitButton.setText("Fit");
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		data.grabExcessHorizontalSpace = true;
//		data.grabExcessVerticalSpace = true;
		doFitButton.setLayoutData(data);
		doFitButton.setEnabled(false);
		
		resetButton = new Button(this, SWT.PUSH);
		resetButton.setText("Reset");
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		data.grabExcessHorizontalSpace = true;
		resetButton.setLayoutData(data);
		resetButton.setEnabled(false);

		parameterGroup = new Group(this, SWT.NONE);
		parameterGroup.setText("Parameters");
		GridLayout propertiesGridLayout = new GridLayout ();
		propertiesGridLayout.numColumns = 2;
		propertiesGridLayout.marginHeight = 3;
		propertiesGridLayout.marginWidth = 3;
		propertiesGridLayout.horizontalSpacing = 3;
		propertiesGridLayout.verticalSpacing = 3;
		parameterGroup.setLayout (propertiesGridLayout);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = 2;
		data.grabExcessHorizontalSpace = true;
		data.verticalAlignment = GridData.BEGINNING;
		parameterGroup.setLayoutData (data);

	}

	private void initListeners() {
		fitEnabledButton.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				try {
					if (!fitEnabledButton.getSelection()) {
						resetFitParameters();
					}
					setEnableFitting(fitEnabledButton.getSelection());
				} catch (Exception e) {
					plot.handleException(e);
				} 
			}

		});

		minLocationListener = new ControlablePointLocationListener(){

			@Override
			public void chartMouseClicked(final ChartMouseEvent event) {
				if (isEnabled() && plot.getCurrentPlotWidget() instanceof IPlot1D){
					setEnabled(false);
					DisplayManager.getDefault().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							if (event instanceof XYChartMouseEvent) {
								minXText.setText(String.valueOf(((XYChartMouseEvent) event).getX()));
								updateFitFunction();
							}
							minXButton.setSelection(false);
						}
					});
				} 
			}

			@Override
			public void chartMouseMoved(ChartMouseEvent event) {
				// TODO Auto-generated method stub
				
			}
			
		};
		
		maxLocationListener = new ControlablePointLocationListener(){

			public void locationUpdated(double x, double y, double val) {
				if (isEnabled && plot.getCurrentPlotWidget() instanceof IPlot1D){
					setEnabled(false);
					
					maxXText.setText(String.valueOf(x));
					updateFitFunction();
					maxXButton.setSelection(false);
				}
			}

			@Override
			public void chartMouseClicked(final ChartMouseEvent event) {
				if (isEnabled && plot.getCurrentPlotWidget() instanceof IPlot1D){
					setEnabled(false);
					DisplayManager.getDefault().asyncExec(new Runnable() {

						@Override
						public void run() {
							if (event instanceof XYChartMouseEvent) {
								maxXText.setText(String.valueOf(((XYChartMouseEvent) event).getX()));
								updateFitFunction();
							}
							maxXButton.setSelection(false);
						}
					});
				}
			}

			@Override
			public void chartMouseMoved(ChartMouseEvent event) {
				// TODO Auto-generated method stub
				
			}
		};
		
		minXButton.addSelectionListener(new SelectionListener(){
			boolean isClicked = false;
			
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				if (! isClicked){
					isClicked = true;
					plot.getCurrentPlotWidget().addChartMouseListener(minLocationListener);
				}
				minLocationListener.setEnabled(true);
			}
		});

		maxXButton.addSelectionListener(new SelectionListener(){
			boolean isClicked = false;
			
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}

			public void widgetSelected(SelectionEvent arg0) {
				if (! isClicked){
					isClicked = true;
					plot.getCurrentPlotWidget().addChartMouseListener(maxLocationListener);
				}
				maxLocationListener.setEnabled(true);
			}
		});

		minXText.addKeyListener(new KeyListener(){

			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.KEYPAD_CR || e.keyCode == SWT.CR){
					double currentValue = Double.valueOf(minXText.getText());
					if (minXValue != currentValue){
						minXValue = currentValue;
						updateFitFunction();
					}
				}
			}
			
		});
		
		minXText.addFocusListener(new FocusListener(){

			public void focusGained(FocusEvent arg0) {
			}

			public void focusLost(FocusEvent arg0) {
				double currentValue = Double.valueOf(minXText.getText());
				if (minXValue != currentValue){
					minXValue = currentValue;
					updateFitFunction();
				}
			}
			
		});
		
		maxXText.addKeyListener(new KeyListener(){

			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.KEYPAD_CR || e.keyCode == SWT.CR){
					double currentValue = Double.valueOf(maxXText.getText());
					if (maxXValue != currentValue){
						maxXValue = currentValue;
						updateFitFunction();
					}
				}
			}
			
		});
		
		maxXText.addFocusListener(new FocusListener(){

			public void focusGained(FocusEvent arg0) {
			}

			public void focusLost(FocusEvent arg0) {
				double currentValue = Double.valueOf(maxXText.getText());
				if (maxXValue != currentValue){
					maxXValue = currentValue;
					updateFitFunction();
				}
			}
			
		});
		
		doFitButton.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {

			}

			public void widgetSelected(SelectionEvent arg0) {
				fitSelectedPlot();
			}

		});

		resetButton.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {

			}

			public void widgetSelected(SelectionEvent arg0) {
				resetFitParameters();
			}

		});

		fitFunctionCombo.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {

			}

			public void widgetSelected(SelectionEvent arg0) {
				String functionName = fitFunctionCombo.getItem(
						fitFunctionCombo.getSelectionIndex());
				if (functionName.equals(FunctionType.AddFunction.getValue())){
					try {
						addNewFunction();
					} catch (Exception e) {
						plot.handleException(e);
					}
				}else{
					try {
						selectFitFunction(functionName);
					} catch (FitterException e) {
						plot.handleException(e);
					}
					if (fitter != null && fitter.isInverseAllowed())
						inverseButton.setEnabled(true);
					else
						inverseButton.setEnabled(false);
				}
			}

		});
	}

	
	private void addNewFunction() throws FitterException, 
	CoreException, DimensionNotSupportedException, IOException {
//		parameterGroup.dispose();
//		userDefinedArea = new Composite(this, SWT.NONE);
		NewFunctionDialog dialog = new NewFunctionDialog(getShell(), "New Function", 
				"Please provide the name and the text of the new function.", 
				null, null);
		dialog.open();
		String functionName = dialog.getFunctionName();
		String functionText = dialog.getFunctionText();
		if (functionName == null || functionText == null)
			return;
		if (functionText.contains("=")){
			functionText = removeEquationChar(functionText);
		}
//		final IWorkspace workspace = getWorkspace();
//		final IWorkspaceRoot root = workspace.getRoot();

		String filename = functionName + ".xml";
		IFolder folder = getProjectFolder();
		IFile file = folder.getFile(filename);
		if (file.exists())
			throw new FitterException("function with the same name already exists");
		String fullPath = folder.getWorkspace().getRoot().getLocation().toString() + 
			folder.getFullPath() + "/" + filename;
		fitter = new UserDefinedFitter(functionName, functionText);
		fitter.createHistogram(getGDMPlot(plot));
		try {
			Exporter xmlExporter = UIAlgorithmManager.getAlgorithmManager().getExporter(Format.xml);
			xmlExporter.signalExport(fitter.toGDMGroup(), ConverterLib.path2URI(fullPath));
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String, Double> parameters = fitter.getParameters();
		updateFunctionCombo();
		setFunctionFromComboSelected(functionName);
		updateParameterGroup(parameters, Double.NaN);
	}

	private void setFunctionFromComboSelected(String functionName) {
		String[] functionNames = fitFunctionCombo.getItems();
		for (int i = 0; i < functionNames.length; i++) {
			if (functionNames[i].equals(functionName))
				fitFunctionCombo.select(i);
		}
	}

	private String removeEquationChar(String functionText) {
		if (functionText.contains("=")){
			functionText = functionText.split("=")[1];
			return removeEquationChar(functionText);
		}
		return functionText;
	}

	private IFolder getProjectFolder() throws CoreException, FitterException{
		final IWorkspace workspace = getWorkspace();
		final IProject project = workspace.getRoot().getProject( UserDefinedFitter.FITTING_PROJECT );
		if( !project.exists() )
			project.create( null );
		project.open( null );
//		final IProject project = workspace.getRoot().getProject( SCRIPTS_PROJECT );
		IFolder folder = project.getFolder(new Path(UserDefinedFitter.FITTING_FOLDER));
		if (! folder.exists()){
			try {
				folder.create( IResource.NONE, true, null );
			} catch (CoreException e) {
				throw new FitterException(e);
			}
		}
		return folder;
	}

	private au.gov.ansto.bragg.datastructures.core.plot.Plot getGDMPlot(Plot plot) 
	throws FitterException{
		final MultiPlotDataManager multiPlotDataManager = plot.getMultiPlotDataManager();
		IGroup plotData =
			multiPlotDataManager.getDisplayablePlotDataItems().get(0).getData();
//			multiPlotDataManager.getPlotDataItems().get(0).getData();
//		multiPlotDataManager.getSingleVisiblePlotDataItem().getData();
		if (plotData instanceof au.gov.ansto.bragg.datastructures.core.plot.Plot) {
			au.gov.ansto.bragg.datastructures.core.plot.Plot aPlot = 
				(au.gov.ansto.bragg.datastructures.core.plot.Plot) plotData;
			return aPlot;
		}
		throw new FitterException("data must be a GDM plot");
	}

	private void updateFitFunction(){
//		try {
//			fitter.getEnginType().name();
//			fitter.createHistogram(getGDMPlot(plot), Double.valueOf(minXText.getText()), 
//					Double.valueOf(maxXText.getText()));
//		} catch (Exception e) {
//			plot.handleException(e);
//		} 
//		Map<String, Double> parameters = fitter.getParameters();
//		updateParameterGroup(parameters, Double.NaN);
		try {
			selectFitFunction(fitter.getFunctionType().name());
		} catch (FitterException e) {
			plot.handleException(e);
		}
	}
	
	private void selectFitFunction(String functionName) throws FitterException{
//		if (FitterType.valueOf(functionName))
		boolean isBuildinFunction = false;
		URI fileUri = null;
		try {
			FunctionType.valueOf(functionName);
			isBuildinFunction = true;
		} catch (Exception e) {
			try {
				fileUri = getProjectFolder().getFile(functionName + ".xml").getRawLocationURI();
			} catch (Exception e1) {
				throw new FitterException("can not find the function: " + functionName);
			}
		}
		double minX = minXValue;
		double maxX = maxXValue;
		try{
			minX = Double.valueOf(minXText.getText());
		}catch (Exception e) {
		}
		try {
			maxX = Double.valueOf(maxXText.getText());
		} catch (Exception e) {
		}
		if (minX > maxX){
			double tempValue = minX;
			minX = maxX;
			maxX = tempValue;
		}
		try {
			if (isBuildinFunction){
//				fitter = Fitter.getFitter(functionName, getGDMPlot(plot));
				fitter = Fitter.getFitter(functionName, getGDMPlot(plot).getRank());
				fitter.createHistogram(getGDMPlot(plot), minX, maxX);
			}
			else{
				fitter = new UserDefinedFitter(fileUri);
				fitter.createHistogram(getGDMPlot(plot), minX, maxX);
			}
		} catch (Exception e) {
			throw new FitterException("failed to create fitter: " + e.getMessage(), e);
		} 
		Map<String, Double> parameters = fitter.getParameters();
		updateParameterGroup(parameters, Double.NaN);
	}

	private void updateParameterGroup(Map<String, Double> parameters, Double quality) {
		inverseButton.setSelection(fitter.isInverse());
		inverseButton.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				if (fitter.isInverseAllowed())
					inverseButton.setEnabled(isEnabled());
				if (inverseButton.getSelection() != fitter.isInverse())
					setInverseValue(inverseButton.getSelection());
			}

		});
		parameterList.clear();
		GridData data;
		parameterGroup.dispose();
		if (removeButton != null) 
			removeButton.dispose();
		parameterGroup = new Group(this, SWT.NONE);
		parameterGroup.setText("Parameters");
		GridLayout propertiesGridLayout = new GridLayout ();
		propertiesGridLayout.numColumns = 2;
		propertiesGridLayout.marginHeight = 3;
		propertiesGridLayout.marginWidth = 3;
		propertiesGridLayout.horizontalSpacing = 3;
		propertiesGridLayout.verticalSpacing = 3;
		parameterGroup.setLayout (propertiesGridLayout);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = 2;
		data.grabExcessHorizontalSpace = true;
		parameterGroup.setLayoutData (data);

		Label functionLabel = new Label(parameterGroup, SWT.NONE);
		functionLabel.setText("Function");
		data = new GridData ();
		data.verticalAlignment = GridData.BEGINNING;
		data.verticalIndent = 3;
		functionLabel.setLayoutData(data);
		
		Text functionText = new Text(parameterGroup, SWT.NONE);
		functionText.setText(fitter.getFunctionText());
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		functionText.setLayoutData (data);
		functionText.setEditable(false);
		
		for (Entry<String, Double> entry : parameters.entrySet()){
			Label label = new Label(parameterGroup, SWT.NONE);
			label.setText(entry.getKey());
			data = new GridData ();
			data.verticalAlignment = GridData.BEGINNING;
			data.verticalIndent = 3;
			label.setLayoutData(data);

			Text text = new Text(parameterGroup, SWT.BORDER);
			text.setData(entry.getKey());
			text.setText(String.valueOf(entry.getValue()));
			data = new GridData ();
			data.horizontalAlignment = GridData.FILL;
			data.grabExcessHorizontalSpace = true;
			text.setLayoutData (data);

			parameterList.add(text);
		}
		Label resolutionlabel = new Label(parameterGroup, SWT.NONE);
		resolutionlabel.setText("resolution");
		data = new GridData ();
		data.verticalAlignment = GridData.BEGINNING;
		data.verticalIndent = 3;
		resolutionlabel.setLayoutData(data);

		resolutionText = new Text(parameterGroup, SWT.BORDER);
		resolutionText.setText(String.valueOf(fitter.getResolutionMultiple()));
//		resolutionText.
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		resolutionText.setLayoutData (data);
		
		if (!quality.isNaN()){
			Label label = new Label(parameterGroup, SWT.NONE);
			label.setText("Quality(" + fitter.getFitterType().getValue() + ")");
			data = new GridData ();
			data.verticalAlignment = GridData.BEGINNING;
			data.verticalIndent = 3;
			label.setLayoutData(data);

			Text text = new Text(parameterGroup, SWT.BORDER);
			text.setText(String.valueOf(quality));
			data = new GridData ();
			data.horizontalAlignment = GridData.FILL;
			data.grabExcessHorizontalSpace = true;
			text.setLayoutData (data);
		}
		if (fitter.getFunctionType() == FunctionType.AddFunction){
			removeButton = new Button(this, SWT.PUSH);
			removeButton.setText("Remove Function " + fitFunctionCombo.getItem(
					fitFunctionCombo.getSelectionIndex()));
			data = new GridData ();
			data.horizontalAlignment = GridData.FILL;
			data.verticalAlignment = GridData.FILL;
			data.horizontalSpan = 2;
			data.grabExcessHorizontalSpace = true;
			data.grabExcessVerticalSpace = true;
			removeButton.setLayoutData(data);
			removeButton.addSelectionListener(new SelectionListener(){

				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				public void widgetSelected(SelectionEvent arg0) {
					try {
						removeFunction(fitFunctionCombo.getItem(fitFunctionCombo.getSelectionIndex()));
					} catch (Exception e) {
						plot.handleException(e);
					}
				}
				
			});
		}
//		parameterGroup.redraw();
//		this.redraw();
//		parent.redraw();
//		redraw();
		layout();
		if (expandItem != null){
			expandItem.setHeight(this.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		}
		parent.update();
		parent.redraw();
//		parent.layout();
	}

	private void removeFunction(String functionName) throws CoreException, FitterException {
		if (MessageDialog.openConfirm(getShell(), 
				"Confirm Removal", "Do you want to remove function " + functionName + "?")){
			String fileName = functionName + ".xml";
			IFolder folder = getProjectFolder();
			IFile file = folder.getFile(fileName);
			File rawFile = new File(file.getRawLocationURI());
			if (rawFile.exists())
				if (!rawFile.delete())
					throw new FitterException("can not remove the function, function is in use");
			updateFunctionCombo();
			fitFunctionCombo.select(0);
			selectFitFunction(fitFunctionCombo.getItem(
					fitFunctionCombo.getSelectionIndex()));
		}
	}

	private void setInverseValue(boolean selection) {
		try{
			fitter.setInverse(selection);
		} catch (Exception e) {
			Util.handleException(parent.getShell(), e);
		} 
		Map<String, Double> parameters = fitter.getParameters();
		updateParameterGroup(parameters, Double.NaN);
	}

	private void fitSelectedPlot() {
		if (fitter != null){
			for (Text text : parameterList){
				String parameterName = (String) text.getData();
				Double parameterValue = Double.valueOf(text.getText());
				fitter.setParameterValue(parameterName, parameterValue);
			}
			fitter.setResolutionMultiple(Integer.valueOf(resolutionText.getText()));
			try {
				fitter.fit();
				au.gov.ansto.bragg.datastructures.core.plot.Plot resultPlot = 
					(au.gov.ansto.bragg.datastructures.core.plot.Plot) fitter.getResult();
				updateParameterGroup(fitter.getParameters(), fitter.getQuality());
				if (plotDataItem == null){
					plotDataItem = new PlotDataItem(resultPlot, DataType.Pattern);
					plot.getMultiPlotDataManager().addPlotDataItem(plotDataItem);
				}else{
					int idx = plot.getMultiPlotDataManager().getPlotDataItems().indexOf(plotDataItem);
					if (idx < 0){
						plotDataItem = new PlotDataItem(resultPlot, DataType.Pattern);
						plot.getMultiPlotDataManager().addPlotDataItem(plotDataItem);
					}
					else
						plot.getMultiPlotDataManager().updatePlotDataContents(plotDataItem, resultPlot);
				}
				plot.getMultiPlotDataManager().setMarker(plotDataItem, MarkerShape.NONE);
			} catch (Exception e) {
				plot.handleException(e);
			} 
		}
	}

	private void updateFunctionCombo() throws CoreException, FitterException{
		fitFunctionCombo.removeAll();
		for (FunctionType functionType : FunctionType.values())
			fitFunctionCombo.add(functionType.getValue());
		IFolder folder = getProjectFolder();
		File jFolder = new File(folder.getRawLocationURI());
		for (File file : jFolder.listFiles()){
			String name = file.getName();
			if (name.toLowerCase().endsWith(".xml")){
				name = name.substring(0, name.toLowerCase().lastIndexOf(".xml"));
				fitFunctionCombo.add(name);
			}
		}
	}

	private void setEnableFitting(boolean isEnabled) throws CoreException, FitterException{
		fitFunctionCombo.setEnabled(isEnabled);
		doFitButton.setEnabled(isEnabled);
		resetButton.setEnabled(isEnabled);
		minXText.setEnabled(isEnabled);
		minXButton.setEnabled(isEnabled);
		maxXButton.setEnabled(isEnabled);
		maxXText.setEnabled(isEnabled);
//		parameterGroup.setEnabled(isEnabled);
		parameterGroup.dispose();
//		inverseButton.setEnabled(isEnabled);
		if (isEnabled){
			List<IArray> axisList;
			try {
				axisList = ((NcGroup) getGDMPlot(plot)).getAxesArrayList();
			} catch (SignalNotAvailableException e) {
				throw new FitterException(e);
			}
			minXValue = axisList.get(axisList.size() - 1).getArrayMath().getMinimum();
			minXText.setText(String.valueOf(minXValue));
			maxXValue = axisList.get(axisList.size() - 1).getArrayMath().getMaximum();
			maxXText.setText(String.valueOf(maxXValue));
			if (fitFunctionCombo.getItemCount() == 0)
				updateFunctionCombo();
			fitFunctionCombo.select(0);
			selectFitFunction(fitFunctionCombo.getItem(
					fitFunctionCombo.getSelectionIndex()));
		}else{
			minXText.setText("");
			maxXText.setText("");
		}
		if (fitter != null && fitter.isInverseAllowed())
			inverseButton.setEnabled(isEnabled);
		else
			inverseButton.setEnabled(false);
	}

	public void setExpandItem(ExpandItem expandItem) {
		this.expandItem = expandItem;
	}
	
	abstract class ControlablePointLocationListener implements ChartMouseListener{
		boolean isEnabled = false;

		/**
		 * @return the isEnabled
		 */
		public boolean isEnabled() {
			return isEnabled;
		}

		/**
		 * @param isEnabled the isEnabled to set
		 */
		public void setEnabled(boolean isEnabled) {
			this.isEnabled = isEnabled;
		}
		
	}
	
	protected void resetFitParameters(){
		try {
			setEnableFitting(true);
			plot.getMultiPlotDataManager().removePlotDataItem(plotDataItem);
		} catch (Exception e) {
			plot.handleException(e);
		} 
	}
	
	@Override
	public void dispose() {
		plot.getCurrentPlotWidget().removeChartMouseListener(maxLocationListener);
		plot.getCurrentPlotWidget().removeChartMouseListener(minLocationListener);
		super.dispose();
	}
}
