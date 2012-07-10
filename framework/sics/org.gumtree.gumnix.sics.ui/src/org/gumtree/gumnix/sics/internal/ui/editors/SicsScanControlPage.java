package org.gumtree.gumnix.sics.internal.ui.editors;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.control.controllers.IScanController;
import org.gumtree.gumnix.sics.control.events.IScanControllerListener;
import org.gumtree.gumnix.sics.control.events.ScanControllerListenerAdapter;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.core.SicsCoreException;
import org.gumtree.gumnix.sics.core.SicsUtils;
import org.gumtree.gumnix.sics.internal.ui.Activator;
import org.gumtree.gumnix.sics.io.SicsIOException;

import ch.psi.sics.hipadaba.Component;

public class SicsScanControlPage extends FormPage {

//	private enum ScanMode {
//		MONITOR("monitor"), TIMER("timer");
//
//		private ScanMode(String modeString) {
//			this.modeString = modeString;
//		}
//		public String getModeString() {
//			return modeString;
//		}
//		public static ScanMode getScanMode(String modeString) {
//			for(ScanMode mode : values()) {
//				if(mode.getModeString().equals(modeString)) {
//					return mode;
//				}
//			}
//			return null;
//		}
//		private String modeString;
//	}

	public static final String ID = "control";

	private static final String TITLE = "Scan Control";

	private static Image IMAGE_ARROW;

	private static Image IMAGE_RUN;

	static {
		SafeRunner.run(new ISafeRunnable() {
			public void handleException(Throwable exception) {
			}
			public void run() throws Exception {
				IMAGE_ARROW = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/elcl16/forward_nav.gif").createImage();
				IMAGE_RUN = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/etool16/run_exc.gif").createImage();
			}
		});

	}

	private IScanController controller;

	private IScanControllerListener controllerListener;

	private FormToolkit toolkit;

	private DataBindingContext bindingContext;

	private ComboViewer modeComboViewer;

	private Text presetText;

	private Spinner npSpinner;

	private Spinner channelSpinner;

	private Text scanVariableText;

	private Text pathText;

	private Text currentValueText;

	private Text startValueText;

	private Text incrementText;

	private Label unitLabel;

	private Spinner repeatSpinner;

	private ComponentFilteredTree tree;

	private Button repeatButton;

	public SicsScanControlPage(FormEditor editor) {
		super(editor, ID, TITLE);
	}

	protected void createFormContent(IManagedForm managedForm) {
		Composite parent = managedForm.getForm().getBody();

		parent.setLayout(new GridLayout());
//		bindingContext = BasicBindingFactory.createContext(parent);
		toolkit = managedForm.getToolkit();

		Composite settingComposite = getToolkit().createComposite(parent);
		settingComposite.setLayout(new GridLayout(2, true));
		settingComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		createScanSetting(settingComposite);
		createRepeatSetting(settingComposite);

		createScanVariableArea(parent);
		createRunButton(parent);

		getScanController().addComponentListener(getControllerListener());

		updateValue();

	}

	private void createScanSetting(Composite parent) {
		Section section = getToolkit().createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
		Composite composite = getToolkit().createComposite(section);
		composite.setLayout(new GridLayout(3, false));
		section.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		section.setClient(composite);
		section.setText("Scan Setting");

		Label label = getToolkit().createLabel(composite, "Mode ", SWT.RIGHT);
		modeComboViewer = new ComboViewer(composite, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		modeComboViewer.setContentProvider(new ArrayContentProvider());
		modeComboViewer.setLabelProvider(new LabelProvider());
		try {
			modeComboViewer.setInput(getScanController().config().getAvailableModes());
		} catch (SicsCoreException e) {
			e.printStackTrace();
		}
		modeComboViewer.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		((GridData)modeComboViewer.getCombo().getLayoutData()).horizontalSpan = 2;
		modeComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object selection = ((IStructuredSelection)event.getSelection()).getFirstElement();
				if(selection instanceof String) {
					String modeString = (String)selection;
					getScanController().config().setMode(modeString);
					if(modeString.equals("monitor")) {
						unitLabel.setText("counts");
					} else if(modeString.equals("timer")) {
						unitLabel.setText("sec");
					}
				}
			}
		});

		label = getToolkit().createLabel(composite, "Preset ", SWT.RIGHT);
		presetText = getToolkit().createText(composite, "");
		presetText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//		getBindingContext().bind(presetText, new Property(getScanController().config(), "preset"), null);
		unitLabel = getToolkit().createLabel(composite, "count");

		Realm.runWithDefault(SWTObservables.getRealm(PlatformUI.getWorkbench()
				.getDisplay()), new Runnable() {
			public void run() {
				DataBindingContext bindingContext = new DataBindingContext();
				bindingContext.bindValue(SWTObservables.observeText(presetText,
						SWT.Modify), BeansObservables.observeValue(
								getScanController().config(), "preset"), new UpdateValueStrategy(), new UpdateValueStrategy());
			}
		});
		
		label = getToolkit().createLabel(composite, "NP ", SWT.RIGHT);
		npSpinner = new Spinner(composite, SWT.BORDER);
		npSpinner.setMinimum(0);
		npSpinner.setMinimum(10000);
		npSpinner.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getScanController().config().setNumberOfPoint(npSpinner.getSelection());
			}
		});
		npSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		((GridData)npSpinner.getLayoutData()).horizontalSpan = 2;

		label = getToolkit().createLabel(composite, "Channel ", SWT.RIGHT);
		channelSpinner = new Spinner(composite, SWT.BORDER);
		try {
			channelSpinner.setMinimum(getScanController().config().getMinimumChannelSize());
			channelSpinner.setMaximum(getScanController().config().getMaximumChannelSize());
		} catch (SicsCoreException e) {
			e.printStackTrace();
		}
		channelSpinner.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getScanController().config().setChannel(channelSpinner.getSelection());
			}
		});
		channelSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		((GridData)channelSpinner.getLayoutData()).horizontalSpan = 2;
	}

	private void createRepeatSetting(Composite parent) {
		Section section = getToolkit().createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
		Composite composite = getToolkit().createComposite(section);
		composite.setLayout(new GridLayout(2, false));
		section.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		section.setClient(composite);
		section.setText("Repeat");
		repeatButton = getToolkit().createButton(composite, "Repeat", SWT.CHECK);
		repeatButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		((GridData)repeatButton.getLayoutData()).horizontalSpan = 2;
		repeatButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				repeatSpinner.setEnabled(repeatButton.getSelection());
			}
		});
		repeatButton.setSelection(false);
		repeatButton.setEnabled(false);

		repeatSpinner = new Spinner(composite, SWT.BORDER);
		repeatSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		repeatSpinner.setEnabled(false);
		getToolkit().createLabel(composite, "times");
	}

	private void createScanVariableArea(Composite parent) {
		Section section = getToolkit().createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
		Composite composite = getToolkit().createComposite(section);
		composite.setLayout(new GridLayout(3, false));
		section.setClient(composite);
		section.setText("Scan Variable Setting");
		section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridData fillGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);

		Composite treeComposite = getToolkit().createComposite(composite);
		treeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		try {
			tree = new ComponentFilteredTree("type", getScanController().config().getScanVariableType());
			tree.createTreeControl(treeComposite);
			tree.addDoubleClickListener(new IDoubleClickListener() {
				public void doubleClick(DoubleClickEvent event) {
					if(tree.getSelectedVariable() != null) {
						getScanController().config().setVariable(tree.getSelectedVariable());
						updateValue();
					}
				}
			});
		} catch (SicsCoreException e) {
			e.printStackTrace();
		}

		Button button = getToolkit().createButton(composite, "", SWT.FLAT);
		button.setImage(IMAGE_ARROW);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(tree.getSelectedVariable() != null) {
					getScanController().config().setVariable(tree.getSelectedVariable());
					updateValue();
				}
			}
		});

		Composite settingComposite = getToolkit().createComposite(composite);
		settingComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createScanVariableSetting(settingComposite);
	}

	private void createScanVariableSetting(Composite parent) {
		parent.setLayout(new GridLayout(2, false));
		GridData fillGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);

		Label label = getToolkit().createLabel(parent, "Scan Variable ", SWT.RIGHT);
		scanVariableText = getToolkit().createText(parent, "");
		scanVariableText.setEditable(false);
		scanVariableText.setLayoutData(fillGridData);
//		getBindingContext().bind(scanVariableText, new Property(getScanController().config(), "variable"), null);

		label = getToolkit().createLabel(parent, "Path ", SWT.RIGHT);
		pathText = getToolkit().createText(parent, "");
		pathText.setEditable(false);
		pathText.setLayoutData(fillGridData);

		label = getToolkit().createLabel(parent, "Current Value ", SWT.RIGHT);
		currentValueText = getToolkit().createText(parent, "");
		currentValueText.setEditable(false);
		currentValueText.setLayoutData(fillGridData);

		label = getToolkit().createLabel(parent, "Start Value ", SWT.RIGHT);
		startValueText = getToolkit().createText(parent, "");
		startValueText.setLayoutData(fillGridData);
//		getBindingContext().bind(startValueText, new Property(getScanController().config(), "startValue"), null);

		label = getToolkit().createLabel(parent, "Increment ", SWT.RIGHT);
		incrementText = getToolkit().createText(parent, "");
		incrementText.setLayoutData(fillGridData);
//		getBindingContext().bind(incrementText, new Property(getScanController().config(), "increment"), null);

		label = getToolkit().createLabel(parent, "Preview ");
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		((GridData)label.getLayoutData()).horizontalSpan = 2;

		ListViewer listViewer = new ListViewer(parent, SWT.BORDER);
		listViewer.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		((GridData)listViewer.getList().getLayoutData()).horizontalSpan = 2;
		
		Realm.runWithDefault(SWTObservables.getRealm(PlatformUI.getWorkbench()
				.getDisplay()), new Runnable() {
			public void run() {
				DataBindingContext bindingContext = new DataBindingContext();
				bindingContext.bindValue(SWTObservables.observeText(scanVariableText,
						SWT.Modify), BeansObservables.observeValue(
								getScanController().config(), "variable"), new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(SWTObservables.observeText(startValueText,
						SWT.Modify), BeansObservables.observeValue(
								getScanController().config(), "startValue"), new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(SWTObservables.observeText(incrementText,
						SWT.Modify), BeansObservables.observeValue(
								getScanController().config(), "increment"), new UpdateValueStrategy(), new UpdateValueStrategy());
			}
		});
	}

	private void createRunButton(Composite parent) {
		Button runButton = getToolkit().createButton(parent, "Run", SWT.PUSH);
		runButton.setImage(IMAGE_RUN);
		runButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		runButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				commitAndRun();
			}
		});
	}

	public void dispose() {
		if(controllerListener != null) {
			getScanController().removeComponentListener(controllerListener);
			controllerListener = null;
		}
		super.dispose();
	}

	private IScanController getScanController() {
		if(controller == null) {
			Object componentController = getEditorInput().getAdapter(IComponentController.class);
			if(componentController instanceof IScanController) {
				controller = (IScanController)componentController;
			}
		}
		return controller;
	}

	private FormToolkit getToolkit() {
		return toolkit;
	}

	private IScanControllerListener getControllerListener() {
		if(controllerListener == null) {
			controllerListener = new ScanControllerListenerAdapter() {
				public void scanConfigUpdated() {
					updateValue();
				}
			};
		}
		return controllerListener;
	}

	private void updateValue() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				SafeRunner.run(new ISafeRunnable() {
					public void handleException(Throwable exception) {
					}
					public void run() throws Exception {
						modeComboViewer.setSelection(new StructuredSelection(getScanController().config().getMode()));
						if(((IStructuredSelection)modeComboViewer.getSelection()).getFirstElement() == null) {
							modeComboViewer.getCombo().select(0);
						}
						if(getScanController().config().getMode().equals("monitor")) {
							unitLabel.setText("counts");
						} else if(getScanController().config().getMode().equals("timer")) {
							unitLabel.setText("sec");
						}
						// Update for databinding is not fully functional in 3.2
						presetText.setText(Float.toString(getScanController().config().getPreset()));
						npSpinner.setSelection(getScanController().config().getNumberOfPoint());
						channelSpinner.setSelection(getScanController().config().getChannel());
						scanVariableText.setText(getScanController().config().getVariable());
						Component selectedComponent = SicsUtils.findComponentFromSingleProperty(SicsCore.getSicsManager().service().getOnlineModel(), "sicsdev", getScanController().config().getVariable());
						if(selectedComponent != null) {
							IComponentController controller = SicsCore.getSicsController().findComponentController(selectedComponent);
							pathText.setText(controller.getPath());
							if(controller instanceof IDynamicController) {
								currentValueText.setText(((IDynamicController)controller).getValue().getStringData());
							}
						}
					}
				});
			}
		});
	}

	private void commitAndRun() {
		try {
			getScanController().config().commit();
			getScanController().asyncExecute();
		} catch (SicsIOException e) {
			e.printStackTrace();
		} catch (SicsCoreException e) {
			e.printStackTrace();
		}

	}

}
