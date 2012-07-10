/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.ui.widget.tree;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * The class is an implementation of ITreeContentProvider interface 
 * to support DefaultMutableTreeNode objects as an elements.
 * 
 * @author Danil Klimontov (dak)
 */
public class DefaultTreeContentProvider implements ITreeContentProvider {

	public Object[] getChildren(Object parentElement) {
		final List childrenArray = new ArrayList();
		Enumeration children = ((DefaultMutableTreeNode)parentElement).children();
		for (;children.hasMoreElements();) {
			childrenArray.add(children.nextElement());
		}
		return childrenArray.toArray();
	}

	public Object getParent(Object element) {
		return ((DefaultMutableTreeNode)element).getParent();
	}

	public boolean hasChildren(Object element) {
		return !((DefaultMutableTreeNode)element).isLeaf();
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
		//nothing to dispose
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
