package org.gumtree.gumnix.sics.ui.internal.transaction;

import java.util.Calendar;
import java.util.Date;

public class SicsTransaction implements ISicsTransaction {

	private Date time;
	
	private String channelId;
	
	private String message;
	
	private TransactionType type;
	
	public SicsTransaction(String message, String channelId, TransactionType type) {
		time = Calendar.getInstance().getTime();
		this.channelId = channelId;
		this.message = message;
		this.type = type;
	}
	
	public String getChannelId() {
		return channelId;
	}

	public String getMessage() {
		return message;
	}

	public Date getTime() {
		return time;
	}

	public TransactionType getType() {
		return type;
	}

}
