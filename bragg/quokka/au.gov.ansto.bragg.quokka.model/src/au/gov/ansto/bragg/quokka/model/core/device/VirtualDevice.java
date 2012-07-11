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
package au.gov.ansto.bragg.quokka.model.core.device;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

import org.gumtree.gumnix.sics.dom.sics.SicsDOMException;

import au.gov.ansto.bragg.quokka.model.core.QuokkaConstants;
import au.gov.ansto.bragg.quokka.model.core.QuokkaModel;
import au.gov.ansto.bragg.quokka.model.core.exception.DeviceNotExistException;
import au.gov.ansto.bragg.quokka.model.core.exception.DriveDeviceFailedException;
import au.gov.ansto.bragg.quokka.model.core.exception.IllegalValueException;
import au.gov.ansto.bragg.quokka.model.core.instrument.QuokkaInstrument;


/**
 * @author nxi
 *
 */
public abstract class VirtualDevice {

	protected List<String> positionDescription;
	protected String name;
	protected String id;
	protected String unit = "mm";
	protected List<String> lookUpTable;
	protected double max;
	protected double min;
	protected List<VirtualDevice> subdeviceList;
	protected String description = "No description for this device";
	protected String sicsDevicePath;
	protected boolean isRadonly = false;
	protected DeviceType deviceType;
	
	public enum DeviceType{moveable, setable, command, macro}; 
//	private String sicsDevicePath = QuokkaConstants.RotApDeg_PATH;

	protected VirtualDevice(){
		lookUpTable = new ArrayList<String>();
		subdeviceList = new ArrayList<VirtualDevice>();
	}

	protected String updateSubdevice (String sicsDevicePath, List<String> positionDescription){
		if (subdeviceList.size() > positionDescription.size()){
			return "no enough number of arguments, " + subdeviceList.size() + " arguments required";
		}
		String result = "";
		Iterator<String> positionIterator = positionDescription.iterator();
		for (VirtualDevice device : subdeviceList){
			List<String> positionList = new ArrayList<String>();
			positionList.add(positionIterator.next());
			result += device.moveTo(positionList);
		}
		try {
			Thread.currentThread().sleep(100);
		} catch (Exception e) {
			// TODO: handle exception
		}
		runAsCommand(sicsDevicePath);
		this.positionDescription = positionDescription;
		return result;
	}
	
	public String getUnit() {
		return unit;
	}

	public String moveTo(List<String> positionDescription){
		if (subdeviceList == null || subdeviceList.size() == 0){
//			if (sicsDevicePath == null)
//				return moveDrivable(positionDescription);
//			else
//				return moveNonDrivable(sicsDevicePath, positionDescription);
			if (getDeviceType() == DeviceType.moveable)
				return moveDrivable(positionDescription);
			else if (getDeviceType() == DeviceType.macro)
				return setValue(positionDescription.get(0));
			else if (getDeviceType() == DeviceType.moveable)
				return moveSetable(sicsDevicePath, positionDescription.get(0));
			else
				return moveNonDrivable(sicsDevicePath, positionDescription);
		}else
			return updateSubdevice(sicsDevicePath, positionDescription);
	}
	
	public String setValue(String value){
//			if (sicsDevicePath == null)
//				return moveDrivable(positionDescription);
//			else
//				return moveNonDrivable(sicsDevicePath, positionDescription);
//			if (getDeviceType() == DeviceType.setable)
			try {
				return moveSetable(sicsDevicePath, value);
			} catch (Exception e) {
				return "failed to set the value, device is not setable";
				// TODO: handle exception
			}
			
	}

	protected void changeModel() throws DriveDeviceFailedException{
		if (subdeviceList == null || subdeviceList.size() == 0)
			if (positionDescription.size() > 0)
				changeModel(positionDescription.get(0));
	}
	
	protected abstract void changeModel(Object positionObject) throws DriveDeviceFailedException;

	protected String moveDrivable(List<String> positionDescription){
//		this.positionDescription = positionDescription;
		try {
			setPosition(positionDescription);
		} catch (IllegalValueException e1) {
			// TODO Auto-generated catch block
			return e1.getMessage() + "\n";
		}
		if (positionDescription.size() != 1)
			return "Can not generate sics command, one position parameter expected\n";
		double position;
		try {
			position = Double.valueOf(positionDescription.get(0));
		} catch (Exception e) {
			// TODO: handle exception
			return "illegale parameter: " + positionDescription.get(0) + ", expecting float value\n";
		}
		try{
			changeModel(position);
		}catch (DriveDeviceFailedException e) {
			// TODO: handle exception
			return e.getMessage() + "\n";
		}
		return QuokkaInstrument.sicsDrive(getId(), position) + "\n";
//		quokkaModel.shrinkL2(- motorPosition);
	}

	protected String moveSetable(String sicsDevicePath, String value){
		List<String> positionList = new ArrayList<String>();
		positionList.add(value);
		try {
			setPosition(positionList);
		} catch (IllegalValueException e1) {
			// TODO Auto-generated catch block
			return e1.getMessage() + "\n";
		}
		if (positionDescription.size() != 1)
			return "Can not generate sics command, one position parameter expected\n";
//		double position;
//		try {
////			position = Double.valueOf(positionDescription.get(0));
//		} catch (Exception e) {
//			// TODO: handle exception
//			return "illegale parameter: " + positionDescription.get(0) + ", expecting float value\n";
//		}
		try{
			changeModel(value);
		}catch (DriveDeviceFailedException e) {
			// TODO: handle exception
			return e.getMessage() + "\n";
		}
		return QuokkaInstrument.sicsSetParameter(getId(), value) + "\n";
	}
	
	protected String moveNonDrivable(String sicsDevicePath, List<String> positionDescription) {
		try {
			setPosition(positionDescription);
		} catch (IllegalValueException e1) {
			// TODO Auto-generated catch block
			return e1.getMessage() + "\n";
		}
		if (positionDescription.size() != 1)
			return "Can not generate sics command, one position parameter expected\n";
		String position = positionDescription.get(0);
//		try {
//			position = Double.valueOf(positionDescription.get(0));
//		} catch (Exception e) {
//			// TODO: handle exception
//			return "illegale parameter: " + positionDescription.get(0) + ", expecting float value";
//		}
		try{
			changeModel(position);
		}catch (DriveDeviceFailedException e) {
			// TODO: handle exception
			return e.getMessage() + "\n";
		}
		return QuokkaInstrument.sicsSet(sicsDevicePath, position);
	}

	protected String runAsCommand(String sicsDevicePath){
		return QuokkaInstrument.sicsRun(sicsDevicePath);
	}
	
	public static VirtualDevice getDevice(String deviceName) 
	throws DeviceNotExistException{
		VirtualDevice device = null;
		try {
			Class<?> deviceClass = Class.forName(VirtualDevice.class.getPackage().getName() 
					+ "." + deviceName);
			Method newInstanceMethod = deviceClass.getMethod("getInstance", new Class[0]);
			device = (VirtualDevice) newInstanceMethod.invoke(deviceClass, new Object[]{});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new DeviceNotExistException(deviceName + " does not exist");
		} 
		return device;
	}

	public List<String> getPosition(){
		try {
			syncWithSics();
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
		return positionDescription;
	}

	public String getName(){
		return name;
	}

	public String getId(){
		return id;
	}

	public String getDescription(){
		return description;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setId(String id) {
		this.id = id;
	}

	protected QuokkaModel getQuokkaModel(){
		return QuokkaModel.getInstance();
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public void setPosition(List<String> positionDescription) throws IllegalValueException {
		if (positionDescription.size() == 1){
			if (! isInLookUpTable(positionDescription.get(0)))
				throw new IllegalValueException("value is not in the look up table");
			if (! isWithinRange(positionDescription.get(0))){
				throw new IllegalValueException("value is not in the range limitation");
			}
		}
		this.positionDescription = positionDescription;
	}

	private boolean isWithinRange(String string) {
		// TODO Auto-generated method stub
		if (max == min && max == 0)
			return true;
		Double value = 0.;
		try {
			value = Double.valueOf(string);
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		if (value > max) 
			return false;
		if (value < min)
			return false;
		return true;
	}

	private boolean isInLookUpTable(String valueString) {
		// TODO Auto-generated method stub
		if (lookUpTable == null || lookUpTable.size() == 0)
			return true;
		for (String item : lookUpTable) {
			if (item.equals(valueString))
				return true;
		}
		double value = 0;
		try {
			value = Double.valueOf(valueString);
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		for (String item : lookUpTable) {
			try {
				Double itemValue = Double.valueOf(item);
				if (itemValue == value)
					return true;
			} catch (Exception e) {
				// TODO: handle exception
			}
		}		
		return false;
	}

	public void setPosition(String position) throws IllegalValueException{
		List<String> positionList = new ArrayList<String>();
		positionList.add(position);
		setPosition(positionList);
	}

	public void syncWithSics() 
	throws SicsDOMException, IllegalValueException, DriveDeviceFailedException{
//		float position = QuokkaInstrument.getSicsDevicePosition(getId());
//		moveTo(position);
		if (subdeviceList == null || subdeviceList.size() == 0){
			if (sicsDevicePath == null){
				float position = QuokkaInstrument.getDrivableDevicePosition(getId());
				setPosition(String.valueOf(position));
				changeModel(position);
			}else{
				String position = QuokkaInstrument.getNonDrivableDeviceValue(sicsDevicePath);
				setPosition(position);
				changeModel(position);
			}
		}else{
			List<String> positionList = new ArrayList<String>();
			for (VirtualDevice device : subdeviceList){
				device.syncWithSics();
				positionList.addAll(device.getPosition());
			}
			setPosition(positionList);
		}
//		changeModel();
	}
	
	public List<String> getLookUpTable(){
		return lookUpTable;
	}
	
	public double[] getRange(){
		return new double[]{max, min};
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setRange(double max, double min){
		this.max = max;
		this.min = min;
	}

	public void setSicsDevicePath(String sicsDevicePath) {
		this.sicsDevicePath = sicsDevicePath;
	}
	
	public void addSubdevice(VirtualDevice device){
		subdeviceList.add(device);
	}
	
	public DeviceType getDeviceType(){
		return deviceType;
	}

	public boolean isRadonly() {
		return isRadonly;
	}

	public void setRadonly(boolean isRadonly) {
		this.isRadonly = isRadonly;
	}
}
