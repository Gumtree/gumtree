import array
Array = array.Array
import simpledata
SimpleData = simpledata.SimpleData
import data
Data = data.Data
import dataset
from dataset import *
DatasetFactory = dataset.DatasetFactory
#import browser
from dataset import *

old_dir = dir

def dir(obj = None):
    if obj is None :
        return old_dir()
    if hasattr(obj, '__dir__') :
        return obj.__dir__()
    else :
        try :
            return obj.__dict__.keys()
        except :
            return old_dir(obj)