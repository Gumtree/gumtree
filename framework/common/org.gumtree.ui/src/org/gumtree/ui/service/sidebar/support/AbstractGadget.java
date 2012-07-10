/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.ui.service.sidebar.support;

import org.gumtree.ui.service.sidebar.IGadget;
import org.gumtree.util.collection.IParameters;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

public abstract class AbstractGadget implements IGadget {

	private String name;
	
	private String perspectives;
	
	private int level;
	
	private IParameters parameters;
	
	public AbstractGadget() {
		super();
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String getPerspectives() {
		return perspectives;
	}

	@Override
	public void setPerspectives(String perspectives) {
		this.perspectives = perspectives;
	}

	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public IParameters getParameters() {
		return parameters;
	}

	@Override
	public void setParameters(IParameters parameters) {
		this.parameters = parameters;
	}
	
	protected ToStringHelper createToStringHelper() {
		return Objects.toStringHelper(getClass())
				.add("name", getName())
				.add("perspectives", getPerspectives())
				.add("level", getLevel());
	}
	
	@Override
	public String toString() {
		return createToStringHelper().toString();
	}
	
}
