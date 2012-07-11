package au.gov.ansto.bragg.wombat.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.nbi.ui.preference.PreferenceConstants;
import au.gov.ansto.bragg.nbi.ui.preference.PreferenceUtils;
import au.gov.ansto.bragg.wombat.ui.internal.ExperimentPerspective;
import au.gov.ansto.bragg.wombat.ui.internal.UserProfileLauncher;
import au.gov.ansto.bragg.wombat.ui.internal.WombatAnalysisPerspective;
import au.gov.ansto.bragg.wombat.ui.views.WombatBatchEditingView;
import au.gov.ansto.bragg.wombat.ui.views.WombatDataSourceView;

public class UserProfileSelectorHandler extends AbstractHandler {

	private static Logger logger = LoggerFactory.getLogger(UserProfileLauncher.class);

	private static final String ID_PROFILE_PREFERENCE_PAGE = "au.gov.ansto.bragg.nbi.ui.userProfile";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String oldUserName = PreferenceUtils.getUserNamePreference();
		String oldUserDirectory = PreferenceUtils.getUserDirectoryPreference();
		if (!MessageDialog.openConfirm(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
				"Confirm to change user profile", "The current user name is " + oldUserName
				+ ", data folder is at " + oldUserDirectory + ".\n" 
				+ "Switching " 
				+ "to new or other existing user profile will clear up current experiment "
				+ "setup, batch files in the run queue and analysis status. \n")) {
			return null;
		}
		try{
			PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
				ID_PROFILE_PREFERENCE_PAGE, new String[]{ID_PROFILE_PREFERENCE_PAGE}, null);
			dialog.open();
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("failed to open user profile preference page", e);
		}
		String newUserName = System.getProperty(PreferenceConstants.USER_PROFILE_USERNAME);
		String newUserDirectory = System.getProperty(PreferenceConstants.USER_PROFILE_DIRECTORY);
		if (newUserDirectory != null && !newUserDirectory.equals(oldUserDirectory)){
			IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			for (int i = 0; i < windows.length; i++) {
				if (windows[i].getActivePage() == null)
					continue;
				IViewReference[] views = windows[i].getActivePage().getViewReferences();
				for (int j = 0; j < views.length; j++) {
					if (views[j].getId().equals(ExperimentPerspective.WORKFLOW_VIEW_ID)){
						IViewPart view = views[j].getView(false);
						if (view instanceof WombatBatchEditingView){
							((WombatBatchEditingView) view).getViewer().clearAll();
						}
					}else if (views[j].getId().equals(WombatAnalysisPerspective.DATA_SOURCE_VIEW_ID)){
						IViewPart view = views[j].getView(false);
						if (view instanceof WombatDataSourceView){
							((WombatDataSourceView) view).removeAllDataSourceFiles(true);
						}
					}
				}
			}
		}
		return null;
	}

}
