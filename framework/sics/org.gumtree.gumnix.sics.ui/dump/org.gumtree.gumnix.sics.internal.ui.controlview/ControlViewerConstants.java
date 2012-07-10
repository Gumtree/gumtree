package org.gumtree.gumnix.sics.internal.ui.controlview;

public class ControlViewerConstants {
	public enum Column {
		NODE("Component"), STATUS("Status"), CURRENT("Current"), TARGET("Target"), STATUS_COLOUR("Colour");
		private Column(String label) {
			this.label = label;
		}
		public String getLabel() {
			return label;
		}
		public int getIndex() {
			for(int i = 0; i < Column.values().length; i++) {
				if(this.equals(Column.values()[i])) {
					return i;
				}
			}
			return -1;
		}
		public static String[] getAllLabels() {
			String[] labels = new String[Column.values().length];
			for(int i = 0; i < Column.values().length; i++) {
				labels[i] = Column.values()[i].getLabel();
			}
			return labels;
		}
		public String toString() {
			return getLabel();
		}
		private String label;
	}

	private ControlViewerConstants() {
		super();
	}

}
