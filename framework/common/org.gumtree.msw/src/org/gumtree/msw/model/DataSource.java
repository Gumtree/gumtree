package org.gumtree.msw.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class DataSource {
	// fields
	private final byte[] data;
	
	// construction
	public DataSource(InputStream stream) {
		data = loadData(stream);
	}
	public DataSource(File file) {
		try {
			data = loadData(new FileInputStream(file));
		}
		catch (FileNotFoundException e) {
			throw new Error("file not found");
		}
	}
	public DataSource(URL url) {
		try {
			data = loadData(url.openStream());
		}
		catch (IOException e) {
			throw new Error("url not found");
		}
	}
	private static byte[] loadData(InputStream stream) {
		try {
			int n;
			byte[] buffer = new byte[4096];
			ByteArrayOutputStream arrayStream = new ByteArrayOutputStream();

			while ((n = stream.read(buffer)) > -1)
				arrayStream.write(buffer, 0, n);

			arrayStream.flush();
			return arrayStream.toByteArray();
			
		}
		catch (IOException e) {
			return null;
		}
	}
	
	// methods
	public InputStream createStream() {
		return new ByteArrayInputStream(data);
	}
}
