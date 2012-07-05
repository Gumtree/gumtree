package fr.soleil.nexus;

// Tools lib
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;

public class AcquisitionData {
    /// Members
    private NexusFileWriter m_nfwFile;    // Manipulator of a NexusFile from which all processes are made

    private ArrayList<WeakReference<IWritableGroupListener>> groupListeners;

    /// Constructor
    public AcquisitionData()                                   { m_nfwFile = new NexusFileWriter(); initGroupListeners(); }
    public AcquisitionData(int iBufferSize)                    { m_nfwFile = new NexusFileWriter(); m_nfwFile.setBufferSize(iBufferSize); initGroupListeners(); }
    public AcquisitionData(String sFilePath)                   { m_nfwFile = new NexusFileWriter(sFilePath); initGroupListeners(); }
    public AcquisitionData(String sFilePath, int iBufferSize)  { m_nfwFile = new NexusFileWriter(sFilePath); m_nfwFile.setBufferSize(iBufferSize); initGroupListeners(); }

    private void initGroupListeners() {
	groupListeners = new ArrayList<WeakReference<IWritableGroupListener>>();
    }

    public void addWritableGroupListener(IWritableGroupListener listener) {
	if (listener != null) {
	    synchronized(groupListeners) {
		boolean canAdd = true;
		for (int i = 0; i < groupListeners.size(); i++) {
		    if (listener.equals(groupListeners.get(i).get())) {
			canAdd = false;
			break;
		    }
		}
		if (canAdd) {
		    groupListeners.add(new WeakReference<IWritableGroupListener>(listener));
		}
	    }
	}
    }

    public void removeWritableGroupListener(IWritableGroupListener listener) {
	if (listener != null) {
	    ArrayList<WeakReference<IWritableGroupListener>> toRemove = new ArrayList<WeakReference<IWritableGroupListener>>();
	    synchronized (groupListeners) {
		for (int i = 0; i < groupListeners.size(); i++) {
		    WeakReference<IWritableGroupListener> ref = groupListeners.get(i);
		    IWritableGroupListener potential = ref.get();
		    if (potential == null || listener.equals(potential)) {
			toRemove.add(ref);
		    }
		    ref = null;
		    potential = null;
		}
		groupListeners.removeAll(toRemove);
	    }
	    toRemove.clear();
	}
    }

    public void removeAllWritableGroupListeners() {
	synchronized (groupListeners) {
	    groupListeners.clear();
	}
    }

    private void fireGroupSubDataWrited(int dataIndex, int totalData) {
	synchronized (groupListeners) {
	    for (int i = 0; i < groupListeners.size(); i++) {
		IWritableGroupListener listener = groupListeners.get(i).get();
		if (listener != null) {
		    listener.dataWrited(dataIndex, totalData);
		}
	    }
	}
    }

    /// Accessors
    protected NexusFileWriter getNexusFileWriter() { return m_nfwFile; }

    /**
     * GetFile/SetFile
     * Getter and setter of the targeted Nexus file path
     */
     public String  getFile()           { return m_nfwFile.getFilePath(); }
    public void    setFile(String sFilePath)  { m_nfwFile.setFile(sFilePath); }

    /**
     * finalize
     * Clean memory
     */
     @Override
     public void finalize() throws Throwable
     {
	 m_nfwFile.finalize();
	 m_nfwFile = null;
     }

     // ---------------------------------------------------------
     // ---------------------------------------------------------
     /// Public Nexus DTD dependent read / write section
     // ---------------------------------------------------------
     // ---------------------------------------------------------

     // ---------------------------------------------------------
     /// Public moding methods
     // ---------------------------------------------------------
     /**
      * setFlagSingleRaw
      * set the multidimensional array mechanism to be active or inactive
      *
      * @param bSingleRaw true if we want to manage single raw arrays
      * @note by default the mechanism is on monodimensional arrays
      */
     public void setFlagSingleRaw(boolean bSingleRaw)
     {
	 m_nfwFile.isSingleRawResult(!bSingleRaw);
     }

     public boolean getFlagSingleRaw()
     {
	 return !m_nfwFile.isSingleRawResult();
     }

     public boolean getCompressedData()        { return m_nfwFile.getCompressedData(); }
     public void     setCompressedData(boolean bComp)  { m_nfwFile.setCompressedData(bComp); }

     // ---------------------------------------------------------
     /// Public browsing methods
     // ---------------------------------------------------------
     /**
      * existPath
      * return a boolean having true as value if the given path exists, else return false
      *
      * @param pnPath path in file to check existence
      */
     public boolean existPath(PathNexus pnPath) throws NexusException
     {
	 boolean bExists = false;

	 // Open the file
	 m_nfwFile.openFile();

	 // Check path existence and close the file
	 try
	 {
	     m_nfwFile.openPath(pnPath);
	     bExists = true;
	     m_nfwFile.closeFile();
	 }
	 catch( NexusException ne )
	 {
	     m_nfwFile.closeFile();
	 }

	 return bExists;
     }

     /**
      * listNodes
      * return a NexusNode array containing all nodes below the specified Nexus Path
      *
      * @param pnPath NexusPath to list
      */
     public NexusNode[] listNodes(PathNexus pnPath) throws NexusException {
	 // Open the file
	 m_nfwFile.openFile();

	 // Go to the NeXus file root
	 m_nfwFile.closeAll();

	 // List nodes below the given path
	 NexusNode[] nodes = m_nfwFile.listChildren(pnPath);

	 // Close the file
	 m_nfwFile.closeFile();

	 return nodes;
     }

     /**
      * getAcquiList
      * return an array string containing all Acquisition's name as direct children of a NeXus file
      */
     public String[] getAcquiList() throws NexusException
     {
	 // Open the file
	 m_nfwFile.openFile();

	 // Go to the NeXus file root
	 m_nfwFile.closeAll();

	 // List all NXentry <=> Acquisition
	 List<NexusNode> nodes = listNode("NXentry");

	 // Prepare output array
	 String[] sOutputList = new String[nodes.size()];
	 int i = 0;
	 for( NexusNode node : nodes ) {
	     sOutputList[i++] = node.getNodeName();
	 }

	 // Close the file
	 m_nfwFile.closeFile();
	 return sOutputList;
     }

     /**
      * getInstrumentList
      * return an array string containing all instrument belonging to an Acquisition (i.e.: NXpositionner, NXinsertion_device...)
      *
      * @param sAcquiName name of the acquisition which belongs instruments
      * @note instruments aren't direct descendant of an Acquisition, they are children of an NXinstrument node
      */
     public String[] getInstrumentList(String sAcquiName) throws NexusException
     {
	 return getInstrumentList(sAcquiName, null);
     }

     /**
      * getInstrumentList
      * return an array string containing all instrument belonging to an Acquisition,
      * having a particular type (i.e.: NXpositionner or NXinsertion_device...)
      *
      * @param sAcquiName name of the acquisition which belongs instruments
      * @note instruments aren't direct descendant of an Acquisition, they are children of an NXinstrument node
      */

     public String[] getInstrumentList(String sAcquiName, Instrument instName) throws NexusException
     {
	 String[] sOutputList = null;

	 // Open the file
	 m_nfwFile.openFile();

	 // Go to the NeXus file root
	 m_nfwFile.closeAll();

	 // Open the required Acquisition (i.e. NXentry) then its NXinstrument
	 try
	 {
	     m_nfwFile.openGroup(sAcquiName, "NXentry");
	     m_nfwFile.fastOpenSubItem(0, "NXinstrument");
	 }
	 catch(NexusException ne)
	 {
	     return sOutputList;
	 }

	 if( instName != null )
	 {
	     // Store instruments list each group we find (i.e. ones not having "SDS" as class name)
	     List<NexusNode> nodes = listNode(instName.getName(), true);

	     // Prepare output array
	     sOutputList = new String[nodes.size()];
	     int i = 0;
	     for( NexusNode node : nodes ) {
		 sOutputList[i++] = node.getNodeName();
	     }
	 }
	 else
	 {
	     // Store instruments list each group we find (i.e. ones not having "SDS" as class name)
	     List<NexusNode> nodes = listNode("SDS", false);

	     // Prepare output array
	     sOutputList = new String[nodes.size()];
	     int i = 0;
	     for( NexusNode node : nodes ) {
		 sOutputList[i++] = node.getNodeName();
	     }
	 }

	 // Free memory and close the file
	 m_nfwFile.closeFile();
	 return sOutputList;
     }

     /**
      * getImageCount
      * Count images belonging to an Acquisition
      *
      * @param sAcquiName requested Acquisition to count images
      */
     public int getImageCount(String sAcquiName) throws NexusException
     {
	 int iImageCount = 0;

	 // Open the file
	 m_nfwFile.openFile();

	 // Go to the NeXus file root
	 m_nfwFile.closeAll();

	 // Open the required Acquisition (i.e. NXentry)
	 m_nfwFile.openGroup(sAcquiName, "NXentry");

	 // List all NXdata <=> image (when current node is the Acquisition) and count results
	 List<NexusNode> lImageNodes = listNode("NXdata");
	 String sNodeName = "";
	 int iIndex = 0;

	 while( iIndex < lImageNodes.size() )
	 {
	     // If node isn't named "image#N" remove it from list
	     sNodeName = lImageNodes.get(iIndex).getNodeName();
	     if( ! sNodeName.toLowerCase().startsWith("image#") )
		 lImageNodes.remove(iIndex);
	     else
	     {
		 m_nfwFile.openGroup(sNodeName, "NXdata");
		 try
		 {
		     m_nfwFile.openSignalDataNode();
		     m_nfwFile.closeData();
		     iIndex++;
		 }
		 catch(NexusException ne)
		 {
		     lImageNodes.remove(iIndex);
		 }
		 m_nfwFile.closeGroup();
	     }
	 }
	 // Return the list's size
	 iImageCount = lImageNodes.size();

	 // Close file
	 m_nfwFile.closeFile();

	 return iImageCount;
     }

     /**
      * getImagePath
      * Returns the path of the image belonging to the Acquisition
      *
      * @param sAcquiName requested Acquisition where image is stored
      * @param iImageIndex number of the requested image to determine path
      * @throws NexusException if no corresponding image was found
      */
     public PathData getImagePath(String sAcquiName, int iImageIndex) throws NexusException
     {
	 PathData pPath = null;

	 // Open the file
	 m_nfwFile.openFile();

	 // Go to the NeXus file root
	 m_nfwFile.closeAll();

	 // Open the required Acquisition (i.e. NXentry)
	 m_nfwFile.openGroup(sAcquiName, "NXentry");

	 // Open the required DataItem
	 m_nfwFile.openSubItem( iImageIndex, "NXdata", "image#.*" );
	 m_nfwFile.openSignalDataNode();

	 pPath = new PathData( m_nfwFile.getCurrentRealPath().getGroupsName(), m_nfwFile.getCurrentRealPath().getDataItemName() );

	 // Close file
	 m_nfwFile.closeFile();

	 return pPath;
     }

     // ---------------------------------------------------------
     /// Public reading methods
     // ---------------------------------------------------------
     /**
      * Return a specified DataItem belonging to an instrument in an acquisition. It corresponds to the following parameters:
      *
      * @param sAcquiName @param instName @param DataItemName @param bCaseSensitive : true / false
      * @return DataItem
      * @throws NexusException
      */
     public DataItem getDataItem(String sAcquiName, String sInstrName, String DataItemName, boolean bCaseSensitive) throws NexusException
     {
	 // Get a DataItem array belonging to an instrument
	 DataItem[] DataItem_array = getInstrumentData(sAcquiName, sInstrName);
	 if(DataItem_array != null)
	 {
	     // Scan the array to find the proper DataItem
	     for (int j = 0 ; j < DataItem_array.length; j++)
	     {
		 DataItem DataItem = DataItem_array[j];
		 if(bCaseSensitive)
		 {
		     if(DataItem.getNodeName().equals(DataItemName))
			 return DataItem;
		 }
		 else //ignore case
		 {
		     if(DataItem.getNodeName().equalsIgnoreCase(DataItemName))
			 return DataItem;
		 }
	     }
	 }
	 return null;
     }

     /**
      * GetData2D
      * Read a 2D data from a NexusFile.
      *
      * @param iImageIndex position of the 2D data to seek in the acquisition
      * @param sAcquiName name of the acquisition to seek in the current file
      */
     public DataItem getData2D(int iImageIndex, String sAcquiName) throws NexusException
     {
	 // Open path to DataItem
	 m_nfwFile.openFile();
	 m_nfwFile.closeAll();
	 m_nfwFile.openGroup(sAcquiName, "NXentry");
	 m_nfwFile.openSubItem(iImageIndex, "NXdata", "image#.*");
	 m_nfwFile.openSignalDataNode();

	 // Get 2D data from opened DataItem
	 DataItem dsData = m_nfwFile.getDataItem(2);

	 // Close all
	 m_nfwFile.closeFile();
	 return dsData;
     }

     /**
      * GetData2D
      * Read a 2D data from a NexusFile.
      *
      * @param iImageIndex position of the 2D data to seek in the acquisition
      * @param iAcquiIndex number of the acquisition to seek in the current file (optional)
      * @note if no iAcqui number is given, then the first encountered one will be opened
      */
     public DataItem getData2D(int iImageIndex )           throws NexusException  { return getData2D(iImageIndex, 0); }
     public DataItem getData2D(int iImageIndex, int iAcquiIndex)  throws NexusException
     {
	 String[] sNodesList = getAcquiList();

	 if( sNodesList.length < iAcquiIndex )
	     throw new NexusException("Acquisition's index is too high!");

	 return getData2D(iImageIndex, sNodesList[iAcquiIndex]);
     }

     /**
      * GetInstrumentData
      * Return an array of DataItem filled by all data stored in the targeted instrument
      *
      * @param sAcquiName parent Acquisition of the requested instrument
      * @param sInstrName instrument's name from which data are requested
      */
     public DataItem[] getInstrumentData(String sAcquiName, String sInstrName) throws NexusException
     {
	 DataItem[] dsDatas = new DataItem[0];

	 // Open the file and go to the NeXus file root
	 m_nfwFile.openFile();
	 m_nfwFile.closeAll();

	 try
	 {
	     // Open the required Acquisition (i.e. NXentry) then its NXinstrument
	     m_nfwFile.openGroup(sAcquiName, "NXentry");
	     m_nfwFile.openSubItem(0, "NXinstrument");

	     // Defining variables
	     Stack<DataItem> hsDataItem;
	     String[]  sNode;
	     String    sNodeName;
	     String    sNodeClass;

	     // Search requested instrument in descendant nodes
	     sNode      = m_nfwFile.tryGuessNodeName(sInstrName);
	     sNodeName  = sNode[0];
	     sNodeClass = sNode[1];

	     // Requested instrument found
	     if( sNodeName != null )
	     {
		 m_nfwFile.openGroup(sNodeName, sNodeClass);

		 // Append its children data
		 hsDataItem = m_nfwFile.getChildrenDatas();
		 dsDatas = hsDataItem.toArray(dsDatas);

		 // Close group
		 m_nfwFile.closeGroup();
	     }
	 }
	 catch(NexusException ne)
	 {
	     // Close file
	     m_nfwFile.closeFile();

	     // Propagate the exception
	     throw ne;
	 }

	 // Close file
	 m_nfwFile.closeFile();
	 return dsDatas;
     }

     // ---------------------------------------------------------
     // ---------------------------------------------------------
     /// Public free read / write section
     // ---------------------------------------------------------
     // ---------------------------------------------------------

     // ---------------------------------------------------------
     /// Public reading methods
     // ---------------------------------------------------------
     /**
      * ReadData
      * Read data to the given path
      *
      * @param pdPath path to set data in current file
      */
     public DataItem readData(PathData pdPath) throws NexusException
     {
	 DataItem oOutput;

	 // Open the file if exists
	 m_nfwFile.openFile();

	 try
	 {
	     // Read data from DataItem
	     oOutput = m_nfwFile.readData(pdPath);

	     // Prepare data for reading
	     oOutput = prepareReadDataItem(oOutput);
	 }
	 catch(NexusException ne)
	 {
	     // Close file
	     m_nfwFile.closeFile();

	     // Propagate the exception
	     throw ne;
	 }

	 // Close file
	 m_nfwFile.closeFile();

	 return oOutput;
     }

     /**
      * readData
      * Read all DataItems that are descendants (direct and indirect children) of the given group
      *
      * @param pgPath Path aiming a group from which the descendants DataItems are wanted
      */
     public DataItem[] readData(PathGroup pgPath) throws NexusException
     {
	 // Open the file
	 m_nfwFile.openFile();

	 // Open the given path
	 m_nfwFile.openPath(pgPath);

	 // Get all descendants datas
	 Stack<DataItem> lDatas = m_nfwFile.getDescendantsDatas();

	 // Close the file
	 m_nfwFile.closeFile();

	 return lDatas.toArray(new DataItem[lDatas.size()]);
     }

     public Object readAttr(String sName, PathNexus pnPath) throws NexusException
     {
	 Object oOutput;

	 // Open the file if exists
	 m_nfwFile.openFile();

	 try
	 {
	     oOutput = m_nfwFile.readAttr(sName, pnPath);
	 }
	 catch(NexusException ne)
	 {
	     // Close file
	     m_nfwFile.closeFile();

	     // Propagate the exception
	     throw ne;
	 }
	 // Close file
	 m_nfwFile.closeFile();

	 return oOutput;
     }

     // ---------------------------------------------------------
     /// Public writing methods
     // ---------------------------------------------------------

     /**
      * assureFileExistence
      * Ensure user that the targeted file exists
      */
     public void assureFileExistence() throws NexusException
     {
	 if( m_nfwFile.getFilePath() != null && m_nfwFile.getFilePath().trim().equals("") )
	     throw new NexusException("AcquisitionData has no file to ensure existence!");

	 m_nfwFile.openFile(NexusFile.NXACC_RDWR);
	 m_nfwFile.closeFile();
     }

     /**
      * WriteData
      * Write data to the given path
      *
      * @param dsData a DataItem object with values to be stored
      * @param pdPath path to set data in current file (can be absolute or local)
      * @note if path don't exists it will be automatically created
      */
     public void writeData(DataItem dsData, PathData pdPath) throws NexusException
     {
	 // Open the file if exists else create it
	 m_nfwFile.openFile(NexusFile.NXACC_RDWR);

	 try
	 {
	     // Prepare data to be put
	     dsData = prepareWriteDataItem(dsData);

	     // Put data
	     pdPath.applyClassPattern(FREE_PATTERN);
	     m_nfwFile.writeData(dsData, pdPath);
	 }
	 catch(NexusException ne)
	 {
	     // Close file
	     m_nfwFile.closeFile();

	     // Propagate the exception
	     throw ne;
	 }

	 // Close file
	 m_nfwFile.closeFile();
     }

     /**
      * writeData
      * Write a single data into the target path. Following methods are overload
      * of WriteData for all primitive types.
      *
      * @param tData a data value (single value or array) to be stored (of a primitive type)
      * @param pdPath path to set data in current file (can be absolute or local)
      * @note if path don't exists it will be automatically created
      */
     public <type> void writeData(type tData, PathData pdPath) throws NexusException
     {
	 // Prepare data to be put
	 Object oData;

	 if( null == tData )
	     oData = NexusFileWriter.NULL_VALUE;
	 else if( !tData.getClass().isArray() && !(tData instanceof String) )
	 {
	     oData = java.lang.reflect.Array.newInstance(tData.getClass(), 1);
	     java.lang.reflect.Array.set(oData, 0, tData);
	 }
	 else
	     oData = tData;

	 DataItem dsData = new DataItem(oData);

	 // Put data
	 writeData(dsData, pdPath);
     }

     /**
      * writeData
      * Write each data contained in the array into its own path. The targeted
      * file is the current one of AcquisitionData.
      *
      * @param datas an array of DataItem fully defined
      * @note each DataItem must have been initialized with a PathData
      */
     public void writeData(DataItem... datas) throws NexusException
     {
	 // Open the file
	 m_nfwFile.openFile(NexusFile.NXACC_RDWR);

	 // For each DataItem in the given array
	 DataItem currentDataItem = null;
	 for(int i = 0; i < datas.length; i++)
	 {
	     // Check the DataItem has a path
	     currentDataItem = datas[i];
	     if(currentDataItem.getPath() == null )
	     {
		 throw new NexusException("DataItem (" + datas[i].getName() + ") doesn't have a defined path!");
	     }

	     try
	     {
		 // Prepare data to be put
		 currentDataItem = prepareWriteDataItem(currentDataItem);

		 // Put data
		 currentDataItem.getPath().applyClassPattern(FREE_PATTERN);
		 m_nfwFile.writeData(currentDataItem, currentDataItem.getPath());
		 fireGroupSubDataWrited(i, datas.length);
	     }
	     catch(NexusException ne)
	     {
		 // Close file
		 m_nfwFile.closeFile();

		 // Propagate the exception
		 throw ne;
	     }
	 }

	 // Close the file
	 m_nfwFile.closeFile();
     }


     /**
      * writeAttr
      * Write an attribute on the node pointed by path.
      *
      * @param tData the data attribut's value to be stored (template argument: it can be any primitive or string)
      * @param pnPath path to set data in current file
      * @note path must have the form: [/]name1/name2/...nameN/name_of_data
      */
     public <type> void writeAttr(String sAttrName, type tData, PathNexus pnPath) throws NexusException
     {
	 // Open the file if exists else create it
	 m_nfwFile.openFile(NexusFile.NXACC_RDWR);

	 // Write the attribute
	 m_nfwFile.writeAttr(sAttrName, tData, pnPath);

	 // Close the file
	 m_nfwFile.closeFile();
     }

     /**
      * writeDeepCopy
      * Copy the sibling node and all its descendants into the given targeted group.
      * All attributes of each node will be copied.
      *
      * @param pnSrcPath path to reach the node to be copied (it can be from another file)
      * @param pgDstPath path targeting a group where to store node to be copied
      */
     public void writeDeepCopy(PathNexus pnSrcPath, PathGroup pgDstPath) throws NexusException
     {
	 // Open another file instance
	 NexusFileReader nfrSource = new NexusFileReader(pnSrcPath.getFilePath());

	 try
	 {
	     // Open path
	     nfrSource.setBufferSize(m_nfwFile.getBufferSize());
	     nfrSource.openFile(NexusFile.NXACC_READ);
	     nfrSource.openPath(pnSrcPath);

	     // Open the file if exists else create it
	     m_nfwFile.openFile(NexusFile.NXACC_RDWR);

	     try
	     {
		 // Open the destination path
		 m_nfwFile.createPath(pgDstPath, true);

		 // Copy nodes
		 m_nfwFile.copyNode(nfrSource);
	     }
	     catch(NexusException ne)
	     {
		 // Propagate the exception
		 throw ne;
	     }
	 }
	 catch(NexusException e)
	 {
	     // Close the file
	     m_nfwFile.closeFile();
	     nfrSource.closeFile();

	     // Propagate the exception
	     throw e;
	 }

	 // Close the file
	 m_nfwFile.closeFile();
	 nfrSource.closeFile();
     }

     /**
      * writeLink
      * Create a link positioned at pgDestPath in the current file that targets pgdestPath.
      *
      * @param pgTargPath path of the target
      * @param pgDestPath path designing where to store the link
      * @note Links can't target the acquisition (NXentry), it's due to the jnexus API
      * @note both path must be of same type: PathData or PathGroup
      */
     public void writeLink(PathNexus pgTargPath, PathNexus pgDestPath) throws NexusException
     {
	 // Open the file if exists else create it
	 m_nfwFile.openFile(NexusFile.NXACC_RDWR);

	 // Write the link
	 m_nfwFile.writeLink(pgTargPath, pgDestPath);

	 // Close the file
	 m_nfwFile.closeFile();
     }

     // ---------------------------------------------------------
     /// Public enumeration
     // ---------------------------------------------------------
     /**
      * Instrument
      * Enumeration of possible instruments.
      */
     public enum Instrument {
	 APERTURE      ("NXaperture"),
	 ATTENUATOR      ("NXattenuator"),
	 BEAM_STOP      ("NXbeam_stop"),
	 BENDING_MAGNET    ("NXbending_magnet"),
	 COLLIMATOR      ("NXcollimator"),
	 CRYSTAL        ("NXcrystal"),
	 DETECTOR      ("NXdetector"),
	 DISK_CHOPPER    ("NXdisk_chopper"),
	 FERMI_CHOPPER    ("NXfermi_chopper"),
	 FILTER        ("NXfilter"),
	 FLIPPER        ("NXflipper"),
	 GUIDE        ("NXguide"),
	 INSERTION_DEVICE  ("NXinsertion_device"),
	 INTENSITY_MONITOR  ("NXintensity_monitor"),
	 MIRROR        ("NXmirror"),
	 MODERATOR      ("NXmoderator"),
	 MONOCHROMATOR    ("NXmonochromator"),
	 POLARIZER      ("NXpolarizer"),
	 POSITIONER      ("NXpositioner"),
	 SOURCE        ("NXsource"),
	 VELOCITY_SELECTOR  ("NXvelocity_selector");

	 private String m_sName;

	 private Instrument(String sName) { m_sName = sName; }
	 public String getName() { return m_sName; }
     };

     // ---------------------------------------------------------
     /// Private definitions
     // ---------------------------------------------------------
     private final static String[]  FREE_PATTERN  = new String[] {"NXentry", "NXdata"};

     // ---------------------------------------------------------
     /// Private methods
     // ---------------------------------------------------------
     /**
      * prepareWriteDataItem
      * Fill the end of a NX_CHAR DataItem with END_STRING_CHAR to ensure it have a length MAX_LEN_CHAR of characters.
      * If this length is not forced, data could not be update after its creation (due to compatibility check).
      * Indeed for NX_CHAR DataItem, the dimension size returned is the exactly the string's length, not MAX_LEN_CHAR
      * DataItem's Initialization.
      *
      * @param dsData DataItem to be stored
      * @return the same DataItem ready for writing
      */
     private DataItem prepareWriteDataItem(DataItem dsData) throws NexusException
     {
	 // Checking if data is null
	 if( null == dsData || null == dsData.getData() )
	     dsData = new DataItem(NexusFileWriter.NULL_VALUE);

	 return dsData;
     }

     /**
      * prepareReadDataItem
      * Clean the end of a NX_CHAR DataItem's value, by deleting undesired chars. Indeed END_STRING_CHAR have been
      * added till the string reach the MAX_LEN_CHAR size. If the type isn't NX_CHAR is returned as it was.
      *
      * @param dsData a DataItem to be clean
      * @return a cleaned DataItem ready to be used after.
      */
     private DataItem prepareReadDataItem(DataItem dsData) throws NexusException
     {
	 if( dsData.getType() == NexusFile.NX_CHAR )
	 {
	     String sTmpStr = (String) dsData.getData();
	     int iSize = sTmpStr.length();

	     // Checking if data is null
	     if( NexusFileWriter.NULL_VALUE.equals(sTmpStr) )
	     {
		 sTmpStr = null;
		 iSize  = 0;
	     }

	     // Update data and its dimension
	     dsData.setData( new SoftReference<Object >(sTmpStr) );
	     dsData.getSize()[dsData.getSize().length-1] = iSize;
	     dsData.getSlabSize()[dsData.getSize().length-1] = iSize;
	 }
	 return dsData;
     }

     /**
      * listNode List all node of a specified class belonging to currently opened
      * node
      * 
      * @param sClassName wanted class name to be listed (NXgroup, NXdata...)
      * @return HashSet of NeXus nodes
      */

     protected List<NexusNode> listNode(String sClassName) throws NexusException {
	 String[] sClassNames = { sClassName };
	 return listNode(sClassNames, true);
     }

     /**
      * listNode List all node of a specified class belonging to currently opened
      * node. NexusNode's returned will be all those having sClassName as class
      * name or those which not having, depending on the value of bEqualityTest
      * 
      * @param sClassName the name of class on which selection test will be done
      * @param bEqualityTest boolean value filtering nodes having sClassName (if true), or
      *            filtering those not having sClassName (if false)
      * @return HashSet of NeXus nodes
      */

     protected List<NexusNode> listNode(String sClassName, boolean bEqualityTest)
	     throws NexusException {
	 String[] sClassNames = { sClassName };
	 return listNode(sClassNames, bEqualityTest);
     }

     /**
      * listNode List all node of specified classes belonging to currently opened
      * node. NexusNode's returned will be all those having a class name
      * contained in the array sClassNames if bEqualityTest is true. If
      * bEqualityTest is false, the returned list will be those which don't have
      * a class name contained in sClassNames.
      * 
      * @param sClassNames array of class names on which selection filter will be applied
      * @param bEqualityTest kind of selection on class name
      * @return HashSet of NeXus nodes
      */

     protected List<NexusNode> listNode(String[] sClassNames, boolean bEqualityTest)
	     throws NexusException {
	 List<NexusNode> hChildren = m_nfwFile.listChildren();
	 List<NexusNode> alNameList;
	 String sClassName;
	 String sItemClass;

	 // Parse children nodes
	 alNameList = new ArrayList<NexusNode>();
	 for (NexusNode node : hChildren ) {
	     // Check class to add node name of the requested type
	     sItemClass = node.getClassName();
	     for (int i = 0; i < sClassNames.length; i++) {
		 sClassName = sClassNames[i];
		 if ((sItemClass.equals(sClassName)) == bEqualityTest ) {
		     alNameList.add(node);
		     break;
		 }
	     }
	 }
	 return alNameList;
     }


}

