package org.gumtree.service.db;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class ControlDB {

	private final static String CONTROL_DB_FILENAME = "controlDB";
	private final static String NOTEBOOK_CURRENTKEY = "current_notebook_page";

	
	private static ControlDB instance;
	
	private ObjectDBService db;
	
	public ControlDB() {
		db = ObjectDBService.getDb(CONTROL_DB_FILENAME);
	}

	public synchronized static ControlDB getInstance(){
		if (instance == null) {
			instance = new ControlDB();
		}
		return instance;
	}

	public String getCurrentSessionId() throws ClassNotFoundException, RecordsFileException, IOException {
			Object entry = db.getEntry(NOTEBOOK_CURRENTKEY);
			if (entry != null) {
				return entry.toString();
			} 
			return null;
	}

	public void updateCurrentSessionId(String sessionId) throws IOException, RecordsFileException {
		if (db.keyExists(NOTEBOOK_CURRENTKEY)) {
			db.updateEntry(NOTEBOOK_CURRENTKEY, sessionId);
		} else {
			db.appendEntry(NOTEBOOK_CURRENTKEY, sessionId);
		}
	}
	
	public void addControlEntry(String entryKey, String entryValue) throws IOException, RecordsFileException {
		if (db.keyExists(entryKey)) {
			db.updateEntry(entryKey, entryValue);
		} else {
			db.appendEntry(entryKey, entryValue);
		}
	}

	public void removeControlEntry(String entryKey) throws IOException, RecordsFileException {
			db.removeEntry(entryKey);
	}

	public List<String> listKeys(){
		Enumeration<String> keys = db.keys();
		return Collections.list(keys);
	}
}
