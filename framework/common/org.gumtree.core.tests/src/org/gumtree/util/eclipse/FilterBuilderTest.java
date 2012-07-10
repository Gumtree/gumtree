package org.gumtree.util.eclipse;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FilterBuilderTest {

	@Test
	public void testSimpleFilter() {
		FilterBuilder builder = new FilterBuilder("a", "1");
		String filter = builder.get();
		assertEquals("(a=1)", filter);
	}
	
	@Test
	public void testNotFiler() {
		FilterBuilder builder = new FilterBuilder("a", "1");
		String filter = builder.not().get();
		assertEquals("(!(a=1))", filter);
	}
	
	@Test
	public void testAndFiler() {
		FilterBuilder builder = new FilterBuilder("a", "1");
		String filter = builder.and("b", "2").get();
		assertEquals("(&(a=1)(b=2))", filter);
	}
	
	@Test
	public void testOrFiler() {
		FilterBuilder builder = new FilterBuilder("a", "1");
		String filter = builder.or("b", "2").get();
		assertEquals("(|(a=1)(b=2))", filter);
	}
	
	@Test
	public void testNotAndFiler() {
		FilterBuilder builder = new FilterBuilder("a", "1");
		String filter = builder.and("b", "2").not().get();
		assertEquals("(!(&(a=1)(b=2)))", filter);
	}
	
	@Test
	public void testAndNotFiler() {
		FilterBuilder builder = new FilterBuilder("a", "1");
		String filter = builder.not().and("b", "2").get();
		assertEquals("(&(!(a=1))(b=2))", filter);
	}
	
	@Test
	public void testAndOrFiler() {
		FilterBuilder builder = new FilterBuilder("a", "1");
		String filter = builder.or("b", "2").and("c", "3").get();
		assertEquals("(&(|(a=1)(b=2))(c=3))", filter);
	}
	
}
