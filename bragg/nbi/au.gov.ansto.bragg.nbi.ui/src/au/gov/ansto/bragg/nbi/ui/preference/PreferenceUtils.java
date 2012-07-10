/**
 * 
 */
package au.gov.ansto.bragg.nbi.ui.preference;

import org.eclipse.core.runtime.Platform;

import au.gov.ansto.bragg.nbi.ui.internal.Activator;

/**
 * @author nxi
 *
 */
public class PreferenceUtils {

	public static String getUserDirectoryPreference(){
		
		return Platform.getPreferencesService().getString(Activator.PLUGIN_ID, 
				PreferenceConstants.P_EXISTING_PROFILE, System.getProperty(
						PreferenceConstants.DEFAULT_USER_DIRECTORY_NAME), null);
	}
	
	public static String getUserNamePreference(){
		
		String userDirectory = getUserDirectoryPreference();
		String userName = null;
		if (userDirectory != null){
			String allProfiles = Platform.getPreferencesService().getString(Activator.PLUGIN_ID, 
				PreferenceConstants.P_HIDDEN_FIELD, "new ...=" + PreferenceConstants.NEW_PROFILE_VALUE, null);
			try {
				userName = findUserName(readProfiles(allProfiles), userDirectory);
			} catch (Exception e) {
			}
		}
		return userName; 
	}
	
	
	static String findUserName(String[][] profiles, String value) {
		for (int i = 0; i < profiles.length; i ++){
			if (profiles[i][1].equals(value))
				return profiles[i][0];
		}
		return null;
	}


	static String[][] readProfiles(String profileString) {
		String[] profiles;
		if (profileString.contains("&")){
			profiles = profileString.split("&");
		}else
			profiles = new String[]{profileString};
		String[][] profilePairs = new String[profiles.length][2];
		for (int i = 0; i < profiles.length; i ++){
			profilePairs[i] = profiles[i].split("=");
		}
		return profilePairs;
	}
}
