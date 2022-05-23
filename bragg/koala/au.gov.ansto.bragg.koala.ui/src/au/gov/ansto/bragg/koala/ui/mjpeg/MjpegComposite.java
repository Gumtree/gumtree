/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.mjpeg;

import java.awt.Button;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * @author nxi
 *
 */
public class MjpegComposite extends Composite {

	private Frame frame;
	private MjpegPanel mPanel;
	
	/**
	 * @param parent
	 * @param style
	 */
	public MjpegComposite(Composite parent, int style) {
		super(parent, SWT.EMBEDDED | style);
//		GridLayoutFactory.fillDefaults().margins(0, 0).spacing(0, 0).applyTo(this);
		FillLayout layout = new FillLayout(SWT.FILL);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.spacing = 0;
		setLayout(layout);
		frame = SWT_AWT.new_Frame(this);
		frame.setBackground(Color.BLACK);
		mPanel = new MjpegPanel(frame);
		mPanel.setBackground(Color.BLACK);
		frame.add(mPanel);
	}

	public MjpegPanel getPanel() {
		return mPanel;
	}
	
	public Frame getFrame() {
		return frame;
	}
}
