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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.interfaces.IGroup;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.cicada.core.Algorithm;
import au.gov.ansto.bragg.cicada.core.AlgorithmConfiguration;
import au.gov.ansto.bragg.cicada.core.AlgorithmInput;
import au.gov.ansto.bragg.cicada.core.AlgorithmManager;
import au.gov.ansto.bragg.cicada.core.DRATask;
import au.gov.ansto.bragg.cicada.core.Exporter;
import au.gov.ansto.bragg.cicada.core.Format;
import au.gov.ansto.bragg.cicada.core.exception.ConfigurationException;
import au.gov.ansto.bragg.cicada.core.exception.ExportException;
import au.gov.ansto.bragg.cicada.core.exception.FailedToExecuteException;
import au.gov.ansto.bragg.cicada.core.exception.LoadAlgorithmFileFailedException;
import au.gov.ansto.bragg.cicada.core.exception.NoneAlgorithmException;
import au.gov.ansto.bragg.cicada.core.exception.SetTunerException;
import au.gov.ansto.bragg.cicada.core.exception.TransferFailedException;
import au.gov.ansto.bragg.cicada.core.exception.TunerNotReadyException;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.kakadu.core.AlgorithmTask;
import au.gov.ansto.bragg.kakadu.core.OperationManager;
import au.gov.ansto.bragg.kakadu.core.UIAlgorithmManager;
import au.gov.ansto.bragg.kakadu.core.data.DataItem;
import au.gov.ansto.bragg.kakadu.core.data.DataType;
import au.gov.ansto.bragg.kakadu.core.data.Operation;
import au.gov.ansto.bragg.kakadu.core.data.OperationDataListener;
import au.gov.ansto.bragg.kakadu.core.data.OperationOptions;
import au.gov.ansto.bragg.kakadu.core.data.OperationParameter;
import au.gov.ansto.bragg.kakadu.core.data.OperationParameterType;
import au.gov.ansto.bragg.kakadu.core.data.OperationStatus;
import au.gov.ansto.bragg.kakadu.core.data.region.RegionOperationParameter;
import au.gov.ansto.bragg.kakadu.ui.Activator;
import au.gov.ansto.bragg.kakadu.ui.ProjectManager;
import au.gov.ansto.bragg.kakadu.ui.SWTResourceManager;
import au.gov.ansto.bragg.kakadu.ui.plot.MultiPlotDataManager;
import au.gov.ansto.bragg.kakadu.ui.plot.Plot;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotDataItem;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotDataReference;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotException;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotManager;
import au.gov.ansto.bragg.kakadu.ui.region.RegionParameter;
import au.gov.ansto.bragg.kakadu.ui.region.RegionParameterManager;
import au.gov.ansto.bragg.kakadu.ui.util.DisplayManager;
import au.gov.ansto.bragg.kakadu.ui.util.Util;
import au.gov.ansto.bragg.kakadu.ui.views.MaskPropertiesView;
import au.gov.ansto.bragg.kakadu.ui.views.OperationParametersView;
import au.gov.ansto.bragg.kakadu.ui.widget.DScrolledComposite;
import au.gov.ansto.bragg.process.port.Tuner;

/**
 * 
 * @author Danil Klimontov (dak)
 */
public class AlgorithmTaskEditor extends EditorPart implements ISaveablePart2 {

	private Composite propertiesComposite;
	private Composite operationsComposite;
	private DScrolledComposite scrolledOperationsComposite;
	private Group optionsGroup;
	private Group parametersGroup;
	private Composite parameterEditorsHolderComposite;
	private Group actionsGroup;
	private MenuItem saveMenuItem;
	private static boolean hideOperationComposite = true;
	private RegionParameterManager regionParameterManager = new RegionParameterManager();
	
	private AlgorithmTask algorithmTask;
	private final SelectionListener operationSelectionListener = new OperationSelectionListener();

	private final OperationDataListener operationDataListener = new OperationDataListener() {
		public void outputDataUpdated(final Operation operation, IGroup oldData, final IGroup newData) {
			DisplayManager.getDefault().asyncExec(new Runnable() {
				public void run() {
					updateOperationExportAction(operation);
					updateOperationClonePlotAction(operation);
				}
			});
		}
	};
//	private final OperationDataListener actualOperationDataListener = new OperationDataListener() {
//	public void outputDataUpdated(final Operation operation, Object oldData, final Object newData) {
//	Display.getDefault().asyncExec(new Runnable() {
//	public void run() {
//	//mark the operations for other DataItems as NOT actual
//	//each time when current DataItem operation has been updated with new data.
//	//The flag will be used to run algorithm form not actual Operation
//	//when another DataItem will be selected.
//	for (int i = 0; i < algorithmTask.getDataItemsCount(); i++) {
//	if (i != algorithmTask.getSelectedDataItemIndex()) {
//	try {
//	final List<Operation> operations = algorithmTask.getOperationManager(i).getOperations();
//	for (Operation anOperation : operations) {
//	if (anOperation.getName().equals(operation.getName())) {
//	anOperation.setActual(false);
//	}
//	}
//	} catch (NoneAlgorithmException e) {
//	handleException(e);
//	} catch (NullSignalException e) {
//	handleException(e);
//	}
//	}
//	}
//	}
//	});
//	}
//	};

	private final OperationParameterEditor.ChangeListener parameterEditorChangeListener = 
		new OperationParameterEditor.ChangeListener() {
		public void dataChanged(Object oldData, Object newData) {
			if (selectedOperationComposite != null) {
				final Operation operation = selectedOperationComposite
				.getOperation();
				operation.updateStatus();
				updateParametersButtons(operation);
			}
		}
	};

	final SelectionAdapter applyParametersListener = new SelectionAdapter() {
		public void widgetDefaultSelected(SelectionEvent e) {
			applyParametersForSelectedOperation();
		}
		public void widgetSelected(SelectionEvent e) {
			applyParametersForSelectedOperation();

		}
	};


	private List<OperationComposite> operationCompositeList = new ArrayList<OperationComposite>();
	private final Map<String, Composite> parameterEditorCompositeMap = new HashMap<String, Composite>();
	private final Map<String, List<OperationParameterEditor>> parameterEditorsMap = new HashMap<String, List<OperationParameterEditor>>(); 

	private Button defaultParametersButton;
	private Button applyParametersButton;
	private Button revertParametersButton;
	private Composite parent;
	private OperationComposite selectedOperationComposite;
	private Composite dataControlComposite;
	private Combo dataItemCombo;
	private Button runAlgorithmButton;
	private Button interruptAlgorithmButton;
//	private Button skipOptionCheckbox;
//	private Button enableOptionCheckbox;
	private Button stopOptionCheckbox;
	private StackLayout parameterEditorsHolderStackLayout;
	private Composite operationsSectionComposite;
	private Color propertiesBorderColor;
	private Menu exportMenu;
	private Button exportButton;
	private Menu configurationMenu;
	private Button configurationButton;
	private Button clonePlotButton;
	private CTabItem operationPropertiesTabItem;
	private SashForm plotAndOperationPropertiesSashForm;
	private ScrolledComposite operationPropertiersScrolledComposite;
	private CTabFolder operationPropertiesTabFolder;
	private Button plotAllInOneButton;

	/**
	 * 
	 */
	public AlgorithmTaskEditor() {
	}

	public void doSave(IProgressMonitor monitor) {
		if (algorithmTask.getFileUri() == null)
			doSaveAs();
		else 
			doSave();
	}

	public void doSave(){
		File file = new File(algorithmTask.getFileUri());
//		if (! file.exists())
		AlgorithmManager algorithmManager = UIAlgorithmManager.getAlgorithmManager();
		DRATask task = null;
		try {
			task = algorithmManager.createDRATask(file.getName().substring(0, 
					file.getName().lastIndexOf(".")), 
					algorithmTask.getAlgorithmInputs().get(0).getAlgorithm());
		} catch (ConfigurationException e1) {
			Util.handleException(getSite().getShell(), e1);
		}
		List<DataItem> dataItemList = algorithmTask.getDataItems();
		for (DataItem item : dataItemList){
			try {
				String location = item.getDataObject().getLocation();
//				List<String> entryNames = findEntryNames(dataItemList, location);
				URI itemUri = ConverterLib.path2URI(location);
				task.addDataSource(itemUri, item.getDataObject().getShortName());
			} catch (Exception e) {
//				e.printStackTrace();
				Util.handleException(getSite().getShell(), e);
			}
		}
		try {
			Exporter exporter = algorithmManager.getExporter(Format.hdf);
			exporter.signalExport(task, algorithmTask.getFileUri());
		} catch (Exception e) {
			Util.handleException(getSite().getShell(), e);
		}
	}
//	private List<String> findEntryNames(List<DataItem> dataItemList,
//			String location) {
//		List<String> entryNames = new ArrayList<String>();
//		for (DataItem item : dataItemList){
////			Group data = 
//			if (item.getDataObject().getLocation().matches(location))
//				entryNames.add(item.getDataObject().getShortName());
//		}
//		return entryNames;
//	}

	public void doSaveAs() {
		String filename = Util.getSaveFilenameFromShell(getSite().getShell(), new String[]{"*.hdf"}, 
				new String[]{"hdf Nexus file"});
		if (filename == null)
			return;
		try {
			URI fileUri = ConverterLib.path2URI(filename);
			algorithmTask.setFileUri(fileUri);
		} catch (FileAccessException e) {
			Util.handleException(getSite().getShell(), e);
		}
		doSave();
		File file = new File(filename);
		setPartName(file.getName() + " - " + algorithmTask.getAlgorithm().getName() + " - Algorithm Task");
	}

	public void init(IEditorSite editorSite, IEditorInput editorInput)
	throws PartInitException {
		setInput(editorInput);
		setSite(editorSite);

		setPartName(editorInput.getName() + " - Algorithm Task");

	}

	public boolean isDirty() {
		// [Tony] [2008-12-16] Do not use Eclipse editor save
		return false;
//		return true;
	}

	/**
	 * Show confirmation dialog before close.
	 */
	public int promptToSaveOnClose() {
//		if (fileUri != null)
//			return ISaveablePart2.CANCEL;
		if (MessageDialog.openQuestion(
				parent.getShell(),
				"Algorithm Task",
				"Save the Algorithm Task [" + algorithmTask.getId() + "] before close?")) {
			return ISaveablePart2.YES;
		}

		return ISaveablePart2.NO;
	}

	public boolean isSaveAsAllowed() {
		// [Tony] [2008-12-16] Do not use Eclipse editor save
		return false;
//		return true;
	}

	public void createPartControl(final Composite parent) {

		this.parent = parent;
		SWTResourceManager.registerResourceUser(parent);

		GridLayout gridLayout = new GridLayout ();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		parent.setLayout (gridLayout);


		final Color[] backgroundGradientColors = new Color[] {
				parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND),
				parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW),
				parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW),
				parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND),
		};
		final int[] backgroundGradientPrasentage = new int[] {1, 90, 100 };
		final ControlListener redrawControlListener = new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}
			public void controlResized(ControlEvent e) {
				//need to be redrawn because gradient was not updated properly
				((Composite)e.getSource()).redraw();
			}
		};

		CTabFolder dataTabFolder = new CTabFolder(parent, SWT.NONE);
		dataTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		dataTabFolder.setSimple(false);
		dataTabFolder.setTabHeight(14);
		dataTabFolder.setSelectionBackground(backgroundGradientColors, backgroundGradientPrasentage);
		dataTabFolder.addControlListener(redrawControlListener);
		final CTabItem dataTabItem = new CTabItem(dataTabFolder, SWT.NONE);
		dataTabItem.setText("Data");
		dataTabFolder.setSelection(0);

		final CTabFolder operationsTabFolder = new CTabFolder(parent, SWT.NONE);
		operationsTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		operationsTabFolder.setSimple(false);
		operationsTabFolder.setTabHeight(14);
		operationsTabFolder.setSelectionBackground(backgroundGradientColors, backgroundGradientPrasentage);
		operationsTabFolder.addControlListener(redrawControlListener);
		final CTabItem operationsTabItem = new CTabItem(operationsTabFolder, SWT.NONE);
		operationsTabItem.setText("Operations");
		operationsTabFolder.setSelection(0);
		operationsTabFolder.setMinimizeVisible(true);
//		operationsTabFolder.setMinimized(false);
		operationsTabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
		      public void minimize(CTabFolderEvent event) {
		    	  operationsTabFolder.setMinimized(true);
//		    	  shell.layout(true);
		    	  operationsTabFolder.layout(true);
		    	  parent.layout();
		      }

		      public void maximize(CTabFolderEvent event) {
//		    	  operationPropertiesTabFolder.setMaximized(true);
//		    	  operationPropertiesTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
//		            true));
//		    	  parent.layout(true);
		      }

		      public void restore(CTabFolderEvent event) {
		    	  operationsTabFolder.setMinimized(false);
		    	  operationsTabFolder.setMaximized(false);
		    	  operationsTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
		    			  false));
		    	  operationsTabFolder.layout(true);
		    	  parent.layout();
		      }
		    });
		
		plotAndOperationPropertiesSashForm = new SashForm(parent, SWT.VERTICAL);
		plotAndOperationPropertiesSashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		operationPropertiesTabFolder = new CTabFolder(plotAndOperationPropertiesSashForm, SWT.NONE);
		//		operationPropertiesTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		operationPropertiesTabFolder.setSimple(false);	    
		operationPropertiesTabFolder.setTabHeight(14);
		operationPropertiesTabFolder.setSelectionBackground(backgroundGradientColors, backgroundGradientPrasentage);
		operationPropertiesTabFolder.addControlListener(redrawControlListener);
		operationPropertiesTabItem = new CTabItem(operationPropertiesTabFolder, SWT.NONE);
		operationPropertiesTabItem.setText("Operation Properties");
		operationPropertiesTabFolder.setSelection(0);
		operationPropertiesTabFolder.setMinimizeVisible(true);
		operationPropertiesTabFolder.setMinimized(true);
		operationPropertiesTabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
		      public void minimize(CTabFolderEvent event) {
		    	  operationPropertiesTabFolder.setMinimized(true);
//		    	  shell.layout(true);
		    	  operationPropertiesTabFolder.layout(true);
		    	  parent.layout();
		      }

		      public void maximize(CTabFolderEvent event) {
//		    	  operationPropertiesTabFolder.setMaximized(true);
//		    	  operationPropertiesTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
//		            true));
//		    	  parent.layout(true);
		      }

		      public void restore(CTabFolderEvent event) {
		    	  operationPropertiesTabFolder.setMinimized(false);
		    	  operationPropertiesTabFolder.setMaximized(false);
		    	  operationPropertiesTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
		    			  false));
		    	  operationPropertiesTabFolder.layout(true);
		    	  parent.layout();
		      }
		    });
		// Data Control
		dataControlComposite = new Composite (dataTabFolder, SWT.NONE);
		GridData data = new GridData ();
//		data.horizontalAlignment = GridData.FILL;
//		data.grabExcessHorizontalSpace = true;
//		dataControlComposite.setLayoutData (data);
		dataTabItem.setControl(dataControlComposite);

		final GridLayout dataControlGridLayout = new GridLayout();
		dataControlGridLayout.numColumns = 7;
		dataControlGridLayout.marginWidth = 3;
		dataControlGridLayout.marginHeight = 3;
		dataControlGridLayout.marginLeft = 3;
		dataControlGridLayout.marginRight = 0;
		dataControlComposite.setLayout(dataControlGridLayout);

		final Label dataItemLabel = new Label(dataControlComposite, SWT.NONE);
		dataItemLabel.setText("Data Item: ");
//		data = new GridData();
//		data.verticalAlignment = SWT.BOTTOM;
//		data.grabExcessVerticalSpace = true;
//		data.horizontalIndent = 0;
//		data.horizontalSpan = 0;
//		dataItemLabel.setLayoutData(data);

		dataItemCombo = new Combo(dataControlComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		dataItemCombo.setToolTipText("Selected Data Item");
//		dataItemCombo.setLayoutData(data);

		runAlgorithmButton = new Button(dataControlComposite, SWT.PUSH);
		runAlgorithmButton.setText("Run");
		runAlgorithmButton.setToolTipText("Re-Run the Algorithm for selected Data Item");
		runAlgorithmButton.setImage(SWTResourceManager.getImage("icons/Play-Normal-16x16.png"));
//		runAlgorithmButton.setLayoutData(data);
		interruptAlgorithmButton = new Button(dataControlComposite, SWT.PUSH);
		interruptAlgorithmButton.setText("Interrupt");
		interruptAlgorithmButton.setToolTipText("Interrupt the running algorithm");
		interruptAlgorithmButton.setImage(SWTResourceManager.getImage("icons/Stop-Normal-Blue-16x16.png"));
		
		
		plotAllInOneButton = new Button(dataControlComposite, SWT.PUSH);
		plotAllInOneButton.setText("Plot All");
		plotAllInOneButton.setToolTipText("Plot all Data Items in one plot for selected operation");
		plotAllInOneButton.setImage(SWTResourceManager.getImage("icons/clonePlot.gif", parent));

		configurationButton = new Button(dataControlComposite, SWT.PUSH);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		configurationButton.setText("Configuration");
		configurationButton.setLayoutData(data);
		configurationButton.setImage(SWTResourceManager.getImage("icons/algorithm_config.gif", 
				parent));
		configurationButton.setAlignment(GridData.HORIZONTAL_ALIGN_BEGINNING);
		configurationMenu = new Menu (configurationButton.getShell(), SWT.POP_UP);
	
//		Label dataControlSeparator = new Label(dataControlComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
//		data = new GridData ();
//		data.horizontalAlignment = GridData.FILL;
//		data.grabExcessHorizontalSpace = true;
//		data.horizontalSpan = 3;
//		dataControlSeparator.setLayoutData (data);

		//Operations list
//		operationsSectionComposite = new Composite (operationsTabFolder, SWT.NONE);
//		data = new GridData ();
//		data.horizontalAlignment = GridData.FILL;
//		data.grabExcessHorizontalSpace = true;
//		operationsSectionComposite.setLayoutData (data);

//		final GridLayout operationsSectionGridLayout = new GridLayout();
//		operationsSectionGridLayout.marginWidth = 0;
//		operationsSectionGridLayout.marginHeight = 0;
//		operationsSectionGridLayout.marginLeft = 0;
//		operationsSectionGridLayout.marginRight = 0;
//		operationsSectionGridLayout.verticalSpacing = 0;
////		operationsSectionGridLayout.marginTop = 3;
//		operationsSectionComposite.setLayout(operationsSectionGridLayout);

//		final Label operationsLabel = new Label(operationsSectionComposite, SWT.NONE);
//		data = new GridData ();
//		data.horizontalIndent = 5;
//		operationsLabel.setText("Operations:");
//		operationsLabel.setLayoutData(data);

		scrolledOperationsComposite = new DScrolledComposite(operationsTabFolder, SWT.NONE);
//		data = new GridData ();
//		data.horizontalAlignment = GridData.FILL;
//		data.grabExcessHorizontalSpace = true;
//		data.horizontalIndent = 0;
//		scrolledOperationsComposite.setLayoutData (data);
		operationsTabItem.setControl(scrolledOperationsComposite);

		operationsComposite = scrolledOperationsComposite.getContent();

		final RowLayout operationCompositeRowLayout = new RowLayout(SWT.HORIZONTAL);
		operationCompositeRowLayout.marginWidth = 0;
		operationCompositeRowLayout.marginTop = 3;
		operationCompositeRowLayout.marginBottom = 3;//-3;
		operationCompositeRowLayout.marginLeft = 5;
		operationCompositeRowLayout.spacing = 5;
		operationsComposite.setLayout(operationCompositeRowLayout);

		operationPropertiersScrolledComposite = new ScrolledComposite(operationPropertiesTabFolder, SWT.V_SCROLL | SWT.H_SCROLL);
		operationPropertiesTabItem.setControl(operationPropertiersScrolledComposite);

		// Properties
		propertiesComposite = new Composite(operationPropertiersScrolledComposite, SWT.NONE);
//		propertiesBorderColor = new Color(parent.getDisplay(), 95,83,156);
//		((BorderedComposite) propertiesComposite ).setBorderColor(propertiesBorderColor);
//		((BorderedComposite) propertiesComposite ).setBorderSize(3);

//		data = new GridData ();
//		data.horizontalAlignment = GridData.FILL;
//		data.grabExcessHorizontalSpace = true;
//		propertiesComposite.setLayoutData (data);

//		operationPropertiesTabItem.setControl(propertiesComposite);

		operationPropertiersScrolledComposite.setContent(propertiesComposite);
		operationPropertiersScrolledComposite.setExpandHorizontal(true);
		operationPropertiersScrolledComposite.setExpandVertical(true);

		GridLayout propertiesCompositGridLayout = new GridLayout();
		propertiesCompositGridLayout.numColumns = 3;
		propertiesComposite.setLayout(propertiesCompositGridLayout);

		//Options
		optionsGroup = new Group(propertiesComposite, SWT.NONE);
		optionsGroup.setText("Options");
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		optionsGroup.setLayoutData (data);
		final RowLayout optionsGroupRowLayout = new RowLayout(SWT.VERTICAL);
		optionsGroupRowLayout.spacing = 2;
		optionsGroup.setLayout(optionsGroupRowLayout);

//		skipOptionCheckbox = new Button(optionsGroup, SWT.CHECK);
//		skipOptionCheckbox.setText("Skip");

//		enableOptionCheckbox = new Button(optionsGroup, SWT.CHECK);
//		enableOptionCheckbox.setText("Enable");

		stopOptionCheckbox = new Button(optionsGroup, SWT.CHECK);
		stopOptionCheckbox.setText("Stop After Complete");


		//Parameters
		parametersGroup = new Group(propertiesComposite, SWT.NONE);
		parametersGroup.setText("Parameters");
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		parametersGroup.setLayoutData (data);
		GridLayout parametersGroupGridLayout = new GridLayout();
		parametersGroupGridLayout.numColumns = 1;
		parametersGroupGridLayout.verticalSpacing = 0;
		parametersGroupGridLayout.horizontalSpacing = 0;
		parametersGroupGridLayout.marginHeight = 0;
		parametersGroupGridLayout.marginWidth = 3;
		parametersGroup.setLayout(parametersGroupGridLayout);

		parameterEditorsHolderComposite = new Composite(parametersGroup, SWT.NONE);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		parameterEditorsHolderComposite.setLayoutData (data);
		parameterEditorsHolderStackLayout = new StackLayout();
		parameterEditorsHolderComposite.setLayout(parameterEditorsHolderStackLayout);

		Label parameterGroupSeparator = new Label(parametersGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		parameterGroupSeparator.setLayoutData (data);

		Composite parameterGroupButtonsComposite = new Composite(parametersGroup, SWT.NONE);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		parameterGroupButtonsComposite.setLayoutData (data);
		GridLayout parameterGroupButtonsCompositeGridLayout = new GridLayout();
		parameterGroupButtonsCompositeGridLayout.numColumns = 3;
		parameterGroupButtonsCompositeGridLayout.marginWidth = 0;
		parameterGroupButtonsCompositeGridLayout.marginHeight = 0;
		parameterGroupButtonsCompositeGridLayout.marginTop = 3;
		parameterGroupButtonsComposite.setLayout(parameterGroupButtonsCompositeGridLayout);

		defaultParametersButton = new Button(parameterGroupButtonsComposite, SWT.PUSH);
		defaultParametersButton.setText("Default");
		data = new GridData ();
		data.grabExcessHorizontalSpace = true;
		defaultParametersButton.setLayoutData (data);

		applyParametersButton = new Button(parameterGroupButtonsComposite, SWT.PUSH);
		applyParametersButton.setText("Apply");
		data = new GridData ();
		applyParametersButton.setLayoutData (data);
		applyParametersButton.setEnabled(false);

		revertParametersButton = new Button(parameterGroupButtonsComposite, SWT.PUSH);
		revertParametersButton.setText("Revert");
		data = new GridData ();
		revertParametersButton.setLayoutData (data);
		revertParametersButton.setEnabled(false);


		//Actions
		actionsGroup = new Group(propertiesComposite, SWT.NONE);
		actionsGroup.setText("Actions");
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		actionsGroup.setLayoutData (data);
		final GridLayout actionGroupGridLayout = new GridLayout();
		actionGroupGridLayout.marginWidth = 3;
		actionGroupGridLayout.marginHeight = 3;
		actionGroupGridLayout.verticalSpacing = 2;
		actionsGroup.setLayout(actionGroupGridLayout);

		exportButton = new Button(actionsGroup, SWT.PUSH);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		exportButton.setText("Export");
		exportButton.setAlignment(SWT.LEFT);
		exportButton.setLayoutData (data);
		exportButton.setImage(SWTResourceManager.getImage("icons/export.gif", parent));

		clonePlotButton = new Button(actionsGroup, SWT.PUSH);
		clonePlotButton.setText("Plot");
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		clonePlotButton.setLayoutData (data);
		clonePlotButton.setImage(SWTResourceManager.getImage("icons/clonePlot.gif", parent));
//		clonePlotButton.setEnabled(false);
//		clonePlotButton.setAlignment(SWT.LEFT);

		exportMenu = new Menu (exportButton.getShell(), SWT.POP_UP);
		//get AlgorithmTask from EditorInput
		algorithmTask = (AlgorithmTask) getEditorInput().getAdapter(AlgorithmTask.class);
		setPartName("T" + algorithmTask.getId() +
				" " + getEditorInput().getName() + " - Algorithm Task");
		initConfigurationMenu();
		try {
			initExportAction();
		} catch (ExportException e1) {
			Util.handleException(getSite().getShell(), e1);
		}
		initListeners();
	}

	private void initExportAction() throws ExportException {
		for (Exporter exporter : UIAlgorithmManager.getAvailableExporterList()) {
			MenuItem menuItem = new MenuItem (exportMenu, SWT.PUSH);
			menuItem.setText (exporter.getFormater().getName());
			menuItem.addSelectionListener(new ExportListener(exporter));
		}
	}

	private void initConfigurationMenu(){
		MenuItem loadMenuItem = new MenuItem (configurationMenu, SWT.PUSH);
		loadMenuItem.setText("Load from");
		loadMenuItem.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}

			public void widgetSelected(SelectionEvent e) {
				URI fileURI = null;
				try{
						File draPath = new File(UIAlgorithmManager.getAlgorithmManager().getAlgorithmSetPath());
						String path = Util.getFilenameFromShell(e.widget.getDisplay().
								getActiveShell(), "*.xml", "'xml' data file", draPath);
						if (path == null) return;
						fileURI = (new File(path)).toURI();
				}catch (Exception ex) {
					handleException(ex);
				}
				try {
//					UIAlgorithmManager.getAlgorithmManager().getCurrentInput().getAlgorithm().
//					loadConfiguration(fileURI);
					for (AlgorithmInput algorithmInput : algorithmTask.getAlgorithmInputs()){
						algorithmInput.getAlgorithm().loadConfiguration(fileURI);
					}
				} catch (Exception e1) {
					handleException(e1);
				}
				updateAllOperationParameters();
			}

			
		});
				
		saveMenuItem = new MenuItem (configurationMenu, SWT.PUSH);
		saveMenuItem.setText("Save");
		saveMenuItem.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}

			public void widgetSelected(SelectionEvent e) {
				URI fileURI = null;
				Algorithm currentAlgorithm = null;
				try{
//					AlgorithmConfiguration configuration = UIAlgorithmManager.getAlgorithmManager().
//					getCurrentInput().getAlgorithm().getConfiguration();
					currentAlgorithm = algorithmTask.getSelectedAlgorithmInput().getAlgorithm();
					AlgorithmConfiguration configuration = currentAlgorithm.getConfiguration();
					if (configuration != null && configuration.getPath() != null){
						fileURI = configuration.getPath();
					} else{
						File draPath = new File(UIAlgorithmManager.getAlgorithmManager().getAlgorithmSetPath());
						String path = Util.saveFilenameFromShell(e.widget.getDisplay().
								getActiveShell(), "xml", "'xml' data file", draPath);
						if (path == null) return;
						fileURI = (new File(path)).toURI();
					}
				}catch (Exception ex) {
					handleException(ex);
				}
				System.out.println("Export data for operation '" + selectedOperationComposite.getOperation().getName() +
						"' to file '" + fileURI + "'...");
				try {
					String filename = (new File(fileURI)).getName().trim();
					filename = filename.substring(0, filename.indexOf("."));
//					algorithmTask.getAlgorithm().exportConfiguration(fileURI, filename);
//					UIAlgorithmManager.getAlgorithmManager().getCurrentInput().getAlgorithm().
//						exportConfiguration(fileURI, filename);
					currentAlgorithm.exportConfiguration(fileURI, filename);
				} catch (Exception e1) {
					handleException(e1);
				}
			}
			
		});

		MenuItem saveAsMenuItem = new MenuItem (configurationMenu, SWT.PUSH);
		saveAsMenuItem.setText("Save as");
		saveAsMenuItem.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}

			public void widgetSelected(SelectionEvent e) {
				File draPath = new File(UIAlgorithmManager.getAlgorithmManager().getAlgorithmSetPath());
				String path = Util.saveFilenameFromShell(e.widget.getDisplay().
						getActiveShell(), "xml", "'xml' data file", draPath);
				if (path == null) return;
				final URI fileURI = (new File(path)).toURI();

				System.out.println("Export data for operation '" + selectedOperationComposite.getOperation().getName() +
						"' to file '" + fileURI + "'...");
				try {
					String filename = (new File(fileURI)).getName().trim();
					filename = filename.substring(0, filename.indexOf("."));
//					algorithmTask.getAlgorithm().exportConfiguration(fileURI, filename);
					UIAlgorithmManager.getAlgorithmManager().getCurrentInput().getAlgorithm().
						exportConfiguration(fileURI, filename);
				} catch (Exception e1) {
					handleException(e1);
				}
			}
			
		});
	}
	public void updateAllOperationParameters() {
		algorithmTask.updateOperationParameters();
//		for (OperationComposite operationComposite : operationCompositeList) {
//			Operation operation = operationComposite.getOperation();
//			operation.updateParameters();
////			updateOperationParameters(operation);
//		}
		final Operation operation = selectedOperationComposite.getOperation();
		updateOperationParameters(operation);
	}

	public void init() throws LoadAlgorithmFileFailedException {

		//init all OperationManagers to load operation parameters and options
		final List<DataItem> dataItems = algorithmTask.getDataItems();
		algorithmTask.registerExceptionHandler(new ThreadExceptionListener(parent.getShell()));
		for (int i = 0; i < dataItems.size(); i++) {
			try {
				algorithmTask.loadOperationManager(i);
			} catch (Exception e) {
				throw new LoadAlgorithmFileFailedException(e.getMessage(), e);
			}
		}

		//init sub components;

//		for (ParameterRegionManager parameterRegionManager : algorithmTask.getRegionManager().getParameterRegionManagerList()) {
//			final String operationName = parameterRegionManager.getOperationName();
//			parameterRegionManager.addRegionListener(new AutoApplyRegionChangesListener(operationName));
//		}

		updateOperationList(algorithmTask.getOperationManager(algorithmTask.getSelectedDataItemIndex()));

		//set default selected operation
		if (operationCompositeList.size() > 0) {
			operationCompositeList.get(operationCompositeList.size() - 1).setSelected(true);
		}
		initRegionParameterManager();

		//load data items list
		for (int i = 0; i < dataItems.size(); i++) {
			DataItem dataItem = dataItems.get(i);
			dataItemCombo.add("" + i + ": " + dataItem.getName());
		}

		//set default selected DataItem
		if (dataItems.size() > 0) {
			dataItemCombo.select(algorithmTask.getSelectedDataItemIndex());
			setSelectedDataItem(algorithmTask.getSelectedDataItemIndex());
		}
//		if (algorithmTask.getAlgorithm().getCurrentSourcePort() == null)
//			dataItemCombo.setEnabled(false);
//		loadDataUpdateListener();


		IWorkbenchWindow workbenchWindow = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
		final IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
		MaskPropertiesView maskPropertiesView = (MaskPropertiesView) workbenchPage.findView("au.gov.ansto.bragg.kakadu.ui.views.MaskPropertiesView");
		if (maskPropertiesView != null && workbenchPage.isPartVisible(maskPropertiesView)) {
			maskPropertiesView.setRegionManager((RegionParameterManager) 
					algorithmTask.getRegionParameterManager());
		}

		operationPropertiesTabFolder.setMinimized(hideOperationComposite);
		operationPropertiesTabFolder.layout(true);
  	  	parent.layout();
	}

	private void initRegionParameterManager() {
		algorithmTask.setRegionParameterManager(regionParameterManager);
		for (Operation operation : algorithmTask.getOperationManager(0).getOperations()) {
			for (OperationParameter parameter : operation.getParameters()) {
				if (parameter.getType() == OperationParameterType.Region) {
					regionParameterManager.addParameter(
							new RegionParameter(operation, parameter.getUILabel()));
				}
			}
		}
	}

	public RegionParameterManager getRegionParameterManager(){
		return regionParameterManager;
	}
	
	public void load() throws LoadAlgorithmFileFailedException {

		//init all OperationManagers to load operation parameters and options
		final List<DataItem> dataItems = algorithmTask.getDataItems();
		algorithmTask.registerExceptionHandler(new ThreadExceptionListener(parent.getShell()));
		for (int i = 0; i < dataItems.size(); i++) {
			try {
				algorithmTask.loadOperationManager(i);
			} catch (Exception e) {
				throw new LoadAlgorithmFileFailedException(e.getMessage(), e);
			}
		}

		//init sub components;
//		for (RegionParameter parameterRegionManager : ((RegionParameterManager) 
//				algorithmTask.getRegionParameterManager()).getParameterList()) {
//			final String operationName = parameterRegionManager.getOperation().getName();
//			parameterRegionManager.addRegionListener(new AutoApplyRegionChangesListener(operationName));
//		}

		updateOperationList(algorithmTask.getOperationManager(algorithmTask.getSelectedDataItemIndex()));

		initRegionParameterManager();

		//set default selected operation
		if (operationCompositeList.size() > 0) {
			operationCompositeList.get(operationCompositeList.size() - 1).setSelected(true);
		}

		//load data items list
		for (int i = 0; i < dataItems.size(); i++) {
			DataItem dataItem = dataItems.get(i);
			dataItemCombo.add("" + i + ": " + dataItem.getName());
		}

		//set default selected DataItem
		if (dataItems.size() > 0) {
			dataItemCombo.select(algorithmTask.getSelectedDataItemIndex());
//			setSelectedDataItem(algorithmTask.getSelectedDataItemIndex());
			int dataItemIndex = algorithmTask.getSelectedDataItemIndex();
			final Operation selectedOperation = selectedOperationComposite.getOperation();
			final String selectedOperationName = selectedOperation.getName();

			if (!applyAllChangedParameters()) {
				//select previous DataItem if applying failed
				dataItemCombo.select(algorithmTask.getSelectedDataItemIndex());
				return;
			}

			//deactivate RegionOperationParameters of previously selected DataItem
			//to avoid updating by new data when user modifies another DataItem's parameters
			final List<Operation> operations = algorithmTask.getOperationManager(algorithmTask.getSelectedDataItemIndex()).getOperations();
			for (Operation operation : operations) {
				for (OperationParameter parameter : operation.getParameters()) {
					if (parameter.getType() == OperationParameterType.Region) {
						((RegionOperationParameter) parameter ).setActive(false);
					}
				}
			}

			//after complete all preparation steps - set the index to algorithmTask
			algorithmTask.setSelectedDataItem(dataItemIndex);

			//OperationManager for just selected data item
			OperationManager operationManager = algorithmTask.getOperationManager(algorithmTask.getSelectedDataItemIndex());

			//detect first no actual Operation data to run the algorithm from the operation.
//			final Operation[] firstNotActualOperation = new Operation[1];

			for (Operation operation : operationManager.getOperations()) {

				//activate RegionOperationParameters of selected DataItem
				for (OperationParameter parameter : operation.getParameters()) {
					if (parameter.getType() == OperationParameterType.Region) {
						((RegionOperationParameter) parameter ).setActive(true);
					}
				}

				//find first operation with not actual data 
//				if (firstNotActualOperation[0] == null && !operation.isActual() && operation.isReprocessable()) {
//					firstNotActualOperation[0] = operation;
//				}
			}

			updateOperationList(operationManager);

			setSelectedOperationComposit(selectedOperationName);
		}
//		if (algorithmTask.getAlgorithm().getCurrentSourcePort() == null)
//			dataItemCombo.setEnabled(false);
//		loadDataUpdateListener();


//		IWorkbenchWindow workbenchWindow = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
//		final IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
//		MaskPropertiesView maskPropertiesView = (MaskPropertiesView) workbenchPage.findView("au.gov.ansto.bragg.kakadu.ui.views.MaskPropertiesView");
//		if (maskPropertiesView != null && workbenchPage.isPartVisible(maskPropertiesView)) {
//		maskPropertiesView.setRegionManager(algorithmTask.getRegionManager());
//		}
		operationPropertiesTabFolder.setMinimized(hideOperationComposite);
		operationPropertiesTabFolder.layout(true);
  	  	parent.layout();
	}

	/**
	 * Add OperationDataListener to all operations to update plot data for all opened plots.
	 */
	private void loadDataUpdateListener() {
		final PlotDataUpdateListener plotDataUpdateListener = new PlotDataUpdateListener();
		for (int i = 0; i < algorithmTask.getDataItemsCount(); i++) {
			final OperationManager operationManager = algorithmTask.getOperationManager(i);
			final List<Operation> operations = operationManager.getOperations();
			for (Operation operation : operations) {
				operation.addOperationDataListener(plotDataUpdateListener);
			}
		}
	}

	private final class PlotDataUpdateListener implements OperationDataListener {
		public void outputDataUpdated(final Operation operation, IGroup oldData, final IGroup newData) {
			try {
				PlotManager.updatePlots(algorithmTask, operation.getName());
			} catch (TunerNotReadyException e) {
				handleException(e);
			} catch (TransferFailedException e) {
				handleException(e);
			} catch (NoneAlgorithmException e) {
				handleException(e);
			} catch (FailedToExecuteException e) {
				handleException(e);
			}
		}
	}


	protected void initListeners() {
		defaultParametersButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				loadDefaulParameters();
			}
		});

		revertParametersButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				Operation operation = selectedOperationComposite.getOperation();
				operation.revertParametersChanges();
				updateOperationParameters(operation);
			}
		});

		applyParametersButton.addSelectionListener(applyParametersListener);

		dataItemCombo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				final int selectionIndex = dataItemCombo.getSelectionIndex();
				setSelectedDataItem(selectionIndex);
			}
		});

		runAlgorithmButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				runAlgorithm();
			}
		});

		interruptAlgorithmButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					// Action: interrupt SICS
					algorithmTask.interrupt();
				} catch (Exception e1) {
					LoggerFactory.getLogger(OperationParametersView.class).error(
							"Failed to send interrupt.", e);
				}
			}
		});

		plotAllInOneButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				plotAllInOne();
			}
		});

//		skipOptionCheckbox.addSelectionListener(new SelectionListener() {
//			public void widgetDefaultSelected(SelectionEvent e) {
//			}
//			public void widgetSelected(SelectionEvent e) {
//				final Operation operation = selectedOperationComposite.getOperation();
//				final boolean runAfterApplying = operation.getStatus() == OperationStatus.Done;
//				final OperationOptions options = operation.getOptions();
//				final Tuner skipTuner = options.getSkipTuner();
//				try {
//					algorithmTask.applyOptionChangesForAllDataItems(
//							skipTuner.getName(),
//							new Boolean(skipOptionCheckbox.getSelection()).booleanValue());
//				} catch (SetTunerException e1) {
//					handleException(e1);
//				}
//				//update with updated values
//				options.setSkipTuner(skipTuner);
//				skipOptionCheckbox.setSelection(options.isSkipped());
//				operation.updateStatus();
//
//				if (runAfterApplying || operation.getStatus() == OperationStatus.Done) {
//					try {
//						algorithmTask.runAlgorithmFromOperation(operation);
//					} catch (TunerNotReadyException e1) {
//						handleException(e1);
//					} catch (TransferFailedException e1) {
//						handleException(e1);
//					}
//				}
//			}
//		});
		
//		enableOptionCheckbox.addSelectionListener(new SelectionListener() {
//			public void widgetDefaultSelected(SelectionEvent e) {
//			}
//			public void widgetSelected(SelectionEvent e) {
//				final Operation operation = selectedOperationComposite.getOperation();
//				final boolean runAfterApplying = operation.getStatus() == OperationStatus.Done;
//				final OperationOptions options = operation.getOptions();
//				final Tuner enableTuner = options.getEnableTuner();
//				try {
//					algorithmTask.applyOptionChangesForAllDataItems(
//							enableTuner.getName(),
//							new Boolean(enableOptionCheckbox.getSelection()).booleanValue());
//				} catch (SetTunerException e1) {
//					handleException(e1);
//				}
//				//update with updated values
//				options.setEnableTuner(enableTuner);
//				enableOptionCheckbox.setSelection(options.isEnabled());
//				operation.updateStatus();
//
//				if (runAfterApplying || operation.getStatus() == OperationStatus.Done) {
//					try {
//						algorithmTask.runAlgorithmFromOperation(operation);
//					} catch (TunerNotReadyException e1) {
//						handleException(e1);
//					} catch (TransferFailedException e1) {
//						handleException(e1);
//					}
//				}
//			}
//		});
		stopOptionCheckbox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				final Operation operation = selectedOperationComposite.getOperation();
				final OperationOptions options = operation.getOptions();
				final Tuner stopAfterCompleteTuner = options.getStopAfterCompleteTuner();
				try {
					algorithmTask.applyOptionChangesForAllDataItems(
							stopAfterCompleteTuner.getName(),
							new Boolean(stopOptionCheckbox.getSelection()).booleanValue());
				} catch (SetTunerException e1) {
					handleException(e1);
				}
				//update with updated values
				options.setStopAfterCompleteTuner(stopAfterCompleteTuner);
				stopOptionCheckbox.setSelection(options.isStopAfterComplete());
				operation.updateStatus();

				if (operation.getStatus() == OperationStatus.Done) {
					try {
						algorithmTask.runAlgorithmFromOperation(operation);
					} catch (TunerNotReadyException e1) {
						handleException(e1);
					} catch (TransferFailedException e1) {
						handleException(e1);
					}
				}
			}
		});

		exportButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
//				if (e.detail == SWT.ARROW) {
				Rectangle rect = exportButton.getBounds ();
//				final Point location = exportButton.getLocation();
//				final Point size = exportButton.getSize();
				Point pt = new Point (rect.x, rect.y
						+ rect.height
				);
				pt = actionsGroup.toDisplay (pt);
				exportMenu.setLocation (pt.x, pt.y);
				exportMenu.setVisible (true);
//				}
			}
		});
		
		configurationButton.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				AlgorithmConfiguration configuration = null;
				try {
//					configuration = UIAlgorithmManager.getAlgorithmManager().
//					getCurrentInput().getAlgorithm().getConfiguration();
					configuration = algorithmTask.getAlgorithmInputs().get(0)
					.getAlgorithm().getConfiguration();
					if (configuration != null && configuration.getPath() != null)
						saveMenuItem.setText("Save to " + configuration.getName());
					else
						saveMenuItem.setText("Save");
				} catch (Exception e) {
					handleException(e);
				}
				Rectangle rect = configurationButton.getBounds ();
//				final Point location = configurationButton.getLocation();
//				final Point size = configurationButton.getSize();
				Point pt = new Point (rect.x, rect.y
						+ rect.height
				);
				pt = dataControlComposite.toDisplay (pt);
				configurationMenu.setLocation (pt.x, pt.y);
				configurationMenu.setVisible (true);
//				}
			}
			
		});
		
		clonePlotButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				plotCurrentOperation();
//				try {
//				PlotManager.openPlot(getAlgorithmTask(), 
//				selectedOperationComposite.getOperation().getName(), 
//				getAlgorithmTask().getSelectedDataItemIndex());
//				} catch (Exception e1) {
//				handleException(e1);
//				}
			}
		});

//		RegionManager.addRegionListener(regionListener);

	}
 
	protected void plotAllInOne() {
		BusyIndicator.showWhile(parent.getDisplay(), new Runnable() {
			public void run() {
				final List<PlotDataItem> plotDataItemList = new ArrayList<PlotDataItem>();

				final int operationIndex = algorithmTask.getOperationManager(
						algorithmTask.getSelectedDataItemIndex())
						.getOperationIndex(selectedOperationComposite.getOperation().getName());

				for (int i = 0; i < algorithmTask.getDataItemsCount(); i++) {
					OperationManager opManager = algorithmTask.getOperationManager(i);
					Operation operation = opManager.getOperation(operationIndex);
					
					PlotDataReference plotDataReference = new PlotDataReference(
							algorithmTask.getId(), operationIndex, i);
					IGroup outputData 
						= (IGroup) operation.getOutputData();
 					
					if (null!=outputData) {
						final DataType dataType = operation.getDataType();

						final PlotDataItem plotDataItem = new PlotDataItem(
							outputData, plotDataReference, dataType);

						plotDataItemList.add(plotDataItem);
					}
					
				}

				if (plotDataItemList.size() <= 0) {
					//Nothing to display
					return;
				}

				DisplayManager.getDefault().asyncExec(new Runnable() {
					public void run() {
						BusyIndicator.showWhile(parent.getDisplay(), new Runnable() {
							public void run() {

								final Plot plot;
								try {
									plot = PlotManager.openPlot(Plot.getDefaultPlotType(plotDataItemList.get(0).getDataType()));
								} catch (PartInitException ex) {
									handleException(ex);
									return;
								}
								final MultiPlotDataManager multiPlotDataManager = plot.getMultiPlotDataManager();
								try {
									for (PlotDataItem plotDataItem : plotDataItemList) {
										multiPlotDataManager.addPlotDataItem(plotDataItem);
									}
								} catch (PlotException e) {
									handleException(e);
								}
								plot.layout();
								plot.redraw();

							}
						});//busy indicator


					}
				});//Display.getDefault().asyncExec

			}
		});//busy indicator
	}

	protected void plotCurrentOperation() {
		PlotManager.plotOperation(getAlgorithmTask(), selectedOperationComposite.getOperation(), 
				getSite().getWorkbenchWindow());

//		final Operation operation = selectedOperationComposite.getOperation();

//		final PlotDataReference plotDataReference = new PlotDataReference(
//		algorithmTask.getId(), 
//		operation.getID(), 
//		algorithmTask.getSelectedDataItemIndex());
//		final PlotDataItem plotDataItem = new PlotDataItem(	
//		operation.getOutputData(), 
//		plotDataReference, 
//		operation.getDataType());

//		Display.getDefault().asyncExec(new Runnable() {
//		public void run() {
//		final Plot plot;
//		try {
//		plot = PlotManager.openPlot(Plot.getDefaultPlotType(plotDataItem.getDataType()));
//		} catch (PartInitException ex) {
//		handleException(ex);
//		return;
//		}
//		final MultiPlotDataManager multiPlotDataManager = plot.getMultiPlotDataManager();
//		try {
//		multiPlotDataManager.addPlotDataItem(plotDataItem);
//		} catch (PlotException e) {
//		handleException(e);
//		}
//		plot.layout();
//		plot.redraw();
//		}
//		});
	}

	/**
	 * Gets AlgorithmTask for the editor.
	 * @return AlgorithmTask object.
	 */
	public AlgorithmTask getAlgorithmTask() {
		return algorithmTask;
	}

	private void handleException(Throwable throwable) {
		throwable.printStackTrace();
		showErrorMessage(throwable.getMessage());
	}

	private void showErrorMessage(String message) {
		MessageDialog.openError(
				parent.getShell(),
				"Algorithm Task",
				message);
	}


	protected void loadDefaulParameters() {
		if (selectedOperationComposite != null) {
			final Operation operation = selectedOperationComposite.getOperation();

			operation.resetParametersToDefault();

			updateOperationParameters(operation);
		}

	}

	private void updateOperationList(OperationManager operationManager) {

		if (operationManager == null) return;
		setSelectedOperationComposite((OperationComposite)null);

		if (operationCompositeList.size() == 0) {
			List<Operation> operations = operationManager.getOperations();
			for (final Operation operation : operations) {
				OperationComposite operationComposite = new OperationComposite(operationsComposite, SWT.NONE);
				operationComposite.setOperation(operation);
				operationComposite.addSelectionListener(operationSelectionListener);
				operationComposite.addOperationEnableListener(new OperationComposite.OperationEnableListener(){

					public void setEnable(boolean isEnabled) {
						final boolean runAfterApplying = operation.getStatus() == OperationStatus.Done;
						final OperationOptions options = operation.getOptions();
						if (options.isEnableSupported()){
							final Tuner enableTuner = options.getEnableTuner();
							try {
								algorithmTask.applyOptionChangesForAllDataItems(
										enableTuner.getName(),
										isEnabled);
							} catch (SetTunerException e1) {
								handleException(e1);
							}
							//update with updated values
							options.setEnableTuner(enableTuner);
							operation.updateStatus();

							if (runAfterApplying || operation.getStatus() == OperationStatus.Done) {
								try {
									algorithmTask.runAlgorithmFromOperation(operation);
								} catch (TunerNotReadyException e1) {
									handleException(e1);
								} catch (TransferFailedException e1) {
									handleException(e1);
								}
							}
						}else if (options.isSkipSupported()){
							final Tuner skipTuner = options.getSkipTuner();
							try {
								algorithmTask.applyOptionChangesForAllDataItems(
										skipTuner.getName(),
										!isEnabled);
							} catch (SetTunerException e1) {
								handleException(e1);
							}
							//update with updated values
							options.setSkipTuner(skipTuner);
							operation.updateStatus();
			
							if (runAfterApplying || operation.getStatus() == OperationStatus.Done) {
								try {
									algorithmTask.runAlgorithmFromOperation(operation);
								} catch (TunerNotReadyException e1) {
									handleException(e1);
								} catch (TransferFailedException e1) {
									handleException(e1);
								}
							}
						}
					}});
				operationCompositeList.add(operationComposite);
			}
		} else {
			List<Operation> operations = operationManager.getOperations();
			for (int i = 0; i < operations.size(); i++) {
				Operation operation = operations.get(i);
				operationCompositeList.get(i).setOperation(operation);

			}

		}

		operationsComposite.setSize(operationsComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		operationsComposite.layout();
		parent.layout();
	}

	private void updateOperationParameters(final Operation operation) {
		final String operationName = operation != null ? operation.getName() : null;
		Composite parameterEditorComposite = parameterEditorCompositeMap.get(operationName);

		if (parameterEditorComposite == null) {
			//create new parameterEditorComposite
			parameterEditorComposite = new Composite(parameterEditorsHolderComposite, SWT.NONE);
			GridLayout parameterEditorsCompositeGridLayout = new GridLayout();
			parameterEditorsCompositeGridLayout.numColumns = 2;
			parameterEditorsCompositeGridLayout.marginWidth = 0;
			parameterEditorsCompositeGridLayout.marginHeight = 3;
			parameterEditorsCompositeGridLayout.verticalSpacing = 2;
			parameterEditorsCompositeGridLayout.horizontalSpacing = 2;
			parameterEditorComposite.setLayout(parameterEditorsCompositeGridLayout);
			parameterEditorCompositeMap.put(operationName, parameterEditorComposite);

			//register new list of parameter editors
			final ArrayList<OperationParameterEditor> parameterEditorList = new ArrayList<OperationParameterEditor>();
			parameterEditorsMap.put(operationName, parameterEditorList);

			if (operation != null) {
				for (OperationParameter operationParameter : operation.getParameters()) {
					OperationParameterEditor operationParameterEditor;
					switch (operationParameter.getType()) {
					case Text:
						operationParameterEditor = new TextOperationParameterEditor(
								operationParameter,
								parameterEditorComposite);
						break;
					case Number:
						operationParameterEditor = new NumericOperationParameterEditor(
								operationParameter,
								parameterEditorComposite);
						break;
					case Boolean:
						operationParameterEditor = new BooleanOperationParameterEditor(
								operationParameter,
								parameterEditorComposite);
						break;
					case Uri:
						operationParameterEditor = new UriOperationParameterEditor(
								operationParameter,
								parameterEditorComposite);
						break;
					case Region:
						operationParameterEditor = new RegionOperationParameterEditor(
								operationParameter,
								parameterEditorComposite);
						((RegionOperationParameterEditor) operationParameterEditor).
						setRegionParameter(regionParameterManager.findParameter(operation));
						break;
					case Option:
						operationParameterEditor = new OptionOperationParameterEditor(
								operationParameter,
								parameterEditorComposite);
						break;
					case StepDirection:
						operationParameterEditor = new StepDirectionOperationParameterEditor(
								operationParameter,
								parameterEditorComposite);
						break;
					default:
						operationParameterEditor = new DefaultOperationParameterEditor(
								operationParameter,
								parameterEditorComposite);
					break;
					}

					operationParameterEditor.addChangeListener(parameterEditorChangeListener);
					operationParameterEditor.addApplyParameterListener(applyParametersListener);
					parameterEditorList.add(operationParameterEditor);
				}
			}

		} else {
			if (operation != null) {
				//update existed editors with parameters of selected operation
				final List<OperationParameter> parameters = operation.getParameters();
				final List<OperationParameterEditor> parameterEditorList = parameterEditorsMap.get(operationName);
				for (int i = 0; i < parameters.size(); i++) {
					OperationParameter parameter = (OperationParameter) parameters.get(i);
					final OperationParameterEditor operationParameterEditor = parameterEditorList.get(i);
					operationParameterEditor.setOperationParameter(parameter);
					operationParameterEditor.loadData();
				}
			}

		}

		if (operation != null) {
			operation.updateStatus();
			updateParametersButtons(operation);
		}

		//define parameterEditorComposite which contains parameter editors of selected operation
		parameterEditorsHolderStackLayout.topControl = parameterEditorComposite;
		parameterEditorComposite.layout();

		parameterEditorsHolderComposite.layout();
		parent.layout();

		propertiesComposite.pack();
		final Point propertiesCompositeSize = propertiesComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		final Point size = plotAndOperationPropertiesSashForm.getSize();
//		operationPropertiersScrolledComposite.setMinSize(propertiesCompositeSize);
//		operationPropertiersScrolledComposite.setMinSize(size);
		operationPropertiersScrolledComposite.layout();
//		final int topFormPracentage = Math.min(95, (propertiesCompositeSize.y + 23) * 100/size.y);// limit height to 95% to avoid sash API errors and show reasonable size. 
//		plotAndOperationPropertiesSashForm.setWeights(new int[]{topFormPracentage, 100 - topFormPracentage});
		plotAndOperationPropertiesSashForm.setWeights(new int[]{100});
//		operationPropertiersScrolledComposite.pack();
	}

	private void updateOperationCompositeListSelection() {
		for (OperationComposite operationComposite : operationCompositeList) {
			operationComposite.setSelected(selectedOperationComposite == operationComposite);
		}
	}

	public void setFocus() {
		operationsComposite.setFocus();
	}

//	private final class AutoApplyRegionChangesListener implements RegionListener {
//		private String operationName;
//
//		public AutoApplyRegionChangesListener(String operationName) {
//			this.operationName = operationName;
//		}
//
//		public void regionAdded(UIRegion region) {
//			applyRegionChanges();
//		}
//
//		public void regionRemoved(UIRegion region) {
//			applyRegionChanges();
//		}
//
//		public void regionUpdated(UIRegion region) {
//			applyRegionChanges();
//		}
//
//		private void applyRegionChanges() {
//			Operation operation = algorithmTask.getOperationManager(algorithmTask.getSelectedDataItemIndex())
//			.getOperation(operationName);
//
//			if (operation != null) {
//				applyChangedParameters(operation);
//			}
//		}
//	}

	private final class ExportListener implements SelectionListener {
		private Exporter exporter;

		public ExportListener(Exporter exporter) {
			this.exporter = exporter;
		}

		public void widgetDefaultSelected(SelectionEvent e) {
		}

		public void widgetSelected(SelectionEvent e) {

			final String formatName = exporter.getFormater().getName();
			final URI fileURI = (new File(Util.saveFilenameFromShell(e.widget.getDisplay().
					getActiveShell(), formatName, "'" + formatName + "' data file"))).toURI();

			System.out.println("Export data for operation '" + selectedOperationComposite.getOperation().getName() +
					"' to file '" + fileURI + "'...");
			try {
				exporter.signalExport(selectedOperationComposite.getOperation().getOutputData(),
						fileURI);
			} catch (ExportException e1) {
				handleException(e1);
			} 
		}
	}

	private final class OperationSelectionListener implements SelectionListener {
		public void widgetDefaultSelected(SelectionEvent e) {
		}

		public void widgetSelected(SelectionEvent e) {

			OperationComposite operationComposite = (OperationComposite) e.widget;
			if (operationComposite.isSelected()) {
				setSelectedOperationComposite(operationComposite);
			}
		}
	}

	public void setSelectedOperationComposite(OperationComposite operationComposite) {
		if (selectedOperationComposite != operationComposite) {
			if (selectedOperationComposite != null) {
				selectedOperationComposite.getOperation().removeOperationDataListener(
						operationDataListener);
			}

			selectedOperationComposite = operationComposite;
			Operation selectedOperation = null;

			if (selectedOperationComposite != null) {
				selectedOperation = selectedOperationComposite.getOperation();
				selectedOperation.addOperationDataListener(operationDataListener);
			}

			updateOperationCompositeListSelection();
			updateTabTitles(selectedOperation);
			updateOperationParameters(selectedOperation);
			updateOperationOptions(selectedOperation != null ? selectedOperation.getOptions() : null);
			updateOperationExportAction(selectedOperation);
			updateOperationClonePlotAction(selectedOperation);
		}		
		if (operationPropertiesTabFolder.getMinimized()){
			operationPropertiesTabFolder.setMinimized(false);
			operationPropertiesTabFolder.setMaximized(false);
			operationPropertiesTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
					false));
			operationPropertiesTabFolder.layout(true);
			parent.layout();
		}
	}

	private void updateTabTitles(Operation selectedOperation) {
		operationPropertiesTabItem.setText(selectedOperation != null ? selectedOperation.getUILabel() + " - Operation properties" : "Operation properties");
	}

	private void updateOperationExportAction(Operation selectedOperation) {
		if (exportButton != null && !exportButton.isDisposed())
			exportButton.setEnabled(selectedOperation != null && selectedOperation.getOutputData() != null);
	}

	private void updateOperationClonePlotAction(Operation selectedOperation){
		if (clonePlotButton != null && !clonePlotButton.isDisposed())
		clonePlotButton.setEnabled(selectedOperation != null && selectedOperation.getOutputData() != null);
	}
	
	private void updateOperationOptions(OperationOptions operationOptions) {
//		skipOptionCheckbox.setEnabled(operationOptions != null ? operationOptions.isSkipSupported() : false);
//		skipOptionCheckbox.setSelection(operationOptions != null ? operationOptions.isSkipped() : false);

//		enableOptionCheckbox.setEnabled(operationOptions != null ? operationOptions.isEnableSupported() : false);
//		enableOptionCheckbox.setSelection(operationOptions != null ? operationOptions.isEnabled() : false);

//		if (operationOptions != null)
//			skipOptionCheckbox.setText(operationOptions.getTitle());
		stopOptionCheckbox.setEnabled(operationOptions != null ? operationOptions.isStopAfterCompleteSupported() : false);
		stopOptionCheckbox.setSelection(operationOptions != null ? operationOptions.isStopAfterComplete() : false);
//		if (operationOptions != null)
//			skipOptionCheckbox.setText(operationOptions.getTitle());
	}

	public void setSelectedOperationComposit(String operationName) {
		for (OperationComposite operationComposite : operationCompositeList) {
			if (operationComposite.getOperation().getName().equals(operationName)) {
				setSelectedOperationComposite(operationComposite);
				break;
			}
		}
	}

	/**
	 * Sets currently selected data item in the AlgorithmTaskEditor.
	 * @param dataItemIndex a data item index.
	 */
	private void setSelectedDataItem(final int dataItemIndex) {
		final Operation selectedOperation = selectedOperationComposite.getOperation();
		final String selectedOperationName = selectedOperation.getName();

		if (!applyAllChangedParameters()) {
			//select previous DataItem if applying failed
			dataItemCombo.select(algorithmTask.getSelectedDataItemIndex());
			return;
		}

		//deactivate RegionOperationParameters of previously selected DataItem
		//to avoid updating by new data when user modifies another DataItem's parameters
		final List<Operation> operations = algorithmTask.getOperationManager(algorithmTask.getSelectedDataItemIndex()).getOperations();
		for (Operation operation : operations) {
			for (OperationParameter parameter : operation.getParameters()) {
				if (parameter.getType() == OperationParameterType.Region) {
					((RegionOperationParameter) parameter ).setActive(false);
				}
			}
		}

		//after complete all preparation steps - set the index to algorithmTask
		algorithmTask.setSelectedDataItem(dataItemIndex);

		//OperationManager for just selected data item
		OperationManager operationManager = algorithmTask.getOperationManager(algorithmTask.getSelectedDataItemIndex());

		//detect first no actual Operation data to run the algorithm from the operation.
		final Operation[] firstNotActualOperation = new Operation[1];

		for (Operation operation : operationManager.getOperations()) {

			//activate RegionOperationParameters of selected DataItem
			for (OperationParameter parameter : operation.getParameters()) {
				if (parameter.getType() == OperationParameterType.Region) {
					((RegionOperationParameter) parameter ).setActive(true);
				}
			}

			//find first operation with not actual data 
//			if (firstNotActualOperation[0] == null && !operation.isActual()) {
//				firstNotActualOperation[0] = operation;
//			}
		}

		Operation lastReprocessableOperation = AlgorithmTask.getOperationChainHead(
				operationManager.getOperations());
		updateOperationList(operationManager);

		setSelectedOperationComposit(selectedOperationName);

		//run Algorithm from first operation with not actual data
		if (lastReprocessableOperation != null) {
//			Display.getDefault().asyncExec(new Runnable() {
//				public void run() {
					try {
//						UIAlgorithmManager.getAlgorithmManager().getCurrentInput().
//						subscribeExceptionHandler(new ThreadExceptionListener(parent.getShell()));
						algorithmTask.runAlgorithmFromOperation(lastReprocessableOperation);
					} catch (TunerNotReadyException e) {
						handleException(e);
					} catch (TransferFailedException e) {
						handleException(e);
					}
//				}
//			});
		}
	}



	private boolean confirmParametersApplying() {
		return MessageDialog.openConfirm(parent.getShell(), "Algorithm Task",
				"Changed parameters are going to be applyed before to continue.\n" +
		"Do you want to apply changed parameters?");
	}

	/**
	 * Checks all parameters for changes and apply changes after confirmation.
	 * @return true if parameters were not changed or successfully applied 
	 * or false if applying cause an error or applying was not confirmed.
	 */
	private boolean applyAllChangedParameters() {
		boolean isParametersChanged = false;

		//check changed parameters
		for (OperationComposite operationComposite : operationCompositeList) {
			if (operationComposite.getOperation().isParametersChanged()) {
				isParametersChanged = true;
				break;
			}
		}

		if (!isParametersChanged) {
			//there are no changed parameters
			return true;
		}

		//confirm changed parameters applying
//		if (confirmParametersApplying()) {
		if (true) {
			//apply changed parameters
			try {
				for (OperationComposite operationComposite : operationCompositeList) {
					algorithmTask.applyParameterChangesForAllDataItems(operationComposite.getOperation());
				}
				//update UI
				updateOperationParameters(selectedOperationComposite != null ? 
						selectedOperationComposite.getOperation() : null);
			} catch (Exception e) {
				handleException(e);

				//select previous DataItem if failed
				dataItemCombo.select(algorithmTask.getSelectedDataItemIndex());
				return false;
			}
		} else {
			//select previous DataItem if not confirmed
			dataItemCombo.select(algorithmTask.getSelectedDataItemIndex());
			return false;
		}

		return true;

	}

	private void updateParametersButtons(final Operation operation) {
		boolean isParametersChanged = operation.isParametersChanged();
		applyParametersButton.setEnabled(isParametersChanged);
		revertParametersButton.setEnabled(isParametersChanged);

		defaultParametersButton.setEnabled(!operation.isDefaultParametersLoaded());
	}

	public void dispose() {
		super.dispose();
		if (!parent.isDisposed())
			parent.dispose();
		if (propertiesBorderColor != null) {
			propertiesBorderColor.dispose();
		}
		for (OperationComposite composite : operationCompositeList){
			if (!composite.isDisposed())
				composite.dispose();
		}
		operationCompositeList.clear();
		operationCompositeList = null;
		selectedOperationComposite = null;
		for (AlgorithmInput input : algorithmTask.getAlgorithmInputs())
			UIAlgorithmManager.getAlgorithmManager().unloadAlgorithm(input.getAlgorithm());
		ProjectManager.removeAlgorithmTask(algorithmTask.getId());
		algorithmTask.dispose();
		algorithmTask = null;
		System.out.println("Algorithm Task Manager Disposed");
	}

	private void applyChangedParameters(Operation operation) {

		//apply changes for all DataItems
		try {
			algorithmTask.applyParameterChangesForAllDataItems(operation);
		} catch (Exception e1) {
			handleException(e1);
			return;
		}

//		try {
//			algorithmTask.runAlgorithmFromOperation(operation);
//		} catch (TunerNotReadyException e1) {
//			handleException(e1);
//		} catch (TransferFailedException e1) {
//			handleException(e1);
//		}
	}

	/**
	 * Runs algorithm for selected data item.
	 */
	public void runAlgorithm() {
		if (applyAllChangedParameters()) {
			try {
//				UIAlgorithmManager.getAlgorithmManager().getCurrentInput().subscribeExceptionHandler(
//						new ThreadExceptionListener(parent.getShell()));
				algorithmTask.runAlgorithm();
			} catch (Exception e1) {
				handleException(e1);
			}
		}
	}

	/**
	 * 
	 */
	private void applyParametersForSelectedOperation() {
		final Operation operation = selectedOperationComposite.getOperation();
		applyChangedParameters(operation);
		updateOperationParameters(operation);
	}

	public void setFileUri(URI fileUri) {
		algorithmTask.setFileUri(fileUri);
	}

	public void addInterruptAlgorithmListener(SelectionListener listener){
		interruptAlgorithmButton.addSelectionListener(listener);
	}

	public void removeInterruptAlgorithmListener(SelectionListener listener){
		interruptAlgorithmButton.removeSelectionListener(listener);
	}
}

