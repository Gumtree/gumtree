/**
 * 
 */
package org.gumtree.service.db;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * @author nxi
 *
 */
public class ObjectDBService {

	private static Map<String, ObjectDBService> dbMap;
	
	private final static String PROP_NOTEBOOK_SAVEPATH = "gumtree.loggingDB.savePath";

	
	static {
		dbMap = new HashMap<String, ObjectDBService>();
	}
	
	private String name;
	private RecordsFile db;
	
	public synchronized static ObjectDBService getDb(String dbName) {
		if (!dbName.toLowerCase().endsWith(".rdf")) {
			dbName += ".rdf";
		}
		if (!dbMap.containsKey(dbName)) {
			ObjectDBService service = new ObjectDBService(dbName);
			dbMap.put(dbName, service);
			return service;
		}
		return dbMap.get(dbName);
	}
	
	public synchronized static ObjectDBService getDb(String path, String dbName) {
		if (!dbName.toLowerCase().endsWith(".rdf")) {
			dbName += ".rdf";
		}
		String fullPath = path + "/" + dbName;
		if (!dbMap.containsKey(fullPath)) {
			ObjectDBService service = new ObjectDBService(path, dbName);
			dbMap.put(fullPath, service);
			return service;
		}
		return dbMap.get(fullPath);
	}
	
	/**
	 * 
	 */
	public ObjectDBService(String name) {
		this(System.getProperty(PROP_NOTEBOOK_SAVEPATH), name);
	}

	public ObjectDBService(String path, String name) {
		this.name = name;
		String dbPath = path + "/" + name;
		File file = new File(dbPath);
		if (!file.exists()) {
			try {
				File parent = file.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}
				db = new RecordsFile(dbPath, 1024);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (RecordsFileException e) {
				e.printStackTrace();
			}
		} else {
			try {
				db = new TextDb(dbPath, "rw");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (RecordsFileException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void appendEntry(String key, Object entry) throws IOException, RecordsFileException {
		RecordWriter rw = new RecordWriter(key);
		rw.writeObject(entry);
		db.insertRecord(rw);
	}
	
	public synchronized Object getEntry(String key) throws RecordsFileException, IOException, ClassNotFoundException {
		RecordReader reader = db.readRecord(key);
		return reader.readObject();
	}
	
	public synchronized Object getEntry(int index) throws RecordsFileException, IOException, ClassNotFoundException {
		RecordReader reader = db.readRecord(index);
		return reader.readObject();
	}
	
	public synchronized String indexToKey(int index) throws IOException{
		return db.readKeyFromIndex(index);
	}
	
	public synchronized List<Object> getEntries(int start, int length) throws RecordsFileException, 
	IOException, ClassNotFoundException {
		List<Object> list = new ArrayList<Object>();
		if (start < 0) {
			return list;
		} else if (start - length + 1 < 0) {
			length = start + 1;
		}
		int numRecords = db.getNumRecords();
		if (start >= numRecords) {
			length = length - (start + 1 - numRecords);
			start = numRecords - 1;
			if (start - length + 1 < 0) {
				length = start + 1;
			}
		}
		for (int i = start; i > start - length; i--) {
			RecordReader reader = db.readRecord(i);
			list.add(reader.readObject());
		}
		return list;
	}
	
	public synchronized LinkedHashMap<String, Object> getKeyEntryPairs(int start, int length) throws RecordsFileException, 
	IOException, ClassNotFoundException {
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		if (start < 0) {
			return map;
		} else if (start - length + 1 < 0) {
			length = start + 1;
		}
		int numRecords = db.getNumRecords();
		if (start >= numRecords) {
			length = length - (start + 1 - numRecords);
			start = numRecords - 1;
			if (start - length + 1 < 0) {
				length = start + 1;
			}
		}
		for (int i = start; i > start - length; i--) {
			String key = indexToKey(i);
			RecordReader reader = db.readRecord(i);
			map.put(key, reader.readObject());
		}
		return map;
	}
	
	public synchronized List<Object> getEntries(int length) throws RecordsFileException, 
	IOException, ClassNotFoundException {
		int start = db.getNumRecords() - 1;
		return getEntries(start, length);
	}
	
	public synchronized LinkedHashMap<String, Object> getKeyEntryPairs(int length) throws RecordsFileException, 
	IOException, ClassNotFoundException {
		int start = db.getNumRecords() - 1;
		return getKeyEntryPairs(start, length);
	}
	
	public int keyToIndex(String key) throws IOException {
		int index = 0;
		if (key == null || key.trim().length() == 0) {
			return -1;
		}
		String cKey;
		int numRecords = db.getNumRecords();
		while (index < numRecords) {
			cKey = db.readKeyFromIndex(index);
			if (key.equals(cKey)) {
				return index;
			}
			index++;
		}
		return -1;
	}
	
	public synchronized void removeEntry(String key) throws RecordsFileException, IOException {
		db.deleteRecord(key);
	}

	public void close() {
		try {
			db.close();
			dbMap.remove(name);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RecordsFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void newDBInstance(){
		close();
		String dbPath = System.getProperty(PROP_NOTEBOOK_SAVEPATH) + "/" + name + ".rdf";
		File oldFile = new File(dbPath);
		if (oldFile.exists()) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
			File newFile = new File(System.getProperty(PROP_NOTEBOOK_SAVEPATH) + "/" + name + "_" + dateFormat.format(new Date()) + ".rdf");
			oldFile.renameTo(newFile);
		}
		try {
			db = new RecordsFile(dbPath, 1024);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RecordsFileException e) {
			e.printStackTrace();
		}
	}
	
	public int getNumRecords() {
		return db.getNumRecords();
	}
	
	public boolean keyExists(String key) {
		return db.recordExists(key);
	}
	
	public void updateEntry(String key, Object entry) throws IOException, RecordsFileException {
		RecordWriter rw = new RecordWriter(key);
		rw.writeObject(entry);		
		db.updateRecord(rw);
	}
	
	public Enumeration<String> keys(){
		return db.enumerateKeys();
	}
}
