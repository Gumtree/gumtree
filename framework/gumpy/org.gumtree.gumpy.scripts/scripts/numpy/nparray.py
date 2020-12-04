'''
    @author: nxi
'''

from gumpy.nexus import array as nxa
from errorhandler import *
import pickle
import numpy as np

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
    An array object represents a multidimensional, homogeneous array of 
    fixed-size items. An associated data-type object describes the format 
    of each element in the array.
    
    T : ndarray
        The transposed array.

    data: buffer
        Python buffer object pointing to the start of the array's data.

    dtype : dtype object
        Data-type of the array's elements.

    flags : not supported
        Information about the memory layout of the array.

    flat : numpy.flatiter object
        A 1-D iterator over the array.

    imag : not supported
        The imaginary part of the array.

    real : not supported
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

    ctypes : not supported

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
            return self._new(buffer = t)
        elif name == 'dtype':
            return self.buffer.dtype
        elif name == 'flags':
            raise NotSupportedError('flags is not supported in Gumpy')
        elif name == 'flat':
            return ndarray(buffer = self.buffer.flatten())
        elif name == 'imag':
            raise NotSupportedError('complex data type is not supported in Gumpy')
        elif name == 'real':
            raise NotSupportedError('complex data type is not supported in Gumpy')
        elif name == 'ndim':
            return self.buffer.ndim
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
        else:
            raise AttributeError('attribute {} not found'.format(name))
        
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
    
    def __repr__(self, indent = '', skip = True, precision = None):
        if precision is None:
            precision = nxa.Array.precision
        out = self.buffer.__repr__(indent, skip = skip, precision = precision)
        return 'a' + out[1:]
        
    def __len__(self):
        return self.shape[0]
    
    def __iter__(self):
        if (self.ndim > 1) :
            return SliceIter(self)
        else :
            return self.buffer.item_iter()
    
    def __copy__(self):
        return self.copy()
        
    def __deepcopy__(self):
        return ndarray(buffer = self.buffer.__deepcopy__())
        
    def __eq__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__eq__(obj)) 
    
    def __ne__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__ne__(obj))
        
    def __lt__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__lt__(obj))

    def __le__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__le__(obj))

    def __gt__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__gt__(obj))

    def __ge__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__ge__(obj))

    def __not__(self):
        return ndarray(buffer = self.buffer.__not__())
    
    def __bool__(self):
        return ndarray(buffer = self.buffer.__bool__())
        
    def __or__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__or__(obj))

    def __ior__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        self.buffer.__ior__(obj)
        return self

    def __xor__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__xor__(obj))

    def __ixor__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        self.buffer.__ixor__(obj)
        return self
        
    def __and__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__and__(obj))

    def __iand__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        self.buffer.__iand__(obj)
        return self

    def __add__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__add__(obj))

    def __iadd__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        self.buffer.__iadd__(obj)
        return self

    def __radd__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__radd__(obj))

    def __div__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__div__(obj))

    def __floordiv__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__floordiv__(obj))

    def __truediv__(self, obj):
        return self.__div__(obj)

    def __mod__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__mod__(obj))
    
    def __rmod__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__rmod__(obj))
            
    def __divmod__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        out = self.buffer.__divmod__(obj)
        return (ndarray(buffer = out[0]), ndarray(buffer = out[1]))
        
    def __idiv__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        self.buffer.__idiv__(obj)
        return self

    def __imod__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        self.buffer.__imod__(obj)
        return self

    def __itruediv__(self, obj):
        return self.__idiv__(obj)

    def __ifloordiv__(self, obj):
        return self.buffer.__ifloordiv__(obj)
    
    def __rdiv__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__rdiv__(obj))

    def __mul__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__mul__(obj))

    def __imul__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        self.buffer.__imul__(obj)
        return self

    def __rmul__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__rmul__(obj))

    def __neg__(self):
        return ndarray(buffer = self.buffer.__neg__())

    def __pos__(self):
        return ndarray(buffer = self.buffer.__pos__())
    
    def __sub__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__sub__(obj))

    def __isub__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        self.buffer.__isub__(obj)
        return self

    def __rsub__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__rsub__(obj))

    def __invert__(self):
        return ndarray(buffer = self.buffer.__invert__())

    def __abs__(self):
        return ndarray(buffer = self.buffer.__abs__())

    def __pow__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__pow__(obj))

    def __ipow__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        self.buffer.__ipow__(obj)
        return self

    def __rpow__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__rpow__(obj))
        
    def __int__(self):
        return self.buffer.__int__()

    def __float__(self):
        return self.buffer.__float__()

    def __long__(self):
        return self.buffer.__long__()
        
    def __matmul__(self, value):
        if isinstance(value, ndarray):
            value = value.buffer
        return self._new(self.buffer.matmul(value))
        
    def _new(self, buffer):
        ''' Function to create a new instance with the same class. This 
            function is designed to be overwritten by the children classes.
            
            Parameters:
            
                buffer : Nexus Array object or array-like object.
                
        ''' 
        if np.iterable(buffer) :
            return ndarray(buffer = buffer)
        else:
            return buffer
        
    def item(self, *args):
        ''' Copy an element of an array to a standard Python scalar and return it.

            Parameters
            
                *args : Arguments (variable number and type)
                    none: in this case, the method only works for arrays with one 
                    element (a.size == 1), which element is copied into a standard 
                    Python scalar object and returned.
            
                    int_type: this argument is interpreted as a flat index into 
                    the array, specifying which element to copy and return.
            
                    tuple of int_types: functions as does a single int_type 
                    argument, except that the argument is interpreted as an 
                    nd-index into the array.
            
            Returns
            
                z : Standard Python scalar object
                    A copy of the specified element of the array as a suitable 
                    Python scalar.

        '''
        return self.buffer.item(*args)
        
    def itemset(self, *args):
        ''' Insert scalar into an array (scalar is cast to array's dtype, if possible)

            There must be at least 1 argument, and define the last argument as item. 
            Then, a.itemset(*args) is equivalent to but faster than a[args] = item. 
            The item should be a scalar value and args must select a single item in 
            the array a.

            Parameters
        
                *args : Arguments
                    If one argument: a scalar, only used in case a is of size 1. 
                    If two arguments: the last argument is the value to be set and 
                    must be a scalar, the first argument specifies a single array 
                    element location. It is either an int or a tuple.
        
            Notes
        
                Compared to indexing syntax, itemset provides some speed increase 
                for placing a scalar into a particular location in an ndarray, if 
                you must do this. However, generally this is discouraged: among 
                other problems, it complicates the appearance of the code. Also, 
                when using itemset (and item) inside a loop, be sure to assign the 
                methods to a local variable to avoid the attribute look-up at each 
                loop iteration.
        '''
        self.buffer.itemset(*args)
        
    def copy(self, subok=True):
        ''' Return a copy of the array.
            
            Parameters:
                subok : bool, optional
                    If True, then sub-classes will be passed-through, otherwise 
                    the returned array will be forced to be a base-class array 
                    (defaults to False).
                    
        '''
        if subok:
            return self._new(buffer = self.buffer.__copy__())
        else:
            return ndarray(buffer = self.buffer.__copy__())

    def dump(self, file):
        ''' Dump a pickle of the array to the specified file. The array 
            can be read back with pickle.load or numpy.load.
    
            Parameters
    
                file : str or Path
                    A string naming the dump file.
    
        '''        
        file = open(file, 'wb')
        try:
            pickle.dump(self.dumps(), file)
        finally:
            file.close()
            
    def dumps(self):
        ''' Returns the pickle of the array as a string. pickle.loads or 
            numpy.loads will convert the string back to an array.

            Parameters: None
        '''
        return 'np.' + self.__repr__(skip = False, precision = np.PRECISION)
        
    def all(self, axis=None, out=None, keepdims=False):
        ''' Returns True if all elements evaluate to True.

            Parameters:
            
                axis : None or int or tuple of ints, optional
                    Axis or axes along which a logical AND reduction is performed. 
                    The default (axis=None) is to perform a logical AND over all 
                    the dimensions of the input array. axis may be negative, in 
                    which case it counts from the last to the first axis.

                    If this is a tuple of ints, a reduction is performed on multiple 
                    axes, instead of a single axis or all the axes as before.
        
                out : ndarray, optional
                    Alternate output array in which to place the result. It must 
                    have the same shape as the expected output and its type is 
                    preserved (e.g., if dtype(out) is float, the result will consist 
                    of 0.0's and 1.0's). See ufuncs-output-type for more details.
        
                keepdims : bool, optional
                    If this is set to True, the axes which are reduced are left in 
                    the result as dimensions with size one. With this option, the 
                    result will broadcast correctly against the input array.
        
                    If the default value is passed, then keepdims will not be passed 
                    through to the all method of sub-classes of ndarray, however any 
                    non-default value will be. If the sub-class' method does not 
                    implement keepdims any exceptions will be raised.
        
            Returns
        
                all : ndarray, bool
                    A new boolean or array is returned unless out is specified, in 
                    which case a reference to out is returned.
            
        '''
        res = self.buffer.all(axis)
        if out is None:
            if keepdims:
                return ndarray(self.shape, bool).fill(res)
            else:
                return res
        else:
            out.fill(res)
            return out

    def any(self, axis=None, out=None, keepdims=False):
        ''' Returns True if any of the elements of a evaluate to True.

            Parameters
        
                axis : None or int or tuple of ints, optional
                    Axis or axes along which a logical OR reduction is performed. 
                    The default (axis=None) is to perform a logical OR over all 
                    the dimensions of the input array. axis may be negative, in 
                    which case it counts from the last to the first axis.
        
                    If this is a tuple of ints, a reduction is performed on 
                    multiple axes, instead of a single axis or all the axes as 
                    before.
        
                out : ndarray, optional
                    Alternate output array in which to place the result. It must 
                    have the same shape as the expected output and its type is 
                    preserved (e.g., if it is of type float, then it will remain 
                    so, returning 1.0 for True and 0.0 for False, regardless of 
                    the type of a). See ufuncs-output-type for more details.
        
                keep : dimsbool, optional
                    If this is set to True, the axes which are reduced are left 
                    in the result as dimensions with size one. With this option, 
                    the result will broadcast correctly against the input array.
        
                    If the default value is passed, then keepdims will not be 
                    passed through to the any method of sub-classes of ndarray, 
                    however any non-default value will be. If the sub-class' 
                    method does not implement keepdims any exceptions will be 
                    raised.
        
            Returns
        
                any : bool or ndarray
                    A new boolean or ndarray is returned unless out is specified, 
                    in which case a reference to out is returned.
        
        '''
        res = self.buffer.any(axis)
        if out is None:
            if keepdims:
                return ndarray(self.shape, bool).fill(res)
            else:
                return res
        else:
            out.fill(res)
            return out
    
    def max(self, axis=None, out=None, keepdims=None, initial=None, where=None):
        ''' Return the maximum along a given axis.

            Parameters
        
                axis : None or int or tuple of ints, optional
                    Axis or axes along which to operate. 
                    By default, flattened input is used.
        
                    If this is a tuple of ints, the maximum is selected over multiple axes, 
                    instead of a single axis or all the axes as before.
        
                out : ndarray, optional
                    Alternative output array in which to place the result. Must be of the 
                    same shape and buffer length as the expected output. See ufuncs-output-type 
                    for more details.
        
                keepdims : not supported
                
                initial : scalar, optional
                    The minimum value of an output element. Must be present to allow computation 
                    on empty slice. See reduce for details.
        
                where : not supported
        
            Returns
        
                amax : ndarray or scalar
                    Maximum of a. If axis is None, the result is a scalar value. If axis is given, 
                    the result is an array of dimension a.ndim - 1.
            
        '''
        if not out is None:
            out = np.asanyarray(out).buffer
        return self._new(self.buffer.amax(axis, out, initial))

    def min(self, axis=None, out=None, keepdims=None, initial=None, where=None):
        ''' Return the minimum along a given axis. 
            
            Parameters
        
                axis : None or int or tuple of ints, optional
                    Axis or axes along which to operate. By default, flattened input is used.
                    If this is a tuple of ints, the minimum is selected over multiple axes, 
                    instead of a single axis or all the axes as before.
        
                out : ndarray, optional
                    Alternative output array in which to place the result. Must be of the same 
                    shape and buffer length as the expected output. See ufuncs-output-type for 
                    more details.
                
                keepdims : not supported
        
                initial : scalar, optional
                    The maximum value of an output element. Must be present to allow computation 
                    on empty slice. See reduce for details.
        
                where : not supported
        
            Returns
        
                amin : ndarray or scalar
                    Minimum of a. If axis is None, the result is a scalar value. If axis is given, 
                    the result is an array of dimension a.ndim - 1.
            
        '''
        if not out is None:
            out = np.asanyarray(out).buffer
        return self._new(self.buffer.amin(axis, out, initial))

    def mean(self, axis=None, dtype=None, out=None, keepdims=False):
        ''' Compute the arithmetic mean along the specified axis.
        
            Returns the average of the array elements. The average is taken over 
            the flattened array by default, otherwise over the specified axis. 
            float64 intermediate and return values are used for integer inputs.
        
            Parameters
        
                axis : None or int or tuple of ints, optional
                    Axis or axes along which the means are computed. The default 
                    is to compute the mean of the flattened array.
        
                    If this is a tuple of ints, a mean is performed over multiple 
                    axes, instead of a single axis or all the axes as before.
                    
                dtype : data-type, optional
                    Type to use in computing the mean. For integer inputs, the 
                    default is float64; for floating point inputs, it is the same 
                    as the input dtype.
        
                out : ndarray, optional
                    Alternate output array in which to place the result. The 
                    default is None; if provided, it must have the same shape as 
                    the expected output, but the type will be cast if necessary. \
                    
                keepdims : not supported
        
            Returns
        
                m : ndarray, see dtype parameter above
                    If out=None, returns a new array containing the mean values, 
                    otherwise a reference to the output array is returned.
        
        '''
        if not out is None:
            out = np.asanyarray(out).buffer
        return self._new(self.buffer.mean(axis, dtype, out))
        
    def argmax(self, axis=None, out=None):
        ''' Returns the indices of the maximum values along an axis.
        
            Parameters
            
                axis : int, optional
                    By default, the index is into the flattened array, 
                    otherwise along the specified axis.
        
                out : array, optional
                    If provided, the result will be inserted into this array. 
                    It should be of the appropriate shape and dtype.
        
            Returns
        
                index_arrayndarray of ints
                    Array of indices into the array. It has the same shape 
                    as a.shape with the dimension along axis removed.
        
        '''
        res = self.buffer.argmax(axis)
        if not out is None:
            out.fill(res)
            return out
        else:
            return self._new(res)
        
    def argmin(self, axis=None, out=None):
        ''' Returns the indices of the minimum values along an axis.
        
            Parameters
        
                axis : int, optional
                    By default, the index is into the flattened array, 
                    otherwise along the specified axis.
        
                out : array, optional
                    If provided, the result will be inserted into this array. 
                    It should be of the appropriate shape and dtype.
        
            Returns
        
                index_array : ndarray of ints
                    Array of indices into the array. It has the same shape as 
                    a.shape with the dimension along axis removed.
        
        '''
        res = self.buffer.argmin(axis)
        if not out is None:
            out.fill(res)
            return out
        else:
            return self._new(res)
        
    def cumprod(self, axis=None, dtype=None, out=None):
        ''' Return the cumulative product of elements along a given axis.
        
            Parameters
        
                axis : int, optional
                    Axis along which the cumulative product is computed. 
                    By default the input is flattened.
        
                dtype : dtype, optional
                    Type of the returned array, as well as of the accumulator 
                    in which the elements are multiplied. If dtype is not 
                    specified, it defaults to the dtype of a, unless a has an 
                    integer dtype with a precision less than that of the 
                    default platform integer. In that case, the default 
                    platform integer is used instead.
        
                out : ndarray, optional
                    Alternative output array in which to place the result. It 
                    must have the same shape and buffer length as the expected 
                    output but the type of the resulting values will be cast 
                    if necessary.
        
            Returns
        
                cumprod : ndarray
        
                    A new array holding the result is returned unless out is 
                    specified, in which case a reference to out is returned.
        
        '''
        return self._new(buffer = self.buffer.cumprod(axis, dtype, out))

    def cumsum(self, axis=None, dtype=None, out=None):
        ''' Return the cumulative sum of the elements along a given axis.
        
            Parameters
        
                axis : int, optional
                    Axis along which the cumulative sum is computed. The default (None) 
                    is to compute the cumsum over the flattened array.
                
                dtype : dtype, optional
                    Type of the returned array and of the accumulator in which the 
                    elements are summed. If dtype is not specified, it defaults to 
                    the dtype of a, unless a has an integer dtype with a precision 
                    less than that of the default platform integer. In that case, 
                    the default platform integer is used.
                
                out : ndarray, optional
                    Alternative output array in which to place the result. It must 
                    have the same shape and buffer length as the expected output 
                    but the type will be cast if necessary. See ufuncs-output-type 
                    for more details.
        
            Returns
        
                cumsum_along_axis : ndarray.
                    A new array holding the result is returned unless out is specified, 
                    in which case a reference to out is returned. The result has the 
                    same size as a, and the same shape as a if axis is not None or a is 
                    a 1-d array.
        
        '''
        return self._new(buffer = self.buffer.cumsum(axis, dtype, out))

    def dot(self, b, out = None):
        ''' Dot product of two arrays. Specifically,
        
                If both a and b are 1-D arrays, it is inner product of vectors (without complex conjugation).
        
                If both a and b are 2-D arrays, it is matrix multiplication, but using matmul or a @ b is preferred.
        
                If either a or b is 0-D (scalar), it is equivalent to multiply and using numpy.multiply(a, b) or a * b is preferred.
        
                If a is an N-D array and b is a 1-D array, it is a sum product over the last axis of a and b.
        
                If a is an N-D array and b is an M-D array (where M>=2), it is a sum product over the last axis of a and the second-to-last axis of b:
        
                dot(a, b)[i,j,k,m] = sum(a[i,j,:] * b[k,:,m])
        
            Parameters
        
                b : array_like
                    Second argument.
        
                out : ndarray, optional
                    Output argument. This must have the exact kind that would be 
                    returned if it was not used. In particular, it must have the 
                    right type, must be C-contiguous, and its dtype must be the 
                    dtype that would be returned for dot(a,b). This is a 
                    performance feature. Therefore, if these conditions are not 
                    met, an exception is raised, instead of attempting to be 
                    flexible.
        
            Returns
        
                outputndarray
        
                    Returns the dot product of a and b. If a and b are both scalars 
                    or both 1-D arrays then a scalar is returned; otherwise an array 
                    is returned. If out is given, then it is returned.
        
            Raises
        
                ValueError
                    If the last dimension of a is not the same size as the 
                    second-to-last dimension of b.
        
        '''
        if np.iterable(b):
            b = np.asanyarray(b).buffer
        if not out is None:
            out = np.asanyarray(out).buffer
        return self._new(buffer = self.buffer.dot(b, out))
        
    def nonzero(self):
        ''' Return the indices of the elements that are non-zero.
        
            Returns a tuple of arrays, one for each dimension of a, containing the 
            indices of the non-zero elements in that dimension.
            
            Parameters
            
                None
        
            Returns
        
                tuple_of_arrays : tuple
                    Indices of elements that are non-zero.
        
        '''
        out = self.buffer.nonzero()
        return tuple([ndarray(buffer = x) for x in out])
        
    def sum(self, axis=None, dtype=None, out=None, keepdims=None, initial=0, where=None):
        ''' Sum of array elements over a given axis.
        
            Parameters
        
                axis : None or int or tuple of ints, optional
                    Axis or axes along which a sum is performed. The default, axis=None, 
                    will sum all of the elements of the input array. If axis is negative 
                    it counts from the last to the first axis.
        
                    If axis is a tuple of ints, a sum is performed on all of the axes 
                    specified in the tuple instead of a single axis or all the axes as 
                    before.
                    
                dtype : dtype, optional
                    The type of the returned array and of the accumulator in which the 
                    elements are summed. The dtype of a is used by default unless a has 
                    an integer dtype of less precision than the default platform integer. 
                    In that case, if a is signed then the platform integer is used while 
                    if a is unsigned then an unsigned integer of the same precision as 
                    the platform integer is used.
                    
                out : ndarray, optional
                    Alternative output array in which to place the result. It must have 
                    the same shape as the expected output, but the type of the output 
                    values will be cast if necessary.
                    
                keepdims : bool, optional
                    If this is set to True, the axes which are reduced are left in the 
                    result as dimensions with size one. With this option, the result will 
                    broadcast correctly against the input array.
        
                    If the default value is passed, then keepdims will not be passed 
                    through to the sum method of sub-classes of ndarray, however any 
                    non-default value will be. If the sub-class' method does not implement 
                    keepdims any exceptions will be raised.
                    
                initial : scalar, optional
                    Starting value for the sum. See reduce for details.
        
                where : not supported
        
            Returns
        
                sum_along_axis : ndarray
                    An array with the same shape as a, with the specified axis removed. 
                    If a is a 0-d array, or if axis is None, a scalar is returned. If an 
                    output array is specified, a reference to out is returned.
        
        '''
        if not out is None:
            out = np.asanyarray(out).buffer
        return self._new(self.buffer.asum(axis, dtype, out, initial))
        
    def prod(self, axis=None, dtype=None, out=None, keepdims=None, initial=1, where=None):
        ''' Return the product of array elements over a given axis.
        
            Parameters
        
                axis : None or int or tuple of ints, optional
                    Axis or axes along which a product is performed. The default, axis=None, 
                    will calculate the product of all the elements in the input array. 
                    If axis is negative it counts from the last to the first axis.
        
                    If axis is a tuple of ints, a product is performed on all of the axes 
                    specified in the tuple instead of a single axis or all the axes as before.
                    
                dtype : dtype, optional
                    The type of the returned array, as well as of the accumulator in which 
                    the elements are multiplied. The dtype of a is used by default unless a 
                    has an integer dtype of less precision than the default platform integer. 
                    In that case, if a is signed then the platform integer is used while if 
                    a is unsigned then an unsigned integer of the same precision as the 
                    platform integer is used.
                    
                out : ndarray, optional
                    Alternative output array in which to place the result. It must have the 
                    same shape as the expected output, but the type of the output values 
                    will be cast if necessary.
                    
                keepdims : not supported
        
                initial : scalar, optional
                    The starting value for this product. 
        
                where : not supported
        
            Returns
        
                product_along_axis : ndarray, see dtype parameter above.
                    An array shaped as a but with the specified axis removed. Returns a 
                    reference to out if specified.
        
        '''
        if not out is None:
            out = np.asanyarray(out).buffer
        return self._new(self.buffer.prod(axis, dtype, out, initial))
    
    def ptp(self, axis=None, out=None, keepdims=None):
        ''' Range of values (maximum - minimum) along an axis.
        
            The name of the function comes from the acronym for 'peak to peak'.
        
            Parameters
        
                axis : None or int or tuple of ints, optional
                    Axis along which to find the peaks. By default, flatten the array. 
                    axis may be negative, in which case it counts from the last to the 
                    first axis.
        
                    If this is a tuple of ints, a reduction is performed on multiple 
                    axes, instead of a single axis or all the axes as before.
                    
                out : array_like
                    Alternative output array in which to place the result. It must 
                    have the same shape and buffer length as the expected output, but 
                    the type of the output values will be cast if necessary.
                    
                keepdims : bool, optional
                    If this is set to True, the axes which are reduced are left in the 
                    result as dimensions with size one. With this option, the result 
                    will broadcast correctly against the input array.
        
                    If the default value is passed, then keepdims will not be passed 
                    through to the ptp method of sub-classes of ndarray, however any 
                    non-default value will be. If the sub-class' method does not 
                    implement keepdims any exceptions will be raised.
        
            Returns
        
                ptp : ndarray
                    A new array holding the result, unless out was specified, in which 
                    case a reference to out is returned.
        
        '''
        if not out is None:
            out = np.asanyarray(out).buffer
        return self._new(buffer = self.buffer.ptp(axis, out))
        
    def round(self, decimals=0, out=None):
        ''' Evenly round to the given number of decimals.
        
            Parameters
        
                decimals : int, optional
                    Number of decimal places to round to (default: 0). If decimals is negative, 
                    it specifies the number of positions to the left of the decimal point.
                
                out : ndarray, optional
                    Alternative output array in which to place the result. It must have the 
                    same shape as the expected output, but the type of the output values will 
                    be cast if necessary. 
        
            Returns
        
                rounded_array : ndarray
                    An array of the same type as a, containing the rounded values. Unless 
                    out was specified, a new array is created. A reference to the result is 
                    returned.
        
                    The real and imaginary parts of complex numbers are rounded separately. 
                    The result of rounding a float is a float.
        
        '''
        if not out is None:
            out = np.asanyarray(out).buffer
        return self._new(self.buffer.round(decimals, out))
    
    def sort(self, axis=-1, reverse=False):
        ''' Return a sorted copy of an array.
        
            Parameters
        
                axis : int or None, optional
                    Axis along which to sort. If None, the array is flattened before 
                    sorting. The default is -1, which sorts along the last axis.
                    
                kind : not supported
        
                order {'asc', 'desc'}: str or list of str, optional
                    Ascending or descending
        
            Returns
        
                sorted_array : ndarray
                    Array of the same type and shape as a.
        
        '''
        self.buffer.sort(axis, reverse)
        
    def std(self, axis=None, dtype=None, out=None, ddof=0):
        ''' Compute the standard deviation along the specified axis.
        
            Returns the standard deviation, a measure of the spread of a distribution, 
            of the array elements. The standard deviation is computed for the flattened 
            array by default, otherwise over the specified axis.
        
            Parameters
        
                axis : None or int or tuple of ints, optional
                    Axis or axes along which the standard deviation is computed. The 
                    default is to compute the standard deviation of the flattened array.
        
                    If this is a tuple of ints, a standard deviation is performed over 
                    multiple axes, instead of a single axis or all the axes as before.
                    
                dtype : dtype, optional
                    Type to use in computing the standard deviation. For arrays of 
                    integer type the default is float64, for arrays of float types 
                    it is the same as the array type.
        
                out : ndarray, optional
                    Alternative output array in which to place the result. It must 
                    have the same shape as the expected output but the type (of the 
                    calculated values) will be cast if necessary.
        
                ddof : int, optional
                    Means Delta Degrees of Freedom. The divisor used in calculations 
                    is N - ddof, where N represents the number of elements. 
                    By default ddof is zero.
        
                keepdims : bool, optional
                    If this is set to True, the axes which are reduced are left in 
                    the result as dimensions with size one. With this option, the 
                    result will broadcast correctly against the input array.
        
                    If the default value is passed, then keepdims will not be passed 
                    through to the std method of sub-classes of ndarray, however any 
                    non-default value will be. If the sub-class' method does not 
                    implement keepdims any exceptions will be raised.
        
            Returns
        
                standard_deviationndarray, see dtype parameter above.
                    If out is None, return a new array containing the standard 
                    deviation, otherwise return a reference to the output array.
        
        '''
        if not out is None:
            out = np.asanyarray(out).buffer
        return self._new(self.buffer.std(axis, dtype, out, ddof))
        
    def var(self, axis=None, dtype=None, out=None, ddof=0):
        ''' Compute the variance along the specified axis.
        
            Returns the variance of the array elements, a measure of the spread of 
            a distribution. The variance is computed for the flattened array by 
            default, otherwise over the specified axis.
        
            Parameters
        
                axis : None or int or tuple of ints, optional
        
                    Axis or axes along which the variance is computed. The default 
                    is to compute the variance of the flattened array.
        
                    If this is a tuple of ints, a variance is performed over multiple 
                    axes, instead of a single axis or all the axes as before.
                    
                dtype : data-type, optional
                    Type to use in computing the variance. For arrays of integer type 
                    the default is float64; for arrays of float types it is the same 
                    as the array type.
                    
                out : ndarray, optional
                    Alternate output array in which to place the result. It must have 
                    the same shape as the expected output, but the type is cast if 
                    necessary.
                    
                ddof : int, optional
                    "Delta Degrees of Freedom": the divisor used in the calculation 
                    is N - ddof, where N represents the number of elements. By default 
                    ddof is zero.
                
                keepdims : not support
        
            Returns
        
                variance : ndarray, see dtype parameter above
                    If out=None, returns a new array containing the variance; otherwise, 
                    a reference to the output array is returned.
        
        '''
        if not out is None:
            out = np.asanyarray(out).buffer
        return self._new(self.buffer.var(axis, dtype, out, ddof))

    def astype(self, dtype, order='K', casting='unsafe', subok=True, copy=True):
        ''' Copy of the array, cast to a specified type.
        
            Parameters
        
                dtype : str or dtype
                    Typecode or data-type to which the array is cast.
        
                order : not supported
        
                casting : not supported
                    
                subok : bool, optional
                    If True, then sub-classes will be passed-through (default), 
                    otherwise the returned array will be forced to be a base-class 
                    array.
    
                copy : not supported
        
            Returns
        
                arr_t : ndarray
                    A new array of the same shape as the input array, with dtype, 
                    order given by dtype, order.
        
            Raises
        
                ComplexWarning
                    When casting from complex to float or int. To avoid this, 
                    one should use a.real.astype(t).
    
        '''
        if subok:
            return self._new(buffer = self.buffer.astype(dtype))
        else:
            return ndarray(buffer = self.buffer.astype(dtype))
        
    def flatten(self, order='C'):
        ''' Return a copy of the array collapsed into one dimension.
        
            Parameters
        
                order : not supported
        
            Returns
        
                y : ndarray
                    A copy of the input array, flattened to one dimension.
        
        '''
        return ndarray(buffer=self.buffer.flatten())
    
    def ravel(self):
        ''' Return a contiguous flattened array.
        
            A 1-D array, containing the elements of the input, is returned. 
            A copy is made only if needed.
        
            Parameters
        
                order : not supported
        
            Returns
        
                y : array_like
                    y is an array of the same subtype as a, with shape (a.size,). 
        
        '''
        return self._new(self.buffer.ravel())
        
    def repeat(self, repeats, axis=None):
        ''' Repeat elements of an array.
        
            Parameters
        
                repeats : int or array of ints
                    The number of repetitions for each element. repeats is broadcasted to 
                    fit the shape of the given axis.
        
                axis : int, optional
                    The axis along which to repeat values. By default, use the flattened 
                    input array, and return a flat output array.
        
            Returns
        
                repeated_array : ndarray
                    Output array which has the same shape as a, except along the given axis.
        
        '''
        return self._new(nxa.repeat(self.buffer, repeats, axis))
        
    def clip(self, min=None, max=None, out=None):
        ''' Clip (limit) the values in an array.
        
            Given an interval, values outside the interval are clipped to the 
            interval edges. For example, if an interval of [0, 1] is specified, 
            values smaller than 0 become 0, and values larger than 1 become 1.
        
            Equivalent to but faster than np.minimum(a_max, np.maximum(a, a_min)).
        
            No check is performed to ensure a_min < a_max.
        
            Parameters
        
                a_min : scalar or array_like or None
                    Minimum value. If None, clipping is not performed on lower 
                    interval edge. Not more than one of a_min and a_max may be None.
        
                a_max : scalar or array_like or None
                    Maximum value. If None, clipping is not performed on upper 
                    interval edge. Not more than one of a_min and a_max may be None. 
                    If a_min or a_max are array_like, then the three arrays will be 
                    broadcasted to match their shapes.
        
                out : ndarray, optional
                    The results will be placed in this array. It may be the input 
                    array for in-place clipping. out must be of the right shape to 
                    hold the output. Its type is preserved.
        
            Returns
        
                clipped_array : ndarray
                    An array with the elements of a, but where values < a_min are 
                    replaced with a_min, and those > a_max with a_max.
        
        '''
        if out is None:
            return self._new(buffer = self.buffer.clip(min, max))
        else :
            if not isinstance(out, ndarray):
                out = np.asanyarray(out)
            return self._new(buffer = self.buffer.clip(min, max, out.buffer))
    
    def put(self, indices, values, mode='raise'):
        ''' Replaces specified elements of an array with given values.
        
            The indexing works on the flattened target array. put is roughly equivalent to:
        
            a.flat[ind] = v
        
            Parameters
        
                i : ndarray_like
                    Target indices, interpreted as integers.
        
                v : array_like
                    Values to place in a at target indices. If v is shorter than ind it will 
                    be repeated as necessary.
        
                mode{'raise', 'wrap', 'clip'}, optional
        
                    Specifies how out-of-bounds indices will behave.
        
                        'raise' - raise an error (default)
        
                        'wrap' - wrap around
        
                        'clip' - clip to the range
        
                    'clip' mode means that all indices that are too large are replaced by the 
                    index that addresses the last element along that axis. Note that this 
                    disables indexing with negative numbers. In 'raise' mode, if an exception 
                    occurs the target array may still be modified.
        
        '''
        self.buffer.put(indices, values, mode)
        
    def moveaxis(self, source, destination):
        ''' Move axes of an array to new positions.
        
            Other axes remain in their original order.
        
            Parameters
        
                source : int or sequence of int
                    Original positions of the axes to move. These must be unique.
        
                destination : int or sequence of int
                    Destination positions for each of the original axes. These must also be unique.
        
            Returns
        
                result : np.ndarray
                    Array with moved axes. This array is a view of the input array.
        
        '''
        if type(source) is int:
            if destination < 0:
                destination = self.ndim + destination
            dims = range(self.ndim)
            s = dims[source]
            nd = dims[:source] + dims[source + 1:]
            nd.insert(destination, s)
        elif hasattr(source, '__len__'):
            dims = range(self.ndim)
            s = []
            for i in xrange(len(source)):
                s.append([dims[source[i]], destination[i]])
            s = sorted(s, key = lambda g:g[1])
            nd = []
            source.sort()
            start = -1
            for i in source:
                nd += dims[start : i]
                start = i + 1
            nd += dims[start :]
            for g in s:
                nd.insert(g[1], g[0])
        return self._new(buffer=self.buffer.permute(nd))
    
    def swapaxes(self, axis1, axis2):
        ''' Interchange two axes of an array.
        
            Parameters
        
                axis1 : int
                    First axis.
                
                axis2 : int
                    Second axis.
        
            Returns
        
                a_swapped : ndarray
                    a veiw of the original array is returned
        
        '''
        nd = range(self.ndim)
        nd[axis1] = axis2
        nd[axis2] = axis1
        return self._new(buffer=self.buffer.permute(nd))
    
    def transpose(self, axes=None):
        ''' Reverse or permute the axes of an array; returns the modified array.
        
            For an array a with two axes, transpose(a) gives the matrix transpose.
        
            Parameters
        
                axes : tuple or list of ints, optional
                    If specified, it must be a tuple or list which contains a 
                    permutation of [0,1,..,N-1] where N is the number of axes of a. 
                    The i'th axis of the returned array will correspond to the axis 
                    numbered axes[i] of the input. If not specified, defaults to 
                    range(a.ndim)[::-1], which reverses the order of the axes.
        
            Returns
        
                p : ndarray
                    a with its axes permuted. A view is returned whenever possible.
        
        '''
        if axes != None and len(axes) > 2:
            return self._new(buffer = self.buffer.permute(axes))
        else:
            return self._new(buffer = self.buffer.transpose(axes))
       
    def compress(self, condition, axis=None, out=None):
        ''' Return selected slices of an array along given axis.
        
            When working along a given axis, a slice along that axis is returned in output for each index where condition evaluates to True. When working on a 1-D array, compress is equivalent to extract.
        
            Parameters
        
                condition : 1-D array of bools
                    Array that selects which entries to return. If len(condition) is less than the size of a along the given axis, then output is truncated to the length of the condition array.
        
                axis : int, optional
                    Axis along which to take slices. If None (default), work on the flattened array.
            
                out : ndarray, optional
                    Output array. Its type is preserved and it must be of the right shape to hold the output.
        
            Returns
        
                compressed_array : ndarray
                    A copy of a without the slices along axis for which condition is false.
        '''
        if out is None:
            return self._new(buffer = self.buffer.compress(condition, axis))
        else:
            out = np.asanyarray(out)
            return self._new(buffer = self.buffer.compress(condition, axis, out.buffer))
        
    def fill(self, value):
        ''' Fill the array with a scalar value.
    
            Parameters
    
               value : scalar
                   All elements of a will be assigned this value.
        '''
        self.buffer.fill(value)
        
    def reshape(self, *shape):
        ''' Create a copy of the array with a new shape.
        
            Parameters
        
                newshape : int or tuple of ints
                    The new shape should be compatible with the original shape. 
                    If an integer, then the result will be a 1-D array of that length. 
                    One shape dimension can be -1. In this case, the value is inferred 
                    from the length of the array and remaining dimensions.
        
                order : not supported
        
            Returns
                reshaped_array : ndarray
                    This will be a new copy of the array. 
        
        '''
        if len(shape) == 0:
            raise IllegalArgumentError('shape must be provided')
        if len(shape) == 1 :
            if type(shape[0]) is list:
                return self._new(buffer = self.buffer.reshape(shape[0]))
            elif type(shape[0]) is int:
                return self._new(buffer = self.buffer.reshape([shape[0]]))
            else:
                return self._new(buffer = self.buffer.reshape(list(shape[0])))
        else:
            return self._new(buffer = self.buffer.reshape(list(shape)))
        
    def resize(self, *new_size):
        ''' Return a new array with the specified shape.
        
            If the new array is larger than the original array, then the new array is 
            filled with repeated copies of a. Note that this behavior is different 
            from a.resize(new_shape) which fills with zeros instead of repeated copies 
            of a.
        
            Parameters
        
                new_shape : int or tuple of int
                    Shape of resized array.
        
            Returns
        
                reshaped_array : ndarray
                    The new array is formed from the data in the old array, repeated 
                    if necessary to fill out the required number of elements. The data 
                    are repeated in the order that they are stored in memory.
        
        '''
        if len(new_size) == 0:
            raise IllegalArgumentError('new size must be provided')
        if len(new_size) == 1 :
            if type(new_size[0]) is list:
                return self._new(buffer = self.buffer.resize(new_size[0]))
            elif type(new_size[0]) is int:
                return self._new(buffer = self.buffer.resize([new_size[0]]))
            else:
                return self._new(buffer = self.buffer.resize(list(new_size[0])))
        else:
            return self._new(buffer = self.buffer.resize(list(new_size)))
        
        
    def squeeze(self, axis = None):
        ''' Remove single-dimensional entries from the shape of an array. Return 
            an array with the same storage.
        
            Parameters
        
                axis : None or int or tuple of ints, optional
                    Selects a subset of the single-dimensional entries in the shape. 
                    If an axis is selected with shape entry greater than one, 
                    an error is raised.
        
            Returns
        
                squeezed : ndarray
                    The input array, but with all or a subset of the dimensions of 
                    length 1 removed. This is always a itself or a view into a. 
                    Note that if all axes are squeezed, the result is a 0d array and 
                    not a scalar.
        
            Raises
        
                ValueError
                    If axis is not None, and an axis being squeezed is not of length 1
        
        '''
        return ndarray(buffer = self.buffer.squeeze(axis))
        
    def searchsorted(self, v, side='left', sorter=None):
        ''' Find indices where elements should be inserted to maintain order.
        
            Find the indices into a sorted array a such that, if the corresponding 
            elements in v were inserted before the indices, the order of a would 
            be preserved.
        
            Assuming that a is sorted:
        
                side    returned index i satisfies
                left    a[i-1] < v <= a[i]
                right   a[i-1] <= v < a[i]
        
            Parameters
        
                v : array_like
                    Values to insert into a.
        
                side : {'left', 'right'}, optional
                    If 'left', the index of the first suitable location found is 
                    given. If 'right', return the last such index. If there is no 
                    suitable index, return either 0 or N (where N is the length 
                    of a).
                    
                sorter : not supported
        
            Returns
        
                indices : array of ints
                    Array of insertion points with the same shape as v.
        
        '''
        return self._new(buffer = self.buffer.searchsorted(v, side, sorter))
        
    def diagonal(self, offset=0, axis1=0, axis2=1):
        """ Return specified diagonals.
            If `a` is 2-D, returns the diagonal of `a` with the given offset,
            i.e., the collection of elements of the form ``a[i, i+offset]``.  If
            `a` has more than two dimensions, then the axes specified by `axis1`
            and `axis2` are used to determine the 2-D sub-array whose diagonal is
            returned.  The shape of the resulting array can be determined by
            removing `axis1` and `axis2` and appending an index to the right equal
            to the size of the resulting diagonals.
        
            Parameters
            ----------
                offset : int, optional
                    Offset of the diagonal from the main diagonal.  Can be positive or
                    negative.  Defaults to main diagonal (0).
                
                axis1 : int, optional
                    Axis to be used as the first axis of the 2-D sub-arrays from which
                    the diagonals should be taken.  Defaults to first axis (0).
                
                axis2 : int, optional
                    Axis to be used as the second axis of the 2-D sub-arrays from
                    which the diagonals should be taken. Defaults to second axis (1).
        
            Returns
            -------
                array_of_diagonals : ndarray
                    If `a` is 2-D, then a 1-D array containing the diagonal and of the
                    same type as `a` is returned unless `a` is a `matrix`, in which case
                    a 1-D array rather than a (2-D) `matrix` is returned in order to
                    maintain backward compatibility.
                    If ``a.ndim > 2``, then the dimensions specified by `axis1` and `axis2`
                    are removed, and a new axis inserted at the end corresponding to the
                    diagonal.
            Raises
            ------
                ValueError
                    If the dimension of `a` is less than 2.
        """
        return ndarray(buffer = self.buffer.diagonal(offset, axis1, axis2))
    
    def trace(self, offset=0, axis1=0, axis2=1, dtype=None, out=None):
        ''' Return the sum along diagonals of the array.
        
            If a is 2-D, the sum along its diagonal with the given offset is returned, 
            i.e., the sum of elements a[i,i+offset] for all i.
        
            If a has more than two dimensions, then the axes specified by axis1 and axis2 
            are used to determine the 2-D sub-arrays whose traces are returned. The shape 
            of the resulting array is the same as that of a with axis1 and axis2 removed.
        
            Parameters
        
                offset : int, optional
                    Offset of the diagonal from the main diagonal. Can be both positive 
                    and negative. Defaults to 0.
                    
                axis1, axis2 : int, optional
                    Axes to be used as the first and second axis of the 2-D sub-arrays 
                    from which the diagonals should be taken. Defaults are the first two 
                    axes of a.
                    
                dtype : dtype, optional
                    Determines the data-type of the returned array and of the accumulator 
                    where the elements are summed. If dtype has the value None and a is 
                    of integer type of precision less than the default integer precision, 
                    then the default integer precision is used. Otherwise, the precision 
                    is the same as that of a.
                    
                out : ndarray, optional
                    Array into which the output is placed. Its type is preserved and it 
                    must be of the right shape to hold the output.
        
            Returns
        
                sum_along_diagonals : ndarray
                    If a is 2-D, the sum along the diagonal is returned. If a has larger 
                    dimensions, then an array of sums along diagonals is returned.
        
        '''
        if out is None:
            return self._new(buffer = self.buffer.trace(offset, axis1, axis2, dtype))
        else:
            out = np.asanyarray(out).buffer
            r = self.buffer.trace(offset, axis1, axis2, dtype, out)
            return out            
        
    def tril(self, k=0):
        ''' Lower triangle of an array.
        
            Return a copy of an array with elements above the k-th diagonal zeroed.
        
            Parameters
        
                k : int, optional
                    Diagonal above which to zero elements. k = 0 (the default) is the 
                    main diagonal, k < 0 is below it and k > 0 is above.
        
            Returns
        
                tril : ndarray, shape (M, N)
                    Lower triangle of m, of same shape and data-type as m.
        
        '''
        return ndarray(buffer = self.buffer.tril(k))

    def triu(self, k=0):
        ''' Upper triangle of an array.
        
            Return a copy of a matrix with the elements below the k-th diagonal zeroed.
        
            Please refer to the documentation for tril for further details.
        
        '''
        return ndarray(buffer = self.buffer.triu(k))
        
    def take(self, indices, axis=None, out=None, mode=None):
        ''' Take elements from an array along an axis.
        
            When axis is not None, this function does the same thing as "fancy" indexing 
            (indexing arrays using arrays); however, it can be easier to use if you need 
            elements along a given axis. A call such as np.take(arr, indices, axis=3) is 
            equivalent to arr[:,:,:,indices,...].
        
            Explained without fancy indexing, this is equivalent to the following use of 
            ndindex, which sets each of ii, jj, and kk to a tuple of indices:
        
                Ni, Nk = a.shape[:axis], a.shape[axis+1:]
                Nj = indices.shape
                for ii in ndindex(Ni):
                    for jj in ndindex(Nj):
                        for kk in ndindex(Nk):
                            out[ii + jj + kk] = a[ii + (indices[jj],) + kk]
        
            Parameters
        
                indices : array_like (Nj ...)
                    The indices of the values to extract.
        
                    Also allow scalars for indices.
        
                axis : int, optional
                    The axis over which to select values. By default, the flattened input 
                    array is used.
                    
                out : ndarray, optional (Ni ..., Nj ..., Nk ...)
                    If provided, the result will be placed in this array. It should be of 
                    the appropriate shape and dtype. 
                    
                mode : not supported
        
            Returns
        
                out : ndarray (Ni ..., Nj ..., Nk ...)
                    The returned array has the same type as a.
        
        '''
        return self._new(buffer = self.buffer.take(indices, axis, out))
        
    def tolist(self):
        ''' Return the array as an a.ndim-levels deep nested list of Python scalars.
    
            Return a copy of the array data as a (nested) Python list. Data items are 
            converted to the nearest compatible builtin Python type, via the item 
            function.
            
            If a.ndim is 0, then since the depth of the nested list is 0, it will not 
            be a list at all, but a simple Python scalar.
            
            Parameters
            
                none
            
            Returns
            
                y : object, or list of object, or list of list of object, or 
            
                    The possibly nested list of array elements.
    
        '''
        if self.size == 0:
            return []
        else:
            return self.buffer.tolist()
        
    ''' the following methods were not implemented
    
        argpartition(kth[, axis, kind, order])
    

        argsort([axis, kind, order])
    

        astype(dtype[, order, casting, subok, copy])
    

        byteswap([inplace])
        
        choose(choices[, out, mode])
        
        conj()
        
        conjugate()
        
        getfield(dtype, offset=0)
        
        newbyteorder(new_order='S')
        
        partition(kth, axis=-1, kind='introselect', order=None)

        setfield(val, dtype, offset=0)
        
        setflags(write=None, align=None, uic=None)
        
        tobytes(order='C')
        
        tofile(fid, sep="", format="%s")
        
        view([dtype][, type])
        
    '''

#####################################################################################
# Array slice iter class
#####################################################################################
class SliceIter():
    ''' A iterator to iterate an array by taking slice by slice. 
    '''
    def __init__(self, array):
        self.array = array
        self.cur_slice = -1
    
    def next(self):
        ''' Get the next slice in the iterating routine. '''
        if self.has_next() :
            self.cur_slice += 1
            arr = ndarray(buffer = self.array.buffer.get_slice(0, self.cur_slice))
        else :
            raise StopIteration
        return arr
        
    def curr(self):
        ''' Return the current slice in the iterating routine. '''
        return ndarray(buffer = self.array.buffer.get_slice(0, self.cur_slice))
    
    def has_next(self):
        ''' Return whether there is more slice in the iterating routine. '''
        return self.cur_slice < len(self.array) - 1
        