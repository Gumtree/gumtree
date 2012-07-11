/**
 * 
 */
package au.gov.ansto.bragg.wombat.ui.script;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import au.gov.ansto.bragg.wombat.ui.script.parts.ScriptControlViewer;

/**
 * @author nxi
 *
 */
public class ControlView extends ViewPart {

	private ScriptControlViewer viewer;
	/**
	 * 
	 */
	public ControlView() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		viewer = new ScriptControlViewer(parent, SWT.None);
		GridDataFactory.fillDefaults().applyTo(viewer);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public ScriptControlViewer getViewer() {
		return viewer;
	}
}
