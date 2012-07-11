package au.gov.ansto.bragg.wombat.dra.algolib.entity;
/**
 * Copyright (c) 2006  Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the GNU GENERAL PUBLIC LICENSE v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */
/**
 * @author J. G. WANG
 *
 */
import au.gov.ansto.bragg.common.dra.algolib.model.Detector;
public class HIPDDetector extends Detector {
	/**
	 * The number of detectors (horizontal)
	 */
	public  int  nScan = 1;
	public  int hCount = 3840;
	/**
	 * The number of pixels in each detector (vertical)
	 */
	public  int xPixels = 3840;
	public  int yPixels = 512;
	/**
	 * The distance between the detectors in degrees.
	 */
	public  double seperation =0.03125;
/**
 * Setup detector shape
 */
	public boolean curvetheta =true;
	public boolean curveY = false;
	/**
	 * The distance between the detectors in mm.
	 * pixelHeight in mm, pixelWidth in degree
	 */
	public  double pixelHeight =0.390625 ;
	public  double pixelWidth = 0.390625;
	/**
	 * The position of the first detector.
	 */
	public double firstPos = 0;
	/**
	 * The curve of detector
	 * @parameter
	 * @radialCurv  radial length for curved detector in mm.
	 * @heigthCurv  height for curved detector
	 * @distance from sample to detector in mm
	 */
	public double  radialCurv = 700;
	public double  heightCurv = 200;
	public double beamX;
	public double beamY;
	public double[][] sensitivity;
	public double cellSizeX;
	public double cellSizeY;
	public double distance =700.0;
	public double geometery;
	public double horisonCurv = 120;
	public double getBeamX() {
		return beamX;
	}
	public void setBeamX(double beamX) {
		this.beamX = beamX;
	}
	public double getBeamY() {
		return beamY;
	}
	public void setBeamY(double beamY) {
		this.beamY = beamY;
	}
	public double getCellSizeX() {
		return cellSizeX;
	}
	public void setCellSizeX(double cellSizeX) {
		this.cellSizeX = cellSizeX;
	}
	public double getCellSizeY() {
		return cellSizeY;
	}
	public void setCellSizeY(double cellSizeY) {
		this.cellSizeY = cellSizeY;
	}
	public int getHCount() {
		return hCount;
	}
	public void setHCount(int count) {
		this.hCount = count;
	}
	public boolean isCurvetheta() {
		return curvetheta;
	}
	public void setCurvetheta(boolean curvetheta) {
		this.curvetheta = curvetheta;
	}
	public boolean isCurveY() {
		return curveY;
	}
	public void setCurveY(boolean curveY) {
		this.curveY = curveY;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public double getFirstPos() {
		return firstPos;
	}
	public void setFirstPos(double firstPos) {
		this.firstPos = firstPos;
	}
	public double getGeometery() {
		return geometery;
	}
	public void setGeometery(double geometery) {
		this.geometery = geometery;
	}
	public double getHeightCurv() {
		return heightCurv;
	}
	public void setHeightCurv(double heightCurv) {
		this.heightCurv = heightCurv;
	}
	public double getPixelHeight() {
		return pixelHeight;
	}
	public void setPixelHeight(double pixelHeight) {
		this.pixelHeight = pixelHeight;
	}
	public double getPixelWidth() {
		return pixelWidth;
	}
	public void setPixelWidth(double pixelWidth) {
		this.pixelWidth = pixelWidth;
	}
	public double getRadialCurv() {
		return radialCurv;
	}
	public void setRadialCurv(double radialCurv) {
		this.radialCurv = radialCurv;
	}
	public double[][] getSensitivity() {
		return sensitivity;
	}
	public void setSensitivity(double[][] sensitivity) {
		this.sensitivity = sensitivity;
	}
	public double getSeperation() {
		return seperation;
	}
	public void setSeperation(double seperation) {
		this.seperation = seperation;
	}
	public int getXpixels() {
		return xPixels;
	}
	public void setXpixels(int xpixels) {
		this.xPixels = xpixels;
	}
	public int getYpixels() {
		return yPixels;
	}
	public void setYpixels(int ypixels) {
		this.yPixels = ypixels;
	}
	public int getnScan() {
		return nScan;
	}
	public void setnScan(int scan) {
		nScan = scan;
	}
}
