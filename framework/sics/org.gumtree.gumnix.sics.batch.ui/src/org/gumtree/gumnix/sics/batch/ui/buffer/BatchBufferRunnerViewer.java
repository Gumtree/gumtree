/**
 * 
 */
package org.gumtree.gumnix.sics.batch.ui.buffer;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Form;
import org.gumtree.gumnix.sics.batch.ui.internal.Activator;
import org.gumtree.ui.util.forms.FormComposite;
import org.gumtree.ui.util.swt.IDNDHandler;

@SuppressWarnings("restriction")
public class BatchBufferRunnerViewer extends FormComposite {
	
	private IEclipseContext eclipseContext;

	public BatchBufferRunnerViewer(Composite parent, int style) {
		super(parent, style);

		// Set up injection
		eclipseContext = Activator.getDefault().getEclipseContext()
				.createChild();
		IDNDHandler<IBatchBufferManager> dndHandler = ContextInjectionFactory
				.make(DNDHandler.class, eclipseContext);
		eclipseContext.set(IDNDHandler.class, dndHandler);

		// Render
		render();
	}

	public void render() {
		this.setLayout(new FillLayout());
		Form form = getToolkit().createForm(this);
		getToolkit().decorateFormHeading(form);
		form.setText("Batch Buffer");
		form.getBody().setLayout(new FillLayout());

		BatchRunnerPage runPage = new BatchRunnerPage(form.getBody(), SWT.NONE);
		ContextInjectionFactory.inject(runPage, eclipseContext);
	}

	@Override
	protected void disposeWidget() {
		if (eclipseContext != null) {
			eclipseContext.dispose();
			eclipseContext = null;
		}
	}

}