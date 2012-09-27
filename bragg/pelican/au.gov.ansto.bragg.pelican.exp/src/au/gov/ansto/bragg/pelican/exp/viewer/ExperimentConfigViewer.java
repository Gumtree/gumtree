package au.gov.ansto.bragg.pelican.exp.viewer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.gumtree.ui.scripting.viewer.CommandLineViewer;
import org.gumtree.ui.scripting.viewer.ICommandLineViewer;

import au.gov.ansto.bragg.nbi.ui.scripting.ScriptPageRegister;
import au.gov.ansto.bragg.nbi.ui.scripting.parts.ScriptControlViewer;
import au.gov.ansto.bragg.pelican.exp.config.CommandHandler;

public class ExperimentConfigViewer extends Composite {

	private ScriptControlViewer controlViewer;
	
	public ExperimentConfigViewer(Composite parent, int style) {
		super(parent, style);
		Color whiteColor = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(this);
		ScriptPageRegister register = new ScriptPageRegister();
		CommandHandler commandHandler = new CommandHandler();
		CommandLineViewer viewer = new CommandLineViewer();
		viewer.setScriptExecutor(commandHandler.getScriptExecutor());
		controlViewer = new ScriptControlViewer(this, style);
		controlViewer.setScriptExecutor(commandHandler.getScriptExecutor());
		controlViewer.setBackground(whiteColor);
		Label titleLabel = new Label(controlViewer, SWT.NONE);
		titleLabel.setBackground(whiteColor);
		FontData[] fD = titleLabel.getFont().getFontData();
		fD[0].setHeight(18);
		titleLabel.setFont(new Font(titleLabel.getDisplay(), fD));
		titleLabel.setText(" Experiment Setup");
		controlViewer.getScrollArea().moveAbove(controlViewer.getStaticComposite());
		titleLabel.moveAbove(controlViewer.getScrollArea());
		controlViewer.getStaticComposite().setVisible(false);
		controlViewer.getStaticComposite().dispose();
		GridLayoutFactory.fillDefaults().applyTo(controlViewer);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(controlViewer);
		ScriptPageRegister.registPage(controlViewer.getScriptRegisterID(), register);
		register.setControlViewer(controlViewer);
		
		Composite consoleComposite = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(consoleComposite);
		GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 104).applyTo(consoleComposite);
		viewer.createPartControl(consoleComposite, ICommandLineViewer.NO_UTIL_AREA | ICommandLineViewer.NO_INPUT_TEXT);
		
		controlViewer.loadScript(ScriptControlViewer.WORKSPACE_FOLDER_PATH + "/Internal/Experiment/config.py");
//		Button applyButton = new Button(this, SWT.PUSH);
//		applyButton.setText("Apply Changes");
	}

	
}
