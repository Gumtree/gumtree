package org.gumtree.util.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

public class CollectionUtilsTest {

	@Test
	public void testCreateMap() {
		Map<String, Integer> map = CollectionUtils.createMap("a", 1);
		assertEquals(1, map.size());
		assertTrue(map.containsKey("a"));
		assertEquals(Integer.valueOf(1), map.get("a"));
	}
	
}
