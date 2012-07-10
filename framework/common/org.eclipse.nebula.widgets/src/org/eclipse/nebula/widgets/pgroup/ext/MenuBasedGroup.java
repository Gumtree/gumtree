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

package org.eclipse.nebula.widgets.pgroup.ext;

import org.eclipse.nebula.widgets.pgroup.PGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;

public class MenuBasedGroup extends PGroup {
	
	private Menu menu;
	
	private boolean clickExpandEnabled;

	public MenuBasedGroup(Composite parent, int style) {
		super(parent, style);
		menu = new Menu(this);
		clickExpandEnabled = true;
		setStrategy(new MenuBasedGroupStrategy());
		setToggleRenderer(new MenuBasedToggleRenderer());
	}

	protected void initListeners() {
		super.initListeners();
		addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event event) {
				onMouseDoubleClick(event);
			}
		});
	}
	
	protected void onMouseDown(Event e) {
		 if (overToggle && e.button == 1) {
			 if (menu != null) {
				 menu.setVisible(true);
			 }
		}
	}
	
	public Menu getMenu() {
		return menu;
	}
	
	protected void onMouseDoubleClick(Event e) {
		// Change the behaviour: use double click to toggle expansion
        if (e.button == 1 && isClickExpandEnabled())
        {
            setExpanded(!expanded);
            setFocus();
            if (expanded)
            {
                notifyListeners(SWT.Expand, new Event());
            }
            else
            {
                notifyListeners(SWT.Collapse, new Event());
            } 
        }
	}

	
	public boolean isClickExpandEnabled() {
		return clickExpandEnabled;
	}

	public void setClickExpandEnabled(boolean clickExpandEnabled) {
		this.clickExpandEnabled = clickExpandEnabled;
	}
	
}
