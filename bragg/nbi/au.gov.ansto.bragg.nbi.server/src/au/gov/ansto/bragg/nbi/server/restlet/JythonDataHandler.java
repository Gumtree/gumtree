package au.gov.ansto.bragg.nbi.server.restlet;

import java.io.File;
import java.util.List;

public class JythonDataHandler {

	private static final String PROP_SICS_DATAPATH = "gumtree.sics.dataPath";
	private static final String PROP_ANALYSIS_SAVEPATH = "gumtree.analysis.savePath";
	private static final int NUMBER_OF_ROWS = 22;
	private String dataPath;
	private String savePath;
	private List<String> selectedFiles;
	
	public JythonDataHandler() {
		dataPath = System.getProperty(PROP_SICS_DATAPATH);
		savePath = System.getProperty(PROP_ANALYSIS_SAVEPATH);
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
					html += "<tr" + divClass + "><td>" + file.getName() + "</td></tr>";
					fileListCommand += "'" + file.getAbsolutePath() + "',";
					counter ++;
				}
				fileListCommand += "])";
				JythonExecutor.runScriptLine(fileListCommand);
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
					fileListCommand += "'" + file.getAbsolutePath() + "',";
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
	
	public void setSelectedData(final List<String> files){
		selectedFiles = files;
	}
}
