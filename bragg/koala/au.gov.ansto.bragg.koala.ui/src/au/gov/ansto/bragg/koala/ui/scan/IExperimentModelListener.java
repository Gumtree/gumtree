/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.scan;

/**
 * @author nxi
 *
 */
public interface IExperimentModelListener {

	public void proposalIdChanged(String newId);
	public void updateLastFilename(String filename);
	public void onError(String errorMessage);
	
}
