package au.gov.ansto.bragg.quokka.msw;

import java.util.Set;

import org.gumtree.msw.IModelProxy;
import org.gumtree.msw.elements.DependencyProperty;
import org.gumtree.msw.elements.Element;
import org.gumtree.msw.elements.IDependencyProperty;

public class ExperimentDescription extends Element {
	// final
	public static final String MANUAL_POSITIONS = "Manual Position";
	public static final String FIXED_POSITION = "Fixed Position";
	public static final String RHEOMETER = "Rheometer";
	public static final String ROTATING_5_POSITIONS = "5 Position Rotating Holder";
	public static final String LINEAR_10_POSITIONS = "10 Position Holder";
	public static final String LINEAR_12_POSITIONS = "12 Position Holder";
	public static final String LINEAR_20_POSITIONS = "20 Position Holder";
	public static final String DEFAULT_SAMPLE_STAGE = LINEAR_20_POSITIONS;
	
	// property names
	public static final DependencyProperty<ExperimentDescription, String> PROPOSAL_NUMBER = new DependencyProperty<>("ProposalNumber", String.class);
	public static final DependencyProperty<ExperimentDescription, String> EXPERIMENT_TITLE = new DependencyProperty<>("ExperimentTitle", String.class);
	public static final DependencyProperty<ExperimentDescription, String> SAMPLE_STAGE = new DependencyProperty<>("SampleStage", String.class);
	// property set
	public static final Set<IDependencyProperty> PROPERTIES = DependencyProperty.createSet(
			PROPOSAL_NUMBER, EXPERIMENT_TITLE, SAMPLE_STAGE);

	// construction
	ExperimentDescription(IModelProxy modelProxy) {
		super(modelProxy, ExperimentDescription.class.getSimpleName());
	}

	// properties
	@Override
	public Set<IDependencyProperty> getProperties() {
		return PROPERTIES;
	}
	public String getProposalNumber() {
		return (String)get(PROPOSAL_NUMBER);
	}
	public void setProposalNumber(String value) {
		set(PROPOSAL_NUMBER, value);
	}
	public String getExperimentTitle() {
		return (String)get(EXPERIMENT_TITLE);
	}
	public void setExperimentTitle(String value) {
		set(EXPERIMENT_TITLE, value);
	}
	public String getSampleStage() {
		return (String)get(SAMPLE_STAGE);
	}
	
	// helper
	public static String getSampleStage(int samplePositions) {
		switch (samplePositions) {
		case 1:
			return FIXED_POSITION;
		case 5:
			return ROTATING_5_POSITIONS;
		case 10:
			return LINEAR_10_POSITIONS;
		case 12:
			return LINEAR_12_POSITIONS;
		case 20:
			return LINEAR_20_POSITIONS;
		default:
			return null;
		}
	}
}
