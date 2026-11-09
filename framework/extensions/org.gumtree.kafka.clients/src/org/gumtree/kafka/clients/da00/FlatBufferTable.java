package org.gumtree.kafka.clients.da00;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * Minimal read-only access to one table in a FlatBuffers buffer, enough for the
 * da00 schema. FlatBuffers data is always little-endian. Fields are addressed by
 * their vtable offset (4, 6, 8, ...), as in the generated Python code's
 * {@code self._tab.Offset(n)}.
 */
final class FlatBufferTable {

	private static final Charset UTF8 = Charset.forName("UTF-8");

	private final ByteBuffer buffer;

	private final int position;

	private final int vtable;

	private final int vtableSize;

	private FlatBufferTable(ByteBuffer buffer, int position) {
		this.buffer = buffer;
		this.position = checkPosition(buffer, position, 4);
		this.vtable = checkPosition(buffer, position - buffer.getInt(position), 4);
		this.vtableSize = buffer.getShort(vtable) & 0xFFFF;
	}

	/** The root table of a buffer. */
	static FlatBufferTable root(byte[] data) {
		ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
		checkPosition(buffer, 0, 8);
		return new FlatBufferTable(buffer, buffer.getInt(0));
	}

	/** The 4-character file identifier stored after the root offset. */
	static String fileIdentifier(byte[] data) {
		if (data == null || data.length < 8) {
			return null;
		}
		return new String(data, 4, 4, UTF8);
	}

	/** @return the absolute position of a field's value, or 0 if absent */
	private int field(int vtableOffset) {
		if (vtableOffset >= vtableSize) {
			return 0;
		}
		int relative = buffer.getShort(vtable + vtableOffset) & 0xFFFF;
		return relative == 0 ? 0 : position + relative;
	}

	/** Follows a uoffset stored at a position. */
	private int indirect(int at) {
		return checkPosition(buffer, at + buffer.getInt(at), 4);
	}

	boolean has(int vtableOffset) {
		return field(vtableOffset) != 0;
	}

	byte getByte(int vtableOffset, byte defaultValue) {
		int at = field(vtableOffset);
		return at == 0 ? defaultValue : buffer.get(at);
	}

	long getLong(int vtableOffset, long defaultValue) {
		int at = field(vtableOffset);
		return at == 0 ? defaultValue : buffer.getLong(at);
	}

	/** @return the string, or null if absent */
	String getString(int vtableOffset) {
		int at = field(vtableOffset);
		if (at == 0) {
			return null;
		}
		int start = indirect(at);
		int length = buffer.getInt(start);
		checkPosition(buffer, start + 4, length);
		return new String(buffer.array(), start + 4, length, UTF8);
	}

	/** @return the number of elements in a vector field, 0 if absent */
	int vectorLength(int vtableOffset) {
		int at = field(vtableOffset);
		return at == 0 ? 0 : buffer.getInt(indirect(at));
	}

	/** @return the absolute position of a vector's first element */
	private int vectorStart(int vtableOffset, int elementSize) {
		int start = indirect(field(vtableOffset));
		checkPosition(buffer, start + 4, buffer.getInt(start) * elementSize);
		return start + 4;
	}

	/** @return a copy of a byte vector, or an empty array if absent */
	byte[] getByteVector(int vtableOffset) {
		int length = vectorLength(vtableOffset);
		byte[] result = new byte[length];
		if (length > 0) {
			System.arraycopy(buffer.array(), vectorStart(vtableOffset, 1), result, 0, length);
		}
		return result;
	}

	/** @return a long vector, or an empty array if absent */
	long[] getLongVector(int vtableOffset) {
		int length = vectorLength(vtableOffset);
		long[] result = new long[length];
		if (length > 0) {
			int start = vectorStart(vtableOffset, 8);
			for (int i = 0; i < length; i++) {
				result[i] = buffer.getLong(start + i * 8);
			}
		}
		return result;
	}

	/** @return a string vector, or an empty array if absent */
	String[] getStringVector(int vtableOffset) {
		int length = vectorLength(vtableOffset);
		String[] result = new String[length];
		if (length > 0) {
			int start = vectorStart(vtableOffset, 4);
			for (int i = 0; i < length; i++) {
				int string = indirect(start + i * 4);
				int size = buffer.getInt(string);
				checkPosition(buffer, string + 4, size);
				result[i] = new String(buffer.array(), string + 4, size, UTF8);
			}
		}
		return result;
	}

	/** @return element j of a vector of tables */
	FlatBufferTable getTable(int vtableOffset, int j) {
		int start = vectorStart(vtableOffset, 4);
		return new FlatBufferTable(buffer, indirect(start + j * 4));
	}

	private static int checkPosition(ByteBuffer buffer, int position, int size) {
		if (position < 0 || size < 0 || position > buffer.capacity() - size) {
			throw new IllegalArgumentException("Corrupt FlatBuffers data: position " + position + " size "
					+ size + " outside a buffer of " + buffer.capacity() + " bytes");
		}
		return position;
	}

}
