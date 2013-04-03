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

package org.gumtree.gumnix.sics.batch.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.gumtree.core.object.ObjectCreateException;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.gumnix.sics.batch.ui.internal.InternalImage;
import org.gumtree.gumnix.sics.batch.ui.registry.ICommandRegistry;
import org.gumtree.ui.util.jface.ITreeNode;
import org.gumtree.ui.util.jface.TreeContentProvider;
import org.gumtree.ui.util.jface.TreeLabelProvider;
import org.gumtree.ui.util.jface.TreeNode;
import org.gumtree.workflow.ui.util.ITaskDescriptor;
import org.gumtree.workflow.ui.util.WorkflowUtils;
import org.gumtree.workflow.ui.viewer2.AbstractWorkflowViewerComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VisualBatchBufferToolViewer extends AbstractWorkflowViewerComponent {

	private static final Logger logger = LoggerFactory.getLogger(VisualBatchBufferToolViewer.class);
	private TreeNode commandTreeNode;
	private TreeNode scientistFolder;
	private TreeNode userFolder;
	
	public VisualBatchBufferToolViewer(Composite parent, int style) {
		super(parent, style);
	}

	protected void componentDispose() {
	}

	protected void createUI() {
		GridLayoutFactory.swtDefaults().applyTo(this);

		Group toolGroup = new Group(this, SWT.NONE);
		toolGroup.setText("Task Library");
		toolGroup.setLayout(new FillLayout());
		getToolkit().adapt(toolGroup);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(toolGroup);
		
		final TreeViewer toolViewer = new TreeViewer(toolGroup, SWT.NONE);
		toolViewer.setContentProvider(new TreeContentProvider() {
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof ICommandRegistry) {
					commandTreeNode = new CommandTreeNode((ICommandRegistry) inputElement, toolViewer);
					scientistFolder = new FolderTreeNode("Instrument Scientist's Tasks", toolViewer);
					userFolder = new FolderTreeNode("User Defined Tasks", toolViewer);
//					return new Object[] { commandTreeNode, scientistFolder, userFolder};
//					return new Object[] { commandTreeNode};
					return commandTreeNode.getChildren();
				} else {
					return new Object[0];
				}
			}
		});
		toolViewer.setLabelProvider(new TreeLabelProvider());
		toolViewer.setInput(ServiceUtils.getService(ICommandRegistry.class));
		toolViewer.addDragSupport(DND.DROP_MOVE,
				new Transfer[] { LocalSelectionTransfer.getTransfer() },
				new DragSourceAdapter() {
					public void dragFinished(DragSourceEvent event) {
						LocalSelectionTransfer.getTransfer().setSelection(null);
					}
					public void dragSetData(DragSourceEvent event) {
						if (LocalSelectionTransfer.getTransfer().isSupportedType(event.dataType)) {
							LocalSelectionTransfer.getTransfer().setSelection(toolViewer.getSelection());
						}
					}
			
		});
		toolViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				ITreeNode node = (ITreeNode) ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (!(node.getOriginalObject() instanceof ITaskDescriptor)) {
					return;
				}
				ITaskDescriptor desc = (ITaskDescriptor) node.getOriginalObject();
				try {
					WorkflowUtils.addNewTask(getWorkflow(), desc);
				} catch (ObjectCreateException e) {
					logger.error("Failed to add from an task descriptor.", e);
				}
			}
		});
		toolViewer.setExpandedState(commandTreeNode, true);
		getParent().layout(true, true);
	}
	
	protected void refreshUI() {
		if (isDisposed()) {
			return;
		}
		for (Control child : getChildren()) {
			child.dispose();
		}
		createUI();
	}
	
	/*************************************************************************
	 * Helper classes for representing objects as tree nodes
	 *************************************************************************/
	
	private class CommandTreeNode extends TreeNode {
		public CommandTreeNode(ICommandRegistry commandRegistry, StructuredViewer viewer) {
			super(commandRegistry, viewer);
		}
		public ICommandRegistry getRegistry() {
			return (ICommandRegistry) getOriginalObject();
		}
		public Image getImage() {
			return InternalImage.LIBRARY.getImage();
		}
		public String getText() {
			return "Commands";
		}
		public ITreeNode[] getChildren() {
			List<ITreeNode> nodes = new ArrayList<ITreeNode>();
			for (ITaskDescriptor desc : getRegistry().getCommandDescriptors()) {
				nodes.add(new TaskTreeNode(desc, getViewer()));
			}
			return nodes.toArray(new ITreeNode[nodes.size()]);
		}
	}
	
	private class FolderTreeNode extends TreeNode {
		public FolderTreeNode(Object originalObject, StructuredViewer viewer) {
			super(originalObject, viewer);
		}
		public Image getImage() {
			return InternalImage.LOAD.getImage();
		}
		public String getText() {
			return getOriginalObject().toString();
		}
	}
	
	private class TaskTreeNode extends TreeNode {
		public TaskTreeNode(ITaskDescriptor desc, StructuredViewer viewer) {
			super(desc, viewer);
		}
		public ITaskDescriptor getDescriptor() {
			return (ITaskDescriptor) getOriginalObject();
		}
		public Image getImage() {
			return getDescriptor().getIcon();
		}
		public String getText() {
			return getDescriptor().getLabel();
		}
	}
	
}
