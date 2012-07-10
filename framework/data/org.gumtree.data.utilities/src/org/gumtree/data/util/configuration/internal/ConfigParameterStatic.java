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
import org.gumtree.data.util.configuration.internal.ConfigParameter.CriterionType;

/**
 * <b>ConfigParameterStatic implements ConfigParameter</b><br>
 * 
 * The aim of that class is to set parameters that are independent of the content 
 * of the IDataset. It means that those parameters are only specific to the data 
 * model.<p> 
 * <b>The parameter is a constant</b> (CriterionType.CONSTANT)<br/>
 * This class is used by ConfigDataset when the plug-in asks for a specific value
 * for that data model. For example is the debug mode activate or a size of 
 * cache... ?<br>  
 * It corresponds to "set" DOM element in "java" part of the "plugin" section
 * in the plugin configuration file.
 *
 * @see ConfigParameter
 * @see CriterionType
 * 
 * @author rodriguez
 */
public final class ConfigParameterStatic implements ConfigParameter {
    private String         mName;     // Parameter name
    private String         mValue;    // Parameter value

    public ConfigParameterStatic(String name, String value) {
        mName = name;
        mValue = value;

    }

    @Override
    public String getValue(IDataset dataset) {
        return mValue;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public CriterionType getType() {
        return CriterionType.CONSTANT;
    }

}

