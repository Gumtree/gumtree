package au.gov.ansto.bragg.nbi.server.restlet;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.gumtree.scripting.IScriptBlock;
import org.gumtree.scripting.ScriptBlock;

import au.gov.ansto.bragg.nbi.scripting.IPyObject;
import au.gov.ansto.bragg.nbi.scripting.ScriptModel;
import au.gov.ansto.bragg.nbi.scripting.ScriptObjectGroup;
import au.gov.ansto.bragg.nbi.scripting.ScriptParameter;
import au.gov.ansto.bragg.nbi.server.internal.Activator;


public class JythonUIHandler {

	private static int SCRIPT_REGISTER_ID = 0;
	public static final String GUMTREE_SCRIPTING_LIST_PROPERTY = "gumtree.scripting.menuitems";
	public static final String GUMTREE_SCRIPTING_INIT_PROPERTY = "gumtree.scripting.initscript";
	public static final String GUMTREE_SCRIPTING_SCRIPTPATH_PROPERTY = "gumtree.analysis.scriptPath";
	public static final String WORKSPACE_FOLDER_PATH = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
	private static final String __INIT__SCRIPT = "/pyscripts/__init__.py";
	private static final String PRE_RUN_SCRIPT = "/pyscripts/pre_run.py";
	private static final String POST_RUN_SCRIPT	= "/pyscripts/post_run.py";

	private int scriptRegisterID;
	private ScriptModel scriptModel;
	private String scriptFilename;
	
	public JythonUIHandler() {
		scriptRegisterID = getNextRegisterID();
	}

	public void runNativeInitScript() {
			try {
				String fn = FileLocator.toFileURL(Activator.getContext().getBundle().getEntry(__INIT__SCRIPT)).getFile();
				JythonExecutor.runScriptLine("__script_model_id__ = " + scriptRegisterID);
				JythonExecutor.runScriptFile(fn);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	public String getAvailableScripts() {
		String html = "";
		String folderString = System.getProperty(GUMTREE_SCRIPTING_SCRIPTPATH_PROPERTY);
		if (folderString != null && folderString.trim().length() > 0){
			File folder = new File(folderString);
			if (folder.exists()){
				String[] files = folder.list();
				for (String file : files) {
					html += file + ";";
				}
			}
		}
		return html;
	}
	
	public String getInitialScriptsHtml() {
		String html;
		runNativeInitScript();
		String initScriptString = System.getProperty(GUMTREE_SCRIPTING_INIT_PROPERTY);
		if (initScriptString != null && initScriptString.trim() != "") {
			String scriptPath = getFullScriptPath(initScriptString);
			if (scriptPath != null) {
				try {
					html = getScriptControlHtml(scriptPath);
					return html;
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			} else {
				JythonExecutor.runScriptLine("print 'failed to load " + initScriptString + "'");
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
		return "<div class=\"div_error_message\">Failed to load script UI. Can't find initial script.</div>";
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
	
	/**
	 * @param scriptFilename the scriptFilename to set
	 */
	protected void setScriptFilename(String scriptFilename) {
		this.scriptFilename = scriptFilename;
	}

	public String getScriptGUIHtml(final String script) {
		if (JythonExecutor.getExecutor().isBusy()){
			return "<div class=\"div_error_message\">Failed to load script UI. Jython engine is busy.</div>";
		}
		scriptModel = new ScriptModel(scriptRegisterID);
		scriptModel.setDirty(true);
		scriptModel.addChangeListener(new ScriptModel.IModelChangeListener() {
			
			@Override
			public void modelChanged() {
				scriptModel.setDirty(false);
			}
		});
		JythonModelRegister.getRegister(scriptRegisterID).setScriptModel(scriptModel);
		JythonExecutor.runScriptLine("__script_model_id__ = " + scriptRegisterID);
//		IScriptBlock preBlock = new ScriptBlock();
//		for (String line : PRE_RUN_SCRIPT) {
//			preBlock.append(line);
//		}
//		executor.runScript(preBlock);
		try {
			String fn = FileLocator.toFileURL(Activator.getContext().getBundle().getEntry(PRE_RUN_SCRIPT)).getFile();
			JythonExecutor.runScriptFile(fn);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			IScriptBlock block = new ScriptBlock(){
				public String getScript() {
					return script;
				}
			};
			JythonExecutor.runScriptBlock(block);
			JythonExecutor.runScriptLine("print '<' + str(__script__.title) + '> loaded'");
		} catch (Exception e) {
			JythonExecutor.runScriptLine("print 'failed to run script'");
		}
//		IScriptBlock postBlock = new ScriptBlock();
//		for (String line : POST_RUN_SCRIPT) {
//			postBlock.append(line);
//		}
//		executor.runScript(postBlock);
		try {
			String fn = FileLocator.toFileURL(Activator.getContext().getBundle().getEntry(POST_RUN_SCRIPT)).getFile();
			JythonExecutor.runScriptFile(fn);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		if (datasetActivityListener == null) {
//			datasetActivityListener = new IActivityListener() {
//				
//				@Override
//				public void datasetAdded(DatasetInfo[] datasets) {
//					runDatasetAddedScript(datasets);
//				}
//				
//				@Override
//				public void runSelected() {
//					runScript();
//				}
//			};
//			if (getDataSourceViewer() != null) {
//				getDataSourceViewer().addActivityListener(datasetActivityListener);
//			}
//		}
		JythonExecutor.runScriptLine("time.sleep(0.1)");
		JythonExecutor.runScriptLine("auto_run()");
		
		while(scriptModel.isDirty()){
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return getControlHtml();
	}
	
	public String getScriptControlHtml(String filePath) throws FileNotFoundException {
		if (JythonExecutor.getExecutor().isBusy()){
			return "<div class=\"div_error_message\">Failed to load script UI. Jython engine is busy.</div>";
		}
		String text = filePath;
		if (filePath.length() > 24) {
			text = filePath.substring(0, 3) + "..." + filePath.substring(filePath.length() - 18);
		}
		setScriptFilename(filePath);
		scriptModel = new ScriptModel(scriptRegisterID);
		scriptModel.setDirty(true);
		scriptModel.addChangeListener(new ScriptModel.IModelChangeListener() {
			
			@Override
			public void modelChanged() {
				scriptModel.setDirty(false);
			}
		});
//		ScriptPageRegister.getRegister(scriptRegisterID).setScriptModel(scriptModel);
		JythonExecutor.runScriptLine("__script_model_id__ = " + scriptRegisterID);
//		IScriptBlock preBlock = new ScriptBlock();
//		for (String line : PRE_RUN_SCRIPT) {
//			preBlock.append(line);
//		}
//		executor.runScript(preBlock);
		try {
			String fn = FileLocator.toFileURL(Activator.getContext().getBundle().getEntry(PRE_RUN_SCRIPT)).getFile();
			JythonExecutor.runScriptFile(fn);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String initFile = getScriptFilename();
		if (initFile != null) {
			try {
				JythonExecutor.runScriptFile(initFile);
				JythonExecutor.runScriptLine("print '<' + str(__script__.title) + '> loaded'");
			} catch (Exception e) {
				JythonExecutor.runScriptLine("print 'failed to load " + initFile + "'");
			}
		}
//		IScriptBlock postBlock = new ScriptBlock();
//		for (String line : POST_RUN_SCRIPT) {
//			postBlock.append(line);
//		}
//		executor.runScript(postBlock);
		try {
			String fn = FileLocator.toFileURL(Activator.getContext().getBundle().getEntry(POST_RUN_SCRIPT)).getFile();
			JythonExecutor.runScriptFile(fn);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		if (datasetActivityListener == null) {
//			datasetActivityListener = new IActivityListener() {
//				
//				@Override
//				public void datasetAdded(DatasetInfo[] datasets) {
//					runDatasetAddedScript(datasets);
//				}
//				
//				@Override
//				public void runSelected() {
//					runScript();
//				}
//			};
//			if (getDataSourceViewer() != null) {
//				getDataSourceViewer().addActivityListener(datasetActivityListener);
//			}
//		}
		JythonExecutor.runScriptLine("time.sleep(0.1)");
		JythonExecutor.runScriptLine("auto_run()");
		
		while(scriptModel.isDirty()){
			try {
				Thread.sleep(00);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return getControlHtml();
	}
	
	/**
	 * @return the scriptFilename
	 */
	public String getScriptFilename() {
		return scriptFilename;
	}
	
	public String getControlHtml() {
		String html = "";
		String title = scriptModel.getTitle();
		if (title != "unknown") {
			String version = scriptModel.getVersion();
			if (version != "unknown") {
				title += " " + version;
			}
			if (title.length() > 100) {
				title = title.substring(0, 3) + "..." + title.substring(title.length() - 94);
			}
		}
		int numColumns = 1;
		if (scriptModel.getNumColumns() > 0){
			numColumns = scriptModel.getNumColumns();
		}
		if (scriptModel.getNumColumns() > 0) {
			html = "<div class=\"div_jython_gui\" id=\"div_jython_gui\"><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" class=\"scrollTable\">"
//                    + "<thead class=\"table_jython_header\"><tr><th colspan=\"" + numColumns * 2 + "\">" + title + "</th></tr></thead>" 
					+ "<tbody class=\"table_jython_content\" id=\"table_jason_group\">" 
					+ "<tr><td colspan=\"" + numColumns * 2 + "\"><div class=\"table_jython_header_td\">" + title + "</div></td></tr>";
		}
		List<ScriptObjectGroup> groups = scriptModel.getGroups();
		List<IPyObject> objs = scriptModel.getControlList();
		List<IPyObject> controls = prepareControlList(objs, groups);
		int colIndex = 0;
		int index = 0;
		List<Integer> cache = new ArrayList<Integer>();
		html += "<tr>";
		for (final IPyObject control : controls) {
				colIndex += control.getColSpan();
				html += control.getHtml();
				if (control.getRowSpan() > 1){
					for (int i = 0; i < control.getRowSpan() - 1; i++){
						if (cache.size() < i + 1){
							cache.add(1);
						} else {
							cache.set(i, cache.get(i) + 1);
						}
					}
				}
				if (colIndex % numColumns == 0) {
					html += "</tr>";
					if (index < controls.size() - 1){
						html += "<tr>";
						if (cache.size() > 0){
							colIndex += cache.get(0);
							cache.remove(0);
						}
					}
				}
				index += 1;
		}
		if (colIndex % numColumns != 0) {
			html += "</tr>";
		}
		for (final IPyObject obj : objs){
			if (obj instanceof ScriptParameter){
				((ScriptParameter) obj).addPropertyChangeListener(new PropertyChangeListener() {
					
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
							ScriptParameter parameter = (ScriptParameter) evt.getSource();
							JythonExecutor.appendEventJs(parameter.getEventJs(evt.getPropertyName()));
					}
				});
			}
		}
		return html + "</tbody></table></div>";
	}
	
	public String getControlJs() {
		String js = "";
		List<IPyObject> objs = scriptModel.getControlList();
		for (final IPyObject obj : objs){
			if (obj.getInitJs() != null){
				js += obj.getInitJs();
			}
		}
		return js;
	}
	
	private List<IPyObject> prepareControlList(List<IPyObject> objs, List<ScriptObjectGroup> groups) {
		List<IPyObject> list = new ArrayList<IPyObject>();
		for (IPyObject obj : objs) {
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
		}
		return list;
	}

	public static int getNextRegisterID() {
		return SCRIPT_REGISTER_ID ++;
	}

	public int getScriptRegisterID() {
		return scriptRegisterID;
	}

	public String getScriptFileContent(String name) throws IOException {
		String folderString = System.getProperty(GUMTREE_SCRIPTING_SCRIPTPATH_PROPERTY);
		if (folderString != null && folderString.trim().length() > 0){
			String path = folderString + "/" + name;
			return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
		}
		return "";
	}

}
