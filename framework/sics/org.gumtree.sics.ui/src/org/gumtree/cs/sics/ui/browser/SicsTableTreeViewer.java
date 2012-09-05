package org.gumtree.cs.sics.ui.browser;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.widgets.swt.ExtendedComposite;

public class SicsTableTreeViewer extends ExtendedComposite implements
		ISicsTableTreeViewer {

	private TreeViewer treeViewer;

	private ISicsTreeNode root;

	public SicsTableTreeViewer(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout());
		treeViewer = new TreeViewer(this);
		treeViewer.setContentProvider(new SicsTableTreeViewerContentProvider());
		treeViewer.setLabelProvider(new SicsTableTreeViewerLabelProvider());

		treeViewer.getTree().setLinesVisible(true);
		treeViewer.getTree().setHeaderVisible(true);
	}

	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	@Override
	public ISicsTreeNode getRoot() {
		return root;
	}

	@Override
	public void setRoot(ISicsTreeNode root) {
		this.root = root;
		treeViewer.setInput(root);
	}

	@Override
	protected void disposeWidget() {
		treeViewer = null;
		root = null;
	}

}
