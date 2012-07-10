package org.gumtree.gumnix.sics.ui.util;

import org.gumtree.ui.util.jface.ITreeColumn;

public class ControlViewerConstants {
	public enum Column implements ITreeColumn {
		NODE("Node"), DEVICE("Device"), TARGET("Target"), CURRENT("Current"), STATUS("Status"), MESSAGE("Message");
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
		public String getName() {
			return name();
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
