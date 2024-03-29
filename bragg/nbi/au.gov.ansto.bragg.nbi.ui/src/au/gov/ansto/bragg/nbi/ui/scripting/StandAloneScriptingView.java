package au.gov.ansto.bragg.nbi.ui.scripting;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import au.gov.ansto.bragg.nbi.ui.scripting.parts.StandAloneScriptingViewer;


public class StandAloneScriptingView extends ViewPart {

	public final static String ID_VIEW_STANDALONESCRIPTING = "au.gov.ansto.bragg.nbi.ui.scripting.StandAloneScriptingView";
	private StandAloneScriptingViewer viewer;
	
	public StandAloneScriptingView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new StandAloneScriptingViewer(parent, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(viewer);
	}

	@Override
	public void setFocus() {
	}

	public StandAloneScriptingViewer getViewer() {
		return viewer;
	}
}
