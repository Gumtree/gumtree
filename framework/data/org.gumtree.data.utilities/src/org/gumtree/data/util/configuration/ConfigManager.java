/*******************************************************************************
 * Copyright (c) 2012 Synchrotron SOLEIL.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Clément Rodriguez (clement.rodriguez@synchrotron-soleil.fr)
 ******************************************************************************/
package org.gumtree.data.util.configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.gumtree.data.Factory;
import org.gumtree.data.IFactory;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.exception.NoResultException;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.util.configuration.internal.ConfigParameter;
import org.gumtree.data.util.configuration.internal.ConfigParameterStatic;
import org.gumtree.data.utils.IFactoryResolver;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * ConfigManager is a <b>singleton</b>, only one instance managing all configurations for all plug-ins.
 * <p>
 * It associates for each plug-in one or several configurations. 
 * For a given IDataset of the plug-in one and only one configuration should match.
 * It permits to <b>determines some parameters that can be statically or dynamically</b> (according
 * to some conditions or values in dataset) fixed for that specific data model.
 * <p>
 * Each IDataset should match a specific ConfigDataset.
 * 
 * @see ConfigDataset
 * @author rodriguez
 *
 */

public class ConfigManager {
	
	// File name used for locating file under OSGi environment
	private static final String CLASS_OSGI_FILE_LOCATOR = "org.gumtree.data.util.configuration.OSGiFileLocator";
	
	// File name used for detect if we are running under OSGi environment
	private static final String CLASS_OSGI_BUNDLE_CONTEXT = "org.osgi.framework.BundleContext";
	
    private static ConfigManager mSingleton;       // Singleton pattern

    private Map<String, IFactory>               mFactories;      // Factories registered by the plug-in name
    private Map<String, File>                   mFiles;          // Files of configuration for each plug-in
    private Map<String, Vector<ConfigDataset> > mConfigurations; // Available configurations for each plug-in
    private Map<String, Boolean>                mInitialized;    // Does each plug-in been initiated

    /**
     * Get the configuration manager unique instance
     * 
     * @param pluginFactory from which we want to access the config manager
     * @param fileName the configuration file name (not path)
     * @return an instance of the manager for that plug-in
     */
    public static ConfigManager getInstance(IFactory pluginFactory, String fileName) {
        synchronized (ConfigManager.class ) {
            // Get the manager singleton instance
            if( mSingleton == null ) {
                mSingleton  = new ConfigManager();
            }

            // Initialize the plug-in's available configurations
            mSingleton.init(pluginFactory, fileName);
        }
        return mSingleton;
    }

    /**
     * Returns the proper configuration for the given dataset
     * @param dataset to detect config
     * @return the ConfigDataset that matches the given dataset
     * @throws FileAccessException if unable to load config file
     */
    public ConfigDataset getConfig( IDataset dataset ) throws NoResultException {
        ConfigDataset result = null;

        // Lock the class
        synchronized (ConfigManager.class ) {
            // Get plug-in name
            String factoryName = dataset.getFactoryName();

            // Check that this plug-in has yet loaded its configuration
            Boolean inited = mInitialized.get(factoryName);
            if( ! inited ) {
                try {
                    load(factoryName);
                }
                catch(FileAccessException e) {
                    throw new NoResultException("No result due to a FileAccessException!", e );
                }
            }

            // Seek for a matching configuration of the plug-in for that dataset
            boolean found = false;
            for( ConfigDataset conf: mConfigurations.get(factoryName) ) {
                if( conf.getCriteria().match(dataset) ) {
                    result = conf;
                    found = true;
                    break;
                }
            }

            // If not found throw
            if( !found ) {
                throw new NoResultException("NO matching configuration has been found!");
            }
        }
        return result;
    }


    // ---------------------------------------------------------
    /// Private methods
    // ---------------------------------------------------------
    /**
     *  Private constructor
     */
    private ConfigManager() {
        mFactories      = new HashMap<String, IFactory> ();
        mFiles          = new HashMap<String, File> ();
        mConfigurations = new HashMap<String, Vector<ConfigDataset> > ();
        mInitialized    = new HashMap<String, Boolean>();
    }

    /**
     * Initialize members
     * @param pluginFactory IFactory instance
     * @param fileName configuration file
     **/
    private void init(IFactory pluginFactory, String fileName) {
        // Get the name of the plug-in factory as key entry for map
        String factory = pluginFactory.getName();

        if( ! mFiles.containsKey( factory ) ) {
            // Get the mapping folder that should contains the configuration file
            String folder = Factory.getMappingDictionaryFolder( pluginFactory );

            // Construct the config file 
            File file = new File( folder + "/" + fileName );

            // Construct maps
            mFiles.put( factory, file );
            mInitialized.put( factory, false );
            mConfigurations.put( factory, new Vector<ConfigDataset>() );
            mFactories.put( factory, pluginFactory );
        }
    }

    /**
     * Parse the plug-in configuration file
     * 
     * @param name of the plug-in
     * @throws FileAccessException if unable to load configuration file
     */
    private void load(String name) throws FileAccessException {
        // Get the configuration file
        File file = mFiles.get(name);

        // Check configuration file's existence
        if( file != null ) {
            // Get the corresponding factory
            IFactory factory = mFactories.get(name);

            // Determine the experiment dictionary according to given path
            if (!file.exists()) {
            	// [ANSTO][Tony][2012-05-04] Second attemp to retrieve config
            	// file under the OSGi environment (subject to change)
            	File potentialFile = null;
            	if (name.equals("org.gumtree.data.soleil.NxsFactory")) {
            		try {
            			Class<?> osgiClass = Class.forName(CLASS_OSGI_BUNDLE_CONTEXT);
            			if (osgiClass != null) {
            				// Use reflection in case OSGi is not available at runtime
            				IFileLocator osgiFileLocator = (IFileLocator) Class
            						.forName(CLASS_OSGI_FILE_LOCATOR).newInstance();
							potentialFile = osgiFileLocator.findFile(
									"org.gumtree.data.soleil", "resources/"
											+ factory.getName() + "/" + file.getName());
            			}
            		} catch (Exception e) {
            		}
            	}
            	if (potentialFile == null || !potentialFile.exists()) {
            		throw new FileAccessException("Configuration file for '" + 
                            factory.getPluginLabel() + "' plug-in doesn't exist:\n" + 
                            file.getAbsolutePath()
                            );
				}
            	file = potentialFile;
            }

            // Parse the XML configuration file
            SAXBuilder xmlFile = new SAXBuilder();
            Document config;
            try {
                config = xmlFile.build(file);
            }
            catch (JDOMException e1) {
                throw new FileAccessException("Error while to parsing the configuration!\n" + file.getAbsolutePath() + "\n", e1);
            }
            catch (IOException e1) {
                throw new FileAccessException("An I/O error prevent parsing configuration!\n" + file.getAbsolutePath() + "\n", e1);
            }

            // Get the XML file root
            Element root = config.getRootElement();

            // Load global section: parameters for all configurations of that plugin
            List<ConfigParameter> params = loadGlobalSection(root);

            // Load each data model configuration
            List<?> nodes = root.getChildren("dataset_model");
            Element elem;
            Vector<ConfigDataset> configurations = new Vector<ConfigDataset>();
            ConfigDataset conf;
            for( Object node : nodes ) {
                elem = (Element) node;
                conf = new ConfigDataset(elem, params);
                configurations.add(conf);
            }
            mConfigurations.put(name, configurations);
            mInitialized.put(name, true);
        }

    }

    /**
     * Parse the global section (i.e: default parameters for each configuration)
     * @param root DOM document's root "configuration"
     * @return
     */
    private List<ConfigParameter> loadGlobalSection( Element root ) {
        List<ConfigParameter> result = new ArrayList<ConfigParameter>();
        List<?> paramNodes;
        Element elem;
        String name;
        String value;

        // Parser the "global" section 
        List<?> nodes = root.getChildren("global");
        for( Object node : nodes ) {
            // Only consider "java" part
            elem = (Element) node;
            elem = elem.getChild("java");

            // Parse all "set"
            paramNodes = elem.getChildren("set");
            for( Object set : paramNodes ) {
                // Construct static parameters
                elem  = (Element) set;
                name  = elem.getAttributeValue("name");
                value = elem.getAttributeValue("value");
                result.add( new ConfigParameterStatic( name, value ) );
            }
        }

        return result;
    }
}
