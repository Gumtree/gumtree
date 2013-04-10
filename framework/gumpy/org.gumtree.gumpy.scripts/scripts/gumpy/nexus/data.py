'''
Created on 01/02/2011

@author: nxi
'''

from org.gumtree.data.nexus.netcdf import NXdata
from org.gumtree.data.nexus.utils import NexusFactory
from org.gumtree.data.math import EMath
from org.gumtree.data.nexus import IAxis
from java.util import ArrayList
from simpledata import SimpleData
import array
import simpledata
from array import Array
from gumpy.commons import jutils
import copy
#from nexus import *
DEFAUT_NXDATA_NAME = "data"
DEFAUT_NXSIGNAL_NAME = "signal"
DEFAUT_NXVARIANCE_NAME = "variance"

nx_factory = NexusFactory()

class Data(SimpleData):

    def __init__(self, storage, shape = None, dtype = None, \
                 var = None, axes = None, anames = None, aunits = None, \
                 default_var = True, default_axes = True, title = None, skip_flaws = False):
        '''
        Constructor
        '''
        if type(storage) is int or type(storage) is float or type(storage) is long or type(storage) is bool :
            storage = [storage]
        arr = None
        if isinstance(storage, Array) :
            arr = storage
        elif isinstance(storage, SimpleData) :
            arr = storage.storage
        elif hasattr(storage, '__len__') :
            arr = Array(storage, shape, dtype)
        if not arr is None :
            name = DEFAUT_NXSIGNAL_NAME
            iSignal = nx_factory.createNXsignal(None, name, arr.__iArray__)
            iNXdata = nx_factory.createNXdata(None, DEFAUT_NXDATA_NAME, \
                                              iSignal, None)
            SimpleData.__init__(self, iNXdata.getSignal(), title = title, skip_flaws = skip_flaws, signal = 1)
            self.__iNXdata__ = iNXdata
            self.var = None
            self.axes = __Axes__(self)
            if var is None :
                if hasattr(storage, 'var') and not storage.var is None :
                    var = storage.var.__copy__()
                elif self.storage.dtype is int or self.storage.dtype is float or self.storage.dtype is long :
                    var = arr.positive_float_copy()
            self.set_var(var)
            if (axes is None or len(axes) == 0) and default_axes :
                if hasattr(storage, 'axes') and not storage.axes is None :
                    axes = storage.axes
                else :
                    axes = __Axes__(self)
                    for val in arr.shape :
                        axes.__append__(array.arange(val, float))
            self.set_axes(axes, anames, aunits)
#            if not var is None :
#                if isinstance(var, Array) :
#                    arr = var
#                elif isinstance(var, SimpleData) :
#                    arr = var.storage
#                else :
#                    arr = Array(var, shape, dtype)
#                iNXvariance = nx_factory.createNXvariance(None, var.__iArray__)
#                iNXdata.setVariance(iNXvariance)
#                self.var = SimpleData(iNXvariance)
#            else :
#                self.var = None
#            self.axes = []
#            if not axes is None :
#                iaxes = []
#                for i in xrange(len(axes)) :
#                    axis = axes[i]
#                    aname = None
#                    
#                    if not axesname is None and len(axesname) > i :
#                        aname = axesname[i]
#                    iNXaxis = nx_factory.createNXaxis(None, aname, \
#                                                            axis.__iArray__)
#                    iaxes += [iNXaxis]
#                line = 'iNXdata.setAxes('
#                for i in xrange(len(iaxes)) :
#                    line += 'iaxes[' + i + ']'
#                    if i < len(iaxes) - 1 :
#                        line += ', '
#                line += ')'
#                print line
#                eval(line)
        else :
            iNXdata = storage
            SimpleData.__init__(self, iNXdata.getSignal(), title = title, skip_flaws = skip_flaws, signal = 1)
            self.__iNXdata__ = iNXdata
            iNXvariance = iNXdata.getVariance()
            if not iNXvariance is None :
                self.var = SimpleData(iNXvariance)
            else :
                self.set_var(self.storage.positive_float_copy())
            axes = iNXdata.findAxes()
            self.axes = __Axes__(self)
            if not axes is None and axes.size() != 0 :
                size = axes.size()
                for count in range(size):
                    self.axes.__append__(SimpleData(axes.get(count)))
#        if not title is None :
#            self.__iNXdata__.setTitle(str(title))

    def set_var(self, var):
        if not var is None :
            if isinstance(var, Array) :
                arr = var
            elif isinstance(var, SimpleData) :
                arr = var.storage
            else :
                arr = Array(var, dtype = float)
                if arr.dtype != float:
                    arr = arr.float_copy()
            iNXvariance = nx_factory.createNXvariance(self.__iNXdata__, arr.__iArray__)
            self.__iNXdata__.setVariance(iNXvariance)
            self.__dict__['var'] = SimpleData(iNXvariance)
        else :
            self.__dict__['var'] = None
    
    def set_err(self, err):
        if not err is None :
            if isinstance(err, Array) :
                arr = err ** 2
            elif isinstance(err, SimpleData) :
                arr = err.storage ** 2
            else :
                arr = Array(err, dtype = float) ** 2
            iNXvariance = nx_factory.createNXvariance(self.__iNXdata__, arr.__iArray__)
            self.__iNXdata__.setVariance(iNXvariance)
            self.var = SimpleData(iNXvariance)
        else :
            self.var = None
        
    def set_axes(self, axes, anames = None, aunits = None):
        self.__dict__['axes'] = __Axes__(self)
        if not axes is None :
            iaxes = []
            for i in xrange(len(axes)) :
                axis = axes[i]
                aname = None
                units = None
                title = None
                if isinstance(axis, Array) :
                    arr = axis
                elif isinstance(axis, SimpleData) :
                    arr = axis.storage
                    aname = axis.name
                    units = axis.units
                    title = axis.title
                else :
                    arr = Array(axis)
                if not anames is None and len(anames) > i :
                    aname = anames[i]
                if aname is None :
                    aname = 'dim_' + str(i)
                if not aunits is None and len(aunits) > i :
                    units = aunits[i]
                iNXaxis = nx_factory.createNXaxis(self.__iNXdata__, aname, \
                                                        arr.__iArray__)
                if not units is None :
                    iNXaxis.setUnits(str(units))
                if not title is None :
                    iNXaxis.setTitle(str(title))
                iaxes += [iNXaxis]
                self.__dict__['axes'].__append__(SimpleData(iNXaxis))
            iNXdata = self.__iNXdata__
#            line = 'iNXdata.setMultipleAxes('
#            for i in xrange(len(iaxes)) :
#                line += 'iaxes[' + str(i) + ']'
#                if i < len(iaxes) - 1 :
#                    line += ', '
#            line += ')'
#            print line
#            eval(line)
            axisList = ArrayList()
            for i in xrange(len(iaxes)) :
                axisList.add(iaxes[i])
            iNXdata.setAxes(axisList)

    def __getattr__(self, name):
        if name == 'err' or name == 'error' :
            err = self.var ** 0.5
            err.__set_name__('error')
            return err
        elif name == 'dict' :
            return self.__iNXdata__.findDictionary()
#        elif name == 'title' :
#            title = self.__iNXdata__.getTitle()
#            if title is None :
#                return self.name
#            else :
#                return title
#
#            return self.__iNXdata__.getTitle()
        else :
            return SimpleData.__getattr__(self, name)
        
#    def __setattr__(self, name, value):
#        if name == 'name' :
#            raise AttributeError, 'name can not be set, try set the title instead'
#        elif name == 'title' :
#            self.__iNXdata__.setTitle(str(value))
#        else :
#            self.__dict__[name] = value
#        if isinstance(storage, Array) :
#            if name is None :
#                name = DEFAUT_NXSIGNAL_NAME
#            iSignal = nx_factory.createNXsignal(None, name, storage.__iArray__)
#            iNXdata = nx_factory.createNXdata(None, DEFAUT_NXDATA_NAME, \
#                                              iSignal, None)
#            SimpleData.__init__(self, iSignal)
#            self.__iNXdata__ = iNXdata
#            self.axes = []
#            self.var = var
#        elif isinstance(storage, SimpleData) :
#            storage = storage.storage
#            if name is None :
#                name = DEFAUT_NXSIGNAL_NAME
#            iSignal = nx_factory.createNXsignal(None, name, storage.__iArray__)
#            iNXdata = nx_factory.createNXdata(None, DEFAUT_NXDATA_NAME, \
#                                              iSignal, None)
#            SimpleData.__init__(self, iSignal)
#            self.__iNXdata__ = iNXdata
#            self.axes = []
#            self.var = var
#        elif hasattr(storage, '__len__') :
#            arr = Array(storage, shape, dtype)
#            if name is None :
#                name = DEFAUT_NXSIGNAL_NAME
#            iSignal = nx_factory.createNXsignal(None, name, arr.__iArray__)
#            iNXdata = nx_factory.createNXdata(None, DEFAUT_NXDATA_NAME, \
#                                              iSignal, None)
#            SimpleData.__init__(self, iSignal)
#            self.__iNXdata__ = iNXdata
#            self.axes = []
#            self.var = var
#        else :
#            SimpleData.__init__(self, storage.getSignal())
#            self.__iNXdata__ = storage
#            self.var = var
#            axes = storage.findAxes()
#            self.axes = []
#            if not axes is None and axes.size() != 0 :
#                size = axes.size()
#                for count in range(size):
#                    self.axes.append(SimpleData(axes.get(count)))
            
#    def __add__(self, object):
#        iNXdata = NXdata(self.__iNXdata__)
#        array1 = self.__iDataItem__.getData()
#        array2 = object.__iDataItem__.getData()
#        iNXdata.getSignal().setCachedData(array1.getArrayMath().
#                toAdd(array2).getArray(), 0)
#        return Data(iNXdata)
#        
#    def __getitem__(self, index):
#        return self
#    
#    def __deepcopy__(self, value):
#        return self
    
    def __new__(self, storage, var = None, axes = None, \
                anames = None, aunits = None, parent = None):
        return new(storage, var, axes, anames, aunits, parent, \
                   default_var = False, default_axes = False, title = self.title)
    
    def __repr__(self, indent = None):
        if indent is None :
            indent = ' ' * 5
        else :
            indent += ' ' * 5
        res = 'Data(' + self.storage.__repr__(indent) + ', \n' \
                + indent + 'title=\'' + self.title + '\''
        if not self.var is None :
            res += ',\n' + indent + 'var=' + self.var.storage.__repr__(indent + ' ' * 4)
        if len(self.axes) > 0 :
            res += ',\n' + indent + 'axes=['
            for i in xrange(len(self.axes)) :
                res += self.axes[i].__repr__(indent + ' ' * 6)
                if i < len(self.axes) - 1 :
                    res += ',\n' + indent + ' ' * 6
            res += ']'
        res += ')'
        return res
    
    def __str__(self, indent = ''):
        res = 'title: ' + self.title + '\n' + indent
        if not self.units is None and len(self.units) > 0:
            res += 'units: ' + self.units + '\n' + indent
        res += 'storage: ' + self.storage.__str__(indent + ' ' * 9)
        if not self.var is None :
            res += '\n' + indent + 'error: ' + \
                    (self.var ** 0.5).storage.__str__(indent + ' ' * 7)
        if len(self.axes) > 0 :
            res += '\n' + indent + 'axes:\n' + indent + ' ' * 2
            for i in xrange(len(self.axes)) :
                res += str(i) + '. ' + self.axes[i].__str__(indent + ' ' * 5)
                if i < len(self.axes) - 1 :
                    res += '\n' + indent + ' ' * 2
        return res

    def __getitem__(self, index):
        nst = self.storage[index]
        if not self.var is None :
            var = self.var[index]
        else :
            var = None
        if isinstance(nst, Array) :
            res = self.__new__(nst)
            res.set_var(var)
            if not self.axes is None and not hasattr(index, 'ndim') :
                axes = self.axes
                storage = self.storage
                naxes = []
                nidx = self.__get_slice_index__(index)
                if not len(nidx) == 0 :
                    naxes = []
                    for i in xrange(len(axes)) :
                        axis = axes[i]
                        idx = nidx[len(nidx) - len(axes) + i]
                        if not idx is None :
                            if idx.start is None and idx.stop is None and idx.step is None :
                                naxes += [axis.__copy__()]
                            else :
                                naxes += [axis.__getitem__(idx)]
                res.set_axes(naxes)
            return res
        else :
            return nst
                    
    def __get_slice_index__(self, index): 
        if type(index) is int :
            if self.ndim > 1 :
                nidx = [None]
                for i in xrange(self.ndim - 1) :
                    nidx += [slice(None)]
                return nidx
            else :
                return [None] * self.ndim
        elif type(index) is slice :
            nidx = [index]
            for i in xrange(self.ndim - 1) :
                nidx += [slice(None)]
            return nidx
        elif type(index) is tuple :
            ndim = 0
            nidx = []
            for i in xrange(len(index)) :
                item = index[i]
                if type(item) is slice :
                    if ndim == 0 :
                        ndim = self.ndim - i
                    nidx += [item]
                elif type(item) is int :
                    if ndim == 0 :
                        nidx += [None]
                    else :
                        nidx += [slice(item, item + 1)]
                else :
                    raise TypeError, 'must be either a slice or a int number'
            for i in xrange(self.ndim - len(index)) :
                nidx += [slice(None)]
            return nidx
        else :
            raise TypeError, 'unsupported argument, must be int, slice or tuple'
        
    def get_slice(self, dim, index):
        res = self.__new__(self.storage.get_slice(dim, index))
        if not self.var is None :
            var = self.var.get_slice(dim, index)
            res.set_var(var)
        if len(self.axes) > 0 :
            naxes = []
            for i in xrange(self.ndim) :
                if i != dim :
                    if i >= self.ndim - len(self.axes) :
                        naxes += [self.axes[i - self.ndim + len(self.axes)].__copy__()]
            res.set_axes(naxes)
        return res
        
    def get_section(self, origin, shape, stride = None): 
        res = self.__new__(self.storage.get_section(origin, shape, stride))
        if not self.var is None :
            var = self.var.get_section(origin, shape, stride)
            res.set_var(var)
        laxes = len(self.axes)
        if laxes > 0 :
            naxes = []
            ismdim = False
            for i in xrange(self.ndim) :
                if i >= self.ndim - laxes : 
                    if shape[i] == 1 :
                        if not ismdim :
                            naxes += [self.axes[i - self.ndim + laxes][origin[i]]]
                    elif shape[i] == self.shape[i] and origin[i] == 0 and \
                            (stride is None or stride[i] is None or stride[i] == 1) :
                        ismdim = True
                        naxes += [self.axes[i - self.ndim + laxes].__copy__()]
                    elif stride is None or stride[i] is None or stride[i] == 1 :
                        ismdim = True
                        s = slice(origin[i], origin[i] + shape[i])
                        naxes += [self.axes[i - self.ndim + laxes][s]]
                    else :
                        ismdim = True
                        s = slice(origin[i], origin[i] + shape[i] * stride[i], stride[i])
                        naxes += [self.axes[i - self.ndim + laxes][s]]
            res.set_axes(naxes)
        return res
    
    def get_reduced(self, dim = None):
        res = self.__new__(self.storage.get_reduced(dim))
        if not self.var is None :
            res.set_var(self.var.get_reduced(dim))
        if len(self.axes) > 0 :
            naxes = []
            ndim = 0
            for i in xrange(self.ndim) :
                if dim is None :
                    if self.shape[i] > 1 :
                        naxes += [self.axes[len(self.axes) - self.ndim + i].__copy__()]
                else :
                    if i != dim :
                        if len(self.axes) >= self.ndim - i :
                            naxes += [self.axes[len(self.axes) - self.ndim + i].__copy__()]
            res.set_axes(naxes)
        return res
        
#    def section_iter(self, shape):
#        self.storage.section_iter(shape)
#        if not self.var is None :
#            self.var.section_iter(shape)
    
#    def next_section(self) :
#        origin = self.cur_section_org
#        shape = [1] * self.ndim
#        lssh = len(self.section_iter_shape)
#        for i in xrange(lssh) :
#            shape[self.ndim - 1 - i] = self.section_iter_shape[lssh - 1 - i]
#        var = None
#        naxes = None
#        if not self.var is None :
#            var = self.var.next_section()
#        laxes = len(self.axes)
#        if laxes > 0 :
#            naxes = []
#            for i in xrange(self.ndim) :
#                if i >= self.ndim - laxes and i >= self.ndim - lssh : 
#                    if shape[i] == 1 :
#                        naxes += [self.axes[i - self.ndim + laxes][origin[i]]]
#                    elif shape[i] == self.shape[i] and origin[i] == 0 :
#                        naxes += [self.axes[i - self.ndim + laxes].__copy__()]
#                    else :
#                        s = slice(origin[i], origin[i] + shape[i])
#                        naxes += [self.axes[i - self.ndim + laxes][s]]
#        res = self.__new__(self.storage.next_section())
#        if not var is None :
#            res.set_var(var)
#        if not naxes is None :
#            res.set_axes(naxes)
#        return res
        
    def take(self, indices, axis=None, mode='raise'):
        res = SimpleData.take(self, indices, axis, None, mode)
        if not self.var is None :
            var = self.var.take(indices, axis, None, mode)
            res.set_var(var)
        if not axis is None :
            naxes = []
            laxes = len(self.axes)
            for i in xrange(self.ndim) :
                if i >= self.ndim - laxes :
                    if i == axis :
                        naxes += [self.axes[i - self.ndim + laxes].take(indices, 0)]
                    else :
                        naxes += [self.axes[i - self.ndim + laxes].__copy__()]
            res.set_axes(naxes)
        return res
    
    def __get_sub_axes__(self, slices):
        laxes = len(self.axes)
        naxes = []
        if laxes > 0 :
            ismdim = False
            for i in xrange(self.ndim) :
                if i >= self.ndim - laxes : 
                    sl = slices[i]
                    if not sl is None :
                        if sl.start is None and sl.stop is None and sl.step is None :
                            naxes += [self.axes[i - self.ndim + laxes].__copy__()]
                        else :
                            naxes += [self.axes[i - self.ndim + laxes][i]]
        return naxes
#####################################################################################
# Array accessing
#####################################################################################    
    def get_value(self, index) :
        return self.storage.get_value(index)
    
#    def __iter__(self):
#        self.storage.__iter__()
#        if not self.var is None :
#            self.var.__iter__()
#        return self        
        
#    def item_iter(self):
#        self.storage.item_iter()
#        if not self.var is None :
#            self.var.item_iter()
#        return self
    
#    def __next__(self):
#        self.storage.__next__()
#        if not self.var is None :
#            self.var.__next__()
    
#    def next_slice(self):
#        if self.storage.cur_slice >= self.__len__() :
##            self.storage.cur_slice = 0
#            raise StopIteration
#        else :
#            res = self.get_slice(0, self.storage.cur_slice)
#        self.storage.cur_slice += 1
#        return res
#    
#    def current_slice(self):
#        res = self.get_slice(0, self.storage.cur_slice)
#        return res    
#
#    def next_var(self):
#        if self.var is None : 
#            return None
#        return self.var.next_value()
#
#    def current_var(self):
#        if self.var is None : 
#            return None
#        return self.var.current_value()
        
    def set_value(self, index, value, variance = None):
        self.storage.set_value(index, value)
        if not variance is None :
            if not self.var is None :
                self.var.set_value(index, variance)
        
#    def set_next_value(self, value, variance = None):
#        self.storage.set_next_value(value)
#        if not variance is None :
#            self.var.set_next_value(variance)
#
#    def set_current_value(self, value, variance = None):
#        self.storage.set_current_value(value)
#        if not variance is None :
#            self.var.set_current_value(variance)
        
#********************************************************************************
#     Array math
#********************************************************************************
    
    def __add__(self, obj):
        ndtype = self.storage.__match_type__(obj)
        if self.dtype is int and ndtype is float :
            sarr = self.storage.float_copy().__iArray__
        else :
            sarr = self.storage.__iArray__
        if not self.var is None :
            svar = self.var.__iArray__
        else :
            svar = __make_default_var__(self.storage).__iArray__
        if isinstance(obj, Data) :
            oarr = obj.storage.__iArray__
            if not obj.var is None :
                ovar = obj.var.__iArray__
            else :
                ovar = __make_default_var__(obj).__iArray__
        elif isinstance(obj, SimpleData) :
            oarr = obj.storage.__iArray__
            ovar = None
        elif isinstance(obj, Array) :
            oarr = obj.__iArray__
            ovar = None
        elif hasattr(obj, '__len__') :
            arr = Array(obj)
            oarr = arr.__iArray__
            ovar = None
        else :
            oarr = float(obj)
            ovar = 0.
        edata = EMath.add(sarr, oarr, svar, ovar)
        return self.__new__(Array(edata.getData()), var = Array(edata.getVariance()), \
                       axes = self.axes)
            
    def __iadd__(self, obj):
        SimpleData.__iadd__(self, obj)
        if hasattr(obj, 'var') :
            if not self.var is None and not obj.var is None:
                self.var.__iadd__(obj.var)
        return self
    
    def __radd__(self, obj):
        return self.__add__(obj)
            
    def __div__(self, obj):
        isValue = False
        ndtype = self.storage.__match_type__(obj)
        if self.dtype is int and ndtype is float :
            sarr = self.storage.float_copy().__iArray__
        else :
            sarr = self.storage.__iArray__
        if not self.var is None :
            svar = self.var.__iArray__
        else :
            svar = __make_default_var__(self.storage).__iArray__
        if isinstance(obj, Data) :
            oarr = obj.storage.__iArray__
            if not obj.var is None :
                ovar = obj.var.__iArray__
            else :
                ovar = __make_default_var__(obj).__iArray__
        elif isinstance(obj, SimpleData) :
            oarr = obj.storage.__iArray__
            ovar = None
        elif isinstance(obj, Array) :
            oarr = obj.__iArray__
            ovar = None
        elif hasattr(obj, '__len__') :
            arr = Array(obj)
            oarr = arr.__iArray__
            ovar = None
        else :
            oarr = 1. / obj
            ovar = 0.
            isValue = True
        if isValue :
            edata= EMath.toScale(sarr, oarr, svar, ovar)
        else :
            edata = EMath.toEltDivideSkipZero(sarr, oarr, svar, ovar)
        return self.__new__(Array(edata.getData()), var = Array(edata.getVariance()), \
                       axes = self.axes)
    
    def __idiv__(self, obj):
        if isinstance(obj, Array) or isinstance(obj, SimpleData) :
            obj = obj.inverse()
        elif hasattr(obj, '__len__') :
            obj = Array(obj).inverse()
        else :
            obj = 1 / float(obj)
        return self.__imul__(obj)
    
    def __rdiv__(self, obj):
        return self.inverse() * obj
    
    def __mul__(self, obj):
        isValue = False
        ndtype = self.storage.__match_type__(obj)
        if self.dtype is int and ndtype is float :
            sarr = self.storage.float_copy().__iArray__
        else :
            sarr = self.storage.__iArray__
        if not self.var is None :
            svar = self.var.__iArray__
        else :
            svar = __make_default_var__(self.storage).__iArray__
        if isinstance(obj, Data) :
            oarr = obj.storage.__iArray__
            if not obj.var is None :
                ovar = obj.var.__iArray__
            else :
                ovar = __make_default_var__(obj).__iArray__
        elif isinstance(obj, SimpleData) :
            oarr = obj.storage.__iArray__
            ovar = None
        elif isinstance(obj, Array) :
            oarr = obj.__iArray__
            ovar = None
        elif hasattr(obj, '__len__') :
            arr = Array(obj)
            oarr = arr.__iArray__
            ovar = None
        else :
            oarr = float(obj)
            ovar = 0.
            isValue = True
        if isValue :
            edata= EMath.toScale(sarr, oarr, svar, ovar)
        else :
            edata = EMath.toEltMultiply(sarr, oarr, svar, ovar)
        return self.__new__(Array(edata.getData()), var = Array(edata.getVariance()), \
                       axes = self.axes)
    
    def __imul__(self, obj):
        isValue = False
        sarr = self.storage.__iArray__
        if not self.var is None :
            svar = self.var.__iArray__
        else :
            svar = __make_default_var__(self.storage).__iArray__
        if isinstance(obj, Data) :
            oarr = obj.storage.__iArray__
            if not obj.var is None :
                ovar = obj.var.__iArray__
            else :
                ovar = __make_default_var__(obj).__iArray__
        elif isinstance(obj, SimpleData) :
            oarr = obj.storage.__iArray__
            ovar = None
        elif isinstance(obj, Array) :
            oarr = obj.__iArray__
            ovar = None
        elif hasattr(obj, '__len__') :
            arr = Array(obj)
            oarr = arr.__iArray__
            ovar = None
        else :
            oarr = float(obj)
            ovar = 0.
            isValue = True
        if isValue :
            edata= EMath.scale(sarr, oarr, svar, ovar)
        else :
            edata = EMath.eltMultiply(sarr, oarr, svar, ovar)
        self.set_var(edata.getVariance())
        return self
    
    def __rmul__(self, obj):
        return self.__mul__(obj)
        
    def __neg__(self):
        return self * -1
    
    def __sub__(self, obj):
        if isinstance(obj, Array) or isinstance(obj, SimpleData) :
            obj = obj * -1
        elif hasattr(obj, '__len__') :
            obj = Array(obj) * -1
        else :
            obj = -obj
        return self.__add__(obj)
    
    def __isub__(self, obj):
        if isinstance(obj, Array) or isinstance(obj, SimpleData) :
            obj = obj * -1
        elif hasattr(obj, '__len__') :
            obj = Array(obj) * -1
        else :
            obj = -obj
        return self.__iadd__(obj)
    
    def __rsub__(self, obj):
        return self * -1 + obj
        
    def inverse(self):
        if not self.var is None :
            var = self.var.__iArray__
        else :
            var = __make_default_var__(self.storage).__iArray__
        edata = EMath.toEltInverseSkipZero(self.storage.__iArray__, var)
        return self.__new__(Array(edata.getData()), var = Array(edata.getVariance()), \
                       axes = self.axes)

#    def __invert__(self):
#        if not self.var is None :
#            var = self.var.__iArray__
#        else :
#            var = __make_default_var__(self.storage).__iArray__
#        edata = EMath.toEltInverseSkipZero(self.storage.__iArray__, var)
#        return self.__new__(Array(edata.getData()), var = Array(edata.getVariance()), \
#                       axes = self.axes)
    
    def __pow__(self, obj):
        otype = type(obj)
        if otype is int or otype is float or otype is long :
            if not self.var is None :
                var = self.var.__iArray__
            else :
                var = __make_default_var__(self.storage).__iArray__
            edata = EMath.toPower(self.storage.__iArray__, float(obj), var)
            return self.__new__(Array(edata.getData()), var = Array(edata.getVariance()), \
                           axes = self.axes)
        else :
            raise TypeError, 'type not supported'
    
    def __exp__(self):
        if not self.var is None :
            var = self.var.__iArray__
        else :
            var = __make_default_var__(self.storage).__iArray__
        edata = EMath.toExp(self.storage.__iArray__, var)
        return self.__new__(Array(edata.getData()), var = Array(edata.getVariance()), \
                       axes = self.axes)

    def __log10__(self):
        if not self.var is None :
            var = self.var.__iArray__
        else :
            var = __make_default_var__(self.storage).__iArray__
        edata = EMath.toLog10(self.storage.__iArray__, var)
        return self.__new__(Array(edata.getData()), var = Array(edata.getVariance()), \
                       axes = self.axes)        
    
    def __ln__(self):
        if not self.var is None :
            var = self.var.__iArray__
        else :
            var = __make_default_var__(self.storage).__iArray__
        edata = EMath.toLn(self.storage.__iArray__, var)
        return self.__new__(Array(edata.getData()), var = Array(edata.getVariance()), \
                       axes = self.axes)

    def __sqrt__(self):
        if not self.var is None :
            var = self.var.__iArray__
        else :
            var = __make_default_var__(self.storage).__iArray__
        edata = EMath.toSqrt(self.storage.__iArray__, var)
        return self.__new__(Array(edata.getData()), var = Array(edata.getVariance()), \
                       axes = self.axes)

    def __rpow__(self, obj):
        return self.__new__(self.storage.__rpow__(obj), var = self.var, \
                           axes = self.axes)
    
    def __mod__(self, obj):
        if isinstance(obj, SimpleData) :
            obj = obj.storage
        return self.__new__(self.storage % obj)

    def __rmod__(self, obj):
        return self.__new__(self.storage.__rmod__(obj), var = self.var, \
                           axes = self.axes)

    def __sin__(self):
        if not self.var is None :
            var = self.var.__iArray__
        else :
            var = __make_default_var__(self.storage).__iArray__
        edata = EMath.toSin(self.storage.__iArray__, var)
        return self.__new__(Array(edata.getData()), var = Array(edata.getVariance()), \
                       axes = self.axes)
    
    def __cos__(self):
        if not self.var is None :
            var = self.var.__iArray__
        else :
            var = __make_default_var__(self.storage).__iArray__
        edata = EMath.toCos(self.storage.__iArray__, var)
        return self.__new__(Array(edata.getData()), var = Array(edata.getVariance()), \
                       axes = self.axes)
    
    def __tan__(self):
        if not self.var is None :
            var = self.var.__iArray__
        else :
            var = __make_default_var__(self.storage).__iArray__
        edata = EMath.toTan(self.storage.__iArray__, var)
        return self.__new__(Array(edata.getData()), var = Array(edata.getVariance()), \
                       axes = self.axes)
    
    def __arcsin__(self):
        if not self.var is None :
            var = self.var.__iArray__
        else :
            var = __make_default_var__(self.storage).__iArray__
        edata = EMath.toAsin(self.storage.__iArray__, var)
        return self.__new__(Array(edata.getData()), var = Array(edata.getVariance()), \
                       axes = self.axes)
    
    def __arccos__(self):
        if not self.var is None :
            var = self.var.__iArray__
        else :
            var = __make_default_var__(self.storage).__iArray__
        edata = EMath.toAcos(self.storage.__iArray__, var)
        return self.__new__(Array(edata.getData()), var = Array(edata.getVariance()), \
                       axes = self.axes)
    
    def __arctan__(self):
        if not self.var is None :
            var = self.var.__iArray__
        else :
            var = __make_default_var__(self.storage).__iArray__
        edata = EMath.toAtan(self.storage.__iArray__, var)
        return self.__new__(Array(edata.getData()), var = Array(edata.getVariance()), \
                       axes = self.axes)
    
    def __prod__(self, axis = None):
        return self.__new__(self.storage.__prod__(axis))
    
    def max(self, axis = None, out = None):
        if axis is None :
            return self.storage.max()
        else :
            if axis >= self.ndim :
                raise ValueError, 'axis index out of stack, ' + str(axis) + \
                                    ' in ' + str(self.ndim)
            nsize = self.shape[axis]
            if out is None :
                out = self.__new__(instance([nsize], 0, self.dtype, None, True, False))
            for i in xrange(nsize) :
                sli = self.get_slice(axis, i)
                siter = sli.item_iter()
                imax = -jutils.get_inf()
                iloc = -1
                for j in xrange(sli.size) :
                    val = siter.next()
                    if val > imax :
                        imax = val
                        iloc = j
                variance = None
                if not self.var is None :
                    iidx = array.get_index_1d_to_nd(j, sli.shape)
                    variance = self.var.get_slice(axis, i).get_value(iidx)
                out.set_value(i, imax, variance)
                out.set_axes([self.axes[axis]]) 
            return out
    
    def min(self, axis = None, out = None):
        if axis is None :
            return self.storage.min()
        else :
            if axis >= self.ndim :
                raise ValueError, 'axis index out of stack, ' + str(axis) + \
                                    ' in ' + str(self.ndim)
            nsize = self.shape[axis]
            if out is None :
                out = self.__new__(instance([nsize], 0, self.dtype, True, False))
            for i in xrange(nsize) :
                sli = self.get_slice(axis, i)
                siter = sli.item_iter()
                imin = jutils.get_inf()
                iloc = -1
                for j in xrange(sli.size) :
                    val = siter.next()
                    if val < imin :
                        imin = val
                        iloc = j
                variance = None
                if not self.var is None and iloc >= 0 :
                    iidx = array.get_index_1d_to_nd(iloc, sli.shape)
                    variance = self.var.get_slice(axis, i).get_value(iidx)
                out.set_value(i, imin, variance)
                out.set_axes([self.axes[axis]]) 
            return out
    
    def sum(self, axis = None, dtype = None, out = None):
        if axis is None :
            return self.storage.sum(dtype = dtype)
        else :
            if out is None :
                out = self.__new__(self.storage.sum(axis, dtype))
                if not self.var is None :
                    out.set_var(self.var.sum(axis, float))
                axis_id = axis + len(self.axes) - self.ndim
                if axis_id >= 0 :
                    out.set_axes([self.axes[axis_id]])
                return out
            else :
                self.storage.sum(axis, dtype, out)
                if not self.var is None and hasattr(out, 'var') and not out.var is None :
                    self.var.sum(axis, float, out.var)
                return out

    def transpose(self, axes = None):
        nstr = self.storage.transpose(axes)
        nvar = None
        if not self.var is None:
            nvar = self.var.storage.transpose(axes)
        saxes = self.axes
        naxes = None
        if not saxes is None and len(saxes) == self.ndim :
            if axes is None:
                dim1 = self.ndim - 1
                dim2 = self.ndim - 2
            else :
                dim1 = axes[0]
                dim2 = axes[1]
            idx = range(self.ndim)
            idx[dim1] = dim2
            idx[dim2] = dim1
            naxes = []
            for i in idx :
                naxes.append(saxes[i])
        return self.__new__(nstr, var = nvar, axes = naxes)

    def compress(self, condition, axis = None, out = None):
        if axis is None:
            nstr = self.storage.compress(condition, axis, out)
            nvar = None
            if not self.var is None:
                nvar = self.var.storage.compress(condition, axis, out)
            return self.__new__(nstr, var = nvar, axes = None)
        else:
            nstr = self.storage.compress(condition, axis, out)
            nvar = None
            if not self.var is None:
                nvar = self.var.storage.compress(condition, axis, out)
            naxes = None
            saxes = self.axes
            if not saxes is None and len(saxes) == self.ndim :
                if axis < self.ndim:
                    naxes = []
                    for i in xrange(len(saxes)):
                        if i != axis:
                            naxes.append(saxes[i])
                        else:
                            naxes.append(saxes[i].compress(condition))
            return self.__new__(nstr, var = nvar, axes = naxes)

#####################################################################################
#   Array modification
#####################################################################################    

    def __setitem__(self, index, value):
        if isinstance(value, SimpleData) :
            arr = value.storage
        else :
            arr = value
        self.storage[index] = arr
        if hasattr(value, 'var') and not value.var is None :
            if not self.var is None :
                self.var[index] = value.var
        elif not self.var is None :
            self.var[index] = arr
    
    def copy_from(self, value, length = -1):
        if isinstance(value, SimpleData) :
            value = value.storage
        self.storage.copy_from(value, length)
        if hasattr(value, 'var') and not value.var is None :
            if not self.var is None :
                self.var.copy_from(value.var, length)
            else :
                self.set_var(zeros_like(self.storage).copy_from(value.var, \
                            length).positive_float_copy())
        else :
            if not self.var is None :
                self.var.copy_from(self.storage, length)
            else :
                self.set_var(self.storage.positive_float_copy())
                    
    def fill(self, val, variance = None):
        self.storage.fill(val)
        avar = abs(val)
        if not self.var is None :
            if not variance is None :
                self.var.fill(variance)
            else :
                self.var.fill(avar)
        else :
            var = array.zeros(self.storage.shape)
            if not variance is None :
                var.fill(variance)
            else :
                var.fill(avar)
            self.set_var(var)
            
    def put(self, indices, values, vars = None, mode='raise') :
        self.storage.put(indices, values, mode)
        if not self.var is None :
            if not vars is None :
                self.var.put(indices, vars, mode)
            else :
                self.var.put(indices, __make_default_var__(values), mode)
        else :
            var = array.zeros(self.storage.shape)
            if not vars is None :
                self.var.put(indices, vars, mode)
            else :
                self.var.put(indices, __make_default_var__(values), mode)
            self.set_var(var)
        
#####################################################################################
#   Reinterpreting arrays
#####################################################################################    
    def reshape(self, shape): 
        res = self.__new__(self.storage.reshape(shape))
        if not self.var is None :
            res.set_var(self.var.reshape(shape))
        return res
    
    def flatten(self) :
        res = self.__new__(self.storage.flatten())
        if not self.var is None :
            res.set_var(self.var.flatten())
        return res

    def view_1d(self):
        res = self.__new__(self.storage.view_1d())
        if not self.var is None :
            res.set_var(self.var.view_1d())
        return res
    
    def __copy__(self):
        res = self.__new__(self.storage.__copy__())
        if not self.var is None :
            res.set_var(self.var.__copy__())
        if len(self.axes) > 0 :
            naxes = []
            for axis in self.axes :
                naxes += [axis.__copy__()]
            res.set_axes(naxes) 
        return res

    def __deepcopy__(self):
        return self.__copy__()
    
    def float_copy(self):
        res = self.__new__(self.storage.float_copy())
        if not self.var is None :
            res.set_var(self.var.float_copy())
        if len(self.axes) > 0 :
            naxes = []
            for axis in self.axes :
                naxes += [axis.__copy__()]
            res.set_axes(naxes) 
        return res

    def absolute_copy(self):
        res = self.__new__(self.storage.positive_float_copy())
        if not self.var is None :
            res.set_var(self.var.float_copy())
        if len(self.axes) > 0 :
            naxes = []
            for axis in self.axes :
                naxes += [axis.__copy__()]
            res.set_axes(naxes) 
        return res
    
    def __dir__(self):
        dirs = SimpleData.__dir__(self)
        dirs.append('axes')
        dirs.append('err')
        dirs.append('error')
        return sorted(dirs)

    def delete_slice(self, indices):
        oitem = self.__iDataItem__
        var = self.var
        axes = self.axes
        SimpleData.delete_slice(self, indices)
        self.__iNXdata__.removeDataItem(oitem)
        self.__iNXdata__.addDataItem(self.__iDataItem__)
        if not var is None :
            var.delete_slice(indices)
        self.set_var(var)
        if not axes is None and len(axes) == self.ndim :
            axes[0].delete_slice(indices)
        naxes = []
        for axis in axes :
            naxes.append(axis)
        self.set_axes(naxes)
        

class __Axes__():
    
    def __init__(self, par):
        self.par = par
        self.ls = []
        
    def __getitem__(self, index):
        return self.ls[index]
    
    def __setitem__(self, index, value):
        ls = copy.copy(self.ls)
        ls[index] = value
        self.par.set_axes(ls)
        
    def __append__(self, axis):
        self.ls.append(axis)
        
    def __len__(self):
        return len(self.ls)
                
def new(storage, var = None, axes = None, anames = None, \
        aunits = None, parent = None, default_var = True, default_axes = True, title = None) :
    return Data(storage, var = var, axes = axes, \
                       anames = anames, aunits = aunits, default_var = default_var, \
                       default_axes = default_axes, title = title)
#        iGroup = None
#        if not parent is None :
#            iGroup = parent.__NXroot__.getFirstEntry()
#        if name is None :
#            name = DEFAUT_NXSIGNAL_NAME
#        iSignal = nx_factory.createNXsignal(None, name, storage.__iArray__)
#        iNXdata = nx_factory.createNXdata(iGroup, DEFAUT_NXDATA_NAME, \
#                                              iSignal, None)
##        if not var is None :
##            iNXvariance = nx_factory.createNXvariance(None, var.__iArray__)
##            iNXdata.setVariance(iNXvariance)
##        if not axes is None and len(axes) > 0 :
##            iaxes = []
##            for i in xrange(len(axes)) :
##                axis = axes[i]
##                aname = None
##                if not axesname is None and len(axesname) > i :
##                    aname = axesname[i]
##                iNXaxis = nx_factory.createNXaxis(None, aname, \
##                                                        axis.__iArray__)
##                iaxes += [iNXaxis]
##            line = 'iNXdata.setAxes('
##            for i in xrange(len(iaxes)) :
##                line += 'iaxes[' + i + ']'
##                if i < len(iaxes) - 1 :
##                    line += ', '
##            line += ')'
##            print line
##            eval(line)
#        data = Data(iNXdata)
#        if var is None :
#            if hasattr(storage, 'var') and not storage.var is None :
#                var = storage.var.__copy__()
#            else :
#                var = storage.positive_float_copy()
#        data.set_var(var)
#        if (axes is None or len(axes) == 0) and default_axes :
#            if hasattr(storage, 'axes') and not storage.axes is None :
#                axes = storage.axes
#            else :
#                axes = []
#                for val in storage.shape :
#                    axes += [array.arange(val)]
#        data.set_axes(axes, anames, aunits)
#        return data



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
    return new(array.eye(N, M, k, dtype), default_var = default_var, \
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

def asarray(obj, dtype = None, default_var = True, default_axes = True):
    if isinstance(obj, SimpleData) :
        obj = obj.storage
    return new(array.asarray(obj, dtype), default_var = default_var, \
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
        res = new(array.append(obj, val, axis), var = __make_default_var__(var), axes = axes, \
                  default_var = default_var, default_axes = default_axes)
    return res

def concatenate(tup, axis = 0, default_var = True, default_axes = True):
    storage = array.concatenate(tup, axis)
    vars = ()
    for item in tup :
        if hasattr(item, 'var') and not item.var is None :
            vars += (item.var,)
        else :
            vars += (__make_default_var__(item),)
    var = array.concatenate(vars, axis)
    return new(storage, var = var, default_var = default_var, default_axes = default_axes)
            
def column_stack(*tup):
    storage = array.column_stack(*tup)
    vars = ()
    for item in tup :
        if hasattr(item, 'var') and not item.var is None :
            vars += (item.var,)
        else :
            vars += (__make_default_var__(item),)
    var = array.column_stack(*vars)
    return new(storage, var = var)

def vstack(*tup):
    storage = array.vstack(*tup)
    vars = ()
    for item in tup :
        if hasattr(item, 'var') and not item.var is None :
            vars += (item.var,)
        else :
            vars += (__make_default_var__(item),)
    var = array.vstack(*vars)
    return new(storage, var = var)

def hstack(*tup):
    storage = array.hstack(*tup)
    vars = ()
    for item in tup :
        if hasattr(item, 'var') and not item.var is None :
            vars += (item.var,)
        else :
            vars += (__make_default_var__(item),)
    var = array.hstack(*vars)
    return new(storage, var = var)
    
def dstack(*tup):
    storage = array.dstack(*tup)
    vars = ()
    for item in tup :
        if hasattr(item, 'var') and not item.var is None :
            vars += (item.var,)
        else :
            vars += (__make_default_var__(item),)
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
    
#####################################################################################
# Data utilities
#####################################################################################
def __make_default_var__(obj): 
    if isinstance(obj, Array) :
        return obj.positive_float_copy()
    elif isinstance(obj, SimpleData) :
        return obj.storage.positive_float_copy()
    elif hasattr(obj, '__len__') :
        return Array(obj).positive_float_copy()
    else :
        return abs(obj)

s_ = array.s_

#####################################################################################
# Numpy standards
#####################################################################################
empty = instance