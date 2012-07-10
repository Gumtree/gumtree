package org.gumtree.ui.util.workbench;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

public final class ContentViewUtils {
	
	private static long viewCounter = 0;
	
	public static IContentView createContentView(
			IViewContentContributor contributor) throws PartInitException {
		return createContentView(contributor, PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow());
	}

	public static IContentView createContentView(
			IViewContentContributor contributor, IWorkbenchWindow window)
			throws PartInitException {
		IContentView contentView = (IContentView) PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().showView(
						ViewUIConstants.ID_VIEW_CONTENT, Long.toString(viewCounter++),
						IWorkbenchPage.VIEW_ACTIVATE);
		contentView.setContentContributor(contributor);
		return contentView;
	}

	public static void createContentPerspective(
			IViewContentContributor contributor) throws WorkbenchException {
		createContentPerspective(contributor, PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow());
	}
	
	public static void createContentPerspective(
			IViewContentContributor contributor, IWorkbenchWindow window)
			throws WorkbenchException {
		IWorkbenchPage page = PlatformUI.getWorkbench().showPerspective(
				ViewUIConstants.ID_PERSPECTIVE_CONTENT, window);
//		IExtendedWorkbenchWindow extendedWindow = (IExtendedWorkbenchWindow) window
//				.getService(IExtendedWorkbenchWindow.class);
////		IWorkbenchBar workbenchBar = extendedWindow.getWorkbenchBar();
//		IWorkbenchBar workbenchBar = null;
//		// Workbench bar can be null if we choose to use the standard eclipse workbench
//		if (workbenchBar != null) {
//			if (contributor.getTitle() != null) {
//				workbenchBar.setPerpspectiveLabel(ViewUIConstants.ID_PERSPECTIVE_CONTENT,
//						contributor.getTitle());
//			}
//			if (contributor.getTitleImage() != null) {
//				workbenchBar.setPerpspectiveImage(ViewUIConstants.ID_PERSPECTIVE_CONTENT,
//						contributor.getTitleImage());
//			}
//		}
		for (IViewReference ref : page.getViewReferences()) {
			IViewPart viewPart = ref.getView(false); 
			if (viewPart instanceof IContentView) {
				((IContentView) viewPart).setContentContributor(contributor);
				break;
			}
		}
	}
	
	private ContentViewUtils() {
		super();
	}
	
}
