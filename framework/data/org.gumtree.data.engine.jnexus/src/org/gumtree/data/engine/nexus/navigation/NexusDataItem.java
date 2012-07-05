package org.gumtree.data.engine.nexus.navigation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.gumtree.data.engine.nexus.array.NexusArray;
import org.gumtree.data.engine.nexus.array.NexusIndex;
import org.gumtree.data.exception.BackupException;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.exception.NoResultException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IDimension;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.data.interfaces.IRange;
import org.gumtree.data.utils.Utilities.ModelType;
import org.nexusformat.NexusFile;

import fr.soleil.nexus.DataItem;
import fr.soleil.nexus.PathGroup;
import fr.soleil.nexus.DataItem.Data;

public final class NexusDataItem implements IDataItem, Cloneable {

    // Inner class
    // Associate a IDimension to an order of the array 
    private static class DimOrder {
	// Members
	private int          mOrder;        // order of the corresponding dimension in the NxsDataItem
	private IDimension   mDimension;    // dimension object

	public DimOrder(int order, IDimension dim) { 
	    mOrder     = order;
	    mDimension = dim;
	}

	public int order() { 
	    return mOrder;
	}

	public IDimension dimension() {
	    return mDimension;
	}
    }    

    /// Members
    private NexusDataset   mCDMDataset;    // CDMA IDataset i.e. file handler
    private IGroup         mParent = null; // parent group
    private DataItem       mn4tDataItem;   // NeXus dataitem support of the data
    private IArray         mArray = null;  // CDMA IArray supporting a view of the data
    private List<DimOrder> mDimensions;    // list of dimensions
    private String         mFactory;

    /// Constructors
    public NexusDataItem(final NexusDataItem dataItem)
    {
	mFactory      = dataItem.mFactory;
	mCDMDataset   = dataItem.mCDMDataset;
	mn4tDataItem  = dataItem.getN4TDataItem();
	mDimensions   = new ArrayList<DimOrder> (dataItem.mDimensions);
	mParent       = dataItem.getParentGroup();
	mArray        = null;
	try {
	    mArray = new NexusArray((NexusArray) dataItem.getData());
	} catch (IOException e) {
	}
    }

    public NexusDataItem(String factoryName, DataItem data, NexusDataset handler) {
	mFactory     = factoryName;
	mCDMDataset  = handler;
	mn4tDataItem = data;
	mDimensions  = new ArrayList<DimOrder>();
	mParent      = null;
	mArray       = null;
    }

    public NexusDataItem(String factoryName, DataItem data, IGroup parent, NexusDataset handler) {
	mFactory     = factoryName;
	mCDMDataset  = handler;
	mn4tDataItem = data;
	mDimensions  = new ArrayList<DimOrder>();
	mParent      = parent;
	mArray       = null;
    }

    /// Methods
    @Override
    public ModelType getModelType() {
	return ModelType.DataItem;
    }

    @Override
    public List<IAttribute> getAttributeList()
    {
	HashMap<String, DataItem.Data<?>> inList;
	List<IAttribute> outList = new ArrayList<IAttribute>();
	NexusAttribute tmpAttr;

	inList = mn4tDataItem.getAttributes();

	Entry<String, Data<?>> sAttr;
	Iterator<Entry<String, Data<?>>> iter = inList.entrySet().iterator();
	while( iter.hasNext() )
	{
	    sAttr = iter.next();
	    tmpAttr   = new NexusAttribute(mFactory, sAttr.getKey(), sAttr.getValue().getValue());
	    outList.add(tmpAttr);
	}

	return outList;
    }

    @Override
    public IArray getData() throws IOException
    {
	if( mArray == null ) {
	    mArray = new NexusArray(mFactory, mn4tDataItem);
	}
	return mArray;
    }

    @Override
    public IArray getData(int[] origin, int[] shape) throws IOException, InvalidRangeException
    {
	IArray array = getData().copy(false);
	IIndex index = new NexusIndex(this.mFactory, shape, origin, shape);

	// TODO changer ici !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!







	/*IIndex index = array.getIndex(); 
        index.setShape(shape);
        index.setOrigin(origin);*/
	array.setIndex(index);
	return array;
    }

    @Override
    public IDataItem clone()
    {
	return new NexusDataItem(this);
    }

    @Override
    public void addOneAttribute(IAttribute att) {
	mn4tDataItem.setAttribute(att.getName(), att.getValue().getStorage());
    }

    @Override
    public void addStringAttribute(String name, String value) {
	mn4tDataItem.setAttribute(name, value);
    }

    @Override
    public IAttribute getAttribute(String name) {
	HashMap<String, Data<?> > inList;

	inList = mn4tDataItem.getAttributes();

	Entry<String, Data<?>> sAttr;
	Iterator<Entry<String, Data<?>>> iter = inList.entrySet().iterator();
	while( iter.hasNext() )
	{
	    sAttr = iter.next();
	    if( sAttr.getKey().equals(name) ) {
		return new NexusAttribute(mFactory, sAttr.getKey(), sAttr.getValue().getValue());
	    }
	}

	return null;
    }

    @Override
    public IAttribute findAttributeIgnoreCase(String name) {
	HashMap<String, Data<?>> inList = mn4tDataItem.getAttributes();

	Entry<String, Data<?>> sAttr;
	Iterator<Entry<String, Data<?>>> iter = inList.entrySet().iterator();
	while( iter.hasNext() )
	{
	    sAttr = iter.next();
	    if( sAttr.getKey().equalsIgnoreCase(name) ) {
		return new NexusAttribute(mFactory, sAttr.getKey(), sAttr.getValue().getValue());
	    }
	}

	return null;
    }

    @Override
    public int findDimensionIndex(String name) {
	for( DimOrder dimord : mDimensions ) {
	    if( dimord.dimension().getName().equals(name) ) {
		return dimord.order();
	    }
	}

	return -1;
    }

    @Override
    public String getDescription() {
	String sDesc = null;

	sDesc = mn4tDataItem.getAttribute("long_name");
	if( sDesc == null ) {
	    sDesc = mn4tDataItem.getAttribute("description");
	}
	if( sDesc == null ) {
	    sDesc = mn4tDataItem.getAttribute("title");
	}
	if( sDesc == null ) {
	    sDesc = mn4tDataItem.getAttribute("standard_name");
	}
	if( sDesc == null ) {
	    sDesc = mn4tDataItem.getAttribute("name");
	}

	return sDesc;
    }

    @Override
    public List<IDimension> getDimensions(int i) {
	ArrayList<IDimension> list = new ArrayList<IDimension>();

	for( DimOrder dim : mDimensions ) {
	    if( dim.order() == i ) {
		list.add( mDimensions.get(i).dimension() );
	    }
	}

	if( list.size() > 0 ) { 
	    return list;
	}
	else {
	    return null;
	}
    }

    @Override
    public List<IDimension> getDimensionList() {
	ArrayList<IDimension> list = new ArrayList<IDimension>();

	for( DimOrder dimOrder : mDimensions ) {
	    list.add(dimOrder.dimension());
	}

	return list;
    }

    @Override
    public String getDimensionsString() {
	StringBuffer dimList = new StringBuffer();

	int i = 0;
	for( DimOrder dim : mDimensions ) {
	    if( i++ != 0 ) {
		dimList.append(" ");
	    }
	    dimList.append(dim.dimension().getName());
	}

	return dimList.toString();
    }

    @Override
    public int getElementSize() {
	int result;
	switch( mn4tDataItem.getType() ) {
	case NexusFile.NX_BINARY:
	    result = 1;
	    break;
	case NexusFile.NX_BOOLEAN:
	    result = 1;
	    break;
	case NexusFile.NX_CHAR:
	    result = 1;
	    break;
	case NexusFile.NX_INT16:
	    result = 2;
	    break;
	case NexusFile.NX_FLOAT32:
	    result = 2;
	    break;
	case NexusFile.NX_INT32:
	    result = 4;
	    break;
	case NexusFile.NX_FLOAT64:
	    result = 4;
	    break;
	case NexusFile.NX_INT64:
	    result = 8;
	    break;
	default:
	    result = 1;
	    break;
	}
	return result;
    }

    @Override
    public String getName() {
	return mn4tDataItem.getPath().toString(false);
    }

    @Override
    public String getNameAndDimensions() {
	StringBuffer buf = new StringBuffer();
	getNameAndDimensions(buf, true, false);
	return buf.toString();
    }

    @Override
    public void getNameAndDimensions(StringBuffer buf, boolean useFullName,
	    boolean showDimLength) {
	String name = useFullName ? getName() : getShortName();
	buf.append(name);

	if (getRank() > 0) {
	    buf.append("(");
	}
	for (int i = 0; i < mDimensions.size(); i++) {
	    DimOrder dim   = mDimensions.get(i);
	    IDimension myd = dim.mDimension;
	    String dimName = myd.getName();
	    if ( dimName == null ){
		dimName = "";
	    }

	    if (myd.isShared()) {
		if (i != 0) {
		    buf.append(", ");
		}
		buf.append(dimName);
		if (showDimLength) {
		    buf.append( "=" );
		    if (myd.isVariableLength()) {
			buf.append("*");
		    }
		    else {
			buf.append( myd.getLength() );
		    }
		}
	    }
	}

	if (getRank() > 0) {
	    buf.append(")");
	}
    }

    @Override
    public IGroup getParentGroup()
    {
	if( mParent == null )
	{
	    PathGroup path = mn4tDataItem.getPath().getParentPath();
	    try {
		mParent = (IGroup) mCDMDataset.getRootGroup().findContainerByPath(path.getValue());
		((NexusGroup) mParent).setChild(this);
	    } catch (NoResultException e) {}
	}
	return mParent;
    }

    @Override
    public List<IRange> getRangeList() {
	List<IRange> list = null;
	try {
	    list = new NexusIndex(mFactory, getData().getShape()).getRangeList();
	} catch( IOException e ) {
	}
	return list;
    }

    @Override
    public List<IRange> getSectionRanges() {
	List<IRange> list = null;
	try {
	    list = ((NexusIndex) getData().getIndex()).getRangeList(); 
	} catch( IOException e ) {
	}
	return list;
    }

    @Override
    public int getRank() {
	int[] shape = getShape();
	int rank;
	if( mn4tDataItem.getType() == NexusFile.NX_CHAR ) {
	    rank = 0;
	}
	else if( shape.length == 1 && shape[0] == 1 ) {
	    rank = 0;
	}
	else {
	    rank = shape.length;
	}
	return rank;
    }

    @Override
    public IDataItem getSection(List<IRange> section) throws InvalidRangeException {
	NexusDataItem item = null;
	try {
	    item = new NexusDataItem(this);
	    mArray = (NexusArray) item.getData().getArrayUtils().sectionNoReduce(section).getArray();
	} catch( IOException e ) {
	}
	return item;
    }



    @Override
    public int[] getShape() {
	int[] shape;
	try {
	    shape = getData().getShape();
	} catch (IOException e) {
	    shape = new int[] {-1};
	}
	return shape;
    }

    @Override
    public String getShortName() {
	return mn4tDataItem.getNodeName();
    }

    @Override
    public long getSize() {
	int[] shape = mn4tDataItem.getSize();
	int size    = shape[0];
	for( int i = 1; i < shape.length; i++ ) {
	    size *= shape[i];
	}

	return size;
    }

    @Override
    public int getSizeToCache() {
	// TODO Auto-generated method stub
	new BackupException( NexusDataset.ERR_NOT_SUPPORTED ).printStackTrace();
	return 0;
    }

    @Override
    public IDataItem getSlice(int dim, int value) throws InvalidRangeException {
	NexusDataItem item = new NexusDataItem(this);
	try {
	    item.mArray = item.getData().getArrayUtils().slice(dim, value).getArray();
	}
	catch (Exception e) {
	    item = null;
	}
	return item;
    }

    @Override
    public IDataItem getASlice(int dimension, int value) throws InvalidRangeException {
	return getSlice(dimension, value);
    }

    @Override
    public Class<?> getType() {
	return mn4tDataItem.getDataClass();
    }

    @Override
    public String getUnitsString() {
	IAttribute attr = getAttribute("unit");
	String value = null;
	if( attr != null ) {
	    value = attr.getStringValue();
	}
	return value;
    }

    @Override
    public boolean hasAttribute(String name, String value) {
	IAttribute attr;
	List<IAttribute> listAttr = getAttributeList();

	Iterator<IAttribute> iter = listAttr.iterator();
	while( iter.hasNext() )
	{
	    attr = iter.next();
	    if( attr.getStringValue().equals(value) ) {
		return true;
	    }
	}
	return false;
    }

    @Override
    public boolean hasCachedData() {
	// TODO Auto-generated method stub
	new BackupException( NexusDataset.ERR_NOT_SUPPORTED ).printStackTrace();
	return false;
    }

    @Override
    public void invalidateCache() {
	// TODO Auto-generated method stub
	new BackupException( NexusDataset.ERR_NOT_SUPPORTED ).printStackTrace();
    }

    @Override
    public boolean isCaching() {
	// TODO Auto-generated method stub
	new BackupException( NexusDataset.ERR_NOT_SUPPORTED ).printStackTrace();
	return false;
    }

    @Override
    public boolean isMemberOfStructure() {
	// TODO Auto-generated method stub
	new BackupException( NexusDataset.ERR_NOT_SUPPORTED ).printStackTrace();
	return false;
    }

    @Override
    public boolean isMetadata() {
	return ( getAttribute("signal") == null );
    }

    @Override
    public boolean isScalar() {
	int rank = 0;
	try {
	    rank = getData().getRank();
	} catch(IOException e) {
	}
	return (rank == 0);
    }

    @Override
    public boolean isUnlimited() {
	return false;
    }

    @Override
    public boolean isUnsigned() {
	int type = mn4tDataItem.getType(); 
	if(
		type == NexusFile.NX_UINT16 ||
		type == NexusFile.NX_UINT32 ||
		type == NexusFile.NX_UINT64 ||
		type == NexusFile.NX_UINT8
		) {
	    return true;
	}
	else {
	    return false;
	}
    }

    @Override
    public byte readScalarByte() throws IOException {
	return ((byte[]) mn4tDataItem.getData())[0];
    }

    @Override
    public double readScalarDouble() throws IOException {
	return ((byte[]) mn4tDataItem.getData())[0];
    }

    @Override
    public float readScalarFloat() throws IOException {
	return ((float[]) mn4tDataItem.getData())[0];
    }

    @Override
    public int readScalarInt() throws IOException {
	return ((int[]) mn4tDataItem.getData())[0];
    }

    @Override
    public long readScalarLong() throws IOException {
	return ((long[]) mn4tDataItem.getData())[0];
    }

    @Override
    public short readScalarShort() throws IOException {
	return ((short[]) mn4tDataItem.getData())[0];
    }

    @Override
    public String readScalarString() throws IOException {
	return (String) mn4tDataItem.getData();
    }

    @Override
    public boolean removeAttribute(IAttribute a) {
	mn4tDataItem.setAttribute(a.getName(), null);
	return false;
    }

    @Override
    public void setCachedData(IArray cacheData, boolean isMetadata)
	    throws InvalidArrayTypeException {
	// TODO Auto-generated method stub
	new BackupException( NexusDataset.ERR_NOT_SUPPORTED ).printStackTrace();

    }

    @Override
    public void setCaching(boolean caching) {
	// TODO Auto-generated method stub
	new BackupException( NexusDataset.ERR_NOT_SUPPORTED ).printStackTrace();
    }

    @Override
    public void setDataType(Class<?> dataType) {
	try {
	    // TODO
	    throw new BackupException( NexusDataset.ERR_NOT_SUPPORTED );
	} catch(BackupException e) {
	    e.printStackTrace();
	}
    }

    @Override
    public void setDimensions(String dimString) {
	mParent = getParentGroup();

	List<String> dimNames = java.util.Arrays.asList(dimString.split(" "));
	List<IDataItem> items = mParent.getDataItemList();

	for( IDataItem item : items ) {
	    IAttribute attr = item.getAttribute("axis");
	    if( attr != null ) {
		if( "*".equals(dimString) ) {
		    setDimension(new NexusDimension(mFactory, item), attr.getNumericValue().intValue() );
		}
		else if( dimNames.contains(attr.getName()) ) {
		    setDimension(new NexusDimension(mFactory, item), attr.getNumericValue().intValue() );
		}
	    }
	}
    }

    @Override
    public void setDimension(IDimension dim, int ind) {
	mDimensions.add( new DimOrder(ind, dim) );
    }

    @Override
    public void setElementSize(int elementSize) {
	try {
	    // TODO
	    throw new BackupException("Method not support in plug-in: setElementSize(int elementSize)!");
	} catch(BackupException e) {
	    e.printStackTrace();
	}
    }

    @Override
    public void setName(String name) {
	mn4tDataItem.setAttribute("name", name);
    }

    @Override
    public void setParent(IGroup group) {
	if( mParent == null || ! mParent.equals(group) )
	{
	    mParent = group;
	    group.addDataItem(this);
	}
    }

    @Override
    public void setSizeToCache(int sizeToCache) {
	// TODO Auto-generated method stub
	new BackupException(NexusDataset.ERR_NOT_SUPPORTED).printStackTrace();

    }

    @Override
    public String toStringDebug() {
	StringBuffer strDebug = new StringBuffer();
	strDebug.append( getName() );
	if( strDebug.length() > 0 ) {
	    strDebug.append("\n");
	}
	try {
	    strDebug.append("shape: ").append(getData().shapeToString()).append("\n");
	    List<IDimension> dimensions = getDimensionList();
	    for( IDimension dim : dimensions ) {
		strDebug.append(dim.getCoordinateVariable().toString());
	    }

	    List<IAttribute> list = getAttributeList();
	    if( list.size() > 0 ) {
		strDebug.append("\nAttributes:\n");
	    }
	    for( IAttribute a : list ) {
		strDebug.append("- ").append(a.toString()).append("\n");
	    }
	} catch( IOException e ) {
	}

	return strDebug.toString();
    }

    @Override
    public String writeCDL(String indent, boolean useFullName, boolean strict) {
	// TODO Auto-generated method stub
	new BackupException(NexusDataset.ERR_NOT_SUPPORTED).printStackTrace();
	return null;
    }

    @Override
    public void setUnitsString(String units) {
	// TODO Auto-generated method stub
	new BackupException(NexusDataset.ERR_NOT_SUPPORTED).printStackTrace();
    }

    @Override
    public IDataset getDataset() {
	return mParent.getDataset();
    }

    @Override
    public String getLocation() {
	return mParent.getLocation();
    }

    @Override
    public IGroup getRootGroup() {
	return mParent.getRootGroup();
    }

    @Override
    public void setShortName(String name) {
	mn4tDataItem.setNodeName(name);

    }

    @Override
    public String getFactoryName() {
	return mFactory;
    }

    public DataItem getN4TDataItem()
    {
	return mn4tDataItem;
    }
    // ------------------------------------------------------------------------
    /// Protected methods
    // ------------------------------------------------------------------------
}
