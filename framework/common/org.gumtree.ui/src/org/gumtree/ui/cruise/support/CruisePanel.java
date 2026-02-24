package org.gumtree.ui.cruise.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.action.Action;
import org.eclipse.nebula.effects.stw.Transition;
import org.eclipse.nebula.effects.stw.TransitionManager;
import org.eclipse.nebula.effects.stw.Transitionable;
import org.eclipse.nebula.effects.stw.transitions.SlideTransition;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.ui.cruise.ICruisePanel;
import org.gumtree.ui.cruise.ICruisePanelManager;
import org.gumtree.ui.cruise.ICruisePanelPage;
import org.gumtree.ui.internal.InternalImage;
import org.gumtree.widgets.swt.forms.ExtendedFormComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("restriction")
public class CruisePanel extends ExtendedFormComposite implements ICruisePanel {

	private static final Logger logger = LoggerFactory
			.getLogger(CruisePanel.class);

	private ICruisePanelManager cruisePanelManager;

	private String currentPageName;

	private StackLayout stackLayout;

	private Form form;

	private SelectionListener transitionLister;

	private TransitionContext transitionContext;

	private Map<String, Composite> pageMap;

	private List<String> pagenames;

	@Inject
	public CruisePanel(Composite parent, @Optional int style) {
		// Initialise
		super(parent, style);
		setLayout(new FillLayout());
		pageMap = new HashMap<String, Composite>();
		pagenames = new ArrayList<String>();
		transitionContext = new TransitionContext();

		// Create widget
		createCruisePanel(this);
		
		// Set current page
		stackLayout.topControl = pageMap.get(currentPageName);
	}

	protected void createCruisePanel(Composite parent) {
		parent.setLayout(new FillLayout());
		ScrolledForm scrolledForm =  getToolkit().createScrolledForm(parent);
		form = scrolledForm.getForm();
		getToolkit().decorateFormHeading(form);

		stackLayout = new StackLayout();
		form.getBody().setLayout(stackLayout);

		ICruisePanelPage[] pages = getCruisePanelManager().getRegisteredPages();
		for (ICruisePanelPage page : pages) {
			createPage(form.getBody(), page);
		}
		if (pages.length > 0) {
			currentPageName = pages[0].getName();
			form.setText(currentPageName);
		}

		// Transition effects
		TransitionManager transitionManager = new TransitionManager(
				new Transitionable() {
					@Override
					public void setSelection(int index) {
					}

					@Override
					public int getSelection() {
						return 0;
					}

					@Override
					public double getDirection(int toIndex, int fromIndex) {
						return transitionContext.transitionDirection;
					}

					@Override
					public Control getControl(int index) {
						return transitionContext.targetControl;
					}

					@Override
					public Composite getComposite() {
						return form.getBody();
					}

					@Override
					public void addSelectionListener(SelectionListener listener) {
						transitionLister = listener;
					}
				});
		Transition transition = new SlideTransition(transitionManager);
		transitionManager.setTransition(transition);

		// Navigation
		Action left = new Action("",
				InternalImage.LEFT_ARROW_16.getDescriptor()) {
			public void runWithEvent(Event e) {
				int index = pagenames.indexOf(getCurrentPageName());
				int newIndex = index - 1;
				if (newIndex < 0) {
					newIndex = pagenames.size() - 1;
				}
				currentPageName = pagenames.get(newIndex);
				transitionContext.targetControl = pageMap.get(currentPageName);
				transitionContext.transitionDirection = Transition.DIR_RIGHT;
				transitionLister.widgetSelected(new SelectionEvent(e));
				stackLayout.topControl = transitionContext.targetControl;
				form.setText(currentPageName);
				CruisePanel.this.layout(true, true);
				// TODO: log
			}
		};
		form.getToolBarManager().add(left);

		Action right = new Action("",
				InternalImage.RIGHT_ARROW_16.getDescriptor()) {
			public void runWithEvent(Event e) {
				int index = pagenames.indexOf(getCurrentPageName());
				int newIndex = index + 1;
				if (newIndex == pagenames.size()) {
					newIndex = 0;
				}
				currentPageName = pagenames.get(newIndex);
				transitionContext.targetControl = pageMap.get(currentPageName);
				transitionContext.transitionDirection = Transition.DIR_LEFT;
				transitionLister.widgetSelected(new SelectionEvent(e));
				stackLayout.topControl = transitionContext.targetControl;
				form.setText(currentPageName);
				CruisePanel.this.layout(true, true);
				// TODO: log
			}
		};
		form.getToolBarManager().add(right);

		form.getToolBarManager().update(true);
	}

	private void createPage(Composite parent, ICruisePanelPage page) {
		try {
			Composite normalPage = page.create(parent);
			pageMap.put(page.getName(), normalPage);
		} catch (Exception e) {
			pageMap.put(page.getName(), createErrorPage(parent, e));
			logger.warn("Error occurred when creating page " + page.getName());
		}
		pagenames.add(page.getName());
	}

	// TODO: test this method!
	@Override
	public void setPage(String pageName) {
		Composite composite = pageMap.get(pageName);
		if (composite != null) {
			stackLayout.topControl = composite;
		}
		currentPageName = pageName;
		form.setText(pageName);
	}

	@Override
	public String getCurrentPageName() {
		return currentPageName;
	}

	@Override
	protected void disposeWidget() {
		if (pageMap != null) {
			pageMap.clear();
			pageMap = null;
		}
		cruisePanelManager = null;
		stackLayout = null;
		form = null;
		transitionLister = null;
		transitionContext = null;
	}

	/*************************************************************************
	 * Helper methods
	 *************************************************************************/

	private Composite createErrorPage(Composite parent, Exception e) {
		// TODO
		return getWidgetFactory().createComposite(parent);
	}

	class TransitionContext {
		private Control targetControl;
		private double transitionDirection;
	}

	/*************************************************************************
	 * Components
	 *************************************************************************/

	public ICruisePanelManager getCruisePanelManager() {
		if (cruisePanelManager == null) {
			cruisePanelManager = ServiceUtils
					.getService(ICruisePanelManager.class);
		}
		return cruisePanelManager;
	}

	public void setCruisePanelManager(ICruisePanelManager cruisePanelManager) {
		this.cruisePanelManager = cruisePanelManager;
	}

}
