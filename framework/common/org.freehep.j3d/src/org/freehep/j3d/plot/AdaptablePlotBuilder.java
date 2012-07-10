/**
 * 
 */
package org.freehep.j3d.plot;

import java.awt.Color;

import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Geometry;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Switch;
import javax.vecmath.Color3b;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.geometry.Stripifier;

/**
 * @author nxi
 *
 */
public class AdaptablePlotBuilder extends AbstractPlotBuilder {

	private static final Color3b grey = new Color3b(Color.black);

	private static final Vector3f xnormal = new Vector3f(1f,0,0);
	private static final Vector3f xabnormal = new Vector3f(-1f,0,0);
	private static final Vector3f ynormal = new Vector3f(0,1f,0);
	private static final Vector3f yabnormal = new Vector3f(0,-1f,0);
	private static final Vector3f znormal = new Vector3f(0,0,1f);
	private static final Vector3f zabnormal = new Vector3f(0,0,-1f);
	private static final int fullPlotChild = 0;
	private static final int sparsePlotChild = 1;

	private boolean linesWhileAnim = true; 
	private int numWireLines = 600;

	private int bcur;
	private QuadArray quad;
	private LineArray line;
	private Shape3D shape;
	private Shape3D lineShape;
	private Switch targetSwitch;
	private int popBins = 0;		// number of populated bins in lego

	private RenderStyle style;
	
	
	public Node buildContent(NormalizedBinned2DData data)
	{
		shape = new Shape3D();
		shape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
		shape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		shape.setAppearance(createAppearance());
		shape.setGeometry(buildGeometry(data));

		targetSwitch = new Switch();
		targetSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);

		lineShape = createLineShape();
		lineShape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);

		targetSwitch.addChild(shape);       // full plot should be added first
		targetSwitch.addChild(lineShape);   // sparse "wire-frame" added second

		//If necessary, create geometry for a sparsified plot
		if (linesWhileAnim && (popBins > numWireLines/2)) {  // if too many bins populated
			lineShape.setGeometry(buildWireGeometry(data));  // build alternate view
			targetSwitch.setUserData(Boolean.TRUE);
		}
		else
			targetSwitch.setUserData(Boolean.FALSE);

		targetSwitch.setWhichChild(fullPlotChild);	// full plot displayed by default                
		return targetSwitch;
	}
		
	private Appearance createAppearance() {
		switch (getRenderStyle()) {
		case THREAD:
			return createWireFrameAppearance();
		default:
			return createMaterialAppearance();
		}
	}
	
	private Appearance createMaterialAppearance() 
	{
        Appearance materialAppear = new Appearance();
        PolygonAttributes polyAttrib = new PolygonAttributes();
        polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
        materialAppear.setPolygonAttributes(polyAttrib);

        Material material = new Material();
        // set diffuse color to red (this color will only be used 
        //     if lighting disabled - per-vertex color overrides)
        material.setDiffuseColor(new Color3f(1.0f, 0.0f, 0.0f));
        materialAppear.setMaterial(material);

        return materialAppear;
    }
	
	private Shape3D createLineShape()
	{
		Shape3D lineShape = new Shape3D();
		lineShape.setAppearance(createWireFrameAppearance());

		return lineShape;
	}
	
	Appearance createWireFrameAppearance()
	{
		Appearance materialAppear = new Appearance();
		LineAttributes lineAttrib = new LineAttributes();
		materialAppear.setLineAttributes(lineAttrib);
		ColoringAttributes redColoring = new ColoringAttributes();
		redColoring.setColor(1.0f, 0.0f, 0.0f);
		materialAppear.setColoringAttributes(redColoring);

		return materialAppear;
	}
	/* (non-Javadoc)
	 * @see org.freehep.j3d.plot.AbstractPlotBuilder#updatePlot(org.freehep.j3d.plot.NormalizedBinned2DData)
	 */
	@Override
	public void updatePlot(NormalizedBinned2DData data) {
		switch (getRenderStyle()) {
		case LEGO:
			updateLegoPlot(data);
			break;
		case SURFACE:
			updateSurfacePlot(data);
			break;
		case THREAD:
			updateThreadPlot(data);
			break;
		default:
			break;
		}
	}

	public void updateSurfacePlot(NormalizedBinned2DData data) {
		shape.setGeometry(buildGeometry(data));
	}
	
	private Geometry buildGeometry(NormalizedBinned2DData data) {
		switch (getRenderStyle()) {
		case SURFACE:
			return buildSurfaceGeometry(data);
		default:
			return buildLegoGeometry(data);
		}
	}
	
	private Geometry buildSurfaceGeometry(NormalizedBinned2DData data) {
		
		int nXbins = data.xBins();
		int nYbins = data.yBins();
		float xBinWidth = 1.f/nXbins;
		float yBinWidth = 1.f/nYbins;
		float x, y, z;
		int i, j, k, l;

		Point3d bcoord[] = new Point3d[((int)nXbins-1)*((int)nYbins-1)*4 ];
		Color3b bcolor[] = new Color3b[((int)nXbins-1)*((int)nYbins-1)*4 ];

		for(i=0;i<((int)nXbins-1)*((int)nYbins-1)*4;i++) {
			bcoord[i] = new Point3d();
			bcolor[i] = new Color3b();
		}
		
		// System.out.println(" ok");
			
		// Fill bcoord array with points that compose the surface
		int bcur=0;
		for (k=0, x=-.5f; k<(int)nXbins-1; k++, x+=xBinWidth) 
			for(l=0, y = -.5f; l<(int)nYbins-1; l++, y+=yBinWidth) {
				
				// Point x,y
				bcoord[bcur].x = x+xBinWidth/2.f;
				bcoord[bcur].y = y+yBinWidth/2.f;
				bcoord[bcur].z = data.zAt(k,l);
				bcolor[bcur] = data.colorAt(k,l);
				bcur++;
				
				// Next point in y direction
				bcoord[bcur].x = x+xBinWidth/2.f;
				bcoord[bcur].y = y+1.5f*xBinWidth;
				bcoord[bcur].z = data.zAt(k,l+1);
				bcolor[bcur] = data.colorAt(k,l+1);
				bcur++;
				
				// Next point diagonally
				bcoord[bcur].x = x+1.5f*xBinWidth;
				bcoord[bcur].y = y+1.5f*yBinWidth;
				bcoord[bcur].z = data.zAt(k+1,l+1);
				bcolor[bcur] = data.colorAt(k+1,l+1);
				bcur++;
				
				// Next point in x direction
				bcoord[bcur].x = x+1.5f*xBinWidth;
				bcoord[bcur].y = y+yBinWidth/2.f;
				bcoord[bcur].z = data.zAt(k+1,l);
				bcolor[bcur] = data.colorAt(k+1,l);
				bcur++;
			}
		
		// System.out.print("debug3:");
		
		// We make a GeometryInfo object so that normals can be generated
		// for us and geometry stripified for us.  J3D documentation
		// says it's best to do these two steps in this order.
		
		GeometryInfo geom = new GeometryInfo(GeometryInfo.QUAD_ARRAY);
		
		//geom.setNormals(normals);
		geom.setCoordinates(bcoord);
		geom.setColors(bcolor);
	    NormalGenerator ng = new NormalGenerator();
		ng.generateNormals(geom);
		
		// Make normals conform to our "handedness" of z direction
		// i.e. change their sign
		Vector3f normals[] = geom.getNormals();
		for (i=0; i< normals.length; ++i) {
			normals[i].x = -normals[i].x;
			normals[i].y = -normals[i].y;
			normals[i].z = -normals[i].z;
		}
		geom.setNormals(normals);
		
		Stripifier st = new Stripifier();
		st.stripify(geom);
		geom.recomputeIndices();

		return geom.getGeometryArray();
	}
	
	public void updateLegoPlot(NormalizedBinned2DData data)
	{
		// Create geometry for full plot
		shape.setAppearance(createMaterialAppearance());
		shape.setGeometry(buildGeometry(data));

		//If necessary, create geometry for a sparsified plot
		// System.out.println("in updatePlot: linesWhileAnim = "+linesWhileAnim+", popBins = "+popBins+", numWireLines = "+numWireLines+", shapeIsLego = "+shapeIsLego);
		if (linesWhileAnim && popBins > numWireLines/2) {   // if too many bins populated
			lineShape.setGeometry(buildWireGeometry(data)); // build alternate view
			targetSwitch.setUserData(Boolean.TRUE);	    // flags whether there is line content
		} else
			targetSwitch.setUserData(Boolean.FALSE);	// no line content to display
	}
	
	public void updateThreadPlot(NormalizedBinned2DData data) {
		popBins = calcPopBins(data);        // buildWireGeometry won't calculate # populated bins
		int[] binInc = {1,1};
		shape.setGeometry(buildWireGeometry(data,binInc));

		//If necessary, create geometry for a sparsified plot
		// System.out.println("in updatePlot: linesWhileAnim = "+linesWhileAnim+", popBins = "+popBins+", numWireLines = "+numWireLines+", shapeIsLego = "+shapeIsLego);
		if (linesWhileAnim && popBins > numWireLines/2) {   // if too many bins populated
			lineShape.setGeometry(buildWireGeometry(data)); // build alternate view
			targetSwitch.setUserData(Boolean.TRUE);	    // flags whether there is line content
		}
		else
			targetSwitch.setUserData(Boolean.FALSE);	// no line content to display
	}
	
	private Geometry buildWireGeometry(NormalizedBinned2DData data)
	{
		// Compute x,y factors: will be > 1 if data is too large and
		// plot needs to be sparsified (i.e. made more sparse)
		int[] wireBinInc = calcXYfactors(data.xBins(), data.yBins());
		return buildWireGeometry(data, wireBinInc);
	}

	private Geometry buildWireGeometry(NormalizedBinned2DData data, int[] wireBinInc)
	{

		int nXbins = data.xBins();
		int nYbins = data.yBins();

		// Create the coordinate and color arrays

		bcur = 0;
		int maxpoints = ((nXbins/wireBinInc[0] + 1) * (nYbins/wireBinInc[1] + 1)) * 3 * 2 + 8; // An over estimate
		line = new LineArray(maxpoints,LineArray.COORDINATES+LineArray.NORMALS+LineArray.COLOR_3);

		float xBinWidth = (float)wireBinInc[0]/(float)(nXbins);
		float yBinWidth = (float)wireBinInc[1]/(float)(nYbins);;
		float x = - .5f;

		// Go through data, drawing a vertical line instead of a bin

		for (int k=0; k < nXbins; k+=wireBinInc[0], x += xBinWidth)
		{
			float y = - .5f;
			for (int l=0; l < nYbins; l+=wireBinInc[1], y += yBinWidth)
			{
				float z = data.zAt(k,l);
				Color3b curColor = data.colorAt(k,l);
				if (z != 0) {   // skip drawing if no height
					drawVLine(x+xBinWidth/2.f, y+yBinWidth/2.f, 0.f, z, curColor, xnormal);
				}
			}
		}
		return line;
	}
	
	private Geometry buildLegoGeometry(NormalizedBinned2DData data)
	{
		int nXbins = data.xBins();
		int nYbins = data.yBins();

		// Create the coordinate and color arrays

		bcur = 0;
		//		int maxpoints = (nXbins+1) * (nYbins+1) * 4 * 3 * 4; // An over estimate
		int maxpoints = (nXbins+1) * (nYbins+1) * 4 * 3 * 4; // An over estimate
		quad = new QuadArray(maxpoints,QuadArray.COORDINATES+QuadArray.NORMALS+QuadArray.COLOR_3);

		// Create the floor

		drawXYrect(-.5f,-.5f,0,.5f,.5f,0,grey,znormal);

		float xBinWidth = 1.f/nXbins;
		float yBinWidth = 1.f/nYbins;
		float x = -xBinWidth - .5f;
		float xBinWAdj = 1.f/nXbins/100.f;
		float yBinWAdj = 1.f/nYbins/100.f;

		popBins = 0;

		// Note, we start a bin -1 on the X and Y axis. The getDataAt method always
		// returns 0 for elements outside of the legal bin range, so this, coupled with
		// the fact that we never draw the tops of bins which have z=0, takes care of all
		// the edge effects.

		for (int k=-1; k < nXbins; k++, x += xBinWidth)
		{
			float y = -yBinWidth - .5f;
			for(int l=-1; l < nYbins; l++, y += yBinWidth)
			{
				float z = data.zAt(k,l);
				Color3b curColor = data.colorAt(k,l);

				// Construct colored, horizontal/top side of lego on the data point
				//                                          (X-Y plane at constant z)

				if (z != 0)    // skip drawing top if no height
				{
					++popBins;			// keep track of how many bins > 0
					drawXYrect(x+xBinWidth-xBinWAdj, y+yBinWidth-yBinWAdj, z,
							x,                    y,                    z,
							curColor, znormal);
					// color bottom of bin (visible when top is scaled off & clipped)
					drawXYrect(x+xBinWidth-xBinWAdj, y+yBinWidth-yBinWAdj, .001f,
							x,                    y,                    .001f,
							curColor, znormal);
				}

				// Construct sides between this and next Y bin
				float nextZ = data.zAt(k,l+1);
				Color3b nextColor =  data.colorAt(k,l+1);
				if (z != 0)	{    // side of current bin - skip drawing if no height
					drawXZrect(x,                    y+yBinWidth-yBinWAdj/2.f, z,
							x+xBinWidth-xBinWAdj, y+yBinWidth-yBinWAdj/2.f, 0,
							curColor, ynormal);
					drawXZrect(x,                    y+yBinWidth-yBinWAdj, z,
							x+xBinWidth-xBinWAdj, y+yBinWidth-yBinWAdj, 0,
							curColor, yabnormal);  // inside - seen only if top clipped
				}
				if (nextZ != 0)	{ // side of next bin - skip drawing if no height
					drawXZrect(x,           y+yBinWidth, 0,
							x+xBinWidth, y+yBinWidth, nextZ,
							nextColor, yabnormal);
					drawXZrect(x,           y+yBinWidth+yBinWAdj, 0,
							x+xBinWidth, y+yBinWidth+yBinWAdj, nextZ,
							nextColor, ynormal);  // inside - seen only if top clipped
				}

				// Construct sides between this and next X bin
				nextZ = data.zAt(k+1,l);
				nextColor = data.colorAt(k+1,l);
				if (z != 0)	 {    // side of current bin - skip drawing if no height
					drawYZrect(x+xBinWidth-xBinWAdj/2.f, y,                    z,
							x+xBinWidth-xBinWAdj/2.f, y+yBinWidth-yBinWAdj, 0,
							curColor, xnormal);
					drawYZrect(x+xBinWidth-xBinWAdj, y,                    z,
							x+xBinWidth-xBinWAdj, y+yBinWidth-yBinWAdj, 0,
							curColor, xabnormal);  // inside - seen only if top clipped
				}
				if (nextZ != 0)	 { // side of next bin - skip drawing if no height
					drawYZrect(x+xBinWidth, y,           0,
							x+xBinWidth, y+yBinWidth, nextZ,
							nextColor, xabnormal);
					drawYZrect(x+xBinWidth+xBinWAdj, y,           0,
							x+xBinWidth+xBinWAdj, y+yBinWidth, nextZ,
							nextColor, xnormal);   // inside - seen only if top clipped
				}
			}
		}
		return quad;
	}
	
	private int[] calcXYfactors(int nXbins, int nYbins)
	{
		int[] binInc = {1,1};
		if (nXbins * nYbins > numWireLines) {
			double xyBinRatio = (double) nXbins / (double)nYbins;
			binInc[0] = (int) (nXbins / (Math.sqrt((double)numWireLines) / xyBinRatio));
			binInc[1] = (int) (nYbins / (Math.sqrt((double)numWireLines) * xyBinRatio));
			if (binInc[0] < 1) binInc[0] = 1;
			if (binInc[1] < 1) binInc[1] = 1;
		}
		// System.out.println("in calcXYfactors: " + binInc[0] + " " + binInc[1]);
		return binInc;
	}

	// calcPopBins - method to calculate the number of bins that are != zero in the
	//               normalized data.  (This routine is called when the lego will not be built.
	//               Otherwise the lego sets popBins while it's building its geometry.)
	private int calcPopBins(Data3D data)
	{
		int nXbins = data.xBins();
		int nYbins = data.yBins();
		int numPopBins = 0;
		float x = -1.f/nXbins - .5f;
		for (int k=-1; k < nXbins; k++, x += 1.f/nXbins)
		{
			float y = -1.f/nYbins - .5f;
			for (int l=-1; l < nYbins; l++, y += 1.f/nYbins)
			{
				float z = data.zAt(k,l);
				if (z != 0)
					++numPopBins;			// keep track of how many bins != 0
			}
		}
		return numPopBins;
	}
	
	// Construct colored line
	private void drawLine(float x1, float y1, float z1,
			float x2, float y2, float z2,
			Color3b lineColor, Vector3f normal)
	{
		line.setCoordinate(bcur,new Point3f(x1,y1,z1));
		line.setColor(bcur,lineColor);
		line.setNormal(bcur,normal);
		bcur++;

		line.setCoordinate(bcur,new Point3f(x2,y2,z2));
		line.setColor(bcur,lineColor);
		line.setNormal(bcur,normal);
		bcur++;

	}

	// Construct colored, vertical line at constant X,Y
	private void drawVLine(float x,  float y, float z1, float z2,
			Color3b lineColor, Vector3f normal)
	{
		line.setCoordinate(bcur,new Point3f(x,y,z1));
		line.setColor(bcur,lineColor);
		line.setNormal(bcur,normal);
		bcur++;

		line.setCoordinate(bcur,new Point3f(x,y,z2));
		line.setColor(bcur,lineColor);
		line.setNormal(bcur,normal);
		bcur++;

	}

	// Construct colored, horizontal rectangle in the X-Y plane at constant Z
	private void drawXYrect(float x1,  float y1, float z1,
			float x2,  float y2, float z2,
			Color3b rectColor, Vector3f normal)
	{
		quad.setCoordinate(bcur,new Point3f(x1,y1,z1));
		quad.setColor(bcur,rectColor);
		quad.setNormal(bcur,normal);
		bcur++;

		quad.setCoordinate(bcur,new Point3f(x1,y2,z1));
		quad.setColor(bcur,rectColor);
		quad.setNormal(bcur,normal);
		bcur++;

		quad.setCoordinate(bcur,new Point3f(x2,y2,z1));
		quad.setColor(bcur,rectColor);
		quad.setNormal(bcur,normal);
		bcur++;

		quad.setCoordinate(bcur,new Point3f(x2,y1,z1));
		quad.setColor(bcur,rectColor);
		quad.setNormal(bcur,normal);
		bcur++;
	}
	// Construct colored, vertical rectangle in the X-Z plane at constant Y
	private void drawXZrect(float x1,  float y1, float z1,
			float x2,  float y2, float z2,
			Color3b rectColor, Vector3f normal)
	{
		quad.setCoordinate(bcur,new Point3f(x1,y1,z1));
		quad.setColor(bcur,rectColor);
		quad.setNormal(bcur,normal);
		bcur++;

		quad.setCoordinate(bcur,new Point3f(x2,y1,z1));
		quad.setColor(bcur,rectColor);
		quad.setNormal(bcur,normal);
		bcur++;

		quad.setCoordinate(bcur,new Point3f(x2,y1,z2));
		quad.setColor(bcur,rectColor);
		quad.setNormal(bcur,normal);
		bcur++;

		quad.setCoordinate(bcur,new Point3f(x1,y1,z2));
		quad.setColor(bcur,rectColor);
		quad.setNormal(bcur,normal);
		bcur++;
	}

	// Construct colored, vertical rectangle in the Y-Z plane at constant X
	private void drawYZrect(float x1,  float y1, float z1,
			float x2,  float y2, float z2,
			Color3b rectColor, Vector3f normal)
	{
		quad.setCoordinate(bcur,new Point3f(x1,y1,z1));
		quad.setColor(bcur,rectColor);
		quad.setNormal(bcur,normal);
		bcur++;

		quad.setCoordinate(bcur,new Point3f(x1,y2,z1));
		quad.setColor(bcur,rectColor);
		quad.setNormal(bcur,normal);
		bcur++;

		quad.setCoordinate(bcur,new Point3f(x1,y2,z2));
		quad.setColor(bcur,rectColor);
		quad.setNormal(bcur,normal);
		bcur++;

		quad.setCoordinate(bcur,new Point3f(x1,y1,z2));
		quad.setColor(bcur,rectColor);
		quad.setNormal(bcur,normal);
		bcur++;
	}

	/**
	 * @return the style
	 */
	public RenderStyle getRenderStyle() {
		return style;
	}

	/**
	 * @param style the style to set
	 */
	public void setRenderStyle(RenderStyle style) {
		this.style = style;
	}
	
	public int getNumWireLines()
	{
		return numWireLines;
	}

	public void setNumWireLines(int val)
	{
		numWireLines = val;
	}
	public boolean getLinesWhileAnim()
	{
		return linesWhileAnim;
	}

	public void setLinesWhileAnim(boolean b)
	{
		linesWhileAnim = b;
	}

}
