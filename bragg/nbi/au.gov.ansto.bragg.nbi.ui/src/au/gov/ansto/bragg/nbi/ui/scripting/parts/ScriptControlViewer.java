/**
 * 
 */
package au.gov.ansto.bragg.nbi.ui.scripting.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.databinding.beans.BeanObservableValueDecorator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.nebula.widgets.pgroup.RectangleGroupStrategy;
import org.eclipse.nebula.widgets.pgroup.ext.MenuBasedGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.ExpandListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.part.NullEditorInput;
import org.gumtree.gumnix.sics.control.ISicsController;
import org.gumtree.gumnix.sics.control.controllers.ComponentDataFormatException;
import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.control.events.DynamicControllerListenerAdapter;
import org.gumtree.gumnix.sics.control.events.IDynamicControllerListener;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.gumnix.sics.ui.SicsUIConstants;
import org.gumtree.scripting.IScriptExecutor;
import org.gumtree.scripting.ScriptExecutor;
import org.gumtree.ui.scripting.viewer.ICommandLineViewer;

import au.gov.ansto.bragg.nbi.scripting.IPyObject;
import au.gov.ansto.bragg.nbi.scripting.IPyObject.PyObjectType;
import au.gov.ansto.bragg.nbi.scripting.PyObjectImp;
import au.gov.ansto.bragg.nbi.scripting.ScriptAction;
import au.gov.ansto.bragg.nbi.scripting.ScriptAction.ActionStatus;
import au.gov.ansto.bragg.nbi.scripting.ScriptAction.IActionStatusListener;
import au.gov.ansto.bragg.nbi.scripting.ScriptModel;
import au.gov.ansto.bragg.nbi.scripting.ScriptObjectGroup;
import au.gov.ansto.bragg.nbi.scripting.ScriptObjectTab;
import au.gov.ansto.bragg.nbi.scripting.ScriptParameter;
import au.gov.ansto.bragg.nbi.scripting.ScriptParameter.PType;
import au.gov.ansto.bragg.nbi.ui.internal.Activator;
import au.gov.ansto.bragg.nbi.ui.internal.InternalImage;
import au.gov.ansto.bragg.nbi.ui.scripting.ScriptPageRegister;
import au.gov.ansto.bragg.nbi.ui.scripting.parts.ImageButton.ButtonListener;
import au.gov.ansto.bragg.nbi.ui.scripting.parts.ScriptDataSourceViewer.IActivityListener;


/**
 * @author nxi
 *
 */
@SuppressWarnings("restriction")
public class ScriptControlViewer extends Composite {

	private final static String FILENAME_NODE_PATH = "/experiment/file_name";
	private final static String SAVE_COUNT_PATH = "/experiment/save_count";
	private final static Image BTN_IMAGE_BEGIN = InternalImage.CATEGORY_BTN_IMAGE_BEGIN.getImage();
	private final static Image BTN_IMAGE_BAR = InternalImage.CATEGORY_BTN_IMAGE_BAR.getImage(30, -1);
	private final static Image BTN_IMAGE_END = InternalImage.CATEGORY_BTN_IMAGE_END.getImage();
	
	protected static String fileDialogPath;
	private static int SCRIPT_REGISTER_ID = 0;
	private static final String ID_PREFERENCE_RECENT_FILE = "org.gumtree.scripting.recent";
	private final static String PROPERTY_ENABLE_EDITING = "gumtree.scripting.enableEditing";
	private static final String DEFAULT_SCRIPTING_FOLDER = "gumtree.scripting.defaultFolder";
	private static final String TEMPLATE_SCRIPT = "/pyscripts/AnalysisScriptingTemplate.py";
	private static final String __INIT__SCRIPT = "/pyscripts/__init__.py";
	private static final String PRE_RUN_SCRIPT = "/pyscripts/pre_run.py";
	private static final String POST_RUN_SCRIPT	= "/pyscripts/post_run.py";
//	private static final String INTERNAL_FOLDER_PATH = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + "/Internal";
	public static final String WORKSPACE_FOLDER_PATH = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
	public static final String GUMTREE_SCRIPTING_LIST_PROPERTY = "gumtree.scripting.menuitems";
	public static final String GUMTREE_SCRIPTING_INIT_PROPERTY = "gumtree.scripting.initscript";
	public static final String GUMTREE_SCRIPTING_ALLOWFOLDING_PROPERTY = "gumtree.scripting.jython.allowFolding";
	
//	private static final String[][] INTERNAL_SCRIPTS = new String[][]{
//									{"Experiment Setup", "/Experiment/Experiment_Setup.py"},
//									{"Nickel Alignment", "/Nickel_Auto/auto_Nickel_align.py"},
//									{"Slits Calibration", "/Experiment/Slits_Calibration.py"},
//									{"Live Data", "/Analysis/Live_Data.py"}, 
//									{"Graffiti Export", "/Analysis/Graffiti_Export.py"}};
//	private static final String[] INITIAL_SCRIPTS = new String[]{
//									"/Experiment/Initialise.py"
//									};
//	private static String[] PRE_RUN_SCRIPT = new String[] {
//		"from gumpy.nexus import *",
//		"from gumpy.control import param",
//		"from gumpy.control import script",
//		"from gumpy.control.param import Par",
//		"from gumpy.control.param import Act",
//		"from gumpy.control.param import Group",
//		"from gumpy.control.script import *",
//		"from au.gov.ansto.bragg.nbi.ui.scripting import ScriptPageRegister",
//		"from gumpy.vis.image2d import Image",
//		"from gumpy.vis.plot1d import Plot",
//		"from gumpy.vis.gplot import GPlot",
//		"from gumpy.vis.event import MouseListener",
//		"from org.eclipse.core.resources import ResourcesPlugin",
//		"from gumpy.commons.logger import log as glog",
//		"from au.gov.ansto.bragg.nbi.ui.scripting.parts import ScriptRunner",
//		"from gumpy.commons import sics",
//		"import time",
//		"__register__ = ScriptPageRegister.getRegister(__script_model_id__)",
//		"__UI__ = __register__.getControlViewer()",
//		"__DATASOURCE__ = __register__.getDataSourceViewer()",
//		"__model__ = __register__.getScriptModel()",
//		"__script__ = Script(__model__)",
//		"__script__.title = 'unknown'",
//		"__script__.version = 'unknown'",
//		"__runner__ = __UI__.getRunner()",
//		"__writer__ = __UI__.getScriptExecutor().getEngine().getContext().getWriter()",
//		"def log(text):",
//		"\tglog(text, __writer__)",
//		"clear = script.clear",
//		"Par.__model__ = __model__",
//		"Act.__model__ = __model__",
//		"Group.__model__ = __model__",
//		"df = script.df",
//		"Plot1 = GPlot(widget=__register__.getPlot1())",
//		"Plot2 = GPlot(widget=__register__.getPlot2())",
//		"Plot3 = GPlot(widget=__register__.getPlot3())",
//		"gumtree_root = str(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString())",
//		"def noclose():",
//		"\tprint 'not closable'",
//		"def load_script(fname):",
//		"\tfname = os.path.dirname(__UI__.getScriptFilename()) + '/' + fname",
//		"\t__UI__.loadScript(fname)",
//		"def confirm(msg):", 
//		"\treturn __runner__.openConfirm(msg)",
//		"def selectSaveFolder():",
//		"\treturn __runner__.selectSaveFile()",
//		"Plot1.close = noclose",
//		"Plot2.close = noclose",
//		"Plot3.close = noclose",
////		"Plot1 = Image(widget=__register__.getPlot1())",
////		"Plot2 = Plot(widget=__register__.getPlot2())",
////		"Plot3 = Image(widget=__register__.getPlot3())",
//		"if '__dispose__' in globals() :",
//		"\t__dispose__()",
//		"def auto_run():",
//		"\tpass",
//		"def run_action(act):",
//		"\tact.set_running_status()",
//		"\ttry:",
//		"\t\texec(act.command)",
//		"\t\tact.set_done_status()",
//		"\texcept Exception, e:",
//		"\t\tact.set_interrupt_status()",
//		"\t\traise Exception, e.message",
//		"\texcept:",
//		"\t\tact.set_error_status()",
//		"\t\traise Exception, 'Error in running ' + act.name",
//		"\tsics.handleInterrupt()",
//		"",
//	};
//	private static String[] POST_RUN_SCRIPT = new String[] {
//		"def set_name(obj, name):",
//		"\tobj.name = name",
//		"",
//		"for __name__to__test__ in globals().scope_keys() :",
//		"\tif eval('isinstance(' + __name__to__test__ + ', Par) or isinstance(' + __name__to__test__ + ', Act)') :",
//        "\t\teval('set_name(' + __name__to__test__ + ', \"' + __name__to__test__ + '\")')",
//        "",
//        "__model__.fireModelChanged()",
//        "if hasattr(__script__, 'dict_path') and __script__.dict_path != None:",
//        "\tDataset.__dicpath__ = __script__.dict_path",
//        "",
//	};
	private Composite staticComposite;
	private ScrolledComposite scroll;
	private Composite dynamicComposite;
	private Label scriptLabel;
	private Button loadButton;
	private Button reloadButton;
	private Button showButton;
	private Button runButton;
	private Menu loadMenu;
	private MenuItem openMenuItem;
	private MenuItem newMenuItem;
	private MenuItem recentMenuItem;
	private Menu recentMenu;
	private List<MenuItem> recentMenuItems;
	private ScriptRunner runner;
	private IEditorPart scriptEditor;
	private IPropertyListener editorListener;
	private int scriptRegisterID;
	private ScriptModel scriptModel;
	private String scriptFilename;
	private Button currentButton;
	private IScriptExecutor scriptExecutor;
//	private IScriptExecutor scriptValidator;
	private IActivityListener datasetActivityListener;
	private boolean groupAllowFolding = false;
	private boolean editingEnabled = true;
	private Color highlightColor;
	private Color defaultColor;
	private Map<Integer, Composite> groupMap;

	
	/**
	 * @param parent
	 * @param style
	 */
	public ScriptControlViewer(Composite parent, int style) {
		super(parent, style);
		highlightColor = Display.getDefault().getSystemColor(SWT.COLOR_RED);
		defaultColor = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
		try {
			groupAllowFolding = Boolean.valueOf(System.getProperty(GUMTREE_SCRIPTING_ALLOWFOLDING_PROPERTY));
		} catch (Exception e) {
		}
		scriptRegisterID = getNextRegisterID();
		GridLayoutFactory.fillDefaults().margins(0, 0).spacing(1, 1).applyTo(this);
		recentMenuItems = new ArrayList<MenuItem>();
		createStaticArea();
		createDynamicArea();
		runner = new ScriptRunner(parent.getShell());
		String enableEditingProperty = System.getProperty(PROPERTY_ENABLE_EDITING);
		if (enableEditingProperty != null) {
			try {
				editingEnabled = Boolean.valueOf(enableEditingProperty);
			} catch (Exception e) {
			}
		}
		groupMap = new HashMap<Integer, Composite>();
	}

	private void createStaticArea() {
		staticComposite = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).margins(0, 0).spacing(1, 1).applyTo(staticComposite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(staticComposite);
		staticComposite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		staticComposite.setBackgroundMode(SWT.INHERIT_FORCE);
		
		loadButton = new Button(staticComposite, SWT.PUSH);
		loadButton.setText("Load Script");
		loadButton.setToolTipText("Load script from file system or create new script.");
		loadButton.setImage(InternalImage.DOWN_16.getImage());
		GridDataFactory.fillDefaults().grab(false, false).minSize(40, 0).applyTo(loadButton);
		loadMenu = new Menu(staticComposite);
		openMenuItem = new MenuItem(loadMenu, SWT.PUSH);
		openMenuItem.setImage(InternalImage.OPEN_16.getImage());
		openMenuItem.setText("Open file...");
		newMenuItem = new MenuItem(loadMenu, SWT.PUSH);
		newMenuItem.setImage(InternalImage.NEW_16.getImage());
		newMenuItem.setText("New script...");
		recentMenuItem = new MenuItem(loadMenu, SWT.CASCADE);
		recentMenuItem.setText("Recent");
		recentMenuItem.setImage(InternalImage.RECENT_16.getImage());
		recentMenu = new Menu(recentMenuItem);
		recentMenuItem.setMenu(recentMenu);
		new MenuItem(loadMenu, SWT.SEPARATOR);
		String scriptListProperty = System.getProperty(GUMTREE_SCRIPTING_LIST_PROPERTY);
		if (scriptListProperty != null) {
			String[] scripts = scriptListProperty.split(",");
			for (int i = 0; i < scripts.length; i++){
				if (scripts[i].contains(":")){
					String[] pairs = scripts[i].split(":");
					MenuItem item = new MenuItem(loadMenu, SWT.PUSH);
					item.setText(pairs[0]);
					final String itemPath = getFullScriptPath(pairs[1]);
					if (itemPath != null) {
						item.addSelectionListener(new SelectionListener() {

							@Override
							public void widgetSelected(SelectionEvent e) {
								loadScript(itemPath);
							}

							@Override
							public void widgetDefaultSelected(SelectionEvent e) {
							}
						});
					}
				} else {
					MenuItem item = new MenuItem(loadMenu, SWT.PUSH);
					final String itemPath = getFullScriptPath(scripts[i]);
					if (itemPath != null) {
						File scriptFile = new File(itemPath);
						item.setText(scriptFile.getName());
						item.addSelectionListener(new SelectionListener() {

							@Override
							public void widgetSelected(SelectionEvent e) {
								loadScript(itemPath);
							}

							@Override
							public void widgetDefaultSelected(SelectionEvent e) {
							}
						});
					}
				}
			}
		}
		
		scriptLabel = new Label(staticComposite, SWT.CENTER);
		GridDataFactory.fillDefaults().grab(false, false).indent(0, 6).span(2, 1).applyTo(scriptLabel);
		
		showButton = new Button(staticComposite, SWT.PUSH);
		showButton.setText("Edit/Hide");
		showButton.setToolTipText("Click to edit or hide the script currently loaded.");
		showButton.setImage(InternalImage.EDIT_16.getImage());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(showButton);
		showButton.setEnabled(editingEnabled && false);

		reloadButton = new Button(staticComposite, SWT.PUSH);
		reloadButton.setText("Reload");
		reloadButton.setToolTipText("Click to reload the current script.");
		reloadButton.setImage(InternalImage.RELOAD_16.getImage());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(reloadButton);
		reloadButton.setEnabled(false);

		runButton = new Button(staticComposite, SWT.PUSH);
		runButton.setText("Run");
		runButton.setToolTipText("Click to run the current script (run command: __run_script__ defined in the script).");
		runButton.setImage(InternalImage.PLAY_16.getImage());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(runButton);
		runButton.setEnabled(false);

		initListeners();
	}

	private void initListeners() {
		openMenuItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.SINGLE);
 				if (fileDialogPath == null){
 					String folderPath = System.getProperty(DEFAULT_SCRIPTING_FOLDER);
 					if (folderPath != null) {
 						File folderFile = new File(folderPath);
 						if (!folderFile.exists()) {
 							folderPath = getFullScriptPath(folderPath);
 						}
						dialog.setFilterPath(folderPath);
// 						dialog.setFilterPath("D:/Git");
 					} else {
 						IWorkspace workspace= ResourcesPlugin.getWorkspace();
 						IWorkspaceRoot root = workspace.getRoot();
 						dialog.setFilterPath(root.getLocation().toOSString());
 					}
 				} else {
 					dialog.setFilterPath(fileDialogPath);
 				}
 				dialog.setFilterExtensions(new String[]{"*.py"});
 				dialog.open();
 				if (dialog.getFileName() == null) {
 					return;
 				}
				String filePath = dialog.getFilterPath() + File.separator + dialog.getFileName();
				File pickedFile = new File(filePath);
				if (!pickedFile.exists() || !pickedFile.isFile())
					return;
				fileDialogPath = pickedFile.getParent();
				if (filePath != null) {
					try {
						initScriptControl(filePath);
						addToRecentList(filePath);
						IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						if (page.isEditorAreaVisible()){
							showHideScript();
						}
					} catch (Exception error) {
						handleException(error, "Cannot open file " + filePath);
						error.printStackTrace();
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		newMenuItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				NewConfigFileWizard wizard = new NewConfigFileWizard();
				wizard.init(null, null);
				WizardDialog dialog = new WizardDialog(getShell(), wizard);
				try {
					dialog.open();
					String filePath = wizard.getFilename();
					if (filePath != null) {
						initScriptControl(filePath);
						addToRecentList(filePath);
						showHideScript();
					}
				}catch (Exception ex) {
					handleException(ex, "failed to create a new script");
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		loadButton.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				for (MenuItem item : recentMenuItems) {
					item.dispose();
				}
				recentMenuItems.clear();
				makeRecentListMenu(recentMenu);
				Rectangle rect = loadButton.getBounds ();
//				final Point location = loadButton.getLocation();
//				final Point size = loadButton.getSize();
				Point pt = new Point (rect.x, rect.y + rect.height);
				pt = loadButton.getParent().toDisplay (pt);
				loadMenu.setLocation (pt.x, pt.y);
				loadMenu.setVisible (true);
			}

		});
		
		editorListener = new IPropertyListener() {
			boolean isDirty = false;
			@Override
			public void propertyChanged(Object arg0, int arg1) {
				if (arg1 == 257) {
					isDirty = !isDirty;
				}
				if (!isDirty) {
					if (!((IEditorPart) arg0).isDirty()){
						confirmReload();
					}
				}
			}
		};
		
		showButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					showHideScript();
				} catch (Exception ex) {
					ex.printStackTrace();
				}				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		reloadButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					initScriptControl(getScriptFilename());
				} catch (FileNotFoundException e1) {
					handleException(e1, "faild to reload the script, " +
							"please see scripting console for details");
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		runButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				runScript();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		try {
//			initSicsListeners();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
//		IResourceChangeListener listener = new MyResourceChangeReporter();
//		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener,
//				IResourceChangeEvent.PRE_CLOSE
//				| IResourceChangeEvent.PRE_DELETE
//				| IResourceChangeEvent.PRE_BUILD
//				| IResourceChangeEvent.POST_BUILD
//				| IResourceChangeEvent.POST_CHANGE);
	}

	public void runNativeInitScript() {
		IScriptExecutor executor = getScriptExecutor();
		if (executor != null) {
			try {
				String fn = FileLocator.toFileURL(Activator.getDefault().getBundle().getEntry(__INIT__SCRIPT)).getFile();
				executor.runScript("__script_model_id__ = " + scriptRegisterID);
				executor.runScript(new FileReader(fn));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
	}
	
	public void runInitialScripts() {
		runNativeInitScript();
		String initScriptString = System.getProperty(GUMTREE_SCRIPTING_INIT_PROPERTY);
		if (initScriptString != null && initScriptString.trim() != "") {
			String scriptPath = getFullScriptPath(initScriptString);
			if (scriptPath != null) {
				try {
					initScriptControl(scriptPath);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			} else {
				IScriptExecutor executor = getScriptExecutor();
				if (executor != null) {
					executor.runScript("print 'failed to load " + initScriptString + "'");
				}
			}
		}
//		for (String item : INITIAL_SCRIPTS){
//			String itemPath = INTERNAL_FOLDER_PATH + item;
//			try {
//				initScriptControl(itemPath);
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
//		}
	}

	private void makeRecentListMenu(Menu parent) {
		String recentFiles = Platform.getPreferencesService().getString(
				Activator.PLUGIN_ID, ID_PREFERENCE_RECENT_FILE, "", null).trim();
		if (recentFiles != null && recentFiles.length() > 0) {
			String[] names = recentFiles.split(";");
			for (int i = 0; i < names.length; i++) {
				final String name = names[i];
				if (name.trim().length() > 0) {
					File file = new File(name);
					if (file.exists()) {
						MenuItem recentItem = new MenuItem(parent, SWT.PUSH);
						if (file.getName().length() > 30) {
							recentItem.setText(file.getName());
						} else {
							if (name.length() > 33){
								recentItem.setText(name.substring(0, 3) + "..." + name.substring(name.length() - 27));
							} else {
								recentItem.setText(name);
							}
						}
						recentItem.addSelectionListener(new SelectionListener() {
							
							@Override
							public void widgetSelected(SelectionEvent e) {
								try {
									initScriptControl(name);
									addToRecentList(name);
								} catch (FileNotFoundException e1) {
									handleException(e1, "failed to load script:" + name);
								}
							}
							
							@Override
							public void widgetDefaultSelected(SelectionEvent e) {
							}
						});
						recentMenuItems.add(recentItem);
					}
				}
			}
		}
	}
	
	private void showHideScript() throws PartInitException {
		File fileToOpen = new File(getRunner().getScriptPath());
		IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart editor = page.getActiveEditor();
		if (editor != null) {
			IEditorInput input = editor.getEditorInput();
			if (input instanceof IURIEditorInput) {
				URI uri = ((IURIEditorInput) input).getURI();
				if (uri.equals(fileStore.toURI())) {
					setEditorVisible(!page.isEditorAreaVisible());
					return;
				}
			} 
		}
		IEditorPart editorPart = IDE.openEditorOnFileStore( page, fileStore );
		setEditorVisible(true);
		if (editorPart != scriptEditor) {
			if (scriptEditor != null){
				scriptEditor.removePropertyListener(editorListener);
			}
			scriptEditor = editorPart;
			editorPart.addPropertyListener(editorListener);
		}
	}
	
	private void confirmReload() {
//		IWorkbenchPage page = ScriptPageRegister.getRegister(scriptRegisterID).getWorkbenchPage();
//		String pageID = page.getPerspective().getId();
//		if (ScriptingPerspective.SCRIPTING_PERSPECTIVE_ID.equals(pageID)) {
			boolean reload = MessageDialog.openQuestion(getShell(), "Reload the Script", "The " +
					"analysis script has been changed, do you want to reload it? All argument " +
					"values will be reset to default if reloaded.");
			if (reload) {
				try {
					initScriptControl(getScriptFilename());
				} catch (FileNotFoundException e) {
					handleException(e, "faild to reload the script, " +
							"please see scripting console for details");
				}
			}
//		}
	}
	
//	public class MyResourceChangeReporter implements IResourceChangeListener {
//		public void resourceChanged(IResourceChangeEvent event) {
//			IResource res = event.getResource();
//			switch (event.getType()) {
//			case IResourceChangeEvent.PRE_CLOSE:
//				System.out.print("Project ");
//				System.out.print(res.getFullPath());
//				System.out.println(" is about to close.");
//				break;
//			case IResourceChangeEvent.PRE_DELETE:
//				System.out.print("Project ");
//				System.out.print(res.getFullPath());
//				System.out.println(" is about to be deleted.");
//				break;
//			case IResourceChangeEvent.POST_CHANGE:
//				System.out.println("Resources have changed.");
//				System.out.println("resource:" + res.getFullPath());
//				System.out.println("script:" + scriptFilename);
//				break;
//			case IResourceChangeEvent.PRE_BUILD:
//				System.out.println("Build about to run.");
//				break;
//			case IResourceChangeEvent.POST_BUILD:
//				System.out.println("Build complete.");
//				break;
//			}
//		}
//	}

	private void runScript() {
//		String arg = "script.load_file([";
		String arg = "[";
		List<DatasetInfo> selectedDatasets = getDataSourceViewer().getSelectedDatasets();
		for (DatasetInfo dataset : selectedDatasets) {
			String location = dataset.getLocation();
			location = location.replaceAll("\\\\", "/");
			arg += "'" + location + "', ";
		}
//		arg += "])";
		arg += "]";
		IScriptExecutor executor = getScriptExecutor();
		executor.runScript("__run_script__(" + arg + ")");
	}
	
	private void runDatasetSelectedScript(List<DatasetInfo> datasets) {
		String arg = "[";
		for (DatasetInfo dataset : datasets) {
			String location = dataset.getLocation();
			location = location.replaceAll("\\\\", "/");
			arg += "'" + location + "', ";
		}
		arg += "]";
		IScriptExecutor executor = getScriptExecutor();
		executor.runScript("__dataset_selected__(" + arg + ")");
	}
	
	private void runDatasetAddedScript(DatasetInfo[] datasets) {
		String arg = "[";
		for (DatasetInfo dataset : datasets) {
			String location = dataset.getLocation();
			location = location.replaceAll("\\\\", "/");
			arg += "'" + location + "', ";
		}
		arg += "]";
		IScriptExecutor executor = getScriptExecutor();
		executor.runScript("__dataset_added__(" + arg + ")");
	}
	
	private void runCommand(String command) {
		IScriptExecutor executor = getScriptExecutor();
		executor.runScript(command);
	}
	
//	public void validateScript(String script) {
//		IScriptExecutor executor = getScriptValidator();
//		executor.runScript(script);
//	}
	
	private void runIndependentCommand(String command) {
		IScriptExecutor executor = getScriptExecutor();
		((ScriptExecutor) executor).runIndependentScript(command);
	}
	
	private void handleException(Exception ex, String message) {
//		MessageDialog.openError(getShell(), "Error", message + ": " + ex.getLocalizedMessage());
		ex.printStackTrace();
	}
	
	private void setEditorVisible(boolean isVisible) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (page != null) {
			page.setEditorAreaVisible(isVisible);
		}
	}
	
	private void createDynamicArea() {
		scroll = new ScrolledComposite(this, SWT.V_SCROLL);
		GridLayoutFactory.fillDefaults().applyTo(scroll);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(scroll);
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		ScrollBar scrollBar = scroll.getVerticalBar();
		scrollBar.setIncrement(16);
		scrollBar.setPageIncrement(320);
		dynamicComposite = new Composite(scroll, SWT.BORDER);
		scroll.setContent(dynamicComposite);
		GridLayoutFactory.fillDefaults().margins(2, 2).spacing(4, 4).numColumns(2).applyTo(
				dynamicComposite);
//		RowLayout layout = new RowLayout(SWT.VERTICAL);
//		layout.marginWidth = layout.marginHeight = 2;
//		layout.spacing = 4;
//		dynamicComposite.setLayout(layout);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(dynamicComposite);
		dynamicComposite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		dynamicComposite.setBackgroundMode(SWT.INHERIT_FORCE);
		scroll.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				Rectangle r = scroll.getClientArea();
				scroll.setMinSize(dynamicComposite.computeSize(r.width, SWT.DEFAULT));
			}
		});
	}

	public void initScriptControl(String filePath) throws FileNotFoundException {
		String text = filePath;
		if (filePath.length() > 24) {
			text = filePath.substring(0, 3) + "..." + filePath.substring(filePath.length() - 18);
		}
		if (!staticComposite.isDisposed()) {
			scriptLabel.setText(text);
			scriptLabel.setToolTipText(filePath);
			getRunner().setScriptPath(filePath);
			reloadButton.setEnabled(true);
			showButton.setEnabled(editingEnabled && true);
			runButton.setEnabled(true);
		}
		setScriptFilename(filePath);
		dynamicComposite.setData(filePath);
		scriptModel = new ScriptModel(scriptRegisterID);
		scriptModel.addChangeListener(new ScriptModel.IModelChangeListener() {
			
			@Override
			public void modelChanged() {
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
//						System.err.println("model " + scriptRegisterID + ": " + getScriptFilename() + ", " + dynamicComposite.getData());
						updateControlUI();
					}
				});
			}
		});
		ScriptPageRegister.getRegister(scriptRegisterID).setScriptModel(scriptModel);
		final IScriptExecutor executor = getScriptExecutor();
		if (executor != null) {
			executor.runScript("__script_model_id__ = " + scriptRegisterID);
		}
		
		Thread launchThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					String fn = FileLocator.toFileURL(Activator.getDefault().getBundle().getEntry(PRE_RUN_SCRIPT)).getFile();
					executor.runScript(new FileReader(fn));
					while (executor.isBusy()) {
						Thread.sleep(200);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				String initFile = getScriptFilename();
				if (initFile != null) {
					try {
						FileReader reader = new FileReader(initFile);
						executor.runScript(reader);
						executor.runScript("t = logln( '<' + str(__script__.title) + '> loaded')");
						while(executor.isBusy()) {
							Thread.sleep(200);
						}
					} catch (Exception e) {
						executor.runScript("t = logln( 'failed to load " + initFile + "')");
					}
				}
//				IScriptBlock postBlock = new ScriptBlock();
//				for (String line : POST_RUN_SCRIPT) {
//					postBlock.append(line);
//				}
//				executor.runScript(postBlock);
				try {
					String fn = FileLocator.toFileURL(Activator.getDefault().getBundle().getEntry(POST_RUN_SCRIPT)).getFile();
					FileReader reader = new FileReader(fn);
					executor.runScript(reader);
					while(executor.isBusy()) {
						Thread.sleep(200);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				executor.runScript("time.sleep(0.1)");
				executor.runScript("auto_run()");				
			}
		});
		launchThread.start();
		if (datasetActivityListener == null) {
			datasetActivityListener = new IActivityListener() {
				
				@Override
				public void datasetAdded(DatasetInfo[] datasets) {
					runDatasetAddedScript(datasets);
				}
				
				@Override
				public void runSelected() {
					runScript();
				}
				
				@Override
				public void selectionChanged(
						List<DatasetInfo> selectedDatatsetList) {
					runDatasetSelectedScript(selectedDatatsetList);
				}
			};
			if (getDataSourceViewer() != null) {
				getDataSourceViewer().addActivityListener(datasetActivityListener);
			}
		}

	}
	
	public void updateUI() {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				updateControlUI();
			}
		});
	}
	
	public void updateGroupUI(final ScriptObjectGroup pyGroup) {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				reloadGroup(pyGroup);
			}
		});
	}
	
	private void updateControlUI() {
//		for (Composite composite : groupMap.values()) {
//			composite.dispose();
//		}
		for (int id : groupMap.keySet()) {
			Composite composite = groupMap.get(id);
			composite.dispose();
		}
		
		groupMap.clear();
		for (Control control : dynamicComposite.getChildren()) {
			control.dispose();
		}
		String title = scriptModel.getTitle();
		if (title != "unknown") {
			String version = scriptModel.getVersion();
			if (version != "unknown") {
				title += " " + version;
			}
			if (title.length() > 22) {
				title = title.substring(0, 3) + "..." + title.substring(title.length() - 16);
			}
			if (!scriptLabel.isDisposed()) {
				scriptLabel.setText(title);
			}
		}
		if (scriptModel.getNumColumns() > 0) {
			boolean equalWidth = false;
			try {
				equalWidth = scriptModel.isEqualWidth();
			} catch (Exception e) {
			}
			GridLayoutFactory.fillDefaults().equalWidth(equalWidth).margins(2, 2).spacing(4, 
					4).numColumns(scriptModel.getNumColumns() * 2).applyTo(
					dynamicComposite);
		} else {
			GridLayoutFactory.fillDefaults().margins(2, 2).spacing(4, 
					4).applyTo(dynamicComposite);
		}
		List<ScriptObjectTab> tabs = scriptModel.getTabs();
		List<ScriptObjectGroup> groups = scriptModel.getGroups();
		List<IPyObject> objs = scriptModel.getControlList();
		List<IPyObject> controls = prepareControlList(objs, tabs, groups);
//		if (tabs.size() > 0) {
//			addTabs(dynamicComposite, tabs);
//		}
		boolean tabsAdded = false;
		for (final IPyObject control : controls) {
			if (control.getObjectType() == PyObjectType.PAR) {
				addParameter(dynamicComposite, (ScriptParameter) control);
			} else if (control.getObjectType() == PyObjectType.ACT) {
				addAction(dynamicComposite, (ScriptAction) control);
			} else if (control.getObjectType() == PyObjectType.GROUP) {
				addGroup(dynamicComposite, (ScriptObjectGroup) control);
			} else if (control.getObjectType() == PyObjectType.TAB) {
				if (!tabsAdded) {
					addTabs(dynamicComposite, tabs);
					tabsAdded = true;
				}
			}
		}
		dynamicComposite.layout(true, true);
		dynamicComposite.update();
		dynamicComposite.redraw();
		Rectangle r = scroll.getClientArea();
		scroll.setMinSize(dynamicComposite.computeSize(r.width, SWT.DEFAULT));
		scroll.layout(true, true);
		scroll.update();
		scroll.redraw();
		layout(true, true);
		update();
		redraw();
	}
	
	private List<IPyObject> prepareControlList(List<IPyObject> objs, List<ScriptObjectTab> tabs, 
			List<ScriptObjectGroup> groups) {
		List<IPyObject> list = new ArrayList<IPyObject>();
		for (IPyObject obj : objs) {
//			if (obj instanceof ScriptParameter || obj instanceof ScriptAction 
//					|| obj instanceof ScriptObjectGroup || obj instanceof ScriptObjectTab) {
			if (obj instanceof PyObjectImp) {
				boolean inGroup = false;
				for (ScriptObjectTab tab : tabs) {
					if (tab.getObjectList().contains(obj)) {
						inGroup = true;
						break;
					}
				}
				for (ScriptObjectGroup group : groups) {
					if (group.getObjectList().contains(obj)) {
						inGroup = true;
						break;
					}
				}
				if (!inGroup) {
					list.add(obj);
				}
			} else {
				list.add(obj);
			}
		}
		return list;
	}

	private void addTabs(final Composite parent, final List<ScriptObjectTab> tabs) {
		final Composite tabTitles = new Composite(parent, SWT.NONE);
//		tabTitles.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(tabTitles);
		GridLayoutFactory.fillDefaults().margins(4, 4).spacing(4, 4).applyTo(tabTitles);


//		tabTitles.setBackground(getBackground());
		
		GridLayout gl_cmpTabs = new GridLayout(tabs.size() * 2 + 3, false);
		gl_cmpTabs.marginWidth = 0;
		gl_cmpTabs.marginHeight = 4;
		gl_cmpTabs.horizontalSpacing = 0;
		gl_cmpTabs.verticalSpacing = 0;
		tabTitles.setLayout(gl_cmpTabs);

		final Label barBegin = new Label(tabTitles, SWT.NONE);
//		lblBarBegin.setBackground(getBackground());
		barBegin.setImage(BTN_IMAGE_BEGIN);
		barBegin.pack();

		Label bar0 = new Label(tabTitles, SWT.NONE);
//		lblBar0.setBackground(getBackground());
		bar0.setImage(BTN_IMAGE_BAR);
		bar0.pack();

		final ImageButton[] buttons = new ImageButton[tabs.size()];
		int i = 0;
		for (final ScriptObjectTab tab : tabs) {
			final ImageButton btnUsers = new ImageButton(tabTitles, SWT.NONE);
			buttons[i] = btnUsers;
//			btnUsers.setBackground(getBackground());
			btnUsers.setText(tab.getName());
//			btnUsers.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.BOLD));
			btnUsers.setSelected(i == 0);
			btnUsers.addCircularTriggerArea(25, 25, 21);
			btnUsers.addCircularTriggerArea(76, 25, 21);
			btnUsers.addRectangularTriggerArea(25, 4, 51, 42);

			final MouseListener mouseListener = new MouseListener() {
				
				@Override
				public void mouseUp(MouseEvent e) {
					String command = tab.getProperty("command");
					if (command != null) {
						runIndependentCommand(command);
					}
				}
				
				@Override
				public void mouseDown(MouseEvent e) {
				}
				
				@Override
				public void mouseDoubleClick(MouseEvent e) {
				}
			};
			btnUsers.addMouseListener(mouseListener);
			
			btnUsers.addDisposeListener(new DisposeListener() {
				
				@Override
				public void widgetDisposed(DisposeEvent e) {
					btnUsers.removeMouseListener(mouseListener);
				}
			});
			
			Label bar1 = new Label(tabTitles, SWT.NONE);
			bar1.setImage(BTN_IMAGE_BAR);
			bar1.pack();
			i ++;
		}
		

		Label barEnd = new Label(tabTitles, SWT.NONE);
//		lblBarEnd.setBackground(getBackground());
		barEnd.setImage(BTN_IMAGE_END);
		barEnd.pack();
		
		final ScrolledComposite cmpMain = new ScrolledComposite(parent, SWT.NONE);
		cmpMain.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
//		cmpMain.setBackground(getBackground());
		cmpMain.setExpandHorizontal(true);
		cmpMain.setExpandVertical(true);

		i = 0;
		for (ScriptObjectTab tab : tabs) {
			final Composite tabComposite = new Composite(cmpMain, SWT.BORDER);
			GridLayout mainLayout = new GridLayout(1, false);
			mainLayout.marginWidth = 0;
			mainLayout.marginHeight = 0;
			mainLayout.verticalSpacing = 0;
			mainLayout.horizontalSpacing = 0;
			tabComposite.setLayout(mainLayout);

//			GridLayout gridLayout = new GridLayout(1, false);
//			gridLayout.verticalSpacing = 0;
//			gridLayout.marginWidth = 10;
//			gridLayout.marginHeight = 10;
//			gridLayout.horizontalSpacing = 0;
//			tabComposite.setLayout(gridLayout);

			loadGroup(tabComposite, tab);
			groupMap.put(tab.getId(), tabComposite);
			
			buttons[i].addListener(
					SWT.Selection,
					new ButtonListener(
							cmpMain, tabComposite,
							buttons[i],
							buttons));
			if (i == 0) {
				cmpMain.setContent(tabComposite);
			}
			i ++;
		}
	}
	
	private void addGroup(final Composite parent, final ScriptObjectGroup objGroup) {
		Composite group;
		String hideTitleString = objGroup.getProperty("hideTitle");
		boolean needRefresh = false;
		if (hideTitleString != null && Boolean.valueOf(hideTitleString)) {
			group = new Composite(parent, SWT.BORDER);
		} else {
			group = new MenuBasedGroup(parent, SWT.NONE);
			final MenuBasedGroup menuGroup = (MenuBasedGroup) group;
			menuGroup.setClickExpandEnabled(groupAllowFolding);
			menuGroup.setText(objGroup.getName());
			final Menu menu = menuGroup.getMenu();
//			group.setClickExpandEnabled(false);
			
			String highlightProperty = objGroup.getProperty("highlight");
			String tierProperty = objGroup.getProperty("tier");
			if (highlightProperty != null) {
				boolean isHighlight = Boolean.valueOf(highlightProperty);
				if (isHighlight) {
					RectangleGroupStrategy strategy = (RectangleGroupStrategy) menuGroup.getStrategy();
					strategy.setBackground(
							new Color[]{Display.getCurrent().getSystemColor(SWT.COLOR_RED), 
									Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW)}, 
							new int[]{100});
				} 
			} else {
				if (tierProperty != null) {
					int tier = 1;
					try {
						tier = Integer.valueOf(tierProperty);
					} catch (Exception e) {
					}
					Color color1 = null;
					Color color2 = null;
					if (tier == 2) {
						color1 = Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
						color1 = Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT);
					} else if (tier == 3) {
						color1 = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
						color1 = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
					} else if (tier > 3) {
						color1 = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE);
						color1 = Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);
					}
					if (color1 != null) {
						RectangleGroupStrategy strategy = (RectangleGroupStrategy) menuGroup.getStrategy();
						strategy.setBackground(
								new Color[]{color1, color2}, 
								new int[]{75}, 
								true);
						menuGroup.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
					}
				}
			}
			
			final ExpandListener expandListener = new ExpandListener() {
				
				@Override
				public void itemExpanded(ExpandEvent e) {
					menuGroup.layout(true, true);
					menuGroup.update();
					Rectangle r = scroll.getClientArea();
					scroll.setMinSize(dynamicComposite.computeSize(r.width, SWT.DEFAULT));
				}
				
				@Override
				public void itemCollapsed(ExpandEvent e) {
					menuGroup.layout(true, true);
					menuGroup.update();
					Rectangle r = scroll.getClientArea();
					scroll.setMinSize(dynamicComposite.computeSize(r.width, SWT.DEFAULT));
				}
			};
			menuGroup.addExpandListener(expandListener);
			
			menuGroup.addDisposeListener(new DisposeListener() {
				
				@Override
				public void widgetDisposed(DisposeEvent e) {
					menuGroup.removeExpandListener(expandListener);
				}
			});
			// Set Name
			final MenuItem editItem = new MenuItem(menu, SWT.PUSH);
//			editItem.setText("Fold");
//			editItem.setImage(InternalImage.TEXT_EDIT.getImage());
			editItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (menuGroup.getExpanded()) {
						menuGroup.setExpanded(false);
						editItem.setText("Expand");
					} else {
						menuGroup.setExpanded(true);
						editItem.setText("Fold");
					}
				}
			});
			
			String foldedProperty = objGroup.getProperty("folded");
			boolean itemFolded = false;
			if (foldedProperty != null) {
				try {
					itemFolded = Boolean.valueOf(foldedProperty);
				} catch (Exception e) {
				}
			}
			if (itemFolded) {
				editItem.setText("Expand");
				needRefresh = true;
			} else {
				editItem.setText("Fold");
			}
			
			Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
				public void run() {
//					DataBindingContext bindingContext = new DataBindingContext();
//					bindingContext.bindValue(SWTObservables.observeText(menuGroup),
//							BeansObservables.observeValue(objGroup, "name"),
//							new UpdateValueStrategy(), new UpdateValueStrategy());
					objGroup.addPropertyChangeListener(new PropertyChangeListener() {

						@Override
						public void propertyChange(final PropertyChangeEvent evt) {
							if (evt.getPropertyName().equals("title")) {
								Display.getDefault().asyncExec(new Runnable() {

									@Override
									public void run() {
										if (!menuGroup.isDisposed()) {
											menuGroup.setText(objGroup.getProperty("title"));
										}
									}
								});
							} else if (evt.getPropertyName().equals("folded")) {
								Display.getDefault().asyncExec(new Runnable() {

									@Override
									public void run() {
										String foldedProperty = objGroup.getProperty("folded");
										boolean itemFolded = false;
										if (!menuGroup.isDisposed()) {
											if (foldedProperty != null) {
												try {
													itemFolded = Boolean.valueOf(foldedProperty);
												} catch (Exception e) {
												}
											}
											if (itemFolded) {
												editItem.setText("Expand");
												menuGroup.setExpanded(false);
											} else {
												editItem.setText("Fold");
											}
										}
									}
								});
							} else if (evt.getPropertyName().equals("highlight")) {
								Display.getDefault().asyncExec(new Runnable() {

									@Override
									public void run() {
										boolean isHighlight = Boolean.valueOf(evt.getNewValue().toString());
										if (!menuGroup.isDisposed()) {
											if (isHighlight) {
												RectangleGroupStrategy strategy = (RectangleGroupStrategy) menuGroup.getStrategy();
												strategy.setBackground(
														new Color[]{Display.getCurrent().getSystemColor(SWT.COLOR_RED), 
																Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW)}, 
														new int[]{100});
											} else {
												Color g1 = Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT);
												Color g2 = Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
												RectangleGroupStrategy strategy = (RectangleGroupStrategy) menuGroup.getStrategy();
												strategy.setBackground(new Color[] {g1, g2 }, new int[] {100 }, true);
											}
										}
									}
								});
							} 
						}
					});
				}
			});

		}

		loadGroup(group, objGroup);
		if (needRefresh) {
			((MenuBasedGroup) group).setExpanded(false);
			group.layout(true, true);
		}
		groupMap.put(objGroup.getId(), group);
	}
	
	private void reloadGroup(final ScriptObjectGroup objGroup) {
		if (groupMap.containsKey(objGroup.getId())) {
			Composite group = groupMap.get(objGroup.getId());
			for (Control child : group.getChildren()) {
				child.dispose();
			}
			loadGroup(group, objGroup);
//			group.layout(true, true);
//			group.update();
//			group.redraw();
			Rectangle r = scroll.getClientArea();
			scroll.setMinSize(dynamicComposite.computeSize(r.width, SWT.DEFAULT));
			scroll.layout(true, true);
			scroll.update();
			scroll.redraw();
//			layout(true);
//			update();
//			redraw();
		}
	}
	
	private void loadGroup(final Composite group, final ScriptObjectGroup objGroup) {
//		final MenuBasedGroup group = new MenuBasedGroup(parent, SWT.NONE);
		int groupNumColumns = 1;
		if (objGroup.getProperty("numColumns") != null) {
			try {
				groupNumColumns = Integer.valueOf(objGroup.getProperty("numColumns"));
			} catch (Exception e) {
			}
		}
		if (groupNumColumns < 1) {
			groupNumColumns = 1;
		}
		boolean isEqualWidth = false;
		try {
			isEqualWidth = Boolean.valueOf(objGroup.getProperty("equalWidth"));
		} catch (Exception e) {
		}
		GridLayoutFactory.fillDefaults().equalWidth(isEqualWidth).numColumns(groupNumColumns * 2).margins(2, 2).spacing(2, 2).applyTo(group);
		int groupColspan = 1;
		if (objGroup.getProperty("colspan") != null) {
			try {
				groupColspan = Integer.valueOf(objGroup.getProperty("colspan"));
			} catch (Exception e) {
			}
		}
		if (groupColspan < 1) {
			groupColspan = 1;
		}
		int groupRowspan = 1;
		if (objGroup.getProperty("rowspan") != null) {
			try {
				groupRowspan = Integer.valueOf(objGroup.getProperty("rowspan"));
			} catch (Exception e) {
			}
		}
		if (groupRowspan < 1) {
			groupRowspan = 1;
		}

		GridDataFactory.fillDefaults().grab(true, false).span(groupColspan * 2, groupRowspan).applyTo(group);
		group.setBackgroundMode(SWT.INHERIT_DEFAULT);
		
		List<ScriptObjectTab> tabs = objGroup.getTabList();
//		if (tabs.size() > 0) {
//			addTabs(group, tabs);
//		}
		List<IPyObject> controls = objGroup.getObjectList();
		boolean tabsAdded = false;
		for (final IPyObject control : controls) {
			if (control.getObjectType() == PyObjectType.PAR) {
				addParameter(group, (ScriptParameter) control);
			} else if (control.getObjectType() == PyObjectType.ACT) {
				addAction(group, (ScriptAction) control);
			} else if (control.getObjectType() == PyObjectType.GROUP) {
				addGroup(group, (ScriptObjectGroup) control);
			} else if (control.getObjectType() == PyObjectType.TAB) {
				if (!tabsAdded) {
					addTabs(group, tabs);
					tabsAdded = true;
				}
			}
		}
	}

	private void addAction(Composite parent, final ScriptAction action) {
		
		int width = SWT.DEFAULT;
		int height = SWT.DEFAULT;
		String widthProperty = action.getProperty("width");
		if (widthProperty != null) {
			width = Integer.valueOf(widthProperty);
		}
		String heightProperty = action.getProperty("height");
		if (heightProperty != null) {
			height = Integer.valueOf(heightProperty);
		}

		int actionColspan = 1;
		if (action.getProperty("colspan") != null) {
			try {
				actionColspan = Integer.valueOf(action.getProperty("colspan"));
			} catch (Exception e) {
			}
		}
		if (actionColspan < 1) {
			actionColspan = 1;
		}
		int actionRowspan = 1;
		if (action.getProperty("rowspan") != null) {
			try {
				actionRowspan = Integer.valueOf(action.getProperty("rowspan"));
			} catch (Exception e) {
			}
		}
		if (actionRowspan < 1) {
			actionRowspan = 1;
		}

		boolean independent = false;
		if (action.getProperty("independent") != null) {
			try {
				independent = Boolean.valueOf(action.getProperty("independent"));
			} catch (Exception e) {
			}
		}
		final boolean isIndependent = independent;
//		Label name = new Label(parent, SWT.RIGHT);
//		name.setText(action.getName());
//		name.setText("");
//		GridDataFactory.fillDefaults().grab(false, false).indent(0, 5).minSize(40, 0).span(actionColspan, actionRowspan).applyTo(name);
		int buttonType = SWT.PUSH;
		if (action.getProperty("type") != null) {
			if (action.getProperty("type").toUpperCase().equals("TOGGLE")){
				buttonType = SWT.TOGGLE;
			}
		}
		final Button actionButton = new Button(parent, buttonType);
		actionButton.setText(String.valueOf(action.getText()));
		String tooltip = action.getProperty("tool_tip");
		if (tooltip !=null) {
			actionButton.setToolTipText(tooltip);
		}
		
		boolean enabled = true;
		String enabledProperty = action.getProperty("enabled");
		if (enabledProperty != null) {
			enabled = Boolean.valueOf(enabledProperty);
		}
		if (!enabled) {
			actionButton.setEnabled(false);
		}
//		actionButton.setBackground(new Color(Display.getDefault(), 240, 240, 255));
//		actionButton.setForeground(new Color(Display.getDefault(), 0, 0, 255));
		GridDataFactory.fillDefaults().grab(true, false).span(actionColspan * 2, actionRowspan
				).minSize(0, 32).hint(width, height).applyTo(actionButton);
		if (action.getProperty("highlight") != null) {
			boolean isHighlight = Boolean.valueOf(action.getProperty("highlight"));
			if (isHighlight) {
				actionButton.setBackground(highlightColor);
			}
		}
		actionButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String command = action.getCommand();
				if (command != null) {
//					currentButton = actionButton;
//					actionButton.setImage(InternalImage.BUSY_STATUS_16.getImage());
//					actionButton.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE));
//					actionButton.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED));
//					runCommand(command);
//					runCommand("__UI__.setActionStatusDone()");
//					runCommand(action.getName() + ".__run__(" + action.getCommand() + ")");
//					runCommand(action.getName() + ".set_running_status()");
					if (isIndependent) {
						runIndependentCommand("run_action(" + action.getName() + ")");						
					} else {
						runCommand("run_action(" + action.getName() + ")");
					}
//					runCommand(action.getName() + ".set_done_status()");
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		final IActionStatusListener statusListener = new IActionStatusListener() {
			
			@Override
			public void statusChanged(final ActionStatus newStatus) {
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						if (actionButton.isDisposed()) {
							return;
						}
						switch (newStatus) {
						case RUNNING:
							actionButton.setImage(InternalImage.BUSY_STATUS_12.getImage());
							actionButton.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE));
							actionButton.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED));
							break;
						case ERROR:
							actionButton.setImage(InternalImage.INTERRUPT_STATUS_12.getImage());
							actionButton.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
							actionButton.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
							break;
						case INTERRUPT:
							actionButton.setImage(InternalImage.INTERRUPT_STATUS_12.getImage());
							actionButton.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
							actionButton.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
							break;
						case DONE:
							actionButton.setImage(InternalImage.DONE_STATUS_12.getImage());
							actionButton.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
							actionButton.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
							break;
						default :
							actionButton.setImage(null);
							actionButton.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
							actionButton.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
							break;
						}
					}
				});
			}
		};
		action.addStatusListener(statusListener);
		
		final PropertyChangeListener propertyListener = new PropertyChangeListener() {

			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				if (evt.getPropertyName().toLowerCase().equals("enabled")) {
					Display.getDefault().asyncExec(new Runnable() {

						@Override
						public void run() {
							if (!actionButton.isDisposed()) {
								actionButton.setEnabled(Boolean.valueOf(evt.getNewValue().toString()));
							}
						}
					});
				} else if (evt.getPropertyName().toLowerCase().equals("tool_tip")) {
					Display.getDefault().asyncExec(new Runnable() {

						@Override
						public void run() {
							if (!actionButton.isDisposed()) {
								if (evt.getNewValue() != null && !evt.getNewValue().equals("None") 
										&& evt.getNewValue().toString().trim().length() > 0) {
									actionButton.setToolTipText(evt.getNewValue().toString());
								} else {
									actionButton.setToolTipText(null);
								}
							}
						}
					});
				} else if (evt.getPropertyName().toLowerCase().equals("highlight")) {
					Display.getDefault().asyncExec(new Runnable() {

						@Override
						public void run() {
							if (!actionButton.isDisposed()) {
								boolean isHighlight = Boolean.valueOf(evt.getNewValue().toString());
								if (isHighlight) {
									actionButton.setBackground(highlightColor);
								} else {
									actionButton.setBackground(defaultColor);
								}
							}
						}
					});
				} else if (evt.getPropertyName().toLowerCase().equals("title")) {
					Display.getDefault().asyncExec(new Runnable() {

						@Override
						public void run() {
							if (!actionButton.isDisposed()) {
								if (evt.getNewValue() != null && !evt.getNewValue().equals("None") 
										&& evt.getNewValue().toString().trim().length() > 0) {
									actionButton.setText(evt.getNewValue().toString());
								} else {
									actionButton.setText(action.getText());
								}
							}
						}
					});
				} else if (evt.getPropertyName().toLowerCase().equals("selected")) {
					Display.getDefault().asyncExec(new Runnable() {

						@Override
						public void run() {
							if (!actionButton.isDisposed()) {
								boolean isSelected = Boolean.valueOf(evt.getNewValue().toString());
								if (isSelected) {
									actionButton.setSelection(true);
								} else {
									actionButton.setSelection(false);
								}
							}
						}
					});
				} 
			}
		};
		action.addPropertyChangeListener(propertyListener);
		
		actionButton.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				action.removeStatusListener(statusListener);
				action.removePropertyChangeListener(propertyListener);
			}
		});
		
	}

	private void addParameter(Composite parent, final ScriptParameter parameter) {
		String enabledProperty = parameter.getProperty("enabled");
		boolean itemEnabled = true;
		if (enabledProperty != null) {
			try {
				itemEnabled = Boolean.valueOf(enabledProperty);
			} catch (Exception e) {
			}
		}
		String highlightProperty = parameter.getProperty("highlight");
		boolean isHighlight = false;
		if (highlightProperty != null) {
			isHighlight = Boolean.valueOf(highlightProperty);
		}
		int parameterColspan = 1;
		if (parameter.getProperty("colspan") != null) {
			try {
				parameterColspan = Integer.valueOf(parameter.getProperty("colspan"));
			} catch (Exception e) {
			}
		}
		if (parameterColspan < 1) {
			parameterColspan = 1;
		}
		int parameterRowspan = 1;
		if (parameter.getProperty("rowspan") != null) {
			try {
				parameterRowspan = Integer.valueOf(parameter.getProperty("rowspan"));
			} catch (Exception e) {
			}
		}
		if (parameterRowspan < 1) {
			parameterRowspan = 1;
		}

		int width = SWT.DEFAULT;
		int height = SWT.DEFAULT;
		String widthProperty = parameter.getProperty("width");
		if (widthProperty != null) {
			width = Integer.valueOf(widthProperty);
		}
		String heightProperty = parameter.getProperty("height");
		if (heightProperty != null) {
			height = Integer.valueOf(heightProperty);
		}
//		int hAlign = SWT.FILL;
//		String hAlignProperty = parameter.getProperty("h_align");
//		if (hAlignProperty != null) {
//			try {
//				hAlign = SWT.class.getField(hAlignProperty.toUpperCase()).getInt(null);
//			} catch (Exception e) {
//			} 
//		}
//		int vAlign = SWT.CENTER;
//		String vAlignProperty = parameter.getProperty("v_align");
//		if (vAlignProperty != null) {
//			try {
//				vAlign = SWT.class.getField(vAlignProperty.toUpperCase()).getInt(null);
//			} catch (Exception e) {
//			} 
//		}
		boolean fillWidth = true;
		String fillWidthProperty = parameter.getProperty("h_fill");
		if (fillWidthProperty != null) {
			fillWidth = Boolean.valueOf(fillWidthProperty);
		}
		boolean fillHeight = false;
		String fillHeightProperty = parameter.getProperty("v_fill");
		if (fillHeightProperty != null) {
			fillHeight = Boolean.valueOf(fillHeightProperty);
		}
		int labelWidth = 40;
		
		if (parameter.getOptions() != null && parameter.getType() != PType.LIST) {
			final Label name = new Label(parent, SWT.RIGHT);
			if (parameter.getProperty("title") != null) {
				name.setText(parameter.getProperty("title"));
			} else {
				name.setText(parameter.getName());
			}
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).indent(0, 3).minSize(labelWidth, 0).align(SWT.END, SWT.CENTER).span(parameterColspan, parameterRowspan).applyTo(name);
			final ComboViewer comboBox = new ComboViewer(parent, SWT.DROP_DOWN);
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(fillWidth, fillHeight).span(parameterColspan, parameterRowspan).hint(width, height).applyTo(comboBox.getControl());
			comboBox.setContentProvider(new ArrayContentProvider());
			comboBox.setLabelProvider(new LabelProvider());
//			comboBox.setSelection(new StructuredSelection(parameter.getValue()));
			comboBox.getCombo().setEnabled(itemEnabled);
			//				comboBox.setSorter(new ViewerSorter());
			comboBox.setInput(parameter.getOptions());
			if (isHighlight) {
				comboBox.getCombo().setForeground(highlightColor);
			} 			

			Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
				public void run() {
					DataBindingContext bindingContext = new DataBindingContext();
					bindingContext.bindValue(ViewersObservables.observeSingleSelection(comboBox),
							BeansObservables.observeValue(parameter, "value"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
					bindingContext = new DataBindingContext();
					bindingContext.bindValue(SWTObservables.observeText(comboBox.getCombo()),
							BeansObservables.observeValue(parameter, "value"),
							new UpdateValueStrategy(), new UpdateValueStrategy());
				}
			});
			
			final PropertyChangeListener propertyListener = new PropertyChangeListener() {

				@Override
				public void propertyChange(final PropertyChangeEvent evt) {
					if (evt.getPropertyName().equals("options")) {
						Display.getDefault().asyncExec(new Runnable() {

							@Override
							public void run() {
								if (!comboBox.getCombo().isDisposed()) {
									Object value = parameter.getValue();
									comboBox.setInput(parameter.getOptions());
									comboBox.refresh();
									if (value == null) {
										comboBox.setSelection(null);
									} else {
										comboBox.setSelection(new StructuredSelection(
												value));
									}
								}
							}
						});
					} else if (evt.getPropertyName().equals("enabled")) {
						Display.getDefault().asyncExec(new Runnable() {

							@Override
							public void run() {
								if (!comboBox.getCombo().isDisposed()) {
									comboBox.getCombo().setEnabled(Boolean.valueOf(evt.getNewValue().toString()));
								}
							}
						});
					} else if (evt.getPropertyName().equals("highlight")) {
						Display.getDefault().asyncExec(new Runnable() {

							@Override
							public void run() {
								if (!comboBox.getCombo().isDisposed()) {
									boolean isHighlight = Boolean.valueOf(evt.getNewValue().toString());
									if (isHighlight) {
										comboBox.getCombo().setForeground(highlightColor);
									} else {
										comboBox.getCombo().setForeground(defaultColor);
									}
								}
							}
						});
					}
				}
			};
			parameter.addPropertyChangeListener(propertyListener);
			
			comboBox.addPostSelectionChangedListener(new ISelectionChangedListener() {
				
				Object selection = ((IStructuredSelection) comboBox.getSelection()).getFirstElement();
				
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					
					Object newSelection = ((IStructuredSelection) comboBox.getSelection()).getFirstElement();
					if ((newSelection == null && selection != null) || (newSelection != null && !newSelection.equals(selection))) {
						String command = parameter.getCommand();
						if (command != null) {
							runIndependentCommand(command);
						}
						selection = newSelection;
					}
				}
				
//				long timeStamp = 0;
//				
//				@Override
//				public void selectionChanged(SelectionChangedEvent event) {
//					comboBox.getSelection();
//					long newStamp = System.currentTimeMillis();
//					if (newStamp - timeStamp > 500) {
//						String command = parameter.getCommand();
//						if (command != null) {
//							runIndependentCommand(command);
//						}
//						timeStamp = newStamp;
//					}
//				}
			});
			comboBox.getCombo().addDisposeListener(new DisposeListener() {
				
				@Override
				public void widgetDisposed(DisposeEvent e) {
					parameter.removePropertyChangeListener(propertyListener);
				}
			});
			
			comboBox.getCombo().addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent e) {
				}
				
				@Override
				public void focusGained(FocusEvent e) {
					String focusCommand = parameter.getProperty("focus");
					if (focusCommand != null) {
						runIndependentCommand(focusCommand);
					}
				}
			});
			
			comboBox.getCombo().addListener(SWT.MouseVerticalWheel, new Listener() {

				@Override
				public void handleEvent(Event arg0)
				{
					arg0.doit = false;
				}
			});
			
		} else {
			PType type = parameter.getType();
			switch (type) {
			case STRING:
				Label name = new Label(parent, SWT.RIGHT);
				if (parameter.getProperty("title") != null) {
					name.setText(parameter.getProperty("title"));
				} else {
					name.setText(parameter.getName());
				}
				GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).indent(0, 3).minSize(labelWidth, 0).align(SWT.END, SWT.CENTER).span(parameterColspan, parameterRowspan).applyTo(name);
				int vHeight = SWT.DEFAULT;
				try {
					String heightString = parameter.getProperty("height");
					if (heightString != null) {
						vHeight = Integer.valueOf(heightString);
					}
				} catch (Exception e) {
				}
				int textType = SWT.BORDER;
				if (vHeight != SWT.DEFAULT) {
					textType = SWT.BORDER | SWT.MULTI | SWT.V_SCROLL;
				}
				final Text stringText = new Text(parent, textType);
				stringText.setText(String.valueOf(parameter.getValue()));
				GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(fillWidth, fillHeight).span(parameterColspan, parameterRowspan).hint(width, vHeight).applyTo(stringText);
				stringText.setEditable(itemEnabled);
				if (isHighlight) {
					stringText.setForeground(highlightColor);
				} 	
				Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
					public void run() {
						DataBindingContext bindingContext = new DataBindingContext();
						bindingContext.bindValue(SWTObservables.observeText(stringText, SWT.Modify),
								BeansObservables.observeValue(parameter, "value"),
								new FlagedUpdateStrategy(), new UpdateValueStrategy());
					}
				});
				final PropertyChangeListener stringPropertyListener = new PropertyChangeListener() {

					@Override
					public void propertyChange(final PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals("enabled")) {
							Display.getDefault().asyncExec(new Runnable() {

								@Override
								public void run() {
									stringText.setEditable(Boolean.valueOf(evt.getNewValue().toString()));
								}
							});
						} else if (evt.getPropertyName().equals("highlight")) {
							Display.getDefault().asyncExec(new Runnable() {

								@Override
								public void run() {
									boolean isHighlight = Boolean.valueOf(evt.getNewValue().toString());
									if (isHighlight) {
										stringText.setForeground(highlightColor);
									} else {
										stringText.setForeground(defaultColor);
									}
								}
							});
						} 
					}
				};
				parameter.addPropertyChangeListener(stringPropertyListener);
				stringText.addDisposeListener(new DisposeListener() {
					
					@Override
					public void widgetDisposed(DisposeEvent e) {
						parameter.removePropertyChangeListener(stringPropertyListener);
					}
				});
//				stringText.addModifyListener(new ModifyListener() {
//					
//					@Override
//					public void modifyText(ModifyEvent e) {
//						String command = parameter.getCommand();
//						if (command != null) {
//							runIndependentCommand(command);
//						}
//					}
//				});
				stringText.addKeyListener(new KeyListener() {
					
					@Override
					public void keyReleased(KeyEvent e) {
					}
					
					@Override
					public void keyPressed(KeyEvent e) {
						if (e.keyCode == SWT.CR || e.keyCode == SWT.LF || e.keyCode == 16777296) {
							if (parameter.getDirtyFlag()){
								String command = parameter.getCommand();
								if (command != null) {
									runIndependentCommand(command);
								}
								parameter.resetDirtyFlag();
							}
						} 
					}
				});
				stringText.addFocusListener(new FocusListener() {
					
					@Override
					public void focusLost(FocusEvent e) {
						if (parameter.getDirtyFlag()){
							String command = parameter.getCommand();
							if (command != null) {
								runIndependentCommand(command);
							}
							parameter.resetDirtyFlag();
						}
					}
					
					@Override
					public void focusGained(FocusEvent e) {
						String focusCommand = parameter.getProperty("focus");
						if (focusCommand != null) {
							runIndependentCommand(focusCommand);
						}
					}
				});
				
				break;
			case INT :
				name = new Label(parent, SWT.RIGHT);
				if (parameter.getProperty("title") != null) {
					name.setText(parameter.getProperty("title"));
				} else {
					name.setText(parameter.getName());
				}
				GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).indent(0, 3).minSize(labelWidth, 0).align(SWT.END, SWT.CENTER)
					.span(parameterColspan, parameterRowspan).applyTo(name);
				final Text intText = new Text(parent, SWT.BORDER);
				intText.setText(String.valueOf(parameter.getValue()));
				GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(fillWidth, fillHeight).hint(width, height).span(parameterColspan, parameterRowspan).applyTo(intText);
				intText.setEditable(itemEnabled);
				if (isHighlight) {
					intText.setForeground(highlightColor);
				} 
				Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
					public void run() {
						DataBindingContext bindingContext = new DataBindingContext();
						bindingContext.bindValue(SWTObservables.observeText(intText, SWT.Modify),
								BeansObservables.observeValue(parameter, "value"),
								new FlagedUpdateStrategy(), new UpdateValueStrategy());
					}
				});
				final PropertyChangeListener intPropertyListener = new PropertyChangeListener() {

					@Override
					public void propertyChange(final PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals("enabled")) {
							Display.getDefault().asyncExec(new Runnable() {

								@Override
								public void run() {
									if (!intText.isDisposed()) {
										intText.setEditable(Boolean.valueOf(evt.getNewValue().toString()));
									}
								}
							});
						} else if (evt.getPropertyName().equals("highlight")) {
							Display.getDefault().asyncExec(new Runnable() {

								@Override
								public void run() {
									if (!intText.isDisposed()) {
										boolean isHighlight = Boolean.valueOf(evt.getNewValue().toString());
										if (isHighlight) {
											intText.setForeground(highlightColor);
										} else {
											intText.setForeground(defaultColor);
										}
									}
								}
							});
						} 
					}
				};
				parameter.addPropertyChangeListener(intPropertyListener);
				intText.addDisposeListener(new DisposeListener() {
					
					@Override
					public void widgetDisposed(DisposeEvent e) {
						parameter.removePropertyChangeListener(intPropertyListener);
					}
				});
//				intText.addModifyListener(new ModifyListener() {
//					
//					@Override
//					public void modifyText(ModifyEvent e) {
//						String command = parameter.getCommand();
//						if (command != null) {
//							runIndependentCommand(command);
//						}
//					}
//				});
				intText.addKeyListener(new KeyListener() {
					
					@Override
					public void keyReleased(KeyEvent e) {
					}
					
					@Override
					public void keyPressed(KeyEvent e) {
						if (e.keyCode == SWT.CR || e.keyCode == SWT.LF || e.keyCode == 16777296) {
							if (parameter.getDirtyFlag()){
								String command = parameter.getCommand();
								if (command != null) {
									runIndependentCommand(command);
								}
								parameter.resetDirtyFlag();
							}
						} 
					}
				});
				intText.addFocusListener(new FocusListener() {
					
					@Override
					public void focusLost(FocusEvent e) {
						if (parameter.getDirtyFlag()){
							String command = parameter.getCommand();
							if (command != null) {
								runIndependentCommand(command);
							}
							parameter.resetDirtyFlag();
						}
					}
					
					@Override
					public void focusGained(FocusEvent e) {
						String focusCommand = parameter.getProperty("focus");
						if (focusCommand != null) {
							runIndependentCommand(focusCommand);
						}
					}
				});
				break;
			case FLOAT :
				name = new Label(parent, SWT.RIGHT);
				if (parameter.getProperty("title") != null) {
					name.setText(parameter.getProperty("title"));
				} else {
					if (parameter.getName() != null) {
						name.setText(parameter.getName());
					} else {
						name.setText("");
					}
				}
				GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).indent(0, 3).minSize(labelWidth, 0).align(SWT.END, SWT.CENTER)
					.span(parameterColspan, parameterRowspan).applyTo(name);
				final Text floatText = new Text(parent, SWT.BORDER);
				floatText.setText(String.valueOf(parameter.getValue()));
				GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(fillWidth, fillHeight).span(parameterColspan, parameterRowspan).hint(width, height).applyTo(floatText);
				floatText.setEditable(itemEnabled);
				if (isHighlight) {
					floatText.setForeground(highlightColor);
				} 
				Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
					public void run() {
						DataBindingContext bindingContext = new DataBindingContext();
						bindingContext.bindValue(SWTObservables.observeText(floatText, SWT.Modify),
								BeansObservables.observeValue(parameter, "value"),
								new FlagedUpdateStrategy(), new UpdateValueStrategy());
					}
				});
//				floatText.addModifyListener(new ModifyListener() {
//					
//					@Override
//					public void modifyText(ModifyEvent e) {
//						String command = parameter.getCommand();
//						if (command != null) {
//							runIndependentCommand(command);
//						}
//					}
//				});
				final PropertyChangeListener floatPropertyListener = new PropertyChangeListener() {

					@Override
					public void propertyChange(final PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals("enabled")) {
							Display.getDefault().asyncExec(new Runnable() {

								@Override
								public void run() {
									if (!floatText.isDisposed()) {
										floatText.setEditable(Boolean.valueOf(evt.getNewValue().toString()));
									}
								}
							});
						} else if (evt.getPropertyName().equals("highlight")) {
							Display.getDefault().asyncExec(new Runnable() {

								@Override
								public void run() {
									if (!floatText.isDisposed()) {
										boolean isHighlight = Boolean.valueOf(evt.getNewValue().toString());
										if (isHighlight) {
											floatText.setForeground(highlightColor);
										} else {
											floatText.setForeground(defaultColor);
										}
									}
								}
							});
						} 
					}
				};
				parameter.addPropertyChangeListener(floatPropertyListener);
				floatText.addDisposeListener(new DisposeListener() {
					
					@Override
					public void widgetDisposed(DisposeEvent e) {
						parameter.removePropertyChangeListener(floatPropertyListener);
					}
				});
				floatText.addKeyListener(new KeyListener() {
					
					@Override
					public void keyReleased(KeyEvent e) {
					}
					
					@Override
					public void keyPressed(KeyEvent e) {
						if (e.keyCode == SWT.CR || e.keyCode == SWT.LF || e.keyCode == 16777296) {
							if (parameter.getDirtyFlag()){
								String command = parameter.getCommand();
								if (command != null) {
									runIndependentCommand(command);
								}
								parameter.resetDirtyFlag();
							}
						} 
					}
				});
				floatText.addFocusListener(new FocusListener() {
					
					@Override
					public void focusLost(FocusEvent e) {
						if (parameter.getDirtyFlag()){
							String command = parameter.getCommand();
							if (command != null) {
								runIndependentCommand(command);
							}
							parameter.resetDirtyFlag();
						}
					}
					
					@Override
					public void focusGained(FocusEvent e) {
						String focusCommand = parameter.getProperty("focus");
						if (focusCommand != null) {
							runIndependentCommand(focusCommand);
						}
					}
				});
				break;
			case BOOL :
				name = new Label(parent, SWT.RIGHT);
				if (parameter.getProperty("title") != null) {
					name.setText(parameter.getProperty("title"));
				} else {
					name.setText(parameter.getName());
				}
				GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).minSize(40, SWT.DEFAULT).align(SWT.END, SWT.CENTER).span(parameterColspan, parameterRowspan).applyTo(name);
				final Button selectBox = new Button(parent, SWT.CHECK);
				selectBox.setSelection(Boolean.valueOf(String.valueOf(parameter.getValue())));
				GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(fillWidth, fillHeight).span(parameterColspan, parameterRowspan).applyTo(selectBox);
				selectBox.setEnabled(itemEnabled);
				if (isHighlight) {
					selectBox.setForeground(highlightColor);
				}
				Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
					public void run() {
						DataBindingContext bindingContext = new DataBindingContext();
						bindingContext.bindValue(SWTObservables.observeSelection(selectBox),
								BeansObservables.observeValue(parameter, "value"),
								new FlagedUpdateStrategy(), new UpdateValueStrategy());
					}
				});
				final PropertyChangeListener boolPropertyListener = new PropertyChangeListener() {

					@Override
					public void propertyChange(final PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals("enabled")) {
							Display.getDefault().asyncExec(new Runnable() {

								@Override
								public void run() {
									if (!selectBox.isDisposed()) {
										selectBox.setEnabled(Boolean.valueOf(evt.getNewValue().toString()));
									}
								}
							});
						} else if (evt.getPropertyName().equals("highlight")) {
							Display.getDefault().asyncExec(new Runnable() {

								@Override
								public void run() {
									if (!selectBox.isDisposed()) {
										boolean isHighlight = Boolean.valueOf(evt.getNewValue().toString());
										if (isHighlight) {
											selectBox.setForeground(highlightColor);
										} else {
											selectBox.setForeground(defaultColor);
										}
									}
								}
							});
						} 
					}
				};
				parameter.addPropertyChangeListener(boolPropertyListener);
				selectBox.addDisposeListener(new DisposeListener() {
					
					@Override
					public void widgetDisposed(DisposeEvent e) {
						parameter.removePropertyChangeListener(boolPropertyListener);
					}
				});
				
				selectBox.addSelectionListener(new SelectionListener() {
					
					@Override
					public void widgetSelected(SelectionEvent e) {
						String command = parameter.getCommand();
						if (command != null) {
							runIndependentCommand(command);
						}
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});
				break;
			case FILE:
				name = new Label(parent, SWT.RIGHT);
				if (parameter.getProperty("title") != null) {
					name.setText(parameter.getProperty("title"));
				} else {
					name.setText(parameter.getName());
				}
				GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).indent(0, 3).minSize(labelWidth, 0).align(SWT.END, SWT.CENTER)
					.span(parameterColspan, parameterRowspan).applyTo(name);
				Composite fileComposite = new Composite(parent, SWT.NONE);
				GridLayoutFactory.fillDefaults().numColumns(2).applyTo(fileComposite);
				GridDataFactory.fillDefaults().grab(true, false).span(parameterColspan, parameterRowspan).hint(width, height).applyTo(fileComposite);
				final Text fileText = new Text(fileComposite, SWT.BORDER);
				String itemText = String.valueOf(parameter.getValue());
				fileText.setText(itemText);
				fileText.setToolTipText(itemText);
				fileText.setEditable(itemEnabled);
				if (isHighlight) {
					fileText.setForeground(highlightColor);
				} 
				GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(fillWidth, fillHeight).applyTo(fileText);
				Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
					public void run() {
						DataBindingContext bindingContext = new DataBindingContext();
						bindingContext.bindValue(SWTObservables.observeText(fileText, SWT.Modify),
								BeansObservables.observeValue(parameter, "value"),
								new FlagedUpdateStrategy(), new UpdateValueStrategy());
					}
				});
				final PropertyChangeListener filePropertyListener = new PropertyChangeListener() {

					@Override
					public void propertyChange(final PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals("enabled")) {
							Display.getDefault().asyncExec(new Runnable() {

								@Override
								public void run() {
									if (!fileText.isDisposed()) {
										fileText.setEditable(Boolean.valueOf(evt.getNewValue().toString()));
									}
								}
							});
						} else if (evt.getPropertyName().equals("highlight")) {
							Display.getDefault().asyncExec(new Runnable() {

								@Override
								public void run() {
									if (!fileText.isDisposed()) {
										boolean isHighlight = Boolean.valueOf(evt.getNewValue().toString());
										if (isHighlight) {
											fileText.setForeground(highlightColor);
										} else {
											fileText.setForeground(defaultColor);
										}
									}
								}
							});
						} 
					}
				};
				parameter.addPropertyChangeListener(filePropertyListener);
				fileText.addDisposeListener(new DisposeListener() {
					
					@Override
					public void widgetDisposed(DisposeEvent e) {
						parameter.removePropertyChangeListener(filePropertyListener);
					}
				});
				final Button fileLocatorButton = new Button(fileComposite, SWT.PUSH);
				fileLocatorButton.setText(">>");
				GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).applyTo(fileLocatorButton);
				fileLocatorButton.setToolTipText("click to locate the " + parameter.getName());
				fileLocatorButton.addSelectionListener(new SelectionListener() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						String dialogTypeString = parameter.getProperty("dtype");
						if (dialogTypeString == null) {
							dialogTypeString = "single";
						}
						if (dialogTypeString.toLowerCase().matches("multi")) {
							FileDialog dialog = new FileDialog(getShell(), SWT.MULTI);
							if (ScriptDataSourceViewer.fileDialogPath == null){
								IWorkspace workspace= ResourcesPlugin.getWorkspace();
								IWorkspaceRoot root = workspace.getRoot();
								dialog.setFilterPath(root.getLocation().toOSString());
							} else {
								dialog.setFilterPath(ScriptDataSourceViewer.fileDialogPath);
							}
							String ext = parameter.getProperty("ext");
							if (ext != null) {
								dialog.setFilterExtensions(ext.split(","));
							} else {
								dialog.setFilterExtensions(new String[]{"*.*"});
							}
							dialog.open();
							String[] filenames = dialog.getFileNames();
							if (filenames == null || filenames.length == 0) {
								return;
							}
							String filePath = "";
							for (String filename : filenames) {
								filePath += dialog.getFilterPath() + File.separator + filename + ";";
							}
							if (filePath != null) {
								fileText.setText(filePath);
								fileText.setToolTipText(filePath);
							}
						} else if (dialogTypeString.toLowerCase().matches("save")){
							FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
							if (ScriptDataSourceViewer.fileDialogPath == null){
								IWorkspace workspace= ResourcesPlugin.getWorkspace();
								IWorkspaceRoot root = workspace.getRoot();
								dialog.setFilterPath(root.getLocation().toOSString());
							} else {
								dialog.setFilterPath(ScriptDataSourceViewer.fileDialogPath);
							}
							String ext = parameter.getProperty("ext");
							if (ext != null) {
								dialog.setFilterExtensions(ext.split(","));
							} else {
								dialog.setFilterExtensions(new String[]{"*.*"});
							}
							dialog.open();
							if (dialog.getFileName() == null || dialog.getFileName().trim().length() == 0) {
								return;
							}
							String filePath = dialog.getFilterPath() + File.separator + dialog.getFileName();
							if (filePath != null) {
								fileText.setText(filePath);
								fileText.setToolTipText(filePath);
							}
						} else if (dialogTypeString.toLowerCase().matches("folder") 
								|| dialogTypeString.toLowerCase().matches("dir")){
							DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SINGLE);
							if (ScriptDataSourceViewer.fileDialogPath == null){
								IWorkspace workspace= ResourcesPlugin.getWorkspace();
								IWorkspaceRoot root = workspace.getRoot();
								dialog.setFilterPath(root.getLocation().toOSString());
							} else {
								dialog.setFilterPath(ScriptDataSourceViewer.fileDialogPath);
							}
							String filePath = dialog.open();
							if (filePath == null || filePath.trim().length() == 0) {
								return;
							}
							fileText.setText(filePath);
							fileText.setToolTipText(filePath);
							ScriptDataSourceViewer.fileDialogPath = dialog.getFilterPath();
						} else {
							FileDialog dialog = new FileDialog(getShell(), SWT.SINGLE);
							if (ScriptDataSourceViewer.fileDialogPath == null){
								IWorkspace workspace= ResourcesPlugin.getWorkspace();
								IWorkspaceRoot root = workspace.getRoot();
								dialog.setFilterPath(root.getLocation().toOSString());
							} else {
								dialog.setFilterPath(ScriptDataSourceViewer.fileDialogPath);
							}
							String ext = parameter.getProperty("ext");
							if (ext != null) {
								dialog.setFilterExtensions(ext.split(","));
							} else {
								dialog.setFilterExtensions(new String[]{"*.*"});
							}
							dialog.open();
							if (dialog.getFileName() == null || dialog.getFileName().trim().length() == 0) {
								return;
							}
							String filePath = dialog.getFilterPath() + File.separator + dialog.getFileName();
							if (filePath != null) {
								fileText.setText(filePath);
								fileText.setToolTipText(filePath);
								ScriptDataSourceViewer.fileDialogPath = dialog.getFilterPath();
							}
						}
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});
				fileText.addModifyListener(new ModifyListener() {
					
					@Override
					public void modifyText(ModifyEvent e) {
						String command = parameter.getCommand();
						if (command != null) {
							runIndependentCommand(command);
						}
					}
				});
				break;
			case LIST:
				name = new Label(parent, SWT.RIGHT);
				if (parameter.getProperty("title") != null) {
					name.setText(parameter.getProperty("title"));
				} else {
					name.setText(parameter.getName());
				}
				GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).indent(0, 3
						).minSize(labelWidth, 0).align(SWT.END, SWT.CENTER).span(parameterColspan, parameterRowspan
						).applyTo(name);

				int swtOpt = SWT.V_SCROLL;
				if (Boolean.valueOf(parameter.getProperty("isSingle"))) {
					swtOpt = swtOpt | SWT.SINGLE;
				} else {
					swtOpt = swtOpt | SWT.MULTI;
				}
				final org.eclipse.swt.widgets.List list = new org.eclipse.swt.widgets.List(parent, swtOpt);
				list.setBackground(getBackground());
				GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(fillWidth, fillHeight
						).span(parameterColspan, parameterRowspan).hint(width, height).applyTo(list);
				if (parameter.getOptions() != null) {
					for (Object item : parameter.getOptions()) {
						list.add(String.valueOf(item));
					}
				}
				if (isHighlight) {
					list.setForeground(highlightColor);
				} 			

				
				parameter.addPropertyChangeListener(new PropertyChangeListener() {
					
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals("value")) {
							Object val = parameter.getValue();
							if (val instanceof List) {
								List<?> l = (List<?>) val;
								final String[] res = new String[l.size()];
								int i = 0;
								for (Object item : l) {
									res[i++] = String.valueOf(item);
								}
								Display.getDefault().asyncExec(new Runnable() {

									@Override
									public void run() {
										if (!list.isDisposed()) {
											list.setSelection(res);
										}
									}
								});
							}
						}
					}
				});
				
				list.addSelectionListener(new SelectionListener() {
					
					String[] selection = list.getSelection();
					
					@Override
					public void widgetSelected(SelectionEvent e) {
						String[] newSel = list.getSelection();
						if (newSel != null) {
							parameter.setValue(Arrays.asList(newSel));
						} else {
							parameter.setValue(new ArrayList<String>());
						}
						if (!Arrays.equals(selection, newSel)) {
							String command = parameter.getCommand();
							if (command != null) {
								runIndependentCommand(command);
							}
						}
						selection = newSel;
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});
				

				final PropertyChangeListener propertyListener = new PropertyChangeListener() {

					@Override
					public void propertyChange(final PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals("options")) {
							Display.getDefault().asyncExec(new Runnable() {

								@Override
								public void run() {
									if (!list.isDisposed()) {
										list.deselectAll();
										list.removeAll();
										List<Object> opts = parameter.getOptions();
										for (Object opt : opts) {
											list.add(String.valueOf(opt));
										}
										Object value = parameter.getValue();
										if (value instanceof List) {
											List<?> l = (List<?>) value;
											String[] s = new String[l.size()];
											int i = 0;
											for (Object item : l) {
												s[i++] = String.valueOf(item);
											}
										}
//										comboBox.refresh();
									}
								}
							});
						} else if (evt.getPropertyName().equals("enabled")) {
							Display.getDefault().asyncExec(new Runnable() {

								@Override
								public void run() {
									if (!list.isDisposed()) {
										list.setEnabled(Boolean.valueOf(evt.getNewValue().toString()));
									}
								}
							});
						} else if (evt.getPropertyName().equals("highlight")) {
							Display.getDefault().asyncExec(new Runnable() {

								@Override
								public void run() {
									if (!list.isDisposed()) {
										boolean isHighlight = Boolean.valueOf(evt.getNewValue().toString());
										if (isHighlight) {
											list.setForeground(highlightColor);
										} else {
											list.setForeground(defaultColor);
										}
									}
								}
							});
						}
					}
				};
				parameter.addPropertyChangeListener(propertyListener);

				list.addDisposeListener(new DisposeListener() {

					@Override
					public void widgetDisposed(DisposeEvent e) {
						parameter.removePropertyChangeListener(propertyListener);
					}
				});

				list.addFocusListener(new FocusListener() {

					@Override
					public void focusLost(FocusEvent e) {
					}

					@Override
					public void focusGained(FocusEvent e) {
						String focusCommand = parameter.getProperty("focus");
						if (focusCommand != null) {
							runIndependentCommand(focusCommand);
						}
					}
				});
				break;
			case PROGRESS:
				final ProgressBar progressBar = new ProgressBar(parent, SWT.HORIZONTAL | SWT.NULL);
				progressBar.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
				GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).minSize(labelWidth, 0).hint(width, height)
					.span(parameterColspan * 2, parameterRowspan).applyTo(progressBar);
				if (isHighlight) {
					progressBar.setForeground(highlightColor);
				}
				if (parameter.getProperty("max") != null) {
					try {
						progressBar.setMaximum(Integer.valueOf(parameter.getProperty("max")));
						if (parameter.getProperty("selection") != null) {
							progressBar.setSelection(Integer.valueOf(parameter.getProperty("selection")));
						}						
					} catch (Exception e) {
					}
				}
				Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
					public void run() {
//						DataBindingContext bindingContext = new DataBindingContext();
//						bindingContext.bindValue(SWTObservables.observeSelection(progressBar),
//								BeansObservables.observeValue(parameter, "value"),
//								new UpdateValueStrategy(), new UpdateValueStrategy());

					}
				});
				final PropertyChangeListener progressPropertyListener = new PropertyChangeListener() {

					@Override
					public void propertyChange(final PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals("enabled")) {
							Display.getDefault().asyncExec(new Runnable() {

								@Override
								public void run() {
									if (!progressBar.isDisposed()) {
										progressBar.setEnabled(Boolean.valueOf(evt.getNewValue().toString()));
									}
								}
							});
						} else if (evt.getPropertyName().equals("max")) {
							Display.getDefault().asyncExec(new Runnable() {

								@Override
								public void run() {
									if (!progressBar.isDisposed()) {
										progressBar.setMaximum(Integer.valueOf(evt.getNewValue().toString()));
									}
								}
							});
						} else if (evt.getPropertyName().equals("selection")) {
							Display.getDefault().asyncExec(new Runnable() {

								@Override
								public void run() {
									if (!progressBar.isDisposed()) {
										progressBar.setSelection(Integer.valueOf(evt.getNewValue().toString()));
									}
								}
							});
						} else if (evt.getPropertyName().equals("highlight")) {
							Display.getDefault().asyncExec(new Runnable() {

								@Override
								public void run() {
									if (!progressBar.isDisposed()) {
										boolean isHighlight = Boolean.valueOf(evt.getNewValue().toString());
										if (isHighlight) {
											progressBar.setForeground(highlightColor);
										} else {
											progressBar.setForeground(defaultColor);
										}
									}
								}
							});
						} 
					}
				};
				parameter.addPropertyChangeListener(progressPropertyListener);
				progressBar.addDisposeListener(new DisposeListener() {
					
					@Override
					public void widgetDisposed(DisposeEvent e) {
						parameter.removePropertyChangeListener(progressPropertyListener);
					}
				});
				break;
			case LABEL:
				final Label label = new Label(parent, SWT.NONE);
				label.setText(String.valueOf(parameter.getValue()));
				String fontSizeProp = parameter.getProperty("font_size");
				if (fontSizeProp != null) {
					int fontSize = Integer.valueOf(fontSizeProp);
					FontData fd = label.getFont().getFontData()[0];
					fd.setHeight(fontSize);
					Font newFont = new Font(label.getDisplay(), fd);
					label.setFont(newFont);
				}
				GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(fillWidth, fillHeight).hint(width, height)
					.span(parameterColspan * 2, parameterRowspan).applyTo(label);
				if (isHighlight) {
					label.setForeground(highlightColor);
				} 
				Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
					public void run() {
						DataBindingContext bindingContext = new DataBindingContext();
						bindingContext.bindValue(SWTObservables.observeText(label),
								BeansObservables.observeValue(parameter, "value"),
								new UpdateValueStrategy(), new UpdateValueStrategy());
					}
				});
				final PropertyChangeListener labelPropertyListener = new PropertyChangeListener() {

					@Override
					public void propertyChange(final PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals("highlight")) {
							Display.getDefault().asyncExec(new Runnable() {

								@Override
								public void run() {
									if (!label.isDisposed()) {
										boolean isHighlight = Boolean.valueOf(evt.getNewValue().toString());
										if (isHighlight) {
											label.setForeground(highlightColor);
										} else {
											label.setForeground(defaultColor);
										}
									}
								}
							});
						} 					
					}
				};
				parameter.addPropertyChangeListener(labelPropertyListener);
				label.addDisposeListener(new DisposeListener() {
					
					@Override
					public void widgetDisposed(DisposeEvent e) {
						parameter.removePropertyChangeListener(labelPropertyListener);
					}
				});
				break;
			case SPACE:
				final Label spaceLabel = new Label(parent, SWT.NONE);
				GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(parameterColspan * 2, parameterRowspan).hint(width, height).applyTo(spaceLabel);
				break;
			default:
				name = new Label(parent, SWT.RIGHT);
				if (parameter.getProperty("title") != null) {
					name.setText(parameter.getProperty("title"));
				} else {
					name.setText(parameter.getName());
				}
				GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).indent(0, 3).minSize(labelWidth, 0).align(SWT.END, SWT.CENTER)
					.span(parameterColspan, parameterRowspan).applyTo(name);
				final Text defaultText = new Text(parent, SWT.BORDER);
				defaultText.setText(String.valueOf(parameter.getValue()));
				GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(fillWidth, fillHeight).span(parameterColspan, parameterRowspan).hint(width, height).applyTo(defaultText);
				defaultText.setEditable(itemEnabled);
				if (isHighlight) {
					defaultText.setForeground(highlightColor);
				}
				Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
					public void run() {
						DataBindingContext bindingContext = new DataBindingContext();
						bindingContext.bindValue(SWTObservables.observeText(defaultText, SWT.Modify),
								BeansObservables.observeValue(parameter, "value"),
								new FlagedUpdateStrategy(), new UpdateValueStrategy());
					}
				});
				final PropertyChangeListener defaultPropertyListener = new PropertyChangeListener() {

					@Override
					public void propertyChange(final PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals("enabled")) {
							Display.getDefault().asyncExec(new Runnable() {

								@Override
								public void run() {
									if (!defaultText.isDisposed()) {
										defaultText.setEditable(Boolean.valueOf(evt.getNewValue().toString()));
									}
								}
							});
						} else if (evt.getPropertyName().equals("highlight")) {
							Display.getDefault().asyncExec(new Runnable() {

								@Override
								public void run() {
									if (!defaultText.isDisposed()) {
										boolean isHighlight = Boolean.valueOf(evt.getNewValue().toString());
										if (isHighlight) {
											defaultText.setForeground(highlightColor);
										} else {
											defaultText.setForeground(defaultColor);
										}
									}
								}
							});
						} 
					}
				};
				parameter.addPropertyChangeListener(defaultPropertyListener);
				defaultText.addDisposeListener(new DisposeListener() {
					
					@Override
					public void widgetDisposed(DisposeEvent e) {
						parameter.removePropertyChangeListener(defaultPropertyListener);
					}
				});
//				defaultText.addModifyListener(new ModifyListener() {
//					
//					@Override
//					public void modifyText(ModifyEvent e) {
//						String command = parameter.getCommand();
//						if (command != null) {
//							runIndependentCommand(command);
//						}
//					}
//				});
				defaultText.addKeyListener(new KeyListener() {
					
					@Override
					public void keyReleased(KeyEvent e) {
					}
					
					@Override
					public void keyPressed(KeyEvent e) {
						if (e.keyCode == SWT.CR || e.keyCode == SWT.LF || e.keyCode == 16777296) {
							if (parameter.getDirtyFlag()){
								String command = parameter.getCommand();
								if (command != null) {
									runIndependentCommand(command);
								}
								parameter.resetDirtyFlag();
							}
						} 
					}
				});
				defaultText.addFocusListener(new FocusListener() {
					
					@Override
					public void focusLost(FocusEvent e) {
						if (parameter.getDirtyFlag()){
							String command = parameter.getCommand();
							if (command != null) {
								runIndependentCommand(command);
							}
							parameter.resetDirtyFlag();
						}
					}
					
					@Override
					public void focusGained(FocusEvent e) {
						String focusCommand = parameter.getProperty("focus");
						if (focusCommand != null) {
							runIndependentCommand(focusCommand);
						}
					}
					
				});
				break;
			}
		}
	}

	public IScriptExecutor getScriptExecutor() {
		if (scriptExecutor != null) {
			return scriptExecutor;
		}
		ScriptPageRegister register = ScriptPageRegister.getRegister(scriptRegisterID);
		if (register != null) {
			scriptExecutor = register.getConsoleViewer().getScriptExecutor();
			return scriptExecutor;
		}
		return null;
	}

//	public IScriptExecutor getScriptValidator() {
//		if (scriptValidator != null) {
//			return scriptValidator;
//		}
//		ScriptPageRegister register = ScriptPageRegister.getRegister(scriptRegisterID);
//		if (register != null) {
//			scriptValidator = register.getScriptValidator();
//			return scriptValidator;
//		}
//		return null;
//	}

	private ScriptDataSourceViewer getDataSourceViewer() {
		return ScriptPageRegister.getRegister(scriptRegisterID).getDataSourceViewer();
	}
	
	public static int getNextRegisterID() {
		return SCRIPT_REGISTER_ID ++;
	}

	public int getScriptRegisterID() {
		return scriptRegisterID;
	}
	
	private void addToRecentList(String filename){
		String newList = "";
		String recentFiles = Platform.getPreferencesService().getString(
				Activator.PLUGIN_ID, ID_PREFERENCE_RECENT_FILE, "", null).trim();
		if (recentFiles == null || recentFiles.length() == 0) {
			newList = filename;
		} else {
			String[] names = recentFiles.split(";");
			if (names.length > 0) {
				int index = -1;
				for (int i = 0; i < names.length; i++) {
					if (names[i].equals(filename)) {
						index = i;
					}
				}
				if (index >= 0) {
					for (int i = 0; i < names.length; i++) {
						if (i != index) {
							String name = names[i];
							if (name.trim().length() > 0) {
								newList += ";" + names[i];
							}
						}
					}
					newList = filename + newList;
				} else {
					if (names.length > 17) {
						for (int i = 0; i < 17; i++) {
							String name = names[i];
							if (name.trim().length() > 0) {
								newList += ";" + names[i];
							}
						}
						newList = filename + newList;
					} else if (recentFiles.trim().length() > 0) {
						newList = filename + ";" + recentFiles;
					}
				} 
			} else {
				newList = filename;
			}
		}
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setValue(ID_PREFERENCE_RECENT_FILE, newList);

	}
	
	protected void initSicsListeners() throws ComponentDataFormatException, SicsIOException {
		final ISicsController sics = SicsCore.getSicsController();
		if (sics != null){
			final IDynamicController filenameNode = (IDynamicController) sics.findComponentController(
					FILENAME_NODE_PATH);
			//		final IDynamicController currentPointNode = (IDynamicController) sics.findComponentController(
			//				CURRENT_POINT_PATH);
			final IDynamicController saveCountNode = (IDynamicController) sics.findComponentController(SAVE_COUNT_PATH);
			//		for (IDynamicControllerListener listener : statusListeners)
			//			saveCountNode.removeComponentListener(listener);
			//		statusListeners.clear();
			IDynamicControllerListener statusListener = new DynamicControllerListenerAdapter() {
				int saveCount = saveCountNode.getValue().getIntData();
				public void valueChanged(IDynamicController controller, final IComponentData newValue) {
					int newCount = Integer.valueOf(newValue.getStringData());
					if(newCount != saveCount) {
						saveCount = newCount;
						try{
							File checkFile = new File(filenameNode.getValue().getStringData());
							String dataPath = System.getProperty("sics.data.path");
							checkFile = new File(dataPath + "/" + checkFile.getName());
							final String filePath = checkFile.getAbsolutePath();
							if (!checkFile.exists()){
								String errorMessage = "The target file :" + checkFile.getAbsolutePath() + 
								" can not be found";
								throw new FileNotFoundException(errorMessage);
							}
							Display.getDefault().asyncExec(new Runnable() {

								public void run() {
									ScriptDataSourceViewer viewer = getDataSourceViewer();
									try {
										viewer.addDataset(filePath, true);
									} catch (Exception e) {
										handleException(e, "failed to add dataset: " + filePath);
									}
								}
							});
						}catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			};

			saveCountNode.addComponentListener(statusListener);
		}
	}
	
	/**
	 * @return the scriptFilename
	 */
	public String getScriptFilename() {
		return scriptFilename;
	}

	/**
	 * @param scriptFilename the scriptFilename to set
	 */
	protected void setScriptFilename(String scriptFilename) {
		this.scriptFilename = scriptFilename;
	}

	public class NewConfigFileWizardPage extends WizardNewFileCreationPage {

	    public NewConfigFileWizardPage(IStructuredSelection selection) {
	        super("NewConfigFileWizardPage", selection);
	        setTitle("Script File");
	        setDescription("Creates a new Script File");
	        setFileExtension("py");
	        IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();
	        setContainerFullPath(root.getLocation());
	    }

	    @Override
	    protected InputStream getInitialContents() {
	    	try {
	            return Activator.getDefault().getBundle().getEntry(
	            		TEMPLATE_SCRIPT).openStream();
	        } catch (IOException e) {
	            return null; // ignore and create empty comments
	        }
	    }
	}
	
	public class NewConfigFileWizard extends Wizard implements INewWizard {

	    private IStructuredSelection selection;
	    private NewConfigFileWizardPage newFileWizardPage;
	    private String filename;
	 
	    public NewConfigFileWizard() {
	        setWindowTitle("New Script File");
	    } 

	    @Override
	    public void addPages() {
	        newFileWizardPage = new NewConfigFileWizardPage(selection);
	        addPage(newFileWizardPage);
	    }
	   
	    @Override
	    public boolean performFinish() {
	       
	        IFile file = newFileWizardPage.createNewFile();
	        filename = file.getRawLocationURI().getPath();
	        if (file != null && file.exists())
	            return true;
	        else
	            return false;
	    }

	    public void init(IWorkbench workbench, IStructuredSelection selection) {
	        this.selection = selection;
	    }
	    
	    public NewConfigFileWizardPage getWizardPage() {
	    	return newFileWizardPage;
	    }
	    
	    public String getFilename() {
	    	return filename;
	    }
	}
	
	public void loadScript(final String filename) {
		if (!isDisposed()) {
			Display.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					try {
						initScriptControl(filename);
					} catch (FileNotFoundException e) {
						handleException(e, "Failed to load script: " + e.getMessage());
					}
				}
			});
		}
	}

	public ScriptRunner getRunner() {
		return runner;
	}

	public void setActionStatus(final ActionStatus status) {
		if (currentButton != null) {
			Display.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					switch (status) {
					case RUNNING:
						currentButton.setImage(InternalImage.BUSY_STATUS_16.getImage());
						currentButton.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE));
						currentButton.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED));
						break;
					case DONE:
						currentButton.setImage(InternalImage.DONE_STATUS_16.getImage());
						currentButton.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
						currentButton.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
						break;
					case ERROR:
						currentButton.setImage(InternalImage.INTERRUPT_STATUS_16.getImage());
						currentButton.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
						currentButton.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
						break;
					case INTERRUPT:
						currentButton.setImage(InternalImage.INTERRUPT_STATUS_16.getImage());
						currentButton.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
						currentButton.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
						break;
					default:
						currentButton.setImage(null);
						currentButton.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
						currentButton.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
						break;
					}
				}
			});
		}
	}
	
	public void setActionStatusDone() {
		setActionStatus(ActionStatus.DONE);
	}

	public void setActionStatusBusy() {
		setActionStatus(ActionStatus.RUNNING);
	}

	public void setActionStatusError() {
		setActionStatus(ActionStatus.ERROR);
	}
	
	public void setActionStatusInterrupt() {
		setActionStatus(ActionStatus.INTERRUPT);
	}
	
	public void setPreference(String name, String value){
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setValue(name, value);
	}

	public void savePreferenceStore(){
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		if (store != null && store.needsSaving()
				&& store instanceof IPersistentPreferenceStore) {
			try {
				((IPersistentPreferenceStore) store).save();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public String getPreference(String name) {
		if (name.contains(":")){
			String[] pairs = name.split(":");
			return Platform.getPreferencesService().getString(
					pairs[0], pairs[1], "", null).trim();
		} else {
			return Platform.getPreferencesService().getString(
					Activator.PLUGIN_ID, name, "", null).trim();
		}
	}
	
	public Composite getStaticComposite() {
		return staticComposite;
	}
	
	public Composite getScrollArea() {
		return scroll;
	}
	
	public void setScriptExecutor(IScriptExecutor executor){
		this.scriptExecutor = executor;
	}
	
	public static String getProjectPath(String projectName) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (project != null) {
			IPath path = project.getLocation();
			if (path != null) { 
				return path.toString();
			}
		}
		return null;
	}
	
	public static String getFullScriptPath(String shortPath) {
		String splitter = null;
		if (shortPath.contains("/")) {
			splitter = "/";
		} else if (shortPath.contains("\\")) {
			splitter = "\\";
		}
		if (splitter == null) {
			return WORKSPACE_FOLDER_PATH + "/" + shortPath;
		}
		String[] list = shortPath.split(splitter);
		if (shortPath.startsWith(splitter)) {
			if (list.length == 2) {
//				return WORKSPACE_FOLDER_PATH + shortPath;
				String projectPath = getProjectPath(list[1]);
				return projectPath;
			} else {
				String projectPath = getProjectPath(list[1]);
				if (projectPath != null) {
					return projectPath + shortPath.substring(list[1].length() + 1);
				} else {
					return null;
				}
			}
		} else {
			String projectPath = getProjectPath(list[0]);
			if (projectPath != null) {
				return projectPath + shortPath.substring(list[0].length());
			} else {
				return null;
			}
		}
	}
	
	public void openSICSEditor(){
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				try {
					ScriptPageRegister.getRegister(scriptRegisterID).getWorkbenchPage().openEditor(new NullEditorInput(), SicsUIConstants.ID_EDITOR_SICS_CONTROL);
				} catch (PartInitException e) {
					e.printStackTrace();
				}					
			}
		});
	}
	
	public void setAutoCompletionEnabled(boolean enabled) {
		try{
			ICommandLineViewer viewer = ScriptPageRegister.getRegister(scriptRegisterID).getConsoleViewer();
			if (viewer != null) {
				viewer.setContentAssistEnabled(enabled);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private class FlagedUpdateStrategy extends UpdateValueStrategy{
		@Override
		protected IStatus doSet(IObservableValue observableValue, Object value) {
			IStatus status = super.doSet(observableValue, value);
			if (status.isOK()) {
				Object obj = ((BeanObservableValueDecorator) observableValue).getObserved();
				if (obj instanceof ScriptParameter) {
					((ScriptParameter) obj).setDirtyFlag();
				}
			}
			return status;
		}
		
	}
	
}
