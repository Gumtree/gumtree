package org.gumtree.util.eclipse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.gumtree.core.tests.internal.Activator;
import org.junit.Test;

public class EclipseUtilsTest {

	@Test
	public void testCreateEclipseContext() {
		IEclipseContext context = EclipseUtils.createEclipseContext(Activator
				.getContext());
		IEventBroker eventBroker = context.get(IEventBroker.class);
		assertNotNull(eventBroker);

		context.set("text", "Hello");
		InjectableObject injectableObject = ContextInjectionFactory.make(
				InjectableObject.class, context);
		assertEquals("Hello", injectableObject.getText());
	}

}

class InjectableObject {

	private String text;

	public String getText() {
		return text;
	}

	@Inject
	public void setText(@Named("text") String text) {
		this.text = text;
	}

}
