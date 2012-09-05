package org.gumtree.ui.widgets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.gumtree.ui.internal.Activator;
import org.gumtree.widgets.IWidget;
import org.gumtree.widgets.swt.util.UIResourceUtils;

public class TimerWidget extends Composite implements IWidget {

	private Label[] timerLabels;

	private Image[] digits;

	private Timer timer;

	public TimerWidget(Composite parent, int style) {
		super(parent, style);
		timerLabels = new Label[8];
		digits = new Image[10];
		createControl();
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				disposeWidget();
			}
		});
	}

	private void createControl() {
		RowLayout rowLayout = new RowLayout();
		rowLayout.spacing = 0;
		rowLayout.wrap = false;
		setLayout(rowLayout);
		for(int i = 0; i < timerLabels.length; i++) {
			timerLabels[i] = new Label(this, SWT.NONE);
		}
		for(int i = 0; i < digits.length; i++) {
			digits[i] = getImage("images/widget/" + i + ".gif");
		}
		clearTimerUI();
	}

	public void clearTimerUI() {
		if(digits == null || digits[0].isDisposed()) {
			return;
		}
		timerLabels[0].setImage(digits[0]);
		timerLabels[1].setImage(digits[0]);
		timerLabels[2].setImage(getImage("images/widget/dot_a.gif"));
		timerLabels[3].setImage(digits[0]);
		timerLabels[4].setImage(digits[0]);
		timerLabels[5].setImage(getImage("images/widget/dot_a.gif"));
		timerLabels[6].setImage(digits[0]);
		timerLabels[7].setImage(digits[0]);
	}

	public void startTimerUI() {
		if(timerLabels == null) {
			return;
		}
		clearTimerUI();
		final long startTime = System.currentTimeMillis();
		if(timer != null) {
			timer.cancel();
		}
		timer = new Timer();
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				long elapsedSecond = (System.currentTimeMillis() - startTime) / 1000;
				final int second = (int)(elapsedSecond % 60);
				long integer = (elapsedSecond - second) / 60;
				final int minute = (int)(integer % 60);
				final int hour = (int)((integer - minute) / 60);
				Display display = Display.getDefault();
				if(hour > 99 || timerLabels[0].isDisposed() || display.isDisposed()) {
					timer.cancel();
					return;
				}
				display.asyncExec(new Runnable() {
					public void run() {
						if(timerLabels[0].isDisposed()) {
							return;
						}
						timerLabels[0].setImage(digits[hour/10]);
						timerLabels[1].setImage(digits[hour%10]);
						timerLabels[3].setImage(digits[minute/10]);
						timerLabels[4].setImage(digits[minute%10]);
						timerLabels[6].setImage(digits[second/10]);
						timerLabels[7].setImage(digits[second%10]);
					}
				});
			}
		};
		timer.scheduleAtFixedRate(timerTask, 0, 500);
	}

	public void stopTimerUI() {
		if(timer != null) {
			timer.cancel();
		}
		timer = null;
	}

	protected void disposeWidget() {
		if(digits != null) {
			for(Image image : digits) {
				image.dispose();
			}
		}
	}
	
	protected Image getImage(String filename) {
		try {
			// Works in non OSGi runtime
			return new Image(Display.getDefault(), new ImageData(new FileInputStream(new File(filename))));
		} catch (FileNotFoundException e) {
		}
		// Works in OSGi runtime
		return UIResourceUtils.imageDescriptorFromPlugin(Activator.PLUGIN_ID,filename).createImage();
	}
	
	
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(1, false));

		TimerWidget timerWidget = new TimerWidget(shell, SWT.NONE);
		timerWidget.startTimerUI();
		
		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
	
}
