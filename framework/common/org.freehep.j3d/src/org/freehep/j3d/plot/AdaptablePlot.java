package org.freehep.j3d.plot;

import javax.media.j3d.Background;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Switch;
import javax.vecmath.Color3f;



/**
 * A simple convenience class that end users can pop into their GUI to produce a
 * lego plot.
 *
 * Warning: LegoPlot extends Canvas3D and thus is a heavyweight object.
 * @author Joy Kyriakopulos (joyk@fnal.gov)
 * @version $Id: LegoPlot.java 8584 2006-08-10 23:06:37Z duns $
 */

public class AdaptablePlot extends Plot3D
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7384016913022983053L;
	private static final Color3f WHITE_COLOR = new Color3f(1f, 1f, 1f);
	private static final Color3f GRAY_COLOR = new Color3f((float) 237/255, (float) 235/255, (float) 219/255);
	private static final Color3f BLACK_COLOR = new Color3f(0f, 0f, 0f);
	private static final ColorTheme DEFAULT_COLOR_THEME = ColorTheme.BLACK;
	private Binned2DData data;
	private AdaptablePlotBuilder builder;
	private Node plot;
	private Shape3D box;
	private Background background;
	private boolean logZscaling = false;
	private boolean linesWhileAnim = true;
	private int sparsifyThreshold = 600;
	private AxisBuilder xAxis;
	private AxisBuilder yAxis;
	private ZAxisBuilder zAxis;
	private BranchGroup bg;
	private BranchGroup axes;
	private String xAxisLabel = "X Axis";
	private String yAxisLabel = "Y Axis";
	private String zAxisLabel = "Z Axis";
	private double xmin;
	private double xmax;
	private double ymin;
	private double ymax;
	private double zmin;
	private double zmax;
	private RenderStyle style = RenderStyle.LEGO;
	private ColorTheme colorTheme = DEFAULT_COLOR_THEME;

	public AdaptablePlot()
	{
		super();
	}
	public void setData(Data3D data3D)
	{
		this.data = (Binned2DData) data3D;
		init();
		if (init) {
			if (data.xMin() != xmin || data.xMax() != xmax) {
				xmin = data.xMin();
				xmax = data.xMax();
				xAxis.createLabelsNTicks(xmin, xmax);
				xAxis.apply();
			}
			if (data.yMin() != ymin || data.yMax() != ymax) {
				ymin = data.yMin();
				ymax = data.yMax();
				yAxis.createLabelsNTicks(ymin, ymax);
				yAxis.apply();
			}
			if (data.zMin() != zmin || data.zMax() != zmax) {
				zmin = data.zMin();
				zmax = data.zMax();
				zAxis.createLabelsNTicks(zmin, zmax, logZscaling);
				zAxis.apply();
			}
			if (logZscaling)
				builder.updatePlot(new NormalizedBinned2DLogData(data));
			else
				builder.updatePlot(new NormalizedBinned2DData(data));
		}
	}

	public boolean getLogZscaling()
	{
		return logZscaling;
	}

	public void setLogZscaling(boolean b)
	{
		// System.out.println("setting Log Scaling to: " + b + " from: " + logZscaling);
		if (logZscaling != b) {
			logZscaling = b;
			if (data != null) {
				zmin = data.zMin();
				zmax = data.zMax();
				zAxis.createLabelsNTicks(zmin, zmax, logZscaling);
				zAxis.apply();
//				if (logZscaling)
//					builder.updatePlot(new NormalizedBinned2DLogData(data));
//				else
//					builder.updatePlot(new NormalizedBinned2DData(data));
			}
		}
	}

	@Override
	public void toggleOutsideBoxEnabled() {
			if (((Group) getPlot()).indexOfChild(bg) < 0) {
				if (((Group) getPlot()).indexOfChild(axes) < 0) {
					((Group) getPlot()).addChild(axes);
					((Group) getPlot()).addChild(bg);
				} else {
					((Group) getPlot()).removeChild(axes);
				}
			} else {
				((Group) getPlot()).removeChild(bg);
			}
	}

	public boolean getLinesWhileAnim()
	{
		return linesWhileAnim;
	}

	public void setLinesWhileAnim(boolean b)
	{
		if (linesWhileAnim != b) {
			linesWhileAnim = b;
			if (builder != null) {
				builder.setLinesWhileAnim(b);
				if (data != null) {
					if (logZscaling)
						builder.updatePlot(new NormalizedBinned2DLogData(data));
					else
						builder.updatePlot(new NormalizedBinned2DData(data));
				}
			}
		}
	}

	public int getSparsifyThreshold()
	{
		return sparsifyThreshold;
	}

	public void setSparsifyThreshold(int s)
	{
		// The sparsify threshold is a limit on the number of bins that should be drawn when animating plot
		// The lego builder's numWiredLines is the target number of bins that it will draw during animations
		if (sparsifyThreshold != s) {
			if (s > 1) {
				sparsifyThreshold = s;
				if (builder != null) {
					builder.setNumWireLines(s);   // Builder will sparsify when numWiredLines > # populated Bins
					if (data != null) {
						if (logZscaling)
							builder.updatePlot(new NormalizedBinned2DLogData(data));
						else
							builder.updatePlot(new NormalizedBinned2DData(data));
					}

				}
			}
			else
				System.out.println("LegoPlot: Invalid SparsifyThreshold: " + s);
		}
	}

	public String getXAxisLabel()
	{
		return xAxisLabel;
	}

	public void setXAxisLabel(String s)
	{
		xAxisLabel = s;
		xAxis.setLabel(s);
		xAxis.apply();
	}

	public String getYAxisLabel()
	{
		return yAxisLabel;
	}

	public void setYAxisLabel(String s)
	{
		yAxisLabel = s;
		yAxis.setLabel(s);
		yAxis.apply();
	}

	public String getZAxisLabel()
	{
		return zAxisLabel;
	}

	public void setZAxisLabel(String s)
	{
		zAxisLabel = s;
		zAxis.setLabel(s);
		zAxis.apply();
	}

	protected Node createPlot()
	{
		Color3f forgroundColor;
		Color3f backgroundColor;
		if (colorTheme == ColorTheme.WHITE) {
			backgroundColor = WHITE_COLOR;
			forgroundColor = BLACK_COLOR;
		} else if (colorTheme == ColorTheme.GRAY) {
			backgroundColor = GRAY_COLOR;
			forgroundColor = BLACK_COLOR;
		} else {
			backgroundColor = BLACK_COLOR;
			forgroundColor = WHITE_COLOR;
		}
		builder = new AdaptablePlotBuilder();
		builder.setRenderStyle(getRenderStyle());
		builder.setNumWireLines(sparsifyThreshold);  // Builder will sparsify when numWiredLines/2 > # populated Bins
		builder.setLinesWhileAnim(linesWhileAnim);
		box = builder.buildOutsideBox(forgroundColor);
		if (logZscaling)
			plot = builder.buildContent(new NormalizedBinned2DLogData(data));
		else
			plot = builder.buildContent(new NormalizedBinned2DData(data));
		double[] tick = {0,.1,.2,.3,.4,.5,.6,.7,.8,.9,1.0};
		String[] labels = {"0.0","0.2","0.4","0.6","0.8","1.0" };
		xAxis = new XAxisBuilder(xAxisLabel,labels,tick, forgroundColor);
		yAxis = new YAxisBuilder(yAxisLabel,labels,tick, forgroundColor);
		zAxis = new ZAxisBuilder(zAxisLabel,labels,tick, forgroundColor);
		xAxis.createLabelsNTicks(data.xMin(), data.xMax());
		yAxis.createLabelsNTicks(data.yMin(), data.yMax());
		zAxis.createLabelsNTicks(data.zMin(), data.zMax(), logZscaling);
		xAxis.apply();
		yAxis.apply();
		zAxis.apply();

		background = new Background();
	    background.setColor(new Color3f(backgroundColor));
	    background.setCapability(Background.ALLOW_COLOR_WRITE);
	    background.setApplicationBounds(getDefaultBounds());
	    
//	    ColoringAttributes caL1 = new ColoringAttributes();
//	    caL1.setColor(new Color3f(1.0f, 0.0f, 0.0f));
//	    Appearance appL1 = new Appearance();
//	    appL1.setColoringAttributes(caL1);
	    
		Group g = new Group();
		g.setCapability(Group.ALLOW_CHILDREN_WRITE);
		g.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		g.addChild(background);
		bg = new BranchGroup();
		bg.setCapability(BranchGroup.ALLOW_DETACH);
		bg.addChild(box);
		axes = new BranchGroup();
		axes.setCapability(BranchGroup.ALLOW_DETACH);
		axes.addChild(xAxis.getNode());
		axes.addChild(yAxis.getNode());
		axes.addChild(zAxis.getNode());
		g.addChild(bg);
		g.addChild(axes);
		g.addChild(plot);
		return g;
	}

	public void setColorTheme(ColorTheme colorTheme) {
		this.colorTheme = colorTheme;
	}
	
	public void applyColorTheme() {
		Color3f forgroundColor;
		Color3f backgroundColor;
		if (colorTheme == ColorTheme.WHITE) {
			backgroundColor = WHITE_COLOR;
			forgroundColor = BLACK_COLOR;
		} else if (colorTheme == ColorTheme.GRAY) {
			backgroundColor = GRAY_COLOR;
			forgroundColor = BLACK_COLOR;
		} else {
			backgroundColor = BLACK_COLOR;
			forgroundColor = WHITE_COLOR;
		}
		if (background != null) {
			background.setColor(backgroundColor);
		}
		if (box != null) {
			builder.updateOutsideBoxColor(forgroundColor);
		}
		if (xAxis != null) {
			xAxis.setForgroundColor(forgroundColor);
			xAxis.apply();
		}
		if (yAxis != null) {
			yAxis.setForgroundColor(forgroundColor);
			yAxis.apply();
		}
		if (zAxis != null) {
			zAxis.setForgroundColor(forgroundColor);
			zAxis.apply();
		}
	}
	
	// Add behavior to switch to "wire-frame" display when mouse is pressed
	//			by overriding defineMouseBehavior
	protected BranchGroup defineMouseBehaviour(Node scene)
	{
		BranchGroup bg = super.defineMouseBehaviour(scene);
		Bounds bounds = getDefaultBounds();

		// change this to be switch node below i.e. not scene
		if (plot instanceof Switch) {
			Switch sw = (Switch)plot;
			MouseDownUpBehavior mouseDnUp = new MouseDownUpBehavior(bounds, sw);
			bg.addChild(mouseDnUp);
		}

		return bg;
	}
	
	@Override
	public void cleanUp() {
		super.cleanUp();
		data = null;
		builder = null;
		plot = null;
		xAxis = null;
		yAxis = null;
		zAxis = null;
	}
	
	@Override
	public AbstractPlotBuilder getBuilder() {
		return builder;
	}
	
	@Override
	public void setRenderStyle(RenderStyle style) {
		if (this.style != style) {
			this.style = style;
			if (builder != null) {
				builder.setRenderStyle(style);
//				if (data != null) {
//					if (logZscaling)
//						builder.updatePlot(new NormalizedBinned2DLogData(data));
//					else
//						builder.updatePlot(new NormalizedBinned2DData(data));
//				}
			}
		}
	}
	
	@Override
	public RenderStyle getRenderStyle() {
		return style;
	}
	
//	@Override
//	public Image createImage(int width, int height) {
//		Image image =  super.createImage(width, height);
//		
//		return image;
//	}
	
	@Override
	public void repaint(Binned2DData data) {
		if (builder != null) {
			if (data != null) {
				if (logZscaling)
					builder.updatePlot(new NormalizedBinned2DLogData(data));
				else
					builder.updatePlot(new NormalizedBinned2DData(data));
			}
		}
	}
	
	@Override
	public ColorTheme getColorTheme() {
		return colorTheme;
	}
}

