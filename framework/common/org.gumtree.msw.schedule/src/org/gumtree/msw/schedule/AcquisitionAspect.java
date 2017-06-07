package org.gumtree.msw.schedule;

import org.gumtree.msw.elements.IDependencyProperty;

public class AcquisitionAspect extends AcquisitionEntry {
	// construction
	public AcquisitionAspect(String name, IDependencyProperty[] properties, AcquisitionEntry ... entries) {
		super(name, properties, entries);
	}
}
