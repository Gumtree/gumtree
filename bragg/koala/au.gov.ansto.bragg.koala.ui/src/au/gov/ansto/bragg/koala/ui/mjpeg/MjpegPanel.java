package au.gov.ansto.bragg.koala.ui.mjpeg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MjpegPanel extends JPanel implements IMjpegPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = -6873196890440671574L;

	public static final String MARKER_SIZE = "gumtree.koala.markerSize";
	public static final String BEAM_CENTRE = "gumtree.koala.beamCentre";
	public static final String CAM_SIZE = "gumtree.koala.camSize";
	
	private static final Stroke BEAM_CENTRE_STROKE = new BasicStroke(1.8f);
	private static final Stroke MARKER_STROKE = new BasicStroke(1.5f);
	
	private final static int clickInterval = (Integer)Toolkit.getDefaultToolkit().
	        getDesktopProperty("awt.multiClickInterval");

	private BufferedImage image;
	private String failedString;
	private byte[] imageBytes;
	private boolean isAlive = true;
	private boolean isRunning = true;
	private boolean needRefresh = false;
	private Point markerCoordinate;
//	private int traceX = -1;
//	private int traceY = -1;
	private boolean isMarkerFixed = false;
	private boolean hideMarker = true;
	private Integer imageWidth;
	private Integer imageHeight;
	private int markerSize;
	private Point beamCentre;
	private float imageScale = 1f;
	private Dimension screenSize;
	private int relativeX;
	private int relativeY;
	private int relativeWidth;
	private int relativeHeight;
	private int relativeCentreX;
	private int relativeCentreY;
	private String lastTimestamp = "n/a";
	private boolean isFullScreen;
	private GraphicsDevice fullScreenDevice;
	private JFrame fullScreenFrame;
	private Frame parentFrame;
	private Timer timer;
	private List<IMjpegPanelListener> listeners;
	private JPanel emptyPanel;

	public MjpegPanel(Frame parent) {
		super();
		this.parentFrame = parent;
		markerSize = Integer.valueOf(System.getProperty(MARKER_SIZE));
		String bc = System.getProperty(BEAM_CENTRE);
		String[] bcPair = bc.split(",");
		beamCentre = new Point(Integer.valueOf(bcPair[0]), Integer.valueOf(bcPair[1]));
		
		String camSize = System.getProperty(CAM_SIZE);
		String[] pair = camSize.split("x");
		imageWidth = Integer.valueOf(pair[0]);
		imageHeight = Integer.valueOf(pair[1]);
		
		listeners = new ArrayList<IMjpegPanelListener>();
		
		UpdateRunner updateRunner = new UpdateRunner();
		(new Thread(updateRunner)).start();
		MouseEventAdapter mouseListener = new MouseEventAdapter();
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
		
		GraphicsEnvironment graphics =
				GraphicsEnvironment.getLocalGraphicsEnvironment();
		fullScreenDevice = graphics.getDefaultScreenDevice();
		emptyPanel = new JPanel() {
		    @Override
		    protected void paintComponent(Graphics g) {
		        super.paintComponent(g);
		    	Dimension d = getSize();
		    	g.drawLine(0, 0, d.width, d.height);
		    	g.drawLine(0, d.height, d.width, 0);
		    	return;

		    }
		};
		emptyPanel.setBackground(Color.black);
	}
	
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    	Dimension d = getSize();
        if (image == null) {
	        g.drawLine(0, 0, d.width, d.height);
	        g.drawLine(0, d.height, d.width, 0);
	        return;
        } else {
        	Graphics2D g2d = (Graphics2D) g.create();
        	try {
//        		int x = (getWidth() - image.getWidth()) / 2;
//        		int y = (getHeight() - image.getHeight()) / 2;
//        		Integer screenWidth = getWidth();
//        		Integer screenHeight = getHeight();
//        		Integer imageWidth = image.getWidth();
//        		Integer imageHeight = image.getHeight();
//        		if (screenWidth == 0 || screenHeight == 0) {
//        			return;
//        		}
//        		int x, y, width, height, centreX, centreY;
//        		if (screenWidth.floatValue() / screenHeight >= imageWidth.floatValue() / imageHeight) {
//        			imageScale = imageHeight.floatValue() / screenHeight;
//        			width = Float.valueOf(imageWidth / imageScale).intValue();
//        			height = Float.valueOf(imageHeight / imageScale).intValue();
//        			x = (screenWidth - width) / 2;
//        			y = 0;
//        			centreX = x + Float.valueOf(beamCentre.x / imageScale).intValue();
//        			centreY = Float.valueOf(beamCentre.y / imageScale).intValue();
//        		} else {
//        			imageScale = imageWidth.floatValue() / screenWidth;
//        			width = Float.valueOf(imageWidth / imageScale).intValue();
//        			height = Float.valueOf(imageHeight / imageScale).intValue();
//        			x = 0;
//        			y = (screenHeight - height) / 2;
//        			centreX = Float.valueOf(beamCentre.x / imageScale).intValue();
//        			centreY = y + Float.valueOf(beamCentre.y / imageScale).intValue();
//        		}
        		if (!d.equals(screenSize)) {
            		Integer screenWidth = getWidth();
            		Integer screenHeight = getHeight();
        			if (screenWidth.floatValue() / screenHeight >= imageWidth.floatValue() / imageHeight) {
            			imageScale = imageHeight.floatValue() / screenHeight;
            			relativeWidth = Float.valueOf(imageWidth / imageScale).intValue();
            			relativeHeight = Float.valueOf(imageHeight / imageScale).intValue();
            			relativeX = (screenWidth - relativeWidth) / 2;
            			relativeY = 0;
            			relativeCentreX = relativeX + Float.valueOf(beamCentre.x / imageScale).intValue();
            			relativeCentreY = Float.valueOf(beamCentre.y / imageScale).intValue();
            		} else {
            			imageScale = imageWidth.floatValue() / screenWidth;
            			relativeWidth = Float.valueOf(imageWidth / imageScale).intValue();
            			relativeHeight = Float.valueOf(imageHeight / imageScale).intValue();
            			relativeX = 0;
            			relativeY = (screenHeight - relativeHeight) / 2;
            			relativeCentreX = Float.valueOf(beamCentre.x / imageScale).intValue();
            			relativeCentreY = relativeY + Float.valueOf(beamCentre.y / imageScale).intValue();
            		}
        			screenSize = d;
        		}
        		g2d.drawImage(image, relativeX, relativeY, relativeWidth, relativeHeight, this);
        		g2d.setColor(Color.blue);
        		g2d.setStroke(BEAM_CENTRE_STROKE);
        		g2d.drawLine(relativeCentreX - markerSize, relativeCentreY - markerSize, 
        				relativeCentreX + markerSize, relativeCentreY + markerSize);
        		g2d.drawLine(relativeCentreX - markerSize, relativeCentreY + markerSize, 
        				relativeCentreX + markerSize, relativeCentreY - markerSize);
        		if (!hideMarker && markerCoordinate != null) {
        			int cordinateX = relativeX + Float.valueOf(markerCoordinate.x / imageScale).intValue();
        			int cordinateY = relativeY + Float.valueOf(markerCoordinate.y / imageScale).intValue();
            		g2d.setColor(Color.red);
            		g2d.setStroke(MARKER_STROKE);
            		g2d.drawLine(cordinateX - markerSize, cordinateY, 
            				cordinateX + markerSize, cordinateY);
            		g2d.drawLine(cordinateX, cordinateY - markerSize, 
            				cordinateX, cordinateY + markerSize);
        		}
        		
        		g2d.setColor(Color.WHITE);
    			g2d.drawString(lastTimestamp, 10, getHeight()-50);
        	} finally {
        		g2d.dispose();
        	}
        }
    }

	@Override
	public void setFailedString(String s) {
		this.failedString = s;
	}
 
	public void setImageBytes(byte[] imageBytes) {
		this.imageBytes = imageBytes;
		needRefresh = true;
	}
	
	public void dispose() {
		isAlive = false;
	}
	
	@Override
	public void start() {
		isRunning = true;
	}
	
	@Override
	public void stop() {
		isRunning = false;
		image = null;
		imageBytes = null;
		repaint();
	}
	
	public void fixLogo(Point point) {
		if (!isMarkerFixed) {
			markerCoordinate = convertMarker(point);
			System.err.println(String.format("fix logo at %d, %d", markerCoordinate.getX(), markerCoordinate.getY()));
			isMarkerFixed = true;
		}
	}
	
	public void unfixLogo() {
		markerCoordinate = null;
		isMarkerFixed = false;
	}
	
    public Point convertMarker(Point point) {
    	if (!isMarkerFixed) {
    		return null;
    	}
    	return null;
    }

//    public Point getMarkerPosition(Point o) {
//    	return null;
//    }

	public Point getMarkerCoordinate() {
		if (!hideMarker) {
			if (isMarkerFixed) {
				return markerCoordinate;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public void setMarkerCoordinate(Point markerCoordinate) {
		this.markerCoordinate = markerCoordinate;
		repaint();
	}

	public void resetMarkerCoordinate() {
		markerCoordinate = null;
		hideMarker();
		repaint();
	}
	
	public void hideMarker() {
		hideMarker = true;
	}
	
	public void showMarker() {
		hideMarker = false;
	}
	
	public void addPanelListener(IMjpegPanelListener listener) {
		listeners.add(listener);
	}
	
	public void removePanelListener(IMjpegPanelListener listener) {
		listeners.remove(listener);
	}
	
	class UpdateRunner implements Runnable{

		@Override
		public void run() {
			try {
				while (isAlive) {
					if (needRefresh) {
						needRefresh = false;
						lastTimestamp = new Date().toString();
						try {
							if (imageBytes != null) {
								ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
								BufferedImage bi = ImageIO.read(bais);
								if (isRunning) {
									if (bi != null) {
										image = bi;
									}
								} else {
									image = null;
								}
							} 
						} catch (Exception e) {
							System.err.println("failed to convert image");
							Thread.sleep(20);
						}
						repaint();
					}
					Thread.sleep(0, 10);
				}
				if (timer != null) {
					timer.cancel();
					timer = null;
				}
			} catch (Exception e) {
			}
		}
	}
	
	class MouseEventAdapter implements MouseListener, MouseMotionListener {

		@Override
		public void mouseDragged(MouseEvent arg0) {
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			if (!hideMarker && !isMarkerFixed && timer == null) {
				Point p = e.getPoint();
				int x, y;
				if (p.x < relativeX) {
					x = 0;
				} else if (p.x > relativeX + relativeWidth) {
					x = imageWidth;
				} else {
					x = Float.valueOf((p.x - relativeX) * imageScale).intValue();
				}
				if (p.y < relativeY) {
					y = 0;
				} else if (p.y > relativeY + relativeHeight) {
					y = imageHeight;
				} else {
					y = Float.valueOf((p.y - relativeY) * imageScale).intValue();
				}
				markerCoordinate = new Point(x, y);
				repaint();
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() >= 2) {
				if (timer != null) {
					timer.cancel();
					timer = null;
				}
			    toggleFullScreen();
			} else {
				if (!hideMarker && ! isMarkerFixed) {
					timer = new Timer();
					timer.schedule(new TimerTask() {

						@Override
						public void run() {
							if (!hideMarker && ! isMarkerFixed) {
								isMarkerFixed = true;
								repaint();
								for (IMjpegPanelListener listener : listeners) {
									listener.markerSet();
								}
							}
							timer = null;
						}
					}, clickInterval);
				}
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}
		
	}

	public void unfixMarker() {
		isMarkerFixed = false;
		showMarker();
	}

	public void toggleFullScreen() {
		isFullScreen = !isFullScreen;
		if (isFullScreen) {
//			fullScreenFrame.setUndecorated(true);
//			fullScreenFrame.setResizable(false);
//			fullScreenDevice.setFullScreenWindow(fullScreenFrame);
//			fullScreenFrame.addWindowListener(new WindowEventAdapter(this));
//			fullScreenFrame.setState(Frame.NORMAL); // restores minimized windows
//			fullScreenFrame.setAlwaysOnTop(true);
//			fullScreenFrame.setAutoRequestFocus(true);
//			fullScreenFrame.toFront(); // brings to front without needing to setAlwaysOnTop
//			fullScreenFrame.requestFocus();
//			fullScreenFrame.requestFocusInWindow();
			fullScreenFrame = new JFrame(fullScreenDevice.getDefaultConfiguration());
			fullScreenFrame.addWindowListener(new WindowEventAdapter(this));
			fullScreenFrame.add(this);
			fullScreenFrame.setLocation(0, 0);
			fullScreenFrame.setSize(fullScreenDevice.getDisplayMode().getWidth(), 
					fullScreenDevice.getDisplayMode().getHeight());
			fullScreenFrame.setAlwaysOnTop(true);
			fullScreenFrame.setVisible(true);
			parentFrame.remove(this);
//			parentFrame.add(emptyPanel);
			parentFrame.repaint();
		} else {
			fullScreenFrame.remove(this);
			fullScreenFrame.setAlwaysOnTop(false);
			fullScreenFrame.setVisible(false);
			fullScreenFrame.dispose();
			fullScreenFrame = null;
			parentFrame.remove(emptyPanel);
			parentFrame.add(this);
			this.updateUI();
		}
	}
	
	class WindowEventAdapter implements WindowListener {
		
		private MjpegPanel panel;
		
		public WindowEventAdapter(MjpegPanel panel) {
			this.panel = panel;
		}
		
		@Override
		public void windowOpened(WindowEvent e) {
		}
		
		@Override
		public void windowIconified(WindowEvent e) {
		}
		
		@Override
		public void windowDeiconified(WindowEvent e) {
		}
		
		@Override
		public void windowDeactivated(WindowEvent e) {
		}
		
		@Override
		public void windowClosing(WindowEvent e) {
			isFullScreen = !isFullScreen;
			fullScreenFrame.remove(panel);
			fullScreenFrame.setAlwaysOnTop(false);
			fullScreenFrame.setVisible(false);
			fullScreenFrame.dispose();
			fullScreenFrame = null;
			parentFrame.add(panel);
			panel.updateUI();
		}
		
		@Override
		public void windowClosed(WindowEvent e) {
		}
		
		@Override
		public void windowActivated(WindowEvent e) {
		}
	}
}
