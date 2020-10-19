'''
    @author: nxi
'''
import random
import numpy as np

_engine = random.random
_para = None

def rand(*shape):
    arr = np.zeros(shape)
#     if engine is None :
#         if para is None :
#             engine = random.random
#         else :
#             engine = random.uniform
    aiter = arr.buffer.item_iter()
    try :
        while True :
            aiter.set_next(_engine())
    except StopIteration:
        pass
#     if para is None :
#         try :
#             while True :
#                 aiter.set_next(engine())
#         except StopIteration:
#             pass
#     else :
#         comd = 'engine('
#         if hasattr(para, '__len__') :
#             for item in para :
#                 comd += str(item) + ","
#             comd +=")"
#         else :
#             comd += str(para) + ")"
#         print comd
#         try :
#             while True :
#                 aiter.set_next(eval(comd))
#         except StopIteration:
#             pass
    return arr


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
def randn(*args):
    arr = np.zeros(args)
    aiter = arr.buffer.item_iter()
    try :
        while True :
            aiter.set_next(random.normalvariate(0, 1))
    except StopIteration:
        pass
    return arr

