/*      Class SecondOrder
*
*       This class contains the constructor to create an instance of
*       a second order process,
*           a.d^2(output)/dt^2 + b.d(output)/dt + c.output  =  d.input
*       and the methods needed to use this process in simulation
*       of control loops.
*
*       This class is a subclass of the superclass BlackBox.
*
*       Author:  Michael Thomas Flanagan.
*
*       Created: March 2003
*       Updated: 23 April 2003, 3 May 2005, 3 April 2006, 2 July 2006
*
*
*       DOCUMENTATION:
*       See Michael T Flanagan's JAVA library on-line web page:
*       SecondOrder.html
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

public class SecondOrder extends BlackBox{

    private double aConst = 1.0D;  // a constant in differential equation above
    private double bConst = 1.0D;  // b constant in differential equation above
    private double cConst = 1.0D;  // c constant in differential equation above
    private double dConst = 1.0D;  // d constant in differential equation above
    private double omegaN = 1.0D;  // undamped natural frequency (resonant frequency)
    private double zeta = 1.0D;    // damping ratio
    private double kConst = 1.0D;  // the standard form gain constant
    private double sigma = 1.0D;   // attenuation (zeta*omegaN)

    // Constructor
    // Sets all constants to unity
    public SecondOrder(){
        super("Second Order Process");
        super.setSnumer(new ComplexPoly(1.0D));
        super.setSdenom(new ComplexPoly(1.0D, 1.0D, 1.0D));
        super.setZtransformMethod(1);
        super.addDeadTimeExtras();
    }

    // Constructor
    // within constants set from argument list
    public SecondOrder(double aa, double bb, double cc, double dd){
        super("Second Order Process");
        this.aConst = aa;
        this.bConst = bb;
        this.cConst = cc;
        this.dConst = dd;
        if(this.cConst>0.0D)this.standardForm();
        super.setSnumer(new ComplexPoly(this.dConst));
        super.setSdenom(new ComplexPoly(this.cConst, this.bConst, this.aConst));
        super.setZtransformMethod(1);
        super.addDeadTimeExtras();
    }

    // Set a, b, c and d
    public void setCoeff(double aa, double bb, double cc, double dd){
        this.aConst = aa;
        this.bConst = bb;
        this.cConst = cc;
        this.dConst = dd;
        if(this.cConst>0.0D)this.standardForm();
        Complex[] num = Complex.oneDarray(1);
        num[0].reset(this.dConst, 0.0);
        super.sNumer.resetPoly(num);
        Complex[] den = Complex.oneDarray(3);
        den[0].reset(this.cConst, 0.0);
        den[1].reset(this.bConst, 0.0);
        den[2].reset(this.aConst, 0.0);
        super.sDenom.resetPoly(den);
        super.fixedName = "Second Order Process";
        this.calcPolesZerosS();
        super.addDeadTimeExtras();
    }

    // Private method for setting the contants of the ntural frequency standard form
    private void standardForm(){
            this.omegaN = Math.sqrt(this.cConst/this.aConst);
            this.zeta = this.bConst/(2.0D*this.aConst*this.omegaN);
            this.kConst = this.dConst/this.cConst;
            this.sigma = this.zeta*this.omegaN;
    }

    public void setA(double aa){
        this.aConst = aa;
        Complex co = new Complex(this.aConst, 0.0);
        super.sDenom.resetCoeff(2, co);
        if(this.cConst>0.0D)this.standardForm();
        this.calcPolesZerosS();
        super.addDeadTimeExtras();
    }

    public void setB(double bb){
        this.bConst = bb;
        Complex co = new Complex(this.bConst, 0.0);
        super.sDenom.resetCoeff(1, co);
        if(this.cConst>0.0D)this.standardForm();
        this.calcPolesZerosS();
        super.addDeadTimeExtras();
    }

    public void setC(double cc){
        this.cConst = cc;
        Complex co = new Complex(this.cConst, 0.0);
        super.sDenom.resetCoeff(0, co);
        if(this.cConst>0.0D)this.standardForm();
        this.calcPolesZerosS();
        super.addDeadTimeExtras();
    }

    public void setD(double dd){
        this.dConst = dd;
        Complex co = new Complex(this.dConst, 0.0);
        super.sNumer.resetCoeff(0, co);
        if(this.cConst>0.0D)this.standardForm();
        this.calcPolesZerosS();
        super.addDeadTimeExtras();
    }

    public void setStandardForm(double zet, double omega, double kk){
        if(omega<=0)throw new IllegalArgumentException("zero or negative natural frequency");
        if(zet<0)throw new IllegalArgumentException("negative damping ratio");
        this.zeta = zet;
        this.omegaN = omega;
        this.kConst = kk;
        this.sigma = this.omegaN*this.zeta;
        this.reverseStandard();
        this.calcPolesZerosS();
        super.addDeadTimeExtras();
    }

    public void setZeta(double zet){
        if(zet<0)throw new IllegalArgumentException("negative damping ratio");
        this.zeta = zet;
        this.sigma = this.omegaN*this.zeta;
        this.reverseStandard();
        this.calcPolesZerosS();
        super.addDeadTimeExtras();
    }

    public void setOmegaN(double omega){
        if(omega<=0)throw new IllegalArgumentException("zero or negative natural frequency");
        this.omegaN = omega;
        this.sigma = this.omegaN*this.zeta;
        this.reverseStandard();
        this.calcPolesZerosS();
        super.addDeadTimeExtras();
    }

    public void setK(double kk){
        this.kConst = kk;
        this.reverseStandard();
        this.calcPolesZerosS();
        super.addDeadTimeExtras();
    }

    // Private method for obtaining a, b c and d from zeta, omegan and k
    private void reverseStandard(){
        this.aConst = this.omegaN*this.omegaN;
        this.bConst = 2.0D*this.zeta*this.omegaN;
        this.cConst = 1.0D;
        this.dConst = this.kConst*this.aConst;
        Complex[] num = Complex.oneDarray(1);
        num[0].reset(this.dConst, 0.0);
        super.sNumer.resetPoly(num);
        Complex[] den = Complex.oneDarray(3);
        den[0].reset(this.cConst, 0.0);
        den[1].reset(this.bConst, 0.0);
        den[2].reset(this.aConst, 0.0);
        super.sDenom.resetPoly(den);
    }

    public double getA(){
        return this.aConst;
    }

    public double getB(){
        return this.bConst;
    }

    public double getC(){
        return this.cConst;
    }

    public double getD(){
        return this.dConst;
    }

    public double getOmegaN(){
        return this.omegaN;
    }

    public double getZeta(){
        return this.zeta;
    }

    public double getK(){
        return this.kConst;
    }

    public double getAttenuation(){
        return this.sigma;
    }

    // Calculate the zeros and poles in the s-domain
    protected void calcPolesZerosS(){
        super.sPoles = super.sDenom.roots();
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
        num = num.times(this.dConst);
        Complex den = new Complex();
        den = this.sValue.times(this.sValue.times(this.aConst));
        den = den.plus(this.sValue.times(this.aConst));
        den = den.plus(this.cConst);
        Complex term = new Complex();
        term = num.over(den);
        super.outputS = term.times(super.inputS);
        if(super.deadTime!=0.0D)super.outputS = super.outputS.times(Complex.exp(super.sValue.times(-super.deadTime)));
        return super.outputS;
    }

    // Perform z transform using an already set delta T
    public void zTransform(){
        if(super.deltaT==0.0D)System.out.println("z-transform attempted in SecondOrder with a zero sampling period");
        if(ztransMethod==0){
            this.mapstozAdHoc();
        }
        else{
            Complex[] ncoef = null;
            Complex[] dcoef = null;
            double bT = this.bConst*this.deltaT;
            double t2 = this.deltaT*this.deltaT;
            double cT2 = this.cConst*t2;
            double dT2 = this.dConst*t2;
            switch(this.integMethod){
                // Trapezium Rule
                case 0: ncoef = Complex.oneDarray(3);
                        ncoef[0].reset(dT2/4.0D, 0.0D);
                        ncoef[1].reset(dT2/2.0D, 0.0D);
                        ncoef[2].reset(dT2/4.0D, 0.0D);
                        super.zNumer=new ComplexPoly(2);
                        super.zNumer.resetPoly(ncoef);
                        super.zNumerDeg=2;
                        dcoef = Complex.oneDarray(3);
                        dcoef[0].reset(this.aConst - bT + cT2/4.0D, 0.0D);
                        dcoef[1].reset(-2.0D*this.aConst + bT + cT2/2.0D, 0.0D);
                        dcoef[2].reset(this.aConst + cT2/4.0D, 0.0D);
                        super.zDenom=new ComplexPoly(2);
                        super.zDenom.resetPoly(dcoef);
                        super.zDenomDeg=2;
                        super.zZeros = zNumer.roots();
                        super.zPoles = zDenom.roots();
                        break;
                //  Backward Rectangular Rule
                case 1: ncoef = Complex.oneDarray(3);
                        ncoef[0].reset(0.0D, 0.0D);
                        ncoef[1].reset(0.0D, 0.0D);
                        ncoef[2].reset(dT2, 0.0D);
                        super.zNumer=new ComplexPoly(2);
                        super.zNumer.resetPoly(ncoef);
                        super.zNumerDeg=2;
                        dcoef = Complex.oneDarray(3);
                        dcoef[0].reset(this.aConst - bT, 0.0D);
                        dcoef[1].reset(-2.0D*this.aConst, 0.0D);
                        dcoef[2].reset(this.aConst + bT + cT2, 0.0D);
                        super.zDenom=new ComplexPoly(2);
                        super.zDenom.resetPoly(dcoef);
                        super.zDenomDeg=2;
                        super.zPoles = zDenom.roots();
                        super.zZeros = Complex.oneDarray(2);
                        super.zZeros[0].reset(0.0D, 0.0D);
                        super.zZeros[1].reset(0.0D, 0.0D);
                        break;
                // Foreward Rectangular Rule
                case 2: ncoef = Complex.oneDarray(3);
                        ncoef[0].reset(0.0D, 0.0D);
                        ncoef[1].reset(0.0D, 0.0D);
                        ncoef[2].reset(dT2, 0.0D);
                        super.zNumer=new ComplexPoly(2);
                        super.zNumer.resetPoly(ncoef);
                        super.zNumerDeg=2;
                        dcoef = Complex.oneDarray(3);
                        dcoef[0].reset(this.aConst - bT + cT2, 0.0D);
                        dcoef[1].reset(-2.0D*this.aConst + bT, 0.0D);
                        dcoef[2].reset(this.aConst, 0.0D);
                        super.zDenom=new ComplexPoly(2);
                        super.zDenom.resetPoly(dcoef);
                        super.zDenomDeg=2;
                        super.zPoles = zDenom.roots();
                        super.zZeros = Complex.oneDarray(2);
                        super.zZeros[0].reset(0.0D, 0.0D);
                        super.zZeros[1].reset(0.0D, 0.0D);
                        break;
                default:    System.out.println("Integration method option in SecondOrder must be 0,1 or 2");
                            System.out.println("It was set at "+integMethod);
                            System.out.println("z-transform not performed");
            }
        }
    }

    // Perform z transform setting delta T
    public void zTransform(double deltaT){
        super.deltaT=deltaT;
        super.deadTimeWarning("zTransform");
        zTransform();
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
        super.outputT[sampLen-1] = (this.cConst*super.inputT[sampLen-1] + this.bConst*(super.inputT[sampLen-1]-super.inputT[sampLen-3])/super.deltaT + this.cConst*(super.inputT[sampLen-1]-2.0D*super.inputT[sampLen-2]+super.inputT[sampLen-3])/(super.deltaT*super.deltaT))/this.dConst;
    }

    // Get the s-domain zeros
    public Complex[] getSzeros(){
        System.out.println("This standard second order process (class SecondOrder) has no s-domain zeros");
        return null;
    }
 }

