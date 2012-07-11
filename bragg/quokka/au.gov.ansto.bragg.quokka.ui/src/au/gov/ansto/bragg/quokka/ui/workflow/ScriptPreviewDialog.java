package au.gov.ansto.bragg.quokka.ui.workflow;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ScriptPreviewDialog extends ApplicationWindow {

	private String script;
	
	public ScriptPreviewDialog(String script) {
		this(script, null);
	}
	
	public ScriptPreviewDialog(String script, Shell parentShell) {
		super(parentShell);
		this.script = script;
	}
	
	protected Control createContents(Composite parent) {
	    getShell().setText("Script Preview");
	    parent.setSize(500, 600);
	    Text text = new Text(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
	    text.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
	    text.setText(script);
	    return parent;
	}
	 
	public static void main(String[] args) {
		ScriptPreviewDialog dialog = new ScriptPreviewDialog("Script");
		dialog.setBlockOnOpen(true);
		dialog.open();
	    Display.getCurrent().dispose();
	}
	
}
