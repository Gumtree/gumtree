package au.gov.ansto.bragg.koala.ui.vlcj;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.nbi.ui.internal.InternalImage;
import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class VlcjViewer extends Composite {

	private static final String NATIVE_LIBRARY_SEARCH_PATH =  "gumtree.koala.vlcpath";
	private static final String CAM1_URL = "gumtree.koala.cam1url";
	private static final String CAM2_URL = "gumtree.koala.cam2url";
//	private static final String CAM_RATIO = "gumtree.koala.camRatio";
	private static final String CAM_SIZE = "gumtree.koala.camSize";
	private static final String LOGO_SIZE = "gumtree.koala.logoSize";
	private static final String BEAM_CENTRE = "gumtree.koala.beamCentre";
	
	private String vlcPath;
	private String cam1Url;
	private String cam2Url;
//	private String camRatio;
	private int logoSize;
	private int camWidth;
	private int camHeight;
	private Point beamCentre;
	private EmbeddedMediaPlayer mediaPlayer1;
	private EmbeddedMediaPlayer mediaPlayer2;
	private SwtMediaPanel mediaPanel1;
	private SwtMediaPanel mediaPanel2;
	private File logoFile;
	private Button markerButton;
	private Button reloadButton;
	private Button alignButton;
	private boolean isAddingMarker;
	private boolean isPlayer1Ready;
	private boolean isPlayer2Ready;
			
	public VlcjViewer(Composite parent, int style) {
		super(parent, style);
		Native.setProtected(true);
		vlcPath = System.getProperty(NATIVE_LIBRARY_SEARCH_PATH);
		cam1Url = System.getProperty(CAM1_URL);
		cam2Url = System.getProperty(CAM2_URL);
		logoSize = Integer.valueOf(System.getProperty(LOGO_SIZE));
		try {
			logoFile = new File(FileLocator.toFileURL(Activator.getDefault().getBundle().getEntry(
					"/icons/plus64.png")).getFile());
		} catch (IOException e1) {
		}
		String bc = System.getProperty(BEAM_CENTRE);
		String[] bcPair = bc.split(",");
		beamCentre = new Point(Integer.valueOf(bcPair[0]), Integer.valueOf(bcPair[1]));

		String camSize = System.getProperty(CAM_SIZE);
		String[] pair = camSize.split("x");
		camWidth = Integer.valueOf(pair[0]);
		camHeight = Integer.valueOf(pair[1]);
		
		GridLayoutFactory.fillDefaults().applyTo(this);
		Composite videoControlComposite = new Composite(this, SWT.BORDER);
		GridLayoutFactory.fillDefaults().spacing(1, 1).numColumns(3).applyTo(videoControlComposite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(videoControlComposite);
		createControls(videoControlComposite);
		
		Composite videoComposite = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).spacing(1, 1).applyTo(videoComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(videoComposite);
		
		Composite videoSurfaceComposite1 = new Composite(
				videoComposite, SWT.EMBEDDED | SWT.NO_BACKGROUND);
//		videoSurfaceComposite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		GridLayoutFactory.fillDefaults().margins(1, 1).applyTo(videoSurfaceComposite1);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(videoSurfaceComposite1);

		Composite videoSurfaceComposite2 = new Composite(
				videoComposite, SWT.EMBEDDED | SWT.NO_BACKGROUND);
//		videoSurfaceComposite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		GridLayoutFactory.fillDefaults().margins(1, 1).applyTo(videoSurfaceComposite2);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(videoSurfaceComposite2);

		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcPath);
		
		mediaPanel1 = new SwtMediaPanel(videoSurfaceComposite1, camWidth, camHeight, logoFile, 
				logoSize, beamCentre);
		mediaPlayer1 = mediaPanel1.getMediaPlayer();
		
        mediaPlayer1.media().start(cam1Url);
        mediaPlayer1.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
        	@Override
        	public void error(MediaPlayer mediaPlayer) {
        		super.error(mediaPlayer);
        		System.err.println("Player 1 error caught");
        		mediaPlayer.release();
        	}
        	
        	@Override
        	public void videoOutput(MediaPlayer mediaPlayer, int newCount) {
        		// TODO Auto-generated method stub
        		super.videoOutput(mediaPlayer, newCount);
        		isPlayer1Ready = true;
        		if (isPlayer1Ready && isPlayer2Ready) {
        			syncSetText(reloadButton, "Reload Video");
        			syncSetImage(reloadButton, InternalImage.REFRESH_16.getImage());
        		}
        		mediaPanel1.showCentre();
        	}
        });
        mediaPlayer1.input().enableMouseInputHandling(false);
        mediaPlayer1.input().enableKeyInputHandling(false);
        mediaPanel1.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(java.awt.event.MouseEvent e) {
//        		System.err.println(String.format("panel size %d, %d", mediaPanel1.getWidth(), mediaPanel1.getHeigh()));
//        		Dimension dim = mediaPanel1.getMediaPlayer().video().videoDimension();
//        		System.err.println(String.format("video size %d, %d", dim.width, dim.height));
//        		System.err.println(String.format("aspect ratio %s", mediaPanel1.getMediaPlayer().video().aspectRatio()));
//        		System.err.println(String.format("clicked %d, %d", e.getX(), e.getY()));
        		
        		super.mouseClicked(e);
        		if (isAddingMarker) {
        			mediaPanel1.fixLogo(e.getPoint());
        			checkMarkers();
        		}
        	}
        	
        	@Override
        	public void mouseExited(java.awt.event.MouseEvent e) {
        		super.mouseExited(e);
        		if (isAddingMarker) {
        			mediaPanel1.hideLogo();
        		}
        	}

        });

        mediaPanel1.addMouseMotionListener(new MouseMotionAdapter() {
        	@Override
        	public void mouseMoved(java.awt.event.MouseEvent e) {
        		super.mouseMoved(e);
        		mediaPanel1.drawLogo(e.getPoint());
        	}
		});
        
        mediaPanel2 = new SwtMediaPanel(videoSurfaceComposite2, camWidth, camHeight, logoFile, 
        		logoSize, beamCentre);
        mediaPlayer2 = mediaPanel2.getMediaPlayer();
        
        mediaPlayer2.media().start(cam2Url);
        mediaPlayer2.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
        	@Override
        	public void error(MediaPlayer mediaPlayer) {
        		super.error(mediaPlayer);
        		System.err.println("Player 2 error caught");
        		mediaPlayer.release();
        	}
        	
        	@Override
        	public void videoOutput(MediaPlayer mediaPlayer, int newCount) {
        		super.videoOutput(mediaPlayer, newCount);
        		isPlayer2Ready = true;
        		if (isPlayer1Ready && isPlayer2Ready) {
        			syncSetText(reloadButton, "Reload Video");
        			syncSetImage(reloadButton, InternalImage.REFRESH_16.getImage());
        		}
        		mediaPanel2.showCentre();
        	}
        });
        mediaPlayer2.input().enableMouseInputHandling(false);
        mediaPlayer2.input().enableKeyInputHandling(false);
        mediaPanel2.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(java.awt.event.MouseEvent e) {
        		super.mouseClicked(e);
        		if (isAddingMarker) {
        			mediaPanel2.fixLogo(e.getPoint());
        			checkMarkers();
        		}
        	}

        	@Override
        	public void mouseExited(java.awt.event.MouseEvent e) {
        		super.mouseExited(e);
        		mediaPanel2.hideLogo();
        	}

		});

        mediaPanel2.addMouseMotionListener(new MouseMotionAdapter() {
        	@Override
        	public void mouseMoved(java.awt.event.MouseEvent e) {
        		super.mouseMoved(e);
        		if (isAddingMarker) {
        			mediaPanel2.drawLogo(e.getPoint());
        		}
        	}
		});

	}

	private void checkMarkers() {
		if (mediaPanel1.getLogoCoordinate() == null || mediaPanel2.getLogoCoordinate() == null) {
			syncSetText(markerButton, "adding ...");
		} else {
			syncSetText(markerButton, "Reset Markers");
			syncSetEnabled(alignButton, true);
		}
	}
	
	private void syncSetText(final Button button, String text) {
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				button.setText(text);
			}
		});
	}

	private void syncSetImage(final Button button, Image image) {
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				button.setImage(image);
			}
		});
	}

	private void syncSetEnabled(final Button button, boolean isEnabled) {
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				button.setEnabled(isEnabled);
			}
		});
	}

	private void createControls(Composite parent) {
		markerButton = new Button(parent, SWT.TOGGLE);
		markerButton.setText("Add Markers");
		markerButton.setImage(InternalImage.ADD_ITEM.getImage());
		markerButton.setSelection(false);
		GridDataFactory.fillDefaults().grab(true, false).minSize(0, 32).hint(100, 32).applyTo(markerButton);
		markerButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
//				if (markerButton.getSelection()) {
//					markerButton.setText("Pause");
//					markerButton.setImage(InternalImage.PAUSE_16.getImage());
//					mediaPlayer1.controls().play();
//					mediaPlayer2.controls().play();
//				} else {
//					markerButton.setText("Play");
//					markerButton.setImage(InternalImage.PLAY_16.getImage());
//					mediaPlayer1.controls().pause();
//					mediaPlayer2.controls().pause();
//				}
				isAddingMarker = markerButton.getSelection();
				if (isAddingMarker) {
					mediaPanel1.unfixLogo();
					mediaPanel2.unfixLogo();
					checkMarkers();
				} else {
					mediaPanel1.resetLogoCoordinate();
					mediaPanel2.resetLogoCoordinate();
					markerButton.setText("Add Markers");
					alignButton.setEnabled(false);
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		alignButton = new Button(parent, SWT.PUSH);
		alignButton.setEnabled(false);
		alignButton.setText("Align Sample");
		alignButton.setImage(InternalImage.PLAY_16.getImage());
		GridDataFactory.fillDefaults().grab(true, false).minSize(0, 32).hint(100, 32).applyTo(alignButton);
		alignButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				alignButton.setEnabled(false);
				mediaPanel1.resetLogoCoordinate();
				mediaPanel2.resetLogoCoordinate();
				markerButton.setText("Add Markers");
				markerButton.setSelection(false);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		reloadButton = new Button(parent, SWT.PUSH);
		reloadButton.setText("Reload Video");
		reloadButton.setImage(InternalImage.BUSY_STATUS_16.getImage());
		GridDataFactory.fillDefaults().grab(true, false).minSize(0, 32).hint(100, 32).applyTo(reloadButton);
		reloadButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				reloadButton.setImage(InternalImage.BUSY_STATUS_16.getImage());
				reloadButton.setText("Loading ...");
				mediaPlayer1.controls().stop();
				mediaPlayer2.controls().stop();
				isPlayer1Ready = false;
				isPlayer2Ready = false;
				mediaPlayer1.controls().start();
				mediaPlayer2.controls().start();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
	}

	@Override
	public void dispose() {
		super.dispose();
		mediaPlayer1.release();
		mediaPlayer2.release();
	}
}
