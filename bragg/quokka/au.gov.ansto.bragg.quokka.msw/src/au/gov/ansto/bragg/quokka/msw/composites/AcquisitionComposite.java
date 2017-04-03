package au.gov.ansto.bragg.quokka.msw.composites;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
import org.gumtree.msw.elements.IElementListener;
import org.gumtree.msw.schedule.AcquisitionAspect;
import org.gumtree.msw.schedule.AcquisitionEntry;
import org.gumtree.msw.schedule.ISchedulerListener;
import org.gumtree.msw.schedule.ScheduledAspect;
import org.gumtree.msw.schedule.ScheduledNode;
import org.gumtree.msw.schedule.Scheduler;
import org.gumtree.msw.schedule.execution.AcquisitionSummary;
import org.gumtree.msw.schedule.execution.IScheduleProvider;
import org.gumtree.msw.schedule.execution.IScheduleWalkerListener;
import org.gumtree.msw.schedule.execution.InitializationSummary;
import org.gumtree.msw.schedule.execution.ParameterChangeSummary;
import org.gumtree.msw.schedule.execution.ScheduleStep;
import org.gumtree.msw.schedule.execution.ScheduleWalker;
import org.gumtree.msw.schedule.execution.Summary;
import org.gumtree.msw.schedule.optimization.ScheduleOptimizer;
import org.gumtree.msw.ui.IModelBinding;
import org.gumtree.msw.ui.Resources;
import org.gumtree.msw.ui.ktable.ButtonInfo;
import org.gumtree.msw.ui.ktable.CheckableCellRenderer;
import org.gumtree.msw.ui.ktable.IButtonListener;
import org.gumtree.msw.ui.ktable.KTable;
import org.gumtree.msw.ui.ktable.KTableCellEditor;
import org.gumtree.msw.ui.ktable.NameCellRenderer;
import org.gumtree.msw.ui.ktable.SWTX;
import org.gumtree.msw.ui.ktable.ScheduleTableModel;
import org.gumtree.msw.ui.ktable.ScheduleTableModel.AcquisitionDetail;
import org.gumtree.msw.ui.ktable.ScheduleTableModel.FixedCellDefinition;
import org.gumtree.msw.ui.ktable.ScheduleTableModel.PropertyCellDefinition;
import org.gumtree.msw.ui.ktable.ScheduleTableModel.PropertyEnabledCondition;
import org.gumtree.msw.ui.ktable.ScheduleTableModel.RowDefinition;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorCheckbox;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorText2;
import org.gumtree.msw.ui.ktable.renderers.DefaultCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.TextCellRenderer;

import au.gov.ansto.bragg.quokka.msw.Configuration;
import au.gov.ansto.bragg.quokka.msw.ConfigurationList;
import au.gov.ansto.bragg.quokka.msw.Environment;
import au.gov.ansto.bragg.quokka.msw.IModelProviderListener;
import au.gov.ansto.bragg.quokka.msw.Measurement;
import au.gov.ansto.bragg.quokka.msw.ModelProvider;
import au.gov.ansto.bragg.quokka.msw.Sample;
import au.gov.ansto.bragg.quokka.msw.SampleList;
import au.gov.ansto.bragg.quokka.msw.SetPoint;
import au.gov.ansto.bragg.quokka.msw.converters.CountValueConverter;
import au.gov.ansto.bragg.quokka.msw.converters.IndexValueConverter;
import au.gov.ansto.bragg.quokka.msw.converters.StringTrimConverter;
import au.gov.ansto.bragg.quokka.msw.converters.TimeValueConverter;
import au.gov.ansto.bragg.quokka.msw.converters.TrimmedDoubleValueConverter;
import au.gov.ansto.bragg.quokka.msw.internal.QuokkaProperties;
import au.gov.ansto.bragg.quokka.msw.report.LogbookReportGenerator;
import au.gov.ansto.bragg.quokka.msw.report.LogbookReportGenerator.TableInfo;
import au.gov.ansto.bragg.quokka.msw.report.ReductionReportGenerator;
import au.gov.ansto.bragg.quokka.msw.report.ReportProvider;
import au.gov.ansto.bragg.quokka.msw.report.ReportProvider.EnvironmentReport;
import au.gov.ansto.bragg.quokka.msw.schedule.CustomInstrumentAction;
import au.gov.ansto.bragg.quokka.msw.schedule.InstrumentActionExecuter;
import au.gov.ansto.bragg.quokka.msw.schedule.SyncScheduleProvider;
import au.gov.ansto.bragg.quokka.msw.util.TertiaryShutter;

public class AcquisitionComposite extends Composite {
	// construction
	public AcquisitionComposite(Composite parent, final ModelProvider modelProvider) {
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
		
		KTable tblAcquisitions = new KTable(cmpTable, SWTX.EDIT_ON_KEY | SWT.V_SCROLL | SWT.H_SCROLL);
		tblAcquisitions.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, true, 1, 1));
		tblAcquisitions.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));

		Composite cmpBottom = new Composite(cmpContent, SWT.NONE);
		cmpBottom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout gl_cmpBottom = new GridLayout(8, false);
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
		new Label(cmpBottom, SWT.NONE);
		
		final Text txtConfigurationTime = new Text(cmpBottom, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
		GridData gd_txtConfigurationTime = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtConfigurationTime.widthHint = 100;
		txtConfigurationTime.setLayoutData(gd_txtConfigurationTime);
		txtConfigurationTime.setEnabled(false);
		Label lblPlus = new Label(cmpBottom, SWT.NONE);
		lblPlus.setText("+");
		final Text txtAcquisitionTime = new Text(cmpBottom, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
		GridData gd_txtAcquisitionTime = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtAcquisitionTime.widthHint = 100;
		txtAcquisitionTime.setLayoutData(gd_txtAcquisitionTime);
		txtAcquisitionTime.setEnabled(false);
		Label lblEqual = new Label(cmpBottom, SWT.NONE);
		lblEqual.setText("=");
		final Text txtTotalTime = new Text(cmpBottom, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
		GridData gd_txtTotalTime = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtTotalTime.widthHint = 100;
		txtTotalTime.setLayoutData(gd_txtTotalTime);
		txtTotalTime.setEnabled(false);
		
		// not needed if estimation is updated automatically
		Button btnRefresh = new Button(cmpBottom, SWT.NONE);
		GridData gd_btnUpdate = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnUpdate.widthHint = 90;
		gd_btnUpdate.heightHint = 23;
		btnRefresh.setLayoutData(gd_btnUpdate);
		btnRefresh.setImage(Resources.IMAGE_REFRESH);
		btnRefresh.setText("Refresh");
		btnRefresh.setVisible(false);
		
		Label lblSpace = new Label(cmpBottom, SWT.NONE);
		lblSpace.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		
		Button btnRun = new Button(cmpBottom, SWT.NONE);
		GridData gd_btnRun = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnRun.widthHint = 90;
		gd_btnRun.heightHint = 23;
		btnRun.setLayoutData(gd_btnRun);
		btnRun.setImage(Resources.IMAGE_PLAY);
		btnRun.setText("Run");

		//
	    final Scheduler scheduler = createScheduler();
	    final ScheduleOptimizer optimizer = new ScheduleOptimizer(
	    		scheduler,
	    		Environment.class,
				ConfigurationList.class,
				SampleList.class);
	    
		// menu
	    Menu menu = new Menu(this);
	    MenuItem menuItem;

	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Reset");
	    menuItem.setImage(Resources.IMAGE_NEW);
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				scheduler.reset();
			}
		});
	    
	    // enable/disable
	    new MenuItem(menu, SWT.SEPARATOR);
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Enable All");
	    menuItem.setImage(Resources.IMAGE_BOX_CHECKED);
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				scheduler.enableAll();
			}
		});
	    
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Disable All");
	    menuItem.setImage(Resources.IMAGE_BOX_UNCHECKED);
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				scheduler.disableAll();
			}
		});
	    
	    // optimize order
	    new MenuItem(menu, SWT.SEPARATOR);
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Optimize Order");
	    menuItem.setImage(Resources.IMAGE_OPTIMIZE);
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<Element> sortable = optimizer.analyze();
				if (sortable.isEmpty()) {
					MessageBox dialog = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
					dialog.setText("Information");
					dialog.setMessage("The order of the scheduled acquisitions is already optimal.");
					dialog.open();
					return;
				}
				else {
					OptimizationDialog dialog = new OptimizationDialog(getShell(), sortable);
					if (dialog.open() == Window.OK)
						optimizer.optimize(dialog.getSelected());
				}
			}
		});
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Reset Order");
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				optimizer.reset();
			}
		});
	    
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
	    
	    final ScheduleWalker walker = createScheduleWalker(modelProvider);
	    final ScheduleTableModel model = createTableModel(tblAcquisitions, walker, menu);
	    //final ScheduleChangeNotifier notifier = new ScheduleChangeNotifier();

	    model.updateSource(scheduler, walker);
	    //notifier.updateSource(scheduler);
	    
	    final File reportFolder = getReportLocation();

		ReportProvider reportProvider = new ReportProvider();
		reportProvider.bind(walker);
		reportProvider.addListener(new ReportProvider.IListener() {
			// fields
			private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
			private File intermediateReport = null;
			private File finalReport = null;
			private File finalHtml = null;
			
			// methods
			@Override
			public void onReset(EnvironmentReport rootReport) {
			    String intermediateName = "QKK_current_report";
			    String finalName = "QKK_" + format.format(Calendar.getInstance().getTime()) + "_report";
			    
			    intermediateReport =
			    		reportFolder == null ? null : new File(reportFolder, String.format("current/%s.xml", intermediateName));
			    
				finalReport =
						reportFolder == null ? null : new File(reportFolder, String.format("%s.xml", finalName));

				finalHtml =
						reportFolder == null ? null : new File(reportFolder, String.format("%s.html", finalName));
				
				ReductionReportGenerator.save(rootReport, intermediateReport);
			}
			@Override
			public void onUpdated(EnvironmentReport rootReport) {
				ReductionReportGenerator.save(rootReport, intermediateReport);
			}
			@Override
			public void onCompleted(EnvironmentReport rootReport) {
				try {
					try {
						ReductionReportGenerator.save(rootReport, intermediateReport);
					}
					finally {
						ReductionReportGenerator.save(rootReport, finalReport);
					}
				}
				finally {
					Iterable<TableInfo> tables = LogbookReportGenerator.create(rootReport);
					try {
						LogbookReportGenerator.save(tables, finalHtml);
					}
					finally {
						modelProvider.getCustomInstrumentAction().publishTables(tables);
					}
				}
			}
		});

		final Runnable estimationTrigger = new Runnable() {
			// methods
			@Override
			public void run() {
				if (txtConfigurationTime.isDisposed() || txtAcquisitionTime.isDisposed() || txtTotalTime.isDisposed())
					return; // stop if widgets are disposed
				
				try {
					TimeEstimator estimator = new TimeEstimator(scheduler);

					txtConfigurationTime.setText(toString(estimator.getConfigTime()));
					txtAcquisitionTime.setText(toString(estimator.getAcquisitionTime()));
					txtTotalTime.setText(toString(estimator.getTotalTime()));
					
					txtConfigurationTime.setEnabled(true);
					txtAcquisitionTime.setEnabled(true);
					txtTotalTime.setEnabled(true);
				}
				catch (Exception e) {
					e.printStackTrace();
					
					txtConfigurationTime.setText("");
					txtAcquisitionTime.setText("");
					txtTotalTime.setText("");
					
					txtConfigurationTime.setEnabled(false);
					txtAcquisitionTime.setEnabled(false);
					txtTotalTime.setEnabled(false);
				}

				getDisplay().timerExec(1000, this);
			}
			
			// helper
			private String toString(long t) {
				long s = t % 60;
				t /= 60;
				
				long m = t % 60;
				t /= 60;
				
				long h = t % 24;
				t /= 24;
				
				long d = t;
				
				if (d == 0)
					return String.format("%d:%02d:%02d", h, m, s);
				else
					return String.format("%d.%d:%02d:%02d", d, h, m, s);
			}
			
		};

		// only update time estimation after 100ms of last modification
		/*
		notifier.addListener(new ScheduleChangeNotifier.IListener() {
			@Override
			public void onScheduleChanged() {
				getDisplay().timerExec(100, estimationTrigger);
			}
		});
		*/
		getDisplay().timerExec(1000, estimationTrigger);
	    
		final IScheduleProvider scheduleProvider = new SyncScheduleProvider(
				getShell(),
				getDisplay(),
				scheduler.createScheduleProvider());
		
		btnRefresh.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TimeEstimator estimator = new TimeEstimator(scheduler);

				txtConfigurationTime.setText(toString(estimator.getConfigTime()));
				txtAcquisitionTime.setText(toString(estimator.getAcquisitionTime()));
				txtTotalTime.setText(toString(estimator.getTotalTime()));
				
				txtConfigurationTime.setEnabled(true);
				txtAcquisitionTime.setEnabled(true);
				txtTotalTime.setEnabled(true);
			}
			private String toString(long t) {
				long s = t % 60;
				t /= 60;
				
				long m = t % 60;
				t /= 60;
				
				long h = t % 24;
				t /= 24;
				
				long d = t;
				
				if (d == 0)
					return String.format("%d:%02d:%02d", h, m, s);
				else
					return String.format("%d.%d:%02d:%02d", d, h, m, s);
			}
		});
		btnRun.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean busy = walker.isBusy() || modelProvider.getCustomInstrumentAction().isBusy();
				
				if (busy) {
					MessageBox dialog = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
					dialog.setText("Information");
					dialog.setMessage("busy");
					dialog.open();
					return;
				}
				
				if (!scheduler.isOperatable()) {
					MessageBox dialog = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
					dialog.setText("Information");
					dialog.setMessage("The schedule table doesn't include any acquisitions.");
					dialog.open();
					return;
				}
				
				List<Element> sortable = optimizer.analyze();
				if (!sortable.isEmpty()) {
					MessageBox dialog = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.YES | SWT.NO | SWT.CANCEL);
					dialog.setText("Information");
					dialog.setMessage("Would you like to optimize the order of the scheduled acquisitions?");
					switch (dialog.open()) {
					case SWT.YES:
						OptimizationDialog dialog2 = new OptimizationDialog(getShell(), sortable);
						if (dialog2.open() == Window.OK) {
							optimizer.optimize(dialog2.getSelected());
							break;
						}
						else {
							return;
						}
						
					case SWT.NO:
						break;
						
					default:
						return;
					}
				}
				
				if (QuokkaProperties.checkTertiaryShutter()) {
					MessageBox dialog;
					switch (TertiaryShutter.acquireState()) {
					case OPEN:
						// ignore
						break;
						
					case CLOSED:
						dialog = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK | SWT.CANCEL);
						dialog.setText("Information");
						dialog.setMessage("Tertiary shutter is closed. Please open the shutter and press OK to continue.");
						if (dialog.open() != SWT.OK)
							return;
						break;
						
					default: // UNKNOWN
						dialog = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
						dialog.setText("Warning");
						dialog.setMessage("The state of the tertiary shutter is unknown. Please ensure that the shutter is open and press OK to continue.");
						if (dialog.open() != SWT.OK)
							return;
						break;
					}
				}

				final TimeEstimator estimator = new TimeEstimator(scheduler);
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						CustomInstrumentAction customAction = modelProvider.getCustomInstrumentAction();
						customAction.publishFinishTime(System.currentTimeMillis() / 1000 + estimator.getTotalTime());

						walker.walk(scheduleProvider, InstrumentActionExecuter.getDefault());
					}
				});
				thread.start();
			}
		});

	    modelProvider.addListener(new IModelProviderListener() {
	    	// fields
		    final List<IModelBinding> modelBindings = new ArrayList<>();
		    
		    // event handling
			@Override
			public void onReset() {
				// clear all previous bindings
				for (IModelBinding binding : modelBindings)
					binding.dispose();
				modelBindings.clear();
				
			    scheduler.updateSource(modelProvider.getLoopHierarchy());

			    modelBindings.add(
			    		updateSampleNodesEnabledDefault(
								modelProvider.getSampleList(),
								modelProvider.getConfigurationList(),
								scheduler));
			}
		});
	}
	private static File getReportLocation() {
		String propertyValue = null;
	    try {
	    	propertyValue = QuokkaProperties.getReportLocation();
	    	if (propertyValue != null) {
			    File target = new File(propertyValue);
			    if (target.isDirectory())
			    	return target;
	    	}
	    }
	    catch (Exception e) {
    		System.out.println(String.format(
    				"%s=%s",
    				"quokka.scan.report.location",
    				propertyValue));
	    	e.printStackTrace();
	    }

	    propertyValue = null;
	    try {
	    	propertyValue = System.getProperty("user.home");
			return new File(propertyValue + "/Desktop");
	    }
	    catch (Exception e) {
    		System.out.println(String.format(
    				"%s=%s",
    				"user.home",
    				propertyValue));
	    	e.printStackTrace();
	    }
	    
    	return null;
	}

	// methods
	private static Scheduler createScheduler() {
		// construction
		return new Scheduler(
		    	Arrays.asList(
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
											new IDependencyProperty[] { Measurement.INDEX, Measurement.ENABLED, Measurement.NAME, Measurement.DESCRIPTION, Measurement.MIN_TIME, Measurement.MAX_TIME, Measurement.TARGET_MONITOR_COUNTS, Measurement.TARGET_DETECTOR_COUNTS, Measurement.MIN_TIME_ENABLED, Measurement.MAX_TIME_ENABLED, Measurement.TARGET_MONITOR_COUNTS_ENABLED, Measurement.TARGET_DETECTOR_COUNTS_ENABLED }),
									// Scattering
									new AcquisitionEntry(
											Measurement.SCATTERING,
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
									new IDependencyProperty[] { SetPoint.INDEX, SetPoint.ENABLED, SetPoint.VALUE, SetPoint.WAIT_PERIOD }))),
									
				Arrays.<IDependencyProperty>asList(Measurement.MIN_TIME, Measurement.MAX_TIME, Measurement.TARGET_MONITOR_COUNTS, Measurement.TARGET_DETECTOR_COUNTS, Measurement.MIN_TIME_ENABLED, Measurement.MAX_TIME_ENABLED, Measurement.TARGET_MONITOR_COUNTS_ENABLED, Measurement.TARGET_DETECTOR_COUNTS_ENABLED));
	}
	private static ScheduleWalker createScheduleWalker(final ModelProvider modelProvider) {
		ScheduleWalker walker = new ScheduleWalker();
		walker.addListener(new IScheduleWalkerListener() {
			// schedule
			@Override
			public void onBeginSchedule() {
				modelProvider.getCustomInstrumentAction().setEnabled(false);

				//System.out.println("onBeginSchedule");
				//display.asyncExec(new Runnable() {
				//	@Override
				//	public void run() {
				//		MessageBox dialog = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
				//		dialog.setText("Information");
				//		dialog.setMessage("started");
				//		dialog.open();
				//	}
				//});
			}
			@Override
			public void onEndSchedule() {
				modelProvider.getCustomInstrumentAction().setEnabled(true);

				//System.out.println("onEndSchedule");
				//display.asyncExec(new Runnable() {
				//	@Override
				//	public void run() {
				//		MessageBox dialog = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
				//		dialog.setText("Information");
				//		dialog.setMessage("finished");
				//		dialog.open();
				//	}
				//});
			}
			// initialization
			@Override
			public void onInitialized(InitializationSummary summary) {
				// System.out.println("onInitialized");
			}
			@Override
			public void onCleanedUp(Summary summary) {
				// System.out.println("onCleanedUp");
			}
			// step
			@Override
			public void onBeginStep(ScheduleStep step) {
				// System.out.println("onBeginStep");
			}
			@Override
			public void onEndStep(ScheduleStep step) {
				// System.out.println("onEndStep");
			}
			// parameters
			@Override
			public void onBeginChangeParameter(ScheduleStep step) {
				// System.out.println("onBeginChangeParameter");
			}
			@Override
			public void onEndChangeParameters(ScheduleStep step, ParameterChangeSummary summary) {
				// System.out.println("onEndChangeParameters");
			}
			// acquisition
			@Override
			public void onBeginPreAcquisition(ScheduleStep step) {
				// System.out.println("onBeginPreAcquisition");
			}
			@Override
			public void onEndPreAcquisition(ScheduleStep step, Summary summary) {
				// System.out.println("onEndPreAcquisition");
			}
			@Override
			public void onBeginDoAcquisition(ScheduleStep step) {
				// System.out.println("onBeginDoAcquisition");
			}
			@Override
			public void onEndDoAcquisition(ScheduleStep step, AcquisitionSummary summary) {
				// System.out.println("onEndDoAcquisition");
			}
			@Override
			public void onBeginPostAcquisition(ScheduleStep step) {
				// System.out.println("onBeginPostAcquisition");
			}
			@Override
			public void onEndPostAcquisition(ScheduleStep step, Summary summary) {
				// System.out.println("onEndPostAcquisition");
			}
		});
		return walker;
	}
	private static ScheduleTableModel createTableModel(final KTable table, ScheduleWalker walker, Menu menu) {
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
		
    	// buttons
    	IButtonListener<ScheduledNode> resetButtonListener = new IButtonListener<ScheduledNode>() {
			@Override
			public void onClicked(int col, int row, ScheduledNode node) {
				MessageBox dialog = new MessageBox(table.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
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
				MessageBox dialog = new MessageBox(table.getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
				dialog.setText("Warning");
				dialog.setMessage("Would you like to delete this node?");

				switch (dialog.open()) {
				case SWT.YES:
					node.delete();
					break;
				}
			}
		};
    	IButtonListener<ScheduledNode> runButtonListener = new IButtonListener<ScheduledNode>() {
			@Override
			public void onClicked(int col, int row, ScheduledNode node) {
				/*
				boolean busy = walker.isBusy() || modelProvider.getCustomInstrumentAction().isBusy();
				
				if (busy) {
					MessageBox dialog = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
					dialog.setText("Information");
					dialog.setMessage("busy");
					dialog.open();
					return;
				}
				*/
				
				MessageBox dialog = new MessageBox(table.getShell(), SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
				dialog.setText("Question");
				dialog.setMessage("Would you like to start from this row?");

				switch (dialog.open()) {
				case SWT.OK:
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
		ButtonInfo<ScheduledNode> runButton =
				new ButtonInfo<ScheduledNode>(Resources.IMAGE_PLAY_SMALL_GRAY, Resources.IMAGE_PLAY_SMALL, runButtonListener);
		
		String elementListButtonsText = "reset";
		String listElementButtonsText = "reset, duplicate or delete";

		// model		
		ScheduleTableModel model = new ScheduleTableModel(
				table,
				menu,
				runButton,
		    	Arrays.asList(
		    			// Environment
		    			new RowDefinition(
		    					Environment.class,
		    					new RGB(176, 229, 124),
		    					elementListButtonsText,
		    					elementListButtons,
		    					new PropertyCellDefinition(Environment.NAME, null, null, -7)),
		    			// SetPoint
		    			new RowDefinition(
		    					SetPoint.class,
		    					new RGB(176, 229, 124),
		    					listElementButtonsText,
		    					listElementButtons,
		    					new PropertyCellDefinition(SetPoint.INDEX, indexRenderer, numberEditor, IndexValueConverter.DEFAULT, 1),
		    					new FixedCellDefinition("Value:", 3),
		    					new PropertyCellDefinition(SetPoint.VALUE, numberRenderer, numberEditor, TrimmedDoubleValueConverter.DEFAULT, 2),
		    					new FixedCellDefinition("Wait:", 3),
		    					new PropertyCellDefinition(SetPoint.WAIT_PERIOD, numberRenderer, numberEditor, TimeValueConverter.DEFAULT, 2)),
		    			
		    			// ConfigurationList
		    			//new RowDefinition(
		    			//		ConfigurationList.class,
		    			//		new RGB(255, 174, 174),
		    			//		new FixedCellDefinition("ConfigurationList", 18)),
		    			// Configuration
						new RowDefinition(
		    					Configuration.class,
		    					new RGB(255, 174, 174),
		    					listElementButtonsText,
		    					listElementButtons,
		    					new PropertyCellDefinition(Configuration.INDEX, indexRenderer, numberEditor, IndexValueConverter.DEFAULT, 1),
		    					new PropertyCellDefinition(Configuration.NAME, nameRenderer, textEditor, StringTrimConverter.DEFAULT, -7)),
		    			// Measurement
		    			new RowDefinition(
		    					Measurement.class,
		    					new RGB(255, 174, 255),
		    					listElementButtonsText,
		    					listElementButtons,
		    					new PropertyCellDefinition(Measurement.INDEX, indexRenderer, numberEditor, IndexValueConverter.DEFAULT, 1),
		    					new PropertyCellDefinition(Measurement.NAME, nameRenderer, textEditor, -7)),

		    			// SampleList
		    			//new RowDefinition(
		    			//		SampleList.class,
		    			//		null,
		    			//		new FixedCellDefinition("SampleList", 14)),
		    			// Sample
		    			new RowDefinition(
		    					Sample.class,
		    					new RGB(145, 204, 255),
		    					listElementButtonsText,
		    					listElementButtons,
		    					new PropertyCellDefinition(Sample.INDEX, indexRenderer, numberEditor, IndexValueConverter.DEFAULT, 1),
		    					new PropertyCellDefinition(Sample.NAME, nameRenderer, textEditor, -7),
		    					new FixedCellDefinition("Position:", 3),
		    					new PropertyCellDefinition(Sample.POSITION, positionRenderer, null, TrimmedDoubleValueConverter.DEFAULT, 2))),
		    					
		    	Arrays.asList(
		    			new AcquisitionDetail(
		    					"Min Time",
		    					new PropertyCellDefinition(Measurement.MIN_TIME_ENABLED, checkableRenderer, checkableEditor, 1),
		    					new PropertyCellDefinition(Measurement.MIN_TIME, numberRenderer, numberEditor, TimeValueConverter.DEFAULT, 3,
		    							new PropertyEnabledCondition(Measurement.MIN_TIME_ENABLED, true))),
		    			new AcquisitionDetail(
		    					"Monitor Cnts",
		    					new PropertyCellDefinition(Measurement.TARGET_MONITOR_COUNTS_ENABLED, checkableRenderer, checkableEditor, 1),
		    					new PropertyCellDefinition(Measurement.TARGET_MONITOR_COUNTS, numberRenderer, numberEditor, CountValueConverter.DEFAULT, 3,
		    							new PropertyEnabledCondition(Measurement.TARGET_MONITOR_COUNTS_ENABLED, true))),
		    			new AcquisitionDetail(
		    					"Detector Cnts",
		    					new PropertyCellDefinition(Measurement.TARGET_DETECTOR_COUNTS_ENABLED, checkableRenderer, checkableEditor, 1),
		    					new PropertyCellDefinition(Measurement.TARGET_DETECTOR_COUNTS, numberRenderer, numberEditor, CountValueConverter.DEFAULT, 3,
		    							new PropertyEnabledCondition(Measurement.TARGET_DETECTOR_COUNTS_ENABLED, true))),
		    			new AcquisitionDetail(
		    					"Max Time",
		    					new PropertyCellDefinition(Measurement.MAX_TIME_ENABLED, checkableRenderer, checkableEditor, 1),
		    					new PropertyCellDefinition(Measurement.MAX_TIME, numberRenderer, numberEditor, TimeValueConverter.DEFAULT, 3,
		    							new PropertyEnabledCondition(Measurement.MAX_TIME_ENABLED, true)))));

		table.setModel(model);
		table.setNumRowsVisibleInPreferredSize(10);
		
		return model;
	}

	// set enabled defaults for samples: "BLOCKED_BEAM", "EMPTY_BEAM", "EMPTY_CELL"
	private static IModelBinding updateSampleNodesEnabledDefault(final SampleList sampleList, final ConfigurationList configurationList, final Scheduler scheduler) {
		sampleList.addListListener(new IElementListListener<Sample>() {
			@Override
			public void onAddedListElement(final Sample sample) {
				sample.addElementListener(new ElementPropertyListener(sample, sampleList, configurationList, scheduler));
			}
			@Override
			public void onDeletedListElement(Sample element) {
			}
		});
		
		final ISchedulerListener schedulerListener = new SchedulerListener(sampleList, configurationList, scheduler);
		scheduler.addListener(schedulerListener);
		
		return new IModelBinding() {
			@Override
			public void dispose() {
				scheduler.removeListener(schedulerListener);
			}
		};
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
	
	private static class ElementPropertyListener implements IElementListener {
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
		@Override
		public void onDisposed() {
			// ignore
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
		@Override
		public void onBeginUpdate() {
		}
		@Override
		public void onEndUpdate() {
		}
	}
	
	private static class OptimizationDialog extends TitleAreaDialog {
		// fields
		private final Iterable<Element> sortable;
		private final Map<Button, Element> chks;
		private final Set<Element> selected;
		
		// construction
		public OptimizationDialog(Shell parentShell, Iterable<Element> sortable) {
			super(parentShell);
			
			this.sortable = sortable;
			this.chks = new HashMap<>();
			this.selected = new HashSet<>();
		}

		// properties
		public Set<Element> getSelected() {
			return selected;
		}
		@Override
		protected Point getInitialSize() {
			return new Point(350, 280);
		}
		@Override
		protected boolean isResizable() {
			return true;
		}
		
		// methods
		@Override
		public void create() {
			super.create();

			setTitle("Optimization");
			setMessage(
					"Select elements you would like to optimize:",
					IMessageProvider.INFORMATION);
		}
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite area = (Composite)super.createDialogArea(parent);

			Composite container = new Composite(area, SWT.NONE);
			container.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
			container.setLayout(new GridLayout(1, false));
			
			for (Element element : sortable) {
				Button chk = new Button(container, SWT.CHECK);
				chk.setSelection(true);

				if (element instanceof ConfigurationList)
					chk.setText("Configurations");
				else if (element instanceof SampleList)
					chk.setText("Samples");
				else if (element instanceof Environment)
					chk.setText(String.format("Environment (%s)", element.get(Environment.NAME)));
				else
					chk.setText(element.getPath().getElementName());
				
				chks.put(chk, element);
			}

			return area;
		}
		@Override
		protected void okPressed() {
			selected.clear();
			for (Entry<Button, Element> chk : chks.entrySet())
				if (chk.getKey().getSelection())
					selected.add(chk.getValue());

			super.okPressed();
		}
	}

	/*
	private static class ScheduleChangeNotifier {
		// fields
		private int suspendCounter = 0;
		private Scheduler scheduler;
		private NodeInfo rootNode;
		private final Map<ScheduledNode, NodeInfo> nodes;
		// source
		private final SchedulerListener schedulerListener;
		private final NodeListener nodeListener;
		// listeners
		private final List<IListener> listeners;
		
		// construction
		public ScheduleChangeNotifier() {
			rootNode = null;
			nodes = new HashMap<>();
			
			schedulerListener = new SchedulerListener();
			nodeListener = new NodeListener();
			
			listeners = new ArrayList<>();
		}
		
		// methods
		public void updateSource(Scheduler scheduler) {
			if (this.scheduler != null) {
				this.scheduler.removeListener(schedulerListener);
				this.scheduler = null;
			}

			// fast clear
			rootNode = null;
			for (ScheduledNode node : nodes.keySet())
				node.removeListener(nodeListener);
			nodes.clear();

			if (scheduler != null) {
				this.scheduler = scheduler;
				this.scheduler.addListener(schedulerListener);
			}
		}
		public void addListener(IListener listener) {
			if (listeners.contains(listener))
				throw new Error("listener already exists");
			
			listeners.add(listener);
		}
		@SuppressWarnings("unused")
		public boolean removeListener(IListener listener) {
			return listeners.remove(listener);
		}
		private void notifyScheduleChanged() {
			if (suspendCounter == 0)
				for (IListener listener : listeners)
					listener.onScheduleChanged();
		}
		
		// layers
	    private void onNewRoot(ScheduledAspect root) {
	    	boolean changed = false;
	    	suspendCounter++;
	    	try {
		    	if (rootNode != null) {
		    		removeNodeTree(null, rootNode);
		    		rootNode = null;
		    		changed = true;
		    	}
		    	
		    	if (!nodes.isEmpty())
		    		throw new IllegalStateException();
	    		
		    	if (root != null) {
		    		changed = true;
		    		rootNode = loadNodeTree(null, root.getNode());
		    	}
	    	}
	    	finally {
	    		suspendCounter--;
	    		if (changed)
	    			notifyScheduleChanged();
	    	}
		}
		private void onNewLayer(Set<ScheduledAspect> owners) {
	    	suspendCounter++;
	    	try {
				for (ScheduledAspect owner : owners)
					for (ScheduledNode leafNode : owner.getLeafNodes())
						insertAspect(leafNode, owner.getLinkAt(leafNode));
	    	}
	    	finally {
	    		suspendCounter--;
	    		notifyScheduleChanged();
	    	}
		}
		private void onDeletedLayer(Set<ScheduledAspect> owners) {
	    	suspendCounter++;
	    	try {
				for (ScheduledAspect owner : owners)
					for (ScheduledNode rootNode : owner.getLeafNodes()) {
						NodeInfo rootInfo = nodes.get(rootNode);
						if (rootInfo == null)
							throw new IllegalArgumentException("node not found");
						
						List<NodeInfo> nodeInfos = rootInfo.getNodes();
						if (nodeInfos.size() != 1)
							throw new IllegalArgumentException("link node should have exactly one child");
						
						NodeInfo oldNode = nodeInfos.get(0);
						
						ScheduledAspect leafAspect = owner.getLinkAt(rootNode);
						if (leafAspect != null) {
							// new leaf aspect was a follower of deleted aspect
							ScheduledNode leafNode = leafAspect.getNode();
							NodeInfo leafInfo = nodes.get(leafNode);
							if (leafInfo == null)
								throw new Error("node not found");
							
							// forward leaf node
							leafInfo.getOwner().removeSubNode(leafInfo);
							rootInfo.addSubNode(leafInfo);
						}
						
						// remove old tree
						removeNodeTree(rootInfo, oldNode);
					}
	    	}
	    	finally {
	    		suspendCounter--;
	    		notifyScheduleChanged();
	    	}
		}
		// aspects
		private void onNewAspects(ScheduledAspect owner, Iterable<ScheduledAspect> aspects) {
			if (owner == null)
				throw new IllegalArgumentException("new apsects can only be added to an existing aspect");
			
			NodeInfo ownerInfo = nodes.get(owner.getNode());
			if (ownerInfo == null)
				throw new IllegalArgumentException("node not found");

	    	suspendCounter++;
	    	try {
				// in case owner aspect hasn't been updated yet  
				loadNodeTree(ownerInfo.getOwner(), ownerInfo.getNode());
	
				for (ScheduledAspect aspect : aspects)
					insertAspect(owner.getLeafNode(aspect), aspect);
	    	}
	    	finally {
	    		suspendCounter--;
	    		notifyScheduleChanged();
	    	}
		}
		private void onDeletedAspects(ScheduledAspect owner, Iterable<ScheduledAspect> aspects) {
			if (owner == null)
				throw new IllegalArgumentException("apsects can only be removed from an existing aspect");

			NodeInfo ownerInfo = nodes.get(owner.getNode());
			if (ownerInfo == null)
				throw new IllegalArgumentException("node not found");

	    	suspendCounter++;
	    	try {
				for (ScheduledAspect aspect : aspects) {
					NodeInfo nodeInfo = nodes.get(aspect.getNode());
					if (nodeInfo == null)
						throw new IllegalArgumentException("node not found");
	
					removeNodeTree(nodeInfo.getOwner(), nodeInfo);
				}
	    	}
	    	finally {
	    		suspendCounter--;
	    		notifyScheduleChanged();
	    	}
		}
		private void onAddedSubNode(ScheduledNode owner, ScheduledNode newNode) {
			NodeInfo ownerInfo = nodes.get(owner);
			if (ownerInfo == null)
				throw new IllegalArgumentException("owner node not found");
			
			if (nodes.containsKey(newNode))
				return; // subNode may have been handled when new aspects were created

	    	suspendCounter++;
	    	try {
	    		loadNodeTree(ownerInfo, newNode);
	    	}
	    	finally {
	    		suspendCounter--;
	    		notifyScheduleChanged();
	    	}
		}
		private void onDeletedSubNode(ScheduledNode owner, ScheduledNode subNode) {
			NodeInfo ownerInfo = nodes.get(owner);
			if (ownerInfo == null)
				throw new IllegalArgumentException("owner not found");
			
			NodeInfo nodeInfo = nodes.get(subNode);
			if (nodeInfo == null)
				throw new IllegalArgumentException("subNode not found");

	    	suspendCounter++;
	    	try {
				// remove node and node ancestors
				removeNodeTree(ownerInfo, nodeInfo);
	    	}
	    	finally {
	    		suspendCounter--;
	    		notifyScheduleChanged();
	    	}
		}
		private void onChangedNodeProperty() {
			notifyScheduleChanged();
		}
		
		// helpers
		private NodeInfo insertAspect(ScheduledNode linkNode, ScheduledAspect aspect) {
			// load aspect and all its following aspects
			NodeInfo ownerInfo;
			if (linkNode == null)
				ownerInfo = null;
			else {
				ownerInfo = nodes.get(linkNode);
				if (ownerInfo == null)
					throw new IllegalArgumentException("node info not found");
				if (ownerInfo.getNodes().size() > 1)
					throw new IllegalArgumentException("link node should have one child at most");
			}

			ScheduledNode aspectNode = aspect.getNode();
			if (nodes.containsKey(aspectNode))
				throw new IllegalArgumentException("node already exists");
				
			NodeInfo aspectInfo = loadNodeTree(ownerInfo, aspectNode);
			
			// ensure that followers are created
			for (ScheduledNode leafNode : aspect.getLeafNodes()) {
				ScheduledAspect leafLink = aspect.getLinkAt(leafNode);
				if (leafLink != null) {
					ScheduledNode leafLinkNode = leafLink.getNode();
					NodeInfo leafLinkInfo = nodes.get(leafLinkNode);
					
					if (leafLinkInfo == null)
						leafLinkInfo = insertAspect(leafNode, leafLink);
					else {
						if (ownerInfo != null) // may be null if leafLink was previously root aspect
							ownerInfo.removeSubNode(leafLinkInfo);

						// link gets new owner 
						nodes.get(leafNode).addSubNode(leafLinkInfo);
					}
				}
			}
			
			return aspectInfo;
		}
		private NodeInfo loadNodeTree(NodeInfo ownerInfo, ScheduledNode node) {
			// load all nodes within aspect
			NodeInfo nodeInfo = nodes.get(node);
			
			if (nodeInfo == null) {
				nodeInfo = new NodeInfo(node);
				nodes.put(node, nodeInfo);

				if (ownerInfo != null)
					ownerInfo.addSubNode(nodeInfo);

				nodeListener.suspendAddedNotifications();
				try {
					node.addListener(nodeListener);
					if (!node.isAspectLeaf())
						for (ScheduledNode subNode : node.getNodes())
							loadNodeTree(nodeInfo, subNode);
				}
				finally {
					nodeListener.resumeAddedNotifications();
				}
			}
			else {
				if (nodeInfo.getOwner() != ownerInfo)
					throw new IllegalArgumentException("owner mismatch");

				nodeListener.suspendAddedNotifications();
				try {
					// don't add listener again
					// node.addListener(nodeListener);
					if (!node.isAspectLeaf())
						for (ScheduledNode subNode : node.getNodes())
							loadNodeTree(nodeInfo, subNode);
				}
				finally {
					nodeListener.resumeAddedNotifications();
				}
			}
			
			return nodeInfo;
		}
		private void removeNodeTree(NodeInfo ownerInfo, NodeInfo nodeInfo) {
			for (NodeInfo subInfo : nodeInfo.getNodes())
				removeNodeTree(null, subInfo); // null is passed to skip removal of sub-node
			
			if (ownerInfo != null)
				ownerInfo.removeSubNode(nodeInfo);
			
			ScheduledNode node = nodeInfo.getNode();
			node.removeListener(nodeListener);
			nodes.remove(node);
		}

		private class SchedulerListener implements ISchedulerListener {
			@Override
			public void onNewRoot(ScheduledAspect root) {
				ScheduleChangeNotifier.this.onNewRoot(root);
			}
			@Override
			public void onNewLayer(Set<ScheduledAspect> owners) {
				ScheduleChangeNotifier.this.onNewLayer(owners);
			}
			@Override
			public void onDeletedLayer(Set<ScheduledAspect> owners) {
				ScheduleChangeNotifier.this.onDeletedLayer(owners);
			}
			@Override
			public void onAddedAspects(ScheduledAspect owner, Iterable<ScheduledAspect> aspects) {
				ScheduleChangeNotifier.this.onNewAspects(owner, aspects);
			}
			@Override
			public void onDuplicatedAspects(ScheduledAspect owner, Iterable<ScheduledAspect> aspects) {
				ScheduleChangeNotifier.this.onNewAspects(owner, aspects);
			}
			@Override
			public void onDeletedAspects(ScheduledAspect owner, Iterable<ScheduledAspect> aspects) {
				ScheduleChangeNotifier.this.onDeletedAspects(owner, aspects);
			}
			@Override
			public void onBeginUpdate() {
			}
			@Override
			public void onEndUpdate() {
			}
		}

		private class NodeListener implements INodeListener {
			// fields
			private int suspendCounter = 0;
			
			// methods
			@Override
			public void onChangedProperty(ScheduledNode owner, IDependencyProperty property, Object oldValue, Object newValue) {
				ScheduleChangeNotifier.this.onChangedNodeProperty();
			}
			@Override
			public void onVisibilityChanged(ScheduledNode owner, boolean newValue) {
			}
			@Override
			public void onAddedSubNode(ScheduledNode owner, ScheduledNode subNode) {
				if (suspendCounter == 0)
					ScheduleChangeNotifier.this.onAddedSubNode(owner, subNode);
			}
			@Override
			public void onDuplicatedNode(ScheduledNode owner, ScheduledNode original, ScheduledNode duplicate) {
				ScheduleChangeNotifier.this.onAddedSubNode(owner, duplicate);
			}
			@Override
			public void onDeletedSubNode(ScheduledNode owner, ScheduledNode subNode) {
				ScheduleChangeNotifier.this.onDeletedSubNode(owner, subNode);
			}
			// locks
			@Override
			public void onPropertiesLocked() {
			}
			@Override
			public void onOrderLocked() {
			}
			@Override
			public void onUnlocked() {
			}		
			// helpers
			public void suspendAddedNotifications() {
				suspendCounter++;
			}
			public void resumeAddedNotifications() {
				suspendCounter--;
				if (suspendCounter < 0)
					throw new Error("invalid operation");
			}
		}
		
		private static class NodeInfo {
			// fields
			private NodeInfo owner;
			private final ScheduledNode node;
			private final List<NodeInfo> nodes;
			
			// construction
			public NodeInfo(ScheduledNode node) {
				this.owner = null;
				this.node = node;
				this.nodes = new ArrayList<>();
			}

			// properties
			public NodeInfo getOwner() {
				return owner;
			}
			public ScheduledNode getNode() {
				return node;
			}
			public List<NodeInfo> getNodes() {
				return nodes;
			}
			
			// methods
			public void addSubNode(NodeInfo subNode) {
				if (subNode.owner != null)
					throw new IllegalArgumentException("subNode.owner != null");
				
				if (nodes.contains(subNode))
					throw new IllegalArgumentException("nodes.contains(subNode)");

				subNode.owner = this;
			    nodes.add(subNode);
			}
			public void removeSubNode(NodeInfo subNode) {
				if (subNode.owner != this)
					throw new IllegalArgumentException("subNode.owner != this");

				if (!nodes.remove(subNode))
					throw new IllegalArgumentException("node not found");

				subNode.owner = null;
			}
		}
		
		private static interface IListener {
			// methods
			public void onScheduleChanged();
		}
	}
	*/
	private static class TimeEstimator {		
		// fields
		private final Element lastElement; // element of last aspect (ConfigurationList, SampleList or Environments)
		// seconds
		private long configTime;
		private long acquisitionTime;
		// helper
		private long lastAttenuationTime;
		
		// construction
		public TimeEstimator(Scheduler scheduler) {
			configTime = 0;
			acquisitionTime = 0;
			
			lastAttenuationTime = 0;
			
			List<Element> elements = scheduler.getElements();
			if (elements.isEmpty())
				lastElement = null;
			else {
				lastElement = elements.get(elements.size() - 1);
				analyze(scheduler.getRoot());
			}
		}
		
		// properties
		public long getConfigTime() {
			return configTime;
		}
		public long getAcquisitionTime() {
			return acquisitionTime;
		}
		public long getTotalTime() {
			return configTime + acquisitionTime;
		}

		// methods
		private void analyze(ScheduledAspect aspect) {
			if (aspect != null)
				analyze(aspect, aspect.getNode());
		}
		private void analyze(ScheduledAspect aspect, ScheduledNode node) {
			if (!node.isEnabled())
				return;
			
			Element element = node.getSourceElement();
			if (element instanceof Configuration) {
				// average of 20min for configuration change (5min voltage ramp x 2 + 15min 1/2 tank detector move)
				configTime += 25 * 60;

				// beamstop time (20sec up and down)
				configTime += 20 * 2;
			}
			else if (element instanceof Sample) {
				// moving sample holder
				configTime += 10;
			}
			else if (element instanceof Measurement) {
				Measurement measurement = (Measurement)element;
				switch (measurement.getAttenuationAlgorithm()) {
				case FIXED_ATTENUATION:
					lastAttenuationTime = 35;
					break;
				case ITERATIVE_ATTENUATION:
					// attenuation is in 30deg steps and we assume each step takes 35sec
					lastAttenuationTime = (measurement.getAttenuationAngle() / 30) * 35;
					break;
				case SMART_ATTENUATION:
					lastAttenuationTime = 120;
					break;
				}
			}
			
			// check if it's an acquisition node
			if (node.isAspectLeaf() && (aspect.getNode().getSourceElement() == lastElement)) {
				configTime += lastAttenuationTime;

				// acquisition time
				if ((Boolean)node.get(Measurement.TARGET_MONITOR_COUNTS_ENABLED)) {
					long monitorCounts = (Long)node.get(Measurement.TARGET_MONITOR_COUNTS);
					long expectedTime = monitorCounts / QuokkaProperties.getExpectedMonitorRate();
					
					if ((Boolean)node.get(Measurement.MAX_TIME_ENABLED)) {
						long maxTime = (Long)node.get(Measurement.MAX_TIME);
						if ((expectedTime > 0) && (expectedTime < maxTime))
							acquisitionTime += expectedTime;
						else
							acquisitionTime += maxTime;
					}
				}
				else if ((Boolean)node.get(Measurement.MAX_TIME_ENABLED)) {
					acquisitionTime += (Long)node.get(Measurement.MAX_TIME);
				}
			}
			
			if (node.isAspectLeaf())
				analyze(aspect.getLinkAt(node));
			else
				for (ScheduledNode subNode : node.getNodes())
					analyze(aspect, subNode);
		}
	}
}
