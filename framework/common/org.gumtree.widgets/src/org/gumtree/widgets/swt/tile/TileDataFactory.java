package org.gumtree.widgets.swt.tile;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;

public final class TileDataFactory {

	private static final int DEFAULT_UNIT = 12;

	private GridData data;

	private int unit;

	private TileDataFactory(GridData data, int unit) {
		this.data = data;
		this.unit = unit;
	}

	public TileDataFactory size(int width, int height) {
		data.widthHint = calculateActualSize(unit, width);
		data.heightHint = calculateActualSize(unit, height);
		data.horizontalSpan = width;
		data.verticalSpan = height;
		return this;
	}
	
	public void applyTo(Control control) {
		control.setLayoutData(GridDataFactory.copyData(data));
	}

	public static TileDataFactory create() {
		return create(DEFAULT_UNIT);
	}

	public static TileDataFactory create(int unit) {
		return new TileDataFactory(new GridData(), DEFAULT_UNIT);
	}
	private static int calculateActualSize(int unit, int size) {
		return unit * (3 * size - 1);
	}

}
