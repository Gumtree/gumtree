package au.gov.ansto.bragg.quokka.msw.composites;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.schedule.execution.Summary;
import org.gumtree.msw.ui.IModelBinding;
import org.gumtree.msw.ui.Resources;
import org.gumtree.msw.ui.ktable.ButtonInfo;
import org.gumtree.msw.ui.ktable.CheckableCellRenderer;
import org.gumtree.msw.ui.ktable.ElementTableModel;
import org.gumtree.msw.ui.ktable.ElementTableModel.ColumnDefinition;
import org.gumtree.msw.ui.ktable.IButtonListener;
import org.gumtree.msw.ui.ktable.KTable;
import org.gumtree.msw.ui.ktable.KTableCellEditor;
import org.gumtree.msw.ui.ktable.NameCellRenderer;
import org.gumtree.msw.ui.ktable.SWTX;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorCheckbox;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorComboText;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorText2;
import org.gumtree.msw.ui.ktable.renderers.DefaultCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.TextCellRenderer;

import au.gov.ansto.bragg.quokka.msw.ExperimentDescription;
import au.gov.ansto.bragg.quokka.msw.IModelProviderListener;
import au.gov.ansto.bragg.quokka.msw.ModelProvider;
import au.gov.ansto.bragg.quokka.msw.Sample;
import au.gov.ansto.bragg.quokka.msw.SampleList;
import au.gov.ansto.bragg.quokka.msw.converters.DoubleValueConverter;
import au.gov.ansto.bragg.quokka.msw.converters.TrimmedDoubleValueConverter;
import au.gov.ansto.bragg.quokka.msw.schedule.CustomInstrumentAction;
import au.gov.ansto.bragg.quokka.msw.schedule.ICustomInstrumentActionListener;
import au.gov.ansto.bragg.quokka.msw.util.CsvTable;
import au.gov.ansto.bragg.quokka.msw.util.LockStateManager;

import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.ISicsProxyListener;
import org.gumtree.gumnix.sics.io.SicsProxyListenerAdapter;

public class SamplesComposite extends Composite {
	// finals
	private static final String SAMPLE_STAGE = "SampleStage";
	
	// fields
	private final ElementTableModel<SampleList, Sample>  tableModel;
	private final AtomicBoolean requestSamplePositions;

    // construction
	public SamplesComposite(Composite parent, final ModelProvider modelProvider, final LockStateManager lockStateManager) {
		super(parent, SWT.BORDER);
		
		requestSamplePositions = new AtomicBoolean(true);
		
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 10;
		gridLayout.marginHeight = 10;
		gridLayout.horizontalSpacing = 0;
		setLayout(gridLayout);
		setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		
		Composite cmpContent = new Composite(this, SWT.NONE);
		cmpContent.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, true, 1, 1));
		cmpContent.setLayout(new GridLayout(1, false));
		cmpContent.setBackground(getBackground());

		KTable tblSamples = new KTable(cmpContent, SWTX.EDIT_ON_KEY | SWT.V_SCROLL | SWT.H_SCROLL);
		tblSamples.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		tblSamples.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
		
		Composite cmpBottom = new Composite(cmpContent, SWT.NONE);
		cmpBottom.setBackground(getBackground());
		
		Button btnDriveToLoad = new Button(cmpBottom, SWT.NONE);
		btnDriveToLoad.setBounds(0, 0, 150, 25);
		btnDriveToLoad.setImage(Resources.IMAGE_LOAD_POSITION);
		btnDriveToLoad.setText("Drive to load position");
		btnDriveToLoad.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String sampleStage = modelProvider.getExperimentDescription().getSampleStage();
				if (!verifySampleStage(getShell(), sampleStage))
					return;
				
				CustomInstrumentAction customAction = modelProvider.getCustomInstrumentAction();

				if (!customAction.driveToLoadPosition(sampleStage)) {
					MessageBox dialog = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
					dialog.setText("Information");
					dialog.setMessage("busy");
					dialog.open();
				}
			}
		});
		modelProvider.getCustomInstrumentAction().addListener(new ICustomInstrumentActionListener() {
			@Override
			public void onActionFinished(String action, Summary summary) {
				if (Objects.equals(action, CustomInstrumentAction.GET_SAMPLE_POSITIONS)) {
					Object positions = summary.getParameters().get("SamplePositions");
					if (positions instanceof Integer) {
						final String sampleStage = ExperimentDescription.getSampleStage((Integer)positions);
						if (sampleStage != null) {
							getDisplay().asyncExec(new Runnable() {
								@Override
								public void run() {
									if (!Objects.equals(sampleStage, modelProvider.getExperimentDescription().getSampleStage())) {
										MessageBox dialog = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
										dialog.setText("Information");
										dialog.setMessage(String.format(
												"%s%n%s%n%n%s",
												"Sics reported that the following sample stage is available:",
												sampleStage,
												"Would you like to acknowledge the adjustment?"));
										
										if (dialog.open() == SWT.YES)
											modelProvider.getSampleList().setSampleStage(modelProvider.getExperimentDescription(), sampleStage);
									}
								}
							});
						}
					}
					return;
				}

				final String message;
				if (Objects.equals(action, CustomInstrumentAction.DRIVE_TO_LOAD_POSITION))
					message = summary.getInterrupted() ? "command failed" : "at load position";
				else if (Objects.equals(action, CustomInstrumentAction.DRIVE_TO_SAMPLE_POSITION))
					message = summary.getInterrupted() ? "command failed" : "at sample position";
				else
					return;
				
				getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						MessageBox dialog = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
						dialog.setText("Information");
						dialog.setMessage(message);
						dialog.open();
					}
				});
			}
		});

		// menu
		Menu menu = new Menu(this);
	    MenuItem menuItem;

	    // sample holders
	    SelectionListener sampleHolderMenuListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object sampleStage = e.widget.getData(SAMPLE_STAGE);
				if (sampleStage instanceof String)
					modelProvider.getSampleList().setSampleStage(modelProvider.getExperimentDescription(), (String)sampleStage);
			}
		};
	    
		menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText(ExperimentDescription.LINEAR_20_POSITIONS);
	    menuItem.setData(SAMPLE_STAGE, ExperimentDescription.LINEAR_20_POSITIONS);
	    menuItem.addSelectionListener(sampleHolderMenuListener);
	    
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText(ExperimentDescription.LINEAR_12_POSITIONS);
	    menuItem.setData(SAMPLE_STAGE, ExperimentDescription.LINEAR_12_POSITIONS);
	    menuItem.addSelectionListener(sampleHolderMenuListener);
	    
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText(ExperimentDescription.LINEAR_10_POSITIONS);
	    menuItem.setData(SAMPLE_STAGE, ExperimentDescription.LINEAR_10_POSITIONS);
	    menuItem.addSelectionListener(sampleHolderMenuListener);
	    
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText(ExperimentDescription.ROTATING_5_POSITIONS);
	    menuItem.setData(SAMPLE_STAGE, ExperimentDescription.ROTATING_5_POSITIONS);
	    menuItem.addSelectionListener(sampleHolderMenuListener);
	    
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText(ExperimentDescription.RHEOMETER);
	    menuItem.setData(SAMPLE_STAGE, ExperimentDescription.RHEOMETER);
	    menuItem.addSelectionListener(sampleHolderMenuListener);
	    
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText(ExperimentDescription.FIXED_POSITION);
	    menuItem.setData(SAMPLE_STAGE, ExperimentDescription.FIXED_POSITION);
	    menuItem.addSelectionListener(sampleHolderMenuListener);
	    
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText(ExperimentDescription.MANUAL_POSITIONS);
	    menuItem.setData(SAMPLE_STAGE, ExperimentDescription.MANUAL_POSITIONS);
	    menuItem.setEnabled(false);
	    menuItem.addSelectionListener(sampleHolderMenuListener);

	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Clear All");
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				modelProvider.getSampleList().clear(modelProvider.getExperimentDescription());
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
				modelProvider.getSampleList().enableAll();
			}
		});
	    
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Disable All");
	    menuItem.setImage(Resources.IMAGE_BOX_UNCHECKED);
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				modelProvider.getSampleList().disableAll();
			}
		});
	    
	    // import/export
	    new MenuItem(menu, SWT.SEPARATOR);
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("CSV Import");
	    menuItem.setImage(Resources.IMAGE_IMPORT);
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<Map<IDependencyProperty, Object>> content = CsvTable.showImportDialog(
						getShell(),
						Sample.ENABLED,
						Sample.POSITION,
						Sample.NAME,
						Sample.THICKNESS,
						Sample.DESCRIPTION);

				if (content != null)
					modelProvider.getSampleList().replaceSamples(
							modelProvider.getExperimentDescription(),
							content,
							// persistent properties of existing samples (in case loaded list is too short)
							Sample.ENABLED,
							Sample.POSITION,
							Sample.NAME,
							Sample.THICKNESS,
							Sample.DESCRIPTION);
			}
		});
	    
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("CSV Export");
	    menuItem.setImage(Resources.IMAGE_EXPORT);
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CsvTable.showExportDialog(
						getShell(),
						modelProvider.getSampleList(),
						Sample.ENABLED,
						Sample.POSITION,
						Sample.NAME,
						Sample.THICKNESS,
						Sample.DESCRIPTION);
			}
		});

	    tableModel = createTableModel(tblSamples, menu, modelProvider);
	    
	    modelProvider.addListener(new IModelProviderListener() {
	    	// fields
		    final List<IModelBinding> modelBindings = new ArrayList<>();
		    final DataBindingContext bindingContext = new DataBindingContext();

		    // event handling
			@Override
			public void onReset() {
				// clear all previous bindings
				for (IModelBinding binding : modelBindings)
					binding.dispose();
				
				modelBindings.clear();
				
				initDataBindings(modelProvider, bindingContext, modelBindings);
			}
		});
	    
	    // request number of sample positions (once this composite becomes visible)
	    addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				if (requestSamplePositions.compareAndSet(true, false)) {
				    CustomInstrumentAction customAction = modelProvider.getCustomInstrumentAction();
				    customAction.requestSamplePositions();
				}
			}
		});
	}

	// methods
	private void initDataBindings(ModelProvider modelProvider, DataBindingContext bindingContext, List<IModelBinding> modelBindings) {
		// source
		SampleList sampleList = modelProvider.getSampleList();

		// setup table
		tableModel.updateSource(sampleList);
		
		// sics listener
		final ISicsProxyListener proxyListener = new SicsProxyListenerAdapter() {
			@Override
			public void proxyConnected() {
				requestSamplePositions.set(true);
			}
		};

		try {
			SicsCore.getSicsManager().proxy().addProxyListener(proxyListener);
			modelBindings.add(new IModelBinding() {
				@Override
				public void dispose() {
					SicsCore.getSicsManager().proxy().removeProxyListener(proxyListener);
				}
			});
		}
		catch (Exception e) {
			System.out.println("Failed to add listener to sics proxy for connect signal.");  
			e.printStackTrace();
		}
	}
	private static ElementTableModel<SampleList, Sample> createTableModel(final KTable table, Menu menu, final ModelProvider modelProvider) {
		// cell rendering
		DefaultCellRenderer positionRenderer = new TextCellRenderer(TextCellRenderer.INDICATION_FOCUS); // not copyable because the holder positions cannot be changed
    	DefaultCellRenderer checkableRenderer = new CheckableCellRenderer(CheckableCellRenderer.INDICATION_FOCUS | TextCellRenderer.INDICATION_COPYABLE);
    	DefaultCellRenderer nameRenderer = new NameCellRenderer(TextCellRenderer.INDICATION_FOCUS | TextCellRenderer.INDICATION_COPYABLE);
		DefaultCellRenderer thicknessRenderer = new TextCellRenderer(TextCellRenderer.INDICATION_FOCUS | TextCellRenderer.INDICATION_COPYABLE);
    	DefaultCellRenderer descriptionRenderer = new TextCellRenderer(TextCellRenderer.INDICATION_FOCUS | TextCellRenderer.INDICATION_COPYABLE); 

    	positionRenderer.setAlignment(SWTX.ALIGN_HORIZONTAL_RIGHT | SWTX.ALIGN_VERTICAL_CENTER);
    	thicknessRenderer.setAlignment(SWTX.ALIGN_HORIZONTAL_RIGHT | SWTX.ALIGN_VERTICAL_CENTER);
    	
    	// cell editing
    	KTableCellEditor numberEditor = new KTableCellEditorText2(SWT.RIGHT);
    	KTableCellEditor checkableEditor = new KTableCellEditorCheckbox();
    	KTableCellEditor nameEditor = new KTableCellEditorComboText(new String[] { Sample.BLOCKED_BEAM, Sample.EMPTY_BEAM, Sample.EMPTY_CELL });
    	KTableCellEditor textEditor = new KTableCellEditorText2();
    	
    	// buttons
    	IButtonListener<Sample> bullseyeButtonListener = new IButtonListener<Sample>() {
			@Override
			public void onClicked(int col, int row, Sample sample) {
				String sampleStage = modelProvider.getExperimentDescription().getSampleStage();
				if (!verifySampleStage(table.getShell(), sampleStage))
					return;
				
				CustomInstrumentAction customAction = modelProvider.getCustomInstrumentAction();

				if (!customAction.driveToSamplePosition(sampleStage, sample.getPosition())) {
					MessageBox dialog = new MessageBox(table.getShell(), SWT.ICON_INFORMATION | SWT.OK);
					dialog.setText("Information");
					dialog.setMessage("busy");
					dialog.open();
				}
			}
		};
    	
    	// construction
    	ElementTableModel<SampleList, Sample> model = new ElementTableModel<SampleList, Sample>(
    			table,
    			menu,
				"drive sample holder to this position",
		    	Arrays.asList(
		    			new ButtonInfo<Sample>(Resources.IMAGE_BULLSEYE_GRAY, Resources.IMAGE_BULLSEYE, bullseyeButtonListener)),
		    	Arrays.asList(
		    			new ColumnDefinition("", 30, Sample.ENABLED, checkableRenderer, checkableEditor),
		    			new ColumnDefinition("", 30, Sample.POSITION, positionRenderer, null, TrimmedDoubleValueConverter.DEFAULT),
		    			new ColumnDefinition("Name", 250, Sample.NAME, nameRenderer, nameEditor),
		    			new ColumnDefinition("Thickness", 60, Sample.THICKNESS, thicknessRenderer, numberEditor, DoubleValueConverter.DEFAULT),
		    			new ColumnDefinition("Description", 500, Sample.DESCRIPTION, descriptionRenderer, textEditor)));
    	    	
    	table.setModel(model);
    	table.setNumRowsVisibleInPreferredSize(20);
    	
    	return model;
	}
	private static boolean verifySampleStage(Shell shell, String sampleStage) {
		if (ExperimentDescription.FIXED_POSITION.equals(sampleStage)) {
			MessageBox dialog = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
			dialog.setText("Warning");
			dialog.setMessage("Sample Stage is currently fixed");
			dialog.open();
			return false;
		}
		
		return true;
	}
}
