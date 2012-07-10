package org.gumtree.ui.service.sidebar.support;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.core.object.IConfigurable;
import org.gumtree.core.object.ObjectCreateException;
import org.gumtree.core.object.ObjectFactory;

public class UserDefinedGadget extends AbstractGadget {

	private String classname;

	public UserDefinedGadget() {
		super();
	}
	
	public UserDefinedGadget(String name, String classname) {
		setName(name);
		setClassname(classname);
	}

	public String getClassname() {
		return classname;
	}

	public void setClassname(String classname) {
		this.classname = classname;
	}

	@Override
	public Composite createGadget(Composite parent) {
		Composite gadgetComposite;
		try {
			gadgetComposite = (Composite) ObjectFactory.instantiateObject(
					getClassname(), new Class[] { Composite.class, int.class },
					parent, SWT.INHERIT_DEFAULT);
			if (gadgetComposite instanceof IConfigurable) {
				((IConfigurable) gadgetComposite)
						.setParameters(getParameters());
				((IConfigurable) gadgetComposite).afterParametersSet();
			}
			return gadgetComposite;
		} catch (Exception e) {
			throw new ObjectCreateException(e);
		}
	}

}
