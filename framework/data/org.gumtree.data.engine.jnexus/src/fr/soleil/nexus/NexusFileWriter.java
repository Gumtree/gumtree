package fr.soleil.nexus;

// Nexus lib
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import org.nexusformat.AttributeEntry;
import org.nexusformat.NXlink;
import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;

public class NexusFileWriter extends NexusFileReader {
    public final static String NULL_VALUE = "null";  // Value used to mean null when reading/writing a DataItem

    protected static final String  DataItem_LINK  = "link_node";    // Generic name of a node wearing a link

    private boolean m_bCompressed;

    /// Constructors
    public NexusFileWriter()          { super(); m_bCompressed = false; }
    public NexusFileWriter(String sFilePath)  { super(sFilePath); m_bCompressed = false; }

    /// Accessors
    public boolean getCompressedData()        { return m_bCompressed; }
    public void     setCompressedData(boolean bComp)  { m_bCompressed = bComp; }

    // ---------------------------------------------------------
    /// Node creation
    // ---------------------------------------------------------
    /**
     * createPath
     * Create all groups defined in path.
     *
     * @param paPath path (PathAbstract object) to create in the NexusFile
     * @param bKeepOpen keep path opened after creation
     */
    protected void createPath(PathNexus paPath)           throws NexusException { createPath(paPath, false); }
    protected void createPath(PathNexus paPath, boolean bKeepOpen)  throws NexusException
    {
	String sCurClass  = "";    // Class name of the group we are processing
	String sCurName    = "";    // Name of the group we are processing
	NexusNode nCurStep  = null;    // Currently opened node name in the NexusFile
	int    iCurDepth  = 0;    // Current depth in the path
	int    iPathDepth  = paPath.getDepth();

	// Return to root node
	closeAll();

	// Create each group
	NexusNode nnNode;
	while( iCurDepth < iPathDepth )
	{
	    // Determine group and class names
	    nnNode    = paPath.getNode(iCurDepth);
	    sCurName  = nnNode.getNodeName();
	    sCurClass = nnNode.getClassName();

	    // Create appropriate group and open it
	    nCurStep = getCurrentRealPath().getCurrentNode();
	    if( nnNode.isGroup() || "NXtechnical_data".equals(nnNode.getClassName()) )
	    {
		try
		{
		    openGroup(sCurName, sCurClass);
		}
		catch(NexusException ne)
		{
		    // Ensure we are still in the expected group
		    if( nCurStep != null && ! nCurStep.equals(getCurrentRealPath().getCurrentNode()) )
			closeGroup();

		    // Create the requested group
		    getNexusFile().makegroup(sCurName, sCurClass);

		    // Force the buffer node list to be updated
		    pushNodeInPath(sCurName, sCurClass);

		    // Open created group
		    openGroup(sCurName, sCurClass);
		}
	    }
	    // Go one step forward in path
	    iCurDepth++;
	}

	// Return to root node if necessary
	if( !bKeepOpen )
	    closeAll();
    }

    /**
     * createDataItem
     * Create all groups defined into the path, then create a DataItem matching type, dimensions sizes of the given DataItem
     * item. When the creation process is done it close if asked all node to rturn bacxk to document's root.
     *
     * @param dsData DataItem item which created DataItem will match
     * @param paPath Path into which the given DataItem will be created
     * @param bKeepOpen boolean telling if the path should be keep opened after work
     * @return true if the node was existing yet , false if the process created a node. The purpose of this return is
     * to tell whether data compatibility should be checked before writing it
     */
    protected boolean createDataItem(DataItem dsData, PathNexus pnPath, boolean bKeepOpen) throws NexusException
    {
	// Checking path contains a DataItem name
	String sDataItemName = pnPath.getDataItemName();
	if( sDataItemName == null )
	    throw new NexusException("Path is invalid: no DataItem name specified to store data!");

	// Open path (or create it if needed)
	createPath(pnPath, true);

	// Open DataItem (or create it if needed)
	boolean bCheckData;
	try
	{
	    openData(sDataItemName);
	    bCheckData = true;
	}
	catch(NexusException ne)
	{
	    makeData(sDataItemName, dsData);
	    openData(sDataItemName);
	    bCheckData = false;
	}

	if( ! bKeepOpen )
	    closeAll();

	return bCheckData;
    }

    /**
     * makeData
     * Create a DataItem named sDataName that fit dsData in the currently opened group: detecting type, size and dim
     *
     * @param sDataName name of the DataItem to create
     * @param oData shape of data that the DataItem will have to contain
     */
    protected void makeData(String sDataName, DataItem dsData) throws NexusException
    {
	/* ******** Warning: jnexus API doesn't support NX_INT64 for writing
	 *  those lines convert NX_INT64 into NX_FLOAT64, so long to double
    // Converting long as double because the JAVA Nexus API uses C++ native methods whom don't support integer 64 bit
    if( dsData.getType() == NexusFile.NX_INT64 )
    {
      dsData.setData(convertArray(dsData));
      dsData.setType(NexusFile.NX_FLOAT64);
    }
	 */
	long iLength   = 1;
	int[] iDimSize = dsData.getSize();
	int iType      = dsData.getType();
	int iRank      = iDimSize.length;

	for( int i = 0; i < iRank; i++ )
	{
	    iLength *= iDimSize[i];
	}

	// Create the DataItem
	if( m_bCompressed && iLength > 1000 && iType != NexusFile.NX_CHAR )
	{
	    getNexusFile().compmakedata(sDataName, iType, iRank, iDimSize, NexusFile.NX_COMP_LZW, iDimSize);
	}
	else
	{
	    getNexusFile().makedata(sDataName, iType, iRank, iDimSize);
	}

	// Update the buffer node list
	pushNodeInPath(sDataName, "SDS");

	// Init DataItem of type NX_CHAR
	if( iType == NexusFile.NX_CHAR )
	{
	    openData(sDataName);
	    Object oBuffer = defineArrayObject(iType, iDimSize);
	    Arrays.fill((byte[])oBuffer, (Byte) initTypeFromNexus(iType));
	    getNexusFile().putdata(oBuffer);
	    oBuffer = null;
	    closeData();
	}
    }

    // ---------------------------------------------------------
    /// Node writing
    // ---------------------------------------------------------
    /**
     * writeAttr
     * Write an attribute on the node pointed by path.
     *
     * @param tData the data attribut's value to be stored (template argument: it can be any primitive or string)
     * @param sPath path to set datas in current file
     */
    public <type> void writeAttr(String sAttrName, type tData, PathNexus paPath) throws NexusException
    {
	// Open destination path
	openPath(paPath);

	// Ensure we have an array of data
	Object oData;
	if( null == tData )
	    oData = NULL_VALUE;
	else if( !tData.getClass().isArray() && !(tData instanceof String) )
	{
	    oData = java.lang.reflect.Array.newInstance(tData.getClass(), 1);
	    java.lang.reflect.Array.set(oData, 0, tData);
	}
	else
	    oData = tData;
	// Write the attribute
	putAttr(sAttrName, oData);

	// Return to document root
	closeAll();
    }

    /**
     * writeData
     * Write a DataItem on the node pointed by path.
     *
     * @param dsData DataItem item to be written
     * @param paPath path to set datas in current file (can be absolute or local)
     * @note if path don't exists it will be automatically created
     */
    public void writeData(DataItem dsData, PathData paPath) throws NexusException
    {
	if( dsData.getData() instanceof PathNexus ) 
	{
	    // Write link
	    writeLink((PathNexus) dsData.getData(), paPath);

	    // Return to document root
	    closeAll();
	}
	else 
	{
	    // Create or open DataItem
	    boolean bNeedDataCheck = createDataItem(dsData, paPath, true);


	    // Check data compatiblity if needed
	    if( bNeedDataCheck )
	    {
		checkDataMatch(dsData);
	    }

	    // Write data
	    putData(dsData);

	    // Write attributes
	    putAttr(dsData);

	    // Return to document root
	    closeAll();
	}
    }

    /**
     * putAttr
     * Write the attribute in the currently opened DataItem
     *
     * @param sAttrName name of the attribute
     * @param tData array of values for the attribute
     */
    @SuppressWarnings("unchecked")
    protected <type> void putAttr(String sAttrName, type tData) throws NexusException
    {
	Object tArrayData;
	if( ! (tData instanceof String) && ! tData.getClass().isArray() )
	{
	    tArrayData = (type[]) java.lang.reflect.Array.newInstance(tData.getClass(), 1);
	    java.lang.reflect.Array.set(tArrayData, 0, tData);
	}
	else
	    tArrayData = tData;

	// Changing the array into a DataItem object to apply conversion methods if needed
	Object iData;
	DataItem dsData = new DataItem(tArrayData);

	// Converting from string to byte[]
	if( dsData.getType() == NexusFile.NX_CHAR )
	    iData = ((String) dsData.getData()).getBytes();

	// Converting from boolean[] to byte[]
	else if( dsData.getType() == NexusFile.NX_BOOLEAN )
	    iData = convertArray(dsData);

	/* ******** Warning: jnexus API doesn't support NX_INT64 for writing
	 *  those lines convert NX_INT64 into NX_FLOAT64, so long to double
    // Converting from long[] to double[]
    else if( dsData.getType() == NexusFile.NX_INT64 )
    {
      iData = convertArray(dsData);
      dsData.setType(NexusFile.NX_FLOAT64);
    }
	 */

	// Setting datas as they are
	else
	    iData = dsData.getData();

	// Putting datas into the attribute
	getNexusFile().putattr(sAttrName, iData, dsData.getType());

	// Free ressources
	iData = null;
    }

    /**
     * putAttr
     * Write all DataItem's attribute in the currently opened DataItem
     */
    protected void putAttr(DataItem dsData) throws NexusException
    {
	HashMap<String, ?> mAttrMap = dsData.getAttributes();
	String sAttrName;
	Object oAttrVal;
	if( mAttrMap != null )
	{
	    for (Iterator<String> iter = mAttrMap.keySet().iterator() ; iter.hasNext() ; )
	    {
		sAttrName = (String) iter.next();
		oAttrVal  = dsData.getAttribute(sAttrName);
		putAttr(sAttrName, oAttrVal);
	    }
	}
    }

    /**
     * putData
     * Put the item value in the currently opened DataItem
     *
     * @param dsData is a DataItem item (properly initiated), having dimensions, type and size corresponding to opened DataItem
     */
    protected void putData(DataItem dsData) throws NexusException
    {
	// Ensures the DataItem has an array value
	dsData.arrayify();

	// Getting data from DataItem
	Object iData = dsData.getData();

	// Converting from string to byte[]
	if( dsData.getType() == NexusFile.NX_CHAR )
	    iData = ((String) iData).getBytes();

	// Converting from boolean[] to byte[]
	else if( dsData.getType() == NexusFile.NX_BOOLEAN )
	    iData = convertArray(dsData);

	/* ******** Warning: jnexus API doesn't support NX_INT64 for writing
	 *  those lines convert NX_INT64 into NX_FLOAT64, so long to double
    // Converting from long[] to double[]
    else if( dsData.getType() == NexusFile.NX_INT64 )
    {
      iData = convertArray(dsData);
      dsData.setType(NexusFile.NX_FLOAT64);
    }
	 */

	// Putting data into opened DataItem
	getNexusFile().putdata(iData);
    }

    /**
     * CopyNode
     * make a full copy of the current source node (itself and its descendants)
     * into the currently opened destination path. The copy cares of all nodes' attributes.
     *
     * @param nfrSource handler on the opened source file
     */
    protected void copyNode(NexusFileReader nfrSource) throws NexusException
    {
	// Keep in buffer current path (used for recursion process)
	PathNexus pnSrcPath = nfrSource.getCurrentRealPath().clone();
	PathNexus pnTgtPath = getCurrentRealPath().clone();
	NexusNode nnCurNode = pnSrcPath.getCurrentNode();
	NexusNode tmpNode;

	if( getCurrentRealPath().getCurrentNode() != null && ! getCurrentRealPath().getCurrentNode().isRealGroup() )
	    throw new NexusException("Invalid destination path: only a group can contain nodes!\n" + getCurrentRealPath());

	// Check the kind of the node
	if( nnCurNode == null || nnCurNode.isGroup() || "NXtechnical_data".equals(nnCurNode.getClassName()) )
	{
	    // Copy the current group
	    PathNexus pnPath = pnTgtPath.clone();
	    if( nnCurNode != null )
		pnPath.pushNode(nnCurNode);
	    createPath(pnPath, true );
	    copyAllAttr(nfrSource);

	    // Copy all its descendants
	    ArrayList<NexusNode> listNode = nfrSource.listChildren();
	    for ( int i = 0; i < listNode.size(); i++ )
	    {
		tmpNode = listNode.get(i);
		nfrSource.openNode(tmpNode);
		copyNode(nfrSource);
	    }
	}
	else
	{
	    // Copy the current DataItem
	    PathData pdDstPath = new PathData(pnTgtPath.getNodes(), nnCurNode.getNodeName());
	    DataItem dsData = nfrSource.getDataItem();
	    writeData(dsData, pdDstPath);
	}

	// Return back to the original position in both source and destination files
	closeAll();
	openPath(pnTgtPath);
	nfrSource.closeAll();
	nfrSource.openPath(pnSrcPath.getParentPath());

    }

    /**
     * copyAllAttr
     * Copy all attributes of the current node from the source file into
     * into the current node.
     *
     * @param nfrSource handler on the opened source file
     */
    protected void copyAllAttr(NexusFileReader nfrSource) throws NexusException
    {
	String sAttrName;
	Hashtable<String, AttributeEntry> listAttr = nfrSource.listAttribute();
	for (Iterator<String> iter = listAttr.keySet().iterator() ; iter.hasNext() ; )
	{
	    sAttrName = (String) iter.next();
	    copyAttr(sAttrName, nfrSource);
	}
    }

    /**
     * copyAttr
     * Copy the named attribute of the current source node into
     * the current node
     *
     * @param sAttrName name of the attribute to be copied
     * @param nfrSource handler on the opened source file
     */
    protected void copyAttr(String sAttrName, NexusFileReader nfrSource) throws NexusException
    {
	PathNexus pnSrcPath = nfrSource.getCurrentRealPath().clone();
	PathNexus pnTgtPath = getCurrentRealPath().clone();
	writeAttr(sAttrName, nfrSource.readAttr(sAttrName, pnSrcPath), getCurrentRealPath());
	openPath(pnTgtPath);
	nfrSource.openPath(pnSrcPath);
    }

    /**
     * writeLink
     * Write both data link or group link depending on the given path.
     *
     * @param pnSrcPath path of node targeted by the link
     * @param prRelPath path of node wearing the link
     * @throws NexusException
     */
    protected void writeLink(PathNexus pnSrcPath, PathNexus prRelPath) throws NexusException
    {
	PathData pdDestNode;

	// Check the source node is well designed
	if( pnSrcPath.isRelative() )
	    throw new NexusException("Path is invalid: the targeted path must be absolute!");

	// Ensure the wearing node is a DataItem
	if( prRelPath.getDataItemName() == null )
	{
	    PathGroup pgDest = new PathGroup(prRelPath.clone());
	    pdDestNode = new PathData(pgDest, generateDataName((PathGroup) pgDest, DataItem_LINK));
	}
	else
	    pdDestNode = (PathData) prRelPath;

	// Check the link is valid
	//PathNexus pnTgtPath = checkRelativeLinkTarget(prRelPath, pdDestNode);

	// Open path to get the corresponding NXlink
	openPath(pnSrcPath);
	NXlink nlLink = getNXlink();

	// Create the source path if needed and open it
	createPath(pdDestNode, true);

	// Write the link
	getNexusFile().makenamedlink(pdDestNode.getDataItemName(), nlLink);

	// Update node list buffer
	NexusNode nnNode = pdDestNode.getCurrentNode();
	pushNodeInPath(nnNode.getNodeName(), nnNode.getClassName());
    }
}
