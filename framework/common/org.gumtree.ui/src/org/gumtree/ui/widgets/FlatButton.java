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

package org.gumtree.ui.widgets;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.gumtree.util.messaging.ListenerManager;
import org.gumtree.util.messaging.SafeListenerRunnable;

public class FlatButton extends Composite implements IWidget {

	private ListenerManager<SelectionListener> listeners;

	private Label label;
	
	private boolean selected;

	private String text;
	
	private Color originalBackground;
	
	private Color originalForeground;
	
	private Color highlightBackground;
	
	private Color highlightForeground;
	
	private Color clickBackground;
	
	private Cursor handCursor;
	
	public FlatButton(Composite parent, int style) {
		super(parent, style);
		GridLayoutFactory.swtDefaults().margins(1, 1).spacing(0, 0).applyTo(this);
		label = new Label(this, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(label);
		listeners = new ListenerManager<SelectionListener>();
		
		originalBackground = getBackground();
		originalForeground = getForeground();
		highlightBackground = getDisplay().getSystemColor(SWT.COLOR_GRAY);
		highlightForeground = getDisplay().getSystemColor(SWT.COLOR_WHITE);
		clickBackground = getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
		handCursor = new Cursor(getDisplay(), SWT.CURSOR_HAND);
		label.setCursor(handCursor);
		
		label.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				setForegroundColor(highlightForeground);
				setBackgroundColor(clickBackground);
			}
			public void mouseUp(MouseEvent e) {
				setSelection(!getSelection());
				handleSelection(getSelection());
			}
		});
		
		label.addMouseTrackListener(new MouseTrackAdapter() {
			public void mouseEnter(MouseEvent e) {
				setForegroundColor(highlightForeground);
				setBackgroundColor(highlightBackground);
			}
			public void mouseExit(MouseEvent e) {
				paint(getSelection());
			}
		});
		
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (listeners != null) {
					listeners.clearListeners();
					listeners = null;
				}
				if (handCursor != null) {
					handCursor.dispose();
					handCursor = null;
				}
				label = null;
			}
		});
	}

	private void handleSelection(boolean selection) {
		// Paint
		paint(selection);
		
		// Send event
		final Event event = new Event();
		event.display = getDisplay();
		event.widget = FlatButton.this;
		listeners.syncInvokeListeners(
			new SafeListenerRunnable<SelectionListener>() {
				public void run(SelectionListener listener)
						throws Exception {
					SelectionEvent selectionEvent = new SelectionEvent(event);
					listener.widgetSelected(selectionEvent);
				}
			});
	}
	
	private void paint(boolean selection) {
		if (selection) {
			setForegroundColor(highlightForeground);
			setBackgroundColor(highlightBackground);
		} else {
			setForegroundColor(originalForeground);
			setBackgroundColor(originalBackground);
		}
	}
	
	private void setBackgroundColor(Color background) {
		super.setBackground(background);
		label.setBackground(background);
	}
	
	private void setForegroundColor(Color foreground) {
		super.setForeground(foreground);
		label.setForeground(foreground);
	}

	public void addSelectionListener(SelectionListener listener) {
		checkWidget ();
		listeners.addListenerObject(listener);
	}

	public void removeSelectionListener(SelectionListener listener) {
		checkWidget ();
		listeners.removeListenerObject(listener);
	}

	public Color getBackground() {
		checkWidget();
		return originalBackground;
	}
	
	public void setBackground(Color background) {
		checkWidget();
		originalBackground = background;
		paint(getSelection());
		super.setBackground(background);
	}
	
	public Color getForeground() {
		checkWidget();
		return originalForeground;
	}
	
	public void setForeground(Color foreground) {
		checkWidget();
		originalForeground = foreground;
		paint(getSelection());
		super.setForeground(foreground);
	}
	
	public Color getHighlightBackground() {
		checkWidget();
		return highlightBackground;
	}
	
	public void setHighlightBackground(Color highlightBackground) {
		checkWidget();
		this.highlightBackground = highlightBackground;
		paint(getSelection());
	}
	
	public Color getHighlightForeground() {
		checkWidget();
		return highlightForeground;
	}
	
	public void setHighlightForeground(Color highlightForeground) {
		checkWidget();
		this.highlightForeground = highlightForeground;
		paint(getSelection());
	}
	
	public Color getClickBackground() {
		checkWidget();
		return clickBackground;
	}
	
	public void setClickBackground(Color clickBackground) {
		checkWidget();
		this.clickBackground = clickBackground;
	}
	
	public boolean getSelection() {
		checkWidget();
		return selected;
	}
	
	public void setSelection(boolean selection) {
		checkWidget();
		selected = selection;
		paint(selected);
	}
	
	public String getText () {
		checkWidget();
		return text;
	}
	
	public void setText(String text) {
		checkWidget();
		this.text = text;
		label.setText(text);
	}
	
	public Font getFont() {
		checkWidget();
		return label.getFont();
	}
	
	public void setFont(Font font) {
		checkWidget();
		label.setFont(font);
	}
	
}
