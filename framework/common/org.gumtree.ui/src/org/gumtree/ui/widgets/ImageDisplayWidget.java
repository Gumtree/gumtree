package org.gumtree.ui.widgets;

import java.net.URI;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.gumtree.service.dataaccess.IDataHandler;
import org.gumtree.ui.util.SafeUIRunner;

public class ImageDisplayWidget extends DataDisplayWidget implements IDataHandler<ImageData> {
	
	private String dataURI;
	
	private Image currentImage;
	
	private Composite imageArea;
	
	private int width = -1;
	
	private int height = -1;
	
	private int displayWidth = -1;
	
	private int displayHeight = -1;
	
	public ImageDisplayWidget(Composite parent, int style) {
		super(parent, style);
		imageArea = createImageArea();
	}

	protected void widgetDispose() {
		if (currentImage != null) {
			currentImage.dispose();
			currentImage = null;
		}
		dataURI = null;
		imageArea = null;
	}

	protected Composite createImageArea() {
		GridLayoutFactory.swtDefaults().margins(0, 0).applyTo(this);
		Composite imageArea = getToolkit().createComposite(this);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, true).applyTo(imageArea);
		imageArea.setLayout(new FillLayout());
		return imageArea;
	}
	
	protected void paintImage(Composite parent, Image image, boolean reset) {
		Label imageLabel = getToolkit().createLabel(parent, "");
		imageLabel.setImage(image);
	}
	
	public String getDataURI() {
		return dataURI;
	}
	
	public void setDataURI(String dataURI) {
		this.dataURI = dataURI;
	}
	
	protected void pullData() {
		getDataAccessManager().get(URI.create(getDataURI()),
					ImageData.class, ImageDisplayWidget.this, getParameters());
	}

	public void handleData(URI uri, ImageData data) {
		updateImage(data);
	}

	public void handleError(URI uri, Exception exception) {
		// Layout even if it has failed to obtain image
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (!isDisposed()) {
					getParent().getParent().layout(true, true);
				}
			}
		});
	}
	
	private void updateImage(final ImageData data) {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (isDisposed()) {
					return;
				}
				// Dispose
				for (Control child : imageArea.getChildren()) {
					child.dispose();
				}
				if (currentImage != null ) {
					currentImage.dispose();
				}
				
				// Scaling
				ImageData scaledData = data.scaledTo(
						getWidth() != -1 ? getWidth() : data.width,
						getHeight() != -1 ? getHeight() : data.height);
				
				// Reset detection
				boolean reset = false;
				if (displayWidth != scaledData.width | displayHeight != scaledData.height) {
					reset = true;
				}
				displayWidth = scaledData.width;
				displayHeight = scaledData.height;
				
				currentImage = new Image(Display.getDefault(), scaledData);
				// Draw
				paintImage(imageArea, currentImage, reset);
				// Layout (optimised so that it only update high level layout on new size)
				if (reset) {
					getParent().layout(true, true);
				} else {
					layout(true, true);
				}
			}
		});
	}

	protected void setupPush() {
		// Not supported
	}
	
	public int getWidth() {
		return width;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}
	
}
