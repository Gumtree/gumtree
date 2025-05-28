/**
 * 
 */
package au.gov.ansto.bragg.platypus.webserver;

import org.restlet.Context;

import au.gov.ansto.bragg.nbi.server.restlet.FileLoaderRestlet;

/**
 * @author nxi
 *
 */
public class PlatypusScriptRestlet extends FileLoaderRestlet {

	private static final String PROP_FILERESOURCE= "gumtree.status.fileResource";
	/**
	 * 
	 */
	public PlatypusScriptRestlet() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 */
	public PlatypusScriptRestlet(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void init() {
		String fileString = System.getProperty(PROP_FILERESOURCE, null);
		if (fileString != null) {
			String[] pairs = fileString.split(";");
			for (String pair : pairs) {
				if (pair.contains("@")) {
					String[] parts = pair.split("@", 2);
					addFilename(parts[0].trim(), parts[1].trim());
				}
			}
		}
	}
}
