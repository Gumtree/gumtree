package org.gumtree.data.soleil.navigation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.gumtree.data.engine.nexus.navigation.NexusDataItem;
import org.gumtree.data.engine.nexus.navigation.NexusDimension;
import org.gumtree.data.exception.BackupException;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IDimension;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.data.interfaces.IRange;
import org.gumtree.data.soleil.NxsFactory;
import org.gumtree.data.soleil.array.NxsArray;
import org.gumtree.data.soleil.array.NxsIndex;
import org.gumtree.data.utils.Utilities.ModelType;
import org.nexusformat.NexusFile;

import fr.soleil.nexus.DataItem;

public final class NxsDataItem implements IDataItem, Cloneable {

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
    private NxsDataset      mDataset;   // CDMA IDataset i.e. file handler
    private IGroup          mParent;    // parent group
    private NexusDataItem[] mDataItems; // NeXus dataitem support of the data
    private IArray          mArray;     // CDMA IArray supporting a view of the data
    private List<DimOrder>  mDimension; // list of dimensions


    /// Constructors
    public NxsDataItem(final NxsDataItem dataItem) {
        mDataset   = dataItem.mDataset;
        mDataItems = dataItem.mDataItems.clone();
        mDimension = new ArrayList<DimOrder> (dataItem.mDimension);
        mParent    = dataItem.getParentGroup();
        mArray     = null;
        try {
            if( mDataItems.length == 1 ) {
                mArray = new NxsArray((NxsArray) dataItem.getData());
            }
            else {
                mArray = new NxsArray((NxsArray) dataItem.getData());
            }
        } catch (IOException e) {
        }
    }

    public NxsDataItem(NexusDataItem[] data, IGroup parent, NxsDataset handler) {
        mDataset   = handler;
        mDataItems = data.clone();
        mDimension = new ArrayList<DimOrder>();
        mParent    = parent;
        mArray     = null;
    }

    public NxsDataItem(NexusDataItem item, IGroup parent, NxsDataset dataset) {
        this(new NexusDataItem[] {item}, parent, dataset);
    }

    public NxsDataItem(NxsDataItem[] items, IGroup parent, NxsDataset dataset) {
        ArrayList<NexusDataItem> list = new ArrayList<NexusDataItem>();
        for( NxsDataItem cur : items ) {
            for( NexusDataItem item : cur.mDataItems ) {
                list.add(item);
            }
        }
        mDataItems = list.toArray( new NexusDataItem[list.size()] );

        mDataset   = dataset;
        mDimension = new ArrayList<DimOrder>();
        mParent    = parent;
        mArray     = null;
    }

    /// Methods
    @Override
    public ModelType getModelType() {
        return ModelType.DataItem;
    }

    @Override
    public List<IAttribute> getAttributeList() {
        //[soleil][clement][12/02/2011] TODO ensure method is correct: shall not we take all attributes of all nodes ?
        return mDataItems[0].getAttributeList();
    }

    @Override
    public IArray getData() throws IOException {
        if( mArray == null && mDataItems.length > 0 ) {
            IArray[] arrays = new IArray[mDataItems.length];
            for( int i = 0; i < mDataItems.length; i++ ) {
                arrays[i] = mDataItems[i].getData();
            }
            mArray = new NxsArray(arrays);
        }
        return mArray;
    }

    @Override
    public IArray getData(int[] origin, int[] shape) throws IOException, InvalidRangeException {
        IArray array = getData().copy(false);
        IIndex index = array.getIndex();
        int str      = 1;
        long[] stride = new long[array.getRank()];
        for( int i = array.getRank() - 1; i >= 0; i-- ) {
            stride[i] = str;
            str *= shape[i];
        }
        index.setStride(stride);
        index.setShape(shape);
        index.setOrigin(origin);
        array.setIndex(index);
        return array;
    }

    @Override
    public IDataItem clone() {
        return new NxsDataItem(this);
    }

    @Override
    public void addOneAttribute(IAttribute att) {
        mDataItems[0].addOneAttribute(att);
    }

    @Override
    public void addStringAttribute(String name, String value) {
        mDataItems[0].addStringAttribute(name, value);
    }

    @Override
    public IAttribute getAttribute(String name) {
        IAttribute result = null;

        for( IDataItem item : mDataItems ) {
            result = item.getAttribute(name);
            if( result != null ) {
                break;
            }
        }

        return result;
    }

    @Override
    public IAttribute findAttributeIgnoreCase(String name) {
        IAttribute result = null;

        for( IDataItem item : mDataItems ) {
            result = item.findAttributeIgnoreCase(name);
            if( result != null ) {
                break;
            }
        }

        return result;
    }

    @Override
    public int findDimensionIndex(String name) {
        int result = -1;
        for( DimOrder dimord : mDimension ) {
            if( dimord.mDimension.getName().equals(name) ) {
                result = dimord.order();
                break;
            }
        }

        return result;
    }

    @Override
    public String getDescription() {
        String result = null;

        for( IDataItem item : mDataItems ) {
            result = item.getDescription();
            if( result != null ) {
                break;
            }
        }

        return result;
    }

    @Override
    public List<IDimension> getDimensions(int i) {
        ArrayList<IDimension> list = null;

        if( i <= getRank() ) {
            list = new ArrayList<IDimension>();
            for( DimOrder dim : mDimension ) {
                if( dim.order() == i ) {
                    list.add( dim.dimension() );
                }
            }
        }

        return list;
    }

    @Override
    public List<IDimension> getDimensionList() {
        ArrayList<IDimension> list = new ArrayList<IDimension>();

        for( DimOrder dimOrder : mDimension ) {
            list.add( dimOrder.dimension() );
        }

        return list;
    }

    @Override
    public String getDimensionsString() {
        StringBuffer dimList = new StringBuffer();

        int i = 0;
        for( DimOrder dim : mDimension ) {
            if( i++ != 0 ) {
                dimList.append(" ");
            }
            dimList.append( dim.dimension().getName() );
        }

        return dimList.toString();
    }

    @Override
    public int getElementSize() {
        return mDataItems[0].getElementSize();
    }

    @Override
    public String getName() {
        return mDataItems[0].getName();
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
        useFullName = useFullName && !showDimLength;
        String name = useFullName ? getName() : getShortName();
        buf.append(name);

        if (getRank() > 0) {
            buf.append("(");
        }
        for (int i = 0; i < mDimension.size(); i++) {
            DimOrder dim   = mDimension.get(i);
            IDimension myd = dim.dimension();
            String dimName = myd.getName();
            if ((dimName == null) || !showDimLength) {
                dimName = "";
            }

            if (i != 0) {
                buf.append(", ");
            }

            if (myd.isVariableLength()) {
                buf.append("*");
            }
            else if (myd.isShared()) {
                if (!showDimLength) {
                    buf.append(dimName);
                    buf.append("=");
                    buf.append( myd.getLength() );
                }
                else {
                    buf.append(dimName);
                }
            }
            else {
                if (dimName != null) {
                    buf.append(dimName);
                }
                buf.append(myd.getLength());
            }
        }
        if (getRank() > 0) {
            buf.append(")");
        }
    }

    @Override
    public IGroup getParentGroup()
    {
        return mParent;
    }

    @Override
    public List<IRange> getRangeList() {
        List<IRange> list = null;
        try {
            list = new NxsIndex(getData().getShape()).getRangeList();
        } catch( IOException e ) {
        }
        return list;
    }

    @Override
    public List<IRange> getSectionRanges() {
        List<IRange> list = null;
        try {
            list = ((NxsIndex) getData().getIndex()).getRangeList(); 
        } catch( IOException e ) {
        }
        return list;
    }

    @Override
    public int getRank() {
        int result;
        int[] shape = getShape();
        if( mDataItems[0].getN4TDataItem().getType() == NexusFile.NX_CHAR ) {
            result = 0;
        }
        else if( shape.length == 1 && shape[0] == 1 ) {
            result = 0;
        }      
        else {
            result = shape.length;
        }
        return result;
    }

    @Override
    public IDataItem getSection(List<IRange> section)
            throws InvalidRangeException {
        NxsDataItem item = null;
        try {
            item = new NxsDataItem(this);
            mArray = (NxsArray) item.getData().getArrayUtils().sectionNoReduce(section).getArray();
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
            shape = new int[] {};
        }
        return shape;
    }

    @Override
    public String getShortName() {
        return mDataItems[0].getShortName();
    }

    @Override
    public long getSize() {
        int[] shape = getShape();
        long  total = 1;
        for( int size : shape ) {
            total *= size;
        }

        return total;
    }

    @Override
    public int getSizeToCache() {
        // TODO Auto-generated method stub
        new BackupException(NxsFactory.ERR_NOT_SUPPORTED).printStackTrace();
        return 0;
    }

    @Override
    public IDataItem getSlice(int dim, int value) throws InvalidRangeException {
        NxsDataItem item = new NxsDataItem(this);
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
        return mDataItems[0].getType();
    }

    @Override
    public String getUnitsString() {
        String value = null;
        IAttribute attr = getAttribute("unit");
        if( attr != null ) {
            value = attr.getStringValue();
        }
        return value;
    }

    @Override
    public boolean hasAttribute(String name, String value) {
        boolean result = false;
        IAttribute attr;
        List<IAttribute> listAttr = getAttributeList();

        Iterator<IAttribute> iter = listAttr.iterator();
        while( iter.hasNext() )
        {
            attr = iter.next();
            if( attr.getStringValue().equals(value) ) {
                result = true;
                break;
            }
        }
        return result;
    }

    @Override
    public boolean hasCachedData() {
        return mDataItems[0].hasCachedData();
    }

    @Override
    public void invalidateCache() {
        mDataItems[0].invalidateCache();
    }

    @Override
    public boolean isCaching() {
        return mDataItems[0].isCaching();
    }

    @Override
    public boolean isMemberOfStructure() {
        return mDataItems[0].isMemberOfStructure();
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
        return mDataItems[0].isUnsigned(); 
    }

    @Override
    public byte readScalarByte() throws IOException {
        return ((byte[]) mDataItems[0].getData().getStorage())[0];
    }

    @Override
    public double readScalarDouble() throws IOException {
        return ((double[]) mDataItems[0].getData().getStorage())[0];
    }

    @Override
    public float readScalarFloat() throws IOException {
        return ((float[]) mDataItems[0].getData().getStorage())[0];
    }

    @Override
    public int readScalarInt() throws IOException {
        return ((int[]) mDataItems[0].getData().getStorage())[0];
    }

    @Override
    public long readScalarLong() throws IOException {
        return ((long[]) mDataItems[0].getData().getStorage())[0];
    }

    @Override
    public short readScalarShort() throws IOException {
        return ((short[]) mDataItems[0].getData().getStorage())[0];
    }

    @Override
    public String readScalarString() throws IOException {
        return (String) mDataItems[0].readScalarString();
    }

    @Override
    public boolean removeAttribute(IAttribute attr) {
        boolean result = false;
        for( IDataItem item : mDataItems ) {
            item.removeAttribute(attr);
        }
        result = true;
        return result;
    }

    @Override
    public void setCachedData(IArray cacheData, boolean isMetadata)
            throws InvalidArrayTypeException {
        for( IDataItem item : mDataItems ) {
            item.setCachedData(cacheData, isMetadata);
        }
    }

    @Override
    public void setCaching(boolean caching) {
        for( IDataItem item : mDataItems ) {
            item.setCaching(caching);
        }
    }

    @Override
    public void setDataType(Class<?> dataType) {
        for( IDataItem item : mDataItems ) {
            item.setDataType(dataType);
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
                    setDimension(new NexusDimension(NxsFactory.NAME, item), attr.getNumericValue().intValue() );
                }
                else if( dimNames.contains(attr.getName()) ) {
                    setDimension(new NexusDimension(NxsFactory.NAME, item), attr.getNumericValue().intValue() );
                }
            }
        }
    }

    @Override
    public void setDimension(IDimension dim, int ind) {
        mDimension.add( new DimOrder(ind, dim) );
    }

    @Override
    public void setElementSize(int elementSize) {
        for( IDataItem item : mDataItems ) {
            item.setElementSize(elementSize);
        }
    }

    @Override
    public void setName(String name) {
        for( IDataItem item : mDataItems ) {
            item.setName(name);
        }
    }

    @Override
    public void setParent(IGroup group) {
        if( mParent == null || ! mParent.equals(group) ) {
            mParent = group;
            group.addDataItem(this);
        }
    }

    @Override
    public void setSizeToCache(int sizeToCache) {
        for( IDataItem item : mDataItems ) {
            item.setSizeToCache(sizeToCache);
        }
    }

    @Override
    public String toStringDebug() {
        StringBuffer strDebug = new StringBuffer();
        strDebug.append( getName() );
        if( strDebug.length() > 0) {
            strDebug.append("\n");
        }
        try {
            strDebug.append( "shape: " + getData().shapeToString() + "\n" );
        } catch( IOException e ) {
        }
        List<IDimension> dimensions = getDimensionList();
        for( IDimension dim : dimensions ) {
            strDebug.append( dim.getCoordinateVariable().toString() );
        }

        List<IAttribute> list = getAttributeList();
        if( list.size() > 0 ) {
            strDebug.append( "\nAttributes:\n" );
        }
        for( IAttribute a : list ) {
            strDebug.append( "- " + a.toString() + "\n" );
        }

        return strDebug.toString();
    }

    @Override
    public String writeCDL(String indent, boolean useFullName, boolean strict) {
        // TODO Auto-generated method stub
        new BackupException(NxsFactory.ERR_NOT_SUPPORTED).printStackTrace();
        return null;
    }

    @Override
    public void setUnitsString(String units) {
        // TODO Auto-generated method stub
        new BackupException(NxsFactory.ERR_NOT_SUPPORTED).printStackTrace();
    }

    @Override
    public IDataset getDataset() {
        return mDataset;
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
        for( IDataItem item : mDataItems ) {
            item.setShortName(name);
        }
    }

    @Override
    public String getFactoryName() {
        return NxsFactory.NAME;
    }

    // specific methods
    public DataItem[] getNexusItems() {
        DataItem[] result = new DataItem[ mDataItems.length ];
        int i = 0;
        for( NexusDataItem item : mDataItems ) {
            result[i] = item.getN4TDataItem();
            i++;
        }
        return result;
    }
}
