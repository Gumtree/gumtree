package au.gov.ansto.bragg.nbi.service.notification.support;

import org.gumtree.service.notification.support.CompositeNotification;

public class ExternalNotification extends CompositeNotification {

	private static final long serialVersionUID = 1766019778004611523L;

	private String sourceId;
	
	public ExternalNotification() {
		super();
	}
	
	public ExternalNotification(CompositeNotification notification, String sourceId) {
		this();
		appendNotification(notification.getNotifications());
		setSourceId(sourceId);
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	
}
