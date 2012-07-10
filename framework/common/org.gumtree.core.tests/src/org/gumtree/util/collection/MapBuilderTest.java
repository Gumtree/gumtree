package org.gumtree.util.collection;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

public class MapBuilderTest {

	@Test
	public void testMapBuilder() {
		Map<String, String> map = new MapBuilder<String, String>()
				.append("a", "1").append("b", "2").append("c", "3").get();
		assertEquals(3, map.size());
		assertEquals("1", map.get("a"));
		assertEquals("2", map.get("b"));
		assertEquals("3", map.get("c"));
	}
	
}
