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
package au.gov.ansto.bragg.quokka.model.core.instrument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.gumtree.gumnix.sics.dom.sics.SicsDOM;
import org.gumtree.gumnix.sics.dom.sics.SicsDOMException;

import au.gov.ansto.bragg.quokka.model.core.QuokkaModel;
import au.gov.ansto.bragg.quokka.model.core.device.VirtualDevice;
import au.gov.ansto.bragg.quokka.model.core.exception.DeviceNotExistException;


/**
 * @author nxi
 *
 */
public class QuokkaInstrument {
//	public enum MotorName{bsz, bsx, detector_x, sample_y
//	, detector_y, apz, sample_z, bs5};

//	public enum MotorName{beamstopper_z_mm, beamstopper_x_mm, detector_x_mm, sampleaperture_y_m
//	, detector_y_m, entranceaperture_radius_mm, sampleaperture_radius_mm, beamstopper_radius_mm};
	public static SicsDOM sics;

	public static Map<String, String> motorTable;

	public static String move(QuokkaModel quokkaModel, String motorId, SicsDOM sics, 
			List<String> positionDescription) throws Exception {
		// TODO Auto-generated method stub
		String result = "";
		if (motorTable == null) initialiseMotorTable();
		String motorName = motorTable.get(motorId);
		if (motorName == null)
			throw new DeviceNotExistException("can not find device " + motorId);
		VirtualDevice device = null;
		if (motorName != null){
			device = VirtualDevice.getDevice(motorName);
//			result += "moving VirtualDevice:" + motorName + " (" + device.getName() +
//			") to " + positionDescription + "\n";
			result += device.moveTo(positionDescription);
//			switch(motorName){
//			case bsz : quokkaModel.moveBeamStopperZ(motorPosition); break;
//			case bsx : quokkaModel.moveBeamStopperX(motorPosition); break;
//			case detector_x : quokkaModel.moveDetectorCenterX(motorPosition); break;
//			case detector_y : quokkaModel.shrinkL2(motorPosition); break;
//			case sample_y : quokkaModel.shrinkL1(motorPosition);
//			quokkaModel.shrinkL2(- motorPosition); break;
//			case apz : quokkaModel.setR1(motorPosition); break;
//			case sample_z : quokkaModel.setR2(motorPosition); break;
//			case bs5 : quokkaModel.setBeamStopperRadius(motorPosition); break;
//			default : throw new Exception("can not drive the instrument " + motorName);
//			}
		}
		return result;
//		try {
//		sics.run(motorId, (float) motorPosition); 
//		} catch (Exception e) {
//		// TODO: handle exception
//		String errorMessage = e.getMessage();
//		if (errorMessage != null)
//		return result + "failed to send command to SICS server, " + e.getMessage() + "\n";
//		else return result + "failed to send command to SICS server\n";
//		}
//		return "moving SICSDevice:" + motorName + " (" + device.getName() +
//		") to " + motorPosition + "\n";
	}
	
	public static String move(QuokkaModel quokkaModel, String motorId, SicsDOM sics, 
			String position) throws Exception {
		List<String> positionDescription = new ArrayList<String>();
		positionDescription.add(position);
		return move(quokkaModel, motorId, sics, positionDescription);
	}

	public static String sicsDrive(String sicsDeviceId, double position){
		String result = "";
		try {
			sics.run(sicsDeviceId, (float) position); 
		} catch (Exception e) {
			String errorMessage = e.getMessage();
			if (errorMessage != null)
				return result + "failed to send command to SICS server, " + e.getMessage() + "\n";
			else return result + "failed to send command to SICS server\n";
		}
		return "moving device:" + sicsDeviceId + " (" + motorTable.get(sicsDeviceId) +
		") to " + position + "\n";
	}
	
	public static String sicsSet(String sicsDevicePath, String position){
		String result = "";
		try {
			sics.setValue(sicsDevicePath, position);
		} catch (Exception e) {
			String errorMessage = e.getMessage();
			if (errorMessage != null)
				return result + "failed to send command to SICS server, " + e.getMessage() + "\n";
			else return result + "failed to send command to SICS server\n";
		}
		return "moving SICSDevice:" + sicsDevicePath + " to " + position + "\n";
	}

	public static String sicsSetParameter(String parameterId, String value){
		String result = "";
		try {
			sics.setParameterValue(parameterId, value);
		} catch (Exception e) {
			// TODO: handle exception
			String errorMessage = e.getMessage();
			if (errorMessage != null)
				return result + "failed to send command to SICS server, " + e.getMessage() + "\n";
			else return result + "failed to send command to SICS server\n";
		}
		return "set SICSParameter:" + parameterId + " to " + value + "\n";
	}
	
	private static void initialiseMotorTable() {
		// TODO Auto-generated method stub
		motorTable = new HashMap<String, String>();
		motorTable.put("bsx", "BeamStopX");
		motorTable.put("bsz", "BeamStopZ");
		motorTable.put("detoff", "DetectorX");
		motorTable.put("det", "DetectorY");
//		motorTable.put("bs5", "BeamStopperRadius");
		motorTable.put("apz", "EntranceRadius");
		motorTable.put("samy", "SampleY");
		motorTable.put("rotapdeg", "RotaryApertureAngle");
		motorTable.put("rotapshape", "RotaryApertureShape");
		motorTable.put("RotAp", "RotaryAperture");
		motorTable.put("samplenum", "SampleID");
		motorTable.put("sampleselection", "SampleSelection");
		motorTable.put("AttRot", "RotaryAttenuator");
		motorTable.put("att", "AttenuatorDegree");
		motorTable.put("apx", "ApertureX");
		motorTable.put("apxsamx", "ApxSamx");
		motorTable.put("attrotdeg", "RotaryAttenuatorAngle");
		motorTable.put("SAp", "SampleAperture");
		motorTable.put("SApSize", "SampleApertureSize");
		motorTable.put("sapshape", "SampleApertureShape");
		motorTable.put("bsshape", "BeamStopShape");
		motorTable.put("lambdaa", "LambdaA");
		motorTable.put("lambdaresfwhm_percent", "LambdaResFWHM");
		motorTable.put("attfactor", "AttenuatorFactor");
		motorTable.put("plexmm", "PerspexThickness");
		motorTable.put("eapxmm", "EntranceApertureX");
		motorTable.put("eapymm", "EntranceApertureY");
		motorTable.put("eapzmm", "EntranceApertureZ");
		motorTable.put("eapshape", "EntranceApertureShape");
		motorTable.put("rotapshape", "RotaryApertureShape");
		motorTable.put("detposxmm", "DetectorPositionX");
		motorTable.put("detposymm", "DetectorPositionY");
		motorTable.put("bsposxmm", "BeamStopPositionX");
		motorTable.put("bsposzmm", "BeamStopPositionZ");
		motorTable.put("sampletiltxdeg", "SampleTiltX");
		motorTable.put("samx", "SampleXmm");
		motorTable.put("samz", "SampleZmm");
		motorTable.put("srce", "SourceAperture");
		motorTable.put("sampletiltydeg", "SampleTiltY");
		motorTable.put("samplerotdeg", "SampleRotary");
		motorTable.put("endfaceposymm", "EndFacePositionY");
		motorTable.put("vsdeg", "VelocitySeletorDeg");
		motorTable.put("vsrpm", "VelocitySeletorRpm");
		motorTable.put("eapposymm", "EntranceAperturePositionY");
		motorTable.put("sapxmm", "SampleApertureX");
		motorTable.put("sapzmm", "SampleApertureZ");
		motorTable.put("sapposxmm", "SampleAperturePositionX");
		motorTable.put("sapposymm", "SampleAperturePositionY");
		motorTable.put("sapposzmm", "SampleAperturePositionZ");	
		motorTable.put("sampleposxmm", "SamplePositionX");
		motorTable.put("sampleposymm", "SamplePositionY");
		motorTable.put("sampleposzmm", "SamplePositionZ");
		motorTable.put("samplerotdeg", "SampleRotary");
		motorTable.put("samphi", "SamplePhi");
		motorTable.put("detposyoffsetmm", "DetectorPositionYOffset");
		motorTable.put("bsxmm", "BeamStopPositionX");
		motorTable.put("bszmm", "BeamStopPositionZ");
		motorTable.put("guide", "Guide");
		motorTable.put("dummy_motor", "DummyMotor");
		motorTable.put("samthet", "SampleTheta");
		motorTable.put("guideconfiguration", "GuideConfiguration");

//		setMotorID();
//		motorTable.put("sample_z", "SampleZ");
//		for (MotorName motorName : MotorName.values()){
//		motorTable.put(motorName.name(), motorName);
//		}
	}

	private static void setMotorID(){
		if (motorTable == null) initialiseMotorTable();
		Set<Entry<String, String>> entrys = motorTable.entrySet();
		for (Iterator<?> iterator = entrys.iterator(); iterator.hasNext();) {
			Entry<?, ?> entry = (Entry<?, ?>) iterator.next();
			String id = (String) entry.getKey();
			String deviceName = (String) entry.getValue();
			VirtualDevice device = null;
			try {
				device = VirtualDevice.getDevice(deviceName);
				device.setId(id);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	public static boolean isExist(String motorId){
		if (motorTable == null) initialiseMotorTable();
		return motorTable.containsKey(motorId);
	}

	public static VirtualDevice getDevice(String motorID) 
	throws DeviceNotExistException{
		if (motorTable == null) initialiseMotorTable();
		return VirtualDevice.getDevice(motorTable.get(motorID));
	}

	public static float getDrivableDevicePosition(String id) throws SicsDOMException{
		if (sics == null) throw new SicsDOMException("SICS server not available");
		return sics.getPosition(id);
	}

	public static String getNonDrivableDeviceValue(String sicsDevicePath) throws SicsDOMException{
		if (sics == null) throw new SicsDOMException("SICS server not available");
		return sics.getValue(sicsDevicePath);
	}
	
	public static String syncWithSics() {
		if (sics == null) return "failed to synchronise the model with SICS, " +
		"SICS DOM is not available";
		if (motorTable == null) initialiseMotorTable();
		String result = "";
		Set<Entry<String, String>> entrys = motorTable.entrySet();
		for (Iterator<?> iterator = entrys.iterator(); iterator.hasNext();) {
			Entry<?, ?> entry = (Entry<?, ?>) iterator.next();
			String id = (String) entry.getKey();
			String deviceName = (String) entry.getValue();
			VirtualDevice device = null;
			try {
				device = VirtualDevice.getDevice(deviceName);
			} catch (DeviceNotExistException e1) {
				// TODO Auto-generated catch block
//				e1.printStackTrace();
				result += e1.getMessage() + "\n";
			}
			try{
				device.syncWithSics();
				result += device.getName() + " synchronised with " + id + "@SICS at position " +
				device.getPosition() + "\n";
			}catch (Exception e) {
				// TODO: handle exception
				result += device.getName() + " failed to synchronise with " + id + "@SICS : " 
				+ e.getMessage() + "\n";
			}
		}
		return result;
	}

	public static void setSics(SicsDOM sicsDom){
		sics = sicsDom;
	}

	public static String sicsRun(String sicsDevicePath) {
		// TODO Auto-generated method stub
		String result = "";
		try {
//			sics.runCommand(sicsDevicePath);
			sics.runCommandWithFeedBack(sicsDevicePath);
		} catch (Exception e) {
			// TODO: handle exception
			String errorMessage = e.getMessage();
			if (errorMessage != null)
				return result + "failed to run the sics command, " + e.getMessage() + "\n";
			else return result + "failed to send command to SICS server\n";
		}
		return "running SICSCommand:" + sicsDevicePath + "\n";
	}

	public static String set(QuokkaModel quokkaModel, String motorId,
			SicsDOM sics2, String value) throws DeviceNotExistException {
		// TODO Auto-generated method stub
		
		String result = "";
		if (motorTable == null) initialiseMotorTable();
		String motorName = motorTable.get(motorId);
		if (motorName == null)
			throw new DeviceNotExistException("can not find device " + motorId);
		VirtualDevice device = null;
		if (motorName != null){
			device = VirtualDevice.getDevice(motorName);
			if (device == null)
				throw new DeviceNotExistException("can not find device " + motorId);
			result += "set VirtualParameter:" + motorName + " (" + device.getName() +
			") to " + value + "\n";
			result += device.setValue(value);
//			switch(motorName){
//			case bsz : quokkaModel.moveBeamStopperZ(motorPosition); break;
//			case bsx : quokkaModel.moveBeamStopperX(motorPosition); break;
//			case detector_x : quokkaModel.moveDetectorCenterX(motorPosition); break;
//			case detector_y : quokkaModel.shrinkL2(motorPosition); break;
//			case sample_y : quokkaModel.shrinkL1(motorPosition);
//			quokkaModel.shrinkL2(- motorPosition); break;
//			case apz : quokkaModel.setR1(motorPosition); break;
//			case sample_z : quokkaModel.setR2(motorPosition); break;
//			case bs5 : quokkaModel.setBeamStopperRadius(motorPosition); break;
//			default : throw new Exception("can not drive the instrument " + motorName);
//			}
		}
		return result;
	}
}
