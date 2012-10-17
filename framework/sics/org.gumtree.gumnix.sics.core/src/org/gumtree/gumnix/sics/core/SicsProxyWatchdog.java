package org.gumtree.gumnix.sics.core;

import org.gumtree.core.management.IManageableBean;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.gumnix.sics.io.ISicsProxyWatchdog;
import org.gumtree.service.notification.INotificationService;
import org.gumtree.service.notification.support.EmailNotification;

public class SicsProxyWatchdog implements ISicsProxyWatchdog, IManageableBean {

	private long notificationSent;

	public SicsProxyWatchdog() {
		super();
		notificationSent = 0;
		SicsCore.getDefaultProxy().setWatchdog(this);
	}

	@Override
	public long getTimeout() {
		return SicsCoreProperties.PROXY_TIMEOUT.getLong();
	}

	@Override
	public void setTimeout(long timeoutInSecond) {
		SicsCoreProperties.PROXY_TIMEOUT.setLong(timeoutInSecond).save();
	}

	public boolean isSendEmailNotification() {
		return SicsCoreProperties.WATCHDOG_NOTIFY.getBoolean();
	}

	public void setSendEmailNotification(boolean sendEmailNotification) {
		SicsCoreProperties.WATCHDOG_NOTIFY.setBoolean(sendEmailNotification).save();
	}

	public String getMessageSubject() {
		return SicsCoreProperties.WATCHDOG_NOTIFICATION_SUBJECT.getValue();
	}

	public void setMessageSubject(String messageSubject) {
		SicsCoreProperties.WATCHDOG_NOTIFICATION_SUBJECT.setValue(messageSubject).save();
	}

	public String getSender() {
		return SicsCoreProperties.WATCHDOG_NOTIFICATION_SENDER.getValue();
	}

	public void setSender(String sender) {
		SicsCoreProperties.WATCHDOG_NOTIFICATION_SENDER.setValue(sender).save();
	}

	public String getRecipients() {
		return SicsCoreProperties.WATCHDOG_NOTIFICATION_RECIPIENTS.getValue();
	}

	public void setRecipients(String recipients) {
		SicsCoreProperties.WATCHDOG_NOTIFICATION_RECIPIENTS.setValue(recipients).save();
	}

	@Override
	public long getNotificationSent() {
		return notificationSent;
	}

	public void resetDefault() {
		SicsCoreProperties.PROXY_TIMEOUT.reset();
		SicsCoreProperties.WATCHDOG_NOTIFY.reset();
		SicsCoreProperties.WATCHDOG_NOTIFICATION_SUBJECT.reset();
		SicsCoreProperties.WATCHDOG_NOTIFICATION_SENDER.reset();
		SicsCoreProperties.WATCHDOG_NOTIFICATION_RECIPIENTS.reset();
	}

	@Override
	public void notifySicsStalled() {
		if (isSendEmailNotification()) {
			INotificationService service = ServiceUtils
					.getService(INotificationService.class);
			if (service == null) {
				return;
			}
			EmailNotification notification = new EmailNotification();
			notification.setFrom(getSender());
			notification.setTo(getRecipients());
			notification.setSubject(getMessageSubject());
			notification
					.setText("This is an automatic notification sent from GumTree.\n");
			service.send(notification);
			notificationSent++;
		}
	}

	@Override
	public String getRegistrationKey() {
		return "org.gumtree.gumnix.sics:type=SicsProxyWatchdog";
	}

}
