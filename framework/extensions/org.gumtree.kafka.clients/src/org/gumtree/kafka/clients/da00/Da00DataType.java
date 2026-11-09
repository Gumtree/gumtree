package org.gumtree.kafka.clients.da00;

/**
 * Element type of a da00 variable's data, with the codes used in the schema
 * (da00_dtype).
 */
public enum Da00DataType {

	NONE(0, 0), INT8(1, 1), UINT8(2, 1), INT16(3, 2), UINT16(4, 2), INT32(5, 4), UINT32(6, 4), INT64(7, 8),
	UINT64(8, 8), FLOAT32(9, 4), FLOAT64(10, 8), C_STRING(11, 1);

	private final int code;

	private final int size;

	private Da00DataType(int code, int size) {
		this.code = code;
		this.size = size;
	}

	/** @return the code stored in the buffer */
	public int getCode() {
		return code;
	}

	/** @return bytes per element */
	public int getSize() {
		return size;
	}

	public static Da00DataType fromCode(int code) {
		for (Da00DataType type : values()) {
			if (type.code == code) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown da00 data type " + code);
	}

}
