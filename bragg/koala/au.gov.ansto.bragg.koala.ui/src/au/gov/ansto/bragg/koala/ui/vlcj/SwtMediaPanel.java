package au.gov.ansto.bragg.koala.ui.vlcj;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JRootPane;

import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import uk.co.caprica.vlcj.player.base.Logo;
import uk.co.caprica.vlcj.player.base.LogoPosition;
import uk.co.caprica.vlcj.player.base.Marquee;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class SwtMediaPanel {

	private Frame frame;
	private JRootPane rootPane;
	private EmbeddedMediaPlayerComponent mediaPlayerComponent;
	private EmbeddedMediaPlayer mediaPlayer;
	private List<MouseListener> mouseListenerList;
	private List<MouseMotionListener> mouseMotionListenerList;
	private int camWidth;
	private int camHeight;
	private int logoSize;
	private int traceX = -1;
	private int traceY = -1;
	private boolean isLogoFixed = true;
//	private File logoFile;
	private Logo logo;
	private Marquee centre;
	private Point logoCoordinate;
	private String centreText;
	
	public SwtMediaPanel(Composite parent, int cameraWidth, int cameraHeight, 
			File logoFile, int logoSize, Point centrePoint, String centreText) {
		
		camWidth = cameraWidth;
		camHeight = cameraHeight;
		this.logoSize = logoSize;
//		this.logoFile = logoFile;
		logo = Logo.logo();
		logo.opacity(255);
		logo.file(logoFile);
		this.centreText = centreText;
		centre = Marquee.marquee();
		centre.opacity(150);
		centre.text(centreText);
		centre.size(128);
		centre.colour(Color.BLUE);
		centre.location(centrePoint.x - 14, centrePoint.y - 14);
		
		
		mouseListenerList = new ArrayList<MouseListener>();
		mouseMotionListenerList = new ArrayList<MouseMotionListener>();
		
		frame = SWT_AWT.new_Frame(parent);
		
		rootPane = new JRootPane();
//		rootPane.setBackground(Color.white);
		frame.add(rootPane);
		
//		mediaPlayerComponent = new CallbackMediaPlayerComponent();
		mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        rootPane.setContentPane(mediaPlayerComponent);
        
        mediaPlayer = mediaPlayerComponent.mediaPlayer();

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
	
	public void displayBeamCentre() {
		
	}
	
	public void dispose() {
		if (mediaPlayerComponent != null) {
			for (MouseListener listener : mouseListenerList) {
				mediaPlayerComponent.videoSurfaceComponent().removeMouseListener(listener);
			}
			for (MouseMotionListener listener : mouseMotionListenerList) {
				mediaPlayerComponent.videoSurfaceComponent().removeMouseMotionListener(listener);
			}
			mediaPlayerComponent.release();
			mediaPlayerComponent = null;
		}
		if (frame != null) {
			frame.remove(rootPane);
			frame.dispose();
			frame = null;
		}
	}
	
	public void addMouseListener(MouseListener mouseListener) {
		if (mediaPlayerComponent != null) {
			mediaPlayerComponent.videoSurfaceComponent().addMouseListener(mouseListener);
			mouseListenerList.add(mouseListener);
		}
	}

	public void addMouseMotionListener(MouseMotionListener motionListener) {
		if (mediaPlayerComponent != null) {
			mediaPlayerComponent.videoSurfaceComponent().addMouseMotionListener(motionListener);
			mouseMotionListenerList.add(motionListener);
		}
	}
	
	public int getWidth() {
		return rootPane.getWidth();
	}

	public int getHeigh() {
		return rootPane.getHeight();
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
	}
	
	public void fixLogo(Point point) {
		if (!isLogoFixed) {
			logoCoordinate = drawLogo(point);
			System.err.println(String.format("fix logo at %d, %d", logo.getX(), logo.getY()));
			isLogoFixed = true;
		}
	}
	
	public void unfixLogo() {
		logoCoordinate = null;
		isLogoFixed = false;
        mediaPlayer.logo().enable(true);
	}
	
    public Point drawLogo(Point point) {
    	if (!isLogoFixed) {
    		Point newPoint = getLogoPosition(point);
    		int x = newPoint.x - logoSize / 2;
    		int y = newPoint.y - logoSize / 2;
    		logo.location(x, y);
    		showLogo();
//    		logo.apply(mediaPlayer);
    		mediaPlayer.logo().set(logo);
    		System.err.println(String.format("draw logo at %d, %d", logo.getX(), logo.getY()));
    		return newPoint;
    	}
    	return null;
    }

    public Point getLogoPosition(Point o) {
    	int windowWidth = getWidth();
    	int windowHeight = getHeigh();
    	float oRatio = 1f * camWidth / camHeight;
    	float nRatio = 1f * windowWidth / windowHeight;
    	int nX;
    	int nY;
    	if (nRatio <= oRatio) {
//    		float newLogoSize = 1f * logoSize * camWidth / windowWidth;
    		nX = (int) ((1. * o.x) * camWidth / windowWidth);
    		float newHeight = windowWidth / oRatio;
    		double gap = (windowHeight - newHeight) / 2.;
    		nY = (int) ((o.y - gap) * camHeight / newHeight);
    	} else {
//    		float newLogoSize = 1f * logoSize * windowHeight / camHeight;
    		nY = Math.round(1f * o.y / windowHeight * camHeight);
    		float newWidth = windowHeight * oRatio;
    		float gap = (windowWidth - newWidth) / 2;
    		nX = Math.round((o.x - gap) / newWidth * camWidth);
    	}
    	return new Point(nX, nY);
    }

	public Point getLogoCoordinate() {
		return logoCoordinate;
	}

	public void setLogoCoordinate(Point logoCoordinate) {
		this.logoCoordinate = logoCoordinate;
		logo.location(logoCoordinate.x, logoCoordinate.y);
		logo.apply(mediaPlayer);
	}

	public void resetLogoCoordinate() {
		logoCoordinate = null;
		hideLogo();
		isLogoFixed = true;
		mediaPlayer.logo().enable(false);
	}
	
	public void hideLogo() {
		if (!isLogoFixed) {
			logo.location(-1, -1);
			logo.opacity(0);
			logo.apply(mediaPlayer);
		}
	}
	
	public void showLogo() {
		logo.opacity(255);
		logo.enable();
	}
	
	public void showCentre() {
		mediaPlayer.marquee().enable(true);
		centre.apply(mediaPlayer);
	}
}
