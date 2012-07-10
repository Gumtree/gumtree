package org.gumtree.ui.service.sidebar.support;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.gumtree.ui.internal.InternalImage;
import org.gumtree.ui.service.sidebar.IGadget;
import org.gumtree.ui.util.forms.FormComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.lambdaj.collection.LambdaCollections;

public class GadgetViewer extends FormComposite {
	
	private static final Logger logger = LoggerFactory.getLogger(GadgetViewer.class);
	
	private ScrolledForm form;
	
	private Map<IGadget, Section> gadgetMap;
	
	public GadgetViewer(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout());
		form = getToolkit().createScrolledForm(this);
		getToolkit().decorateFormHeading(form.getForm());
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(form.getBody());
		gadgetMap = new LinkedHashMap<IGadget, Section>(2);
		
		// Menu
		Action expandAllAction = new Action() {
			public void run() {
				for (Section section : gadgetMap.values()) {
					section.setExpanded(true);
				}
			}
		};
		expandAllAction.setToolTipText("Expand All");
		expandAllAction.setImageDescriptor(InternalImage.EXPAND_ALL.getDescriptor());
		form.getForm().getToolBarManager().add(expandAllAction);
		
		Action collapseAllAction = new Action() {
			public void run() {
				for (Section section : gadgetMap.values()) {
					section.setExpanded(false);
				}
			}
		};
		collapseAllAction.setToolTipText("Collapse All");
		collapseAllAction.setImageDescriptor(InternalImage.COLLAPSE_ALL.getDescriptor());
		form.getForm().getToolBarManager().add(collapseAllAction);
		
		Action preferencesAction = new Action() {
		};
		preferencesAction.setToolTipText("Preferences...");
		preferencesAction.setImageDescriptor(InternalImage.PREFERENCES.getDescriptor());
		form.getForm().getToolBarManager().add(preferencesAction);
		
		form.getToolBarManager().update(true);
		
		// Add drop support
		int operations = DND.DROP_MOVE | DND.DROP_COPY;
		DropTarget dropTarget = new DropTarget(form.getBody(), operations);
		dropTarget.setTransfer(new Transfer[] { FileTransfer.getInstance() });
		dropTarget.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent event) {
				if (FileTransfer.getInstance().isSupportedType(
						event.currentDataType)) {
					String[] files = (String[]) event.data;
					if (files.length == 1) {
						try {
							XwtBasedGadget gadget = new XwtBasedGadget(
									new File(files[0]).toURI().toURL());
							addGadget(gadget);
						} catch (Exception e) {
							logger.error("Cannot read from file " + files[0], e);
						}
					}
				}
			}
		});
	}

	public IGadget[] getGagdgets() {
		return LambdaCollections.with(gadgetMap).keySet()
				.toArray(IGadget.class);
	}
	
	public void addGadget(final IGadget gadget) {
		Section section = getToolkit().createSection(form.getBody(), Section.SHORT_TITLE_BAR | Section.TWISTIE |Section.EXPANDED);
		if (gadget.getName() != null) {
			section.setText(gadget.getName());
		} else {
			section.setText("XWT");
		}
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(section);
		gadgetMap.put(gadget, section);
		
		Composite sectionClient = getToolkit().createComposite(section);
		sectionClient.setLayout(new FillLayout());
		Composite gadgetHolder = new Composite(sectionClient, SWT.NONE) {
			public void layout (boolean changed, boolean all) {
				refrehLayout();
			}
		};
		gadgetHolder.setLayout(new FillLayout());
		gadget.createGadget(gadgetHolder);
		section.setClient(sectionClient);
		
		// Gadget remove control
		if (gadget instanceof XwtBasedGadget) {
			
			ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
			ToolBar toolbar = toolBarManager.createControl(section);
			Action action = new Action() {
				public void run() {
					removeGadget(gadget);
				}
			};
			action.setImageDescriptor(InternalImage.DELETE_12.getDescriptor());
			toolBarManager.add(action);
			toolBarManager.update(true);
			getToolkit().adapt(toolbar);
			section.setTextClient(toolbar);
		}
		
		refrehLayout();
	}
	
	public void removeGadget(IGadget gadget) {
		Control control = gadgetMap.get(gadget);
		if (control != null) {
			gadgetMap.remove(gadget);
			control.dispose();
			refrehLayout();
		}
	}
	
	@Override
	protected void disposeWidget() {
		if (gadgetMap != null) {
			gadgetMap.clear();
			gadgetMap = null;
		}
		form = null;
	}

	private void refrehLayout() {
		layout(true, true);
		if (form != null) {
			form.reflow(true);
		}
	}
	
}
