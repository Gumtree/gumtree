package fr.soleil.nexus;

// Nexus Lib
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;

public class DataItem implements Cloneable
{
    public class Data<T>
    {
	private T      m_tValue;
	private Class<T>  m_cType;

	@SuppressWarnings("unchecked")
	protected     Data(T tValue)  { m_tValue = tValue; m_cType = (Class<T>) tValue.getClass(); }
	public T      getValue()    { return m_tValue; }
	public Class<T> getType()    { return m_cType; }
    }

    /// Members
    private int       mType;      // Data type (see NexusFile class)
    private int[]     mDimSize;   // DataItem (node) dimension's sizes
    private int[]     mDimData;   // Data slab dimension's sizes
    private int[]     mStart;     // Data slab start position
    private int[]     mPrevStart; // Previously loaded origin
    private int[]     mPrevShape; // Previously loaded shape
    private Object    mData;      // Data it's an array of values belonging to a DataItem
    private String    mNodeName;  // DataItem 's node name
    private boolean   mSingleRaw; // Is the data stored in memory a single raw
    private PathData  mPath;      // Path were the DataItem has been READ (not used when writing)
    private int     mPrevSlabStart;
    private int     mPrevSlabLength;
    private SoftReference<Object>  mPrevSlab;

    private HashMap<String, Data<?> >  mAttribs;  // Map containing all node's attributes having name as key associated to value


    // Constructors
    public DataItem() { 
	mSingleRaw = true;
	mAttribs   = new HashMap<String, Data<?>>(); 
	mPath      = null; 
	mData      = null; 
	mDimData   = null;
	mDimSize   = null;
	mStart     = null;
    }

    public DataItem(Object oArray) throws NexusException  { 
	mAttribs = new HashMap<String, Data<?>>(); 
	initFromData(oArray); 
	mPath = null;

    }

    @SuppressWarnings("unchecked")
    public DataItem clone() throws CloneNotSupportedException {
	DataItem result = new DataItem();

	if(mStart != null ) {
	    result.mStart = this.mStart.clone();
	}
	result.mSingleRaw = this.mSingleRaw;
	result.mDimData   = this.mDimData.clone();
	result.mDimSize   = this.mDimData.clone();
	result.mType      = this.mType;
	result.mAttribs   = (HashMap<String, Data<?>>) this.mAttribs.clone();
	result.mPath      = this.mPath;
	result.mNodeName  = this.mNodeName;
	result.mData      = this.mData;
	result.mPrevStart = this.mPrevStart; 
	result.mPrevShape = this.mPrevShape;

	return result;
    }

    // Accessors
    public int      getType()     { return mType; }
    public int[]    getSize()     { return mDimSize; }
    public int[]    getSlabSize() { return mDimData; }
    public int[]    getStart()    { return mStart; }
    public PathData getPath()     { return mPath; }

    public void setType(int iType)        { mType = iType; }
    public void setSize(int[] iDimS)      { mDimSize = iDimS.clone(); }
    public void setStart(int[] iStart)    { mStart = iStart.clone(); }
    public void setSlabSize(int[] iSlab)  { mDimData = iSlab.clone(); }
    public void setData(Object oData)     { mData = oData; }
    public void setPath(PathNexus pnPath) { if( pnPath instanceof PathData ) { mPath = (PathData) pnPath; } else { mPath = PathData.Convert(pnPath); } }

    // Attribute accessors
    public String  getUnit()     { Object attrValue = getAttribute("units");       return attrValue == null ? null : attrValue.toString(); }
    public String  getDesc()     { Object attrValue = getAttribute("description"); return attrValue == null ? null : attrValue.toString(); }
    public String  getTime()     { Object attrValue = getAttribute("timestamp");   return attrValue == null ? null : attrValue.toString(); }
    public String  getFormat()   { Object attrValue = getAttribute("format");      return attrValue == null ? null : attrValue.toString(); }
    public String  getName()     { Object attrValue = getAttribute("name");        return attrValue == null ? null : attrValue.toString(); }
    public String  getNodeName() { return mNodeName; }

    public <T> void  setUnit(T sUnit)              { setAttribute("units", sUnit); }
    public <T> void  setDesc(T sDesc)              { setAttribute("description", sDesc); }
    public <T> void  setTime(T sTime)              { setAttribute("timestamp", sTime); }
    public <T> void  setFormat(T sForm)            { setAttribute("format", sForm); }
    public <T> void  setName(T sName)              { setAttribute("name", sName); }
    public     void  setNodeName(String sNodeName) { mNodeName = sNodeName; }

    // Data accessor
    public Object getData()
    {
	return getData(mStart, mDimData);
    }

    // Generic attribute accessors
    public HashMap<String, Data<?>> getAttributes() { return mAttribs; }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String sAttrName)
    {
	if( mAttribs == null || !mAttribs.containsKey(sAttrName))
	{
	    return null;
	}
	Data<T> tVal = (Data<T>) mAttribs.get(sAttrName);
	return tVal.getType().cast(tVal.getValue());
    }

    public <T> void setAttribute(String sAttrName, T oValue)
    {
	if( mAttribs == null )
	{
	    mAttribs = new HashMap<String, Data<?>>();
	}

	Data<T> tValue = new Data<T>(oValue);
	mAttribs.put(sAttrName, tValue);
    }

    @Override
    public void finalize() { mData = null; mDimSize = null; mDimData = null; mNodeName = null; mAttribs = null; }

    // ---------------------------------------------------------
    /// Public methods
    // ---------------------------------------------------------
    /**
     * InitFromObject
     * Initialization an empty DataItem with datas and infos guessed from a data object. i.e: type, rank, dimsize...
     *
     * @param oData datas permitting detection of type
     */
    public void initFromData(Object oData) throws NexusException
    {
	Class<?> cDataClass  = oData.getClass();

	// Manage particular case of string
	if(oData instanceof String)
	{
	    mType = NexusFile.NX_CHAR;
	    mDimSize = new int[] {((String) oData).length()};
	    mData = new SoftReference<Object>(oData);
	    mDimData = new int[] {((String) oData).length()};
	    mStart = new int[] {0};
	    mPrevShape = mDimData;
	    mPrevStart = mStart;
	}
	else if( oData instanceof PathNexus )
	{
	    mType = NexusFile.NX_BINARY;   
	    mDimSize = new int[] {0};
	    mData = new SoftReference<Object>(oData);
	    mDimData = new int[] {0};
	    mStart = new int[] {0};
	    mPrevShape = mDimData;
	    mPrevStart = mStart;
	    return;
	}
	// Try to determine type of data (primitive only) and its dimension size
	else
	{
	    //  If data is not array try to arrify it
	    if( !oData.getClass().isArray() && !(oData instanceof String) )
	    {
		Object oObj = java.lang.reflect.Array.newInstance(oData.getClass(), 1);
		java.lang.reflect.Array.set(oObj, 0, oData);
		oData = oObj;
		cDataClass  = oData.getClass();
	    }

	    // Check if data is array
	    if( !cDataClass.isArray() )
	    {
		throw new NexusException("Only strings and arrays are allowed to be manipulated!");
	    }
	    mData = new SoftReference<Object>(oData);
	    mType = NexusFileReader.defineNexusFromType(oData);
	    initDimSize();
	}
    }

    /**
     * toString
     * Give a string representation of the DataItem with its principal infos
     */
    @Override
    public String toString()
    {
	StringBuffer str = new StringBuffer();
	str.append("     - Node name: " + mNodeName + "   Type: "+ mType + "\n     - Node Size: [");
	if( mDimSize != null ) {
	    for(int i=0; i< mDimSize.length;i++)
	    {
		str.append(String.valueOf(mDimSize[i])+ (( i<mDimSize.length-1)? ", " : " "));
	    }
	}
	if( mDimData != null ) {
	    str.append("]   Slab Size: [");
	    for(int i=0; i< mDimData.length;i++)
	    {
		str.append(String.valueOf(mDimData[i])+ (( i<mDimData.length-1)? ", " : " "));
	    }   
	}
	if(mStart != null ) {
	    str.append("]   Slab Pos: [");
	    for(int i=0; i< mStart.length;i++)
	    {
		str.append(String.valueOf(mStart[i])+ (( i<mStart.length-1)? ", " : " "));
	    }
	}
	str.append("]");

	String sAttrName;
	if( mAttribs != null )
	{
	    for(Iterator<String> iter = mAttribs.keySet().iterator() ; iter.hasNext() ; )
	    {
		sAttrName = (String) iter.next();
		str.append("\n     - " + sAttrName + ": " + getAttribute(sAttrName));
	    }
	}
	str.append("\n     - Node Path: " + mPath);
	Object data = getData();
	if( mType == NexusFile.NX_CHAR && null != data ) {
	    str.append("\n     - Value: "+ data);
	}
	return str.toString();
    }

    public void arrayify()
    {
	Object data = getRawData();
	if( data != null && ! data.getClass().isArray() && !(data instanceof String) )
	{
	    Object oTmp = java.lang.reflect.Array.newInstance(data.getClass(), 1);
	    java.lang.reflect.Array.set(oTmp, 0, data);
	    mData = new SoftReference<Object>(oTmp);
	}
    }

    /**
     * getDataClass
     * return the primitive type of the owned data according to the NexusFile.Type
     */
    public Class<?> getDataClass()
    {
	switch( mType )
	{
	case NexusFile.NX_UINT32:
	case NexusFile.NX_INT32 :
	    return Integer.TYPE;
	case NexusFile.NX_UINT16:
	case NexusFile.NX_INT16 :
	    return Short.TYPE;
	case NexusFile.NX_FLOAT32 :
	    return Float.TYPE;
	case NexusFile.NX_FLOAT64 :
	    return Double.TYPE;
	case NexusFile.NX_UINT64:
	case NexusFile.NX_INT64 :
	    return Long.TYPE;
	case NexusFile.NX_CHAR :
	    return String.class;
	case NexusFile.NX_BOOLEAN :
	    return Boolean.TYPE;
	case NexusFile.NX_BINARY :
	default:
	    return Byte.TYPE;

	}
    }

    public boolean isSingleRawArray()
    {
	return mSingleRaw;
    }

    @SuppressWarnings("unchecked")
    public Object getData(int[] pos, int[] shape) {
	// save current position and shape
	boolean reload = !alreadyLoaded(pos, shape) && mType != NexusFile.NX_CHAR;
	Object data = getRawData();
	if( reload || data == null ) {
	    mStart = pos;
	    mDimData = shape;
	    loadData();
	    data = ((SoftReference<Object>) mData).get();
	    mPrevSlabStart = -1; 
	    mPrevSlabLength = -1;
	    mPrevSlab = null;
	}
	boolean slabData = !(java.util.Arrays.equals(shape, mDimData) && java.util.Arrays.equals(mStart, pos));
	if( slabData ) {
	    int start  = 0;
	    int length = 1;
	    int stride = 1;
	    for( int i = mDimData.length - 1; i >= 0 ; i-- ) {
		start  += (pos[i] - mStart[i]) * stride;
		length *= shape[i];
		stride *= shape[i];
	    }
	    if( mPrevSlabStart != start || mPrevSlabLength != length || mPrevSlab.get() == null ) {
		data = copy( data, start, length);
		mPrevSlabStart = start; 
		mPrevSlabLength = length;
		mPrevSlab = new SoftReference<Object>(data);
	    }
	    else {
		data = mPrevSlab.get();
	    }
	}

	// restore previous position and shape
	return data;
    }

    // ---------------------------------------------------------
    /// Protecte methods
    // ---------------------------------------------------------
    protected void isSingleRawArray(boolean bSingleRaw)
    {
	mSingleRaw = bSingleRaw;
    }

    // ---------------------------------------------------------
    /// Private methods
    // ---------------------------------------------------------
    /**
     * InitDimSize
     * Initialize member dimension sizes 'm_iDimS' according to defined member data 'm_oData'
     */
    private void initDimSize() throws NexusException
    {
	Object oArray = getRawData();
	// Check data existence
	if( oArray == null )
	    throw new NexusException("No data to determine Nexus data dimension sizes!");

	// Determine rank of array (by parsing data array class name)
	String sClassName = oArray.getClass().getName();
	int iRank  = 0;
	int iIndex = 0;
	char cChar;
	while (iIndex < sClassName.length()) {
	    cChar = sClassName.charAt(iIndex);
	    iIndex++;
	    if (cChar == '[') {
		iRank++;
	    }
	}

	// Set dimension rank
	mDimData = new int[iRank];
	mDimSize = new int[iRank];
	mStart   = new int[iRank];

	// Fill dimension size array
	for ( int i = 0; i < iRank; i++) {
	    mDimSize[i] = java.lang.reflect.Array.getLength(oArray);
	    mDimData[i] = mDimSize[i];
	    mStart[i] = 0;
	    oArray = java.lang.reflect.Array.get(oArray,0);
	}
	mPrevShape = mDimData;
	mPrevStart = mStart;
    }

    private void loadData()
    {
	NexusFileReader nfrFile = new NexusFileReader(mPath.getFilePath());
	nfrFile.isSingleRawResult(mSingleRaw);
	try
	{
	    nfrFile.openFile(NexusFile.NXACC_READ);
	    if( mStart == null) {
		mData = new SoftReference<Object>(nfrFile.readData(mPath).getRawData());
		mPrevStart = new int[mDimSize.length];
		mPrevShape = mDimSize;
	    } else {
		mData = new SoftReference<Object>(nfrFile.readDataSlab(mPath, mStart, mDimData).getRawData());
		mPrevStart = mStart;
		mPrevShape = mDimData;
	    }

	    nfrFile.closeFile();
	}
	catch(NexusException ne)
	{
	    mData = new SoftReference<Object>(null);
	    try {
		nfrFile.closeFile();
	    } catch (NexusException e) {
		e.printStackTrace();
	    }
	}
    }

    private boolean alreadyLoaded(int[] pos, int[] shape) {
	boolean result = ( mPrevStart != null );

	if( result ) {
	    for( int i = 0; i < mDimData.length; i++ ) {
		if( mPrevStart[i] > pos[i] ) {
		    result = false;
		    break;
		}
		if( (pos[i] + shape[i]) > mPrevShape[i] + mPrevStart[i] ) {
		    result = false;
		    break;
		}
	    }
	}      
	return result;
    }

    @SuppressWarnings("unchecked")
    private Object getRawData()
    {
	Object result = null;
	if( mData != null )
	{
	    if( mData instanceof SoftReference )
	    {
		result = ((SoftReference<Object>) mData).get();
	    }
	    else
	    {
		result = mData;
	    }
	}

	return result;
    }

    private Object copy( Object data, int start, int length ) {
	Object result = null;
	switch( mType )
	{
	case NexusFile.NX_UINT32:
	case NexusFile.NX_INT32 :
	    result = Arrays.copyOfRange( (int[]) data, start, start + length );
	    break;
	case NexusFile.NX_UINT16:
	case NexusFile.NX_INT16 :
	    result = Arrays.copyOfRange( (short[]) data, start, start + length );
	    break;
	case NexusFile.NX_FLOAT32 :
	    result = Arrays.copyOfRange( (float[]) data, start, start + length );
	    break;
	case NexusFile.NX_FLOAT64 :
	    result = Arrays.copyOfRange( (double[]) data, start, start + length );
	    break;
	case NexusFile.NX_UINT64:
	case NexusFile.NX_INT64 :
	    result = Arrays.copyOfRange( (long[]) data, start, start + length );
	    break;
	case NexusFile.NX_CHAR :
	    result = Arrays.copyOfRange( (String[]) data, start, start + length );
	    break;
	case NexusFile.NX_BOOLEAN :
	    result = Arrays.copyOfRange( (boolean[]) data, start, start + length );
	    break;
	case NexusFile.NX_BINARY :
	default:
	    result = Arrays.copyOfRange( (byte[]) data, start, start + length );
	    break;

	}
	return result;
    }
}
