package org.gumtree.gumnix.sics.ui.util;

import java.util.Map;

import org.gumtree.gumnix.sics.ui.controlview.INodeSet;
import org.gumtree.ui.util.jface.ITreeNode;

/**
 * Indicates the tree node is subject to the Sics content cntribution to the
 * common navigator.
 *
 * @since 1.0
 */
public interface ISicsTreeNode extends ITreeNode {
	
	public Map<String, Boolean> getVisibilityMap();
	
	public void setVisibilityMap(Map<String, Boolean> visibilityMap);
	
	public INodeSet getNodeSet();
	
	public void setNodeSet(INodeSet nodeSet);
	
	public String getPath();
	
}
