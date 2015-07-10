package org.gumtree.service.db;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

public class SessionDB {

	private final static String SESSION_DB_FILENAME = "sessionDB";
	
	private static SessionDB instance;
	
	private ObjectDBService db;
	
	public SessionDB() {
		db = ObjectDBService.getDb(SESSION_DB_FILENAME);
	}

	public synchronized static SessionDB getInstance(){
		if (instance == null) {
			instance = new SessionDB();
		}
		return instance;
	}

	public void putSession(String sessionId, String value) throws IOException, RecordsFileException{
		db.appendEntry(sessionId, value);
	}
	
	public String getSessionValue(String sessionId) throws ClassNotFoundException, RecordsFileException, IOException {
			Object entry = db.getEntry(sessionId);
			if (entry != null) {
				return entry.toString();
			} 
			return null;
	}
	
	public String getSessiongId(String value) throws IOException, ClassNotFoundException, RecordsFileException {
		int index = 0;
		if (value == null) {
			return null;
		}
		int numRecords = db.getNumRecords();
		while (index < numRecords) {
			if (value.equals(db.getEntry(index))) {
				return db.indexToKey(index);
			}
			index++;
		}
		return null;
	}
	
	public String createNewSessionId(String value) throws IOException, RecordsFileException {
		UUID uuid = UUID.randomUUID();
		putSession(uuid.toString(), value);
		return uuid.toString();
	}
	
	public void updateSessionId(String value, String sessionId) throws IOException, RecordsFileException {
		if (db.keyExists(value)) {
			db.updateEntry(value, sessionId);
		} else {
			db.appendEntry(value, sessionId);
		}
	}
	
	public List<String> listSessionIds(){
		Enumeration<String> keys = db.keys();
		return Collections.list(keys);
	}
}
