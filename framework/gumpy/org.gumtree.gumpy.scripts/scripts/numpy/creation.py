'''
    @author: nxi
'''

from nparray import ndarray
from gumpy.nexus import array as nxa
from errorhandler import *


''' Return a new array of given shape and type, without initializing entries.

    Parameters
    
        shape : int or tuple of int
    
            Shape of the empty array, e.g., (2, 3) or 2.
        dtype : data-type, optional
    
            Desired output data-type for the array, e.g, numpy.int8. Default is numpy.float64.
        order : not supported
    
            For compatibility purpose only
    Returns
    
        out : ndarray
    
            Array of uninitialized (arbitrary) data of the given shape, dtype, and order. 
            Object arrays will be initialized to None.
'''
def empty(shape, dtype=float, order=None):
    return ndarray(shape, dtype)

''' Return a new array with the same shape and type as a given array.

    Parameters
    
        prototype : array_like
    
            The shape and data-type of prototype define these same attributes of the returned array.
        dtype : data-type, optional
    
            Overrides the data type of the result.
    
        order : not supported
    
            For compatibility purpose only.
    
        subok : not supported
    
        shape : int or sequence of ints, optional.
    
            Overrides the shape of the result. 
    
    Returns
    
        out : ndarray
    
            Array of uninitialized (arbitrary) data with the same shape and type as prototype.

'''
def empty_like(prototype, dtype=None, order=None, subok=True, shape=None):
    oshape = nxa.get_shape(prototype)
    if dtype is None:
        dtype = nxa.get_type(prototype)
    out = empty(oshape, dtype)
    if not shape is None:
        out = out.reshape(shape)
    return out

''' Return a 2-D array with ones on the diagonal and zeros elsewhere.

    Parameters
    
        N : int
            Number of rows in the output.
            
        M : int, optional
            Number of columns in the output. If None, defaults to N.
            
        k : int, optional
            Index of the diagonal: 0 (the default) refers to the main diagonal, 
            a positive value refers to an upper diagonal, and a negative value 
            to a lower diagonal.
            
        dtype : data-type, optional
            Data-type of the returned array.
            
        order : not supported
    
    Returns
    
        I : ndarray of shape (N,M)
            An array where all elements are equal to zero, except for the k-th diagonal, whose values are equal to one.

'''
def eye(N, M=None, k=0, dtype=float, order=None):
    if M is None:
        M = N
    out = empty([N, M], float)
    if k >= 0:
        for i in xrange(N):
            j = i + k
            if j < M:
                out[i, j] = 1
            else:
                break
    else:
        for j in xrange(M):
            i = j - k
            if i < N:
                out[i, j] = 1
            else:
                break
    return out  
      
''' Return the identity array.

    The identity array is a square array with ones on the main diagonal.

    Parameters

        n : int
            Number of rows (and columns) in n x n output.

        dtype : data-type, optional
            Data-type of the output. Defaults to float.

    Returns
        out : ndarray

            n x n array with its main diagonal set to one, and all other elements 0.

'''
def identity(n, dtype=None):
    return eye(n, dtype = dtype)

''' Return a new array of given shape and type, filled with ones.

    Parameters

        shape : int or sequence of ints
            Shape of the new array, e.g., (2, 3) or 2.

        dtype : data-type, optional
            The desired data-type for the array, e.g., numpy.int8. Default is numpy.float64.

        order : not supported

    Returns

        out : ndarray
            Array of ones with the given shape, dtype, and order.

'''
def ones(shape, dtype=None, order=None):
    return ndarray(buffer = nxa.ones(shape, dtype))

''' Return an array of ones with the same shape and type as a given array.

    Parameters

        a : array_like
            The shape and data-type of a define these same attributes of the returned array.
        
        dtype : data-type, optional
            Overrides the data type of the result.

        order : not supported

        subok : not supported

        shape : int or sequence of ints, optional.
            Overrides the shape of the result. 

    Returns

        out : ndarray
            Array of ones with the same shape and type as a.

'''
def ones_like(a, dtype=None, order=None, subok=None, shape=None):
    oshape = nxa.get_shape(a)
    if dtype is None:
        dtype = nxa.get_type(a)
    out = ones(oshape, dtype)
    if not shape is None:
        out = out.reshape(shape)
    return out

''' Return a new array of given shape and type, filled with zeros.

    Parameters

        shape : int or tuple of ints
            Shape of the new array, e.g., (2, 3) or 2.
        
        dtype : data-type, optional
            The desired data-type for the array, e.g., numpy.int8. Default is numpy.float64.

        order : not supported

    Returns

        out : ndarray

            Array of zeros with the given shape, dtype, and order.

'''
def zeros(shape, dtype=float, order=None):
    return empty(shape, dtype)

''' Return an array of zeros with the same shape and type as a given array.

    Parameters

        a : array_like
            The shape and data-type of a define these same attributes of the returned array.

        dtype : data-type, optional
            Overrides the data type of the result.

        order : not supported
            Overrides the memory layout of the result. 'C' means C-order, 'F' means F-order, 'A' means 'F' if a is Fortran contiguous, 'C' otherwise. 'K' means match the layout of a as closely as possible.

        subok : not supported

        shape : int or sequence of ints, optional.

            Overrides the shape of the result. 
            
    Returns

        out : ndarray

            Array of zeros with the same shape and type as a.

'''
def zeros_like(a, dtype=None, order=None, subok=None, shape=None):
    return empty_like(a, dtype, shape = shape)

''' Return a new array of given shape and type, filled with fill_value.

    Parameters

        shape : int or sequence of ints
            Shape of the new array, e.g., (2, 3) or 2.

        fill_value : scalar or array_like
            Fill value.

        dtype : data-type, optional

            The desired data-type for the array The default, None, means

                np.array(fill_value).dtype.

        order : not supported

    Returns

        out : ndarray

            Array of fill_value with the given shape, dtype, and order.

'''
def full(shape, fill_value, dtype=None, order=None):
    out = empty(shape, dtype)
    out.fill(fill_value)
    return out

''' Return a full array with the same shape and type as a given array.

    Parameters

        a : array_like
            The shape and data-type of a define these same attributes of the returned array.

        fill_value : scalar
            Fill value.

        dtype : data-type, optional
            Overrides the data type of the result.

        order : not supported

        subok : not supported

        shape : int or sequence of ints, optional.

            Overrides the shape of the result. 

    Returns

        out : ndarray
            Array of fill_value with the same shape and type as a.
'''
def full_like(a, fill_value, dtype=None, order=None, subok=None, shape=None):
    out = empty_like(a, dtype, shape)
    out.fill(fill_value)
    return out

''' Create an array.

    Parameters

        object : array_like
            An array, any object exposing the array interface, an object whose __array__ method \
            returns an array, or any (nested) sequence.

        dtype : data-type, optional
            The desired data-type for the array. If not given, then the type will be determined 
            as the minimum type required to hold the objects in the sequence.

        copy : bool, optional
            If true (default), then the object is copied. Otherwise, a copy will only be made 
            if __array__ returns a copy, if obj is a nested sequence, or if a copy is needed to 
            satisfy any of the other requirements (dtype, order, etc.).

        order : not supported
        
        subok : not supported
        
        ndmin : int, optional

            Specifies the minimum number of dimensions that the resulting array should have. 
            Ones will be pre-pended to the shape as needed to meet this requirement.

    Returns

        out : ndarray
            An array object satisfying the specified requirements.

'''
def array(object, dtype=None, copy=True, order=None, subok=None, ndmin=0):
    return ndarray(buffer = nxa.asarray(object, dtype))

''' same function as array(). See above document for details.'''
def Array(object, dtype=None, copy=True, order=None, subok=None, ndmin=0):
    return array(object, dtype, copy, ndmin=ndmin)
    
    
''' Convert the input to an ndarray, but pass ndarray subclasses through.

    Parameters

        a : array_like
            includes scalars, lists, lists of tuples, tuples, tuples of tuples, 
            tuples of lists, and ndarrays.

        dtype : data-type, optional
            By default, the data-type is inferred from the input data.

        order : not supported

    Returns

        out : ndarray or an ndarray subclass

            Array interpretation of a. If a is an ndarray or a subclass of 
            ndarray, it is returned as-is and no copy is performed.

'''
def asanyarray(a, dtype=None, order=None):
    if isinstance(a, ndarray):
        return a
    elif isinstance(a, nxa.Array):
        return ndarray(dtype, buffer = a)
    else:
        return ndarray(buffer = nxa.asarray(a, dtype))

''' Not supported. Just for compatibility purpose. '''
def ascontiguousarray(a, dtype=None):
    return array(a, dtype)

''' Return an array copy of the given object.

    Parameters

        a : array_like
            Input data.

        order : not supported

        subok : not supported

    Returns

        arr : ndarray
            Array interpretation of a.

'''
def copy(a, order=None, subok=None):
    return array(a)

''' Interpret a buffer as a 1-dimensional array.

    Parameters

        buffer : buffer_like
            An object that exposes the buffer interface.

        dtype : data-type, optional
            Data-type of the returned array; default: float.

        count : int, optional
            Number of items to read. -1 means all data in the buffer.

        offset : int, optional
            Start reading the buffer from this offset (in bytes); default: 0.

'''
def frombuffer(buffer, dtype=float, count=-1, offset=0):
    return array(buffer, dtype, False)


''' Construct an array from data in a text or binary file.

    A highly efficient way of reading binary data with a known data-type, as well 
    as parsing simply formatted text files. Data written using the tofile method 
    can be read using this function.

    Parameters

        file : file or str or Path
            Open file object or filename.

        dtype : data-type
            Data type of the returned array. For binary files, it is used to determine 
            the size and byte-order of the items in the file. Most builtin numeric 
            types are supported and extension types may be supported.

        count : int
            Number of items to read. -1 means all items (i.e., the complete file).

        sep : str
            Separator between items if file is a text file. Empty ("") separator means 
            the file should be treated as binary. Spaces (" ") in the separator match 
            zero or more whitespace characters. A separator consisting only of spaces 
            must match at least one whitespace.

        offset : int

            The offset (in bytes) from the file's current position. Defaults to 0. 
            Only permitted for binary files.

'''
def fromfile(file, dtype=float, count=-1, sep='', offset=0):
    raise NotImplementedError()


def fromfunction(function, shape, dtype=float, **kwargs):
    raise NotImplementedError()

def fromiter(iterable, dtype, count=-1):
    raise NotImplementedError()

def fromstring(string, dtype=float, count=-1, sep=''):
    raise NotImplementedError()

def loadtxt(fname, dtype=float, comments='#', delimiter=None, 
            converters=None, skiprows=0, usecols=None, unpack=False, ndmin=0, 
            encoding='bytes', max_rows=None):
    raise NotImplementedError()

''' Return evenly spaced values within a given interval.

    Values are generated within the half-open interval [start, stop) (in other words, the 
    interval including start but excluding stop). For integer arguments the function is 
    equivalent to the Python built-in range function, but returns an ndarray rather than 
    a list.

    When using a non-integer step, such as 0.1, the results will often not be consistent. 
    It is better to use numpy.linspace for these cases.

    Parameters

        start : number, optional
            Start of interval. The interval includes this value. The default start value is 0.

        stop : number
            End of interval. The interval does not include this value, except in some cases 
            where step is not an integer and floating point round-off affects the length of out.

        step : number, optional
            Spacing between values. For any output out, this is the distance between two 
            adjacent values, out[i+1] - out[i]. The default step size is 1. If step is 
            specified as a position argument, start must also be given.

        dtype : dtype
            The type of the output array. If dtype is not given, infer the data type from the 
            other input arguments.

    Returns

        arange : ndarray
            Array of evenly spaced values.

            For floating point arguments, the length of the result is ceil((stop - start)/step). 
            Because of floating point overflow, this rule may result in the last element of out 
            being greater than stop.

'''
def arange(*args):
    return ndarray(buffer = nxa.arange(*args))

''' Return evenly spaced numbers over a specified interval.

    Returns num evenly spaced samples, calculated over the interval [start, stop].

    The endpoint of the interval can optionally be excluded.

    Changed in version 1.16.0: Non-scalar start and stop are now supported.

    Parameters

        start : array_like
            The starting value of the sequence.

        stop : array_like
            The end value of the sequence, unless endpoint is set to False. In that case, the sequence consists of all but the last of num + 1 evenly spaced samples, so that stop is excluded. Note that the step size changes when endpoint is False.

        num : int, optional
            Number of samples to generate. Default is 50. Must be non-negative.

        endpoint : bool, optional
            If True, stop is the last sample. Otherwise, it is not included. Default is True.

        retstep : bool, optional
            If True, return (samples, step), where step is the spacing between samples.

        dtype : dtype, optional
            The type of the output array. If dtype is not given, infer the data type 
            from the other input arguments.

        axis : not supported

    Returns

        samples : ndarray
            There are num equally spaced samples in the closed interval [start, stop] or 
            the half-open interval [start, stop) (depending on whether endpoint is True 
            or False).

        step : float, optional
            Only returned if retstep is True
            Size of spacing between samples.

'''
def linspace(start, stop, num=50, endpoint=True, retstep=False, dtype=None, axis=0):
    out = nxa.linspace(start, stop, num, None, endpoint, retstep)
    if retstep:
        return (ndarray(buffer = out[0]), out[1])
    else:
        return ndarray(buffer = out)
        
''' Return numbers spaced evenly on a log scale.

    In linear space, the sequence starts at base ** start (base to the power of start) 
    and ends with base ** stop (see endpoint below).

    Non-scalar start and stop are now supported.

    Parameters

        start : array_like
            base ** start is the starting value of the sequence.

        stop : array_like
            base ** stop is the final value of the sequence, unless endpoint is False. 
            In that case, num + 1 values are spaced over the interval in log-space, of 
            which all but the last (a sequence of length num) are returned.

        num : integer, optional
            Number of samples to generate. Default is 50.

        endpoint : boolean, optional
            If true, stop is the last sample. Otherwise, it is not included. 
            Default is True.

        base : float, optional
            The base of the log space. The step size between the elements in 
            ln(samples) / ln(base) (or log_base(samples)) is uniform. Default is 10.0.

        dtype : dtype
            The type of the output array. If dtype is not given, infer the data type 
            from the other input arguments.

        axis : int, optional
            The axis in the result to store the samples. Relevant only if start or 
            stop are array-like. By default (0), the samples will be along a new axis 
            inserted at the beginning. Use -1 to get an axis at the end.

    Returns

        samples : ndarray
            num samples, equally spaced on a log scale.

'''
def logspace(start, stop, num=50, endpoint=True, base=10.0, dtype=None, axis=0):
    oa = nxa.linspace(start, stop, num, None, endpoint)
    oa = base ** oa
    return ndarray(buffer = oa)

def geomspace(start, stop, num=50, endpoint=True, dtype=None, axis=0):
    raise NotImplementedError()

def meshgrid(xi, copy=True, sparse=False, indexing='xy'):
    raise NotImplementedError()

''' Extract a diagonal or construct a diagonal array.

    See the more detailed documentation for numpy.diagonal if you use this 
    function to extract a diagonal and wish to write to the resulting array; 
    whether it returns a copy or a view depends on what version of numpy 
    you are using.

    Parameters

        v : array_like
            If v is a 2-D array, return a copy of its k-th diagonal. 
            If v is a 1-D array, return a 2-D array with v on the k-th diagonal.

        k : int, optional
            Diagonal in question. The default is 0. Use k>0 for diagonals above 
            the main diagonal, and k<0 for diagonals below the main diagonal.

    Returns

        out : ndarray
            The extracted diagonal or constructed diagonal array.

'''
def diag(v, k=0):
    v = asanyarray(v)
    s = v.shape
    if len(s) == 1:
        n = s[0]+abs(k)
        res = zeros((n, n), v.dtype)
        if k >= 0:
            i = k
        else:
            i = (-k) * n
        res[:n-k].flat[i::n+1] = v
        return res
    elif len(s) == 2:
        return diagonal(v, k)
    else:
        raise ValueError("Input must be 1- or 2-d.")
    
''' Create a two-dimensional array with the flattened input as a diagonal.

    Parameters

        v : array_like
            Input data, which is flattened and set as the k-th diagonal of the output.

        k : int, optional
            Diagonal to set; 0, the default, corresponds to the "main" diagonal, 
            a positive (negative) k giving the number of the diagonal above (below) 
            the main.

    Returns

        out : ndarray
            The 2-D output array.

'''
def diagflat(v, k=0):
    return ndarray(buffer = nxa.diagflat(v, k))

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
        a : array_like
            Array from which the diagonals are taken.
        
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
def diagonal(a, offset=0, axis1=0, axis2=1):
    return a.diagonal(offset, axis1, axis2)

''' An array with ones at and below the given diagonal and zeros elsewhere.

    Parameters

        N : int
            Number of rows in the array.

        M : int, optional
            Number of columns in the array. By default, M is taken equal to N.

        k : int, optional
            The sub-diagonal at and below which the array is filled. k = 0 is 
            the main diagonal, while k < 0 is below it, and k > 0 is above. 
            The default is 0.

        dtype : dtype, optional
            Data type of the returned array. The default is float.

    Returns

        tri : ndarray of shape (N, M)
            Array with its lower triangle filled with ones and zero elsewhere; 
            in other words T[i,j] == 1 for j <= i + k, 0 otherwise.

'''
def tri(N, M=None, k=0, dtype=float):
    return ndarray(buffer = nxa.tri(N, M, k, dtype))

''' Lower triangle of an array.

    Return a copy of an array with elements above the k-th diagonal zeroed.

    Parameters

        m : array_like, shape (M, N)
            Input array.
            
        k : int, optional
            Diagonal above which to zero elements. k = 0 (the default) is the 
            main diagonal, k < 0 is below it and k > 0 is above.

    Returns

        tril : ndarray, shape (M, N)
            Lower triangle of m, of same shape and data-type as m.

'''
def tril(m, k=0):
    m = asanyarray(m)
    return m.tril(k)

''' Upper triangle of an array.

    Return a copy of a matrix with the elements below the k-th diagonal zeroed.

    Please refer to the documentation for tril for further details.

'''
def triu(m, k=0):
    m = asanyarray(m)
    return m.triu(k)
    
''' Generate a Vandermonde matrix.

    The columns of the output matrix are powers of the input vector. The order of 
    the powers is determined by the increasing boolean argument. Specifically, when 
    increasing is False, the i-th output column is the input vector raised 
    element-wise to the power of N - i - 1. Such a matrix with a geometric 
    progression in each row is named for Alexandre- Theophile Vandermonde.

    Parameters

        x : array_like
            1-D input array.

        N : int, optional
            Number of columns in the output. If N is not specified, a square array 
            is returned (N = len(x)).

        increasing : bool, optional
            Order of the powers of the columns. If True, the powers increase from 
            left to right, if False (the default) they are reversed.

    Returns

        out : ndarray

            Vandermonde matrix. If increasing is False, the first column is x^(N-1), 
            the second x^(N-2) and so forth. If increasing is True, the columns are 
            x^0, x^1, ..., x^(N-1).
'''
def vander(x, N=None, increasing=False):
    return ndarray(buffer = nxa.vander(x, N, increasing))

