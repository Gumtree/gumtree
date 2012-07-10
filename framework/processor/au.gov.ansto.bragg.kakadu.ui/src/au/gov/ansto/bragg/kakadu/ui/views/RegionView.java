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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.vis.mask.AbstractMask;

import au.gov.ansto.bragg.kakadu.ui.region.RegionEventListener;

/**
 * The view for Region UI.
 * @author Danil Klimontov (dak)
 */
public class RegionView extends ViewPart {

	private RegionEventListener regionListener = new RegionEventListener() {
		public void maskAdded(AbstractMask region) {
			viewer.refresh();
		}
		public void maskRemoved(AbstractMask region) {
			viewer.refresh();
		}
		public void maskUpdated(AbstractMask region) {
			viewer.refresh();
		}
	};

	public class RegionLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			AbstractMask uiRegion = (AbstractMask) element;
			switch (columnIndex) {
			case 0:
				return uiRegion.getName();
			default:
				return "";
			}
		}
	}

	public class RegionListContentProvider implements IStructuredContentProvider {

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
		public Object[] getElements(Object inputElement) {
//			return regionManager.getRegions().toArray();
			return null;
		}
	}

	private TableViewer viewer;
	private TableColumn tableColumnName;
//	private ParameterRegionManager regionManager = new ParameterRegionManager();

	/**
	 * 
	 */
	public RegionView() {
	}

	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		
		viewer = new TableViewer(parent, SWT.SINGLE
			| SWT.H_SCROLL
			| SWT.V_SCROLL
			| SWT.FULL_SELECTION);
		
		tableColumnName = new TableColumn(viewer.getTable(), SWT.NONE);
		tableColumnName.setText("Name");
		tableColumnName.setWidth(60);
		tableColumnName.setMoveable(true);

		viewer.setContentProvider(new RegionListContentProvider());
		viewer.setLabelProvider(new RegionLabelProvider());
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);

		viewer.setInput(getViewSite());
		
		initListeners();

	}

	private void initListeners() {
//		regionManager.addRegionListener(regionListener);
	}

	public void setFocus() {
	}

	public void dispose() {
		super.dispose();
//		regionManager.removeRegionListener(regionListener);
	}

	
}
