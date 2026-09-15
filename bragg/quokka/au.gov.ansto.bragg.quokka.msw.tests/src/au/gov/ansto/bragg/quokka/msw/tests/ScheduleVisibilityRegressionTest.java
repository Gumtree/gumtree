package au.gov.ansto.bragg.quokka.msw.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gumtree.msw.dummy.DummyModelProxy;
import org.gumtree.msw.dummy.DummyRefIdProvider;
import org.gumtree.msw.elements.IDependencyProperty;
import org.gumtree.msw.model.DataSource;
import org.gumtree.msw.model.Model;
import org.gumtree.msw.schedule.AcquisitionAspect;
import org.gumtree.msw.schedule.AcquisitionEntry;
import org.gumtree.msw.schedule.ScheduledAspect;
import org.gumtree.msw.schedule.ScheduledNode;
import org.gumtree.msw.schedule.Scheduler;
import org.gumtree.msw.schedule.execution.AcquisitionSummary;
import org.gumtree.msw.schedule.execution.IScheduleExecuter;
import org.gumtree.msw.schedule.execution.IScheduleProvider;
import org.gumtree.msw.schedule.execution.InitializationSummary;
import org.gumtree.msw.schedule.execution.ParameterChangeSummary;
import org.gumtree.msw.schedule.execution.ScheduleWalker;
import org.gumtree.msw.schedule.execution.Summary;
import org.gumtree.msw.util.SynchronizedModel;
import org.junit.Test;

import au.gov.ansto.bragg.quokka.msw.Configuration;
import au.gov.ansto.bragg.quokka.msw.ConfigurationList;
import au.gov.ansto.bragg.quokka.msw.Environment;
import au.gov.ansto.bragg.quokka.msw.Measurement;
import au.gov.ansto.bragg.quokka.msw.ModelProvider;
import au.gov.ansto.bragg.quokka.msw.Sample;
import au.gov.ansto.bragg.quokka.msw.SampleList;
import au.gov.ansto.bragg.quokka.msw.SetPoint;

/**
 * Regression tests for ISSUE-001 - "Quokka MSW runs samples that are not selected" - and
 * for the follow-up report from QKK that the first attempt at fixing it silently dropped
 * selections the user had made.
 *
 * <p><b>The contract under test.</b> {@link ScheduledNode} carries an "enabled" flag at
 * two levels, and they are deliberately different things:
 *
 * <ul>
 * <li>{@link ScheduledNode#isThisVisible()} reads the <em>source element</em>. One
 *     {@code Sample} element backs one scheduled node per (configuration x measurement)
 *     branch, so this is the global "is this sample selected" flag, and it decides whether
 *     the row exists in the acquisition tree at all
 *     ({@code ScheduleTableModel.cleanCollapsedNodes}, {@code ScheduleTableExporter}).</li>
 * <li>{@link ScheduledNode#isEnabled()} reads the <em>per-branch</em> override
 *     ({@code values} -&gt; {@code defaults} -&gt; source). That is what the checkbox on
 *     the row shows and what {@code ScheduleTableModel} writes through
 *     {@code node.setEnabled(...)}. It is the run gate in
 *     {@code Scheduler.ScheduleProvider.createScheduleStep}.</li>
 * </ul>
 *
 * <p>A per-branch tick is allowed to exceed the per-branch default - that is how
 * "transmission for this sample at this configuration only" is expressed, and how
 * {@code AcquisitionComposite}'s BLOCKED_BEAM / EMPTY_BEAM rules and its bulk transmission
 * toggle work (all of which drive {@code setDefault(Sample.ENABLED, false)} /
 * {@code clearDefault}). Execution must therefore <b>not</b> be gated on visibility.
 *
 * <p>The original defect was narrower: a per-branch tick that outlived the deselection of
 * its sample. The row vanished from the tree, so the user could no longer see or undo the
 * tick, but the walker still ran it. That is fixed where it originates - in
 * {@code ScheduledNode.onChangedProperty}, which drops the node-local tick when the source
 * element is deselected - so display and execution agree by construction.
 *
 * <p>Note: the model is schema-driven, so these tests load the real {@code msw.xsd} /
 * {@code msw.xml} resources from the sibling {@code au.gov.ansto.bragg.quokka.msw} bundle
 * (mirroring the file fallback in that bundle's {@code Activator}).
 */
public class ScheduleVisibilityRegressionTest {

	private static final String MSW_BUNDLE = "../au.gov.ansto.bragg.quokka.msw";
	private static final String NAME_KEEP = "KEEP";
	private static final String NAME_DROP = "DROP";

	/**
	 * ISSUE-001 proper. A sample is ticked on one branch of the acquisition tree and is
	 * afterwards deselected in the sample list. The tick must not survive, and the sample
	 * must not be acquired.
	 *
	 * <p>Fails without the {@code onChangedProperty} clean-up in {@link ScheduledNode}:
	 * the stale {@code values[enabled] == true} keeps {@code isEnabled()} true and the
	 * hidden sample is acquired.
	 */
	@Test
	public void staleTickIsClearedWhenSampleIsDeselected() {
		Fixture fixture = new Fixture();
		try {
			List<ScheduledNode> dropNodes = fixture.nodesFor(fixture.dropSample);
			assertFalse("no scheduled nodes for the DROP sample", dropNodes.isEmpty());

			for (ScheduledNode node : dropNodes) {
				// branch starts unticked, as the bulk transmission toggle and the
				// BLOCKED_BEAM / EMPTY_BEAM rules leave it
				assertTrue(node.setDefault(Sample.ENABLED, Boolean.FALSE));
				assertFalse(node.isEnabled());
				// ... and the user then ticks that row in the acquisition tree
				assertTrue(node.set(Sample.ENABLED, Boolean.TRUE));
				assertTrue(node.isEnabled());
			}

			// the user now deselects the sample in the sample list, which removes every
			// one of its rows from the acquisition tree
			fixture.dropSample.set(Sample.ENABLED, Boolean.FALSE);

			for (ScheduledNode node : dropNodes) {
				assertFalse("deselected sample must not be visible", node.isThisVisible());
				assertFalse(
						"ISSUE-001: a per-branch tick must not survive deselection of its sample",
						node.isEnabled());
			}

			List<String> acquired = fixture.walk();
			assertTrue("the selected sample should still have been acquired",
					acquired.contains(NAME_KEEP));
			assertFalse("ISSUE-001: a deselected sample must never be acquired",
					acquired.contains(NAME_DROP));
		}
		finally {
			fixture.dispose();
		}
	}

	/**
	 * The other direction, reported from QKK after the first fix attempt: a row the user
	 * had ticked was not collected. Guards against any run gate that refuses a per-branch
	 * tick which exceeds the per-branch default - for example AND-ing
	 * {@code isThisVisible()} into {@code createScheduleStep}, or resolving the tick
	 * through {@code getDefault()} instead of {@code get()}.
	 */
	@Test
	public void perBranchTickOnSelectedSampleIsAcquired() {
		Fixture fixture = new Fixture();
		try {
			List<ScheduledNode> keepNodes = fixture.nodesFor(fixture.keepSample);
			assertFalse("no scheduled nodes for the KEEP sample", keepNodes.isEmpty());

			for (ScheduledNode node : keepNodes) {
				assertTrue(node.setDefault(Sample.ENABLED, Boolean.FALSE));
				assertFalse("branch should start unticked", node.isEnabled());
				assertTrue(node.set(Sample.ENABLED, Boolean.TRUE));
				assertTrue("the tick must be what isEnabled() reports", node.isEnabled());
				assertTrue("the row is still in the tree", node.isThisVisible());
			}

			List<String> acquired = fixture.walk();
			assertTrue(
					"a row the user ticked must be acquired, even though the per-branch "
							+ "default says otherwise",
					acquired.contains(NAME_KEEP));
		}
		finally {
			fixture.dispose();
		}
	}

	/**
	 * Pins the two-level contract itself, without walking a schedule: row existence
	 * follows the source element, the checkbox follows the per-branch override, and the
	 * two are allowed to disagree.
	 *
	 * <p>Fails if {@code isThisVisible()} is made to read {@code get(enabled)} - that
	 * collapses the two levels, makes hidden rows runnable again, and turns any
	 * visibility-based run gate into a no-op.
	 */
	@Test
	public void visibilityTracksSourceElementNotThePerBranchTick() {
		Fixture fixture = new Fixture();
		try {
			List<ScheduledNode> keepNodes = fixture.nodesFor(fixture.keepSample);
			assertFalse("no scheduled nodes for the KEEP sample", keepNodes.isEmpty());
			ScheduledNode node = keepNodes.get(0);

			// unticked on this branch, but the sample is selected -> the row still exists
			assertTrue(node.setDefault(Sample.ENABLED, Boolean.FALSE));
			assertFalse(node.isEnabled());
			assertTrue("visibility must follow the source element, not the tick",
					node.isThisVisible());

			// ticked on this branch -> visibility is unchanged
			assertTrue(node.set(Sample.ENABLED, Boolean.TRUE));
			assertTrue(node.isEnabled());
			assertTrue(node.isThisVisible());

			// deselected in the sample list -> the row disappears, and the tick with it
			fixture.keepSample.set(Sample.ENABLED, Boolean.FALSE);
			assertFalse(node.isThisVisible());
			assertFalse(node.isEnabled());
		}
		finally {
			fixture.dispose();
		}
	}

	// fixture --------------------------------------------------------------

	/**
	 * One configuration (Transmission + Scattering, both enabled) and two named samples,
	 * both selected at the model level.
	 */
	private static class Fixture {
		final Scheduler scheduler;
		final SampleList sampleList;
		final Sample keepSample;
		final Sample dropSample;

		Fixture() {
			ModelProvider modelProvider = createModelProvider();
			scheduler = createScheduler();

			boolean ok = false;
			try {
				scheduler.updateSource(modelProvider.getLoopHierarchy());

				// one configuration -> auto-creates a Transmission and a Scattering
				// measurement; enable both so the schedule has acquisition steps
				// regardless of any schema default
				ConfigurationList configurationList = modelProvider.getConfigurationList();
				configurationList.addConfiguration();

				Configuration configuration = single(fetch(configurationList, Configuration.class));
				configuration.set(Configuration.ENABLED, Boolean.TRUE);
				for (Measurement measurement : fetch(configuration, Measurement.class))
					measurement.set(Measurement.ENABLED, Boolean.TRUE);

				sampleList = modelProvider.getSampleList();
				keepSample = findByPosition(sampleList, 1.0);
				dropSample = findByPosition(sampleList, 2.0);
				assertNotNull("position 1 sample missing", keepSample);
				assertNotNull("position 2 sample missing", dropSample);

				// both samples start selected; each test deselects what it needs to
				keepSample.set(Sample.NAME, NAME_KEEP);
				keepSample.set(Sample.ENABLED, Boolean.TRUE);
				dropSample.set(Sample.NAME, NAME_DROP);
				dropSample.set(Sample.ENABLED, Boolean.TRUE);

				ok = true;
			}
			finally {
				if (!ok)
					scheduler.dispose();
			}
		}

		/** Every scheduled node backed by the given sample element (one per branch). */
		List<ScheduledNode> nodesFor(Sample sample) {
			List<ScheduledNode> result = new ArrayList<>();
			for (ScheduledAspect aspect : scheduler.getAspects(sampleList))
				for (ScheduledNode node : aspect.getNode().getNodes())
					if (node.getSourceElement() == sample)
						result.add(node);
			return result;
		}

		/** Walks the whole schedule and returns the names of the samples acquired. */
		List<String> walk() {
			RecordingExecuter executer = new RecordingExecuter();
			IScheduleProvider provider = scheduler.createScheduleProvider();
			new ScheduleWalker().walk(provider, executer);
			return executer.acquiredSampleNames;
		}

		void dispose() {
			scheduler.dispose();
		}
	}

	// model construction ---------------------------------------------------

	private static ModelProvider createModelProvider() {
		DataSource xsd = new DataSource(new File(MSW_BUNDLE + "/resources/msw.xsd"));
		DataSource xml = new DataSource(new File(MSW_BUNDLE + "/resources/msw.xml"));
		Model model = new Model(DummyRefIdProvider.DEFAULT, xsd, xml);
		return new ModelProvider(new DummyModelProxy(new SynchronizedModel(model)));
	}

	// mirrors AcquisitionComposite.createScheduler()
	private static Scheduler createScheduler() {
		return new Scheduler(
				Arrays.asList(
						new AcquisitionAspect(
								ConfigurationList.class.getSimpleName(),
								new IDependencyProperty[] { ConfigurationList.DESCRIPTION },
								new AcquisitionEntry(
										Configuration.class.getSimpleName(),
										new IDependencyProperty[] { Configuration.INDEX, Configuration.ENABLED, Configuration.NAME, Configuration.DESCRIPTION },
										new AcquisitionEntry(
												Measurement.TRANSMISSION,
												new IDependencyProperty[] { Measurement.INDEX, Measurement.ENABLED, Measurement.NAME, Measurement.DESCRIPTION, Measurement.MIN_TIME, Measurement.MAX_TIME, Measurement.TARGET_MONITOR_COUNTS, Measurement.TARGET_DETECTOR_COUNTS, Measurement.MIN_TIME_ENABLED, Measurement.MAX_TIME_ENABLED, Measurement.TARGET_MONITOR_COUNTS_ENABLED, Measurement.TARGET_DETECTOR_COUNTS_ENABLED }),
										new AcquisitionEntry(
												Measurement.SCATTERING,
												new IDependencyProperty[] { Measurement.INDEX, Measurement.ENABLED, Measurement.NAME, Measurement.DESCRIPTION, Measurement.MIN_TIME, Measurement.MAX_TIME, Measurement.TARGET_MONITOR_COUNTS, Measurement.TARGET_DETECTOR_COUNTS, Measurement.MIN_TIME_ENABLED, Measurement.MAX_TIME_ENABLED, Measurement.TARGET_MONITOR_COUNTS_ENABLED, Measurement.TARGET_DETECTOR_COUNTS_ENABLED }))),
						new AcquisitionAspect(
								SampleList.class.getSimpleName(),
								new IDependencyProperty[] { SampleList.DESCRIPTION },
								new AcquisitionEntry(
										Sample.class.getSimpleName(),
										new IDependencyProperty[] { Sample.INDEX, Sample.ENABLED, Sample.NAME, Sample.DESCRIPTION })),
						new AcquisitionAspect(
								Environment.class.getSimpleName(),
								new IDependencyProperty[] { Environment.NAME, Environment.DESCRIPTION },
								new AcquisitionEntry(
										SetPoint.class.getSimpleName(),
										new IDependencyProperty[] { SetPoint.INDEX, SetPoint.ENABLED, SetPoint.VALUE, SetPoint.WAIT_PERIOD, SetPoint.TIME_ESTIMATE }))),
				Arrays.<IDependencyProperty>asList(Measurement.MIN_TIME, Measurement.MAX_TIME, Measurement.TARGET_MONITOR_COUNTS, Measurement.TARGET_DETECTOR_COUNTS, Measurement.MIN_TIME_ENABLED, Measurement.MAX_TIME_ENABLED, Measurement.TARGET_MONITOR_COUNTS_ENABLED, Measurement.TARGET_DETECTOR_COUNTS_ENABLED));
	}

	// helpers --------------------------------------------------------------

	private static <T extends org.gumtree.msw.elements.Element> Set<T> fetch(
			org.gumtree.msw.elements.ElementList<T> list, Class<T> type) {
		Set<T> elements = new HashSet<>();
		list.fetchElements(elements);
		return elements;
	}

	private static <T> T single(Set<T> elements) {
		assertTrue("expected exactly one element, found " + elements.size(), elements.size() == 1);
		return elements.iterator().next();
	}

	private static Sample findByPosition(SampleList sampleList, double position) {
		List<Sample> samples = new ArrayList<>();
		sampleList.fetchElements(samples);
		for (Sample sample : samples)
			if (sample.getPosition() == position)
				return sample;
		return null;
	}

	// recording executer ---------------------------------------------------

	/** A no-op executer that records the name of every sample actually acquired. */
	private static class RecordingExecuter implements IScheduleExecuter {
		final List<String> acquiredSampleNames = new ArrayList<>();

		@Override
		public InitializationSummary initiate() {
			return new InitializationSummary(null, 0, false, null);
		}
		@Override
		public Summary cleanUp() {
			return new Summary(null, 0, false, null);
		}
		@Override
		public ParameterChangeSummary setParameters(String name, Map<String, Object> parameters) {
			return new ParameterChangeSummary(name, parameters, 0, false, null);
		}
		@Override
		public Summary preAcquisition() {
			return new Summary(null, 0, false, null);
		}
		@Override
		public AcquisitionSummary doAcquisition(Map<String, Object> parameters) {
			Object name = parameters.get(Sample.NAME.getName());
			acquiredSampleNames.add(name == null ? null : name.toString());
			return new AcquisitionSummary(parameters, 0, false, null);
		}
		@Override
		public Summary postAcquisition() {
			return new Summary(null, 0, false, null);
		}
	}
}
