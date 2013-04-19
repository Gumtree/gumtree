package au.gov.ansto.bragg.kookaburra.ui.workflow;

import org.gumtree.workflow.ui.AbstractTask;
import org.gumtree.workflow.ui.WorkflowException;

import au.gov.ansto.bragg.kookaburra.experiment.model.Experiment;

/**
 * Abstract experiment task provides empty implemention to some workflow task
 * methods. Kookaburra experiment workflow tests should extend this class.
 * 
 */
public abstract class AbstractExperimentTask extends AbstractTask {

	@Override
	protected Object createModelInstance() {
		// Kookaburra workflow tasks do not use individual model instance.
		// It uses the global experiment model instead.
		return null;
	}

	@Override
	public Object run(Object object) throws WorkflowException {
		// Do nothing. Subclass should override this to add run action.
		return null;
	}

	protected Experiment getExperiment() {
		return getContext().getSingleValue(Experiment.class);
	}
	
	public Class<?>[] getInputTypes() {
		return null;
	}
	
	public Class<?>[] getOutputTypes() {
		return null;
	}
	
}
