package org.gumtree.msw.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

public class ImageButton extends Canvas {
	// fields
    private final List<ITriggerArea> triggers;
    private final Image[] images;
    private String text;
    // state
    private State state = State.DEFAULT;
    private boolean hit = false;

    // construction
    public ImageButton(Composite parent, int style) {
        super(parent, style);

        triggers = new ArrayList<ITriggerArea>();
        images = new Image[3];

        addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
            	if (images[state.index] != null)
            		e.gc.drawImage(images[state.index], 0, 0);
            	
            	if ((text != null) && !text.isEmpty()) {
                	Font oldFont = e.gc.getFont();
                    e.gc.setFont(getFont());
                	
            		Rectangle bounds = ImageButton.this.getBounds();
                	Point textSize = e.gc.stringExtent(text);
                	
                	int yOffset = state == State.MOUSE_DOWN ? 0 : -1;
                    e.gc.drawString(
                    		text,
                    		bounds.width / 2 - textSize.x / 2 + textSize.x % 2,
                    		bounds.height / 2 - textSize.y / 2 + yOffset,
                    		true);
                    
                    e.gc.setFont(oldFont);
            	}
            }
        });
        
        addMouseMoveListener(new MouseMoveListener() {
            public void mouseMove(MouseEvent e) {
            	State oldMouse = state;
            	if (!contains(e.x, e.y))
               		state = State.DEFAULT;
            	else if (!hit)
               		state = State.MOUSE_OVER;
            	else
        			state = State.MOUSE_DOWN;

            	if (oldMouse != state)
            		redraw();
            }
        });
        
        addMouseTrackListener(new MouseTrackAdapter() {
            public void mouseEnter(MouseEvent e) {
                if (contains(e.x, e.y)) {
	                state = State.MOUSE_OVER;
	                redraw();
                }
            }

            public void mouseExit(MouseEvent e) {
            	if (state != State.DEFAULT) {
                    state = State.DEFAULT;
                    redraw();
            	}
            }
        });
        
        addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
                if (contains(e.x, e.y)) {
                    hit = true;
                    state = State.MOUSE_DOWN;
                    redraw();
                }
            }
            public void mouseUp(MouseEvent e) {
            	boolean oldHit = hit;
                hit = false;
                
                if (contains(e.x, e.y))
                	 state = State.MOUSE_OVER;
                else
                	 state = State.DEFAULT;
                
                redraw();
                if (oldHit && (state == State.MOUSE_OVER))
                    notifyListeners(SWT.Selection, null);
            }
        });
        
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == '\r' || e.character == ' ') {
                    Event event = new Event();
                    notifyListeners(SWT.Selection, event);
                }
            }
        });
    }

    // properties
	public String getText(String text) {
		return this.text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public void setImageUp(Image buttonUp) {
		images[0] = buttonUp;
	}
	public void setImageOver(Image buttonOver) {
		images[1] = buttonOver;
	}
	public void setImageDown(Image buttonDown) {
		images[2] = buttonDown;
	}
	
	// methods
	private boolean contains(int x, int y) {
        if (!triggers.isEmpty()) {
            for (ITriggerArea trigger : triggers)
            	if (trigger.contains(x, y))
            		return true;
            
            return false;
        }
        else {
        	Rectangle bounds = getBounds();
        	return (x >= 0) && (y >= 0) && (x < bounds.width) && (y < bounds.height);
        }
	}
	// public
	public void addRectangularTriggerArea(int x, int y, int width, int height) {
		triggers.add(new RectangularTriggerArea(x, y, width, height));
	}
	public void addCircularTriggerArea(int r) {
		int width, height;
		
		Rectangle bounds = this.getBounds();
		if (!bounds.isEmpty()) {
			width = bounds.width;
			height = bounds.height;
		}
		else {
			Point size = computeSize(SWT.DEFAULT, SWT.DEFAULT);
			width = size.x;
			height = size.y;
		}
		triggers.add(new CircularTriggerArea(width / 2, height / 2, r));
	}
	public void addCircularTriggerArea(int x, int y, int r) {
		triggers.add(new CircularTriggerArea(x, y, r));
	}
	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		if (images[0] == null)
			return super.computeSize(wHint, hHint, changed);
		
		ImageData imageData = images[0].getImageData();
		return new Point(imageData.width, imageData.height);
	}

	// helpers
	private static interface ITriggerArea {
		// methods
		public boolean contains(int x, int y);
	}
	private static class RectangularTriggerArea implements ITriggerArea {
		// fields
		private Rectangle area;

		// construction
		public RectangularTriggerArea(int x, int y, int width, int height) {
			area = new Rectangle(x, y, width, height);
		}
		
		// methods
		@Override
		public boolean contains(int x, int y) {
			return area.contains(x, y);
		}
	}
	private static class CircularTriggerArea implements ITriggerArea {
		// fields
		private int x;
		private int y;
		private int rSquare;

		// construction
		public CircularTriggerArea(int x, int y, int r) {
			this.x = x;
			this.y = y;
			this.rSquare = r * r;
		}
		
		// methods
		@Override
		public boolean contains(int x, int y) {
			int dx = x - this.x;
			int dy = y - this.y;
			return (dx * dx + dy * dy) < rSquare;
		}
	}

	// state
	private static enum State {
		DEFAULT(0),
		MOUSE_OVER(1),
		MOUSE_DOWN(2);
		
		// fields
		private final int index;
		
		// construction
		private State(int index) {
			this.index = index;
		}
	}
}
