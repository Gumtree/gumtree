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

package org.gumtree.widgets.swt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.gumtree.widgets.swt.util.SafeUIRunner;

public class FlatButton extends ExtendedComposite {

	private List<SelectionListener> listeners;

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
		GridLayoutFactory.swtDefaults().margins(1, 1).spacing(0, 0)
				.applyTo(this);
		label = getWidgetFactory().createLabel(this, "");
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)
				.applyTo(label);
		listeners = new ArrayList<SelectionListener>(2);

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
	}

	@Override
	protected void disposeWidget() {
		if (listeners != null) {
			listeners.clear();
			listeners = null;
		}
		if (handCursor != null) {
			handCursor.dispose();
			handCursor = null;
		}
		label = null;
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

	/*************************************************************************
	 * Events
	 *************************************************************************/
	
	public void addSelectionListener(SelectionListener listener) {
		checkWidget();
		listeners.add(listener);
	}

	public void removeSelectionListener(SelectionListener listener) {
		checkWidget();
		listeners.remove(listener);
	}
	
	private void handleSelection(boolean selection) {
		// Paint
		paint(selection);

		// Send event
		final Event event = new Event();
		event.display = getDisplay();
		event.widget = FlatButton.this;
		SafeUIRunner.asyncExec(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				for (SelectionListener listener : listeners) {
					SelectionEvent selectionEvent = new SelectionEvent(event);
					listener.widgetSelected(selectionEvent);
				}
			}
		});
	}

	/*************************************************************************
	 * Properties
	 *************************************************************************/
	
	@Override
	public Color getBackground() {
		checkWidget();
		return originalBackground;
	}

	@Override
	public void setBackground(Color background) {
		checkWidget();
		originalBackground = background;
		paint(getSelection());
		super.setBackground(background);
	}

	@Override
	public Color getForeground() {
		checkWidget();
		return originalForeground;
	}

	@Override
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

	public String getText() {
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

	/*************************************************************************
	 * Utilities
	 *************************************************************************/
	
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		GridLayoutFactory.swtDefaults().applyTo(shell);

		FlatButton flatButton = new FlatButton(shell, SWT.NONE);
		flatButton.setText("Button");
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)
				.grab(true, true).applyTo(flatButton);

		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

}
