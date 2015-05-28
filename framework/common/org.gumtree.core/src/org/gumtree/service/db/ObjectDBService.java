/**
 * 
 */
package org.gumtree.service.db;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author nxi
 *
 */
public class ObjectDBService {

	private static Map<String, ObjectDBService> dbMap;
	
	private final static String PROP_NOTEBOOK_SAVEPATH = "gumtree.notebook.savePath";

	
	static {
		dbMap = new HashMap<String, ObjectDBService>();
	}
	
	private String name;
	private RecordsFile db;
	
	public synchronized static ObjectDBService getDb(String dbName) {
		if (!dbMap.containsKey(dbName)) {
			ObjectDBService service = new ObjectDBService(dbName);
			dbMap.put(dbName, service);
			return service;
		}
		return dbMap.get(dbName);
	}
	
	/**
	 * 
	 */
	public ObjectDBService(String name) {
		this.name = name;
		String dbPath = System.getProperty(PROP_NOTEBOOK_SAVEPATH) + "/" + name + ".rdf";
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
	
	public synchronized List<Object> getEntries(int length) throws RecordsFileException, 
	IOException, ClassNotFoundException {
		int start = db.getNumRecords() - 1;
		return getEntries(start, length);
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

	public void close() {
		try {
			db.close();
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
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-DDTHH-mm-ss");
			File newFile = new File(name + "_" + dateFormat.format(new Date()) + ".rdf");
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
}
