package au.gov.ansto.bragg.kakadu.ui;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.cicada.core.Algorithm;
import au.gov.ansto.bragg.cicada.core.exception.ConfigurationException;
import au.gov.ansto.bragg.cicada.core.exception.LoadAlgorithmFileFailedException;
import au.gov.ansto.bragg.cicada.core.exception.NoneAlgorithmException;
import au.gov.ansto.bragg.cicada.dom.core.CicadaDOM;
import au.gov.ansto.bragg.kakadu.ui.views.DataSourceView;

public final class KakaduUI {

	private static final Logger logger = LoggerFactory.getLogger(KakaduUI.class);
	
	private static volatile Boolean perspectiveShowed = false;
	
	private static volatile Boolean algorithmLoaded = false;
	
	private static volatile Boolean algorithmRun = false;
	
	private static volatile Boolean dataSourceLoaded = false;
	
	private static volatile CicadaDOM cicadaDOM;
	
	public static void openKakaduPerspective() {
		synchronized (perspectiveShowed) {
			perspectiveShowed = false;
			SafeUIRunner.asyncExec(new SafeRunnable() {
				public void run() throws Exception {
					IWorkbench workbench = PlatformUI.getWorkbench();
					IPerspectiveDescriptor persp = workbench
							.getPerspectiveRegistry().findPerspectiveWithId(
									KakaduPerspective.KAKADU_PERSPECTIVE_ID);
					workbench.getActiveWorkbenchWindow().getActivePage()
							.setPerspective(persp);
					perspectiveShowed = true;
				}
				public void handleException(Throwable e) {
					perspectiveShowed = true;
				}
			});
			LoopRunner.run(new ILoopExitCondition() {
				public boolean getExitCondition() {
					return perspectiveShowed;
				}
			});
		}
	}
	
	public static Algorithm loadAlgorithm(String algorithmName) throws NoneAlgorithmException {
		Algorithm algorithm = getCicadaDOM().loadAlgorithm(algorithmName);
		loadAlgorithm(algorithm);
		return algorithm;
	}
	
	public static void loadAlgorithm(final Algorithm algorithm) {
		synchronized (algorithmLoaded) {
			algorithmLoaded = false;
			SafeUIRunner.asyncExec(new SafeRunnable() {
				public void run() throws Exception {
					IWorkbench workbench = PlatformUI.getWorkbench();
					IPerspectiveDescriptor persp = workbench
							.getPerspectiveRegistry().findPerspectiveWithId(
									KakaduPerspective.KAKADU_PERSPECTIVE_ID);
					List<IPerspectiveDescriptor> openedPersps = Arrays
							.asList(workbench.getActiveWorkbenchWindow()
									.getActivePage().getOpenPerspectives());
					if (!openedPersps.contains(persp)) {
						openKakaduPerspective();
					}
					ProjectManager.loadAlgorithmOnWindow(algorithm, persp);
					algorithmLoaded = true;
				}
				public void handleException(Throwable e) {
					algorithmLoaded = true;
				}
			});
			LoopRunner.run(new ILoopExitCondition() {
				public boolean getExitCondition() {
					return algorithmLoaded;
				}
			});
		}
	}
	
	public static Algorithm runAlgorithm(String algorithmName) throws NoneAlgorithmException {
		Algorithm algorithm = getCicadaDOM().loadAlgorithm(algorithmName);
		runAlgorithm(algorithm);
		return algorithm;
	}
	
	public static void runAlgorithm(final Algorithm algorithm) {
		synchronized (algorithmRun) {
			algorithmRun = false;
			SafeUIRunner.asyncExec(new SafeRunnable() {
				public void run() throws Exception {
					ProjectManager.runAlgorithm(algorithm);
				}
				public void handleException(Throwable e) {
					algorithmRun = true;
				}
			});
			LoopRunner.run(new ILoopExitCondition() {
				public boolean getExitCondition() {
					return algorithmRun;
				}
			});
		}
	}
	
	public static void addDataSourceFile(final String filePath) {
		synchronized (dataSourceLoaded) {
			dataSourceLoaded = false;
			SafeUIRunner.asyncExec(new SafeRunnable() {
				public void run() throws Exception {
					DataSourceView dataSourceView = (DataSourceView) PlatformUI
							.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().showView(
									KakaduPerspective.DATA_SOURCE_VIEW_ID,
									null, IWorkbenchPage.VIEW_CREATE);
					dataSourceView.addDataSourceFile(filePath);
					dataSourceLoaded = true;
				}

				public void handleException(Throwable e) {
					dataSourceLoaded = true;
				}
			});
			LoopRunner.run(new ILoopExitCondition() {
				public boolean getExitCondition() {
					return dataSourceLoaded;
				}
			});
		}
	}
	
	private static CicadaDOM getCicadaDOM() {
		if (cicadaDOM == null) {
			synchronized (KakaduUI.class) {
				if (cicadaDOM == null) {
					try {
						cicadaDOM = new CicadaDOM();
					} catch (ConfigurationException e) {
						logger.error("Failed to create cicada dom", e);
					} catch (LoadAlgorithmFileFailedException e) {
						logger.error("Failed to create cicada dom", e);
					}
				}
			}
		}
		return cicadaDOM;
	}
	
	private KakaduUI() {
		super();
	}
	
}
