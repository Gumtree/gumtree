package org.gumtree.data.soleil.navigation;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.gumtree.data.Factory;
import org.gumtree.data.dictionary.IExtendedDictionary;
import org.gumtree.data.dictionary.ILogicalGroup;
import org.gumtree.data.dictionary.impl.LogicalGroup;
import org.gumtree.data.engine.nexus.navigation.NexusDataset;
import org.gumtree.data.engine.nexus.navigation.NexusGroup;
import org.gumtree.data.exception.WriterException;
import org.gumtree.data.exception.NoResultException;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IContainer;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.soleil.NxsDatasource.NeXusFilter;
import org.gumtree.data.soleil.NxsFactory;
import org.gumtree.data.soleil.dictionary.NxsLogicalGroup;
import org.gumtree.data.util.configuration.ConfigDataset;
import org.gumtree.data.util.configuration.ConfigManager;

import fr.soleil.nexus.NexusFileWriter;

public final class NxsDataset implements IDataset {
    /*
     Inner class that concretes the abstract NexusDataset 
     */
    private class NexusDatasetImpl extends NexusDataset {
        public NexusDatasetImpl(NexusDatasetImpl dataset) {
            super(dataset);
        }

        public NexusDatasetImpl( File nexusFile ) {
            super( NxsFactory.NAME, nexusFile );
        }

        @Override
        public ILogicalGroup getLogicalRoot() {
            return new LogicalGroup(null, null, this, false);
        }
    }

    private ConfigDataset      mConfig;
    private List<NexusDataset> mDatasets;     // all found datasets in the folder
    private URI                mPath;         // folder containing all datasets
    private IGroup             mRootPhysical; // Physical root of the document 
    private ILogicalGroup      mRootLogical;  // Logical root of the document
    private boolean            mOpen;         // is the dataset open 

    public static NxsDataset instanciate(File destination) throws IOException, NoResultException
    {
        NxsDataset dataset = new NxsDataset(destination);
        dataset.open();
        ConfigDataset conf = ConfigManager.getInstance( NxsFactory.getInstance(), NxsFactory.CONFIG_FILE ).getConfig(dataset);
        dataset.setConfig(conf);
        dataset.close();
        return dataset;
    }

    @Override
    public String getFactoryName() {
        return NxsFactory.NAME;
    }

    @Override
    public ILogicalGroup getLogicalRoot() {
        if( mRootLogical == null ) {
            boolean debug = false;
            if( null != System.getProperty(NxsFactory.DEBUG_INF, System.getenv(NxsFactory.DEBUG_INF)) )
            {
                debug = true;
            }
            mRootLogical = new NxsLogicalGroup(null, null, this, debug);

        }
        else {
            IExtendedDictionary dict = mRootLogical.getDictionary();
            if ( dict != null && ! dict.getView().equals( Factory.getActiveView() ) ) {
                mRootLogical.setDictionary(mRootLogical.findAndReadDictionary());
            }
        }
        return mRootLogical;
    }

    @Override
    public IGroup getRootGroup() {
        if( mRootPhysical == null && mDatasets.size() > 0 ) {
            NexusGroup[] groups = new NexusGroup[mDatasets.size()];
            int i = 0;
            for( IDataset dataset : mDatasets ) {
                groups[i++] = (NexusGroup) dataset.getRootGroup();
            }
            mRootPhysical = new NxsGroup(groups, null, this);
        }
        return mRootPhysical;
    }


    @Override
    public void saveTo(String location) throws WriterException {
        for( IDataset dataset : mDatasets ) {
            dataset.saveTo(location);
        }
    }

    @Override
    public void save(IContainer container) throws WriterException {
        for( IDataset dataset : mDatasets ) {
            dataset.save(container);
        }
    }

    @Override
    public void save(String parentPath, IAttribute attribute)
            throws WriterException {
        for( IDataset dataset : mDatasets ) {
            dataset.save(parentPath, attribute);
        }
    }

    @Override
    public boolean sync() throws IOException {
        boolean result = true;
        for( IDataset dataset : mDatasets ) {
            if( ! dataset.sync() ) {
                result = false;
            }
        }
        return result;
    }

    @Override
    public void writeNcML(OutputStream os, String uri) throws IOException {
        for( IDataset dataset : mDatasets ) {
            dataset.writeNcML(os, uri);
        }
    }

    @Override
    public void close() throws IOException {
        /*
   for( IDataset dataset : mDatasets ) {
      dataset.close();
    }
         */    mOpen = false;
    }

    @Override
    public String getLocation() {
        return mPath.getPath();
    }

    @Override
    public String getTitle() {
        String title = "";
        try
        {
            title = mDatasets.get(0).getTitle();
        }
        catch( NoSuchElementException e ) {}

        return title;
    }

    @Override
    public void setLocation(String location) {
        File newLoc = new File( location );
        File oldLoc = new File( mPath );
        if( ! oldLoc.equals(newLoc) ) {
            try {
                mPath = new URI(newLoc.getAbsolutePath());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            mDatasets.clear();
        }
    }

    @Override
    public void setTitle(String title) {
        try
        {
            mDatasets.get(0).setTitle(title);
        }
        catch( NoSuchElementException e ) {}
    }

    @Override
    public void open() throws IOException {
        /*
   for( IDataset dataset : mDatasets ) {
      dataset.open();
    }
    mOpen = true;
         */
    }

    @Override
    public void save() throws WriterException {
        for( IDataset dataset : mDatasets ) {
            dataset.save();
        }
    }

    @Override
    public boolean isOpen() {
        return mOpen;
    }

    public ConfigDataset getConfiguration() {
        return mConfig;
    }

    // ---------------------------------------------------------
    /// Private methods
    // ---------------------------------------------------------
    private NxsDataset(File destination) {
        mPath = destination.toURI();
        mDatasets = new ArrayList<NexusDataset>();
        if( destination.exists() && destination.isDirectory() ) {
            IDataset datafile;
            NeXusFilter filter = new NeXusFilter();
            for( File file : destination.listFiles(filter) ) {
                datafile = new NexusDatasetImpl(file);
                mDatasets.add( (NexusDatasetImpl) datafile );
            }
        }
        else {
            mDatasets.add( new NexusDatasetImpl(destination ) );
        }
        mOpen = false;
    }

    private void setConfig(ConfigDataset conf) {
        mConfig = conf;
    }
}
