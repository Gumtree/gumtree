package org.gumtree.control.ui.batch;

//import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.Form;
import org.gumtree.control.core.SicsManager;
import org.gumtree.widgets.swt.forms.ExtendedFormComposite;

@SuppressWarnings("restriction")
public class BatchManagerViewer extends ExtendedFormComposite {

	public static final int HIDE_RUNNER = 1 << 1;

	public static final int HIDE_VALIDATOR = 1 << 2;

	private IEclipseContext eclipseContext;
	
	private IBatchManager manager;

	private int viwerStyle;

	public BatchManagerViewer(Composite parent, int style) {
		super(parent, SWT.NONE);

		viwerStyle = style;

		manager = BatchManager.getBatchScriptManager(SicsManager.getSicsProxy());

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
		runnerPage.setBatchBufferManager(manager);
		runnerPage.render();
//		ContextInjectionFactory.inject(runnerPage, eclipseContext);
		return runnerPage;
	}

	private Control createValidatorPage(Composite parent) {
		BatchValidationPage validationPage = new BatchValidationPage(parent,
				SWT.NONE);
		validationPage.setManager(manager);
		validationPage.render();
//		ContextInjectionFactory.inject(validationPage, eclipseContext);
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
