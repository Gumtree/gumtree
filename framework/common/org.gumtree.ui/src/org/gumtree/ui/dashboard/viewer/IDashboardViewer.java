package org.gumtree.ui.dashboard.viewer;

import org.gumtree.core.object.IConfigurable;
import org.gumtree.ui.dashboard.model.Dashboard;

public interface IDashboardViewer extends IConfigurable {

	public Dashboard getModel();
	
	public void setModel(Dashboard model);
	
}
