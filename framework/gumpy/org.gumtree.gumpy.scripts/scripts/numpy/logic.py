'''
    @author nxi
'''
import gumpy.nexus.array as nxa
from creation import *

def all(a, axis=None, out=None, keepdims=None):
    ''' Test whether all array elements along a given axis evaluate to True.
    
        Parameters
    
            a : array_like
                Input array or object that can be converted to an array.
    
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
    a = asanyarray(a)
    return a.all(axis, out, keepdims)
    

def any(a, axis=None, out=None, keepdims=None):
    ''' Test whether any array element along a given axis evaluates to True.
    
        Returns single boolean unless axis is not None
    
        Parameters
    
            a : array_like
                Input array or object that can be converted to an array.
    
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
    a = asanyarray(a)
    return a.any(axis, out, keepdims)

def argmax(a, axis=None, out=None):
    ''' Returns the indices of the maximum values along an axis.
    
        Parameters
    
            a : array_like
                Input array.
            
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
    a = asanyarray(a)
    return a.argmax(axis, out)
        
def argmin(a, axis=None, out=None):
    ''' Returns the indices of the minimum values along an axis.
    
        Parameters
    
            a : array_like
                Input array.
    
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
    a = asanyarray(a)
    return a.argmin(axis, out)
    
def array_equal(a1, a2, equal_nan=False):
    ''' True if two arrays have the same shape and elements, False otherwise.
    
        Parameters
    
            a1, a2 : array_like
                Input arrays.
                
            equal_nan : bool
                Whether to compare NaN's as equal. If the dtype of a1 and a2 
                is complex, values will be considered equal if either the real 
                or the imaginary component of a given value is nan.
    
        Returns
    
            b : bool
                Returns True if the arrays are equal.
    
    '''
    a1 = asanyarray(a1)
    a2 = asanyarray(a2)
    return nxa.array_equal(a1.buffer, a2.buffer, equal_nan)

def allclose(a, b, rtol=1e-05, atol=1e-08, equal_nan=False):
    ''' Returns True if two arrays are element-wise equal within a tolerance.
    
        The tolerance values are positive, typically very small numbers. The 
        relative difference (rtol * abs(b)) and the absolute difference atol 
        are added together to compare against the absolute difference between 
        a and b.
    
        NaNs are treated as equal if they are in the same place and if 
        equal_nan=True. Infs are treated as equal if they are in the same 
        place and of the same sign in both arrays.
    
        Parameters
    
            a, b : array_like
                Input arrays to compare.
    
            rtol : float
                The relative tolerance parameter.
    
            atol : float
                The absolute tolerance parameter.
    
            equal_nan : bool
                Whether to compare NaN's as equal. If True, NaN's in a will 
                be considered equal to NaN's in b in the output array.
    
        Returns
    
            allclose : bool
                Returns True if the two arrays are equal within the given 
                tolerance; False otherwise.
    
    '''
    a = asanyarray(a)
    b = asanyarray(b)
    return nxa.allclose(a.buffer, b.buffer, rtol, atol, equal_nan)

def sort(a, axis=-1, kind=None, order='asc'):
    ''' Return a sorted copy of an array.
    
        Parameters
    
            a : array_like
                Array to be sorted.
    
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
    a = asanyarray(a)
    if order.lower() == 'asc':
        reverse = False
    elif order.lower() == 'desc':
        reverse = True
    a.sort(axis, reverse)