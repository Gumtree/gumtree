package au.gov.ansto.bragg.pelican.exp.viewer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.gumtree.scripting.ScriptExecutor;
import org.gumtree.ui.scripting.viewer.CommandLineViewer;
import org.gumtree.ui.scripting.viewer.ICommandLineViewer;

import au.gov.ansto.bragg.nbi.ui.scripting.ScriptPageRegister;
import au.gov.ansto.bragg.nbi.ui.scripting.parts.ScriptControlViewer;

public class ExperimentConfigViewer extends Composite {

	private static final String CONFIG_PROPERTY_NAME = "gumtree.workflow.configscript";
	
	private ScriptExecutor Jython_Executor;
	private ScriptControlViewer controlViewer;
	private CommandLineViewer consoleViewer;
	
	public ExperimentConfigViewer(Composite parent, int style) {
		super(parent, style);
		GridLayoutFactory.fillDefaults().applyTo(this);
		ScriptPageRegister register = new ScriptPageRegister();
		
		SashForm level1Form = new SashForm(this, SWT.VERTICAL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(level1Form);

		createControlArea(level1Form);

		Composite consoleComposite = new Composite(level1Form, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(consoleComposite);
		consoleViewer.createPartControl(consoleComposite, ICommandLineViewer.NO_UTIL_AREA);

		level1Form.setWeights(new int[]{9, 3});
		
		ScriptPageRegister.registPage(controlViewer.getScriptRegisterID(), register);
		register.setControlViewer(controlViewer);
		register.setConsoleViewer(consoleViewer);
		controlViewer.runNativeInitScript();
		controlViewer.loadScript(ScriptControlViewer.getFullScriptPath(System.getProperty(CONFIG_PROPERTY_NAME)));
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
//	public ExperimentConfigViewer(Composite parent, int style) {
//		super(parent, style);
//		Color whiteColor = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
//		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(this);
//		ScriptPageRegister register = new ScriptPageRegister();
//		CommandHandler commandHandler = new CommandHandler();
//		CommandLineViewer viewer = new CommandLineViewer();
//		try {
//			Thread.sleep(3000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		viewer.setScriptExecutor(commandHandler.getScriptExecutor());
//		controlViewer = new ScriptControlViewer(this, style);
//		controlViewer.setScriptExecutor(commandHandler.getScriptExecutor());
//		controlViewer.setBackground(whiteColor);
//		Label titleLabel = new Label(controlViewer, SWT.NONE);
//		titleLabel.setBackground(whiteColor);
//		FontData[] fD = titleLabel.getFont().getFontData();
//		fD[0].setHeight(18);
//		titleLabel.setFont(new Font(titleLabel.getDisplay(), fD));
//		titleLabel.setText(" Experiment Setup");
//		controlViewer.getScrollArea().moveAbove(controlViewer.getStaticComposite());
//		titleLabel.moveAbove(controlViewer.getScrollArea());
//		controlViewer.getStaticComposite().setVisible(false);
//		controlViewer.getStaticComposite().dispose();
//		GridLayoutFactory.fillDefaults().applyTo(controlViewer);
//		GridDataFactory.fillDefaults().grab(true, true).applyTo(controlViewer);
//		ScriptPageRegister.registPage(controlViewer.getScriptRegisterID(), register);
//		register.setControlViewer(controlViewer);
//		
//		Composite consoleComposite = new Composite(this, SWT.NONE);
//		GridLayoutFactory.fillDefaults().applyTo(consoleComposite);
//		GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 180).applyTo(consoleComposite);
//		viewer.createPartControl(consoleComposite, ICommandLineViewer.NO_UTIL_AREA | ICommandLineViewer.NO_INPUT_TEXT);
//		
//		controlViewer.runNativeInitScript();
//		controlViewer.loadScript(ScriptControlViewer.getFullScriptPath(System.getProperty(CONFIG_PROPERTY_NAME)));
//	}

	
}
