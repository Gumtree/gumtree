/**
 * 
 */
package au.gov.ansto.bragg.nbi.ui.preference;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import au.gov.ansto.bragg.nbi.ui.scripting.StandAloneScriptingView;

/**
 * @author nxi
 *
 */
public class ShowScriptingConsoleHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			try {
				IViewReference[] viewReferences = window.getActivePage().getViewReferences();
				for (IViewReference viewReference : viewReferences) {
					if (StandAloneScriptingView.ID_VIEW_STANDALONESCRIPTING.equals(viewReference.getId())) {
						IViewPart viewPart = viewReference.getView(false);
						if (viewPart != null && viewPart instanceof StandAloneScriptingView) {
							((StandAloneScriptingView) viewPart).getViewer().toggleShowingConsole();
						}
					}
				}
			} catch (Exception e) {
			}
		}
		return null;
	}

}
