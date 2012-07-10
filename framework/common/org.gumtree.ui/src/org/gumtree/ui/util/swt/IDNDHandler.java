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

package org.gumtree.ui.util.swt;

import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.DragDetectEvent;

public interface IDNDHandler<T> {

	public void handleDrop(DropTargetEvent event);
	
	public void handleDrag(DragDetectEvent event);
	
	public T getHost();
	
	public void setHost(T host);
	
}
