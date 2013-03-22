package au.gov.ansto.bragg.kowari.ui.internal;

import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "au.gov.ansto.bragg.kowari.ui";

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
		
		PlatformUI.getWorkbench().addWindowListener(new IWindowListener() {
			
			@Override
			public void windowOpened(IWorkbenchWindow window) {
				PlatformUI.getWorkbench().getThemeManager().setCurrentTheme(
						TCLRunnerPerspective.DEFAULT_PERSPECTIVE_THEME);
				window.addPerspectiveListener(new IPerspectiveListener() {
					
					@Override
					public void perspectiveChanged(IWorkbenchPage page,
							IPerspectiveDescriptor perspective, String changeId) {
					}
					
					@Override
					public void perspectiveActivated(IWorkbenchPage page,
							IPerspectiveDescriptor perspective) {
						BundleContext bundleContext = Activator.getDefault().getBundle().getBundleContext();
						ServiceReference<IThemeManager> ref = bundleContext.getServiceReference(IThemeManager.class);
						IThemeManager manager = bundleContext.getService(ref);
						IThemeEngine engine = manager.getEngineForDisplay(Display.getCurrent());
						if (perspective.getId().equals(TCLRunnerPerspective.EXPERIMENT_PERSPECTIVE_ID)) {
							PlatformUI.getWorkbench().getThemeManager().setCurrentTheme(
									TCLRunnerPerspective.EXPERIMENT_PERSPECTIVE_THEME);
							engine.setTheme("au.gov.ansto.bragg.kowari.ui.kowaritheme", true);
						} else {
							engine.setTheme("au.gov.ansto.bragg.kowari.ui.defaulttheme", true);
						}
						
					}
				});
			}
			
			@Override
			public void windowDeactivated(IWorkbenchWindow window) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowClosed(IWorkbenchWindow window) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowActivated(IWorkbenchWindow window) {
//				if (TCLRunnerPerspective.EXPERIMENT_PERSPECTIVE_ID.equals(
//						window.getActivePage().getPerspective().getId())) {
//					PlatformUI.getWorkbench().getThemeManager().setCurrentTheme(
//							TCLRunnerPerspective.EXPERIMENT_PERSPECTIVE_THEME);
//					System.err.println(TCLRunnerPerspective.EXPERIMENT_PERSPECTIVE_THEME);
//				} else {
//					PlatformUI.getWorkbench().getThemeManager().setCurrentTheme(
//							TCLRunnerPerspective.DEFAULT_PERSPECTIVE_THEME);
//					System.err.println(TCLRunnerPerspective.DEFAULT_PERSPECTIVE_THEME);
//				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
