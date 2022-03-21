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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.internal.KoalaImage;
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
	private Thread controlThread1;
	private Thread controlThread2;
	private boolean isInitialised1;
	private boolean isInitialised2;
			
	public VlcjViewer(Composite parent, int style) {
		super(parent, style);
		Native.setProtected(true);
		vlcPath = System.getProperty(NATIVE_LIBRARY_SEARCH_PATH);
		cam1Url = System.getProperty(CAM1_URL);
		cam2Url = System.getProperty(CAM2_URL);
		logoSize = Integer.valueOf(System.getProperty(LOGO_SIZE));
		try {
			logoFile = new File(FileLocator.toFileURL(Activator.getDefault().getBundle().getEntry(
					"/icons/plus_thin_red.png")).getFile());
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
		GridLayoutFactory.fillDefaults().margins(0, 0).spacing(0, 0).numColumns(3).applyTo(videoControlComposite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(videoControlComposite);
		createControls(videoControlComposite);
		
		Composite videoComposite = new Composite(this, SWT.NONE);
		int columns = 1;
		if ((style & SWT.HORIZONTAL) != 0) {
			columns = 3;
		}
		GridLayoutFactory.fillDefaults().numColumns(columns).spacing(0, 0).applyTo(videoComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(videoComposite);
		
		Composite videoSurfaceComposite1 = new Composite(
				videoComposite, SWT.EMBEDDED | SWT.NO_BACKGROUND);
//		videoSurfaceComposite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		GridLayoutFactory.fillDefaults().margins(1, 1).applyTo(videoSurfaceComposite1);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(videoSurfaceComposite1);

		Composite wrapComposite = new Composite(videoComposite, SWT.EMBEDDED);
		wrapComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		GridLayoutFactory.fillDefaults().margins(0, 0).applyTo(wrapComposite);
		GridDataFactory.fillDefaults().grab(false, true).applyTo(wrapComposite);
		
		Composite axesControlComposite = new Composite(wrapComposite, SWT.EMBEDDED);
//		axesControlComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
		GridLayoutFactory.fillDefaults().margins(8, 8).numColumns(2).applyTo(axesControlComposite);
		GridDataFactory.fillDefaults().grab(false, true).align(SWT.CENTER, SWT.CENTER).applyTo(axesControlComposite);
//		axesControlComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, true, 1, 1));
		
		final Button phiSButton = new Button(axesControlComposite, SWT.PUSH);
//		phiSButton.setImage(KoalaImage.PLAY48.getImage());
		phiSButton.setText("Phi -90\u00b0");
		phiSButton.setFont(Activator.getMiddleFont());
		phiSButton.setCursor(Activator.getHandCursor());
		GridDataFactory.swtDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).span(1, 3).hint(120, 48).applyTo(phiSButton);

		final Button phiNButton = new Button(axesControlComposite, SWT.PUSH);
//		phiNButton.setImage(KoalaImage.PLAY48.getImage());
		phiNButton.setText("Phi +90\u00b0");
		phiNButton.setFont(Activator.getMiddleFont());
		phiNButton.setCursor(Activator.getHandCursor());
		GridDataFactory.swtDefaults().grab(false, false).align(SWT.END, SWT.CENTER).span(1, 3).hint(120, 48).applyTo(phiNButton);


		Group xGroup = new Group(axesControlComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(3).applyTo(xGroup);
		xGroup.setText("Sample X offset");
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).align(SWT.CENTER, SWT.CENTER).applyTo(xGroup);
		
		final Label curLabel = new Label(xGroup, SWT.NONE);
		curLabel.setText("Current");
		curLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(100, 40).applyTo(curLabel);
		
		final Text curText = new Text(xGroup, SWT.BORDER);
		curText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(100, 40).applyTo(curText);
		
		final Button driveButton = new Button(xGroup, SWT.PUSH);
		driveButton.setImage(KoalaImage.PLAY48.getImage());
		driveButton.setText("Drive");
		driveButton.setFont(Activator.getMiddleFont());
		driveButton.setCursor(Activator.getHandCursor());
		GridDataFactory.swtDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).span(1, 3).hint(180, 64).applyTo(driveButton);

		final Label tarLabel = new Label(xGroup, SWT.NONE);
		tarLabel.setText("Target");
		tarLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(100, 40).applyTo(tarLabel);
		
		final Text tarText = new Text(xGroup, SWT.BORDER);
		tarText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(100, 40).applyTo(tarText);
		
		final Slider slider = new Slider(xGroup, SWT.HORIZONTAL);
//	    slider.setBounds(0, 0, 40, 200);
	    slider.setMaximum(100);
	    slider.setMinimum(0);
	    slider.setIncrement(1);
	    slider.setPageIncrement(5);
	    slider.setThumb(4);
	    slider.setSelection(48);
	    GridDataFactory.fillDefaults().grab(false, false).span(2, 1).applyTo(slider);
	    
		Group yGroup = new Group(axesControlComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(3).applyTo(yGroup);
		yGroup.setText("Sample Y offset");
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).align(SWT.CENTER, SWT.CENTER).applyTo(yGroup);
		
		final Label curYLabel = new Label(yGroup, SWT.NONE);
		curYLabel.setText("Current");
		curYLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(100, 40).applyTo(curYLabel);
		
		final Text curYText = new Text(yGroup, SWT.BORDER);
		curYText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(100, 40).applyTo(curYText);
		
		final Button driveYButton = new Button(yGroup, SWT.PUSH);
		driveYButton.setImage(KoalaImage.PLAY48.getImage());
		driveYButton.setText("Drive");
		driveYButton.setFont(Activator.getMiddleFont());
		driveYButton.setCursor(Activator.getHandCursor());
		GridDataFactory.swtDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).span(1, 3).hint(180, 64).applyTo(driveYButton);

		final Label tarYLabel = new Label(yGroup, SWT.NONE);
		tarYLabel.setText("Target");
		tarYLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(100, 40).applyTo(tarYLabel);
		
		final Text tarYText = new Text(yGroup, SWT.BORDER);
		tarYText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(100, 40).applyTo(tarYText);
		
		final Slider sliderY = new Slider(yGroup, SWT.HORIZONTAL);
//	    slider.setBounds(0, 0, 40, 200);
		sliderY.setMaximum(100);
		sliderY.setMinimum(0);
		sliderY.setIncrement(1);
		sliderY.setPageIncrement(5);
		sliderY.setThumb(4);
		sliderY.setSelection(48);
	    GridDataFactory.fillDefaults().grab(false, false).span(2, 1).applyTo(sliderY);
	    
		final Button phiEButton = new Button(axesControlComposite, SWT.PUSH);
//		phiEButton.setImage(KoalaImage.PLAY48.getImage());
		phiEButton.setText("Phi 0\u00b0");
		phiEButton.setFont(Activator.getMiddleFont());
		phiEButton.setCursor(Activator.getHandCursor());
		phiEButton.setForeground(Activator.getLightColor());
		GridDataFactory.swtDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).span(1, 3).hint(120, 48).applyTo(phiEButton);

		final Button phiWButton = new Button(axesControlComposite, SWT.PUSH);
//		phiNButton.setImage(KoalaImage.PLAY48.getImage());
		phiWButton.setText("Phi -180\u00b0");
		phiWButton.setFont(Activator.getMiddleFont());
		phiWButton.setCursor(Activator.getHandCursor());
		GridDataFactory.swtDefaults().grab(false, false).align(SWT.END, SWT.CENTER).span(1, 3).hint(120, 48).applyTo(phiWButton);


		
		Composite videoSurfaceComposite2 = new Composite(
				videoComposite, SWT.EMBEDDED | SWT.NO_BACKGROUND);
//		videoSurfaceComposite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		GridLayoutFactory.fillDefaults().margins(1, 1).applyTo(videoSurfaceComposite2);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(videoSurfaceComposite2);

		NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcPath);
		
		mediaPanel1 = new SwtMediaPanel(videoSurfaceComposite1, camWidth, camHeight, logoFile, 
				logoSize, beamCentre);
		mediaPlayer1 = mediaPanel1.getMediaPlayer();
		
        mediaPlayer1.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
        	@Override
        	public void error(MediaPlayer mediaPlayer) {
        		super.error(mediaPlayer);
        		System.err.println("Player 1 error caught");
        		isPlayer1Ready = false;
        		mediaPlayer.release();
        	}
        	
        	@Override
        	public void videoOutput(MediaPlayer mediaPlayer, int newCount) {
        		// TODO Auto-generated method stub
        		super.videoOutput(mediaPlayer, newCount);
        		isPlayer1Ready = true;
//        		if (isPlayer1Ready && isPlayer2Ready) {
        			syncSetText(reloadButton, "Pause Video");
        			syncSetImage(reloadButton, InternalImage.PAUSE_16.getImage());
//        		}
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
        
        mediaPlayer2.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
        	@Override
        	public void error(MediaPlayer mediaPlayer) {
        		super.error(mediaPlayer);
        		if (isPlayer2Ready) {
        			mediaPlayer.controls().stop();
        			isPlayer2Ready = false;
        		}
        	}
        	
        	@Override
        	public void videoOutput(MediaPlayer mediaPlayer, int newCount) {
        		super.videoOutput(mediaPlayer, newCount);
        		isPlayer2Ready = true;
//        		if (isPlayer1Ready && isPlayer2Ready) {
        			syncSetText(reloadButton, "Pause Video");
        			syncSetImage(reloadButton, InternalImage.PAUSE_16.getImage());
//        		}
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

//		controlThread1 = new Thread() {
//			public void run() {
//				try {
//					isInitialised1 = true;
//					mediaPlayer1.media().start(cam1Url, "network-caching=0");
////						mediaPlayer2.media().start(cam2Url);
//					syncSetText(reloadButton, "Pause Video");
//					syncSetImage(reloadButton, InternalImage.STOP_16.getImage());
//				} catch (Exception e) {
//					System.err.println("failed to start players");
//				}
//			};
//		};
//		controlThread1.start();

	}

	private void checkMarkers() {
		if (mediaPanel1.getLogoCoordinate() == null || mediaPanel2.getLogoCoordinate() == null) {
			syncSetText(markerButton, "adding ...");
		} else {
			syncSetText(markerButton, "Reset Markers");
			syncSetEnabled(alignButton, true);
		}
	}
	
	private void syncSetText(final Button button, final String text) {
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				button.setText(text);
			}
		});
	}

	private void syncSetImage(final Button button, final Image image) {
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				button.setImage(image);
			}
		});
	}

	private void syncSetEnabled(final Button button, final boolean isEnabled) {
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
		reloadButton.setText("Start Video");
		reloadButton.setImage(InternalImage.PLAY_16.getImage());
		GridDataFactory.fillDefaults().grab(true, false).minSize(0, 32).hint(100, 32).applyTo(reloadButton);
		reloadButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				reloadButton.setImage(InternalImage.BUSY_STATUS_16.getImage());
				reloadButton.setText("Loading ...");
				if (isInitialised1) {
					if (isPlayer1Ready) {
						mediaPlayer1.controls().setPause(true);
						reloadButton.setImage(InternalImage.PLAY_16.getImage());
						reloadButton.setText("Play Video");
						isPlayer1Ready = false;
					} else {
						mediaPlayer1.controls().setPause(false);
						reloadButton.setImage(InternalImage.PAUSE_16.getImage());
						reloadButton.setText("Pause Video");
						isPlayer1Ready = true;
					}
				} else {
					controlThread1 = new Thread() {
						public void run() {
							try {
								isInitialised1 = true;
								mediaPlayer1.media().start(cam1Url, "network-caching=0", "live-caching=0");
//									mediaPlayer2.media().start(cam2Url);
								syncSetText(reloadButton, "Pause Video");
								syncSetImage(reloadButton, InternalImage.STOP_16.getImage());
							} catch (Exception e) {
								System.err.println("failed to start players");
							}
						};
					};
					controlThread1.start();
				}
				
//				if (isPlayer1Ready && isPlayer2Ready) {
//					mediaPlayer1.controls().setPause(true);
////					mediaPlayer2.controls().stop();
//					isPlayer1Ready = false;
//					isPlayer2Ready = false;
//					mediaPlayer1.controls().start();
////					mediaPlayer2.controls().start();					
//				} else {
//					if (isPlayer1Ready) {
//						mediaPlayer1.controls().stop();
//						isPlayer1Ready = false;
//						mediaPlayer1.controls().start();
//					} else {
//						mediaPlayer1.release();
//						mediaPlayer1.media().start(cam1Url);
//					}
////					if (isPlayer2Ready) {
////						mediaPlayer2.controls().stop();
////						isPlayer2Ready = false;
////						mediaPlayer2.controls().start();
////					} else {
////						mediaPlayer2.release();
////						mediaPlayer2.media().start(cam2Url);
////					}
//				}
				
				
//				if (controlThread1 != null && controlThread1.isAlive()) {
//					controlThread1.interrupt();
//				}
//				if (controlThread2 != null && controlThread1.isAlive()) {
//					controlThread2.interrupt();
//				}
//				controlThread1 = new Thread() {
//					public void run() {
//						if (isInitialised1) {
//							if (isPlayer1Ready) {
//								try {
//									mediaPlayer1.controls().stop();
//									isPlayer1Ready = false;
//									if (!isPlayer1Ready && !isPlayer2Ready) {
//										syncSetText(reloadButton, "Start Video");
//										syncSetImage(reloadButton, InternalImage.PLAY_16.getImage());
//									}
//								} catch (Exception ex) {
//									System.err.println("failed to stop player");
//								}
//							} else {
//								try {
//									mediaPlayer1.controls().play();
//								} catch (Exception ex) {
//									System.err.println("failed to start player");
//								}
//							}
//							
//						} else {
//							try {
//								isInitialised1 = true;
//								mediaPlayer1.media().start(cam1Url, "network-caching=0");
//							} catch (Exception e) {
//								System.err.println("failed to start players");
//							}
//						}
//					};
//				};
//				controlThread2 = new Thread() {
//					public void run() {
//						if (isInitialised2) {
//							if (isPlayer2Ready) {
//								try {
//									mediaPlayer2.controls().stop();
//									isPlayer2Ready = false;
//									if (!isPlayer1Ready && !isPlayer2Ready) {
//										syncSetText(reloadButton, "Start Video");
//										syncSetImage(reloadButton, InternalImage.PLAY_16.getImage());
//									}
//								} catch (Exception ex) {
//									System.err.println("failed to stop player");
//								}
//							} else {
//								try {
//									mediaPlayer2.controls().play();
//								} catch (Exception ex) {
//									System.err.println("failed to start player");
//								}
//							}
//							
//						} else {
//							try {
//								isInitialised2 = true;
//								mediaPlayer2.media().start(cam2Url, "network-caching=0");
//							} catch (Exception e) {
//								System.err.println("failed to start players");
//							}
//						}
//					};
//				};
//				controlThread1.start();
////				controlThread2.start();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
	}

	@Override
	public void dispose() {
		super.dispose();
		Thread dispose1 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				mediaPlayer1.release();
			}
		});
		dispose1.start();
		Thread dispose2 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				mediaPlayer2.release();
			}
		});
		dispose2.start();
		System.err.println("Vlcj disposed successfully");
	}
}
