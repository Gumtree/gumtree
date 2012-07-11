package au.gov.ansto.bragg.quokka.experiment.util;

import java.io.InputStream;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.gumtree.util.eclipse.EclipseUtils;
import org.junit.Test;

import au.gov.ansto.bragg.quokka.core.tests.Activator;
import au.gov.ansto.bragg.quokka.experiment.model.Experiment;
import au.gov.ansto.bragg.quokka.experiment.model.Sample;

public class ExperimentModelUtilsTest {

	@Test
	public void testLoadExcel() throws Exception {
		// Create model
		Experiment experiment = new Experiment();
		Sample sample1 = new Sample();
		experiment.getSamples().add(sample1);
		Sample sample2 = new Sample();
		experiment.getSamples().add(sample2);
		
		// Load file
		IFileStore reportFileStore = EclipseUtils.find(Activator.PLUGIN_ID,
				"/data/excel/test.xls");
		InputStream input = reportFileStore.openInputStream(EFS.NONE,
				new NullProgressMonitor());
		
		// Refine
		ExperimentModelUtils.refineExperimentFromExcel(experiment, input);
	}

}
