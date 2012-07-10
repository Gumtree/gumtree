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
package au.gov.ansto.bragg.kakadu.ui.widget;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.TableTreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableTree;
import org.eclipse.swt.custom.TableTreeItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

import au.gov.ansto.bragg.kakadu.core.DataSourceManager;
import au.gov.ansto.bragg.kakadu.core.data.DataSourceFile;

/**
 * A table tree viewer class wich supports checkboxes for first tree column.
 * The widget implements some utility methods to improve flaxibility.
 * 
 * @author Danil Klimontov (dak)
 */
public class CheckboxTableTreeViewer extends TableTreeViewer implements ICheckable {
    /**
     * List of check state listeners (element type: <code>ICheckStateListener</code>).
     */
    private ListenerList checkStateListeners = new ListenerList();
    private ListenerList selectionChangedListeners = new ListenerList();

    /** 
     * The listner is used to handle auto checked mode.
     * @see #setAutoCheckedMode(boolean)
     */
    protected Listener updateCheckboxesListener = null;
	
	public CheckboxTableTreeViewer(Composite parent, int style) {
		super(parent, style);
	}

	public CheckboxTableTreeViewer(Composite parent) {
		super(parent, SWT.NONE);
	}

	public CheckboxTableTreeViewer(TableTree tree) {
		super(tree);
	}

	public void addCheckStateListener(ICheckStateListener listener) {
        checkStateListeners.add(listener);
	}

	public void removeCheckStateListener(ICheckStateListener listener) {
		checkStateListeners.remove(listener);
	}
	
    /**
     * Notifies any check state listeners that a check state changed  has been received.
     * Only listeners registered at the time this method is called are notified.
     *
     * @param event a check state changed event
     *
     * @see ICheckStateListener#checkStateChanged
     */
    private void fireCheckStateChanged(final CheckStateChangedEvent event) {
        Object[] array = checkStateListeners.getListeners();
        for (int i = 0; i < array.length; i++) {
            final ICheckStateListener l = (ICheckStateListener) array[i];
            SafeRunnable.run(new SafeRunnable() {
                public void run() {
                    l.checkStateChanged(event);
                }
            });
        }
    }

    public void handleSelect(SelectionEvent event) {
    	super.handleSelect(event); // this will change the current selection
        if (event.detail == SWT.CHECK) {
        	System.out.println("check");
        	TableTreeItem item = (TableTreeItem) event.item;
            fireCheckStateChanged(item);
        }
        else if (event.detail == 0){
        	TableTreeItem item = (TableTreeItem) event.item;
//        	fireCheckStateChanged(item);
        	fireSelectItem(item, event);
//        	System.out.println("select");
        }
//        else {
//			super.handleSelect(event);
//		}
    }

	private void fireSelectItem(TableTreeItem item, SelectionEvent event) {
		// TODO Auto-generated method stub
		
		// [Tony] [2008-12-16] This is right?  Do we still need this bit of code?
		if (item.getData() instanceof DefaultMutableTreeNode) {
			Object data = ((DefaultMutableTreeNode) item.getData()).getUserObject();
			if (data instanceof DataSourceFile){
				System.out.println("is a data file");
				for (Object listener : selectionChangedListeners.getListeners()){
					if (listener instanceof SelectionListener){
						event.data = data;
						((SelectionListener) listener).widgetSelected(event);
					}
				}
				DataSourceManager.setSelectedFile((DataSourceFile) data);
			}
		}
	}

	protected void fireCheckStateChanged(TableTreeItem item) {
		Object data = item.getData();
		if (data != null) {
		    fireCheckStateChanged(new CheckStateChangedEvent(this, data,
		            item.getChecked()));
		}
	}

    public boolean getChecked(Object element) {
        Widget widget = findItem(element);
        if (widget instanceof TableTreeItem) {
            return ((TableTreeItem) widget).getChecked();
        }
        return false;
	}

	public boolean setChecked(Object element, boolean state) {
        Assert.isNotNull(element);
        Widget widget = findItem(element);
        if (widget != null && widget instanceof TableTreeItem) {
            TableTreeItem tableTreeItem = (TableTreeItem) widget;
			setChecked(tableTreeItem, state);			
			return true;
        }
        return false;
	}

	public void setChecked(TableTreeItem tableTreeItem, boolean state) {
		if (tableTreeItem.getChecked() != state) {
			tableTreeItem.setChecked(state);
			
			fireCheckStateChanged(tableTreeItem);

			if (getAutoCheckedMode()) {
				updateParentGrayCheckedState(tableTreeItem);
			}
		}
	}
	
    /**
     * Returns a list of elements corresponding to checked table items in this
     * viewer.
     * <p>
     * This method is typically used when preserving the interesting
     * state of a viewer; <code>setCheckedElements</code> is used during the restore.
     * </p>
     *
     * @return the array of checked elements
     * @see #setCheckedElements
     */
    public Object[] getCheckedElements() {
        TableTreeItem[] children = getTableTree().getItems();
        ArrayList v = new ArrayList(children.length);
        for (int i = 0; i < children.length; i++) {
            TableTreeItem item = children[i];
            if (item.getChecked()) {
				v.add(item.getData());
			}
            
            v.addAll(getCheckedChildrenData(item));
        }
        return v.toArray();
    }

    private List getCheckedChildrenData(TableTreeItem item) {
    	ArrayList result = new ArrayList();
    	TableTreeItem[] children = item.getItems();
        for (int i = 0; i < children.length; i++) {
            TableTreeItem child = children[i];
            if (child.getChecked()) {
				result.add(child.getData());
			}
            
			if (child.getItemCount() > 0) {
				result.addAll(getCheckedChildrenData(child));
			}            
        }
    	
		return result;
	}

	/**
     * Sets which nodes are checked in this viewer.
     * The given list contains the elements that are to be checked;
     * all other nodes are to be unchecked.
     * <p>
     * This method is typically used when restoring the interesting
     * state of a viewer captured by an earlier call to <code>getCheckedElements</code>.
     * </p>
     *
     * @param elements the list of checked elements (element type: <code>Object</code>)
     * @see #getCheckedElements
     */
    public void setCheckedElements(Object[] elements) {
        assertElementsNotNull(elements);
        for (Object element : elements) {
        	setChecked(element, true);
		}

//        List<Object> elementsList = Arrays.asList(elements);
//        TableTreeItem[] items = getTableTree().getItems();
//        for (int i = 0; i < items.length; ++i) {
//            TableTreeItem item = items[i];
//            Object element = item.getData();
//            if (element != null) {
//                boolean check = elementsList.contains(element);
//                // only set if different, to avoid flicker
//                if (item.getChecked() != check) {
//                    item.setChecked(check);
//                }
//            }
//        }
    }

    /**
     * Sets the checked state for the given element and its visible
     * children in this viewer.
     * Assumes that the element has been expanded before. To enforce
     * that the item is expanded, call <code>expandToLevel</code>
     * for the element.
     *
     * @param element the element
     * @param state <code>true</code> if the item should be checked,
     *  and <code>false</code> if it should be unchecked
     * @return <code>true</code> if the checked state could be set, 
     *  and <code>false</code> otherwise
     */
    public boolean setSubtreeChecked(Object element, boolean state) {
        Widget widget = internalExpand(element, false);
        if (widget instanceof TableTreeItem) {
        	return setSubtreeChecked((TableTreeItem) widget, state);
        }
        return false;
    }

    /**
     * Sets the checked state for the given element and its visible
     * children in this viewer.
     * Sets grayed state to not grayed for the item. 
     * Assumes that the element has been expanded before. To enforce
     * that the item is expanded, call <code>expandToLevel</code>
     * for the element.
     *
     * @param item the table tree item
     * @param state <code>true</code> if the item should be checked,
     *  and <code>false</code> if it should be unchecked
     * @return <code>true</code> if the checked state could be set, 
     *  and <code>false</code> otherwise
     */
    public boolean setSubtreeChecked(TableTreeItem item, boolean state) {
	    item.setGrayed(false);
        setChecked(item, state);
        setCheckedChildren(item, state);
        return true;
    }


    /**
     * Sets to the given value the checked state for all elements in this viewer.
     *
     * @param state <code>true</code> if the element should be checked,
     *  and <code>false</code> if it should be unchecked
     *  
     *  @since 3.2
     */
	public void setAllChecked(boolean state) {
		setAllChecked(state,  getTableTree().getItems());
		
	}

	/**
	 * Set the checked state of items and thier children to state.
	 * @param state
	 * @param items
	 */
	private void setAllChecked(boolean state, TableTreeItem[] items) {
		for (int i = 0; i < items.length; i++) {
			if (items[i].getChecked() != state) {
				setChecked(items[i], state);
			}			
			TableTreeItem[] children = items[i].getItems();
			setAllChecked(state, children);
		}
	}
	
    /**
     * Sets the checked state for the children of the given item.
     *
     * @param item the item
     * @param state <code>true</code> if the item should be checked,
     *  and <code>false</code> if it should be unchecked
     */
    private void setCheckedChildren(Item item, boolean state) {
        createChildren(item);
        Item[] items = getChildren(item);
        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                Item it = items[i];
                if (it.getData() != null && (it instanceof TableTreeItem)) {
                	TableTreeItem treeItem = (TableTreeItem) it;
					setChecked(treeItem, state);
                }
            }
        }
    }

    /**
     * Sets the grayed state for the given element in this viewer.
     *
     * @param element the element
     * @param state <code>true</code> if the item should be grayed,
     *  and <code>false</code> if it should be ungrayed
     * @return <code>true</code> if the gray state could be set, 
     *  and <code>false</code> otherwise
     */
    public boolean setGrayed(Object element, boolean state) {
        Assert.isNotNull(element);
        Widget widget = internalExpand(element, false);
        if (widget instanceof TableTreeItem) {
            ((TableTreeItem) widget).setGrayed(state);
            return true;
        }
        return false;
    }

    /**
     * Check and gray the selection rather than calling both
     * setGrayed and setChecked as an optimization.
     * @param element the item being checked
     * @param state a boolean indicating selection or deselection
     * @return boolean indicating success or failure.
     */
    public boolean setGrayChecked(Object element, boolean state) {
        Assert.isNotNull(element);
        Widget widget = internalExpand(element, false);
        if (widget instanceof TableTreeItem) {
        	TableTreeItem item = (TableTreeItem) widget;
            setChecked(item, state);
            item.setGrayed(state);
            return true;
        }
        return false;
    }
    
    /**
     * Defines auto checked mode for the widget.
     * For the mode if parent item has been selected/deselected all child items 
     * will be selected/deselected automatically. 
     * Parent item become grayed if not all of its child items selected. 
     * @param autoCheckedMode true if the mode should be switched on or false otherwise.
     */
    public void setAutoCheckedMode(boolean autoCheckedMode) {
		if (autoCheckedMode && !getAutoCheckedMode()) {
			if (updateCheckboxesListener == null) {
				updateCheckboxesListener = new Listener() {
					public void handleEvent(Event event) {
						if (event.detail == SWT.CHECK) {
							TableItem tableItem = (TableItem) event.item;
							TableTreeItem item = (TableTreeItem) tableItem
									.getData("TableTreeItemID");
							boolean checked = item.getChecked();
							setSubtreeChecked(item, checked);
							updateParentGrayCheckedState(item);
						}
					}
				};
			}
			getTableTree().getTable().addListener(SWT.Selection,
					updateCheckboxesListener);
		} else if (getAutoCheckedMode()) {
			getTableTree().getTable().removeListener(SWT.Selection,
					updateCheckboxesListener);
			updateCheckboxesListener = null;
		}
    }
    
    /**
     * Gets current state of about checked mode.
     * @return true if the mode is switched on or false otherwise.
     * @see #setAutoCheckedMode(boolean)
     */
    public boolean getAutoCheckedMode() {
		return updateCheckboxesListener != null;
	}
    
    /**
     * Update parent item gray and chacked state of the item.
     * @param item item to start update 
     */
	public void updateParentGrayCheckedState(TableTreeItem item) {
		updateParentGrayCheckedState(item.getParentItem(), item.getChecked(), false);
	}
	
	/**
	 * Updates gray and checked state of the item and all its parent items.
	 * @param item the item
	 * @param checked chacked state
	 * @param grayed gray state
	 */
	protected void updateParentGrayCheckedState(TableTreeItem item, boolean checked, boolean grayed) {
	    if (item == null) {
			return;
		}
	    if (grayed) {
	        checked = true;
	    } else {
	        TableTreeItem[] items = item.getItems();
	        for (int index = 0; index < items.length; index++) {
	        	TableTreeItem child = items[index];
	            if (child.getGrayed() || checked != child.getChecked()) {
	                checked = grayed = true;
	                break;
	            }
	        }
	    }
	    setChecked(item, checked);
	    item.setGrayed(grayed);
	    updateParentGrayCheckedState(item.getParentItem(), checked, grayed);
	}

	public void addSelectionChangedListener(SelectionListener listener){
		selectionChangedListeners.add(listener);
	}
	
	public void removeSelectionChangedListener(SelectionListener listener){
		selectionChangedListeners.remove(listener);
	}
	
	
}
