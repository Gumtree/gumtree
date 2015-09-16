package org.gumtree.msw.schedule;

import org.gumtree.msw.elements.IDependencyProperty;

public class AcquisitionEntry extends AcquisitionAspect {
	// e.g. Configuration contains Transmission/Scattering but the modification is just called Measurement
	public final String modificationName;

	// construction
	public AcquisitionEntry(String name, IDependencyProperty[] properties, AcquisitionEntry ... entries) {
		this(name , name, properties, entries);
	}
	public AcquisitionEntry(String name, String modificationName, IDependencyProperty[] properties, AcquisitionEntry ... entries) {
		super(name, properties, entries);
		this.modificationName = modificationName;
	}
}
