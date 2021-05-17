package au.gov.ansto.bragg.koala.ui.vlcj;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import com.sun.jna.platform.WindowUtils;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class SwtCanvasPanel {

	private Frame frame;
	private EmbeddedMediaPlayer mediaPlayer;
	private List<MouseListener> mouseListenerList;
	private List<MouseMotionListener> mouseMotionListenerList;
	private Canvas canvas;
	private Overlay overlay;
	private int traceX = -1;
	private int traceY = -1;
	
	public SwtCanvasPanel(Composite parent) {
		
		mouseListenerList = new ArrayList<MouseListener>();
		mouseMotionListenerList = new ArrayList<MouseMotionListener>();
		
		frame = SWT_AWT.new_Frame(parent);
		frame.setLayout(new BorderLayout());
		canvas = new BaseCanvas();
//		canvas.setOpaque(false);
//		canvas.setBackground(new Color(0.f,0.f,0.f,0.1f));
//        canvas.setBounds(100, 60, 200, 300);
		
        frame.add(canvas, BorderLayout.CENTER);
        
		final MediaPlayerFactory factory = new MediaPlayerFactory();
		mediaPlayer = factory.mediaPlayers().newEmbeddedMediaPlayer();
		mediaPlayer.videoSurface().set(factory.videoSurfaces().newVideoSurface(canvas));
        
        overlay = new Overlay(frame);
        overlay.setAlwaysOnTop(true);
        overlay.setAutoRequestFocus(true);
        overlay.setIgnoreRepaint(true);

        mediaPlayer.overlay().set(overlay);
        mediaPlayer.overlay().enable(true);
        
        parent.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		});
	}
	
	public EmbeddedMediaPlayer getMediaPlayer() {
		return mediaPlayer;
	}
	
	public void dispose() {
		if (mediaPlayer != null) {
			mediaPlayer.release();
			mediaPlayer = null;
		}
		for (MouseListener listener : mouseListenerList) {
			canvas.removeMouseListener(listener);
		}
		for (MouseMotionListener listener : mouseMotionListenerList) {
			canvas.removeMouseMotionListener(listener);
		}
		if (frame != null) {
			frame.remove(canvas);
			frame.dispose();
			frame = null;
		}
	}
	
	public void addMouseListener(MouseListener mouseListener) {
		if (canvas != null) {
			canvas.addMouseListener(mouseListener);
			mouseListenerList.add(mouseListener);
		}
	}

	public void addMouseMotionListener(MouseMotionListener motionListener) {
		if (canvas != null) {
			canvas.addMouseMotionListener(motionListener);
			mouseMotionListenerList.add(motionListener);
		}
	}
	
	public int getWidth() {
		return canvas.getWidth();
	}

	public int getHeigh() {
		return canvas.getHeight();
	}

    public int getTraceX() {
		return traceX;
	}

	public void setTraceX(int traceX) {
		this.traceX = traceX;
	}

	public int getTraceY() {
		return traceY;
	}

	public void setTraceY(int traceY) {
		this.traceY = traceY;
	}

	public void repaint() {
		canvas.repaint();
		overlay.setVisible(false);
		overlay.setVisible(true);
	}
	
	private class BaseCanvas extends Canvas {
		
		@Override
		public void paint(Graphics g) {
			// TODO Auto-generated method stub
			super.paint(g);
			System.err.println("width = " + getWidth());
//			setBounds(frame.getBounds());
//			setBounds(100, 60, 200, 300);
//			setBounds(0, 0, frame.getWidth(), frame.getHeight());
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            System.err.println("repaint canvas");

            g2.setPaint(Color.YELLOW);
    		g2.setStroke(new BasicStroke(0.5f));
        	g2.draw(new Line2D.Float(getTraceX(), 0, getTraceX(), getHeigh()));
        	g2.draw(new Line2D.Float(0, getTraceY(), getWidth(), getTraceY()));
        	
        	g2.drawString("CANVAS", getTraceX(), getTraceY());
		}
	}
	
	private class Overlay extends Window {

        public Overlay(Window owner) {
            super(owner, WindowUtils.getAlphaCompatibleGraphicsConfiguration());

            setBackground(new Color(0, 0, 0, 0)); // This is what you do in JDK7

            setLayout(null);

        }
        
		@Override
		public void paintComponents(Graphics g) {
			// TODO Auto-generated method stub
			super.paintComponents(g);
			System.err.println("overlay paint components");
            Graphics2D g2 = (Graphics2D)g;

    		g2.setPaint(Color.RED);
    		g2.setStroke(new BasicStroke(0.5f));
        	g2.draw(new Line2D.Float(100, 0, 100, getHeigh()));
        	g2.draw(new Line2D.Float(0, 300, getWidth(), 300));

		}
		
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            System.err.println("repaint overlay at " + getTraceX() + ", " + getTraceY());
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    		g2.setPaint(Color.RED);
    		g2.setStroke(new BasicStroke(0.5f));
        	g2.draw(new Line2D.Float(getTraceX(), 0, getTraceX(), getHeigh()));
        	g2.draw(new Line2D.Float(0, getTraceY(), getWidth(), getTraceY()));
        	
        	g2.drawString("hello", getTraceX(), getTraceY());
        }
    }
	

}
