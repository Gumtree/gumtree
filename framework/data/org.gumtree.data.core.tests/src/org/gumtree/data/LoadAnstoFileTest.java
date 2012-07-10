package org.gumtree.data;

import static org.gumtree.data.core.tests.DataTestConstants.PLUGIN_ID;
import static org.junit.Assert.assertEquals;

import org.eclipse.core.filesystem.IFileStore;
import org.gumtree.data.core.tests.DataTestObject;
import org.gumtree.data.core.tests.DataTestUtils;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;
import org.junit.Test;

public class LoadAnstoFileTest extends DataTestObject {

	private static final String DATA_PATH = "/data/ansto/QKKData.nx.hdf";
	
	@Test
	public void testReadStructure() throws Exception {
		IFileStore file = DataTestUtils.find(PLUGIN_ID, DATA_PATH);
		IDataset dataset = getFactory().createDatasetInstance(file.toURI());
		if (!dataset.isOpen()) {
			dataset.open();
		}
		
		IGroup rootGroup = dataset.getRootGroup();
		assertEquals(1, rootGroup.getGroupList().size());
		
		IGroup entryGroup = rootGroup.getGroupList().get(0);
		
		IGroup instrumentGroup = entryGroup.getGroup("instrument");
		assertEquals(7, instrumentGroup.getGroupList().size());
		
		IDataItem totalCount = entryGroup.getGroup("data").getDataItem("total_counts");
		assertEquals(12362698, totalCount.readScalarInt());
		
		dataset.close();
	}
	
}
