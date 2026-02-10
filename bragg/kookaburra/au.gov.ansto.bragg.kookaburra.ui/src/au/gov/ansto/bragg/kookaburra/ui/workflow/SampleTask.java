package au.gov.ansto.bragg.kookaburra.ui.workflow;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.statushandlers.StatusManager;
import org.gumtree.ui.util.swt.FloatTextVerifyListener;
import org.gumtree.widgets.swt.util.UIResources;
import org.gumtree.workflow.ui.AbstractTaskView;
import org.gumtree.workflow.ui.ITaskView;

import au.gov.ansto.bragg.kookaburra.experiment.model.Sample;
import au.gov.ansto.bragg.kookaburra.experiment.util.ExperimentModelUtils;
import au.gov.ansto.bragg.kookaburra.experiment.util.InstrumentOperationHelper;
import au.gov.ansto.bragg.kookaburra.experiment.util.SampleType;
import au.gov.ansto.bragg.kookaburra.ui.internal.Activator;
import au.gov.ansto.bragg.kookaburra.ui.internal.InternalImage;

/**
 * Sample task handles the input of sample details. The order of input is mapped
 * directly to the available sample holder for Kookaburra.
 * 
 */
public class SampleTask extends AbstractExperimentTask {
	
	@Override
	protected ITaskView createViewInstance() {
		return new SampleTaskView();
	}
	
	class SampleTaskView extends AbstractTaskView {

		private Cursor handCursor;
		
		public void createPartControl(final Composite parent) {
			parent.addPaintListener(new PaintListener() {
				@Override
				public void paintControl(PaintEvent event) {
					fireRefresh();
				}
			});
			
			handCursor = new Cursor(Display.getDefault(), SWT.CURSOR_HAND);
			
			GridLayoutFactory.createFrom(new GridLayout(5, false))
					.margins(1, 1).spacing(1, 0).applyTo(parent);
			
			/*****************************************************************
			 * Create table (under UI thread)
			 *****************************************************************/
			Realm.runWithDefault(DisplayRealm.getRealm(Display.getDefault()), new Runnable() {
				public void run() {
					/*****************************************************************
					 * Create top button area
					 *****************************************************************/
					Composite buttonArea = getToolkit().createComposite(parent);
					GridDataFactory.fillDefaults().span(5, 1).applyTo(buttonArea);
					createButtonArea(buttonArea);
					
					createTable(parent);
					
					// Col 1
					Label label = getToolkit().createLabel(parent, "");
					GridDataFactory.swtDefaults().applyTo(label);

					// Col 2
					label = getToolkit().createLabel(parent, "");
					GridDataFactory.swtDefaults().applyTo(label);
					
					// Col 3: Clears all names
					Button clearNameButton = getToolkit().createButton(parent, "Clear All", SWT.PUSH);
					GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(clearNameButton);
					clearNameButton.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							for (Sample sample : getExperiment().getSamples()) {
								sample.setName("");
							}
						}
					});
					
					// Col 4: Fills all thickness
					Button fillButton = getToolkit().createButton(parent, "Fill from Sample 1", SWT.PUSH);
					GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(fillButton);
					fillButton.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							if (getExperiment().getSamples().size() > 0) {
								float firstSampleThickness = getExperiment()
										.getSamples().get(0).getThickness();
								for (Sample sample : getExperiment().getSamples()) {
									sample.setThickness(firstSampleThickness);
								}
							}
						}
					});
					
					// Col 5: Clears all descriptions
					Button clearDescButton = getToolkit().createButton(parent, "Clear All", SWT.PUSH);
					GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(clearDescButton);
					clearDescButton.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							for (Sample sample : getExperiment().getSamples()) {
								sample.setDescription("");
							}
						}
					});
					
				}
			});	
		}
		
		private void createTable(Composite parent) {
			// Create header
			Label label = getToolkit().createLabel(parent, "Run   ");
			Font boldFont = UIResources.getDefaultFont(SWT.BOLD);
			label.setFont(boldFont);
			label = getToolkit().createLabel(parent, "Type");
			label.setFont(boldFont);
			label = getToolkit().createLabel(parent, "Sample Name");
			label.setFont(boldFont);
			label = getToolkit().createLabel(parent, "Thickness (mm)");
			label.setFont(boldFont);
			label = getToolkit().createLabel(parent, "Sample Description");
			label.setFont(boldFont);
			
			// Create data binding context
			DataBindingContext bindingContext = new DataBindingContext();
			
			for (int i = 0; i < getExperiment().getSamples().size(); i++) {
				
				final Sample sample = getExperiment().getSamples().get(i);
				
				/*************************************************************
				 * Create editable widgets
				 *************************************************************/
				// Col 1
				Button runButton = getToolkit().createButton(parent, (i + 1) + ".", SWT.CHECK);
				
				// Col 2
				Composite typeHolder = getToolkit().createComposite(parent, SWT.BORDER);
				GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(2).applyTo(typeHolder);
				final Label typeButton = getToolkit().createLabel(typeHolder, "");
				typeButton.setImage(InternalImage.DOWN_ARROW.getImage());				
				// Create menu
				final Menu menu = createMenu(typeButton, sample);
				// Show mouse pointer
				typeButton.addMouseTrackListener(new MouseTrackAdapter() {
					public void mouseEnter(MouseEvent e) {
						typeButton.setCursor(handCursor);
					}
					public void mouseExit(MouseEvent e) {
						typeButton.setCursor(null);
					}
				});
				// Show menu
				typeButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseDown(MouseEvent e) {
						menu.setVisible(true);
						Point location = typeButton.toDisplay(typeButton.getLocation());
						menu.setLocation(location.x, location.y + typeButton.getSize().y);
					}
				});				
				final Label typeLabel = getToolkit().createLabel(typeHolder, sample.getType().name());
				GridDataFactory.fillDefaults().hint(100, 14).applyTo(typeLabel);
				
				// Col 3
				final Text nameText = getToolkit().createText(parent, "");
				GridDataFactory.fillDefaults().hint(200, 14).applyTo(nameText);
				
				// Col 4
				Text thicknessText = getToolkit().createText(parent, "");
				// Check for float
				thicknessText.addVerifyListener(new FloatTextVerifyListener());
				GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).hint(90, 14).applyTo(thicknessText);
				
				// Col 5
				Text descText = getToolkit().createText(parent, "");
				GridDataFactory.swtDefaults().hint(400, 14).applyTo(descText);

				/*************************************************************
				 * Data binding
				 *************************************************************/
				bindingContext.bindValue(
						WidgetProperties.buttonSelection().observe(runButton),
						BeanProperties.value("runnable").observe(sample),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(
						WidgetProperties.text(SWT.Modify).observe(nameText),
						BeanProperties.value("name").observe(sample),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(
						WidgetProperties.text(SWT.Modify).observe(thicknessText),
						BeanProperties.value("thickness").observe(sample),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext.bindValue(
						WidgetProperties.text(SWT.Modify).observe(descText),
						BeanProperties.value("description").observe(sample),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				
				/*************************************************************
				 * Trigger to set sample runnable if valid text is entered
				 *************************************************************/
				nameText.addModifyListener(new ModifyListener() {
					@Override
					public void modifyText(ModifyEvent event) {
						if (nameText.getText() != null && nameText.getText().length() > 0) {
							sample.setRunnable(true);
						} else {
							sample.setRunnable(false);
						}
					}
				});
				
				/*************************************************************
				 * Auto update sample name based on type selection
				 *************************************************************/
				// TODO: Remove property change listener to avoid memory leak?
				sample.addPropertyChangeListener("type", new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						if (sample.getType().equals(SampleType.EMPTY_BEAM)) {
							nameText.setText("MT beam");
						} else if (sample.getType().equals(SampleType.EMPTY_CELL)) {
							nameText.setText("MT cell");
						}
						typeLabel.setText(sample.getType().name());
					}					
				});
			}
		}
		
		private Menu createMenu(Control parent, final Sample sample) {
			Menu menu = new Menu(parent);
			parent.setMenu(menu);
			for (final SampleType type : SampleType.values()) {
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText(type.name());
				item.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						sample.setType(type);
					}
				});
			}
			return menu;
		}
		
		private void createButtonArea(final Composite parent) {
			GridLayoutFactory.swtDefaults().numColumns(5).equalWidth(false).margins(0, SWT.DEFAULT).applyTo(parent);
			
			/*****************************************************************
			 * Use fixed position
			 *****************************************************************/
			Button fixedPositionButton = getToolkit().createButton(parent, "Fix Sample Holder", SWT.CHECK);
			fixedPositionButton.setFont(UIResources.getDefaultFont(SWT.BOLD));
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).grab(true, false).applyTo(fixedPositionButton);
			DataBindingContext bindingContext = new DataBindingContext();
			bindingContext.bindValue(WidgetProperties.buttonSelection().observe(fixedPositionButton), 
					BeanProperties.value("fixedSamplePosition").observe(getExperiment()));
			
			
			/*****************************************************************
			 * Import CSV
			 *****************************************************************/
			Button importCSVButton = getToolkit().createButton(parent, "Load CSV", SWT.PUSH);
			importCSVButton.setImage(InternalImage.IMPORT.getImage());
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(importCSVButton);
			importCSVButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					FileDialog dialog = new FileDialog(parent.getShell(), SWT.OPEN);
					String filename = dialog.open();
					try {
						ExperimentModelUtils.loadSamplesFromCSV(getExperiment(), filename);
					} catch (IOException ioe) {
						StatusManager.getManager().handle(
								new Status(IStatus.ERROR, Activator.PLUGIN_ID,
										"Failed to load from CSV file.", ioe),
								StatusManager.SHOW);
					}
				}
			});

			/*****************************************************************
			 * Export CSV
			 *****************************************************************/
			Button exportCSVButton = getToolkit().createButton(parent, "Export CSV", SWT.PUSH);
			exportCSVButton.setImage(InternalImage.EXPORT.getImage());
			exportCSVButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					FileDialog dialog = new FileDialog(parent.getShell(), SWT.SAVE);
					String filename = dialog.open();
					try {
						ExperimentModelUtils.saveSamplesToCSV(getExperiment(), filename);
					} catch (IOException ioe) {
						StatusManager.getManager().handle(
								new Status(IStatus.ERROR, Activator.PLUGIN_ID,
										"Failed to save in CSV file.", ioe),
								StatusManager.SHOW);
					}
				}
			});
			
			/*****************************************************************
			 * Separator
			 *****************************************************************/
			getToolkit().createLabel(parent, "       ");
			
			/*****************************************************************
			 * Drive to load position
			 *****************************************************************/
			Button loadButton = getToolkit().createButton(parent, "Drive to load position", SWT.PUSH);
			loadButton.setImage(InternalImage.LOAD.getImage());
			loadButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					InstrumentOperationHelper.setToSampleLoadPosition();
				}
			});
		}
		
		public void dispose() {
			if (handCursor != null) {
				handCursor.dispose();
				handCursor = null;
			}
		}
		
	}
	
}
