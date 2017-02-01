package au.gov.ansto.bragg.nbi.server.internal;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.gumtree.util.eclipse.EclipseUtils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import au.gov.ansto.bragg.nbi.server.catalog.CatalogCollectingService;

public class Activator implements BundleActivator {

	private static BundleContext context;
	
	private static Activator instance;

	private static CatalogCollectingService catalogCollectingService;
	
	private IEclipseContext eclipseContext;
	
	public static final String PLUGIN_ID = "au.gov.ansto.bragg.nbi.server";
			
	public static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		instance = this;
		scheduleDelayedTask();
//		catalogCollectingService = new CatalogCollectingService();
	}

	private void scheduleDelayedTask(){
//		final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
//		executor.schedule(new Runnable() {
//		  @Override
//		  public void run() {
//			  catalogCollectingService = new CatalogCollectingService();
//		  }
//		}, 1, TimeUnit.MINUTES);
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
				}
				catalogCollectingService = new CatalogCollectingService();
			}
		});
		thread.start();
	};
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		if (eclipseContext != null) {
			eclipseContext.dispose();
			eclipseContext = null;
		}
		instance = null;
		catalogCollectingService.dispose();
		Activator.context = null;
	}

	public IEclipseContext getEclipseContext() {
		if (eclipseContext == null) {
			eclipseContext = EclipseUtils.createEclipseContext(context.getBundle());
		}
		return eclipseContext;
	}
	
	public static Activator getDefault() {
		return instance;
	}
	
}
