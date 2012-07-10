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
package au.gov.ansto.bragg.datastructures.core.plot;

/**
 * @author nxi
 * Created on 22/01/2009
 */
public class Position {

	private String name = "id";
	private double x = Double.NaN;
	private double y = Double.NaN;
	private double value = Double.NaN;
	
	public Position(){
		super();
	}

	/**
	 * @param value
	 * @param x
	 * @param y
	 */
	public Position(double value, double x, double y) {
		super();
		this.value = value;
		this.x = x;
		this.y = y;
	}

	public Position(String name, double value, double x, double y) {
		super();
		this.name = name;
		this.value = value;
		this.x = x;
		this.y = y;
	}
	
	public Position(String positionString){
		super();
		String parts[];
		String string = positionString;
		if (positionString.contains(":")){
			parts = positionString.split(":");
			name = parts[0].trim();
			string = parts[1].trim();
		}
		if (string.toLowerCase().equals("nan"))
			return;
		parts = string.split(",");
		try{
			x = Double.valueOf(parts[0]);
		}catch (Exception e) {
		}
		try{
			y = Double.valueOf(parts[1]);
		}catch (Exception e) {
		}
		try{
			value = Double.valueOf(parts[2]);
		}catch (Exception e) {
		}
	}
	
	public String getName(){
		return name;
	}
	
	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}
	
	public double getValue(){
		return value;
	}
	
	public String toString(){
		String result = "";
		if (!name.equals("id"))
			result += name + ": ";
		if (Double.isNaN(x) && Double.isNaN(y))
			return result + "NaN";
		if (!Double.isNaN(value) || !Double.isNaN(y))
			return result + x + "," + y + "," + value; 
		return result + String.valueOf(x);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		Position position = null;
		if (obj instanceof Position){
			position = (Position) obj;
		}else if (obj instanceof String){
			position = new Position(obj.toString());
		}else
			return false;
		return (Double.valueOf(x).equals(Double.valueOf(position.getX()))
				&& Double.valueOf(y).equals(Double.valueOf(position.getY()))
				&& Double.valueOf(value).equals(Double.valueOf(position.getValue())));
	}
	

}
