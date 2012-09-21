package org.gumtree.ui.missioncontrol.support;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.gumtree.ui.internal.InternalImage;
import org.gumtree.ui.missioncontrol.IAppHubData;
import org.gumtree.ui.missioncontrol.IHub;
import org.gumtree.ui.missioncontrol.IHubRegistry;
import org.gumtree.widgets.swt.ExtendedComposite;
import org.gumtree.widgets.swt.IControlFactory;
import org.gumtree.widgets.swt.navigation.NavigationComposite;
import org.gumtree.widgets.swt.tile.TileColor;
import org.gumtree.widgets.swt.tile.TileDataFactory;
import org.gumtree.widgets.swt.tile.TileLayoutFactory;
import org.gumtree.widgets.swt.util.UIResourceManager;
import org.gumtree.widgets.swt.util.UIResources;

@SuppressWarnings("restriction")
public class MissionControlWidget extends ExtendedComposite {

	private IHubRegistry hubRegistry;

	private IHub hub;

	private UIContext context;

	@Inject
	public MissionControlWidget(Composite parent, @Optional int style) {
		super(parent, style);
		context = new UIContext();
	}

	@PostConstruct
	public void render() {
		context.resourceManager = new UIResourceManager(this);
		GridLayoutFactory.swtDefaults().margins(0, 0).spacing(0, 0)
				.applyTo(this);
		createToolbar(this);
		createMainArea(this);
	}

	private void createToolbar(Composite parent) {
		Composite toolbarArea = getWidgetFactory().createComposite(parent);
		toolbarArea.setBackground(UIResources.getSystemColor(SWT.COLOR_BLACK));
		toolbarArea.setBackgroundMode(SWT.INHERIT_FORCE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(toolbarArea);
		GridLayoutFactory.swtDefaults().margins(5, 5).spacing(5, 0)
				.numColumns(3).applyTo(toolbarArea);

		Label backButton = getWidgetFactory().createLabel(toolbarArea, "");
		backButton.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
		backButton.setToolTipText("Back");
		backButton.setImage(InternalImage.BACK_22.getImage());
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)
				.grab(true, false).applyTo(backButton);

		Label hubButton = getWidgetFactory().createLabel(toolbarArea, "");
		hubButton.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
		hubButton.setToolTipText("Hub");
		hubButton.setImage(InternalImage.WINDOWS_22.getImage());
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)
				.grab(true, false).applyTo(hubButton);

		Label settingsButton = getWidgetFactory().createLabel(toolbarArea, "");
		settingsButton.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
		settingsButton.setToolTipText("Settings");
		settingsButton.setImage(InternalImage.SETTINGS_22.getImage());
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)
				.grab(true, false).applyTo(settingsButton);
		
		// Listener
		backButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
			}
		});

		hubButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				clearComposite(context.mainArea);
				createHubSelectionPage(context.mainArea);
				layout(true, true);
			}
		});

		settingsButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
			}
		});

	}

	private void createMainArea(Composite parent) {
		context.mainArea = getWidgetFactory().createComposite(parent);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, true).applyTo(context.mainArea);
		context.mainArea.setLayout(new FillLayout());
		createHubPage(context.mainArea, getHub());
	}

	private void createHubSelectionPage(Composite parent) {
		Composite composite = getWidgetFactory().createComposite(parent);
		composite.setBackground(UIResources.getSystemColor(SWT.COLOR_BLACK));
		composite.setForeground(UIResources.getSystemColor(SWT.COLOR_WHITE));
		TileLayoutFactory.create().numColumns(8).applyTo(composite);
		
		Label label = getWidgetFactory().createLabel(composite, "Hubs");
		label.setFont(context.resourceManager.createDefaultFont(24, SWT.BOLD));
		TileDataFactory.create().size(8, 2).applyTo(label);
		
		if (getHubRegistry() != null) {
			for (IHub hub : getHubRegistry().getHubs()) {
				createHubTile(composite, hub);
			}
		}
	}

	private void createHubPage(Composite parent, final IHub hub) {
		if (hub == null) {
			createHubSelectionPage(parent);
			return;
		}
		NavigationComposite navigationComposite = new NavigationComposite(
				parent, SWT.NONE);
		navigationComposite.setBackground(UIResources
				.getSystemColor(SWT.COLOR_BLACK));
		navigationComposite.setForeground(UIResources
				.getSystemColor(SWT.COLOR_WHITE));
		navigationComposite.render();

		navigationComposite.addPage(hub.getLabel(), hub.getLabel(),
				new IControlFactory() {
					@Override
					public Control createControl(Composite parent) {
						Composite composite = getWidgetFactory()
								.createComposite(parent);
						TileLayoutFactory.create().numColumns(8)
								.applyTo(composite);
						for (IAppHubData data : hub.getAppHubData()) {
							// Tile
							Composite tile = getWidgetFactory()
									.createComposite(composite);
							TileDataFactory.create()
									.size(data.getWidth(), data.getHeight())
									.applyTo(tile);
							tile.setBackground(getRandomTileColor());
							GridLayoutFactory.swtDefaults().applyTo(tile);

							// Label
							Label label = getWidgetFactory().createLabel(tile,
									data.getAppId(), SWT.WRAP);
							label.setFont(UIResources.getDefaultFont(SWT.BOLD));
							GridDataFactory.swtDefaults()
									.align(SWT.CENTER, SWT.CENTER)
									.grab(true, true).applyTo(label);
						}
						return composite;
					}
				});
	}

	private void createHubTile(Composite parent, final IHub hub) {
		// Tile
		Composite tile = getWidgetFactory().createComposite(parent);
		tile.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
		TileDataFactory.create().size(4, 4).applyTo(tile);
		tile.setBackground(getRandomTileColor());
		GridLayoutFactory.swtDefaults().applyTo(tile);

		// Label
		Label label = getWidgetFactory().createLabel(tile, hub.getLabel(),
				SWT.WRAP);
		label.setFont(context.resourceManager.createDefaultFont(16, SWT.BOLD));
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)
				.grab(true, true).applyTo(label);

		// Listener
		tile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				clearComposite(context.mainArea);
				createHubPage(context.mainArea, hub);
				layout(true, true);
			}
		});
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				clearComposite(context.mainArea);
				createHubPage(context.mainArea, hub);
				layout(true, true);
			}
		});
	}

	@Override
	protected void disposeWidget() {
		context = null;
		hubRegistry = null;
		hub = null;
	}

	/*************************************************************************
	 * API
	 *************************************************************************/

	public IHub getHub() {
		return hub;
	}

	public void setHub(IHub hub) {
		this.hub = hub;
	}

	/*************************************************************************
	 * Components
	 *************************************************************************/

	public IHubRegistry getHubRegistry() {
		return hubRegistry;
	}

	public void setHubRegistry(IHubRegistry hubRegistry) {
		this.hubRegistry = hubRegistry;
	}

	/*************************************************************************
	 * Utilities
	 *************************************************************************/

	private class UIContext {
		private UIResourceManager resourceManager;
		private Composite mainArea;
	}

	private static Color getRandomTileColor() {
		TileColor[] colors = TileColor.values();
		int colorCount = colors.length;
		return colors[(int) Math.round(Math.floor(Math.random() * colorCount))]
				.getColor();
	}

	private static void clearComposite(Composite composite) {
		for (Control child : composite.getChildren()) {
			child.dispose();
		}
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		MissionControlWidget widget = new MissionControlWidget(shell, SWT.NONE);

		HubRegistry hubRegistry = new HubRegistry();
		hubRegistry.activate();
		IHub hub = new Hub("Instrument");
		hub.addApp("sics");
		hub.addApp("proxy");
		hub.addApp("status", 8, 4);
		hub.addApp("m1", 2, 2);
		hub.addApp("m2", 2, 2);
		hub.addApp("m3", 2, 2);
		hub.addApp("m4", 2, 2);
		hubRegistry.addHub(hub);
//		widget.setHub(hub);
		hub = new Hub("Experiment");
		hubRegistry.addHub(hub);
		hub = new Hub("Data Reduction");
		hubRegistry.addHub(hub);
		hub = new Hub("Settings");
		hubRegistry.addHub(hub);
		widget.setHubRegistry(hubRegistry);

		widget.render();

		shell.setSize(340, 800);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

}
