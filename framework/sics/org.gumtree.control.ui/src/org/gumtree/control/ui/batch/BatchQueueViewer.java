/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.control.ui.batch;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.databinding.beans.IBeanValueProperty;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.EditorInputTransfer;
import org.gumtree.control.batch.IBatchScript;
import org.gumtree.control.batch.ResourceBasedBatchScript;
import org.gumtree.control.ui.batch.taskeditor.DNDHandler;
import org.gumtree.control.ui.viewer.InternalImage;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.forms.FormControlWidget;
import org.gumtree.ui.util.swt.IDNDHandler;
import org.gumtree.util.bean.AbstractModelObject;

public class BatchQueueViewer extends FormControlWidget {

	private IBatchManager manager;
	
	private IDNDHandler<IBatchManager> dndHandler;
	
//	private ??? openHandler;
	
	// Listener to the batch buffer queue
	private PropertyChangeListener propertyChangeListener;
	
	private TableViewer queueViewer;
	
	private IAction renameAction;
	
	private IAction deleteAction;
	
	public BatchQueueViewer(Composite parent) {
		this(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
	}
	
	public BatchQueueViewer(Composite parent, int style) {
		super(parent, style);
		// Update UI for queue
		propertyChangeListener = new PropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent event) {
				SafeUIRunner.asyncExec(new SafeRunnable() {
					public void run() throws Exception {
						if (isDisposed()) {
							return;
						}
						if (event.getPropertyName().equals("batchBufferQueue")) {
							queueViewer.refresh();
						}
					}
				});
			}
		};
		// Create UI
		createUI();
	}
	
	protected void widgetDispose() {
		if (manager != null) {
			((AbstractModelObject) getManager()).removePropertyChangeListener(propertyChangeListener);
			propertyChangeListener = null;
			manager = null;
		}
		dndHandler = null;
		queueViewer = null;
		renameAction = null;
		deleteAction = null;
	}

	public void afterParametersSet() {
		if (getManager() == null) {
			setManager(ServiceUtils.getService(IBatchManager.class));
		}
	}
	
	private void createUI() {
		this.setLayout(new FillLayout());
		
		queueViewer = new TableViewer(this, getOriginalStyle());
		ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		queueViewer.setContentProvider(contentProvider);
//		IObservableMap[] attributeMaps = BeansObservables.observeMaps(contentProvider.getKnownElements(), IBatchScript.class, new String[] { "name" });
//		IObservableMap[] attributeMaps = BeanProperties.values(new String[]{"name"}).observeEach(contentProvider.getKnownElements());
		IBeanValueProperty[] beanProps = BeanProperties.values(new String[]{"name"});
		IObservableMap[] attributeMaps = new IObservableMap[beanProps.length];
		
		for (int i = 0; i < beanProps.length; i++) {
			attributeMaps[i] = beanProps[i].observeDetail(contentProvider.getKnownElements());
		}
		
		queueViewer.setLabelProvider(new ObservableMapLabelProvider(attributeMaps) {
			public Image getColumnImage(Object element, int columnIndex) {
				if (columnIndex == 0) {
					if (element instanceof ResourceBasedBatchScript) {
						return InternalImage.FILE.getImage();
					} else {
						return InternalImage.BUFFER.getImage();
					}
				}
				return null;
			}
		});
		createContextMenu(queueViewer.getControl());
		Transfer[] transfers = new Transfer[] {
				LocalSelectionTransfer.getTransfer(), FileTransfer.getInstance(), EditorInputTransfer.getInstance() };
		queueViewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE, transfers, new DropTargetAdapter() {			
			public void drop(DropTargetEvent event) {
				getDNDHandler().handleDrop(event);
			}
		});
		
		getParent().layout(true, true);
	}
	
	private void createContextMenu(Control parent) {
		final MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		  mgr.addMenuListener(new IMenuListener() {
			  public void menuAboutToShow(IMenuManager manager) {
				  fillContextMenu(manager);
			  }
		  });
		  Menu menu = mgr.createContextMenu(parent);
		  parent.setMenu(menu);
	}
	
	private void fillContextMenu(IMenuManager manager) {
		IStructuredSelection selections = (IStructuredSelection) queueViewer.getSelection();
		
		/*********************************************************************
		 * Rename buffer action
		 *********************************************************************/
		if (renameAction == null) {
			renameAction = new Action("Rename buffer", InternalImage.TEXT_EDIT
					.getDescriptor()) {
				public void run() {
					IBatchScript buffer = (IBatchScript) ((IStructuredSelection) queueViewer
							.getSelection()).getFirstElement();
					if (buffer != null) {
						// Ask for buffer name input
						InputDialog dialog = new InputDialog(getShell(),
								"New Batch Buffer",
								"Enter new batch buffer name:", buffer
										.getName(), new IInputValidator() {
									public String isValid(String newText) {
										if (newText == null
												|| newText.length() == 0) {
											return "Buffer name is empty";
										}
										return null;
									}
								});
						if (dialog.open() == Window.CANCEL) {
							return;
						}
						buffer.setName(dialog.getValue());
					}
				}
			};
		}
		if (selections.size() == 1) {
			manager.add(renameAction);
		}
		
		/*********************************************************************
		 * Delete buffer action
		 *********************************************************************/
		if (deleteAction == null) {
			deleteAction = new Action("Delete buffer", InternalImage.DELETE
					.getDescriptor()) {
				public void run() {
					IStructuredSelection selections = (IStructuredSelection) queueViewer
							.getSelection();
					for (Object selection : selections.toList()) {
						Object buffer = (IBatchScript) selection;
						getManager().getBatchBufferQueue().remove(buffer);
					}
				}
			};
		}
		if (selections.size() >= 1) {
			manager.add(deleteAction);
		}
	}
	
	public IBatchManager getManager() {
		return manager;
	}
	
	public void setManager(IBatchManager manager) {
		if (this.manager != null) {
			((AbstractModelObject) this.manager).removePropertyChangeListener(propertyChangeListener);			
		}
		this.manager = manager;
		//Prepare UI (sync queue)
		Realm.runWithDefault(DisplayRealm.getRealm(Display.getDefault()), new Runnable() {
			public void run() {
				queueViewer.setInput(new WritableList(getManager().getBatchBufferQueue(), IBatchScript.class));
				((AbstractModelObject) getManager()).addPropertyChangeListener(propertyChangeListener);
			}
		});
	}
	
	public IDNDHandler<IBatchManager> getDNDHandler() {
		if (dndHandler == null) {
			dndHandler = new DNDHandler();
			dndHandler.setHost(getManager());
		}
		return dndHandler;
	}

	public void setDNDHandler(IDNDHandler<IBatchManager> dndHandler) {
		this.dndHandler = dndHandler;
	}
	
	public TableViewer getViewer() {
		return queueViewer;
	}
	
}
