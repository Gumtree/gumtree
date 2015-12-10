package au.gov.ansto.bragg.nbi.ui.scripting.parts;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.data.ui.viewers.PlotViewer;
import org.gumtree.scripting.ScriptExecutor;
import org.gumtree.ui.scripting.viewer.CommandLineViewer;
import org.gumtree.ui.scripting.viewer.ICommandLineViewer;

import au.gov.ansto.bragg.nbi.ui.scripting.ScriptPageRegister;

public class StandAloneScriptingViewer extends Composite {

	private final static String PROPERTY_SHOW_CONSOLE = "gumtree.scripting.showConsole";
	private ScriptControlViewer controlViewer;
	private CommandLineViewer consoleViewer;
	private ScriptDataSourceViewer dataSourceViewer;
	private ScriptInfoViewer infoViewer;
	private boolean showConsole = true;
	private SashForm level3Bottom;
	
	public StandAloneScriptingViewer(Composite parent, int style) {
		super(parent, style);
		GridLayoutFactory.fillDefaults().applyTo(this);
		ScriptPageRegister register = new ScriptPageRegister();
		
		SashForm level1Form = new SashForm(this, SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(level1Form);
		
		SashForm level2Left = new SashForm(level1Form, SWT.VERTICAL);
		SashForm level2Right = new SashForm(level1Form, SWT.VERTICAL);
		level1Form.setWeights(new int[]{3, 7});

		createDatasetArea(level2Left);
		createControlArea(level2Left);
		level2Left.setWeights(new int[]{4, 6});
		
		SashForm level3Top = new SashForm(level2Right, SWT.VERTICAL);
		level3Bottom = new SashForm(level2Right, SWT.HORIZONTAL);
		
		PlotViewer plot1Viewer = new PlotViewer(level3Top, SWT.NONE);
		PlotViewer plot2Viewer = new PlotViewer(level3Top, SWT.NONE);
		level3Top.setWeights(new int[]{1, 1});

		createConsoleArea(level3Bottom);
		PlotViewer plot3Viewer = new PlotViewer(level3Bottom, SWT.NONE);
		String showConsoleProperty = System.getProperty(PROPERTY_SHOW_CONSOLE);
		if (showConsoleProperty != null) {
			showConsole = Boolean.valueOf(showConsoleProperty);
		}
		if (showConsole) {
			level3Bottom.setWeights(new int[]{1, 1});
		}else {
			level3Bottom.setWeights(new int[]{0, 1});
		}
		level2Right.setWeights(new int[]{6, 3});
		
		ScriptPageRegister.registPage(controlViewer.getScriptRegisterID(), register);
		register.setControlViewer(controlViewer);
		register.setConsoleViewer(consoleViewer);
		register.setDataSourceViewer(dataSourceViewer);
		register.setInfoViewer(infoViewer);
		register.registerObject("Plot1", plot1Viewer);
		register.registerObject("Plot2", plot2Viewer);
		register.registerObject("Plot3", plot3Viewer);
		controlViewer.runInitialScripts();
	}

	private void createDatasetArea(SashForm sashForm) {
		dataSourceViewer = new ScriptDataSourceViewer(sashForm, SWT.NONE);
	}
	
	private void createControlArea(SashForm sashForm) {
		controlViewer = new ScriptControlViewer(sashForm, SWT.NONE);

	}

	private void createConsoleArea(SashForm sashForm) {
		// TODO Auto-generated method stub
		CommandLineViewer commandHandler = new CommandLineViewer();
		consoleViewer = new CommandLineViewer();
		ScriptExecutor Jython_Executor = new ScriptExecutor("jython");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		consoleViewer.setScriptExecutor(Jython_Executor);
//		controlViewer = new ScriptControlViewer(level2Left, SWT.NONE);
//		controlViewer.setScriptExecutor(commandHandler.getScriptExecutor());
//		Color whiteColor = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
//		controlViewer.setBackground(whiteColor);
//		Label titleLabel = new Label(controlViewer, SWT.NONE);
//		titleLabel.setBackground(whiteColor);
//		FontData[] fD = titleLabel.getFont().getFontData();
//		fD[0].setHeight(18);
//		titleLabel.setFont(new Font(titleLabel.getDisplay(), fD));
//		titleLabel.setText(" Taipan Calibration");
//		controlViewer.getScrollArea().moveAbove(controlViewer.getStaticComposite());
//		titleLabel.moveAbove(controlViewer.getScrollArea());
//		controlViewer.getStaticComposite().setVisible(false);
//		controlViewer.getStaticComposite().dispose();
//		GridLayoutFactory.fillDefaults().applyTo(controlViewer);
//		GridDataFactory.fillDefaults().grab(true, true).applyTo(controlViewer);
		
//		controlViewer = new ScriptControlViewer(level2Left, SWT.NONE);

		Composite consoleComposite = new Composite(sashForm, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(consoleComposite);
//		GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 180).applyTo(consoleComposite);
		consoleViewer.createPartControl(consoleComposite, ICommandLineViewer.NO_UTIL_AREA);
	}

	public void toggleShowingConsole() {
		showConsole = !showConsole;
		if (showConsole) {
			level3Bottom.setWeights(new int[]{1, 1});
		} else {
			level3Bottom.setWeights(new int[]{0, 1});
		}
	}
}
