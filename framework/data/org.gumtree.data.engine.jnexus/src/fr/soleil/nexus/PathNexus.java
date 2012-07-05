package fr.soleil.nexus;

// Tools lib
import java.io.File;
import java.util.ArrayList;
import java.util.List;

// Nexus lib
import org.nexusformat.NexusException;

public class PathNexus implements Cloneable {
    // Public definition
    public static final PathGroup ROOT_PATH = new PathGroup(new String[0]);  // Path to reach the document root
    public static final String    PARENT_NODE = "..";                       // Pattern meaning parent node of the current one

    // Private members
    private String   m_sFilePath;
    private List<NexusNode> m_nnNode;

    // Constructors
    protected PathNexus()                  { m_nnNode = new ArrayList<NexusNode>(); }
    protected PathNexus(String[] sGroups)  { boolean[] bIsGroup = new boolean[sGroups.length]; java.util.Arrays.fill(bIsGroup, true); initPath(sGroups, bIsGroup); }
    public PathNexus(NexusNode[] nnNodes)  { m_nnNode = new ArrayList<NexusNode>(); setPath(nnNodes); }
    protected PathNexus(NexusNode[] nnNodes, String sNodeName)
    {
	m_nnNode = new ArrayList<NexusNode>();
	setPath(nnNodes);
	m_nnNode.add(new NexusNode(sNodeName, "", false));
    }

    protected PathNexus(String[] sGroups, String sDataName)
    {
	List<String> lGroups = new ArrayList<String>(java.util.Arrays.asList(sGroups));
	boolean[] bIsGroup;

	if( sDataName != null )
	{
	    lGroups.add(sDataName);
	    bIsGroup = new boolean[sGroups.length + 1];
	    java.util.Arrays.fill(bIsGroup, true);
	    bIsGroup[sGroups.length] = false;
	    sGroups = lGroups.toArray(sGroups);
	}
	else
	{
	    bIsGroup = new boolean[sGroups.length];
	    java.util.Arrays.fill(bIsGroup, true);
	    sGroups = lGroups.toArray(sGroups);
	}
	initPath(sGroups, bIsGroup);
    }

    // Setters
    protected void setPath(String sAcquiName)                     { String[] sArray = {sAcquiName}; initPath(sArray, new boolean[] {true}); }
    protected void setPath(String sAcquiName, String sInstName)           { String[] sArray = {sAcquiName, sInstName}; initPath(sArray, new boolean[] {true, true}); }
    protected void setPath(String sAcquiName, String sInstName, String sDataName)  { String[] sArray = {sAcquiName, sInstName, sDataName}; initPath(sArray, new boolean[] {true, true, false}); }
    protected void setPath(NexusNode[] nnNodes)                    { m_nnNode.clear(); int i=0; while(i < nnNodes.length) { m_nnNode.add(nnNodes[i]); i++;} }
    public    void setFile(String sFileName)                    { m_sFilePath = sFileName; }

    // Getters
    /**
     * getFilePath
     * returns the path to reach the current file
     */
    public String getFilePath()
    {
	return m_sFilePath;
    }

    /**
     * getDepth
     * returns the path's depth (i.e number of group or DataItem described inside)
     */
    public int getDepth()
    {
	return m_nnNode.size();
    }

    /**
     * getCurrentNode
     * returns the node that the path is aiming to or null if it contains no node
     */
    public NexusNode getCurrentNode()
    {
	if( m_nnNode.size() > 0 )
	    return m_nnNode.get(m_nnNode.size() - 1);
	else
	    return null;
    }

    public NexusNode[] getNodes() { return m_nnNode.toArray(new NexusNode[m_nnNode.size()]); }

    /**
     * getParentPath
     * Return a new PathGroup sibling the parent of the given node.
     * @param pnPath sibling node from which we want to have the parent
     * @return the PathGroup containing the sibling node (or null in case of root)
     */
    public PathGroup getParentPath()
    {
	if( m_nnNode.size() == 0 )
	    return null;

	// Copy the given node in a buffer
	PathNexus pnBuf = (PathNexus) clone();

	// Remove the last node
	pnBuf.popNode();

	// Instantiate a new item and fill it
	PathGroup pgRes = new PathGroup("_");
	pgRes.setPath(pnBuf.m_nnNode.toArray(new NexusNode[pnBuf.m_nnNode.size()]));
	pgRes.setFile(pnBuf.m_sFilePath);

	return pgRes;
    }

    /**
     * getNode
     * returns node's name and class name at the iDepth depth in path
     *
     * @param iDepth depth position in path (starting to 0)
     * @throws NexusException if a problem occurs
     */
    public NexusNode getNode(int iDepth) throws NexusException
    {
	if(iDepth >= m_nnNode.size() )
	    throw new NexusException("The path do not have such depth!");

	if( "".equals(m_nnNode.get(iDepth).getNodeName()) && "".equals(m_nnNode.get(iDepth).getClassName()) )
	    throw new NexusException("Cannot define a node with no name and no class!");

	return m_nnNode.get(iDepth);
    }

    /**
     * getValue
     * return the path value in a Nexus file as a String
     */
    public String getValue()
    {
	StringBuffer buf = new StringBuffer();

	if( ! isRelative() )
	    buf.append( NexusFileInstance.PATH_SEPARATOR );

	NexusNode nnNode;
	for( int i = 0; i < m_nnNode.size(); i++)
	{
	    nnNode = m_nnNode.get(i);
	    if( ! "".equals(nnNode.toString().trim()) )
	    {
		buf.append(nnNode.toString());
		if( nnNode.isGroup() )
		    buf.append(NexusFileInstance.PATH_SEPARATOR);
	    }
	}
	return buf.toString();
    }

    /**
     * getDataItemName
     * returns the name of the DataItem in path (node having isGroup == false)
     * @note if no DataItem provided returns null (i.e. the path targets a group)
     */
    public String getDataItemName()
    {
	for(int i = m_nnNode.size() - 1; i >= 0; i--)
	{
	    if( ! m_nnNode.get(i).isGroup() )
		return m_nnNode.get(i).getNodeName();
	}
	return null;
    }

    /**
     * getGroupsName
     * returns an array containing groups name in the path (node having isGroup == true)
     */
    public String[] getGroupsName()
    {
	ArrayList<String> aNodes = new ArrayList<String>();
	for(int i = 0; i < m_nnNode.size(); i++)
	{
	    if( m_nnNode.get(i).isGroup() )
		aNodes.add( m_nnNode.get(i).getNodeName() );
	}
	return aNodes.toArray(new String[aNodes.size()]);
    }

    public String[] getGroups()
    {
	ArrayList<String> aNodes = new ArrayList<String>();
	for(int i = 0; i < m_nnNode.size(); i++)
	{
	    if( m_nnNode.get(i).isGroup() )
		aNodes.add( m_nnNode.get(i).toString() );
	}
	return aNodes.toArray(new String[aNodes.size()]);
    }

    /**
     * toString
     * give a string representation of path
     */
    @Override
    public String toString()
    {
	return toString(true);
    }

    public String toString(boolean showFile)
    {
	if( showFile && m_sFilePath != null && !"".equals(m_sFilePath.trim())  )
	    return m_sFilePath + ";" + getValue();
	else
	    return getValue();
    }

    /**
     * pushNode
     * Add specified node to the last position in the path
     * @param nnNode node to add in end of the path
     */
    public void pushNode(NexusNode nnNode)
    {
	m_nnNode.add(nnNode);
    }

    /**
     * popNode
     * Remove the last node from the path and returns it
     * @return node removed
     */
    public NexusNode popNode()
    {
	if( m_nnNode.size() > 0 )
	    return m_nnNode.remove(m_nnNode.size() - 1);
	else
	    return null;
    }

    /**
     * clearNodes
     * Remove all node in the path (equivalent to document root)
     */
    public void clearNodes()
    {
	m_nnNode.clear();
    }

    /**
     * insertNode
     * Insert a node in path to the iIndex position.
     * @param iIndex position of insertion
     * @param nnNode node to be inserted
     */
    public void insertNode(int iIndex, NexusNode nnNode) throws NexusException
    {
	if( iIndex > m_nnNode.size() )
	    throw new NexusException("Node insertion failed: invalid position in path!");

	m_nnNode.add(iIndex, nnNode);
    }

    /**
     * insertNode
     * Remove a node in path to the iIndex position.
     * @param iIndex position of removal
     */
    public void removeNode(int iIndex) throws NexusException
    {
	if( iIndex > m_nnNode.size() )
	    throw new NexusException("Node insertion failed: invalid position in path!");

	m_nnNode.remove(iIndex);
    }

    /**
     * isRelative
     * Scan all nodes of the current path and check if it has a back value
     */
    public boolean isRelative()
    {
	for( int i = 0; i < m_nnNode.size(); i++ )
	{
	    if( m_nnNode.get(i).getNodeName().equals(PARENT_NODE) )
		return true;
	}
	return false;
    }

    public static String[] splitStringPath(String sPath)
    {
	if( sPath.startsWith(NexusFileInstance.PATH_SEPARATOR) )
	    return sPath.substring(1).split(NexusFileInstance.PATH_SEPARATOR);
	else
	    return sPath.split(NexusFileInstance.PATH_SEPARATOR);
    }

    public static NexusNode[] splitStringToNode(String sPath) {
	String[] names    = splitStringPath(sPath);
	NexusNode[] nodes = null;

	int nbNodes = 0;
	for( String name : names ) {
	    if( ! name.isEmpty() ) {
		nbNodes++;
	    }
	}

	if( nbNodes > 0 ) {
	    nodes = new NexusNode[nbNodes];
	    int i = 0;
	    for( String name : names ) {
		if( !name.isEmpty() ) {
		    nodes[i] = new NexusNode( NexusNode.extractName(name), NexusNode.extractClass(name) );
		    i++;
		}
	    }
	}
	else {
	    nodes = new NexusNode[0];
	}
	return nodes;
    }

    @Override
    public PathNexus clone()
    {
	PathNexus paPath = new PathNexus();
	NexusNode[] nNodes = new NexusNode[m_nnNode.size()];
	for(int i = 0; i < nNodes.length; i++)
	{
	    nNodes[i] = m_nnNode.get(i);
	}

	paPath.setPath(nNodes);

	paPath.m_sFilePath = m_sFilePath;
	return paPath;
    }

    // ---------------------------------------------------------
    // ---------------------------------------------------------
    /// Protected
    // ---------------------------------------------------------
    // ---------------------------------------------------------
    protected void initPath(String[] sPathArray, boolean[] bIsGroup )
    {

	NexusNode node = null;

	m_nnNode = new ArrayList<NexusNode>();

	for(int i = 0; i < bIsGroup.length; i++)
	{
	    node = NexusNode.getNexusNode(sPathArray[i], bIsGroup[i]);
	    if( node != null )
		m_nnNode.add(node);
	}
    }

    /**
     * applyClassPattern
     * For each of NexusNode in this path, if no class name are specified the corresponding one
     * will be set
     * @param sGroupsClass string array containing the class name pattern to apply
     * @note for each node to be processed that is, deeper than the sGroupsClass array the last class name will be set
     */
    protected void applyClassPattern(String[] sGroupsClass)
    {
	for(int i = 0; i < m_nnNode.size(); i++)
	{
	    if( m_nnNode.get(i).isGroup() && "".equals(m_nnNode.get(i).getClassName()) )
	    {
		if( i < sGroupsClass.length )
		    m_nnNode.get(i).setClassName(sGroupsClass[i]);
		else
		    m_nnNode.get(i).setClassName(sGroupsClass[sGroupsClass.length - 1]);
	    }
	}
    }

    /**
     * determinePath
     * Return the shortest path from file fFrom to reach file fTarget
     * @param fFrom File we want to leave
     * @param fTarget File we want to reach
     */
    protected static  String determinePath(File fFrom, File fTarget)
    {
	String sFileSep = File.separator;

	// Getting folders
	File fSrc = fFrom.getParentFile();
	File fTgt = fTarget.getParentFile();

	ArrayList<String> alSources = new ArrayList<String> ();
	ArrayList<String> alTargets = new ArrayList<String> ();

	// Storing folders tree of the starting file (root is object of index 0)
	while( fSrc != null )
	{
	    alSources.add(0, fSrc.getName());
	    fSrc = fSrc.getParentFile();
	}

	// Storing folders tree of the ending file (root is object of index 0)
	while( fTgt != null )
	{
	    alTargets.add(0, fTgt.getName());
	    fTgt = fTgt.getParentFile();
	}

	// Removing all commons parts in paths
	while( alSources.size() > 0 && alTargets.size() > 0 && alSources.get(0).equals(alTargets.get(0)) )
	{
	    alTargets.remove(0);
	    alSources.remove(0);
	}

	// Adding the "../" to local path
	int iIndex = 0;
	StringBuffer sLocalPath = new StringBuffer();
	while( iIndex < alSources.size() )
	{
	    if( ! "".equals(alSources.get(iIndex)) )
		sLocalPath.append(".." + sFileSep);
	    iIndex++;
	}

	// Adding the "my_folder/" to local path
	iIndex = 0;
	while( iIndex < alTargets.size() )
	{
	    if( ! "".equals(alTargets.get(iIndex)) )
		sLocalPath.append(alTargets.get(iIndex) + sFileSep);
	    iIndex++;
	}

	return (sLocalPath.toString() + fTarget.getName()).replace(File.separator, NexusFileInstance.PATH_SEPARATOR);
    }

}
