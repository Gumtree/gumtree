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

/**
 * A function representing a straight line between two points.
 * 
 * @author lwi
 */
public class LinearFunction extends AbstractDistributionFunction {
	
	private Point2D point1;
	private Point2D point2;
	private Interval domainOfDefinition;
	
	public LinearFunction(Point2D point1, Point2D point2) {
		this.point1 = point1;
		this.point2 = point2;
		domainOfDefinition = new Interval(point1.getX(), point2.getX());
	}
	
	public Interval getDomainOfDefinition() {
		return domainOfDefinition;
	}

	public double getArea(Interval interval) {
		if (!domainOfDefinition.contains(interval)) {
			String errorMessage = "interval is not contained in the domain of definition";
			throw new IllegalArgumentException(errorMessage);
		}
		double areaA = getAreaA(interval);
		double areaB = getAreaB(interval);
		double result = areaA + areaB;
		return result;
	}

	public double getFunctionValue(double argument) {
		if (!domainOfDefinition.contains(argument)) {
			String errorMessage = "argument is not contained in the domain of definition";
			throw new IllegalArgumentException(errorMessage);
		}
		double gradient = getGradient();
		double result = gradient * (argument - point1.getX()) + point1.getY();
		return result;
	}
	
	private double getHeight1(Interval interval) {
		double start = interval.getStart();
		double end = interval.getEnd();
		double startValue = getFunctionValue(start);
		double endValue = getFunctionValue(end);
		return Math.min(startValue, endValue);
	}
	
	private double getHeight2(Interval interval) {
		double start = interval.getStart();
		double end = interval.getEnd();
		double startValue = getFunctionValue(start);
		double endValue = getFunctionValue(end);
		return Math.max(startValue, endValue);
	}
	
	private double getGradient() {
		double heightDifference = point2.getY() - point1.getY();
		double overallWidth = domainOfDefinition.getSize();
		double result = heightDifference / overallWidth;
		return result;
	}
	
	private double getAreaA(Interval interval) {
		return ((getHeight2(interval) - getHeight1(interval))* interval.getSize()) / 2;
		
	}

	private double getAreaB(Interval interval) {
		
		double height1 = getHeight1(interval);
		double intervalSize = interval.getSize();
		return height1 * intervalSize;
	}
}
