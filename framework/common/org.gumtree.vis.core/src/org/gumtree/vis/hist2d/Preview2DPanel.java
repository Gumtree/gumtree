/**
 * 
 */
package org.gumtree.vis.hist2d;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.gumtree.vis.core.internal.SWTChartComposite.ImageSelection;
import org.gumtree.vis.core.internal.StaticValues;
import org.gumtree.vis.hist2d.color.ColorScale;
import org.gumtree.vis.interfaces.IDataset;
import org.gumtree.vis.interfaces.IExporter;
import org.gumtree.vis.interfaces.IHelpProvider;
import org.gumtree.vis.interfaces.IPreview2D;
import org.gumtree.vis.interfaces.IPreview2DDataset;
import org.gumtree.vis.mask.AbstractMask;
import org.gumtree.vis.mask.IMaskEventListener;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.ui.ExtensionFileFilter;

/**
 * @author nxi
 *
 */
public class Preview2DPanel extends JPanel implements IPreview2D, Printable {

	private static IndexColorModel colorModel = 
		ColorScale.getColorModel(ColorScale.getCurrentColorScale());

	private Image image;
	private IPreview2DDataset dataset;
	private List<Line2D> lines;
	private List<Point> indexLocatons;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1623448496540317399L;

	/**
	 * 
	 */
	public Preview2DPanel() {
//		super();
	}

	/**
	 * @param arg0
	 */
	public Preview2DPanel(LayoutManager arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public Preview2DPanel(boolean arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public Preview2DPanel(LayoutManager arg0, boolean arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
//		long time = System.currentTimeMillis();
        Graphics2D g2 = (Graphics2D) g.create();

        // first determine the size of the chart rendering area...
        Dimension size = getSize();
        Insets insets = getInsets();
        Rectangle2D available = new Rectangle2D.Double(insets.left, insets.top,
                size.getWidth() - insets.left - insets.right,
                size.getHeight() - insets.top - insets.bottom);

        double drawWidth = available.getWidth();
        double drawHeight = available.getHeight();

        image = createPreviewImage(drawWidth, drawHeight);
        if (image != null) {
        	g2.drawImage(image, insets.left, insets.top, null);
        }
        g2.setBackground(Color.white);
        drawGrids(g2);
        drawText(g2);
//        long diff = System.currentTimeMillis() - time;
//        System.out.println("refreshing cost: " + diff);
	}
	
	private void drawText(Graphics2D g2) {
		g2.setPaint(Color.white);
//		g2.setStroke(new BasicStroke(0.1f));
		int count = 0;
		for (Point point : indexLocatons) {
			count++;
			g2.drawString(String.valueOf(count), point.x, point.y);
		}
	}

	private void drawGrids(Graphics2D g2) {
		
		g2.setPaint(Color.white);
//		g2.setStroke(new BasicStroke(0.1f));
		g2.setStroke(new BasicStroke(
			      1f, 
			      BasicStroke.CAP_ROUND, 
			      BasicStroke.JOIN_ROUND, 
			      1f, 
			      new float[] {5}, 
			      0f));
		for (Line2D line : lines) {
			g2.draw(line);
		}
	}

	private Image createPreviewImage(double drawWidth, double drawHeight) {
		if (dataset == null) {
			return null;
		}
		int frameWidth = dataset.getXSize(0);
		int frameHeight = dataset.getYSize(0);
		int numberOfFrame = dataset.getNumberOfFrames(0);
		int columnCounts = calculateNumberOfColumns(drawWidth, 
				drawHeight, frameWidth, frameHeight, numberOfFrame);
		int rowCounts = calculateNumberOfRows(columnCounts, 
				numberOfFrame);
		int fullImageWidth = columnCounts * frameWidth;
		int fullImageHeight = rowCounts * frameHeight;
		int imageWidth = fullImageWidth < drawWidth ? 
				fullImageWidth : (int) drawWidth;
		int imageHeight = fullImageHeight < drawHeight ?
				fullImageHeight : (int) drawHeight;
//		createImage(dataset, imageWidth, imageHeight);
		double maxValue = dataset.getZMax(0);
		double valueColorRate = ColorScale.DIVISION_COUNT / maxValue;
		double horizontalJump = (double) fullImageWidth / imageWidth;
		double verticalJump = (double) fullImageHeight / imageHeight;
		byte[] imageData = new byte[imageWidth * imageHeight];
		int count = 0;
		for (int i = 0; i < imageHeight; i++) {
			int fullIndexVertical = (int) Math.round(i * verticalJump);
			int frameIndexVertical = fullIndexVertical / frameHeight;
			int verticalIndex = fullIndexVertical % frameHeight;
			int horizontalIndex;
			int frameIndexHorizontal;
			int frameIndex;
			for (int j = 0; j < imageWidth; j++) {
//				double value = findImagePixel(dataset, i, j, 
//						rowCounts, columnCounts, verticalJump, 
//						horizontalJump);
				int fullIndexHorizontal = (int) Math.round(j * horizontalJump);
				frameIndexHorizontal = fullIndexHorizontal / frameWidth;
				frameIndex = frameIndexHorizontal * rowCounts + frameIndexVertical;
				if (frameIndex < numberOfFrame) {
					horizontalIndex = fullIndexHorizontal % frameWidth;
					double value = dataset.getZValue(0, frameIndex, verticalIndex,
							horizontalIndex);
					imageData[count] = Double.valueOf(value * valueColorRate).byteValue();
				} else {
//					imageData[count] = (byte) (ColorScale.DIVISION_COUNT - 1);
					
				}
				count++;
			}
		}
		image = Toolkit.getDefaultToolkit().createImage(
	            new MemoryImageSource(imageWidth, imageHeight, colorModel, 
	            		imageData, 0, imageWidth));
		lines = new ArrayList<Line2D>();
		int rowButtom;
		for (int i = 0; i < rowCounts; i++) {
			rowButtom = (int) Math.round(i * frameHeight / verticalJump);
			if (rowButtom < drawHeight && rowButtom > 0) {
				lines.add(new Line2D.Float(0, rowButtom, (int) drawWidth, rowButtom)); 
			}
		}
		int columnRight;
		for (int i = 0; i < columnCounts; i++) {
			columnRight = (int) Math.round(i * frameWidth / horizontalJump);
			if (columnRight < drawWidth && columnRight > 0) {
				lines.add(new Line2D.Float(columnRight, 0, columnRight, (int) drawHeight)); 
			}
		}
		indexLocatons = new ArrayList<Point>();
		for (int i = 0; i < numberOfFrame; i++) {
			int locationRow = i % rowCounts + 1;
			int locationColumn = i / rowCounts;
			int locationY = (int) Math.round(locationRow * frameHeight / verticalJump) - 4;
			int locationX = (int) Math.round(locationColumn * frameWidth / horizontalJump) + 4;
			indexLocatons.add(new Point(locationX, locationY));
		}
		return image;
	}

	private int calculateNumberOfRows(int columnCounts, int numberOfFrame) {
		return (int) Math.ceil((double) numberOfFrame / columnCounts);
	}

	private int calculateNumberOfColumns(double drawWidth, double drawHeight,
			int frameWidth, int frameHeight, int numberOfFrame) {
		// TODO Auto-generated method stub
		double columns = Math.sqrt(frameHeight * drawWidth * numberOfFrame /
				frameWidth / drawHeight);
		int columnCounts = 0;
		if (Math.abs(columns - Math.round(columns)) < 0.25) {
			columnCounts = (int) Math.round(columns);
		} else {
			int lessColumns = (int) columns;
			int moreRows = calculateNumberOfRows(lessColumns, numberOfFrame);
			double highFrameRate = (double) moreRows * frameHeight / lessColumns / frameWidth;
			int moreColumns = (int) (columns + 1);
			int lessRows = calculateNumberOfRows(moreColumns, numberOfFrame);
			double lowFrameRate = (double) lessRows * frameHeight / moreColumns / frameWidth;
			double realFrameRate = drawHeight / drawWidth;
			if (Math.abs(lowFrameRate - realFrameRate) < Math.abs(highFrameRate - realFrameRate)) {
				columnCounts = moreColumns;
			} else {
				columnCounts = lessColumns;
			}
		}
		int rowCounts = calculateNumberOfRows(columnCounts, numberOfFrame);
		if (columnCounts * rowCounts >= numberOfFrame + rowCounts) {
			columnCounts--;
		}
		return columnCounts;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#addChartMouseListener(org.jfree.chart.ChartMouseListener)
	 */
	@Override
	public void addChartMouseListener(ChartMouseListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public int print(Graphics g, PageFormat pf, int pageIndex)
			throws PrinterException {
        if (pageIndex != 0) {
            return NO_SUCH_PAGE;
        }
        Graphics2D g2 = (Graphics2D) g;
        double x = pf.getImageableX();
        double y = pf.getImageableY();
        double w = pf.getImageableWidth();
        double h = pf.getImageableHeight();
        double screenWidth = getWidth();
        double screenHeight = getHeight();
        double widthRatio = w / screenWidth;
        double heightRatio = h / screenHeight;
        double overallRatio = 1;
        overallRatio = widthRatio < heightRatio ? widthRatio : heightRatio;
        Rectangle2D printArea = new Rectangle2D.Double(x, y, screenWidth * overallRatio, 
        		screenHeight * overallRatio);
        draw(g2, printArea, 0, 0);
        return PAGE_EXISTS;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#createChartPrintJob()
	 */
	@Override
	public void createChartPrintJob() {
        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat pf = job.defaultPage();
        PageFormat pf2 = job.pageDialog(pf);
        if (pf2 != pf) {
            job.setPrintable(this, pf2);
            if (job.printDialog()) {
                try {
                    job.print();
                }
                catch (PrinterException e) {
                    JOptionPane.showMessageDialog(this, e);
                }
            }
        }
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#doCopy()
	 */
	@Override
	public void doCopy() {
		final Clipboard systemClipboard
				= Toolkit.getDefaultToolkit().getSystemClipboard();
		Cursor currentCursor = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        Dimension size = getSize();
        Insets insets = getInsets();
        Rectangle2D available = new Rectangle2D.Double(insets.left, insets.top,
                size.getWidth() - insets.left - insets.right,
                size.getHeight() - insets.top - insets.bottom);

        double drawWidth = available.getWidth();
        double drawHeight = available.getHeight();
        image = createPreviewImage(drawWidth, drawHeight);
        BufferedImage buffer = null;
        if (image instanceof BufferedImage) {
        	buffer = (BufferedImage) image;
        	Graphics2D g2 = buffer.createGraphics();
            drawGrids(g2);
            drawText(g2);
        	g2.dispose();
        } else {
        	buffer = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
        	Graphics2D g2 = buffer.createGraphics();
        	g2.drawImage(image, 0, 0, null);
            drawGrids(g2);
            drawText(g2);
        	g2.dispose();
        }
		systemClipboard.setContents(new ImageSelection(buffer), null);
		
		setCursor(currentCursor);
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#doSaveAs()
	 */
	@Override
	public void doSaveAs() throws IOException {
		JFileChooser fileChooser = new JFileChooser();
        String currentDirectory = System.getProperty(StaticValues.SYSTEM_SAVE_PATH_LABEL);
        if (currentDirectory != null) {
        	File savePath = new File(currentDirectory);
        	if (savePath.exists() && savePath.isDirectory()) {
        		fileChooser.setCurrentDirectory(savePath);
        	}
        }
        ExtensionFileFilter jpgFilter = new ExtensionFileFilter("JPG_Image_Files", ".jpg");
        ExtensionFileFilter pngFilter = new ExtensionFileFilter("PNG_Image_Files", ".png");
        fileChooser.addChoosableFileFilter(pngFilter);
        fileChooser.addChoosableFileFilter(jpgFilter);

        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
        	String filename = fileChooser.getSelectedFile().getPath();
        	String selectedDescription = fileChooser.getFileFilter().getDescription();
        	String fileExtension = StaticValues.DEFAULT_IMAGE_FILE_EXTENSION;
        	if (selectedDescription.toLowerCase().contains("png")) {
        		fileExtension = "png";
        		if (!filename.toLowerCase().endsWith(".png")) {
        			filename = filename + ".png";
        		}
        	} else if (selectedDescription.toLowerCase().contains("jpg")) {
        		fileExtension = "jpg";
        		if (!filename.toLowerCase().endsWith(".jpg")) {
        			filename = filename + ".jpg";
        		}
        	}
        	File selectedFile = new File(filename);
        	int confirm = JOptionPane.YES_OPTION;
        	if (selectedFile.exists()) {
        		confirm = JOptionPane.showConfirmDialog(this, selectedFile.getName() + " exists, overwrite?", 
        				"Confirm Overwriting", JOptionPane.YES_NO_OPTION);
        	}
        	if (confirm == JOptionPane.YES_OPTION) {
        		saveTo(filename, fileExtension);
        		System.setProperty(StaticValues.SYSTEM_SAVE_PATH_LABEL, 
        				fileChooser.getSelectedFile().getParent());
        	}
        }
	}
	
	public void saveTo(String filename, String fileType) 
	throws IOException{
		int filterIndex = 0;
		if (fileType != null) {
			if (fileType.toLowerCase().contains("png")) {
				filterIndex = 0;
			} else if (fileType.toLowerCase().contains("jpg") || 
					fileType.toLowerCase().contains("jpeg")) {
				filterIndex = 1;
			} 
		}
        OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(filename)));

		Dimension size = getSize();
        Insets insets = getInsets();
        Rectangle2D available = new Rectangle2D.Double(insets.left, insets.top,
                size.getWidth() - insets.left - insets.right,
                size.getHeight() - insets.top - insets.bottom);

        double drawWidth = available.getWidth();
        double drawHeight = available.getHeight();
        image = createPreviewImage(drawWidth, drawHeight);
        BufferedImage buffer = null;
        if (image instanceof BufferedImage) {
        	buffer = (BufferedImage) image;
        	Graphics2D g2 = buffer.createGraphics();
            drawGrids(g2);
            drawText(g2);
        	g2.dispose();
        } else {
        	buffer = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
        	Graphics2D g2 = buffer.createGraphics();
        	g2.drawImage(image, 0, 0, null);
            drawGrids(g2);
            drawText(g2);
        	g2.dispose();
        }

		if (filterIndex == 0) {
	        try{
	        	ChartUtilities.writeBufferedImageAsPNG(out, buffer);
	        } finally {
	        	out.close();
	        }
		} else if (filterIndex == 1) {
			try{
	        	ChartUtilities.writeBufferedImageAsJPEG(out, buffer);
	        } finally {
	        	out.close();
	        }
		}
		
	}


	
	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#getChart()
	 */
	@Override
	public JFreeChart getChart() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#getChartX()
	 */
	@Override
	public double getChartX() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#getChartY()
	 */
	@Override
	public double getChartY() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#getDataset()
	 */
	@Override
	public IDataset getDataset() {
		// TODO Auto-generated method stub
		return dataset;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#getHorizontalAxis()
	 */
	@Override
	public ValueAxis getHorizontalAxis() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#getImage()
	 */
	@Override
	public Image getImage() {
		// TODO Auto-generated method stub
		return image;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#getMasks()
	 */
	@Override
	public List<AbstractMask> getMasks() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#getSelectedMask()
	 */
	@Override
	public AbstractMask getSelectedMask() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#getTitle()
	 */
	@Override
	public TextTitle getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#getVerticalAxis()
	 */
	@Override
	public ValueAxis getVerticalAxis() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#getXYPlot()
	 */
	@Override
	public XYPlot getXYPlot() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setAutoUpdate(boolean)
	 */
	@Override
	public void setAutoUpdate(boolean autoUpdate) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#isHorizontalAxisFlipped()
	 */
	@Override
	public boolean isHorizontalAxisFlipped() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#isMouseWheelEnabled()
	 */
	@Override
	public boolean isMouseWheelEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#isToolTipFollowerEnabled()
	 */
	@Override
	public boolean isToolTipFollowerEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#isVerticalAxisFlipped()
	 */
	@Override
	public boolean isVerticalAxisFlipped() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#moveSelectedMask(int)
	 */
	@Override
	public void moveSelectedMask(int keyCode) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#removeChartMouseListener(org.jfree.chart.ChartMouseListener)
	 */
	@Override
	public void removeChartMouseListener(ChartMouseListener listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#removeSelectedMask()
	 */
	@Override
	public void removeSelectedMask() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#restoreAutoBounds()
	 */
	@Override
	public void restoreAutoBounds() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#restoreHorizontalBounds()
	 */
	@Override
	public void restoreHorizontalBounds() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#restoreVerticalBounds()
	 */
	@Override
	public void restoreVerticalBounds() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#isAutoUpdate()
	 */
	@Override
	public boolean isAutoUpdate() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setBackgroundColor(java.awt.Color)
	 */
	@Override
	public void setBackgroundColor(Color color) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setDataset(org.gumtree.vis.interfaces.IDataset)
	 */
	@Override
	public void setDataset(IDataset dataset) {
		if (dataset instanceof IPreview2DDataset) {
			this.dataset = (IPreview2DDataset) dataset;
		} else {
			throw new IllegalArgumentException("not a IPreview2DDataset");
		}
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setHorizontalAxisFlipped(boolean)
	 */
	@Override
	public void setHorizontalAxisFlipped(boolean isFlipped) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setHorizontalAxisTrace(boolean)
	 */
	@Override
	public void setHorizontalAxisTrace(boolean isEnabled) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setHorizontalZoomable(boolean)
	 */
	@Override
	public void setHorizontalZoomable(boolean isZoomable) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setMouseWheelEnabled(boolean)
	 */
	@Override
	public void setMouseWheelEnabled(boolean isEnabled) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setSelectedMask(org.gumtree.vis.mask.AbstractMask)
	 */
	@Override
	public void setSelectedMask(AbstractMask selectedMask) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setToolTipFollowerEnabled(boolean)
	 */
	@Override
	public void setToolTipFollowerEnabled(boolean enabled) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setVerticalAxisFlipped(boolean)
	 */
	@Override
	public void setVerticalAxisFlipped(boolean isFlipped) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setVerticalAxisTrace(boolean)
	 */
	@Override
	public void setVerticalAxisTrace(boolean isEnabled) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setVerticalZoomable(boolean)
	 */
	@Override
	public void setVerticalZoomable(boolean isZoomable) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setZoomInFactor(double)
	 */
	@Override
	public void setZoomInFactor(double factor) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setZoomOutFactor(double)
	 */
	@Override
	public void setZoomOutFactor(double factor) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#zoomInBoth(double, double)
	 */
	@Override
	public void zoomInBoth(double x, double y) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#zoomInHorizontal(double, double)
	 */
	@Override
	public void zoomInHorizontal(double x, double y) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#zoomInVertical(double, double)
	 */
	@Override
	public void zoomInVertical(double x, double y) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#zoomOutBoth(double, double)
	 */
	@Override
	public void zoomOutBoth(double x, double y) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#zoomOutHorizontal(double, double)
	 */
	@Override
	public void zoomOutHorizontal(double x, double y) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#zoomOutVertical(double, double)
	 */
	@Override
	public void zoomOutVertical(double x, double y) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#draw(java.awt.Graphics2D, java.awt.geom.Rectangle2D, double, double)
	 */
	@Override
	public void draw(Graphics2D g2, Rectangle2D area, double shiftX,
			double shiftY) {
		Dimension size = getSize();
        Insets insets = getInsets();
        Rectangle2D available = new Rectangle2D.Double(insets.left, insets.top,
                size.getWidth() - insets.left - insets.right,
                size.getHeight() - insets.top - insets.bottom);

        double drawWidth = available.getWidth();
        double drawHeight = available.getHeight();
        double widthRatio = area.getWidth() / drawWidth;
        double heightRatio = area.getHeight() / drawHeight;
        double overallRatio = 1;
        overallRatio = widthRatio < heightRatio ? widthRatio : heightRatio;
        image = createPreviewImage(drawWidth * overallRatio, drawHeight * overallRatio);

        g2.drawImage(image, (int) area.getX(), (int) area.getY(), (int) area.getWidth(), (int) area.getHeight(), null);
        
        g2.setPaint(Color.white);
		g2.setStroke(new BasicStroke(
			      1f, 
			      BasicStroke.CAP_ROUND, 
			      BasicStroke.JOIN_ROUND, 
			      1f, 
			      new float[] {5}, 
			      0f));
		for (Line2D line : lines) {
			g2.draw(new Line2D.Double(line.getX1() + area.getX(), line.getY1() + area.getY(), 
					line.getX2() + area.getX(), line.getY2() + area.getY()));
		}
        
        g2.setPaint(Color.white);
		int count = 0;
		for (Point point : indexLocatons) {
			count++;
			g2.drawString(String.valueOf(count), point.x + (int) area.getX(), point.y + (int) area.getY());
		}
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#updatePlot()
	 */
	@Override
	public void updatePlot() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#updateLabels()
	 */
	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#addMask(org.gumtree.vis.mask.AbstractMask)
	 */
	@Override
	public void addMask(AbstractMask mask) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#addMasks(java.util.List)
	 */
	@Override
	public void addMasks(List<AbstractMask> maskList) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#setMaskingEnabled(boolean)
	 */
	@Override
	public void setMaskingEnabled(boolean enabled) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#isMaskingEnabled()
	 */
	@Override
	public boolean isMaskingEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#removeMask(org.gumtree.vis.mask.AbstractMask)
	 */
	@Override
	public void removeMask(AbstractMask mask) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#addMaskEventListener(org.gumtree.vis.mask.IMaskEventListener)
	 */
	@Override
	public void addMaskEventListener(IMaskEventListener listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.gumtree.vis.interfaces.IPlot#removeMaskEventListener(org.gumtree.vis.mask.IMaskEventListener)
	 */
	@Override
	public void removeMaskEventListener(IMaskEventListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processMouseWheelEvent(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub
		super.processMouseWheelEvent(arg0);
	}

	@Override
	public void doEditChartProperties() {
		// TODO add implementation
		
	}

	@Override
	public void setPlotTitle(String title) {
	}
	
	@Override
	public void doHelp() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void doExport(IExporter exporter) throws IOException {
		
	}
	
	@Override
	public void cleanUp() {
		
	}

	@Override
	public void setLogarithmEnabled(boolean enabled) {
	}

	@Override
	public boolean isLogarithmEnabled() {
		return false;
	}
	
	@Override
	public IHelpProvider getHelpProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addShape(Shape shape, Color color) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeShape(Shape shape) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearShapes() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isShapeEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setShapeEnabled(boolean isShapeEnabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addDomainAxisMarker(double x, int height, Color color) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addRangeAxisMarker(double y, int width, Color color) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addMarker(double x, double y, Color color) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeMarker(double x, double y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeRangeAxisMarker(double y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeDomainAxisMarker(double x) {
		// TODO Auto-generated method stub
		
	}
}
