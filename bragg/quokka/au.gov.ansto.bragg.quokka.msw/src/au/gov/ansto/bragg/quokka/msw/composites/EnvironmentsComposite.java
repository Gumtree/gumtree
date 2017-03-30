package au.gov.ansto.bragg.quokka.msw.composites;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IElementListener;
import org.gumtree.msw.ui.IModelBinding;
import org.gumtree.msw.ui.IModelValueConverter;
import org.gumtree.msw.ui.ModelBinder;
import org.gumtree.msw.ui.Resources;
import org.gumtree.msw.ui.ktable.ButtonInfo;
import org.gumtree.msw.ui.ktable.CheckableCellRenderer;
import org.gumtree.msw.ui.ktable.ElementTableModel;
import org.gumtree.msw.ui.ktable.ElementTableModel.CellDefinition;
import org.gumtree.msw.ui.ktable.ElementTableModel.ColumnDefinition;
import org.gumtree.msw.ui.ktable.IButtonListener;
import org.gumtree.msw.ui.ktable.KTable;
import org.gumtree.msw.ui.ktable.KTableCellEditor;
import org.gumtree.msw.ui.ktable.NameCellRenderer;
import org.gumtree.msw.ui.ktable.SWTX;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorCheckbox;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorText2;
import org.gumtree.msw.ui.ktable.renderers.DefaultCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.TextCellRenderer;
import org.gumtree.msw.ui.observable.IProxyElementListener;
import org.gumtree.msw.ui.observable.ProxyElement;

import au.gov.ansto.bragg.quokka.msw.ConfigurationList;
import au.gov.ansto.bragg.quokka.msw.Environment;
import au.gov.ansto.bragg.quokka.msw.IModelProviderListener;
import au.gov.ansto.bragg.quokka.msw.LoopHierarchy;
import au.gov.ansto.bragg.quokka.msw.ModelProvider;
import au.gov.ansto.bragg.quokka.msw.SampleList;
import au.gov.ansto.bragg.quokka.msw.SetPoint;
import au.gov.ansto.bragg.quokka.msw.converters.DoubleValueConverter;
import au.gov.ansto.bragg.quokka.msw.converters.IndexValueConverter;
import au.gov.ansto.bragg.quokka.msw.converters.LongValueConverter;
import au.gov.ansto.bragg.quokka.msw.converters.StringTrimConverter;
import au.gov.ansto.bragg.quokka.msw.internal.QuokkaProperties;
import au.gov.ansto.bragg.quokka.msw.schedule.CustomInstrumentAction;
import au.gov.ansto.bragg.quokka.msw.util.CsvTable;
import au.gov.ansto.bragg.quokka.msw.util.ScriptCodeFont;

public class EnvironmentsComposite extends Composite {
	// finals
	private static final String[] EMPTY_STRING_ARRAY = {};
	
	// fields
	private final Map<String, String> environmentTemplates;
	private final ProxyElement<Environment> selectedEnvironment;
	private final ElementTableModel<LoopHierarchy, Element> elementsModel;
	private final ElementTableModel<Environment, SetPoint> setPointsModel;
	private Text txtName;
	private Text txtDescription;
	private Combo cmbTemplate;
	private Text txtInitializeScript;
	private Text txtDriveScript;
	private Button btnInitializeScriptApply;
	private Button btnInitializeScriptTestDrive;
	private Button btnDriveScriptApply;
	private Button btnDriveScriptTestDrive;
	private Button btnGenerate;
	private Text txtFrom;
	private Text txtTo;
	private Text txtSteps;
	private Text txtWait;
	
	// construction
	public EnvironmentsComposite(Composite parent, final ModelProvider modelProvider) {
		super(parent, SWT.BORDER);
		
		environmentTemplates = QuokkaProperties.getEnvironmentTemplates();
		
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 10;
		gridLayout.marginHeight = 10;
		gridLayout.horizontalSpacing = 0;
		setLayout(gridLayout);
		setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));

		Composite cmpContent = new Composite(this, SWT.NONE);
		cmpContent.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true, 1, 1));
		cmpContent.setLayout(new GridLayout(2, false));
		cmpContent.setBackground(getBackground());

		Composite cmpLeft = new Composite(cmpContent, SWT.NONE);
		cmpLeft.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		GridLayout gl_cmpLeft = new GridLayout(1, false);
		gl_cmpLeft.verticalSpacing = 0;
		gl_cmpLeft.horizontalSpacing = 0;
		gl_cmpLeft.marginWidth = 0;
		gl_cmpLeft.marginHeight = 0;
		cmpLeft.setLayout(gl_cmpLeft);
		cmpLeft.setBackground(getBackground());
		
		KTable tblEnvironments = new KTable(cmpLeft, SWTX.EDIT_ON_KEY | SWT.V_SCROLL | SWT.H_SCROLL);
		tblEnvironments.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		tblEnvironments.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));

		Composite cmpRight = new Composite(cmpContent, SWT.NONE);
		cmpRight.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		GridLayout gl_cmpRight = new GridLayout(1, true);
		gl_cmpRight.marginHeight = 0;
		gl_cmpRight.marginWidth = 0;
		cmpRight.setLayout(gl_cmpRight);
		cmpRight.setBackground(getBackground());

		Group grpEnvironment = new Group(cmpRight, SWT.NONE);
		grpEnvironment.setLayout(new GridLayout(1, false));
		grpEnvironment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		grpEnvironment.setText("Environment");

		Composite cmpEnvironment = new Composite(grpEnvironment, SWT.NONE);
		cmpEnvironment.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		GridLayout gl_cmpEnvironment = new GridLayout(4, false);
		gl_cmpEnvironment.marginWidth = 0;
		gl_cmpEnvironment.marginHeight = 0;
		cmpEnvironment.setLayout(gl_cmpEnvironment);
		cmpEnvironment.setBackground(getBackground());

		Label lblName = new Label(cmpEnvironment, SWT.NONE);
		lblName.setText("Name:");
		
		txtName = new Text(cmpEnvironment, SWT.BORDER);
		txtName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Label lblGroup = new Label(cmpEnvironment, SWT.NONE);
		lblGroup.setText("Template:");

		cmbTemplate = new Combo(cmpEnvironment, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		cmbTemplate.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		clearTemplateCombo();

		Label lblDescription = new Label(cmpEnvironment, SWT.NONE);
		lblDescription.setText("Description:");
		
		txtDescription = new Text(cmpEnvironment, SWT.BORDER);
		txtDescription.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		
		Group grpScripts = new Group(cmpRight, SWT.NONE);
		grpScripts.setLayout(new GridLayout(1, false));
		GridData gd_grpScripts = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_grpScripts.heightHint = 260;
		grpScripts.setLayoutData(gd_grpScripts);
		grpScripts.setText("Scripts");

		Composite cmpScripts = new Composite(grpScripts, SWT.NONE);
		cmpScripts.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		GridLayout gl_cmpScripts = new GridLayout(1, false);
		gl_cmpScripts.marginWidth = 0;
		gl_cmpScripts.marginHeight = 0;
		cmpScripts.setLayout(gl_cmpScripts);
		cmpScripts.setBackground(getBackground());

		TabFolder tabInitialize = new TabFolder(cmpScripts, SWT.NONE);
		tabInitialize.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TabItem tbtInitialize = new TabItem(tabInitialize, SWT.NONE);
		tbtInitialize.setText("Initialize");
		
		Composite tbtInitializeComposite = new Composite(tabInitialize, SWT.NONE);
		tbtInitialize.setControl(tbtInitializeComposite);
		GridLayout gl_tbtInitializeComposite = new GridLayout(2, false);
		gl_tbtInitializeComposite.verticalSpacing = 2;
		gl_tbtInitializeComposite.marginTop = 1;
		gl_tbtInitializeComposite.marginRight = 1;
		gl_tbtInitializeComposite.marginWidth = 0;
		gl_tbtInitializeComposite.marginHeight = 0;
		tbtInitializeComposite.setLayout(gl_tbtInitializeComposite);
		tbtInitializeComposite.setBackground(getBackground());

		txtInitializeScript = new Text(tbtInitializeComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		txtInitializeScript.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		txtInitializeScript.setFont(ScriptCodeFont.get());

		btnInitializeScriptApply = new Button(tbtInitializeComposite, SWT.NONE);
		GridData gd_btnInitializeScriptApply = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		gd_btnInitializeScriptApply.heightHint = 21;
		gd_btnInitializeScriptApply.widthHint = 90;
		btnInitializeScriptApply.setLayoutData(gd_btnInitializeScriptApply);
		btnInitializeScriptApply.setImage(Resources.IMAGE_TICK);
		btnInitializeScriptApply.setText("Apply");
		
		btnInitializeScriptTestDrive = new Button(tbtInitializeComposite, SWT.NONE);
		btnInitializeScriptTestDrive.setToolTipText("execute specified script");
		GridData gd_btnInitializeScriptTestDrive = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_btnInitializeScriptTestDrive.heightHint = 21;
		gd_btnInitializeScriptTestDrive.widthHint = 90;
		btnInitializeScriptTestDrive.setLayoutData(gd_btnInitializeScriptTestDrive);
		btnInitializeScriptTestDrive.setImage(Resources.IMAGE_PLAY);
		btnInitializeScriptTestDrive.setText("Test Drive");

		TabFolder tabDrive = new TabFolder(cmpScripts, SWT.NONE);
		tabDrive.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TabItem tbtDrive = new TabItem(tabDrive, SWT.NONE);
		tbtDrive.setText("Drive");
		
		Composite tbtDriveComposite = new Composite(tabDrive, SWT.NONE);
		tbtDrive.setControl(tbtDriveComposite);
		GridLayout gl_tbtDriveComposite = new GridLayout(2, false);
		gl_tbtDriveComposite.verticalSpacing = 2;
		gl_tbtDriveComposite.marginTop = 1;
		gl_tbtDriveComposite.marginRight = 1;
		gl_tbtDriveComposite.marginWidth = 0;
		gl_tbtDriveComposite.marginHeight = 0;
		tbtDriveComposite.setLayout(gl_tbtDriveComposite);
		tbtDriveComposite.setBackground(getBackground());

		txtDriveScript = new Text(tbtDriveComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		txtDriveScript.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		txtDriveScript.setFont(ScriptCodeFont.get());

		btnDriveScriptApply = new Button(tbtDriveComposite, SWT.NONE);
		GridData gd_btnDriveScriptApply = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		gd_btnDriveScriptApply.heightHint = 21;
		gd_btnDriveScriptApply.widthHint = 90;
		btnDriveScriptApply.setLayoutData(gd_btnDriveScriptApply);
		btnDriveScriptApply.setImage(Resources.IMAGE_TICK);
		btnDriveScriptApply.setText("Apply");
		
		btnDriveScriptTestDrive = new Button(tbtDriveComposite, SWT.NONE);
		btnDriveScriptTestDrive.setToolTipText("execute specified script");
		GridData gd_btnDriveScriptTestDrive = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_btnDriveScriptTestDrive.heightHint = 21;
		gd_btnDriveScriptTestDrive.widthHint = 90;
		btnDriveScriptTestDrive.setLayoutData(gd_btnDriveScriptTestDrive);
		btnDriveScriptTestDrive.setImage(Resources.IMAGE_PLAY);
		btnDriveScriptTestDrive.setText("Test Drive");

		Group grpValues = new Group(cmpRight, SWT.NONE);
		grpValues.setLayout(new GridLayout(1, false));
		grpValues.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		grpValues.setText("Values");

		Composite cmpValues = new Composite(grpValues, SWT.NONE);
		GridData gd_cmpValues = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_cmpValues.minimumHeight = 200;
		cmpValues.setLayoutData(gd_cmpValues);
		GridLayout gl_cmpValues = new GridLayout(1, false);
		gl_cmpValues.marginWidth = 0;
		gl_cmpValues.marginHeight = 0;
		cmpValues.setLayout(gl_cmpValues);
		cmpValues.setBackground(getBackground());

		KTable tblValues = new KTable(cmpValues, SWTX.EDIT_ON_KEY | SWT.V_SCROLL | SWT.H_SCROLL);
		tblValues.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		tblValues.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));

		Composite cmpGenerate = new Composite(cmpValues, SWT.NONE);
		cmpGenerate.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		GridLayout gl_cmpGenerate = new GridLayout(9, false);
		gl_cmpGenerate.marginWidth = 0;
		gl_cmpGenerate.marginHeight = 0;
		cmpGenerate.setLayout(gl_cmpGenerate);
		cmpGenerate.setBackground(getBackground());
		
		final int widthHint = 35;

		Label lblFrom = new Label(cmpGenerate, SWT.NONE);
		lblFrom.setText("From:");

		txtFrom = new Text(cmpGenerate, SWT.BORDER | SWT.RIGHT);
		GridData gd_txtFrom = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1);
		gd_txtFrom.widthHint = widthHint;
		txtFrom.setLayoutData(gd_txtFrom);
		txtFrom.setText("0");
		txtFrom.addModifyListener(new GenerateArgumentListener() {
			@Override
			public boolean valuate(String value) {
				Double.parseDouble(value);
				return true;
			}
		});

		Label lblTo = new Label(cmpGenerate, SWT.NONE);
		lblTo.setText("To:");

		txtTo = new Text(cmpGenerate, SWT.BORDER | SWT.RIGHT);
		GridData gd_txtTo = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1);
		gd_txtTo.widthHint = widthHint;
		txtTo.setLayoutData(gd_txtTo);
		txtTo.setText("10");
		txtTo.addModifyListener(new GenerateArgumentListener() {
			@Override
			public boolean valuate(String value) {
				Double.parseDouble(value);
				return true;
			}
		});

		Label lblSteps = new Label(cmpGenerate, SWT.NONE);
		lblSteps.setText("Steps:");

		txtSteps = new Text(cmpGenerate, SWT.BORDER | SWT.RIGHT);
		GridData gd_txtSteps = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1);
		gd_txtSteps.widthHint = widthHint;
		txtSteps.setLayoutData(gd_txtSteps);
		txtSteps.setText("11");
		txtSteps.addModifyListener(new GenerateArgumentListener() {
			@Override
			public boolean valuate(String value) {
				return Integer.parseInt(value) > 0;
			}
		});

		Label lblWait = new Label(cmpGenerate, SWT.NONE);
		lblWait.setText("Wait:");

		txtWait = new Text(cmpGenerate, SWT.BORDER | SWT.RIGHT);
		GridData gd_txtWait = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1);
		gd_txtWait.widthHint = widthHint;
		txtWait.setLayoutData(gd_txtWait);
		txtWait.setText("0");
		txtWait.addModifyListener(new GenerateArgumentListener() {
			@Override
			public boolean valuate(String value) {
				return Long.parseLong(value) >= 0;
			}
		});

		btnGenerate = new Button(cmpGenerate, SWT.NONE);
		btnGenerate.setToolTipText("append a generated list of values to the environment");
		GridData gd_btnGenerate = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_btnGenerate.heightHint = 21;
		gd_btnGenerate.widthHint = 90;
		btnGenerate.setLayoutData(gd_btnGenerate);
		btnGenerate.setImage(Resources.IMAGE_GENERATE);
		btnGenerate.setText("Generate");
		
		// menu

		selectedEnvironment = new ProxyElement<Environment>();
		
	    elementsModel = createElementsModel(tblEnvironments, createElementsMenu(modelProvider), modelProvider);
	    setPointsModel = createSetPointsModel(tblValues, createSetPointsMenu(), modelProvider);
	    
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
	}

	private Menu createElementsMenu(final ModelProvider modelProvider) {
		Menu menu = new Menu(this);
		MenuItem menuItem;

	    // add/remove
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Add New");
	    menuItem.setImage(Resources.IMAGE_PLUS);
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				modelProvider.getLoopHierarchy().addEnvironment();
			}
		});
	    
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Remove All");
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				modelProvider.getLoopHierarchy().removeAllEnvironments();
			}
		});
	    
	    // import/export
	    /*
	    new MenuItem(menu, SWT.SEPARATOR);
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("XML Import");
	    menuItem.setImage(Resources.IMAGE_IMPORT);
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);

				fileDialog.setFilterNames(new String[] { "Extensible Markup Language (*.xml)", "All Files (*.*)" });
				fileDialog.setFilterExtensions(new String[] { "*.xml", "*.*" });

				String filename = fileDialog.open();
				if ((filename != null) && (filename.length() > 0)) {
					boolean succeeded = false;
					try (InputStream stream = new FileInputStream(filename)) {
						//succeeded = modelProvider.getLoopHierarchy().replaceEnvironments(stream);
					}
					catch (Exception e2) {
					}
					if (!succeeded) {
						MessageBox dialog = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
						dialog.setText("Warning");
						dialog.setMessage("Unable to import from selected xml file.");
						dialog.open();
					}
				}
			}
		});
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("XML Export");
	    menuItem.setImage(Resources.IMAGE_EXPORT);
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);

				fileDialog.setFilterNames(new String[] { "Extensible Markup Language (*.xml)", "All Files (*.*)" });
				fileDialog.setFilterExtensions(new String[] { "*.xml", "*.*" });

				String filename = fileDialog.open();
				if ((filename != null) && (filename.length() > 0)) {
					boolean succeeded = false;
					try (OutputStream stream = new FileOutputStream(filename)) {
						//succeeded = modelProvider.getLoopHierarchy().saveEnvironmentsTo(stream);
					}
					catch (Exception e2) {
					}
					if (!succeeded) {
						MessageBox dialog = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
						dialog.setText("Warning");
						dialog.setMessage("Unable to export to selected xml file.");
						dialog.open();
					}
				}
			}
		});
	    */
	    return menu;
	}
	private Menu createSetPointsMenu() {
		Menu menu = new Menu(this);
		MenuItem menuItem;

	    // add/remove
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Add New");
	    menuItem.setImage(Resources.IMAGE_PLUS);
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (selectedEnvironment.hasTarget())
					selectedEnvironment.getTarget().addSetPoint();
			}
		});
	    
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Remove All");
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (selectedEnvironment.hasTarget())
					selectedEnvironment.getTarget().clear();
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
				if (selectedEnvironment.hasTarget())
					selectedEnvironment.getTarget().enableAll();
			}
		});
	    
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Disable All");
	    menuItem.setImage(Resources.IMAGE_BOX_UNCHECKED);
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (selectedEnvironment.hasTarget())
					selectedEnvironment.getTarget().disableAll();
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
				if (selectedEnvironment.hasTarget()) {
					Environment environment = selectedEnvironment.getTarget();
					
					List<Map<IDependencyProperty, Object>> content = CsvTable.showImportDialog(
							getShell(),
							SetPoint.ENABLED,
							SetPoint.VALUE,
							SetPoint.WAIT_PERIOD);

					if (content != null)
						environment.replaceSetPoints(content);
				}
			}
		});
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("CSV Export");
	    menuItem.setImage(Resources.IMAGE_EXPORT);
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (selectedEnvironment.hasTarget()) {
					Environment environment = selectedEnvironment.getTarget();
					
					CsvTable.showExportDialog(
							getShell(),
							environment,
							SetPoint.ENABLED,
							SetPoint.VALUE,
							SetPoint.WAIT_PERIOD);
				}
			}
		});

	    return menu;
	}
	
	// methods
	private void initDataBindings(final ModelProvider modelProvider, final DataBindingContext bindingContext, final List<IModelBinding> modelBindings) {
		// source
		LoopHierarchy loopHierarchy = modelProvider.getLoopHierarchy();

		// setup table
		elementsModel.updateSource(loopHierarchy);
		
		// selection
		final ProxyElement<Element> selectedElement = elementsModel.getSelectedElement();

		final IProxyElementListener<Element> selectedElementListener = new IProxyElementListener<Element>() {
			// methods
			@Override
			public void onTargetChange(Element oldTarget, Element newTarget) {
				if (newTarget instanceof Environment)
					selectedEnvironment.setTarget((Environment)newTarget);
				else
					selectedEnvironment.setTarget(null);
			}
		};
		final IProxyElementListener<Environment> selectedEnvironmentListener = new IProxyElementListener<Environment>() {
			@Override
			public void onTargetChange(Environment oldTarget, Environment newTarget) {
				setPointsModel.updateSource(newTarget);
			}
		};
		
		selectedElement.addListener(selectedElementListener);
		selectedEnvironment.addListener(selectedEnvironmentListener);
		modelBindings.add(new IModelBinding() {
			@Override
			public void dispose() {
				selectedElement.removeListener(selectedElementListener);
				selectedEnvironment.removeListener(selectedEnvironmentListener);
				
				selectedEnvironment.setTarget(null);
				setPointsModel.updateSource(null);
			}
		});
		
		// data binding
		modelBindings.add(
			ModelBinder.createTextBinding(
					bindingContext,
					txtName,
					selectedEnvironment,
					Environment.NAME,
					StringTrimConverter.DEFAULT));

		modelBindings.add(
			ModelBinder.createTextBinding(
					bindingContext,
					txtDescription,
					selectedEnvironment,
					Environment.DESCRIPTION,
					StringTrimConverter.DEFAULT));
		
		modelBindings.add(
			ModelBinder.createMultiLineBinding(
					bindingContext,
					txtInitializeScript,
					selectedEnvironment,
					Environment.SETUP_SCRIPT,
					StringTrimConverter.DEFAULT));

		modelBindings.add(
			ModelBinder.createMultiLineBinding(
					bindingContext,
					txtDriveScript,
					selectedEnvironment,
					Environment.DRIVE_SCRIPT,
					StringTrimConverter.DEFAULT));

		// Apply / Test Drive
		modelBindings.add(ModelBinder.createEnabledBinding(
				btnInitializeScriptApply,
				selectedEnvironment));
		modelBindings.add(ModelBinder.createEnabledBinding(
				btnInitializeScriptTestDrive,
				selectedEnvironment));
		modelBindings.add(ModelBinder.createEnabledBinding(
				btnDriveScriptApply,
				selectedEnvironment));
		modelBindings.add(ModelBinder.createEnabledBinding(
				btnDriveScriptTestDrive,
				selectedEnvironment));
		
		// generate
		modelBindings.add(ModelBinder.createEnabledBinding(
				btnGenerate,
				selectedEnvironment));
		
		
		// save button
		modelBindings.add(ModelBinder.createEnabledBinding(
				cmbTemplate,
				selectedEnvironment));
		
		// template
		final ModifyListener cmbTemplateListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String selection = cmbTemplate.getText();
				if (environmentTemplates.containsKey(selection)) {
					String setupScript = "";
					String driveScript = environmentTemplates.get(selection);
					if (
							!Objects.equals(setupScript, selectedEnvironment.get(Environment.SETUP_SCRIPT)) ||
							!Objects.equals(driveScript, selectedEnvironment.get(Environment.DRIVE_SCRIPT)))

						selectedEnvironment.getTarget().applyTemplate(setupScript, driveScript);
				}
			}
		};
		final IElementListener selectedEnvironmentTemplateListener = new IElementListener() {
			// fields
			private final Runnable updateCombo = new Runnable() {
				@Override
				public void run() {
					if (selectedEnvironment.hasTarget()) {
						Object setupScript = selectedEnvironment.get(Environment.SETUP_SCRIPT);
						Object driveScript = selectedEnvironment.get(Environment.DRIVE_SCRIPT);

						if (!Objects.equals("", setupScript) || !environmentTemplates.containsValue(driveScript)) {
							clearTemplateCombo();
						}
						else {
							for (Entry<String, String> entry : environmentTemplates.entrySet())
								if (Objects.equals(entry.getValue(), driveScript)) {
									if (!Objects.equals(cmbTemplate.getText(), entry.getKey()))
										cmbTemplate.setText(entry.getKey());
									break;
								}
						}
					}
				}
			};
			
			// methods
			@Override
			public void onChangedProperty(IDependencyProperty property, Object oldValue, Object newValue) {
				if ((property == Environment.SETUP_SCRIPT) || (property == Environment.DRIVE_SCRIPT))
					getDisplay().asyncExec(updateCombo);
			}
			@Override
			public void onDisposed() {
				// ignore
			}};
		

		cmbTemplate.addModifyListener(cmbTemplateListener);
		selectedEnvironment.addListener(selectedEnvironmentTemplateListener);
		modelBindings.add(new IModelBinding() {
			@Override
			public void dispose() {
				cmbTemplate.removeModifyListener(cmbTemplateListener);
				selectedEnvironment.removeListener(selectedEnvironmentTemplateListener);
			}
		});
		
		// test drive buttons

		final SelectionListener initializeScriptTestDrivelistener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Environment environment = selectedEnvironment.getTarget();
				if (environment == null)
					return;

				String script = environment.getSetupScript();
				CustomInstrumentAction customAction = modelProvider.getCustomInstrumentAction();
				
				if (!customAction.environmentSetup(script)) {
					MessageBox dialog = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
					dialog.setText("Information");
					dialog.setMessage("busy");
					dialog.open();
				}
			}
		};
		final SelectionListener driveScriptTestDrive = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Environment environment = selectedEnvironment.getTarget();
				if (environment == null)
					return;

				String script = environment.getDriveScript();

				EnvironmentDriveDialog dialog = new EnvironmentDriveDialog(getShell());
				if ((dialog.open() == Window.OK) && dialog.isValueValid()) {
					CustomInstrumentAction customAction = modelProvider.getCustomInstrumentAction();
					if (!customAction.environmentDrive(script, dialog.getValue())) {
						MessageBox msgBox = new MessageBox(getShell(), SWT.ICON_INFORMATION | SWT.OK);
						msgBox.setText("Information");
						msgBox.setMessage("busy");
						msgBox.open();
					}
				}
			}
		};
		
		btnInitializeScriptTestDrive.addSelectionListener(initializeScriptTestDrivelistener);
		btnDriveScriptTestDrive.addSelectionListener(driveScriptTestDrive);

		modelBindings.add(new IModelBinding() {
			@Override
			public void dispose() {
				btnInitializeScriptTestDrive.removeSelectionListener(initializeScriptTestDrivelistener);
				btnDriveScriptTestDrive.removeSelectionListener(driveScriptTestDrive);
			}
		});
		
		// generate
		
		final SelectionListener btnGenerateListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (selectedEnvironment.hasTarget()) {
					double from;
					double to;
					int steps;
					long wait;
					try {
						from = Double.parseDouble(txtFrom.getText());
						to = Double.parseDouble(txtTo.getText());
						steps = Integer.parseInt(txtSteps.getText());
						wait = Long.parseLong(txtWait.getText());
					}
					catch (Exception exc) {
						MessageBox dialog = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
						dialog.setText("Warning");
						dialog.setMessage("Please enter valid values for the to be generated set-points.");
						dialog.open();
						return;
					}

					selectedEnvironment.getTarget().generate(from, to, steps, wait);
				}
			}
		};

		btnGenerate.addSelectionListener(btnGenerateListener);
		modelBindings.add(new IModelBinding() {
			@Override
			public void dispose() {
				btnGenerate.removeSelectionListener(btnGenerateListener);
			}
		});
	}
	private static ElementTableModel<LoopHierarchy, Element> createElementsModel(final KTable table, Menu menu, final ModelProvider modelProvider) {
		// cell rendering
		DefaultCellRenderer indexRenderer = new TextCellRenderer(TextCellRenderer.INDICATION_FOCUS);
		DefaultCellRenderer nameRenderer = new NameCellRenderer(TextCellRenderer.INDICATION_FOCUS | TextCellRenderer.INDICATION_COPYABLE);
		DefaultCellRenderer listRenderer = new NameCellRenderer(TextCellRenderer.INDICATION_FOCUS);
		listRenderer.setForeground(SWTResourceManager.getColor(100, 100, 100));

    	indexRenderer.setAlignment(SWTX.ALIGN_HORIZONTAL_RIGHT | SWTX.ALIGN_VERTICAL_CENTER);

    	// cell editing
    	KTableCellEditor indexEditor = new KTableCellEditorText2(SWT.RIGHT);
    	KTableCellEditor nameEditor = new KTableCellEditorText2();

    	// buttons
    	IButtonListener<Element> addButtonListener = new IButtonListener<Element>() {
			@Override
			public void onClicked(int col, int row, Element element) {
				modelProvider.getLoopHierarchy().addEnvironment(element.getIndex());
			}
		};
    	IButtonListener<Element> duplicateButtonListener = new IButtonListener<Element>() {
			@Override
			public void onClicked(int col, int row, Element element) {
				if (element instanceof Environment)
					((Environment)element).duplicate();
			}
		};
    	IButtonListener<Element> deleteButtonListener = new IButtonListener<Element>() {
			@Override
			public void onClicked(int col, int row, Element element) {
				if (element instanceof Environment)
					((Environment)element).delete();
			}
		};
		
		// cell definitions (for name)
		CellDefinition nameCell = new CellDefinition(Environment.NAME, nameRenderer, nameEditor);
		CellDefinition configListCell = new CellDefinition(null, listRenderer, null, new FixedNameConverter("Configurations"));
		CellDefinition sampleListCell = new CellDefinition(null, listRenderer, null, new FixedNameConverter("Samples"));
		Map<Class<? extends Element>, CellDefinition> nameCellDefinitions = new HashMap<>();
		nameCellDefinitions.put(Environment.class, nameCell);
		nameCellDefinitions.put(ConfigurationList.class, configListCell);
		nameCellDefinitions.put(SampleList.class, sampleListCell);
    	
    	// construction
    	ElementTableModel<LoopHierarchy, Element> model = new ElementTableModel<LoopHierarchy, Element>(
    			table,
    			menu,
		    	"add, duplicate or delete environment",
		    	Arrays.asList(
		    			new ButtonInfo<Element>(Resources.IMAGE_PLUS_SMALL_GRAY, Resources.IMAGE_PLUS_SMALL, addButtonListener),
		    			new ButtonInfo<Element>(Resources.IMAGE_COPY_SMALL_GRAY, Resources.IMAGE_COPY_SMALL, duplicateButtonListener),
		    			new ButtonInfo<Element>(Resources.IMAGE_MINUS_SMALL_GRAY, Resources.IMAGE_MINUS_SMALL, deleteButtonListener)),
		    	Arrays.asList(
		    			new ColumnDefinition("", 30, Environment.INDEX, indexRenderer, indexEditor, IndexValueConverter.DEFAULT),
		    			new ColumnDefinition("Name", 200, nameCellDefinitions)));

    	table.setModel(model);
    	
    	return model;
	}
	private static ElementTableModel<Environment, SetPoint> createSetPointsModel(final KTable table, Menu menu, final ModelProvider modelProvider) {
		// cell rendering
		DefaultCellRenderer indexRenderer = new TextCellRenderer(TextCellRenderer.INDICATION_FOCUS);
		DefaultCellRenderer checkableRenderer = new CheckableCellRenderer(CheckableCellRenderer.INDICATION_FOCUS | TextCellRenderer.INDICATION_COPYABLE);
		DefaultCellRenderer valueRenderer = new TextCellRenderer(TextCellRenderer.INDICATION_FOCUS | TextCellRenderer.INDICATION_COPYABLE);

    	indexRenderer.setAlignment(SWTX.ALIGN_HORIZONTAL_RIGHT | SWTX.ALIGN_VERTICAL_CENTER);
    	valueRenderer.setAlignment(SWTX.ALIGN_HORIZONTAL_RIGHT | SWTX.ALIGN_VERTICAL_CENTER);

    	// cell editing
    	KTableCellEditor numberEditor = new KTableCellEditorText2(SWT.RIGHT);
    	KTableCellEditor checkableEditor = new KTableCellEditorCheckbox();

    	// buttons
    	IButtonListener<SetPoint> addButtonListener = new IButtonListener<SetPoint>() {
			@Override
			public void onClicked(int col, int row, SetPoint setPoint) {
				setPoint.add();
			}
		};
    	IButtonListener<SetPoint> duplicateButtonListener = new IButtonListener<SetPoint>() {
			@Override
			public void onClicked(int col, int row, SetPoint setPoint) {
				setPoint.duplicate();
			}
		};
    	IButtonListener<SetPoint> deleteButtonListener = new IButtonListener<SetPoint>() {
			@Override
			public void onClicked(int col, int row, SetPoint setPoint) {
				setPoint.delete();
			}
		};

    	// construction
    	ElementTableModel<Environment, SetPoint> model = new ElementTableModel<Environment, SetPoint>(
    			table,
    			menu,
		    	"add, duplicate or delete set-point",
		    	Arrays.asList(
		    			new ButtonInfo<SetPoint>(Resources.IMAGE_PLUS_SMALL_GRAY, Resources.IMAGE_PLUS_SMALL, addButtonListener),
		    			new ButtonInfo<SetPoint>(Resources.IMAGE_COPY_SMALL_GRAY, Resources.IMAGE_COPY_SMALL, duplicateButtonListener),
		    			new ButtonInfo<SetPoint>(Resources.IMAGE_MINUS_SMALL_GRAY, Resources.IMAGE_MINUS_SMALL, deleteButtonListener)),
		    	Arrays.asList(
		    			new ColumnDefinition("", 30, SetPoint.ENABLED, checkableRenderer, checkableEditor),
		    			new ColumnDefinition("", 30, SetPoint.INDEX, indexRenderer, numberEditor, IndexValueConverter.DEFAULT),
		    			new ColumnDefinition("Value", 80, SetPoint.VALUE, valueRenderer, numberEditor, DoubleValueConverter.DEFAULT),
		    			new ColumnDefinition("Wait", 80, SetPoint.WAIT_PERIOD, valueRenderer, numberEditor, LongValueConverter.DEFAULT)));

    	table.setModel(model);
    	
    	return model;
	}

	// helpers
	private void clearTemplateCombo() {
		String[] items = cmbTemplate.getItems();
		if ((items == null) || (items.length == 0)) {
			ArrayList<String> result = new ArrayList<>(environmentTemplates.keySet());
			Collections.sort(result);
			items = result.toArray(new String[result.size()]);
		}
		cmbTemplate.setItems(EMPTY_STRING_ARRAY);
		cmbTemplate.setText("");
		cmbTemplate.setItems(items);
	}

	private static class FixedNameConverter implements IModelValueConverter<Object, String> {
		// fields
		private final String name;
		
		// construction
		public FixedNameConverter(String name) {
			this.name = name;
		}
		
		// methods
		@Override
		public Class<Object> getModelValueType() {
			return Object.class;
		}
		@Override
		public Class<String> getTargetValueType() {
			return String.class;
		}
		@Override
		public String fromModelValue(Object value) {
			return name;
		}
		@Override
		public Object toModelValue(String value) {
			return null;
		}
	}
	
	private static abstract class GenerateArgumentListener implements ModifyListener {
		// methods
		@Override
		public void modifyText(ModifyEvent e) {
			Object source = e.getSource();
			if (source instanceof Text) {
				Text control = (Text)source;
				try {
					if (valuate(control.getText())) {
						control.setBackground(Resources.COLOR_DEFAULT);
						update(true);
						return;
					}
				}
				catch (Exception exc) {
				}
				control.setBackground(Resources.COLOR_ERROR);
				update(false);
			}
			
		}
		protected void update(boolean valid) {
		}
		protected abstract boolean valuate(String value);
	}
	
	private static class EnvironmentDriveDialog extends TitleAreaDialog {
		// fields
		private Text txtValue;
		private double value;
		private boolean valid;
		
		// construction
		public EnvironmentDriveDialog(Shell parentShell) {
			super(parentShell);
			this.valid = false;
		}

		// properties
		public boolean isValueValid() {
			return valid;
		}
		public double getValue() {
			return value;
		}
		@Override
		protected Point getInitialSize() {
			return new Point(380, 200);
		}
		@Override
		protected boolean isResizable() {
			return true;
		}
		
		// methods
		@Override
		public void create() {
			super.create();

			setTitle("Test Drive");
			setMessage(
					"Please specify the target value for this test drive:",
					IMessageProvider.INFORMATION);
		}
		@Override
		protected Control createDialogArea(Composite parent) {
			Composite area = (Composite)super.createDialogArea(parent);

			Composite container = new Composite(area, SWT.NONE);
			container.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
			container.setLayout(new GridLayout(1, false));

			txtValue = new Text(container, SWT.BORDER | SWT.RIGHT);
			GridData gd_txtValue = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
			gd_txtValue.widthHint = 100;
			txtValue.setLayoutData(gd_txtValue);
			txtValue.setText("0.0");
			txtValue.addModifyListener(new GenerateArgumentListener() {
				@Override
				public boolean valuate(String value) {
					Double.parseDouble(value);
					return true;
				}
				@Override
				protected void update(boolean valid) {
					Button ok = getButton(IDialogConstants.OK_ID);
					if (ok != null)
					  ok.setEnabled(valid);
				}
			});
			
			return area;
		}
		@Override
		protected void okPressed() {
			try {
				value = Double.parseDouble(txtValue.getText());
				valid = true;
			}
			catch (Exception e) {
			}
			super.okPressed();
		}
	}
}
