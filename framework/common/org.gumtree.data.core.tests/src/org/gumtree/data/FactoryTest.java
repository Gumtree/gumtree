package org.gumtree.data;

import static org.gumtree.data.core.tests.DataTestConstants.PLUGIN_ID;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.eclipse.core.filesystem.IFileStore;
import org.gumtree.data.core.tests.DataTestObject;
import org.gumtree.data.core.tests.DataTestUtils;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IKey;
import org.junit.Test;

public class FactoryTest extends DataTestObject {

	private static final String DATA_PATH = "/data/ansto/QKKData.nx.hdf";
	
	@Test
	public void testOpenDataset() throws Exception {
		IFileStore file = DataTestUtils.find(PLUGIN_ID, DATA_PATH);
		IDataset dataset = getFactory().openDataset(file.toURI());
		assertEquals(getFactory().getName(), dataset.getFactoryName());
	}
	
	@Test
	public void testCreateDatasetInstance() throws Exception {
		IFileStore file = DataTestUtils.find(PLUGIN_ID, DATA_PATH);
		IDataset dataset = getFactory().createDatasetInstance(file.toURI());
		assertEquals(getFactory().getName(), dataset.getFactoryName());
	}
	
	@Test
	public void testCreateEmptyDatasetInstance() throws IOException {
		IDataset dataset = getFactory().createEmptyDatasetInstance();
		assertEquals(getFactory().getName(), dataset.getFactoryName());
	}
	
	@Test
	public void testCreate1DArray() {
		IArray array = getFactory().createArray(new int[] { 0, 1, 2, 3, 4 });
		assertEquals(getFactory().getName(), array.getFactoryName());
		assertEquals(0, array.getInt(array.getIndex().set0(0)));
		assertEquals(1, array.getInt(array.getIndex().set0(1)));
		assertEquals(4, array.getInt(array.getIndex().set0(4)));
	}
	
	@Test
	public void testCreateDoubleArray() {
		IArray array = getFactory().createDoubleArray(new double[] { 0.0, 1.0, 2.0,
				3.0, 4.0 });
		assertEquals(getFactory().getName(), array.getFactoryName());
		assertEquals(0.0, array.getDouble(array.getIndex().set0(0)), 0.1);
		assertEquals(1.0, array.getDouble(array.getIndex().set0(1)), 0.1);
		assertEquals(4.0, array.getDouble(array.getIndex().set0(4)), 0.1);
	}
	
	@Test
	public void testCreateStringAttribute() {
		IAttribute attribute = getFactory().createAttribute("att", "123");
		assertEquals(getFactory().getName(), attribute.getFactoryName());
		assertEquals("att", attribute.getName());
		assertEquals("123", attribute.getStringValue());
	}
	
	@Test
	public void testCreateSingleGroup() throws IOException {
		IGroup group = getFactory().createGroup("data");
		assertEquals(getFactory().getName(), group.getFactoryName());
		assertEquals("data", group.getShortName());
	}
	
	@Test
	public void testCreateAndAttachGroup() throws IOException {
		IDataset dataset = getFactory().createEmptyDatasetInstance();
		IGroup group = getFactory().createGroup(dataset.getRootGroup(), "data", true);
		assertEquals(getFactory().getName(), group.getFactoryName());
		assertEquals("data", group.getShortName());
		assertEquals(dataset.getRootGroup(), group.getParentGroup());
		assertEquals(group, dataset.getRootGroup().getGroup("data"));
		assertEquals(1, dataset.getRootGroup().getGroupList().size());
		assertEquals(group, dataset.getRootGroup().getGroupList().get(0));
	}
	
	@Test
	public void testCreateKey() {
		IKey key = getFactory().createKey("detector");
		assertEquals(getFactory().getName(), key.getFactoryName());
		assertEquals("detector", key.getName());
	}
	
	
}
