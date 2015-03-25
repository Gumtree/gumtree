/**
 * 
 */
package au.gov.ansto.bragg.taipan.ui.internal;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

/**
 * @author nxi
 *
 */
public class LaunchCalibrationHandler extends AbstractHandler {

	private static final String ID_CALIBRATION_PERSPECTIVE = "au.gov.ansto.bragg.taipan.ui.TaipanCalibrationPerspective";
	/**
	 * 
	 */
	public LaunchCalibrationHandler() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
			PlatformUI.getWorkbench().showPerspective(ID_CALIBRATION_PERSPECTIVE, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		} catch (WorkbenchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
