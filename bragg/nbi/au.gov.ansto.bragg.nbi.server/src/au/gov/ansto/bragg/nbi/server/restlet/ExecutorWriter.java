/**
 * 
 */
package au.gov.ansto.bragg.nbi.server.restlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * @author nxi
 *
 */
public class ExecutorWriter extends PrintWriter {

	private String text = "";
	
	/**
	 * @param out
	 */
	public ExecutorWriter(Writer out) {
		super(out);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param out
	 */
	public ExecutorWriter(OutputStream out) {
		super(out);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param fileName
	 * @throws FileNotFoundException
	 */
	public ExecutorWriter(String fileName) throws FileNotFoundException {
		super(fileName);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param file
	 * @throws FileNotFoundException
	 */
	public ExecutorWriter(File file) throws FileNotFoundException {
		super(file);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param out
	 * @param autoFlush
	 */
	public ExecutorWriter(Writer out, boolean autoFlush) {
		super(out, autoFlush);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param out
	 * @param autoFlush
	 */
	public ExecutorWriter(OutputStream out, boolean autoFlush) {
		super(out, autoFlush);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param fileName
	 * @param csn
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public ExecutorWriter(String fileName, String csn)
			throws FileNotFoundException, UnsupportedEncodingException {
		super(fileName, csn);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param file
	 * @param csn
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public ExecutorWriter(File file, String csn) throws FileNotFoundException,
			UnsupportedEncodingException {
		super(file, csn);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void write(String s) {
		super.write(s);
		text += s;
	}
	
	public String getText() {
		return text;
	}
}
