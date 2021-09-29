package au.gov.ansto.bragg.koala.ui.parts;

import org.eclipse.swt.widgets.Composite;

public abstract class AbstractPanel extends Composite {

	AbstractPanel(Composite parent, int style) {
		super(parent, style);
	}

	public abstract void next();
	public abstract void back();
//	public abstract boolean isBackEnabled();
//	public abstract boolean isNextEnabled();
	public abstract void show();

}
