'''
@author nxi
'''
from creation import *


''' Gives a new shape to an array without changing its data.

    Parameters

        a : array_like
            Array to be reshaped.
            
        newshape : int or tuple of ints
            The new shape should be compatible with the original shape. 
            If an integer, then the result will be a 1-D array of that length. 
            One shape dimension can be -1. In this case, the value is inferred 
            from the length of the array and remaining dimensions.

        order : not supported

    Returns
        reshaped_array : ndarray
            This will be a new view object if possible; otherwise, it will be 
            a copy. 

'''
def reshape(a, newshape, order='C'):
    return asanyarray(a).reshape(*newshape)