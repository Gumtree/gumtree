package au.gov.ansto.bragg.nbi.server.restlet;

import java.io.File;
import java.util.List;

import au.gov.ansto.bragg.nbi.server.jython.JythonRunner;

public class JythonDataHandler {

	private static final String PROP_SICS_DATAPATH = "gumtree.sics.dataPath";
	private static final String PROP_ANALYSIS_CALIBRATIONPATH = "gumtree.sics.calibrationPath";
	private static final String PROP_ANALYSIS_SAVEPATH = "gumtree.analysis.savePath";
	private static final String PROP_ANALYSIS_STOREPATH = "gumtree.analysis.storePath";
	private static final int NUMBER_OF_ROWS = 22;
	private String dataPath;
	private String savePath;
	private String storePath;
	private String calibrationPath;
	private List<String> selectedFiles;
	private List<String> selectedUserFiles;
	private JythonRunner jythonRunner;
	
	public JythonDataHandler() {
		dataPath = System.getProperty(PROP_SICS_DATAPATH);
		savePath = System.getProperty(PROP_ANALYSIS_SAVEPATH);
		storePath = System.getProperty(PROP_ANALYSIS_STOREPATH);
		calibrationPath = System.getProperty(PROP_ANALYSIS_CALIBRATIONPATH);
	}

	public JythonDataHandler(JythonRunner runner) {
		this();
		jythonRunner = runner;
	}
	
	public String getAllDataHtml() {
		String html = "";
		int counter = 0;
		if (dataPath != null){
			File folder = new File(dataPath);
			if (folder.exists()){
				File[] files = folder.listFiles();
				String fileListCommand = "__set_loaded_files__([";
				String divClass;
				for (File file : files) {
					if (file.isDirectory()){
						continue;
					}
					divClass = "";
					if (selectedFiles != null){
						for (String selectedFile : selectedFiles){
							if (selectedFile.equals(file.getName())){
								divClass = " class=\"ui-state-highlight-customised ui-selected\"";
								break;
							}
						}
					}
					html += "<tr" + divClass + "><td><div class=\"div_file_name\">" + file.getName() + "</div><div class=\"div_run_image\" onmousedown=\"sendJython('__selected_files__=[\\\'" + file.getName() + "\\\'];__run_script__(__selected_files__)')\"><img class=\"class_run_image ui-corner-all \" src=\"images/go_button_grey.png\" onmouseover=\"run_image_hover(this);\" onmouseout=\"run_image_unhover(this);\"></div></td></tr>";
//					html += "<tr" + divClass + "><td class=\"td_run_image\"><div class=\"div_file_name\">" + file.getName() + "</div><div class=\"div_run_image\"></div></td></tr>";
					fileListCommand += "'" + file.getAbsolutePath() + "',\\\n";
					counter ++;
				}
				fileListCommand += "])";
				if (jythonRunner != null) {
					jythonRunner.runScriptLine(fileListCommand);
				} else {
					JythonExecutor.runScriptLine(fileListCommand);
				}
			}
		}
		if (counter < NUMBER_OF_ROWS){
			for (int i = 0; i < NUMBER_OF_ROWS - counter; i++) {
				html += "<tr><td></td></tr>";
			}
		}
		return html;
	}

	public String getLoadedDataCommand(){
		String fileListCommand = "__set_loaded_files__([";
		if (dataPath != null){
			File folder = new File(dataPath);
			if (folder.exists()){
				File[] files = folder.listFiles();
				for (File file : files) {
					if (file.isDirectory()){
						continue;
					}
					fileListCommand += "'" + file.getAbsolutePath() + "',\\\n";
				}
			}
		}
		fileListCommand += "])";
		return fileListCommand;
	}
	
	public String getDataPath(){
		return dataPath;
	}
	
	public String getSavePath(){
		return savePath;
	}
	
	public String getStorePath(){
		return storePath;
	}

	public void setSelectedData(final List<String> files){
		selectedFiles = files;
	}
	
	public void setSelectedUserFiles(final List<String> files){
		selectedUserFiles = files;
	}
	
	public String getCalibrationPath(){
		return calibrationPath;
	}

	public String getUserPath(String uuid) {
		return getStorePath() + "/" + uuid;
	}
	
	public String appendUserFiles(List<File> files) {
		String html = "";
		String fileListCommand = "__append_user_files__([";
		String divClass;
		for (File file : files) {
			divClass = "";
			if (selectedUserFiles != null){
				for (String selectedFile : selectedUserFiles){
					if (selectedFile.equals(file.getName())){
						divClass = " class=\"ui-state-highlight-customised ui-selected\"";
						break;
					}
				}
			}
			html += "<tr" + divClass + "><td><div class=\"div_file_name\">" + file.getName() + 
					"</div><div class=\"div_run_image\" onmousedown=\"sendJython('__selected_user_files__=[\\\'" + 
					file.getName() + "\\\'];__run_script__(__selected_user_files__)')\">" + 
					"<img class=\"class_run_image ui-corner-all \" src=\"images/go_button_grey.png\" " +
					"onmouseover=\"run_image_hover(this);\" onmouseout=\"run_image_unhover(this);\"></div></td></tr>";
			fileListCommand += "r'" + file.getAbsolutePath() + "',";
		}
		fileListCommand += "])";
		if (jythonRunner != null) {
			jythonRunner.runScriptLine(fileListCommand);
		} else {
			JythonExecutor.runScriptLine(fileListCommand);
		}
		return html;
	}
	
	public String getUserDataHtml(String uuid) {
		String html = "";
		int counter = 0;
		String userPath = getUserPath(uuid);
		if (userPath != null){
			File folder = new File(userPath);
			if (folder.exists()){
				File[] files = folder.listFiles();
				String fileListCommand = "__set_user_files__([";
				String divClass;
				for (File file : files) {
					if (file.isDirectory()){
						continue;
					}
					divClass = "";
					if (selectedUserFiles != null){
						for (String selectedFile : selectedUserFiles){
							if (selectedFile.equals(file.getName())){
								divClass = " class=\"ui-state-highlight-customised ui-selected\"";
								break;
							}
						}
					}
					html += "<tr" + divClass + "><td><div class=\"div_file_name\">" + file.getName() + "</div><div class=\"div_run_image\" onmousedown=\"sendJython('__selected_user_files__=[\\\'" + file.getName() + "\\\'];__run_script__(__selected_user_files__)')\"><img class=\"class_run_image ui-corner-all \" src=\"images/go_button_grey.png\" onmouseover=\"run_image_hover(this);\" onmouseout=\"run_image_unhover(this);\"></div></td></tr>";
//					html += "<tr" + divClass + "><td class=\"td_run_image\"><div class=\"div_file_name\">" + file.getName() + "</div><div class=\"div_run_image\"></div></td></tr>";
					fileListCommand += "r'" + file.getAbsolutePath() + "',";
					counter ++;
				}
				fileListCommand += "])";
				if (jythonRunner != null) {
					jythonRunner.runScriptLine(fileListCommand);
				} else {
					JythonExecutor.runScriptLine(fileListCommand);
				}
			}
		}
//		if (counter < NUMBER_OF_ROWS){
//			for (int i = 0; i < NUMBER_OF_ROWS - counter; i++) {
//				html += "<tr><td></td></tr>";
//			}
//		}
		return html;
	}
}
