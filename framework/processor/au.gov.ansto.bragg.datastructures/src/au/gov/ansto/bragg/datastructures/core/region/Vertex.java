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
package au.gov.ansto.bragg.datastructures.core.region;


/**
 * This class defines a point Vertex and can support 1D, 2D and 3D co-ordinates
 * A Vertex is either X/Y or X,Y or X,Y,Z
 * @author rdd
 *
 */
public class Vertex {

	public final static int VERTEX_3D = 3;
	public final static int VERTEX_2D = 2;
	public final static int VERTEX_1D = 1;
	
	private int xLoc = 0; // X location
	private int yLoc = 0; // Y location
	private int zLoc = 0; // Z location
	
	private int vertexType = 0;
	
	/**
	 * Default constructor
	 */
	public Vertex() {
		vertexType = 0;
	}
	
	public Vertex(int x) { // 1D vertex
		xLoc = x;
		vertexType = VERTEX_1D;
	}
	
	public Vertex(int x, int y) { // 2D vertex
		xLoc = x;
		yLoc = y;
		vertexType = VERTEX_2D;
	}
	
	public Vertex(int x, int y, int z) { // 3D vertex
		xLoc = x;
		yLoc = y;
		zLoc = z;
		vertexType = VERTEX_3D;
	}

	public int getXLoc() {
		return xLoc;
	}

	public void setXLoc(int loc) {
		xLoc = loc;
	}

	public int getYLoc() {
		return yLoc;
	}

	public void setYLoc(int loc) {
		yLoc = loc;
	}

	public int getZLoc() {
		return zLoc;
	}

	public void setZLoc(int loc) {
		zLoc = loc;
	}

	public int getVertexType() {
		return vertexType;
	}

	public void setVertexType(int vertexType) {
		this.vertexType = vertexType;
	}
}
