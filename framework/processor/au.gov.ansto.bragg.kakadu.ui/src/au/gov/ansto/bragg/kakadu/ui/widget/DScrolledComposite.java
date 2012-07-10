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
package au.gov.ansto.bragg.kakadu.ui.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import au.gov.ansto.bragg.kakadu.ui.util.DisplayManager;

/**
 * The widget shows content in scrolling area. 
 * To scroll side buttons are used.
 *  
 * @author Danil Klimontov (dak)
 */
public class DScrolledComposite extends Composite {

	protected Button leftButton;
	protected Button rightButton;
	protected ScrolledCompositeExtension scrolledComposite;
	protected Composite contentComposite;

	/**
	 * Creates new <code>DScrolledComposite</code> widget.
	 * @param parent parent Composite
	 * @param style SWT style
	 */
	public DScrolledComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
		initListeners();
	}

	/**
	 * Initializes UI components for the widget.
	 */
	protected void initialize() {
		GridLayout gridLayout = new GridLayout ();
		gridLayout.numColumns = 3;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 1;
		gridLayout.verticalSpacing = 0;
		setLayout (gridLayout);

		leftButton = new Button (this, SWT.ARROW | SWT.LEFT);
		GridData data = new GridData ();
		data.verticalAlignment = GridData.FILL;
		leftButton.setLayoutData (data);

		scrolledComposite = new ScrolledCompositeExtension(this, SWT.NONE);
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		scrolledComposite.setLayoutData (data);
		
		contentComposite = new Composite (scrolledComposite, SWT.NONE){
			public void layout() {
				//update size of scrollable composite because it can be changed
				setSize(computeSize(SWT.DEFAULT, SWT.DEFAULT));
				super.layout();
				//because of size change buttons state need to be updated appropriately 
				updateButtons();
				scrolledComposite.updateContentLoaction();
			}
		};
		scrolledComposite.setContent(contentComposite);

		rightButton = new Button (this, SWT.ARROW | SWT.RIGHT);
		data = new GridData ();
		data.verticalAlignment = GridData.FILL;
		rightButton.setLayoutData (data);
	}

	/**
	 * Initializes listeners for the widget.
	 */
	protected void initListeners() {
		final ScrollListener leftScrollListener = new ScrollListener(SWT.LEFT);
		leftButton.addSelectionListener(leftScrollListener);
		leftButton.addMouseListener(leftScrollListener);
		
		final ScrollListener rihgtScrollListener = new ScrollListener(SWT.RIGHT);
		rightButton.addSelectionListener(rihgtScrollListener);
		rightButton.addMouseListener(rihgtScrollListener);

		//validates scroll area after control was resized.
		addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
			}
			public void controlResized(ControlEvent e) {
				DisplayManager.getDefault().asyncExec(new Runnable() {
					public void run() {
						updateButtons();
						scrolledComposite.updateContentLoaction();
					}
				});
			}
		});
	}


	/**
	 * Updates buttons enabled states.
	 */
	protected void updateButtons() {
		final Rectangle bounds = scrolledComposite.getContent().getBounds();
		final Rectangle clientArea = scrolledComposite.getClientArea();
		
		if (clientArea.width >= bounds.width) {
			leftButton.setEnabled(false);
			rightButton.setEnabled(false);
		} else {
			leftButton.setEnabled(bounds.x < 0);
			rightButton.setEnabled(bounds.x + bounds.width > clientArea.width);
		}

		
	}

	/**
	 * Gets the content Composite that will be scrolled.
	 * Use the Composite to put custom objects inside.
	 * @return Composite object
	 */
	public Composite getContent() {
		return contentComposite;
	}

	/**
	 * Repeats scrolling to defined direction until button is presses or the end of list achieved.
	 * 
	 * @author Danil Klimontov (dak)
	 */
	protected class ScrollListener implements MouseListener, SelectionListener {
		private boolean mouseUp = false;
		private boolean buttonEnabled;
		final private Button button;
		private final int dirrection;
		private boolean isScrolledContinuesly = false;

		/**
		 * Creates new listener instance.
		 * @param dirrection scrolling direction. Use SWT.LEFT or SWT.RIGHT constants.
		 */
		public ScrollListener(int dirrection) {
			this.dirrection = dirrection;
			
			switch (dirrection) {
				case SWT.LEFT:
					button = leftButton;
					break;
				case SWT.RIGHT:
				default:
					button = rightButton;
					break;
			}
		}

		public void mouseDoubleClick(MouseEvent e) {
		}

		public void mouseDown(MouseEvent e) {
			mouseUp = false;
			buttonEnabled = button.getEnabled();
			isScrolledContinuesly = false;
			
			new Thread() {
				public void run() {
					try {
						//Catch only long pressing of the button
						sleep(350);
					} catch (InterruptedException e) {
						return;
					}
					while (buttonEnabled && !mouseUp) { 
						
						DisplayManager.getDefault().asyncExec(new Runnable() {

							public void run() {
								switch (dirrection) {
								case SWT.LEFT:
									scrolledComposite.scrollLeft(2);
									break;
								case SWT.RIGHT:
								default:
									scrolledComposite.scrollRight(2);
									break;
								}
								updateButtons();
								buttonEnabled = button.getEnabled();
								
								//mark that scrolling is already done by long pressing
								isScrolledContinuesly = true;
							}
						});
						
						try {
							sleep(5);
						} catch (InterruptedException e) {
							return;
						}
					}
				}
			}.start();
		}

		public void mouseUp(MouseEvent e) {
			mouseUp = true;
		}
		
		public void widgetDefaultSelected(SelectionEvent e) {
		}

		public void widgetSelected(SelectionEvent e) {
			if (isScrolledContinuesly) {
				//scrolling will be performed by long pressing on the button.
				//be the way should not be scrolled for a big step after this.
				return;
			}
			switch (dirrection) {
			case SWT.LEFT:
				scrolledComposite.scrollLeft(70);
				break;
			case SWT.RIGHT:
			default:
				scrolledComposite.scrollRight(70);
				break;
			}
			updateButtons();
		}
	}

	/**
	 * Extends ScrolledComposite widget with scrolling methods.
	 * Scrolling can be supported even if ScrollBars are not visible.
	 * 
	 * @author Danil Klimontov (dak)
	 */
	protected class ScrolledCompositeExtension extends ScrolledComposite {

		private ScrolledCompositeExtension(Composite parent, int style) {
			super(parent, style);
		}
		
		/**
		 * Updates content location for scrolling area.
		 * Should be called if control size was changed. 
		 */
		public void updateContentLoaction() {
			checkWidget();
			final Rectangle bounds = this.getContent().getBounds();
			final Rectangle clientArea = getClientArea();
			final Point newLocation = new Point(bounds.x , bounds.y);

			if (newLocation.x + bounds.width < clientArea.width) {
				newLocation.x = clientArea.width - bounds.width;
			}
			if (newLocation.x > 0 || clientArea.width >= bounds.width) {
				newLocation.x = 0;
			}
			
			this.getContent().setLocation(newLocation);
			
		}

		/**
		 * Scrolls left.
		 * @param stepSize shift size in pixels
		 */
		public void scrollLeft(int stepSize) {
			checkWidget();
			final Rectangle bounds = this.getContent().getBounds();
			final Rectangle clientArea = getClientArea();
			final Point newLocation = new Point(bounds.x , bounds.y);

			newLocation.x += stepSize;
			
			if (newLocation.x > 0 || clientArea.width >= bounds.width) {
				newLocation.x = 0;
			} else if (newLocation.x + bounds.width < clientArea.width) {
				newLocation.x = clientArea.width - bounds.width;
			}
			
			this.getContent().setLocation(newLocation);
		}

		/**
		 * Scrolls right.
		 * @param stepSize shift size in pixels
		 */
		public void scrollRight(int stepSize) {
			checkWidget();
			final Rectangle bounds = this.getContent().getBounds();
			final Rectangle clientArea = getClientArea();
			final Point newLocation = new Point(bounds.x , bounds.y);

			newLocation.x -= stepSize;
			
			if (newLocation.x > 0 || clientArea.width >= bounds.width) {
				newLocation.x = 0;
			} else if (newLocation.x + bounds.width < clientArea.width) {
				newLocation.x = clientArea.width - bounds.width;
			}
			
			this.getContent().setLocation(newLocation);
		}
	}

}
