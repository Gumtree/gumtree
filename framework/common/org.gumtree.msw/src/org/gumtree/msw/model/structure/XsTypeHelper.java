package org.gumtree.msw.model.structure;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.xs.XSConstants;

public final class XsTypeHelper {
	// finals
	private static final Map<Class<?>, ISerializer> serializers;
	
	// construction
	static {
		serializers = new HashMap<Class<?>, ISerializer>();
		serializers.put(String.class, StringSerializer.DEFAULT);
		serializers.put(Boolean.class, BooleanSerializer.DEFAULT);
		serializers.put(Float.class, FloatSerializer.DEFAULT);
		serializers.put(Double.class, DoubleSerializer.DEFAULT);
		serializers.put(Byte.class, ByteSerializer.DEFAULT);
		serializers.put(Short.class, ShortSerializer.DEFAULT);
		serializers.put(Integer.class, IntegerSerializer.DEFAULT);
		serializers.put(Long.class, LongSerializer.DEFAULT);
		serializers.put(BigInteger.class, BigIntegerSerializer.DEFAULT);
	}
	
	// methods
	public static Class<?> type(short valueType) {
		final Class<?>[] result = new Class<?>[1];
		accept(valueType, new IVisitor() {
			@Override
			public void visit(Class<BigInteger> clazz, BigIntegerSerializer serializer) {
				result[0] = clazz;
			}
			@Override
			public void visit(Class<Long> clazz, LongSerializer serializer) {
				result[0] = clazz;
			}			
			@Override
			public void visit(Class<Integer> clazz, IntegerSerializer serializer) {
				result[0] = clazz;
			}			
			@Override
			public void visit(Class<Short> clazz, ShortSerializer serializer) {
				result[0] = clazz;
			}			
			@Override
			public void visit(Class<Byte> clazz, ByteSerializer serializer) {
				result[0] = clazz;
			}			
			@Override
			public void visit(Class<Double> clazz, DoubleSerializer serializer) {
				result[0] = clazz;
			}			
			@Override
			public void visit(Class<Float> clazz, FloatSerializer serializer) {
				result[0] = clazz;
			}			
			@Override
			public void visit(Class<Boolean> clazz, BooleanSerializer serializer) {
				result[0] = clazz;
			}			
			@Override
			public void visit(Class<String> clazz, StringSerializer serializer) {
				result[0] = clazz;
			}
		});
		return result[0];
	}
	public static ISerializer serializer(short valueType) {
		final ISerializer[] result = new ISerializer[1];
		accept(valueType, new IVisitor() {
			@Override
			public void visit(Class<BigInteger> clazz, BigIntegerSerializer serializer) {
				result[0] = serializer;
			}
			@Override
			public void visit(Class<Long> clazz, LongSerializer serializer) {
				result[0] = serializer;
			}			
			@Override
			public void visit(Class<Integer> clazz, IntegerSerializer serializer) {
				result[0] = serializer;
			}			
			@Override
			public void visit(Class<Short> clazz, ShortSerializer serializer) {
				result[0] = serializer;
			}			
			@Override
			public void visit(Class<Byte> clazz, ByteSerializer serializer) {
				result[0] = serializer;
			}			
			@Override
			public void visit(Class<Double> clazz, DoubleSerializer serializer) {
				result[0] = serializer;
			}			
			@Override
			public void visit(Class<Float> clazz, FloatSerializer serializer) {
				result[0] = serializer;
			}			
			@Override
			public void visit(Class<Boolean> clazz, BooleanSerializer serializer) {
				result[0] = serializer;
			}			
			@Override
			public void visit(Class<String> clazz, StringSerializer serializer) {
				result[0] = serializer;
			}
		});
		return result[0];
	}
	public static ISerializer serializer(Class<?> valueClass) {
		return serializers.get(valueClass);
	}
	public static boolean accept(short valueType, IVisitor visitor) {
		switch (valueType) {
		case XSConstants.STRING_DT:
			visitor.visit(String.class, StringSerializer.DEFAULT);
			return true;
			
		case XSConstants.BOOLEAN_DT:
			visitor.visit(Boolean.class, BooleanSerializer.DEFAULT);
			return true;
			
		case XSConstants.FLOAT_DT:
			visitor.visit(Float.class, FloatSerializer.DEFAULT);
			return true;
			
		case XSConstants.DOUBLE_DT:
			visitor.visit(Double.class, DoubleSerializer.DEFAULT);
			return true;
						
		case XSConstants.BYTE_DT:
			visitor.visit(Byte.class, ByteSerializer.DEFAULT);
			return true;
			
		case XSConstants.SHORT_DT:
		case XSConstants.UNSIGNEDBYTE_DT:
			visitor.visit(Short.class, ShortSerializer.DEFAULT);
			return true;

		case XSConstants.INT_DT:
		case XSConstants.UNSIGNEDSHORT_DT:
			visitor.visit(Integer.class, IntegerSerializer.DEFAULT);
			return true;

		case XSConstants.LONG_DT:
		case XSConstants.UNSIGNEDINT_DT:
			visitor.visit(Long.class, LongSerializer.DEFAULT);
			return true;

		case XSConstants.INTEGER_DT:
		case XSConstants.UNSIGNEDLONG_DT:
		case XSConstants.POSITIVEINTEGER_DT:
		case XSConstants.NEGATIVEINTEGER_DT:
		case XSConstants.NONPOSITIVEINTEGER_DT:
		case XSConstants.NONNEGATIVEINTEGER_DT:
			visitor.visit(BigInteger.class, BigIntegerSerializer.DEFAULT);
			return true;

		default:
			return false;
		}
	}
	
	// visitor
	public static interface IVisitor {
		// methods
		public void visit(Class<String> clazz, StringSerializer serializer);
		public void visit(Class<Boolean> clazz, BooleanSerializer serializer);
		public void visit(Class<Float> clazz, FloatSerializer serializer);
		public void visit(Class<Double> clazz, DoubleSerializer serializer);
		public void visit(Class<Byte> clazz, ByteSerializer serializer);
		public void visit(Class<Short> clazz, ShortSerializer serializer);
		public void visit(Class<Integer> clazz, IntegerSerializer serializer);
		public void visit(Class<Long> clazz, LongSerializer serializer);
		public void visit(Class<BigInteger> clazz, BigIntegerSerializer serializer);
	}

	// Serialization
	public static interface ISerializer {
		public Object defaultValue();
		public Object clone(Object value);
		public String serialize(Object value);
		public Object deserialize(String value) throws IllegalArgumentException;
	}
	
	// implementation
	public static class StringSerializer implements ISerializer {
		// finals
		public static StringSerializer DEFAULT = new StringSerializer();
		
		// methods
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
		public String deserialize(String value) {
			return value;
		}
	}
	public static class BooleanSerializer implements ISerializer {
		// finals
		public static BooleanSerializer DEFAULT = new BooleanSerializer();
		
		// methods
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
		public Boolean deserialize(String value) throws IllegalArgumentException {
			if ("true".equalsIgnoreCase(value))
				return Boolean.TRUE;
			if ("false".equalsIgnoreCase(value))
				return Boolean.FALSE;

			throw new IllegalArgumentException();
		}
	}
	// numeric
	public static abstract class NumericSerializer implements ISerializer {
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
	public static class FloatSerializer extends NumericSerializer {
		// finals
		public static FloatSerializer DEFAULT = new FloatSerializer();
		
		// methods
		@Override
		public Float defaultValue() {
			return (float)0;
		}
		@Override
		public Float deserialize(String value) throws IllegalArgumentException {
			return Float.parseFloat(value);
		}
	}
	public static class DoubleSerializer extends NumericSerializer {
		// finals
		public static DoubleSerializer DEFAULT = new DoubleSerializer();
		
		// methods
		@Override
		public Double defaultValue() {
			return (double)0;
		}
		@Override
		public Double deserialize(String value) throws IllegalArgumentException {
			return Double.parseDouble(value);
		}
	}
	public static class ByteSerializer extends NumericSerializer {
		// finals
		public static ByteSerializer DEFAULT = new ByteSerializer();
		
		// methods
		@Override
		public Byte defaultValue() {
			return (byte)0;
		}
		@Override
		public Byte deserialize(String value) throws IllegalArgumentException {
			return Byte.parseByte(value);
		}
	}
	public static class ShortSerializer extends NumericSerializer {
		// finals
		public static ShortSerializer DEFAULT = new ShortSerializer();
		
		// methods
		@Override
		public Short defaultValue() {
			return (short)0;
		}
		@Override
		public Short deserialize(String value) throws IllegalArgumentException {
			return Short.parseShort(value);
		}
	}
	public static class IntegerSerializer extends NumericSerializer {
		// finals
		public static IntegerSerializer DEFAULT = new IntegerSerializer();
		
		// methods
		@Override
		public Integer defaultValue() {
			return (int)0;
		}
		@Override
		public Integer deserialize(String value) throws IllegalArgumentException {
			return Integer.parseInt(value);
		}
	}
	public static class LongSerializer extends NumericSerializer {
		// finals
		public static LongSerializer DEFAULT = new LongSerializer();
		
		// methods
		@Override
		public Long defaultValue() {
			return (long)0;
		}
		@Override
		public Long deserialize(String value) throws IllegalArgumentException {
			return Long.parseLong(value);
		}
	}
	public static class BigIntegerSerializer extends NumericSerializer {
		// finals
		public static BigIntegerSerializer DEFAULT = new BigIntegerSerializer();
		
		// methods
		@Override
		public BigInteger defaultValue() {
			return BigInteger.ZERO;
		}
		@Override
		public BigInteger deserialize(String value) throws IllegalArgumentException {
			return new BigInteger(value);
		}
	}
}
