package org.gumtree.data.ui.viewers;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IContainer;

public class AttributeViewer extends ExtendedComposite {

	private IContainer container;

	private TableViewer tableViewer;

	public AttributeViewer(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout());
		tableViewer = new TableViewer(this, getOriginalStyle());
		
		TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
		column.getColumn().setText("Name");
		column.getColumn().setWidth(100);
		column.getColumn().setResizable(true);
		column = new TableViewerColumn(tableViewer, SWT.NONE);
		column.getColumn().setText("Value");
		column.getColumn().setWidth(200);
		column.getColumn().setResizable(true);
		
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new AttributeViewerLabelProvider());
	}

	public void setContainer(IContainer container) {
		// Update only if input is changed
		if (container != null && (container != this.container || !container.equals(this.container))) {
			this.container = container;
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					updateViewer();
				}
			});
		}
	}

	private void updateViewer() {
		tableViewer.setInput(container.getAttributeList().toArray(
				new IAttribute[container.getAttributeList().size()]));
		tableViewer.refresh();
	}

	@Override
	protected void disposeWidget() {
		tableViewer = null;
		container = null;
	}

	public void refresh() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				updateViewer();
			}
		});
	}
	
	public void clear() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				tableViewer.setInput(null);
			}
		});
	}

}
