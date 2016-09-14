package org.gumtree.service.db;

import java.io.IOException;


public class MapDatabase {

	private ObjectDBService db;
	
	public MapDatabase() {
		db = ObjectDBService.getDb("genericMapDB");
	}

	public MapDatabase(String dbName) {
		db = ObjectDBService.getDb(dbName);
	}
	
	public synchronized static MapDatabase getInstance(String dbName){
		return new MapDatabase(dbName);
	}
	
	public synchronized void put(String key, String value) throws IOException, RecordsFileException {
		db.updateEntry(key, value);
	}

	public synchronized String get(String key) throws ClassNotFoundException, RecordsFileException, IOException {
		return String.valueOf(db.getEntry(key));
	}
	
	public boolean containsKey(String key) {
		return db.keyExists(key);
	}
	
	public synchronized void remove(String key) throws RecordsFileException, IOException{
		db.removeEntry(key);
	}
	
	public synchronized int size() {
		return db.getNumRecords();
	}
	
	public void close() {
		if (db != null) {
			db.close();
		}
	}
}
