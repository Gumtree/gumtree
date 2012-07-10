package org.gumtree.service.dataaccess.converter;

import static org.junit.Assert.assertNotNull;

import java.net.URISyntaxException;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.gumtree.core.tests.internal.Activator;
import org.gumtree.data.impl.NcFactory;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.service.dataaccess.converters.GumTreeDataConverter;
import org.gumtree.util.eclipse.EclipseUtils;
import org.junit.Test;

public class GumTreeDataConverterTest {

	static final String DIR_TEST_DATA = "data";
	static final String TEST_FILE_NAME = "simple.nx.hdf";

	@Test
	public void testGetDataset() throws CoreException, URISyntaxException {
		IFileStore dataFile = EclipseUtils.find(Activator.PLUGIN_ID,
				DIR_TEST_DATA + "/" + TEST_FILE_NAME);
		GumTreeDataConverter converter = new GumTreeDataConverter();
		converter.setFactory(new NcFactory());
		IDataset dataset = converter.convert(dataFile, IDataset.class, null);
		assertNotNull(dataset.getRootGroup().getGroup("entry1"));
	}

}
