/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.scan;

import au.gov.ansto.bragg.koala.ui.sics.ControlHelper.InstrumentPhase;

/**
 * @author nxi
 *
 */
public interface IExperimentModelListener {

	public void proposalIdChanged(String newId);
	public void updateLastFilename(String filename);
	public void onError(String errorMessage);
	public void phaseChanged(InstrumentPhase newPhase, int time);
	
}
