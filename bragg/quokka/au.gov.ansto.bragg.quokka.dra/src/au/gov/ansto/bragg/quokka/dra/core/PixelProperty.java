package au.gov.ansto.bragg.quokka.dra.core;

import org.gumtree.data.Factory;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.quokka.dra.core.internal.ConcreteProcessor;


public class PixelProperty implements ConcreteProcessor {

	IGroup pixelProperty_scanData = null;
	IGroup pixelProperty_output = null;
	Integer pixelProperty_pixelX = 0;
	Integer pixelProperty_pixelZ = 0;
	Boolean pixelProperty_stop = false;

	private double[] beamCenter = null;
	private double detectorDistance = 0;
	private double lambda = 0.;
	private double pixelDistance = 5.;
//	private 
	
	public PixelProperty() {
		// TODO Auto-generated constructor stub
	}

	public Boolean process() throws Exception {
		// TODO Auto-generated method stub
		getStorageData();
		double[] distanceToCenterXZ = getDistanceToCenter();
		double distanceToCenter = Math.sqrt(distanceToCenterXZ[0] * distanceToCenterXZ[0] + 
				distanceToCenterXZ[1] * distanceToCenterXZ[1]);
		double twoTheta = Math.atan(distanceToCenter / detectorDistance);
		double[] twoThetaXZ = findTwoThetaXZ(distanceToCenterXZ);
		double Q = findQ(twoTheta);
		double[] QXZ = new double[]{findQ(twoThetaXZ[0]), findQ(twoThetaXZ[1])};
		double sampleDistance = Math.sqrt(distanceToCenter * distanceToCenter + 
				detectorDistance * detectorDistance);
		double[] unitXZY = findUnitVector(distanceToCenterXZ[0], distanceToCenterXZ[1], 
				detectorDistance, sampleDistance);
		
		pixelProperty_output = Factory.createGroup(pixelProperty_scanData.getDataset(), 
				pixelProperty_scanData, "pixel_property", true);
		IArray distanceToCenterArray = Factory.createArray(new double[]{distanceToCenterXZ[0], 
				distanceToCenterXZ[1], distanceToCenter});
		IDataItem distanceToCenterDataItem = Factory.createDataItem(pixelProperty_output.getDataset(), 
				pixelProperty_output, "distance_to_center", distanceToCenterArray);
		IArray twoThetaArray = Factory.createArray(new double[]{twoThetaXZ[0], twoThetaXZ[1], 
				twoTheta});
		IDataItem twoThetaDataItem = Factory.createDataItem(pixelProperty_output.getDataset(), 
				pixelProperty_output, "two_theta", twoThetaArray);
		IArray QArray = Factory.createArray(new double[]{QXZ[0], QXZ[1], Q});
		IDataItem QDataItem = Factory.createDataItem(pixelProperty_output.getDataset(), 
				pixelProperty_output, "Q", QArray);
		IArray unitXZYArray = Factory.createArray(unitXZY);
		IDataItem unitXZYDataItem = Factory.createDataItem(pixelProperty_output.getDataset(), 
				pixelProperty_output, "unit_vector", unitXZYArray);
		pixelProperty_output.addDataItem(distanceToCenterDataItem);
		pixelProperty_output.addDataItem(twoThetaDataItem);
		pixelProperty_output.addDataItem(QDataItem);
		pixelProperty_output.addDataItem(unitXZYDataItem);
//		pixelProperty_output.buildResultGroup(unitXZYDataItem, distanceToCenterDataItem, twoThetaDataItem, QDataItem);
		System.out.println(pixelProperty_output);
		return pixelProperty_stop;
	}

	private double[] findUnitVector(double X, double Z,	double Y, double sampleDistance) {
		// TODO Auto-generated method stub
		return new double[]{X / sampleDistance, Z / sampleDistance, Y / sampleDistance};
	}

	private double findQ(double twoTheta) {
		// TODO Auto-generated method stub
		return 4 * Math.PI * Math.sin(twoTheta / 2) / lambda;
	}

	private double[] findTwoThetaXZ(double[] distanceToCenterXZ) {
		// TODO Auto-generated method stub
		double[] twoThetaXZ = new double[2];
		twoThetaXZ[0] = Math.atan(distanceToCenterXZ[0] / detectorDistance);
		twoThetaXZ[1] = Math.atan(distanceToCenterXZ[1] / detectorDistance);
		return twoThetaXZ;
	}

	private void getStorageData() {
		// TODO Auto-generated method stub
		IDataItem beamCenterData = pixelProperty_scanData.getDataItem("BCENT");
		try{
			beamCenter = (double[]) beamCenterData.getData().getArrayUtils().copyTo1DJavaArray();
			if (beamCenter.length != 2) throw new Exception();
			System.out.println("find beam center at (" + beamCenter[0] + ", " + beamCenter[1] + ")");
		}catch(Exception ex){
			System.out.println("can not find beam center in the data, use default value (96.0, 96.0)");
			beamCenter = new double[2];
			beamCenter[0] = 96.;
			beamCenter[1] = 96.;			
		}
		IDataItem detectorDistanceData = pixelProperty_scanData.getDataItem("DET_DIST");
		try {
			detectorDistance = detectorDistanceData.getData().getArrayMath().getMaximum() * 1000;
			System.out.println("find detector distance: " + detectorDistance);
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("can not find detector distance in the data, use default value 10(m)");
			detectorDistance = 10000;
		}
		IDataItem lambdaData = pixelProperty_scanData.getDataItem("LAMBDA");
		try {
			lambda = lambdaData.getData().getArrayMath().getMaximum();
			System.out.println("find lambda: " + lambda);
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("can not find lambda in the data, use default value 8.4(A)");
			lambda = 8.4;
		}

	}

	private double[] getDistanceToCenter() {
		// TODO Auto-generated method stub
		return new double[]{Math.abs(pixelProperty_pixelX - beamCenter[0]) * pixelDistance, 
				Math.abs(pixelProperty_pixelZ - beamCenter[1]) * pixelDistance};
	}

	public IGroup getPixelProperty_output() {
		return pixelProperty_output;
	}

	public void setPixelProperty_scanData(IGroup pixelProperty_scanData) {
		this.pixelProperty_scanData = pixelProperty_scanData;
	}

	public void setPixelProperty_pixelX(Integer pixelProperty_pixelX) {
		this.pixelProperty_pixelX = pixelProperty_pixelX;
	}

	public void setPixelProperty_pixelZ(Integer pixelProperty_pixelZ) {
		this.pixelProperty_pixelZ = pixelProperty_pixelZ;
	}

	public void setPixelProperty_stop(Boolean pixelProperty_stop) {
		this.pixelProperty_stop = pixelProperty_stop;
	}

}
