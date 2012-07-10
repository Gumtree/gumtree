package org.gumtree.vis.interfaces;

import java.io.File;
import java.io.IOException;

public interface IExporter {

	public void export(File file, IDataset dataset) throws IOException;
	
	public String getExtensionName();
}
