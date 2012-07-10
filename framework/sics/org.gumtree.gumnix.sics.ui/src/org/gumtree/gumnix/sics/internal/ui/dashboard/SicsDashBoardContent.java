package org.gumtree.gumnix.sics.internal.ui.dashboard;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.internal.ui.Activator;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.ui.service.shortcuts.ActionShortcutException;
import org.gumtree.ui.service.shortcuts.IActionShortcutDescriptor;
import org.gumtree.ui.service.shortcuts.IActionShortcutRegistry;

public class SicsDashBoardContent {

	private static Image DEFAULT_ICON_16;

	private static Image STOP_ICON;

	static {
		if(Activator.getDefault() != null) {
			SafeRunner.run(new ISafeRunnable() {
				public void handleException(Throwable exception) {
				}
				public void run() throws Exception {
					DEFAULT_ICON_16 = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/obj16/welcome_item.gif").createImage();
					STOP_ICON = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/Hand.png").createImage();
				}
			});
		}
	}

	private IActionShortcutRegistry registry;
	
	public void createContentControl(Composite parent) {
		createShortcutTabs(parent);
	}

	private void createShortcutTabs(Composite parent) {
		parent.setLayout(new GridLayout(2, false));

		Button stopButton = new Button(parent, SWT.PUSH);
		stopButton.setImage(STOP_ICON);
		stopButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					SicsCore.getSicsController().interrupt();
				} catch (SicsIOException e1) {
					e1.printStackTrace();
				}
			}
		});
		stopButton.setToolTipText("Interrupt SICS");

		TabFolder folder =  new TabFolder(parent, SWT.NONE);
		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		SortedSet<String> categories = new TreeSet<String>();
		Map<String, ToolBar> shelfItemMap = new HashMap<String, ToolBar>();
		IActionShortcutDescriptor[] descs = getShortcutRegistry().getAllShortcutDescriptors();

		// Finds all shelf items first
		for(IActionShortcutDescriptor desc : descs) {
			String category = getShortcutRegistry().getCategoryName(desc.getCategory());
			categories.add(category);
		}

		// Builds shelf items
		for(String category : categories) {
			TabItem item = new TabItem(folder, SWT.NONE);
			item.setText(category);
			Composite composite = new Composite(folder, SWT.NONE);
			composite.setLayout(new GridLayout());
			item.setControl(composite);
			ToolBar toolbar = new ToolBar(composite, SWT.HORIZONTAL | SWT.RIGHT | SWT.WRAP | SWT.FLAT);
			shelfItemMap.put(category, toolbar);
		}

		// Adds shortcuts
		for(final IActionShortcutDescriptor desc : descs) {
			String category = getShortcutRegistry().getCategoryName(desc.getCategory());
			ToolBar toolbar = shelfItemMap.get(category);

			ToolItem toolItem = new ToolItem(toolbar, SWT.NONE);
			toolItem.setText(desc.getLabel());
			if(desc.getIcon16() != null) {
				toolItem.setImage(desc.getIcon16().createImage());
			} else {
				toolItem.setImage(DEFAULT_ICON_16);
			}
			toolbar.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
			toolItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					try {
						final IAction action = desc.getAction();
						if(action != null) {
							PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable () {
								public void run() {
									action.run();
								}
							});
						}
					} catch (ActionShortcutException e1) {
						e1.printStackTrace();
					}
				}
			});
		}
	}

	public void dispose() {

	}

	private IActionShortcutRegistry getShortcutRegistry() {
		if (registry == null) {
			registry = ServiceUtils.getService(IActionShortcutRegistry.class);
		}
		return registry;
	}

}
