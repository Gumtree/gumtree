package org.gumtree.data.engine.nexus.navigation;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.exception.WriterException;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IContainer;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;
import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;

import fr.soleil.nexus.DataItem;
import fr.soleil.nexus.NexusFileWriter;
import fr.soleil.nexus.NexusNode;
import fr.soleil.nexus.PathData;
import fr.soleil.nexus.PathGroup;
import fr.soleil.nexus.PathNexus;


public abstract class NexusDataset implements IDataset, Cloneable {

    public static final String ERR_NOT_SUPPORTED = "Method not supported yet in this plug-in!";

    public NexusDataset( String factoryName, File nexusFile, int buffer_size ) {
	mFactory      = factoryName;
	mN4TWriter    = null;
	mRootPhysical = null;
	mN4TWriter    = new NexusFileWriter(nexusFile.getAbsolutePath());
	mN4TCurPath   = PathNexus.ROOT_PATH.clone();
	mTitle        = nexusFile.getName();
	mN4TCurPath.setFile(nexusFile.getAbsolutePath());
	mN4TWriter.setBufferSize(buffer_size);
	mN4TWriter.isSingleRawResult(true);
	mN4TWriter.setCompressedData(true);
    }

    public NexusDataset( String factoryName, File nexusFile )
    {
	this(factoryName, nexusFile, N4T_BUFF_SIZE );
    }

    public NexusDataset( NexusDataset dataset )
    {
	mFactory      = dataset.mFactory;
	mRootPhysical = dataset.mRootPhysical;
	mN4TWriter    = dataset.mN4TWriter;
	mN4TCurPath   = dataset.mN4TCurPath.clone();
	mTitle        = dataset.mTitle;
	mN4TWriter.isSingleRawResult(true);
    }

    @Override
    public void open() throws IOException
    {
	try
	{
	    mN4TWriter.closeFile();
	    mN4TWriter.openFile(mN4TCurPath.getFilePath(), NexusFile.NXACC_READ);
	    mN4TWriter.openPath(mN4TCurPath);
	}
	catch(NexusException ne)
	{
	    throw new IOException(ne);
	}
    }

    /// Methods
    @Override
    public void close() throws IOException
    {
	try
	{
	    mN4TWriter.closeFile();
	}
	catch(NexusException ne)
	{
	    throw new IOException(ne);
	}
    }


    @Override
    public String getLocation() {
	if( mN4TCurPath != null )
	{
	    return mN4TCurPath.getFilePath();
	}
	return null;
    }

    @Override
    public String getTitle() {
	return mTitle;
    }

    @Override
    public void save() throws WriterException {
	List<IDataItem> items = new ArrayList<IDataItem>(); 
	NexusGroup.getDescendentDataItem(items, mRootPhysical);
	try {
	    // Open the destination file
	    mN4TWriter.openFile(mN4TWriter.getFilePath(), NexusFile.NXACC_RDWR);

	    // Save each IDataItem
	    DataItem data;
	    for( IDataItem item : items ) {
		data = ((NexusDataItem) item).getN4TDataItem();
		mN4TWriter.writeData(data, data.getPath());

	    }

	    // Close the file
	    mN4TWriter.closeFile();
	} catch(NexusException e) {
	    throw new WriterException(e.getMessage(), e);
	}
    }

    @Override
    public void setLocation(String location)
    {
	String sCurFile = "";
	PathNexus path = new PathGroup(location.split("/"));

	if( ! mRootPhysical.equals(PathNexus.ROOT_PATH) ) {
	    sCurFile = mRootPhysical.getLocation();
	}

	try
	{
	    mN4TWriter.openPath(path);
	}
	catch(NexusException e1)
	{
	    NexusNode topNode = path.popNode();
	    path = new PathData((PathGroup) path, topNode.getNodeName());
	    try
	    {
		mN4TWriter.openPath(path);
	    } catch(NexusException e2) {}
	}
	mN4TCurPath = path;
	mN4TCurPath.setFile(sCurFile);
    }

    @Override
    public void setTitle(String title) {
	mTitle = title;
    }

    @Override
    public boolean isOpen() {
	return mN4TWriter.isFileOpened();
    }

    public NexusFileWriter getHandler()
    {
	return mN4TWriter;
    }

    // -----------------------------------------------------------
    /// protected methods
    // -----------------------------------------------------------
    // Accessors
    protected PathNexus getCurrentPath()
    {
	return mN4TCurPath;
    }

    protected PathNexus getRootPath()
    {
	return ((NexusGroup) mRootPhysical).getPathNexus();
    }

    protected void setLocation(PathNexus location)
    {
	try
	{
	    mN4TWriter.openPath(location);
	} catch(NexusException e) {
	}
	mN4TCurPath = location.clone();
    }

    // Methods
    protected void setRootGroup(PathNexus rootPath)
    {
	mRootPhysical = new NexusGroup(mFactory, rootPath, this);
    }

    @Override
    public String getFactoryName() {
	return mFactory;
    }

    @Override
    public IGroup getRootGroup() {
	if( mRootPhysical == null ) {
	    mRootPhysical = new NexusGroup(mFactory, null, PathNexus.ROOT_PATH.clone(), this);
	}
	return mRootPhysical;
    }

    @Override
    public boolean sync() throws IOException {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public void saveTo(String location) throws WriterException {
	// TODO Auto-generated method stub

    }

    @Override
    public void save(IContainer container) throws WriterException {
	// TODO Auto-generated method stub

    }

    @Override
    public void save(String parentPath, IAttribute attribute)
	    throws WriterException {
	// TODO Auto-generated method stub

    }

    @Override
    public void writeNcML(OutputStream os, String uri) throws IOException {
	// TODO Auto-generated method stub

    }

    private IGroup           mRootPhysical;       // Physical root of the document 
    private NexusFileWriter  mN4TWriter;          // Instance manipulating the NeXus file
    private String           mTitle;
    private String           mFactory;
    private PathNexus        mN4TCurPath;         // Instance of the current path
    private static final int N4T_BUFF_SIZE = 300; // Size of the buffer managed by NeXus4Tango

}
