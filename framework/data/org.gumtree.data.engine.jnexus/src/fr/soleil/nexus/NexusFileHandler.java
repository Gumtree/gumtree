package fr.soleil.nexus;

import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;

public class NexusFileHandler extends NexusFile {
    public NexusFileHandler(String filename, int access) throws NexusException  {
	super(filename, access);
    }

    /**
     * getSubItemName Returns the name of the item having the given class name
     * 
     * @param iIndex index of the sub-item to open (class name dependent)
     * @param sNodeClass class name of the sub-item to open
     * @return item's name
     * @throws NexusException
     * @note the first item has index number 0
     */
    public String getSubItemName(int iIndex, String sNodeClass) throws NexusException {
	if(handle < 0) throw new NexusException("NAPI-ERROR: File not open");
	String names[] = new String[2];
	int i = 0;
	while(nextentry(handle,names) != -1)
	{
	    if( names[1].equals(sNodeClass) ) {
		if( i == iIndex ) {
		    return names[0];
		}
		i++;
	    }
	}
	return null;
    }
}
