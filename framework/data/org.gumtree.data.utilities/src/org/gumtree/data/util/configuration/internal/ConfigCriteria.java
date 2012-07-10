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
package org.gumtree.data.util.configuration.internal;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.util.configuration.internal.ConfigParameter.CriterionValue;
import org.jdom.Element;

/**
 * This class <b>ConfigCriteria</b> gather a set of criterion. It is used to 
 * determine wether a IDataset is matching to <b>ConfigDataset</b>'s criteria.
 * <p>
 * To be considered as matching the IDataset must be returning <b>CriterionValue.TRUE</b>
 * at the evaluation of each <b>ConfigParameter</b>
 * <p>
 * Each <b>ConfigParameter</b> of the contained set of criterion, must return the
 * string form of a <b>CriterionValue</b>. 
 *
 * @see ConfigParameterCriterion ConfigParameterCriterion implements ConfigParameter 
 * @see CriterionValue
 * @author rodriguez
 *
 */
public class ConfigCriteria {
    // Private members
    private List<ConfigParameterCriterion> mCriterion;

    /**
     * Constructor
     * 
     * @note if no ConfigParameterCriterion are added, then it will match any IDataset
     */
    public ConfigCriteria() {
        mCriterion = new ArrayList<ConfigParameterCriterion>();
    }

    /**
     * Parse the DOM element, to add every "if" children element as ConfigParameterCriterion.
     * 
     * @param domCriteria DOM element corresponding to "criteria" section in XML
     */
    public void add(Element domCriteria) {
        // Managing criterion
        if( domCriteria.getName().equals("criteria") ) {
            List<?> nodes = domCriteria.getChildren("if");
            for( Object node : nodes ) {
                mCriterion.add( new ConfigParameterCriterion((Element) node) );
            }
        }
    }

    /**
     * Add the given ConfigParameterCriterion to the set of ConfigParameter.
     * 
     * @param item ConfigParameterCriterion that will be checked when matching 
     */
    public void add(ConfigParameterCriterion item) {
        mCriterion.add(item);
    }

    /**
     * Tells if the given IDataset matches this ConfigCriteria. All clause must be
     * respected by the given IDataset. 
     * 
     * @param dataset to be checked
     * @return boolean true if each ConfigParameterCriterion are respected in the given IDataset
     */
    public boolean match( IDataset dataset ) {
        boolean result = true;

        for( ConfigParameterCriterion criterion : mCriterion ) {
            if( criterion.getValue(dataset).equals(CriterionValue.FALSE.toString()) ) {
                result = false;
                break;
            }
        }

        return result;
    }
}
