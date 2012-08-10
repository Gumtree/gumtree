package org.gumtree.app.workbench.internal;

import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindowElement;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.gumtree.ui.util.workbench.WorkbenchUtils;

@SuppressWarnings("restriction")
public class CruisePanelActivator implements IStartup {

	@Override
	public void earlyStartup() {
		// Initial check
		openCruisePanel(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		
		// Listener
		PlatformUI.getWorkbench().addWindowListener(new IWindowListener() {
			@Override
			public void windowOpened(IWorkbenchWindow window) {
				openCruisePanel(window);
			}			
			@Override
			public void windowDeactivated(IWorkbenchWindow window) {
			}
			@Override
			public void windowClosed(IWorkbenchWindow window) {
			}			
			@Override
			public void windowActivated(IWorkbenchWindow window) {
			}
		});
	}
	
	private void openCruisePanel(IWorkbenchWindow window) {
		// Check
		if (window == null) {
			return;
		}
		// Get current window model
		MWindow mWindow = WorkbenchUtils.getMWindow(window);
		// Do nothing if the window has already been processed
		String processed = mWindow.getProperties().get("processed");
		if (processed != null) {
			return;
		}
		// Get original container with this window
		MWindowElement originalContainer = mWindow.getChildren().get(0);
		// Create a new sash
		MPartSashContainer partSashContainer = MBasicFactory.INSTANCE
				.createPartSashContainer();
		partSashContainer.setHorizontal(true);
		mWindow.getChildren().add(partSashContainer);
		// Move the original container
		mWindow.getChildren().remove(originalContainer);
		partSashContainer.getChildren().add(
				(MPartSashContainerElement) originalContainer);
		// Create cruise panel
		MPart part = MBasicFactory.INSTANCE.createPart();
		part.setLabel("Cruise Panel");
		part.setContributionURI("bundleclass://org.gumtree.ui/org.gumtree.ui.cruise.support.CruisePanel");
		partSashContainer.getChildren().add(part);
		// Set sash weight
		originalContainer.setContainerData("8000");
		part.setContainerData("2000");
		// Mark as processed
		mWindow.getProperties().put("processed", "true");
	}

}
