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

import java.io.IOException;

import org.gumtree.data.exception.NoResultException;
import org.gumtree.data.interfaces.IContainer;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.util.configuration.internal.ConfigParameter.CriterionType;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * <b>ConfigParameterDynamic implements ConfigParameter</b><br>
 * 
 * The aim of that class is to define <b>parameters that are dependent</b> of the content 
 * of the IDataset. It means that those parameters <b>are specific to that file</b>.
 * The evaluation will be performed on the fly when requested.<p> 
 * 
 * This class is used by ConfigDataset when the plug-in asks for a specific value
 * for that specific file. For example is:<br/>
 * - how to know on which beamline it was created</br>
 * - what the data model is</br><p>
 * 
 * <b>The parameter's type can be:</b><br/>
 * - CriterionType.EXIST will try to find a specific path in the IDataset <br/>
 * - CriterionType.NAME get the name of the object targeted by the path<br/>
 * - CriterionType.VALUE get the value of IDataItem targeted by the path<br/>
 * - CriterionType.CONSTANT the value will be a constant<br/>
 * - CriterionType.EQUAL will compare values targeted by a path to a referent one<p>
 * 
 * It corresponds to "parameter" DOM element in the "parameters" section
 * of the plug-in's configuration file.
 *
 * @see ConfigParameter
 * @see CriterionType
 * 
 * @author rodriguez
 */
public final class ConfigParameterDynamic implements ConfigParameter {
    private String         mName;        // Parameter name
    private String         mPath;        // Path to seek in
    private CriterionType  mType;        // Type of operation to do
    private CriterionValue mTest;        // Expected property
    private String         mValue;       // Comparison value with the expected property


    public ConfigParameterDynamic(Element parameter) {
        init(parameter);
    }

    @Override
    public CriterionType getType() {
        return mType;
    }

    public String getName() {
        return mName;
    }

    public String getValue(IDataset dataset) {
        String result = "";
        IContainer cnt;
        switch( mType ) {
        case EXIST:
            cnt = openPath(dataset);
            CriterionValue crt;
            if( cnt != null ) {
                crt = CriterionValue.TRUE;
            }
            else {
                crt = CriterionValue.FALSE;
            }
            result = mTest.equals( crt ) ? CriterionValue.TRUE.toString() : CriterionValue.FALSE.toString();
            break;
        case NAME:
            cnt = openPath(dataset);
            if( cnt != null ) {
                result = cnt.getShortName();
            }
            else {
                result = null;
            }
            break;
        case VALUE:
            cnt = openPath(dataset);
            if( cnt != null && cnt instanceof IDataItem ) {
                try {
                    result = ((IDataItem) cnt).getData().getObject(((IDataItem) cnt).getData().getIndex()).toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                result = null;
            }
            break;
        case CONSTANT:
            result = mValue;
            break;
        case EQUAL:
            cnt = openPath(dataset);
            if( cnt != null && cnt instanceof IDataItem ) {
                String value;
                try {
                    value = ((IDataItem) cnt).getData().getObject(((IDataItem) cnt).getData().getIndex()).toString();
                    if( value.equals(mValue) ) {
                        crt = CriterionValue.TRUE;
                    }
                    else {
                        crt = CriterionValue.FALSE;
                    }
                    result = mTest.equals( crt ) ? CriterionValue.TRUE.toString() : CriterionValue.FALSE.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                result = null;
            }
            break;
        case NONE:
        default:
            result = null;
            break;
        }
        return result;
    }

    // ---------------------------------------------------------
    /// Private methods
    // ---------------------------------------------------------
    private void init(Element dom) {
        // Given element is <parameter>
        Element valueNode = dom.getChild("value");
        mName = dom.getAttributeValue("name");
        mPath = valueNode.getAttributeValue("target");
        Attribute attribute;

        String test;

        attribute = valueNode.getAttribute("type");
        if( attribute != null )
        {
            mType  = CriterionType.valueOf(attribute.getValue().toUpperCase());
            mTest  = CriterionValue.NONE;
            String value = valueNode.getAttributeValue("value");
            if( value != null ) {
                mValue = value;
            }
            else {
                value = valueNode.getAttributeValue("constant");
                if( value != null ) {
                    mValue = value;
                }
            }

            test = valueNode.getAttributeValue("test");
            if( test != null ) {
                mTest = CriterionValue.valueOf(test.toUpperCase());
            }
            mValue = value;
            return;
        }
        else {
            attribute = valueNode.getAttribute("exist");
            if( attribute != null ) {
                mType  = CriterionType.EXIST;
                mTest  = CriterionValue.valueOf(attribute.getValue().toUpperCase());
                mValue = "";
                return;
            }

            if( attribute == null ) {
                attribute = valueNode.getAttribute("equal");
                if( attribute != null ) {
                    mType  = CriterionType.EQUAL;
                    mTest  = CriterionValue.TRUE;
                    mValue = attribute.getValue();
                    return;
                }
            }
            if( attribute == null ) {
                attribute = valueNode.getAttribute("not_equal");
                if( attribute != null ) {
                    mType  = CriterionType.EQUAL;
                    mTest  = CriterionValue.FALSE;
                    mValue = attribute.getValue();
                    return;
                }
            }
            if( attribute == null ) {
                attribute = valueNode.getAttribute("constant");
                if( attribute != null ) {
                    mType  = CriterionType.CONSTANT;
                    mTest  = CriterionValue.NONE;
                    mValue = attribute.getValue();
                    return;
                }
            }
        }
    }

    private IContainer openPath( IDataset dataset ) {
        IGroup root = dataset.getRootGroup();
        IContainer result = null;
        try {
            result = root.findContainerByPath(mPath);
        } catch (NoResultException e) {
        }
        return result;
    }
}
