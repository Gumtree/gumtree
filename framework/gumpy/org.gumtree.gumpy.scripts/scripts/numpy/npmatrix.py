'''
    @author: nxi
'''

from gumpy.nexus import array as nxa
from errorhandler import *
from copy import copy as ncopy
from nparray import ndarray
        
class matrix(ndarray):
    ''' A matrix is a specialized 2-D array that retains its 2-D nature through operations. 
        It has certain special operators, such as * (matrix multiplication) and ** (matrix 
        power).

        Attributes
    
            A : ndarray
                Return self as an ndarray object.
                
            A1 : ndarray
                Return self as a flattened ndarray.
            
            H : not supported
                Returns the (complex) conjugate transpose of self.
            
            I : matrix
                Returns the (multiplicative) inverse of invertible self.
            
            T : matrix
                Returns the transpose of the matrix.
            
            base : Nexus Array
                Base object if memory is from some other object.
            
            ctypes : not supported
            
            data : Nexus Array
                Python buffer object pointing to the start of the array's data.
            
            dtype : data-type
                Data-type of the array's elements.
            
            flags : not supported
            
            flat : ndarray
                A 1-D iterator over the array.
            
            imag : not supported
            
            itemsize : int
                Length of one array element in bytes.
            
            nbytes : int
                Total bytes consumed by the elements of the array.
            
            ndim : int
                Number of array dimensions.
            
            real : not supported
            
            shape : tuple of ints
                Tuple of array dimensions.
            
            size : int
                Number of elements in the array.
            
            strides : tuple of ints
                Tuple of bytes to step in each dimension when traversing an array.

    '''
    def __init__(self, data, dtype=None, copy=True):
        ''' Create a matrix from an array-like object, or from a string of data. 

            Parameters

                data : array_like or string
                    If data is a string, it is interpreted as a matrix with commas 
                    or spaces separating columns, and semicolons separating rows.
                    
                dtype : data-type
                    Data-type of the output matrix.
                    
                copy : bool
                    If data is already an ndarray, then this flag determines 
                    whether the data is copied (the default), or whether a view 
                    is constructed.

        '''
        if copy:
            if not isinstance(data, nxa.Array):
                data = nxa.Array(data)
            if dtype is None or dtype is data.dtype:
                self.buffer = ncopy(data)
            else:
                b = nxa.instance(data.shape, dtype = dtype)
                b[:] = data
                self.buffer = b
        else:
            if isinstance(data, ndarray):
                self.buffer = data.buffer
            elif isinstance(data, nxa.Array):
                self.buffer = data
            else:
                self.buffer = nxa.Array(data)
        
    def __repr__(self, indent = ''):
        out = ndarray.__repr__(self, ' ')
        return 'matrix' + out[5:]
    
    def __getattr__(self, name):
        if name == 'A':
            return ndarray(buffer = self.buffer)
        elif name == 'A1':
            return ndarray(buffer = self.buffer.flatten())
        elif name == 'I':
            return matrix(data = self.buffer.matrix_invert(), copy = False)
        elif name == 'T':
            return matrix(data = self.buffer.transpose(), copy = False)
        else:
            return ndarray.__getattr__(self, name)
        
    def _new(self, buffer):
        return matrix(data = buffer)