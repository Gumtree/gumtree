/*******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation,
 * Synchrotron SOLEIL and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     Norman XIONG (Bragg Institute) - initial API and implementation
 *     Clément RODRIGUEZ (SOLEIL) - initial API and implementation
 *     Tony LAM (Bragg Institute) - implementation
 ******************************************************************************/

package org.gumtree.data.dictionary.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gumtree.data.Factory;
import org.gumtree.data.IFactory;
import org.gumtree.data.dictionary.IExtendedDictionary;
import org.gumtree.data.dictionary.IPath;
import org.gumtree.data.dictionary.IPathMethod;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.interfaces.IDictionary;
import org.gumtree.data.interfaces.IKey;
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
public final class ExtendedDictionary implements IExtendedDictionary, Cloneable{
    private IFactory            mFactory;     // Name of the plug-in's factory that created this object 
    private String              mExperiment;  // Experiment matching that dictionary
    private String              mVersion;     // Version of the read dictionary
    private ExternalClassLoader mClassLoader; // Object that loads external classes

    private String mKeyFile; // Path to reach the key file (containing the view)
    private String mMapFile; // Path to reach the mapping file

    private Map<IKey, String>  mKeyMap = new HashMap<IKey, String>();   // Key / ID association
    private Map<String, IPath> mPathMap = new HashMap<String, IPath>(); // ID / Path association
    private Map<String, ExtendedDictionary> mSubDict = new HashMap<String, ExtendedDictionary>(); // ID / sub-dictionaries

    public ExtendedDictionary(IFactory factory, String keyFile, String mapFile) { 
        mFactory    = factory; 
        mExperiment = Factory.getActiveView();
        mKeyFile    = keyFile;
        mMapFile    = mapFile;
    }

    protected ExtendedDictionary(IFactory factory, String keyFile, String mapFile, String experiment) {
        mExperiment = experiment;
        mFactory    = factory;
        mKeyFile    = keyFile;
        mMapFile    = mapFile;
    }

    @Override
    public void addEntry(String keyName, String entryPath)
    {
        IPath path = mFactory.createPath(entryPath);
        IKey key = mFactory.createKey(keyName);
        mKeyMap.put(key, keyName);
        mPathMap.put(keyName, path);
    }

    @Override
    public void addEntry(String keyName, IPath path) {
        IKey key = mFactory.createKey(keyName);
        mKeyMap.put(key, keyName);
        mPathMap.put(keyName, path);
    }

    @Override
    public boolean containsKey(String keyName)
    {
        IKey key = mFactory.createKey(keyName);
        return mKeyMap.containsKey(key);
    }

    @Override
    public List<IKey> getAllKeys()
    {
        return new ArrayList<IKey>(mKeyMap.keySet());
    }

    @Override
    public List<IPath> getAllPaths(IKey key)
    {
        String keyId = mKeyMap.get(key);
        List<IPath> result = null;
        if( keyId != null ) {
            result = new ArrayList<IPath>();
            result.add(mPathMap.get(keyId));
        }

        return result;
    }

    @Override
    public IPath getPath(IKey key)
    {
        IPath path = null;
        if( mKeyMap.containsKey(key) )
        {
            String keyName = mKeyMap.get(key);
            path = mPathMap.get(keyName);
        }
        return path;
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
            String filePath = dicFile.getAbsolutePath();
            readEntries(filePath);
        } 
        catch (Exception ex) 
        {
            throw new FileAccessException("failed to open the dictionary file", ex);
        }
    }

    @Override
    public void readEntries(String filePath) throws FileAccessException {
        if( filePath != null ) {
            mKeyFile = filePath;
        }

        // Read corresponding dictionaries
        readKeyDictionary();
        readMappingDictionary();
    }

    @Override
    public void readEntries() throws FileAccessException {
        readEntries((String) null);
    }

    @Override
    public void removeEntry(String keyName, String path) {
        IKey key = Factory.getFactory().createKey(keyName);
        String keyID = mKeyMap.get(key);
        mKeyMap.remove(key);
        mPathMap.remove(keyID);
    }

    @Override
    public void removeEntry(String keyName) {
        IKey key = Factory.getFactory().createKey(keyName); 
        String keyID = mKeyMap.get(key);
        mKeyMap.remove(key);
        mPathMap.remove(keyID);
    }


    @SuppressWarnings("unchecked")
    @Override
    public IDictionary clone() throws CloneNotSupportedException
    {
        ExtendedDictionary dict = new ExtendedDictionary(mFactory, mKeyFile, mMapFile, mExperiment);
        dict.mClassLoader = mClassLoader;
        dict.mVersion = mVersion;
        dict.mKeyMap  = (HashMap<IKey, String>)((HashMap<IKey, String>) mKeyMap).clone();
        dict.mPathMap = (HashMap<String, IPath>)((HashMap<String, IPath>) mPathMap).clone();
        dict.mSubDict = (HashMap<String, ExtendedDictionary>)((HashMap<String, ExtendedDictionary>) mSubDict).clone();
        return dict;
    }

    @Override
    public ExtendedDictionary getDictionary(IKey key) {
        String keyID = mKeyMap.get(key);
        ExtendedDictionary subDict = null;

        if( keyID != null ) {
            subDict = mSubDict.get(keyID);
        }

        return subDict;
    }

    @Override
    public String getVersionNum() {
        return mVersion;
    }

    @Override
    public String getView() {
        return mExperiment;
    }

    @Override
    public ExternalClassLoader getClassLoader() {
        if( mClassLoader == null ) {
            mClassLoader = AccessController.doPrivileged(new PrivilegedAction<ExternalClassLoader>() {
                public ExternalClassLoader run() {
                    return new ExternalClassLoader(mFactory.getName(), mVersion);
                }
            });
        }

        return mClassLoader;
    }

    @Override
    public String getFactoryName() {
        return mFactory.getName();
    }

    @Override
    public String getKeyFilePath() {
        return mKeyFile;
    }

    @Override
    public String getMappingFilePath() {
        return mMapFile;
    }


    // ---------------------------------------------------------------
    // PRIVATE : Reading methods
    // ---------------------------------------------------------------
    private Map<IKey, String> readKeyDictionary() throws FileAccessException {
        Element root = saxBuildFile(mKeyFile);

        String exp = root.getAttributeValue("name");
        if( ! exp.equalsIgnoreCase(mExperiment) ) {
            throw new FileAccessException("an I/O error prevent parsing dictionary!\nThe dictionary doesn't match the experiment!");
        }
        return readKeyDictionary(root);
    }

    private Map<IKey, String> readKeyDictionary(Element xmlNode) throws FileAccessException {

        List<?> nodes = xmlNode.getChildren();
        Element elem;
        IKey key;
        String keyName;

        for( Object current : nodes ) {
            elem = (Element) current;
            keyName = elem.getAttributeValue("key");
            if( keyName != null && !keyName.isEmpty() ) {
                key = mFactory.createKey(keyName);

                // If the element is an entry
                if( elem.getName().equals("item") ) {
                    mKeyMap.put(key, keyName);
                }
                // If the element is a group of keys
                else if( elem.getName().equals("group") ){
                    // Read corresponding dictionaries
                    ExtendedDictionary dict = new ExtendedDictionary(mFactory, mKeyFile, mMapFile, mExperiment);
                    dict.readKeyDictionary(elem);
                    dict.readMappingDictionary();
                    mSubDict.put(keyName, dict);
                    mKeyMap.put(key, keyName);
                }
            }
        }

        return mKeyMap;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> readMappingDictionary() throws FileAccessException {
        HashMap<String, String> mappingMap = new HashMap<String, String>();

        Element root = saxBuildFile(mMapFile);
        mVersion = root.getAttributeValue("version");

        List<?> nodes = root.getChildren();

        IPathMethod meth;
        IPath path;
        String keyID;
        IKey key;

        for( Entry<IKey, String> fullKey : mKeyMap.entrySet() ) {
            key   = fullKey.getKey();
            keyID = fullKey.getValue();

            // Check if a corresponding logical group exists
            if( mSubDict.containsKey(keyID) ) {
                // Create the corresponding path for a logical group
                meth = new PathMethod( "org.gumtree.data.Factory.createLogicalGroup" );
                path = mFactory.createPath(keyID);
                meth.pushParam(key);
                meth.isExternal(false);
                mKeyMap.put(key, keyID);
                mPathMap.put(keyID, path);
            }
            // No corresponding path where found so doing key/path association
            else if( !mPathMap.containsKey(keyID) ) {
                for( Element elem : (List<Element>) nodes ) {
                    if( elem.getAttributeValue("key").equals(keyID) ) {
                        path = loadPath(elem);
                        if( path == null ) {
                            throw new FileAccessException("error while associating IKey to IPath from dictionary!");
                        }
                        mPathMap.put(keyID, path);
                        break;
                    }
                }
            }
        }

        return mappingMap;
    }

    @SuppressWarnings("unchecked")
    private IPath loadPath(Element elem) {
        IPath path = null;
        List<?> nodes = elem.getChildren();
        List<IPathMethod> methods = new ArrayList<IPathMethod>();
        PathMethod method;
        path = mFactory.createPath("");
        for( Element node : (List<Element>) nodes ) {
            if( node.getName().equals("path") )  {
                path.setValue(node.getText());
            }
            else if( node.getName().equals("call") )  {
                method = new PathMethod( node.getText() );
                method.isExternal(true);
                methods.add( method );

                // Set method calls on path
                path.setMethods(methods);
            }
        }

        return path;
    }

    private Element saxBuildFile(String filePath) throws FileAccessException {
        // Determine the experiment dictionary according to given path
        File dicFile = null;
        if( filePath != null && !filePath.isEmpty() ) {
            dicFile = new File(filePath);
            // file doesn't exist
            if (!dicFile.exists()) {
                throw new FileAccessException("the target dictionary file does not exist:\n" + filePath);
            }
        }
        if( dicFile == null ) {
            dicFile = new File( Factory.getKeyDictionaryPath() );

            if( ! dicFile.exists() ) {
                throw new FileAccessException("the target dictionary file does not exist");
            }
        }

        // Parse the XML key dictionary
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
        root = dictionary.getRootElement();

        return root;
    }
}

