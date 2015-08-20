package org.gumtree.service.db;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class ProposalDB {

	private final static String PROPOSAL_DB_FILENAME = "proposalDB";
	
	private static ProposalDB instance;
	
	private ObjectDBService db;
	
	public ProposalDB() {
		db = ObjectDBService.getDb(PROPOSAL_DB_FILENAME);
	}

	public synchronized static ProposalDB getInstance(){
		if (instance == null) {
			instance = new ProposalDB();
		}
		return instance;
	}

	public void putSession(String proposalId, String sessionId) throws IOException, RecordsFileException, ClassNotFoundException{
		String currentIds = getSessionIds(proposalId);
		if (currentIds != null) {
			if (currentIds.trim().length() > 0) {
				sessionId = currentIds + ":" + sessionId;
			} 
			db.updateEntry(proposalId, sessionId);
		} else {
			db.appendEntry(proposalId, sessionId);
		}
	}
	
	public String getSessionIds(String proposalId) throws ClassNotFoundException, RecordsFileException, IOException {
			Object entry = null;
			try {
				entry = db.getEntry(proposalId);
			} catch (Exception e) {
			}
			if (entry != null) {
				return entry.toString();
			} 
			return null;
	}
	
	public String findProposalId(String sessionId) throws IOException, ClassNotFoundException, RecordsFileException {
		int index = 0;
		if (sessionId == null) {
			return null;
		}
		int numRecords = db.getNumRecords();
		while (index < numRecords) {
			if (db.getEntry(index).toString().contains(sessionId)) {
				return db.indexToKey(index);
			}
			index++;
		}
		return null;
	}
	
	public void updateSessionId(String sessionId, String proposalId) throws IOException, RecordsFileException, ClassNotFoundException {
		String currentSessionIds = getSessionIds(proposalId);
		if (currentSessionIds != null && currentSessionIds.contains(sessionId)) {
			return;
		}
		int index = 0;
		int numRecords = db.getNumRecords();
		while (index < numRecords) {
			String sessionIds = db.getEntry(index).toString();
			if (sessionIds.contains(sessionId)) {
				String currentProposalId = db.indexToKey(index);
				String[] sessionArray = sessionIds.split(":");
				if (sessionArray.length == 1) {
					db.removeEntry(currentProposalId);
				} else {
					String newValue = "";
					for (int i = 0; i < sessionArray.length; i++) {
						if (sessionArray[i].equals(currentSessionIds)) {
							newValue += (newValue.length() > 0 ? ":" : "") + sessionArray[i];
						}
					}
					db.updateEntry(currentProposalId, newValue);
				}
				break;
			}
			index++;
		}
		putSession(proposalId, sessionId);
	}
	
	public List<String> listProposalIds(){
		Enumeration<String> keys = db.keys();
		return Collections.list(keys);
	}
}
