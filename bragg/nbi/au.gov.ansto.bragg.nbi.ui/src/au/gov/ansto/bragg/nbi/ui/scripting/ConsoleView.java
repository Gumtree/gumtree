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

	private static final String CONTENT_ASSISTANT_ENABLED = "gumtree.scripting.assistantEnabled";

	private ICommandLineViewer viewer;
	private IScriptExecutor executor;
	private IScriptExecutor validator;
	
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
		viewer.setContentAssistEnabled(true);
//		try {
//			if (!Boolean.valueOf(System.getProperty(CONTENT_ASSISTANT_ENABLED))) {
//				viewer.setContentAssistEnabled(false);
//			}
//		} catch (Exception e) {
//		}
		executor = new ScriptExecutor("jython");
		validator = new ScriptExecutor("jython");
		viewer.setScriptExecutor(executor);
//		validator.getEngine().getContext().setErrorWriter(executor.getEngine().getContext().getErrorWriter());
		viewer.createPartControl(parent, ICommandLineViewer.NO_UTIL_AREA);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public ICommandLineViewer getCommandLineViewer() {
		return viewer;
	}

	public IScriptExecutor getExecutor() {
		return executor;
	}

	public IScriptExecutor getValidator() {
		return validator;
	}
}
