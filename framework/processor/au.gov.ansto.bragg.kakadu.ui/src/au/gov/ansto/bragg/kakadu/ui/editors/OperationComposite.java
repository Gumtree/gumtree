/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.ui.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

import au.gov.ansto.bragg.kakadu.core.data.Operation;
import au.gov.ansto.bragg.kakadu.core.data.OperationOptions;
import au.gov.ansto.bragg.kakadu.core.data.OperationStatus;
import au.gov.ansto.bragg.kakadu.ui.SWTResourceManager;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotDataReference;
import au.gov.ansto.bragg.kakadu.ui.plot.dnd.PlotDataReferenceTransfer;
import au.gov.ansto.bragg.kakadu.ui.util.DisplayManager;
import au.gov.ansto.bragg.kakadu.ui.widget.BorderedComposite;
import au.gov.ansto.bragg.process.agent.Agent;
import au.gov.ansto.bragg.process.agent.AgentListener;
import au.gov.ansto.bragg.process.agent.ProcessorAgent;
import au.gov.ansto.bragg.process.processor.Processor_.ProcessorStatus;

/**
 * The class represents UI for an operation.
 * 
 * @author Danil Klimontov (dak)
 */
public class OperationComposite extends BorderedComposite {


	private final Color mouseOverBorderColor = new Color(getDisplay(), 114,123,132);
	private final Color defaultBorderColor = new Color(getDisplay(), 162,173,188);
	private Color selectedBackgroundColor = new Color(getDisplay(), 246, 244, 218);//getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
	private Color defaultBackgroundColor = new Color(getDisplay(), 244, 243, 238);
	private Color skippedBackgroundColor = getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
	private final Color processingStatusFontColor = new Color(getDisplay(), 0, 255, 0);
	private final Color interruptedStatusFontColor = getDisplay().getSystemColor(SWT.COLOR_YELLOW);
	private final Color errorStatusFontColor = new Color(getDisplay(), 255, 0, 0);
	private final Color defaultStatusFontColor = new Color(getDisplay(), 0, 0, 0);
	private final Color runningBackgroundColor = new Color(getDisplay(), 223, 148, 132);

	private Operation operation;
	private Label name;
	private Label statusTextLabel;
	private boolean isSelected = false;
	public boolean isMouseOver;
	private Button enableCheckBox;
	private List<OperationEnableListener> enableListeners = new ArrayList<OperationEnableListener>();

	private OperationMouseListener mouseTrackListener = new OperationMouseListener();
	private List<SelectionListener> selectionListenerList  = new ArrayList<SelectionListener>();
	private Label dragLabel;
	private DragSource dragSource;


	/**
	 * Updates UI after operation status was changed.
	 */
//	private final OperationStatusListener operationStatusListener = new OperationStatusListener() {
//	public void statusUpdated(Operation operation, OperationStatus oldOperationStatus,
//	final OperationStatus newOperationStatus) {
//	Display.getDefault().asyncExec(new Runnable() {
//	public void run() {
//	setStatus(newOperationStatus);
//	layout();
//	getParent().layout();
////	redraw();
//	}
//	});
//	}
//	};
	private final AgentListener operationStatusListener = new AgentListener() {
		public void onChange(final Agent agent){
			DisplayManager.getDefault().asyncExec(new Runnable() {
				public void run() {
					try {
						ProcessorStatus processorStatus = ProcessorStatus.Error;
						if (agent instanceof ProcessorAgent) {
							processorStatus = ((ProcessorAgent) agent).getProcessorStatus();
						}
						OperationStatus operationStatus = OperationStatus.Error;
						switch (processorStatus) {
						case Inprogress:
							operationStatus = OperationStatus.InProgress;
							break;
						case Done:
							operationStatus = OperationStatus.Done;
							break;
						case Ready:
							operationStatus = OperationStatus.Ready;
							break;
						case Error:
							operationStatus = OperationStatus.Error;
							break;
						case Interrupted:
							operationStatus = OperationStatus.Interrupted;
							break;
						default:
							break;
						}
						setStatus(operationStatus);
					} catch (Exception e) {
						// TODO Auto-generated catch block
//						e.printStackTrace();
						setStatus(OperationStatus.Error);
					}
					layout();
					getParent().layout();
//					redraw();
				}
			});
		}
	};

	/**
	 * Creates a new UI component.
	 * @param parent parent UI object.
	 * @param swtArguments augments to configure style of the UI component.
	 */
	public OperationComposite(Composite parent, int swtArguments) {
		super(parent, swtArguments);
		initialize();

		initListeners();

		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent arg0) {
				dispose();
			}			
		});
	}

	/**
	 * Initialise all inner listeners.
	 */
	protected void initListeners() {
		addMouseTrackListener(mouseTrackListener);
		addMouseListener(mouseTrackListener);
		Control[] children = getChildren();
		for (Control control : children) {
			control.addMouseTrackListener(mouseTrackListener);
			control.addMouseListener(mouseTrackListener);
		}

		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				disposeResources();
			}
		});

	}

	/**
	 * Initialise UI components.
	 */
	protected void initialize() {

		GridLayout gridLayout = new GridLayout ();
		gridLayout.numColumns = 3;
		gridLayout.marginLeft = 3;
		gridLayout.marginRight = 3;
		gridLayout.marginBottom = 3;
		gridLayout.marginTop = 3;
		gridLayout.verticalSpacing = 10;
		setLayout (gridLayout);

		name = new Label(this, SWT.NONE);
		GridData data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = 2;
		data.grabExcessHorizontalSpace = true;
		name.setLayoutData (data);

		dragLabel = new Label(this, SWT.NONE);
		dragLabel.setImage(SWTResourceManager.getImage("icons/operation_drag_icon.gif", this));
//		data = new GridData ();
//		dragLabel.setLayoutData (data);

		Label statusLabel = new Label(this, SWT.NONE);
		statusLabel.setText("Status: ");
		data = new GridData ();
		data.horizontalAlignment = SWT.BEGINNING;
		statusLabel.setLayoutData (data);

		statusTextLabel = new Label(this, SWT.NONE);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
//		data.horizontalSpan = 2;
		statusTextLabel.setLayoutData (data);

		enableCheckBox = new Button(this, SWT.CHECK);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		enableCheckBox.setLayoutData(data);
		enableCheckBox.setVisible(false);
		updateUI();
		initDnD();

	}


	private void initDnD() {
		int operations = DND.DROP_MOVE;
		dragSource = new DragSource(dragLabel, operations);
		dragLabel.setCursor(new Cursor (getDisplay(), SWT.CURSOR_HAND));

		Transfer[] types = new Transfer[] {PlotDataReferenceTransfer.getInstance()};
		dragSource.setTransfer(types);

		dragSource.addDragListener(new DragSourceListener() {
			public void dragStart(DragSourceEvent event) {
				// Only start the drag if there is actually text in the
				// label - this text will be what is dropped on the target.
//				if (dragLabel.getText().length() == 0) {
//				event.doit = false;
//				}
			}

			public void dragSetData(DragSourceEvent event) {
				// Provide the data of the requested type.
				if (PlotDataReferenceTransfer.getInstance().isSupportedType(event.dataType)) {
					event.data = new PlotDataReference(
							operation.getAlgorithmTaskId(),
							operation.getID(),
							operation.getDataItemIndex());
				}
			}
			public void dragFinished(DragSourceEvent event) {
			}
		});

	}

	/**
	 * Gets an operation instance associated for the UI component.
	 * @return currently associated Operation instance.
	 */
	public Operation getOperation() {
		return operation;
	}

	/**
	 * Sets an Operation to associate the UI component with 
	 * @param operation
	 */
	public void setOperation(final Operation operation) {
		if (this.operation != null) {
//			this.operation.removeOperationStatusListener(operationStatusListener);
			operation.getAgent().subscribe(operationStatusListener);
		}

		this.operation = operation;

		setName(operation.getUILabel());
		setStatus(operation.getStatus());

//		operation.addOperationStatusListener(operationStatusListener);
		operation.getAgent().subscribe(operationStatusListener);
		updateUI();
		OperationOptions options = operation.getOptions();
		if (options.isSkipSupported() || options.isEnableSupported()){
			enableCheckBox.addSelectionListener(new SelectionListener(){

				public void widgetDefaultSelected(SelectionEvent arg0) {
					// TODO Auto-generated method stub
					
				}

				public void widgetSelected(SelectionEvent arg0) {
					// TODO Auto-generated method stub
					for (OperationEnableListener listener : enableListeners)
						listener.setEnable(enableCheckBox.getSelection());
				}});
		}
	}

	public void setName(String name) {
		this.name.setText(name);
	}

	public void setStatus(OperationStatus operationStatus) {
		switch (operationStatus) {
		case InProgress:
			updateOperationBackgroundColor(runningBackgroundColor);
			statusTextLabel.setForeground(processingStatusFontColor);
			break;
		case Error:
			updateOperationBackgroundColor(null);
			statusTextLabel.setForeground(errorStatusFontColor);
			break;
		case Interrupted:
			updateOperationBackgroundColor(null);
			statusTextLabel.setForeground(interruptedStatusFontColor);
			break;
		default:
			updateOperationBackgroundColor(null);
			statusTextLabel.setForeground(defaultStatusFontColor);
			break;
		}
		statusTextLabel.setText(operationStatus.toString());
	}

	public void setStatus(String operationStatus) {
		statusTextLabel.setText(operationStatus);
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		boolean previouos_isSelected = this.isSelected;
		this.isSelected = isSelected;

		if (previouos_isSelected != isSelected) {
			updateUI();

			fireSelectionListener();
		}
	}
	
	public void forceSelected(boolean isSelected) {
		boolean previouos_isSelected = this.isSelected;
		this.isSelected = isSelected;

		updateUI();
		fireSelectionListener();
	}

	private void updateUI() {
//		Color newColor = isSelected ? selectedBackgroundColor : defaultBackgroundColor;
		updateOperationBackgroundColor(null);
//		layout();
//		getParent().layout();
//		getParent().getParent().layout();


		if (isMouseOver || isSelected) {
			setBorderColor(mouseOverBorderColor);
			setBorderSize(3);
		} else {
			setBorderColor(defaultBorderColor);
			setBorderSize(1);
		}

		if (operation != null){
			OperationOptions options = operation.getOptions();
			if (options.isSkipSupported()){
				enableCheckBox.setVisible(true);
				enableCheckBox.setSelection(!options.isSkipped());
			}else if (options.isEnableSupported()){
				enableCheckBox.setVisible(true);
				enableCheckBox.setSelection(options.isEnabled());
			}else
				enableCheckBox.setVisible(false);
		}
		redraw();
	}

	private void updateOperationBackgroundColor(Color newColor) {
		// TODO Auto-generated method stub
		if (newColor == null){
			if (isSelected)
				newColor = selectedBackgroundColor;
			else if (operation != null && ((operation.getOptions().isSkipSupported() && operation.getOptions().isSkipped()) || 
					(operation.getOptions().isEnableSupported() && !operation.getOptions().isEnabled())))
				newColor = skippedBackgroundColor;
			else 
				newColor = defaultBackgroundColor;
		}
//		Font currFont = getFont();
//		FontData[] fontData2 = currFont.getFontData();
//		for (FontData fontData : fontData2) {
//		fontData.setStyle(isSelected ? SWT.BOLD : SWT.NORMAL);
//		}
//		Font newFont = new Font(getDisplay(), fontData2);
//		setFont(newFont);
		setBackground(newColor);
		Control[] children = getChildren();
		for (Control control : children) {
			control.setBackground(newColor);
//			control.setFont(newFont);
		}
	}

	/**
	 * Adds the listener to the collection of listeners who will
	 * be notified when the control is selected by the user, by sending
	 * it one of the messages defined in the <code>SelectionListener</code>
	 * interface.
	 * <p>
	 * <code>widgetSelected</code> is called when the control is selected by the user.
	 * <code>widgetDefaultSelected</code> is not called.
	 * </p>
	 *
	 * @param listener the listener which should be notified
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 *
	 * @see SelectionListener
	 * @see #removeSelectionListener
	 * @see SelectionEvent
	 */
	public void addSelectionListener(SelectionListener listener) {
		checkWidget ();
		if (listener == null) 
			SWT.error (SWT.ERROR_NULL_ARGUMENT);

		selectionListenerList.add(listener);
	}

	/**
	 * Removes the listener from the collection of listeners who will
	 * be notified when the control is selected by the user.
	 *
	 * @param listener the listener which should no longer be notified
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 *
	 * @see SelectionListener
	 * @see #addSelectionListener
	 */
	public void removeSelectionListener (SelectionListener listener) {
		checkWidget ();
		if (listener == null) 
			SWT.error (SWT.ERROR_NULL_ARGUMENT);
		selectionListenerList.remove(listener);
	}

	public List<SelectionListener> getSelectionListeners() {
		return new ArrayList<SelectionListener>(selectionListenerList);
	}

	protected void fireSelectionListener() {
		for (SelectionListener selectionListener : selectionListenerList) {
			Event event = new Event();
			event.display = getDisplay();
			event.widget = this;
			selectionListener.widgetSelected(new SelectionEvent(event));
		}
	}


	protected void disposeResources() {

		if (mouseOverBorderColor != null) {
			mouseOverBorderColor.dispose();
		}
		if (defaultBorderColor != null) {
			defaultBorderColor.dispose();
		}
		if (selectedBackgroundColor != null) {
			selectedBackgroundColor.dispose();
		}
		if (defaultBackgroundColor != null) {
			defaultBackgroundColor.dispose();
		}

		if (skippedBackgroundColor != null) {
			skippedBackgroundColor.dispose();
		}
		
		if (runningBackgroundColor != null) 
			runningBackgroundColor.dispose();
		
		if (processingStatusFontColor != null)
			processingStatusFontColor.dispose();
		
		if (interruptedStatusFontColor != null)
			interruptedStatusFontColor.dispose();
		
		if (errorStatusFontColor != null)
			errorStatusFontColor.dispose();
		
		if (defaultStatusFontColor != null)
			defaultStatusFontColor.dispose();
		
		if (operation != null) {
			operation.removeOperationStatusListener(operationStatusListener);
		}
	}



	private final class OperationMouseListener implements MouseTrackListener, MouseListener {
		public void mouseEnter(MouseEvent arg0) {
			isMouseOver = true;
			updateUI();
//			setBorderColor(mouseOverBorderColor);
//			setBorderSize(3);
		}

		public void mouseExit(MouseEvent arg0) {
			isMouseOver = false;
			updateUI();
//			setBorderColor(defaultBorderColor);
//			setBorderSize(1);
		}

		public void mouseHover(MouseEvent arg0) {
		}
		public void mouseDoubleClick(MouseEvent me) {
			forceSelected(true);
		}
		public void mouseDown(MouseEvent me) {
		}
		public void mouseUp(MouseEvent me) {
			setSelected(true);
		}
	}

	public interface OperationEnableListener{
		public void setEnable(boolean isEnabled);
	}
	
	public void addOperationEnableListener(OperationEnableListener listener){
		enableListeners.add(listener);
	}
	
	public void removeOperationEnableListener(OperationEnableListener listener){
		enableListeners.remove(listener);
	}
	
	public void dispose(){
		super.dispose();
		operation = null;
		if (enableListeners != null){
			enableListeners.clear();
			enableListeners = null;
		}
		mouseTrackListener = null;
		selectionListenerList.clear();
		selectionListenerList = null;
		mouseOverBorderColor.dispose();
		defaultBorderColor.dispose();
		selectedBackgroundColor.dispose();
		defaultBackgroundColor.dispose();
		skippedBackgroundColor.dispose();
		processingStatusFontColor.dispose();
		interruptedStatusFontColor.dispose();
		errorStatusFontColor.dispose();
		defaultStatusFontColor.dispose();
		runningBackgroundColor.dispose();
		System.out.println("Operation Composite Disposed");
	}
}
