package au.gov.ansto.bragg.kookaburra.ui.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.ui.scripting.viewer.CommandLineViewer;
import org.gumtree.ui.scripting.viewer.ICommandLineViewer;
import org.gumtree.util.eclipse.EclipseUtils;
import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.tasks.ScriptEngineTask;
import org.gumtree.workflow.ui.util.WorkflowFactory;
import org.gumtree.workflow.ui.viewer.WizardWorkflowViewer;

import au.gov.ansto.bragg.kookaburra.ui.KookaburraUIConstants;

public class KookaburraScanView extends ViewPart {

	private CTabFolder experimentFolder;
	
	private CTabFolder consoleFolder;
	
	private ICommandLineViewer commandLineViewer;
	
	private FormToolkit toolkit;
	
	public KookaburraScanView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		toolkit = new FormToolkit(parent.getDisplay());
		toolkit.adapt(parent);
		
//		Composite toolbarArea = toolkit.createComposite(parent);
//		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(toolbarArea);
		// 1. Create toolbar area
//		createToolbarArea(toolbarArea);
		
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		toolkit.adapt(sashForm);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(sashForm);
		// 2. Create experiment area
		createExperimentArea(sashForm);
		// 3. Create console area
		createConsoleArea(sashForm);
		
		sashForm.setWeights(new int[] {5, 1});
	}

	private void createExperimentArea(Composite parent) {
		experimentFolder = new CTabFolder(parent, SWT.BORDER);
		experimentFolder.setSimple(false);
		toolkit.adapt(experimentFolder);
		CTabItem item = new CTabItem(experimentFolder, SWT.NONE);
		item.setText("Experiment Control");
		
		// Create workflow viewer
		WizardWorkflowViewer viewer = new WizardWorkflowViewer();
		// Load workflow
		try {
			IFileStore workflowConfig = EclipseUtils.find(
					System.getProperty(KookaburraUIConstants.PROP_SCAN_CONFIG_PLUGIN),
					System.getProperty(KookaburraUIConstants.PROP_SCAN_CONFIG_PATH));
			// Prepare initial context with script viewer
			Map<String, Object> initialContext = new HashMap<String, Object>();
			commandLineViewer = new CommandLineViewer();
			initialContext.put(ScriptEngineTask.CONTEXT_KEY_SCRIPT_VIEWER, commandLineViewer);
			IWorkflow workflow = WorkflowFactory.createWorkflow(workflowConfig.openInputStream(EFS.NONE, new NullProgressMonitor()), initialContext);
			viewer.setWorkflow(workflow);
			Composite viewerHolder = toolkit.createComposite(experimentFolder);
			viewer.createPartControl(viewerHolder);
			viewer.setFocus();
			item.setControl(viewerHolder);
			experimentFolder.setSelection(item);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	private void createConsoleArea(Composite parent) {
		consoleFolder = new CTabFolder(parent, SWT.BORDER);
		consoleFolder.setSimple(false);
		toolkit.adapt(consoleFolder);
		CTabItem item = new CTabItem(consoleFolder, SWT.NONE);
		item.setText("Console");
		Composite viewerHolder = toolkit.createComposite(consoleFolder);
		commandLineViewer.createPartControl(viewerHolder, ICommandLineViewer.NO_INPUT_TEXT | ICommandLineViewer.NO_UTIL_AREA);
		// Maintain UI consistency
		for (Control child : viewerHolder.getChildren()) {
			if (child instanceof Composite) {
				toolkit.adapt((Composite) child);
			}
		}
		item.setControl(viewerHolder);
		consoleFolder.setSelection(item);
	}
	
	@Override
	public void setFocus() {
	}

}
