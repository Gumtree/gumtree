package de.kupzog.ktable;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public interface IKTableTooltip {
	public void show(KTable ktable, TooltipAssistant calc);
	public void dispose(KTable ktable);
	public boolean isDisposed();
	public boolean isEmpty();
	public boolean isLocked();

	public interface TooltipAssistant {
		public Rectangle calcBounds(Point size);
	}
}