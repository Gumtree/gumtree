'''
@author: nxi
'''
from java.lang import Double

nan = float('nan')
inf = float('inf')
NaN = nan
NAN = nan
Nan = nan

def iterable(object):
    return hasattr(object, '__iter__')

def isfinite(obj):
    return Double.isFinite(obj)

def isnan(obj):
    return Double.isNaN(obj)

class npslice():
    def __init__(self):
        pass
    
    def __getitem__(self, s):
        return s

s_ = npslice()