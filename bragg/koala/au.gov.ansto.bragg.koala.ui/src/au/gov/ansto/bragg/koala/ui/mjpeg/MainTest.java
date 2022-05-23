package au.gov.ansto.bragg.koala.ui.mjpeg;

import java.io.IOException;
import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class MainTest {

	public static void main(String[] args) {
	    final Display display = new Display();
	    final Shell shell = new Shell(display);
	    shell.setLayout(new FillLayout());
	    
//	    Composite composite = new Composite(shell, SWT.EMBEDDED);
//	    Frame frame = SWT_AWT.new_Frame(composite);
	    
	    MjpegComposite composite = new MjpegComposite(shell, SWT.NONE);
	    MjpegRunner runner = null;
	    
//	    Canvas canvas = new Canvas() {
//	      public void paint(Graphics g) {
//	        Dimension d = getSize();
//	        System.out.println(d);
//	        g.drawLine(0, 0, d.width, d.height);
//	      }
//	    };
//	    JPanel panel = new JPanel();
//	    panel.add(canvas);
//	    frame.add(panel);
////	    frame.add(canvas);
//	    Button button = new Button("button");
//	    panel.add(button);

//	    MjpegPanel mPanel = new MjpegPanel();
//	    frame.add(mPanel);
	    
	    shell.open();
	    
	    try {
	    	runner = new MjpegRunner(composite.getPanel(), 
					new URL("http://192.168.0.10:2323/mimg"));
			new Thread(runner).start();
			final MjpegRunner r = runner;
		    composite.addDisposeListener(new DisposeListener() {
				
				@Override
				public void widgetDisposed(DisposeEvent e) {
					
					if (r != null) {
						r.stop();
						composite.getPanel().dispose();
					}
				}
			});

        } catch (IOException e) {
            e.printStackTrace();
        }
	    
	    while (!shell.isDisposed()) {
	      if (!display.readAndDispatch())
	        display.sleep();
	    }
	    display.dispose();
	  }
}
