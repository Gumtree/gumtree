package au.gov.ansto.bragg.nbi.server.login;

import org.gumtree.service.directory.LDAPService.GroupLevel;

public class UserSessionObject {

	private String userName;
	private String sessionId;
	private boolean isValid = false;
	private boolean isDAV = false;
	private boolean isICS = false;
	private GroupLevel level = GroupLevel.INVALID;
	private String message = "";
	
	public UserSessionObject(){
	}

	public void setSessionId(String sessionId){
		this.sessionId = sessionId;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public UserSessionObject(String userName, String sessionId) {
		this.userName = userName;
		this.sessionId = sessionId;
	}

	public void appendMessage(String text) {
		message += text;
	}
	
	public void setValid(boolean flag) {
		isValid = flag;
	}
	
	public void setLevel(GroupLevel level) {
		this.level = level;
	}
	
	public GroupLevel getLevel() {
		return level;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	
	public boolean isValid() {
		return isValid;
	}
	
	public String getMessage() {
		return message;
	}

	public boolean isDAV() {
		return isDAV;
	}

	public void setDAV(boolean isDAV) {
		this.isDAV = isDAV;
	}

	public boolean isICS() {
		return isICS;
	}

	public void setICS(boolean isICS) {
		this.isICS = isICS;
	}

}
