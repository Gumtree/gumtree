package au.gov.ansto.bragg.quokka.msw.composites;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.wb.swt.SWTResourceManager;
import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.elements.IElementListListener;
import org.gumtree.msw.elements.IElementListener;
import org.gumtree.msw.ui.IModelBinding;
import org.gumtree.msw.ui.ModelBinder;
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
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorText2;
import org.gumtree.msw.ui.ktable.renderers.DefaultCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.TextCellRenderer;
import org.gumtree.msw.ui.observable.IProxyElementListener;
import org.gumtree.msw.ui.observable.ProxyElement;

import au.gov.ansto.bragg.quokka.msw.Configuration;
import au.gov.ansto.bragg.quokka.msw.ConfigurationList;
import au.gov.ansto.bragg.quokka.msw.IModelProviderListener;
import au.gov.ansto.bragg.quokka.msw.Measurement;
import au.gov.ansto.bragg.quokka.msw.ModelProvider;
import au.gov.ansto.bragg.quokka.msw.converters.AttenuationAngleConverter;
import au.gov.ansto.bragg.quokka.msw.converters.CountValueConverter;
import au.gov.ansto.bragg.quokka.msw.converters.GroupTrimConverter;
import au.gov.ansto.bragg.quokka.msw.converters.IndexValueConverter;
import au.gov.ansto.bragg.quokka.msw.converters.StringTrimConverter;
import au.gov.ansto.bragg.quokka.msw.converters.TimeValueConverter;
import au.gov.ansto.bragg.quokka.msw.schedule.CustomInstrumentAction;
import au.gov.ansto.bragg.quokka.msw.util.ConfigurationCatalogDialog;
import au.gov.ansto.bragg.quokka.msw.util.ScriptCodeFont;

public class ConfigurationsComposite extends Composite {
	// finals
	private static final Map<IDependencyProperty, Boolean> EXPAND_CONDITIONS;
	private static final String[] ATTENUATION_ANGLES = new String[] {"330°", "300°", "270°", "240°", "210°", "180°", "150°", "120°", "90°", "60°", "30°", "0°"};
	
	// fields
	private final ElementTableModel<ConfigurationList, Configuration> tableModel;
	private Text txtName;
	private Combo cmbGroup;
	private Button btnSave;
	private Text txtDescription;
	private Text txtInitializeScript;
	private Text txtPretransmissionScript;
	private Text txtPrescatteringScript;
	private Button btnInitializeScriptApply;
	private Button btnInitializeScriptTestDrive;
	private Button btnPretransmissionScriptApply;
	private Button btnPretransmissionScriptTestDrive;
	private Button btnPrescatteringScriptApply;
	private Button btnPrescatteringScriptTestDrive;

	private Composite cmpTransmission;
	private Combo cmbTransmissionAttAlgo;
	private Combo cmbTransmissionAttAngle;
	private Label lblTransmissionMaxTime;
	private Text txtTransmissionMaxTime;
	private Label lblTransmissionMaxTimeUnit;
	
	private Composite cmpScattering;
	private Combo cmbScatteringAttAlgo;
	private Combo cmbScatteringAttAngle;
	private Label lblScatteringMaxTime;
	private Text txtScatteringMaxTime;
	private Label lblScatteringMaxTimeUnit;
	// expanding (used for advanced users)
	private Composite cmpExpand;
	private Button btnExpand;
	private Label lblExpand;

	private Button chkTransmissionMinTime;
	private Button chkTransmissionMaxTime;
	private Button chkTransmissionMonitorCounts;
	private Button chkTransmissionDetectorCounts;
	private Text txtTransmissionMinTime;
	private Label lblTransmissionMinTimeUnit;
	private Text txtTransmissionMonitorCounts;
	private Text txtTransmissionDetectorCounts;

	private Button chkScatteringMinTime;
	private Button chkScatteringMaxTime;
	private Button chkScatteringMonitorCounts;
	private Button chkScatteringDetectorCounts;
	private Text txtScatteringMinTime;
	private Label lblScatteringMinTimeUnit;
	private Text txtScatteringMonitorCounts;
	private Text txtScatteringDetectorCounts;
		
	// construction
	static {
		// if any condition is fulfilled, show expanded UI 
		Map<IDependencyProperty, Boolean> map = new HashMap<>();
		map.put(Measurement.MIN_TIME_ENABLED, Boolean.TRUE);
		map.put(Measurement.MAX_TIME_ENABLED, Boolean.FALSE);
		map.put(Measurement.TARGET_MONITOR_COUNTS_ENABLED, Boolean.TRUE);
		map.put(Measurement.TARGET_DETECTOR_COUNTS_ENABLED, Boolean.TRUE);
		
		EXPAND_CONDITIONS = Collections.unmodifiableMap(map);
	}
	public ConfigurationsComposite(Composite parent, final ModelProvider modelProvider) {
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
		
		KTable tblConfigurations = new KTable(cmpLeft, SWTX.EDIT_ON_KEY | SWT.V_SCROLL | SWT.H_SCROLL);
		tblConfigurations.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		tblConfigurations.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));

		Composite cmpRight = new Composite(cmpContent, SWT.NONE);
		cmpRight.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		GridLayout gl_cmpRight = new GridLayout(2, true);
		gl_cmpRight.marginHeight = 0;
		gl_cmpRight.marginWidth = 0;
		cmpRight.setLayout(gl_cmpRight);
		cmpRight.setBackground(getBackground());
		
		Group grpConfiguration = new Group(cmpRight, SWT.NONE);
		grpConfiguration.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 2, 1));
		grpConfiguration.setLayout(new GridLayout(1, false));
		grpConfiguration.setText("Configuration");
		
		Composite cmpConfiguration = new Composite(grpConfiguration, SWT.NONE);
		cmpConfiguration.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		GridLayout gl_cmpConfiguration = new GridLayout(5, false);
		gl_cmpConfiguration.marginWidth = 0;
		gl_cmpConfiguration.marginHeight = 0;
		cmpConfiguration.setLayout(gl_cmpConfiguration);
		cmpConfiguration.setBackground(getBackground());
		
		Label lblName = new Label(cmpConfiguration, SWT.NONE);
		lblName.setText("Name:");

		txtName = new Text(cmpConfiguration, SWT.BORDER);
		txtName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Label lblGroup = new Label(cmpConfiguration, SWT.NONE);
		lblGroup.setText("Group:");

		cmbGroup = new Combo(cmpConfiguration, SWT.BORDER | SWT.DROP_DOWN);
		cmbGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		cmbGroup.setItems(listGroups());
		
		btnSave = new Button(cmpConfiguration, SWT.NONE);
		btnSave.setToolTipText("save this configuration in specified group");
		GridData gd_btnSave = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
		gd_btnSave.heightHint = 21;
		btnSave.setLayoutData(gd_btnSave);
		btnSave.setImage(Resources.IMAGE_DISK);
		btnSave.setText("Save");

		Label lblDescription = new Label(cmpConfiguration, SWT.NONE);
		lblDescription.setText("Description:");
		
		txtDescription = new Text(cmpConfiguration, SWT.BORDER);
		txtDescription.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		
		Group grpScripts = new Group(cmpRight, SWT.NONE);
		grpScripts.setLayout(new GridLayout(1, false));
		GridData gd_grpScripts = new GridData(SWT.FILL, SWT.FILL, false, true, 2, 1);
		gd_grpScripts.minimumHeight = 200;
		grpScripts.setLayoutData(gd_grpScripts);
		grpScripts.setText("Scripts");
		
		Composite cmpScripts = new Composite(grpScripts, SWT.NONE);
		cmpScripts.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		GridLayout gl_cmpScripts = new GridLayout(1, false);
		gl_cmpScripts.marginWidth = 0;
		gl_cmpScripts.marginHeight = 0;
		cmpScripts.setLayout(gl_cmpScripts);
		cmpScripts.setBackground(getBackground());

		TabFolder tabScripts = new TabFolder(cmpScripts, SWT.NONE);
		tabScripts.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TabItem tbtInitialize = new TabItem(tabScripts, SWT.NONE);
		tbtInitialize.setText("Initialize");
		
		Composite tbtInitializeComposite = new Composite(tabScripts, SWT.NONE);
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
		
		TabItem tbtPretransmission = new TabItem(tabScripts, SWT.NONE);
		tbtPretransmission.setText("Pre-Transmission");

		Composite tbtPretransmissionComposite = new Composite(tabScripts, SWT.NONE);
		tbtPretransmission.setControl(tbtPretransmissionComposite);
		GridLayout gl_tbtPretransmissionComposite = new GridLayout(2, false);
		gl_tbtPretransmissionComposite.verticalSpacing = 2;
		gl_tbtPretransmissionComposite.marginTop = 1;
		gl_tbtPretransmissionComposite.marginRight = 1;
		gl_tbtPretransmissionComposite.marginWidth = 0;
		gl_tbtPretransmissionComposite.marginHeight = 0;
		tbtPretransmissionComposite.setLayout(gl_tbtPretransmissionComposite);
		tbtPretransmissionComposite.setBackground(getBackground());
		
		txtPretransmissionScript = new Text(tbtPretransmissionComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		txtPretransmissionScript.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		txtPretransmissionScript.setFont(ScriptCodeFont.get());
		
		btnPretransmissionScriptApply = new Button(tbtPretransmissionComposite, SWT.NONE);
		GridData gd_btnPretransmissionScriptApply = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		gd_btnPretransmissionScriptApply.heightHint = 21;
		gd_btnPretransmissionScriptApply.widthHint = 90;
		btnPretransmissionScriptApply.setLayoutData(gd_btnPretransmissionScriptApply);
		btnPretransmissionScriptApply.setImage(Resources.IMAGE_TICK);
		btnPretransmissionScriptApply.setText("Apply");
		
		btnPretransmissionScriptTestDrive = new Button(tbtPretransmissionComposite, SWT.NONE);
		btnPretransmissionScriptTestDrive.setToolTipText("execute specified script");
		GridData gd_btnPretransmissionScriptTestDrive = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_btnPretransmissionScriptTestDrive.heightHint = 21;
		gd_btnPretransmissionScriptTestDrive.widthHint = 90;
		btnPretransmissionScriptTestDrive.setLayoutData(gd_btnPretransmissionScriptTestDrive);
		btnPretransmissionScriptTestDrive.setImage(Resources.IMAGE_PLAY);
		btnPretransmissionScriptTestDrive.setText("Test Drive");
	
		TabItem tbtPrescattering = new TabItem(tabScripts, SWT.NONE);
		tbtPrescattering.setText("Pre-Scattering");

		Composite tbtPrescatteringComposite = new Composite(tabScripts, SWT.NONE);
		tbtPrescattering.setControl(tbtPrescatteringComposite);
		GridLayout gl_tbtPrescatteringComposite = new GridLayout(2, false);
		gl_tbtPrescatteringComposite.verticalSpacing = 2;
		gl_tbtPrescatteringComposite.marginTop = 1;
		gl_tbtPrescatteringComposite.marginRight = 1;
		gl_tbtPrescatteringComposite.marginWidth = 0;
		gl_tbtPrescatteringComposite.marginHeight = 0;
		tbtPrescatteringComposite.setLayout(gl_tbtPrescatteringComposite);
		tbtPrescatteringComposite.setBackground(getBackground());
		
		txtPrescatteringScript = new Text(tbtPrescatteringComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		txtPrescatteringScript.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		txtPrescatteringScript.setFont(ScriptCodeFont.get());

		btnPrescatteringScriptApply = new Button(tbtPrescatteringComposite, SWT.NONE);
		GridData gd_btnPrescatteringScriptApply = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		gd_btnPrescatteringScriptApply.heightHint = 21;
		gd_btnPrescatteringScriptApply.widthHint = 90;
		btnPrescatteringScriptApply.setLayoutData(gd_btnPrescatteringScriptApply);
		btnPrescatteringScriptApply.setImage(Resources.IMAGE_TICK);
		btnPrescatteringScriptApply.setText("Apply");
		
		btnPrescatteringScriptTestDrive = new Button(tbtPrescatteringComposite, SWT.NONE);
		btnPrescatteringScriptTestDrive.setToolTipText("execute specified script");
		GridData gd_btnPrescatteringScriptTestDrive = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_btnPrescatteringScriptTestDrive.heightHint = 21;
		gd_btnPrescatteringScriptTestDrive.widthHint = 90;
		btnPrescatteringScriptTestDrive.setLayoutData(gd_btnPrescatteringScriptTestDrive);
		btnPrescatteringScriptTestDrive.setImage(Resources.IMAGE_PLAY);
		btnPrescatteringScriptTestDrive.setText("Test Drive");
		
		// transmission
		Group grpTransmission = new Group(cmpRight, SWT.NONE);
		grpTransmission.setLayout(new GridLayout(1, false));
		grpTransmission.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpTransmission.setText(Measurement.TRANSMISSION);

		cmpTransmission = new Composite(grpTransmission, SWT.NONE);
		cmpTransmission.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout gl_cmpTransmission = new GridLayout(3, false);
		gl_cmpTransmission.marginHeight = 0;
		gl_cmpTransmission.marginWidth = 0;
		cmpTransmission.setLayout(gl_cmpTransmission);
		cmpTransmission.setBackground(getBackground());

		cmbTransmissionAttAlgo = new Combo(cmpTransmission, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gd_cmbTransmissionAlgoType = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_cmbTransmissionAlgoType.widthHint = 110;
		cmbTransmissionAttAlgo.setLayoutData(gd_cmbTransmissionAlgoType);
		cmbTransmissionAttAlgo.setItems(new String[] {"fixed attenuation"});
		cmbTransmissionAttAlgo.setText("fixed attenuation");
		cmbTransmissionAttAngle = new Combo(cmpTransmission, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY | SWT.RIGHT);
		cmbTransmissionAttAngle.setItems(ATTENUATION_ANGLES);
		cmbTransmissionAttAngle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbTransmissionAttAngle.setText("150°");
		new Label(cmpTransmission, SWT.NONE);

		lblTransmissionMaxTime = new Label(cmpTransmission, SWT.NONE);
		GridData gd_lblTransmissionMaxTime = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblTransmissionMaxTime.horizontalIndent = 4;
		lblTransmissionMaxTime.setLayoutData(gd_lblTransmissionMaxTime);
		lblTransmissionMaxTime.setText("Acquisition Time:");
		txtTransmissionMaxTime = new Text(cmpTransmission, SWT.BORDER | SWT.RIGHT);
		txtTransmissionMaxTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblTransmissionMaxTimeUnit = new Label(cmpTransmission, SWT.NONE);
		lblTransmissionMaxTimeUnit.setText("sec");

		// scattering
		Group grpScattering = new Group(cmpRight, SWT.NONE);
		grpScattering.setLayout(new GridLayout(1, false));
		grpScattering.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpScattering.setText(Measurement.SCATTERING);

		cmpScattering = new Composite(grpScattering, SWT.NONE);
		cmpScattering.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout gl_cmpScattering = new GridLayout(3, false);
		gl_cmpScattering.marginWidth = 0;
		gl_cmpScattering.marginHeight = 0;
		cmpScattering.setLayout(gl_cmpScattering);
		cmpScattering.setBackground(getBackground());

		cmbScatteringAttAlgo = new Combo(cmpScattering, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gd_cmbScatteringAlgoType = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_cmbScatteringAlgoType.widthHint = 110;
		cmbScatteringAttAlgo.setLayoutData(gd_cmbScatteringAlgoType);
		cmbScatteringAttAlgo.setItems(new String[] {"fixed attenuation", "iterative attenuation", "smart attenuation"});
		cmbScatteringAttAlgo.setText("iterative attenuation");
		cmbScatteringAttAngle = new Combo(cmpScattering, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY | SWT.RIGHT);
		cmbScatteringAttAngle.setItems(ATTENUATION_ANGLES);
		cmbScatteringAttAngle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbScatteringAttAngle.setText("90°");
		new Label(cmpScattering, SWT.NONE);

		lblScatteringMaxTime = new Label(cmpScattering, SWT.NONE);
		GridData gd_lblScatteringMaxTime = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblScatteringMaxTime.horizontalIndent = 4;
		lblScatteringMaxTime.setLayoutData(gd_lblScatteringMaxTime);
		lblScatteringMaxTime.setText("Acquisition Time:");
		txtScatteringMaxTime = new Text(cmpScattering, SWT.BORDER | SWT.RIGHT);
		txtScatteringMaxTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblScatteringMaxTimeUnit = new Label(cmpScattering, SWT.NONE);
		lblScatteringMaxTimeUnit.setText("sec");
		
		// expand button
		cmpExpand = new Composite(cmpRight, SWT.NONE);
		cmpExpand.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, false, 2, 1));
		GridLayout gl_cmpExpand = new GridLayout(2, false);
		gl_cmpExpand.marginHeight = 0;
		cmpExpand.setLayout(gl_cmpExpand);
		cmpExpand.setBackground(getBackground());
		
		btnExpand = new Button(cmpExpand, SWT.NONE);
		GridData gd_btnScatteringExpand = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnScatteringExpand.heightHint = 15;
		gd_btnScatteringExpand.widthHint = 15;
		gd_btnScatteringExpand.verticalIndent = 2;
		btnExpand.setLayoutData(gd_btnScatteringExpand);
		btnExpand.setImage(Resources.IMAGE_DOWN);
		
		lblExpand = new Label(cmpExpand, SWT.NONE);
		GridData gd_lblScatteringExpand = new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1);
		gd_lblScatteringExpand.widthHint = 100;
		gd_lblScatteringExpand.horizontalIndent = -3;
		gd_lblScatteringExpand.verticalIndent = 2;
		lblExpand.setLayoutData(gd_lblScatteringExpand);
		lblExpand.setText("Advanced Options");


		// menu
		Menu menu = new Menu(this);
	    MenuItem menuItem;

	    // add new/saved
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Add New");
	    menuItem.setImage(Resources.IMAGE_PLUS);
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				modelProvider.getConfigurationList().addConfiguration();
			}
		});

	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Add Saved...");
	    menuItem.setImage(Resources.IMAGE_IMPORT_FILE);
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			@SuppressWarnings("static-access")
			public void widgetSelected(SelectionEvent e) {
				ConfigurationCatalogDialog dialog = new ConfigurationCatalogDialog(getShell());
				if (dialog.open() == Window.OK)
					modelProvider.getConfigurationList().addConfigurations(
							dialog.INSTRUMENT_CONFIG_ROOT,
							dialog.getConfigurations());

				// update list of groups
				String tmp = cmbGroup.getText();
				cmbGroup.setItems(listGroups());
				cmbGroup.setText(tmp);
			}
		});

	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Remove All");
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				modelProvider.getConfigurationList().clear();
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
				modelProvider.getConfigurationList().enableAll();
			}
		});
	    
	    menuItem = new MenuItem(menu, SWT.NONE);
	    menuItem.setText("Disable All");
	    menuItem.setImage(Resources.IMAGE_BOX_UNCHECKED);
	    menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				modelProvider.getConfigurationList().disableAll();
			}
		});
	    
	    // import/export
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
						succeeded = modelProvider.getConfigurationList().importConfiguration(stream);
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
						succeeded = modelProvider.getConfigurationList().saveTo(stream);
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

	    tableModel = createTableModel(tblConfigurations, menu, modelProvider);
	    
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
	private void createExpandedUi() {
		// check if UI is already expanded
		if (cmpExpand.isDisposed())
			return;
		
		cmpExpand.dispose();

        final ToolTip toolTip = new ToolTip(getShell(), SWT.ICON_INFORMATION);
        toolTip.setText("Information");
        toolTip.setMessage(
				"After the specified minimal time the acquisition is stopped when either the " +
				"desired Detector Counts or Monitor Counts have been collected but at latest " +
				"at the specified maximal time.");
        toolTip.setAutoHide(false);
		
        final MouseTrackListener listener = new MouseTrackAdapter() {
        	// finals
		    final int TOOLTIP_HIDE_DELAY = 300; // ms
		    final int MOUSE_OFFSET = 15;
		    final int BOTTOM_OFFSET = 150;
		    
        	// methods
			@Override
			public void mouseHover(MouseEvent e) {
				Display display = getDisplay();
				
				Point p = display.getCursorLocation();
				p.x += MOUSE_OFFSET;
				p.y += MOUSE_OFFSET;

				Rectangle bounds = display.getBounds();
				if (p.y > bounds.height - BOTTOM_OFFSET)
					p.y = bounds.height - BOTTOM_OFFSET;
				
				toolTip.setLocation(p.x, p.y);
				toolTip.setVisible(true);
			}
			@Override
			public void mouseExit(MouseEvent e) {
                getDisplay().timerExec(TOOLTIP_HIDE_DELAY, new Runnable() {
                    public void run() {
                        toolTip.setVisible(false);
                    }
                });
			}
		};
        
		cmpTransmission.addMouseTrackListener(listener);
		
		chkTransmissionMaxTime = new Button(cmpTransmission, SWT.CHECK);
		chkTransmissionMaxTime.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		chkTransmissionMaxTime.setText("Max Time:");
		chkTransmissionMaxTime.setSelection(true);
		chkTransmissionMaxTime.addMouseTrackListener(listener);
		txtTransmissionMaxTime.addMouseTrackListener(listener);
		lblTransmissionMaxTimeUnit.addMouseTrackListener(listener);
		
		chkTransmissionMaxTime.moveAbove(lblTransmissionMaxTime);
		
		chkTransmissionMinTime = new Button(cmpTransmission, SWT.CHECK);
		chkTransmissionMinTime.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		chkTransmissionMinTime.setText("Min Time:");
		chkTransmissionMinTime.addMouseTrackListener(listener);
		txtTransmissionMinTime = new Text(cmpTransmission, SWT.BORDER | SWT.RIGHT);
		txtTransmissionMinTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		txtTransmissionMinTime.addMouseTrackListener(listener);
		lblTransmissionMinTimeUnit = lblTransmissionMaxTime; // label is replaced by check-box, and then used as lblTransmissionMinTimeUnit
		lblTransmissionMinTimeUnit.setText("sec");
		lblTransmissionMinTimeUnit.addMouseTrackListener(listener);

		chkTransmissionMinTime.moveAbove(chkTransmissionMaxTime);
		txtTransmissionMinTime.moveAbove(chkTransmissionMaxTime);
		lblTransmissionMinTimeUnit.moveAbove(chkTransmissionMaxTime);

		chkTransmissionMonitorCounts = new Button(cmpTransmission, SWT.CHECK);
		chkTransmissionMonitorCounts.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		chkTransmissionMonitorCounts.setText("Monitor Counts:");
		chkTransmissionMonitorCounts.addMouseTrackListener(listener);
		txtTransmissionMonitorCounts = new Text(cmpTransmission, SWT.BORDER | SWT.RIGHT);
		txtTransmissionMonitorCounts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		txtTransmissionMonitorCounts.addMouseTrackListener(listener);
		Label blank1 = new Label(cmpTransmission, SWT.NONE);

		chkTransmissionMonitorCounts.moveAbove(chkTransmissionMaxTime);
		txtTransmissionMonitorCounts.moveAbove(chkTransmissionMaxTime);
		blank1.moveAbove(chkTransmissionMaxTime);

		chkTransmissionDetectorCounts = new Button(cmpTransmission, SWT.CHECK);
		chkTransmissionDetectorCounts.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		chkTransmissionDetectorCounts.setText("Detector Counts:");
		chkTransmissionDetectorCounts.addMouseTrackListener(listener);
		txtTransmissionDetectorCounts = new Text(cmpTransmission, SWT.BORDER | SWT.RIGHT);
		txtTransmissionDetectorCounts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		txtTransmissionDetectorCounts.addMouseTrackListener(listener);
		Label blank2 = new Label(cmpTransmission, SWT.NONE);

		chkTransmissionDetectorCounts.moveAbove(chkTransmissionMaxTime);
		txtTransmissionDetectorCounts.moveAbove(chkTransmissionMaxTime);
		blank2.moveAbove(chkTransmissionMaxTime);
		

		cmpScattering.addMouseTrackListener(listener);
		
		chkScatteringMaxTime = new Button(cmpScattering, SWT.CHECK);
		chkScatteringMaxTime.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		chkScatteringMaxTime.setText("Max Time:");
		chkScatteringMaxTime.setSelection(true);
		chkScatteringMaxTime.addMouseTrackListener(listener);
		txtScatteringMaxTime.addMouseTrackListener(listener);
		lblScatteringMaxTimeUnit.addMouseTrackListener(listener);

		chkScatteringMaxTime.moveAbove(lblScatteringMaxTime);
		
		chkScatteringMinTime = new Button(cmpScattering, SWT.CHECK);
		chkScatteringMinTime.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		chkScatteringMinTime.setText("Min Time:");
		chkScatteringMinTime.addMouseTrackListener(listener);
		txtScatteringMinTime = new Text(cmpScattering, SWT.BORDER | SWT.RIGHT);
		txtScatteringMinTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		txtScatteringMinTime.addMouseTrackListener(listener);
		lblScatteringMinTimeUnit = lblScatteringMaxTime;
		lblScatteringMinTimeUnit.setText("sec");
		lblScatteringMinTimeUnit.addMouseTrackListener(listener);

		chkScatteringMinTime.moveAbove(chkScatteringMaxTime);
		txtScatteringMinTime.moveAbove(chkScatteringMaxTime);
		lblScatteringMinTimeUnit.moveAbove(chkScatteringMaxTime);
		
		chkScatteringMonitorCounts = new Button(cmpScattering, SWT.CHECK);
		chkScatteringMonitorCounts.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		chkScatteringMonitorCounts.setText("Monitor Counts:");
		chkScatteringMonitorCounts.addMouseTrackListener(listener);
		txtScatteringMonitorCounts = new Text(cmpScattering, SWT.BORDER | SWT.RIGHT);
		txtScatteringMonitorCounts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		txtScatteringMonitorCounts.addMouseTrackListener(listener);
		Label blank3 = new Label(cmpScattering, SWT.NONE);

		chkScatteringMonitorCounts.moveAbove(chkScatteringMaxTime);
		txtScatteringMonitorCounts.moveAbove(chkScatteringMaxTime);
		blank3.moveAbove(chkScatteringMaxTime);
		
		chkScatteringDetectorCounts = new Button(cmpScattering, SWT.CHECK);
		chkScatteringDetectorCounts.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		chkScatteringDetectorCounts.setText("Detector Counts:");
		chkScatteringDetectorCounts.addMouseTrackListener(listener);
		txtScatteringDetectorCounts = new Text(cmpScattering, SWT.BORDER | SWT.RIGHT);
		txtScatteringDetectorCounts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		txtScatteringDetectorCounts.addMouseTrackListener(listener);
		Label blank4 = new Label(cmpScattering, SWT.NONE);

		chkScatteringDetectorCounts.moveAbove(chkScatteringMaxTime);
		txtScatteringDetectorCounts.moveAbove(chkScatteringMaxTime);
		blank4.moveAbove(chkScatteringMaxTime);

		layout(true, true);
	}
	
	// methods
	private void initDataBindings(final ModelProvider modelProvider, final DataBindingContext bindingContext, final List<IModelBinding> modelBindings) {
		// source
		ConfigurationList configurationList = modelProvider.getConfigurationList();
		
		// setup table
		tableModel.updateSource(configurationList);
		
		// selection
		final AtomicBoolean expandedBindings = new AtomicBoolean(false);
		final ProxyElement<Configuration> selectedConfiguration = tableModel.getSelectedElement();
		final ProxyElement<Measurement> selectedTransmissionMeasurement = new ProxyElement<Measurement>();
		final ProxyElement<Measurement> selectedScatteringMeasurement = new ProxyElement<Measurement>();
		final List<Configuration> observedConfigurations = new ArrayList<>(); // normally there shouldn't be more than 1 selected configuration

		final IElementListListener<Measurement> configurationListener = new IElementListListener<Measurement>() {
			@Override
			public void onAddedListElement(Measurement element) {
				if (element.getPath().getElementName().startsWith(Measurement.TRANSMISSION))
					selectedTransmissionMeasurement.setTarget(element);
				if (element.getPath().getElementName().startsWith(Measurement.SCATTERING))
					selectedScatteringMeasurement.setTarget(element);
			}
			@Override
			public void onDeletedListElement(Measurement element) {
				if (selectedTransmissionMeasurement.getTarget() == element)
					selectedTransmissionMeasurement.setTarget(null);
				else if (selectedScatteringMeasurement.getTarget() == element)
					selectedScatteringMeasurement.setTarget(null);
			}
		};
		final IProxyElementListener<Configuration> configurationProxyListener = new IProxyElementListener<Configuration>() {
			// methods
			@Override
			public void onTargetChange(Configuration oldTarget, Configuration newTarget) {
				if (oldTarget != null) {
					oldTarget.removeListListener(configurationListener);
					observedConfigurations.remove(oldTarget);
				}
				
				selectedTransmissionMeasurement.setTarget(null);
				selectedScatteringMeasurement.setTarget(null);
				
				if (newTarget != null) {
					newTarget.addListListener(configurationListener);
					observedConfigurations.add(newTarget);
				}
			}
		};
		// monitor when expanded UI is needed
		final IElementListener measurementListener = new IElementListener() {
			@Override
			public void onChangedProperty(IDependencyProperty property, Object oldValue, Object newValue) {
				if (EXPAND_CONDITIONS.containsKey(property) && Objects.equals(newValue, EXPAND_CONDITIONS.get(property)))
					// don't expand now, because that will add listeners to proxy that called this listener (resulting in ConcurrentModificationException)
					getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							// simulate expand click
							if (!btnExpand.isDisposed())
								btnExpand.notifyListeners(SWT.Selection, new Event());
						}
					});
			}
			@Override
			public void onDisposed() {
				// ignore
			}
		};
		final IProxyElementListener<Measurement> measurementProxyListener = new IProxyElementListener<Measurement>() {
			@Override
			public void onTargetChange(Measurement oldTarget, Measurement newTarget) {
				if (newTarget != null)
					for (Entry<IDependencyProperty, Boolean> entry : EXPAND_CONDITIONS.entrySet())
						if (Objects.equals(newTarget.get(entry.getKey()), entry.getValue())) {
							// don't expand now, because that will add listeners to proxy that called this listener (resulting in ConcurrentModificationException)
							getDisplay().asyncExec(new Runnable() {
								@Override
								public void run() {
									// simulate expand click
									if (!btnExpand.isDisposed())
										btnExpand.notifyListeners(SWT.Selection, new Event());
								}
							});
							return;
						}
			}
		};
		selectedTransmissionMeasurement.addListener(measurementListener);
		selectedTransmissionMeasurement.addListener(measurementProxyListener);
		selectedScatteringMeasurement.addListener(measurementListener);
		selectedScatteringMeasurement.addListener(measurementProxyListener);
		
		selectedConfiguration.addListener(configurationProxyListener);
		modelBindings.add(new IModelBinding() {
			@Override
			public void dispose() {
				for (Configuration configuration : observedConfigurations)
					configuration.removeListListener(configurationListener);
				observedConfigurations.clear();
				
				selectedConfiguration.removeListener(configurationProxyListener);
				
				selectedTransmissionMeasurement.setTarget(null);
				selectedScatteringMeasurement.setTarget(null);
			}
		});

		// data binding
		modelBindings.add(
			ModelBinder.createTextBinding(
					bindingContext,
					txtName,
					selectedConfiguration,
					Configuration.NAME,
					StringTrimConverter.DEFAULT));

		modelBindings.add(
			ModelBinder.createComboBinding(
					bindingContext,
					cmbGroup,
					selectedConfiguration,
					Configuration.GROUP,
					GroupTrimConverter.DEFAULT));

		modelBindings.add(
			ModelBinder.createTextBinding(
					bindingContext,
					txtDescription,
					selectedConfiguration,
					Configuration.DESCRIPTION,
					StringTrimConverter.DEFAULT));
		
		// save button
		modelBindings.add(ModelBinder.createEnabledBinding(
				btnSave,
				selectedConfiguration));
		
		final SelectionListener btnSaveListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Configuration configuration = selectedConfiguration.getTarget();
				if (configuration != null) {
					File root = ConfigurationCatalogDialog.INSTRUMENT_CONFIG_ROOT.toFile();
					File group = new File(root, configuration.getGroup());
					File file = new File(group, configuration.getName() + ".xml");
					
					if (!checkPath(file))
						showError("specified group is invalid");
					else if (!mkdirs(group))
						showError("unable to create group");
					else {
						if (file.exists()) {
							MessageBox dialog = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
							dialog.setText("Question");
							dialog.setMessage(String.format(
									"%s%n%n%s",
									"A configuration with the same name in the specified group already exists.",
									"Would you like to replace it?"));
							if (dialog.open() != SWT.YES)
								return;
						}

						if (!save(file, configuration))
							showError("unable to save configuration");
					}
				}
			}
			// helpers
			private boolean checkPath(File file) {
				try {
					// check if path is valid
					file.toPath();
					return true;
				}
				catch (Exception e) {
					return false;
				}
			}
			private boolean mkdirs(File file) {
				try {
					return file.isDirectory() || file.mkdirs();
				}
				catch (Exception e) {
					return false;
				}
			}
			private boolean save(File file, Configuration configuration) {
				try (OutputStream stream = new FileOutputStream(file.toString())) {
					return configuration.saveTo(stream);
				}
				catch (Exception e) {
					return false;
				}
			}
			private void showError(String message) {
				MessageBox dialog = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
				dialog.setText("Error");
				dialog.setMessage(message);
				dialog.open();
			}
		};
		
		btnSave.addSelectionListener(btnSaveListener);
		
		modelBindings.add(new IModelBinding() {
			@Override
			public void dispose() {
				btnSave.removeSelectionListener(btnSaveListener);
			}
		});
		
		// Scripts
		modelBindings.add(
			ModelBinder.createMultiLineBinding(
					bindingContext,
					txtInitializeScript,
					selectedConfiguration,
					Configuration.SETUP_SCRIPT,
					StringTrimConverter.DEFAULT));
		modelBindings.add(
			ModelBinder.createMultiLineBinding(
					bindingContext,
					txtPretransmissionScript,
					selectedTransmissionMeasurement,
					Measurement.SETUP_SCRIPT,
					StringTrimConverter.DEFAULT));
		modelBindings.add(
			ModelBinder.createMultiLineBinding(
					bindingContext,
					txtPrescatteringScript,
					selectedScatteringMeasurement,
					Measurement.SETUP_SCRIPT,
					StringTrimConverter.DEFAULT));
		
		// Test Drive
		modelBindings.add(ModelBinder.createEnabledBinding(
				btnInitializeScriptTestDrive,
				selectedConfiguration));
		modelBindings.add(ModelBinder.createEnabledBinding(
				btnPretransmissionScriptTestDrive,
				selectedTransmissionMeasurement));
		modelBindings.add(ModelBinder.createEnabledBinding(
				btnPrescatteringScriptTestDrive,
				selectedScatteringMeasurement));
		
		// Transmission - Attenuation
		modelBindings.add(
				ModelBinder.createComboBinding(
					bindingContext,
					cmbTransmissionAttAlgo,
					selectedTransmissionMeasurement,
					Measurement.ATTENUATION_ALGORITHM));
		modelBindings.add(
				ModelBinder.createComboBinding(
					bindingContext,
					cmbTransmissionAttAngle,
					selectedTransmissionMeasurement,
					Measurement.ATTENUATION_ANGLE,
					AttenuationAngleConverter.DEFAULT));

		// Transmission - MaxTime
		modelBindings.add(
				ModelBinder.createEnabledBinding(
						lblTransmissionMaxTime,
						selectedTransmissionMeasurement));
		modelBindings.add(
				ModelBinder.createEnabledBinding(
						bindingContext,
						txtTransmissionMaxTime,
						selectedTransmissionMeasurement,
						Measurement.MAX_TIME_ENABLED));
		modelBindings.add(
				ModelBinder.createTextBinding(
						bindingContext,
						txtTransmissionMaxTime,
						selectedTransmissionMeasurement,
						Measurement.MAX_TIME,
						TimeValueConverter.DEFAULT));
		modelBindings.add(
				ModelBinder.createEnabledBinding(
						lblTransmissionMaxTimeUnit,
						selectedConfiguration));

		// Scattering - Attenuation
		modelBindings.add(
				ModelBinder.createComboBinding(
						bindingContext,
						cmbScatteringAttAlgo,
						selectedScatteringMeasurement,
						Measurement.ATTENUATION_ALGORITHM));
		modelBindings.add(
				ModelBinder.createComboBinding(
						bindingContext,
						cmbScatteringAttAngle,
						selectedScatteringMeasurement,
						Measurement.ATTENUATION_ANGLE,
						AttenuationAngleConverter.DEFAULT));

		// Scattering - MaxTime
		modelBindings.add(
				ModelBinder.createEnabledBinding(
						lblScatteringMaxTime,
						selectedScatteringMeasurement));
		modelBindings.add(
				ModelBinder.createEnabledBinding(
						bindingContext,
						txtScatteringMaxTime,
						selectedScatteringMeasurement,
						Measurement.MAX_TIME_ENABLED));
		modelBindings.add(
				ModelBinder.createTextBinding(
						bindingContext,
						txtScatteringMaxTime,
						selectedScatteringMeasurement,
						Measurement.MAX_TIME,
						TimeValueConverter.DEFAULT));
		modelBindings.add(
				ModelBinder.createEnabledBinding(
						lblScatteringMaxTimeUnit,
						selectedConfiguration));

		// expand buttons (force expansion)
		if (!cmpExpand.isDisposed()) {
			final SelectionListener btnExpandListener = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					createExpandedUi();
					initExpandedBindings(modelProvider, bindingContext, modelBindings, expandedBindings, selectedTransmissionMeasurement, selectedScatteringMeasurement);
				}
			};
			final MouseListener lblExpandListener = new MouseAdapter() {
				@Override
				public void mouseUp(MouseEvent e) {
					createExpandedUi();
					initExpandedBindings(modelProvider, bindingContext, modelBindings, expandedBindings, selectedTransmissionMeasurement, selectedScatteringMeasurement);
				}
			};
			
			btnExpand.addSelectionListener(btnExpandListener);
			lblExpand.addMouseListener(lblExpandListener);
	
			modelBindings.add(new IModelBinding() {
				@Override
				public void dispose() {
					if (!cmpExpand.isDisposed()) {
						btnExpand.removeSelectionListener(btnExpandListener);
						lblExpand.removeMouseListener(lblExpandListener);
					}
				}
			});
		}
		
		// apply buttons

		modelBindings.add(new ApplyButtonBinding<Configuration>(
				txtInitializeScript,
				btnInitializeScriptApply,
				selectedConfiguration,
				Configuration.SETUP_SCRIPT));

		modelBindings.add(new ApplyButtonBinding<Measurement>(
				txtPretransmissionScript,
				btnPretransmissionScriptApply,
				selectedTransmissionMeasurement,
				Measurement.SETUP_SCRIPT));

		modelBindings.add(new ApplyButtonBinding<Measurement>(
				txtPrescatteringScript,
				btnPrescatteringScriptApply,
				selectedScatteringMeasurement,
				Measurement.SETUP_SCRIPT));

		// test drive buttons
		final Shell shell = getShell();
		final CustomInstrumentAction customAction = modelProvider.getCustomInstrumentAction();
		
		final SelectionListener initializeScriptTestDrivelistener = new TestDriveAction(
				shell,
				customAction,
				new IScriptProvider() {
					@Override
					public String generateScript() {
						Configuration configuration = selectedConfiguration.getTarget();
						if (configuration == null)
							return null;
						
						return configuration.getSetupScript();
					}
				});

		final SelectionListener pretransmissionScriptTestDrive = new TestDriveAction(
				shell,
				customAction,
				new IScriptProvider() {
					@Override
					public String generateScript() {
						Configuration configuration = selectedConfiguration.getTarget();
						Measurement transmissionMeasurement = selectedTransmissionMeasurement.getTarget();
						if ((configuration == null) || (transmissionMeasurement == null))
							return null;
						
						MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
						dialog.setText("Question");
						dialog.setMessage("Would you like to run the initialization script before the pre-transmission script?");
						switch (dialog.open()) {
						case SWT.YES:
							return String.format("%s%n%s", configuration.getSetupScript(), transmissionMeasurement.getSetupScript());
						case SWT.NO:
							return transmissionMeasurement.getSetupScript();
						default:
							return null;
						}
					}
				});
				
		final SelectionListener prescatteringScriptTestDrive = new TestDriveAction(
				shell,
				customAction,
				new IScriptProvider() {
					@Override
					public String generateScript() {
						Configuration configuration = selectedConfiguration.getTarget();
						Measurement scatteringMeasurement = selectedScatteringMeasurement.getTarget();
						if ((configuration == null) || (scatteringMeasurement == null))
							return null;
						
						MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
						dialog.setText("Question");
						dialog.setMessage("Would you like to run the initialization script before the pre-scattering script?");
						switch (dialog.open()) {
						case SWT.YES:
							return String.format("%s%n%s", configuration.getSetupScript(), scatteringMeasurement.getSetupScript());
						case SWT.NO:
							return scatteringMeasurement.getSetupScript();
						default:
							return null;
						}
					}
				});
		
		btnInitializeScriptTestDrive.addSelectionListener(initializeScriptTestDrivelistener);
		btnPretransmissionScriptTestDrive.addSelectionListener(pretransmissionScriptTestDrive);
		btnPrescatteringScriptTestDrive.addSelectionListener(prescatteringScriptTestDrive);
		
		modelBindings.add(new IModelBinding() {
			@Override
			public void dispose() {
				btnInitializeScriptTestDrive.removeSelectionListener(initializeScriptTestDrivelistener);
				btnPretransmissionScriptTestDrive.removeSelectionListener(pretransmissionScriptTestDrive);
				btnPrescatteringScriptTestDrive.removeSelectionListener(prescatteringScriptTestDrive);
			}
		});

		initExpandedBindings(modelProvider, bindingContext, modelBindings, expandedBindings, selectedTransmissionMeasurement, selectedScatteringMeasurement);
	}
	private void initExpandedBindings(
			final ModelProvider modelProvider,
			final DataBindingContext bindingContext,
			final List<IModelBinding> modelBindings,
			final AtomicBoolean expandedBindings,
			final ProxyElement<Measurement> selectedTransmissionMeasurement,
			final ProxyElement<Measurement> selectedScatteringMeasurement) {
	
		// check that UI is expanded
		if (!cmpExpand.isDisposed())
			return;
		
		// check if already bound
		if (!expandedBindings.compareAndSet(false, true))
			return;
		
		// Transmission - MaxTime
		modelBindings.add(
				ModelBinder.createCheckedBinding(
						bindingContext,
						chkTransmissionMaxTime,
						selectedTransmissionMeasurement,
						Measurement.MAX_TIME_ENABLED));

		// Transmission - MinTime
		modelBindings.add(
				ModelBinder.createCheckedBinding(
						bindingContext,
						chkTransmissionMinTime,
						selectedTransmissionMeasurement,
						Measurement.MIN_TIME_ENABLED));
		modelBindings.add(
				ModelBinder.createEnabledBinding(
						bindingContext,
						txtTransmissionMinTime,
						selectedTransmissionMeasurement,
						Measurement.MIN_TIME_ENABLED));
		modelBindings.add(
				ModelBinder.createTextBinding(
						bindingContext,
						txtTransmissionMinTime,
						selectedTransmissionMeasurement,
						Measurement.MIN_TIME,
						TimeValueConverter.DEFAULT));
		
		// lblTransmissionMinTimeUnit is lblTransmissionMaxTime, which is already bound
		//modelBindings.add(
		//		ModelBinder.createEnabledBinding(
		//				lblTransmissionMinTimeUnit,
		//				selectedTransmissionMeasurement));

		// Transmission - MonitorCounts
		modelBindings.add(
				ModelBinder.createCheckedBinding(
						bindingContext,
						chkTransmissionMonitorCounts,
						selectedTransmissionMeasurement,
						Measurement.TARGET_MONITOR_COUNTS_ENABLED));
		modelBindings.add(
				ModelBinder.createEnabledBinding(
						bindingContext,
						txtTransmissionMonitorCounts,
						selectedTransmissionMeasurement,
						Measurement.TARGET_MONITOR_COUNTS_ENABLED));
		modelBindings.add(
				ModelBinder.createTextBinding(
						bindingContext,
						txtTransmissionMonitorCounts,
						selectedTransmissionMeasurement,
						Measurement.TARGET_MONITOR_COUNTS,
						CountValueConverter.DEFAULT));

		// Transmission - DetectorCounts
		modelBindings.add(
			ModelBinder.createCheckedBinding(
					bindingContext,
					chkTransmissionDetectorCounts,
					selectedTransmissionMeasurement,
					Measurement.TARGET_DETECTOR_COUNTS_ENABLED));
		modelBindings.add(
				ModelBinder.createEnabledBinding(
						bindingContext,
						txtTransmissionDetectorCounts,
						selectedTransmissionMeasurement,
						Measurement.TARGET_DETECTOR_COUNTS_ENABLED));
		modelBindings.add(
				ModelBinder.createTextBinding(
						bindingContext,
						txtTransmissionDetectorCounts,
						selectedTransmissionMeasurement,
						Measurement.TARGET_DETECTOR_COUNTS,
						CountValueConverter.DEFAULT));
		
		// Scattering - MaxTime
		modelBindings.add(
				ModelBinder.createCheckedBinding(
						bindingContext,
						chkScatteringMaxTime,
						selectedScatteringMeasurement,
						Measurement.MAX_TIME_ENABLED));
		
		// Scattering - MinTime
		modelBindings.add(
				ModelBinder.createCheckedBinding(
						bindingContext,
						chkScatteringMinTime,
						selectedScatteringMeasurement,
						Measurement.MIN_TIME_ENABLED));
		modelBindings.add(
				ModelBinder.createEnabledBinding(
						bindingContext,
						txtScatteringMinTime,
						selectedScatteringMeasurement,
						Measurement.MIN_TIME_ENABLED));
		modelBindings.add(
				ModelBinder.createTextBinding(
						bindingContext,
						txtScatteringMinTime,
						selectedScatteringMeasurement,
						Measurement.MIN_TIME,
						TimeValueConverter.DEFAULT));
		
		// lblScatteringMinTimeUnit is lblScatteringMaxTime, which is already bound
		//modelBindings.add(
		//		ModelBinder.createEnabledBinding(
		//				lblScatteringMinTimeUnit,
		//				selectedScatteringMeasurement));
		
		// Scattering - MonitorCounts
		modelBindings.add(
				ModelBinder.createCheckedBinding(
						bindingContext,
						chkScatteringMonitorCounts,
						selectedScatteringMeasurement,
						Measurement.TARGET_MONITOR_COUNTS_ENABLED));
		modelBindings.add(
				ModelBinder.createEnabledBinding(
						bindingContext,
						txtScatteringMonitorCounts,
						selectedScatteringMeasurement,
						Measurement.TARGET_MONITOR_COUNTS_ENABLED));
		modelBindings.add(
				ModelBinder.createTextBinding(
						bindingContext,
						txtScatteringMonitorCounts,
						selectedScatteringMeasurement,
						Measurement.TARGET_MONITOR_COUNTS,
						CountValueConverter.DEFAULT));
		
		// Scattering - DetectorCounts
		modelBindings.add(
				ModelBinder.createCheckedBinding(
						bindingContext,
						chkScatteringDetectorCounts,
						selectedScatteringMeasurement,
						Measurement.TARGET_DETECTOR_COUNTS_ENABLED));
		modelBindings.add(
				ModelBinder.createEnabledBinding(
						bindingContext,
						txtScatteringDetectorCounts,
						selectedScatteringMeasurement,
						Measurement.TARGET_DETECTOR_COUNTS_ENABLED));
		modelBindings.add(
				ModelBinder.createTextBinding(
						bindingContext,
						txtScatteringDetectorCounts,
						selectedScatteringMeasurement,
						Measurement.TARGET_DETECTOR_COUNTS,
						CountValueConverter.DEFAULT));
	}
	private static ElementTableModel<ConfigurationList, Configuration> createTableModel(final KTable table, Menu menu, final ModelProvider modelProvider) {
		// cell rendering
		DefaultCellRenderer indexRenderer = new TextCellRenderer(TextCellRenderer.INDICATION_FOCUS);
    	DefaultCellRenderer checkableRenderer = new CheckableCellRenderer(CheckableCellRenderer.INDICATION_FOCUS | CheckableCellRenderer.INDICATION_COPYABLE);
    	DefaultCellRenderer nameRenderer = new NameCellRenderer(TextCellRenderer.INDICATION_FOCUS | TextCellRenderer.INDICATION_COPYABLE); 

    	indexRenderer.setAlignment(SWTX.ALIGN_HORIZONTAL_RIGHT | SWTX.ALIGN_VERTICAL_CENTER);
    	
    	// cell editing
    	KTableCellEditor indexEditor = new KTableCellEditorText2(SWT.RIGHT);
    	KTableCellEditor checkableEditor = new KTableCellEditorCheckbox();
    	KTableCellEditor nameEditor = new KTableCellEditorText2();
    	
    	// buttons
    	IButtonListener<Configuration> addButtonListener = new IButtonListener<Configuration>() {
			@Override
			public void onClicked(int col, int row, Configuration configuration) {
				modelProvider.getConfigurationList().addConfiguration(configuration.getIndex());
			}
		};
    	IButtonListener<Configuration> duplicateButtonListener = new IButtonListener<Configuration>() {
			@Override
			public void onClicked(int col, int row, Configuration configuration) {
				configuration.duplicate();
			}
		};
    	IButtonListener<Configuration> deleteButtonListener = new IButtonListener<Configuration>() {
			@Override
			public void onClicked(int col, int row, Configuration configuration) {
				configuration.delete();
			}
		};
    	
    	// construction
    	ElementTableModel<ConfigurationList, Configuration> model = new ElementTableModel<ConfigurationList, Configuration>(
    			table,
    			menu,
		    	"add, duplicate or delete configuration",
		    	Arrays.asList(
		    			new ButtonInfo<Configuration>(Resources.IMAGE_PLUS_SMALL_GRAY, Resources.IMAGE_PLUS_SMALL, addButtonListener),
		    			new ButtonInfo<Configuration>(Resources.IMAGE_COPY_SMALL_GRAY, Resources.IMAGE_COPY_SMALL, duplicateButtonListener),
		    			new ButtonInfo<Configuration>(Resources.IMAGE_MINUS_SMALL_GRAY, Resources.IMAGE_MINUS_SMALL, deleteButtonListener)),
		    	Arrays.asList(
		    			new ColumnDefinition("", 30, Configuration.ENABLED, checkableRenderer, checkableEditor),
		    			new ColumnDefinition("", 30, Configuration.INDEX, indexRenderer, indexEditor, IndexValueConverter.DEFAULT),
		    			new ColumnDefinition("Name", 200, Configuration.NAME, nameRenderer, nameEditor)));

    	table.setModel(model);
    	
    	return model;
	}

	// helpers
	private static String[] listGroups() {
		ArrayList<String> result = new ArrayList<>();
		
		listGroups(
				result,
				ConfigurationCatalogDialog.INSTRUMENT_CONFIG_ROOT,
				ConfigurationCatalogDialog.INSTRUMENT_CONFIG_ROOT);
		
		return result.toArray(new String[result.size()]);
	}
	private static void listGroups(List<String> groups, Path root, Path directory) {
	    try(DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
	        for (Path p : stream) {
	            if(Files.isDirectory(p)) {
	            	groups.add(GroupTrimConverter.DEFAULT.toModelValue(root.relativize(p).toString()));
	            	listGroups(groups, root, p);
	            }
	        }
	    }
	    catch (IOException e) {
	    	// ignore
		}
	}
	
	// enable apply button when script has been modified
	private static class ApplyButtonBinding<TElement extends Element> implements IModelBinding {
		// fields
		private boolean enabled = false;
		// ui
		private final Text text;
		private final ModifyListener textModifyListener;
		private final IElementListener elementListener;
		private final IProxyElementListener<TElement> proxyListener;
		// deferred update
		private final Runnable updater;
		
		// construction
		public ApplyButtonBinding(final Text text, final Button button, final ProxyElement<TElement> proxy, final IDependencyProperty property) {
			this.text = text;
			
			updater = new Runnable() {
				@Override
				public void run() {
					if (!button.isDisposed())
						button.setEnabled(enabled);
				}
			};
			
			textModifyListener = new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					TElement element = proxy.getTarget();
					
					enabled =
							(element != null) &&
							!Objects.equals(text.getText(), element.get(property));
					
					text.getDisplay().asyncExec(updater);
				}
			};
			
			elementListener = new IElementListener() {
				@Override
				public void onChangedProperty(IDependencyProperty p, Object oldValue, Object newValue) {
					if (p == property) {
						enabled = !Objects.equals(text.getText(), newValue);
						text.getDisplay().asyncExec(updater);
					}
				}
				@Override
				public void onDisposed() {
					// ignore
				}
			};
			
			proxyListener = new IProxyElementListener<TElement>() {
				@Override
				public void onTargetChange(TElement oldTarget, TElement newTarget) {
					enabled =
							(newTarget != null) &&
							!Objects.equals(text.getText(), newTarget.get(property));
					
					text.getDisplay().asyncExec(updater);
				}
			};
			
			text.addModifyListener(textModifyListener);
			proxy.addListener(elementListener);
			proxy.addListener(proxyListener);
		}
		@Override
		public void dispose() {
			text.removeModifyListener(textModifyListener);
		}
	}
	
	// test drive button
	private static interface IScriptProvider {
		// methods
		public String generateScript();
	}
	private static class TestDriveAction extends SelectionAdapter {
		// fields
		private final Shell shell;
		private final CustomInstrumentAction customAction;
		private final IScriptProvider scriptProvider;
		
		// construction
		public TestDriveAction(Shell shell, CustomInstrumentAction customAction, IScriptProvider scriptProvider) {
			this.shell = shell;
			this.customAction = customAction;
			this.scriptProvider = scriptProvider;
		}
		
		// event handling
		@Override
		public void widgetSelected(SelectionEvent e) {
			String script = scriptProvider.generateScript();
			if (script == null)
				return;
			
			if (!customAction.testDrive(script)) {
				MessageBox dialog = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
				dialog.setText("Information");
				dialog.setMessage("busy");
				dialog.open();
			}
		}
	}
}
