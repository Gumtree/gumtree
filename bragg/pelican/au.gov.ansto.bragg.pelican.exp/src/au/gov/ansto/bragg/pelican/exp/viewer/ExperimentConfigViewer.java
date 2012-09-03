package au.gov.ansto.bragg.pelican.exp.viewer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

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
		controlViewer = new ScriptControlViewer(this, style);
		controlViewer.setScriptExecutor(commandHandler.getScriptExecutor());
		controlViewer.setBackground(whiteColor);
		Label titleLabel = new Label(controlViewer, SWT.NONE);
		titleLabel.setBackground(whiteColor);
		FontData[] fD = titleLabel.getFont().getFontData();
		fD[0].setHeight(28);
		titleLabel.setFont(new Font(titleLabel.getDisplay(), fD));
		titleLabel.setText(" Experiment Setup");
		controlViewer.getScrollArea().moveAbove(controlViewer.getStaticComposite());
		titleLabel.moveAbove(controlViewer.getScrollArea());
		controlViewer.getStaticComposite().setVisible(false);
		GridLayoutFactory.fillDefaults().applyTo(controlViewer);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(controlViewer);
		ScriptPageRegister.registPage(controlViewer.getScriptRegisterID(), register);
		register.setControlViewer(controlViewer);
		
		controlViewer.loadScript(ScriptControlViewer.WORKSPACE_FOLDER_PATH + "/Internal/Experiment/config.py");
//		Button applyButton = new Button(this, SWT.PUSH);
//		applyButton.setText("Apply Changes");
	}

	
}
