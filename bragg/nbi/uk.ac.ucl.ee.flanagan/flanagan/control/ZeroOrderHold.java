/*          Class ZeroOrderHold
*
*           This class contains the constructor to create an instance of
*           a zero order hold (ZOH) and the methods needed to use this ZOH
*           in control loops in the time domain, Laplace transform s domain
*           or the z-transform z domain.
*
*           s-domain transfer function = (1 - exp(-Td.s))/s
*           Td is the delay time.
*           Pade approximation always used in s-domain
*           1 to 4 order Pade approximations available
*
*           This class is a subclass of the superclass BlackBox.
*
*           Author:  Michael Thomas Flanagan.
*
*           Created: 26 June 2003.
*           Updated: 2 July 2006
*
*
*           DOCUMENTATION:
*           See Michael T Flanagan's JAVA library on-line web page:
*           ZeroOrderHold.html
*
*   Copyright (c) July 2006   Michael Thomas Flanagan
*
*   PERMISSION TO COPY:
*   Permission to use, copy and modify this software and its documentation for
*   NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*   to the author, Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies.
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability
*   or fitness of the software for any or for a particular purpose.
*   Michael Thomas Flanagan shall not be liable for any damages suffered
*   as a result of using, modifying or distributing this software or its derivatives.
*
***************************************************************************************/

package flanagan.control;

import flanagan.complex.Complex;
import flanagan.complex.ComplexPoly;

public class ZeroOrderHold extends BlackBox{

    // Constructor
    public ZeroOrderHold(double deltaT, int orderPade){
        super("ZeroOrderHold");
        super.setDeltaT(deltaT);
        super.setPadeOrder(orderPade);
        this.setNumDen(deltaT);
    }

    // Constructor
    // Default Pade approximation order = 2
    public ZeroOrderHold(double deltaT){
        super("ZeroOrderHold");
        super.setDeltaT(deltaT);
        this.setNumDen(deltaT);
    }

    // set the numerators and denominators
    // same polynomials, using Pade approximation for Pade and non-Pade forms
    public void setNumDen(double deltaT){
        // set denominator, s
        super.sDenom = new ComplexPoly(0.0D, 1.0D);
        super.sPoles[0].reset(0.0D, 0.0D);
        super.sPoles = Complex.oneDarray(1);

        // set exp(-sT) part of pade numerator
        super.sNumer = new ComplexPoly(1.0D);
        super.deadTime = deltaT;
        super.pade();
        super.deadTime = 0.0D;

        // add 1 to exp(-sT)[=padeNumer/padeDenom]/s
        super.sNumerPade = super.sNumerPade.plus(super.sDenomPade);
        super.sZerosPade = sNumerPade.roots();
    }
}




