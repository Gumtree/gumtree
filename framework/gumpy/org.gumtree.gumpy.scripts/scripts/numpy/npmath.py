'''
    @author: nxi
'''
import numpy as np

''' Clip (limit) the values in an array.

    Given an interval, values outside the interval are clipped to the 
    interval edges. For example, if an interval of [0, 1] is specified, 
    values smaller than 0 become 0, and values larger than 1 become 1.

    Equivalent to but faster than np.minimum(a_max, np.maximum(a, a_min)).

    No check is performed to ensure a_min < a_max.

    Parameters

        a : array_like
            Array containing elements to clip.

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
def clip(a, a_min, a_max, out=None, **kwargs):
    a = np.asanyarray(a)
    return a.clip(a_min, a_max, out)