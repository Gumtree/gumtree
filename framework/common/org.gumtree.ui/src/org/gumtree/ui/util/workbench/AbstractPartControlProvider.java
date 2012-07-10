package org.gumtree.ui.util.workbench;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPartControlProvider implements IPartControlProvider {

	private static final Logger logger = LoggerFactory.getLogger(AbstractPartControlProvider.class);
	
	private boolean isDisposed;
	
	public void createPartControl(Composite parent) {
		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (isDisposed()) {
					logger.info("Unnecessary dispose method is called.");
					return;
				}
				dispose();
				isDisposed = true;
			}
		});
		createControl(parent);
	}

	protected abstract void createControl(Composite parent);
	
	public boolean isDisposed() {
		return isDisposed;
	}

	public void setFocus() {
	}

}
