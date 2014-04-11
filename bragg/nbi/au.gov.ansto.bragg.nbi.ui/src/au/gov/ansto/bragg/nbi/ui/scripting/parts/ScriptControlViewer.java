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
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
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
import org.eclipse.nebula.widgets.pgroup.ext.MenuBasedGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ProgressBar;
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
import org.gumtree.ui.scripting.viewer.CommandLineViewer;

import au.gov.ansto.bragg.nbi.ui.internal.Activator;
import au.gov.ansto.bragg.nbi.ui.internal.InternalImage;
import au.gov.ansto.bragg.nbi.ui.scripting.ScriptPageRegister;
import au.gov.ansto.bragg.nbi.ui.scripting.ScriptingPerspective;
import au.gov.ansto.bragg.nbi.ui.scripting.parts.ScriptDataSourceViewer.IActivityListener;
import au.gov.ansto.bragg.nbi.ui.scripting.pyobj.IPyObject;
import au.gov.ansto.bragg.nbi.ui.scripting.pyobj.ScriptAction;
import au.gov.ansto.bragg.nbi.ui.scripting.pyobj.ScriptAction.ActionStatus;
import au.gov.ansto.bragg.nbi.ui.scripting.pyobj.ScriptModel;
import au.gov.ansto.bragg.nbi.ui.scripting.pyobj.ScriptObjectGroup;
import au.gov.ansto.bragg.nbi.ui.scripting.pyobj.ScriptParameter;
import au.gov.ansto.bragg.nbi.ui.scripting.pyobj.ScriptParameter.PType;


/**
 * @author nxi
 *
 */
@SuppressWarnings("restriction")
public class ScriptControlViewer extends Composite {

	private final static String FILENAME_NODE_PATH = "/experiment/file_name";
	private final static String SAVE_COUNT_PATH = "/experiment/save_count";
	protected static String fileDialogPath;
	private static int SCRIPT_REGISTER_ID = 0;
	private static final String ID_PREFERENCE_RECENT_FILE = "org.gumtree.scripting.recent";
	private static final String DEFAULT_SCRIPTING_FOLDER = "gumtree.scripting.defaultFolder";
	private static final String TEMPLATE_SCRIPT = "/pyscripts/AnalysisScriptingTemplate.py";
	private static final String __INIT__SCRIPT = "/pyscripts/__init__.py";
	private static final String PRE_RUN_SCRIPT = "/pyscripts/pre_run.py";
	private static final String POST_RUN_SCRIPT	= "/pyscripts/post_run.py";
//	private static final String INTERNAL_FOLDER_PATH = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + "/Internal";
	public static final String WORKSPACE_FOLDER_PATH = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
	public static final String GUMTREE_SCRIPTING_LIST_PROPERTY = "gumtree.scripting.menuitems";
	public static final String GUMTREE_SCRIPTING_INIT_PROPERTY = "gumtree.scripting.initscript";
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
	private ScriptExecutor scriptExecutor;
	private IActivityListener datasetActivityListener;
	/**
	 * @param parent
	 * @param style
	 */
	public ScriptControlViewer(Composite parent, int style) {
		super(parent, style);
		scriptRegisterID = getNextRegisterID();
		GridLayoutFactory.fillDefaults().margins(0, 0).spacing(1, 1).applyTo(this);
		recentMenuItems = new ArrayList<MenuItem>();
		createStaticArea();
		createDynamicArea();
		runner = new ScriptRunner(parent.getShell());
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
		showButton.setEnabled(false);

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
		IWorkbenchPage page = ScriptPageRegister.getRegister(scriptRegisterID).getWorkbenchPage();
		String pageID = page.getPerspective().getId();
		System.out.println(pageID);
		if (ScriptingPerspective.SCRIPTING_PERSPECTIVE_ID.equals(pageID)) {
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
		}
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
			showButton.setEnabled(true);
			runButton.setEnabled(true);
		}
		setScriptFilename(filePath);
		scriptModel = new ScriptModel(scriptRegisterID);
		scriptModel.addChangeListener(new ScriptModel.IModelChangeListener() {
			
			@Override
			public void modelChanged() {
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						updateControlUI();
					}
				});
			}
		});
		ScriptPageRegister.getRegister(scriptRegisterID).setScriptModel(scriptModel);
		IScriptExecutor executor = getScriptExecutor();
		if (executor != null) {
			executor.runScript("__script_model_id__ = " + scriptRegisterID);
		}
//		IScriptBlock preBlock = new ScriptBlock();
//		for (String line : PRE_RUN_SCRIPT) {
//			preBlock.append(line);
//		}
//		executor.runScript(preBlock);
		try {
			String fn = FileLocator.toFileURL(Activator.getDefault().getBundle().getEntry(PRE_RUN_SCRIPT)).getFile();
			executor.runScript(new FileReader(fn));
		} catch (Exception e) {
			e.printStackTrace();
		}
		String initFile = getScriptFilename();
		if (initFile != null) {
			try {
				FileReader reader = new FileReader(initFile);
				executor.runScript(reader);
				executor.runScript("print '<' + str(__script__.title) + '> loaded'");
			} catch (Exception e) {
				executor.runScript("print 'failed to load " + initFile + "'");
			}
		}
//		IScriptBlock postBlock = new ScriptBlock();
//		for (String line : POST_RUN_SCRIPT) {
//			postBlock.append(line);
//		}
//		executor.runScript(postBlock);
		try {
			String fn = FileLocator.toFileURL(Activator.getDefault().getBundle().getEntry(POST_RUN_SCRIPT)).getFile();
			executor.runScript(new FileReader(fn));
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			};
			if (getDataSourceViewer() != null) {
				getDataSourceViewer().addActivityListener(datasetActivityListener);
			}
		}
		executor.runScript("time.sleep(0.1)");
		executor.runScript("auto_run()");
	}
	
	private void updateControlUI() {
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
			GridLayoutFactory.fillDefaults().margins(2, 2).spacing(4, 4).numColumns(scriptModel.getNumColumns() * 2).applyTo(
					dynamicComposite);
		}
		List<ScriptObjectGroup> groups = scriptModel.getGroups();
		List<IPyObject> objs = scriptModel.getControlList();
		List<IPyObject> controls = prepareControlList(objs, groups);
		for (final IPyObject control : controls) {
			if (control instanceof ScriptParameter) {
				addParameter(dynamicComposite, (ScriptParameter) control);
			} else if (control instanceof ScriptAction) {
				addAction(dynamicComposite, (ScriptAction) control);
			} else if (control instanceof ScriptObjectGroup) {
				addGroup(dynamicComposite, (ScriptObjectGroup) control);
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
	
	private List<IPyObject> prepareControlList(List<IPyObject> objs, List<ScriptObjectGroup> groups) {
		List<IPyObject> list = new ArrayList<IPyObject>();
		for (IPyObject obj : objs) {
			if (obj instanceof ScriptParameter || obj instanceof ScriptAction) {
				boolean inGroup = false;
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

	private void addGroup(Composite parent, ScriptObjectGroup objGroup) {
//		Group group = new Group(parent, SWT.NONE);
		MenuBasedGroup group = new MenuBasedGroup(parent, SWT.NONE);
		group.setClickExpandEnabled(false);
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
		GridLayoutFactory.fillDefaults().numColumns(groupNumColumns * 2).margins(2, 2).spacing(2, 2).applyTo(group);
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
		group.setText(objGroup.getName());
		group.setBackgroundMode(SWT.INHERIT_DEFAULT);
		List<IPyObject> controls = objGroup.getObjectList();
		for (final IPyObject control : controls) {
			if (control instanceof ScriptParameter) {
				addParameter(group, (ScriptParameter) control);
			} else if (control instanceof ScriptAction) {
				addAction(group, (ScriptAction) control);
			} else if (control instanceof ScriptObjectGroup) {
				addGroup(group, (ScriptObjectGroup) control);
			}
		}
	}

	private void addAction(Composite parent, final ScriptAction action) {
		
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

		Label name = new Label(parent, SWT.RIGHT);
//		name.setText(action.getName());
		name.setText("");
		GridDataFactory.fillDefaults().grab(false, false).indent(0, 5).minSize(40, 0).span(actionColspan, actionRowspan).applyTo(name);
		final Button actionButton = new Button(parent, SWT.PUSH);
		actionButton.setText(String.valueOf(action.getText()));
		GridDataFactory.fillDefaults().grab(true, false).span(actionColspan, actionRowspan).applyTo(actionButton);
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
					runCommand("run_action(" + action.getName() + ")");
//					runCommand(action.getName() + ".set_done_status()");
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		action.addStatusListener(new ScriptAction.IActionStatusListener() {
			
			@Override
			public void statusChanged(final ActionStatus newStatus) {
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						switch (newStatus) {
						case RUNNING:
							actionButton.setImage(InternalImage.BUSY_STATUS_16.getImage());
							actionButton.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE));
							actionButton.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED));
							break;
						case ERROR:
							actionButton.setImage(InternalImage.ERROR_STATUS_16.getImage());
							actionButton.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
							actionButton.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
							break;
						case INTERRUPT:
							actionButton.setImage(InternalImage.INTERRUPT_STATUS_16.getImage());
							actionButton.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
							actionButton.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
							break;
						case DONE:
							actionButton.setImage(InternalImage.DONE_STATUS_16.getImage());
							actionButton.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
							actionButton.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
							break;
						default:
							actionButton.setImage(null);
							actionButton.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
							actionButton.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
							break;
						}
					}
				});
			}
		});
		action.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals("enabled")) {
					Display.getDefault().asyncExec(new Runnable() {

						@Override
						public void run() {
							actionButton.setEnabled(Boolean.valueOf(evt.getNewValue().toString()));
						}
					});
				} 
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

		if (parameter.getOptions() != null) {
			Label name = new Label(parent, SWT.RIGHT);
			if (parameter.getProperty("title") != null) {
				name.setText(parameter.getProperty("title"));
			} else {
				name.setText(parameter.getName());
			}
			GridDataFactory.fillDefaults().grab(false, false).indent(0, 3).minSize(40, 0).span(parameterColspan, parameterRowspan).applyTo(name);
			final ComboViewer comboBox = new ComboViewer(parent, SWT.DROP_DOWN);
			GridDataFactory.fillDefaults().grab(false, false).span(parameterColspan, parameterRowspan).applyTo(comboBox.getControl());
			comboBox.setContentProvider(new ArrayContentProvider());
			comboBox.setLabelProvider(new LabelProvider());
			comboBox.getCombo().setEnabled(itemEnabled);
			//				comboBox.setSorter(new ViewerSorter());
			comboBox.setInput(parameter.getOptions());
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
					parameter.addPropertyChangeListener(new PropertyChangeListener() {

						@Override
						public void propertyChange(final PropertyChangeEvent evt) {
							if (evt.getPropertyName().equals("options")) {
								Display.getDefault().asyncExec(new Runnable() {

									@Override
									public void run() {
										comboBox.setInput(parameter.getOptions());
										if (parameter.getValue() == null) {
											comboBox.setSelection(null);
										} else {
											comboBox.setSelection(new StructuredSelection(
													parameter.getValue()));
										}
										comboBox.refresh();
									}
								});
							} else if (evt.getPropertyName().equals("enabled")) {
								Display.getDefault().asyncExec(new Runnable() {

									@Override
									public void run() {
										comboBox.getCombo().setEnabled(Boolean.valueOf(evt.getNewValue().toString()));
									}
								});
							} 
						}
					});
				}
			});
			comboBox.addPostSelectionChangedListener(new ISelectionChangedListener() {
				
				Object selection;
				
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					
					Object newSelection = ((IStructuredSelection) comboBox.getSelection()).getFirstElement();
					if (newSelection != selection) {
						String command = parameter.getCommand();
						if (command != null) {
							runCommand(command);
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
//							runCommand(command);
//						}
//						timeStamp = newStamp;
//					}
//				}
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
				GridDataFactory.fillDefaults().grab(false, false).indent(0, 3).minSize(40, 0).span(parameterColspan, parameterRowspan).applyTo(name);
				final Text stringText = new Text(parent, SWT.BORDER);
				stringText.setText(String.valueOf(parameter.getValue()));
				GridDataFactory.fillDefaults().grab(true, false).span(parameterColspan, parameterRowspan).applyTo(stringText);
				stringText.setEditable(itemEnabled);
				Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
					public void run() {
						DataBindingContext bindingContext = new DataBindingContext();
						bindingContext.bindValue(SWTObservables.observeText(stringText, SWT.Modify),
								BeansObservables.observeValue(parameter, "value"),
								new UpdateValueStrategy(), new UpdateValueStrategy());
						parameter.addPropertyChangeListener(new PropertyChangeListener() {

							@Override
							public void propertyChange(final PropertyChangeEvent evt) {
								if (evt.getPropertyName().equals("enabled")) {
									Display.getDefault().asyncExec(new Runnable() {

										@Override
										public void run() {
											stringText.setEditable(Boolean.valueOf(evt.getNewValue().toString()));
										}
									});
								} 
							}
						});
					}
				});
				stringText.addModifyListener(new ModifyListener() {
					
					@Override
					public void modifyText(ModifyEvent e) {
						String command = parameter.getCommand();
						if (command != null) {
							runCommand(command);
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
				GridDataFactory.fillDefaults().grab(false, false).indent(0, 3).minSize(40, 0).span(parameterColspan, parameterRowspan).applyTo(name);
				final Text intText = new Text(parent, SWT.BORDER);
				intText.setText(String.valueOf(parameter.getValue()));
				GridDataFactory.fillDefaults().grab(true, false).span(parameterColspan, parameterRowspan).applyTo(intText);
				intText.setEditable(itemEnabled);
				Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
					public void run() {
						DataBindingContext bindingContext = new DataBindingContext();
						bindingContext.bindValue(SWTObservables.observeText(intText, SWT.Modify),
								BeansObservables.observeValue(parameter, "value"),
								new UpdateValueStrategy(), new UpdateValueStrategy());
						parameter.addPropertyChangeListener(new PropertyChangeListener() {

							@Override
							public void propertyChange(final PropertyChangeEvent evt) {
								if (evt.getPropertyName().equals("enabled")) {
									Display.getDefault().asyncExec(new Runnable() {

										@Override
										public void run() {
											intText.setEditable(Boolean.valueOf(evt.getNewValue().toString()));
										}
									});
								} 
							}
						});
					}
				});
				intText.addModifyListener(new ModifyListener() {
					
					@Override
					public void modifyText(ModifyEvent e) {
						String command = parameter.getCommand();
						if (command != null) {
							runCommand(command);
						}
					}
				});
				break;
			case FLOAT :
				name = new Label(parent, SWT.RIGHT);
				if (parameter.getProperty("title") != null) {
					name.setText(parameter.getProperty("title"));
				} else {
					name.setText(parameter.getName());
				}
				GridDataFactory.fillDefaults().grab(false, false).indent(0, 3).minSize(40, 0).span(parameterColspan, parameterRowspan).applyTo(name);
				final Text floatText = new Text(parent, SWT.BORDER);
				floatText.setText(String.valueOf(parameter.getValue()));
				GridDataFactory.fillDefaults().grab(true, false).span(parameterColspan, parameterRowspan).applyTo(floatText);
				floatText.setEditable(itemEnabled);
				Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
					public void run() {
						DataBindingContext bindingContext = new DataBindingContext();
						bindingContext.bindValue(SWTObservables.observeText(floatText, SWT.Modify),
								BeansObservables.observeValue(parameter, "value"),
								new UpdateValueStrategy(), new UpdateValueStrategy());
						parameter.addPropertyChangeListener(new PropertyChangeListener() {

							@Override
							public void propertyChange(final PropertyChangeEvent evt) {
								if (evt.getPropertyName().equals("enabled")) {
									Display.getDefault().asyncExec(new Runnable() {

										@Override
										public void run() {
											floatText.setEditable(Boolean.valueOf(evt.getNewValue().toString()));
										}
									});
								} 
							}
						});
					}
				});
				floatText.addModifyListener(new ModifyListener() {
					
					@Override
					public void modifyText(ModifyEvent e) {
						String command = parameter.getCommand();
						if (command != null) {
							runCommand(command);
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
				GridDataFactory.fillDefaults().grab(false, false).minSize(40, 0).span(parameterColspan, parameterRowspan).applyTo(name);
				final Button selectBox = new Button(parent, SWT.CHECK);
				selectBox.setSelection(Boolean.valueOf(String.valueOf(parameter.getValue())));
				GridDataFactory.fillDefaults().grab(false, false).span(parameterColspan, parameterRowspan).applyTo(selectBox);
				selectBox.setEnabled(itemEnabled);
				Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
					public void run() {
						DataBindingContext bindingContext = new DataBindingContext();
						bindingContext.bindValue(SWTObservables.observeSelection(selectBox),
								BeansObservables.observeValue(parameter, "value"),
								new UpdateValueStrategy(), new UpdateValueStrategy());
						parameter.addPropertyChangeListener(new PropertyChangeListener() {

							@Override
							public void propertyChange(final PropertyChangeEvent evt) {
								if (evt.getPropertyName().equals("enabled")) {
									Display.getDefault().asyncExec(new Runnable() {

										@Override
										public void run() {
											selectBox.setEnabled(Boolean.valueOf(evt.getNewValue().toString()));
										}
									});
								} 
							}
						});
					}
				});
				selectBox.addSelectionListener(new SelectionListener() {
					
					@Override
					public void widgetSelected(SelectionEvent e) {
						String command = parameter.getCommand();
						if (command != null) {
							runCommand(command);
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
				GridDataFactory.fillDefaults().grab(false, false).indent(0, 3).minSize(40, 0).span(parameterColspan, parameterRowspan).applyTo(name);
				Composite fileComposite = new Composite(parent, SWT.NONE);
				GridLayoutFactory.fillDefaults().numColumns(2).applyTo(fileComposite);
				GridDataFactory.swtDefaults().grab(true, false).span(parameterColspan, parameterRowspan).applyTo(fileComposite);
				final Text fileText = new Text(fileComposite, SWT.BORDER);
				String itemText = String.valueOf(parameter.getValue());
				fileText.setText(itemText);
				fileText.setToolTipText(itemText);
				fileText.setEditable(itemEnabled);
				GridDataFactory.fillDefaults().grab(true, false).applyTo(fileText);
				Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
					public void run() {
						DataBindingContext bindingContext = new DataBindingContext();
						bindingContext.bindValue(SWTObservables.observeText(fileText, SWT.Modify),
								BeansObservables.observeValue(parameter, "value"),
								new UpdateValueStrategy(), new UpdateValueStrategy());
						parameter.addPropertyChangeListener(new PropertyChangeListener() {

							@Override
							public void propertyChange(final PropertyChangeEvent evt) {
								if (evt.getPropertyName().equals("enabled")) {
									Display.getDefault().asyncExec(new Runnable() {

										@Override
										public void run() {
											fileText.setEditable(Boolean.valueOf(evt.getNewValue().toString()));
										}
									});
								} 
							}
						});
					}
				});
				final Button fileLocatorButton = new Button(fileComposite, SWT.PUSH);
				fileLocatorButton.setText(">>");
				GridDataFactory.fillDefaults().grab(false, true).applyTo(fileLocatorButton);
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
							if (dialog.getFileName() == null) {
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
							if (filePath == null) {
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
							if (dialog.getFileName() == null) {
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
							runCommand(command);
						}
					}
				});
				break;
			case PROGRESS:
				final ProgressBar progressBar = new ProgressBar(parent, SWT.HORIZONTAL | SWT.NULL);
				progressBar.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
				GridDataFactory.fillDefaults().grab(true, false).minSize(40, 0).span(parameterColspan > 2 ? parameterColspan : 2, parameterRowspan).applyTo(progressBar);
				Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
					public void run() {
//						DataBindingContext bindingContext = new DataBindingContext();
//						bindingContext.bindValue(SWTObservables.observeSelection(progressBar),
//								BeansObservables.observeValue(parameter, "value"),
//								new UpdateValueStrategy(), new UpdateValueStrategy());
						parameter.addPropertyChangeListener(new PropertyChangeListener() {

							@Override
							public void propertyChange(final PropertyChangeEvent evt) {
								if (evt.getPropertyName().equals("enabled")) {
									Display.getDefault().asyncExec(new Runnable() {

										@Override
										public void run() {
											progressBar.setEnabled(Boolean.valueOf(evt.getNewValue().toString()));
										}
									});
								} else if (evt.getPropertyName().equals("max")) {
									Display.getDefault().asyncExec(new Runnable() {

										@Override
										public void run() {
											progressBar.setMaximum(Integer.valueOf(evt.getNewValue().toString()));
										}
									});
								} else if (evt.getPropertyName().equals("selection")) {
									Display.getDefault().asyncExec(new Runnable() {

										@Override
										public void run() {
											progressBar.setSelection(Integer.valueOf(evt.getNewValue().toString()));
										}
									});
								}
							}
						});
					}
				});
				break;
			default:
				name = new Label(parent, SWT.RIGHT);
				if (parameter.getProperty("title") != null) {
					name.setText(parameter.getProperty("title"));
				} else {
					name.setText(parameter.getName());
				}
				GridDataFactory.fillDefaults().grab(false, false).indent(0, 3).minSize(40, 0).span(parameterColspan, parameterRowspan).applyTo(name);
				final Text defaultText = new Text(parent, SWT.BORDER);
				defaultText.setText(String.valueOf(parameter.getValue()));
				GridDataFactory.fillDefaults().grab(true, false).span(parameterColspan, parameterRowspan).applyTo(defaultText);
				defaultText.setEditable(itemEnabled);
				Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
					public void run() {
						DataBindingContext bindingContext = new DataBindingContext();
						bindingContext.bindValue(SWTObservables.observeText(defaultText, SWT.Modify),
								BeansObservables.observeValue(parameter, "value"),
								new UpdateValueStrategy(), new UpdateValueStrategy());
						parameter.addPropertyChangeListener(new PropertyChangeListener() {

							@Override
							public void propertyChange(final PropertyChangeEvent evt) {
								if (evt.getPropertyName().equals("enabled")) {
									Display.getDefault().asyncExec(new Runnable() {

										@Override
										public void run() {
											defaultText.setEditable(Boolean.valueOf(evt.getNewValue().toString()));
										}
									});
								} 
							}
						});
					}
				});
				defaultText.addModifyListener(new ModifyListener() {
					
					@Override
					public void modifyText(ModifyEvent e) {
						String command = parameter.getCommand();
						if (command != null) {
							runCommand(command);
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
			return register.getConsoleViewer().getScriptExecutor();
		}
		return null;
	}
	
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
	        setTitle("Config File");
	        setDescription("Creates a new Config File");
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
	        setWindowTitle("New Config File");
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
						currentButton.setImage(InternalImage.ERROR_STATUS_16.getImage());
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
	
	public void setScriptExecutor(ScriptExecutor executor){
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
			}
		});
	}
	
	public void setAutoCompletionEnabled(boolean enabled) {
		try{
			CommandLineViewer viewer = ScriptPageRegister.getRegister(scriptRegisterID).getConsoleViewer();
			if (viewer != null) {
				viewer.setContentAssistEnabled(enabled);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
