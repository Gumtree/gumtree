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

package org.gumtree.control.ui.batch.command;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.gumtree.control.batch.tasks.ISicsCommand;
import org.gumtree.control.ui.viewer.InternalImage;
import org.gumtree.control.ui.batch.taskeditor.*;
import org.gumtree.core.object.ObjectNotFoundException;

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
			Class<? extends ISicsCommand> commandClass,
			Class<? extends ISicsCommandView<? extends ISicsCommand>> commandViewClass) {
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
	
	public Class<? extends ISicsCommand> getCommandClass() {
		return commandClass;
	}
	
	public Class<? extends ISicsCommandView<? extends ISicsCommand>> getCommandViewClass() {
		return commandViewClass;
	}
	
	public static SicsCommandType getType(Class<? extends ISicsCommand> commandClass) {
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
	
	private Class<? extends ISicsCommand> commandClass;
	
	private Class<? extends ISicsCommandView<? extends ISicsCommand>> commandViewClass;
	
}
