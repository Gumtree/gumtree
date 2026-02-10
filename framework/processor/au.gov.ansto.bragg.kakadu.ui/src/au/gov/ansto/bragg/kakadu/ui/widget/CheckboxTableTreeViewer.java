/*******************************************************************************
 * Copyright (c) 2007, 2023 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *     Tony Lam (Bragg Institute) - custom selection handling
 *     Baha El-Kassaby (ANSTO) - migration to CheckboxTreeViewer for Eclipse 4.x
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.ui.widget;

import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;

import au.gov.ansto.bragg.kakadu.core.DataSourceManager;
import au.gov.ansto.bragg.kakadu.core.data.DataSourceFile;

/**
 * A tree viewer that supports checkboxes, replacing the deprecated TableTreeViewer.
 * This viewer provides an "auto-check" mode where checking a parent node
 * affects its children, and the parent's check state reflects the state of its children.
 * It also maintains a legacy selection notification mechanism for compatibility.
 *
 * @author Danil Klimontov (dak)
 * @author Baha El-Kassaby (baha) - Rewritten for modern SWT/JFace
 */
public class CheckboxTableTreeViewer extends CheckboxTreeViewer {

	private final ListenerList selectionChangedListeners = new ListenerList();

	/**
	 * The listener is used to handle auto checked mode.
	 * @see #setAutoCheckedMode(boolean)
	 */
	private ICheckStateListener autoCheckListener;

	public CheckboxTableTreeViewer(Composite parent, int style) {
		// Ensure CHECK style is present for CheckboxTreeViewer to work
		super(parent, style | SWT.CHECK);
		addLegacySelectionListener();
	}

	public CheckboxTableTreeViewer(Composite parent) {
		// Provide default styles
		this(parent, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
	}

	public CheckboxTableTreeViewer(Tree tree) {
		super(tree);
		addLegacySelectionListener();
	}

	/**
	 * Adds a listener to bridge JFace's ISelectionChangedListener to the
	 * legacy SWT SelectionListener for backward compatibility.
	 */
	private void addLegacySelectionListener() {
		this.addSelectionChangedListener(event -> {
			ISelection selection = event.getSelection();
			if (selection instanceof IStructuredSelection) {
				Object firstElement = ((IStructuredSelection) selection).getFirstElement();
				fireLegacySelectItem(firstElement, getTree());
			}
		});
	}

	/**
	 * Fires a legacy selection event for compatibility with older code.
	 * This part of the code is specific to the Kakadu application's data model.
	 */
	private void fireLegacySelectItem(Object element, Widget widget) {
		Object data = element;
		// The original code unwrapped a DefaultMutableTreeNode
		if (element instanceof DefaultMutableTreeNode) {
			data = ((DefaultMutableTreeNode) element).getUserObject();
		}

		if (data instanceof DataSourceFile) {
			// Create a synthetic SelectionEvent
			Event swtEvent = new Event();
			swtEvent.widget = widget;
			swtEvent.data = data;
			SelectionEvent selectionEvent = new SelectionEvent(swtEvent);

			for (Object listener : selectionChangedListeners.getListeners()) {
				if (listener instanceof SelectionListener) {
					SafeRunnable.run(new SafeRunnable() {
						@Override
						public void run() {
							((SelectionListener) listener).widgetSelected(selectionEvent);
						}
					});
				}
			}
			// Kakadu-specific logic
			DataSourceManager.setSelectedFile((DataSourceFile) data);
		}
	}

	/**
	 * Defines auto checked mode for the widget.
	 * In this mode, if a parent item is checked/unchecked, all child items
	 * will be checked/unchecked automatically.
	 * A parent item becomes grayed if not all of its child items are checked.
	 * @param autoCheckedMode true to switch the mode on, false otherwise.
	 */
	public void setAutoCheckedMode(boolean autoCheckedMode) {
		if (autoCheckedMode) {
			if (autoCheckListener == null) {
				autoCheckListener = new ICheckStateListener() {
					@Override
					public void checkStateChanged(CheckStateChangedEvent event) {
						// When an item is checked, update its children and parents
						Object element = event.getElement();
						boolean checked = event.getChecked();
						// Update children to match the new state
						setSubtreeChecked(element, checked);
						// Update parents' grayed/checked state
						updateParentGrayCheckedState(element);
					}
				};
				addCheckStateListener(autoCheckListener);
			}
		} else {
			if (autoCheckListener != null) {
				removeCheckStateListener(autoCheckListener);
				autoCheckListener = null;
			}
		}
	}

	/**
	 * Gets the current state of auto checked mode.
	 * @return true if the mode is on, false otherwise.
	 * @see #setAutoCheckedMode(boolean)
	 */
	public boolean getAutoCheckedMode() {
		return autoCheckListener != null;
	}

	/**
	 * Updates the gray and checked state of the parent of the given element.
	 * This method is typically called after an element's check state changes.
	 * @param element the element whose parent needs updating.
	 */
	public void updateParentGrayCheckedState(Object element) {
		if (!(getContentProvider() instanceof ITreeContentProvider)) {
			return;
		}
		ITreeContentProvider provider = (ITreeContentProvider) getContentProvider();
		Object parent = provider.getParent(element);
		if (parent != null) {
			updateParentState(parent);
		}
	}

	/**
	 * Recursively updates the state of a parent and its ancestors.
	 * @param parent the parent element to update.
	 */
	private void updateParentState(Object parent) {
		if (parent == null) {
			return;
		}

		ITreeContentProvider provider = (ITreeContentProvider) getContentProvider();
		Object[] children = provider.getChildren(parent);

		if (children == null || children.length == 0) {
			// No children, so it should not be grayed
			setGrayed(parent, false);
			return;
		}

		int checkedCount = 0;
		int grayedCount = 0;
		for (Object child : children) {
			if (getGrayed(child)) {
				grayedCount++;
			}
			if (getChecked(child)) {
				checkedCount++;
			}
		}

		if (grayedCount > 0 || (checkedCount > 0 && checkedCount < children.length)) {
			// Some children are checked, or some are grayed -> parent is grayed and checked
			setGrayChecked(parent, true);
		} else if (checkedCount == children.length) {
			// All children are checked -> parent is checked and not grayed
			setGrayed(parent, false);
			setChecked(parent, true);
		} else { // checkedCount == 0
			// No children are checked -> parent is unchecked and not grayed
			setGrayed(parent, false);
			setChecked(parent, false);
		}

		// Recurse to the next parent
		Object grandParent = provider.getParent(parent);
		if (grandParent != null) {
			updateParentState(grandParent);
		}
	}

	/**
	 * Check and gray the selection rather than calling both
	 * setGrayed and setChecked as an optimization.
	 * @param element the item being checked
	 * @param state a boolean indicating selection or deselection
	 * @return boolean indicating success or failure.
	 */
	public boolean setGrayChecked(Object element, boolean state) {
		return super.setGrayed(element, state) && super.setChecked(element, state);
	}

	/**
	 * Sets to the given value the checked state for all elements in this viewer.
	 *
	 * @param state <code>true</code> if the element should be checked,
	 *  and <code>false</code> if it should be unchecked
	 */
	public void setAllChecked(boolean state) {
		if (getContentProvider() instanceof ITreeContentProvider) {
			Object[] elements = ((ITreeContentProvider) getContentProvider()).getElements(getInput());
			for (Object element : elements) {
				setSubtreeChecked(element, state);
			}
		}
	}

	/**
	 * Adds a legacy SelectionListener.
	 * @param listener the listener to add.
	 */
	public void addSelectionChangedListener(SelectionListener listener) {
		selectionChangedListeners.add(listener);
	}

	/**
	 * Removes a legacy SelectionListener.
	 * @param listener the listener to remove.
	 */
	public void removeSelectionChangedListener(SelectionListener listener) {
		selectionChangedListeners.remove(listener);
	}

	// The following methods from the original class are now provided by CheckboxTreeViewer
	// and do not need to be overridden unless custom behavior is required:
	// - addCheckStateListener(ICheckStateListener)
	// - removeCheckStateListener(ICheckStateListener)
	// - getChecked(Object)
	// - setChecked(Object, boolean)
	// - getCheckedElements()
	// - setCheckedElements(Object[])
	// - setSubtreeChecked(Object, boolean)
	// - setGrayed(Object, boolean)
}
