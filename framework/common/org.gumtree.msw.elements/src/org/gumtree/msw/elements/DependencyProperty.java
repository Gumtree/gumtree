package org.gumtree.msw.elements;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class DependencyProperty<TOwner extends Element, T> implements IDependencyProperty {
	// finals
	public static final Set<IDependencyProperty> EMPTY_SET = Collections.unmodifiableSet(new HashSet<IDependencyProperty>());
	
	// fields
	private final String name;
	private final Class<T> propertyType;
	
	// construction
	public DependencyProperty(String name, Class<T> propertyType) {
		this.name = name;
		this.propertyType = propertyType;
	}

	// properties
	@Override
	public String getName() {
		return name;
	}
	@Override
	public Class<T> getPropertyType() {
		return propertyType;
	}
	
	// methods
	@Override
	public boolean matches(String name, Class<?> propertyType) {
		return
				this.name.equalsIgnoreCase(name) &&
				this.propertyType.equals(propertyType);
	}
	
	// helper
	public static Set<IDependencyProperty> createSet(IDependencyProperty ... properties) {
		return Collections.unmodifiableSet(new LinkedHashSet<IDependencyProperty>(Arrays.asList(properties)));
	}
}
