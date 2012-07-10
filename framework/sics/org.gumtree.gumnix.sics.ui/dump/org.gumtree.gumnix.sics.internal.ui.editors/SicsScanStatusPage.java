package org.gumtree.gumnix.sics.internal.ui.editors;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.IScanController;
import org.gumtree.gumnix.sics.control.events.IScanControllerListener;
import org.gumtree.gumnix.sics.control.events.ScanControllerListenerAdapter;
import org.gumtree.vis.ui.onedplot.OneDVis;

public class SicsScanStatusPage extends FormPage {

	public static final String ID = "status";

	private static final String TITLE = "Scan Status";

	private IScanController controller;

	private FormToolkit toolkit;

	private IScanControllerListener controllerListener;

	private Text modeText;

	private Text presetText;

	private Text variableText;

	private Text filenameText;

	private Text npText;

	private Text variableValueText;

	private Text countText;

	private OneDVis oneDVis;

	public SicsScanStatusPage(FormEditor editor) {
		super(editor, ID, TITLE);
	}

	protected void createFormContent(IManagedForm managedForm) {
		Composite parent = managedForm.getForm().getBody();
//		parent.setLayout(new FillLayout());
		parent.setLayout(new GridLayout(2, true));
//		bindingContext = BasicBindingFactory.createContext(parent);
		toolkit = managedForm.getToolkit();

//		SashForm sashForm = new SashForm(parent, SWT.NONE);
//		getToolkit().adapt(sashForm);
//		Composite statusArea = getToolkit().createComposite(sashForm);
		createStatusArea(parent);
//		Composite graphArea = getToolkit().createComposite(sashForm);
		createPlotArea(parent);
//		sashForm.setWeights(new int[] {1, 1});

		getScanController().addComponentListener(getControllerListener());

		updateValues();
	}

	private void createStatusArea(Composite parent) {
//		parent.setLayout(new GridLayout());
		GridData fillGridData = new GridData(SWT.FILL, SWT.TOP, true, false);

		Section settingSection = getToolkit().createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
		settingSection.setText("Scan Setting");
		Composite composite = getToolkit().createComposite(settingSection);
		settingSection.setClient(composite);
		composite.setLayout(new GridLayout(2, false));
		settingSection.setLayoutData(fillGridData);

		Label label = getToolkit().createLabel(composite, "Mode ", SWT.RIGHT_TO_LEFT);
		modeText = getToolkit().createText(composite, "", SWT.READ_ONLY);
		modeText.setLayoutData(fillGridData);
//		getBindingContext().bind(modeText, new Property(getScanController().status(), "mode"), null);

		label = getToolkit().createLabel(composite, "Preset ");
		presetText = getToolkit().createText(composite, "", SWT.READ_ONLY);
		presetText.setLayoutData(fillGridData);
//		getBindingContext().bind(presetText, new Property(getScanController().status(), "preset"), null);

		label = getToolkit().createLabel(composite, "Scan Variable ");
		variableText = getToolkit().createText(composite, "", SWT.READ_ONLY);
		variableText.setLayoutData(fillGridData);
//		getBindingContext().bind(variableText, new Property(getScanController().config(), "variable"), null);

		Section currentSection = getToolkit().createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
		currentSection.setText("Current Values");
		composite = getToolkit().createComposite(currentSection);
		currentSection.setClient(composite);
		composite.setLayout(new GridLayout(2, false));
		currentSection.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		label = getToolkit().createLabel(composite, "Filename ");
		filenameText = getToolkit().createText(composite, "", SWT.READ_ONLY);
		filenameText.setLayoutData(fillGridData);
//		getBindingContext().bind(presetText, new Property(getScanController().status(), "filename"), null);

		label = getToolkit().createLabel(composite, "Scan Point ");
		npText = getToolkit().createText(composite, "", SWT.READ_ONLY);
		npText.setLayoutData(fillGridData);
//		getBindingContext().bind(npText, new Property(getScanController().status(), "currentScanPoint"), null);

		label = getToolkit().createLabel(composite, "Variable Value ");
		variableValueText = getToolkit().createText(composite, "", SWT.READ_ONLY);
		variableValueText.setLayoutData(fillGridData);
//		getBindingContext().bind(variableValueText, new Property(getScanController().status(), "scanVariableValue"), null);

		label = getToolkit().createLabel(composite, "Count ");
		countText = getToolkit().createText(composite, "", SWT.READ_ONLY);
		countText.setLayoutData(fillGridData);
//		getBindingContext().bind(countText, new Property(getScanController().status(), "count"), null);
		
		Realm.runWithDefault(SWTObservables.getRealm(PlatformUI.getWorkbench()
				.getDisplay()), new Runnable() {
			public void run() {
				DataBindingContext bindingContext = new DataBindingContext();
				bindingContext.bindValue(SWTObservables.observeText(modeText,
						SWT.Modify), BeansObservables.observeValue(
								getScanController().status(), "mode"), new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(SWTObservables.observeText(presetText,
						SWT.Modify), BeansObservables.observeValue(
								getScanController().status(), "preset"), new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(SWTObservables.observeText(variableText,
						SWT.Modify), BeansObservables.observeValue(
								getScanController().status(), "variable"), new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(SWTObservables.observeText(npText,
						SWT.Modify), BeansObservables.observeValue(
								getScanController().status(), "currentScanPoint"), new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(SWTObservables.observeText(variableValueText,
						SWT.Modify), BeansObservables.observeValue(
								getScanController().status(), "scanVariableValue"), new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(SWTObservables.observeText(countText,
						SWT.Modify), BeansObservables.observeValue(
								getScanController().status(), "count"), new UpdateValueStrategy(), new UpdateValueStrategy());
			}
		});
	}

	private void createPlotArea(Composite parent) {
		oneDVis = new OneDVis(parent, SWT.NONE);
		oneDVis.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		((GridData)oneDVis.getLayoutData()).horizontalSpan = 2;
//		getToolkit().adapt(oneDVis);

//		Composite embeddedComposite = new Composite(parent, SWT.EMBEDDED);
//		embeddedComposite.setLayout(new FillLayout());
//		Frame frame = SWT_AWT.new_Frame(embeddedComposite);
//
//		XYSeries series = new XYSeries("Data");
//
//		XYSeriesCollection dataset = new XYSeriesCollection();
//		dataset.addSeries(series);
//
//		JFreeChart chart = ChartFactory.createXYLineChart("Scan Data", "Scan", "Count", dataset, PlotOrientation.VERTICAL, true, true, false);
//		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)((XYPlot)chart.getPlot()).getRenderer();
//		renderer.setShapesVisible(true);
//		renderer.setDrawOutlines(true);
//		renderer.setShapesFilled(false);
//		chart.setBackgroundPaint(Color.WHITE);
//		ChartPanel panel = new ChartPanel(chart);
//		frame.add(panel);
	}

	public void dispose() {
		if(controllerListener != null) {
			getScanController().removeComponentListener(controllerListener);
			controllerListener = null;
		}
		super.dispose();
	}

	private IScanController getScanController() {
		if(controller == null) {
			Object componentController = getEditorInput().getAdapter(IComponentController.class);
			if(componentController instanceof IScanController) {
				controller = (IScanController)componentController;
			}
		}
		return controller;
	}

	private FormToolkit getToolkit() {
		return toolkit;
	}

	private IScanControllerListener getControllerListener() {
		if(controllerListener == null) {
			controllerListener = new ScanControllerListenerAdapter() {
				public void scanStatusUpdated() {
					updateValues();
				}
			};
		}
		return controllerListener;
	}

	private void updateValues() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				SafeRunner.run(new ISafeRunnable() {
					public void handleException(Throwable exception) {
					}
					public void run() throws Exception {
						modeText.setText(getScanController().status().getMode());
						presetText.setText(Float.toString(getScanController().status().getPreset()));
						variableText.setText(getScanController().config().getVariable());
						filenameText.setText(getScanController().status().getFilename());
						npText.setText(Integer.toString(getScanController().status().getCurrentScanPoint()));
						variableValueText.setText(Float.toString(getScanController().status().getScanVariableValue()));
						countText.setText(Integer.toString(getScanController().status().getCount()));
					}
				});
			}
		});
	}

}
