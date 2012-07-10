package org.gumtree.gumnix.sics.ui.componentview;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.controllers.ComponentData;
import org.gumtree.gumnix.sics.control.controllers.ICommandController;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.control.events.IDynamicControllerListener;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.core.SicsUtils;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.slf4j.Logger;

import ch.psi.sics.hipadaba.Property;

public class OldScanControllerViewContent implements IComponentViewContent {

	private static Logger logger;

	private enum NodeControllerId {
		mode, NP, preset, scan_increment, scan_start, scan_variable
	}

	private Map<NodeControllerId, IDynamicController> nodeControllerMap;

	private FormToolkit toolkit;

	private ICommandController controller;

	private Text npText;

	private Text presetText;

	private Text scanStartText;

	private Text scanIncrementText;

	private Text variableText;

	private ComboViewer modeComboViewer;

	private ListViewer listViewer;

	private boolean isDisposed;

	private ScanCommandMonitor monitor;

	public OldScanControllerViewContent() {
		isDisposed = false;
	}

	public void createPartControl(Composite parent,
			IComponentController controller) {
		Assert.isTrue(controller instanceof ICommandController);
		this.controller = (ICommandController)controller;
		toolkit = new FormToolkit(parent.getDisplay());
		parent.setLayout(new FillLayout());
		ScrolledForm form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(new GridLayout());
		SashForm sashForm = new SashForm(form.getBody(), SWT.NONE);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Composite configSectionComposite = toolkit.createComposite(sashForm);
		createConfigSection(configSectionComposite);
		Composite previewSectionComposite = toolkit.createComposite(sashForm);
		createPreviewSection(previewSectionComposite);
		sashForm.setWeights(new int[] { 1, 1 });
		Composite buttonBar = toolkit.createComposite(form.getBody());
		buttonBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		createButtonBar(buttonBar);
		updateValue();
		getScanCommandMonitor();
	}

	private void createConfigSection(final Composite parent) {
		parent.setLayout(new GridLayout());

		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.horizontalSpan = 2;

		Section section = toolkit.createSection(parent, Section.DESCRIPTION|Section.TITLE_BAR);
		section.setText("Configuration");
		Composite client = toolkit.createComposite(section, SWT.WRAP);
		section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		client.setLayout(new GridLayout(3, false));

		getToolkit().createLabel(client, "Mode: ");
		modeComboViewer = new ComboViewer(client, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		String[] items = new String[0];
		Property property = SicsUtils.getProperty(getNodeControllerMap().get(NodeControllerId.mode).getComponent(), "values");
		if(property != null) {
			items = (String[])property.getValue().toArray(new String[property.getValue().size()]);
		}
		modeComboViewer.setContentProvider(new ArrayContentProvider());
		modeComboViewer.setLabelProvider(new LabelProvider());
		modeComboViewer.setInput(items);
		modeComboViewer.getCombo().setLayoutData(gridData);

		getToolkit().createLabel(client, "Preset: ");
		presetText = getToolkit().createText(client, "", SWT.BORDER);
		presetText.setLayoutData(gridData);
		presetText.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
//				try {
//					Float.parseFloat(e.text);
////					try {
////						getNodeController(NodeControllerId.preset).setTargetValue(ComponentData.createStringData(e.text));
////					} catch (SicsIOException e1) {
////						e1.printStackTrace();
////					}
//				} catch (NumberFormatException ex) {
//					e.doit = false;
//				}
			}
		});

		getToolkit().createLabel(client, "NP: ");
		npText = getToolkit().createText(client, "", SWT.BORDER);
		npText.setLayoutData(gridData);
		npText.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				try {
					Integer.parseInt(e.text);
//					try {
//						getNodeController(NodeControllerId.NP).setTargetValue(ComponentData.createStringData(e.text));
//					} catch (SicsIOException e1) {
//						e1.printStackTrace();
//					}
				} catch (NumberFormatException ex) {
					e.doit = false;
				}
			}
		});

		getToolkit().createLabel(client, "Scan Variable: ");
		variableText = getToolkit().createText(client, "", SWT.BORDER | SWT.READ_ONLY);
		variableText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		Button findButton = getToolkit().createButton(client, "Find...", SWT.PUSH);
		findButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String value = SicsUtils.getPropertyFirstValue(getNodeControllerMap().get(NodeControllerId.scan_variable).getComponent(), "argtype");
				if(value != null) {
					VariableSelectionDialog dialog = new VariableSelectionDialog(parent.getShell(), "type", value);
					dialog.setTitle("Select a variable for scan");
					dialog.open();
					String variable = dialog.getSelectedVariable();
					if(variable != null) {
						variableText.setText(variable);
//						try {
//							getNodeController(NodeControllerId.scan_variable).setTargetValue(ComponentData.createStringData(variable));
//						} catch (SicsIOException e1) {
//							e1.printStackTrace();
//						}
					}
				}
			}
		});

		getToolkit().createLabel(client, "Start Value: ");
		scanStartText = getToolkit().createText(client, "", SWT.BORDER);
		scanStartText.setLayoutData(gridData);
		scanStartText.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
//				try {
//					Float.parseFloat(e.text);
////					try {
////						getNodeController(NodeControllerId.scan_start).setTargetValue(ComponentData.createStringData(e.text));
////					} catch (SicsIOException e1) {
////						e1.printStackTrace();
////					}
//				} catch (NumberFormatException ex) {
//					e.doit = false;
//				}
			}
		});

		getToolkit().createLabel(client, "Step: ");
		scanIncrementText = getToolkit().createText(client, "", SWT.BORDER);
		scanIncrementText.setLayoutData(gridData);
		scanIncrementText.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
//				try {
//					Float.parseFloat(e.text);
////					try {
////						getNodeController(NodeControllerId.scan_start).setTargetValue(ComponentData.createStringData(e.text));
////					} catch (SicsIOException e1) {
////						e1.printStackTrace();
////					}
//				} catch (NumberFormatException ex) {
//					e.doit = false;
//				}
			}
		});

		section.setClient(client);
	}

	private void createPreviewSection(Composite parent) {
		parent.setLayout(new GridLayout());
		Section section = toolkit.createSection(parent, Section.DESCRIPTION|Section.TITLE_BAR);
		section.setText("Scan Points Preview");
		Composite client = toolkit.createComposite(section, SWT.WRAP);
		section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		client.setLayout(new GridLayout());
		listViewer = new ListViewer(client, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		listViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		listViewer.setContentProvider(new ArrayContentProvider());
		listViewer.setLabelProvider(new LabelProvider());
		Button previewButton = getToolkit().createButton(client, "Preview Scan Points", SWT.PUSH);
		previewButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		previewButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int np = Integer.parseInt(npText.getText());
				float start = Float.parseFloat(scanStartText.getText());
				float step = Float.parseFloat(scanIncrementText.getText());
				String[] points = new String[np];
				for(int i = 0; i < np; i++) {
					points[i] = Float.toString(start + i * step);
				}
				listViewer.setInput(points);
				listViewer.refresh();
			}
		});

		section.setClient(client);
	}

	/**
	 * @param parent
	 */
	private void createButtonBar(final Composite parent) {
		parent.setLayout(new GridLayout());
		Button startButton = getToolkit().createButton(parent, "Start Scan", SWT.PUSH);
		startButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		startButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					Object selection = ((IStructuredSelection)modeComboViewer.getSelection()).getFirstElement();
					if(selection == null) {
						MessageDialog.openError(parent.getShell(), "Scan configuration incompleted", "Please select scan mode");
						return;
					}
					// This will not only as set target does not commit automatically in version 0.2.0
					getNodeController(NodeControllerId.mode).setTargetValue(ComponentData.createStringData(selection.toString()));
					getNodeController(NodeControllerId.preset).setTargetValue(ComponentData.createStringData(presetText.getText()));
					getNodeController(NodeControllerId.NP).setTargetValue(ComponentData.createStringData(npText.getText()));
					getNodeController(NodeControllerId.scan_increment).setTargetValue(ComponentData.createStringData(scanIncrementText.getText()));
					getNodeController(NodeControllerId.scan_start).setTargetValue(ComponentData.createStringData(scanStartText.getText()));
					getNodeController(NodeControllerId.scan_variable).setTargetValue(ComponentData.createStringData(variableText.getText()));
					getController().asyncExecute();
				} catch (SicsIOException e1) {
					e1.printStackTrace();
				}
			}
		});
		Button stopButton = getToolkit().createButton(parent, "Interrupt", SWT.PUSH);
		stopButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		stopButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					SicsCore.getDefaultProxy().send("INT1712 2", null);
				} catch (SicsIOException e1) {
					e1.printStackTrace();
				}
			}
		});
	}

	public void dispose() {
	}

	public ICommandController getController() {
		return controller;
	}

	private FormToolkit getToolkit() {
		return toolkit;
	}

	private Map<NodeControllerId, IDynamicController> getNodeControllerMap() {
		if(nodeControllerMap == null) {
			nodeControllerMap = new HashMap<NodeControllerId, IDynamicController>();
			for(NodeControllerId controlNodeId : NodeControllerId.values()) {
				nodeControllerMap.put(controlNodeId, getNodeController(controlNodeId));
			}
		}
		return nodeControllerMap;
	}

	private IDynamicController getNodeController(NodeControllerId id) {
		for(IComponentController controller : getController().getChildControllers()) {
			if(controller.getComponent().getId().equals(id.name()) && controller instanceof IDynamicController) {
				return (IDynamicController)controller;
			}
		}
		return null;
	}

	private void updateValue() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if(npText == null || npText.isDisposed()) {
					return;
				}
				try {
					String mode = getNodeController(NodeControllerId.mode).getValue().getSicsString();
					modeComboViewer.setSelection(new StructuredSelection(mode.trim()));
					npText.setText(getNodeController(NodeControllerId.NP).getValue().getSicsString());
					presetText.setText(getNodeController(NodeControllerId.preset).getValue().getSicsString());
					scanStartText.setText(getNodeController(NodeControllerId.scan_start).getValue().getSicsString());
					scanIncrementText.setText(getNodeController(NodeControllerId.scan_increment).getValue().getSicsString());
					String variable = getNodeController(NodeControllerId.scan_variable).getValue().getSicsString();
					variableText.setText(variable);
				} catch (SicsIOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private ScanCommandMonitor getScanCommandMonitor() {
		if(monitor == null) {
			monitor = new ScanCommandMonitor();
		}
		return monitor;
	}

	private class ScanCommandMonitor implements IDynamicControllerListener {

		public ScanCommandMonitor() {
			for (IDynamicController controller : getNodeControllerMap().values()) {
				controller.addComponentListener(this);
			}
		}

		public void targetChanged(IDynamicController controller, IComponentData newTarget) {
		}

		public void valueChanged(IDynamicController controller, IComponentData newValue) {
			if(isDisposed) {
				for (IDynamicController nodeController : getNodeControllerMap().values()) {
					nodeController.removeComponentListener(this);
				}
			} else {
				updateValue();
			}

		}

		public void componentStatusChanged(ControllerStatus newStatus) {
		}
	}

}
