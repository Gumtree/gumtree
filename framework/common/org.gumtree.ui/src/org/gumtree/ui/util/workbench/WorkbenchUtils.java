package org.gumtree.ui.util.workbench;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.gumtree.ui.internal.Activator;
import org.gumtree.util.collection.IMapFilter;

@SuppressWarnings("restriction")
public final class WorkbenchUtils {

	public static IStatusLineManager getStatusLineManager(
			IWorkbenchWindow window) {
		// XXX. Blatant hack. Cheating by using reflect to access the window's
		// status line manager.
		// There's no other way, currently, of contributing globally to the
		// status line.
		// Must use reflect instead of other approaches (e.g.
		// WorkbenchWindow.getActionBars())
		// due to incompatibilities in the internals between 2.1 and 3.0.
		IStatusLineManager statusLine = null;
		try {
			Method gslm = WorkbenchWindow.class.getDeclaredMethod(
					"getStatusLineManager", new Class[0]); //$NON-NLS-1$
			gslm.setAccessible(true);
			statusLine = (IStatusLineManager) gslm
					.invoke(window, new Object[0]);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			// getLog().log(new Status(IStatus.ERROR, getDescriptor().getUniqueIdentifier(), 0, "Can't find method WorkbenchWindow.getStatusLineManager()", e)); //$NON-NLS-1$
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			// getLog().log(new Status(IStatus.ERROR, getDescriptor().getUniqueIdentifier(), 0, "Can't access method WorkbenchWindow.getStatusLineManager()", e)); //$NON-NLS-1$
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			// getLog().log(new Status(IStatus.ERROR, getDescriptor().getUniqueIdentifier(), 0, "Can't invoke WorkbenchWindow.getStatusLineManager()", e)); //$NON-NLS-1$
		}
		return statusLine;
	}

	public static IEclipseContext getWorkbenchContext() {
//		E4Processor processor = Activator.getDefault().getEclipseContext()
//				.get(E4Processor.class);
//		if (processor != null) {
//			return processor.getEclipseContext();
//		}
//		return null;
		if (PlatformUI.isWorkbenchRunning()) {
	        MApplication app = (MApplication) PlatformUI.getWorkbench().getService(MApplication.class);
	        if (app != null) {
	            IEclipseContext context = app.getContext();
	            if (context != null) {
	                return context;
	            }
	        }
	    }
	    return null;
	}

	public static IWorkbenchWindow openEmptyWorkbenchWindow()
			throws WorkbenchException {
		IAdaptable input = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().getInput();
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.openWorkbenchWindow(input);
		window.getActivePage().closePerspective(
				window.getActivePage().getPerspective(), false, false);
		return window;
	}

	public static MApplication getMApplication() {
		return WorkbenchUtils.getWorkbenchContext().get(MApplication.class);
	}

	// nxi to fix the null window context problem
	public static MWindow getMWindow(IWorkbenchWindow workbenchWindow) {
		for (MWindow mWindow : getMApplication().getChildren()) {
			IWorkbenchWindow window = null;
			try {
				window = mWindow.getContext().get(
						IWorkbenchWindow.class);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			// Reference comparison
			if (workbenchWindow == window) {
				return mWindow;
			}
		}
		return null;
	}

	public static MWindow getActiveMWindow() {
		return getMWindow(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
	}

	public static MPerspectiveStack getMPerspectiveStack(MWindow mWindow) {
		return WorkbenchUtils.getFirstChild(mWindow, MPerspectiveStack.class);
	}

	public static MPerspectiveStack getActiveMPerspectiveStack() {
		MWindow mWindow = WorkbenchUtils.getActiveMWindow();
		return getMPerspectiveStack(mWindow);
	}

	public static MPerspective getActivePerspective() {
		return getActiveMPerspectiveStack().getSelectedElement();
	}

	public static MPlaceholder creatView(String viewId) {
		EPartService partService = WorkbenchUtils.getWorkbenchContext().get(
				EPartService.class);
		return partService.createSharedPart(viewId);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getFirstChild(MElementContainer<?> container,
			Class<T> childClass) {
		T result = null;
		for (MUIElement child : container.getChildren()) {
			if (childClass.isInstance(child)) {
				result = (T) child;
				break;
			} else {
				if (child instanceof MElementContainer) {
					result = getFirstChild((MElementContainer<?>) child,
							childClass);
					if (result != null) {
						break;
					}
				}
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T extends MContext> T getFirstChildWithProperty(
			MElementContainer<?> container, Class<T> childClass,
			IMapFilter<String, String> propertyFilter) {
		if (propertyFilter == null) {
			return null;
		}
		T result = null;
		for (MUIElement child : container.getChildren()) {
			if (childClass.isInstance(child)) {
				for (Entry<String, String> entry : ((MContext) child)
						.getProperties().entrySet()) {
					if (propertyFilter.accept(entry.getKey(), entry.getValue())) {
						result = (T) child;
						break;
					}
				}
			}
			if (result == null) {
				if (child instanceof MElementContainer) {
					result = getFirstChildWithProperty(
							(MElementContainer<?>) child, childClass,
							propertyFilter);
					if (result != null) {
						break;
					}
				}
			}
		}
		return result;
	}

	private WorkbenchUtils() {
		super();
	}

}
