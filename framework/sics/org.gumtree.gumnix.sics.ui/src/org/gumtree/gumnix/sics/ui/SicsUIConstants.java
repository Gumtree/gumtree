package org.gumtree.gumnix.sics.ui;

public class SicsUIConstants {

	/**
	 * Unique identifer for the component view.
	 */
	@Deprecated
	public static final String ID_COMPONENT_VIEW = "org.gumtree.gumnix.sics.ui.componentView";

	/**
	 * Unique identifer for the component control view.
	 */
	@Deprecated
	public static final String ID_COMPONENT_CONTROL_VIEW = "org.gumtree.gumnix.sics.ui.componentControlView";

	/**
	 * Unique identifier for the batch control view.
	 */
	public static final String ID_VIEW_BATCH_CONTROL = "org.gumtree.gumnix.sics.ui.sicsBatchView";
	
	/**
	 * Unique identifer for the SICS telnet communication adapter.
	 */
	public static final String ID_SICS_TERMINAL_ADAPTER = "org.gumtree.gumnix.sics.ui.telnetCommunicationAdapter";

	/**
	 * Unique identifer for the SICS server communication adapter.
	 */
	public static final String ID_SICS_CONSOLE_ADAPTER = "org.gumtree.gumnix.sics.ui.serverCommunicationAdapter";

	/**
	 * Unique identifer for the SICS control editor.
	 */
	public static final String ID_EDITOR_SICS_CONTROL = "org.gumtree.gumnix.sics.ui.sicsControl";

	/**
	 * Unique identifer for the component control editor.
	 */
	public static final String ID_EDITOR_COMPONENT_CONTROL = "org.gumtree.gumnix.sics.ui.componentControl";

	/**
	 * Unique identifer for the scan editor.
	 */
	public static final String ID_EDITOR_SCAN = "org.gumtree.gumnix.sics.ui.scanEditor";

	public final static String ID_SICS_PERSPECTIVE = "org.gumtree.gumnix.sics.ui.sicsPerspective";
	/**
	 * Private constructor to block instance creation.
	 */
	private SicsUIConstants() {
		super();
	}

}
