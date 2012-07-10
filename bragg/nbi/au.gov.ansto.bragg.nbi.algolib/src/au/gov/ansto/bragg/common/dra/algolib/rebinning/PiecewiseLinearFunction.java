/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lindsay Winkler (Bragg Institute) - initial implementation
 *******************************************************************************/

package au.gov.ansto.bragg.common.dra.algolib.rebinning;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

/**
 * A function made up piecewise of linear functions defined over sub-intervals.
 * 
 * @author lwi
 */
public class PiecewiseLinearFunction extends AbstractDistributionFunction {
	
	private List<LinearFunction> linearPieces;
	private List<Double> divisionPoints;
	
	// FIXME
	// Convert Point to Point2D.Double
	public PiecewiseLinearFunction(List<Point2D> points) {
		linearPieces = new LinkedList<LinearFunction>();
		divisionPoints = new LinkedList<Double>();

		// FIXME
		// Confirm that points are in order, cover the domain and 
		// the intervals they define are exclusive.
		
		Point2D previousPoint = null;
		for (Point2D currentPoint : points) {
			divisionPoints.add(currentPoint.getX());
			if (previousPoint != null) {
				linearPieces.add(new LinearFunction(previousPoint, currentPoint));
			}
			previousPoint = currentPoint;
		}
	}

	public double getArea(Interval interval) {
		double result = 0;
		// 1. Find the function pieces which fall into the interval for evaluation.
		// 2. Split the interval in domains of definition for those function pieces.
		// 3. Evaluate the resulting area under each function piece and sum into result.
		for (LinearFunction functionPiece : linearPieces) {
			Interval domainOfDefinition = functionPiece.getDomainOfDefinition();
			Interval intersection = interval.intersection(domainOfDefinition); 
			if (intersection != null) {
				double currentArea = functionPiece.getArea(intersection);
				result += currentArea;
			}
		}
		
		return result;
	}

	public double getFunctionValue(double argument) {
		// Find out which linear function piece the point belongs to and
		// simply evaluate that function piece at the point.
		for (LinearFunction functionPiece : linearPieces) {
			Interval domainOfDefinition = functionPiece.getDomainOfDefinition();
			if (domainOfDefinition.contains(argument)) {
				return functionPiece.getFunctionValue(argument);
			}
		}
		// We should not be able to reach this point.
		return 0;
	}

}
