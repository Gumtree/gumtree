/*  Class Matrix
*
*   Defines a matrix and includes the methods
*   needed for standard matrix manipulations, e.g. multiplation,
*   and related procedures, e.g. solution of linear
*   simultaneous equations
*
*   See class ComplexMatrix for  complex matrix arithmetic
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	 June 2002
*   UPDATES:  21 April 2004, 19 January 2005, 1 May 2005
*   UPDATES:  16 February 2006 Set methods corrected thanks to Myriam Servières, Equipe IVC , Ecole Polytechnique de l'université de Nantes, Laboratoire IRCCyN/UMR CNRS
*   UPDATES:  31 March 2006 Norm methods corrected thanks to Jinshan Wu, University of British Columbia
*   UPDATES:  22 April 2006 getSubMatrix methods corrected thanks to Joachim Wesner
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   Matrix.html
*
*   Copyright (c) April 2004  Michael Thomas Flanagan
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

package flanagan.math;

public class Matrix{

	private int nrow = 0;               	// number of rows
	private int ncol = 0;               	// number of columns
	private double matrix[][] = null; 	    // 2-D  Matrix
	private int index[] = null;            	// row permutation index
	private double dswap = 1.0D;           	// row swap index
	private boolean matrixCheck = true;     // check on matrix status
	                                	    // true - no problems encountered
	                                	    // false - attempted a LU decomposition on a singular matrix

	private static final double TINY = 1.0e-30;

	// CONSTRUCTORS
	// Construct a nrow x ncol matrix of variables all equal to zero
    	public Matrix(int nrow, int ncol){
		this.nrow = nrow;
		this.ncol = ncol;
	    	this.matrix = new double[nrow][ncol];
		this.index = new int[nrow];
        for(int i=0;i<nrow;i++)this.index[i]=i;
    }

	// Construct a nrow x ncol matrix of variables all equal to the number const
    	public Matrix(int nrow, int ncol, double constant){
		this.nrow = nrow;
		this.ncol = ncol;
		this.matrix = new double[nrow][ncol];
        	for(int i=0;i<nrow;i++){
            		for(int j=0;j<ncol;j++)this.matrix[i][j]=constant;
		}
		this.index = new int[nrow];
        for(int i=0;i<nrow;i++)this.index[i]=i;
 	}

	// Construct matrix with a reference to an existing nrow x ncol 2-D array of variables
    	public Matrix(double[][] twoD){
		this.nrow = twoD.length;
		this.ncol = twoD[0].length;
		for(int i=0; i<nrow; i++){
		    if(twoD[i].length!=ncol)throw new IllegalArgumentException("All rows must have the same length");
		}
		this.matrix = twoD;
		this.index = new int[nrow];
        for(int i=0;i<nrow;i++)this.index[i]=i;
   	}

	// Construct matrix with a reference to the 2D matrix and permutation index of an existing Matrix bb.
    	public Matrix(Matrix bb){
		this.nrow = bb.nrow;
		this.ncol = bb.ncol;
		this.matrix = bb.matrix;
		this.index = bb.index;
        this.dswap = bb.dswap;
 	}


    	// METHODS
    	// SET VALUES
    	// Set the matrix with a copy of an existing nrow x ncol 2-D matrix of variables
    	public void setTwoDarray(double[][] aarray){
		if(this.nrow != aarray.length)throw new IllegalArgumentException("row length of this Matrix differs from that of the 2D array argument");
		if(this.ncol != aarray[0].length)throw new IllegalArgumentException("column length of this Matrix differs from that of the 2D array argument");
		for(int i=0; i<nrow; i++){
		    	if(aarray[i].length!=ncol)throw new IllegalArgumentException("All rows must have the same length");
    			for(int j=0; j<ncol; j++){
    		    		this.matrix[i][j]=aarray[i][j];
    			}
		}
	}

    	// Set an individual array element
    	// i = row index
    	// j = column index
    	// aa = value of the element
    	public void setElement(int i, int j, double aa){
        	this.matrix[i][j]=aa;
    	}

    	// Set a sub-matrix starting with row index i, colum index j
    	public void setSubMatrix(int i, int j, double[][] subMatrix){
    	    int k = subMatrix.length;
    	    int l = subMatrix[0].length;
        	if(i>k)throw new IllegalArgumentException("row indices inverted");
        	if(j>l)throw new IllegalArgumentException("column indices inverted");
        	int n=k-i+1, m=l-j+1;
        	for(int p=0; p<n; p++){
            		for(int q=0; q<m; q++){
                		this.matrix[i+p][j+q] = subMatrix[p][q];
            		}
        	}
    	}

    	// Set a sub-matrix starting with row index i, colum index j
    	// and ending with row index k, column index l
    	public void setSubMatrix(int i, int j, int k, int l, double[][] subMatrix){
        	if(i>k)throw new IllegalArgumentException("row indices inverted");
        	if(j>l)throw new IllegalArgumentException("column indices inverted");
        	int n=k-i+1, m=l-j+1;
        	for(int p=0; p<n; p++){
            		for(int q=0; q<m; q++){
                		this.matrix[i+p][j+q] = subMatrix[p][q];
            		}
        	}
    	}

    	// Set a sub-matrix
    	// row = array of row indices
    	// col = array of column indices
    	public void setSubMatrix(int[] row, int[] col, double[][] subMatrix){
        	int n=row.length;
        	int m=col.length;
        	for(int p=0; p<n; p++){
            		for(int q=0; q<m; q++){
                		this.matrix[row[p]][col[q]] = subMatrix[p][q];
            		}
        	}
    	}

    	// Get the value of matrixCheck
    	public boolean getMatrixCheck(){
        	return this.matrixCheck;
    	}

    	// SPECIAL MATRICES
    	// Construct an identity matrix
    	public static Matrix identityMatrix(int nrow){
        	Matrix u = new Matrix(nrow, nrow);
        	for(int i=0; i<nrow; i++){
            		u.matrix[i][i]=1.0;
        	}
        	return u;
    	}

    	// Construct a scalar matrix
    	public static Matrix scalarMatrix(int nrow, double diagconst){
        	Matrix u = new Matrix(nrow, nrow);
        	double[][] uarray = u.getArrayReference();
        	for(int i=0; i<nrow; i++){
            		for(int j=i; j<nrow; j++){
                		if(i==j){
                    			uarray[i][j]= diagconst;
                		}
            		}
        	}
        	return u;
    	}

    	// Construct a diagonal matrix
    	public static Matrix diagonalMatrix(int nrow, double[] diag){
        	if(diag.length!=nrow)throw new IllegalArgumentException("matrix dimension differs from diagonal array length");
        	Matrix u = new Matrix(nrow, nrow);
        	double[][] uarray = u.getArrayReference();
        	for(int i=0; i<nrow; i++){
            		uarray[i][i]=diag[i];
        	}
        	return u;
    	}

    	// GET VALUES
        // Return the number of rows
    	public int getNrow(){
        	return this.nrow;
    	}

    	// Return the number of columns
    	public int getNcol(){
        	return this.ncol;
    	}

    	// Return a reference to the internal 2-D array
    	public double[][] getArrayReference(){
        	return this.matrix;
    	}

    	// Return a reference to the internal 2-D array
    	// included for backward compatibility with incorrect earlier documentation
    	public double[][] getArrayPointer(){
        	return this.matrix;
    	}

    	// Return a copy of the internal 2-D array
    	public double[][] getArrayCopy(){
        	double[][] c = new double[this.nrow][this.ncol];
		for(int i=0; i<nrow; i++){
		    	for(int j=0; j<ncol; j++){
		        	c[i][j]=this.matrix[i][j];
		    	}
		    }
        	return c;
    	}

        // Return  a single element of the internal 2-D array
    	public double getElement(int i, int j){
         	return this.matrix[i][j];
    	}

    	// Return a single element of the internal 2-D array
    	// included for backward compatibility with incorrect earlier documentation
    	public double getElementCopy(int i, int j){
         	return this.matrix[i][j];
    	}

    	// Return a single element of the internal 2-D array
    	// included for backward compatibility with incorrect earlier documentation
    	public double getElementPointer(int i, int j){
         	return this.matrix[i][j];
    	}

    	// Return a sub-matrix starting with row index i, column index j
    	// and ending with row index k, column index l
    	public Matrix getSubMatrix(int i, int j, int k, int l){
        	if(i>k)throw new IllegalArgumentException("row indices inverted");
        	if(j>l)throw new IllegalArgumentException("column indices inverted");
        	int n=k-i+1, m=l-j+1;
        	Matrix subMatrix = new Matrix(n, m);
        	double[][] sarray = subMatrix.getArrayReference();
        	for(int p=0; p<n; p++){
            		for(int q=0; q<m; q++){
                		sarray[p][q]= this.matrix[i+p][j+q];
            		}
        	}
        	return subMatrix;
    	}

    	// Return a sub-matrix
    	// row = array of row indices
   	    // col = array of column indices
    	public Matrix getSubMatrix(int[] row, int[] col){
        	int n = row.length;
        	int m = col.length;
        	Matrix subMatrix = new Matrix(n, m);
        	double[][] sarray = subMatrix.getArrayReference();
        	for(int i=0; i<n; i++){
            		for(int j=0; j<m; j++){
                		sarray[i][j]= this.matrix[row[i]][col[j]];
            		}
        	}
        	return subMatrix;
    	}

    	// Return a reference to the permutation index array
    	public int[]  getIndexReference(){
         	return this.index;
    	}

    	// Return a reference to the permutation index array
    	// included for backward compatibility with incorrect earlier documentation
    	public int[]  getIndexPointer(){
         	return this.index;
    	}

    	// Return a copy of the permutation index array
    	public int[]  getIndexCopy(){
        	int[] indcopy = new int[this.nrow];
        	for(int i=0; i<this.nrow; i++){
            		indcopy[i]=this.index[i];
        	}
        	return indcopy;
    	}

    	// Return the row swap index
    	public double getSwap(){
         	return this.dswap;
    	}

    	// COPY
    	// Copy a Matrix [static method]
    	public static Matrix copy(Matrix a){
    	    if(a==null){
    	        return null;
    	    }
    	    else{
        	    int nr = a.getNrow();
        	    int nc = a.getNcol();
        	    double[][] aarray = a.getArrayReference();
        	    Matrix b = new Matrix(nr,nc);
        	    b.nrow = nr;
        	    b.ncol = nc;
        	    double[][] barray = b.getArrayReference();
        	    for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		barray[i][j]=aarray[i][j];
            		}
        	    }
        	    for(int i=0; i<nr; i++)b.index[i] = a.index[i];
        	    return b;
        	}
    	}

    	// Copy a Matrix [instance method]
    	public Matrix copy(){
    	    if(this==null){
    	        return null;
    	    }
    	    else{
        	    int nr = this.nrow;
        	    int nc = this.ncol;
        	    Matrix b = new Matrix(nr,nc);
        	    double[][] barray = b.getArrayReference();
        	    b.nrow = nr;
        	    b.ncol = nc;
        	    for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		barray[i][j]=this.matrix[i][j];
            		}
        	    }
        	    for(int i=0; i<nr; i++)b.index[i] = this.index[i];
        	    return b;
        	}
    	}

    	// Clone a Matrix
    	public Object clone(){
            if(this==null){
    	        return null;
    	    }
    	    else{
    	        int nr = this.nrow;
        	    int nc = this.ncol;
        	    Matrix b = new Matrix(nr,nc);
        	    double[][] barray = b.getArrayReference();
        	    b.nrow = nr;
        	    b.ncol = nc;
        	    for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		barray[i][j]=this.matrix[i][j];
            		}
        	    }
        	    for(int i=0; i<nr; i++)b.index[i] = this.index[i];
        	    return (Object) b;
        	}
    	}

    	// ADDITION
    	// Add this matrix to matrix B.  This matrix remains unaltered [instance method]
    	public Matrix plus(Matrix bmat){
        	if((this.nrow!=bmat.nrow)||(this.ncol!=bmat.ncol)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}
        	int nr=bmat.nrow;
        	int nc=bmat.ncol;
        	Matrix cmat = new Matrix(nr,nc);
        	double[][] carray = cmat.getArrayReference();
        	for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		carray[i][j]=this.matrix[i][j] + bmat.matrix[i][j];
            		}
        	}
        	return cmat;
    	}

    	// Add this matrix to 2-D array B.  This matrix remains unaltered [instance method]
    	public Matrix plus(double[][] bmat){
    	    int nr=bmat.length;
        	int nc=bmat[0].length;
        	if((this.nrow!=nr)||(this.ncol!=nc)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}

        	Matrix cmat = new Matrix(nr,nc);
        	double[][] carray = cmat.getArrayReference();
        	for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		carray[i][j]=this.matrix[i][j] + bmat[i][j];
            		}
        	}
        	return cmat;
    	}


    	// Add matrices A and B [static method]
    	public static Matrix plus(Matrix amat, Matrix bmat){
        	if((amat.nrow!=bmat.nrow)||(amat.ncol!=bmat.ncol)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}
        	int nr=amat.nrow;
        	int nc=amat.ncol;
        	Matrix cmat = new Matrix(nr,nc);
        	double[][] carray = cmat.getArrayReference();
        	for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		carray[i][j]=amat.matrix[i][j] + bmat.matrix[i][j];
            		}
        	}
        	return cmat;
    	}

    	// Add matrix B to this matrix [equivalence of +=]
    	public void plusEquals(Matrix bmat){
        	if((this.nrow!=bmat.nrow)||(this.ncol!=bmat.ncol)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}
        	int nr=bmat.nrow;
        	int nc=bmat.ncol;

        	for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		this.matrix[i][j] += bmat.matrix[i][j];
	            	}
        	}
    	}

    	// SUBTRACTION
    	// Subtract matrix B from this matrix.   This matrix remains unaltered [instance method]
    	public Matrix minus(Matrix bmat){
        	if((this.nrow!=bmat.nrow)||(this.ncol!=bmat.ncol)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}
        	int nr=this.nrow;
        	int nc=this.ncol;
        	Matrix cmat = new Matrix(nr,nc);
        	double[][] carray = cmat.getArrayReference();
        	for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		carray[i][j]=this.matrix[i][j] - bmat.matrix[i][j];
            		}
        	}
        	return cmat;
    	}

    	// Subtract a  2-D array from this matrix.  This matrix remains unaltered [instance method]
    	public Matrix minus(double[][] bmat){
    	    int nr=bmat.length;
        	int nc=bmat[0].length;
        	if((this.nrow!=nr)||(this.ncol!=nc)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}

        	Matrix cmat = new Matrix(nr,nc);
        	double[][] carray = cmat.getArrayReference();
        	for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		carray[i][j]=this.matrix[i][j] - bmat[i][j];
            		}
        	}
        	return cmat;
    	}


    	// Subtract matrix B from matrix A [static method]
    	public static Matrix minus(Matrix amat, Matrix bmat){
        	if((amat.nrow!=bmat.nrow)||(amat.ncol!=bmat.ncol)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}
        	int nr=amat.nrow;
        	int nc=amat.ncol;
        	Matrix cmat = new Matrix(nr,nc);
        	double[][] carray = cmat.getArrayReference();
        	for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		carray[i][j]=amat.matrix[i][j] - bmat.matrix[i][j];
            		}
        	}
        	return cmat;
    	}

    	// Subtract matrix B from this matrix [equivlance of -=]
    	public void minusEquals(Matrix bmat){
        	if((this.nrow!=bmat.nrow)||(this.ncol!=bmat.ncol)){
            		throw new IllegalArgumentException("Array dimensions do not agree");
        	}
        	int nr=bmat.nrow;
        	int nc=bmat.ncol;

        	for(int i=0; i<nr; i++){
            		for(int j=0; j<nc; j++){
                		this.matrix[i][j] -= bmat.matrix[i][j];
            		}
        	}
    	}

    	// MULTIPLICATION
    	// Multiply this  matrix by a matrix.   [instance method]
    	// This matrix remains unaltered.
    	public Matrix times(Matrix bmat){
        	if(this.ncol!=bmat.nrow)throw new IllegalArgumentException("Nonconformable matrices");

        	Matrix cmat = new Matrix(this.nrow, bmat.ncol);
        	double [][] carray = cmat.getArrayReference();
        	double sum = 0.0D;

        	for(int i=0; i<this.nrow; i++){
            		for(int j=0; j<bmat.ncol; j++){
                		sum=0.0D;
                		for(int k=0; k<this.ncol; k++){
                       			sum += this.matrix[i][k]*bmat.matrix[k][j];
                		}
                		carray[i][j]=sum;
            		}
        	}
        	return cmat;
    	}

    	// Multiply this  matrix by a 2-D array.   [instance method]
    	// This matrix remains unaltered.
    	public Matrix times(double[][] bmat){
    	    int nr=bmat.length;
        	int nc=bmat[0].length;

        	if(this.ncol!=nr)throw new IllegalArgumentException("Nonconformable matrices");

        	Matrix cmat = new Matrix(this.nrow, nc);
        	double [][] carray = cmat.getArrayReference();
        	double sum = 0.0D;

        	for(int i=0; i<this.nrow; i++){
            		for(int j=0; j<nc; j++){
                		sum=0.0D;
                		for(int k=0; k<this.ncol; k++){
                       			sum += this.matrix[i][k]*bmat[k][j];
                		}
                		carray[i][j]=sum;
            		}
        	}
        	return cmat;
    	}

    	// Multiply this matrix by a constant [instance method]
    	// This matrix remains unaltered
    	public Matrix times(double constant){
        	Matrix cmat = new Matrix(this.nrow, this.ncol);
        	double [][] carray = cmat.getArrayReference();

        	for(int i=0; i<this.nrow; i++){
            		for(int j=0; j<this.ncol; j++){
                  		carray[i][j] = this.matrix[i][j]*constant;
            		}
        	}
        	return cmat;
    	}

    	// Multiply two matrices {static method]
    	public static Matrix times(Matrix amat, Matrix bmat){
        	if(amat.ncol!=bmat.nrow)throw new IllegalArgumentException("Nonconformable matrices");

        	Matrix cmat = new Matrix(amat.nrow, bmat.ncol);
        	double [][] carray = cmat.getArrayReference();
        	double sum = 0.0D;

        	for(int i=0; i<amat.nrow; i++){
            		for(int j=0; j<bmat.ncol; j++){
                		sum=0.0D;
                		for(int k=0; k<amat.ncol; k++){
                       			sum += (amat.matrix[i][k]*bmat.matrix[k][j]);
                		}
                		carray[i][j]=sum;
            		}
        	}
        	return cmat;
    	}

   	    // Multiply a matrix by a constant [static method]
    	public static Matrix times(Matrix amat, double constant){
        	Matrix cmat = new Matrix(amat.nrow, amat.ncol);
        	double [][] carray = cmat.getArrayReference();

 	       	for(int i=0; i<amat.nrow; i++){
            		for(int j=0; j<amat.ncol; j++){
                  		carray[i][j] = amat.matrix[i][j]*constant;
            		}
        	}
        	return cmat;
    	}

    	// Multiply this matrix by a matrix [equivalence of *=]
    	public void timesEquals(Matrix bmat){
        	if(this.ncol!=bmat.nrow)throw new IllegalArgumentException("Nonconformable matrices");

        	double sum = 0.0D;

        	for(int i=0; i<this.nrow; i++){
            		for(int j=0; j<bmat.ncol; j++){
                		sum=0.0D;
                		for(int k=0; k<this.ncol; k++){
                       			sum += (this.matrix[i][k]*bmat.matrix[k][j]);
                		}
                		this.matrix[i][j] = sum;
            		}
        	}
    	}

   	    // Multiply this matrix by a constant [equivalence of *=]
    	public void timesEquals(double constant){

        	for(int i=0; i<this.nrow; i++){
            		for(int j=0; j<this.ncol; j++){
                  		this.matrix[i][j] *= constant;
            		}
        	}
    	}


    	// INVERSE
    	// Inverse of a square matrix [instance method]
    	public Matrix inverse(){
        	int n = this.nrow;
        	if(n!=this.ncol)throw new IllegalArgumentException("Matrix is not square");
        	double[] col = new double[n];
        	double[] xvec = new double[n];
        	Matrix invmat = new Matrix(n, n);
        	double[][] invarray = invmat.getArrayReference();
        	Matrix ludmat;

	    	ludmat = this.luDecomp();
        	for(int j=0; j<n; j++){
            		for(int i=0; i<n; i++)col[i]=0.0D;
            		col[j]=1.0;
            		xvec=ludmat.luBackSub(col);
            		for(int i=0; i<n; i++)invarray[i][j]=xvec[i];
        	}
       		return invmat;
    	}

    	// Inverse of a square matrix [static method]
       	public static Matrix inverse(Matrix amat){
        	int n = amat.nrow;
        	if(n!=amat.ncol)throw new IllegalArgumentException("Matrix is not square");
        	double[] col = new double[n];
        	double[] xvec = new double[n];
        	Matrix invmat = new Matrix(n, n);
        	double[][] invarray = invmat.getArrayReference();
        	Matrix ludmat;

	    	ludmat = amat.luDecomp();
        	for(int j=0; j<n; j++){
            		for(int i=0; i<n; i++)col[i]=0.0D;
            		col[j]=1.0;
            		xvec=ludmat.luBackSub(col);
            		for(int i=0; i<n; i++)invarray[i][j]=xvec[i];
        	}
        	return invmat;
    	}

    	// TRANSPOSE
    	// Transpose of a matrix [instance method]
    	public Matrix transpose(){
        	Matrix tmat = new Matrix(this.ncol, this.nrow);
        	double[][] tarray = tmat.getArrayReference();
        	for(int i=0; i<this.ncol; i++){
            		for(int j=0; j<this.nrow; j++){
                		tarray[i][j]=this.matrix[j][i];
            		}
        	}
        	return tmat;
    	}

    	// Transpose of a matrix [static method]
    	public static Matrix transpose(Matrix amat){
        	Matrix tmat = new Matrix(amat.ncol, amat.nrow);
        	double[][] tarray = tmat.getArrayReference();
        	for(int i=0; i<amat.ncol; i++){
            		for(int j=0; j<amat.nrow; j++){
                		tarray[i][j]=amat.matrix[j][i];
            		}
        	}
        	return tmat;
    	}

    	// OPPOSITE
    	// Opposite of a matrix [instance method]
    	public Matrix opposite(){
        	Matrix opp = Matrix.copy(this);
        	for(int i=0; i<this.nrow; i++){
            		for(int j=0; j<this.ncol; j++){
                		opp.matrix[i][j]=-this.matrix[i][j];
            		}
        	}
        	return opp;
    	}

    	// Opposite of a matrix [static method]
    	public static Matrix opposite(Matrix amat){
        	Matrix opp = Matrix.copy(amat);
        	for(int i=0; i<amat.nrow; i++){
            		for(int j=0; j<amat.ncol; j++){
                		opp.matrix[i][j]=-amat.matrix[i][j];
            		}
        	}
        	return opp;
    	}

    	// TRACE
    	// Trace of a  matrix [instance method]
    	public double trace(){
        	double trac = 0.0D;
        	for(int i=0; i<Math.min(this.ncol,this.ncol); i++){
                	trac += this.matrix[i][i];
        	}
        	return trac;
    	}

    	// Trace of a matrix [static method]
    	public static double trace(Matrix amat){
        	double trac = 0.0D;
        	for(int i=0; i<Math.min(amat.ncol,amat.ncol); i++){
                	trac += amat.matrix[i][i];
        	}
        	return trac;
    	}

    	// DETERMINANT
    	//  Returns the determinant of a square matrix [instance method]
    	public double determinant(){
        	int n = this.nrow;
        	if(n!=this.ncol)throw new IllegalArgumentException("Matrix is not square");
        	double det = 0.0D;
        	Matrix ludmat = this.luDecomp();

	    	det = ludmat.dswap;
        	for(int j=0; j<n; j++){
            		det *= ludmat.matrix[j][j];
         	}
        	return det;
    	}

    	//  Returns the determinant of a square matrix [static method]
    	public static double determinant(Matrix amat){
        	int n = amat.nrow;
        	if(n!=amat.ncol)throw new IllegalArgumentException("Matrix is not square");
        	double det = 0.0D;
        	Matrix ludmat = amat.luDecomp();

	    	det = ludmat.dswap;
        	for(int j=0; j<n; j++){
            		det *= (ludmat.matrix[j][j]);
         	}
        	return det;
    	}

    	// Returns the log(determinant) of a square matrix [instance method].
    	// Useful if determinant() underflows or overflows.
    	public double logDeterminant(){
        	int n = this.nrow;
        	if(n!=this.ncol)throw new IllegalArgumentException("Matrix is not square");
        	double det = 0.0D;
        	Matrix ludmat = this.luDecomp();

	    	det = ludmat.dswap;
	    	det=Math.log(det);
        	for(int j=0; j<n; j++){
            		det += Math.log(ludmat.matrix[j][j]);
        	}
        	return det;
    	}

    	// Returns the log(determinant) of a square matrix [static method].
    	// Useful if determinant() underflows or overflows.
    	public static double logDeterminant(Matrix amat){
        	int n = amat.nrow;
        	if(n!=amat.ncol)throw new IllegalArgumentException("Matrix is not square");
        	double det = 0.0D;
        	Matrix ludmat = amat.luDecomp();

	    	det = ludmat.dswap;
	    	det=Math.log(det);
        	for(int j=0; j<n; j++){
            		det += Math.log(ludmat.matrix[j][j]);
        	}
       		return det;
    	}

    	// FROBENIUS (EUCLIDEAN) NORM of a matrix
    	public double frobeniusNorm(){
        	double norm=0.0D;
        	for(int i=0; i<this.ncol; i++){
            		for(int j=0; j<this.nrow; j++){
                		norm=hypot(norm, Math.abs(matrix[i][j]));
            		}
        	}
        	return norm;
    	}

    	// ONE NORM of a matrix
    	public double oneNorm(){
        	double norm = 0.0D;
        	double sum = 0.0D;
        	for(int i=0; i<this.nrow; i++){
            		sum=0.0D;
            		for(int j=0; j<this.ncol; j++){
                		sum+=Math.abs(this.matrix[i][j]);
            		}
            		norm=Math.max(norm,sum);
        	}
        	return norm;
    	}

    	// INFINITY NORM of a matrix
    	public double infinityNorm(){
        	double norm = 0.0D;
        	double sum = 0.0D;
        	for(int i=0; i<this.nrow; i++){
            		sum=0.0D;
            		for(int j=0; j<this.ncol; j++){
                		sum+=Math.abs(this.matrix[i][j]);
            		}
            		norm=Math.max(norm,sum);
        	}
        	return norm;
    	}


    	// LU DECOMPOSITION OF MATRIX A
    	// For details of LU decomposition
    	// See Numerical Recipes, The Art of Scientific Computing
    	// by W H Press, S A Teukolsky, W T Vetterling & B P Flannery
	    // Cambridge University Press,   http://www.nr.com/
	    // Matrix ludmat is the returned LU decompostion
	    // int[] index is the vector of row permutations
	    // dswap returns +1.0 for even number of row interchanges
	    //       returns -1.0 for odd number of row interchanges
	    public Matrix luDecomp(){
        	if(this.nrow!=this.ncol)throw new IllegalArgumentException("A matrix is not square");
        	int n = this.nrow;
	    	int imax = 0;
	    	double dum = 0.0D, temp = 0.0D, big = 0.0D;
	    	double[] vv = new double[n];
	    	double sum = 0.0D;
	    	double dumm = 0.0D;

	    	this.matrixCheck = true;

	      	Matrix ludmat = Matrix.copy(this);
	    	double[][] ludarray = ludmat.getArrayReference();

    		ludmat.dswap=1.0D;
	    	for (int i=0;i<n;i++) {
		    	big=0.0D;
		    	for (int j=0;j<n;j++)if  ((temp=Math.abs(ludarray[i][j])) > big) big=temp;
        		if (big == 0.0D){
        	    		System.out.println("Attempted LU Decomposition of a singular matrix in Matrix.luDecomp()");
         	    		System.out.println("NaN matrix returned and matrixCheck set to false");
         	    		this.matrixCheck=false;
         	    		for(int k=0;k<n;k++)for(int j=0;j<n;j++)ludarray[k][j]=Double.NaN;
         	    		return ludmat;
         		}
    			vv[i]=1.0/big;
	    	}
	    	for (int j=0;j<n;j++) {
		    	for (int i=0;i<j;i++) {
			    	sum=ludarray[i][j];
			    	for (int k=0;k<i;k++) sum -= ludarray[i][k]*ludarray[k][j];
			    	ludarray[i][j]=sum;
		    	}
		    	big=0.0D;
		    	for (int i=j;i<n;i++) {
    				sum=ludarray[i][j];
	    			for (int k=0;k<j;k++)
				    	sum -= ludarray[i][k]*ludarray[k][j];
			    		ludarray[i][j]=sum;
     					if ((dum=vv[i]*Math.abs(sum)) >= big) {
				        	big=dum;
				        	imax=i;
			    		}
		    		}
		    		if (j != imax) {
			    	for (int k=0;k<n;k++) {
				    	dumm=ludarray[imax][k];
				    	ludarray[imax][k]=ludarray[j][k];
    					ludarray[j][k]=dumm;
			    	}
			    	ludmat.dswap = -ludmat.dswap;
    				vv[imax]=vv[j];
	    		}
		    	ludmat.index[j]=imax;

		    	if(ludarray[j][j]==0.0D){
		        	ludarray[j][j]=TINY;
		    	}
		    	if(j != n-1) {
			    	dumm=1.0/ludarray[j][j];
    				for (int i=j+1;i<n;i++){
    			    		ludarray[i][j]*=dumm;
	    	    		}
	    		}
	    	}
	    	return ludmat;
	    }

    	// Solves the set of n linear equations A.X=B using not A but its LU decomposition
    	// bvec is the vector B (input)
    	// xvec is the vector X (output)
    	// index is the permutation vector produced by luDecomp()
    	public double[] luBackSub(double[] bvec){
	    	int ii = 0,ip = 0;
	    	int n=bvec.length;
	    	if(n!=this.ncol)throw new IllegalArgumentException("vector length is not equal to matrix dimension");
	    	if(this.ncol!=this.nrow)throw new IllegalArgumentException("matrix is not square");
	    	double sum= 0.0D;
	    	double[] xvec=new double[n];
	    	for(int i=0; i<n; i++){
	        	xvec[i]=bvec[i];
	    	}
	    	for (int i=0;i<n;i++) {
		    	ip=this.index[i];
		    	sum=xvec[ip];
		    	xvec[ip]=xvec[i];
		    	if (ii==0){
			    	for (int j=ii;j<=i-1;j++){
			        	sum -= this.matrix[i][j]*xvec[j];
			    	}
			    }
		    	else{
		        	if(sum==0.0) ii=i;
		    	}
		    	xvec[i]=sum;
	    	}
	    	for(int i=n-1;i>=0;i--) {
		    	sum=xvec[i];
		    	for (int j=i+1;j<n;j++){
		        	sum -= this.matrix[i][j]*xvec[j];
		    	}
		    	xvec[i]= sum/matrix[i][i];
	    	}
	    	return xvec;
    	}

    	// Solves the set of n linear equations A.X=B
    	// bvec is the vector B (input)
    	// xvec is the vector X (output)
    	public double[] solveLinearSet(double[] bvec){
        	Matrix ludmat =	this.luDecomp();

        	return ludmat.luBackSub(bvec);
    	}

    	// Real methods not in java.lang.maths required in this Class
    	// See Fmath.class for public versions of these methods
    	private static double hypot(double aa, double bb){
        	double cc = 0.0D, ratio = 0.0D;
        	double amod=Math.abs(aa);
        	double bmod=Math.abs(bb);

        	if(amod==0.0D){
         	   	cc=bmod;
        	}
        	else{
            		if(bmod==0.0D){
                		cc=amod;
            		}
            		else{
                		if(amod<=bmod){
                    			ratio=amod/bmod;
                    			cc=bmod*Math.sqrt(1.0D+ratio*ratio);
                		}
                		else{
                    			ratio=bmod/amod;
                    			cc=amod*Math.sqrt(1.0D+ratio*ratio);
                		}
            		}
        	}
        	return cc;
    	}
}

