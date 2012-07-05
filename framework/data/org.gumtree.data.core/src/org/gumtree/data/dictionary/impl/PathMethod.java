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

import org.gumtree.data.dictionary.IPathMethod;

public class PathMethod implements IPathMethod  {
	String            m_method;
	ArrayList<Object> m_param;
	boolean           m_external = false; // Is the method from the core or is it an external one 
	
	public PathMethod(String method) { m_method = method; m_param = new ArrayList<Object>(); }
	public PathMethod(String method, Object param) { m_method = method; m_param = new ArrayList<Object>(); m_param.add(param); }
	
	@Override
	public String getName() { return m_method; }

	@Override
	public void setName(String method) { m_method = method; }

	@Override
	public Object[] getParam() { return m_param.toArray(); }
	
	@Override
	public void pushParam(Object param) { m_param.add(param); }

	@Override
	public Object popParam() { return m_param.remove(m_param.size()); }

	@Override
	public boolean isExternalCall() { return m_external; }

	@Override
	public void isExternal(boolean external) { m_external = external; }
}
