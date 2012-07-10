/*******************************************************************************
 * Copyright (c) 2004  Australian Nuclear Science and Technology Organisation.
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * GumTree Platform is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * Contributors:
 *  Bragg Institute - initial API and implementation
 *******************************************************************************/

package au.gov.ansto.bragg.common.dra.algolib.math;

/**
 * A utility class used to make geometry correction for some detectors. 
 * Keeps track of the detector distance, pixel width,
 * detector curvature etc.
 * @author jgw  Modified math formula for geometry correction.
 * @author hrz
 */
public class GeometryCorrecter {

	private double distance;
	private double cpx;
	private double cpy;
	private double cdx;
	private double cdy;
	private boolean curveX;
	private boolean curveY;
	private double pdx;
	private double pdy;
	private double lambda;
	/**
	 * Creates a new GeometryCorrecter
	 * @param dist The detector distance to the Sample.
	 * @param centerPos The position of the center in pixel coordinates.
	 * @param centerDist The position of the center in world coordiantes.
	 * @param curvedX If true, the detector is curved in the X direction.
	 * @param curvedY If true, the detector is curved in the Y direction.
	 * @param pixelDist The distance or angle between pixels.
	 * @param wavelength The wavelength.
	 */
	public GeometryCorrecter(double dist, FPoint centerPos, FPoint centerDist,
			boolean curvedX, boolean curvedY,
			FPoint pixelDist, double wavelength) {
		distance = dist;
		
		cpx = centerPos.x;
		cpy = centerPos.y;
		cdx = centerDist.x;
		cdy = centerDist.y;

		curveX = curvedX;
		curveY = curvedY;
		pdx = pixelDist.x;
		pdy = pixelDist.y;
		
		lambda = wavelength;
	}
	/**
	 * Creates a simple sample image based on the correction
	 * @param x The width of the image.
	 * @param y The height of the image.
	 * @return The sample image. An array with dimensions [y][x]
	 */
	public double[][] sampleImage(int x, int y)
	{
		double[][] out = new double[y][x];
		for(int i =0; i < x; i++)
		{
			for(int j = 0; j < y; j++)
			{
				out[j][i] = getAngle2theta(i,j);
			}
		}
		return out;
	}
	
	private static boolean printing = false;
	/**
	 * Returns the angles corresponding to a set of pixel
	 * coordinates.
	 * @param x The x pixel coordinate.
	 * @param y The y pixel coordinate.
	 * @param z The z pixel coordinate.
	 * @return ThetaX and ThetaY of the point.
	 */
	public FPoint flatDetCorrect(double x, double y)
	{
		double xPos = 0, yPos = 0, zPos = distance;
		double xAngle, yAngle;
		double yFactor;
		if(curveY)
		{
			yFactor = Math.cos(y*pdy+cpy);
		}
		else
		{
			yFactor = 1;
		}
		if(curveX)
		{
			xPos = distance*Math.sin(x * pdx + cpx) * yFactor;
			zPos = distance*Math.cos(x * pdx + cpx) * yFactor;
		}
		else
		{
			xPos =  ((x * pdx + cdx)*Math.cos(cpx)+
					        distance*Math.sin(cpx)) * yFactor;
			zPos =  (-(x * pdx + cdx)*Math.sin(cpx)+
			        distance*Math.cos(cpx)) * yFactor;
		}
		if(curveY)
		{
			yPos = distance * Math.sin(y*pdy + cpy);
		}
		else
		{
			yPos = (y*pdy)*Math.cos(cpy) + cdy;
		}
		if(printing)
		System.out.println("("+xPos+","+yPos+","+zPos+")");
		xAngle = Math.atan2(xPos,zPos);
		yAngle = Math.atan2(yPos,Math.sqrt(xPos*xPos+zPos*zPos));
		
		return new FPoint((double)xAngle,(double)yAngle);
	}
	/**
	 * Calculates qx and qy for a point.
	 * @param x The x pixel coordiante.
	 * @param y The y pixel coordinate.
	 * @return A FPoint with p.x = qx and p.y = qy;
	 */
	public FPoint qxqy(double x, double y)
	{
		FPoint p = flatDetCorrect(x,y);
		p.x = (double)(2 * Math.PI * Math.sin(p.x) / lambda);
		p.y = (double)(2 * Math.PI * Math.sin(p.y) / lambda);
		return p;
	}
	/**
	 * Calculates the overall value of q at a point.
	 * @param x The x pixel coordinate.
	 * @param y The y pixel coordinate.
	 * @return The value of q.
	 */
	public double q(double x, double y)
	{
		return (double)(2 * Math.PI * Math.sin(getAngle2theta(x,y)) / lambda);
	}
	
	/**
	 * Calculates the total angle to a point.
	 * Designed for HRPD and HIPD curved detector
	 * @param x pTheta, twoTheta for detector position in radian,
	 * @param y The y pixel coordinate.
	 * @return The total angle.
	 */

	public double getAngle2theta(double pTheta, double y )
	{
		double xPos = 0, yPos = 0, zPos = distance;
		double yFactor;
		double xFactor;
		double x = pTheta;    //in radian
// zPos = distance is distance from sample to detectors.
		if(curveY)
		{
			yFactor = Math.cos(y*pdy+cpy);
		}
		else
		{
			yFactor = y - cpy ;
		}
		if(curveX)
		{
			xFactor =  zPos / Math.sqrt(zPos*zPos + yFactor*yFactor);
		}
		else
		{
			xFactor = 1;
		}
		if(curveY)
		{
			yPos = distance * Math.sin(y*pdy + cpy);
		}
		else
		{
			yPos = (y*pdy)*Math.cos(cpy) + cdy;
		}
		if(printing)
		System.out.println("("+xPos+","+yPos+","+zPos+")");
		
		return (double)Math.acos(xFactor*(Math.cos(x)));
	}
	/**
	 * A helper class used to return a tuple of doubles.
	 * @author hrz
	 *
	 */
	public static class FPoint{
		/**
		 * The first data element.
		 */
		public double x;
		/**
		 * The second data element.
		 */
		public double y;
		/**
		 * Creates and initialises a new FPoint.
		 * @param xv The initial value of x.
		 * @param yv The initial value of y.
		 */
		public FPoint(double xv, double yv)
		{
			this.x = xv;
			this.y = yv;
		}
		
		public String toString()
		{
			return "("+x+","+y+")";
		}
	}
	/**
	 * Calculates the total angle to a point.
	 * Designed for 3 dimensional movable flat detector
	 * @param x The x pixel coordinate.
	 * @param y The y pixel coordinate.
	 * @return The total angle 2theta.
	 */
	public double flat3DDetCorrect(double z, double y, double twoTheta)
	{
		double zPos = z, yPos = y, detPos = distance;
		double tFactor;
		double bFactor;
		double x = twoTheta;


			bFactor =   Math.sqrt(detPos*detPos + yPos*yPos + zPos*zPos);
	
			tFactor =  Math.sqrt(zPos*zPos + yPos*yPos);


		
		return (double)Math.acos(tFactor*(Math.cos(x))/bFactor);
	}
	/**
	 * Returns a standard AngleCorrecter used for testing.
	 * @return An angle corrector with the arguments (1,0,0,0,-0.0375f,true,false,(double)(0.4*Math.PI/180),0.01f, 1)
	 */
	public static GeometryCorrecter getTestCorrecter()
	{
		return new GeometryCorrecter(1,new FPoint(0,0),
				new FPoint(0,-0.01f*37.5f),
				true,false,
				new FPoint((double)(0.4*Math.PI/180),0.01f),
				1);
	}
	/**
	 * Switches the columns and rows of a 2d array of doubles.
	 * @param in The array to transpose.
	 * @return A new, transposed array.
	 */
	public  double[][] transpose(double[][] in)
	{
		double[][] out = new double[in[0].length][in.length];
		for(int i = 0 ; i < in.length;i++)
		{
			for(int j= 0; j < out.length; j++)
			{
				out[j][i] = in[i][j];
			}
		}
		return out;
	}
}
