package org.gumtree.gumnix.sics.internal.io;

public class MessageContainer {

	private String channelId;
	
	private String message;
	
	public MessageContainer(String channelId, String message) {
		this.channelId = channelId;
		this.message = message;
	}

	public String getChannelId() {
		return channelId;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((channelId == null) ? 0 : channelId.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MessageContainer other = (MessageContainer) obj;
		if (channelId == null) {
			if (other.channelId != null)
				return false;
		} else if (!channelId.equals(other.channelId))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		return true;
	}
	
}
