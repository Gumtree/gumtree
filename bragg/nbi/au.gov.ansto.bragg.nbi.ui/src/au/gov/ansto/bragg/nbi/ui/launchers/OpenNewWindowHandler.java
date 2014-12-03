/**
 * 
 */
package au.gov.ansto.bragg.nbi.ui.launchers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.gumtree.ui.service.multimonitor.IMultiMonitorManager;
import org.gumtree.ui.service.multimonitor.support.MultiMonitorManager;

/**
 * @author nxi
 *
 */
public class OpenNewWindowHandler extends AbstractHandler {

	private static final String ID_DEFAULT_NEW_WINDOW = "gumtree.platform.defaultNewWindow";
	/**
	 * 
	 */
	public OpenNewWindowHandler() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		System.err.println("new winodw button clicked");
		IMultiMonitorManager mmManager = new MultiMonitorManager();
		
		mmManager.openWorkbenchWindow(System.getProperty(ID_DEFAULT_NEW_WINDOW), 0, false);
		
		return null;
	}

}
