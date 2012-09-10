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

package org.gumtree.widgets.swt.tree;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/**
 * Default implementation of ITreeNode.
 *
 * @since 1.0
 */
public class TreeNode implements ITreeNode {

	public static final ITreeNode[] EMPTY_TREE_NODE_ARRAY = new ITreeNode[0];

	public static final String EMPTY_STRING = "";

	private StructuredViewer viewer;

	private boolean visible;
	
	private Object originalObject;

	public TreeNode() {
		this(null, null);
	}

	public TreeNode(Object originalObject) {
		this(originalObject, null);
	}

	public TreeNode(Object originalObject, StructuredViewer viewer) {
		super();
		this.viewer = viewer;
		this.originalObject = originalObject;
		visible = true;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.ui.util.ITreeNode#getChildren()
	 */
	public ITreeNode[] getChildren() {
		return EMPTY_TREE_NODE_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.ui.util.ITreeNode#getColumnImage(int)
	 */
	public Image getColumnImage(int columnIndex) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.ui.util.ITreeNode#getText(int)
	 */
	public String getColumnText(int columnIndex) {
		return EMPTY_STRING;
	}

	public StructuredViewer getViewer() {
		return viewer;
	}

	public void setViewer(StructuredViewer viewer) {
		this.viewer = viewer;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.ui.util.ITreeNode#getOriginalObject()
	 */
	public Object getOriginalObject() {
		return originalObject;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.ui.util.ITreeNode#getImage()
	 */
	public Image getImage() {
		return getColumnImage(0);
	}

	/* (non-Javadoc)
	 * @see org.gumtree.ui.util.ITreeNode#getText()
	 */
	public String getText() {
		return getColumnText(0);
	}

	/* (non-Javadoc)
	 * @see org.gumtree.ui.util.ITreeNode#getBackground()
	 */
	public Color getBackground() {
		return getColumnBackground(0);
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.ui.util.ITreeNode#getForeground()
	 */
	public Color getForeground() {
		return getColumnForeground(0);
	}

	/* (non-Javadoc)
	 * @see org.gumtree.ui.util.ITreeNode#getColumnBackground(int)
	 */
	public Color getColumnBackground(int columnIndex) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.ui.util.ITreeNode#getColumnForeground(int)
	 */
	public Color getColumnForeground(int columnIndex) {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.ui.util.ITreeNode#isVisible()
	 */
	public boolean isVisible() {
		return visible;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.ui.util.ITreeNode#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.ui.util.ITreeNode#dispose()
	 */
	public void dispose() {
	}

}
