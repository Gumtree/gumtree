/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*******************************************************************************/
package au.gov.ansto.bragg.quokka.dra.algolib.core;

public class Pixel {

	private int pixelZ = 0;
	private int pixelX = 0;
	private Image image = null;
	double[] distanceToCenterZX = null;
	double distanceToCenter = 0;
	double twoTheta = 0;
	double[] twoThetaZX = null;
	double Q = 0;
	double[] QZX = null;
	double sampleDistance = 0;
	double[] unitYZX = null;
	double intensity = 0;
	
	public Pixel(int z, int x, Image image){
		pixelZ = z;
		pixelX = x;
		this.image = image;
		getProperties();
	}


	private void getProperties() {
		// TODO Auto-generated method stub
		double detectorDistance = image.getDetectorDistance();
		distanceToCenterZX = getDistanceToCenter();
		distanceToCenter = Math.sqrt(distanceToCenterZX[0] * distanceToCenterZX[0] + 
				distanceToCenterZX[1] * distanceToCenterZX[1]);
		twoTheta = Math.atan(distanceToCenter / detectorDistance);
		twoThetaZX = findTwoThetaZX();
		Q = findQ(twoTheta);
		QZX = new double[]{findQ(twoThetaZX[0]), findQ(twoThetaZX[1])};
		sampleDistance = Math.sqrt(distanceToCenter * distanceToCenter + 
				detectorDistance * detectorDistance);
		unitYZX = new double[]{detectorDistance / sampleDistance, 
				distanceToCenterZX[0] / sampleDistance, 
				distanceToCenterZX[1] / sampleDistance};
		intensity = image.getRawDataArray().getDouble(
				image.getRawDataArray().getIndex().set(pixelZ, pixelX));
	}

	private double findQ(double twoTheta) {
		// TODO Auto-generated method stub
		return 4 * Math.PI * Math.sin(twoTheta / 2) / image.getLambda();
	}

	private double[] findTwoThetaZX() {
		// TODO Auto-generated method stub
		return new double[]{Math.atan(distanceToCenterZX[0] / image.getDetectorDistance()),
				Math.atan(distanceToCenterZX[1] / image.getDetectorDistance())};
	}
	
	private double[] getDistanceToCenter() {
		// TODO Auto-generated method stub
		return new double[]{Math.abs(pixelZ - image.getBeamCenter()[0]) * 
				image.getDetectorDistance(), Math.abs(pixelX - image.getBeamCenter()[1]) * 
				image.getDetectorDistance()};
	}


	public int getPixelZ() {
		return pixelZ;
	}


	public int getPixelX() {
		return pixelX;
	}


	public double[] getDistanceToCenterZX() {
		return distanceToCenterZX;
	}


	public double getTwoTheta() {
		return twoTheta;
	}


	public double[] getTwoThetaZX() {
		return twoThetaZX;
	}


	public double getQ() {
		return Q;
	}


	public double[] getQZX() {
		return QZX;
	}


	public double getSampleDistance() {
		return sampleDistance;
	}


	public double[] getUnitYZX() {
		return unitYZX;
	}
}
