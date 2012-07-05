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

import org.gumtree.data.interfaces.IDataset;

/**
 * <b>Interface ConfigParameter</b><br/>It permits to associate a label
 * to a string value. The way the value will be defined depends on the 
 * implementation: it can be done statically, dynamically or it can correspond 
 * to a boolean test.
 * <p>
 * There are 2 main usages of those ConfigParameter:<br>
 * - the first one is to perform a matching test between a IDataset and a 
 * ConfigurationDataset that will carry some parameters used by the plug-in.
 * Using <b>ConfigParameterCriterion</b><br>
 * - the second is to evaluate parameters according to the IDataset. Those parameters
 * will be accessible through the ConfigDataset and defined on the fly. Using 
 * <b>ConfigParameterDynamic</b> or <b>ConfigParameterStatic</b>
 * 
 *
 * @see ConfigParameterCriterion
 * @see ConfigParameterDynamic
 * @see ConfigParameterStatic
 * 
 * @author rodriguez
 */
public interface ConfigParameter {
    /**
     * Get the name of that ConfigParameter
     * @return name of the parameter
     */
    public String getName();

    /**
     * Get the type of this ConfigParameter 
     * @return CriterionType
     * @see CriterionType
     */
    public CriterionType getType();

    /**
     * Evaluate the parameter according its type and a given IDataset.
     * 
     * @param dataset the data source that will be used to perform evaluation
     * 
     * @return the result of the evaluation in a String representation
     */
    public String getValue(IDataset dataset);

    /**
     * CriterionType is an enumeration where elements are :<br/>
     * - EXIST check if the path exists in IDataset, a boolean value can be associated<br/>
     * - NAME get the name of the object targeted by the path<br/>
     * - VALUE get the value of IDataItem targeted by the path<br/>
     * - CONSTANT the value will be a constant<br/>
     * - EQUAL will operate an equality test between targeted IDataItem's and a given value<br/>
     * - NONE parameter value will be null<br/>
     */
    public enum CriterionType {
        EXIST     ("exist"),
        NAME      ("name"),
        VALUE     ("value"),
        CONSTANT  ("constant"),
        EQUAL     ("equal"),
        NONE      ("");

        private String mName;

        private CriterionType(String type) { mName = type; }
        public String getName()            { return mName; }
    }

    /**
     * CriterionValue is an enumeration where elements are :<br/>
     * - TRUE having value string "true"<br/>
     * - FALSE having value string "false"<br/>
     * - NONE having value string ""<br/>
     */
    public enum CriterionValue {
        TRUE     ("true"),
        FALSE    ("false"),
        NONE     ("");

        private String mName;

        private CriterionValue(String type) { mName = type; }
        public String getName()             { return mName; }
    }
}
