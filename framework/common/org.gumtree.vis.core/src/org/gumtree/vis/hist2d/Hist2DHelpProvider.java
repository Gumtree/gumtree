/**
 * 
 */
package org.gumtree.vis.hist2d;

import org.gumtree.vis.awt.AbstractHelpProvider;

/**
 * @author nxi
 *
 */
public class Hist2DHelpProvider extends AbstractHelpProvider{

	public Hist2DHelpProvider() {
		add("zoom in", "Mouse Drag or Mouse Wheel Up Scroll", "Use mouse drag from " +
				"top-left to bottom-right to zoom in a region.\nOr Scroll mouse wheel " +
				"up to zoom in proportionally.");
		add("zoom out", "Mouse Wheel Down Scroll", "Scroll mouse wheel down to zoom out proportionally.");
		add("zoom reset", "Ctrl + R, Ctrl + Z or Reverse Mouse Drag", "Use mouse drag from " +
				"bottom-right to top-left to reset the bounds. Or use Ctrl + R key combination " +
				"to reset the zoom when the image is focused");
		add("panning", "Ctrl + Mouse Drag", "Press down the Ctrl key, then use the mouse drag to pan " +
				"the image in any direction.");
		add("draw inclusive mask", "Shift + Mouse Button Drag", "Press down the Shift key, " +
				"then use the button of the mouse drag to draw a mask. If the left " +
				"button of the mouse is used, the mask will be rectangle. If the right " +
				"button of the mouse is used, the mask will be ellipse.");
		add("draw exclusive mask", "Shift + Alt + Mouse Button Drag", "Press down the Shift key, " +
				"then use the button of the mouse drag to draw a mask. If the left " +
				"button of the mouse is used, the mask will be rectangle. If the right " +
				"button of the mouse is used, the mask will be ellipse.");
		add("select a mask", "Mouse Left Button Click", "Simple use the left button of the mouse to" +
				" select a mask. If more than one masks are overlapping with each other, click again " +
				"on the overlapping area will switch the section. There is only one mask can be selected" +
				" at a time.");
		add("modify a mask", "Mouse Drag on Boarders of Selected Mask", "To modify a mask, it needs to be selected " +
				"first. Drag the boarder of the mask to change the size. ");
		add("shift a mask", "Mouse Drag or Arrow Keys", "To shift a mask, it needs to be " +
				"selected. Drag the centre of the mask to move the mask. Or use Arrow keys to shift " +
				"the mask accurately.");
		add("remove a mask", "Delete Key", "To remove a mask, first select the mask. Then use the " +
				"Delete key to remove it.");
		add("copy image to clipboard", "Ctrl + C", "use Ctrl + C to copy the image to clipboard. " +
				"The image can be pasted to image processing software.");
		add("print the image", "Ctrl + P", "use Ctrl + P to pop up the printing dialog.");
		add("export the image", "Ctrl + E", "use Ctrl + E to pop up the 'save as' dialog.");
		add("export the image", "Ctrl + E", "use Ctrl + E to pop up the 'save as' dialog.");
	}
	
}
