package au.gov.ansto.bragg.quokka.msw.composites;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
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
import org.gumtree.msw.ui.Resources;
import org.gumtree.msw.ui.ktable.ButtonInfo;
import org.gumtree.msw.ui.ktable.CheckableCellRenderer;
import org.gumtree.msw.ui.ktable.IButtonListener;
import org.gumtree.msw.ui.ktable.ElementTableModel;
import org.gumtree.msw.ui.ktable.ElementTableModel.ColumnDefinition;
import org.gumtree.msw.ui.ktable.NameCellRenderer;

import au.gov.ansto.bragg.quokka.msw.ExperimentDescription;
import au.gov.ansto.bragg.quokka.msw.ModelProvider;
import au.gov.ansto.bragg.quokka.msw.Sample;
import au.gov.ansto.bragg.quokka.msw.SampleList;
import au.gov.ansto.bragg.quokka.msw.converters.PositionValueConverter;
import au.gov.ansto.bragg.quokka.msw.converters.ThicknessValueConverter;
import au.gov.ansto.bragg.quokka.msw.schedule.CustomInstrumentAction;
import au.gov.ansto.bragg.quokka.msw.schedule.ICustomInstrumentActionListener;
import au.gov.ansto.bragg.quokka.msw.util.CsvTableExporter;
import au.gov.ansto.bragg.quokka.msw.util.CsvTableImporter;
import org.gumtree.msw.ui.ktable.KTable;
import org.gumtree.msw.ui.ktable.KTableCellEditor;
import org.gumtree.msw.ui.ktable.SWTX;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorCheckbox;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorComboText;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorText2;
import org.gumtree.msw.ui.ktable.renderers.DefaultCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.TextCellRenderer;

public class SamplesComposite extends Composite {
	// finals
	private static final String SAMPLE_STAGE = "SampleStage";
	
	// fields
	private final KTable tblSamples;
	private final Menu menu;
	private ExperimentDescription experimentDescription;
	private SampleList sampleList;

    // construction
	public SamplesComposite(Composite parent, final ModelProvider provider) {
		super(parent, SWT.BORDER);
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

		tblSamples = new KTable(cmpContent, SWTX.EDIT_ON_KEY | SWT.V_SCROLL | SWT.H_SCROLL);
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
				String sampleStage = experimentDescription.getSampleStage();
				if (!verifySampleStage(getShell(), sampleStage))
					return;
				
				CustomInstrumentAction customAction = provider.getCustomInstrumentAction();

				if (!customAction.driveToLoadPosition(sampleStage)) {
					MessageBox dialog = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
					dialog.setText("Information");
					dialog.setMessage("busy");
					dialog.open();
				}
			}
		});
		provider.getCustomInstrumentAction().addListener(new ICustomInstrumentActionListener() {
			@Override
			public void onActionFinished(String action, Summary summary) {
				final String message;
				if (action == CustomInstrumentAction.DRIVE_TO_LOAD_POSITION)
					message = "at load position";
				else if (action == CustomInstrumentAction.DRIVE_TO_SAMPLE_POSITION)
					message = "at sample position";
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
	    menu = new Menu(this);
	    MenuItem menuItem;

	    // sample holders
	    SelectionListener sampleHolderMenuListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object sampleStage = e.widget.getData(SAMPLE_STAGE);
				if (sampleStage instanceof String)
					sampleList.setSampleStage(experimentDescription, (String)sampleStage);
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
	    menuItem.setText("Remove All");
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sampleList.clear(experimentDescription);
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
				sampleList.enableAll();
			}
		});
	    
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Disable All");
	    menuItem.setImage(Resources.IMAGE_BOX_UNCHECKED);
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sampleList.disableAll();
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
				List<Map<IDependencyProperty, String>> content = CsvTableImporter.showDialog(
						getShell(),
						sampleList,
						Sample.ENABLED,
						Sample.POSITION,
						Sample.NAME,
						Sample.THICKNESS,
						Sample.DESCRIPTION);

				if (content != null)
					sampleList.replaceSamples(
							experimentDescription,
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
				CsvTableExporter.showDialog(
						getShell(),
						sampleList,
						Sample.ENABLED,
						Sample.POSITION,
						Sample.NAME,
						Sample.THICKNESS,
						Sample.DESCRIPTION);
			}
		});

	    initDataBindings(provider);
	}

	// methods
	private void initDataBindings(ModelProvider provider) {
		if (provider == null)
			return;
		
		experimentDescription = provider.getExperimentDescription();
		sampleList = provider.getSampleList();

		// setup table
		createTableModel(provider, tblSamples, experimentDescription, sampleList, menu);
	}
	private static ElementTableModel<SampleList, Sample> createTableModel(final ModelProvider provider, final KTable table, final ExperimentDescription experimentDescription, SampleList sampleList, Menu menu) {
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
				String sampleStage = experimentDescription.getSampleStage();
				if (!verifySampleStage(table.getShell(), sampleStage))
					return;
				
				CustomInstrumentAction customAction = provider.getCustomInstrumentAction();

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
    			sampleList,
    			menu,
				"drive sample holder to this position",
		    	Arrays.asList(
		    			new ButtonInfo<Sample>(Resources.IMAGE_BULLSEYE_GRAY, Resources.IMAGE_BULLSEYE, bullseyeButtonListener)),
		    	Arrays.asList(
		    			new ColumnDefinition(Sample.ENABLED, "", 30, checkableRenderer, checkableEditor),
		    			new ColumnDefinition(Sample.POSITION, "", 30, positionRenderer, null, new PositionValueConverter()),
		    			new ColumnDefinition(Sample.NAME, "Name", 250, nameRenderer, nameEditor),
		    			new ColumnDefinition(Sample.THICKNESS, "Thickness", 60, thicknessRenderer, numberEditor, new ThicknessValueConverter()),
		    			new ColumnDefinition(Sample.DESCRIPTION, "Description", 500, descriptionRenderer, textEditor)));
    	
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
