package org.gumtree.core.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ObjectFactoryTest {

	@Test
	public void testObjectFactory() throws ClassNotFoundException {
		Class<?> clazz = ObjectFactory.instantiateClass(String.class.getName());
		assertEquals(String.class, clazz);
		
		String string = ObjectFactory.instantiateObject(String.class);
		assertNotNull(string);
		
		string = ObjectFactory.instantiateObject(String.class, new Class[] { String.class }, "abc");
		assertEquals("abc", string);
	}
	
}
