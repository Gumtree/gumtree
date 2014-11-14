/**
 * 
 */
package au.gov.ansto.bragg.nbi.server.restlet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import au.gov.ansto.bragg.nbi.scripting.ScriptModel;
import au.gov.ansto.bragg.nbi.server.image.ChartImage;
import au.gov.ansto.bragg.nbi.server.internal.Activator;
import au.gov.ansto.bragg.nbi.server.jython.JythonRunner;


/**
 * @author nxi
 *
 */
public class JythonModelRegister {

	private static Map<Integer, JythonModelRegister> registerTray = 
			new HashMap<Integer, JythonModelRegister>();
	
	private ScriptModel scriptModel;

	private JythonRunner jythonRunner;
	
	public JythonModelRegister() {
	}
	
	public JythonModelRegister(JythonRunner runner) {
		this();
		jythonRunner = runner;
	}

	public static void registPage(int registerID, JythonModelRegister register) {
		registerTray.put(registerID, register);
	}
	
	public static JythonModelRegister getRegister(int registerID) {
		return registerTray.get(registerID);
	}

	/**
	 * @return the scriptModel
	 */
	public ScriptModel getScriptModel() {
		return scriptModel;
	}

	/**
	 * @param scriptModel the scriptModel to set
	 */
	public void setScriptModel(ScriptModel scriptModel) {
		this.scriptModel = scriptModel;
	}

	public String getDataPath(){
		if (jythonRunner != null) {
			return jythonRunner.getDataHandler().getDataPath();
		} else {
			return JythonExecutor.getDataHandler().getDataPath();
		}
	}

	/**
	 * @return the savePath
	 */
	public String getSavePath() {
		if (jythonRunner != null) {
			return jythonRunner.getDataHandler().getSavePath();
		} else {
			return JythonExecutor.getDataHandler().getSavePath();
		}
	}

	public void reportFileForDownload(String filename){
		if (jythonRunner != null) {
			jythonRunner.appendFilesForDownload(filename);
		} else {
			JythonExecutor.appendFilesForDownload(filename);
		}
	}
	
	public void reportAddingUserFiles(List<String> filenames) {
		String fileListCommand = "__append_user_files__([";
		String filenameString = "";
		for (String filename : filenames) {
			fileListCommand += "r'" + filename + "',";
			File file = new File(filename);
			filenameString += file.getName() + ",";
		}
		fileListCommand += "])";
		jythonRunner.runScriptLine(fileListCommand);
		jythonRunner.appendEventJs("appendUserFiles('" + filenameString + "');");
	}
	
	public JythonDataHandler getDataHandler(){
		if (jythonRunner != null) {
			return jythonRunner.getDataHandler();
		} else {
			return JythonExecutor.getDataHandler();
		}
	}
	
	public static void setPreference(String name, String value){
		IEclipsePreferences preferences = InstanceScope.INSTANCE
				  .getNode(Activator.PLUGIN_ID);
		preferences.put(name, value);
	}

	public static void savePreferenceStore(){
		IEclipsePreferences preferences = InstanceScope.INSTANCE
				  .getNode(Activator.PLUGIN_ID);
		try {
			// forces the application to save the preferences
			preferences.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getPreference(String name) {
		IEclipsePreferences preferences = InstanceScope.INSTANCE
				  .getNode(Activator.PLUGIN_ID);
		return preferences.get(name, "");
	}

	public String getScriptPath(){
		if (jythonRunner != null) {
			return jythonRunner.getUIHandler().getScriptPath();
		} else {
			return JythonExecutor.getUIHandler().getScriptPath();
		}
	}

	/**
	 * @return the calibrationPath
	 */
	public String getCalibrationPath() {
		if (jythonRunner != null) {
			return jythonRunner.getDataHandler().getCalibrationPath();
		} else {
			return JythonExecutor.getDataHandler().getCalibrationPath();
		}
	}

	/**
	 * @return the store path
	 */
	public String getStorePath() {
		if (jythonRunner != null) {
			return jythonRunner.getDataHandler().getStorePath();
		} else {
			return JythonExecutor.getDataHandler().getStorePath();
		}
	}

	public ChartImage getPlot1(){
		if (jythonRunner != null) {
			return jythonRunner.getPlot1();
		} else {
			return JythonRestlet.getPlot1();
		}
	}

	public ChartImage getPlot2(){
		if (jythonRunner != null) {
			return jythonRunner.getPlot2();
		} else {
			return JythonRestlet.getPlot2();
		}
	}
	
	public ChartImage getPlot3(){
		if (jythonRunner != null) {
			return jythonRunner.getPlot3();
		} else {
			return JythonRestlet.getPlot3();
		}
	}

	public String getUUID() {
		if (jythonRunner != null) {
			return jythonRunner.getUuid().toString();
		} else {
			return "";
		}
	}
	
	public String getUserPath() {
		String path = getStorePath() + '/' + getUUID();
		File folder = new File(path);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		return path;
	}
}
