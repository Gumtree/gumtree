package org.gumtree.jython.ui;

import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.collection.LambdaCollections.with;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.gumtree.jython.ui.internal.InternalImage;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.jface.ITreeNode;
import org.gumtree.ui.util.jface.TreeContentProvider;
import org.gumtree.ui.util.jface.TreeLabelProvider;
import org.gumtree.ui.util.jface.TreeNode;
import org.gumtree.ui.widgets.ExtendedComposite;

@SuppressWarnings("restriction")
public class JythonScriptDemoWidget extends ExtendedComposite {

	private IDataAccessManager dataAccessManager;

	private IEclipseContext eclipseContext;

	private Map<String, List<ScriptContext>> scriptRegistry;

	private UIContext context;

	public JythonScriptDemoWidget(Composite parent, int style) {
		super(parent, style);
		scriptRegistry = new HashMap<String, List<ScriptContext>>(2);
		context = new UIContext();
	}

	@PostConstruct
	public void render() {
		setLayout(new FillLayout());

		SashForm sashFormMain = getWidgetFactory().createSashForm(this,
				SWT.HORIZONTAL);
		createTreeViewer(sashFormMain);

		SashForm sashFormRight = getWidgetFactory().createSashForm(
				sashFormMain, SWT.VERTICAL);

		context.renderArea = getWidgetFactory().createComposite(sashFormRight,
				SWT.BORDER);
		context.renderArea.setLayout(new FillLayout());

		context.lineStyler = new JythonLineStyler();
		context.sourceText = new StyledText(sashFormRight, SWT.BORDER
				| SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		context.sourceText.addLineStyleListener(context.lineStyler);
		context.sourceText.setEditable(false);

		sashFormMain.setWeights(new int[] { 1, 3 });
		sashFormRight.setWeights(new int[] { 3, 1 });
	}

	private void createTreeViewer(Composite parent) {
		context.treeViewer = new TreeViewer(parent);
		context.treeViewer.setContentProvider(new TreeContentProvider());
		context.treeViewer.setLabelProvider(new TreeLabelProvider());
		context.treeViewer.getTree().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				Object node = ((IStructuredSelection) context.treeViewer
						.getSelection()).getFirstElement();
				if (node instanceof GroupTreeNode) {
					TreeItem item = context.treeViewer.getTree().getSelection()[0];
					boolean expanded = item.getExpanded();
					item.setExpanded(!expanded);
				} else if (node instanceof ScriptTreeNode) {
					loadScript(((ScriptTreeNode) node).getScriptContext());
				}
			}
		});
		context.treeViewer.setInput(createTreeNode());
	}

	@Override
	protected void disposeWidget() {
		if (scriptRegistry != null) {
			scriptRegistry.clear();
			scriptRegistry = null;
		}
		if (context != null) {
			if (context.lineStyler != null) {
				context.lineStyler.disposeColors();
			}
			context = null;
		}
	}

	public JythonScriptDemoWidget addScript(String group, String label,
			String scriptURI) {
		ScriptContext scriptContext = new ScriptContext();
		scriptContext.label = label;
		scriptContext.scriptURI = URI.create(scriptURI);
		List<ScriptContext> contexts = scriptRegistry.get(group);
		if (contexts == null) {
			contexts = new ArrayList<ScriptContext>(2);
			scriptRegistry.put(group, contexts);
		}
		contexts.add(scriptContext);
		return this;
	}

	/**************************************************************************
	 * Components
	 **************************************************************************/

	public IDataAccessManager getDataAccessManager() {
		return dataAccessManager;
	}

	@Inject
	public void setDataAccessManager(IDataAccessManager dataAccessManager) {
		this.dataAccessManager = dataAccessManager;
	}

	public IEclipseContext getEclipseContext() {
		return eclipseContext;
	}

	@Inject
	public void setEclipseContext(IEclipseContext eclipseContext) {
		this.eclipseContext = eclipseContext;
	}

	/**************************************************************************
	 * Utilities
	 **************************************************************************/

	private ITreeNode[] createTreeNode() {
		List<ITreeNode> treeNodes = new ArrayList<ITreeNode>(2);
		for (Entry<String, List<ScriptContext>> entry : scriptRegistry
				.entrySet()) {
			GroupTreeNode groupTreeNode = new GroupTreeNode(entry.getKey());
			for (ScriptContext context : entry.getValue()) {
				groupTreeNode.getChildrenList()
						.add(new ScriptTreeNode(context));
			}
			treeNodes.add(groupTreeNode);
		}
		return with(treeNodes).sort(on(ITreeNode.class).getText()).toArray(
				ITreeNode.class);
	}

	private void loadScript(ScriptContext scriptContext) {
		for (Control control : context.renderArea.getChildren()) {
			control.dispose();
		}

		JythonScriptWidget scriptWidget = new JythonScriptWidget(
				context.renderArea, SWT.NONE);
		scriptWidget.setScriptURI(scriptContext.scriptURI);
		ContextInjectionFactory.inject(scriptWidget, getEclipseContext());

		String scriptText = getDataAccessManager().get(scriptContext.scriptURI,
				String.class);
		context.sourceText.setText(scriptText);

		// Refresh
		SafeUIRunner.asyncExec(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				if (context != null) {
					context.renderArea.layout(true, true);
				}
			}
		});
	}

	public class GroupTreeNode extends TreeNode {
		private String group;

		private List<ITreeNode> children;

		public GroupTreeNode(String group) {
			this.group = group;
			children = new ArrayList<ITreeNode>(2);
		}

		public String getText() {
			return group;
		}

		public Image getImage() {
			return InternalImage.FOLDER.getImage();
		}

		public List<ITreeNode> getChildrenList() {
			return children;
		}

		public ITreeNode[] getChildren() {
			return with(getChildrenList()).sort(on(ITreeNode.class).getText())
					.toArray(ITreeNode.class);
		}
	}

	public class ScriptTreeNode extends TreeNode {
		private ScriptContext scriptContext;

		public ScriptTreeNode(ScriptContext scriptContext) {
			this.scriptContext = scriptContext;
		}

		public String getText() {
			return scriptContext.label;
		}

		public Image getImage() {
			return InternalImage.SCRIPT_FILE.getImage();
		}

		public ScriptContext getScriptContext() {
			return scriptContext;
		}
	}

	public class ScriptContext {
		String label;
		URI scriptURI;
	}

	private class UIContext {
		TreeViewer treeViewer;
		Composite renderArea;
		StyledText sourceText;
		JythonLineStyler lineStyler;
	}

}
