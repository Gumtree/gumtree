/**
 * 
 */
package au.gov.ansto.bragg.nbi.core;

/**
 * @author nxi
 *
 */
public class NBISystemProperties {


	public static boolean USE_NEW_PROXY = Boolean.valueOf(System.getProperty("gumtree.sics.useNewProxy", "false"));
	
	public static String PORTAL_ADDRESS = System.getProperty("gumtree.portalAddress", "http://neutron.ansto.gov.au");

}
