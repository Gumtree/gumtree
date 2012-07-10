package org.freehep.j3d.plot;
import javax.media.j3d.IndexedLineArray;
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
/**
 * This is a base class for all PlotBuilders.
 * 
 * Note that all of its build* methods return BranchGraphs which can be added to a 3D display.
 * @author Joy Kyriakopulos (joyk@fnal.gov)
 * @version $Id: AbstractPlotBuilder.java 8584 2006-08-10 23:06:37Z duns $
 */
public abstract class AbstractPlotBuilder
{
	
	private Shape3D cubeShape;
	
	public Shape3D buildOutsideBox(Color3f color)
	{
		if (cubeShape == null) {
			cubeShape = new Shape3D();
			cubeShape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
		}
		// Allocate line array for wire-frame cube - 8 vertices, 24 coordinates (i.e. size of array)
		IndexedLineArray xCube = new IndexedLineArray(8, IndexedLineArray.COORDINATES | IndexedLineArray.COLOR_3
				| IndexedLineArray.ALLOW_COLOR_WRITE , 24);
		
		// Set coordinates for the cube //
		xCube.setCoordinate(0, new Point3d(-0.5,+0.5,0.0));
		xCube.setCoordinate(1, new Point3d(+0.5,+0.5,0.0));
		xCube.setCoordinate(2, new Point3d(+0.5,-0.5,0.0));
		xCube.setCoordinate(3, new Point3d(-0.5,-0.5,0.0));
		xCube.setCoordinate(4, new Point3d(-0.5,+0.5,1.0));
		xCube.setCoordinate(5, new Point3d(+0.5,+0.5,1.0));
		xCube.setCoordinate(6, new Point3d(+0.5,-0.5,1.0));
		xCube.setCoordinate(7, new Point3d(-0.5,-0.5,1.0));
	
		// Construct the vertical //
		xCube.setCoordinateIndex(0, 0);
		xCube.setCoordinateIndex(1, 1);
		xCube.setCoordinateIndex(2, 3);
		xCube.setCoordinateIndex(3, 2);
		xCube.setCoordinateIndex(4, 4);
		xCube.setCoordinateIndex(5, 5);
		xCube.setCoordinateIndex(6, 7);
		xCube.setCoordinateIndex(7, 6);

		// Construct the lower side //
		xCube.setCoordinateIndex(8, 0);
		xCube.setCoordinateIndex(9, 4);
		xCube.setCoordinateIndex(10, 4);
		xCube.setCoordinateIndex(11, 7);
		xCube.setCoordinateIndex(12, 7);
		xCube.setCoordinateIndex(13, 3);
		xCube.setCoordinateIndex(14, 3);
		xCube.setCoordinateIndex(15, 0);

		// Construct the upper side //
		xCube.setCoordinateIndex(16, 1);
		xCube.setCoordinateIndex(17, 5);
		xCube.setCoordinateIndex(18, 5);
		xCube.setCoordinateIndex(19, 6);
		xCube.setCoordinateIndex(20, 6);
		xCube.setCoordinateIndex(21, 2);
		xCube.setCoordinateIndex(22, 2);
		xCube.setCoordinateIndex(23, 1);
		
		xCube.setColor(0, color);
		cubeShape.setGeometry(xCube);
		return cubeShape;
	}

	public void updateOutsideBoxColor(Color3f color) {
		if (cubeShape == null) {
			cubeShape = new Shape3D();
			cubeShape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
		}
		// Allocate line array for wire-frame cube - 8 vertices, 24 coordinates (i.e. size of array)
		IndexedLineArray xCube = new IndexedLineArray(8, IndexedLineArray.COORDINATES | IndexedLineArray.COLOR_3
				| IndexedLineArray.ALLOW_COLOR_WRITE , 24);
		
		// Set coordinates for the cube //
		xCube.setCoordinate(0, new Point3d(-0.5,+0.5,0.0));
		xCube.setCoordinate(1, new Point3d(+0.5,+0.5,0.0));
		xCube.setCoordinate(2, new Point3d(+0.5,-0.5,0.0));
		xCube.setCoordinate(3, new Point3d(-0.5,-0.5,0.0));
		xCube.setCoordinate(4, new Point3d(-0.5,+0.5,1.0));
		xCube.setCoordinate(5, new Point3d(+0.5,+0.5,1.0));
		xCube.setCoordinate(6, new Point3d(+0.5,-0.5,1.0));
		xCube.setCoordinate(7, new Point3d(-0.5,-0.5,1.0));
	
		// Construct the vertical //
		xCube.setCoordinateIndex(0, 0);
		xCube.setCoordinateIndex(1, 1);
		xCube.setCoordinateIndex(2, 3);
		xCube.setCoordinateIndex(3, 2);
		xCube.setCoordinateIndex(4, 4);
		xCube.setCoordinateIndex(5, 5);
		xCube.setCoordinateIndex(6, 7);
		xCube.setCoordinateIndex(7, 6);

		// Construct the lower side //
		xCube.setCoordinateIndex(8, 0);
		xCube.setCoordinateIndex(9, 4);
		xCube.setCoordinateIndex(10, 4);
		xCube.setCoordinateIndex(11, 7);
		xCube.setCoordinateIndex(12, 7);
		xCube.setCoordinateIndex(13, 3);
		xCube.setCoordinateIndex(14, 3);
		xCube.setCoordinateIndex(15, 0);

		// Construct the upper side //
		xCube.setCoordinateIndex(16, 1);
		xCube.setCoordinateIndex(17, 5);
		xCube.setCoordinateIndex(18, 5);
		xCube.setCoordinateIndex(19, 6);
		xCube.setCoordinateIndex(20, 6);
		xCube.setCoordinateIndex(21, 2);
		xCube.setCoordinateIndex(22, 2);
		xCube.setCoordinateIndex(23, 1);
		
		xCube.setColor(0, color);
		cubeShape.setGeometry(xCube);
	}
	/**
	 * @param label 
	 * @param location 
	 * @param tickLocation Value from 0-1 for each tick location
	 * @param tickLabels Label for each tick mark
	 */
	public Node buildAxis(int location, String label, double[] tickLocation, String[] tickLabels)
	{
		return null;
	}
	
	public abstract void updatePlot(NormalizedBinned2DData data);
}

