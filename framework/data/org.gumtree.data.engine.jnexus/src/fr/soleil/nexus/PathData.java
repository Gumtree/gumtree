package fr.soleil.nexus;

// Nexus lib
import org.nexusformat.NexusException;

public class PathData extends PathNexus {
    /**
     * PathData
     * Create an object PathData
     *
     * @param sAcquiName name of the acquisition which the DataItem will belong to
     * @param sInstName name of the instrument which the DataItem will belong to
     * @param sDataName name of the DataItem
     * @note group's class can be specified by adding "<" and ">" to a class name: i.e. "my_entry<NXentry>"
     * @note BE AWARE that it's better not to force the group's class. By default they are mapped by the API to apply Nexus format DTD
     */
    public PathData(String sAcquiName, String sInstName, String sDataName) { super(new String[] {sAcquiName, sInstName}, sDataName); }

    public PathData(String[] sGroups, String sDataName)    { super(sGroups, sDataName); }
    public PathData(NexusNode[] nnGroups, String sDataName)  { super(nnGroups, sDataName); }
    public PathData(PathGroup pgPath, String sDataName)
    {
	super();
	PathNexus p = (PathNexus) pgPath.clone();
	int i = 0;
	while(i < p.getDepth() )
	{
	    try
	    {
		pushNode(p.getNode(i));
	    }
	    catch( NexusException e)  { }
	    i++;
	}

	if( sDataName != null )
	    pushNode(new NexusNode(sDataName, "", false));

	this.setFile(p.getFilePath());
    }

    static public PathData Convert(PathNexus pnPath)
    {
	if( pnPath.getDataItemName() != null )
	{
	    return new PathData( pnPath.getParentPath(), pnPath.getDataItemName());
	}
	else
	{
	    return new PathData( pnPath.getParentPath(), pnPath.getCurrentNode().getNodeName());
	}
    }


}
