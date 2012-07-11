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
package au.gov.ansto.bragg.quokka.model.core;

import au.gov.ansto.bragg.quokka.model.core.instrument.QuokkaInstrument;
import au.gov.ansto.bragg.quokka.model.core.lib.Formulae;


public class QuokkaModel {
	double L1;
	double L2;
	double r1;
//	double r2;
//	double rotaryApertureAngle = 15;
	BeamStopper beamStop;
	Detector detector;
	private double waveLength = 0;
	private double scanTime = 0;
	double[][] beamData = null;
	RotaryAperture rotartyAperture = new RotaryAperture(30, "circ");
	RotaryAttenuator rotaryAttenuator = new RotaryAttenuator(30);
	SampleAperture sampleAperture = new SampleAperture(QuokkaConstants.SAMPLE_APERTURE_RADIUS, "circ");
	Sample currentSample = new Sample(0);
	
	private static QuokkaModel model;
	
	public static QuokkaModel getInstance(){
		if (model == null){
			model = new QuokkaModel();
		}
		return model;
	}
	
	protected QuokkaModel(){
		L1 = QuokkaConstants.ENTRANCE_DISTANCE;
		L2 = QuokkaConstants.SAMPLE_DISTANCE;
		r1 = QuokkaConstants.ENTRANCE_APERTURE_RADIUS;
//		r2 = QuokkaConstants.SAMPLE_APERTURE_RADIUS;
		beamStop = new BeamStopper(0, 0, QuokkaConstants.DEFAULT_BEAMSTOPPER_RADIUS);
		detector = new Detector(0);
	}
	
	public String syncWithSics(){
		String result = "";
		try {
			result = QuokkaInstrument.syncWithSics();
		} catch (Exception e) {
			// TODO: handle exception
			result = "Not synchronised with SICS";
		}
		return result;
	}
	
	public void shrinkL1(double position){
		L1 = QuokkaConstants.ENTRANCE_DISTANCE - position;
	}
	
	public void shrinkL2(double position){
		L2 = QuokkaConstants.SAMPLE_DISTANCE - position;
	}
	
	public void setR1(double radius){
		r1 = radius;
	}
	
	public void setR2(double radius){
		sampleAperture.setSize(radius);
//		r2 = radius;
	}

	public void moveBeamStopX(double location){
		beamStop.setCenterX(location);
	}
	
	public void moveBeamStopZ(double location){
		beamStop.setCenterZ(location);
	}

	public void setBeamStopperRadius(double radius){
		beamStop.radius = radius;
	}
	
	public void moveDetectorCenterX(double location){
		detector.setXPosition(location);
	}
	
	public class BeamStopper{
		double centerZ;
		double centerX;
		double radius;
		String shape;

		public BeamStopper(double centerZ, double centerX, double radius){
			this.centerX = centerX;
			this.centerZ = centerZ;
			this.radius = radius;
			this.shape = "circ";
		}

		public double getRadius() {
			return radius;
		}

		public double getCenterZ() {
			return centerZ;
		}

		public void setCenterZ(double centerZ) {
			this.centerZ = centerZ;
		}

		public double getCenterX() {
			return centerX;
		}

		public void setCenterX(double centerX) {
			this.centerX = centerX;
		}
		
		public String toString(){
			return "beam stop: center(z,x) = [" + centerZ + "," + centerX + "], " +
					"radius = " + radius;
		}

		public String getShape() {
			return shape;
		}

		public void setShape(String shape) {
			this.shape = shape;
		}
	}
	
	public class Detector{
		final static int binResolution = 10;
		double zPosition = 0.;
		double xPosition = 0.;
		int zResolution = 192;
		int xResolution = 192;
		double pixelWidth = 5;
		boolean isChanged = false;
		
		public Detector(double xPosition){
			this.xPosition = xPosition;
		}
		
		public boolean isChanged(){
			return isChanged;
		}
		
		public void resetChangedFlag(){
			isChanged = false;
		}
		
		public int getBinResolution(){
			return binResolution;
		}
		
		public int getUnitResolution(){
			return (int) (binResolution / pixelWidth);
		}
		
		public void setXPosition(double xPosition){
			this.xPosition = xPosition;
			isChanged = true;
		}

		public double[] getBeamCenter() {
			// TODO Auto-generated method stub
			double[] beamCenter = new double[2];
			beamCenter[0] = zResolution * pixelWidth / 2.0 - zPosition;
			beamCenter[1] = xResolution * pixelWidth / 2.0 - xPosition;
			return beamCenter;
		}
		
		public double[] getOrigin(){
			double[] origin = new double[2];
			origin[0] = zResolution * pixelWidth / 2.0;
			origin[1] = xResolution * pixelWidth / 2.0;
			return origin;
		}
		
		public String toString(){
			return "Detector: origin(z,x) = [" + zPosition + "," + xPosition + "], "; 
		}
	}
	
	public double[][] generateDetectorData(double time, double waveLength){
		double[][] beamData = createBeamData(time, waveLength);
		double[][] detectorDataHiRes = createStoppedData(beamData);
		double[][] detectorData = rebinDetectorData(detectorDataHiRes);
		return detectorData;
	}
	
	private double[][] rebinDetectorData(double[][] detectorDataHiRes) {
		// TODO Auto-generated method stub
		double[][] detectorData = new double[detector.zResolution][detector.xResolution];
		for (int i = 0; i < detectorData.length; i++) 
			for (int j = 0; j < detectorData[0].length; j++) {
				double binReading = 0;
				for (int ii = 0; ii < detector.getBinResolution(); ii++) 
					for (int jj = 0; jj < detector.getBinResolution(); jj++) 
						binReading += detectorDataHiRes[i * detector.getBinResolution() + ii]
						                                [j * detector.getBinResolution() + jj];
				detectorData[i][j] = binReading;
			}
		return detectorData;
	}

	private double[][] createBeamData(double time, double waveLength) {
		// TODO Auto-generated method stub
		if (beamData != null && !detector.isChanged && this.scanTime == time 
				&& this.waveLength == waveLength){
			return beamData;
		}
//		double mean = 96;
//		double variance = 0.04;
		this.scanTime = time;
		this.waveLength = waveLength;
		int[] size = {detector.zResolution * detector.getBinResolution(), detector.xResolution
				* detector.getBinResolution()};
//		double[][] beamData = null;
		double[][] gaussian1 = null;
		double[][] gaussian2 = null;
		double r2 = sampleAperture.getSize();
		double rd = r2 + (r1 + r2) * L2 / L1;
		double rt = Math.abs(r2 + (r2 - r1) * L2 / L1);
		double variance = (rd + rt) / 2;
		double[] mean = detector.getBeamCenter();
		for (int i = 0; i < mean.length; i++) {
			mean[i] = mean[i] * detector.getUnitResolution();
		}
		double[] variances = new double[]{variance, variance};
		double covariance = time / waveLength;
//		int beamStopperRadius = 18;
//		private int[] block_centre = {106, 80};
//		private int radius = 18;
//		private int numOfEntries = 30;
//		private int xShift = -1;
//		private int zShift = 0;
//		initialliseMotorPosition(size, beamStopperRadius);
		try {
			gaussian1 = (double[][]) Formulae.generateGaussian(size, mean, variances, covariance);
			gaussian2 = (double[][]) Formulae.generateGaussian(size, mean, new double[]{variances[0] * 5, variances[1] * 5}, covariance);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} ;
		beamData = new double[size[0]][size[1]];
		for (int i = 0; i < size[0]; i++)
			for (int j = 0; j < size[1]; j++){
				beamData[i][j] = gaussian1[i][j] + gaussian2[i][j];
			}
		detector.resetChangedFlag();
		return beamData;
	}
	
	private double[][] createStoppedData(double[][] beamData) {
		// TODO Auto-generated method stub

		double[][] stoppedData = new double[beamData.length][beamData[0].length];
		double[] beamStopperCenterPixel = new double[2];
		double[] detectorOrigin = detector.getOrigin();
//		beamStopperCenterPixel[0] = beamStopper.centerZ / detector.pixelWidth + detectorCenter[0]; 
//		beamStopperCenterPixel[1] = beamStopper.centerX / detector.pixelWidth + detectorCenter[1];
		beamStopperCenterPixel[0] = (beamStop.centerZ + detectorOrigin[0]) * detector.getUnitResolution(); 
		beamStopperCenterPixel[1] = (beamStop.centerX + detectorOrigin[1]) * detector.getUnitResolution();
		
		for (int i = 0; i < beamData.length; i++) {
			for (int j = 0; j < beamData[0].length; j++) {
				double distance = (i - beamStopperCenterPixel[0]) * (i - beamStopperCenterPixel[0]) 
				+ (j - beamStopperCenterPixel[1]) * (j - beamStopperCenterPixel[1]);
				if (distance <= beamStop.getRadius() * detector.getUnitResolution() 
						* beamStop.getRadius() * detector.getUnitResolution()) 
					stoppedData[i][j] = 0;
				else stoppedData[i][j] = beamData[i][j];
			}
		}
		return stoppedData;
	}
	
	public String getStatus(){
		String result = "The Quokka instrument model description is:\n\n";
		result += "L1 (entrance to sample) = " + L1 + " m\n";
		result += "L2 (sample to detector) = " + L2 + " m\n";
		result += "r1 (entrance radius) = " + r1 + " mm\n";
		result += "r2 (sample radius) = " + sampleAperture.getSize() + " mm\n";
//		result += "beam stop position at Z axis = " + beamStop.getCenterZ() + " mm\n";
//		result += "beam stop position at X axis = " + beamStop.getCenterX() + " mm\n";
//		result += "beam stop radius = " + beamStop.getRadius() + " mm\n";
		result += beamStop.toString() + "\n";
//		result += "detector position at X axis = " + detector.xPosition + " mm\n";
		result += detector.toString() + "\n";
		result += rotaryAttenuator.toString() + "\n";
		result += rotartyAperture.toString() + "\n";
		result += sampleAperture.toString() + "\n";
		result += currentSample.toString() + "\n";
		return result;
	}
	
	public class RotaryAttenuator{
		private double angle;

		
		public RotaryAttenuator(double angle) {
			super();
			this.angle = angle;
		}

		public double getAngle() {
			return angle;
		}

		public void setAngle(double angle) {
			this.angle = angle;
		}
		
		public String toString(){
			return "rotary attenuator angle = " + angle;
		}
	}
	
	public class RotaryAperture{
		private double angle;
		private String shape;
		public RotaryAperture(double angle, String shape) {
			super();
			this.angle = angle;
			this.shape = shape;
		}
		public double getAngle() {
			return angle;
		}
		public void setAngle(double angle) {
			this.angle = angle;
		}
		public String getShape() {
			return shape;
		}
		public void setShape(String shape) {
			this.shape = shape;
		}
		
		public String toString(){
			return "rotary aperture: angle = " + angle + ", shape = " + shape;
		}
	}
	
	public class SampleAperture{
		private double size;
		private String shape;
		public SampleAperture(double size, String shape) {
			super();
			this.size = size;
			this.shape = shape;
		}
		public double getSize() {
			return size;
		}
		public void setSize(double size) {
			this.size = size;
		}
		public String getShape() {
			return shape;
		}
		public void setShape(String shape) {
			this.shape = shape;
		}
		
		public String toString(){
			return "sample aperture size = " + size + ", shape = " + shape;
		}
	}

	public class Sample{
		private int sampleNum;

		public Sample(int sampleNum) {
			super();
			this.sampleNum = sampleNum;
		}

		public int getSampleNum() {
			return sampleNum;
		}

		public void setSampleNum(int sampleNum) {
			this.sampleNum = sampleNum;
		}
		
		public void setSampleNum(double sampleNum) {
			this.sampleNum = (int) sampleNum;
		}
		
		public String toString(){
			return "current sample id = " + sampleNum;
		}
	}
	
	public RotaryAperture getRotaryAperture() {
		return rotartyAperture;
	}

	public void setRotartyAperture(RotaryAperture rotartyAperture) {
		this.rotartyAperture = rotartyAperture;
	}

	public RotaryAttenuator getRotaryAttenuator() {
		return rotaryAttenuator;
	}

	public void setRotaryAttenuator(RotaryAttenuator rotaryAttenuator) {
		this.rotaryAttenuator = rotaryAttenuator;
	}

	public SampleAperture getSampleAperture() {
		return sampleAperture;
	}

	public void setSampleAperture(SampleAperture sampleAperture) {
		this.sampleAperture = sampleAperture;
	}

	public Sample getCurrentSample() {
		return currentSample;
	}

	public void setCurrentSample(Sample currentSample) {
		this.currentSample = currentSample;
	}
	
	public String getBeamStopShape(){
		return beamStop.getShape();
	}
	
	public void setBeamStopShape(String shape){
		beamStop.setShape(shape);
	}

	public double getWaveLength() {
		return waveLength;
	}

	public void setWaveLength(double waveLength) {
		this.waveLength = waveLength;
	}
}
