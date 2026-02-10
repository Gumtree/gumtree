/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*******************************************************************************/
package au.gov.ansto.bragg.echidna.exp.task;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.gumtree.gumnix.sics.batch.ui.model.ISicsCommandElement;
import org.gumtree.gumnix.sics.batch.ui.util.SicsBatchUIUtils;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITaskView;
import org.gumtree.workflow.ui.models.AbstractModelObject;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.echidna.exp.command.DoRTCommand;

/**
 * @author nxi
 * Created on 24/07/2009
 */
public class DoRTScanTask extends AbstractEchidnaScanTask {

    public final static String TASK_TITLE = "Room Temperature Robot Scan";
//  private DoRTCommand command;
    /**
     * 
     */
    public DoRTScanTask() {
        super();
    }
    
    public void initialise() {
        super.initialise();
//      command = new DoRTCommand();
////      command.setSicsVariable("run sc");
////      command.setValue("0");
//      getDataModel().addCommand(command);
    }

    protected ITaskView createViewInstance() {
        return new DoRTScanTaskView();
    }
        
    private class DoRTScanTaskView extends AbstractTaskView {
        protected void createCommandUI(final Composite parent, final DoRTCommand command){
            
//          final DoRTCommand command = new DoRTCommand();
//          getDataModel().addCommand(command);

            getToolkit().createLabel(parent, "doRT ");
            final Text sampnameText = getToolkit().createText(parent, "");
            GridDataFactory.swtDefaults().hint(SWT.DEFAULT, SWT.DEFAULT).align(
                    SWT.FILL, SWT.CENTER).grab(true, false).applyTo(sampnameText);
            addValidator(sampnameText, notEmptyValidator);
            sampnameText.setToolTipText("sample information");

            final Text sizeText = getToolkit().createText(parent, "");
            GridDataFactory.swtDefaults().hint(SWT.DEFAULT, SWT.DEFAULT).align(
                    SWT.FILL, SWT.CENTER).grab(true, false).applyTo(sizeText);
            addValidator(sizeText, notEmptyValidator);
            sizeText.setToolTipText("container or bulk sample size");

//          final Text sampposText = getToolkit().createText(parent, "");
//          GridDataFactory.fillDefaults().grab(true, false).applyTo(sampposText);
//          addValidator(sampposText, floatValidator);
            final ComboViewer sampposAB = new ComboViewer(parent, SWT.READ_ONLY);
            sampposAB.setContentProvider(new ArrayContentProvider());
            sampposAB.setLabelProvider(new LabelProvider());
            sampposAB.setSorter(new ViewerSorter());
            sampposAB.setInput(new String[]{"A", "B"});
//          sampposAB.setSelection(new StructuredSelection("A"));

            final ComboViewer sampposNumber = new ComboViewer(parent, SWT.READ_ONLY);
            sampposNumber.setContentProvider(new ArrayContentProvider());
            sampposNumber.setLabelProvider(new LabelProvider());
//          sampposNumber.setSorter(null);
            String[] numberInput = new String[50];
            for (int i = 0; i < 50; i++)
                numberInput[i] = String.valueOf(i + 1);
            sampposNumber.setInput(numberInput);
//          sampposNumber.setSelection(new StructuredSelection(numberInput[0]));
            
//          Group group = new Group(parent, SWT.NONE);
//          getToolkit().adapt(group);
//          group.setText("position-2");
//          GridLayout propertiesGridLayout = new GridLayout ();
//          propertiesGridLayout.numColumns = 4;
//          propertiesGridLayout.marginHeight = 3;
//          propertiesGridLayout.marginWidth = 3;
//          propertiesGridLayout.horizontalSpacing = 3;
//          propertiesGridLayout.verticalSpacing = 3;
//          group.setLayout (propertiesGridLayout);
//          GridData data = new GridData ();
//          data.horizontalAlignment = GridData.FILL;
//          data.horizontalSpan = 4;
//          data.grabExcessHorizontalSpace = true;
//          group.setLayoutData (data);
            final ComboViewer overlapsOption = new ComboViewer(parent, SWT.READ_ONLY);
            overlapsOption.setContentProvider(new ArrayContentProvider());
            overlapsOption.setLabelProvider(new LabelProvider());
            overlapsOption.setInput(new String[]{"0", "1", "2"});
            
//          final Text startangText = getToolkit().createText(parent, "");
//          GridDataFactory.swtDefaults().hint(SWT.DEFAULT, SWT.DEFAULT).align(
//                  SWT.FILL, SWT.CENTER).grab(true, false).applyTo(startangText);
//          addValidator(startangText, floatValidator);
//          
//          final Text finishangText = getToolkit().createText(parent, "");
//          GridDataFactory.swtDefaults().hint(SWT.DEFAULT, SWT.DEFAULT).align(
//                  SWT.FILL, SWT.CENTER).grab(true, false).applyTo(finishangText);
//          addValidator(finishangText, floatValidator);
            
            final Text stepsizeText = getToolkit().createText(parent, "");
            GridDataFactory.swtDefaults().hint(SWT.DEFAULT, SWT.DEFAULT).align(
                    SWT.FILL, SWT.CENTER).grab(true, false).applyTo(stepsizeText);
            addValidator(stepsizeText, floatValidator);
            
            final Text tot_timeText = getToolkit().createText(parent, "");
            GridDataFactory.swtDefaults().hint(SWT.DEFAULT, SWT.DEFAULT).align(
                    SWT.FILL, SWT.CENTER).grab(true, false).applyTo(tot_timeText);
            addValidator(tot_timeText, floatValidator);
            
//          final Text rotateText = getToolkit().createText(parent, "");
//          GridDataFactory.swtDefaults().hint(SWT.DEFAULT, SWT.DEFAULT).align(
//                  SWT.FILL, SWT.CENTER).grab(true, false).applyTo(rotateText);
//          Validator limitValidator = new Validator() {
//              
//              private float lower = 0;
//              private float upper = 50;
//              public boolean isValid(String text) {
//                  if (text == null || text.trim().length() == 0)
//                      return false;
//                  try{
//                      float value = Float.valueOf(text);
//                      if (value >= lower && value <= upper) {
//                          return true;
//                      } else {
//                          return false;
//                      }
//                  }catch (Exception e) {
//                      return false;
//                  }
//              }
//              
//              public String getErrorMessage() {
//                  return "Input must be a number within (" + lower + ", " + upper + ")";
//              }
//              
////              public void setLimit(float lower, float upper) {
////                  if (lower < upper) {
////                      this.lower = lower;
////                      this.upper = upper;
////                  }else {
////                      this.upper = lower;
////                      this.lower = upper;
////                  }
////              }
//          };
//          addValidator(rotateText, limitValidator);
//          rotateText.setToolTipText("0 for stationary sample");
            
            Realm.runWithDefault(DisplayRealm.getRealm(Display.getDefault()), new Runnable() {
                public void run() {
                    DataBindingContext bindingContext = new DataBindingContext();
                    bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(sampnameText),
                    		BeanProperties.value("sampname").observe(command),
                            new UpdateValueStrategy(), new UpdateValueStrategy());
                    bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(sizeText),
                    		BeanProperties.value("size").observe(command),
                            new UpdateValueStrategy(), new UpdateValueStrategy());
                    bindingContext.bindValue(ViewerProperties.singleSelection().observe(sampposAB),
                    		BeanProperties.value("sampposAB").observe(command),
                            new UpdateValueStrategy(), new UpdateValueStrategy());
                    bindingContext.bindValue(ViewerProperties.singleSelection().observe(sampposNumber),
                    		BeanProperties.value("sampposNumber").observe(command),
                            new UpdateValueStrategy(), new UpdateValueStrategy());
                    bindingContext.bindValue(ViewerProperties.singleSelection().observe(overlapsOption),
                    		BeanProperties.value("overlaps").observe(command),
                            new UpdateValueStrategy(), new UpdateValueStrategy());
//                  bindingContext.bindValue(TypedSWTObservables.observeText(startangText),
//                          BeansObservables.observeValue(command, "startang"),
//                          new UpdateValueStrategy(), new UpdateValueStrategy());
//                  bindingContext.bindValue(TypedSWTObservables.observeText(finishangText),
//                          BeansObservables.observeValue(command, "finishang"),
//                          new UpdateValueStrategy(), new UpdateValueStrategy());
                    bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(stepsizeText),
                    		BeanProperties.value("stepsize").observe(command),
                            new UpdateValueStrategy(), new UpdateValueStrategy());
                    bindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(tot_timeText),
                    		BeanProperties.value("tot_time").observe(command),
                            new UpdateValueStrategy(), new UpdateValueStrategy());
//                  bindingContext.bindValue(TypedSWTObservables.observeText(rotateText),
//                          BeansObservables.observeValue(command, "rotate"),
//                          new UpdateValueStrategy(), new UpdateValueStrategy());
                }
            });

            Button addButton = getToolkit().createButton(parent, "", SWT.PUSH);
            try {
                addButton.setImage(SicsBatchUIUtils.getBatchEditorImage("ADD"));
            } catch (FileNotFoundException e2) {
                LoggerFactory.getLogger(this.getClass()).error("can not find ADD image", e2);
            }
            addButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    DoRTCommand newCommand = new DoRTCommand();
                    newCommand.setSampname(command.getSampname());
                    newCommand.setSize(command.getSize());
                    newCommand.setSampposAB(command.getSampposAB());
                    newCommand.setSampposNumber(command.getSampposNumber());
//                  newCommand.setStartang(command.getStartang());
//                  newCommand.setFinishang(command.getFinishang());
                    newCommand.setOverlaps(command.getOverlaps());
                    newCommand.setStepsize(command.getStepsize());
                    newCommand.setTot_time(command.getTot_time());
//                  newCommand.setRotate(command.getRotate());
                    getDataModel().insertCommand(getDataModel().indexOf(command) + 1, newCommand);
                    refreshUI(parent);
                    notifyPropertyChanged(newCommand, null);
                }
            });
            addButton.setToolTipText("click to add a new line");
            
            Button removeButton = getToolkit().createButton(parent, "", SWT.PUSH);
            try {
                removeButton.setImage(SicsBatchUIUtils.getBatchEditorImage("REMOVE"));
            } catch (FileNotFoundException e1) {
                LoggerFactory.getLogger(this.getClass()).error("can not find REMOVE image", e1);
            }
            removeButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    if (getDataModel().getCommands().length <= 1)
                        return;
                    getDataModel().removeCommand(command);
                    refreshUI(parent);
                    notifyPropertyChanged(command, null);
                }
            });
            removeButton.setToolTipText("click to remove this line, if it is not the single line");
        }
        
        public void createPartControl(Composite parent) {
            GridLayoutFactory.swtDefaults().numColumns(10).applyTo(parent);
            if (getDataModel().getCommands().length == 0){
                DoRTCommand newCommand = new DoRTCommand();
                newCommand.setSampposAB("A");
                newCommand.setSampposNumber("1");
                getDataModel().addCommand(newCommand);
            }
            createTaskUI(parent);
//          createCommandUI(parent, command);

        }

        private void createLabelArea(Composite parent) {
            getToolkit().createLabel(parent, "");
            getToolkit().createLabel(parent, "Sample");
            getToolkit().createLabel(parent, "Sample");
            Label sampposLabel = getToolkit().createLabel(parent, "Sample position");
//          getToolkit().createLabel(parent, "Starting");
//          getToolkit().createLabel(parent, "Finishing");
            getToolkit().createLabel(parent, "Overlaps");
            getToolkit().createLabel(parent, "Step size");
            getToolkit().createLabel(parent, "Total");
//          getToolkit().createLabel(parent, "Rotate");
//          Label sampposLabel = getToolkit().createLabel(parent, "sample_position");
            GridDataFactory.fillDefaults().span(2, 1).applyTo(sampposLabel);
//          getToolkit().createLabel(parent, "start_angle");
//          getToolkit().createLabel(parent, "finish_angle");
//          getToolkit().createLabel(parent, "number_of_step");
//          getToolkit().createLabel(parent, "total_time");
            getToolkit().createLabel(parent, "");
            getToolkit().createLabel(parent, "");
            
            getToolkit().createLabel(parent, "");
            getToolkit().createLabel(parent, "name");
            getToolkit().createLabel(parent, "shape");
            Label sampposLabel2 = getToolkit().createLabel(parent, "");
//          getToolkit().createLabel(parent, "angle(deg)");
//          getToolkit().createLabel(parent, "angle(deg)");
            getToolkit().createLabel(parent, "");
            getToolkit().createLabel(parent, "(deg)");
            getToolkit().createLabel(parent, "time (h)");
//          getToolkit().createLabel(parent, "(deg/s)");
//          Label sampposLabel = getToolkit().createLabel(parent, "sample_position");
            GridDataFactory.fillDefaults().span(2, 1).applyTo(sampposLabel2);
//          getToolkit().createLabel(parent, "start_angle");
//          getToolkit().createLabel(parent, "finish_angle");
//          getToolkit().createLabel(parent, "number_of_step");
//          getToolkit().createLabel(parent, "total_time");
            getToolkit().createLabel(parent, "");
            getToolkit().createLabel(parent, "");
        }

        private void createTaskUI(Composite parent) {
            createLabelArea(parent);
            for (ISicsCommandElement command : getDataModel().getCommands()){
                if (command instanceof DoRTCommand){
                    createCommandUI(parent, (DoRTCommand) command);
                    addCommandListener(command);
                }
            }
//          parent.update();
//          parent.layout();
//          parent.redraw();
            fireRefresh();
        }

        private void addCommandListener(final ISicsCommandElement command) {
            if (command instanceof AbstractModelObject)
                ((AbstractModelObject) command).addPropertyChangeListener(new PropertyChangeListener() {
                    
                    public void propertyChange(PropertyChangeEvent arg0) {
                        if (arg0.getPropertyName().equals("nosteps") || arg0.getPropertyName().equals("tot_time"))
                            notifyPropertyChanged(command, arg0);
                    }
                });
        }

        private void refreshUI(Composite parent) {
            for (Control control : parent.getChildren())
                if (!control.isDisposed())
                    control.dispose();
            createTaskUI(parent);
        }
        
    }

    /* (non-Javadoc)
     * @see au.gov.ansto.bragg.echidna.exp.task.AbstractEchidnaScanTask#createSingleCommandLine(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createSingleCommandLine(Composite parent) {
        
    }

    @Override
    public String getTitle() {
        return TASK_TITLE;
    }

    /* (non-Javadoc)
     * @see au.gov.ansto.bragg.echidna.exp.task.AbstractEchidnaScanTask#getEstimatedTime()
     */
    @Override
    public float getEstimatedTime() {
        float estimatedTime = 0;
        for (ISicsCommandElement command : getDataModel().getCommands()){
            if (command instanceof DoRTCommand){
                DoRTCommand doRTCommand = (DoRTCommand) command;
                estimatedTime += doRTCommand.getTot_time();
            }
        }
        return estimatedTime;
    }
    
}