'''
    @author nxi
'''
import gumpy.nexus.array as nxa
from creation import *

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
def array_equal(a1, a2, equal_nan=False):
    a1 = asanyarray(a1)
    a2 = asanyarray(a2)
    return nxa.array_equal(a1.buffer, a2.buffer, equal_nan)

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
def allclose(a, b, rtol=1e-05, atol=1e-08, equal_nan=False):
    a = asanyarray(a)
    b = asanyarray(b)
    return nxa.allclose(a.buffer, b.buffer, rtol, atol, equal_nan)
