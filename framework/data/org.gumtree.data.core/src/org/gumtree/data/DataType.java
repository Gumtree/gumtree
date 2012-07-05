/******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation,
 * Synchrotron SOLEIL and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     Norman XIONG (Bragg Institute) - initial API and implementation
 *     Clément RODRIGUEZ (SOLEIL) - initial API and implementation
 *     Tony LAM (Bragg Institute) - implementation
 ******************************************************************************/

package org.gumtree.data;

import java.nio.ByteBuffer;

/**
 * Data types for IArray.
 * 
 * @author nxi
 */
public enum DataType {

	/*************************************************************************
	 * Data types
	 *************************************************************************/
	BOOLEAN("boolean", 1),
	BYTE("byte", 1),
	CHAR("char", 1),
	SHORT("short", 2),
	INT("int", 4),
	LONG("long", 8),
	FLOAT("float", 4),
	DOUBLE("double", 8),

	/*************************************************************************
	 * Object types
	 *************************************************************************/
	SEQUENCE("Sequence", 4), // 32-bit index
	STRING("String", 4), // 32-bit index
	STRUCTURE("Structure", 1), // size meaningless

	ENUM1("enum1", 1), // byte
	ENUM2("enum2", 2), // short
	ENUM4("enum4", 4), // int

	OPAQUE("opaque", 1); // byte blobs

	private String niceName;
	private int size;

	/**
     * @param s String value
     * @param size integer value
	 */
	private DataType(final String s, final int size) {
		this.niceName = s;
		this.size = size;
	}

	/**
	 * The DataType name, eg "byte", "float", "String".
	 * 
	 * @return String value
	 */
	public String toString() {
		return niceName;
	}

	/**
	 * Size in bytes of one element of this data type. Strings dont know, so
	 * return 0. Structures return 1.
	 * 
	 * @return Size in bytes of one element of this data type.
	 */
	public int getSize() {
		return size;
	}

	/**
	 * The Object class type: Character, Byte, Float, Double, Short, Integer,
	 * Boolean, Long, String, StructureData.
	 * 
	 * @deprecated use getPrimitiveClassType()
	 * @return the primitive class type
	 */
	public Class<?> getClassType() {
		return getPrimitiveClassType();
	}

	/**
	 * The primitive class type: char, byte, float, double, short, int, long,
	 * boolean, String, StructureData.
	 * 
	 * @return the primitive class type
	 */
	public Class<?> getPrimitiveClassType() {
		if (this == DataType.FLOAT) {
			return float.class;
		}
		if (this == DataType.DOUBLE) {
			return double.class;
		}
		if ((this == DataType.SHORT) || (this == DataType.ENUM2)) {
			return short.class;
		}
		if ((this == DataType.INT) || (this == DataType.ENUM4)) {
			return int.class;
		}
		if ((this == DataType.BYTE) || (this == DataType.ENUM1)) {
			return byte.class;
		}
		if (this == DataType.CHAR) {
			return char.class;
		}
		if (this == DataType.BOOLEAN) {
			return boolean.class;
		}
		if (this == DataType.LONG) {
			return long.class;
		}
		if (this == DataType.STRING) {
			return String.class;
		}
		if (this == DataType.OPAQUE) {
			return ByteBuffer.class;
		}
		return null;
	}

	/**
	 * Is String or Char.
	 * 
	 * @return true if String or Char
	 */
	public boolean isString() {
		return (this == DataType.STRING) || (this == DataType.CHAR);
	}

	/**
	 * Is Byte, Float, Double, Int, Short, or Long.
	 * 
	 * @return true if numeric
	 */
	public boolean isNumeric() {
        boolean result = false;
        // Below test is weird but is done as it is to prevent having a boolean test complexity higher than 3
        if( (this == DataType.BYTE) || (this == DataType.FLOAT) ) {
            result = true;
        }
        else if( (this == DataType.DOUBLE) || (this == DataType.INT) ) {
            result = true;
        }
        else if ( (this == DataType.SHORT) || (this == DataType.LONG) ) {
            result = true;   
        }
        return result;
	}

	/**
	 * Is Byte, Int, Short, or Long.
	 * 
	 * @return true if integral
	 */
	public boolean isIntegral() {
		return (this == DataType.BYTE) || (this == DataType.INT)
				|| (this == DataType.SHORT) || (this == DataType.LONG);
	}

	/**
	 * Is this an enumeration types?
	 * 
	 * @return true if ENUM1, 2, or 4
	 */
	public boolean isEnum() {
		return (this == DataType.ENUM1) || (this == DataType.ENUM2)
				|| (this == DataType.ENUM4);
	}

	/**
	 * Find the DataType that matches this name.
	 * 
     * @param name find DataType with this name.
	 * @return DataType or null if no match.
	 */
	public static DataType getType(final String name) {
		if (name == null) {
			return null;
		}
		try {
			return valueOf(name.toUpperCase());
		} catch (IllegalArgumentException e) { // lame!
			return null;
		}
	}

	/**
	 * Find the DataType that matches this class.
	 * 
     * @param c primitive or object class, eg float.class or Float.class
	 * @return DataType or null if no match.
	 */
	public static DataType getType(final Class<?> c) {
		if ((c == float.class) || (c == Float.class)) {
			return DataType.FLOAT;
		}
		if ((c == double.class) || (c == Double.class)) {
			return DataType.DOUBLE;
		}
		if ((c == short.class) || (c == Short.class)) {
			return DataType.SHORT;
		}
		if ((c == int.class) || (c == Integer.class)) {
			return DataType.INT;
		}
		if ((c == byte.class) || (c == Byte.class)) {
			return DataType.BYTE;
		}
		if ((c == char.class) || (c == Character.class)) {
			return DataType.CHAR;
		}
		if ((c == boolean.class) || (c == Boolean.class)) {
			return DataType.BOOLEAN;
		}
		if ((c == long.class) || (c == Long.class)) {
			return DataType.LONG;
		}
		if (c == String.class) {
			return DataType.STRING;
		}
		if (c == ByteBuffer.class) {
			return DataType.OPAQUE;
		}
		return null;
	}

	/**
	 * widen an unsigned int to a long.
	 * 
     * @param i unsigned int
	 * @return equivilent long value
	 */
	public static long unsignedIntToLong(final int i) {
		return (i < 0) ? (long) i + 4294967296L : (long) i;
	}

	/**
	 * widen an unsigned short to an int.
	 * 
     * @param s unsigned short
	 * @return equivalent int value
	 */
	public static int unsignedShortToInt(final short s) {
		return (s & 0xffff);
	}

	/**
	 * widen an unsigned byte to a short.
	 * 
     * @param b unsigned byte
	 * @return equivalent short value
	 */
	public static short unsignedByteToShort(final byte b) {
		return (short) (b & 0xff);
	}

}