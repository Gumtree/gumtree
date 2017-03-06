package org.gumtree.msw.ui.ktable;

import java.util.Arrays;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.gumtree.msw.elements.Element;
import org.gumtree.msw.ui.Resources;

import org.gumtree.msw.ui.ktable.KTable;

class SettingsCellRenderer extends ButtonRenderer {
	// fields
	private final Menu menu;

	// construction
	public SettingsCellRenderer(KTable table, int optimalWidth, Menu menu) {
		super(
				table,
				optimalWidth,
				Arrays.asList(
						new ButtonInfo<Element>(Resources.IMAGE_SETTINGS_DROPDOWN, Resources.IMAGE_SETTINGS_DROPDOWN, null)));

		this.menu = menu;
	}

	// methods
	@Override
	protected int isValidColumn(int x, int y) {
		Rectangle rect = table.getCellRect(0, 0);
		return (0 <= x) && (x < rect.width) ? 0 : -1;
	}
	@Override
	protected int isValidRow(int x, int y) {
		int row = table.getRowForY(y);
		return row == 0 ? row : -1;
	}
	@Override
	protected void clicked(int col, int row, int index) {
		if (menu != null) {
        	Point point = table.toDisplay(0, KTableResources.COLUMN_HEADER_HEIGHT);
			menu.setLocation(point.x, point.y);
			menu.setVisible(true);
		}
	}
}
