package org.gumtree.gumnix.sics.batch.ui.buffer;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.Form;
import org.gumtree.gumnix.sics.batch.ui.internal.Activator;
import org.gumtree.ui.util.forms.FormComposite;
import org.gumtree.ui.util.swt.IDNDHandler;

@SuppressWarnings("restriction")
public class BatchBufferManagerViewer extends FormComposite {

	public static final int HIDE_RUNNER = 1 << 1;

	public static final int HIDE_VALIDATOR = 1 << 2;

	private IEclipseContext eclipseContext;

	private int viwerStyle;

	public BatchBufferManagerViewer(Composite parent, int style) {
		super(parent, SWT.NONE);

		viwerStyle = style;

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

		if (viwerStyle == 0) {
			CTabFolder tabFolder = new CTabFolder(form.getBody(), SWT.BOTTOM);

			// Run tab
			CTabItem runTab = new CTabItem(tabFolder, SWT.NONE);
			Control runnerPage = createRunnerPage(tabFolder);
			runTab.setControl(runnerPage);
			runTab.setText("Run");

			// Validation tab
			CTabItem validationTab = new CTabItem(tabFolder, SWT.NONE);
			Control validationPage = createValidatorPage(tabFolder);
			validationTab.setControl(validationPage);
			validationTab.setText("Validate");

			// Set default
			tabFolder.setSelection(runTab);
		} else if ((viwerStyle & HIDE_RUNNER) == 0) {
			createRunnerPage(form.getBody());
			form.setText("Batch Buffer Runner");
		} else if ((viwerStyle & HIDE_VALIDATOR) == 0) {
			createValidatorPage(form.getBody());
			form.setText("Batch Buffer Validation");
		}
	}

	private Control createRunnerPage(Composite parent) {
		BatchRunnerPage runnerPage = new BatchRunnerPage(parent, SWT.NONE);
		ContextInjectionFactory.inject(runnerPage, eclipseContext);
		return runnerPage;
	}

	private Control createValidatorPage(Composite parent) {
		BatchValidationPage validationPage = new BatchValidationPage(parent,
				SWT.NONE);
		ContextInjectionFactory.inject(validationPage, eclipseContext);
		return validationPage;
	}

	@Override
	protected void disposeWidget() {
		if (eclipseContext != null) {
			eclipseContext.dispose();
			eclipseContext = null;
		}
	}

}
