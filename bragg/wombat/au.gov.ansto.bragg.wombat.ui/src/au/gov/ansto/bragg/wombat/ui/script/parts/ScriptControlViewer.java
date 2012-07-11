/**
 * 
 */
package au.gov.ansto.bragg.wombat.ui.script.parts;

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
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
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
import org.gumtree.gumnix.sics.control.ISicsController;
import org.gumtree.gumnix.sics.control.controllers.ComponentDataFormatException;
import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.control.events.DynamicControllerListenerAdapter;
import org.gumtree.gumnix.sics.control.events.IDynamicControllerListener;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.scripting.IScriptBlock;
import org.gumtree.scripting.IScriptExecutor;
import org.gumtree.scripting.ScriptBlock;

import au.gov.ansto.bragg.wombat.ui.internal.Activator;
import au.gov.ansto.bragg.wombat.ui.script.ScriptPageRegister;
import au.gov.ansto.bragg.wombat.ui.script.WombatScriptPerspective;
import au.gov.ansto.bragg.wombat.ui.script.parts.ScriptDataSourceViewer.IActivityListener;
import au.gov.ansto.bragg.wombat.ui.script.pyobj.IPyObject;
import au.gov.ansto.bragg.wombat.ui.script.pyobj.ScriptAction;
import au.gov.ansto.bragg.wombat.ui.script.pyobj.ScriptModel;
import au.gov.ansto.bragg.wombat.ui.script.pyobj.ScriptParameter;
import au.gov.ansto.bragg.wombat.ui.script.pyobj.ScriptParameter.PType;

/**
 * @author nxi
 *
 */
public class ScriptControlViewer extends Composite {

	private final static String FILENAME_NODE_PATH = "/experiment/file_name";
	private final static String SAVE_COUNT_PATH = "/experiment/save_count";
	protected static String fileDialogPath;
	private static int SCRIPT_REGISTER_ID = 0;
	private static final String ID_PREFERENCE_RECENT_FILE = "org.gumtree.scripting.recent";
	private static String[] PRE_RUN_SCRIPT = new String[] {
		"from nexus import *",
		"from control import param",
		"from control import script",
		"from control.param import Par",
		"from control.param import Act",
		"from control.script import Script",
		"from au.gov.ansto.bragg.wombat.ui.script import ScriptPageRegister",
		"from vis.image2d import Image",
		"from vis.plot1d import Plot",
		"from vis.gplot import GPlot",
		"from vis.event import MouseListener",
		"from org.eclipse.core.resources import ResourcesPlugin",
		"__register__ = ScriptPageRegister.getRegister(__script_model_id__)",
		"__model__ = __register__.getScriptModel()",
		"__script__ = Script(__model__)",
		"__script__.title = 'unknown'",
		"__script__.version = 'unknown'",
		"clear = script.clear",
		"Par.__model__ = __model__",
		"Act.__model__ = __model__",
		"df = script.df",
		"Plot1 = GPlot(widget=__register__.getPlot1())",
		"Plot2 = GPlot(widget=__register__.getPlot2())",
		"Plot3 = GPlot(widget=__register__.getPlot3())",
		"gumtree_root = str(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString())",
		"def noclose():",
		"\tprint 'not closable'",
		"Plot1.close = noclose",
		"Plot2.close = noclose",
		"Plot3.close = noclose",
//		"Plot1 = Image(widget=__register__.getPlot1())",
//		"Plot2 = Plot(widget=__register__.getPlot2())",
//		"Plot3 = Image(widget=__register__.getPlot3())",
		"if '__dispose__' in globals() :",
		"\t__dispose__()",
		"",
	};
	private static String[] POST_RUN_SCRIPT = new String[] {
		"def set_name(obj, name):",
		"\tobj.name = name",
		"",
		"for __name__to__test__ in globals().scope_keys() :",
		"\tif eval('isinstance(' + __name__to__test__ + ', Par)') :",
        "\t\teval('set_name(' + __name__to__test__ + ', \"' + __name__to__test__ + '\")')",
        "",
        "__model__.fireModelChanged()",
        "if hasattr(__script__, 'dict_path') and __script__.dict_path != None:",
        "\tDataset.__dicpath__ = __script__.dict_path",
        "",
	};
	private Composite staticComposite;
	private Composite dynamicComposite;
	private Label scriptLabel;
	private Button loadButton;
	private Button reloadButton;
	private Button showButton;
	private Button runButton;
	private Menu loadMenu;
	private MenuItem openMenuItem;
	private MenuItem newMenuItem;
	private List<MenuItem> recentMenuItems;
	private ScriptRunner runner;
	private IEditorPart scriptEditor;
	private IPropertyListener editorListener;
	private int scriptRegisterID;
	private ScriptModel scriptModel;
	private String scriptFilename;
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
		runner = new ScriptRunner();
		
	}

	private void createStaticArea() {
		staticComposite = new Composite(this, SWT.EMBEDDED);
		GridLayoutFactory.fillDefaults().numColumns(3).margins(0, 0).spacing(1, 1).applyTo(staticComposite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(staticComposite);
		staticComposite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		staticComposite.setBackgroundMode(SWT.INHERIT_FORCE);
		
		loadButton = new Button(staticComposite, SWT.PUSH);
		loadButton.setText("Load Script");
		loadButton.setImage(Activator.imageDescriptorFromPlugin(
				Activator.PLUGIN_ID, "icons/script/down_16.png").createImage());
		GridDataFactory.fillDefaults().grab(false, false).minSize(40, 0).applyTo(loadButton);
		loadMenu = new Menu(staticComposite);
		openMenuItem = new MenuItem(loadMenu, SWT.PUSH);
		openMenuItem.setImage(Activator.imageDescriptorFromPlugin(
				Activator.PLUGIN_ID, "icons/script/toc_open.gif").createImage());
		openMenuItem.setText("Open file...");
		newMenuItem = new MenuItem(loadMenu, SWT.PUSH);
		newMenuItem.setImage(Activator.imageDescriptorFromPlugin(
				Activator.PLUGIN_ID, "icons/script/new_con.gif").createImage());
		newMenuItem.setText("New script...");
		new MenuItem(loadMenu, SWT.SEPARATOR);
		
		scriptLabel = new Label(staticComposite, SWT.CENTER);
		GridDataFactory.fillDefaults().grab(false, false).indent(0, 6).span(2, 1).applyTo(scriptLabel);
		
		showButton = new Button(staticComposite, SWT.PUSH);
		showButton.setText("Edit/Hide");
		showButton.setImage(Activator.imageDescriptorFromPlugin(
				Activator.PLUGIN_ID, "icons/script/edit_16.png").createImage());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(showButton);
		showButton.setEnabled(false);

		reloadButton = new Button(staticComposite, SWT.PUSH);
		reloadButton.setText("Reload");
		reloadButton.setImage(Activator.imageDescriptorFromPlugin(
				Activator.PLUGIN_ID, "icons/script/reload_page16x16.png").createImage());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(reloadButton);
		reloadButton.setEnabled(false);

		runButton = new Button(staticComposite, SWT.PUSH);
		runButton.setText("Run");
		runButton.setImage(Activator.imageDescriptorFromPlugin(
				Activator.PLUGIN_ID, "icons/script/Play-Normal-16x16.png").createImage());
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
 					IWorkspace workspace= ResourcesPlugin.getWorkspace();
 					IWorkspaceRoot root = workspace.getRoot();
 					dialog.setFilterPath(root.getLocation().toOSString());
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
				makeRecentListMenu(loadMenu);
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
					confirmReload();
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
					initScriptControl(scriptFilename);
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
			initSicsListeners();
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
							if (name.length() > 30){
								recentItem.setText("..." + name.substring(name.length() - 27));
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
		File fileToOpen = new File(runner.getScriptPath());
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
		if (WombatScriptPerspective.SCRIPTING_PERSPECTIVE_ID.equals(pageID)) {
			boolean reload = MessageDialog.openQuestion(getShell(), "Reload the Script", "The " +
					"analysis script has been changed, do you want to reload it? All argument " +
					"values will be reset to default if reloaded.");
			if (reload) {
				try {
					initScriptControl(scriptFilename);
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
	
	private void runCommand(String command) {
		IScriptExecutor executor = getScriptExecutor();
		executor.runScript(command);
	}
	
	private void handleException(Exception ex, String message) {
		MessageDialog.openError(getShell(), "Error", message + ": " + ex.getLocalizedMessage());
	}
	
	private void setEditorVisible(boolean isVisible) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (page != null) {
			page.setEditorAreaVisible(isVisible);
		}
	}
	
	private void createDynamicArea() {
		dynamicComposite = new Composite(this, SWT.BORDER);
		GridLayoutFactory.fillDefaults().margins(2, 2).spacing(4, 4).numColumns(2).applyTo(
				dynamicComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(dynamicComposite);
		dynamicComposite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		dynamicComposite.setBackgroundMode(SWT.INHERIT_FORCE);
	}

	private void initScriptControl(String filePath) throws FileNotFoundException {
		String text = filePath;
		if (filePath.length() > 24) {
			text = filePath.substring(0, 3) + "..." + filePath.substring(filePath.length() - 18);
		}
		scriptLabel.setText(text);
		scriptLabel.setToolTipText(filePath);
		runner.setScriptPath(filePath);
		reloadButton.setEnabled(true);
		showButton.setEnabled(true);
		runButton.setEnabled(true);
		scriptFilename = filePath;
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
		IScriptBlock preBlock = new ScriptBlock();
		for (String line : PRE_RUN_SCRIPT) {
			preBlock.append(line);
		}
		executor.runScript(preBlock);
		FileReader reader = new FileReader(scriptFilename);
		executor.runScript(reader);
		executor.runScript("print 'script loaded'");
		IScriptBlock postBlock = new ScriptBlock();
		for (String line : POST_RUN_SCRIPT) {
			postBlock.append(line);
		}
		executor.runScript(postBlock);
//		updateControlUI();
		if (datasetActivityListener == null) {
			datasetActivityListener = new IActivityListener() {
				
				@Override
				public void activityTriggered(DatasetInfo dataset) {
					runScript();
				}
				
				@Override
				public void activitiesTriggered(DatasetInfo[] datasets) {
					runScript();
				}
			};
			getDataSourceViewer().addActivityListener(datasetActivityListener);
		}
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
			scriptLabel.setText(title);
		}
		List<IPyObject> controls = scriptModel.getControlList();
		for (final IPyObject control : controls) {
			if (control instanceof ScriptParameter) {
				final ScriptParameter parameter = (ScriptParameter) control;
				if (parameter.getOptions() != null) {
					Label name = new Label(dynamicComposite, SWT.RIGHT);
					name.setText(parameter.getName());
					GridDataFactory.fillDefaults().grab(false, false).indent(0, 3).minSize(40, 0).applyTo(name);
					final ComboViewer comboBox = new ComboViewer(dynamicComposite, SWT.READ_ONLY);
					GridDataFactory.fillDefaults().grab(false, false).applyTo(comboBox.getControl());
					comboBox.setContentProvider(new ArrayContentProvider());
					comboBox.setLabelProvider(new LabelProvider());
					//				comboBox.setSorter(new ViewerSorter());
					comboBox.setInput(parameter.getOptions());
					Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
						public void run() {
							DataBindingContext bindingContext = new DataBindingContext();
							bindingContext.bindValue(ViewersObservables.observeSingleSelection(comboBox),
									BeansObservables.observeValue(parameter, "value"),
									new UpdateValueStrategy(), new UpdateValueStrategy());
							parameter.addPropertyChangeListener(new PropertyChangeListener() {

								@Override
								public void propertyChange(PropertyChangeEvent evt) {
									if (evt.getPropertyName().equals("options")) {
										Display.getDefault().asyncExec(new Runnable() {

											@Override
											public void run() {
												comboBox.setInput(parameter.getOptions());
												comboBox.setSelection(new StructuredSelection(
														parameter.getValue()));
											}
										});
									}
								}
							});
						}
					});
					comboBox.addSelectionChangedListener(new ISelectionChangedListener() {
						
						@Override
						public void selectionChanged(SelectionChangedEvent event) {
							String command = parameter.getCommand();
							if (command != null) {
								runCommand(command);
							}
						}
					});
				} else {
					PType type = parameter.getType();
					switch (type) {
					case STRING:
						Label name = new Label(dynamicComposite, SWT.RIGHT);
						name.setText(parameter.getName());
						GridDataFactory.fillDefaults().grab(false, false).indent(0, 3).minSize(40, 0).applyTo(name);
						final Text stringText = new Text(dynamicComposite, SWT.BORDER);
						stringText.setText(String.valueOf(parameter.getValue()));
						GridDataFactory.fillDefaults().grab(true, false).applyTo(stringText);
						Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
							public void run() {
								DataBindingContext bindingContext = new DataBindingContext();
								bindingContext.bindValue(SWTObservables.observeText(stringText, SWT.Modify),
										BeansObservables.observeValue(parameter, "value"),
										new UpdateValueStrategy(), new UpdateValueStrategy());
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
						name = new Label(dynamicComposite, SWT.RIGHT);
						name.setText(parameter.getName());
						GridDataFactory.fillDefaults().grab(false, false).indent(0, 3).minSize(40, 0).applyTo(name);
						final Text intText = new Text(dynamicComposite, SWT.BORDER);
						intText.setText(String.valueOf(parameter.getValue()));
						GridDataFactory.fillDefaults().grab(true, false).applyTo(intText);
						Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
							public void run() {
								DataBindingContext bindingContext = new DataBindingContext();
								bindingContext.bindValue(SWTObservables.observeText(intText, SWT.Modify),
										BeansObservables.observeValue(parameter, "value"),
										new UpdateValueStrategy(), new UpdateValueStrategy());
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
						name = new Label(dynamicComposite, SWT.RIGHT);
						name.setText(parameter.getName());
						GridDataFactory.fillDefaults().grab(false, false).indent(0, 3).minSize(40, 0).applyTo(name);
						final Text floatText = new Text(dynamicComposite, SWT.BORDER);
						floatText.setText(String.valueOf(parameter.getValue()));
						GridDataFactory.fillDefaults().grab(true, false).applyTo(floatText);
						Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
							public void run() {
								DataBindingContext bindingContext = new DataBindingContext();
								bindingContext.bindValue(SWTObservables.observeText(floatText, SWT.Modify),
										BeansObservables.observeValue(parameter, "value"),
										new UpdateValueStrategy(), new UpdateValueStrategy());
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
						name = new Label(dynamicComposite, SWT.RIGHT);
						name.setText(parameter.getName());
						GridDataFactory.fillDefaults().grab(false, false).minSize(40, 0).applyTo(name);
						final Button selectBox = new Button(dynamicComposite, SWT.CHECK);
						selectBox.setSelection(Boolean.valueOf(String.valueOf(parameter.getValue())));
						GridDataFactory.fillDefaults().grab(false, false).applyTo(selectBox);
						Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
							public void run() {
								DataBindingContext bindingContext = new DataBindingContext();
								bindingContext.bindValue(SWTObservables.observeSelection(selectBox),
										BeansObservables.observeValue(parameter, "value"),
										new UpdateValueStrategy(), new UpdateValueStrategy());
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
						name = new Label(dynamicComposite, SWT.RIGHT);
						name.setText(parameter.getName());
						GridDataFactory.fillDefaults().grab(false, false).indent(0, 3).minSize(40, 0).applyTo(name);
						Composite fileComposite = new Composite(dynamicComposite, SWT.NONE);
						GridLayoutFactory.fillDefaults().numColumns(2).applyTo(fileComposite);
						GridDataFactory.fillDefaults().applyTo(fileComposite);
						final Text fileText = new Text(fileComposite, SWT.BORDER);
						fileText.setText(String.valueOf(parameter.getValue()));
						GridDataFactory.fillDefaults().grab(true, false).applyTo(fileText);
						Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
							public void run() {
								DataBindingContext bindingContext = new DataBindingContext();
								bindingContext.bindValue(SWTObservables.observeText(fileText, SWT.Modify),
										BeansObservables.observeValue(parameter, "value"),
										new UpdateValueStrategy(), new UpdateValueStrategy());
							}
						});
						final Button fileLocatorButton = new Button(fileComposite, SWT.PUSH);
						fileLocatorButton.setText(">>");
						GridDataFactory.fillDefaults().grab(false, true).applyTo(fileLocatorButton);
						fileLocatorButton.setToolTipText("click to locate the " + parameter.getName());
						fileLocatorButton.addSelectionListener(new SelectionListener() {

							@Override
							public void widgetSelected(SelectionEvent e) {
								FileDialog dialog = new FileDialog(getShell(), SWT.SINGLE);
								if (ScriptDataSourceViewer.fileDialogPath == null){
									IWorkspace workspace= ResourcesPlugin.getWorkspace();
									IWorkspaceRoot root = workspace.getRoot();
									dialog.setFilterPath(root.getLocation().toOSString());
								} else {
									dialog.setFilterPath(ScriptDataSourceViewer.fileDialogPath);
								}
								dialog.setFilterExtensions(new String[]{"*.hdf"});
								dialog.open();
								if (dialog.getFileName() == null) {
									return;
								}
								String filePath = dialog.getFilterPath() + File.separator + dialog.getFileName();
								if (filePath != null) {
									fileText.setText(filePath);
									fileText.setToolTipText(filePath);
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
					default:
						name = new Label(dynamicComposite, SWT.RIGHT);
						name.setText(parameter.getName());
						GridDataFactory.fillDefaults().grab(false, false).indent(0, 3).minSize(40, 0).applyTo(name);
						final Text defaultText = new Text(dynamicComposite, SWT.BORDER);
						defaultText.setText(String.valueOf(parameter.getValue()));
						GridDataFactory.fillDefaults().grab(true, false).applyTo(defaultText);
						Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
							public void run() {
								DataBindingContext bindingContext = new DataBindingContext();
								bindingContext.bindValue(SWTObservables.observeText(defaultText, SWT.Modify),
										BeansObservables.observeValue(parameter, "value"),
										new UpdateValueStrategy(), new UpdateValueStrategy());
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
			} else if (control instanceof ScriptAction) {
				final ScriptAction action = (ScriptAction) control;
				Label name = new Label(dynamicComposite, SWT.RIGHT);
				name.setText(action.getName());
				GridDataFactory.fillDefaults().grab(false, false).indent(0, 5).minSize(40, 0).applyTo(name);
				final Button actionButton = new Button(dynamicComposite, SWT.PUSH);
				actionButton.setText(String.valueOf(action.getText()));
				GridDataFactory.fillDefaults().grab(true, false).applyTo(actionButton);
				actionButton.addSelectionListener(new SelectionListener() {
					
					@Override
					public void widgetSelected(SelectionEvent e) {
						String command = action.getCommand();
						if (command != null) {
							runCommand(command);
						}
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});
			}
		}
		dynamicComposite.layout(true, true);
		dynamicComposite.update();
		dynamicComposite.redraw();
		layout(true, true);
		update();
		redraw();
	}
	
	private IScriptExecutor getScriptExecutor() {
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
	            return Activator.getDefault().getBundle().getEntry("/samples/pyscript/AnalysisScriptingTemplate.py").openStream();
	        } catch (IOException e) {
	            return null; // ignore and create empty comments
	        }
	    }
	}
	
	public class NewConfigFileWizard extends Wizard implements INewWizard {

	    private IStructuredSelection selection;
	    private NewConfigFileWizardPage newFileWizardPage;
	    private IWorkbench workbench;
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
	        if (file != null)
	            return true;
	        else
	            return false;
	    }

	    public void init(IWorkbench workbench, IStructuredSelection selection) {
	        this.workbench = workbench;
	        this.selection = selection;
	    }
	    
	    public NewConfigFileWizardPage getWizardPage() {
	    	return newFileWizardPage;
	    }
	    
	    public String getFilename() {
	    	return filename;
	    }
	}
}
