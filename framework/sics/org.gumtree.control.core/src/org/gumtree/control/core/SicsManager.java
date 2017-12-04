package org.gumtree.control.core;

public class SicsManager {

	private static ISicsProxy sicsProxy;
	private static ISicsModel sicsModel;
	
	public static ISicsProxy getSicsProxy() {
		return sicsProxy;
	}

	public static ISicsModel getSicsModel() {
		return sicsModel;
	}

	static {
		
	}
	
	
}
