package org.gumtree.service.preferences;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.gumtree.core.tests.internal.Activator;
import org.gumtree.service.preferences.support.PreferencesManager;
import org.gumtree.util.string.StringUtils;
import org.junit.Test;

public class UsabilityTest {

	@Test
	public void testCompleteUsageCycle() {
		// Use a new preferences file
		PreferencesManager preferencesManager = new PreferencesManager();
		preferencesManager.setPreferences(InstanceScope.INSTANCE
				.getNode(Activator.PLUGIN_ID));

		// Start a new object (cycle 1)
		Component component = new Component();
		component.setPreferencesManager(preferencesManager);
		component.activate();

		// Initially this is empty
		assertTrue(StringUtils.isEmpty(component.getText()));
		assertTrue(StringUtils.isEmpty(preferencesManager
				.get(Component.PROP_KEY_TEXT)));

		// Set to save into preferences
		component.setText("123");
		assertEquals("123", component.getText());
		assertEquals("123", preferencesManager.get(Component.PROP_KEY_TEXT));

		// Kill this object
		component.deactivate();

		// Create another object (cycle 2)
		component = new Component();
		component.setPreferencesManager(preferencesManager);
		component.activate();

		// Value should be persisted
		assertEquals("123", component.getText());
		assertEquals("123", preferencesManager.get(Component.PROP_KEY_TEXT));

		// Kill this object and reset preferences
		component.deactivate();
		preferencesManager.reset(Component.PROP_KEY_TEXT);
		preferencesManager.save();

		// Create another object (cycle 3)
		component = new Component();
		component.setPreferencesManager(preferencesManager);
		component.activate();

		// Should be empty after reset
		assertTrue(StringUtils.isEmpty(component.getText()));
		assertTrue(StringUtils.isEmpty(preferencesManager
				.get(Component.PROP_KEY_TEXT)));

		// Kill this object and set default via system properties
		component.deactivate();
		System.setProperty(Component.PROP_KEY_TEXT, "abc");

		// Create another object (cycle 4)
		component = new Component();
		component.setPreferencesManager(preferencesManager);
		component.activate();

		// Value should come from system properties
		assertEquals("abc", component.getText());
		assertEquals("abc", preferencesManager.get(Component.PROP_KEY_TEXT));
		
		// End of cycle
		component.deactivate();
		preferencesManager.reset(Component.PROP_KEY_TEXT);
		preferencesManager.save();
	}

	class Component {

		private static final String PROP_KEY_TEXT = "component.text";

		private String text;

		private IPreferencesManager preferencesManager;

		public Component() {
			super();
		}

		public void activate() {
			text = getPreferencesManager().get(PROP_KEY_TEXT);
		}

		public void deactivate() {
			text = null;
		}

		/*************************************************************************
		 * Components
		 *************************************************************************/

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
			getPreferencesManager().set(PROP_KEY_TEXT, text);
			getPreferencesManager().save();
		}

		public IPreferencesManager getPreferencesManager() {
			return preferencesManager;
		}

		public void setPreferencesManager(IPreferencesManager preferencesManager) {
			this.preferencesManager = preferencesManager;
		}

	}
}
