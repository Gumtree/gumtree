/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package au.gov.ansto.bragg.quokka.experiment.report;

import java.io.StringWriter;

import junit.framework.TestCase;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.gumtree.util.eclipse.EclipseUtils;

import au.gov.ansto.bragg.quokka.core.tests.Activator;
import au.gov.ansto.bragg.quokka.core.tests.ExperimentModelFactory;
import au.gov.ansto.bragg.quokka.experiment.model.Experiment;

public class ExperimentUserReportGenerationTest extends TestCase {

	public void testNormalAcqusitionReport() {
		// Prepares experiment model
		Experiment experiment = ExperimentModelFactory
				.createSimpleNormalExperiment();

		ExperimentUserReport report = ExperimentUserReportUtils
				.createExperimentUserReport(experiment);

		assertEquals(experiment.getTitle(), report.getTitle());
		assertEquals(experiment.getUser().getName(), report.getName());
		assertEquals(experiment.getInstrumentConfigs().size(), report
				.getConfigs().size());

		StringWriter writer = new StringWriter();
		ExperimentUserReportUtils.getXStream().toXML(report, writer);
		ExperimentUserReport loadedReport = (ExperimentUserReport) ExperimentUserReportUtils
				.getXStream().fromXML(writer.getBuffer().toString());
		System.out.println(loadedReport);
	}

	public void testControlledAcquisition() {
		Experiment experiment = ExperimentModelFactory
				.createSimpleControlledExperiment();
		ExperimentUserReport report = ExperimentUserReportUtils
				.createExperimentUserReport(experiment);
		StringWriter writer = new StringWriter();
		ExperimentUserReportUtils.getXStream().toXML(report, writer);
		ExperimentUserReport loadedReport = (ExperimentUserReport) ExperimentUserReportUtils
				.getXStream().fromXML(writer.getBuffer().toString());
		System.out.println(loadedReport);
	}

	public void testReadNormalAcqusitionReport() throws CoreException {
//		IFileStore reportFileStore = EclipseUtils.find(Activator.PLUGIN_ID,
//				"/data/reports/QKK_2009-12-17_102252_report.xml");
//		ExperimentUserReport report = (ExperimentUserReport) ExperimentUserReportUtils
//				.getXStream().fromXML(
//						reportFileStore.openInputStream(EFS.NONE,
//								new NullProgressMonitor()));
//		assertEquals(6, report.getConfigs().size());
	}
}
