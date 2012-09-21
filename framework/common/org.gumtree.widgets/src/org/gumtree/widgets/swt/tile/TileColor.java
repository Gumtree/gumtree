package org.gumtree.widgets.swt.tile;

import org.eclipse.swt.graphics.Color;
import org.gumtree.widgets.swt.util.UIResourceManager;

public enum TileColor {
	
	AMBER(240, 163, 10),
	BROWN(130, 90, 43),
	COBALT(0, 80, 239),
	CRIMSON(161, 0, 37),
	CYAN(27, 161, 226),
	MAGENTA(216, 0, 115),
	LIME(216, 0, 115),
	INDIGO(105, 0, 254),
	GREEN(95, 168, 23),
	EMERALD(27, 161, 226),
	MAUVE(118, 96, 138),
	OLIVE(109, 135, 100),
	ORANGE(250, 104, 0),
	PINK(244, 113, 208),
	RED(229, 20, 0),
	SIENNA(112, 59, 63),
	STEEL(100, 118, 135),
	TEAL(0, 170, 168),
	VIOLET(170, 0, 255),
	YELLOW(216, 193, 0);

	private TileColor(int red, int green, int blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public Color getColor() {
		if (color == null) {
			color = resourceManager.createColor(red, green, blue);
		}
		return color;
	}

	private int red;

	private int green;

	private int blue;

	private Color color;

	private static UIResourceManager resourceManager = new UIResourceManager();

}
