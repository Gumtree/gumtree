/*      Class FirstOrder
*
*       This class contains the constructor to create an instance of
*       a first order process,
*           a.d(output)/dt + b.output  =  c.input
*       and the methods needed to use this process in simulation
*       of control loops.
*
*       This class is a subclass of the superclass BlackBox.
*
*       Author:  Michael Thomas Flanagan.
*
*       Created: August 2002
*       Updated: 20 April 2003, 3 May 2005, 3 April 2006, 2 July 2006
*
*
*       DOCUMENTATION:
*       See Michael T Flanagan's JAVA library on-line web page:
*       FirstOrder.html
*
*   Copyright (c) July 2006  Michael Thomas Flanagan
*
*   PERMISSION TO COPY:
*   Permission to use, copy and modify this software and its documentation for
*   NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*   to the author, Michael Thomas Flanagan at www.ee.ac.uk/~mflanaga, appears in all copies.
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

public class FirstOrder extends BlackBox{

    private double aConst = 1.0D;  // a constant in differential equation above
    private double bConst = 1.0D;  // b constant in differential equation above
    private double cConst = 1.0D;  // c constant in differential equation above

    // Constructor
    // Sets all constants to unity
    public FirstOrder(){
        super("First Order Process");
        super.setSnumer(new ComplexPoly(1.0D));
        super.setSdenom(new ComplexPoly(1.0D, 1.0D));
        super.setZtransformMethod(1);
        super.addDeadTimeExtras();
    }

    // Constructor
    // within constants set from argument list
    public FirstOrder(double aa, double bb, double cc){
        super("First Order Process");
        this.aConst = aa;
        this.bConst = bb;
        this.cConst = cc;
        super.setSnumer(new ComplexPoly(this.cConst));
        super.setSdenom(new ComplexPoly(this.bConst, this.aConst));
        super.sPoles = Complex.oneDarray(1);
        super.setZtransformMethod(1);
        super.addDeadTimeExtras();
    }

    // Set coefficients
    public void setCoeff(double aa, double bb, double cc){
        this.aConst = aa;
        this.bConst = bb;
        this.cConst = cc;
        Complex[] num = Complex.oneDarray(1);
        num[0].reset(this.cConst, 0.0);
        super.sNumer.resetPoly(num);
        Complex[] den = Complex.oneDarray(2);
        den[0].reset(this.bConst, 0.0);
        den[1].reset(this.aConst, 0.0);
        super.sDenom.resetPoly(den);
        this.calcPolesZerosS();
        super.addDeadTimeExtras();
    }

    public void setA(double aa){
        this.aConst = aa;
        Complex co = new Complex(this.aConst, 0.0);
        super.sDenom.resetCoeff(1, co);
        this.calcPolesZerosS();
        super.addDeadTimeExtras();
    }

    public void setB(double bb){
        this.bConst = bb;
        Complex co = new Complex(this.bConst, 0.0);
        super.sDenom.resetCoeff(0, co);
        this.calcPolesZerosS();
        super.addDeadTimeExtras();
    }

    public void setC(double cc){
        this.cConst = cc;
        Complex co = new Complex(this.cConst, 0.0);
        super.sNumer.resetCoeff(0, co);
        this.calcPolesZerosS();
        super.addDeadTimeExtras();
    }

    // Get coefficients
    public double getA(){
        return this.aConst;
    }

    public double getB(){
        return this.bConst;
    }

    public double getC(){
        return this.cConst;
    }

    // Get time constant
    public double getTimeConstant(){
        return this.aConst/this.bConst;
    }

    // Calculate the zeros and poles in the s-domain
    protected void calcPolesZerosS(){
        super.sPoles[0].setReal(-bConst/aConst);
    }

    // Perform z transform using an already set delta T
    public void zTransform(){
        if(super.deltaT==0.0D)System.out.println("z-transform attempted in FirstOrder with a zero sampling period");
        super.deadTimeWarning("zTransform");
        if(ztransMethod==0){
            this.mapstozAdHoc();
        }
        else{
            Complex[] ncoef = null;
            Complex[] dcoef = null;
            switch(this.integMethod){
                // Trapezium rule
                case 0: ncoef = Complex.oneDarray(2);
                        ncoef[0].reset(this.deltaT*this.cConst,0.0D);
                        ncoef[1].reset(this.deltaT*this.cConst,0.0D);
                        super.zNumer=new ComplexPoly(1);
                        super.zNumer.resetPoly(ncoef);
                        super.zNumerDeg=1;
                        dcoef = Complex.oneDarray(2);
                        dcoef[0].reset(this.bConst*this.deltaT - 2*this.aConst,0.0D);
                        dcoef[1].reset(this.bConst*this.deltaT + 2*this.aConst,0.0D);
                        super.zDenom=new ComplexPoly(1);
                        super.zDenom.resetPoly(dcoef);
                        super.zDenomDeg=1;
                        super.zZeros = Complex.oneDarray(1);
                        super.zZeros[0].reset(-1.0D, 0.0D);
                        super.zPoles = Complex.oneDarray(1);
                        super.zPoles[0].reset((2.0D*this.aConst-super.deltaT*this.bConst)/(2.0D*this.aConst+super.deltaT*this.bConst), 0.0D);
                        break;
                // Backward rectangulr rule
                case 1: ncoef = Complex.oneDarray(2);
                        ncoef[0].reset(0.0D,0.0D);
                        ncoef[1].reset(this.cConst*this.deltaT,0.0D);
                        super.zNumer=new ComplexPoly(1);
                        super.zNumer.resetPoly(ncoef);
                        super.zNumerDeg=1;
                        dcoef = Complex.oneDarray(2);
                        dcoef[0].reset(this.bConst*this.deltaT + this.aConst,0.0D);
                        dcoef[1].reset(this.aConst,0.0D);
                        super.zDenom=new ComplexPoly(1);
                        super.zDenom.resetPoly(dcoef);
                        super.zDenomDeg=1;
                        super.zZeros = Complex.oneDarray(1);
                        super.zZeros[0].reset(0.0D, 0.0D);
                        super.zPoles = Complex.oneDarray(1);
                        super.zPoles[0].reset(this.aConst/(super.deltaT*this.bConst+this.aConst), 0.0D);
                        break;
                // Foreward rectangular rule
                case 2: ncoef = Complex.oneDarray(1);
                        ncoef[0].reset(this.cConst*this.deltaT,0.0D);
                        super.zNumer=new ComplexPoly(0);
                        super.zNumer.resetPoly(ncoef);
                        super.zNumerDeg=0;
                        dcoef = Complex.oneDarray(2);
                        dcoef[0].reset(-this.aConst,0.0D);
                        dcoef[1].reset(this.bConst*this.deltaT - this.aConst,0.0D);
                        super.zDenom=new ComplexPoly(1);
                        super.zDenom.resetPoly(dcoef);
                        super.zDenomDeg=1;
                        super.zPoles = Complex.oneDarray(1);
                        super.zPoles[0].reset(this.aConst/(super.deltaT*this.bConst-this.aConst), 0.0D);
                        break;
                default:    System.out.println("Integration method option in FirstOrder must be 0,1 or 2");
                            System.out.println("It was set at "+integMethod);
                            System.out.println("z-transform not performed");
            }
        }
    }

    // Perform z transform setting delta T
    public void zTransform(double deltaT){
        super.deltaT=deltaT;
        zTransform();
    }

    //  Get the s-domain output for a given s-value and a given input.
    public Complex getOutputS(Complex sValue, Complex iinput){
        super.sValue=sValue;
        super.inputS=iinput;
        return this.getOutputS();
    }

    //  Get the s-domain output for the stored input and  s-value.
    public Complex getOutputS(){
        Complex num = Complex.plusOne();
        num = num.times(this.cConst);
        Complex den = new Complex();
        den = this.sValue.times(this.aConst);
        den = den.plus(this.bConst);
        Complex term = new Complex();
        term = num.over(den);
        super.outputS = term.times(super.inputS);
        if(super.deadTime!=0.0D)super.outputS = super.outputS.times(Complex.exp(super.sValue.times(-super.deadTime)));
        return super.outputS;
    }

    //  Calculate the current time domain output for a given input and given time
    //  resets deltaT
    public void calcOutputT(double ttime, double inp){
        if(ttime<=time[this.sampLen-1])throw new IllegalArgumentException("Current time equals or is less than previous time");
        super.deltaT = ttime - super.time[this.sampLen-1];
        super.sampFreq = 1.0D/super.deltaT;
        super.deadTimeWarning("calcOutputT(time, input)");
        for(int i=0; i<super.sampLen-2; i++){
            super.time[i]=super.time[i+1];
            super.inputT[i]=super.inputT[i+1];
            super.outputT[i]=super.outputT[i+1];
        }
        super.time[super.sampLen-1]=ttime;
        super.inputT[super.sampLen-1]=inp;
        super.outputT[super.sampLen-1]=Double.NaN;
        this.calcOutputT();
    }

    //  Get the output for the stored sampled input, time and deltaT.
    public void calcOutputT(){
        super.deadTimeWarning("calcOutputT()");
        super.outputT[sampLen-1] = (this.bConst*super.inputT[sampLen-1] + this.aConst*(super.inputT[sampLen-1]-super.inputT[sampLen-2])/super.deltaT)/this.cConst;
    }


    // Get the s-domain zeros
    public Complex[] getSzeros(){
        System.out.println("This standard first order process (class FirstOrder) has no s-domain zeros");
        return null;
    }
 }

