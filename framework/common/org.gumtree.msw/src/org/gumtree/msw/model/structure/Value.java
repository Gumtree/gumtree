package org.gumtree.msw.model.structure;

import java.math.BigInteger;

import org.apache.xerces.xs.datatypes.XSDecimal;
import org.apache.xerces.xs.datatypes.XSDouble;
import org.apache.xerces.xs.datatypes.XSFloat;
import org.gumtree.msw.model.structure.XsTypeHelper.BigIntegerSerializer;
import org.gumtree.msw.model.structure.XsTypeHelper.BooleanSerializer;
import org.gumtree.msw.model.structure.XsTypeHelper.ByteSerializer;
import org.gumtree.msw.model.structure.XsTypeHelper.DoubleSerializer;
import org.gumtree.msw.model.structure.XsTypeHelper.FloatSerializer;
import org.gumtree.msw.model.structure.XsTypeHelper.IntegerSerializer;
import org.gumtree.msw.model.structure.XsTypeHelper.LongSerializer;
import org.gumtree.msw.model.structure.XsTypeHelper.ShortSerializer;
import org.gumtree.msw.model.structure.XsTypeHelper.StringSerializer;

final class Value {
	// fields
	private Object value;
	private final Class<?> valueClass;
	private final XsTypeHelper.ISerializer serializer;
	private final FacetList facets;
	
	// construction
	public Value(Class<?> valueClass, FacetList facets) {
		this.valueClass = valueClass;
		this.serializer = XsTypeHelper.serializer(valueClass);
		this.facets = facets;

		this.value = serializer.defaultValue();
	}
	public <T> Value(Class<T> valueClass, T value, FacetList facets) {
		this.valueClass = valueClass;
		this.serializer = XsTypeHelper.serializer(valueClass);
		this.facets = facets;
		
		this.value = serializer.clone(value);
	}
	private <T> Value(Class<T> valueClass, T value, XsTypeHelper.ISerializer serializer, FacetList facets) {
		this.valueClass = valueClass;
		this.serializer = serializer;
		this.facets = facets;
		
		this.value = value;
	}
	private Value(Value reference) {
		this.valueClass = reference.valueClass;
		this.serializer = reference.serializer;
		this.facets = reference.facets;

		this.value = serializer.clone(reference.value);
	}
	
	// static
	public static Value from(short valueType, final Object value, final FacetList facets) {
		if (value == null)
			return null;
		
		final Value[] result = new Value[1];
		boolean supported = XsTypeHelper.accept(valueType, new XsTypeHelper.IVisitor() {
			@Override
			public void visit(Class<String> clazz, StringSerializer serializer) {
				result[0] = new Value(clazz, (String)value, serializer, facets);
			}
			@Override
			public void visit(Class<Boolean> clazz, BooleanSerializer serializer) {
				result[0] = new Value(clazz, (Boolean)value, serializer, facets);
			}
			@Override
			public void visit(Class<Float> clazz, FloatSerializer serializer) {
				result[0] = new Value(clazz, ((XSFloat)value).getValue(), serializer, facets);
			}
			@Override
			public void visit(Class<Double> clazz, DoubleSerializer serializer) {
				result[0] = new Value(clazz, ((XSDouble)value).getValue(), serializer, facets);
			}
			@Override
			public void visit(Class<Byte> clazz, ByteSerializer serializer) {
				result[0] = new Value(clazz, ((XSDecimal)value).getByte(), serializer, facets);
			}
			@Override
			public void visit(Class<Short> clazz, ShortSerializer serializer) {
				result[0] = new Value(clazz, ((XSDecimal)value).getShort(), serializer, facets);
			}
			@Override
			public void visit(Class<Integer> clazz, IntegerSerializer serializer) {
				result[0] = new Value(clazz, ((XSDecimal)value).getInt(), serializer, facets);
			}
			@Override
			public void visit(Class<Long> clazz, LongSerializer serializer) {
				result[0] = new Value(clazz, ((XSDecimal)value).getLong(), serializer, facets);
			}
			@Override
			public void visit(Class<BigInteger> clazz, BigIntegerSerializer serializer) {
				result[0] = new Value(clazz, ((XSDecimal)value).getBigInteger(), serializer, facets);
			}
		});
		
		if (supported)
			return result[0];

		System.out.println(String.format("WARNING: unsupported value type (%d)", valueType));
		return null;
	}
	
	// properties
	public Class<?> getValueClass() {
		return valueClass;
	}

	// methods
	public Object get() {
		return value;
	}
	public boolean validate(Object value) {
		if (!valueClass.isInstance(value))
			return false;
		
		return facets.validate(value);
	}
	public boolean set(Object value) {
		if (!validate(value))
			return false;
		
		this.value = value;
		return true;
	}
	// serialization
	@Override
	public Value clone() {
		return new Value(this);
	}
	public String serialize() {
		return serializer.serialize(value);
	}
	public boolean deserialize(String value) {
		try {
			this.value = serializer.deserialize(value);
			return true;
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}
}
