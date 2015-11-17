'''
Created on 31/01/2011

@author: nxi
'''

#from data import Data
from org.gumtree.data.nexus.utils import NexusUtils
from org.gumtree.data.nexus.utils import NexusFactory
from org.gumtree.data.utils import FactoryManager
#from nexus import array
#from nexus.array import Array
#from nexus import simpledata
#from nexus.simpledata import SimpleData
#from nexus import data
#from nexus.data import Data
from gumpy.nexus import *
import os.path
from copy import copy

DEFAUT_ENTRY_NAME = "entry1"
nx_factory = NexusFactory()
gdm_factory = FactoryManager().getFactory()
s_ = array.s_

class Dataset(Data):
    '''
    A Nexus dataset that interpreting a Nexus file. It provides accessing to the 
    data of the file.
    '''
    __dicpath__ = None
    
    def __init__(self, storage, shape = None, dtype = None, name = None, \
                 var = None, axes = None, anames = None, aunits = None, \
                 default_var = True, default_axes = True, dic_path = None, title = None, skip_flaws = False):
        '''
        Constructor
        '''
        if type(storage) is int or type(storage) is float or type(storage) is long or type(storage) is bool :
            storage = [storage]
        if type(storage) is str :
            storage = NexusUtils.readNexusDataset(storage)
        if hasattr(storage, '__len__') :
            Data.__init__(self, storage, shape, dtype, var, axes, anames, \
                          aunits, default_var, default_axes)
            self.__iNXDataset__ = nx_factory.createNXDataset()
            self.__iNXDataset__.open()
            self.__iNXroot__ = self.__iNXDataset__.getNXroot()
            nx_factory.createNXentry(self.__iNXroot__, DEFAUT_ENTRY_NAME, \
                                         self.__iNXdata__)
        else :
            self.__iNXDataset__ = storage
            self.__iNXroot__ = storage.getNXroot()
            Data.__init__(self, self.__iNXroot__.getFirstEntry().getData(), skip_flaws = skip_flaws)
        if not dic_path is None :
            self.__iDictionary__ = gdm_factory.openDictionary(dic_path)
            self.__iNXroot__.setDictionary(self.__iDictionary__)
        elif not Dataset.__dicpath__ is None :
            self.__iDictionary__ = gdm_factory.openDictionary(\
                                                Dataset.__dicpath__)
            self.__iNXroot__.setDictionary(self.__iDictionary__)
        else :
            self.__iDictionary__ = gdm_factory.createDictionary()
            self.__iNXroot__.setDictionary(self.__iDictionary__)
            items = self.__iNXroot__.getDataItemList()
            for item in items :
                self.__iDictionary__.addEntry(item.getShortName(), nx_factory.createPath('/' + item.getShortName()))
        if name == None :
            location = self.__iNXDataset__.getLocation()
            if not location is None :
                sep = None
                if location.__contains__(os.path.sep):
                    sep = os.path.sep
                elif location.__contains__('/') :
                    sep = '/'
                if not sep is None:
                    name = location.split(sep)[-1]
                else:
                    name = location
            else :
                name = str(id(self))
            self.name = name
        if title is None :
            title = name
        self.set_title(title)

    
    def __setattr__(self, name, value):
        if name == 'name' :
            self.__dict__[name] = value
        elif name == 'title' :
            self.set_title(str(value))
        elif name == 'location' :
            self.set_location(str(value))
        elif name == 'var' :
            self.set_var(value)
        elif name == 'err' or name == 'error' :
            self.set_err(value)
        elif name == 'axes' :
            self.set_axes(value)
        elif name == 'skip_flaws' :
            self.__dict__[name] = value
        elif str(name).startswith('__') :
            self.__dict__[name] = value
        elif name == 'storage' :
            if hasattr(self, '__iNXdata__') :
                data = SimpleData(value)
                data.__set_name__(self.__iDataItem__.getShortName())
                self.__iNXdata__.removeDataItem(self.__iDataItem__)
                self.__iNXdata__.addDataItem(data.__iDataItem__)
            self.__dict__[name] = value
            if hasattr(self, '__iDictionary__') :
                self.__iDictionary__.addEntry(name, nx_factory.createPath('/' + name))
        elif hasattr(self, 'storage') and hasattr(self.storage, name):
            raise AttributeError, 'attribute ' + name + ' is not set-able'
        else :
            if hasattr(self, '__iNXroot__') and not self.__iNXroot__ is None :
                item = self.__iNXroot__.findDataItem(name)
                if not item is None :
                    data = SimpleData(item, skip_flaws = self.skip_flaws)
                    if not value is None :
                        dtype = type(value)
                        if dtype is int or dtype is float or dtype is long :
                            data.fill(value)
                        elif dtype is str :
                            item.setCachedData(Array(value).__iArray__, False)
                        elif hasattr(value, '__len__') :
                            data.copy_from(value)
                        else :
                            item.setCachedData(Array(value), False)
                    else :
                        item.getParentGroup().removeDataItem(item)
                        self.__dict__[name] = value
                else :
                    self.add_metadata(name, value)
            else :
                self.__dict__[name] = value

    def add_metadata(self, name, value, tag = None, append = False):
        if hasattr(self, '__iNXroot__') and not self.__iNXroot__ is None :
            item = self.__iNXroot__.findDataItem(name)
            if not item is None :
                data = SimpleData(item)
                if tag:
                    data.set_attribute('METADATA_TAG', str(tag))
#                    setattr(data, 'METADATA_TAG', str(tag))
                dtype = type(value)
                if dtype is int or dtype is float or dtype is long :
                    data.fill(value)
                elif dtype is str :
                    if append:
                        value = str(item.getData().toString()) + '\n' + value
                    item.setCachedData(Array(value).__iArray__, False)
                elif hasattr(value, '__len__') :
                    data.copy_from(value)
                else :
                    item.setCachedData(Array(value), False)
            else :
                data = SimpleData(value)
                data.__set_name__(name)
                if tag:
                    data.set_attribute('METADATA_TAG', str(tag))
#                    setattr(data, 'METADATA_TAG', str(tag))
                self.__iNXroot__.addDataItem(data.__iDataItem__)
            if hasattr(self, '__iDictionary__') :
                keys = self.__iDictionary__.addEntry(name, '/' + name)

    def harvest_metadata(self, tag):
        meta = dict()
        if hasattr(self, '__iDictionary__') :
            keys = self.__iDictionary__.getAllKeys().toArray()
            for key in keys :
                item = self.__iNXroot__.findDataItem(self.__iDictionary__.getPath(key).getValue())
                if item:
                    if item.getAttribute('METADATA_TAG'):
                        meta[key.getName()] = SimpleData[item]
            items = self.__iNXroot__.getDataItemList()
            for item in items :
                if item.getAttribute('METADATA_TAG'):
                    name = str(item.getShortName())
                    meta[name] = SimpleData(item)
        return meta
        
    def get_metadata(self, name):
        if name.__contains__('/') :
            item = self.__iNXroot__.findContainerByPath(name)
        else :
            item = self.__iNXroot__.findDataItem(name)
        if not item is None :
            data = SimpleData(item, skip_flaws = self.skip_flaws)
# change the performance
#            if data.size == 1 :
#                return data[0]
#            else :
#                return data
            return data
        else :
            return None 
        
    def set_title(self, title): 
        self.__iNXDataset__.setTitle(str(title))
        
    def set_location(self, location):
        self.__iNXDataset__.setLocation(location)
    
    def set_data(self, data):
        self.__iNXdata__ = data.__iNXdata__
        self.__iNXroot__.getDefaultEntry().removeGroup('data')
        self.__iNXroot__.getDefaultEntry().addSubgroup(data.__iNXdata__)

    def __getattr__(self, name):
        if name == 'title' or name == 'name' :
            return self.__iNXDataset__.getTitle()
        elif name == 'log' :
            try:
                log = self.get_metadata('log')
                if not log :
                    return ''
                else:
                    return str(log)
            except:
                return ''
        elif name == 'id' :
            location = self.__iNXDataset__.getLocation()
            fname = location
            if not location is None:
                fname = os.path.basename(location)
                try:
                    flen = len(fname)
                    fid = fname[flen - 14 : flen - 7]
                    fid = int(fid)
                    return fid
                except:
                    pass
            return fname
        elif name == 'location' :
            return self.__iNXDataset__.getLocation()
        elif name == 'skip_flaws' :
            return self.__dict__[name]
        elif name == 'metadata' :
            dirs = []
            if hasattr(self, '__iDictionary__') :
                keys = self.__iDictionary__.getAllKeys().toArray()
                for key in keys :
                    dirs.append(str(key))
            items = self.__iNXroot__.getDataItemList()
            for item in items :
                name = str(item.getShortName())
                if not dirs.__contains__(name) :
                    dirs.append(str(item.getShortName()))
            return dirs
#        elif hasattr(self, 'storage') and hasattr(self.storage, name) :
#            return self.storage.__getattr__(name)
        elif name == '__iArray__' :
            return self.storage.__iArray__
        elif str(name).startswith('__') :
            try :
                return self.__dict__[name]
            except :
                raise AttributeError, name + ' not exists'
        else :
            item = self.__iNXroot__.findDataItem(name)
            if not item is None :
                data = SimpleData(item, skip_flaws = self.skip_flaws)
                if data.size == 1 :
                    return data[0]
                else :
                    return data
            else :
                item = self.__iNXroot__.getAttribute(name)
                if not item is None :
                    data = SimpleData(str(item.value))
                    if data.size == 1 :
                        return data[0]
                    else :
                        return data
                else :
                    return Data.__getattr__(self, name) 
    
    def close(self):
        if self.__iNXDataset__.isOpen() :
            self.__iNXDataset__.close()
#        self.storage = None
#        self.var = None
#        self.axes = None
#        self.__iNXdata__ = None
#        self.__iNXroot__ = None
#        self.__iNXDataset__ = None
        
    def __del__(self): 
        self.close()
        
    def __new__(self, storage, name = None, var = None, axes = None, \
                anames = None, aunits = None, title = None):
        if title is None :
            title = self.title
        if not title.endswith('_') :
            title += '_'
        return new(storage, name, var, axes, anames, aunits, False, False, title = title)

    
    def __copy_metadata__(self, dfrom, mslice = None, deep = False):
        if hasattr(dfrom, '__iDictionary__') and not dfrom.__iDictionary__ is None :
            keys = dfrom.__iDictionary__.getAllKeys().toArray()
            for key in keys :
                item = dfrom.__iNXroot__.findDataItem(key)
                if not item is None :
                    item.getData(True)
                    sdata = SimpleData(nx_factory.copyToNXDataItem(item))
                    if deep:
                        arr = sdata.storage.__copy__()
                        sdata.__iDataItem__.setCachedData(arr.__iArray__, False)
#                        sdata = sdata.__copy__()
                    stype = sdata.dtype
                    if not mslice is None and (stype is int or stype is float or stype is long) \
                            and sdata.size > 1 : 
                        if type(mslice) is int :
                            mslice = slice(mslice, mslice + 1)
                        sdata = sdata.__getitem__(mslice)
                    sdata.__set_name__(key)
                    self.__iNXroot__.addDataItem(sdata.__iDataItem__)
                    if not self.__iDictionary__ is None :
                        self.__iDictionary__.removeEntry(key.getName())
                        self.__iDictionary__.addEntry(key, nx_factory.createPath('/' + key.getName()))
#                        print key
    
    def copy_metadata_deep(self, dfrom, mslice = None):
        self.__copy_metadata__(dfrom, mslice, True)
        
    def copy_metadata_shallow(self, dfrom, mslice = None):
        self.__copy_metadata__(dfrom, mslice, False)
        
    def __getitem__(self, index):
        if type(index) is str :
            res = self.get_metadata(index)
            if res.dtype is str:
                return str(res)
            elif res.size == 1 :
                return res[0]
            else:
                return res
        res = Data.__getitem__(self, index)
        if isinstance(res, SimpleData) and not hasattr(index, 'ndim') :
            nidx = self.__get_slice_index__(index)
            mslice = None
            if len(nidx) > 2 :
                if type(index) is int :
                    mslice = slice(index, index + 1)
                elif type(index) is slice :
                    mslice = index
                elif type(index) is tuple :
                    if type(index[0]) is int :
                        mslice = slice(index[0], index[0] + 1)
                    else :
                        mslice = index[0]
            res.__copy_metadata__(self, mslice)
            return res
        else :
            return res

    def get_slice(self, dim, index):
        res = Data.get_slice(self, dim, index)
        mslice = None
        if self.ndim > 2 and dim == 0 :
            mslice = slice(index, index + 1)
        res.__copy_metadata__(self, mslice)
        return res
            
    def get_section(self, origin, shape, stride = None): 
        res = Data.get_section(self, origin, shape, stride)
        if stride is None :
            mslice = slice(origin[0], origin[0] + shape[0])
        else :
            mslice = slice(origin[0], origin[0] + shape[0], stride[0])
        res.__copy_metadata__(self, mslice)
        return res
    
    def get_reduced(self, dim = None):
        res = Data.get_reduced(self, dim)
        res.__copy_metadata__(self)
        return res
    
    def __repr__(self, indent = None):
        if indent is None :
            indent = ' ' * 8
        else :
            indent += ' ' * 8
        res = 'Dataset(' + self.storage.__repr__(indent) + str(', \n' \
                + indent + 'title=\'' + self.title + '\'')
        if not self.var is None :
            res += ',\n' + indent + 'var=' + self.var.storage.__repr__(indent + ' ' * 4)
        if len(self.axes) > 0 :
            res += ',\n' + indent + 'axes=['
            for i in xrange(len(self.axes)) :
                res += str(self.axes[i].__repr__(indent + ' ' * 6))
                if i < len(self.axes) - 1 :
                    res += ',\n' + indent + ' ' * 6
            res += ']'
        res += ')'
        return res
    
    def __str__(self, indent = ''):
        res = 'title: ' + self.title + '\n' + indent
        if not self.units is None and len(self.units) > 0 :
            res += 'units: ' + self.units + '\n' + indent
        res = str(res + 'storage: ') + self.storage.__str__(indent + ' ' * 9)
        if not self.var is None :
            res += '\n' + indent + 'error: ' + \
                    (self.var ** 0.5).storage.__str__(indent + ' ' * 7)
        if len(self.axes) > 0 :
            res += '\n' + indent + 'axes:\n' + indent + ' ' * 2
            for i in xrange(len(self.axes)) :
                res += str(i) + '. ' + str(self.axes[i].__str__(indent + ' ' * 5))
                if i < len(self.axes) - 1 :
                    res += '\n' + indent + ' ' * 2
        return res

    def __add__(self, obj):
        res = Data.__add__(self, obj)
        res.__copy_metadata__(self)
        return res
            
    def __div__(self, obj):
        res = Data.__div__(self, obj)
        res.__copy_metadata__(self)
        return res
    
    def __mul__(self, obj):
        res = Data.__mul__(self, obj)
        res.__copy_metadata__(self)
        return res
    
    def inverse(self):
        res = Data.inverse(self)
        res.__copy_metadata__(self)
        return res
        
    def __invert__(self):
        res = Data.__invert__(self)
        res.__copy_metadata__(self)
        return res
    
    def __pow__(self, obj):
        res = Data.__pow__(self, obj)
        res.__copy_metadata__(self)
        return res
    
    def __exp__(self):
        res = Data.__exp__(self)
        res.__copy_metadata__(self)
        return res
    
    def __log10__(self):
        res = Data.__log10__(self)
        res.__copy_metadata__(self)
        return res
    
    def __ln__(self):
        res = Data.__ln__(self)
        res.__copy_metadata__(self)
        return res

    def __sqrt__(self):
        res = Data.__sqrt__(self)
        res.__copy_metadata__(self)
        return res

    def __mod__(self, obj):
        res = Data.__mod__(self, obj)
        res.__copy_metadata__(self)
        return res

    def __sin__(self):
        res = Data.__sin__(self)
        res.__copy_metadata__(self)
        return res
    
    def __cos__(self):
        res = Data.__cos__(self)
        res.__copy_metadata__(self)
        return res
    
    def __tan__(self):
        res = Data.__tan__(self)
        res.__copy_metadata__(self)
        return res
    
    def __arcsin__(self):
        res = Data.__arcsin__(self)
        res.__copy_metadata__(self)
        return res
    
    def __arccos__(self):
        res = Data.__arccos__(self)
        res.__copy_metadata__(self)
        return res
    
    def __arctan__(self):
        res = Data.__arctan__(self)
        res.__copy_metadata__(self)
        return res
    
    def __prod__(self, axis = None):
        res = Data.__prod__(self, axis)
        if axis == 0 :
            res.__copy_metadata__(self)
        return res

    def sum(self, axis = None, dtype = None, out = None):
        if axis is None :
            return self.storage.sum(dtype = dtype)
        else :
            if out is None :
                res = Data.sum(self, axis, dtype)
                res.__copy_metadata__(self)
                return res
            else :
                return Data.sum(self, axis, dtype, out)
        
    def reshape(self, shape): 
        res = Data.reshape(self, shape)
        res.__copy_metadata__(self)
        return res
    
    def flatten(self) :
        res = Data.flatten(self)
        res.__copy_metadata__(self)
        return res

    def view_1d(self):
        res = Data.view_1d(self)
        res.__copy_metadata__(self)
        return res
    
    def __copy__(self):
        res = Data.__copy__(self)
        res.__copy_metadata__(self, deep = True)
        return res

    def float_copy(self):
        res = Data.float_copy(self)
        res.__copy_metadata__(self, deep = True)
        return res
    
    def absolute_copy(self):
        res = Data.absolute_copy(self)
        res.__copy_metadata__(self)
        return res

    def save(self, name = None):
        location = self.location
        if location is None :
            raise IOError, 'can not save to None location'
        else :
            try :
                if name is None :
                    self.__iNXDataset__.save()
                else :
                    item = eval('self.' + name)
                    if not item is None :
                        self.__iNXDataset__.save(self.__iNXroot__.findDataItem(name))
            except :
                raise IOError, 'failed to write to: ' + location
    
    def save_copy(self, path):
        try :
            self.__iNXDataset__.saveTo(path)
        except :
            raise IOError, 'failed to write to: ' + path

#    def argmax(self, axis = None):
#        return self.storage.argmax(axis)
#
#    def argmin(self, axis = None):
#        return self.storage.argmin(axis)
        
    def normalise(self, attr_name): 
        if hasattr(self, attr_name) :
            attr = eval('self.' + attr_name)
            if not attr is None :
                self.storage = self.storage.float_copy()
                amax = attr.max()
                for i in xrange(len(attr)) :
                    if attr[i] != 0 :
                        norm = float(amax) / attr[i]
                    else :
                        norm = 1
                    self[i] *= norm
                self.norm_attr = attr_name
                self.norm_value = amax
                eval('self.' + attr_name + '.fill(amax)')
                
    def __dir__(self):
        dirs = Data.__dir__(self)
        dirs.append('location')
        dirs.append('metadata')
        dirs.append('title')
        if hasattr(self, '__iDictionary__') :
            keys = self.__iDictionary__.getAllKeys().toArray()
            for key in keys :
                dirs.append(str(key))
        items = self.__iNXroot__.getDataItemList()
        for item in items :
            name = str(item.getShortName())
            if not dirs.__contains__(name) :
                dirs.append(str(item.getShortName()))
        return sorted(dirs)
        
    def delete_slice(self, indices):
        nslice = self.shape[0]
        Data.delete_slice(self, indices)
        meta = self.metadata
        if meta is None:
            return
        for item in meta :
            value = self.get_metadata(item)
            if not value is None and hasattr(value, '__len__') and len(value) == nslice :
                oitem = value.__iDataItem__
                value.delete_slice(indices)
                par = oitem.getParentGroup()
                if not par is None :
                    par.removeDataItem(oitem)
                    par.addDataItem(value.__iDataItem__)
        
    def append_log(self, log):
        self.log = self.log + log.encode('ascii') + "\n"
        
    def transpose(self, axes = None):
        res = Data.transpose(self, axes)
        res.__copy_metadata__(self)
        return res
        
    def compress(self, condition, axis = None, out = None):
        res = Data.compress(self, condition, axis, out)
        res.__copy_metadata__(self)
        return res
        
    def clip(self, a_min, a_max, out = None):
        res = Data.clip(self, a_min, a_max, out)
        res.__copy_metadata__(self)
        return res
        
    def mean(self, axis = None, dtype = None, out = None):
        res = Data.mean(self, axis, dtype, out )
        return res
    
    def intg(self, axis = None, out = None, keepdims = False):
        res = Data.intg(self, axis, out, keepdims)
        res.__copy_metadata__(self)
        return res

def new(storage, name = None, var = None, axes = None, anames = None, \
        aunits = None, default_var = True, default_axes = True, title = None) :
        return Dataset(storage, name = name, var = var, axes = axes, \
                       anames = anames, aunits = aunits, default_var = default_var, \
                       default_axes = default_axes, title = title)


#####################################################################################
# Array creation
#####################################################################################    
def zeros(shape, dtype = float, default_var = True, default_axes = True): 
    return new(array.zeros(shape, dtype), default_var = default_var, default_axes = default_axes)

def zeros_like(obj, default_var = True, default_axes = True):
    if isinstance(obj, SimpleData) :
        obj = obj.storage
    return new(array.zeros_like(obj), default_var = default_var, \
               default_axes = default_axes)
    
def ones(shape, dtype = float, default_var = True, default_axes = True):
    return new(array.ones(shape, dtype), default_var = default_var, \
               default_axes = default_axes)
    
def ones_like(obj, default_var = True, default_axes = True):
    if isinstance(obj, SimpleData) :
        obj = obj.storage
    return new(array.ones_like(obj), default_var = default_var, default_axes = default_axes)

def eye(N, M = None, k = 0, dtype = float, default_var = True, default_axes = True):
    return new(array.ones(N, M, k, dtype), default_var = default_var, \
               default_axes = default_axes)
    
def rand(shape, para = None, engine = None, dtype = float, \
         default_var = True, default_axes = True):
    return new(array.rand(shape, para, engine, dtype), \
               default_var = default_var, default_axes = default_axes)
    
def instance(shape, init = 0, dtype = float, var = None,\
             default_var = True, default_axes = True):
    avar = None
    if not var is None :
        avar = array.instance(shape, var, float)
    return new(array.instance(shape, init, dtype), var = avar, \
               default_var = default_var, default_axes = default_axes)

def asarray(obj, dtype = None, var=None, default_var = True, default_axes = True):
    if isinstance(obj, SimpleData) :
        obj = obj.storage
    return new(array.asarray(obj, dtype), var = var, default_var = default_var, \
               default_axes = default_axes)
    
def arange(*args) :
    return new(array.arange(*args))

def linspace(start, end, num, shape = None, endpoint=True, retstep=False, \
             default_var = True, default_axes = True):
    return new(array.linspace(start, end, num, shape, endpoint, retstep), \
        default_var, default_axes)

#####################################################################################
# Create array from existing data
#####################################################################################
def take(sdata, indices, axis=None, out=None):
    return sdata.take(indices, axis, out)
    
#####################################################################################
# Joining arrays
#####################################################################################
def append(obj, val, axis = None, default_var = True, default_axes = True):
    if axis is None :
        if isinstance(obj, SimpleData) :
            obj = obj.storage
        res = new(array.append(obj, val, axis), default_var = default_var, \
               default_axes = default_axes)
    else :
        storage = array.append(obj, val, axis)
        var = None
        if hasattr(obj, 'var') :
            if not obj.var is None :
                if hasattr(val, 'var') and not val.var is None and \
                        val.var.ndim > axis :
                    var = simpledata.append(obj.var, val.var, axis)
                else :
                    var = simpledata.append(obj.var, val, axis)
            else :
                ovar = obj.float_copy()
                if hasattr(val, 'var') and not val.var is None and \
                        val.var.ndim > axis :
                    var = simpledata.append(ovar, val.var, axis)
                else :
                    var = simpledata.append(ovar, val, axis)
        axes = None
        if hasattr(obj, 'axes') and len(obj.axes) > 0 :
            axes = []
            oaxes = obj.axes
            laxes = len(oaxes)
            for i in xrange(obj.ndim) :
                if i >= obj.ndim - laxes :
                    if not i == axis :
                        axes += [oaxes[i - obj.ndim + laxes].__copy__()]
                    else :
                        oaxis = oaxes[i - obj.ndim + laxes]
                        if len(oaxis) > 1 :
                            step = (float(oaxis[len(oaxis) - 1]) - oaxis[0]) / (len(oaxis) - 1)
                            start = oaxis[len(oaxis) - 1] + step
                            stop = oaxis[len(oaxis) - 1] + step * \
                                    (storage.shape[axis] - obj.shape[axis] + 1)
                            axes += [simpledata.append(oaxis, array.arange(start, \
                                                                    stop, step, oaxis.dtype))]
                        elif len(oaxis) == 1 :
                            naxis = range(2, storage.shape[axis] + 1)
                            for i in xrange(len(naxis)) :
                                naxis[i] *= oaxis[0]
                            axes += [simpledata.append(oaxis, naxis)]
                        else :
                            axes += array.arange(storage.shape[axis])
        res = new(array.append(obj, val, axis), var = data.__make_default_var__(var), axes = axes, \
                  default_var = default_var, default_axes = default_axes)
    return res

def concatenate(tup, axis = 0, default_var = True, default_axes = True):
    storage = array.concatenate(tup, axis)
    vars = ()
    for item in tup :
        if hasattr(item, 'var') and not item.var is None :
            vars += (item.var,)
        else :
            vars += (data.__make_default_var__(item),)
    var = array.concatenate(vars, axis)
    return new(storage, var = var, default_var = default_var, default_axes = default_axes)
            
def column_stack(*tup):
    storage = array.column_stack(*tup)
    vars = ()
    for item in tup :
        if hasattr(item, 'var') and not item.var is None :
            vars += (item.var,)
        else :
            vars += (data.__make_default_var__(item),)
    var = array.column_stack(*vars)
    return new(storage, var = var)

def vstack(*tup):
    storage = array.vstack(*tup)
    vars = ()
    for item in tup :
        if hasattr(item, 'var') and not item.var is None :
            vars += (item.var,)
        else :
            vars += (data.__make_default_var__(item),)
    var = array.vstack(*vars)
    return new(storage, var = var)

def hstack(*tup):
    storage = array.hstack(*tup)
    vars = ()
    for item in tup :
        if hasattr(item, 'var') and not item.var is None :
            vars += (item.var,)
        else :
            vars += (data.__make_default_var__(item),)
    var = array.hstack(*vars)
    return new(storage, var = var)
    
def dstack(*tup):
    storage = array.dstack(*tup)
    vars = ()
    for item in tup :
        if hasattr(item, 'var') and not item.var is None :
            vars += (item.var,)
        else :
            vars += (data.__make_default_var__(item),)
    var = array.dstack(*vars)
    return new(storage, var = var)

#####################################################################################
# Splitting arrays
#####################################################################################
def array_split(obj, indices_or_sections, axis = 0):
    storages = simpledata.array_split(obj, indices_or_sections, axis)
    vars = None
    if hasattr(obj, 'var') and not obj.var is None :
        vars = simpledata.array_split(obj.var, indices_or_sections, axis)
    allAxes = None
    if hasattr(obj, 'axes') and not obj.axes is None :
        laxes = len(obj.axes)
        naxis = None
        if laxes > axis :
            naxis = simpledata.array_split(obj.axes[axis], indices_or_sections, 0)
        allAxes = []
        for i in xrange(len(storages)) :
            axes = []
            for j in xrange(obj.ndim) :
                if j >= obj.ndim - laxes :
                    if j == axis :
                        if not naxis is None and len(naxis) > i :
                            axes += [naxis[i]]
                        else :
                            axes += [None]
                    else :
                        axes += [obj.axes[j - obj.ndim + laxes].__copy__()]
                else :
                    axes += [None]
            allAxes += [axes]
    res = []
    for i in xrange(len(storages)) :
        storage = storages[i]
        var = None
        if not vars is None and len(vars) > i :
            var = vars[i]
        axes = None
        if not allAxes is None and len(allAxes) > i :
            axes = allAxes[i]
        res += [new(storage, var = var, axes = axes)]
    return res

def split(obj, indices_or_sections, axis = 0):
    if axis >= obj.ndim :
        raise ValueError, 'axis must be within the ndim of the array, ' + str(axis) \
                    + ' in ' + str(obj.ndim)
    if type(indices_or_sections) is int :
        osize = obj.shape[axis]
        if osize != osize / indices_or_sections * indices_or_sections :
            raise ValueError, 'array split does not result in an equal division'
    return array_split(obj, indices_or_sections, axis)
        
def dsplit(obj, indices_or_sections):
    if obj.ndim < 3 :
        raise ValueError, 'array must have at least 3 dimensions, got ' + str(obj.ndim)
    return array_split(obj, indices_or_sections, 2)

def hsplit(obj, indices_or_sections):
    if obj.ndim == 1 :
        return array_split(obj, indices_or_sections, 0)
    return array_split(obj, indices_or_sections, 1)

def vsplit(obj, indices_or_sections):
    return array_split(obj, indices_or_sections, 0)


#####################################################################################
# Enlarging arrays
#####################################################################################
def tile(obj, reps, default_var = True, default_axes = True):
    var = None
    if hasattr(obj, 'var') and not obj.var is None :
        var = simpledata.tile(obj.var, reps)
    return new(simpledata.tile(obj, reps), var = var, default_var = default_var, \
               default_axes = default_axes)

def repeat(obj, repeats, axis=None, default_var = True, default_axes = True):
    raise TypeError, 'not supported'
    
    
#####################################################################################
# Adding and removing elements
#####################################################################################    
def delete(sdata, obj, axis=None) :
    if axis is None :
        return new(simpledata.delete(sdata, obj, None))
    else :
        storage = simpledata.delete(sdata, obj, axis)
        var = None
        if hasattr(sdata, 'var') and not sdata.var is None :
            var = simpledata.delete(sdata.var, obj, axis)
        if hasattr(sdata, 'axes') and not sdata.axes is None and \
            len(sdata.axes) > 0 :
            laxes = len(sdata.axes)
            axes = []
            for i in xrange(sdata.ndim) :
                if i >= sdata.ndim - laxes :
                    if not i == axis :
                        axes += [sdata.axes[i - sdata.ndim + laxes].__copy__()]
                    else :
                        axes += [simpledata.delete(sdata.axes[i - sdata.ndim + laxes\
                                            ].__copy__(), obj)]
        return new(storage, var = var, axes = axes, 
                   default_var = False, default_axes = False)
                    

#####################################################################################
# Array modification
#####################################################################################
def put(sdata, indices, values, mode='raise') :
    sdata.put(indices, values, mode)
    
#####################################################################################
# Array output
#####################################################################################
def tolist(sdata):
    return sdata.tolist()
            
#####################################################################################
# Array math
#####################################################################################
def exp(obj):
    return obj.__exp__()
    
def log10(obj):
    return obj.__log10__()
    
def ln(obj):
    return obj.__ln__()

def sqrt(obj):
    return obj.__sqrt__()
    
def sin(obj):
    return obj.__sin__()

def cos(obj):
    return obj.__cos__()

def tan(obj):
    return obj.__tan__()

def arcsin(obj):
    return obj.__arcsin__()

def arccos(obj):
    return obj.__arccos__()

def arctan(obj):
    return obj.__arctan__()

def max(obj, axis = None, out = None):
        return obj.max(axis, out)
    
def min(obj, axis = None, out = None):
        return obj.min(axis, out)

def cov(m, y = None, bias = 0, ddof = None):
    if not hasattr(m, 'ndim') :
        raise Exception, 'm must be a 1-D or 2-D dataset'
    ndim = m.ndim
    if ndim > 2:
        raise Exception, 'm must be a 1-D or 2-D dataset, but got ' + str(ndim) + ' dimensions'
    if m.size < 1:
        raise Exception, 'm must have 2 or more values'
    if y is None:
        if ndim == 1:
            return __self_cov__(m)
        else:
            mean = m.mean(0)
            sh0 = m.shape[0]
            sh1 = m.shape[1]
            sh1_1 = sh1 - 1
            cache = []
            res = instance([sh0, sh0], dtype = float)
            for i in xrange(sh0):
                cache.append(m[i] - mean[i])
            for i in xrange(sh0):
                row1 = cache[i]
                for j in xrange(sh0):
                    if i == j:
                        res[i, j] = __self_cov__(row1)
                    elif i < j:
                        row2 = cache[j]
                        s = row1 * row2
                        res[i, j] = s.sum() / sh1_1
                    else:
                        res[i, j] = res[j, i]
            return res
    else :
        if not hasattr(y, 'ndim') :
            raise Exception, 'y must be a 1-D or 2-D dataset'
        ndim = y.ndim
        if ndim > 2:
            raise Exception, 'y must be a 1-D or 2-D dataset, but got ' + str(ndim) + ' dimensions'
        if y.size < 1:
            raise Exception, 'y must have 2 or more values'
        cache = []
        if m.ndim == 1:
            cache.append(m - m.mean())
            den = m.size - 1
        else:
            for item in m:
                cache.append(item - item.mean())
            den = m.shape[m.ndim - 1] - 1
        if y.ndim == 1:
            cache.append(y - y.mean())
        else:
            for item in y:
                cache.append(item - item.mean())
        clen = len(cache)
        res = instance([clen, clen], dtype = float)
        for i in xrange(clen):
            row1 = cache[i]
            for j in xrange(clen):
                if i == j:
                    res[i, j] = __self_cov__(row1)
                elif i < j:
                    row2 = cache[j]
                    s = row1 * row2
                    res[i, j] = s.sum() / den
                else:
                    res[i, j] = res[j, i]
        return res
        
            
def __self_cov__(m, mean = None):
    if mean is None:
        mean = m.mean()
    res = 0
    for val in m:
        res += (val - mean) ** 2
    return res / (m.size - 1)
    
class DatasetFactory:
    
    __path__ = ''
    __prefix__ = ''
    __normalising_factor__ = None
    __path_sep__ = '/'
    __cache_enabled__ = True
    
    def __init__(self, path = None, prefix = None, factor = None):
        if not path is None :
            self.path = path
        else :
            self.path = DatasetFactory.__path__
        if not prefix is None :
            self.prefix = prefix
        else :
            self.prefix = DatasetFactory.__prefix__
        if not factor is None :
            self.normalising_factor = factor
        else :
            self.normalising_factor = DatasetFactory.__normalising_factor__
        self.datasets = {}
    
    def __getitem__(self, index):
        if type(index) is slice :
            datasets = []
            step = 1
            if not index.step is None :
                step = index.step
            for item in range(index.start, index.stop, step) :
                datasets.append(self[item])
            return datasets
        if not self.datasets.has_key(index):
            filepath = self.path
            if type(index) is int :
                if filepath is None or len(filepath) == 0 :
                    sname = '%(index)07d.nx.hdf' % {'index' : index}
                    for key in self.datasets.keys() :
                        if key.__contains__(sname) :
                            return self.datasets[key]
                else:
                    filepath += DatasetFactory.__path_sep__ + DatasetFactory.__prefix__ \
                        + '%(index)07d.nx.hdf' % {'index' : index}
            elif type(index) is str :
                filepath += DatasetFactory.__path_sep__ + index
                if not index.lower().endswith('.hdf') :
                    filepath += '.hdf'
                if not os.path.exists(filepath) :
                    filepath = index
                    if not index.lower().endswith('.hdf') :
                        filepath += '.hdf'
            ds = Dataset(NexusUtils.readNexusDataset(filepath))
            if not self.normalising_factor is None :
                if hasattr(ds, self.normalising_factor) :
                    ds.normalise(self.normalising_factor)
            if DatasetFactory.__cache_enabled__ :
                self.datasets[index] = ds
            return ds
        return self.datasets[index]
    
    def load_dataset(self, file_path, skip_flaws = False):
        filepath = self.path
        if type(file_path) is int :
            filepath += DatasetFactory.__path_sep__ + DatasetFactory.__prefix__ \
                + '%(index)07d.nx.hdf' % {'index' : file_path}
        elif type(file_path) is str :
            filepath += DatasetFactory.__path_sep__ + file_path
            if not file_path.lower().endswith('.hdf') :
                filepath += '.hdf'
            if not os.path.exists(filepath) :
                filepath = file_path
                if not file_path.lower().endswith('.hdf') :
                    filepath += '.hdf'
        ds = Dataset(NexusUtils.readNexusDataset(filepath), skip_flaws = skip_flaws)
        if not self.normalising_factor is None :
            if hasattr(ds, self.normalising_factor) :
                ds.normalise(self.normalising_factor)
        return ds
    
    def __setitem__(self, index, dataset):
        self.datasets[index] = dataset

        
#df = DatasetFactory()

#####################################################################################
# Numpy standards
#####################################################################################
empty = instance