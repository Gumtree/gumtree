/**
 * 
 */
package au.gov.ansto.bragg.kakadu.ui.views;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.gumtree.ui.util.SafeUIRunner;

import au.gov.ansto.bragg.cicada.core.Algorithm;
import au.gov.ansto.bragg.cicada.core.AlgorithmStatusListener;
import au.gov.ansto.bragg.cicada.core.Algorithm.AlgorithmStatus;
import au.gov.ansto.bragg.datastructures.core.plot.Position;
import au.gov.ansto.bragg.datastructures.core.plot.StepDirection;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.kakadu.core.data.OperationParameterType;
import au.gov.ansto.bragg.kakadu.ui.util.Util;
import au.gov.ansto.bragg.process.agent.ProcessorAgent;
import au.gov.ansto.bragg.process.port.Tuner;

/**
 * @author nxi
 *
 */
public class ParameterControlViewer extends Composite {

	private Composite formBody;
	protected FormToolkit toolkit;
	protected Algorithm algorithm;
	protected ProgressBar progressBar;
	protected Button defaultButton;
	protected Button runButton;
	private AlgorithmStatusListener statusListener;

//	private List<AlgorithmStatusListener> statusListeners = new ArrayList<AlgorithmStatusListener>();
	
	/**
	 * @param parent
	 * @param style
	 */
	public ParameterControlViewer(Composite parent, int style) {
		super(parent, style);
		toolkit = new FormToolkit(parent.getDisplay());
		ScrolledForm form = toolkit.createScrolledForm(this);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(form);
		formBody = form.getBody();
		GridLayoutFactory.swtDefaults().applyTo(formBody);
	}
	
	public void setAlgorithm(Algorithm algorithm){
		this.algorithm = algorithm;
//		SafeUIRunner.asyncExec(new SafeRunnable() {
//			
//			@Override
//			public void run() throws Exception {
//				createParameterUI(formBody);
//				createControlUI(formBody);
//				formBody.layout();
//			}
//		});
		createParameterUI(formBody);
		createStatusUI(formBody);
		createControlUI(formBody);
		formBody.layout();
	}

	private void createParameterUI(Composite parent) {
		List<ProcessorAgent> agents = algorithm.getProcessorAgentList();
		for (ProcessorAgent agent : agents){
			createAgentUI(parent, agent);
		}
	}

	private void createAgentUI(Composite parent, ProcessorAgent agent) {
		List<Tuner> tuners = agent.getTuners();
		boolean hasVisibleTuner = false;
		for (Tuner tuner : tuners){
			if (tuner.isVisible()){
				hasVisibleTuner = true;
				break;
			}
		}
		if (hasVisibleTuner){
			Group agentGroup = new Group(parent, SWT.NULL);
			GridDataFactory.fillDefaults().applyTo(agentGroup);
			GridLayoutFactory.swtDefaults().numColumns(2).applyTo(agentGroup);
			agentGroup.setBackground(parent.getBackground());
			agentGroup.setText(agent.getLabel());
			for (Tuner tuner : tuners){
				if (tuner.isVisible())
					createTunerUI(agentGroup, tuner);
			}
		}
	}

	private void createTunerUI(Composite parent, Tuner tuner) {
		OperationParameterType type = getType(tuner);
		switch (type) {
		case Boolean:
			createBooleanUI(parent, tuner);
			break;
		case Number:
			createNumericUI(parent, tuner);
			break;
		case Option:
			createOptionUI(parent, tuner);
			break;
		case Position:
			createPositionUI(parent, tuner);
			break;
		case Region:
			createRegionUI(parent, tuner);
			break;
		case StepDirection:
			createDirectionUI(parent, tuner);
			break;
		case Text:
			createTextUI(parent, tuner);
			break;
		case Uri:
			createUriUI(parent, tuner);
			break;
		case Unknown:
			createTextUI(parent, tuner);
			break;
		default:
			break;
		}
	}
	
	private void createBooleanUI(Composite parent, final Tuner tuner) {
		
		Label label = toolkit.createLabel(parent, tuner.getLabel());
		GridDataFactory.fillDefaults().applyTo(label);
		final Button checkbox = toolkit.createButton(parent, "", SWT.CHECK);
		GridDataFactory.fillDefaults().applyTo(checkbox);
		checkbox.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				try {
					tuner.setSignal(new Boolean(checkbox.getSelection()));
				} catch (Exception e) {
					Util.handleException(getShell(), e);
				}
			}
		});
	}

	private void createNumericUI(Composite parent, final Tuner tuner) {
		Label label = toolkit.createLabel(parent, tuner.getLabel());
		GridDataFactory.fillDefaults().applyTo(label);
		final Text text = toolkit.createText(parent, tuner.getSignal().toString());
		GridDataFactory.fillDefaults().applyTo(text);

		text.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent arg0) {
				try{
					Double value = Double.valueOf(text.getText());
					tuner.setSignal(value);
				}catch (Exception e) {
					Util.handleException(getShell(), e);
				}
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
			}
		});
	}

	private void createOptionUI(Composite parent, final Tuner tuner) {
		// TODO Auto-generated method stub
		
	}

	private void createPositionUI(Composite parent, final Tuner tuner) {
		// TODO Auto-generated method stub
		
	}

	private void createRegionUI(Composite parent, final Tuner tuner) {
		// TODO Auto-generated method stub
		
	}

	private void createDirectionUI(Composite parent, final Tuner tuner) {
		// TODO Auto-generated method stub
		
	}

	private void createStatusUI(Composite parent) {
		progressBar = new ProgressBar(parent, SWT.HORIZONTAL | SWT.NULL);
		GridDataFactory.fillDefaults().applyTo(progressBar);
		progressBar.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
		progressBar.setMinimum(0);
		if (algorithm != null && algorithm.getAgentList().size() > 0)
			progressBar.setMaximum(algorithm.getAgentList().size());
		progressBar.setEnabled(false);
		
		statusListener = new AlgorithmStatusListener() {
			
			@Override
			public void setStage(final int operationIndex, final AlgorithmStatus status) {
				SafeUIRunner.asyncExec(new SafeRunnable() {
					
					@Override
					public void run() throws Exception {
						if (status == AlgorithmStatus.Running){
							if (!progressBar.isEnabled())
								progressBar.setEnabled(true);
							progressBar.setSelection(operationIndex + 1);
						}else{
							progressBar.setSelection(0);
							progressBar.setEnabled(false);
						}
					}
				});
			}
			
			@Override
			public void onStatusChanged(AlgorithmStatus status) {
			}
		};
		algorithm.addStatusListener(statusListener);
	}

	private void createUriUI(final Composite parent, final Tuner tuner) {
		Label label = toolkit.createLabel(parent, tuner.getLabel());
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(label);

		Composite uriComposite = toolkit.createComposite(parent);
		GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(2).applyTo(uriComposite);
		Object oldValue = tuner.getSignal();
		String oldString = "";
		if (oldValue != null)
			oldString = oldValue.toString();
		final Text text = toolkit.createText(uriComposite, oldString);
		GridDataFactory.swtDefaults().hint(120, SWT.DEFAULT).align(SWT.LEFT, SWT.CENTER).applyTo(text);

		Button button = toolkit.createButton(uriComposite, ">", SWT.PUSH);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(button);
		button.setToolTipText("Click to select a file from the file system");
		button.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				String filename = Util.getFilenameFromShellNoCheck(parent.getShell(), 
						new String[]{"*.*"}, new String[]{"All"});
				if (filename != null) {
					text.setText(new File(filename).toURI().toString());
					try {
						URI newUri = ConverterLib.path2URI(filename);
						tuner.setSignal(newUri);
					} catch (Exception e2) {
						Util.handleException(getShell(), e2);
					}
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

	}

	private void createTextUI(Composite parent, final Tuner tuner) {
		Label label = toolkit.createLabel(parent, tuner.getLabel());
		GridDataFactory.fillDefaults().applyTo(label);
		final Text text = toolkit.createText(parent, tuner.getSignal().toString());
		GridDataFactory.fillDefaults().applyTo(text);

		text.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent arg0) {
				try{
					tuner.setSignal(text.getText());
				}catch (Exception e) {
					Util.handleException(getShell(), e);
				}
			}
			
			@Override
			public void focusGained(FocusEvent arg0) {
			}
		});		
	}

	public static OperationParameterType getType(Tuner tuner) {
		List<?> options = tuner.getOptions();
		OperationParameterType type;
		Class<?> parameterValueClass = null;
		try {
			parameterValueClass = Class.forName(tuner.getType());
		} catch (Exception e) {
			type = OperationParameterType.Unknown;
		}
		if (options != null && options.size() > 0){
			type = OperationParameterType.Option;
		}else if (Number.class.isAssignableFrom(parameterValueClass)) {
			type = OperationParameterType.Number;
		} else if (parameterValueClass == Boolean.class) {
			type = OperationParameterType.Boolean;
		} else if (parameterValueClass == String.class) {
			type = OperationParameterType.Text;
		} else if (parameterValueClass == URI.class) {
			type = OperationParameterType.Uri;
		} else if (parameterValueClass == StepDirection.class) {
			type = OperationParameterType.StepDirection;
		} else if (parameterValueClass == Position.class) {
			type = OperationParameterType.Position;
		} else if (parameterValueClass == Group.class && tuner.getUsage().equals("region")) {
			type = OperationParameterType.Region;
		} else {
			type = OperationParameterType.Unknown;
		}
		return type;
	}
	
	public Composite getFormBody(){
		return formBody;
	}

	private void createControlUI(Composite parent) {
		Composite controlComposite = toolkit.createComposite(parent);
		GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(2).applyTo(controlComposite);
//		GridDataFactory.swtDefaults().grab(true, false).applyTo(controlComposite);
		defaultButton = toolkit.createButton(controlComposite, "Load Default", SWT.PUSH);
		GridDataFactory.swtDefaults().hint(90, SWT.DEFAULT).grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(defaultButton);
		defaultButton.setToolTipText("Click to load default values for all parameters");
		defaultButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		runButton = toolkit.createButton(controlComposite, "Run", SWT.PUSH);
		GridDataFactory.fillDefaults().grab(true, true).hint(90, SWT.DEFAULT).align(SWT.FILL, SWT.FILL).applyTo(runButton);
		runButton.setToolTipText("Click to run the algorithm");
		
		runButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					algorithm.execute(null);
				} catch (Exception e) {
					Util.handleException(getShell(), e);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
	}

	public void addAlgorithmStatusListener(AlgorithmStatusListener listener){
//		statusListeners.add(listener);
		if (algorithm != null)
			algorithm.addStatusListener(listener);
	}
	
	public void removeAlgorithmStatusListener(AlgorithmStatusListener listener){
//		statusListeners.remove(listener);
		if (algorithm != null)
			algorithm.removeStatusListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		algorithm.removeStatusListener(statusListener);
	}
	

}
