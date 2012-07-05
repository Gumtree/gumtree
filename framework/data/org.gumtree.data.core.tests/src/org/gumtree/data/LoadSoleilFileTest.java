package org.gumtree.data;

import static org.gumtree.data.core.tests.DataTestConstants.PLUGIN_ID;
import static org.junit.Assert.assertEquals;

import org.eclipse.core.filesystem.IFileStore;
import org.gumtree.data.core.tests.DataTestObject;
import org.gumtree.data.core.tests.DataTestUtils;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;
import org.junit.Test;

public class LoadSoleilFileTest extends DataTestObject {

	private static final String SIMPLE_DATA_PATH = "/data/soleil/Test_Nexus_File.nxs";
	
	private static final String SWING_DATA_PATH = "/data/soleil/FlyscanSwing_2011-04-11_15-44-11.nxs";
	
	@Test
	public void testReadSimpleStructure() throws Exception {
		IFileStore file = DataTestUtils.find(PLUGIN_ID, SIMPLE_DATA_PATH);
		IDataset dataset = getFactory().createDatasetInstance(file.toURI());
		if (!dataset.isOpen()) {
			dataset.open();
		}
		
		IGroup rootGroup = dataset.getRootGroup();
		assertEquals(1, rootGroup.getGroupList().size());
		
		IGroup entryGroup = rootGroup.getGroupList().get(0);
		
		IDataItem affiliation = entryGroup.getGroup("User").getDataItem("affiliation");
		assertEquals("Synchrotron SOLEIL", affiliation.readScalarString());
		
		IDataItem data_01 = entryGroup.getGroup("scan_data").getDataItem("data_01");
		assertEquals(1, data_01.getRank());
		// [ANSTO][Tony][2011-09-02] TODO: getDimensionList() returns null in SOLEIL's implementation
//		assertEquals(41, data_01.getDimensionList().get(0).getLength());
		assertEquals("A", data_01.getAttribute("units").getStringValue());
		
		IArray data_01Array = data_01.getData();
		assertEquals(1, data_01Array.getRank());
		assertEquals(41, data_01Array.getShape()[0]);
		assertEquals(4.04e-4, data_01Array.getDouble(data_01Array.getIndex().set0(0)), 1e-5);
		
		dataset.close();
	}
	
	@Test
	public void testReadSwingStructure() throws Exception {
		IFileStore file = DataTestUtils.find(PLUGIN_ID, SWING_DATA_PATH);
		IDataset dataset = getFactory().createDatasetInstance(file.toURI());
		if (!dataset.isOpen()) {
			dataset.open();
		}
		
		IGroup rootGroup = dataset.getRootGroup();
		assertEquals(1, rootGroup.getGroupList().size());
		
		IGroup entryGroup = rootGroup.getGroupList().get(0);
		
		// [ANSTO][Tony][2011-09-19] TODO: returns no attribute in SOLEIL's implementation
//		assertEquals("NXentry", entryGroup.getAttribute("NX_class").getStringValue());
		
		IDataItem affiliation = entryGroup.getGroup("User").getDataItem("affiliation");
		assertEquals("Synchrotron SOLEIL", affiliation.readScalarString());
		
		IDataItem data_01 = entryGroup.getGroup("scan_data").getDataItem("basler_image");
		assertEquals(3, data_01.getRank());
		assertEquals(10, data_01.getShape()[0]);
		assertEquals(494, data_01.getShape()[1]);
		assertEquals(659, data_01.getShape()[2]);
		
		IArray data_01Array = data_01.getData();
		assertEquals(25, data_01Array.getShort(data_01Array.getIndex().set0(0)));
	}
	
}
