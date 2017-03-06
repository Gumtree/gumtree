package au.gov.ansto.bragg.quokka.msw.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.scripting.ScriptExecutor;
import org.gumtree.ui.scripting.viewer.CommandLineViewer;
import org.gumtree.ui.scripting.viewer.ICommandLineViewer;

import au.gov.ansto.bragg.quokka.msw.ModelProvider;
import au.gov.ansto.bragg.quokka.msw.composites.WorkflowComposite;
import au.gov.ansto.bragg.quokka.msw.schedule.InstrumentActionExecuter;
import au.gov.ansto.bragg.quokka.msw.schedule.PythonInstrumentActionExecuter;

public class QuokkaMswView extends ViewPart {
	// fields
	WorkflowComposite workflowComposite;

	// construction
	public QuokkaMswView() {
	}

	// methods
	public void createPartControl(Composite parent) {
		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		workflowComposite = new WorkflowComposite(sashForm, SWT.NONE);

		CTabFolder consoleFolder = new CTabFolder(sashForm, SWT.BORDER);
		consoleFolder.setSimple(false);

		CTabItem item = new CTabItem(consoleFolder, SWT.NONE);
		item.setText("Console");
		item.setControl(createPythonComposite(consoleFolder));
		
		consoleFolder.setSelection(item);

		sashForm.setWeights(new int[] {5, 1});
	}
	public void setFocus() {
		workflowComposite.setFocus();
	}
	
	// helpers
	private Composite createPythonComposite(Composite parent) {
		Composite cmpCmdViewer = new Composite(parent, SWT.NONE);
		cmpCmdViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		ScriptExecutor executor = new ScriptExecutor("jython");
		ICommandLineViewer cmdViewer = new CommandLineViewer();
		
		cmdViewer.setScriptExecutor(executor);
		cmdViewer.createPartControl(cmpCmdViewer, ICommandLineViewer.NO_UTIL_AREA); // ICommandLineViewer.NO_INPUT_TEXT
		
		ModelProvider modelProvider = workflowComposite.getModelProvider();
		InstrumentActionExecuter.setDefault(new PythonInstrumentActionExecuter(
				executor.getEngine(),
				modelProvider));
		
		return cmpCmdViewer;
	}
}
