package org.gumtree.ui.dashboard.viewer;

import java.awt.Component;
import java.awt.Frame;
import java.beans.Statement;
import java.lang.reflect.InvocationTargetException;
import java.util.Map.Entry;

import net.miginfocom.swt.MigLayout;

import org.apache.commons.beanutils.BeanUtils;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.gumtree.core.object.IConfigurable;
import org.gumtree.core.object.ObjectCreateException;
import org.gumtree.core.object.ObjectFactory;
import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.ui.dashboard.internal.DashboardEventBus;
import org.gumtree.ui.dashboard.model.Dashboard;
import org.gumtree.ui.dashboard.model.Widget;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.forms.FormControlWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DashboardViewer extends FormControlWidget implements IDashboardViewer {

	private static final Logger logger = LoggerFactory.getLogger(DashboardViewer.class);
	
	private Dashboard model;
	
	private DashboardEventBus eventBus;
	
	private ScrolledForm form;
	
	public DashboardViewer(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout());
		eventBus = new DashboardEventBus();
	}

	protected void widgetDispose() {
		model = null;
		form = null;
		if (eventBus != null) {
			eventBus.dispose();
			eventBus = null;
		}
	}

	@Override
	public Dashboard getModel() {
		return model;
	}

	@Override
	public void setModel(Dashboard model) {
		this.model = model;
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				updateUI();
			}
		});
	}

	protected void updateUI() {
		// Dispose old UI
		for (Control child : this.getChildren()) {
			child.dispose();
		}
		// Clear event bus
		eventBus.clearWidgetRegistry();
		
		// Create dashboard form
		form = getToolkit().createScrolledForm(this);
		if (getModel().getTitle() != null) {
			getToolkit().decorateFormHeading(form.getForm());
			form.setText(getModel().getTitle());
		}
		form.getBody().setLayout(new MigLayout(
				getModel().getLayoutConstraints(),
				getModel().getColConstraints(),
				getModel().getRowConstraints()));
		
		// Create widget
		for (Widget widget : getModel().getWidgets()) {
			try {
				// 1. Create object
				Control control = null;
				if (!widget.isHideTitleBar()) {
					// Use section if show title bar
					int style = Section.TITLE_BAR;
					if (!widget.isCollapsed()) {
						style = style | Section.EXPANDED;
					}
					if (widget.isCollapsible()) {
						style = style | Section.TWISTIE;
					}
					Section section = getToolkit().createSection(form.getBody(), style);
					section.setLayout(new FillLayout());					
					control = createWidget(section, widget);
					section.setClient(control.getParent());
					// Set layout data
					setProperty(section, "layoutData", widget.getLayoutData());
					// Set title
					if (widget.getTitle() != null) {
						section.setText(widget.getTitle());
					}
					// Handle expansion
					section.addExpansionListener(new ExpansionAdapter() {
						public void expansionStateChanged(ExpansionEvent e) {
							form.reflow(true);
						}
					});
				} else {
					control = createWidget(form.getBody(), widget);
					// Set layout data
					setProperty(control.getParent(), "layoutData", widget.getLayoutData());
				}
				// 3. Set all parameters as a whole
				if (control instanceof IConfigurable) {
					IConfigurable configurable = (IConfigurable) control;
					configurable.setParameters(widget.getParameters());
					configurable.afterParametersSet();
				}
				// 4. Set event bus (show no warning)
				setProperty(control, "eventBus", eventBus, false);
				// 5. Adapt look and feel
				getToolkit().adapt(control, true, true);
				// 6. Register publisher info
				if (widget.getId() != null) {
					eventBus.registerPublisherWidget(control, widget.getId());
				}
				// 7. Register subscriber info
				if (widget.getListenToWidgets() != null
						&& control instanceof IEventHandler<?>) {
					String[] publisherIds = widget.getListenToWidgets().split(",");
					for (String publisherId : publisherIds) {
						eventBus.registerSubscriberWidger(publisherId.trim(),
								(IEventHandler<?>) control);
					}
				}
			} catch (Exception e) {
				logger.error("Failed to create widget " + widget.getClassname(), e);
			}
		}
		
		// Layout
		layout(true, true);
	}
	
	private Control createWidget(Composite parent, Widget widget) throws ObjectCreateException, ClassNotFoundException {
		// Create a holder that can catch layout change
		Composite holder = new Composite(parent, SWT.NONE) {
			public void layout (boolean changed, boolean all) {
				super.layout(changed, all);
				form.reflow(changed);
			}
		};
		getToolkit().adapt(holder);
		holder.setLayout(new FillLayout());
		
		Control control = null;
		Class<?> widgetClass = ObjectFactory.instantiateClass(widget.getClassname());
		if (Component.class.isAssignableFrom(widgetClass)) {
			// [GUMTREE-155] Swing support
			Composite SWT_AWT_container = getToolkit().createComposite(holder, SWT.EMBEDDED);
			SWT_AWT_container.setLayout(new FillLayout());
			Frame frame = SWT_AWT.new_Frame(SWT_AWT_container);
			Component component = (Component) ObjectFactory.instantiateObject(widgetClass);
			// Set properties
			for (Entry<String, Object> entry : widget.getParameters().entrySet()) {
				setProperty(component, entry.getKey(), entry.getValue());
			}
			frame.add(component);
			control = SWT_AWT_container;
		} else {
			// Create directly without the section
			control = (Control) ObjectFactory.instantiateObject(
					widget.getClassname(),
					new Class[] { Composite.class, int.class },
					holder, widget.getStyle());
			// Set properties
			for (Entry<String, Object> entry : widget.getParameters().entrySet()) {
				setProperty(control, entry.getKey(), entry.getValue());
			}
		}

		return control;
	}
	
	private void setProperty(Object object, String property, Object value) {
		setProperty(object, property, value, false);
	}
	
	private void setProperty(Object object, String property, Object value, boolean showWarning) {
		try {
			BeanUtils.setProperty(object, property, value);
		} catch (IllegalAccessException e) {
			if (showWarning) {
				logger.warn("Failed to set property \"" + property + "\" for " + object.toString(), e);
			}
		} catch (InvocationTargetException e) {
			if (showWarning) {
				logger.warn("Failed to set property \"" + property + "\" for " + object.toString(), e);
			}
		}
	}

	public void afterParametersSet() {
	}
	
}
