package au.gov.ansto.bragg.quokka.msw.util;

import java.util.Map;

public interface IExperimentDescriptionLoaderListener {
	// methods
	public void onLoaded(Map<String, String> response);
}
