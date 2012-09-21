package org.gumtree.widgets.swt.tile;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class TileLayoutFactory {

	private static final int DEFAULT_UNIT = 12;
	
	private static final int MARGAIN = 24;
	
	private GridLayout layout;

	private int unit;
	
	private TileLayoutFactory(GridLayout layout, int unit) {
		this.layout = layout;
		this.unit = unit;
		margins(MARGAIN, MARGAIN);
		spacing(unit, unit);
		layout.makeColumnsEqualWidth = true;
	}
	
	public TileLayoutFactory margins(int width, int height) {
		layout.marginWidth = width;
		layout.marginHeight = height;
		return this;
	}

	public TileLayoutFactory spacing(int hSpacing, int vSpacing) {
		layout.horizontalSpacing = hSpacing;
		layout.verticalSpacing = vSpacing;
        return this;
    }
	
	public TileLayoutFactory numColumns(int numColumns) {
		layout.numColumns = numColumns;
		return this;
	}
	
	public void applyTo(Composite composite) {
		composite.setLayout(GridLayoutFactory.copyLayout(layout));
	}

	public static TileLayoutFactory create() {
		return create(DEFAULT_UNIT);
	}

	public static TileLayoutFactory create(int unit) {
		return new TileLayoutFactory(new GridLayout(), DEFAULT_UNIT);
	}
	
}
