/**
 * 
 */
package au.gov.ansto.bragg.nbi.server.restlet;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import au.gov.ansto.bragg.nbi.scripting.ScriptModel;
import au.gov.ansto.bragg.nbi.server.internal.Activator;


/**
 * @author nxi
 *
 */
public class JythonModelRegister {

	private static Map<Integer, JythonModelRegister> registerTray = 
			new HashMap<Integer, JythonModelRegister>();
	
	private ScriptModel scriptModel;

	private String dataPath;
	
	private String savePath;
	
	public JythonModelRegister() {
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

	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}

	public String getDataPath(){
		return dataPath;
	}

	/**
	 * @return the savePath
	 */
	public String getSavePath() {
		return savePath;
	}

	/**
	 * @param savePath the savePath to set
	 */
	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}
	
	public void reportFileForDownload(String filename){
		JythonExecutor.appendFilesForDownload(filename);
	}
	
	public JythonDataHandler getDataHandler(){
		return JythonExecutor.getDataHandler();
	}
	
	public void setPreference(String name, String value){
		IEclipsePreferences preferences = InstanceScope.INSTANCE
				  .getNode(Activator.PLUGIN_ID);
		preferences.put(name, value);
	}

	public void savePreferenceStore(){
		IEclipsePreferences preferences = InstanceScope.INSTANCE
				  .getNode(Activator.PLUGIN_ID);
		try {
			// forces the application to save the preferences
			preferences.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getPreference(String name) {
		IEclipsePreferences preferences = InstanceScope.INSTANCE
				  .getNode(Activator.PLUGIN_ID);
		return preferences.get(name, "");
	}


}
