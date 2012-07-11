package au.gov.ansto.bragg.quokka.ui.internal;

import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroPart;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.ui.service.launcher.AbstractLauncher;
import org.gumtree.ui.service.launcher.ILauncher;
import org.gumtree.ui.service.launcher.LauncherException;
import org.gumtree.ui.service.multimonitor.IMultiMonitorManager;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.workbench.ViewUIConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.nbi.ui.launchers.InstrumentDashboardLauncher;
import au.gov.ansto.bragg.quokka.ui.QuokkaUIConstants;

public class MultiSampleWorkflowLauncher extends AbstractLauncher {

	private static Logger logger = LoggerFactory.getLogger(MultiSampleWorkflowLauncher.class);
	
	private static final String ID_PERSPECTIVE_STATUS_MONITOR = "org.gumtree.dashboard.ui.rcp.statusMonitorPerspective";
	
	private static final String ID_PERSPECTIVE_WORKFLOW = "org.gumtree.workflow.ui.perspective";
	
	private static final String ID_PERSPECTIVE_ANALYSIS = "au.gov.ansto.bragg.quokka.ui.analysis";
	
	private static final String ID_WORKFLOW_MULTI_SAMPLE = "au.gov.ansto.bragg.quokka.multiSampleExperimentWorkflow";
	
	private static final String PROP_STATUS_DASHBOARD_CONFIG_FILE = "status.dashboardConfigFile";
	
	public MultiSampleWorkflowLauncher() {
		super();
	}

	public void launch() throws LauncherException {
//		try {			
			// TODO: move this logic to experiment UI manager service
			
			final IMultiMonitorManager mmManager = ServiceUtils.getService(IMultiMonitorManager.class);
			// Prepare status in screen 1 (maximised)
			mmManager.showPerspectiveOnOpenedWindow(ViewUIConstants.ID_PERSPECTIVE_CONTENT, 0, 0, true);
			ILauncher launcher = new InstrumentDashboardLauncher();
			launcher.launch();
//			mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_STATUS_MONITOR, 0, 0, true);
//			String configFile = System.getProperty(PROP_STATUS_DASHBOARD_CONFIG_FILE);
//			if (configFile != null) {
//				try {
//					IFileStore fileStore = EFS.getStore(new URI(configFile));
//					IDashboardConfig config = DashboardConfigUtils.loadDashboardConfig(fileStore.openInputStream(EFS.NONE, new NullProgressMonitor()));
//					DashboardUI.setStatusViewConfig(config);
//				} catch (Exception e) {
//					// Oops ... nothing I can do
//					logger.error("Cannot load dashboard config while setting to status monitor.", e);
//				}
//			}
			
			// Attempt to close intro
			IIntroPart introPart = PlatformUI.getWorkbench().getIntroManager().getIntro();
			PlatformUI.getWorkbench().getIntroManager().closeIntro(introPart);
			
			// Open workflow in screen 2 (maximise only for multi system)
			if (PlatformUI.getWorkbench().getWorkbenchWindowCount() < 2) {
				mmManager.openWorkbenchWindow(QuokkaUIConstants.ID_PERSPECTIVE_QUOKKA_SCAN, 1, true);
			}
			mmManager.showPerspectiveOnOpenedWindow(QuokkaUIConstants.ID_PERSPECTIVE_QUOKKA_SCAN, 1, 1, mmManager.isMultiMonitorSystem());
			
//			IWorkflowDescriptor descriptor = WorkflowUI.getWorkflowRegistry().getDescriptor(ID_WORKFLOW_MULTI_SAMPLE);
//			WorkflowUtils.openWorkflowEditor(descriptor.createWorkflow());
//			PlatformUI.getWorkbench().getActiveWorkbenchWindow().expandInforBar();

			// Add delay to open the third one (avoid editor going into the wrong place)
			SafeUIRunner.asyncExec(new SafeRunnable() {
				public void run() throws Exception {
					// Open kakadu in screen 3 (maximise only for multi system)
					if (PlatformUI.getWorkbench().getWorkbenchWindowCount() < 3) {
						mmManager.openWorkbenchWindow(1, true);
//						mmManager.openWorkbenchWindow(ID_PERSPECTIVE_ANALYSIS, 1, true);
					}
					mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_ANALYSIS, 2, 2, mmManager.isMultiMonitorSystem());
				}
			});
			
//			PlatformUI.getWorkbench().getActiveWorkbenchWindow().hideInforBar();
			
//		} catch (PartInitException e) {
//			throw new LauncherException("Cannot open workflow editor.", e);
//		} catch (WorkflowException e) {
//			throw new LauncherException("Cannot create workflow.", e);
//		}
	}

}
