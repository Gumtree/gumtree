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

import org.gumtree.data.IFactory;
import org.gumtree.data.dictionary.IPathParameter;
import org.gumtree.data.utils.Utilities.ParameterType;

public class PathParameter implements IPathParameter {
	private ParameterType  m_type;
    private Object         m_value;
    private String         m_name;
    private String         m_factory;
    
    public PathParameter(IFactory factory, ParameterType filter, String name) {
    	m_name    = name;
        m_type    = filter;
        m_value   = null;
        m_factory = factory.getName();
    }
    
    public PathParameter(IFactory factory, ParameterType filter, String name, Object value) {
        m_type    = filter;
        m_value   = value;
        m_name    = name;
        m_factory = factory.getName();
    } 

    @Override
    public ParameterType getType() {
    	return m_type;
    }
    
    @Override
    public Object getValue() {
    	return m_value;
    }
    
    @Override
    public void setValue(Object value) {
    	m_value = value;
    }

    @Override
    public boolean equals(IPathParameter keyfilter) {
        return ( m_value.equals(keyfilter.getValue()) && m_type.equals(keyfilter.getType()) ) ;
    }
    
    @Override
    public String toString() {
        return m_name + "=" + m_value; 
    }
    
    @Override
    public IPathParameter clone() {
    	PathParameter param = new PathParameter();
    	param.m_factory = m_factory;
    	param.m_name    = m_name;
    	param.m_type    = m_type;
    	param.m_value   = m_value;
    	return param;
    }

	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public String getFactoryName() {
		return m_factory;
	}

	/*
	@Override
	public void update(IPath path) {
		String value = path.getValue();
		value = value.replaceAll("\\$\\(" + m_name + "\\)", m_value.toString() );
		path.setValue(value);
	}
	*/
	private PathParameter() {};
}
