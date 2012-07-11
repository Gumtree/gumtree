package au.gov.ansto.bragg.quokka.ui.workflow;

import java.io.FileReader;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.statushandlers.StatusManager;
import org.gumtree.ui.util.resource.UIResourceManager;
import org.gumtree.ui.util.resource.UIResources;

import au.gov.ansto.bragg.quokka.experiment.model.InstrumentConfig;
import au.gov.ansto.bragg.quokka.experiment.report.ExperimentConfig;
import au.gov.ansto.bragg.quokka.experiment.report.ExperimentUserReport;
import au.gov.ansto.bragg.quokka.experiment.report.ExperimentUserReportUtils;
import au.gov.ansto.bragg.quokka.ui.internal.Activator;

public class LoadReportDialog extends MessageDialog {

	private InstrumentConfig config;
	
	private UIResourceManager resourceManager;
	
	private Text filenameText;
	
	private TableViewer configTable;
	
	private Button emptyBeamTranmissionButton;
	
	private Text emptyBeamTranmissionText;
	
	private Button emptyCellTranmissionButton;
	
	private Text emptyCellTranmissionText;
	
	private Button emptyCellScatteringButton;
	
	private Text emptyCellScatteringText;
	
	public LoadReportDialog(Shell parentShell, InstrumentConfig config) {
		super(parentShell, "Load Report", null, "Select report file to load",
				NONE, new String[] { IDialogConstants.OK_LABEL,
						IDialogConstants.CANCEL_LABEL }, 0);
		this.config = config;
	}
	
	protected Control createCustomArea(Composite parent) {
		parent.setLayout(new FillLayout());
		Composite mainArea = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(mainArea);
		mainArea.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				dialogDispose();
			}
		});
		
		Composite reportSelectionArea = new Composite(mainArea, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(reportSelectionArea);
		createReportSelectionArea(reportSelectionArea);
		
		Composite configSelection = new Composite(mainArea, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(configSelection);
		createConfigSelectionArea(configSelection);
		
		Composite fileAssociationSelection = new Composite(mainArea, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(fileAssociationSelection);
		createFileAssociationSelectionArea(fileAssociationSelection);
		
		return mainArea;
	}
	
	private void createReportSelectionArea(Composite parent) {
		GridLayoutFactory.swtDefaults().numColumns(3).margins(0, 0).applyTo(parent);
		
		Label label = new Label(parent, SWT.NONE);
		label.setText("Report File: ");
		label.setFont(UIResources.getDefaultFont(SWT.BOLD));
		
		filenameText = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(filenameText);
		
		Button loadButton = new Button(parent, SWT.PUSH);
		loadButton.setText("Load");
		loadButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// Open file dialog
				FileDialog fileDialog = new FileDialog(getShell());
				fileDialog.setFilterExtensions(new String[] { "*.xml" });
				String filename = fileDialog.open();
				if (filename != null) {
					try {
						// Load report
						ExperimentUserReport report = (ExperimentUserReport) ExperimentUserReportUtils
								.getXStream().fromXML(new FileReader(filename));
						if (report != null) {
							// Update UI
							filenameText.setText(filename);
							configTable.setInput(report.getConfigs().toArray(new ExperimentConfig[report.getConfigs().size()]));
							// Set default selection
							if (report.getConfigs().size() > 0) {
								configTable.setSelection(new StructuredSelection(report.getConfigs().get(0)));
							}
							// Be more intelligent by setting the first matching config
							for (ExperimentConfig configItem : report.getConfigs()) {
								if (config.getName().equals(configItem.getName())) {
									configTable.setSelection(new StructuredSelection(configItem));
									break;
								}
							}
						}
					} catch (Exception exception) {
						// Display error
						StatusManager.getManager().handle(
							new Status(IStatus.ERROR,
									Activator.PLUGIN_ID,
									"Failed to open report " + filename, exception),
									StatusManager.SHOW);
					}
				}
			}
		});
	}
	
	private void createConfigSelectionArea(Composite parent) {
		GridLayoutFactory.fillDefaults().margins(0, 0).applyTo(parent);
		
		Label label = new Label(parent, SWT.NONE);
		label.setText("Configurations:");
		label.setFont(UIResources.getDefaultFont(SWT.BOLD));
		
		configTable = new TableViewer(parent);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(configTable.getControl());
		configTable.setContentProvider(new ArrayContentProvider());
		configTable.setLabelProvider(new DecoratingLabelProvider(new LabelProvider(), null) {
			public String getText(Object element) {
				ExperimentConfig configItem = (ExperimentConfig) element;
				return configItem.getName();
			}
			public Font getFont(Object element) {
				// High light mateched selection
				ExperimentConfig configItem = (ExperimentConfig) element;
				if (config.getName().equals(configItem.getName())) {
					return JFaceResources.getFontRegistry().getBold(
							JFaceResources.DEFAULT_FONT);
				}
				return null;
			}
		});
		
		// Handle selection
		configTable.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				clearUI();
				
				ExperimentConfig config = (ExperimentConfig) ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (config == null) {
					return;
				}
				
				if (config.getEmptyBeamTransmissionRunId() != null) {
					emptyBeamTranmissionButton.setEnabled(true);
					emptyBeamTranmissionButton.setSelection(true);
					emptyBeamTranmissionText.setText(config.getEmptyBeamTransmissionRunId());
				}
				if (config.getEmptyCellTransmissionRunId() != null) {
					emptyCellTranmissionButton.setEnabled(true);
					emptyCellTranmissionButton.setSelection(true);
					emptyCellTranmissionText.setText(config.getEmptyCellTransmissionRunId());
				}
				if (config.getEmptyCellScatteringRunId() != null) {
					emptyCellScatteringButton.setEnabled(true);
					emptyCellScatteringButton.setSelection(true);
					emptyCellScatteringText.setText(config.getEmptyCellScatteringRunId());
				}
			}
		});

	}

	private void createFileAssociationSelectionArea(Composite parent) {
		GridLayoutFactory.swtDefaults().numColumns(3).margins(0, 0).applyTo(parent);
		
		emptyBeamTranmissionButton = new Button(parent, SWT.CHECK);
		Label label = new Label(parent, SWT.NONE);
		label.setText("Empty Beam Transmission: ");
		emptyBeamTranmissionText = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
		emptyBeamTranmissionText.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(emptyBeamTranmissionText);
		
		emptyCellTranmissionButton = new Button(parent, SWT.CHECK);
		label = new Label(parent, SWT.NONE);
		label.setText("Empty Cell Transmission: ");
		emptyCellTranmissionText = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
		emptyCellTranmissionText.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(emptyCellTranmissionText);
		
		emptyCellScatteringButton = new Button(parent, SWT.CHECK);
		label = new Label(parent, SWT.NONE);
		label.setText("Empty Cell Scattering: ");
		emptyCellScatteringText = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
		emptyCellScatteringText.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(emptyCellScatteringText);
		
		clearUI();
	}

	
	private void clearUI() {
		emptyBeamTranmissionButton.setSelection(false);
		emptyBeamTranmissionButton.setEnabled(false);
		emptyBeamTranmissionText.setText("");
		emptyCellTranmissionButton.setSelection(false);
		emptyCellTranmissionButton.setEnabled(false);
		emptyCellTranmissionText.setText("");
		emptyCellScatteringButton.setSelection(false);
		emptyCellScatteringButton.setEnabled(false);
		emptyCellScatteringText.setText("");
	}
	
	protected void buttonPressed(int buttonId) {
		// Set model when OK is pressed
		if (buttonId == 0) {
			if (emptyBeamTranmissionButton.getSelection()) {
				config.setEmptyBeamTransmissionDataFile(emptyBeamTranmissionText.getText());
			}
			if (emptyCellTranmissionButton.getSelection()) {
				config.setEmptyCellTransmissionDataFile(emptyCellTranmissionText.getText());
			}
			if (emptyCellScatteringButton.getSelection()) {
				config.setEmptyCellScatteringDataFile(emptyCellScatteringText.getText());
			}
		}
        super.buttonPressed(buttonId);
    }
	
	private void dialogDispose() {
		config = null;
		filenameText = null;
		configTable = null;
		emptyBeamTranmissionText = null;
		emptyCellTranmissionText = null;
		emptyCellScatteringText = null;
		emptyBeamTranmissionButton = null;
		emptyCellTranmissionButton = null;
		emptyCellScatteringButton = null;
	}
	
}
