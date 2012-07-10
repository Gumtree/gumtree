package org.gumtree.cs.sics.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.gumtree.sics.control.ISicsController;
import org.gumtree.sics.control.support.SicsController;
import org.junit.Test;

public class SicsControllerTest {

	@Test
	public void testSingleController() {
		ISicsController controller = new SicsController();
		
		controller.setId("sample_x");
		assertEquals("sample_x", controller.getId());
		
		controller.setDeviceId("samx");
		assertEquals("samx", controller.getDeviceId());
		
		assertEquals("/sample_x", controller.getPath());
		
		controller.disposeObject();
	}
	
	@Test
	public void testTreeController() {
		ISicsController parent = new SicsController();
		parent.setId("");
		
		SicsController sample = new SicsController();
		sample.setId("sample");
		parent.addChild(sample);
		
		SicsController sample_x = new SicsController();
		sample_x.setId("sample_x");
		sample.addChild(sample_x);
		
		assertEquals("/", parent.getPath());
		assertEquals("/sample", sample.getPath());
		assertEquals("/sample/sample_x", sample_x.getPath());
		
		assertEquals(sample, parent.getChildren()[0]);
		assertEquals(sample_x, sample.getChildren()[0]);
		
		assertEquals(sample, parent.getChild("sample"));
		assertEquals(sample_x, sample.getChild("sample_x"));
		assertNull(parent.getChild("name"));
		assertNull(sample_x.getChild("sample_y"));
		
		assertEquals(sample, parent.findChild("/sample"));
		assertEquals(sample_x, parent.findChild("/sample/sample_x"));
		assertEquals(sample_x, sample.findChild("/sample_x"));
		assertEquals(parent, parent.findChild("/"));
		assertNull(parent.findChild("/name"));
		assertNull(parent.findChild("/name/123"));
		assertNull(parent.findChild("/sample/sample_y"));
		
		sample_x.disposeObject();
		sample.disposeObject();
		parent.disposeObject();
	}
	
	public void testByModel() {
		
	}
	
}
