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

package au.gov.ansto.bragg.errorpropagation;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.StringTokenizer;

/**
 * Utility methods for assisting in presentation of results and errors to the
 * correct number of significant figures.
 * 
 * Operations are generally implemented by string processing, since they are 
 * generally formatting operations on the representation of numbers, rather than
 * mathematical operations. There are mathematical implications of the operations,
 * though, because we are changing the values of variables.
 * 
 * Presentation of the final values is not considered - we are only interested
 * in obtaining the figures to the correct number of significant figures.
 * 
 * @author lwi
 */
public class ErrorManager {

	private DecimalFormat numberFormat;
	
	public ErrorManager() {
		// FIXME
		numberFormat = new DecimalFormat("0.0###########################################E0");
	}
	/**
	 * Present error to a single significant figure, and round result to
	 * an appropriate, matching number of significant figures.
	 * 
	 * This method accept a pair of doubles representing a measurement and the
	 * corresponding measurement error. The error figure should be presented with 
	 * a single significant figure, and the measurement to a number of figures 
	 * equal to the difference in magnitude between the measurement and error
	 * figures.
	 */
	public double [] formatResultAndError(double [] resultWithError) {
		double data = resultWithError[0];
		double error = resultWithError[1];
		// If the error is zero, we do not do any rounding. Just return
		// everything as it is.
		if (error == 0) {
			return new double [] {data, error};
		}
		int roundingAmount = getMagnitudeDifference(data, error) + 1;
		String dataExponent = getExponent(data);
		String errorExponent = getExponent(error);
		String dataSigFigs = getSignificantFigures(data, roundingAmount + 1);
		String errorSigFigs = getSignificantFigures(error, 2);

		// Rounding is done on the mantissas
		double roundedData = round(new Double(dataSigFigs), roundingAmount - 1);
		double roundedError = round(new Double(errorSigFigs), 0);
		
		// Now re-chop rounded figures
		String finalData = getSignificantFigures(roundedData, roundingAmount); 
		String finalError = getSignificantFigures(roundedError, 1);
		
		double formattedData = new Double(finalData + "E" + dataExponent);
		double formattedError = new Double(finalError + "E" + errorExponent);
		
		return new double[]{formattedData, formattedError};
	}
	
	/**
	 * Round a number to a given number of decimal places.
	 */
	private double round(double input, int decimalPlaces) {
		
		BigDecimal bigDec = new BigDecimal(input);
		bigDec = bigDec.setScale(decimalPlaces, BigDecimal.ROUND_HALF_EVEN);
		return bigDec.doubleValue();
		// Shift, round, then shift back.
		// Shifting of the decimal point is managed as a string operation; managing it
		// mathematically by multiplication and division results in floating point errors
		// which give incorrect results.
//		int shift = decimalPlaces - 1; // Because there is already one figure in front of the point.
//		
//		// Move decimal point 'shift' positions to the left.
//		double toRound = input * Math.pow(10, shift);
//		
//		long rounded = Math.round(toRound);
//		
//		// Move decimal point 'shift' positions to the right.
//		double result = rounded * Math.pow(10, -shift);
//		
//		return result;
	}
	
	/**
	 * Get difference in magnitude between two numbers.
	 * 
	 * In particular, we are interested in the difference in magnitude between
	 * the data and the error, because this defines how many significant figures
	 * should be used in quoting the data.
	 * 
	 * The returned result refers to the numbers of powers of ten magnitude
	 * difference between the two figures.
	 */
	private int getMagnitudeDifference(double input1, double input2) {
		// Determine which number is larger
		// Compute difference in exponents
		// Return this difference with an appropriate sign, according to
		// which of the input parameters was larger
		
		String exponent1 = getExponent(input1);
		String exponent2 = getExponent(input2);
		int result = Integer.parseInt(exponent1) - Integer.parseInt(exponent2);
		return result;
	}
	
	/**
	 * Get <code>data</code> represented to <code>numFigures</code>
	 * significant figures.
	 * 
	 * In other words, select the first <code>numFigures</code> figures
	 * from <code>data</code>.
	 */
	private String getSignificantFigures(double data, int numFigures) {
		// We need to get the first numFigures numerical figures of the
		// mantissa. We can do this by taking an appropriate length
		// substring, allowing a space for the decimal point.
		// The sign of the number should be disregarded, and will
		// therefore be trimmed before extracting the significant figures.
		
		if (data == 0) {
			return "0";
		}
		
		String mantissa = getMantissa(data);
		int startIndex = 0;
		if (mantissa.startsWith("-")) {
			startIndex = 1;
		}
		
		// But if the mantissa is already shorter than the precision we want, 
		// we obviously don't need to truncate it further.
		int offset = numFigures + 1 + startIndex;
		if (mantissa.length() <= offset) {
			return mantissa;
		}
		String result = mantissa.substring(startIndex, offset);
		return result;
	}
	
	private String getMantissa(double number) {
		String formattedNumber = numberFormat.format(number);
		StringTokenizer tokenizer = new StringTokenizer(formattedNumber, "E");
		String mantissa = tokenizer.nextToken();
//		String exponent = tokenizer.nextToken();
		return mantissa;
	}
	
	private String getExponent(double number) {
		String formattedNumber = numberFormat.format(number);
		StringTokenizer tokenizer = new StringTokenizer(formattedNumber, "E");
//		String mantissa = 
		tokenizer.nextToken();
		String exponent = tokenizer.nextToken();
		return exponent;
	}
	
	/**
	 * For command line testing of private methods as they are developed.
	 * Normal unit testing will be used for assemblies.
	 */
	public static void main(String[] args) {
		ErrorManager errorManager = new ErrorManager();
		
		// First test splitting into mantissa and exponent
		// double input = 10000000000d;
		double input = -0.000000000017698d;
		String mantissa = errorManager.getMantissa(input);
		String exponent = errorManager.getExponent(input);
		System.out.println("Mantissa = " + mantissa);
		System.out.println("Exponent = " + exponent);
		
		// Use mantissa and exponent to create a copy of the number and check 
		// that the copy is equal to the original
		String copyString = mantissa + "E" + exponent;
		double copy = new Double(copyString);
		
		System.out.println(copy == input);
		
		// Second check that we can extract a given number of significant figures
		input = -123456789;
		String sigFigs = errorManager.getSignificantFigures(input, 3);
		System.out.println("Significant figures = " + sigFigs);
		
		// Confirm that we can compute magnitude differences between numbers.
		double figure1 = 200000d; // 10E5
		double figure2 = 1000d; // 10E3
		// Magnitude difference should be 2.
		int magnitudeDifference = errorManager.getMagnitudeDifference(figure1, figure2);
		System.out.println("Magnitude difference = " + magnitudeDifference);
		
		// Finally, make sure that we can get an appropriate number of
		// significant figures from a number according to the matching error term.
		double data = 123456789;
		double error = 5600;
		double [] formatted = errorManager.formatResultAndError(new double[]{data, error});
		System.out.println("Data = " + formatted[0] + ", Error = " + formatted[1]);
	}
}
