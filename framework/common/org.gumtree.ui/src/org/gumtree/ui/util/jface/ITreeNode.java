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

package org.gumtree.ui.util.jface;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/**
 * TreeNode is a wrapper for model object in the
 * multi column tree viewer.  It encapsulates the display
 * logic and makes viewer content and label provider to be
 * more generic.
 *
 * @since 1.0
 */
public interface ITreeNode {

	/**
	 * Returns the children of this node in wrapped objects.
	 *
	 * @return an array of children tree node
	 */
	public ITreeNode[] getChildren();

	/**
	 * Returns the text of this tree node.  By default is should
	 * return getColumnText(0) (default column text)
	 *
	 * @return text label of the node
	 */
	public String getText();

	/**
	 * Returns the image of this tree node.  By default is should
	 * return getColumnImage(0) (default column image)
	 *
	 * @return text label of the node
	 */
	public Image getImage();

	/**
	 * Returns the background colour of this tree node.  By default is should
	 * return getColumnBackgroundColor(0) (default column background colour)
	 *
	 * @return background colour of the node
	 */
	public Color getBackground();
	
	/**
	 * Returns the foreground colour of this tree node.  By default is should
	 * return getColumnForegroundColor(0) (default column foreground colour)
	 *
	 * @return foreground colour of the node
	 */
	public Color getForeground();
	
	/**
	 * Returns the text label of the node for a specified column.
	 *
	 * @param columnIndex index of the column
	 * @return text label of the node
	 */
	public String getColumnText(int columnIndex);

	/**
	 * Returns an image of the node for a specified column.
	 *
	 * @param columnIndex index of the column
	 * @return image of the node
	 */
	public Image getColumnImage(int columnIndex);

	/**
	 * Returns the background colour of the node for a specific column.
	 * 
	 * @param columnIndex index of the column
	 * @return background colour of the node
	 */
	public Color getColumnBackground(int columnIndex);
	
	/**
	 * Returns the foreground colour of the node for a specific column.
	 * 
	 * @param columnIndex index of the column
	 * @return foreground colour of the node
	 */
	public Color getColumnForeground(int columnIndex);
	
	/**
	 * Returns rather is node should be included in the tree viewer or not.
	 * If node is set to non-visible, all its children will not be included
	 * in the tree.
	 * 
	 * @return the visibility of the node
	 */
	public boolean isVisible();
	
	/**
	 * Manually sets the visibility of this node.
	 *  
	 * @param visible visibility of the node
	 */
	public void setVisible(boolean visible);
	
	/**
	 * Retunrns the viewer which this node belongs to.  Users can
	 * use this up refresh the viewer if node has made any changes.
	 *
	 * @return the structured viewer for the node
	 */
	public StructuredViewer getViewer();

	/**
	 * Sets the viewer which this node belongs to.
	 *
	 * @param viewer the structured viewer for the node
	 */
	public void setViewer(StructuredViewer viewer);

	/**
	 * Returns the object that is wrapped by this tree node.
	 *
	 * @return original node object
	 */
	public Object getOriginalObject();

	/**
	 * Called when this tree node is disposed by the hosting widget.
	 */
	public void dispose();

}
