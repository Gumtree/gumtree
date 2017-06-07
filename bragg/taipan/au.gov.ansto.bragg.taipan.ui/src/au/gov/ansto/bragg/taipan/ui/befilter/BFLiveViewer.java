package au.gov.ansto.bragg.taipan.ui.befilter;

import java.io.FileNotFoundException;

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

public class BFLiveViewer extends Composite {

	private ScriptExecutor Jython_Executor;
	private static final String BF_LIVESCRIPT_NAME = "gumtree.scripting.BFInitialiseScript";
	private static final String BF_REDUCTIONSCRIPT_NAME = "gumtree.scripting.reductionScript";
	
	private ScriptControlViewer controlViewer;
	private CommandLineViewer consoleViewer;
	private ScriptDataSourceViewer dataSourceViewer;
	private ScriptInfoViewer infoViewer;
	
	public BFLiveViewer(Composite parent, int style) {
		super(parent, style);
		GridLayoutFactory.fillDefaults().applyTo(this);
		ScriptPageRegister register = new ScriptPageRegister();
		
		SashForm level1Form = new SashForm(this, SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(level1Form);

		SashForm level2Left = new SashForm(level1Form, SWT.VERTICAL);
		SashForm level2Right = new SashForm(level1Form, SWT.VERTICAL);
		level1Form.setWeights(new int[]{3, 7});
		
		dataSourceViewer = new ScriptDataSourceViewer(level2Left, SWT.NONE);
		createControlArea(level2Left);
		level2Left.setWeights(new int[]{4, 6});

//		SashForm level2Left = new SashForm(level1Form, SWT.VERTICAL);

//		level2Left.setWeights(new int[]{21, 10});
		
		SashForm level3Top = new SashForm(level2Right, SWT.HORIZONTAL);
		SashForm level4Left = new SashForm(level3Top, SWT.VERTICAL);
		PlotViewer plot1Viewer = new PlotViewer(level4Left, SWT.NONE);
		PlotViewer plot2Viewer = new PlotViewer(level4Left, SWT.NONE);
		level4Left.setWeights(new int[]{1, 1});
		PlotViewer plot3Viewer = new PlotViewer(level3Top, SWT.NONE);
		level3Top.setWeights(new int[]{1, 1});
		Composite consoleComposite = new Composite(level2Right, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(consoleComposite);
		level2Right.setWeights(new int[]{2, 1});
//		GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 180).applyTo(consoleComposite);
		consoleViewer.createPartControl(consoleComposite, ICommandLineViewer.NO_UTIL_AREA);
		
		ScriptPageRegister.registPage(controlViewer.getScriptRegisterID(), register);
		register.setControlViewer(controlViewer);
		register.setConsoleViewer(consoleViewer);
		register.setDataSourceViewer(dataSourceViewer);
		register.setInfoViewer(infoViewer);
		register.registerObject("Plot1", plot1Viewer);
		register.registerObject("Plot2", plot2Viewer);
		register.registerObject("Plot3", plot3Viewer);
		controlViewer.runNativeInitScript();
//		controlViewer.loadScript(ScriptControlViewer.getFullScriptPath(System.getProperty(BF_LIVESCRIPT_NAME)));
		Thread delayedThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
				}
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						try {
							controlViewer.initScriptControl(ScriptControlViewer.getFullScriptPath(System.getProperty(BF_LIVESCRIPT_NAME)));
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}
		});
		delayedThread.start();

	}

	private void createControlArea(SashForm parent) {
		ScriptExecutor scriptExecutor = getScriptExecutor();
		while(scriptExecutor.getEngine() == null) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.err.println("waiting for engine");
		}
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
//		controlViewer.getStaticComposite().setVisible(false);
//		controlViewer.getStaticComposite().dispose();
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
