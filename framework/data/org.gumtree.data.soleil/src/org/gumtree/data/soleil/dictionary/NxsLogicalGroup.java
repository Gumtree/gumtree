package org.gumtree.data.soleil.dictionary;

import org.gumtree.data.Factory;
import org.gumtree.data.IFactory;
import org.gumtree.data.dictionary.IExtendedDictionary;
import org.gumtree.data.dictionary.ILogicalGroup;
import org.gumtree.data.dictionary.impl.ExtendedDictionary;
import org.gumtree.data.dictionary.impl.LogicalGroup;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IKey;
import org.gumtree.data.soleil.NxsFactory;
import org.gumtree.data.soleil.navigation.NxsDataset;

public class NxsLogicalGroup extends LogicalGroup {

    public NxsLogicalGroup(IDataset dataset, IKey key) {
        super(key, dataset);
    }

    public NxsLogicalGroup(IDataset dataset, IKey key, boolean debug) {
        super(key, dataset, debug);
    }

    public NxsLogicalGroup(ILogicalGroup parent, IKey key, IDataset dataset) {
        super(parent, key, dataset, false);
    }

    public NxsLogicalGroup(ILogicalGroup parent, IKey key, IDataset dataset, boolean debug) {
        super(parent, key, dataset, debug);
    }

    public IExtendedDictionary findAndReadDictionary() {
        IFactory factory = NxsFactory.getInstance();
        IExtendedDictionary dictionary;
        // Detect the key dictionary file and mapping dictionary file
        String keyFile = "";
        String mapFile = "";
        try {

            keyFile = Factory.getKeyDictionaryPath();
            mapFile = Factory.getMappingDictionaryFolder( factory ) + NxsDictionary.detectDictionaryFile( (NxsDataset) getDataset() );
            dictionary = new ExtendedDictionary( NxsFactory.getInstance(), keyFile, mapFile );
            dictionary.readEntries();
        } catch (FileAccessException e) {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<\n"+keyFile +"\n" + mapFile + ">>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<\n");
            e.printStackTrace();
            dictionary = null;
        }

        return dictionary;
    }
}
