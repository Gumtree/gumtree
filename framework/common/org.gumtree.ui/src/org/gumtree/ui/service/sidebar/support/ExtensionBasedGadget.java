package org.gumtree.ui.service.sidebar.support;

import static org.gumtree.util.eclipse.ExtensionRegistryConstants.*;
import static org.gumtree.ui.service.sidebar.support.GadgetRegistryConstants.*;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.core.object.IConfigurable;
import org.gumtree.core.object.ObjectCreateException;
import org.gumtree.core.object.ObjectFactory;
import org.gumtree.util.collection.IParameters;
import org.gumtree.util.collection.Parameters;

public class ExtensionBasedGadget extends AbstractGadget {

	private IConfigurationElement element;
	
	public ExtensionBasedGadget(IConfigurationElement element) {
		this.element = element;
		load();
	}
	
	@Override
	public Composite createGadget(Composite parent) {
		Composite gadgetComposite;
		try {
			gadgetComposite = (Composite) ObjectFactory
					.instantiateObject(element.getAttribute(ATTRIBUTE_CLASS),
							new Class[] { Composite.class, int.class }, parent,
							SWT.INHERIT_DEFAULT);
			if (gadgetComposite instanceof IConfigurable) {
				((IConfigurable) gadgetComposite).setParameters(getParameters());
				((IConfigurable) gadgetComposite).afterParametersSet();
			}
			return gadgetComposite;
		} catch (Exception e) {
			throw new ObjectCreateException(e);
		}
	}

	private void load() {
		setName(element.getAttribute(ATTRIBUTE_NAME));
		setPerspectives(element.getAttribute(ATTRIBUTE_PERSPECTIVES));
		try {
			setLevel(Integer.parseInt(element.getAttribute(ATTRIBUTE_LEVEL)));
		} catch (Exception e) {
		}
		IParameters parameters = new Parameters();
		if (element.getChildren(ELEMENT_PROPERTY) != null) {
			for (IConfigurationElement childElement : element.getChildren(ELEMENT_PROPERTY)) {
				String name = childElement.getAttribute(ATTRIBUTE_NAME);
				String value = childElement.getAttribute(ATTRIBUTE_VALUE);
				if (name != null && value != null) {
					parameters.put(name, value);
				}
				setParameters(parameters);
			}
		}
	}
	
}
