'''
    @author: nxi
'''
import random
import numpy as np

_engine = random.random
_para = None

def rand(*shape):
    ''' Random values in a given shape.

        Note

            This is a convenience function for users porting code from Matlab, and wraps 
            random_sample. That function takes a tuple to specify the size of the output, 
            which is consistent with other NumPy functions like numpy.zeros and numpy.ones.

            Create an array of the given shape and populate it with random samples from 
            a uniform distribution over [0, 1).

    Parameters

        d0, d1, , dn : int, optional
            The dimensions of the returned array, must be non-negative. If no argument 
            is given a single Python float is returned.

    Returns

        out : ndarray, shape (d0, d1, ..., dn)
            Random values.

    '''
    arr = np.zeros(shape)
    aiter = arr.buffer.item_iter()
    try :
        while True :
            aiter.set_next(_engine())
    except StopIteration:
        pass
    return arr


def randn(*args):
    ''' Return a random matrix with data from the "standard normal" distribution.
    
        randn generates a matrix filled with random floats sampled from a univariate "normal" 
        (Gaussian) distribution of mean 0 and variance 1.
    
        Parameters
    
            *args : tuple of int values
                Shape of the output. If given as N integers, each integer specifies the size 
                of one dimension. If given as a tuple, this tuple gives the complete shape.
    
        Returns
            Z : matrix of floats
                A matrix of floating-point samples drawn from the standard normal distribution.
    
    '''
    arr = np.zeros(args)
    aiter = arr.buffer.item_iter()
    try :
        while True :
            aiter.set_next(random.normalvariate(0, 1))
    except StopIteration:
        pass
    return arr

