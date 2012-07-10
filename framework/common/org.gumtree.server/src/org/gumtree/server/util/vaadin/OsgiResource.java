package org.gumtree.server.util.vaadin;

import java.io.File;

import org.gumtree.util.eclipse.OsgiUtils;

import com.vaadin.Application;
import com.vaadin.terminal.FileResource;

public class OsgiResource extends FileResource {

	private static final long serialVersionUID = 3452034409493863216L;

	public OsgiResource(String bundleId, String relativePath,
			Application application) throws Exception {
		super(new File(OsgiUtils.findFilePath(bundleId, relativePath)),
				application);
	}

}
