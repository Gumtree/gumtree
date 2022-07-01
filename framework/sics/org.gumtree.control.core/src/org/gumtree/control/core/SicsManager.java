package org.gumtree.control.core;

import org.gumtree.control.batch.IBatchControl;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.imp.SicsProxy;

public class SicsManager {

	private static final String PROP_SERVER_NAME = "gumtree.control.serverHost";
	private static final String PROP_AUTO_CONNECT = "gumtree.control.autoConnect";
	private static final String PROP_SUB_PORT = "gumtree.control.subPort";
	private static final String PROP_DEALER_PORT = "gumtree.control.dealerPort";
	
	private static ISicsProxy sicsProxy;
	
	private static ISicsProxy validatorProxy;
	
	public static synchronized ISicsProxy getSicsProxy(String serverAddress, String publisherAddress) {
		if (serverAddress != null) {
			if (sicsProxy == null) {
				sicsProxy = new SicsProxy();
			} 
			try {
				sicsProxy.connect(serverAddress, publisherAddress);
			} catch (SicsException e) {
				e.printStackTrace();
			}
		}
		return sicsProxy;
	}
	
	public static synchronized ISicsProxy getSicsProxy() {
		if (sicsProxy == null) {
			sicsProxy = new SicsProxy();
		}
		return sicsProxy;
	}

	public static synchronized ISicsProxy getValidatorProxy(String serverAddress, String publisherAddress) {
		if (serverAddress != null) {
			if (validatorProxy == null) {
				validatorProxy = new SicsProxy();
			} else if (validatorProxy.isConnected()) {
				validatorProxy.disconnect();
			}
			try {
				validatorProxy.connect(serverAddress, publisherAddress);
			} catch (SicsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return validatorProxy;
	}
	
	public static ISicsProxy getValidatorProxy() {
		if (validatorProxy == null) {
			validatorProxy = new SicsProxy();
		}
		return validatorProxy;
	}
	
	public static ISicsModel getSicsModel() {
		return getSicsProxy().getSicsModel();
	}

	public static IBatchControl getBatchControl() {
		return getSicsProxy().getBatchControl();
	}
	
	public static void autoStartProxy() throws Exception {
		boolean autoStart = false;
		try {
			autoStart = Boolean.valueOf(System.getProperty(PROP_AUTO_CONNECT));
		} catch (Exception e) {
		}
		if (autoStart) {
			String host = System.getProperty(PROP_SERVER_NAME);
			if (host != null) {
				String subAddress = host + ":" + System.getProperty(PROP_SUB_PORT);
				String dealerAddress = host + ":" + System.getProperty(PROP_DEALER_PORT);
				SicsManager.getSicsProxy(dealerAddress, subAddress);
			}
		}
	}
	

}
