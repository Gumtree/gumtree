package org.gumtree.msw.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class DataSource {
	// fields
	private final byte[] data;
	
	// construction
	public DataSource(InputStream stream) {
		try {
			data = loadData(stream);
		} catch (IOException e) {
			throw new Error("stream not readable");
		}
	}
	public DataSource(File file) {
		try (InputStream stream = new FileInputStream(file)) {
			data = loadData(stream);
		}
		catch (IOException e) {
			throw new Error("file not readable");
		}
	}
	public DataSource(URL url) {
		try (InputStream stream = url.openStream()) {
			data = loadData(stream);
		}
		catch (IOException e) {
			throw new Error("url not readable");
		}
	}
	// helper
	private static byte[] loadData(InputStream stream) throws IOException {
		int n;
		byte[] buffer = new byte[4096];
		ByteArrayOutputStream arrayStream = new ByteArrayOutputStream();

		while ((n = stream.read(buffer)) > -1)
			arrayStream.write(buffer, 0, n);

		arrayStream.flush();
		return arrayStream.toByteArray();
	}
	
	// methods
	public InputStream createStream() {
		return new ByteArrayInputStream(data);
	}
}
