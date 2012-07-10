/**
 * 
 */
package org.gumtree.data.ui.part;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.data.ui.viewers.PlotViewer;

/**
 * @author nxi
 *
 */
public class PlotView extends ViewPart {

	private final static String VIEW_ID = "org.gumtree.data.ui.PlotView";
	private static PlotView instance;
	private static int index;
	private PlotViewer viewer;

	/**
	 * 
	 */
	public PlotView() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		GridLayoutFactory.fillDefaults().spacing(0, 0).margins(0, 0).applyTo(parent);
		viewer = new PlotViewer(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).indent(0, 0).applyTo(viewer);
	}

	public static PlotView getNewInstance() {
//		if (instance != null && !instance.getViewer().isDisposed()) {
//			return instance;
//		}
		instance = null;
		Display.getDefault().asyncExec(new Runnable(){

			public void run() {
				try{
					IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					final IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
					
					instance = (PlotView) workbenchPage.showView(
								VIEW_ID, String.valueOf(index++), IWorkbenchPage.VIEW_VISIBLE);
				}catch (Exception e) {
					e.printStackTrace();
				}
			}});
		int sleepTime = 0;
		while((instance == null || instance.getViewer() == null) && sleepTime < 5000){
			try {
				Thread.sleep(200);
				sleepTime += 200;
			} catch (Exception e) {
				throw new NullPointerException("The view is not available");
			}
		}
		return instance;
		
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
	}

	public PlotViewer getViewer() {
		return viewer;
	}

	public void setViewer(PlotViewer viewer) {
		this.viewer = viewer;
	}

	public void update() {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				getViewer().update();
				getViewer().layout(getViewer().getChildren());
				getViewer().redraw();
			}
		});
	}

	public static void setCurrentIndex(int i) {
		index = i;
	}
	
	@Override
	public void dispose() {
		viewer.dispose();
		super.dispose();
	}
	
	
}
