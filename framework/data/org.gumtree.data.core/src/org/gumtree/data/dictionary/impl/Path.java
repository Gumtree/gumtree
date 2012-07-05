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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gumtree.data.Factory;
import org.gumtree.data.IFactory;
import org.gumtree.data.dictionary.IPath;
import org.gumtree.data.dictionary.IPathMethod;
import org.gumtree.data.dictionary.IPathParameter;
import org.gumtree.data.utils.Utilities.ParameterType;

public class Path implements IPath {
	final public String PARAM_PATTERN = "\\$\\(([^\\)]*)\\)"; // parameters have following shape "/my/path/$(parameter)/my_node"
	final private String PATH_SEPARATOR;
	
	String            m_factory;
	String            m_pathValue;
	String            m_pathOrigin;
	List<IPathMethod> m_methods;

	public Path(IFactory factory) {
		m_factory      = factory.getName();
		m_pathValue    = null;
		m_pathOrigin   = null;
		m_methods      = new ArrayList<IPathMethod>();
		PATH_SEPARATOR = factory.getPathSeparator();
	}

	public Path(IFactory factory, String path) {
		m_factory      = factory.getName();
		m_pathOrigin   = path;
		m_pathValue    = path;
		m_methods      = new ArrayList<IPathMethod>();
		PATH_SEPARATOR = factory.getPathSeparator();
	}
	
	@Override
	public String toString() {
		return m_pathValue;
	}

	@Override
	public String getFactoryName() {
		return null;
	}

	@Override
	public String getValue() {
		return m_pathValue;
	}
	
	@Override
	public List<IPathMethod> getMethods() {
		return Collections.unmodifiableList(new ArrayList<IPathMethod>(m_methods));
	}

	public void setMethods(List<IPathMethod> methods) {
		m_methods = methods;
	}

	@Override
	public void setValue(String path) {
		m_pathOrigin = path;
		m_pathValue  = path;
	}
	
	@Override
	public void applyParameters(List<IPathParameter> params) {
		for( IPathParameter param : params ) {
			m_pathValue = m_pathValue.replace( "$("+ param.getName() + ")" , param.getValue().toString() );
		}
	}
	
	@Override
	public void removeUnsetParameters() {
		m_pathValue = m_pathValue.replaceAll( PARAM_PATTERN , "");
	}
	
	@Override
	public void resetParameters() {
		m_pathValue = m_pathOrigin;
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public IPathParameter getFirstPathParameter(StringBuffer output) {
		IPathParameter result = null;
		String[] pathParts = m_pathValue.split(Pattern.quote(PATH_SEPARATOR));
		String name;

		// Split the path into nodes
		for( String part : pathParts ) {
			if( part != null && !part.isEmpty() ) {
				output.append(PATH_SEPARATOR);
				Pattern pattern = Pattern.compile(PARAM_PATTERN);
				Matcher matcher = pattern.matcher(part);
				if( matcher.find() ) {
					name = part.replaceAll(".*" + PARAM_PATTERN + ".*", "$1");
					result = Factory.getFactory(m_factory).createPathParameter(ParameterType.SUBSTITUTION, name, "");
					output.append(part.replaceAll( PARAM_PATTERN , "") );
					break;
				}
				else {
					output.append(part);
				}
			}
		}
		
		return result;
	}
	
}
