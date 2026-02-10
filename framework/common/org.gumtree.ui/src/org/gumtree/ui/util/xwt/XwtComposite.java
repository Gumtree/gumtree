package org.gumtree.ui.util.xwt;

import java.net.URL;

import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.xwt.XWT;
import org.gumtree.core.object.IConfigurable;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.widgets.swt.ExtendedComposite;

public class XwtComposite extends ExtendedComposite {

	private URL fileUrl;
	
	public XwtComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout());
	}

	@Override
	protected void disposeWidget() {
		fileUrl = null;
	}
	
	public URL getFileUrl() {
		return fileUrl;
	}
	
	public void setFileUrl(final URL fileUrl) {
		if (fileUrl != null) {
			SafeUIRunner.asyncExec(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					for (Control child : getChildren()) {
						child.dispose();
					}
					XWT.setLoadingContext(new LoadingContext());
					Control control = (Control) XWT.load(XwtComposite.this, fileUrl);
					activateControls(control);
					if (control instanceof Shell) {
						((Shell) control).pack();
						((Shell) control).open();
					} else {
						layout(true, true);
					}
				}
			});
		}
	}

	// Recursively activate configurable widgets
	private void activateControls(Control control) {
		if (control instanceof Composite) {
			for (Control child : ((Composite) control).getChildren()) {
				activateControls(child);
			}
		}
		if (control instanceof IConfigurable) {
			((IConfigurable) control).afterParametersSet();
		}
	}

}
