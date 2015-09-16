package org.gumtree.msw.ui.ktable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.wb.swt.SWTResourceManager;
import org.gumtree.msw.elements.DependencyProperty;
import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.schedule.INodeListener;
import org.gumtree.msw.schedule.ISchedulerListener;
import org.gumtree.msw.schedule.ScheduledAspect;
import org.gumtree.msw.schedule.ScheduledNode;
import org.gumtree.msw.schedule.Scheduler;
import org.gumtree.msw.schedule.execution.AcquisitionSummary;
import org.gumtree.msw.schedule.execution.IScheduleWalkerListener;
import org.gumtree.msw.schedule.execution.ParameterChangeSummary;
import org.gumtree.msw.schedule.execution.ScheduleStep;
import org.gumtree.msw.schedule.execution.ScheduleWalker;
import org.gumtree.msw.schedule.execution.Summary;
import org.gumtree.msw.ui.IModelValueConverter;
import org.gumtree.msw.ui.Resources;
import org.gumtree.msw.ui.ktable.internal.NodeElementAdapter;
import org.gumtree.msw.ui.ktable.internal.TableCellEditorListener;
import org.gumtree.msw.ui.ktable.internal.TextModifyListener;

import org.gumtree.msw.ui.ktable.ITableCellEditorListener;
import org.gumtree.msw.ui.ktable.KTable;
import org.gumtree.msw.ui.ktable.KTableCellEditor;
import org.gumtree.msw.ui.ktable.KTableCellRenderer;
import org.gumtree.msw.ui.ktable.KTableDefaultModel;
import org.gumtree.msw.ui.ktable.KTableModel;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorCheckbox;
import org.gumtree.msw.ui.ktable.editors.KTableCellEditorText2;
import org.gumtree.msw.ui.ktable.renderers.BarDiagramCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.DefaultCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.FixedCellRenderer;
import org.gumtree.msw.ui.ktable.renderers.TextCellRenderer;

public class ScheduleTableModel extends KTableDefaultModel {
	// finals
	private static final int PROGRESS_COLUMNSPAN = 4;
	private static final ARGB PROGRESS_RGB = new ARGB(255, 113, 163, 244); // new ARGB(150, 0, 65, 200);
	
	// fields
	private final SchedulerListener schedulerListener;
	private final NodeListener nodeListener;
	// nodes
	private NodeInfo rootNode;
	private final List<NodeInfo> rows;
	private final Map<ScheduledNode, NodeInfo> nodes;
	//
	private int dataColumnSpan;
	// collapsed
    private final List<Integer> vr2dr;	// visual-row -> data-row
    private final List<Integer> dr2vr;	// data-row -> visual-row 
    private final Set<NodeInfo> collapsedNodes;
	private final Map<Class<?>, RowDefinition> rowDefinitions;
    // used to update text background
    private final TextModifyListener textModifyListener;
	// table
	private final KTable table;
    private final KTableCellRenderer columnHeaderRenderer;
    private final Map<Class<?>, KTableCellRenderer> rowHeaderRenderers;
    private final KTableCellRenderer settingsCellRenderer;
    private final TextCellRenderer textCellRenderer;
    private final KTableCellRenderer collapsableCellRenderer;
    private final KTableCellRenderer checkableCellRenderer;
    private final DefaultCellRenderer clearCellRenderer;
    private final DefaultCellRenderer endOfLineCellRenderer;
    private final KTableCellEditor checkableCellEditor;
    private final DefaultCellRenderer barDiagramCellRenderer;
    private final ModifiedCellRenderer modifiedCellRenderer;
    private final KTableCellEditor readonlyTextCellEditor;
	
    // construction
    public ScheduleTableModel(final KTable table, Scheduler scheduler, ScheduleWalker walker, final Menu menu, Iterable<RowDefinition> rowDefinitions) {
        initialize();

        textModifyListener = new TextModifyListener();
        ITableCellEditorListener cellEditorListener = new TableCellEditorListener(textModifyListener);
        Set<KTableCellEditor> editors = new HashSet<>(); // editors being listened to
        
        this.rowDefinitions = new HashMap<>();
        for (RowDefinition rowDefinition : rowDefinitions) {
        	this.rowDefinitions.put(rowDefinition.getElementType(), rowDefinition);
        	
        	for (CellDefinition cellDefinition : rowDefinition.getCells())
        		if (cellDefinition instanceof IModelCellDefinition) {
        			KTableCellEditor editor = ((IModelCellDefinition)cellDefinition).getCellEditor();
        			if (TableCellEditorListener.isValidEditor(editor) && !editors.contains(editor)) {
                		editor.addListener(cellEditorListener);
                		editors.add(editor);
            		}
        		}
        }

        // final
        this.table = table;
        this.table.setNumColsVisibleInPreferredSize(getColumnCount());
        this.table.setPreferredSizeDefaultRowHeight(KTableResources.ROW_HEIGHT);

    	final int firstColumnWidth = Resources.IMAGE_SETTINGS_DROPDOWN.getBounds().width;

    	columnHeaderRenderer = new FixedCellRenderer(FixedCellRenderer.STYLE_FLAT);
    	rowHeaderRenderers = new HashMap<>();
        for (RowDefinition rowDefinition : rowDefinitions)
        	rowHeaderRenderers.put(
        			rowDefinition.getElementType(),
        			new ButtonRenderer(
        	    			table,
        	    			firstColumnWidth,
        	    			rowDefinition.getElementType(),
        	    			rowDefinition.getButtons()) {
        				@Override
        				protected int isValidColumn(int x, int y) {
        					Rectangle rect = table.getCellRect(0, 0);
        					return (0 <= x) && (x < rect.width) ? 0 : -1;
        				}
        				@Override
        				protected int isValidRow(int x, int y) {
        					int row = table.getRowForY(y);
        					if ((row > 0) && (elementType == getNodeInfo(row).getRowDefinition().getElementType()))
        						return row;

        					return -1;
        				}
        				@Override
        				protected void clicked(int col, int row, int index) {
        					if (col != 0)
        						return;
        					
        					NodeInfo nodeInfo = getNodeInfo(row);
        					RowDefinition definition = nodeInfo.getRowDefinition();
        					if (elementType == definition.getElementType()) {
        						definition.getButtons().get(index).getListener().onClicked(
        								col,
        								row,
        								nodeInfo.getNode());
        					}
        				}
        	    	});
    	settingsCellRenderer = new SettingsCellRenderer(table, firstColumnWidth, menu);
    	textCellRenderer = new TextCellRenderer(TextCellRenderer.INDICATION_FOCUS);
    	
    	Color backgroundColor = SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND);
    	collapsableCellRenderer = new CheckableCellRenderer(
    			CheckableCellRenderer.INDICATION_FOCUS | TextCellRenderer.INDICATION_COPYABLE,
    			Resources.IMAGE_EXPANDED,
    			Resources.IMAGE_COLLAPSED,
    			backgroundColor,
    			DefaultCellRenderer.COLOR_LINE_LIGHTGRAY);
    	checkableCellRenderer = new CheckableCellRenderer(
    			CheckableCellRenderer.INDICATION_FOCUS,
    			backgroundColor,
    			DefaultCellRenderer.COLOR_LINE_LIGHTGRAY);
    	clearCellRenderer = new ClearCellRenderer();
    	endOfLineCellRenderer = new EndOfLineRenderer();
    	
    	checkableCellEditor = new KTableCellEditorCheckbox();
    	readonlyTextCellEditor = new KTableCellEditorText2(SWT.READ_ONLY);
    	
    	barDiagramCellRenderer = new BarDiagramCellRenderer(SWT.None);
    	modifiedCellRenderer = new ModifiedCellRenderer();

    	// load nodes
    	nodes = new HashMap<>();
    	rows = new ArrayList<>();
    	
        vr2dr = new ArrayList<>();
        dr2vr = new ArrayList<>();
        
        collapsedNodes = new HashSet<>();
        
        schedulerListener = new SchedulerListener(this);
    	nodeListener = new NodeListener(this);
    	scheduler.addListener(schedulerListener);
    	
    	walker.addListener(new IScheduleWalkerListener() {
    		// methods
    		private Display getDisplay() {
				try {
					if (!table.isDisposed())
						return table.getDisplay();
				}
				catch (Exception e) {
				}
				return null;
    		}
			// schedule
			@Override
			public void onBeginSchedule() {
				Display display = getDisplay();
				if (display != null)
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							for (NodeInfo nodeInfo : nodes.values())
								nodeInfo.resetProgress(true);
							
							table.redraw();
						}
					});
			}
			@Override
			public void onEndSchedule() {
				Display display = getDisplay();
				if (display != null)
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							for (NodeInfo nodeInfo : nodes.values())
								nodeInfo.resetProgress(false);
							
							table.redraw();
						}
					});
			}
			// step
			@Override
			public void onBeginStep(ScheduleStep step) {
			}
			@Override
			public void onEndStep(final ScheduleStep step) {
				Display display = getDisplay();
				if (display != null)
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							NodeInfo nodeInfo = nodes.get(step.getScheduledNode());
							if (nodeInfo != null) {
								nodeInfo.completed();
								
								if (step.isEnabled())
									table.redraw();
							}
						}
					});
			}
			// parameters
			@Override
			public void onBeginChangeParameter(final ScheduleStep step) {
				Display display = getDisplay();
				if (display != null)
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							NodeInfo nodeInfo = nodes.get(step.getScheduledNode());
							if (nodeInfo != null) {
								nodeInfo.beginChangeParameter();
								table.redraw();
							}
						}
					});
			}
			@Override
			public void onEndChangeParameters(final ScheduleStep step, final ParameterChangeSummary summary) {
				Display display = getDisplay();
				if (display != null)
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							NodeInfo nodeInfo = nodes.get(step.getScheduledNode());
							if (nodeInfo != null) {
								nodeInfo.endChangeParameter(summary);
								table.redraw();
							}
						}
					});
			}
			// acquisition
			@Override
			public void onBeginPreAcquisition(final ScheduleStep step) {
				Display display = getDisplay();
				if (display != null)
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							NodeInfo nodeInfo = nodes.get(step.getScheduledNode());
							if (nodeInfo != null) {
								nodeInfo.beginPreAcquisition();
								table.redraw();
							}
						}
					});
			}
			@Override
			public void onEndPreAcquisition(final ScheduleStep step, final Summary summary) {
				Display display = getDisplay();
				if (display != null)
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							NodeInfo nodeInfo = nodes.get(step.getScheduledNode());
							if (nodeInfo != null) {
								nodeInfo.endPreAcquisition(summary);
								table.redraw();
							}
						}
					});
			}
			@Override
			public void onBeginDoAcquisition(final ScheduleStep step) {
				Display display = getDisplay();
				if (display != null)
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							NodeInfo nodeInfo = nodes.get(step.getScheduledNode());
							if (nodeInfo != null) {
								nodeInfo.beginDoAcquisition();
								table.redraw();
							}
						}
					});
			}
			@Override
			public void onEndDoAcquisition(final ScheduleStep step, final AcquisitionSummary summary) {
				Display display = getDisplay();
				if (display != null)
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							NodeInfo nodeInfo = nodes.get(step.getScheduledNode());
							if (nodeInfo != null) {
								nodeInfo.endDoAcquisition(summary);
								table.redraw();
							}
						}
					});
			}
			@Override
			public void onBeginPostAcquisition(final ScheduleStep step) {
				Display display = getDisplay();
				if (display != null)
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							NodeInfo nodeInfo = nodes.get(step.getScheduledNode());
							if (nodeInfo != null) {
								nodeInfo.beginPostAcquisition();
								table.redraw();
							}
						}
					});
			}
			@Override
			public void onEndPostAcquisition(final ScheduleStep step, final Summary summary) {
				Display display = getDisplay();
				if (display != null)
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							NodeInfo nodeInfo = nodes.get(step.getScheduledNode());
							if (nodeInfo != null) {
								nodeInfo.endPostAcquisition(summary);
								table.redraw();
							}
						}
					});
			}
		});
    }

    // nodes
    private void onNewRoot(ScheduledAspect root) {
    	if (rootNode != null)
    		removeNodeTree(null, rootNode);
    	
    	if (root != null)
    		rootNode = insertAspect(null, root);
    	else
    		rootNode = null;
    	
    	rebuildIndicesAndCounts();
    	table.redraw();
	}
	private void onNewLayer(Set<ScheduledAspect> owners) {
		for (ScheduledAspect owner : owners)
			for (ScheduledNode leafNode : owner.getLeafNodes())
				insertAspect(leafNode, owner.getLinkAt(leafNode));

    	rebuildIndicesAndCounts();
    	table.redraw();
	}
	private void onDeletedLayer(Set<ScheduledAspect> owners) {
		for (ScheduledAspect owner : owners)
			for (ScheduledNode rootNode : owner.getLeafNodes()) {
				NodeInfo rootInfo = nodes.get(rootNode);
				if (rootInfo == null)
					throw new Error("node not found");
				
				List<NodeInfo> nodeInfos = rootInfo.getNodes();
				if (nodeInfos.size() != 1)
					throw new Error("link node should have exactly one child");
				NodeInfo oldNode = nodeInfos.get(0);
				
				ScheduledAspect leafAspect = owner.getLinkAt(rootNode);
				if (leafAspect != null) {
					// new leaf aspect was a follower of deleted aspect
					ScheduledNode leafNode = leafAspect.getNode();
					NodeInfo leafInfo = nodes.get(leafNode);
					if (leafInfo == null)
						throw new Error("node not found");
					
					// forward leaf node
					leafInfo.getOwner().removeSubNode(leafInfo);
					rootInfo.addSubNode(leafInfo);
				}
				
				// remove old tree
				removeNodeTree(rootInfo, oldNode);
			}
		
    	rebuildIndicesAndCounts();
    	table.redraw();
	}
	// aspect
	private void onNewAspects(ScheduledAspect owner, Iterable<ScheduledAspect> aspects) {
		if (owner == null)
			throw new Error("new apsects can only be added to an existing aspect");
		
		NodeInfo ownerInfo = nodes.get(owner.getNode());
		if (ownerInfo == null)
			throw new Error("node not found");
		
		for (ScheduledAspect aspect : aspects) {
			// in case owner aspect hasn't been updated yet  
			loadNodeTree(ownerInfo.getOwner(), owner.getNode());

			insertAspect(owner.getLeafNode(aspect), aspect);
		}

    	rebuildIndicesAndCounts();
    	table.redraw();
	}
	private void onDeletedAspects(ScheduledAspect owner, Iterable<ScheduledAspect> aspects) {
		for (ScheduledAspect aspect : aspects) {
			NodeInfo nodeInfo = nodes.get(aspect.getNode());
			if (nodeInfo == null)
				throw new Error("node not found");

			// remove old tree
			removeNodeTree(nodeInfo.getOwner(), nodeInfo);
		}

    	rebuildIndicesAndCounts();
    	table.redraw();
	}
    // node
	private void onChangedNodeProperty(ScheduledNode owner, IDependencyProperty property) {
		// owner of property
		NodeInfo nodeInfo = nodes.get(owner);
		if (nodeInfo == null)
			throw new Error("node not found");

		if (Element.INDEX == property) {
			// owner of node (only sub nodes which have an owner can change their indices)
			NodeInfo ownerInfo = nodes.get(owner.getOwner());
			if (ownerInfo == null)
				throw new Error("node not found");
			
			// sort() returns true, if list had to be sorted
			if (ownerInfo.sort()) {
				//updateNodes(
				//		ownerInfo.getIndex(),
				//		ownerInfo.getDepth(),
				//		ownerInfo);
				
				int index = ownerInfo.getIndex() + 1;		// index of next node
				for (NodeInfo subNode : ownerInfo.getNodes())
					index += updateNodes(index, subNode.getDepth(), subNode);

				cleanCollapsedNodes();
				table.redraw();
			}
		}
		else {
			// row might be hidden
			int row = dr2vr.get(nodeInfo.getIndex());
			if (row != -1)
				// redraw complete row
				table.redraw(0, getFixedRowCount() + row, getColumnCount(), 1);
		}
	}
	private void onVisibilityChanged(ScheduledNode owner) {
		NodeInfo nodeInfo = nodes.get(owner);
		if (nodeInfo == null)
			throw new Error("node not found");
		
		cleanCollapsedNodes();
		table.redraw();
	}
	private void onAddedSubNode(ScheduledNode owner, ScheduledNode newNode) {
		NodeInfo ownerInfo = nodes.get(owner);
		if (ownerInfo == null)
			throw new Error("node not found");
		
		if (nodes.containsKey(newNode))
			return; // subNode may have been handled when new aspects were created

		loadNodeTree(ownerInfo, newNode);

    	rebuildIndicesAndCounts();
    	table.redraw();
	}
	private void onDeletedSubNode(ScheduledNode owner, ScheduledNode subNode) {
		NodeInfo ownerInfo = nodes.get(owner);
		if (ownerInfo == null)
			throw new Error("node not found");
		
		NodeInfo nodeInfo = nodes.get(subNode);
		if (nodeInfo == null)
			throw new Error("node not found");

		// remove node and node ancestors
		removeNodeTree(ownerInfo, nodeInfo);
		
    	rebuildIndicesAndCounts();
    	table.redraw();
	}
	// determine depth, index and count for each node
	private void rebuildIndicesAndCounts() {
		rows.clear();
		
		updateNodes(0, 0, rootNode);
		cleanCollapsedNodes();
		
		// determine how many columns are needed
		int columnSpan = 0;
		for (NodeInfo nodeInfo : rows)
			if (nodeInfo.hasRowDefinition())
				columnSpan = Math.max(columnSpan, nodeInfo.getDepth() + nodeInfo.getRowDefinition().getColumnSpan());
			
		if (dataColumnSpan != columnSpan) {
			dataColumnSpan = columnSpan;
			table.setNumColsVisibleInPreferredSize(getColumnCount());
		}
	}
	private int updateNodes(int index, int depth, NodeInfo node) {
		int nodeIndex = index;
		if (index == rows.size())
			rows.add(node);
		else
			rows.set(index, node);
		index++;
		
		// sort any sub nodes
		node.sort();
		
		int nextDepth = node.hasRowDefinition() ? depth + 1 : depth;
		for (NodeInfo subNode : node.getNodes())
			index += updateNodes(index, nextDepth, subNode);

		int totalCount = index - nodeIndex;
		int ancestorCount = totalCount - 1;
		node.update(depth, nodeIndex, ancestorCount);
		return totalCount;
	}
	private void cleanCollapsedNodes() {
        // rebuild map: visual-row -> data-row (hide invisible nodes)
        vr2dr.clear();
        dr2vr.clear();
        
        while (dr2vr.size() != rows.size()) {
        	NodeInfo row = rows.get(dr2vr.size());
    		if (row.getIndex() != dr2vr.size())
    			throw new Error("inconsistent row index");
    		
        	if (!row.getNode().isThisVisible()) {
        		// no connection from data-row to visual-row (including all ancestors)
        		dr2vr.add(-1);
        		for (int i = 0, n = row.getAncestorCount(); i != n; i++)
            		dr2vr.add(-1);
        	}
        	else if (row.hasRowDefinition()) {
        		dr2vr.add(vr2dr.size());
        		vr2dr.add(row.getIndex());
        	}
        	else {
        		// e.g. SampleList and ConfigurationList
        		dr2vr.add(-1);
        	}
        }

		if (collapsedNodes.isEmpty())
			return;

		// find existing and collapsed nodes
		TreeMap<Integer, NodeInfo> newCollapsedNodes = new TreeMap<>();
    	for (NodeInfo node : collapsedNodes)
    		if (nodes.containsValue(node))
    			newCollapsedNodes.put(node.getIndex(), node);
        
        // collapse nodes (top-down to accelerate process)
        collapsedNodes.clear();
    	for (NodeInfo node : newCollapsedNodes.values())
    		collapsedNode(node, false);
	}
	
	
	private NodeInfo insertAspect(ScheduledNode linkNode, ScheduledAspect aspect) {
		// after call to this function, indices have to be rebuild
		
		NodeInfo ownerInfo;
		if (linkNode == null)
			ownerInfo = null;
		else {
			ownerInfo = nodes.get(linkNode);
			if (ownerInfo == null)
				throw new Error("node info not found");
			if (ownerInfo.getNodes().size() > 1)
				throw new Error("link node should have one child at most");
		}

		ScheduledNode aspectNode = aspect.getNode();
		if (nodes.containsKey(aspectNode))
			throw new Error("node already exists");
			
		NodeInfo aspectInfo = loadNodeTree(ownerInfo, aspectNode);
		
		// ensure that followers are created
		for (ScheduledNode leafNode : aspect.getLeafNodes()) {
			ScheduledAspect leafLink = aspect.getLinkAt(leafNode);
			if (leafLink != null) {
				ScheduledNode leafLinkNode = leafLink.getNode();
				NodeInfo leafLinkInfo = nodes.get(leafLinkNode);
				
				if (leafLinkInfo == null)
					leafLinkInfo = insertAspect(leafNode, leafLink);
				else {
					if (ownerInfo != null) // may be null if leafLink was previously root aspect
						ownerInfo.removeSubNode(leafLinkInfo);

					// link gets new owner 
					nodes.get(leafNode).addSubNode(leafLinkInfo);
				}
			}
		}
		
		return aspectInfo;
	}
	private NodeInfo loadNodeTree(NodeInfo ownerInfo, ScheduledNode node) {
		NodeInfo nodeInfo = nodes.get(node);
		
		if (nodeInfo == null) {
			nodeInfo = new NodeInfo(
					node,
					rowDefinitions.get(node.getSourceElement().getClass()));

			nodes.put(node, nodeInfo);

			if (ownerInfo != null)
				ownerInfo.addSubNode(nodeInfo);

			nodeListener.suspendAddedNotifications();
			try {
				node.addListener(nodeListener);
				if (!node.isAspectLeaf())
					for (ScheduledNode subNode : node.getNodes())
						loadNodeTree(nodeInfo, subNode);
			}
			finally {
				nodeListener.resumeAddedNotifications();
			}
		}
		else {
			if (nodeInfo.getOwner() != ownerInfo)
				throw new Error("owner mismatch");

			nodeListener.suspendAddedNotifications();
			try {
				// don't add listener again
				// node.addListener(nodeListener);
				if (!node.isAspectLeaf())
					for (ScheduledNode subNode : node.getNodes())
						loadNodeTree(nodeInfo, subNode);
			}
			finally {
				nodeListener.resumeAddedNotifications();
			}
		}

		return nodeInfo;
	}
	private void removeNodeTree(NodeInfo ownerInfo, NodeInfo nodeInfo) {
		for (NodeInfo subInfo : nodeInfo.getNodes())
			removeNodeTree(null, subInfo); // null is passed to skip removal of sub-node
		
		if (ownerInfo != null)
			ownerInfo.removeSubNode(nodeInfo);
		
		ScheduledNode node = nodeInfo.getNode();
		node.removeListener(nodeListener);
		nodes.remove(node);
	}
	
	
    private NodeInfo getNodeInfo(int row) {
    	int index = row - getFixedRowCount();
    	return rows.get(vr2dr.get(index));
    }
	private void collapsedNode(NodeInfo node, boolean refresh) {
		if (collapsedNodes.add(node)) {
			int row = node.getIndex();
			int anc = node.getAncestorCount();
			
			// check if node has ancestors to collapse
			if (anc == 0)
				return;
			
			int vr = dr2vr.get(row);

			// check if node is visible
			if (vr == -1)
				return;

			int ancFirst = row + 1;
			int ancEnd  = ancFirst + anc;
			if (ancEnd > dr2vr.size())
				throw new Error();

			int vrFirst = vr + 1;
			int vrEnd   = vrFirst + anc;
			if (vrEnd > vr2dr.size())
				vrEnd = vr2dr.size();
			
			for (int i = vrFirst; i < vrEnd; i++)
				if (vr2dr.get(i) >= ancEnd) {
					vrEnd = i;
					break;
				}
			
			// should never happen
			if (vrFirst >= vrEnd)
				return;
			
			// update dr2vr
			int n = vrEnd - vrFirst;
			for (int i = ancFirst; i < ancEnd; i++)
				dr2vr.set(i, -1);
			for (int i = ancEnd; i < dr2vr.size(); i++) {
				int oldIndex = dr2vr.get(i);
				if (oldIndex != -1)
					dr2vr.set(i, oldIndex - n);
			}
			
			// update vr2dr
			vr2dr.subList(vrFirst, vrEnd).clear();
			
			// System.out.println("collapsedNode");
			if (refresh)
				table.redraw();
		}
	}
	private void expandNode(NodeInfo node, boolean refresh) {
		if (collapsedNodes.remove(node)) {
			cleanCollapsedNodes();
			if (refresh)
				table.redraw();
		}
	}
	
	// methods
    @Override
    public int doGetRowCount() {
        return getFixedRowCount() + vr2dr.size();
    }
    @Override
    public int doGetColumnCount() {
        return
        		getFixedColumnCount()
        		+ 2						// collapsed + enabled
        		+ dataColumnSpan
        		+ PROGRESS_COLUMNSPAN;	// progress
    }
    @Override
    public Point doBelongsToCell(int col, int row) {
    	if (!isValid(col, row))
    		return new Point(col, row);

    	int progressColumn = doGetColumnCount() - PROGRESS_COLUMNSPAN;
    	
    	if (col == 0)
    		return new Point(0, row);
    	if (col >= progressColumn)
    		return new Point(progressColumn, row);
    	if (row == 0)
    		return new Point(1, row);

    	NodeInfo nodeInfo = getNodeInfo(row);

		int colTarget = col;
		if ((col -= nodeInfo.getDepth()) > 0) {
			switch (col) {
			case 1:
				// Collapsed
				return new Point(colTarget, row);
			case 2:
				// Enabled
				if (nodeInfo.canBeDisabled())
					return new Point(colTarget, row);
				
				break;
				
			default:
				if (nodeInfo.canBeDisabled())
					col--; // enabled button
					
				break;
			}

			// custom buttons, expandable button
			col -= 2;
			
			if (col > 0) { // (col >= 0)
				RowDefinition rowDefinition = nodeInfo.getRowDefinition();
				if (rowDefinition != null)
					for (CellDefinition cell : rowDefinition.getCells()) {
						int span = cell.getColumnSpan();
						if (col < span)
							break;
						col -= span;
					}

		        return new Point(colTarget - col, row);
			}
		}

        return new Point(colTarget, row);
    }
	@Override
	public Object doGetContentAt(int col, int row) {
    	if (!isValid(col, row))
    		return null;

    	if (col == 0)
    		return null;
    	if (row == 0) {
        	int progressColumn = doGetColumnCount() - PROGRESS_COLUMNSPAN;
        	if (col >= progressColumn)
        		return "Progress";
        	else
        		return "Acquisition Tree";
    	}

    	NodeInfo nodeInfo = getNodeInfo(row);
    	
    	if (col == doGetColumnCount() - PROGRESS_COLUMNSPAN)
    		return nodeInfo.getProgress();
	
		if ((col -= nodeInfo.getDepth()) > 0) {
			switch (col) {
			case 1:
				// Collapsed
				if (!nodeInfo.isLeaf())
					return !collapsedNodes.contains(nodeInfo);
				else
					return null;
			case 2:
				// Enabled
				if (nodeInfo.canBeDisabled())
					return nodeInfo.isEnabled();
				
				break;
				
			default:
				if (nodeInfo.canBeDisabled())
					col--; // enabled button
					
				break;
			}

			// custom buttons, expandable button
			col -= 2;
			
			RowDefinition rowDefinition = nodeInfo.getRowDefinition();
			if (rowDefinition != null)
				for (CellDefinition cell : rowDefinition.getCells())
					if (col < 0)
						break;
					else if (col == 0)
						return cell.getValue(nodeInfo.getNode());
					else
						col -= cell.getColumnSpan();

			if (col >= 0)
				return "";
			
			return "?";
		}
    	
    	return null;
	}
	@Override
	public void doSetContentAt(int col, int row, Object value) {
    	if (!isValid(col, row))
    		return;
    	if ((col == 0) || (row == 0))
    		return;

    	NodeInfo nodeInfo = getNodeInfo(row);

		if ((col -= nodeInfo.getDepth()) > 0) {
			switch (col) {
			case 1:
				// Collapsed
				if (Objects.equals(Boolean.TRUE, value))
					expandNode(nodeInfo, true);
				else
					collapsedNode(nodeInfo, true);
				return;
				
			case 2:
				// Enabled
				if (nodeInfo.canBeDisabled()) {
					nodeInfo.getNode().setEnabled(value);
					return;
				}
				
				break;
				
			default:
				if (nodeInfo.canBeDisabled())
					col--; // enabled button
					
				break;
			}

			// custom buttons, expandable button
			col -= 2;
			if (col < 0)
				return;
			
			RowDefinition rowDefinition = nodeInfo.getRowDefinition();
			if (rowDefinition != null)
				for (CellDefinition cell : rowDefinition.getCells())
					if (col < 0)
						break;
					else if (col == 0) {
						try {
							cell.setValue(nodeInfo.getNode(), value);
						}
						catch (Exception e) {
							// ignore cast exceptions
						}
						return;
					}
					else
						col -= cell.getColumnSpan();
		}
	}
	@Override
	public KTableCellEditor doGetCellEditor(int col, int row) {
    	if (!isValid(col, row))
    		return null;
    	if ((col == 0) || (row == 0))
    		return null;

    	NodeInfo nodeInfo = getNodeInfo(row);

    	if (col == doGetColumnCount() - PROGRESS_COLUMNSPAN) {
			if (nodeInfo.getProgress() instanceof String)
				return readonlyTextCellEditor;
    	}
    	
		if ((col -= nodeInfo.getDepth()) > 0) {
			switch (col) {
			case 1:
				// Collapsed
				if (!nodeInfo.isLeaf())
					return checkableCellEditor;
				else
					return null;
			case 2:
				// Enabled
				if (nodeInfo.canBeDisabled())
					return checkableCellEditor;
				
				break;
				
			default:
				if (nodeInfo.canBeDisabled())
					col--; // enabled button
					
				break;
			}

	    	if (nodeInfo.getNode().getPropertiesLocked())
	    		return null;

			// custom buttons, expandable button
			col -= 2;
			if (col < 0)
				return null;
	    	
			RowDefinition rowDefinition = nodeInfo.getRowDefinition();
			if (rowDefinition != null)
				for (CellDefinition cell : rowDefinition.getCells())
					if (col < 0)
						break;
					else if (col == 0) {
						KTableCellEditor cellEditor = cell.getCellEditor();
						if ((cellEditor != null) && (cell instanceof IModelCellDefinition))
					    	textModifyListener.update(
					    			(IModelCellDefinition)cell,
					    			new NodeElementAdapter(nodeInfo.getNode()));
						
						return cellEditor;
					}
					else
						col -= cell.getColumnSpan();
		}
		
		return null;
	}
	@Override
	public KTableCellRenderer doGetCellRenderer(int col, int row) {
    	if (!isValid(col, row))
    		return null;
    	if ((row == 0) && (col == 0))
    		return settingsCellRenderer;
        if (row == 0)
            return columnHeaderRenderer;

    	NodeInfo nodeInfo = getNodeInfo(row);
    	
    	if (col == 0)
    		return rowHeaderRenderers.get(nodeInfo.getRowDefinition().getElementType());
    	if (col == doGetColumnCount() - PROGRESS_COLUMNSPAN) {
    		if (nodeInfo.hasRowDefinition()) {
    			Object progress = nodeInfo.getProgress();
    			RowDefinition rowDefinition = nodeInfo.getRowDefinition();
    			RGB color = rowDefinition.getColor();
    			
    			DefaultCellRenderer cellRenderer;
    			
    			if (progress instanceof String) {
    				cellRenderer = textCellRenderer;
					if (nodeInfo.isTreeEnabled())
						cellRenderer.setForeground(null);
					else
						cellRenderer.setForeground(KTableResources.COLOR_DISABLED);
    			}
    			else {
    				cellRenderer = barDiagramCellRenderer;
        			if (color != null)
        				cellRenderer.setForeground(SWTResourceManager.getColor(PROGRESS_RGB.overlay(color)));
        			else
        				cellRenderer.setForeground(SWTResourceManager.getColor(PROGRESS_RGB.overlay(new RGB(255, 255, 255))));
    			}

				if (color != null)
					cellRenderer.setBackground(SWTResourceManager.getColor(color));
				else
					cellRenderer.setBackground(null);
    			
        		return cellRenderer;
    		}
    	}
	
		if ((col -= nodeInfo.getDepth()) > 0) {
			switch (col) {
			case 1:
				// Collapsed
				if (!nodeInfo.isLeaf())
					return collapsableCellRenderer;
				else
					return clearCellRenderer;
			case 2:
				// Enabled
				if (nodeInfo.canBeDisabled())
					return checkableCellRenderer;
				
				break;
				
			default:
				if (nodeInfo.canBeDisabled())
					col--; // enabled button
					
				break;
			}

			// custom buttons, expandable button
			col -= 2;
			if (col < 0)
				return clearCellRenderer;
			
			RowDefinition rowDefinition = nodeInfo.getRowDefinition();
			if (rowDefinition != null)
				for (CellDefinition cell : rowDefinition.getCells())
					if (col < 0)
						break;
					else if (col == 0) {
						DefaultCellRenderer cellRenderer = cell.getCellRenderer();
						if (cellRenderer == null)
							cellRenderer = textCellRenderer;
						
						if (rowDefinition.getColor() != null)
							cellRenderer.setBackground(SWTResourceManager.getColor(rowDefinition.getColor()));
						else
							cellRenderer.setBackground(null);

						if (nodeInfo.isTreeEnabled())
							cellRenderer.setForeground(null);
						else
							cellRenderer.setForeground(KTableResources.COLOR_DISABLED);

						if (cell instanceof PropertyCellDefinition) {
							PropertyCellDefinition propertyCell = (PropertyCellDefinition)cell;
							if (nodeInfo.getNode().isModified(propertyCell.getProperty())) {
								modifiedCellRenderer.setCellRenderer(cellRenderer);
								return modifiedCellRenderer;
							}
						}
						
						return cellRenderer;
					}
					else
						col -= cell.getColumnSpan();

			if (col >= 0) {
				if (rowDefinition.getColor() != null)
					endOfLineCellRenderer.setBackground(SWTResourceManager.getColor(rowDefinition.getColor()));
				else
					endOfLineCellRenderer.setBackground(null);
				return endOfLineCellRenderer;
			}
    	}

		return clearCellRenderer;
	}
    @Override
    public String doGetTooltipAt(int col, int row) {
    	if ((row == 0) && (col == 0))
    		return "click to expand menu";
    	if (col == 0) {
        	NodeInfo nodeInfo = getNodeInfo(row);
    		return nodeInfo.getRowDefinition().getButtonHeader();
    	}
    	
        return null;
    }
	// fixed
    @Override
    public int getFixedHeaderColumnCount() {
        return 1;
    }
    @Override
    public int getFixedSelectableColumnCount() {
        return 0;
    }
    @Override
    public int getFixedHeaderRowCount() {
        return 1;
    }
    @Override
    public int getFixedSelectableRowCount() {
        return 0;
    }
	// heights/widths
	@Override
	public int getRowHeightMinimum() {
        return KTableResources.ROW_HEIGHT;
	}
	@Override
	public int getInitialColumnWidth(int col) {
    	if (col < getFixedColumnCount())
    		return KTableResources.ROW_HEADER_WIDTH;

    	return KTableResources.ROW_HEIGHT + 2;
	}
	@Override
	public int getInitialRowHeight(int row) {
    	if (row < getFixedHeaderRowCount())
    		return KTableResources.COLUMN_HEADER_HEIGHT;
    	
    	return KTableResources.ROW_HEIGHT;
	}
    @Override
    public boolean isColumnResizable(int col) {
        return false;
    }
    @Override
    public boolean isRowResizable(int row) {
        return false;
    }

    // bug check
    private boolean isValid(int col, int row) {
    	return
    			(col >= 0) || (col < getColumnCount()) ||
    			(row >= 0) || (row < getRowCount());
    }
    
	//////////////////////////////////////////////
    
    private static class SchedulerListener implements ISchedulerListener {
		// fields
		private final ScheduleTableModel model;
		
		// construction
		public SchedulerListener(ScheduleTableModel model) {
			this.model = model;
		}

		// methods
		@Override
		public void onNewRoot(ScheduledAspect root) {
			model.onNewRoot(root);
		}
		@Override
		public void onNewLayer(Set<ScheduledAspect> owners) {
			model.onNewLayer(owners);
		}
		@Override
		public void onDeletedLayer(Set<ScheduledAspect> owners) {
			model.onDeletedLayer(owners);
		}
		// aspect
		@Override
		public void onAddedAspects(ScheduledAspect owner, Iterable<ScheduledAspect> aspects) {
			model.onNewAspects(owner, aspects);
		}
		@Override
		public void onDuplicatedAspects(ScheduledAspect owner, Iterable<ScheduledAspect> aspects) {
			model.onNewAspects(owner, aspects);
		}
		@Override
		public void onDeletedAspects(ScheduledAspect owner, Iterable<ScheduledAspect> aspects) {
			model.onDeletedAspects(owner, aspects);
		}
	}
    
	private static class NodeListener implements INodeListener {
		// fields
		private final ScheduleTableModel model;
		private int suspendCounter = 0;
		
		// construction
		public NodeListener(ScheduleTableModel model) {
			this.model = model;
		}
		
		// methods
		@Override
		public void onChangedProperty(ScheduledNode owner, IDependencyProperty property, Object oldValue, Object newValue) {
			model.onChangedNodeProperty(owner, property);
		}
		@Override
		public void onVisibilityChanged(ScheduledNode owner, boolean newValue) {
			model.onVisibilityChanged(owner);
		}
		@Override
		public void onAddedSubNode(ScheduledNode owner, ScheduledNode subNode) {
			if (suspendCounter == 0)
				model.onAddedSubNode(owner, subNode);
		}
		@Override
		public void onDuplicatedNode(ScheduledNode owner, ScheduledNode original, ScheduledNode duplicate) {
			model.onAddedSubNode(owner, duplicate);
		}
		@Override
		public void onDeletedSubNode(ScheduledNode owner, ScheduledNode subNode) {
			model.onDeletedSubNode(owner, subNode);
		}
		// locks
		@Override
		public void onPropertiesLocked() {
			// TODO Auto-generated method stub
		}
		@Override
		public void onOrderLocked() {
			// TODO Auto-generated method stub
		}
		@Override
		public void onUnlocked() {
			// TODO Auto-generated method stub
		}		
		// helpers
		public void suspendAddedNotifications() {
			suspendCounter++;
		}
		public void resumeAddedNotifications() {
			suspendCounter--;
			if (suspendCounter < 0)
				throw new Error("invalid operation");
		}
	}
	
	private static class NodeInfo {
		// fields
		private final RowDefinition rowDefinition;
		// nodes
		private NodeInfo owner;
		private final ScheduledNode node;
		private final List<NodeInfo> nodes;
		// cached
		private int depth;
		private int index;
		private int ancestorCount; // count of all ancestors
		// progress
		private double progress;
		private String progressText;
		
		// construction
		public NodeInfo(ScheduledNode node, RowDefinition rowDefinition) {
			this.owner = null;
			this.node = node;
			this.rowDefinition = rowDefinition;
			
			nodes = new ArrayList<>();
		}

		// properties
		public boolean hasRowDefinition() {
			return rowDefinition != null;
		}
		public RowDefinition getRowDefinition() {
			return rowDefinition;
		}
		public NodeInfo getOwner() {
			return owner;
		}
		public ScheduledNode getNode() {
			return node;
		}
		// depth and count
		public int getDepth() {
			return depth;
		}
		public int getIndex() {
			return index;
		}
		public int getAncestorCount() {
			return ancestorCount;
		}
		public boolean isLeaf() {
			return ancestorCount == 0;
		}
		public boolean isOrdered() {
			if (nodes.size() < 2)
				return true;

		    Comparator<NodeInfo> comparator = ScheduledNodeComparator.DEFAULT;
		    
		    NodeInfo n0 = nodes.get(0);
		    for (int i = 1, n = nodes.size(); i != n; i++) {
		    	NodeInfo n1 = nodes.get(i);

		        if (comparator.compare(n0, n1) > 0)
		            return false;
		        
		        n0 = n1;
		    }
		    return true;
		}
		public List<NodeInfo> getNodes() {
			return nodes;
		}
		// progress
		public Object getProgress() {
			return progressText == null ? progress : progressText;
		}

		// methods
		public void update(int depth, int index, int ancestorCount) {
			this.depth = depth;
			this.index = index;
			this.ancestorCount = ancestorCount;
		}
		public boolean sort() {
			if (isOrdered())
				return false;

			Collections.sort(nodes, ScheduledNodeComparator.DEFAULT);
			return true;
		}
		public int addSubNode(NodeInfo subNode) {
			if (subNode.owner != null)
				throw new Error("subNode.owner != null");

		    Comparator<NodeInfo> comparator = ScheduledNodeComparator.DEFAULT;

			subNode.owner = this;
			ancestorCount++;
			
			int i = 0;					// index for nodes list
			int subIndex = index + 1;	// row index
			
		    for (int n = nodes.size(); i != n; i++) {
		    	NodeInfo refNode = nodes.get(i);
		        if (comparator.compare(refNode, subNode) < 0)
			        subIndex += 1 + refNode.ancestorCount;
		        else
		        	break;
		    }
		    
		    nodes.add(i, subNode);
		    return subIndex;
		}
		public void removeSubNode(NodeInfo subNode) {
			if (subNode.owner != this)
				throw new Error("subNode.owner != this");

			if (!nodes.remove(subNode))
				throw new Error("node not found");

			ancestorCount--;
			subNode.owner = null;
		}
		// progress
		public void beginChangeParameter() {
			progressText = "setup";
		}
		public void endChangeParameter(ParameterChangeSummary summary) {
			if (summary.getInterrupted())
				progressText = "interrupted";
			else
				progressText = null;
		}
		public void beginPreAcquisition() {
			progressText = "attenuation";
		}
		public void endPreAcquisition(Summary summary) {
			if (summary.getInterrupted())
				progressText = "interrupted";
		}
		public void beginDoAcquisition() {
			progressText = "acquisition";
		}
		public void endDoAcquisition(AcquisitionSummary summary) {
			if (summary.getInterrupted())
				progressText = "interrupted";
			else {
				String filename = summary.getFilename();
				if ((filename != null) && !"".equals(filename)) {
					if (filename.endsWith(".nx.hdf"))
						filename = filename.substring(0, filename.length() - ".nx.hdf".length());

					progressText = filename;
				}
				else {
					progressText = "completed";
				}
			}
		}
		public void beginPostAcquisition() {
		}
		public void endPostAcquisition(Summary summary) {
			// should be in notes !!!
			//if (summary.getInterrupted())
			//	progressText = "interrupted";
		}
		public void completed() {
			if (nodes.isEmpty())
				progress = 1.0;
			
			if (owner != null)
				owner.updateProgress();
		}
		public void resetProgress(boolean text) {
			progress = 0.0;
			if (text)
				progressText = null;
		}
		private void updateProgress() {
			int totalCount = 0;
			double totalProgress = 0.0;
			
			for (NodeInfo subNode : nodes)
				if (subNode.node.isEnabled()) {
					totalCount++;
					totalProgress += subNode.progress;
				}
			
			if (totalCount == 0)
				progress = 1.0;
			else
				progress = totalProgress / totalCount;

			if (owner != null)
				owner.updateProgress();
		}
		
		// enabled is just a normal property and shouldn't be treated specially
		public boolean canBeDisabled() {
			return node.canBeDisabled();
		}
		public boolean isEnabled() {
			return node.isEnabled();
		}
		public boolean isTreeEnabled() {
			NodeInfo node = this;
			do {
				if (!node.isEnabled())
					return false;
				
				node = node.owner;
			} while (node != null);
			return true;
		}
	}
	
	private static class ScheduledNodeComparator implements Comparator<NodeInfo> {
		// finals
		public static final Comparator<NodeInfo> DEFAULT = new ScheduledNodeComparator();
		
		// methods
		@Override
		public int compare(NodeInfo node1, NodeInfo node2) {
			return Integer.compare(node1.node.getIndex(), node2.node.getIndex());
		}
	}
	
	//////////////////////////////////////////////

	private static class ARGB {
		// fields
		public final int alpha;
		public final int red;
		public final int green;
		public final int blue;
		
		// construction
		public ARGB(int alpha, int red, int green, int blue) {
			if ((alpha > 255) || (alpha < 0) ||
				(red > 255) || (red < 0) ||
				(green > 255) || (green < 0) ||
				(blue > 255) || (blue < 0))
				SWT.error(SWT.ERROR_INVALID_ARGUMENT);

			this.alpha = alpha;
			this.red = red;
			this.green = green;
			this.blue = blue;
		}
				
		// methods
		public RGB overlay(RGB color) {
			float a = alpha / 255.0f;
			float invA = 1.0f - a;
			
			return new RGB(
					(int)Math.round(a * red + invA * color.red),
					(int)Math.round(a * green + invA * color.green),
					(int)Math.round(a * blue + invA * color.blue));
		}
		// object
		@Override
		public boolean equals(Object object) {
			if (object == this)
				return true;
			
			if (!(object instanceof ARGB))
				return false;
			
			ARGB rgb = (ARGB)object;
			return
					(rgb.alpha == this.alpha) &&
					(rgb.red == this.red) &&
					(rgb.green == this.green) &&
					(rgb.blue == this.blue);
		}
		@Override
		public int hashCode() {
			return (alpha << 24) | (red << 16) | (green << 8) | blue;
		}
		@Override
		public String toString() {
			return "ARGB {" + alpha + ", " + red + ", " + green + ", " + blue + "}";
		}
	}
	
    // cell renderer
    private static class ClearCellRenderer extends DefaultCellRenderer {
        // construction
    	public ClearCellRenderer() {
    		super(SWT.NONE);
    	}
    	
    	// methods
    	@Override
    	public int getOptimalWidth(GC gc, int col, int row, Object content, boolean fixed, KTableModel model) {
    		return 1;
    	}
    	@Override
    	public void drawCell(GC gc, Rectangle rect, int col, int row, Object content, boolean focus, boolean header, boolean clicked, KTableModel model) {
            gc.setBackground(getBackground());
            gc.fillRectangle(new Rectangle(rect.x, rect.y, rect.width + 1, rect.height));
    	}
    }
    private static class EndOfLineRenderer extends DefaultCellRenderer {
        // construction
    	public EndOfLineRenderer() {
    		super(SWT.NONE);
    	}
    	
    	// methods
    	@Override
    	public int getOptimalWidth(GC gc, int col, int row, Object content, boolean fixed, KTableModel model) {
    		return 1;
    	}
    	@Override
    	public void drawCell(GC gc, Rectangle rect, int col, int row, Object content, boolean focus, boolean header, boolean clicked, KTableModel model) {
            gc.setBackground(getBackground());
            gc.fillRectangle(rect);
    	}
    }
    private static class ModifiedCellRenderer implements KTableCellRenderer {
    	// fields
    	private DefaultCellRenderer cellRenderer;
    	
    	// properties
    	public void setCellRenderer(DefaultCellRenderer value) {
    		cellRenderer = value;
    	}

    	// methods
		@Override
		public int getOptimalWidth(GC gc, int col, int row, Object content, boolean fixed, KTableModel model) {
			if (cellRenderer == null)
				return 0;
			
			return cellRenderer.getOptimalWidth(gc, col, row, content, fixed, model);
		}
		@Override
    	public void drawCell(GC gc, Rectangle rect, int col, int row, Object content, boolean focus, boolean header, boolean clicked, KTableModel model) {
			if (cellRenderer == null)
				return;
			
			int originalStyle = cellRenderer.getStyle(); 
			try {
				cellRenderer.setStyle(originalStyle | SWT.ITALIC);
				cellRenderer.drawCell(gc, rect, col, row, content, focus, header, clicked, model);
			}
			finally {
				cellRenderer.setStyle(originalStyle);
			}
		}
    }

    // definitions
    public static class RowDefinition {
    	// fields
    	private final Class<? extends Element> elementType;
    	private final RGB color;
    	private final String buttonHeader;
    	private final List<ButtonInfo<ScheduledNode>> buttons;
    	private final List<CellDefinition> cells;
    	private final int columnSpan;
    	
    	// construction
    	public RowDefinition(Class<? extends Element> elementType, RGB color, String buttonHeader, List<ButtonInfo<ScheduledNode>> buttons, CellDefinition ... cells) {
    		this.elementType = elementType;
    		this.color = color;
    		this.buttonHeader = buttonHeader;
    		this.buttons = Collections.unmodifiableList(buttons);
    		this.cells = Collections.unmodifiableList(Arrays.asList(cells));
    		
    		int columnSpan = 0;
    		for (CellDefinition cell : cells)
    			columnSpan += cell.columnSpan;
    		this.columnSpan = columnSpan;
    	}
    	
    	// properties
    	public Class<? extends Element> getElementType() {
    		return elementType;
    	}
    	public RGB getColor() {
    		return color;
    	}
    	public String getButtonHeader() {
    		return buttonHeader;
    	}
    	public List<ButtonInfo<ScheduledNode>> getButtons() {
    		return buttons;
    	}
    	public List<CellDefinition> getCells() {
    		return cells;
    	}
    	public int getColumnSpan() {
    		return columnSpan;
    	}
    }
    public static abstract class CellDefinition {
    	// fields
    	protected final int columnSpan;
    	protected final IModelValueConverter<?, ?> converter;
    	
    	// construction
    	protected CellDefinition(IModelValueConverter<?, ?> converter, int columnSpan) {
    		this.columnSpan = columnSpan;
    		this.converter = converter;
    	}
    	
    	// properties
    	public int getColumnSpan() {
    		return columnSpan;
    	}
    	public DefaultCellRenderer getCellRenderer() {
    		return null;
    	}
    	public KTableCellEditor getCellEditor() {
    		return null;
    	}
    	public IModelValueConverter<?, ?> getConverter() {
        	return converter;
    	}
    	
    	// methods
    	public abstract Object getValue(ScheduledNode node);
    	public abstract void setValue(ScheduledNode node, Object value);
    }
    public static class PropertyCellDefinition extends CellDefinition implements IModelCellDefinition {
		// fields
    	private final IDependencyProperty property;
    	private final DefaultCellRenderer cellRenderer;
    	private final KTableCellEditor cellEditor;

    	// construction
    	public <TElement extends Element, TModel>
    	PropertyCellDefinition(DependencyProperty<TElement, TModel> property, DefaultCellRenderer cellRenderer, KTableCellEditor cellEditor, int columnSpan) {
			this(property, cellRenderer, cellEditor, null, columnSpan);
		}
    	public <TElement extends Element, TModel>
    	PropertyCellDefinition(DependencyProperty<TElement, TModel> property, DefaultCellRenderer cellRenderer, KTableCellEditor cellEditor, IModelValueConverter<TModel, ?> converter, int columnSpan) {
			super(converter, columnSpan);
			this.property = property;
	    	this.cellRenderer = cellRenderer;
	    	this.cellEditor = cellEditor;
		}

    	// properties
		@Override
		public IDependencyProperty getProperty() {
			return property;
		}
		@Override
		public DefaultCellRenderer getCellRenderer() {
	    	return cellRenderer;
		}
		@Override
		public KTableCellEditor getCellEditor() {
	    	return cellEditor;
		}
		
    	// methods
		@Override
		public Object getValue(ScheduledNode node) {
			return convertFromModel(node.get(property));
		}
		@Override
    	public void setValue(ScheduledNode node, Object value) {
			node.set(property, convertToModelValue(value));
		}
    	
		// helpers
		@Override
    	@SuppressWarnings("unchecked")
    	public Object convertFromModel(Object value) {
    		if (converter == null)
    			return value;
    		
    		return ((IModelValueConverter<Object, Object>)converter).fromModelValue(
    				converter.getModelValueType().cast(value));
    	}
    	@Override
    	@SuppressWarnings("unchecked")
    	public Object convertToModelValue(Object value) {
    		if (converter == null)
    			return value;
    		
    		return ((IModelValueConverter<Object, Object>)converter).toModelValue(
    				converter.getTargetValueType().cast(value));
    	}
    }
    public static class FixedCellDefinition extends CellDefinition {
		// fields
    	private final String value;

    	// construction
    	public FixedCellDefinition(String value, int columnSpan) {
			super(null, columnSpan);
			this.value = value;
		}
    	
    	// methods
		@Override
		public String getValue(ScheduledNode node) {
			return value;
		}
		@Override
    	public void setValue(ScheduledNode node, Object value) {
			// not supported
		}
    }
}
