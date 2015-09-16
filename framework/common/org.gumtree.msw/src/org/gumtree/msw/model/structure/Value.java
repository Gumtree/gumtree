package org.gumtree.msw.model.structure;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.datatypes.XSDecimal;
import org.apache.xerces.xs.datatypes.XSDouble;
import org.apache.xerces.xs.datatypes.XSFloat;

final class Value {
	// finals
	private static final Map<Class<?>, ISerializer> serializers;
	
	// fields
	private Object value;
	private final Class<?> valueClass;
	private final ISerializer serializer;
	
	// construction
	static {
		serializers = new HashMap<Class<?>, ISerializer>();
		serializers.put(String.class, new StringSerializer());
		serializers.put(Boolean.class, new BooleanSerializer());
		serializers.put(Float.class, new FloatSerializer());
		serializers.put(Double.class, new DoubleSerializer());
		serializers.put(Byte.class, new ByteSerializer());
		serializers.put(Short.class, new ShortSerializer());
		serializers.put(Integer.class, new IntSerializer());
		serializers.put(Long.class, new LongSerializer());
		serializers.put(BigInteger.class, new BigIntegerSerializer());
	}
	public Value(Class<?> valueClass) {
		this.valueClass = valueClass;
		this.serializer = serializers.get(valueClass);

		this.value = serializer.defaultValue();
	}
	public Value(Object value, Class<?> valueClass) {
		this.valueClass = valueClass;
		this.serializer = serializers.get(valueClass);
		
		this.value = serializer.clone(value);
	}
	private Value(Value reference) {
		valueClass = reference.valueClass;
		serializer = reference.serializer;

		value = serializer.clone(reference.value);
	}
	
	// static
	public static Value from(short valueType, Object value) {
		if (value == null)
			return null;
		
		switch (valueType) {
		case XSConstants.STRING_DT:
			return new Value((String)value, String.class);
			
		case XSConstants.BOOLEAN_DT:
			return new Value((Boolean)value, Boolean.class);
			
		case XSConstants.FLOAT_DT:
			return new Value(((XSFloat)value).getValue(), Float.class);
			
		case XSConstants.DOUBLE_DT:
			return new Value(((XSDouble)value).getValue(), Double.class);
						
		case XSConstants.BYTE_DT:
			return new Value(((XSDecimal)value).getByte(), Byte.class);
			
		case XSConstants.SHORT_DT:
		case XSConstants.UNSIGNEDBYTE_DT:
			return new Value(((XSDecimal)value).getShort(), Short.class);

		case XSConstants.INT_DT:
		case XSConstants.UNSIGNEDSHORT_DT:
			return new Value(((XSDecimal)value).getInt(), Integer.class);

		case XSConstants.LONG_DT:
		case XSConstants.UNSIGNEDINT_DT:
			return new Value(((XSDecimal)value).getLong(), Long.class);

		case XSConstants.INTEGER_DT:
		case XSConstants.UNSIGNEDLONG_DT:
		case XSConstants.POSITIVEINTEGER_DT:
		case XSConstants.NEGATIVEINTEGER_DT:
		case XSConstants.NONPOSITIVEINTEGER_DT:
		case XSConstants.NONNEGATIVEINTEGER_DT:
			return new Value(((XSDecimal)value).getBigInteger(), BigInteger.class);

		default:
			unsupported(valueType);
			return null;
		}
	}
	public static Class<?> type(short valueType) {
		switch (valueType) {
		case XSConstants.STRING_DT:
			return String.class;
			
		case XSConstants.BOOLEAN_DT:
			return Boolean.class;
			
		case XSConstants.FLOAT_DT:
			return Float.class;
			
		case XSConstants.DOUBLE_DT:
			return Double.class;
						
		case XSConstants.BYTE_DT:
			return Byte.class;
			
		case XSConstants.SHORT_DT:
		case XSConstants.UNSIGNEDBYTE_DT:
			return Short.class;

		case XSConstants.INT_DT:
		case XSConstants.UNSIGNEDSHORT_DT:
			return Integer.class;

		case XSConstants.LONG_DT:
		case XSConstants.UNSIGNEDINT_DT:
			return Long.class;

		case XSConstants.INTEGER_DT:
		case XSConstants.UNSIGNEDLONG_DT:
		case XSConstants.POSITIVEINTEGER_DT:
		case XSConstants.NEGATIVEINTEGER_DT:
		case XSConstants.NONPOSITIVEINTEGER_DT:
		case XSConstants.NONNEGATIVEINTEGER_DT:
			return BigInteger.class;

		default:
			unsupported(valueType);
			return null;
		}
	}
	
	// properties
	public Class<?> getValueClass() {
		return valueClass;
	}

	// methods
	public Object get() {
		return value;
	}
	public boolean set(Object value) {
		if (!valueClass.isInstance(value))
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
	// helpers
	private static void unsupported(short valueType) {
		System.out.println(String.format("WARNING: unsupported value type (%d)", valueType));		
	}

	// Serialization
	private static interface ISerializer {
		public Object defaultValue();
		public Object clone(Object value);
		public String serialize(Object value);
		public Object deserialize(String value) throws IllegalArgumentException;
	}
	// implementation
	private static class StringSerializer implements ISerializer {
		@Override
		public Object defaultValue() {
			return "";
		}
		@Override
		public Object clone(Object value) {
			return value; // immutable
		}
		@Override
		public String serialize(Object value) {
			return (String)value;
		}
		@Override
		public Object deserialize(String value) {
			return value;
		}
	}
	private static class BooleanSerializer implements ISerializer {
		@Override
		public Object defaultValue() {
			return false;
		}
		@Override
		public Object clone(Object value) {
			return value; // immutable
		}
		@Override
		public String serialize(Object value) {
			return (boolean)value ? "true" : "false";
		}
		@Override
		public Object deserialize(String value) throws IllegalArgumentException {
			if ("true".equalsIgnoreCase(value))
				return Boolean.TRUE;
			if ("false".equalsIgnoreCase(value))
				return Boolean.FALSE;

			throw new IllegalArgumentException();
		}
	}
	// numeric
	private static abstract class NumericSerializer implements ISerializer {
		@Override
		public Object clone(Object value) {
			return value; // immutable
		}
		@Override
		public String serialize(Object value) {
			if (value != null)
				return value.toString();
			else
				return null;
		}
	}
	private static class FloatSerializer extends NumericSerializer {
		@Override
		public Object defaultValue() {
			return (float)0;
		}
		@Override
		public Object deserialize(String value) throws IllegalArgumentException {
			return Float.parseFloat(value);
		}
	}
	private static class DoubleSerializer extends NumericSerializer {
		@Override
		public Object defaultValue() {
			return (double)0;
		}
		@Override
		public Object deserialize(String value) throws IllegalArgumentException {
			return Double.parseDouble(value);
		}
	}
	private static class ByteSerializer extends NumericSerializer {
		@Override
		public Object defaultValue() {
			return (byte)0;
		}
		@Override
		public Object deserialize(String value) throws IllegalArgumentException {
			return Byte.parseByte(value);
		}
	}
	private static class ShortSerializer extends NumericSerializer {
		@Override
		public Object defaultValue() {
			return (short)0;
		}
		@Override
		public Object deserialize(String value) throws IllegalArgumentException {
			return Short.parseShort(value);
		}
	}
	private static class IntSerializer extends NumericSerializer {
		@Override
		public Object defaultValue() {
			return (int)0;
		}
		@Override
		public Object deserialize(String value) throws IllegalArgumentException {
			return Integer.parseInt(value);
		}
	}
	private static class LongSerializer extends NumericSerializer {
		@Override
		public Object defaultValue() {
			return (long)0;
		}
		@Override
		public Object deserialize(String value) throws IllegalArgumentException {
			return Long.parseLong(value);
		}
	}
	private static class BigIntegerSerializer extends NumericSerializer {
		@Override
		public Object defaultValue() {
			return BigInteger.ZERO;
		}
		@Override
		public Object deserialize(String value) throws IllegalArgumentException {
			return new BigInteger(value);
		}
	}
}
