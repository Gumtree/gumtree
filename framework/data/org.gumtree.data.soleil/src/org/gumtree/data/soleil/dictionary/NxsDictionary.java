package org.gumtree.data.soleil.dictionary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gumtree.data.IFactory;
import org.gumtree.data.dictionary.IPath;
import org.gumtree.data.dictionary.impl.Key;
import org.gumtree.data.dictionary.impl.Path;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.interfaces.IDictionary;
import org.gumtree.data.interfaces.IKey;
import org.gumtree.data.soleil.NxsFactory;
import org.gumtree.data.soleil.navigation.NxsDataset;
import org.gumtree.data.util.configuration.ConfigDataset;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


/**
 * @note This class is just a test and is not representative of how the real implementation should work.
 * Behaviors and algorithms of this class do not apply to the CDMA dictionary's behaviour!
 * @author rodriguez
 *
 */
public final class NxsDictionary implements IDictionary, Cloneable {
    private String mPath;              // Path of the XML file carrying the dictionary 
    private Map<IKey, IPath> mItemMap; // Map associating keys from view file to path from mapping file

    public NxsDictionary() {
        mItemMap = new HashMap<IKey, IPath>();
    }

    @Override
    public void addEntry(String keyName, String path)
    {
        IFactory factory = NxsFactory.getInstance();
        mItemMap.put(new Key(factory, keyName), new Path(factory, path));
    }

    @Override
    public boolean containsKey(String keyName)
    {
        return mItemMap.containsKey(keyName);
    }

    @Override
    public List<IKey> getAllKeys()
    {
        return new ArrayList<IKey>(mItemMap.keySet());
    }

    @Override
    public List<IPath> getAllPaths(IKey keyName)
    {
        return new ArrayList<IPath>(mItemMap.values());
    }

    @Override
    public IPath getPath(IKey keyName)
    {
        if( mItemMap.containsKey(keyName) )
        {
            return mItemMap.get(keyName);
        }
        else
        {
            return null;
        }
    }

    @Override
    public void readEntries(URI uri) throws FileAccessException
    {
        File dicFile = new File(uri);
        if (!dicFile.exists()) 
        {
            throw new FileAccessException("the target dictionary file does not exist");
        }
        try 
        {
            BufferedReader br = new BufferedReader(new FileReader(dicFile));
            while (br.ready())
            {
                String line = br.readLine();
                if( line != null ) {
                    String[] temp = line.split("=");
                    if (0 < (temp[0].length())) 
                    {
                        addEntry(temp[0], temp[1]);
                    }
                }
            }
            br.close();
        } 
        catch (IOException ex) 
        {
            throw new FileAccessException("failed to open the dictionary file\n", ex);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readEntries(String filePath) throws FileAccessException
    {
        File dicFile = new File(filePath);
        if (!dicFile.exists()) 
        {
            throw new FileAccessException("the target dictionary file does not exist");
        }

        // Parse the XML dictionary
        SAXBuilder xmlFile = new SAXBuilder();
        Element root;
        Document dictionary;
        try {
            dictionary = xmlFile.build(dicFile);
        }
        catch (JDOMException e1) {
            throw new FileAccessException("error while to parsing the dictionary!\n", e1);
        }
        catch (IOException e1) {
            throw new FileAccessException("an I/O error prevent parsing dictionary!\n", e1);
        }

        mPath = dicFile.getAbsolutePath();
        root = dictionary.getRootElement();

        List<?> nodes = root.getChildren("item"), tmpList;

        String key;
        String path = "";
        for( Element node : (List<Element>) nodes ) {
            key = node.getAttributeValue("key");
            path = node.getChildText("path");
            if( key != null && !key.isEmpty() && path != null && ! path.isEmpty() ) {
                addEntry(key, path);
            }
        }
    }

    @Override
    public void removeEntry(String keyName, String path) {
        mItemMap.remove(keyName);
    }

    @Override
    public void removeEntry(String keyName) {
        mItemMap.remove(keyName);
    }


    @SuppressWarnings("unchecked")
    @Override
    public IDictionary clone() throws CloneNotSupportedException
    {
        NxsDictionary dict = new NxsDictionary();
        dict.mItemMap = (HashMap<IKey, IPath>) ((HashMap<IKey, IPath>) mItemMap).clone();
        return dict;
    }

    /**
     * @return path of the dictionary file
     */
    public String getPath() {
        return mPath;
    }

    @Override
    public void addEntry(String key, IPath path) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getFactoryName() {
        return NxsFactory.NAME;
    }

    /**
     * According to the current corresponding dataset, this method will try
     * to guess which XML dictionary mapping file should be used
     * @return
     * @throws FileAccessException 
     */
    public static String detectDictionaryFile(NxsDataset dataset) throws FileAccessException {
        // Get the configuration
        ConfigDataset conf = dataset.getConfiguration();

        // Ask for beamline and datamodel parameters
        String beamline = conf.getParameter("BEAMLINE", dataset);
        String model = conf.getParameter("MODEL", dataset);

        // Construct the dictionary file name
        if( beamline != null ) {
            beamline = beamline.toLowerCase();
        }
        else {
            beamline = "UNKNOWN";
        }
        if( model != null ) {
            model = model.toLowerCase();
        }
        else {
            model = "UNKNOWN";
        }
        return beamline + "_" + model + ".xml";
    }
}
