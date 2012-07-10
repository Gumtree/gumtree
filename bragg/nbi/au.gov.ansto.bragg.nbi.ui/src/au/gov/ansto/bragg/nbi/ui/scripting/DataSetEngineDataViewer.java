package au.gov.ansto.bragg.nbi.ui.scripting;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.gumtree.ui.scripting.support.AbstractEgnineDataViewer;

public class DataSetEngineDataViewer extends AbstractEgnineDataViewer {

	public void createControl(Composite parent) {
		parent.setLayout(new FillLayout());
		Text text = new Text(parent, SWT.READ_ONLY | SWT.WRAP | SWT.BORDER);
		text.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		if (getEngineDataTreeNode().getData() != null) {
			text.setText(getEngineDataTreeNode().getData().toString());
		} else {
			text.setText("UNAVAILABLE");
		}
	}

	public void dispose() {
		
	}

}
