package au.gov.ansto.bragg.quokka.msw.composites;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IElementListListener;
import org.gumtree.msw.elements.IElementPropertyListener;
import org.gumtree.msw.schedule.AcquisitionAspect;
import org.gumtree.msw.schedule.AcquisitionEntry;
import org.gumtree.msw.schedule.ISchedulerListener;
import org.gumtree.msw.schedule.ScheduledAspect;
import org.gumtree.msw.schedule.ScheduledNode;
import org.gumtree.msw.schedule.Scheduler;
import org.gumtree.msw.schedule.execution.AcquisitionSummary;
import org.gumtree.msw.schedule.execution.IScheduleProvider;
import org.gumtree.msw.schedule.execution.IScheduleWalkerListener;
import org.gumtree.msw.schedule.execution.ParameterChangeSummary;
import org.gumtree.msw.schedule.execution.ScheduleStep;
import org.gumtree.msw.schedule.execution.ScheduleWalker;
import org.gumtree.msw.schedule.execution.Summary;
import org.gumtree.msw.ui.Resources;
import org.gumtree.msw.ui.ktable.ButtonInfo;
import org.gumtree.msw.ui.ktable.CheckableCellRenderer;
import org.gumtree.msw.ui.ktable.IButtonListener;
import org.gumtree.msw.ui.ktable.NameCellRenderer;
import org.gumtree.msw.ui.ktable.ScheduleTableModel;
import org.gumtree.msw.ui.ktable.ScheduleTableModel.FixedCellDefinition;
import org.gumtree.msw.ui.ktable.ScheduleTableModel.PropertyCellDefinition;
import org.gumtree.msw.ui.ktable.ScheduleTableModel.RowDefinition;

import au.gov.ansto.bragg.quokka.msw.Configuration;
import au.gov.ansto.bragg.quokka.msw.ConfigurationList;
import au.gov.ansto.bragg.quokka.msw.Environment;
import au.gov.ansto.bragg.quokka.msw.LoopHierarchy;
import au.gov.ansto.bragg.quokka.msw.Measurement;
import au.gov.ansto.bragg.quokka.msw.ModelProvider;
import au.gov.ansto.bragg.quokka.msw.Sample;
import au.gov.ansto.bragg.quokka.msw.SampleList;
import au.gov.ansto.bragg.quokka.msw.SetPoint;
import au.gov.ansto.bragg.quokka.msw.converters.IndexValueConverter;
import au.gov.ansto.bragg.quokka.msw.converters.PositionValueConverter;
import au.gov.ansto.bragg.quokka.msw.converters.TimeValueConverter;
import au.gov.ansto.bragg.quokka.msw.schedule.InstrumentActionExecuter;
import au.gov.ansto.bragg.quokka.msw.schedule.SyncScheduleProvider;
import au.gov.ansto.bragg.quokka.msw.util.QkkReportGenerator;
import org.gumtree.msw.ui.ktable.KTable;
import org.gumtree.msw.ui.ktable.KTableCellEditor;
import org.gumtree.msw.ui.ktable.SWTX;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorCheckbox;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorText2;
import org.gumtree.msw.ui.ktable.renderers.DefaultCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.TextCellRenderer;

public class AcquisitionComposite extends Composite {
	// fields
	private final KTable tblAcquisitions;
	private final Menu menu;
	private LoopHierarchy loopHierarchy;
	private Button btnRun;

	// construction
	public AcquisitionComposite(Composite parent, ModelProvider provider) {
		super(parent, SWT.BORDER);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 10;
		gridLayout.marginHeight = 10;
		gridLayout.horizontalSpacing = 0;
		setLayout(gridLayout);
		setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		
		Composite cmpContent = new Composite(this, SWT.NONE);
		cmpContent.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true, 1, 1));
		cmpContent.setLayout(new GridLayout(1, false));
		cmpContent.setBackground(getBackground());
		
		Composite cmpTable = new Composite(cmpContent, SWT.NONE);
		cmpTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		GridLayout gl_cmpTable = new GridLayout(1, false);
		gl_cmpTable.verticalSpacing = 0;
		gl_cmpTable.marginWidth = 0;
		gl_cmpTable.marginHeight = 0;
		gl_cmpTable.horizontalSpacing = 0;
		cmpTable.setLayout(gl_cmpTable);
		cmpTable.setBackground(getBackground());
		
		tblAcquisitions = new KTable(cmpTable, SWTX.EDIT_ON_KEY | SWT.V_SCROLL | SWT.H_SCROLL);
		tblAcquisitions.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, true, 1, 1));
		tblAcquisitions.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));

		Composite cmpBottom = new Composite(cmpContent, SWT.NONE);
		cmpBottom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout gl_cmpBottom = new GridLayout(7, false);
		gl_cmpBottom.marginWidth = 0;
		gl_cmpBottom.marginHeight = 0;
		gl_cmpBottom.horizontalSpacing = 10;
		cmpBottom.setLayout(gl_cmpBottom);
		cmpBottom.setBackground(getBackground());

		Label lblConfigurationTime = new Label(cmpBottom, SWT.NONE);
		lblConfigurationTime.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblConfigurationTime.setText("Configuration Time");
		new Label(cmpBottom, SWT.NONE);
		Label lblAcquisitionTime = new Label(cmpBottom, SWT.NONE);
		lblAcquisitionTime.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblAcquisitionTime.setText("Acquisition Time");
		new Label(cmpBottom, SWT.NONE);
		Label lblTotalTime = new Label(cmpBottom, SWT.NONE);
		lblTotalTime.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblTotalTime.setText("Total Time");
		
		new Label(cmpBottom, SWT.NONE);
		new Label(cmpBottom, SWT.NONE);
		
		Text txtConfigurationTime = new Text(cmpBottom, SWT.BORDER);
		GridData gd_txtConfigurationTime = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtConfigurationTime.widthHint = 100;
		txtConfigurationTime.setLayoutData(gd_txtConfigurationTime);
		txtConfigurationTime.setEnabled(false);
		Label lblPlus = new Label(cmpBottom, SWT.NONE);
		lblPlus.setText("+");
		Text txtAcquisitionTime = new Text(cmpBottom, SWT.BORDER);
		GridData gd_txtAcquisitionTime = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtAcquisitionTime.widthHint = 100;
		txtAcquisitionTime.setLayoutData(gd_txtAcquisitionTime);
		txtAcquisitionTime.setEnabled(false);
		Label lblEqual = new Label(cmpBottom, SWT.NONE);
		lblEqual.setText("=");
		Text txtTotalTime = new Text(cmpBottom, SWT.BORDER);
		GridData gd_txtTotalTime = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtTotalTime.widthHint = 100;
		txtTotalTime.setLayoutData(gd_txtTotalTime);
		txtTotalTime.setEnabled(false);
		
		Label lblSpace = new Label(cmpBottom, SWT.NONE);
		lblSpace.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		
		btnRun = new Button(cmpBottom, SWT.NONE);
		GridData gd_btnRun = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnRun.widthHint = 90;
		gd_btnRun.heightHint = 23;
		btnRun.setLayoutData(gd_btnRun);
		btnRun.setImage(Resources.IMAGE_PLAY);
		btnRun.setText("Run");
		
		// menu
	    menu = new Menu(this);
	    MenuItem menuItem;
	    
	    // enable/disable
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Enable All");
	    menuItem.setImage(Resources.IMAGE_BOX_CHECKED);
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Disable All");
	    menuItem.setImage(Resources.IMAGE_BOX_UNCHECKED);
	    
	    // export
	    new MenuItem(menu, SWT.SEPARATOR);
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("CSV Export");
	    menuItem.setImage(Resources.IMAGE_EXPORT);
	    menuItem.setEnabled(false);
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("PDF Export");
	    menuItem.setImage(Resources.IMAGE_EXPORT);
	    menuItem.setEnabled(false);
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("PNG Export");
	    menuItem.setImage(Resources.IMAGE_EXPORT);
	    menuItem.setEnabled(false);
	    
	    initDataBindings(provider);
	}

	// methods
	private void initDataBindings(final ModelProvider provider) {
		if (provider == null)
			return;
		
		loopHierarchy = provider.getLoopHierarchy();
		
		// setup table
		final Shell shell = getShell();
		final Display display = getDisplay();

		final ScheduleWalker walker = new ScheduleWalker();
		walker.addListener(new QkkReportGenerator(
				"D:/Users/davidm/Desktop/reportIntermediate.xml",
				"D:/Users/davidm/Desktop/reportFinal.xml"));
		walker.addListener(new IScheduleWalkerListener() {
			// schedule
			@Override
			public void onBeginSchedule() {
				System.out.println("onBeginSchedule");
				provider.getCustomInstrumentAction().setEnabled(false);

				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						MessageBox dialog = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
						dialog.setText("Information");
						dialog.setMessage("started");
						dialog.open();
					}
				});
			}
			@Override
			public void onEndSchedule() {
				System.out.println("onEndSchedule");
				provider.getCustomInstrumentAction().setEnabled(true);
				
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						MessageBox dialog = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
						dialog.setText("Information");
						dialog.setMessage("finished");
						dialog.open();
					}
				});
			}
			// step
			@Override
			public void onBeginStep(ScheduleStep step) {
				System.out.println("onBeginStep");
			}
			@Override
			public void onEndStep(ScheduleStep step) {
				System.out.println("onEndStep");
			}
			// parameters
			@Override
			public void onBeginChangeParameter(ScheduleStep step) {
				System.out.println("onBeginChangeParameter");
			}
			@Override
			public void onEndChangeParameters(ScheduleStep step, ParameterChangeSummary summary) {
				System.out.println("onEndChangeParameters");
			}
			// acquisition
			@Override
			public void onBeginPreAcquisition(ScheduleStep step) {
				System.out.println("onBeginPreAcquisition");
			}
			@Override
			public void onEndPreAcquisition(ScheduleStep step, Summary summary) {
				System.out.println("onEndPreAcquisition");
			}
			@Override
			public void onBeginDoAcquisition(ScheduleStep step) {
				System.out.println("onBeginDoAcquisition");
			}
			@Override
			public void onEndDoAcquisition(ScheduleStep step, AcquisitionSummary summary) {
				System.out.println("onEndDoAcquisition");
			}
			@Override
			public void onBeginPostAcquisition(ScheduleStep step) {
				System.out.println("onBeginPostAcquisition");
			}
			@Override
			public void onEndPostAcquisition(ScheduleStep step, Summary summary) {
				System.out.println("onEndPostAcquisition");
			}
		});
		
		Scheduler scheduler = createTableModel(shell, tblAcquisitions, loopHierarchy, walker, menu);
		
		updateSampleNodesEnabledDefault(
				provider.getSampleList(),
				provider.getConfigurationList(),
				scheduler);

		final IScheduleProvider scheduleProvider = new SyncScheduleProvider(
				shell,
				display,
				scheduler.createScheduleProvider());

		// buttons
		btnRun.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean busy = walker.isBusy() || provider.getCustomInstrumentAction().isBusy();
				
				if (busy) {
					MessageBox dialog = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
					dialog.setText("Information");
					dialog.setMessage("busy");
					dialog.open();
					return;
				}
				
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						walker.walk(scheduleProvider, InstrumentActionExecuter.getDefault());
					}
				});
				thread.start();
			}
		});
	}
	private static Scheduler createTableModel(final Shell shell, KTable table, LoopHierarchy loopHierarchy, ScheduleWalker walker, Menu menu) {
		// construction
		Scheduler scheduler = new Scheduler(
				loopHierarchy,
				
				// ConfigurationList
				new AcquisitionAspect(
						ConfigurationList.class.getSimpleName(),
						new IDependencyProperty[] { ConfigurationList.DESCRIPTION },
						// Configuration
						new AcquisitionEntry(
								Configuration.class.getSimpleName(),
								new IDependencyProperty[] { Configuration.INDEX, Configuration.ENABLED, Configuration.NAME, Configuration.DESCRIPTION },
								// Transmission
								new AcquisitionEntry(
										Measurement.TRANSMISSION,
										Measurement.class.getSimpleName(),	// Modification / Configuration / Measurement
										new IDependencyProperty[] { Measurement.INDEX, Measurement.ENABLED, Measurement.NAME, Measurement.DESCRIPTION, Measurement.MIN_TIME, Measurement.MAX_TIME, Measurement.TARGET_MONITOR_COUNTS, Measurement.TARGET_DETECTOR_COUNTS, Measurement.MIN_TIME_ENABLED, Measurement.MAX_TIME_ENABLED, Measurement.TARGET_MONITOR_COUNTS_ENABLED, Measurement.TARGET_DETECTOR_COUNTS_ENABLED }),
								// Scattering
								new AcquisitionEntry(
										Measurement.SCATTERING,
										Measurement.class.getSimpleName(),	// Modification / Configuration / Measurement
										new IDependencyProperty[] { Measurement.INDEX, Measurement.ENABLED, Measurement.NAME, Measurement.DESCRIPTION, Measurement.MIN_TIME, Measurement.MAX_TIME, Measurement.TARGET_MONITOR_COUNTS, Measurement.TARGET_DETECTOR_COUNTS, Measurement.MIN_TIME_ENABLED, Measurement.MAX_TIME_ENABLED, Measurement.TARGET_MONITOR_COUNTS_ENABLED, Measurement.TARGET_DETECTOR_COUNTS_ENABLED }))),
										
				// SampleList
				new AcquisitionAspect(
						SampleList.class.getSimpleName(),
						new IDependencyProperty[] { SampleList.DESCRIPTION },
						// Sample
						new AcquisitionEntry(
								Sample.class.getSimpleName(),
								new IDependencyProperty[] { Sample.INDEX, Sample.ENABLED, Sample.NAME, Sample.DESCRIPTION })),
								
				// Environment
				new AcquisitionAspect(
						Environment.class.getSimpleName(),
						new IDependencyProperty[] { Environment.NAME, Environment.DESCRIPTION },
						// SetPoint
						new AcquisitionEntry(
								SetPoint.class.getSimpleName(),
								new IDependencyProperty[] { SetPoint.INDEX, SetPoint.ENABLED, SetPoint.VALUE, SetPoint.WAIT_PERIOD })));

		// cell rendering
		DefaultCellRenderer indexRenderer = new TextCellRenderer(TextCellRenderer.INDICATION_FOCUS);
    	DefaultCellRenderer nameRenderer = new NameCellRenderer(TextCellRenderer.INDICATION_FOCUS | TextCellRenderer.INDICATION_COPYABLE);
    	DefaultCellRenderer numberRenderer = new NameCellRenderer(TextCellRenderer.INDICATION_FOCUS | TextCellRenderer.INDICATION_COPYABLE);
    	DefaultCellRenderer positionRenderer = new NameCellRenderer(TextCellRenderer.INDICATION_FOCUS);
    	DefaultCellRenderer checkableRenderer = new CheckableCellRenderer(CheckableCellRenderer.INDICATION_FOCUS | TextCellRenderer.INDICATION_COPYABLE);

    	indexRenderer.setAlignment(SWTX.ALIGN_HORIZONTAL_RIGHT | SWTX.ALIGN_VERTICAL_CENTER);
    	numberRenderer.setAlignment(SWTX.ALIGN_HORIZONTAL_RIGHT | SWTX.ALIGN_VERTICAL_CENTER);
    	positionRenderer.setAlignment(SWTX.ALIGN_HORIZONTAL_RIGHT | SWTX.ALIGN_VERTICAL_CENTER);
    	
    	// cell editing
    	KTableCellEditor textEditor = new KTableCellEditorText2();
    	KTableCellEditor numberEditor = new KTableCellEditorText2(SWT.RIGHT);
    	KTableCellEditor checkableEditor = new KTableCellEditorCheckbox();
		
    	// button handlers
    	IButtonListener<ScheduledNode> resetButtonListener = new IButtonListener<ScheduledNode>() {
			@Override
			public void onClicked(int col, int row, ScheduledNode node) {
				MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
				dialog.setText("Question");
				dialog.setMessage("Would you also like to reset all sub nodes?");

				switch (dialog.open()) {
				case SWT.YES:
					node.reset(true);
					break;
				case SWT.NO:
					node.reset(false);
					break;
				}
			}
		};
    	IButtonListener<ScheduledNode> duplicateButtonListener = new IButtonListener<ScheduledNode>() {
			@Override
			public void onClicked(int col, int row, ScheduledNode node) {
				node.duplicate();
			}
		};
    	IButtonListener<ScheduledNode> deleteButtonListener = new IButtonListener<ScheduledNode>() {
			@Override
			public void onClicked(int col, int row, ScheduledNode node) {
				MessageBox dialog = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
				dialog.setText("Warning");
				dialog.setMessage("Would you like to delete this node?");

				switch (dialog.open()) {
				case SWT.OK:
					node.delete();
					break;
				}
			}
		};

    	// buttons lists
		List<ButtonInfo<ScheduledNode>> elementListButtons = Arrays.asList(
    			new ButtonInfo<ScheduledNode>(Resources.IMAGE_NEW_GRAY, Resources.IMAGE_NEW, resetButtonListener));
		List<ButtonInfo<ScheduledNode>> listElementButtons = Arrays.asList(
    			new ButtonInfo<ScheduledNode>(Resources.IMAGE_NEW_GRAY, Resources.IMAGE_NEW, resetButtonListener),
    			new ButtonInfo<ScheduledNode>(Resources.IMAGE_COPY_SMALL_GRAY, Resources.IMAGE_COPY_SMALL, duplicateButtonListener),
    			new ButtonInfo<ScheduledNode>(Resources.IMAGE_MINUS_SMALL_GRAY, Resources.IMAGE_MINUS_SMALL, deleteButtonListener));

		// model
		IndexValueConverter indexValueConverter = new IndexValueConverter();
		
		ScheduleTableModel model = new ScheduleTableModel(
				table,
				scheduler,
				walker,
				menu,
		    	Arrays.asList(
		    			// Environment
		    			new RowDefinition(
		    					Environment.class,
		    					new RGB(176, 229, 124),
		    			    	null,
		    					elementListButtons,
		    					new PropertyCellDefinition(Environment.NAME, null, null, 13)),
		    			// SetPoint
		    			new RowDefinition(
		    					SetPoint.class,
		    					new RGB(176, 229, 124),
		    			    	"reset, duplicate or delete set-point",
		    					listElementButtons,
		    					new PropertyCellDefinition(SetPoint.INDEX, indexRenderer, numberEditor, indexValueConverter, 1),
		    					new FixedCellDefinition("Value:", 3),
		    					new PropertyCellDefinition(SetPoint.VALUE, numberRenderer, numberEditor, new PositionValueConverter(), 2),
		    					new FixedCellDefinition("Wait:", 3),
		    					new PropertyCellDefinition(SetPoint.WAIT_PERIOD, numberRenderer, numberEditor, new PositionValueConverter(), 2)),
		    			
		    			// ConfigurationList
		    			//new RowDefinition(
		    			//		ConfigurationList.class,
		    			//		new RGB(255, 174, 174),
		    			//		new FixedCellDefinition("ConfigurationList", 18)),
		    			// Configuration
						new RowDefinition(
		    					Configuration.class,
		    					new RGB(255, 174, 174),
		    					"Configuration",
		    					listElementButtons,
		    					new PropertyCellDefinition(Configuration.INDEX, indexRenderer, numberEditor, indexValueConverter, 1),
		    					new PropertyCellDefinition(Configuration.NAME, nameRenderer, textEditor, 16)),
		    			// Measurement
		    			new RowDefinition(
		    					Measurement.class,
		    					new RGB(255, 174, 255),
		    					"Measurement",
		    					listElementButtons,
		    					new PropertyCellDefinition(Measurement.INDEX, indexRenderer, numberEditor, indexValueConverter, 1),
		    					new PropertyCellDefinition(Measurement.NAME, nameRenderer, textEditor, 8),

		    					//new PropertyCellDefinition(Measurement.MIN_TIME_ENABLED, checkableRenderer, checkableEditor, 1),
		    					//new FixedCellDefinition("MinTime:", 4),
		    					//new PropertyCellDefinition(Measurement.MIN_TIME, numberRenderer, numberEditor, new TimeValueConverter(), 2),
		    					
		    					new PropertyCellDefinition(Measurement.MAX_TIME_ENABLED, checkableRenderer, checkableEditor, 1),
		    					new FixedCellDefinition("Time (sec):", 4),
		    					new PropertyCellDefinition(Measurement.MAX_TIME, numberRenderer, numberEditor, new TimeValueConverter(), 2)),

		    			// SampleList
		    			//new RowDefinition(
		    			//		SampleList.class,
		    			//		null,
		    			//		new FixedCellDefinition("SampleList", 14)),
		    			// Sample
		    			new RowDefinition(
		    					Sample.class,
		    					new RGB(145, 204, 255),
		    					"Sample",
		    					listElementButtons,
		    					new PropertyCellDefinition(Sample.INDEX, indexRenderer, numberEditor, indexValueConverter, 1),
		    					new PropertyCellDefinition(Sample.NAME, nameRenderer, textEditor, 7),
		    					new FixedCellDefinition("Position:", 3),
		    					new PropertyCellDefinition(Sample.POSITION, positionRenderer, null, new PositionValueConverter(), 2)

		    					//new PropertyCellDefinition(Sample.MAX_TIME_ENABLED, checkableRenderer, checkableEditor, 1),
		    					//new FixedCellDefinition("Time (sec):", 4),
		    					//new PropertyCellDefinition(Sample.MAX_TIME, numberRenderer, numberEditor, new TimeValueConverter(), 2)
		    					
		    					)));

		table.setModel(model);
		table.setNumRowsVisibleInPreferredSize(10);
		
		return scheduler;
	}

	// set enabled defaults for samples: "BLOCKED_BEAM", "EMPTY_BEAM", "EMPTY_CELL"
	private static void updateSampleNodesEnabledDefault(final SampleList sampleList, final ConfigurationList configurationList, final Scheduler scheduler) {
		sampleList.addListListener(new IElementListListener<Sample>() {
			@Override
			public void onAddedListElement(final Sample sample) {
				sample.addPropertyListener(new ElementPropertyListener(sample, sampleList, configurationList, scheduler));
			}
			@Override
			public void onDeletedListElement(Sample element) {
			}
		});
		
		scheduler.addListener(new SchedulerListener(sampleList, configurationList, scheduler));
	}
	private static boolean isOrderApplicable(SampleList sampleList, ConfigurationList configurationList, Scheduler scheduler) {
		List<Element> elements = scheduler.getElements();
		int sampleListIndex = elements.indexOf(sampleList);
		int configurationListIndex = elements.indexOf(configurationList);
		
		return
				(sampleListIndex != -1) &&
				(configurationListIndex != -1) &&
				(configurationListIndex < sampleListIndex);
	}
	
	private static class ElementPropertyListener implements IElementPropertyListener {
		// fields
		private final Sample sample;
		private final SampleList sampleList;
		private final ConfigurationList configurationList;
		private final Scheduler scheduler;
		
		// construction
		public ElementPropertyListener(Sample sample, SampleList sampleList, ConfigurationList configurationList, Scheduler scheduler) {
			this.sample = sample;
			this.sampleList = sampleList;
			this.configurationList = configurationList;
			this.scheduler = scheduler;
		}
		
		// methods
		@Override
		public void onChangedProperty(IDependencyProperty property, Object oldValue, Object newValue) {
			if (Sample.NAME != property)
				return;

			String measurementNameEnabled = null;
			if (Sample.BLOCKED_BEAM.equals(oldValue))
				measurementNameEnabled = Measurement.TRANSMISSION;
			else if (Sample.EMPTY_BEAM.equals(oldValue))
				measurementNameEnabled = Measurement.SCATTERING;
			
			String measurementNameDisabled = null;
			if (Sample.BLOCKED_BEAM.equals(newValue))
				measurementNameDisabled = Measurement.TRANSMISSION;
			else if (Sample.EMPTY_BEAM.equals(newValue))
				measurementNameDisabled = Measurement.SCATTERING;
			
			if ((measurementNameEnabled == null) && (measurementNameDisabled == null))
				return;
			
			if (!isOrderApplicable(sampleList, configurationList, scheduler))
				return;

			Set<ScheduledAspect> scheduledSampleLists = scheduler.getAspects(sampleList);
			if ((scheduledSampleLists == null) || scheduledSampleLists.isEmpty())
				return;
			
			Set<ScheduledAspect> scheduledConfigurationLists = scheduler.getAspects(configurationList);
			if ((scheduledConfigurationLists == null) || scheduledConfigurationLists.isEmpty())
				return;
			
			if (measurementNameEnabled != null)
				for (ScheduledAspect scheduledConfigurationList : scheduledConfigurationLists)
					for (ScheduledNode scheduledConfiguration : scheduledConfigurationList.getNode().getNodes())
						for (ScheduledNode scheduledMeasurement : scheduledConfiguration.getNodes())
							if (measurementNameEnabled.equals(scheduledMeasurement.getSourceElement().get(Measurement.NAME)))
								clearEnabledDefaults(scheduledConfigurationList.getLinkAt(scheduledMeasurement));

			if (measurementNameDisabled != null)
				for (ScheduledAspect scheduledConfigurationList : scheduledConfigurationLists)
					for (ScheduledNode scheduledConfiguration : scheduledConfigurationList.getNode().getNodes())
						for (ScheduledNode scheduledMeasurement : scheduledConfiguration.getNodes())
							if (measurementNameDisabled.equals(scheduledMeasurement.getSourceElement().get(Measurement.NAME)))
								setEnabledDefaults(scheduledConfigurationList.getLinkAt(scheduledMeasurement));
		}
		// helpers
		private void setEnabledDefaults(ScheduledAspect aspect) {
			ScheduledNode aspectNode = aspect.getNode();
			if (aspectNode.getSourceElement() != sampleList)
				for (ScheduledAspect link : aspect.getLinks())
					setEnabledDefaults(link);
			else
				for (ScheduledNode sampleNode : aspectNode.getNodes())
					if (sampleNode.getSourceElement() == sample)
						sampleNode.setDefault(Sample.ENABLED, false);
		}
		private void clearEnabledDefaults(ScheduledAspect aspect) {
			ScheduledNode aspectNode = aspect.getNode();
			if (aspectNode.getSourceElement() != sampleList)
				for (ScheduledAspect link : aspect.getLinks())
					clearEnabledDefaults(link);
			else
				for (ScheduledNode sampleNode : aspectNode.getNodes())
					if (sampleNode.getSourceElement() == sample)
						sampleNode.clearDefault(Sample.ENABLED);
		}
	}
	
	private static class SchedulerListener implements ISchedulerListener {
		// fields
		private final SampleList sampleList;
		private final ConfigurationList configurationList;
		private final Scheduler scheduler;
		
		// construction
		public SchedulerListener(SampleList sampleList, ConfigurationList configurationList, Scheduler scheduler) {
			this.sampleList = sampleList;
			this.configurationList = configurationList;
			this.scheduler = scheduler;
		}
		
		// methods
		@Override
		public void onNewRoot(ScheduledAspect root) {
		}
		@Override
		public void onNewLayer(Set<ScheduledAspect> owners) {
		}
		@Override
		public void onDeletedLayer(Set<ScheduledAspect> owners) {
		}
		@Override
		public void onAddedAspects(ScheduledAspect owner, Iterable<ScheduledAspect> aspects) {
			if (!isOrderApplicable(sampleList, configurationList, scheduler))
				return;
			
			// check if there are new scheduled samples
			List<Element> elements = scheduler.getElements();

			int sampleListIndex = elements.indexOf(sampleList);
			int ownerIndex = elements.indexOf(owner.getNode().getSourceElement());
			if ((sampleListIndex == -1) || (ownerIndex == -1))
				throw new Error("invalid arguments");
			
			if (sampleListIndex < ownerIndex)
				return;

			int configurationListIndex = elements.indexOf(configurationList);
			if (ownerIndex == configurationListIndex)
				updateConfigurationList(owner, aspects);
			else if (ownerIndex < configurationListIndex)
				findConfigurationList(aspects);
			else
				updateSampleList(owner, findMeasurementType(owner)); // [ConfigurationList] -> [OwnerAspect] -> [SampleList]
		}
		private String findMeasurementType(ScheduledAspect aspect) {
			ScheduledAspect parent = aspect.getParent();
			while (parent.getNode().getSourceElement() != configurationList) {
				aspect = parent;
				parent = parent.getParent();
			}
			
			ScheduledNode scheduledMeasurement = parent.getLeafNode(aspect);
			return String.valueOf(scheduledMeasurement.getSourceElement().get(Measurement.NAME));
		}
		private void findConfigurationList(Iterable<ScheduledAspect> aspects) {
			for (ScheduledAspect aspect : aspects)
				if (aspect.getNode().getSourceElement() == configurationList)
					updateConfigurationList(aspect, aspect.getLinks());
				else
					findConfigurationList(aspect.getLinks());
		}
		private void updateConfigurationList(ScheduledAspect scheduledConfigurationList, Iterable<ScheduledAspect> affectedAspects) {
			for (ScheduledNode scheduledConfiguration : scheduledConfigurationList.getNode().getNodes())
				for (ScheduledNode scheduledMeasurement : scheduledConfiguration.getNodes()) {
					String measurementType = String.valueOf(scheduledMeasurement.getSourceElement().get(Measurement.NAME));
					
					ScheduledAspect subAspect = scheduledConfigurationList.getLinkAt(scheduledMeasurement);
					for (ScheduledAspect affectedAspect : affectedAspects)
						if (subAspect == affectedAspect)
							updateSampleList(subAspect, measurementType);
				}
		}
		private void updateSampleList(ScheduledAspect aspect, String measurementType) {
			ScheduledNode aspectNode = aspect.getNode();
			if (aspectNode.getSourceElement() != sampleList)
				for (ScheduledAspect subAspect : aspect.getLinks())
					updateSampleList(subAspect, measurementType);
			else
				for (ScheduledNode sampleNode : aspectNode.getNodes())
					if (Sample.BLOCKED_BEAM.equals(sampleNode.getSourceElement().get(Sample.NAME))) {
						if (Measurement.TRANSMISSION.equals(measurementType))
							sampleNode.setDefault(Sample.ENABLED, false);
						else if (Measurement.SCATTERING.equals(measurementType))
							sampleNode.clearDefault(Sample.ENABLED);
					}
					else if (Sample.EMPTY_BEAM.equals(sampleNode.getSourceElement().get(Sample.NAME))) {
						if (Measurement.SCATTERING.equals(measurementType))
							sampleNode.setDefault(Sample.ENABLED, false);
						else if (Measurement.TRANSMISSION.equals(measurementType))
							sampleNode.clearDefault(Sample.ENABLED);
					}
		}
		
		@Override
		public void onDuplicatedAspects(ScheduledAspect owner, Iterable<ScheduledAspect> aspects) {
		}
		@Override
		public void onDeletedAspects(ScheduledAspect owner, Iterable<ScheduledAspect> aspects) {
		}
	}
}
