package org.gumtree.service.db;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Enumeration;

public abstract class BaseRecordsFile {
	// The database file.
	private RandomAccessFile file;
	// Current file pointer to the start of the record data.
	protected long dataStartPtr;
	// Total length in bytes of the global database headers.
	protected static final int FILE_HEADERS_REGION_LENGTH = 16;
	// Number of bytes in the record header.
	protected static final int RECORD_HEADER_LENGTH = 16;
	// The length of a key in the index.
	protected static final int MAX_KEY_LENGTH = 64;
	// The total length of one index entry - the key length plus the record
	// header length.
	protected static final int INDEX_ENTRY_LENGTH = MAX_KEY_LENGTH
			+ RECORD_HEADER_LENGTH;
	// File pointer to the num records header.
	protected static final long NUM_RECORDS_HEADER_LOCATION = 0;
	// File pointer to the data start pointer header.
	protected static final long DATA_START_HEADER_LOCATION = 4;

	/**
	 * Creates a new database file, initializing the appropriate headers. Enough
	 * space is allocated in the index for the specified initial size.
	 */
	protected BaseRecordsFile(String dbPath, int initialSize)
			throws IOException, RecordsFileException {
		File f = new File(dbPath);
		if (f.exists()) {
			throw new RecordsFileException("Database already exits: " + dbPath);
		}
		file = new RandomAccessFile(f, "rw");
		dataStartPtr = indexPositionToKeyFp(initialSize); // Record Data Region
															// starts were the
		setFileLength(dataStartPtr); // (i+1)th index entry would start.
		writeNumRecordsHeader(0);
		writeDataStartPtrHeader(dataStartPtr);
	}

	/**
	 * Opens an existing database file and initializes the dataStartPtr. The
	 * accessFlags parameter can be "r" or "rw" -- as defined in
	 * RandomAccessFile.
	 */
	protected BaseRecordsFile(String dbPath, String accessFlags)
			throws IOException, RecordsFileException {
		File f = new File(dbPath);
		if (!f.exists()) {
			throw new RecordsFileException("Database not found: " + dbPath);
		}
		file = new RandomAccessFile(f, accessFlags);
		dataStartPtr = readDataStartHeader();
	}

	/**
	 * Returns an Enumeration of the keys of all records in the database.
	 */
	public abstract Enumeration enumerateKeys();

	/**
	 * Returns the number or records in the database.
	 */
	public abstract int getNumRecords();

	/**
	 * Checks there is a record with the given key.
	 */
	public abstract boolean recordExists(String key);

	/**
	 * Maps a key to a record header.
	 */
	protected abstract RecordHeader keyToRecordHeader(String key)
			throws RecordsFileException;

	/**
	 * Locates space for a new record of dataLength size and initializes a
	 * RecordHeader.
	 */
	protected abstract RecordHeader allocateRecord(String key, int dataLength)
			throws RecordsFileException, IOException;

	/**
	 * Returns the record to which the target file pointer belongs - meaning the
	 * specified location in the file is part of the record data of the
	 * RecordHeader which is returned. Returns null if the location is not part
	 * of a record. (O(n) mem accesses)
	 */
	protected abstract RecordHeader getRecordAt(long targetFp)
			throws RecordsFileException;

	protected long getFileLength() throws IOException {
		return file.length();
	}

	protected void setFileLength(long l) throws IOException {
		file.setLength(l);
	}

	/**
	 * Reads the number of records header from the file.
	 */
	protected int readNumRecordsHeader() throws IOException {
		file.seek(NUM_RECORDS_HEADER_LOCATION);
		return file.readInt();
	}

	/**
	 * Writes the number of records header to the file.
	 */
	protected void writeNumRecordsHeader(int numRecords) throws IOException {
		file.seek(NUM_RECORDS_HEADER_LOCATION);
		file.writeInt(numRecords);
	}

	/**
	 * Reads the data start pointer header from the file.
	 */
	protected long readDataStartHeader() throws IOException {
		file.seek(DATA_START_HEADER_LOCATION);
		return file.readLong();
	}

	/**
	 * Writes the data start pointer header to the file.
	 */
	protected void writeDataStartPtrHeader(long dataStartPtr)
			throws IOException {
		file.seek(DATA_START_HEADER_LOCATION);
		file.writeLong(dataStartPtr);
	}

	/**
	 * Returns a file pointer in the index pointing to the first byte in the key
	 * located at the given index position.
	 */
	protected long indexPositionToKeyFp(int pos) {
		return FILE_HEADERS_REGION_LENGTH + (INDEX_ENTRY_LENGTH * pos);
	}

	/**
	 * Returns a file pointer in the index pointing to the first byte in the
	 * record pointer located at the given index position.
	 */
	long indexPositionToRecordHeaderFp(int pos) {
		return indexPositionToKeyFp(pos) + MAX_KEY_LENGTH;
	}

	/**
	 * Reads the ith key from the index.
	 */
	public String readKeyFromIndex(int position) throws IOException {
		file.seek(indexPositionToKeyFp(position));
		return file.readUTF();
	}

	/**
	 * Reads the ith record header from the index.
	 */
	RecordHeader readRecordHeaderFromIndex(int position) throws IOException {
		file.seek(indexPositionToRecordHeaderFp(position));
		return RecordHeader.readHeader(file);
	}

	/**
	 * Writes the ith record header to the index.
	 */
	protected void writeRecordHeaderToIndex(RecordHeader header)
			throws IOException {
		file.seek(indexPositionToRecordHeaderFp(header.indexPosition));
		header.write(file);
	}

	/**
	 * Appends an entry to end of index. Assumes that insureIndexSpace() has
	 * already been called.
	 */
	protected void addEntryToIndex(String key, RecordHeader newRecord,
			int currentNumRecords) throws IOException, RecordsFileException {
		DbByteArrayOutputStream temp = new DbByteArrayOutputStream(
				MAX_KEY_LENGTH);
		(new DataOutputStream(temp)).writeUTF(key);
		if (temp.size() > MAX_KEY_LENGTH) {
			throw new RecordsFileException(
					"Key is larger than permitted size of " + MAX_KEY_LENGTH
							+ " bytes");
		}
		file.seek(indexPositionToKeyFp(currentNumRecords));
		temp.writeTo(file);
		file.seek(indexPositionToRecordHeaderFp(currentNumRecords));
		newRecord.write(file);
		newRecord.setIndexPosition(currentNumRecords);
		writeNumRecordsHeader(currentNumRecords + 1);
	}

	/**
	 * Removes the record from the index. Replaces the target with the entry at
	 * the end of the index.
	 */
	protected void deleteEntryFromIndex(String key, RecordHeader header,
			int currentNumRecords) throws IOException, RecordsFileException {
		if (header.indexPosition != currentNumRecords - 1) {
			String lastKey = readKeyFromIndex(currentNumRecords - 1);
			RecordHeader last = keyToRecordHeader(lastKey);
			last.setIndexPosition(header.indexPosition);
			file.seek(indexPositionToKeyFp(last.indexPosition));
			file.writeUTF(lastKey);
			file.seek(indexPositionToRecordHeaderFp(last.indexPosition));
			last.write(file);
		}
		writeNumRecordsHeader(currentNumRecords - 1);
	}

	/**
	 * Adds the given record to the database.
	 */
	public synchronized void insertRecord(RecordWriter rw)
			throws RecordsFileException, IOException {
		String key = rw.getKey();
		if (recordExists(key)) {
			throw new RecordsFileException("Key exists: " + key);
		}
		insureIndexSpace(getNumRecords() + 1);
		RecordHeader newRecord = allocateRecord(key, rw.getDataLength());
		writeRecordData(newRecord, rw);
		addEntryToIndex(key, newRecord, getNumRecords());
	}

	/**
	 * Adds the given record to the database.
	 */
	public synchronized void insertRecord(RecordWriter rw, int index)
			throws RecordsFileException, IOException {
		String key = rw.getKey();
		if (recordExists(key)) {
			throw new RecordsFileException("Key exists: " + key);
		}
		insureIndexSpace(getNumRecords() + 1);
		RecordHeader newRecord = allocateRecord(key, rw.getDataLength());
		writeRecordData(newRecord, rw);
		addEntryToIndex(key, newRecord, index);
	}

	/**
	 * Updates an existing record. If the new contents do not fit in the
	 * original record, then the update is handled by deleting the old record
	 * and adding the new.
	 */
	public synchronized void updateRecord(RecordWriter rw)
			throws RecordsFileException, IOException {
		RecordHeader header = null;
		try {
			header = keyToRecordHeader(rw.getKey());
		} catch (Exception e) {
			// TODO: handle exception
		} 
		if (header != null) {
			if (rw.getDataLength() > header.dataCapacity) {
				deleteRecord(rw.getKey());
				insertRecord(rw);
			} else {
				writeRecordData(header, rw);
				writeRecordHeaderToIndex(header);
			}
		} else {
			insertRecord(rw);
		}
	}

	/**
	 * Reads a record.
	 */
	public synchronized RecordReader readRecord(String key)
			throws RecordsFileException, IOException {
		byte[] data = readRecordData(key);
		return new RecordReader(key, data);
	}
	
	/**
	 * Reads a record with a given index.
	 */
	public synchronized RecordReader readRecord(int index)
			throws RecordsFileException, IOException {
		if (index >= getNumRecords()) {
			throw new IOException("index out of boundary");
		}
		String key = readKeyFromIndex(index);
		byte[] data = readRecordData(key);
		return new RecordReader(key, data);
	}

	/**
	 * Reads the data for the record with the given key.
	 */
	protected byte[] readRecordData(String key) throws IOException,
			RecordsFileException {
		return readRecordData(keyToRecordHeader(key));
	}

	/**
	 * Reads the record data for the given record header.
	 */
	protected byte[] readRecordData(RecordHeader header) throws IOException {
		byte[] buf = new byte[header.dataCount];
		file.seek(header.dataPointer);
		file.readFully(buf);
		return buf;
	}

	/**
	 * Updates the contents of the given record. A RecordsFileException is
	 * thrown if the new data does not fit in the space allocated to the record.
	 * The header's data count is updated, but not written to the file.
	 */
	protected void writeRecordData(RecordHeader header, RecordWriter rw)
			throws IOException, RecordsFileException {
		if (rw.getDataLength() > header.dataCapacity) {
			throw new RecordsFileException("Record data does not fit");
		}
		header.dataCount = rw.getDataLength();
		file.seek(header.dataPointer);
		rw.writeTo((DataOutput) file);
	}

	/**
	 * Updates the contents of the given record. A RecordsFileException is
	 * thrown if the new data does not fit in the space allocated to the record.
	 * The header's data count is updated, but not written to the file.
	 */
	protected void writeRecordData(RecordHeader header, byte[] data)
			throws IOException, RecordsFileException {
		if (data.length > header.dataCapacity) {
			throw new RecordsFileException("Record data does not fit");
		}
		header.dataCount = data.length;
		file.seek(header.dataPointer);
		file.write(data, 0, data.length);
	}

	/**
	 * Deletes a record.
	 */
	public synchronized void deleteRecord(String key)
			throws RecordsFileException, IOException {
		RecordHeader delRec = keyToRecordHeader(key);
		int currentNumRecords = getNumRecords();
		if (getFileLength() == delRec.dataPointer + delRec.dataCapacity) {
			// shrink file since this is the last record in the file
			setFileLength(delRec.dataPointer);
		} else {
			RecordHeader previous = getRecordAt(delRec.dataPointer - 1);
			if (previous != null) {
				// append space of deleted record onto previous record
				previous.dataCapacity += delRec.dataCapacity;
				writeRecordHeaderToIndex(previous);
			} else {
				// target record is first in the file and is deleted by adding
				// its space to
				// the second record.
				RecordHeader secondRecord = getRecordAt(delRec.dataPointer
						+ (long) delRec.dataCapacity);
				if (secondRecord != null) {
					byte[] data = readRecordData(secondRecord);
					secondRecord.dataPointer = delRec.dataPointer;
					secondRecord.dataCapacity += delRec.dataCapacity;
					writeRecordData(secondRecord, data);
					writeRecordHeaderToIndex(secondRecord);
				}
			}
		}
		deleteEntryFromIndex(key, delRec, currentNumRecords);
	}

	/**
	 * Deletes a record.
	 */
	public synchronized void deleteRecord(int index)
			throws RecordsFileException, IOException {
		String key = readKeyFromIndex(index);
		RecordHeader delRec = keyToRecordHeader(key);
		int currentNumRecords = getNumRecords();
		if (getFileLength() == delRec.dataPointer + delRec.dataCapacity) {
			// shrink file since this is the last record in the file
			setFileLength(delRec.dataPointer);
		} else {
			RecordHeader previous = getRecordAt(delRec.dataPointer - 1);
			if (previous != null) {
				// append space of deleted record onto previous record
				previous.dataCapacity += delRec.dataCapacity;
				writeRecordHeaderToIndex(previous);
			} else {
				// target record is first in the file and is deleted by adding
				// its space to
				// the second record.
				RecordHeader secondRecord = getRecordAt(delRec.dataPointer
						+ (long) delRec.dataCapacity);
				if (secondRecord != null) {
					byte[] data = readRecordData(secondRecord);
					secondRecord.dataPointer = delRec.dataPointer;
					secondRecord.dataCapacity += delRec.dataCapacity;
					writeRecordData(secondRecord, data);
					writeRecordHeaderToIndex(secondRecord);
				}
			}
		}
		deleteEntryFromIndex(key, delRec, currentNumRecords);
	}
	
	// Checks to see if there is space for and additional index entry. If
	// not, space is created by moving records to the end of the file.
	protected void insureIndexSpace(int requiredNumRecords)
			throws RecordsFileException, IOException {
		int currentNumRecords = getNumRecords();
		long endIndexPtr = indexPositionToKeyFp(requiredNumRecords);
		if (endIndexPtr > getFileLength() && currentNumRecords == 0) {
			setFileLength(endIndexPtr);
			dataStartPtr = endIndexPtr;
			writeDataStartPtrHeader(dataStartPtr);
			return;
		}
		while (endIndexPtr > dataStartPtr) {
			RecordHeader first = getRecordAt(dataStartPtr);
			byte[] data = readRecordData(first);
			first.dataPointer = getFileLength();
			first.dataCapacity = data.length;
			setFileLength(first.dataPointer + data.length);
			writeRecordData(first, data);
			writeRecordHeaderToIndex(first);
			dataStartPtr += first.dataCapacity;
			writeDataStartPtrHeader(dataStartPtr);
		}
	}

	/**
	 * Closes the file.
	 */
	public synchronized void close() throws IOException, RecordsFileException {
		try {
			file.close();
		} finally {
			file = null;
		}
	}
}