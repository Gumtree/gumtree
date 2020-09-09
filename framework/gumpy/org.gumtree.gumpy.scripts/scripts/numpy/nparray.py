'''
    @author: nxi
'''

from gumpy.nexus import array as nxa
from errorhandler import *

class ndarray():
    def __init__(self, shape = None, dtype=float, buffer=None, offset=None, strides=None, order=None):
        if buffer is None:
            self.buffer = nxa.instance(shape, dtype = dtype)
        else:
            if isinstance(buffer, ndarray):
                buffer = buffer.buffer
            if strides is None:
                self.buffer = buffer
            else:
                if offset is None:
                    offset = [0] * buffer.ndim
                self.buffer = buffer.get_section(offset, shape, strides)
    
    '''
    T : ndarray

        The transposed array.
    data: buffer

        Python buffer object pointing to the start of the array's data.
    dtype : dtype object

        Data-type of the array's elements.
    flags : dict

        Information about the memory layout of the array.
    flat : numpy.flatiter object

        A 1-D iterator over the array.
    imag : ndarray

        The imaginary part of the array.
    real : ndarray

        The real part of the array.
    size : int

        Number of elements in the array.
    itemsize : int

        Length of one array element in bytes.
    nbytes : int

        Total bytes consumed by the elements of the array.
    ndim : int

        Number of array dimensions.
    shape : tuple of ints

        Tuple of array dimensions.
    strides : tuple of ints

        Tuple of bytes to step in each dimension when traversing an array.
    ctypes : ctypes object

        An object to simplify the interaction of the array with the ctypes module.
    base : ndarray

        Base object if memory is from some other object.
    '''
    def __getattr__(self, name):
        if name == 'shape':
            return tuple(self.buffer.shape)
        elif name == 'data':
            return self.buffer
        elif name == 'T':
            t = self.buffer.transpose()
            return ndarray(t.shape, buffer = t)
        elif name == 'dtype':
            return self.buffer.dtype
        elif name == 'flags':
            raise NotSupportedError('flags is not supported in Gumpy')
        elif name == 'imag':
            raise NotSupportedError('complex data type is not supported in Gumpy')
        elif name == 'real':
            raise NotSupportedError('complex data type is not supported in Gumpy')
        elif name == 'size':
            return self.buffer.size
        elif name == 'itemsize':
            return self.buffer.itemsize
        elif name == 'nbytes':
            return self.buffer.nbytes
        elif name == 'strides':
            return self.buffer.stride
        elif name == 'ctype':
            raise NotSupportedError('C type is not supported in Gumpy')
        elif name == 'base':
            raise NotSupportedError('memory access is not supported in Gumpy')
        
    def __getitem__(self, index):
        out = self.buffer.__getitem__(index)
        if isinstance(out, nxa.Array):
            return ndarray(None, buffer = out)
        else:
            return out
        
    def __setitem__(self, index, value):
        self.buffer.__setitem__(index, value)
        
    def __str__(self, indent = ''):
        return self.buffer.__str__(indent)
    
    def __repr__(self, indent = ''):
        out = self.buffer.__repr__(indent)
        return 'a' + out[1:]
        
    def __len__(self):
        return self.shape[0]
    
    ############## logic functions ##############
    
    def __eq__(self, obj):
        if isinstance(obj, ndarray):
            return ndarray(buffer = self.buffer == obj.buffer)
        else :
            return ndarray(buffer = self.buffer == obj) 
    
    def all(axis=None, out=None, keepdims=False):
        res = self.buffer.all()
        if out is None:
            if keepdims:
                return ndarray(self.shape, bool).fill(res)
            else:
                return res
        else:
            return out.fill(res)
        
    '''
    Fill the array with a scalar value.

    Parameters

       value : scalar
            All elements of a will be assigned this value.
    '''
    def fill(self, value):
        self.buffer.fill(value)
        
    def reshape(self, *shape):
        if len(shape) == 0:
            raise IllegalArgumentError('shape must be provided')
        if len(shape) == 1 and type(shape[0]) is list:
            return ndarray(buffer = self.buffer.reshape(shape[0]))
        else:
            return ndarray(buffer = self.buffer.reshape(list(shape)))
        
    def diagonal(self, offset=0, axis1=0, axis2=1):
        return ndarray(buffer = self.buffer.diagonal(offset, axis1, axis2))
    
    def tril(self, k=0):
        return ndarray(buffer = self.buffer.tril(k))

    def triu(self, k=0):
        return ndarray(buffer = self.buffer.triu(k))
        
class matrix(ndarray):
    def __init__(self, shape, dtype=float, buffer=None, offset=None, strides=None, order=None):
        ndarray(shape, dtype, buffer, offset, strides)
        
    def __repr__(self, indent=''):
        out = ndarray.__repr__(self, indent=indent)
        return 'matrix' + out[5:]
    