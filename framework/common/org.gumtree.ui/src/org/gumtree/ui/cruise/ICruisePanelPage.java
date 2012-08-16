package org.gumtree.ui.cruise;

import org.eclipse.swt.widgets.Composite;
import org.gumtree.core.service.IContributionService;

public interface ICruisePanelPage extends IContributionService {
	
	public String getName();
	
	public Composite create(Composite parent);
	
}
