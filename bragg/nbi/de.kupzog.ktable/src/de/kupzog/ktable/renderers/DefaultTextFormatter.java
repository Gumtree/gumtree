package de.kupzog.ktable.renderers;

public class DefaultTextFormatter implements ITextFormatter {
	private static DefaultTextFormatter inst;
	
	private DefaultTextFormatter() {}
	
	public static synchronized DefaultTextFormatter getInst() {
		if (inst == null) {
			inst = new DefaultTextFormatter();
		}
		return inst;
	}
	
	public String format(Object object) {
		return object != null ? object.toString() : null;
	}
	
}
