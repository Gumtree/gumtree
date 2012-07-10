package au.gov.ansto.bragg.cicada.core;

import org.gumtree.util.ISystemProperty;
import org.gumtree.util.SystemProperty;

public final class CicadaCoreProperties {

	public static final ISystemProperty AlGORITHM_SET_PLUGIN = new SystemProperty(
			"gumtree.processor.algoSetPlugin", "au.gov.ansto.bragg.cicada.dra");
	
	private CicadaCoreProperties() {
		super();
	}
	
}
