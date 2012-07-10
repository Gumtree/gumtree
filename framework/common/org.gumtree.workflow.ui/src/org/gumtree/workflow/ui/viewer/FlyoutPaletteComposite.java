/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.gumtree.workflow.ui.viewer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.Button;
import org.eclipse.draw2d.ButtonBorder;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FocusEvent;
import org.eclipse.draw2d.FocusListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.ImageUtilities;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleControlAdapter;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tracker;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.DragCursors;
import org.gumtree.workflow.ui.internal.Activator;
import org.gumtree.workflow.ui.internal.SharedCursors;

/**
 * The FlyoutPaletteComposite is used to show a flyout palette alongside another control. 
 * The flyout palette auto-hides (thus maximizing space) when not in use, but can also be
 * pinned open if so desired.  It will only be visible when the PaletteView is not.
 * 
 * @author Pratik Shah
 * @since 3.0
 */
public class FlyoutPaletteComposite
	extends Composite
{
	
private static final FontManager FONT_MGR = new FontManager();
	
private static final String PROPERTY_PALETTE_WIDTH
		= "org.eclipse.gef.ui.palette.fpa.paletteWidth"; //$NON-NLS-1$
private static final String PROPERTY_STATE
		= "org.eclipse.gef.ui.palette.fpa.state"; //$NON-NLS-1$
private static final String PROPERTY_DOCK_LOCATION
		= "org.eclipse.gef.ui.palette.fpa.dock"; //$NON-NLS-1$

private static final int DEFAULT_PALETTE_SIZE = 125;
private static final int MIN_PALETTE_SIZE = 20;
private static final int MAX_PALETTE_SIZE = 500;

private static final int STATE_HIDDEN = 8;
private static final int STATE_EXPANDED = 1;
/**
 * One of the two possible initial states of the flyout palette.  This is the default one.
 * When in this state, only the flyout palette's sash is visible.
 */
public static final int STATE_COLLAPSED = 2;
/**
 * One of the two possible initial states of the flyout palette.  When in this state,
 * the flyout palette is completely visible and pinned open so that it doesn't disappear
 * when the user wanders away from the flyout.
 */
public static final int STATE_PINNED_OPEN = 4;

private static final Image LEFT_ARROW = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/palette_left.gif").createImage();
private static final Image RIGHT_ARROW = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/palette_right.gif").createImage();

private PropertyChangeSupport listeners = new PropertyChangeSupport(this);
private Composite paletteContainer;
private Composite clientArea;
private String titleText = "";
private Control graphicalControl, sash;
private FlyoutPreferences prefs;
private Point cachedBounds = new Point(0, 0);
/*
 * Fix for Bug# 71525
 * transferFocus is used to transfer focus from the button in the vertical sash title
 * to the button in the horizontal paletteComposite title.  When either button is pressed
 * it is set to true, and when either the sash or the paletteComposite gets notified of 
 * the change in state, they transfer the focus to their button if this flag is set to
 * true and if that button is visible.
 */
private boolean transferFocus = false;
private int dock = PositionConstants.EAST;
private int paletteState = STATE_HIDDEN;
private int paletteWidth = DEFAULT_PALETTE_SIZE;
private int minWidth = MIN_PALETTE_SIZE;
private int cachedSize = -1, cachedState = -1, cachedLocation = -1;

/**
 * Constructor
 * 
 * @param	parent		The parent Composite
 * @param	style		The style of the widget to construct; only SWT.BORDER is allowed
 * @param	preferences	To save/retrieve the preferences for the flyout
 */
public FlyoutPaletteComposite(Composite parent, int style, FlyoutPreferences preferences, String titleText) {
	super(parent, style & SWT.BORDER);
	setTitleText(titleText);
	prefs = preferences;
	sash = createSash();
	paletteContainer = createPaletteContainer();
	
	// Initialize the state properly
	if (prefs.getPaletteWidth() <= 0)
		prefs.setPaletteWidth(DEFAULT_PALETTE_SIZE);
	setPaletteWidth(prefs.getPaletteWidth());
	setDockLocation(prefs.getDockLocation());
	updateState(null);

	addListener(SWT.Resize, new Listener() {
		public void handleEvent(Event event) {
			Rectangle area = getClientArea();
			/*
			 * @TODO:Pratik
			 * Sometimes, the editor is resized to 1,1 or 0,0 (depending on the platform)
			 * when the editor is closed or maximized.  We have to ignore such resizes.
			 * See Bug# 62748
			 */
			if (area.width > minWidth)
				layout(true);
		}
	});


	listeners.addPropertyChangeListener(new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			String property = evt.getPropertyName();
			if (property.equals(PROPERTY_PALETTE_WIDTH))
				prefs.setPaletteWidth(paletteWidth);
			else if (property.equals(PROPERTY_DOCK_LOCATION))
				prefs.setDockLocation(dock);
			else if (property.equals(PROPERTY_STATE))
				if (paletteState == STATE_COLLAPSED || paletteState == STATE_PINNED_OPEN)
					prefs.setPaletteState(paletteState);
		}
	});
}

public String getTitleText() {
	return titleText;
}

private void setTitleText(String titleText) {
	this.titleText = titleText;
}

public Composite getClientComposite() {
	return clientArea;
}

private void addListenerToCtrlHierarchy(Control parent, int eventType, 
		Listener listener) {
	parent.addListener(eventType, listener);
	if (!(parent instanceof Composite))
		return;
	Control[] children = ((Composite)parent).getChildren();
	for (int i = 0; i < children.length; i++) {
		addListenerToCtrlHierarchy(children[i], eventType, listener);
	}
}

private Control createFlyoutControlButton(Composite parent) {
	return new ButtonCanvas(parent);
}

/**
 * This is a convenient method to get a default FlyoutPreferences object.  The returned
 * FlyoutPreferences does not save any changes made to the given 
 * {@link Preferences Preferences}.  It's upto the owner plugin to 
 * {@link Plugin#savePluginPreferences() save} the changes before it
 * {@link Plugin#stop(org.osgi.framework.BundleContext) stops}.
 * @param prefs {@link Plugin#getPluginPreferences() a plugin's Preferences}
 * @return a default implementation of FlyoutPreferences that stores the settings in the
 * given Preferences
 * @since 3.2
 */
public static FlyoutPreferences createFlyoutPreferences(Preferences prefs) {
	return new DefaultFlyoutPreferences(prefs);
}

private Composite createPaletteContainer() {
	return new PaletteComposite(this, SWT.NONE);
}

private Control createSash() {
	return new Sash(this, SWT.NONE);
}

private Control createTitle(Composite parent, boolean isHorizontal) {
	return new TitleCanvas(parent, isHorizontal);
}

private Control getPaletteViewerControl() {
	Control result = null;
	if (clientArea != null)
		result = clientArea;
	// Fix for bug 101703 -- pViewer.getControl().getParent() might be parented 
	// by paletteContainer
	if (result != null && !result.isDisposed() && result.getParent() != paletteContainer)
		result = result.getParent();
	return result;
}

// Will return false if the ancestor or descendant is null
private boolean isDescendantOf(Control ancestor, Control descendant) {
	if (ancestor == null || descendant == null)
		return false;
	while (descendant != null) {
		if (ancestor == descendant)
			return true;
		descendant = descendant.getParent();
	}
	return false;
}

private boolean isInState(int state) {
	return (paletteState & state) != 0;
}

private boolean isMirrored() {
	return (getStyle() & SWT.MIRRORED) != 0;
}

/**
 * @see	Composite#layout(boolean)
 */
public void layout(boolean changed) {
	if (graphicalControl == null || graphicalControl.isDisposed()) 
		return;
	
	Rectangle area = getClientArea();
	if (area.width == 0 || area.height == 0) return;
	
	int sashWidth = sash.computeSize(-1, -1).x;
	int pWidth = paletteWidth;
	int maxWidth = Math.min(area.width / 2, MAX_PALETTE_SIZE);
	maxWidth = Math.max(maxWidth, minWidth);
	pWidth = Math.max(pWidth, minWidth);
	pWidth = Math.min(pWidth, maxWidth);
	
	/*
	 * Fix for Bug# 65892
	 * Laying out only when necessary helps reduce flicker on GTK in the case where the 
	 * flyout palette is being resized past its maximum size.
	 */
	if (paletteState == cachedState && pWidth == cachedSize && cachedLocation == dock 
			&& cachedBounds == getSize())
		return;
	cachedState = paletteState;
	cachedSize = pWidth;
	cachedLocation = dock;
	cachedBounds = getSize();
	
	setRedraw(false);
	if (isInState(STATE_HIDDEN)) {
		sash.setVisible(false);
		paletteContainer.setVisible(false);
		graphicalControl.setBounds(area);
	} else if (dock == PositionConstants.EAST)
		layoutComponentsEast(area, sashWidth, pWidth);
	else
		layoutComponentsWest(area, sashWidth, pWidth);
	setRedraw(true);
	update();
}

private void layoutComponentsEast(Rectangle area, int sashWidth, int pWidth) {
	if (isInState(STATE_COLLAPSED)) {
		paletteContainer.setVisible(false);
		sash.setBounds(area.x + area.width - sashWidth, area.y, sashWidth, area.height);
		graphicalControl.setBounds(area.x, area.y, area.width - sashWidth, area.height);
		sash.setVisible(true);
	} else if (isInState(STATE_EXPANDED)) {
		paletteContainer.moveAbove(graphicalControl);
		sash.moveAbove(paletteContainer);
		paletteContainer.setBounds(area.x + area.width - pWidth, area.y, 
				pWidth, area.height);
		sash.setBounds(area.x + area.width - pWidth - sashWidth, area.y, sashWidth, 
				area.height);
		graphicalControl.setBounds(area.x, area.y, area.width - sashWidth, area.height);
		sash.setVisible(true);
		paletteContainer.setVisible(true);
	} else if (isInState(STATE_PINNED_OPEN)) {
		paletteContainer.setBounds(area.x + area.width - pWidth, area.y, 
				pWidth, area.height);
		sash.setBounds(area.x + area.width - pWidth - sashWidth, area.y, sashWidth, 
				area.height);
		graphicalControl.setBounds(area.x, area.y, area.width - sashWidth - pWidth, 
				area.height);		
		sash.setVisible(true);
		paletteContainer.setVisible(true);
	}
}

private void layoutComponentsWest(Rectangle area, int sashWidth, int pWidth) {
	if (isInState(STATE_COLLAPSED)) {
		sash.setVisible(true);
		paletteContainer.setVisible(false);
		sash.setBounds(area.x, area.y, sashWidth, area.height);
		graphicalControl.setBounds(area.x + sashWidth, area.y,
				area.width - sashWidth, area.height);
	} else if (isInState(STATE_EXPANDED)) {
		sash.setVisible(true);
		paletteContainer.setVisible(true);
		paletteContainer.moveAbove(graphicalControl);
		sash.moveAbove(paletteContainer);
		paletteContainer.setBounds(area.x, area.y, pWidth, area.height);
		sash.setBounds(area.x + pWidth, area.y, sashWidth, area.height);
		graphicalControl.setBounds(area.x + sashWidth, area.y, 
				area.width - sashWidth, area.height);
	} else if (isInState(STATE_PINNED_OPEN)) {
		sash.setVisible(true);
		paletteContainer.setVisible(true);
		paletteContainer.setBounds(area.x, area.y, pWidth, area.height);
		sash.setBounds(area.x + pWidth, area.y, sashWidth, area.height);
		graphicalControl.setBounds(area.x + pWidth + sashWidth, area.y,
				area.width - sashWidth - pWidth, area.height);		
	}	
}

private void setDockLocation(int position) {
	if (position != PositionConstants.EAST && position != PositionConstants.WEST)
		return;
	if (position != dock) {
		int oldPosition = dock;
		dock = position;
		listeners.firePropertyChange(PROPERTY_DOCK_LOCATION, oldPosition, dock);
		if (clientArea != null)
			layout(true);
	}
}

private void setPaletteWidth(int newSize) {
	if (paletteWidth != newSize) {
		int oldValue = paletteWidth;
		paletteWidth = newSize;
		listeners.firePropertyChange(PROPERTY_PALETTE_WIDTH, oldValue, paletteWidth);
		if (clientArea != null)
			layout(true);
	}
}

/**
 * Sets the control along the side of which the palette is to be displayed.  The given
 * Control should be a child of this Composite.  This method should only be invoked once.
 * 
 * @param	graphicalViewer		the control of the graphical viewer; cannot be
 * 								<code>null</code>
 */
public void setGraphicalControl(Control graphicalViewer) {
	Assert.isTrue(graphicalViewer != null);
	Assert.isTrue(graphicalViewer.getParent() == this);
	Assert.isTrue(graphicalControl == null);
	graphicalControl = graphicalViewer;
	addListenerToCtrlHierarchy(graphicalControl, SWT.MouseEnter, new Listener() {
		public void handleEvent(Event event) {
			if (!isInState(STATE_EXPANDED))
				return;
			Display.getCurrent().timerExec(250, new Runnable() {
				public void run() {
					if (isDescendantOf(graphicalControl, 
							Display.getCurrent().getCursorControl())
							&& isInState(STATE_EXPANDED))
						setState(STATE_COLLAPSED);
				}
			});
		}
	});
}

/*
 * If the given state is invalid (as could be the case when 
 * FlyoutPreferences.getPaletteState() is invoked for the first time), it will be 
 * defaulted to STATE_COLLAPSED.
 */
public void setState(int newState) {
	/*
	 * Fix for Bug# 69617 and Bug# 81248
	 * FlyoutPreferences.getPaletteState() could return an invalid state if none is
	 * stored.  In that case, we use the default state: STATE_COLLAPSED.
	 */
	if (newState != STATE_HIDDEN && newState != STATE_PINNED_OPEN 
			&& newState != STATE_EXPANDED)
		newState = STATE_COLLAPSED;
	if (paletteState == newState)
		return;
	int oldState = paletteState;
	paletteState = newState;
	
	switch (paletteState) {
		case STATE_EXPANDED:
		case STATE_COLLAPSED:
		case STATE_PINNED_OPEN:
			if (clientArea == null) {
				clientArea = new Composite(paletteContainer, SWT.NONE);
				clientArea.setLayout(new FillLayout());				
				
				clientArea.addMouseTrackListener(new MouseTrackAdapter() {
					public void mouseExit(MouseEvent e) {
						if (isInState(STATE_COLLAPSED)) {
							setState(STATE_EXPANDED);
//							setState(STATE_PINNED_OPEN);
						}

					}
				});
				
//				ExpandBar bar = new ExpandBar(clientArea, SWT.V_SCROLL);
//				ExpandItem item0 = new ExpandItem (bar, SWT.NONE, 0);
//				item0.setText("Experiment");
//				Composite composite = new Composite (bar, SWT.NONE);
//				item0.setControl(composite);
//				ExpandItem item1 = new ExpandItem (bar, SWT.NONE, 1);
//				item1.setText("Instrument");
//				composite = new Composite (bar, SWT.NONE);
//				item1.setControl(composite);
//				ExpandItem item2 = new ExpandItem (bar, SWT.NONE, 2);
//				item2.setText("Analysis");
//				composite = new Composite (bar, SWT.NONE);
//				item2.setControl(composite);
				
//				if (externalViewer != null)
//					transferState(externalViewer, pViewer);
//				else
//					restorePaletteState(pViewer, capturedPaletteState);
				minWidth = Math.max(clientArea.computeSize(0, 0).x, 
						MIN_PALETTE_SIZE);
			}
			break;
		case STATE_HIDDEN:
			if (clientArea == null)
				break;
//			if (externalViewer != null) {
//				provider.getEditDomain().setPaletteViewer(externalViewer);
//				transferState(pViewer, externalViewer);
//			}
//			if (provider.getEditDomain().getPaletteViewer() == pViewer)
//				provider.getEditDomain().setPaletteViewer(null);
			Control pViewerCtrl = getPaletteViewerControl();
			if (pViewerCtrl != null && !pViewerCtrl.isDisposed())
				pViewerCtrl.dispose();
			clientArea = null;
	}
	/*
	 * Fix for Bug# 63901
	 * When the flyout collapses, if the palette has focus, throw focus to the
	 * graphical control.  That way, hitting ESC will still deactivate the current tool
	 * and load the default one.
	 * Note that focus is being set on RulerComposite and not GraphicalViewer's
	 * control.  But this is okay since RulerComposite passes the focus on to its
	 * first child, which is the graphical viewer's control.
	 */
	if (paletteState == STATE_COLLAPSED && clientArea.isFocusControl())
		graphicalControl.setFocus();
	layout(true);
	listeners.firePropertyChange(PROPERTY_STATE, oldState, newState);
}

//private void transferState(PaletteViewer src, PaletteViewer dest) {
//	restorePaletteState(dest, capturePaletteState(src));
//}

private void updateState(IWorkbenchPage page) {
//	IViewReference view = page.findViewReference(PaletteView.ID);
//	if (view == null && isInState(STATE_HIDDEN))
		setState(prefs.getPaletteState());
//	if (view != null && !isInState(STATE_HIDDEN))
//		setState(STATE_HIDDEN);
}

/**
 * FlyoutPreferences is used to save/load the preferences for the flyout palette.  
 * 
 * @author Pratik Shah
 * @since 3.0
 */
public interface FlyoutPreferences {
	/**
	 * Should return {@link PositionConstants#EAST} or {@link PositionConstants#WEST}. 
	 * Any other int will be ignored and the default dock location (EAST) will be 
	 * used instead.
	 * @return the saved dock location of the Palette
	 */
	int getDockLocation();
	/**
	 * When there is no saved state, this method can return any non-positive int (which 
	 * will result in the palette using the default state -- collapsed), or
	 * {@link FlyoutPaletteComposite#STATE_COLLAPSED}, or 
	 * {@link FlyoutPaletteComposite#STATE_PINNED_OPEN}
	 * @return	the saved state of the palette
	 */
	int getPaletteState();
	/**
	 * When there is no saved width, this method can return any int (preferrably a
	 * non-positive int).  Returning a non-positive int will cause the palette to be
	 * sized to the default size, whereas returning a postive int will find the
	 * closest match in the valid range (>= minimum and <= maximum)
	 * @return	the saved width of the flyout palette
	 */
	int getPaletteWidth();
	/**
	 * This method is invoked when the flyout palette's dock location is changed.  The
	 * provided dock location should be persisted and returned in 
	 * {@link #getDockLocation()}.
	 * @param	location	{@link PositionConstants#EAST} or {@link PositionConstants#WEST}
	 */
	void setDockLocation(int location);
	/**
	 * This method is invoked when the flyout palette's state is changed (the new state
	 * becomes the default).  The provided state should be persisted and returned in 
	 * {@link #getPaletteState()}.
	 * @param	state		{@link FlyoutPaletteComposite#STATE_COLLAPSED} or 
	 * {@link FlyoutPaletteComposite#STATE_PINNED_OPEN}
	 */
	void setPaletteState(int state);
	/**
	 * This method is invoked when the flyout palette is resized.  The provided width
	 * should be persisted and returned in {@link #getPaletteWidth()}.
	 * @param	width	the new size of the flyout palette
	 */
	void setPaletteWidth(int width);
}

private class Sash extends Composite {
	private Control button, title;
	public Sash(Composite parent, int style) {
		super(parent, style);
		button = createFlyoutControlButton(this);
		title = createTitle(this, false);
		new SashDragManager();
		
//		addMouseTrackListener(new MouseTrackAdapter() {
//			public void mouseHover(MouseEvent e) {
//				if (isInState(STATE_COLLAPSED))
//					setState(STATE_EXPANDED);
//			}
//		});

		addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event event) {
				paintSash(event.gc);
			}
		});
		
		addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				layout(true);
			}
		});
		
		listeners.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(PROPERTY_STATE))
					updateState();
			}
		});
	}
	/**
	 * @see org.eclipse.swt.widgets.Control#computeSize(int, int, boolean)
	 */
	public Point computeSize(int wHint, int hHint, boolean changed) {
		if (isInState(STATE_PINNED_OPEN))
			return new Point(6, 1);
		Point buttonSize = button.computeSize(wHint, hHint);
		Point titleSize = title.computeSize(wHint, hHint);
		return new Point(Math.max(buttonSize.x, titleSize.x) + 2, 
				buttonSize.y + titleSize.y + 7);
	}
	private void handleSashDragged(int shiftAmount) {
		int newSize = paletteContainer.getBounds().width 
				+ (dock == PositionConstants.EAST ? -shiftAmount : shiftAmount);
		setPaletteWidth(newSize);
	}
	/**
	 * @see org.eclipse.swt.widgets.Composite#layout(boolean)
	 */
	public void layout(boolean changed) {
		if (button == null || title == null)
			return;
		
		if (isInState(STATE_PINNED_OPEN)) {
			title.setVisible(false);
			button.setVisible(false);
			return;
		}
		
		title.setVisible(true);
		button.setVisible(true);
		Rectangle area = getClientArea();
		// 1 pixel margin all around to draw the raised border
		area.x += 1;
		area.y += 1;
		area.width -= 2;
		area.height -= 2;
		button.setBounds(area.x, area.y, area.width, area.width);
		// 5-pixel spacing
		area.y += area.width + 3;
		title.setBounds(area.x, area.y, area.width, 
				title.computeSize(-1, -1).y);
		if (transferFocus) {
			transferFocus = false;
			button.setFocus();
		}
	}
	private void paintSash(GC gc) {
		Rectangle bounds = getBounds();
		gc.setForeground(ColorConstants.buttonLightest);
		gc.drawLine(0, 0, bounds.width, 0);
		gc.drawLine(0, 0, 0, bounds.height);
		gc.setForeground(ColorConstants.buttonDarker);
		gc.drawLine(bounds.width - 1, 0, bounds.width - 1, bounds.height - 1);
		gc.drawLine(0, bounds.height - 1, bounds.width - 1, bounds.height - 1);
	}
	private void updateState() {
		setCursor(isInState(STATE_EXPANDED | STATE_PINNED_OPEN) 
				? SharedCursors.SIZEWE : null);
	}
	
	private class SashDragManager 
			extends MouseAdapter 
			implements MouseMoveListener {
		protected boolean dragging = false;
		protected boolean correctState = false;
		protected boolean mouseDown = false;
		protected int origX;
		protected Listener keyListener = new Listener() {
			public void handleEvent(Event event) {
				if (event.keyCode == SWT.ALT || event.keyCode == SWT.ESC) {
					dragging = false;
					Display.getCurrent().removeFilter(SWT.KeyDown, this);
				}
				event.doit = false;
				event.type = SWT.None;
			}
		};
		public SashDragManager() {
			Sash.this.addMouseMoveListener(this);
			Sash.this.addMouseListener(this);
		}
		public void mouseDown(MouseEvent me) {
			if (me.button != 1)
				return;
			mouseDown = true;
			correctState = isInState(STATE_EXPANDED | STATE_PINNED_OPEN);
			origX = me.x;
			Display.getCurrent().addFilter(SWT.KeyDown, keyListener);
		}
		public void mouseMove(MouseEvent me) {
			if (mouseDown)
				dragging = true;
			if (dragging && correctState)
				handleSashDragged(me.x - origX);
		}
		public void mouseUp(MouseEvent me) {
			Display.getCurrent().removeFilter(SWT.KeyDown, keyListener);
			if (!dragging && me.button == 1) {
				if (isInState(STATE_COLLAPSED)) {
//					setState(STATE_EXPANDED);					
					setState(STATE_PINNED_OPEN);
				}
				else if (isInState(STATE_EXPANDED))
					setState(STATE_COLLAPSED);
			}
			dragging = false;
			correctState = false;
			mouseDown = false;
		}
	}
}

private class ResizeAction extends Action {
	public ResizeAction() {
		super("Resize");
	}
	public boolean isEnabled() {
		return !isInState(STATE_COLLAPSED);
	}
	public void run() {
		final Tracker tracker = new Tracker(FlyoutPaletteComposite.this, 
				SWT.RIGHT | SWT.LEFT);
		Rectangle[] rects = new Rectangle[1];
		rects[0] = sash.getBounds();
		tracker.setCursor(SharedCursors.SIZEE);
		tracker.setRectangles(rects);
		tracker.setStippled(true);
		if (tracker.open()) {
			int deltaX = sash.getBounds().x - tracker.getRectangles()[0].x;
			if (dock == PositionConstants.WEST)
				deltaX = -deltaX;
			setPaletteWidth(paletteContainer.getBounds().width + deltaX);
		}
		tracker.dispose();
	}
}

private class TitleDragManager
		extends MouseAdapter
		implements Listener, MouseTrackListener {
	protected boolean switchDock = false;
	protected boolean dragging = false;
	protected int threshold;
	public TitleDragManager(Control ctrl) {
		ctrl.addListener(SWT.DragDetect, this);
		ctrl.addMouseListener(this);
		ctrl.addMouseTrackListener(this);
	}
	public void handleEvent(Event event) {
		dragging = true;
		switchDock = false;
		threshold = dock == PositionConstants.EAST ? Integer.MAX_VALUE / 2 : -1;
		final Composite flyout = FlyoutPaletteComposite.this;
		final Rectangle flyoutBounds = flyout.getBounds();
		final int switchThreshold = flyoutBounds.x + (flyoutBounds.width / 2);
		Rectangle bounds = sash.getBounds();
		if (paletteContainer.getVisible())
			bounds = bounds.union(paletteContainer.getBounds());
		final Rectangle origBounds = Display.getCurrent().map(flyout, null, bounds);
		final Tracker tracker = new Tracker(Display.getDefault(), SWT.NULL);
		tracker.setRectangles(new Rectangle[] {origBounds});
		tracker.setStippled(true);
		tracker.addListener(SWT.Move, new Listener() {
			public void handleEvent(final Event evt) {
				Display.getCurrent().syncExec(new Runnable() {
					public void run() {
						Control ctrl = Display.getCurrent().getCursorControl();
						Point pt = flyout.toControl(evt.x, evt.y);
						switchDock = isDescendantOf(graphicalControl, ctrl)
								&& ((dock == PositionConstants.WEST && pt.x > threshold - 10)
								|| (dock == PositionConstants.EAST && pt.x < threshold + 10));
						boolean invalid = false;
						if (!switchDock)
							invalid = !isDescendantOf(FlyoutPaletteComposite.this, ctrl);
						if (switchDock) {
							if (dock == PositionConstants.WEST) {
								threshold = Math.max(threshold, pt.x);
								threshold = Math.min(threshold, switchThreshold);
							} else {
								threshold = Math.min(threshold, pt.x);
								threshold = Math.max(threshold, switchThreshold);
							}
						}
						Rectangle placeHolder = origBounds;
						if (switchDock) {
							if (dock == PositionConstants.EAST)
								placeHolder = new Rectangle(0, 0,
										origBounds.width, origBounds.height);
							else
								placeHolder = new Rectangle(flyoutBounds.width 
										- origBounds.width, 0, origBounds.width, 
										origBounds.height);
							placeHolder =
									Display.getCurrent().map(flyout, null, placeHolder);
						}
						// update the cursor
						int cursor;
						if (invalid)
							cursor = DragCursors.INVALID;
						else if ((!switchDock && dock == PositionConstants.EAST)
								|| (switchDock && dock == PositionConstants.WEST))
							cursor = DragCursors.RIGHT;
						else
							cursor = DragCursors.LEFT;
						if (isMirrored()) {
							if (cursor == DragCursors.RIGHT)
								cursor = DragCursors.LEFT;
							else if (cursor == DragCursors.LEFT)
								cursor = DragCursors.RIGHT;
						}
						tracker.setCursor(DragCursors.getCursor(cursor));
						// update the rectangle only if it has changed
						if (!tracker.getRectangles()[0].equals(placeHolder))
							tracker.setRectangles(new Rectangle[] {placeHolder});
					}
				});
			}
		});
		if (tracker.open()) {
			if (switchDock)
				setDockLocation(PositionConstants.EAST_WEST & ~dock);
			// mouse up is received by the tracker and by this listener, so we set dragging
			// to be false
			dragging = false;
		}
		tracker.dispose();
	}
	public void mouseEnter(MouseEvent e) { }
	public void mouseExit(MouseEvent e) {	}
	
	public void mouseHover(MouseEvent e) {
		/*
		 * @TODO:Pratik   Mouse hover events are received if the hover occurs just before 
		 * you finish or cancel the drag.  Open a bugzilla about it?
		 */
//		if (isInState(STATE_COLLAPSED))
//			setState(STATE_EXPANDED);
	}
	public void mouseUp(MouseEvent me) {
		if (me.button != 1)
			return;
//		if (isInState(STATE_COLLAPSED))
//			setState(STATE_EXPANDED);
//		else if (isInState(STATE_EXPANDED))
//			setState(STATE_COLLAPSED);
		transferFocus = true;
		if (isInState(STATE_COLLAPSED))
			setState(STATE_PINNED_OPEN);
		else
			setState(STATE_COLLAPSED);
	}
}

private class PaletteComposite extends Composite {
	protected Control button, title;
	public PaletteComposite(Composite parent, int style) {
		super(parent, style);
		createComponents();

		listeners.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(PROPERTY_STATE))
					updateState();
				else if (evt.getPropertyName().equals(PROPERTY_DOCK_LOCATION))
					if (getVisible())
						layout(true);
			}
		});
		
		addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				layout(true);
			}
		});
		
		updateState();
	}
	protected void createComponents() {
		title = createTitle(this, true);
		button = createFlyoutControlButton(this);
	}
	public void layout(boolean changed) {
		Control pCtrl = getPaletteViewerControl();
		if (pCtrl == null || pCtrl.isDisposed())
			return;
		
		Rectangle area = getClientArea();
		if (title.getVisible()) {
			Point titleSize = title.computeSize(-1, -1);
			Point buttonSize = button.computeSize(-1, -1);
			int height = Math.max(titleSize.y, buttonSize.y);
			buttonSize.x = Math.max(height, buttonSize.x);
			if (dock == PositionConstants.EAST) {
				int buttonX = area.width - buttonSize.x;
				button.setBounds(buttonX, 0, buttonSize.x, height);
				// leave some space between the button and the title
				title.setBounds(0, 0, buttonX - 2, height);
			} else {
				int titleX = buttonSize.x + 2;
				button.setBounds(0, 0, buttonSize.x, height);
				title.setBounds(titleX, 0, area.width - titleX, height);
			}
			area.y += height;
			area.height -= height;
		}
		pCtrl.setBounds(area);
	}
	protected void updateState() {
		title.setVisible(isInState(STATE_PINNED_OPEN));
		button.setVisible(isInState(STATE_PINNED_OPEN));
		if (transferFocus && button.getVisible()) {
			transferFocus = false;
			button.setFocus();
		}
		layout(true);
	}
}

private class RotatedTitleLabel 
		extends ImageFigure {
	public RotatedTitleLabel() {
		FlyoutPaletteComposite.this.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (getImage() != null && !getImage().isDisposed())
					getImage().dispose();
			}
		});
	}
	protected void paintFigure(Graphics graphics) {
		if (getImage() == null)
			updateImage();
		super.paintFigure(graphics);
		if (hasFocus())
			graphics.drawFocus(0, 0, bounds.width - 1, bounds.height - 1);
	}
	public void setFont(Font f) {
		if (f != getLocalFont()) {
			super.setFont(f);
			updateImage();
		}
	}
	protected void updateImage() {
		if (getImage() != null)
			getImage().dispose();
		IFigure fig = new TitleLabel(false, getTitleText());
		fig.setFont(getFont());
		fig.setBackgroundColor(ColorConstants.button);
		fig.setOpaque(true);
		// This is a hack.  TitleLabel does not return a proper preferred size, since
		// its getInsets() method depends on its current size.  To make it work properly,
		// we first make the size really big.
		fig.setSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
		Dimension imageSize = fig.getPreferredSize(-1, -1);
		fig.setSize(imageSize);
		Image img = new Image(null, imageSize.width, imageSize.height);
		GC gc = new GC(img);
		Graphics graphics = new SWTGraphics(gc);
		fig.paint(graphics);
		graphics.dispose();
		gc.dispose();
		setImage(ImageUtilities.createRotatedImage(img));
		img.dispose();
	}
}

private static class TitleLabel extends Label {
	protected static final Border BORDER = new MarginBorder(0, 3, 0, 3);
	protected static final Border TOOL_TIP_BORDER = new MarginBorder(0, 2, 0, 2);
	private static final int H_GAP = 4;
//	private static final int LINE_LENGTH = 20;
	private static final int LINE_LENGTH = 0;	// Don't draw line
	private static final int MIN_LINE_LENGTH = 6;
	private boolean horizontal;
	public TitleLabel(boolean isHorizontal, String titleText) {
		super(titleText);
		horizontal = isHorizontal;
		setLabelAlignment(PositionConstants.LEFT);
		setBorder(BORDER);
		Label tooltip = new Label(getText());
		tooltip.setBorder(TOOL_TIP_BORDER);
		setToolTip(tooltip);
		setForegroundColor(ColorConstants.listForeground);
	}
	public Insets getInsets() {
		Insets insets = super.getInsets();
		Dimension diff = getBounds().getCropped(insets).getSize()
				.getDifference(getTextBounds().getSize());
		if (diff.width > 0) {
			insets = new Insets(insets);
			int width = Math.min(LINE_LENGTH + H_GAP, diff.width / 2);
			insets.left += width;
			insets.right += width;
		}
		return insets;
	}
	public IFigure getToolTip() {
		if (isTextTruncated())
			return super.getToolTip();
		return null;
	}
	protected void paintFigure(Graphics graphics) {
		super.paintFigure(graphics);
		org.eclipse.draw2d.geometry.Rectangle area = 
				getBounds().getCropped(super.getInsets());
		org.eclipse.draw2d.geometry.Rectangle textBounds = getTextBounds();
		// We reduce the width by 1 because FigureUtilities grows it by 1 unnecessarily
		textBounds.width--;

		if (hasFocus())
			graphics.drawFocus(bounds.getResized(-1, -1)
					.intersect(textBounds.getExpanded(getInsets())));
		
		int lineWidth = Math.min((area.width - textBounds.width - H_GAP * 2) / 2, 
				LINE_LENGTH);
		if (lineWidth >= MIN_LINE_LENGTH) {
			int centerY = area.height / 2;
			graphics.setForegroundColor(ColorConstants.buttonLightest);
			graphics.drawLine(textBounds.x - H_GAP - lineWidth, centerY - 3, 
					textBounds.x - H_GAP, centerY - 3);
			graphics.drawLine(textBounds.x - H_GAP - lineWidth, centerY + 2, 
					textBounds.x - H_GAP, centerY + 2);
			graphics.drawLine(textBounds.right() + H_GAP, centerY - 3, 
					textBounds.right() + H_GAP + lineWidth, centerY - 3);
			graphics.drawLine(textBounds.right() + H_GAP, centerY + 2, 
					textBounds.right() + H_GAP + lineWidth, centerY + 2);
			graphics.setForegroundColor(ColorConstants.buttonDarker);
			graphics.drawLine(textBounds.x - H_GAP - lineWidth, centerY + 3, 
					textBounds.x - H_GAP, centerY + 3);
			graphics.drawLine(textBounds.x - H_GAP - lineWidth, centerY - 2, 
					textBounds.x - H_GAP, centerY - 2);
			graphics.drawLine(textBounds.right() + H_GAP, centerY - 2, 
					textBounds.right() + H_GAP + lineWidth, centerY - 2);
			graphics.drawLine(textBounds.right() + H_GAP, centerY + 3, 
					textBounds.right() + H_GAP + lineWidth, centerY + 3);
			if (horizontal) {
				graphics.drawPoint(textBounds.x - H_GAP, centerY + 2);
				graphics.drawPoint(textBounds.x - H_GAP, centerY - 3);
				graphics.drawPoint(textBounds.right() + H_GAP + lineWidth, centerY - 3);
				graphics.drawPoint(textBounds.right() + H_GAP + lineWidth, centerY + 2);
				graphics.setForegroundColor(ColorConstants.buttonLightest);
				graphics.drawPoint(textBounds.x - H_GAP - lineWidth, centerY - 2);
				graphics.drawPoint(textBounds.x - H_GAP - lineWidth, centerY + 3);
				graphics.drawPoint(textBounds.right() + H_GAP, centerY - 2);
				graphics.drawPoint(textBounds.right() + H_GAP, centerY + 3);
			} else {
				graphics.drawPoint(textBounds.x - H_GAP - lineWidth, centerY + 2);
				graphics.drawPoint(textBounds.x - H_GAP - lineWidth, centerY - 3);
				graphics.drawPoint(textBounds.right() + H_GAP, centerY - 3);
				graphics.drawPoint(textBounds.right() + H_GAP, centerY + 2);
				graphics.setForegroundColor(ColorConstants.buttonLightest);
				graphics.drawPoint(textBounds.x - H_GAP, centerY - 2);
				graphics.drawPoint(textBounds.x - H_GAP, centerY + 3);
				graphics.drawPoint(textBounds.right() + H_GAP + lineWidth, centerY - 2);
				graphics.drawPoint(textBounds.right() + H_GAP + lineWidth, centerY + 3);
			}
		}
	}
}

private class ButtonCanvas extends Canvas {
	private LightweightSystem lws;
	public ButtonCanvas(Composite parent) {
		super(parent, SWT.NO_REDRAW_RESIZE | SWT.NO_BACKGROUND);
		init();
		provideAccSupport();
	}
	public Point computeSize(int wHint, int hHint, boolean changed) {
		Dimension size = lws.getRootFigure().getPreferredSize(wHint, hHint);
		size.union(new Dimension(wHint, hHint));
		return new org.eclipse.swt.graphics.Point(size.width, size.height);
	}
	private Image getButtonImage() {
		Image arrow = null;
		if (isInState(STATE_EXPANDED | STATE_PINNED_OPEN))
			arrow = dock == PositionConstants.WEST ? LEFT_ARROW : RIGHT_ARROW;
		else 
			arrow = dock == PositionConstants.WEST ? RIGHT_ARROW : LEFT_ARROW;
		if (isMirrored()) {
			if (arrow == LEFT_ARROW)
				arrow = RIGHT_ARROW;
			else
				arrow = LEFT_ARROW;
		}
		return arrow;	
	}
	private String getButtonTooltipText() {
		if (isInState(STATE_COLLAPSED))
			return "Show Bar";
		return "Hide Bar";
	}
	private void init() {
		setCursor(SharedCursors.ARROW);
		lws = new LightweightSystem();
		lws.setControl(this);
		final ImageButton b = new ImageButton(getButtonImage());
		b.setRolloverEnabled(true);
		b.setBorder(new ButtonBorder(ButtonBorder.SCHEMES.TOOLBAR));
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				transferFocus = true;
				if (isInState(STATE_COLLAPSED))
					setState(STATE_PINNED_OPEN);
				else
					setState(STATE_COLLAPSED);
			}
		});
		listeners.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(PROPERTY_STATE)) {
					b.setImage(getButtonImage());
					setToolTipText(getButtonTooltipText());
				} else if (evt.getPropertyName().equals(PROPERTY_DOCK_LOCATION))
					b.setImage(getButtonImage());
			}
		});
		lws.setContents(b);
	}
	private void provideAccSupport() {
		getAccessible().addAccessibleListener(new AccessibleAdapter() {
			public void getDescription(AccessibleEvent e) {
				e.result = "";
			}
			public void getHelp(AccessibleEvent e) {
				getDescription(e);
			}
			public void getName(AccessibleEvent e) {
				e.result = getToolTipText();
			}
		});
		getAccessible().addAccessibleControlListener(new AccessibleControlAdapter() {
			public void getRole(AccessibleControlEvent e) {
				e.detail = ACC.ROLE_PUSHBUTTON;
			}
		});
	}
	private class ImageButton extends Button {
		public ImageButton(Image img) {
			super();
			setContents(new ImageFigure(img));
		}
		public void setImage(Image img) {
			((ImageFigure)getChildren().get(0)).setImage(img);
		}
	}
}

private class TitleCanvas extends Canvas {
	private LightweightSystem lws;
	public TitleCanvas(Composite parent, boolean horizontal) {
		super(parent, SWT.NO_REDRAW_RESIZE | SWT.NO_BACKGROUND);
		init(horizontal);
		provideAccSupport();
	}
	/**
	 * @see org.eclipse.swt.widgets.Control#computeSize(int, int, boolean)
	 */
	public Point computeSize(int wHint, int hHint, boolean changed) {
		Dimension size = lws.getRootFigure().getPreferredSize(wHint, hHint);
		size.union(new Dimension(wHint, hHint));
		return new org.eclipse.swt.graphics.Point(size.width, size.height);
	}
	private void init(boolean isHorizontal) {
		IFigure fig;
		if (isHorizontal)
			fig = new TitleLabel(true, getTitleText());
		else
			fig = new RotatedTitleLabel();
		final IFigure contents = fig;
		contents.setRequestFocusEnabled(true);
		contents.setFocusTraversable(true);
		contents.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent fe) {
				fe.gainer.repaint();
			}
			public void focusLost(FocusEvent fe) {
				fe.loser.repaint();
			}
		});
		
		lws = new LightweightSystem();
		lws.setControl(this);
		lws.setContents(contents);
		setCursor(SharedCursors.SIZEALL);
		FONT_MGR.register(this);
		new TitleDragManager(this);
		final MenuManager manager = new MenuManager();
		MenuManager mgr = new MenuManager("Dock");
		mgr.add(new ChangeDockAction("Left", PositionConstants.WEST));
		mgr.add(new ChangeDockAction("Right", PositionConstants.EAST));
		manager.add(new ResizeAction());
		manager.add(mgr);
		setMenu(manager.createContextMenu(this));
		mgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager menuMgr) {
				IContributionItem[] items = menuMgr.getItems();
				for (int i = 0; i < items.length; i++) {
					((ActionContributionItem)items[i]).update();
				}
			}
		});
		
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				FONT_MGR.unregister(TitleCanvas.this);
				manager.dispose();
			}
		});		
	}
	private void provideAccSupport() {
		getAccessible().addAccessibleListener(new AccessibleAdapter() {
			public void getDescription(AccessibleEvent e) {
				e.result = getTitleText();
			}
			public void getHelp(AccessibleEvent e) {
				getDescription(e);
			}
			public void getName(AccessibleEvent e) {
				e.result = getTitleText();
			}
		});
		getAccessible().addAccessibleControlListener(new AccessibleControlAdapter() {
			public void getRole(AccessibleControlEvent e) {
				e.detail = ACC.ROLE_SLIDER;
			}
		});
	}
	public void setFont(Font font) {
		((IFigure)lws.getRootFigure().getChildren().get(0)).setFont(font);
		if (isVisible()) {
			/*
			 * If this canvas is in the sash, we want the FlyoutPaletteComposite
			 * to layout (which will cause the sash to be resized and laid out).  
			 * However, if this canvas is in the paletteContainer, the 
			 * paletteContainer's bounds won't change, and hence it won't layout.
			 * Thus, we also invoke getParent().layout().
			 */
			FlyoutPaletteComposite.this.layout(true);
			getParent().layout(true);
		}
	}
}

private class ChangeDockAction extends Action {
	private int position;
	/**
	 * Constructor
	 * @param	text		this action's text
	 * @param	position	the dock side that this action represents: 
	 * 						PositionConstants.EAST or PositionConstants.WEST 
	 */
	public ChangeDockAction(String text, int position) {
		super(text, IAction.AS_RADIO_BUTTON);
		this.position = position;
	}
	/**
	 * This Action is checked when the palette is docked on the side this action
	 * represents
	 * @see org.eclipse.jface.action.IAction#isChecked()
	 */
	public boolean isChecked() {
		return dock == position;
	}
	/**
	 * Changes the palette's dock location to the side this action represents
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		setDockLocation(position);
	}
}

private static class FontManager {
	private final String fontName = getFontType();
	private List registrants = new ArrayList();
	private Font titleFont;
	private boolean newFontCreated = false;
	private final IPropertyChangeListener fontListener = new IPropertyChangeListener() {
		public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
			if (fontName.equals(event.getProperty()))
				handleFontChanged();
		}
	};
	private FontManager() {
	}
	protected final Font createTitleFont() {
		newFontCreated = false;
		FontData[] data = JFaceResources.getFont(fontName).getFontData();
		for (int i = 0; i < data.length; i++)
			if ((data[i].getStyle() & SWT.BOLD) == 0) {
				/*
				 * @TODO:Pratik  need to test this in an environment where there are
				 * multiple FontDatas for a font
				 */
				// Any problems with style settings (eg., in the case of a font that
				// does not support bold case), will cause the font to ignore that style
				// setting.
				data[i].setStyle(data[i].getStyle() | SWT.BOLD);
				newFontCreated = true;
			}
		if (newFontCreated)
			return new Font(Display.getCurrent(), data);
		return JFaceResources.getFont(fontName);
	}
	protected void dispose() {
		if (newFontCreated && titleFont != null && !titleFont.isDisposed())
			titleFont.dispose();
		titleFont = null;
		JFaceResources.getFontRegistry().removeListener(fontListener);
	}
	protected String getFontType() {
		return JFaceResources.DIALOG_FONT;
	}
	protected void handleFontChanged() {
		if (titleFont == null)
			return;
		Font oldFont = titleFont;
		titleFont = createTitleFont();
		for (Iterator iter = registrants.iterator(); iter.hasNext();)
			((Control)iter.next()).setFont(titleFont);
		oldFont.dispose();
	}
	protected void init() {
		titleFont = createTitleFont();
		JFaceResources.getFontRegistry().addListener(fontListener);		
	}
	public void register(Control ctrl) {
		if (titleFont == null)
			init();
		ctrl.setFont(titleFont);
		registrants.add(ctrl);
	}
	public void unregister(Control ctrl) {
		registrants.remove(ctrl);
		if (registrants.isEmpty())
			dispose();
	}
}

/**
 * Default implementation of FlyoutPreferences that stores the flyout palette settings
 * in the given Preferences.
 * @author Pratik Shah
 * @since 3.2
 */
private static class DefaultFlyoutPreferences implements FlyoutPreferences {
	/*
	 * There's no need to set the default for these properties since the default-default 
	 * of 0 for ints will suffice.
	 */
	private static final String PALETTE_DOCK_LOCATION = "org.gumtree.ui.pdock"; //$NON-NLS-1$
	private static final String PALETTE_SIZE = "org.gumtree.ui.psize"; //$NON-NLS-1$
	private static final String PALETTE_STATE = "org.gumtree.ui.pstate"; //$NON-NLS-1$

	private Preferences prefs;

	private DefaultFlyoutPreferences(Preferences preferences) {
		prefs = preferences;
	}
	public int getDockLocation() {
		// Set default
		if (!prefs.contains(PALETTE_DOCK_LOCATION)) {
			setDockLocation(PositionConstants.WEST);
		}
		return prefs.getInt(PALETTE_DOCK_LOCATION);
	}
	public int getPaletteState() {
		if (!prefs.contains(PALETTE_STATE)) {
			setPaletteState(STATE_PINNED_OPEN);
		}
		return prefs.getInt(PALETTE_STATE);
	}
	public int getPaletteWidth() {
		return prefs.getInt(PALETTE_SIZE);
	}
	public void setDockLocation(int location) {
		prefs.setValue(PALETTE_DOCK_LOCATION, location);
	}
	public void setPaletteState(int state) {
		prefs.setValue(PALETTE_STATE, state);
	}
	public void setPaletteWidth(int width) {
		prefs.setValue(PALETTE_SIZE, width);
	}	
}

}