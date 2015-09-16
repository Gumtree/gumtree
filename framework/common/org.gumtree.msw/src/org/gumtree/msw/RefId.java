package org.gumtree.msw;

public final class RefId {
	// finals
	public static final int XML_ID = 0;						// reference XML
	public static final int SERVER_ID = 1;					// server id (client id > 1)
	public static final int NONE_ID = Integer.MAX_VALUE;
	// e.g. #1-a51
	public static final char HASH = '#';
	public static final char DASH = '-';
	
	// fields
	private final int sourceId;		// for server this is 1 for clients this provided by server !!! 
	private final long objectId;	// determined by source

	// construction
	public RefId(int sourceId, long objectId) {
		this.sourceId = sourceId;
		this.objectId = objectId;
	}
	
	// properties
	public int getSourceId() {
		return sourceId;
	}
	public long getObjectId() {
		return objectId;
	}

	// methods
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj instanceof RefId) {
			RefId other = (RefId)obj;
			return (sourceId == other.sourceId) && (objectId == other.objectId);
		}
		return false;
	}
	@Override
	public int hashCode() {
		return Integer.reverse(sourceId) ^ Long.valueOf(objectId).hashCode();
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(HASH).append(Integer.toHexString(sourceId));
		sb.append(DASH).append(Long.toHexString(objectId));
		return sb.toString();
	}
	
	// static
	public static RefId parse(String s) {
		if ((s == null) || (s.lastIndexOf(HASH) != 0))
			return null;
		
		int i = s.indexOf(DASH);
		if (i == -1)
			return null;
		
		try {
			return new RefId(
					Integer.parseInt(s.substring(1, i), 16),
					Long.parseLong(s.substring(i + 1), 16));
		}
		catch (NumberFormatException e) {
			return null;
		}
	}
}
