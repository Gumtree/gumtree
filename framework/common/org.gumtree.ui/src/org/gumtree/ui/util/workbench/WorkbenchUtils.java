package org.gumtree.ui.util.workbench;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.ui.IWorkbenchWindow;
import org.gumtree.ui.internal.Activator;

public final class WorkbenchUtils {

	public static IStatusLineManager getStatusLineManager(IWorkbenchWindow window) {
        // XXX. Blatant hack.  Cheating by using reflect to access the window's status line manager.
        // There's no other way, currently, of contributing globally to the status line.
        // Must use reflect instead of other approaches (e.g. WorkbenchWindow.getActionBars())
        // due to incompatibilities in the internals between 2.1 and 3.0.
        IStatusLineManager statusLine = null;
        try {
            Method gslm = ApplicationWindow.class.getDeclaredMethod("getStatusLineManager", new Class[0]); //$NON-NLS-1$
            gslm.setAccessible(true);
            statusLine = (IStatusLineManager) gslm.invoke(window, new Object[0]);
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
//            getLog().log(new Status(IStatus.ERROR, getDescriptor().getUniqueIdentifier(), 0, "Can't find method WorkbenchWindow.getStatusLineManager()", e)); //$NON-NLS-1$
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
//            getLog().log(new Status(IStatus.ERROR, getDescriptor().getUniqueIdentifier(), 0, "Can't access method WorkbenchWindow.getStatusLineManager()", e)); //$NON-NLS-1$
        }
        catch (InvocationTargetException e) {
            e.printStackTrace();
//            getLog().log(new Status(IStatus.ERROR, getDescriptor().getUniqueIdentifier(), 0, "Can't invoke WorkbenchWindow.getStatusLineManager()", e)); //$NON-NLS-1$
        }
        return statusLine;
    }
	
	public static IEclipseContext getWorkbenchContext() {
		E4Processor processor = Activator.getDefault().getEclipseContext()
				.get(E4Processor.class);
		if (processor != null) {
			return processor.getEclipseContext();
		}
		return null;
	}
	
	private WorkbenchUtils() {
		super();
	}
	
}
