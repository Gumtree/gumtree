package org.gumtree.gumnix.sics.internal.ui.componentview;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.core.ISicsManager;
import org.gumtree.gumnix.sics.core.SicsUtils;
import org.gumtree.gumnix.sics.ui.componentview.IComponentView;
import org.gumtree.gumnix.sics.ui.componentview.IComponentViewContent;

public class ComponentView extends ViewPart implements IComponentView {

	private static int viewActivationCount = 1;

//	private IComponentUIPart part;

	private IComponentViewContent content;
	
	private Composite holder;

	public ComponentView() {
		holder = null;
	}

	@Override
	public void createPartControl(Composite parent) {
		holder = new Composite(parent, SWT.NONE);
	}

	@Override
	public void setFocus() {
	}

	public void setComponentController(IComponentController controller) {
		if(holder != null && controller != null) {
			for(Control childControl : holder.getChildren()) {
				childControl.dispose();
			}

			Object contentObject = Platform.getAdapterManager().getAdapter(controller, IComponentViewContent.class);
			if(contentObject != null && contentObject instanceof IComponentViewContent) {
				content = (IComponentViewContent)contentObject;
				content.createPartControl(holder, controller);
			}

			holder.update();
			holder.layout(true);
		}
	}

	public void dispose() {
		if (content != null) {
			content.dispose();
			content = null;
		}
	}
	
	public static int getAndIncreaseViewActivationCount() {
		return viewActivationCount++;
	}

}
