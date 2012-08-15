/**
 * 
 */
package au.gov.ansto.bragg.nbi.ui.scripting;

import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.gumtree.ui.util.SafeUIRunner;

/**
 * @author nxi
 *
 */
public class ScriptingUIStartup implements IStartup {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IStartup#earlyStartup()
	 */
	@Override
	public void earlyStartup() {
		// [ANSTO][Tony][2012-08-15] We need to execute this in the UI thread
		// in order to retrieve the workbench window instance.
		SafeUIRunner.asyncExec(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getWorkbenchWindows()[0];
				String pid = workbenchWindow.getActivePage().getPerspective().getId();
				if (ScriptingPerspective.SCRIPTING_PERSPECTIVE_ID.equals(pid)) {
					ScriptPageRegister register = new ScriptPageRegister();
					ScriptingPerspective.registerViews(register);
				}
			}
		});
//		final IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getWorkbenchWindows()[0];
//		String pid = workbenchWindow.getActivePage().getPerspective().getId();
//		if (ScriptingPerspective.SCRIPTING_PERSPECTIVE_ID.equals(pid)) {
//			final ScriptPageRegister register = new ScriptPageRegister();
//
//			Display.getDefault().asyncExec(new Runnable(){
//
//				public void run() {
//					try{
//						ScriptingPerspective.registerViews(register);
////						IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
////						final IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
////						register.setWorkbenchPage(workbenchPage);
////
////						DataSourceView dataSourceView = (DataSourceView) workbenchPage.showView(
////								ScriptingPerspective.SCRIPT_DATASOURCE_VIEW_ID, null, IWorkbenchPage.VIEW_CREATE);
////						register.setDataSourceViewer(dataSourceView.getViewer());
////						ControlView controlView = (ControlView) workbenchPage.showView(
////								ScriptingPerspective.SCRIPT_CONTROL_VIEW_ID, null, IWorkbenchPage.VIEW_CREATE);
////						ScriptPageRegister.registPage(controlView.getViewer().getScriptRegisterID(), register);
////						register.setControlViewer(controlView.getViewer());
////
////						ConsoleView consoleView = (ConsoleView) workbenchPage.showView(
////								ScriptingPerspective.SCRIPT_CONSOLE_VIEW_ID, null, IWorkbenchPage.VIEW_CREATE);
////						register.setConsoleViewer(consoleView.getCommandLineViewer());
////
////						PlotView plot1 = (PlotView) workbenchPage.showView(
////								ScriptingPerspective.PLOT_VIEW_ID, "1", IWorkbenchPage.VIEW_CREATE);
////						register.setPlot1(plot1);
////
////						PlotView plot2 = (PlotView) workbenchPage.showView(
////								ScriptingPerspective.PLOT_VIEW_ID, "2", IWorkbenchPage.VIEW_CREATE);
////						register.setPlot2(plot2);
////
////						PlotView plot3 = (PlotView) workbenchPage.showView(
////								ScriptingPerspective.PLOT_VIEW_ID, "3", IWorkbenchPage.VIEW_CREATE);
////						register.setPlot3(plot3);
////						PlotView.setCurrentIndex(4);
////
////						workbenchPage.hideView(workbenchPage.findViewReference(ScriptingPerspective.DUMMY_VIEW_ID, "1"));
////						workbenchPage.hideView(workbenchPage.findViewReference(ScriptingPerspective.DUMMY_VIEW_ID, "2"));
////						workbenchPage.hideView(workbenchPage.findViewReference(ScriptingPerspective.DUMMY_VIEW_ID, "3"));
//
//						//											factory.getViewLayout(PLOT_VIEW_ID + ":3").setMoveable(false);
//						//											factory.getViewLayout(PLOT_VIEW_ID + ":3").setCloseable(false);
//
//					}catch (Exception e) {
//						e.printStackTrace();
//					}
//				}});
//		}
//		System.err.println(PlatformUI.getWorkbench().getWorkbenchWindowCount());
//		final IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getWorkbenchWindows()[0];
//		workbenchWindow.addPerspectiveListener(new PerspectiveAdapter() {
//			@Override
//			public void perspectiveOpened(IWorkbenchPage page,
//					IPerspectiveDescriptor perspective) {
//				super.perspectiveOpened(page, perspective);
//				final PerspectiveAdapter adapter = this;
//				final ScriptPageRegister register = new ScriptPageRegister();
//				Display.getDefault().asyncExec(new Runnable(){
//
//					public void run() {
//						try{
//							IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//							final IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
//							register.setWorkbenchPage(workbenchPage);
//							
//							DataSourceView dataSourceView = (DataSourceView) workbenchPage.showView(
//									ScriptingPerspective.SCRIPT_DATASOURCE_VIEW_ID, null, IWorkbenchPage.VIEW_CREATE);
//							register.setDataSourceViewer(dataSourceView.getViewer());
//							ControlView controlView = (ControlView) workbenchPage.showView(
//									ScriptingPerspective.SCRIPT_CONTROL_VIEW_ID, null, IWorkbenchPage.VIEW_CREATE);
//							ScriptPageRegister.registPage(controlView.getViewer().getScriptRegisterID(), register);
//							register.setControlViewer(controlView.getViewer());
//							
//							ConsoleView consoleView = (ConsoleView) workbenchPage.showView(
//									ScriptingPerspective.SCRIPT_CONSOLE_VIEW_ID, null, IWorkbenchPage.VIEW_CREATE);
//							register.setConsoleViewer(consoleView.getCommandLineViewer());
//							
//							PlotView plot1 = (PlotView) workbenchPage.showView(
//									ScriptingPerspective.PLOT_VIEW_ID, "1", IWorkbenchPage.VIEW_CREATE);
//							register.setPlot1(plot1);
//							
//							PlotView plot2 = (PlotView) workbenchPage.showView(
//									ScriptingPerspective.PLOT_VIEW_ID, "2", IWorkbenchPage.VIEW_CREATE);
//							register.setPlot2(plot2);
//							
//							PlotView plot3 = (PlotView) workbenchPage.showView(
//									ScriptingPerspective.PLOT_VIEW_ID, "3", IWorkbenchPage.VIEW_CREATE);
//							register.setPlot3(plot3);
//							PlotView.setCurrentIndex(4);
//							
//							workbenchPage.hideView(workbenchPage.findViewReference(ScriptingPerspective.DUMMY_VIEW_ID, "1"));
//							workbenchPage.hideView(workbenchPage.findViewReference(ScriptingPerspective.DUMMY_VIEW_ID, "2"));
//							workbenchPage.hideView(workbenchPage.findViewReference(ScriptingPerspective.DUMMY_VIEW_ID, "3"));
//							
////							factory.getViewLayout(PLOT_VIEW_ID + ":3").setMoveable(false);
////							factory.getViewLayout(PLOT_VIEW_ID + ":3").setCloseable(false);
//
//							workbenchWindow.removePerspectiveListener(adapter);
//							System.err.println("Perspective opened");
//						}catch (Exception e) {
//							e.printStackTrace();
//						}
//					}});
//			}
//		});
//		System.err.println("this is called");
	}

}
