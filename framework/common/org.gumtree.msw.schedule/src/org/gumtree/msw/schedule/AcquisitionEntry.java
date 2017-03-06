package org.gumtree.msw.schedule;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.gumtree.msw.elements.IDependencyProperty;

public class AcquisitionEntry {
	// fields
	public final String name;
	public final Set<IDependencyProperty> properties;	// not all properties can be changed
	public final Set<AcquisitionEntry> entries;
	
	// construction
	public AcquisitionEntry(String name, IDependencyProperty[] properties, AcquisitionEntry ... entries) {
		this.name = name;
		this.properties = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(properties)));
		this.entries = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(entries)));
	}
}
