/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.scan;

import java.io.File;

import au.gov.ansto.bragg.koala.ui.Activator;

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
	
	public ExperimentModel() {
		loadPref();
	}

	public interface IModelListener{
		public void proposalIdChanged(String newId);
		
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
				proposalFolder = System.getProperty(PROP_SAVING_PATH) + "\\" 
						+ propId.replaceAll("[\\D]", "") + "\\";
				File pf = new File(proposalFolder);
				if (!pf.exists()) {
					pf.mkdir();
				}
			} else {
//				popupError("No proposal folder found. Please add a proposal ID before commencing your experiment.");
//				throw new KoalaModelException("No proposal folder found. Please add a proposal ID before commencing your experiment.");
			}
		}

	}

	public void setProposalInfo(String proposalId, String username, String localContact) 
	throws KoalaModelException {
		this.proposalId = proposalId;
		this.username = username;
		this.localContact = localContact;
		try {
			String proposalFolder = System.getProperty(PROP_SAVING_PATH) + "\\" 
					+ proposalId.replaceAll("[\\D]", "") + "\\";
			File pf = new File(proposalFolder);
			if (!pf.exists()) {
				pf.mkdir();
			}
			Activator.setPreference(Activator.NAME_PROP_FOLDER, proposalFolder);
//			mainPart.setProposalFolder(proposalFolder);
		} catch (Exception e) {
//			mainPart.popupError("Failed to create proposal folder, " + e.getMessage());
//			return;
			throw new KoalaModelException(e);
		}
		Activator.setPreference(Activator.NAME_PROP_ID, proposalId);
		Activator.setPreference(Activator.NAME_USER_NAME, username);
		Activator.setPreference(Activator.NAME_LOCAL_SCI, localContact);
		Activator.flushPreferenceStore();
	}
}
