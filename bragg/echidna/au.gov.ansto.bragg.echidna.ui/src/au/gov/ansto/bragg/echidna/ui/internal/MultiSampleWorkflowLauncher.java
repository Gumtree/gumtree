package au.gov.ansto.bragg.echidna.ui.internal;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroPart;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.ui.service.launcher.AbstractLauncher;
import org.gumtree.ui.service.launcher.LauncherException;
import org.gumtree.ui.service.multimonitor.IMultiMonitorManager;
import org.gumtree.workflow.ui.WorkflowException;
import org.gumtree.workflow.ui.util.IWorkflowDescriptor;
import org.gumtree.workflow.ui.util.WorkflowUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiSampleWorkflowLauncher extends AbstractLauncher {

	private static Logger logger = LoggerFactory.getLogger(MultiSampleWorkflowLauncher.class);
	
	private static final String ID_PERSPECTIVE_STATUS_MONITOR = "org.gumtree.dashboard.ui.rcp.statusMonitorPerspective";
	
	private static final String ID_PERSPECTIVE_WORKFLOW = "org.gumtree.workflow.ui.perspective";
	
	private static final String ID_PERSPECTIVE_KAKADU = "au.gov.ansto.bragg.kakadu.ui.KakaduPerspective";
	
	private static final String ID_WORKFLOW_MULTI_SAMPLE = "au.gov.ansto.bragg.echidna.multiSampleExperimentWorkflow";
	
	private static final String PROP_STATUS_DASHBOARD_CONFIG_FILE = "status.dashboardConfigFile";
	
	public MultiSampleWorkflowLauncher() {
		super();
	}

	public void launch() throws LauncherException {
		try {			
			// TODO: move this logic to experiment UI manager service
			
			IMultiMonitorManager mmManager = ServiceUtils.getService(IMultiMonitorManager.class);
			// Prepare status in screen 1 (maximised)
			mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_STATUS_MONITOR, 0, 0, true);
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
				mmManager.openWorkbenchWindow(ID_PERSPECTIVE_WORKFLOW, 1, true);
			}
			mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_WORKFLOW, 1, 1, mmManager.isMultiMonitorSystem());
			
			IWorkflowDescriptor descriptor = WorkflowUI.getWorkflowRegistry().getDescriptor(ID_WORKFLOW_MULTI_SAMPLE);
			WorkflowUI.openWorkflowEditor(descriptor.createWorkflow());
//			PlatformUI.getWorkbench().getActiveWorkbenchWindow().expandInforBar();

			// Open kakadu in screen 3 (maximise only for multi system)
			if (PlatformUI.getWorkbench().getWorkbenchWindowCount() < 3) {
				mmManager.openWorkbenchWindow(ID_PERSPECTIVE_KAKADU, 1, true);
			}
			mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_KAKADU, 2, 2, mmManager.isMultiMonitorSystem());
//			PlatformUI.getWorkbench().getActiveWorkbenchWindow().hideInforBar();
			
		} catch (PartInitException e) {
			throw new LauncherException("Cannot open workflow editor.", e);
		} catch (WorkflowException e) {
			throw new LauncherException("Cannot create workflow.", e);
		}
	}

}
