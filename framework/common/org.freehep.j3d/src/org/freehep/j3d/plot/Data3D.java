/**
 * 
 */
package org.freehep.j3d.plot;

import javax.vecmath.Color3b;

/**
 * @author nxi
 *
 */
public interface Data3D {

	int xBins();
	int yBins();
	float zAt(int xIndex, int yIndex);
	Color3b colorAt(int xIndex, int yIndex);
}
