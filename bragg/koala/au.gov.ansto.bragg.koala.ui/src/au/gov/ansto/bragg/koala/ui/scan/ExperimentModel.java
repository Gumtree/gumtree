/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.scan;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.events.SicsProxyListenerAdapter;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.imp.DynamicController;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.sics.ControlHelper;

/**
 * @author nxi
 *
 */
public class ExperimentModel {

	public static final String PROP_SAVING_PATH = "gumtree.koala.proposalFolder";
	
	private PhysicsModel physicsModel;
	private ChemistryModel chemistryModel;
	private String proposalId;
	private String username;
	private String localContact;
	private String proposalFolder;
	private String lastFilename;
	private String errorMessage;
	private ControlHelper controlHelper;
	private UserControl control;
//	private InstrumentPhase instrumentPhase;
	
	private List<IExperimentModelListener> modelListeners;
	
	public ExperimentModel() {
		loadPref();
		controlHelper = ControlHelper.getInstance();
		modelListeners = new ArrayList<IExperimentModelListener>();
		control = new UserControl();
	}

	public PhysicsModel getPhysicsModel() {
		return physicsModel;
	}

	public void setPhysicsModel(PhysicsModel physicsModel) {
		this.physicsModel = physicsModel;
	}

	public ChemistryModel getChemistryModel() {
		return chemistryModel;
	}

	public void setChemistryModel(ChemistryModel chemistryModel) {
		this.chemistryModel = chemistryModel;
	}

	public String getProposalId() {
		return proposalId;
	}

//	public void setProposalId(String proposalId) {
//		this.proposalId = proposalId;
//	}

	public String getUsername() {
		return username;
	}

//	public void setUsername(String username) {
//		this.username = username;
//	}

	public String getLocalContact() {
		return localContact;
	}

//	public void setLocalContact(String localContact) {
//		this.localContact = localContact;
//	}

	public String getProposalFolder() {
		return proposalFolder;
	}

//	public void setProposalFolder(String proposalFolder) {
//		this.proposalFolder = proposalFolder;
//	}
	
	public String getLastFilename() {
		return lastFilename;
	}
	
	public void setLastFilename(final String lastFilename) {
		this.lastFilename = lastFilename;
		for (IExperimentModelListener listener : modelListeners) {
			listener.updateLastFilename(lastFilename);
		}
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public void publishErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;
		for (IExperimentModelListener listener : modelListeners) {
			listener.onError(errorMessage);
		}
	}
	
//	public void setPhase(final InstrumentPhase phase, final int time) {
//		instrumentPhase = phase;
//		for (IExperimentModelListener listener : modelListeners) {
//			listener.phaseChanged(phase, time);
//		}
//	}
	
	private void loadPref() {
		String propId =  Activator.getPreference(Activator.NAME_PROP_ID);
		if (propId != null) {
			this.proposalId = propId;
		} 
		String userName = Activator.getPreference(Activator.NAME_USER_NAME);
		if (userName != null) {
			this.username = userName;
		} 
		String localSci = Activator.getPreference(Activator.NAME_LOCAL_SCI);
		if (localSci != null) {
			this.localContact = localSci;
		} 
		String pFolder = Activator.getPreference(Activator.NAME_PROP_FOLDER);
		if (pFolder != null) {
			proposalFolder = pFolder;
		} else {
			if (propId != null) {
				proposalFolder = System.getProperty(PROP_SAVING_PATH) + File.separator
						+ propId.replaceAll("[\\D]", "") + File.separator;
				File pf = new File(proposalFolder);
				if (!pf.exists()) {
					pf.mkdir();
				}
			} else {
//				popupError("No proposal folder found. Please add a proposal ID before commencing your experiment.");
//				throw new KoalaModelException("No proposal folder found. Please add a proposal ID before commencing your experiment.");
			}
		}
		ControlHelper.proposalFolder = proposalFolder;
	}

	public void setProposalInfo(String proposalId, String username, String localContact) 
	throws KoalaModelException {
		this.proposalId = proposalId;
		this.username = username;
		this.localContact = localContact;
		try {
			String pFolder = System.getProperty(PROP_SAVING_PATH) + File.separator 
					+ proposalId.replaceAll("[\\D]", "") + File.separator;
			File pf = new File(pFolder);
			if (!pf.exists()) {
				pf.mkdir();
			}
			Activator.setPreference(Activator.NAME_PROP_FOLDER, pFolder);
			this.proposalFolder = pFolder;
			control.applyChange();
//			mainPart.setProposalFolder(proposalFolder);
		} catch (Exception e) {
//			mainPart.popupError("Failed to create proposal folder, " + e.getMessage());
//			return;
			throw new KoalaModelException(e);
		}
		ControlHelper.proposalFolder = proposalFolder;
		Activator.setPreference(Activator.NAME_PROP_ID, proposalId);
		Activator.setPreference(Activator.NAME_USER_NAME, username);
		Activator.setPreference(Activator.NAME_LOCAL_SCI, localContact);
		Activator.flushPreferenceStore();
		for (IExperimentModelListener listener : modelListeners) {
			listener.proposalIdChanged(proposalId);
		}
	}
	
	public void addExperimentModelListener(IExperimentModelListener listener) {
		modelListeners.add(listener);
	}
	
	public void removeExperimentModelListener(IExperimentModelListener listener) {
		modelListeners.remove(listener);
	}
	
	class UserControl {
		
		private ISicsController userController;
		
		public UserControl() {
			ISicsProxyListener proxyListener = new SicsProxyListenerAdapter() {
				
				@Override
				public void connect() {
					userController = SicsManager.getSicsModel().findControllerByPath(
							System.getProperty(ControlHelper.GUMTREE_USER_NAME));
				}
			};
			controlHelper.addProxyListener(proxyListener);
		}
		
		public void applyChange() throws SicsException {
			((DynamicController) userController).setValue(username);
		}
	}

}
