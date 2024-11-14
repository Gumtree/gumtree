package au.gov.ansto.bragg.nbi.server;

import org.gumtree.util.ISystemProperty;
import org.gumtree.util.SystemProperty;

public class NBIServerProperties {
	
	public static final String PROPERTY_SSH_KEYPATH = "gumtree.ssh.keypath";
	public static final String PROPERTY_SSH_PASSPHRASE = "gumtree.ssh.passphrase";

	public static final ISystemProperty SICS_RESTLET_HOST = new SystemProperty(
			"gumtree.sics.restletHost", "localhost");

	public static final ISystemProperty SICS_RESTLET_PORT = new SystemProperty(
			"gumtree.sics.restletPort", "60033");
	
}
