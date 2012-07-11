/**
 * 
 */
package au.gov.ansto.bragg.quokka.ui.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Composite;

import au.gov.ansto.bragg.kakadu.ui.instrument.InstrumentDataSourceComposite;

/**
 * @author nxi
 *
 */
public class QuokkaDataSourceComposite extends InstrumentDataSourceComposite {

	/**
	 * @param parent
	 * @param style
	 */
	public QuokkaDataSourceComposite(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	protected void createActions() {
		super.createActions();
	}
	
	@Override
	public List<Object> getActionList() {
		final ArrayList<Object> result = new ArrayList<Object>();
		result.add(addFileAction);
		result.add(addDirectoryAction);
		result.add(removeFileAction);
		result.add(removeAllAction);
		result.add(new Separator());
		result.add(dynamicHelpAction);
		
		return result;
	}
}
