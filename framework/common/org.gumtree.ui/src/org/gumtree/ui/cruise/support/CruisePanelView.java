package org.gumtree.ui.cruise.support;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISizeProvider;
import org.eclipse.ui.part.ViewPart;

public class CruisePanelView extends ViewPart {

	@Override
	public void createPartControl(final Composite parent) {
		new CruisePanel(parent, SWT.NONE);
	}

	@Override
	public void setFocus() {
	}

	public Object getAdapter(Class adapter) {
	    if (ISizeProvider.class == adapter) {
	        return new ISizeProvider() {
	            public int getSizeFlags(boolean width) {
	                return SWT.MIN | SWT.MAX | SWT.FILL;
	            }

	            public int computePreferredSize(boolean width, int availableParallel, int availablePerpendicular, int preferredResult) {
	                return width ? 200 : preferredResult;
	            }
	        };
	    }
	    return super.getAdapter(adapter);
	}
	
}
