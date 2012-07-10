/**
 * 
 */
package au.gov.ansto.bragg.nbi.ui.scripting;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.scripting.IScriptExecutor;
import org.gumtree.scripting.ScriptExecutor;
import org.gumtree.ui.scripting.viewer.CommandLineViewer;
import org.gumtree.ui.scripting.viewer.ICommandLineViewer;

/**
 * @author nxi
 *
 */
public class ConsoleView extends ViewPart {

	private CommandLineViewer viewer;
	private IScriptExecutor executor;
	
	/**
	 * 
	 */
	public ConsoleView() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		viewer = new CommandLineViewer();
		executor = new ScriptExecutor("jython");
		viewer.setScriptExecutor(executor);
		viewer.createPartControl(parent, ICommandLineViewer.NO_UTIL_AREA);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public CommandLineViewer getCommandLineViewer() {
		return viewer;
	}

	public IScriptExecutor getExecutor() {
		return executor;
	}

}
