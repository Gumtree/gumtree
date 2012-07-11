/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*******************************************************************************/
package au.gov.ansto.bragg.quokka.exp.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.gumtree.ui.internal.cli.beanshell.BeanShellCommandLineView;

import au.gov.ansto.bragg.kakadu.dom.PlotDOM;
import au.gov.ansto.bragg.quokka.exp.core.QuokkaExperiment;
import au.gov.ansto.bragg.quokka.exp.core.scanfunction.Function;

//import au.gov.ansto.bragg.kakadu.ui.plot.Plot;

/**
 * @author nxi
 *
 */
public class ExperimentPlotEditor extends EditorPart {

	Composite composite; 
//	private CTabItem plotTabItem;
//	private Plot mainPlot;
	private PlotDOM plotDom;
	private List<CTabFolder> folderList;
	private List<Composite> compositeList;
	/**
	 * 
	 */
	public ExperimentPlotEditor() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input)
	throws PartInitException {
		// TODO Auto-generated method stub
		setInput(input);
		setSite(site);

		setPartName(input.getName() + " - Experiment Plot");
		setTitleToolTip(input.getName() + " - Experiment Plot");
//		this.entryArray = ((ExperimentPlotInput) input).getEntryArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite composite) {
		// TODO Auto-generated method stub
		this.composite = composite;
		GridLayout gridLayout = new GridLayout ();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		composite.setLayout (gridLayout);

		final Color[] backgroundGradientColors = new Color[] {
				composite.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND),
				composite.getDisplay().getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW),
				composite.getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW),
				composite.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND),
		};
		final ControlListener redrawControlListener = new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}
			public void controlResized(ControlEvent e) {
				//need to be redrawn because gradient was not updated properly
				((Composite)e.getSource()).redraw();
			}
		};		final int[] backgroundGradientPrasentage = new int[] {1, 90, 100 };
		SashForm plotAndOperationPropertiesSashForm = new SashForm(composite, SWT.VERTICAL);
		plotAndOperationPropertiesSashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		folderList = new ArrayList<CTabFolder>();
		compositeList = new ArrayList<Composite>();
		for (Iterator<?> iterator = QuokkaExperiment.getFunctionList().iterator(); iterator.hasNext();) {
			CTabFolder plotTabFolder = new CTabFolder(plotAndOperationPropertiesSashForm, SWT.NONE);
			folderList.add(plotTabFolder);
			plotTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			plotTabFolder.setSimple(false);
			plotTabFolder.setTabHeight(14);
			plotTabFolder.setSelectionBackground(backgroundGradientColors, backgroundGradientPrasentage);
			plotTabFolder.addControlListener(redrawControlListener);
			Function function = (Function) iterator.next();
			CTabItem plotTabItem = new CTabItem(plotTabFolder, SWT.NONE);
			plotTabItem.setText("Plot - " + function.getUITitle());
			try {
				Composite plotComposite = new Composite(plotTabFolder, SWT.BORDER);
				compositeList.add(plotComposite);
//				composite.setLayout(new GridLayout(1, false));
				plotComposite.setLayout(new FillLayout(SWT.VERTICAL));
//				button = new Button(plotComposite, SWT.PUSH);
//				button.setText("test button");

				plotDom = function.plot(plotComposite);
				plotComposite.pack();
				plotTabItem.setControl(plotComposite);
				plotTabFolder.setSelection(0);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

//		mainPlot = new Plot(plotTabFolder, SWT.NONE);
//		plotTabItem.setControl(mainPlot);	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public static void loadExperimentPlot() throws PartInitException{
//		kuranda = visdom;
//		final IWorkbench workbench = PlatformUI.getWorkbench();
//		final IWorkbenchWindow workbenchWindow = workbench
//		.getActiveWorkbenchWindow();
//		final IWorkbenchPage activePage = workbenchWindow.getActivePage();
//		final IViewReference dataSourceViewReference = activePage.findViewReference(
//		"au.gov.ansto.bragg.kakadu.ui.editors.ExperimentPlotEditor");
//		final IViewReference[] activeViewReferences = activePage.getViewReferences();
//		for (IViewReference viewReference : activeViewReferences) {
//		if (dataSourceViewReference == viewReference) {
//		DataSourceView dataSourceView = null;
//		try {
//		dataSourceView = (DataSourceView)activePage.showView(KakaduPerspective.DATA_SOURCE_VIEW_ID);
//		} catch (PartInitException e) {
//		showErrorMessage(e.getMessage());
//		}
//		return;
//		}
//		}
//		final IWorkbenchWindow workbenchWindow = 
//			PlatformUI.getWorkbench().getActiveWorkbenchWindow();
////		workbenchWindow = Activator.getDefault().getWorkbench().getEditorRegistry().setDefaultEditor();
//		final IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
		final IWorkbenchPage workbenchPage = BeanShellCommandLineView.getWorkbenchPage();
//		IEditorDescriptor editor = workbenchWindow.getWorkbench().getEditorRegistry().getDefaultEditor("au.gov.ansto.bragg.cicada.vi.eclipse.main.CicadaMain");
//		DemoView1 view = (DemoView1) 
//		workbenchPage.showView("org.eclipse.team.ui.GenericHistoryView");
		final IEditorInput input = new ExperimentPlotInput();
		workbenchPage.getWorkbenchWindow().getWorkbench().
			getDisplay().asyncExec(new Runnable(){

				public void run() {
					// TODO Auto-generated method stub
					try {
						workbenchPage.openEditor(input, 
								"au.gov.ansto.bragg.quokka.exp.ui.ExperimentPlotEditor");
					} catch (PartInitException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			
		});
	}
	
	@Override
	public void dispose() {
		if (plotDom != null)
			plotDom.dispose();
		super.dispose();
	}
}
