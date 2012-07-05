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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.util.configuration.internal.ConfigCriteria;
import org.gumtree.data.util.configuration.internal.ConfigParameter;
import org.gumtree.data.util.configuration.internal.ConfigParameterDynamic;
import org.gumtree.data.util.configuration.internal.ConfigParameterStatic;
import org.jdom.Element;

/**
 * ConfigDataset defines some criteria that will permit to verify the given IDataset
 * matches it. A IDataset of the plug-in has one and only one configuration.
 * <p>
 * It permits to determines some parameters that can be statically or dynamically (according
 * to some conditions or values in dataset) fixed for a that specific data model.
 * Those parameters (ConfigParameter) are resolved according the given IDataset.
 * <p>
 * Each IDataset should match a specific ConfigDataset.
 * 
 * @see ConfigParameter ConfigParameter: interface a parameter must implement
 * @see ConfigCriteria ConfigCriteria: criteria that an IDataset must respect to match a ConfigDataset
 * @author rodriguez
 *
 */

public class ConfigDataset {
    private ConfigCriteria mCriteria;             // Criteria dataset should match for this configuration
    private Map<String, ConfigParameter> mParams; // Param name/value to set in the plug-in when using this configuration
    private String mConfigLabel;                  // Name of the configuration

    /**
     * Constructor of the dataset configuration
     * need a dom element named: "config_dataset"
     * @param dataset_model DOM element "dataset_model"
     * @param params some default parameters that can be override by this Config
     */
    public ConfigDataset(Element dataset_model, List<ConfigParameter> params ) {
        mConfigLabel = dataset_model.getAttributeValue("name");
        mParams = new HashMap<String, ConfigParameter>();
        for( ConfigParameter param : params ) {
            mParams.put(param.getName(), param);
        }
        init(dataset_model);
    }

    /**
     * Return the label of that ConfigDataset
     * @return
     */
    public String getLabel() {
        return mConfigLabel;
    }

    /**
     * Returns the list of all parameters that can be asked for that configuration
     * @return list of ConfigParameter
     */
    public List<ConfigParameter> getParameters() {
        List<ConfigParameter> result = new ArrayList<ConfigParameter>();

        // Fills the output list with existing parameters
        for( Entry<String, ConfigParameter> entry : mParams.entrySet()) {
            result.add( entry.getValue() );
        }
        return result;
    }

    /**
     * Returns the value of the named <b>ConfigParameter</b> for the given IDataset. 
     * @param label of the parameter 
     * @param dataset used to resolve that parameter
     * @return the string value of the parameter
     */
    public String getParameter(String label, IDataset dataset) {
        String result = "";
        if( mParams.containsKey(label) ) {
            result = mParams.get(label).getValue(dataset);
        }
        return result;
    }

    /**
     * Returns the criteria a IDataset must respect to match that configuration.
     * @return ConfigCriteria object
     */
    public ConfigCriteria getCriteria() {
        return mCriteria;
    }

    /**
     * Add a parameter to that configuration
     * @param param implementing ConfigParameter interface
     */
    public void addParameter(ConfigParameter param) {
        mParams.put(param.getName(), param);
    }


    // ---------------------------------------------------------
    /// Private methods
    // ---------------------------------------------------------
    /**
     * Parse the DOM element "dataset_model" to initialize this object
     * @param config_dataset dom element markup "dataset_model"
     */
    private void init(Element config_dataset) {
        List<?> nodes;
        Element elem;
        Element section;
        ConfigParameter parameter;
        String name;
        String value;

        // Managing criteria
        mCriteria = new ConfigCriteria();
        section = config_dataset.getChild("criteria");
        if( section != null ) {
            mCriteria.add( section );
        }

        // Managing plugin parameters
        section = config_dataset.getChild("plugin");
        if( section != null ) {
            Element javaSection = section.getChild("java");
            if( javaSection != null ) {
                nodes = javaSection.getChildren("set");
                for( Object set : nodes ) {
                    elem  = (Element) set;
                    name  = elem.getAttributeValue("name");
                    value = elem.getAttributeValue("value");
                    parameter = new ConfigParameterStatic( name, value );
                    mParams.put( name, parameter );
                }
            }
        }

        // Managing dynamic parameters
        section = config_dataset.getChild("parameters");
        if( section != null ) {
            nodes = section.getChildren("parameter");
            for( Object node : nodes ) {
                elem  = (Element) node;
                name  = elem.getAttributeValue("name");
                value = elem.getAttributeValue("value");

                // Dynamic parameter
                if( value == null ) {
                    parameter = new ConfigParameterDynamic(elem);
                }
                // Static parameter (constant)
                else {
                    parameter = new ConfigParameterStatic(name, value);
                }
                mParams.put(name, parameter);
            }
        }
    }
}
