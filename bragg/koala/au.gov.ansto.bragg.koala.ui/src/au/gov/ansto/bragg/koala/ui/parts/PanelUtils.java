package au.gov.ansto.bragg.koala.ui.parts;

public class PanelUtils {

	public PanelUtils() {
	}

	public static String convertTimeString(int time) {
		if (time / 3600 > 0) {
			return String.format("%dh ", time / 3600) + convertTimeString(time % 3600);
		} else if (time / 60 > 0) {
			return String.format("%02dm ", time / 60) + convertTimeString(time % 60);
		} else if (time > 0) {
			return String.format("%02ds", time);
		} else {
			return "";
		}
	}
		
}
