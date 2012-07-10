package org.gumtree.gumnix.sics.ui.internal.transaction;

import java.util.Date;

public interface ISicsTransaction {

	public Date getTime();
	
	public String getChannelId();
	
	public String getMessage();
	
	public TransactionType getType();
	
}
