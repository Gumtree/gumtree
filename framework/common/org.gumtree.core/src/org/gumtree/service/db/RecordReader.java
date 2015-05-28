package org.gumtree.service.db;

import java.io.*;
public class RecordReader {
	String key;
	byte[] data;
	ByteArrayInputStream in;
	ObjectInputStream objIn;
	public RecordReader(String key, byte[] data) {
		this.key = key;
		this.data = data;
		in = new ByteArrayInputStream(data);
	}
	public String getKey() {
		return key;
	}
	public byte[] getData() {
		return data;
	}
	public InputStream getInputStream() throws IOException {
		return in;
	}
	public ObjectInputStream getObjectInputStream() throws IOException {
		if (objIn == null) {
			objIn = new ObjectInputStream(in);
		}
		return objIn;
	}
	/**
	 * Reads the next object in the record using an ObjectInputStream.
	 */
	public Object readObject() throws IOException, OptionalDataException, ClassNotFoundException {
		return getObjectInputStream().readObject();
	}
}