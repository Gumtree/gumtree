package org.gumtree.data.soleil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URI;

import org.gumtree.data.IDatasource;
import org.gumtree.data.IFactory;
import org.gumtree.data.dictionary.ILogicalGroup;
import org.gumtree.data.dictionary.IPath;
import org.gumtree.data.dictionary.IPathParamResolver;
import org.gumtree.data.dictionary.IPathParameter;
import org.gumtree.data.dictionary.impl.Key;
import org.gumtree.data.dictionary.impl.Path;
import org.gumtree.data.dictionary.impl.PathParameter;
import org.gumtree.data.engine.nexus.navigation.NexusAttribute;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IDictionary;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IKey;
import org.gumtree.data.math.IArrayMath;
import org.gumtree.data.soleil.array.NxsArray;
import org.gumtree.data.soleil.dictionary.NxsLogicalGroup;
import org.gumtree.data.soleil.dictionary.NxsPathParamResolver;
import org.gumtree.data.soleil.navigation.NxsDataset;
import org.gumtree.data.soleil.navigation.NxsGroup;
import org.gumtree.data.soleil.utils.NxsArrayMath;
import org.gumtree.data.soleil.utils.NxsArrayUtils;
import org.gumtree.data.utils.IArrayUtils;
import org.gumtree.data.utils.Utilities.ParameterType;

import fr.soleil.nexus.DataItem;
import fr.soleil.nexus.PathGroup;
import fr.soleil.nexus.PathNexus;

public final class NxsFactory implements IFactory {
    private static NxsFactory factory;
    private static NxsDatasource detector;
    public static final String NAME        = "org.gumtree.data.soleil.NxsFactory";
    public static final String LABEL       = "SOLEIL's NeXus plug-in";
    public static final String DEBUG_INF   = "CDMA_DEBUG_NXS";
    public static final String CONFIG_FILE = "cdma_nexussoleil_config.xml";

    public static final String ERR_NOT_SUPPORTED = "not supported yet in plug-in!";

    public NxsFactory() {
    }

    public static NxsFactory getInstance() {
        synchronized (NxsFactory.class ) {
            if( factory == null ) {
                factory  = new NxsFactory();
                detector = new NxsDatasource();
            }
        }
        return factory;
    }

    @Override
    public IArray createArray(Class<?> clazz, int[] shape) {
        Object o = java.lang.reflect.Array.newInstance(clazz, shape);
        return new NxsArray( o, shape);
    }

    @Override
    public IArray createArray(Class<?> clazz, int[] shape, Object storage) {
        IArray result = null;
        if( storage instanceof IArray[] ) {
            result = new NxsArray( (IArray[]) storage );
        }
        else if( storage instanceof DataItem ) {
            result = new NxsArray( (DataItem) storage );
        }
        else {
            result = new NxsArray( storage, shape);
        }
        return result;
    }

    @Override
    public IArray createArray(Object javaArray) {
        IArray result = null;
        // [ANSTO][Tony][2011-08-31] testing isArray may be slow
        // [SOLEIL][clement][2012-04-18] as the supported array is a primitive type the "instanceof" won't be correct 
        if (javaArray != null && javaArray.getClass().isArray()) {
            int size = Array.getLength(javaArray);
            result = new NxsArray(javaArray, new int[] { size });
        }
        return result;
    }

    @Override
    public IArray createArrayNoCopy(Object array) {
        IArray result = null;
        if( array instanceof IArray[] ) {
            result = new NxsArray( (IArray[]) array);
        }
        else if( array instanceof DataItem ) {
            result = new NxsArray( (DataItem) array);
        }
        else {
            DataItem dataset = null;
            try {
                dataset = new DataItem(array);
                result = new NxsArray(dataset);
            } catch( Exception e ) {
                result = null;
            }

        }
        return result;
    }

    @Override
    public IAttribute createAttribute(String name, Object value) {
        return new NexusAttribute(NAME ,name, value);
    }

    @Override
    public IDataItem createDataItem(IGroup parent, String shortName, IArray array) throws InvalidArrayTypeException {
        throw new InvalidArrayTypeException(ERR_NOT_SUPPORTED);
    }

    @Override
    public IDataset createDatasetInstance(URI uri) throws Exception {
        return NxsDataset.instanciate(new File(uri.getPath()));
    }

    @Override
    public IArray createDoubleArray(double[] javaArray) {
        DataItem data;
        try {
            data = new DataItem(javaArray);
        } catch( Exception e ) {
            data = null;
        }
        return new NxsArray(data);
    }

    @Override
    public IArray createDoubleArray(double[] javaArray, int[] shape) {
        DataItem data;
        try {
            data = new DataItem(javaArray);
        } catch( Exception e ) {
            data = null;
        }
        return new NxsArray(data);
    }

    @Override
    public IDataset createEmptyDatasetInstance() throws IOException {
        // TODO Auto-generated method stub
        throw new IOException(ERR_NOT_SUPPORTED);
    }

    @Override
    public IGroup createGroup(IGroup parent, String shortName, boolean updateParent) {
        String path_val = parent.getLocation();
        PathGroup path = new PathGroup(PathNexus.splitStringPath(path_val));
        NxsGroup group = new NxsGroup( parent, (PathNexus) path, (NxsDataset) parent.getDataset());

        return group;
    }

    @Override
    public IGroup createGroup(String shortName) throws IOException {
        // TODO Auto-generated method stub
        throw new IOException(ERR_NOT_SUPPORTED);
    }

    @Override
    public IArray createStringArray(String string) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getPluginLabel() {
        return LABEL;
    }

    @Override
    public IDatasource getPluginURIDetector() {
        synchronized (NxsDatasource.class ) {
            if( detector == null ) {
                detector = new NxsDatasource();
            }
        }
        return detector;
    }

    @Override
    public IDataset openDataset(URI uri) throws FileAccessException {
        // TODO Auto-generated method stub
        throw new FileAccessException(ERR_NOT_SUPPORTED);
    }

    @Override
    public IKey createKey(String keyName) {
        return new Key(this, keyName);
    }

    @Override
    public IPathParameter createPathParameter(ParameterType type, String name, Object value) {
        return new PathParameter(this, type, name, value);
    }

    @Override
    public ILogicalGroup createLogicalGroup(IDataset dataset, IKey key) {
        return new NxsLogicalGroup(dataset, key);
    }

    @Override
    public IPath createPath(String path) {
        return new Path(this, path);
    }

    @Override
    public String getPathSeparator() {
        return "/";
    }

    @Override
    public IPathParamResolver createPathParamResolver(IPath path) {
        IPathParamResolver result;
        if( path instanceof Path ) {
            result = new NxsPathParamResolver(this, (Path) path);
        }
        else {
            result = new NxsPathParamResolver( this, new Path(this, path.getValue() ) );
        }

        return result;
    }

    @Override
    public IDictionary openDictionary(URI uri) throws FileAccessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public IDictionary openDictionary(String filepath)
            throws FileAccessException {
        throw new UnsupportedOperationException();
    }

    @Override
    public IDictionary createDictionary() {
        throw new UnsupportedOperationException();
    }

    public static IArrayUtils createArrayUtils(NxsArray array) {
        return new NxsArrayUtils(array);
    }

    public static IArrayMath createArrayMath(NxsArray array) {
        return new NxsArrayMath(array);
    }
}
