package org.gumtree.service.preferences;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.gumtree.service.preferences.support.PreferencesManager;
import org.junit.Test;

public class PreferencesTest {

	@Test
	public void testGetFromNonExistingString() {
		IPreferencesManager manager = new PreferencesManager();
		assertEquals("", manager.get("some_key_0"));
	}

	@Test
	public void testGetFromSystemProperties() {
		IPreferencesManager manager = new PreferencesManager();

		// Nothing from system properties
		assertNull(System.getProperty("some_key_1"));
		assertEquals("", manager.get("some_key_1"));

		// Accessible after we set in the system properties
		System.setProperty("some_key_1", "some_value");
		assertEquals("some_value", manager.get("some_key_1"));
	}

	@Test
	public void testGetFromUserPreferences() {
		IPreferencesManager manager = new PreferencesManager();

		// Nothing from system properties
		assertNull(System.getProperty("some_key_2"));
		assertEquals("", manager.get("some_key_2"));

		// Accessible after we set in the system properties
		System.setProperty("some_key_2", "some_value");
		assertEquals("some_value", manager.get("some_key_2"));

		// Save preferences
		manager.set("some_key_2", "some_value_else");
		// Not saved but value is changed
		assertEquals("some_value_else", manager.get("some_key_2"));
		// Save to disk and value should be changed
		manager.save();
		assertEquals("some_value_else", manager.get("some_key_2"));
	}

	@Test
	public void testGetInt() {
		IPreferencesManager manager = new PreferencesManager();

		assertNull(System.getProperty("key_int"));
		assertEquals(0, manager.getInt("key_int"));

		System.setProperty("key_int", "100");
		assertEquals(100, manager.getInt("key_int"));

		// Set and reset user preferences
		manager.setInt("key_int", 101);
		assertEquals(101, manager.getInt("key_int"));
		manager.reset("key_int");
		assertEquals(100, manager.getInt("key_int"));
	}

	@Test
	public void testGetLong() {
		IPreferencesManager manager = new PreferencesManager();

		assertNull(System.getProperty("key_long"));
		assertEquals(0, manager.getLong("key_long"));

		System.setProperty("key_long", "100");
		assertEquals(100, manager.getLong("key_long"));

		// Set and reset user preferences
		manager.setLong("key_long", 101);
		assertEquals(101, manager.getLong("key_long"));
		manager.reset("key_long");
		assertEquals(100, manager.getLong("key_long"));
	}

	@Test
	public void testGetFloat() {
		IPreferencesManager manager = new PreferencesManager();

		assertNull(System.getProperty("key_float_0"));
		assertEquals(0.0f, manager.getFloat("key_float_0"), 0.0f);

		System.setProperty("key_float_1", "1.01");
		assertEquals(1.01f, manager.getFloat("key_float_1"), 0.0f);

		// Illegal version, but should be cause error
		System.setProperty("key_float_2", "hello");
		assertEquals(0.0f, manager.getFloat("key_float_2"), 0.0f);

		// Set and reset user preferences
		manager.setFloat("key_float_1", 2.01f);
		assertEquals(2.01f, manager.getFloat("key_float_1"), 0.0f);
		manager.reset("key_float_1");
		assertEquals(1.01f, manager.getFloat("key_float_1"), 0.0f);
	}

	@Test
	public void testGetDouble() {
		IPreferencesManager manager = new PreferencesManager();

		assertNull(System.getProperty("key_double_0"));
		assertEquals(0.0, manager.getDouble("key_double_0"), 0.0f);

		System.setProperty("key_double_1", "1.01234");
		assertEquals(1.01234, manager.getDouble("key_double_1"), 0.0);

		// Illegal version, but should be cause error
		System.setProperty("key_double_2", "hello");
		assertEquals(0.0, manager.getDouble("key_double_2"), 0.0);

		// Set and reset user preferences
		manager.setDouble("key_double_1", 2.01234);
		assertEquals(2.01234, manager.getDouble("key_double_1"), 0.0);
		manager.reset("key_double_1");
		assertEquals(1.01234, manager.getDouble("key_double_1"), 0.0);
	}

	@Test
	public void testGetBoolean() {
		IPreferencesManager manager = new PreferencesManager();

		assertNull(System.getProperty("key_boolean_0"));
		assertFalse(manager.getBoolean("key_boolean_0"));

		System.setProperty("key_boolean_1", "true");
		assertTrue(manager.getBoolean("key_boolean_1"));

		System.setProperty("key_boolean_2", "hello");
		assertFalse(manager.getBoolean("key_boolean_2"));

		// Set and reset user preferences
		manager.setBoolean("key_boolean_1", false);
		assertFalse(manager.getBoolean("key_boolean_1"));
		manager.reset("key_boolean_1");
		assertTrue(manager.getBoolean("key_boolean_1"));
	}

}