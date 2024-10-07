package au.gov.ansto.bragg.bilby.ui.internal;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.gumtree.data.ui.viewers.PlotViewer;
import org.gumtree.scripting.ScriptExecutor;
import org.gumtree.ui.scripting.viewer.CommandLineViewer;
import org.gumtree.ui.scripting.viewer.ICommandLineViewer;

import au.gov.ansto.bragg.nbi.ui.scripting.ScriptPageRegister;
import au.gov.ansto.bragg.nbi.ui.scripting.parts.ScriptControlViewer;
import au.gov.ansto.bragg.nbi.ui.scripting.parts.ScriptDataSourceViewer;
import au.gov.ansto.bragg.nbi.ui.scripting.parts.ScriptInfoViewer;

public class BilbyWorkflowViewer extends Composite {

	private ScriptExecutor Jython_Executor;
	private static final String WORKFLOW_SCRIPT_NAME = "gumtree.bilby.workflowScript";
	
	private ScriptControlViewer controlViewer;
	private CommandLineViewer consoleViewer;
	private ScriptDataSourceViewer dataSourceViewer;
	private ScriptInfoViewer infoViewer;
	
	public BilbyWorkflowViewer(Composite parent, int style) {
		super(parent, style);
		GridLayoutFactory.fillDefaults().applyTo(this);
		ScriptPageRegister register = new ScriptPageRegister();
		
		SashForm level1Form = new SashForm(this, SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(level1Form);

		createControlArea(level1Form);

//		SashForm level2Left = new SashForm(level1Form, SWT.VERTICAL);
		SashForm level2Right = new SashForm(level1Form, SWT.VERTICAL);
		level1Form.setWeights(new int[]{11, 10});

//		level2Left.setWeights(new int[]{21, 10});
		
		SashForm level3Form = new SashForm(level2Right, SWT.HORIZONTAL);
		SashForm level4Left = new SashForm (level3Form, SWT.VERTICAL);
		dataSourceViewer = new ScriptDataSourceViewer(level4Left, SWT.NONE);
		infoViewer = new ScriptInfoViewer(level4Left, SWT.NONE);
		level4Left.setWeights(new int[]{7, 1});
		SashForm level4Right = new SashForm(level3Form, SWT.VERTICAL);
		PlotViewer plot1Viewer = new PlotViewer(level4Right, SWT.NONE);
		PlotViewer plot2Viewer = new PlotViewer(level4Right, SWT.NONE);
		level4Right.setWeights(new int[]{1, 1});
		level3Form.setWeights(new int[]{3, 7});
//		PlotViewer plot3Viewer = new PlotViewer(level2Right, SWT.NONE);
		Composite consoleComposite = new Composite(level2Right, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(consoleComposite);
//		GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 180).applyTo(consoleComposite);
		consoleViewer.createPartControl(consoleComposite, ICommandLineViewer.NO_UTIL_AREA);
		
		level2Right.setWeights(new int[]{15, 8});
		
		ScriptPageRegister.registPage(controlViewer.getScriptRegisterID(), register);
		register.setControlViewer(controlViewer);
		register.setConsoleViewer(consoleViewer);
		register.setDataSourceViewer(dataSourceViewer);
		register.setInfoViewer(infoViewer);
		register.registerObject("Plot1", plot1Viewer);
		register.registerObject("Plot2", plot2Viewer);
//		register.registerObject("Plot3", plot3Viewer);
		controlViewer.runNativeInitScript();
		controlViewer.loadScript(ScriptControlViewer.getFullScriptPath(System.getProperty(WORKFLOW_SCRIPT_NAME)));
	}

	private void createControlArea(SashForm parent) {
		ScriptExecutor scriptExecutor = getScriptExecutor();
		consoleViewer = new CommandLineViewer();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		consoleViewer.setScriptExecutor(scriptExecutor);
		controlViewer = new ScriptControlViewer(parent, SWT.NONE);
		controlViewer.setScriptExecutor(scriptExecutor);
		Color whiteColor = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
		controlViewer.setBackground(whiteColor);
//		Label titleLabel = new Label(controlViewer, SWT.NONE);
//		titleLabel.setBackground(whiteColor);
//		FontData[] fD = titleLabel.getFont().getFontData();
//		fD[0].setHeight(18);
//		titleLabel.setFont(new Font(titleLabel.getDisplay(), fD));
//		titleLabel.setText(" Taipan Calibration");
//		controlViewer.getScrollArea().moveAbove(controlViewer.getStaticComposite());
//		titleLabel.moveAbove(controlViewer.getScrollArea());
		controlViewer.getStaticComposite().setVisible(false);
		controlViewer.getStaticComposite().dispose();
		GridLayoutFactory.fillDefaults().applyTo(controlViewer);
//		GridDataFactory.fillDefaults().grab(true, true).applyTo(controlViewer);
		

	}

	public ScriptExecutor getScriptExecutor(){
//		if (Jython_Executor == null || Jython_Executor.getEngine() == null) {
			Jython_Executor = new ScriptExecutor("jython");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
//		}
		return Jython_Executor;
	}

	@Override
	public void dispose() {
		super.dispose();
		if (Jython_Executor != null) {
			Jython_Executor.interrupt();
			Jython_Executor = null;
		}
	}
}
