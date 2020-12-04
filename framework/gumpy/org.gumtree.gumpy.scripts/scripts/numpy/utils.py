'''
@author: nxi
'''
from java.lang import Double
import numbers

''' Define NaN, Inf number for numpy '''
nan = float('nan')
inf = float('inf')
NaN = nan
NAN = nan
Nan = nan

def iterable(object):
    ''' Return if the object is iterable.
    '''
    return hasattr(object, '__iter__')

def isfinite(obj):
    ''' Return if the number is infinite.
    '''
    return Double.isFinite(obj)

def isscalar(obj):
    ''' Returns True if the type of element is a scalar type.

        Parameters
        
            obj : any
                Input argument, can be of any type and shape.
        
        Returns
        
            val : bool
                True if element is a scalar type, False if it is not.

    '''
    return isinstance(obj, numbers.Number)
    
def isnan(obj):
    ''' Return if the value is NaN.
    '''
    return Double.isNaN(obj)

class npslice():
    ''' A helper class to create a slice instance. '''
    def __init__(self):
        pass
    
    def __getitem__(self, s):
        return s

s_ = npslice()