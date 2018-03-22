package org.gumtree.control.core;

import org.gumtree.control.batch.IBatchControl;
import org.gumtree.control.imp.SicsProxy;

public class SicsManager {

	private static ISicsProxy sicsProxy;
	
	private static ISicsProxy validatorProxy;
	
	public static synchronized ISicsProxy getSicsProxy(String serverAddress, String publisherAddress) {
		if (serverAddress != null) {
			if (sicsProxy == null) {
				sicsProxy = new SicsProxy();
			} else if (sicsProxy.isConnected()) {
				sicsProxy.disconnect();
			}
			sicsProxy.connect(serverAddress, publisherAddress);
		}
		return sicsProxy;
	}
	
	public static ISicsProxy getSicsProxy() {
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
			validatorProxy.connect(serverAddress, publisherAddress);
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
}
