/**
 * 
 */
package org.gumtree.vis.surf3d;

import org.gumtree.vis.awt.AbstractHelpProvider;

/**
 * @author nxi
 *
 */
public class Surf3DHelpProvider extends AbstractHelpProvider{

	public Surf3DHelpProvider() {
		add("zoom in", "Mouse Wheel Drag or Mouse Wheel Up Scroll", "Hold the mouse wheel button, " +
				"or middle (2nd) mouse button, and push the mouse up to zoom in (move closer to the object).");
		add("zoom out", "Mouse Wheel Drag or Mouse Wheel Up Scroll", "Hold the mouse wheel button, " +
				"or middle (2nd) mouse button, and push the mouse down to zoom out (move further from the object).");
		add("zoom reset", "Ctrl + R or Ctrl + Z", "Use Ctrl + R or Ctrl + Z key combinations " +
				"to reset the plot to the default view. Or click on the Reset camera distance menu button " +
				"to reset the zooming.");
		add("pan the view", "Right Mouse Button Drag", "Press down the right mouse button (3rd button) and drag " +
				"the mouse to pan the view.");
		add("rotate", "Left Mouse Button Drag", "Press down the left button of the mouse and drag to rotate " +
				"the object. To reset to the default orientation, use Ctrl + R, Ctrl + Z or click on the " +
				"Reset orientation menu button.");
		add("copy image to clipboard", "Ctrl + C", "use Ctrl + C to copy the image to clipboard. " +
				"The image can be pasted to image processing software.");
		add("print the image", "Ctrl + P", "use Ctrl + P to pop up the printing dialog.");
		add("export the image", "Ctrl + E", "use Ctrl + E to pop up the 'save as' dialog.");
	}
	
}
