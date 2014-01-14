package org.gumtree.app.workbench.internal;

import java.util.List;

import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindowElement;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.workbench.WorkbenchUtils;
import org.gumtree.util.collection.IMapFilter;
import org.gumtree.util.messaging.EventHandler;
import org.gumtree.util.string.StringUtils;
import org.osgi.service.event.Event;

@SuppressWarnings("restriction")
public class SidebarActivator implements IStartup {

	@Override
	public void earlyStartup() {
		// Initial check
//		openCruisePanel(PlatformUI.getWorkbench().getActiveWorkbenchWindow());

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
				openCruisePanel(window);
			}
		});
		new EventHandler(UIEvents.UILifeCycle.PERSPECTIVE_OPENED) {
			@Override
			public void handleEvent(Event event) {
				SafeUIRunner.asyncExec(new SafeRunnable() {
					@Override
					public void run() throws Exception {
						openCruisePanel(PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow());
					}
				});
			}
		}.activate();
	}

	// nxi fix the null mWindow problem. 
	private void openCruisePanel(IWorkbenchWindow window) {
		// Check
		if (window == null) {
			return;
		}
		// Get current window model
		MWindow mWindow = WorkbenchUtils.getMWindow(window);
		if (mWindow == null) {
			return;
		}
		try{
			String perspectiveId = window.getActivePage().getPerspective().getId();
			List<String> disablePages = StringUtils.split(SystemProperties.CRUISE_ENABLE_PAGE.getValue(), ",");
			boolean cruiseEnabled = false;
			for (String id : disablePages) {
				if (perspectiveId != null && perspectiveId.equals(id)){
					cruiseEnabled = true;
					break;
				}
			}
			if (!cruiseEnabled) {
				return;
			}
		}catch (Exception e) {
			return;
		}
		// Find existing cruise part
		MPart cruisePart = WorkbenchUtils.getFirstChildWithProperty(mWindow,
				MPart.class, new IMapFilter<String, String>() {
					@Override
					public boolean accept(String key, String value) {
						return "type".equals(key)
								&& "sidebar".equals(value);
					}
				});
		// Do nothing if the window has already been processed
		if (cruisePart != null) {
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
		part.setLabel("Sidebar");
		part.setContributionURI(SystemProperties.SIDEBAR_PART_URI.getValue());
		part.getProperties().put("type", "sidebar");
		partSashContainer.getChildren().add(part);
		// Set sash weight
		originalContainer.setContainerData("8000");
		part.setContainerData("2000");
	}
}
