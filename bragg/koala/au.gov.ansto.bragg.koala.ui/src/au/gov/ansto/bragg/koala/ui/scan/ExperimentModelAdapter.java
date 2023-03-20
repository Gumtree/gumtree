/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.scan;

import au.gov.ansto.bragg.koala.ui.sics.ControlHelper.InstrumentPhase;

/**
 * @author nxi
 *
 */
public class ExperimentModelAdapter implements IExperimentModelListener {

	@Override
	public void proposalIdChanged(String newId) {
	}

	@Override
	public void updateLastFilename(String filename) {
	}

	@Override
	public void onError(String errorMessage) {
	}
	
	@Override
	public void phaseChanged(InstrumentPhase newPhase, int time) {
	}
	
	@Override
	public void onNotice(String noticeMessage) {
	}
}
