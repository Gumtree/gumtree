/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.ui.views;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.vis.mask.AbstractMask;

import au.gov.ansto.bragg.cicada.core.Exporter;
import au.gov.ansto.bragg.cicada.core.Format;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.kakadu.core.AlgorithmTask;
import au.gov.ansto.bragg.kakadu.core.UIAlgorithmManager;
import au.gov.ansto.bragg.kakadu.ui.Activator;
import au.gov.ansto.bragg.kakadu.ui.ProjectManager;
import au.gov.ansto.bragg.kakadu.ui.editors.AlgorithmTaskEditor;
import au.gov.ansto.bragg.kakadu.ui.region.RegionParameter;
import au.gov.ansto.bragg.kakadu.ui.region.RegionParameterManager;
import au.gov.ansto.bragg.kakadu.ui.util.DisplayManager;
import au.gov.ansto.bragg.kakadu.ui.util.Util;
import au.gov.ansto.bragg.kakadu.ui.views.mask.MaskPropertiesComposite;
import au.gov.ansto.bragg.kakadu.ui.views.mask.MaskPropertiesComposite.ActionStateChangeListener;

/**
 * The view for Mask Properties managing.
 * 
 * @author Danil Klimontov (dak)
 */
public class MaskPropertiesView extends ViewPart {

	private MaskPropertiesComposite maskPropertiesComposite;
	private RegionParameterManager currentRegionManager;
	private Composite parent;
	
	//Associate the view with active AlgorithmTaskEditor
	final IPartListener2 catchAlgorithmTaskEditorPartListener = new IPartListener2(){


		public void partActivated(IWorkbenchPartReference partRef) {
			final IWorkbenchPart part = partRef.getPart(false);
			if (part != null && part instanceof AlgorithmTaskEditor) {
				final AlgorithmTask algorithmTask = ((AlgorithmTaskEditor) part).getAlgorithmTask();
				final RegionParameterManager regionManager = (RegionParameterManager) 
				algorithmTask.getRegionParameterManager();
				if (currentRegionManager != regionManager) {
					setRegionManager(regionManager);
				}
			}
		}

		public void partBroughtToTop(IWorkbenchPartReference partRef) {
		}

		public void partClosed(IWorkbenchPartReference partRef) {
			final IWorkbenchPart part = partRef.getPart(false);
			if (part != null && part instanceof AlgorithmTaskEditor) {
				setRegionManager(null);
			}
		}

		public void partDeactivated(IWorkbenchPartReference partRef) {
		}

		public void partHidden(IWorkbenchPartReference partRef) {
		}

		public void partInputChanged(IWorkbenchPartReference partRef) {
		}

		public void partOpened(IWorkbenchPartReference partRef) {
		}

		public void partVisible(IWorkbenchPartReference partRef) {
		}
		
	};
	private Action createNewMaskAction;
	private Action removeMaskAction;
	private Action removeAllMasksAction;
	private Action saveRegionSetAction;
	private Action loadRegionSetAction;
	private Action saveThisRegionAction;

	public MaskPropertiesView() {
	}

	public void createPartControl(Composite parent) {
		this.parent = parent;

		initialise();
	}
	private void initialise() {
		parent.setLayout(new FillLayout());
		maskPropertiesComposite = new MaskPropertiesComposite(parent, SWT.NONE);
		
		initListeners();
		
		initActions();
		
		final AlgorithmTask currentAlgorithmTask = ProjectManager.getCurrentAlgorithmTask();
		setRegionManager(currentAlgorithmTask != null ? 
				(RegionParameterManager) currentAlgorithmTask.getRegionParameterManager() : null);
	}

	/**
	 * Initialize listeners for the view.
	 */
	private void initListeners() {
		//the PartListener listener must be added after active workbenchPage was created.
		//to ensure it the operation should be performed in asyncExec block  
		DisplayManager.getDefault().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow workbenchWindow = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
				final IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
				workbenchPage.addPartListener(catchAlgorithmTaskEditorPartListener);
			}
		});
		
		maskPropertiesComposite.addActionStateChangeListener(new ActionStateChangeListener() {
			public void updateActionsState() {
				final RegionParameter currentParameter = maskPropertiesComposite.getCurrentParameterRegionManager();
				createNewMaskAction.setEnabled(currentParameter != null);

				final AbstractMask selectedRegion = maskPropertiesComposite.getSelectedRegion();
				saveRegionSetAction.setEnabled(currentParameter != null 
						&& currentParameter.getMaskList().size() > 0);
				loadRegionSetAction.setEnabled(currentParameter != null);
				removeMaskAction.setEnabled(selectedRegion != null);
				
				removeAllMasksAction.setEnabled(currentParameter != null && currentParameter.getMaskList().size() > 0);
			}
		});
		
	}

	/**
	 * 
	 */
	private void initActions() {
		createNewMaskAction = new Action() {
			public void run() {
				createNewMask();
			}
		};
		createNewMaskAction.setText("New Mask");
		createNewMaskAction.setToolTipText("Create new mask");
		createNewMaskAction.setImageDescriptor(Activator.getImageDescriptor("icons/add_item.gif"));
		createNewMaskAction.setDisabledImageDescriptor(Activator.getImageDescriptor("icons/add_item_dis.gif"));
		createNewMaskAction.setEnabled(false);

		removeMaskAction = new Action() {
			public void run() {
				removeMask();
			}
		};

		removeMaskAction.setText("Remove Mask");
		removeMaskAction.setToolTipText("Remove mask");
		removeMaskAction.setImageDescriptor(Activator.getImageDescriptor("icons/rem_item.gif"));
		removeMaskAction.setDisabledImageDescriptor(Activator.getImageDescriptor("icons/rem_item_dis.gif"));
		removeMaskAction.setEnabled(false);
		
		removeAllMasksAction = new Action() {
			public void run() {
				removeAllMasks();
			}
		};
		removeAllMasksAction.setText("Remove All Masks");
		removeAllMasksAction.setToolTipText("Remove all masks");
		removeAllMasksAction.setImageDescriptor(Activator.getImageDescriptor("icons/rem_all_items.gif"));
		removeAllMasksAction.setDisabledImageDescriptor(Activator.getImageDescriptor("icons/rem_all_items_dis.gif"));
		removeAllMasksAction.setEnabled(false);
		
		saveRegionSetAction = new Action(){
			public void run(){
				try {
					IGroup regionSet = RegionParameterManager.createRegionSet(
							maskPropertiesComposite.getRegionList());
					saveRegionToURI(regionSet);
				} catch (Exception e) {
					// TODO: handle exception
					Util.handleException(parent.getShell(), e);
				}
			}
		};
		saveRegionSetAction.setText("Save All Regions");
		saveRegionSetAction.setToolTipText("Save all regions to an XML file");
		saveRegionSetAction.setImageDescriptor(Activator.getImageDescriptor("icons/saveas_edit.gif"));
		saveRegionSetAction.setDisabledImageDescriptor(Activator.getImageDescriptor("icons/saveas_edit_dis.gif"));
		saveRegionSetAction.setEnabled(false);
		
		saveThisRegionAction = new Action(){
			public void run(){
					try {
						List<AbstractMask> regionList = new ArrayList<AbstractMask>();
						regionList.add(maskPropertiesComposite.getSelectedRegion());
						IGroup regionSet = RegionParameterManager.createRegionSet(regionList);
						saveRegionToURI(regionSet);
					} catch (Exception e) {
						// TODO: handle exception
						Util.handleException(parent.getShell(), e);
					}
			}
		};
		saveThisRegionAction.setText("Save Region");
		saveThisRegionAction.setToolTipText("Save region to an XML file");
		saveThisRegionAction.setImageDescriptor(Activator.getImageDescriptor("icons/save.gif"));
		
		loadRegionSetAction = new Action(){
			public void run(){
				URI uri = null;
				IGroup regionSet = null;
				try {
					String filename = au.gov.ansto.bragg.kakadu.ui.util.Util.getFilenameFromShell(
							parent.getShell(), "*.xml", "XML Region Set Container");
					if (filename == null)
						return;
					uri = ConverterLib.path2URI(filename);
					regionSet = UIAlgorithmManager.getAlgorithmManager().loadDataFromFile(uri);
				} catch (Exception e) {
					// TODO: handle exception
					Util.handleException(parent.getShell(), e);
				}
				maskPropertiesComposite.addNewMask(regionSet);
			}
		};
		loadRegionSetAction.setText("Load Saved Regions");
		loadRegionSetAction.setToolTipText("Load regions from an XML file");
		loadRegionSetAction.setImageDescriptor(Activator.getImageDescriptor("icons/opentype.gif"));
		loadRegionSetAction.setDisabledImageDescriptor(Activator.getImageDescriptor("icons/opentype_dis.gif"));
		loadRegionSetAction.setEnabled(false);
		
		contributeToActionBars();
	}
	
	private void saveRegionToURI(IGroup regionSet){
		URI uri = null;
		try {
			String filename = au.gov.ansto.bragg.kakadu.ui.util.Util.getSaveFilenameFromShell(
					parent.getShell(), new String[]{"*.xml"}, 
					new String[]{"XML Region Set Container"});
			if (filename == null) 
				return;
			if (! filename.endsWith(".xml"))
				filename += ".xml";
			uri = ConverterLib.path2URI(filename);
		} catch (Exception e) {
			// TODO: handle exception
			Util.handleException(parent.getShell(), e);
		}
		Exporter exporter = null;
		try{
			exporter = UIAlgorithmManager.getAlgorithmManager().getExporter(Format.xml);
//				new Exporter_(new Formater_(Format.xml));
			exporter.signalExport(regionSet, uri);
		}catch (Exception e) {
			// TODO: handle exception
			Util.handleException(parent.getShell(), e);
		}
	}
	
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillDropDownMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(maskPropertiesComposite.getControl());
		maskPropertiesComposite.getControl().setMenu(menu);
//		getSite().registerContextMenu(menuMgr, maskPropertiesComposite);
		
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
		hookContextMenu();
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(createNewMaskAction);
		manager.add(loadRegionSetAction);
		manager.add(saveRegionSetAction);
		manager.add(removeMaskAction);
		manager.add(new Separator());
		manager.add(removeAllMasksAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(createNewMaskAction);
		manager.add(loadRegionSetAction);
		manager.add(saveRegionSetAction);
		manager.add(removeMaskAction);
		manager.add(removeAllMasksAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillDropDownMenu(IMenuManager manager) {
		manager.add(saveThisRegionAction);
		manager.add(removeMaskAction);
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(createNewMaskAction);
		manager.add(loadRegionSetAction);
		manager.add(saveRegionSetAction);
		manager.add(removeMaskAction);
		manager.add(new Separator());
		manager.add(removeAllMasksAction);
	}


	protected void createNewMask() {
		maskPropertiesComposite.createNewMask();
	}

	protected void removeMask() {
		maskPropertiesComposite.removeSelectedMask();
	}
	
	protected void removeAllMasks() {
		maskPropertiesComposite.removeAllMasks();
	}


	public void setFocus() {
		maskPropertiesComposite.setFocus();
	}
	
	public void dispose() {
		super.dispose();

		IWorkbenchWindow workbenchWindow = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
		final IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
		
		if (workbenchPage != null) {
			workbenchPage.removePartListener(catchAlgorithmTaskEditorPartListener);
		}
}


	public void setRegionParameter(RegionParameter regionParameter) {
		maskPropertiesComposite.setRegionParameter(regionParameter);
		
	}

	public void setRegionManager(RegionParameterManager regionManager) {
		currentRegionManager = regionManager;
		maskPropertiesComposite.setRegionManager(regionManager);

	}
}
