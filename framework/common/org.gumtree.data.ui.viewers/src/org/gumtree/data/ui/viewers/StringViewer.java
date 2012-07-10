package org.gumtree.data.ui.viewers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

public class StringViewer extends ExtendedComposite {
	
	private Text text;
	
	public StringViewer(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout());
		
		Group dataGroup = getWidgetFactory().createGroup(this, "Data", SWT.NONE);
		dataGroup.setText("Data");
		dataGroup.setLayout(new FillLayout());
		
		text = getWidgetFactory().createText(dataGroup, "", SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
		
	}

	public void setString(final String string) {
		Display display = Display.getDefault(); 
		if (display == null || display.isDisposed()) {
			return;
		}
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				if (text != null) {
					text.setText(string);
				}
			}
		});
	}
	
	@Override
	protected void disposeWidget() {
		text = null;
	}

}
