/**
 * 
 */
package au.gov.ansto.bragg.nbi.server.notebook.db;

import java.io.IOException;

/**
 * @author nxi
 *
 */
public class NotebookDb extends RecordsFile {

	/**
	 * @param dbPath
	 * @param initialSize
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	public NotebookDb(String dbPath, int initialSize) throws IOException,
			RecordsFileException {
		super(dbPath, initialSize);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param dbPath
	 * @param accessFlags
	 * @throws IOException
	 * @throws RecordsFileException
	 */
	public NotebookDb(String dbPath, String accessFlags) throws IOException,
			RecordsFileException {
		super(dbPath, accessFlags);
		// TODO Auto-generated constructor stub
	}

	public synchronized void appendEntry(String key, String entry) throws IOException, RecordsFileException {
		RecordWriter rw = new RecordWriter(key);
		rw.writeObject(rw);
		insertRecord(rw);
	}
	
	public synchronized String getEntries(int start, int length) throws RecordsFileException, 
	IOException, ClassNotFoundException {
		String html = "";
		if (start < 0) {
			return html;
		} else if (start - length + 1 < 0) {
			length = start + 1;
		}
		int numRecords = getNumRecords();
		if (start >= numRecords) {
			length = length - (start + 1 - numRecords);
			start = numRecords - 1;
			if (start - length + 1 < 0) {
				length = start + 1;
			}
		}
		for (int i = start; i > start - length; i--) {
			RecordReader reader = readRecord(i);
			html += "\n" + reader.readObject();
		}
		return start + ":" + (start - length + 1) + ";" + html;
	}
	
	public synchronized String getEntries(int length) throws RecordsFileException, 
	IOException, ClassNotFoundException {
		int start = getNumRecords() - 1;
		return getEntries(start, length);
	}
	
	
	public int keyToIndex(String key) throws IOException {
		int index = 0;
		if (key == null || key.trim().length() == 0) {
			return -1;
		}
		String cKey;
		int numRecords = getNumRecords();
		while (index < numRecords) {
			cKey = readKeyFromIndex(index);
			if (key.equals(cKey)) {
				return index;
			}
			index++;
		}
		return -1;
	}
}
