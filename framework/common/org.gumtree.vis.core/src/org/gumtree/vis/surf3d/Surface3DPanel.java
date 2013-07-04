/**
 * 
 */
package org.gumtree.vis.surf3d;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.media.j3d.Canvas3D;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Screen3D;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.freehep.j3d.plot.AdaptablePlot;
import org.freehep.j3d.plot.Plot3D;
import org.freehep.j3d.plot.Plot3D.ColorTheme;
import org.freehep.j3d.plot.RenderStyle;
import org.gumtree.vis.core.internal.SWTChartComposite.ImageSelection;
import org.gumtree.vis.core.internal.StaticValues;
import org.gumtree.vis.hist2d.color.ColorScale;
import org.gumtree.vis.interfaces.IDataset;
import org.gumtree.vis.interfaces.IExporter;
import org.gumtree.vis.interfaces.IHelpProvider;
import org.gumtree.vis.interfaces.ISurf3D;
import org.gumtree.vis.interfaces.IXYZDataset;
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
public class Surface3DPanel extends JPanel implements ISurf3D, Printable {

	private final static RenderStyle DEFAULT_RENDER_STYLE = RenderStyle.LEGO;
	private final static ColorScale DEFAULT_COLOR_SCALE = ColorScale.Nature;
	private IXYZDataset xyzDataset;
	private Internal3DDataset internalDataset;
	private Plot3D renderer;
	private Image image;
	private boolean isLegoFeel = true;
	/**
	 * 
	 */
	private static final long serialVersionUID = 7651051224094304338L;
	private static final String PRINT_JOB_NAME = "3D plot";


	public Surface3DPanel(LayoutManager layout) {
		setLayout(layout);
		setBorder(BorderFactory.createLineBorder (Color.black));
//		setBackground(Color.black);
		createRender();
	}

	private void createRender() {
		setLayout(new BorderLayout());
//		if (isLegoFeel) {
//			renderer = new LegoPlot();
//			((LegoPlot) renderer).setDrawBlocks(false);
//		} else {
//			renderer = new SurfacePlot();
//		}
		renderer = new AdaptablePlot();
		renderer.setRenderStyle(DEFAULT_RENDER_STYLE);
//		renderer.setColorTheme(ColorTheme.WHITE);
		add(renderer, BorderLayout.CENTER);
	}

	@Override
	public void addChartMouseListener(ChartMouseListener listener) {
		// no action needed
		
	}

	@Override
	public void createChartPrintJob() {
		IDataset dataset = getDataset();
		String title = PRINT_JOB_NAME;
		if (dataset != null) { 
			title = dataset.getTitle();
		}
		PrinterJob job = PrinterJob.getPrinterJob();
		job.setJobName(title);
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
        BufferedImage buffer = createOffScreenImage(drawWidth, drawHeight);
//        if (image instanceof BufferedImage) {
//        	buffer = (BufferedImage) image;
//        	Graphics2D g2 = buffer.createGraphics();
////            drawGrids(g2);
////            drawText(g2);
//        	g2.dispose();
//        } else {
//        	buffer = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
//        	Graphics2D g2 = buffer.createGraphics();
//        	g2.drawImage(image, 0, 0, null);
////            drawGrids(g2);
////            drawText(g2);
//        	g2.dispose();
//        }
		systemClipboard.setContents(new ImageSelection(buffer), null);
		
		setCursor(currentCursor);
	}

	private BufferedImage createOffScreenImage(double drawWidth, double drawHeight) {
		if (renderer != null) {
			Canvas3D offScreenCanvas = new Canvas3D(renderer.getGraphicsConfiguration(), true);
//			offScreenCanvas.setSize(renderer.getSize());
			Screen3D onScreen = renderer.getScreen3D();
			Screen3D offScreen = offScreenCanvas.getScreen3D();
			offScreen.setSize(onScreen.getSize());
			offScreen.setPhysicalScreenWidth(onScreen.getPhysicalScreenWidth());
			offScreen.setPhysicalScreenHeight(onScreen.getPhysicalScreenHeight());
			renderer.getUniverse().getViewer().getView().addCanvas3D(offScreenCanvas);
			
			//			image = renderer.createImage((int) drawWidth, (int) drawHeight);
			BufferedImage bImage = new BufferedImage((int) drawWidth, (int) drawHeight,
					BufferedImage.TYPE_INT_ARGB);

			ImageComponent2D buffer = new ImageComponent2D(
					ImageComponent.FORMAT_RGBA, bImage);

			offScreenCanvas.setOffScreenBuffer(buffer);
			offScreenCanvas.renderOffScreenBuffer();
			offScreenCanvas.waitForOffScreenRendering();
			BufferedImage image = offScreenCanvas.getOffScreenBuffer().getImage();
			renderer.getUniverse().getViewer().getView().removeCanvas3D(offScreenCanvas);
			return image;
		}
		return null;
	}
	
	@Override
	public void doEditChartProperties() {
		Surf3DPlotEditor editor = new Surf3DPlotEditor(this);
        int result = JOptionPane.showConfirmDialog(this, editor,
                "Chart_Properties", JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            editor.updatePlot();
        }
	}

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

	@Override
	public JFreeChart getChart() {
		// no action needed
		return null;
	}

    private void showPropertyEditor(int tabIndex) {
    	Surf3DPlotEditor editor = new Surf3DPlotEditor(this);
    	editor.getTabs().setSelectedIndex(tabIndex);
        int result = JOptionPane.showConfirmDialog(this, editor,
                "Chart_Properties", JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            editor.updatePlot();
        }
    }
    
	@Override
	public double getChartX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getChartY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IDataset getDataset() {
		return xyzDataset;
	}

	@Override
	public ValueAxis getHorizontalAxis() {
		// no action needed
		return null;
	}

	@Override
	public Image getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AbstractMask> getMasks() {
		// no action needed
		return null;
	}

	@Override
	public AbstractMask getSelectedMask() {
		// no action needed
		return null;
	}

	@Override
	public TextTitle getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ValueAxis getVerticalAxis() {
		// no action needed
		return null;
	}

	@Override
	public XYPlot getXYPlot() {
		// no action needed
		return null;
	}

	@Override
	public void setAutoUpdate(boolean autoUpdate) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isHorizontalAxisFlipped() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isMouseWheelEnabled() {
		// no action needed
		return false;
	}

	@Override
	public boolean isToolTipFollowerEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isVerticalAxisFlipped() {
		// no action needed
		return false;
	}

	@Override
	public void moveSelectedMask(int keyCode) {
		// no action needed
		
	}

	@Override
	public void removeChartMouseListener(ChartMouseListener listener) {
		// no action needed
		
	}

	@Override
	public void removeSelectedMask() {
		// no action needed
		
	}

	@Override
	public void restoreAutoBounds() {
		resetPlot();
	}

	@Override
	public void restoreHorizontalBounds() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restoreVerticalBounds() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveTo(String filename, String fileType) throws IOException {
		int filterIndex = 0;
		if (fileType != null) {
			if (fileType.toLowerCase().contains("png")) {
				filterIndex = 0;
			} else if (fileType.toLowerCase().contains("jpg") || 
					fileType.toLowerCase().contains("jpeg")) {
				filterIndex = 1;
			} 
		}
		File fileTarget = new File(filename);
        OutputStream out = new BufferedOutputStream(new FileOutputStream(fileTarget));

		Dimension size = getSize();
        Insets insets = getInsets();
        Rectangle2D available = new Rectangle2D.Double(insets.left, insets.top,
                size.getWidth() - insets.left - insets.right,
                size.getHeight() - insets.top - insets.bottom);

        double drawWidth = available.getWidth();
        double drawHeight = available.getHeight();
        BufferedImage buffer = createOffScreenImage(drawWidth, drawHeight);
//        BufferedImage buffer = null;
//        if (image instanceof BufferedImage) {
//        	buffer = (BufferedImage) image;
//        	Graphics2D g2 = buffer.createGraphics();
////            drawGrids(g2);
////            drawText(g2);
//        	g2.dispose();
//        } else {
//        	buffer = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB_PRE);
//        	Graphics2D g2 = buffer.createGraphics();
//        	g2.drawImage(image, 0, 0, null);
////            drawGrids(g2);
////            drawText(g2);
//        	g2.dispose();
//        }

		if (filterIndex == 0) {
	        try{
	        	ChartUtilities.writeBufferedImageAsPNG(out, buffer);
	        } finally {
	        	out.close();
	        }
		} else if (filterIndex == 1) {
			try{
				BufferedImage image = new BufferedImage(buffer.getWidth(null), buffer.getHeight(null), BufferedImage.TYPE_INT_RGB);
				Graphics2D g2 = image.createGraphics();
				g2.setBackground(Color.black);
				g2.drawImage(buffer, 0, 0, null);
	        	ChartUtilities.writeBufferedImageAsJPEG(out, image);
//	        	ImageIO.write(buffer, "jpg", );
	        } finally {
	        	out.close();
	        }
		}
	}

	@Override
	public boolean isAutoUpdate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setBackgroundColor(Color color) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDataset(IDataset dataset) {
		if (dataset instanceof IXYZDataset) {
			this.xyzDataset = (IXYZDataset) dataset;
			internalDataset = new Internal3DDataset(xyzDataset);
			internalDataset.setColorScale(DEFAULT_COLOR_SCALE);
			renderer.setData(internalDataset);
			String xLabel = "X Axis";
			String xTitle = xyzDataset.getXTitle();
			String xUnits = xyzDataset.getXUnits();
			if (xTitle != null && xTitle.trim().length() > 0) {
				xLabel = xTitle;
			}
			if (xUnits != null && xUnits.trim().length() > 0) {
				xLabel += " (" + xUnits + ")";
			}
			renderer.setXAxisLabel(xLabel);
			String yLabel = "Y Axis";
			String yTitle = xyzDataset.getYTitle();
			String yUnits = xyzDataset.getYUnits();
			if (yTitle != null && yTitle.trim().length() > 0) {
				yLabel = yTitle;
			}
			if (yUnits != null && yUnits.trim().length() > 0) {
				yLabel += " (" + yUnits + ")";
			}
			renderer.setYAxisLabel(yLabel);
			String zLabel = "";
			String zTitle = xyzDataset.getZTitle();
			String zUnits = xyzDataset.getZUnits();
			if (zTitle != null && zTitle.trim().length() > 0) {
				zLabel = zTitle;
				if (zUnits != null && zUnits.trim().length() > 0) {
					zLabel += " (" + zUnits + ")";
				}
			} 
			renderer.setZAxisLabel(zLabel);
		} else {
			throw new IllegalArgumentException("not a XYZ Dataset");
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Dimension dim = getSize();
		if (renderer != null) {
			renderer.setSize(dim);
		}
//		if (dim.height < dim.width) {
//			renderer.zoomInDepth((double) dim.height / dim.width);
//		}
	}
	
	@Override
	public void setHorizontalAxisFlipped(boolean isFlipped) {
		// no action needed
		
	}

	@Override
	public void setHorizontalAxisTrace(boolean isEnabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHorizontalZoomable(boolean isZoomable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMouseWheelEnabled(boolean isEnabled) {
		// no action needed
		
	}

	@Override
	public void setSelectedMask(AbstractMask selectedMask) {
		// no action needed
		
	}

	@Override
	public void setToolTipFollowerEnabled(boolean enabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setVerticalAxisFlipped(boolean isFlipped) {
		// no action needed
		
	}

	@Override
	public void setVerticalAxisTrace(boolean isEnabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setVerticalZoomable(boolean isZoomable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setZoomInFactor(double factor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setZoomOutFactor(double factor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void zoomInBoth(double x, double y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void zoomInHorizontal(double x, double y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void zoomInVertical(double x, double y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void zoomOutBoth(double x, double y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void zoomOutHorizontal(double x, double y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void zoomOutVertical(double x, double y) {
		// TODO Auto-generated method stub
		
	}

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
        image = createOffScreenImage(drawWidth * overallRatio, drawHeight * overallRatio);

        g2.drawImage(image, (int) area.getX(), (int) area.getY(), (int) area.getWidth(), (int) area.getHeight(), null);
        
//        g2.setPaint(Color.white);
//		g2.setStroke(new BasicStroke(
//			      1f, 
//			      BasicStroke.CAP_ROUND, 
//			      BasicStroke.JOIN_ROUND, 
//			      1f, 
//			      new float[] {5}, 
//			      0f));
//		for (Line2D line : lines) {
//			g2.draw(new Line2D.Double(line.getX1() + area.getX(), line.getY1() + area.getY(), 
//					line.getX2() + area.getX(), line.getY2() + area.getY()));
//		}
//        
//        g2.setPaint(Color.white);
//		int count = 0;
//		for (Point point : indexLocatons) {
//			count++;
//			g2.drawString(String.valueOf(count), point.x + (int) area.getX(), point.y + (int) area.getY());
//		}
	}

	@Override
	public void updatePlot() {
		if (internalDataset != null) {
			renderer.repaint(internalDataset);
		}
	}

	@Override
	public void updateLabels() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addMask(AbstractMask mask) {
		// no action needed
		
	}

	@Override
	public void addMasks(List<AbstractMask> maskList) {
		// no action needed
		
	}

	@Override
	public void setMaskingEnabled(boolean enabled) {
		// no action needed
		
	}

	@Override
	public boolean isMaskingEnabled() {
		// no action needed
		return false;
	}

	@Override
	public void removeMask(AbstractMask mask) {
		// no action needed
		
	}

	@Override
	public void addMaskEventListener(IMaskEventListener listener) {
		// no action needed
		
	}

	@Override
	public void removeMaskEventListener(IMaskEventListener listener) {
		// no action needed
		
	}

	@Override
	public void setPlotTitle(String title) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doHelp() {
		Surf3DPlotEditor editor = new Surf3DPlotEditor(this);
    	editor.showHelpPanel();
        int result = JOptionPane.showConfirmDialog(this, editor,
                "Chart_Properties", JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            editor.updatePlot();
        }
	}

	@Override
	public void doExport(IExporter exporter) throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        String currentDirectory = System.getProperty(StaticValues.SYSTEM_SAVE_PATH_LABEL);
        if (currentDirectory != null) {
        	File savePath = new File(currentDirectory);
        	if (savePath.exists() && savePath.isDirectory()) {
        		fileChooser.setCurrentDirectory(savePath);
        	}
        }
    	String fileExtension = exporter.getExtensionName();
        ExtensionFileFilter extensionFilter = new ExtensionFileFilter(exporter.toString(), "." + fileExtension);
        fileChooser.addChoosableFileFilter(extensionFilter);

        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
        	String filename = fileChooser.getSelectedFile().getPath();
//        	String selectedDescription = fileChooser.getFileFilter().getDescription();
        	if (!filename.toLowerCase().endsWith("." + fileExtension)) {
        		filename = filename + "." + fileExtension;
        	}
        	File selectedFile = new File(filename);
        	int confirm = JOptionPane.YES_OPTION;
        	if (selectedFile.exists()) {
        		confirm = JOptionPane.showConfirmDialog(this, selectedFile.getName() + " exists, overwrite?", 
        				"Confirm Overwriting", JOptionPane.YES_NO_OPTION);
        	} else {
        		selectedFile.createNewFile();
        	}
        	if (confirm == JOptionPane.YES_OPTION) {
        		exporter.export(selectedFile, getDataset());
        		System.setProperty(StaticValues.SYSTEM_SAVE_PATH_LABEL, 
        				fileChooser.getSelectedFile().getParent());
        	}
        }
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

	@Override
	public void processMouseWheelEvent(MouseWheelEvent e) {
		// no action needed
		super.processMouseWheelEvent(e);
	}

	/**
	 * @return the isLegoFeel
	 */
	public boolean isLegoFeel() {
		return isLegoFeel;
	}

	/**
	 * @param isLegoFeel the isLegoFeel to set
	 */
	public void setLegoFeel(boolean isLegoFeel) {
		if (this.isLegoFeel ^ isLegoFeel) {
			this.isLegoFeel = isLegoFeel;
			remove(renderer);
			createRender();
		}
	}
	public void cleanUp() {
		renderer.cleanUp();
		xyzDataset = null;
		internalDataset = null;
		remove(renderer);
		getLayout().removeLayoutComponent(renderer);
		setLayout(null);
		renderer = null;
	}
	
	@Override
	public Plot3D get3DRenderer() {
		return renderer;
	}
	
	@Override
	public void resetOrientation() {
		renderer.resetOrientation();
	}

	@Override
	public void resetZoomScale() {
		renderer.resetZoomDepth();
		Dimension dim = getSize();
		if (dim.height < dim.width) {
			double factor = 1 - (double) dim.width / dim.height;
			System.out.println(factor);
			renderer.zoomInDepth(factor);
		}
	}

	@Override
	public void resetCenter() {
		renderer.resetCenter();
	}

	@Override
	public void resetPlot() {
		renderer.resetToDefault();
		Dimension dim = getSize();
		if (dim.height < dim.width) {
			renderer.zoomInDepth(1 - (double) dim.width / dim.height);
		}
	}
	
	@Override
	public void setColorScale(ColorScale colorScale) {
		if (internalDataset != null) {
			internalDataset.setColorScale(colorScale);
		}
	}
	
	@Override
	public ColorScale getColorScale() {
		if (internalDataset != null) {
			return internalDataset.getColorScale();
		}
		return null;
	}
	
	@Override
	public RenderStyle getRenderStyle() {
		if (get3DRenderer() != null) {
			return get3DRenderer().getRenderStyle();
		}
		return null;
	}
	
	@Override
	public void setRenderStyle(RenderStyle style) {
		if (get3DRenderer() != null) {
			get3DRenderer().setRenderStyle(style);
		}
	}

	@Override
	public void setLogarithmEnabled(boolean enabled) {
		if (internalDataset != null) {
			internalDataset.setLogScale(enabled);
		}
		if (renderer != null) {
			renderer.setLogZscaling(enabled);
		}
	}

	@Override
	public boolean isLogarithmEnabled() {
		return renderer.getLogZscaling();
	}
	
	@Override
	public IHelpProvider getHelpProvider() {
		return new Surf3DHelpProvider();
	}
	
	@Override
	public ColorTheme getColorTheme() {
		return renderer.getColorTheme();
	}
	
	@Override
	public void setColorTheme(ColorTheme theme) {
		renderer.setColorTheme(theme);
		renderer.applyColorTheme();
	}
	
	@Override
	public void toggleOutsideBoxEnabled() {
		renderer.toggleOutsideBoxEnabled();
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

	@Override
	public void clearMarkers() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearDomainAxisMarkers() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearRangeAxisMarkers() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isTextInputEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setTextInputEnabled(boolean isTextInputEnabled) {
		// TODO Auto-generated method stub
		
	}
}
