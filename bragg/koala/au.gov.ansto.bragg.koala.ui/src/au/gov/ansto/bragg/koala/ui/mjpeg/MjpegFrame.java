package au.gov.ansto.bragg.koala.ui.mjpeg;

import java.io.IOException;
import java.net.URL;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class MjpegFrame extends Composite {

	private static final String CAM1_URL = "gumtree.koala.mjpeg1Url";
	private static final String CAM2_URL = "gumtree.koala.mjpeg2Url";
//	private static final String CAM_SIZE = "gumtree.koala.camSize";
	private static final String MARKER_SIZE = "gumtree.koala.markerSize";

	private MjpegRunner runner1;
	private MjpegRunner runner2;
	private MjpegComposite mjpeg1;
	private MjpegComposite mjpeg2;
	private String cam1Url;
	private String cam2Url;
	private int markerSize;
	
	public MjpegFrame(Composite parent, int style) {
		super(parent, style);
		cam1Url = System.getProperty(CAM1_URL);
		cam2Url = System.getProperty(CAM2_URL);
		markerSize = Integer.valueOf(System.getProperty(MARKER_SIZE));

		GridLayoutFactory.fillDefaults().margins(0, 0).spacing(0, 0).applyTo(this);
		
		Composite controlComposite = new Composite(this, SWT.NONE);
//		controlComposite.setLayout(new FillLayout());
		GridLayoutFactory.fillDefaults().numColumns(3).margins(1, 1).spacing(0, 0).applyTo(controlComposite);
		GridDataFactory.fillDefaults().applyTo(controlComposite);
		
		Button startButton = new Button(controlComposite, SWT.PUSH);
		startButton.setText("Start Video");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(startButton);
		startButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				startRunner();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		Button stopButton = new Button(controlComposite, SWT.PUSH);
		stopButton.setText("Stop Video");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(stopButton);
		stopButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				stopRunner();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		final Button pauseButton = new Button(controlComposite, SWT.TOGGLE);
		pauseButton.setText("Pause Video");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(pauseButton);
		pauseButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean isPaused = pauseButton.getSelection();
				setRunnerPaused(isPaused);
				if (isPaused) {
					pauseButton.setText("Unpause Video");
				} else {
					pauseButton.setText("Pause Video");
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		Composite imageComposite = new Composite(this, SWT.BORDER);
		imageComposite.setLayout(new FillLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(imageComposite);
		
		mjpeg1 = new MjpegComposite(imageComposite, SWT.BORDER);
		mjpeg2 = new MjpegComposite(imageComposite, SWT.BORDER);
//		GridDataFactory.fillDefaults().grab(true, true).align(SWT.CENTER, SWT.CENTER).applyTo(mjpeg);
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				mjpeg1.getPanel().dispose();
				mjpeg2.getPanel().dispose();
				stopRunner();
			}
		});
	}

	public void startRunner() {
		try {
			runner1 = new MjpegRunner(mjpeg1.getPanel(), 
					new URL(cam1Url));
			new Thread(runner1).start();
			runner2 = new MjpegRunner(mjpeg2.getPanel(), 
					new URL(cam2Url));
			new Thread(runner2).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
		
	}
	
	public void stopRunner() {
		try {
			if (runner1 != null) {
				runner1.stop();
			}
			if (runner2 != null) {
				runner2.stop();
			}
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	public void setRunnerPaused(boolean isPaused) {
		try {
			if (runner1 != null) {
				runner1.setPaused(isPaused);
			}
			if (runner2 != null) {
				runner2.setPaused(isPaused);
			}
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	public static void main(String[] args) {
		System.setProperty(CAM1_URL, "http://192.168.0.9:2323/mimg");
		System.setProperty(CAM2_URL, "http://192.168.0.10:2323/mimg");
		System.setProperty(MARKER_SIZE, "32");
	    final Display display = new Display();
	    final Shell shell = new Shell(display);
	    shell.setLayout(new FillLayout());

		MjpegFrame viewer = new MjpegFrame(shell, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer);

		shell.open();

//		viewer.startRunner();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
