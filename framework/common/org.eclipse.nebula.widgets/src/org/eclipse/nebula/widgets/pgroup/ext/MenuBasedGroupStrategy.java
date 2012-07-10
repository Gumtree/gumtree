package org.eclipse.nebula.widgets.pgroup.ext;

import org.eclipse.nebula.widgets.pgroup.RectangleGroupStrategy;

public class MenuBasedGroupStrategy extends RectangleGroupStrategy {

	public boolean isToggleLocation(int x, int y) {
		if (getGroup().getToggleRenderer() != null) {
			return getGroup().getToggleRenderer().getBounds().contains(x, y);	
		}
		return false;
	}
	
}
