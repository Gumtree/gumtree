package au.gov.ansto.bragg.nbi.service.notification.support;

import org.gumtree.service.notification.support.AbstractNotification;

public class TwitterNotification extends AbstractNotification {

	private static final long serialVersionUID = -358949079958301883L;

	private static final String PROCOTOL = "twitter";
	
	private String screenName;
	
	private String password;
	
	private String message;
	
	public TwitterNotification() {
		super();
	}
	
	public TwitterNotification(String screenName, String password, String message) {
		super();
		setScreenName(screenName);
		setPassword(password);
		setMessage(message);
	}
	
	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String getProtocol() {
		return PROCOTOL;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result
				+ ((password == null) ? 0 : password.hashCode());
		result = prime * result
				+ ((screenName == null) ? 0 : screenName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TwitterNotification other = (TwitterNotification) obj;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (screenName == null) {
			if (other.screenName != null)
				return false;
		} else if (!screenName.equals(other.screenName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TwitterNotification [screenName=" + screenName + ", password="
				+ password + ", message=" + message + ", getTimestamp()="
				+ getTimestamp() + "]";
	}
	
}
