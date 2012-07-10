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
package au.gov.ansto.bragg.kakadu.ui.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.cicada.core.Algorithm.AlgorithmStatus;
import au.gov.ansto.bragg.cicada.core.exception.TransferFailedException;
import au.gov.ansto.bragg.cicada.core.exception.TunerNotReadyException;
import au.gov.ansto.bragg.kakadu.core.AlgorithmTask;
import au.gov.ansto.bragg.kakadu.core.AlgorithmTaskStatusListener;
import au.gov.ansto.bragg.kakadu.core.OperationManager;
import au.gov.ansto.bragg.kakadu.core.data.Operation;
import au.gov.ansto.bragg.kakadu.core.data.OperationParameter;
import au.gov.ansto.bragg.kakadu.ui.Activator;
import au.gov.ansto.bragg.kakadu.ui.ProjectManager;
import au.gov.ansto.bragg.kakadu.ui.SWTResourceManager;
import au.gov.ansto.bragg.kakadu.ui.editors.AlgorithmTaskEditor;
import au.gov.ansto.bragg.kakadu.ui.editors.BooleanOperationParameterEditor;
import au.gov.ansto.bragg.kakadu.ui.editors.DefaultOperationParameterEditor;
import au.gov.ansto.bragg.kakadu.ui.editors.NumericOperationParameterEditor;
import au.gov.ansto.bragg.kakadu.ui.editors.OperationParameterEditor;
import au.gov.ansto.bragg.kakadu.ui.editors.OptionOperationParameterEditor;
import au.gov.ansto.bragg.kakadu.ui.editors.RegionOperationParameterEditor;
import au.gov.ansto.bragg.kakadu.ui.editors.StepDirectionOperationParameterEditor;
import au.gov.ansto.bragg.kakadu.ui.editors.TextOperationParameterEditor;
import au.gov.ansto.bragg.kakadu.ui.editors.UriOperationParameterEditor;
import au.gov.ansto.bragg.kakadu.ui.region.RegionParameterManager;
import au.gov.ansto.bragg.kakadu.ui.util.DisplayManager;

/**
 * The view displays operation parameters for 
 * all operations in current AlgorithmTask.
 * 
 * @author Danil Klimontov (dak)
 */
public class OperationParametersView extends ViewPart {

	protected Composite parametersComposite;
	protected Composite parameterEditorsHolderComposite;
	protected Button defaultParametersButton;
	protected Button applyParametersButton;
	protected Button revertParametersButton;
	protected Button stopOperationButton;
	protected Button startOperationButton;	
	protected ScrolledComposite parameterEditorsScrolledComposite;
	private AlgorithmTaskStatusListener statusListener;
	private static List<ButtonClickListener> stopButtonListenerList;

	private final OperationParameterEditor.ChangeListener parameterEditorChangeListener = new OperationParameterEditor.ChangeListener() {
		public void dataChanged(Object oldData, Object newData) {
			updateParametersButtons();
		}
	};
	
	private final SelectionListener applyParametersListener = new SelectionListener() {
		public void widgetDefaultSelected(SelectionEvent e) {
			applyParameters();
		}
		public void widgetSelected(SelectionEvent e) {
			applyParameters();

		}
	};
	
	//Associate the view with active AlgorithmTaskEditor
	final IPartListener2 catchAlgorithmTaskEditorPartListener = new IPartListener2(){


		public void partActivated(IWorkbenchPartReference partRef) {
			final IWorkbenchPart part = partRef.getPart(false);
			if (part != null && part instanceof AlgorithmTaskEditor) {
				final AlgorithmTask algorithmTask = ((AlgorithmTaskEditor) part).getAlgorithmTask();
				if (OperationParametersView.this.algorithmTask != algorithmTask) {
					setAlgorithmTask(algorithmTask);
				}
			}
		}

		public void partBroughtToTop(IWorkbenchPartReference partRef) {
		}

		public void partClosed(IWorkbenchPartReference partRef) {
			final IWorkbenchPart part = partRef.getPart(false);
			if (part != null && part instanceof AlgorithmTaskEditor) {
				setAlgorithmTask(null);
			}
		}

		public void partDeactivated(IWorkbenchPartReference partRef) {
		}

		public void partHidden(IWorkbenchPartReference partRef) {
		}

		public void partInputChanged(IWorkbenchPartReference partRef) {
		}

		public void partOpened(IWorkbenchPartReference partRef) {
		}

		public void partVisible(IWorkbenchPartReference partRef) {
		}
		
	};

	/**
	 * The listener is used to load OperationParameters objects to editors if DataItem index was changed.
	 */
	final AlgorithmTask.AlgorithmTaskListener algorithmTaskListener = new AlgorithmTask.AlgorithmTaskListener() {
		public void selectedDataItemIndexChanged(AlgorithmTask algorithmTask, int newIndex) {
			loadParameters();
		}
	};

	
	protected AlgorithmTask algorithmTask;
	protected List<Operation> operations;
	protected Composite parent;
	private final Map<String, List<OperationParameterEditor>> parameterEditorsMap = 
		new HashMap<String, List<OperationParameterEditor>>();

	/**
	 * Creates a new view.
	 */
	public OperationParametersView() {
	}

	public void createPartControl(Composite parent) {
		SWTResourceManager.registerResourceUser(parent);
		
		this.parent = parent;
		parent.setLayout(new FillLayout());
		
		parametersComposite = new Composite(parent, SWT.NONE);
		GridData data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		parametersComposite.setLayoutData (data);
		GridLayout parametersCompositeGridLayout = new GridLayout();
		parametersCompositeGridLayout.numColumns = 1;
		parametersCompositeGridLayout.verticalSpacing = 0;
		parametersCompositeGridLayout.marginHeight = 3;
		parametersCompositeGridLayout.marginWidth = 3;
		parametersComposite.setLayout(parametersCompositeGridLayout);
		
		parameterEditorsScrolledComposite = new ScrolledComposite(parametersComposite, SWT.V_SCROLL | SWT.H_SCROLL);
		parameterEditorsScrolledComposite.setExpandHorizontal(true);
		parameterEditorsScrolledComposite.setExpandVertical(true);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		parameterEditorsScrolledComposite.setLayoutData (data);
		
		parameterEditorsHolderComposite = new Composite(parameterEditorsScrolledComposite, SWT.NONE);
		final GridLayout parameterEditorHolderCompositeGridLayout = new GridLayout();
		parameterEditorHolderCompositeGridLayout.marginHeight = 0;
		parameterEditorHolderCompositeGridLayout.marginWidth = 0;
		parameterEditorHolderCompositeGridLayout.verticalSpacing = 3;
		parameterEditorHolderCompositeGridLayout.horizontalSpacing = 0;
		parameterEditorsHolderComposite.setLayout(parameterEditorHolderCompositeGridLayout);
		
		parameterEditorsScrolledComposite.setContent(parameterEditorsHolderComposite);
		
		Label parameterGroupSeparator = new Label(parametersComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		parameterGroupSeparator.setLayoutData (data);
		
		Composite parameterGroupButtonsComposite = new Composite(parametersComposite, SWT.NONE);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
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

		Composite controlGroupButtonsComposite = new Composite(parametersComposite, SWT.NONE);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		controlGroupButtonsComposite.setLayoutData (data);
		GridLayout controlGroupButtonsCompositeGridLayout = new GridLayout();
		controlGroupButtonsCompositeGridLayout.numColumns = 2;
		controlGroupButtonsCompositeGridLayout.marginWidth = 0;
		controlGroupButtonsCompositeGridLayout.marginHeight = 0;
		controlGroupButtonsCompositeGridLayout.marginTop = 3;
		controlGroupButtonsComposite.setLayout(controlGroupButtonsCompositeGridLayout);
		
		stopOperationButton = new Button(controlGroupButtonsComposite, SWT.PUSH);
		stopOperationButton.setImage(Activator.getImageDescriptor("icons/Stop-Normal-Red-128x128.png").createImage());
		stopOperationButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					// Action: interrupt SICS
					algorithmTask.interrupt();
					if (stopButtonListenerList != null)
						for (ButtonClickListener listener : stopButtonListenerList){
							listener.onClick();
						}
				} catch (Exception e1) {
					LoggerFactory.getLogger(OperationParametersView.class).error(
							"Failed to send interrupt.", e);
				}
			}
		});
		startOperationButton = new Button(controlGroupButtonsComposite, SWT.PUSH);
		startOperationButton.setImage(Activator.getImageDescriptor("icons/Play-Normal-128x128.png").createImage());
		startOperationButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					// Action: interrupt SICS
					for (Operation operation : operations) 
						algorithmTask.applyParameterChangesForAllDataItems(operation);
					algorithmTask.runAlgorithm();
				} catch (Exception e1) {
					LoggerFactory.getLogger(OperationParametersView.class).error(
							"Failed to run the algorithm.", e);
				}
			}
		});

		initListeners();
		setAlgorithmTask(ProjectManager.getCurrentAlgorithmTask());
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
				revertParameters();
			}
		});
		applyParametersButton.addSelectionListener(applyParametersListener);
		
	
		//the PartListener listener must be added after active workbenchPage was created.
		//to ensure it the operation should be performed in asyncExec block  
		DisplayManager.getDefault().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow workbenchWindow = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
				final IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
//				workbenchPage.addPartListener(catchAlgorithmTaskEditorPartListener);
			}
		});


	}

	protected void loadDefaulParameters() {
		for (Operation operation : operations) {
			operation.resetParametersToDefault();
		}
		
		updateParametersData();
	}

	private void revertParameters() {
		for (Operation operation : operations) {
			operation.revertParametersChanges();
		}
		updateParametersData();
	}
	
//	private void applyParameters() {
//		
//		boolean isParametersChanged = false;
//		
//		//check is changed parameters available
//		for (Operation operation : operations) {
//			if (operation.isParametersChanged()) {
//				isParametersChanged = true;
//				break;
//			}
//		}
//		
//		Operation lastOperation = operations.get(operations.size() - 1);
//		if (!isParametersChanged) {
//			//there are no changed parameters
//			return;
//		}
//		
//		//apply changed parameters
//		try {
//			for (Operation operation : operations) {
//				algorithmTask.applyParameterChangesForAllDataItems(operation);
//			}
//		} catch (Exception e) {
//			handleException(e);
//		}
//
//		
//		//OperationManager for selected data item
//		OperationManager operationManager = algorithmTask.getOperationManager(algorithmTask.getSelectedDataItemIndex());
//		
//		List<Operation> operationList = operationManager.getOperations();
//		Operation operation = null;
//		Operation secondOperation = null;
//		Operation thirdOperation = null;
//		if (operationList != null && operationList.size() > 2){
//			operation = operationList.get(operationList.size() - 1);
//			secondOperation = operationList.get(operationList.size() - 2);
//			thirdOperation = operationList.get(operationList.size() - 3);
//		}
//		Operation targetOperation = null;
//		if (secondOperation != null && !secondOperation.isActual() && 
//				thirdOperation != null && thirdOperation.isActual())
//			targetOperation = secondOperation;
//		if (operation != null && !operation.isActual() && 
//				secondOperation != null && secondOperation.isActual())
//			targetOperation = operation;
//		if (targetOperation != null){
//			final Operation runOperation = targetOperation;
//			Display.getDefault().asyncExec(new Runnable() {
//				public void run() {
//					try {
//						algorithmTask.runAlgorithmFromOperation(runOperation);
//					} catch (TunerNotReadyException e) {
//						handleException(e);
//					} catch (TransferFailedException e) {
//						handleException(e);
//					}
//				}
//			});
//		}
//
//		
//		updateParametersData();
//	}

	private void applyParameters() {

		boolean isParametersChanged = false;

		//check is changed parameters available
		for (Operation operation : operations) {
			if (operation.isParametersChanged()) {
				isParametersChanged = true;
				break;
			}
		}

		if (!isParametersChanged) {
			//there are no changed parameters
			return;
		}

		//apply changed parameters
		try {
			for (Operation operation : operations) {
				algorithmTask.applyParameterChangesForAllDataItems(operation);
			}
		} catch (Exception e) {
			handleException(e);
		}


		//OperationManager for selected data item
		OperationManager operationManager = algorithmTask.getOperationManager(algorithmTask.getSelectedDataItemIndex());

		//detect first no actual Operation data to run the algorithm from the operation.
//		final Operation[] firstNotActualOperation = new Operation[1];
//
//		for (Operation operation : operationManager.getOperations()) {
//			//find first operation with not actual data 
//			if (firstNotActualOperation[0] == null && !operation.isActual() && operation.isReprocessable()) {
//				firstNotActualOperation[0] = operation;
//				break;
//			}
//		}
		
		final Operation lastReprocessableOperation = AlgorithmTask.getOperationChainHead(
				operationManager.getOperations());

		//run Algorithm from first operation with not actual data
		if (lastReprocessableOperation != null) {
//			Display.getDefault().asyncExec(new Runnable() {
//				public void run() {
					try {
						algorithmTask.runAlgorithmFromOperation(lastReprocessableOperation);
					} catch (TunerNotReadyException e) {
						handleException(e);
					} catch (TransferFailedException e) {
						handleException(e);
					}
//				}
//			});
		}


		updateParametersData();
	}
	/**
	 * Updates parameter editors with data from holders. 
	 */
	private void updateParametersData() {
		for (Operation operation : operations) {
			//update existed editors with parameters of selected operation
			final List<OperationParameter> parameters = operation.getParameters();
			final List<OperationParameterEditor> parameterEditorList = parameterEditorsMap.get(operation.getName());
			for (int i = 0; i < parameters.size(); i++) {
				OperationParameter parameter = parameters.get(i);
				final OperationParameterEditor operationParameterEditor = parameterEditorList.get(i);
				operationParameterEditor.setOperationParameter(parameter);
				operationParameterEditor.loadData();
			}
		}
	}
	
	public void setFocus() {
		
	}
	
	public void setAlgorithmTask(AlgorithmTask algorithmTask) {
		clearView();
		
		if (this.algorithmTask != null) {
			this.algorithmTask.removeAlgorithmTaskListener(algorithmTaskListener);
			this.algorithmTask.removeStatusListener(statusListener);
		}
		
		this.algorithmTask = algorithmTask;
		
		if (algorithmTask != null) {
			loadParameters();
			algorithmTask.addAlgorithmTaskListener(algorithmTaskListener);
		}
		
		
		statusListener = new AlgorithmTaskStatusListener(){

			public void onChange(final AlgorithmStatus status) {
				// TODO Auto-generated method stub
				DisplayManager.getDefault().asyncExec(new Runnable(){

					public void run() {
						// TODO Auto-generated method stub
						switch (status) {
						case Running:
							startOperationButton.setEnabled(false);
							startOperationButton.setImage(Activator.getImageDescriptor("icons/Play-Disabled-128x128.png").createImage());
							break;
						case Idle:
							startOperationButton.setEnabled(true);
							startOperationButton.setImage(Activator.getImageDescriptor("icons/Play-Normal-128x128.png").createImage());
							break;					
						case Interrupt:
							startOperationButton.setEnabled(true);
							startOperationButton.setImage(Activator.getImageDescriptor("icons/Play-Normal-128x128.png").createImage());
							break;					
						case Error:
							startOperationButton.setEnabled(true);
							startOperationButton.setImage(Activator.getImageDescriptor("icons/Play-Normal-128x128.png").createImage());
							break;					
						default:
							break;
						}						
					}
					
				});
			}

			public void setStage(int operationIndex, AlgorithmStatus status) {
				// TODO Auto-generated method stub
				
			}
			
		};
		algorithmTask.addStatusListener(statusListener);
	}

	private void loadParameters() {
		//TODO check is operation manager initialized if no then add listener to update the view late.
		operations = algorithmTask.getOperationManager(algorithmTask.getSelectedDataItemIndex()).getOperations();
		if (parameterEditorsMap.isEmpty()) {
			for (Operation operation : operations) {
				addOperationParameters(operation);
			}
		} else {
			updateParametersData();
		}
		updateParametersButtons();

		parameterEditorsScrolledComposite.setMinSize(parameterEditorsHolderComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private void addOperationParameters(final Operation operation) {

			//create a new group of parameters
		if (operation.getParameters().size() > 0){
			Group parameterEditorsGroup = new Group(parameterEditorsHolderComposite, SWT.NONE);
			parameterEditorsGroup.setText(operation.getUILabel());
			parameterEditorsGroup.setLayoutData(new GridData(GridData.FILL, SWT.CENTER, true, false));
			GridLayout parameterEditorsCompositeGridLayout = new GridLayout();
			parameterEditorsCompositeGridLayout.numColumns = 2;
			parameterEditorsCompositeGridLayout.marginWidth = 2;
			parameterEditorsCompositeGridLayout.marginBottom = 0;
			parameterEditorsCompositeGridLayout.marginHeight = 2;
			parameterEditorsCompositeGridLayout.marginTop = 0;
			parameterEditorsCompositeGridLayout.verticalSpacing = 2;
			parameterEditorsGroup.setLayout(parameterEditorsCompositeGridLayout);
			//			parameterEditorCompositeMap.put(operationName, parameterEditorComposite);

			//register new list of parameter editors
			final ArrayList<OperationParameterEditor> parameterEditorList = new ArrayList<OperationParameterEditor>();
			parameterEditorsMap.put(operation.getName(), parameterEditorList);

			if (operation != null) {
				for (OperationParameter operationParameter : operation.getParameters()) {
					OperationParameterEditor operationParameterEditor;
					switch (operationParameter.getType()) {
					case Text:
						operationParameterEditor = new TextOperationParameterEditor(
								operationParameter,
								parameterEditorsGroup);
						break;
					case Number:
						operationParameterEditor = new NumericOperationParameterEditor(
								operationParameter,
								parameterEditorsGroup);
						break;
					case Boolean:
						operationParameterEditor = new BooleanOperationParameterEditor(
								operationParameter,
								parameterEditorsGroup);
						break;
					case Uri:
						operationParameterEditor = new UriOperationParameterEditor(
								operationParameter,
								parameterEditorsGroup);
						break;
					case Region:
						operationParameterEditor = new RegionOperationParameterEditor(
								operationParameter,
								parameterEditorsGroup);
						((RegionOperationParameterEditor) operationParameterEditor).setRegionParameter(
								((RegionParameterManager) algorithmTask.getRegionParameterManager()
										).findParameter(operation));
						break;
					case Option:
						operationParameterEditor = new OptionOperationParameterEditor(
								operationParameter,
								parameterEditorsGroup);
						break;
					case StepDirection:
						operationParameterEditor = new StepDirectionOperationParameterEditor(
								operationParameter,
								parameterEditorsGroup);								
						break;
					default:
						operationParameterEditor = new DefaultOperationParameterEditor(
								operationParameter,
								parameterEditorsGroup);
					break;
					}

					operationParameterEditor.addChangeListener(parameterEditorChangeListener);
					operationParameterEditor.addApplyParameterListener(applyParametersListener);
					parameterEditorList.add(operationParameterEditor);
				}
			}

			//		} else {
			//			if (operation != null) {
			//				//update existed editors with parameters of selected operation
			//				final List<OperationParameter> parameters = operation.getParameters();
			//				final List<OperationParameterEditor> parameterEditorList = parameterEditorsMap.get(operationName);
			//				for (int i = 0; i < parameters.size(); i++) {
			//					OperationParameter parameter = (OperationParameter) parameters.get(i);
			//					final OperationParameterEditor operationParameterEditor = parameterEditorList.get(i);
			//					operationParameterEditor.setOperationParameter(parameter);
			//					operationParameterEditor.loadData();
			//				}
			//			}
			//			
			//		}


			//define parameterEditorComposite which contains parameter editors of selected operation
			parameterEditorsGroup.layout();

			parameterEditorsHolderComposite.layout();
			//		parent.layout();

			//		final Point propertiesCompositeSize = propertiesComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			//		final Point size = plotAndOperationPropertiesSashForm.getSize();
			//		operationPropertiersScrolledComposite.setMinSize(propertiesCompositeSize);
			//		operationPropertiersScrolledComposite.layout();
		}
	}

	private void updateParametersButtons() {
		boolean isParametersChanged = false;
		boolean isDefaultParametersLoaded = true;
		for (Operation operation : operations) {
			operation.updateStatus();
			isParametersChanged |= operation.isParametersChanged();
			isDefaultParametersLoaded &= operation.isDefaultParametersLoaded();
		}
		
		applyParametersButton.setEnabled(isParametersChanged);
		revertParametersButton.setEnabled(isParametersChanged);
		defaultParametersButton.setEnabled(!isDefaultParametersLoaded);
		
	}

	private void handleException(Throwable throwable) {
		throwable.printStackTrace();
		showErrorMessage(throwable.getMessage());
	}
	
	private void showErrorMessage(String message) {
		MessageDialog.openError(
			parent.getShell(),
			"Operation Parameters View",
			message);
	}
	

	private void clearView() {
		for (List<OperationParameterEditor> list : parameterEditorsMap.values()) {
			for (OperationParameterEditor operationParameterEditor : list) {
				operationParameterEditor.removeChangeListener(parameterEditorChangeListener);
			}
		}
		parameterEditorsMap.clear();
		
		if (!parameterEditorsHolderComposite.isDisposed()) {
			for (Control control : parameterEditorsHolderComposite
					.getChildren()) {
				if (!control.isDisposed()) {
					control.dispose();
				}
			}
		}
	}
	
	
	public void dispose() {
		clearView();
		
		super.dispose();

		IWorkbenchWindow workbenchWindow = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
		final IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
		
		if (workbenchPage != null) {
			workbenchPage.removePartListener(catchAlgorithmTaskEditorPartListener);
		}
	}

	public static void subscribeStopButtonListener(ButtonClickListener listener){
		if (stopButtonListenerList == null)
			stopButtonListenerList = new ArrayList<ButtonClickListener>();
		stopButtonListenerList.add(listener);
	}
	
	public static void unsubscribeStopButtonListener(ButtonClickListener listener){
		if (stopButtonListenerList != null)
			stopButtonListenerList.remove(listener);
	}
}
