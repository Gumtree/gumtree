package fr.soleil.nexus;

// Tools lib
import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import ncsa.hdf.hdflib.HDFNativeData;

import org.nexusformat.AttributeEntry;
import org.nexusformat.NXlink;
import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;

public class NexusFileBrowser extends NexusFileInstance {

    // Member attributes
    // members concerning current path in file
    private PathNexus m_pVirtualPath; // Current path into the Nexus File
    private PathNexus m_pRealPath;    // Current path into the Nexus File (full real path)

    // members concerning link mechanism
    private boolean m_bFileChanged = true; // True if the file has been changed since last open
    private boolean m_bUpdatePath = true;  // True when m_pVisiblePath will be updated while browsing file

    // members concerning nodes' buffer
    private int m_iBufferSize; // Maximum size of the buffer (number of available slots)
    private TreeMap<String, String> m_tNodeTab; // TreeMap of all node belonging to last listed group's children (node name => node class)
    private final TreeMap<String, TreeMap<String, String>> m_tNodeInPath; // TreeMap containing all node for a specific path (path in file => [node name => node class])
    private final HashMap<String, Integer> m_hPathUsageWeigth; // TreeMap containing path usage count (used to remove less used path when cleaning buffer)

    // members concerning nodes' attributes buffer
    private String m_sAttrPath; // Path in string format of the last read attribute
    private Hashtable<String, AttributeEntry> m_hAttrTab; // Hashtable of all attributes lastly read (attribute name => attribute value)

    // Constructors
    public NexusFileBrowser() {
	super();
	m_pVirtualPath = new PathNexus();
	m_pRealPath = new PathNexus();
	m_tNodeTab = new TreeMap<String, String>();
	m_tNodeInPath = new TreeMap<String, TreeMap<String, String>>(
		new PathCollator());
	m_hPathUsageWeigth = new HashMap<String, Integer>();
	m_iBufferSize = 100;
    }

    public NexusFileBrowser(String sFilePath) {
	super(sFilePath);
	initPath(sFilePath);
	m_tNodeTab = new TreeMap<String, String>();
	m_tNodeInPath = new TreeMap<String, TreeMap<String, String>>(
		new PathCollator());
	m_hPathUsageWeigth = new HashMap<String, Integer>();
	m_bFileChanged = false;
	m_iBufferSize = 100;
    }

    /**
     * getBufferSize return the current maximum size of the node buffer (in
     * number of slots)
     */
    public int getBufferSize() {
	return m_iBufferSize;
    }

    /**
     * setBufferSize Set the current maximum size of the node buffer (in number
     * of slots)
     * 
     * @param iSize new number of available slots in the node buffer
     */
    public void setBufferSize(int iSize) {
	if (iSize > 10)
	    m_iBufferSize = iSize;
    }

    /**
     * getCurrentPath return the current visible path inside the opened Nexus
     * file
     */
    public PathNexus getCurrentPath() {
	return m_pVirtualPath;
    }

    /**
     * getCurrentRealPath return the current real path inside the opened Nexus
     * file
     */
    protected PathNexus getCurrentRealPath() {
	return m_pRealPath;
    }

    /**
     * openFile Set specified file as the current one and open it with
     * appropriate access mode
     */
    @Override
    public void openFile(String sFilePath, int iAccessMode)
	    throws NexusException {
	m_pRealPath.clearNodes();
	m_pVirtualPath.clearNodes();
	if (m_bFileChanged) {
	    initPath(sFilePath);
	}
	super.openFile(sFilePath, iAccessMode);
    }

    /**
     * setFile Set specified file as the current one
     */
    @Override
    public void setFile(String sFilePath) {
	m_bFileChanged = true;
	m_pRealPath.clearNodes();
	m_pVirtualPath.clearNodes();
	initPath(sFilePath);
	super.setFile(sFilePath);
    }

    /**
     * closeFile Close the current file, but keep its path so we can easily open
     * it again.
     */
    @Override
    public void closeFile() throws NexusException {
	closeAll();
	super.closeFile();
    }

    /**
     * finalize Free resources
     */
    @Override
    protected void finalize() throws Throwable {
	m_bUpdatePath = false;
	m_pVirtualPath = null;
	super.finalize();
    }

    // ---------------------------------------------------------
    // / Navigation through nodes
    // ---------------------------------------------------------
    /**
     * openPath opens groups and DataItems according to the path (PathNexus) in
     * the opened file given. All objects of the path must exist.
     * 
     * @param paPath The path string
     */
    public void openPath(PathNexus pnPath) throws NexusException {
	if (!pnPath.isRelative())
	    closeAll();

	// for each node of the path
	boolean opened;
	for (NexusNode pattern : pnPath.getNodes()) {
	    opened = false;

	    // for each child
	    List<NexusNode> child = listChildren();
	    for (NexusNode node : child) {
		// node comparison
		if (node.matchesPartNode(pattern)) {
		    opened = true;
		    openNode(node);
		    break;
		}
	    }

	    // No node opened throw exception
	    if (!opened) {
		throw new NexusException("Path invalid path in file: "
			+ pnPath.toString() + "\nfailed at: "
			+ m_pVirtualPath.toString());
	    }
	}
    }

    /**
     * openGroup opens the group name with class nxclass. The group must exist,
     * otherwise an exception is thrown. opengroup is similar to a cd name in a
     * filesystem.
     * 
     * @param sNodeName the name of the group to open.
     * @param sNodeClass the classname of the group to open.
     * @note this method is case insensitive for the node's name
     */
    protected void openGroup(String sNodeName, String sNodeClass)
	    throws NexusException {
	if (sNodeName.equals(PathNexus.PARENT_NODE)) {
	    closeData();
	    return;
	}

	String sItemName = sNodeName;
	String sItemClass = sNodeClass;

	// Ensure a class has been set
	if (sItemClass.trim().equals("")) {
	    sItemClass = getNodeClassName(sItemName);
	}

	// Try to open requested node as it was given
	boolean bIsGroup = true;
	try {
	    if (!sItemClass.equals("SDS")) {
		getNexusFile().opengroup(sItemName, sItemClass);
	    } else {
		getNexusFile().opendata(sItemName);
	    }
	    bIsGroup = !(sItemClass.equals("SDS") || sItemClass
		    .equals("NXtechnical_data"));
	    if (m_bUpdatePath)
		m_pVirtualPath.pushNode(new NexusNode(sItemName, sItemClass,
			bIsGroup));
	    m_pRealPath
	    .pushNode(new NexusNode(sItemName, sItemClass, bIsGroup));
	} catch (NexusException nException) {
	    // Don't succeed so we search (case insensitive) for a node
	    // having the given name
	    sItemName = getCaseInsensitiveNodeName(sNodeName);
	    getNexusFile().opengroup(sItemName, sItemClass);
	    bIsGroup = !(sItemClass.equals("SDS") || sItemClass
		    .equals("NXtechnical_data"));
	    if (m_bUpdatePath)
		m_pVirtualPath.pushNode(new NexusNode(sItemName, sItemClass,
			bIsGroup));
	    m_pRealPath
	    .pushNode(new NexusNode(sItemName, sItemClass, bIsGroup));
	}
    }

    /**
     * openData opens the DataItem name with class SDS. The DataItem must exist,
     * otherwise an exception is thrown. opendata is similar to a 'cd' command
     * in a file system.
     * 
     * @param sNodeName the name of the group to open.
     * @param bCaseSensitive true if node name corresponds exactly to the requested node
     *            (optional)
     * @param bJumpNodes true if the NXtechnical_data are not considered as DataItem
     *            but as group (optional)
     * @note the pattern ".." means close current node
     */
    protected void openData(String sNodeName) throws NexusException {
	openData(sNodeName, false, false);
    }

    private void openData(String sNodeName, boolean bCaseSensitive,
	    boolean bDirectChild) throws NexusException {
	String sItemName;
	if (!bCaseSensitive)
	    sItemName = getCaseInsensitiveNodeName(sNodeName);
	else
	    sItemName = sNodeName;

	if (sNodeName.equals(PathNexus.PARENT_NODE)) {
	    closeData();
	    return;
	}

	try {
	    getNexusFile().opendata(sItemName);
	    if (m_bUpdatePath)
		m_pVirtualPath.pushNode(new NexusNode(sItemName, "", false));
	    m_pRealPath.pushNode(new NexusNode(sItemName, "", false));
	} catch (NexusException ne1) {
	    String sNodeClass = getNodeClassName(sItemName);
	    if ("NXtechnical_data".equals(sNodeClass)) {
		// Opening the group
		getNexusFile().opengroup(sItemName, sNodeClass);
		if (m_bUpdatePath)
		    m_pVirtualPath.pushNode(new NexusNode(sItemName,
			    "NXtechnical_data", false));
		m_pRealPath.pushNode(new NexusNode(sItemName,
			"NXtechnical_data", false));
	    } else
		throw ne1;
	}
    }

    /**
     * openNode Open the requested node of the currently opened file.
     * 
     * @param nnNode node to be opened
     */
    public void openNode(NexusNode nnNode) throws NexusException {
	if (nnNode == null) {
	    throw new NexusException(
		    "Invalid node to open: can't open a null node!");
	}
	String sNodeName = nnNode.getNodeName();
	String sNodeClass = nnNode.getClassName();

	// Open the requested node
	if (nnNode.isGroup()
		|| (nnNode.isRealGroup() && !"".equals(nnNode.getClassName()))) {
	    // Close the node if the requested one is PathNexus.PARENT_NODE
	    if (PathNexus.PARENT_NODE.equals(sNodeName))
		closeData();
	    // Open the group
	    else if (!"".equals(sNodeName)) {
		if ("".equals(sNodeClass)) {
		    sNodeClass = getNodeClassName(sNodeName);
		}
		openGroup(sNodeName, sNodeClass);
	    } else
		openSubItem(0, sNodeClass);
	}
	// Open the DataItem
	else {
	    openData(sNodeName);
	}
    }

    /**
     * openSubItem Open the iIndex sub-item of current group having the sClass
     * as class name
     * 
     * @param nfFile NexusFile to explore
     * @param iIndex index of the sub-item to open (class name dependent)
     * @param sNodeClass class name of the sub-item to open
     * @param sPatternName pattern name for nodes to be considered
     * @throws NexusException
     * @note the first item has index number 0
     */
    protected void openSubItem(int iIndex, String sNodeClass)
	    throws NexusException {
	openSubItem(iIndex, sNodeClass, ".*");
    }

    protected void openSubItem(int iIndex, String sNodeClass,
	    String sPatternName) throws NexusException {
	// Index of the currently examined node in list
	int iCurIndex = 0; 
	// Reg exp to rename dynamically node and get them in a correct order
	String sRegExp = "^(.*)#([0-9]*$)";
	// Class and name of the currently
	String sItemClass, sItemName;

	String sTmpNum, sKeyName;
	TreeMap<String, String> tmBuff; // Ordered buffer of node name and entry
	// node badly named

	// Parse children nodes
	listGroupChild();
	tmBuff = new TreeMap<String, String>(new NameCollator());

	// Re-map entry set to have items sorted correctly: image#2 must be
	// before image#19
	for (Iterator<String> iter = m_tNodeTab.keySet().iterator(); iter
		.hasNext();) {
	    // Ensure
	    sItemName = iter.next();
	    sTmpNum = sItemName.replaceFirst(sRegExp, "$2");
	    if (!sTmpNum.equals(sItemName)) {
		iCurIndex = Integer.parseInt(sTmpNum);
		sKeyName = "" + iCurIndex;
		if (iCurIndex == 0)
		    iCurIndex = 1;
		while (iCurIndex < 10000) {
		    sKeyName = "0" + sKeyName;
		    iCurIndex *= 10;
		}
		sKeyName = sItemName.replaceFirst(sRegExp, "$1") + "#"
			+ sKeyName;
	    } else
		sKeyName = sItemName;
	    tmBuff.put(sKeyName, sItemName);
	}
	iCurIndex = 0;
	// Get the item number iIndex according to tmBuff's entry set (which is
	// correctly sorted)
	for (Iterator<String> iter = tmBuff.keySet().iterator(); iter.hasNext();) {
	    // Check class name and index to open write node
	    sItemName = tmBuff.get(iter.next());
	    sItemClass = m_tNodeTab.get(sItemName);
	    if (sNodeClass.equals(sItemClass)
		    && sItemName.matches(sPatternName)) {
		if (iIndex == iCurIndex) {
		    openGroup(sItemName, sItemClass);
		    return;
		}
		iCurIndex++;
	    }
	}
	throw new NexusException("Failed to open sub-item " + iIndex + " of "
		+ m_pVirtualPath.getValue());
    }

    /**
     * openSignalDataNode Open the DataItem containing the signal data. This
     * node must be direct descendant of current group
     * 
     * @throws NexusException
     */
    public void openSignalDataNode() throws NexusException {
	String sNodeName = "", sNodeClass;
	Map<String, String> mNodeMap;

	// Parse children
	Entry<String, String> entry;
	mNodeMap = listGroupChild();
	for (Iterator<Entry<String, String>> iter = mNodeMap.entrySet()
		.iterator(); iter.hasNext();) {
	    entry = iter.next();
	    sNodeName = entry.getKey();
	    sNodeClass = entry.getValue();

	    // Seek DataItem nodes (class name = SDS)
	    if (sNodeClass.equals("SDS")) {
		// open DataItem
		openData(sNodeName);
		int[] iAttrVal = new int[1];
		int[] iAttrProp = { 1, NexusFile.NX_INT32 };
		try {
		    // check it has the signal attribute and its value is a
		    // NX_INT32 type
		    getNexusFile().getattr("signal", iAttrVal, iAttrProp);
		    return;
		}
		// Catch the exception and do NOT propagate it
		catch (NexusException ne) {
		}

		// Signal DataItem not found, so we continue parsing children
		closeData();
	    }
	}
	throw new NexusException("No DataItem found in current group: "
		+ m_pVirtualPath.getValue());
    }

    /**
     * fastOpenSubItem Open the item number iIndex having sNodeClass for class
     * name
     * 
     * @param iIndex index of the item that matches the class
     * @param sNodeClass node class that we want to open
     * @throws NexusException
     *             if node was not found
     * @note THE NODE BUFFER LIST ISN'T UPDATED
     */
    public void fastOpenSubItem(int iIndex, String sNodeClass)
	    throws NexusException {
	String name = getNexusFile().getSubItemName(iIndex, sNodeClass);

	if (name == null) {
	    throw new NexusException("Item not found!");
	}
	NexusNode node = new NexusNode(name, sNodeClass);
	try {
	    if (sNodeClass.equals("SDS")) {
		openData(name, true, false);
	    } else {
		openGroup(name, sNodeClass);
	    }
	} catch (NexusException e) {
	    openNode(node);
	}
    }

    /**
     * closeGroup Close currently opened group, and return to parent node
     * 
     * @throws NexusException
     */
    protected void closeGroup() throws NexusException {
	getNexusFile().closegroup();
	m_pRealPath.popNode();
	if (m_bUpdatePath) {
	    m_pVirtualPath.popNode();
	}
    }

    /**
     * closeData Close currently opened DataItem, and return to parent node
     * 
     * @throws NexusException
     */
    public void closeData() throws NexusException {
	NexusNode nnNode = m_pRealPath.getCurrentNode();
	if (nnNode != null && !nnNode.getClassName().equals("NXtechnical_data")
		&& !nnNode.isGroup()) {
	    getNexusFile().closedata();
	} else if (nnNode != null) {
	    getNexusFile().closegroup();
	}
	m_pRealPath.popNode();
	if (m_bUpdatePath) {
	    m_pVirtualPath.popNode();
	}
    }

    /**
     * closeAll Close every opened DataItem and/or groups to step back until the
     * Nexus file root is reached
     * 
     * @note the NeXus file is kept opened
     */
    public void closeAll() throws NexusException {
	// Check the file is opened else throws Exception
	try {
	    if (getNexusFile() != null) {
		// Try to close DataItem
		try {
		    closeData();
		} catch (NexusException ne) {
		    /* Nothing to do: no DataItem were opened */
		}

		// Closes groups until the path is empty, i.e. reaching NeXus
		// file root
		while (m_pRealPath.getCurrentNode() != null) {
		    closeGroup();
		}
	    }
	}
	// No file opened!
	catch (NexusException ne) {/* Nothing to do: we are at document root */
	}

	// Clearing current path
	if (m_bUpdatePath)
	    m_pVirtualPath.setPath(new NexusNode[0]);
	m_pRealPath.setPath(new NexusNode[0]);
    }

    // ---------------------------------------------------------
    // / Nodes informations
    // ---------------------------------------------------------
    /**
     * getNodeClassName Return the class name of a specified node in currently
     * opened group
     * 
     * @param sNodeName name of the node from which we want to know the class name
     * @throws NexusException
     *             if no corresponding node was found
     */
    protected String getNodeClassName(String sNodeName) throws NexusException {
	// Parse children
	String sItemName = sNodeName.toUpperCase();
	String sCurName;
	listGroupChild();

	for (Iterator<String> iter = m_tNodeTab.keySet().iterator(); iter
		.hasNext();) {
	    // Check if names are equals
	    sCurName = iter.next();
	    if (sItemName.equals(sCurName.toUpperCase())) {
		return m_tNodeTab.get(sCurName);
	    }
	}

	throw new NexusException("NexusNode not found: " + sNodeName);
    }

    /**
     * getNode Create a new NexusNode by scanning child of the current path
     * according to given node name
     */
    public NexusNode getNode(String sNodeName) throws NexusException {
	if (sNodeName == null || sNodeName.trim().equals(""))
	    return null;

	// Parse children
	String sItemName = NexusNode.extractName(sNodeName).toUpperCase();
	String sItemClass = NexusNode.extractClass(sNodeName).toUpperCase();
	String sCurName, sCurFullName, sCurClass;
	listGroupChild();

	for (Iterator<String> iter = m_tNodeTab.keySet().iterator(); iter
		.hasNext();) {
	    // Check if names are equals
	    sCurName = iter.next();
	    sCurClass = m_tNodeTab.get(sCurName);
	    sCurFullName = NexusNode.getNodeFullName(sCurName, sCurClass)
		    .toUpperCase();
	    if (sItemName.equals(sCurFullName)
		    || sItemName.equals(sCurName.toUpperCase())
		    || (sItemClass.equals(sCurClass.toUpperCase()) && sItemName
			    .equals(""))) {
		return new NexusNode(sCurName, sCurClass,
			!(sCurClass.equals("SDS") || sCurClass
				.equals("NXtechnical_data")));
	    }
	}

	throw new NexusException("NexusNode not found: " + sNodeName);
    }

    /**
     * isOpenedDataItem Return true if the opened item is a DataItem
     * 
     * @param sNodeName name of the node from which we want to know the class name
     */
    public boolean isOpenedDataItem() {
	return m_pRealPath.getDataItemName() != null;
    }

    /**
     * tryGuessNodeName Try to find a node corresponding to given name. The
     * method will try name, then replace '/' by '__', then will try to add '__'
     * in end of name, or try adding '__#1'... The method will return first
     * matching pattern
     * 
     * @param sNodeName approximative node name
     * @return a string array containing the write name of a node as first
     *         element and classname as second element
     */
    protected String[] tryGuessNodeName(String sNodeName) throws NexusException {
	String[] sFoundNode = { "", "" };
	String sTmpName = sNodeName;
	try { // Try to find requested name
	    sFoundNode[1] = getNodeClassName(sTmpName);
	} catch (NexusException ne1) {
	    try { // don't succeed, so we try replacing "/" by "__"
		sTmpName = sTmpName.replace("/", "__");
		sFoundNode[1] = getNodeClassName(sTmpName);
	    } catch (NexusException ne2) {
		try { // don't succeed: trying adding "#1" at end of name
		    sTmpName += "#1";
		    sFoundNode[1] = getNodeClassName(sTmpName);
		} catch (NexusException ne3) {
		    try { // don't succeed: trying with "__#1" at end of name
			sTmpName = sTmpName.substring(0, sTmpName.length() - 2)
				+ "__#1";
			sFoundNode[1] = getNodeClassName(sTmpName);
		    } catch (NexusException ne4) {
			// don't succeed, no more good idea... we send an
			// Exception
			throw new NexusException("NexusNode name not found: "
				+ sNodeName + "!");
		    }
		}
	    }
	}
	sFoundNode[0] = sTmpName;
	return sFoundNode;
    }

    protected NXlink getNXlink() throws NexusException {
	NXlink nlLink = null;

	if (isOpenedDataItem())
	    nlLink = getNexusFile().getdataID();
	else
	    nlLink = getNexusFile().getgroupID();

	return nlLink;
    }

    // ---------------------------------------------------------
    // / Browsing nodes
    // ---------------------------------------------------------
    /**
     * listChildren List all direct descendants of the node ending the given
     * path and returns it as a list of NexusNode
     * 
     * @throws NexusException
     */
    public NexusNode[] listChildren(PathNexus pnPath) throws NexusException {
	ArrayList<NexusNode> nnNodes;

	// Open the requested node
	openPath(pnPath);

	// Get all its descendants
	nnNodes = listChildren();

	// Return to document root
	closeAll();

	return nnNodes.toArray(new NexusNode[nnNodes.size()]);
    }

    /**
     * listChildren List all direct descendants of an opened node and returns it
     * as a list of NexusNode
     * 
     * @throws NexusException
     */
    public ArrayList<NexusNode> listChildren() throws NexusException {
	ArrayList<NexusNode> nnNodes;
	String sNodeName;
	String sNodeClass;

	// Get all its direct descendants
	TreeMap<String, String> hmNodeMap = listGroupChild();
	nnNodes = new ArrayList<NexusNode>(hmNodeMap.size());
	int index = 0;

	// Parse children
	Entry<String, String> entry;
	for (Iterator<Entry<String, String>> iter = hmNodeMap.entrySet()
		.iterator(); iter.hasNext();) {
	    entry = iter.next();
	    sNodeName = entry.getKey();
	    sNodeClass = entry.getValue();
	    boolean bRealGroup = !(sNodeClass.equals("SDS") || sNodeClass
		    .equals("NXtechnical_data"));
	    nnNodes.add(new NexusNode(sNodeName, sNodeClass, bRealGroup));
	    index++;
	}

	return nnNodes;
    }

    // ---------------------------------------------------------
    // / Link nodes
    // ---------------------------------------------------------
    /**
     * listAttribute List all attributes of the currently opened node and store
     * it into a hashtable member variable.
     * 
     * @note A Hashtable which will hold the names of the attributes as keys.
     *       For each key there is an AttributeEntry class as value.
     */
    @SuppressWarnings("unchecked")
    public Hashtable<String, AttributeEntry> listAttribute()
	    throws NexusException {
	// Check we have to update the list
	if (m_hAttrTab == null || !m_sAttrPath.equals(m_pRealPath.toString())) {
	    // Clear previous attributes
	    if (m_hAttrTab != null)
		m_hAttrTab.clear();

	    // Update the map
	    m_hAttrTab = getNexusFile().attrdir();
	    m_sAttrPath = m_pRealPath.toString();
	}

	return m_hAttrTab;
    }

    public static String getStringValue(Byte[] reference) {
	byte[] toTransform = null;
	if (reference == null) {
	    return "";
	} else {
	    toTransform = new byte[reference.length];
	}
	for (int i = 0; i < reference.length; i++) {
	    if (reference[i] == null) {
		toTransform[i] = (byte) 0;
	    } else {
		toTransform[i] = reference[i].byteValue();
	    }
	}

	return new String(toTransform);
    }

    // ---------------------------------------------------------
    // ---------------------------------------------------------
    // / Private methods
    // ---------------------------------------------------------
    // ---------------------------------------------------------
    /**
     * initPath Split the given string to initialize the member NexusPath
     * 
     * sFilePath file path to init
     */
    private void initPath(String sFilePath) {
	sFilePath = sFilePath.replace(File.separator,
		NexusFileInstance.PATH_SEPARATOR);
	m_pVirtualPath = new PathNexus();
	m_pVirtualPath.setFile(sFilePath);

	m_pRealPath = new PathNexus();
	m_pRealPath.setFile(sFilePath);

	m_bFileChanged = false;

    }

    protected Object getAttribute(String sAttrName) throws NexusException {
	Object oAttrVal;
	int[] iAttrInf = { 0, 0 };

	// Get a map of attribute
	Hashtable<String, AttributeEntry> hAttrList = listAttribute();

	if (!hAttrList.containsKey(sAttrName))
	    throw new NexusException("No corresponding attribute found: "
		    + sAttrName + "!");

	// Get infos on attribut
	iAttrInf[0] = m_hAttrTab.get(sAttrName).length;
	iAttrInf[1] = m_hAttrTab.get(sAttrName).type;

	// Initialize an array of proper type with enough space to store
	// attribute value
	oAttrVal = HDFNativeData.defineDataObject(iAttrInf[1], iAttrInf[0]
		+ (iAttrInf[1] == NexusFile.NX_CHAR ? 1 : 0));

	// Get attribute value
	getNexusFile().getattr(sAttrName, oAttrVal, iAttrInf);

	// Convert bytes array (representing chars) to string
	if (iAttrInf[1] == NexusFile.NX_CHAR) {
	    oAttrVal = new String((byte[]) oAttrVal);
	    oAttrVal = ((String) oAttrVal).substring(0,
		    ((String) oAttrVal).length() - 1);
	}
	return oAttrVal;
    }

    /**
     * getCaseInsensitiveNodeName Scan children of the currently opened group to
     * determine the write node's name
     * 
     * @param sNodeName The name of the node we want to find (case insensitive)
     */
    private String getCaseInsensitiveNodeName(String sNodeName)
	    throws NexusException {
	// Parse children nodes to get real node's name
	listGroupChild();

	String sItemName = sNodeName;
	String sReqName = sNodeName.toUpperCase();
	String sCurName;

	for (Iterator<String> iter = m_tNodeTab.keySet().iterator(); iter
		.hasNext();) {
	    sCurName = iter.next();
	    if (sReqName.equals(sCurName.toUpperCase())) {
		sItemName = sCurName;
		break;
	    }
	}

	return sItemName;
    }

    /**
     * listGroupChild Stores in a buffer all children of the currently opened
     * group
     * 
     * @note this method is here to avoid JVM crash due to Nexus API (in version
     *       4.2.0)
     * @throws NexusException
     */
    @SuppressWarnings("unchecked")
    protected TreeMap<String, String> listGroupChild() throws NexusException {
	if (!isListGroupChildUpToDate()) {
	    TreeMap<String, String> hmNodeList;

	    // Case we are in a DataItem
	    if (m_pRealPath.getGroupsName() == null) {
		m_tNodeTab.clear();
	    }
	    // Case we are in a group
	    else {
		Long time = System.currentTimeMillis();

		hmNodeList = new TreeMap<String, String>(new NameCollator());
		hmNodeList.putAll(getNexusFile().groupdir());
		time = System.currentTimeMillis() - time;
		m_tNodeTab = hmNodeList;
		putNodeInPath((TreeMap<String, String>) m_tNodeTab.clone(),
			time.intValue());
	    }
	} else
	    m_tNodeTab = (TreeMap<String, String>) getNodeInPath().clone();
	return m_tNodeTab;
    }

    /**
     * isListGroupChildUpToDate return true if the children buffer node list has
     * to be updated
     */
    private boolean isListGroupChildUpToDate() {
	return getNodeInPath() != null;
    }

    protected void pushNodeInPath(String sCurName, String sCurClass) {
	pushNodeInPath(sCurName, sCurClass, 1);
    }

    private void pushNodeInPath(String sCurName, String sCurClass,
	    int iTimeToAccessNode) {
	TreeMap<String, String> tmMap = m_tNodeInPath.get(m_pRealPath
		.toString());
	if (tmMap == null) {
	    tmMap = new TreeMap<String, String>();
	    tmMap.put(sCurName, sCurClass);
	    putNodeInPath(tmMap, iTimeToAccessNode);
	} else
	    tmMap.put(sCurName, sCurClass);
    }

    protected void putNodeInPath(TreeMap<String, String> tmNodes,
	    int iTimeToAccessNode) {
	freeBufferSpace();

	Integer value = m_hPathUsageWeigth.get(m_pRealPath.toString());
	if (value == null)
	    value = iTimeToAccessNode;
	else
	    value += iTimeToAccessNode + 1;
	m_hPathUsageWeigth.put(m_pRealPath.toString(), value);

	m_tNodeInPath.put(m_pRealPath.toString(), tmNodes);
    }

    /**
     * getNodeInPath Return the buffered map of the node's names and node's
     * class for the current path
     */
    protected TreeMap<String, String> getNodeInPath() {
	Integer value = m_hPathUsageWeigth.get(m_pRealPath.toString());
	if (value == null)
	    value = 1;
	else
	    value++;
	m_hPathUsageWeigth.put(m_pRealPath.toString(), value);

	return m_tNodeInPath.get(m_pRealPath.toString());
    }

    protected TreeMap<String, String> popNodeInPath() {
	Integer value = m_hPathUsageWeigth.get(m_pRealPath.toString());
	if (value != null) {
	    value--;
	    m_hPathUsageWeigth.put(m_pRealPath.toString(), value);
	}

	return m_tNodeInPath.remove(m_pRealPath.toString());
    }

    private void freeBufferSpace() {
	if (m_tNodeInPath.size() > m_iBufferSize) {
	    int iNumToRemove = (m_iBufferSize / 2), iRemovedItem = 0, iInfLimit;
	    Object[] frequency = m_hPathUsageWeigth.values().toArray();
	    java.util.Arrays.sort(frequency);
	    iInfLimit = (Integer) frequency[frequency.length / 2];
	    Iterator<String> keys_iter = m_hPathUsageWeigth.keySet().iterator();
	    int freq;
	    String key;
	    while (keys_iter.hasNext() && iRemovedItem < iNumToRemove) {
		key = keys_iter.next();
		freq = m_hPathUsageWeigth.get(key);

		if (freq <= iInfLimit) {
		    keys_iter.remove();
		    m_tNodeInPath.remove(key);
		    iRemovedItem++;
		}
	    }
	}
    }

    static public class PathCollator implements Comparator<String> {
	@Override
	public int compare(String arg0, String arg1) {
	    if (arg0.length() > arg1.length())
		return 1;
	    else if (arg0.length() < arg1.length())
		return -1;
	    else
		return Collator.getInstance().compare(arg0, arg1);
	}
    }

    static public class NameCollator implements Comparator<String> {
	@Override
	public int compare(final String arg0, final String arg1) {
	    int iCmp;
	    if (arg0.matches(".*[0-9].*") && arg1.matches(".*[0-9].*")) {
		// Prepare string by marking up every digit
		String argA, argB;
		argA = arg0.replaceAll("(\\d+)", "#$1#");
		argB = arg1.replaceAll("(\\d+)", "#$1#");

		// Separate characters and digit
		String[] arg0Parts, arg1Parts;
		arg0Parts = argA.split("#");
		arg1Parts = argB.split("#");

		// Compare strings until one is lesser than the other
		iCmp = 0;
		int index = 0;
		while (iCmp == 0) {
		    // If remains string in both parts
		    if (index < arg0Parts.length && index < arg1Parts.length) {
			// If digits
			if (arg0Parts[index].matches("[0-9]+")
				&& arg1Parts[index].matches("[0-9]+")) {
			    int iArg0 = Integer.parseInt(arg0Parts[index]);
			    int iArg1 = Integer.parseInt(arg1Parts[index]);

			    if (iArg0 > iArg1)
				iCmp = 1;
			    else if (iArg0 < iArg1)
				iCmp = -1;
			    else
				iCmp = 0;
			}
			// If characters
			else {
			    iCmp = Collator.getInstance().compare(
				    arg0Parts[index], arg1Parts[index]);
			}
		    }
		    // One of the part is empty
		    else {
			if (arg0Parts.length == arg1Parts.length)
			    iCmp = 0;
			else
			    iCmp = (arg0Parts.length > arg1Parts.length) ? 1
				    : -1;
			break;
		    }
		    index++;
		}
	    } else {
		iCmp = Collator.getInstance().compare(arg0, arg1);
	    }
	    return iCmp;
	}
    }

}