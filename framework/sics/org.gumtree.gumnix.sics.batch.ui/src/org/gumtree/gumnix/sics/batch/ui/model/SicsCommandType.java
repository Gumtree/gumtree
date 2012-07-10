/*****************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *****************************************************************************/

package org.gumtree.gumnix.sics.batch.ui.model;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.gumtree.core.object.ObjectNotFoundException;
import org.gumtree.gumnix.sics.batch.ui.commands.CountableCommand;
import org.gumtree.gumnix.sics.batch.ui.commands.DevicePropertyCommand;
import org.gumtree.gumnix.sics.batch.ui.commands.DrivableCommand;
import org.gumtree.gumnix.sics.batch.ui.commands.LineScriptCommand;
import org.gumtree.gumnix.sics.batch.ui.commands.ScriptCommand;
import org.gumtree.gumnix.sics.batch.ui.commands.ServerCommand;
import org.gumtree.gumnix.sics.batch.ui.commands.SicsVariableCommand;
import org.gumtree.gumnix.sics.batch.ui.commands.UserDefinedCommand;
import org.gumtree.gumnix.sics.batch.ui.internal.InternalImage;
import org.gumtree.gumnix.sics.batch.ui.views.DevicePropertyView;
import org.gumtree.gumnix.sics.batch.ui.views.DrivableCommandView;
import org.gumtree.gumnix.sics.batch.ui.views.ISicsCommandView;
import org.gumtree.gumnix.sics.batch.ui.views.LineScriptView;
import org.gumtree.gumnix.sics.batch.ui.views.ScriptView;
import org.gumtree.gumnix.sics.batch.ui.views.DynamicCommandView;
import org.gumtree.gumnix.sics.batch.ui.views.SicsVariableView;

public enum SicsCommandType {

	LINE_SCRIPT("Line Script", new RGB(0, 0, 255), InternalImage.BLUE,
			LineScriptCommand.class, LineScriptView.class),
			
	SCRIPT("Script", new RGB(255, 0, 0), InternalImage.RED,
			ScriptCommand.class, ScriptView.class),
			
	SICS_VARIABLE("Variable", new RGB(0, 128, 0), InternalImage.DARK_GREEN,
			SicsVariableCommand.class, SicsVariableView.class),
			
	DRIVABLE("Drivable", new RGB(64, 128, 128), InternalImage.CYAN,
			DrivableCommand.class, DrivableCommandView.class),
	
	DEVICE_PROPERTY("Device", new RGB(128, 64, 0), InternalImage.DARK_ORANGE,
			DevicePropertyCommand.class, DevicePropertyView.class),
	
	USER_DEFINED("User", new RGB(255, 0, 255), InternalImage.MAGENTA,
			UserDefinedCommand.class, DynamicCommandView.class),
			
	COUNTABLE("Countable", new RGB(155, 128, 0), InternalImage.ORANGE,
			CountableCommand.class, DynamicCommandView.class),
			
	SERVER_COMMAND("Command", new RGB(0, 0, 0), InternalImage.BLACK,
			ServerCommand.class, DynamicCommandView.class);
	
	private SicsCommandType(String label, RGB defaultRGB, InternalImage image,
			Class<? extends ISicsCommandElement> commandClass,
			Class<? extends ISicsCommandView<? extends ISicsCommandElement>> commandViewClass) {
		this.label = label;
		this.defaultRGB = defaultRGB;
		this.image = image;
		this.commandClass = commandClass;
		this.commandViewClass = commandViewClass;
	}
	
	public String getLabel() {
		return label;
	}
	
	public RGB getDefaultRGB() {
		return defaultRGB;
	}
	
	public Image getImage() {
		return image.getImage();
	}
	
	public Class<? extends ISicsCommandElement> getCommandClass() {
		return commandClass;
	}
	
	public Class<? extends ISicsCommandView<? extends ISicsCommandElement>> getCommandViewClass() {
		return commandViewClass;
	}
	
	public static SicsCommandType getType(Class<? extends ISicsCommandElement> commandClass) {
		for (SicsCommandType type : values()) {
			if (type.getCommandClass().equals(commandClass)) {
				return type;
			}
		}
		throw new ObjectNotFoundException(commandClass + " has no associated type.");
	}
	
	private String label;
	
	private RGB defaultRGB;
	
	private InternalImage image;
	
	private Class<? extends ISicsCommandElement> commandClass;
	
	private Class<? extends ISicsCommandView<? extends ISicsCommandElement>> commandViewClass;
	
}
