/**
 * 
 */
package au.gov.ansto.bragg.wombat.dra.core;

import java.net.URI;

import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.nexus.NexusUtils;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 *
 */
public class DataLocation extends ConcreteProcessor {

	private IGroup inputGroup;
	private URI outURI;
	/**
	 * 
	 */
	public DataLocation() {
		
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.process.processor.ConcreteProcessor#process()
	 */
	@Override
	public Boolean process() throws Exception {
		if (inputGroup == null || inputGroup.getLocation() == null)
			return true;
		
		outURI = NexusUtils.createNexusDataURI(inputGroup.getLocation(), null, 0);
		return false;
	}

	public URI getOutURI() {
		return outURI;
	}

	public void setInputGroup(IGroup inputGroup) {
		this.inputGroup = inputGroup;
	}

	public Boolean getToResetHistory(){
		return Boolean.TRUE;
	}
}
