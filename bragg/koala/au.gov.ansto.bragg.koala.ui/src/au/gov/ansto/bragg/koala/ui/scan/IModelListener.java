/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.scan;

import au.gov.ansto.bragg.koala.ui.scan.AbstractScanModel.ModelStatus;

/**
 * @author nxi
 *
 */
public interface IModelListener {

	void modelChanged();
	
	void progressUpdated(ModelStatus status);
}
