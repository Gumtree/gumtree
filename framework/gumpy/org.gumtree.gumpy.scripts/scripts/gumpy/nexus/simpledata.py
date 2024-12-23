'''
Created on 01/02/2011

@author: nxi
'''

import gumpy
import array
import copy
import math
from array import Array, NexusException
#from org.gumtree.data.utils import Utilities
#from org.gumtree.data.utils import FactoryManager
from org.gumtree.data.nexus.utils import NexusFactory
from java.lang import String
from gumpy.commons import jutils
from org.gumtree.data import DataType

#gdm_factory = FactoryManager().getFactory()
nx_factory = NexusFactory()

class SimpleData:
 
    def __init__(self, storage, shape = None, dtype = None, units = None, title = None, skip_flaws = False, signal = None, 
                 lazy_loading = False):
        '''
        Constructor
        '''
        if title is None :
            if hasattr(storage, 'title') :
                title = storage.title
        self.skip_flaws = skip_flaws
        if type(storage) is int or type(storage) is float or type(storage) is long or type(storage) is bool :
            storage = [storage]
        lazy_flag = False
        if isinstance(storage, Array) :
            self.storage = storage
#            self.name = name
            if signal :
                self.__iDataItem__ = nx_factory.\
                            createNXsignal(None, 'none', storage.__iArray__)
            else :
                self.__iDataItem__ = nx_factory.\
                            createNXDataItem(None, 'none', storage.__iArray__)
            name = 'sd' + str(id(self.__iDataItem__))
            self.__iDataItem__.setShortName(name)
            if not units is None :
                self.__iDataItem__.setUnits(str(units))
        elif isinstance(storage, SimpleData) :
            name = storage.name
            self.storage = storage.storage
            if signal :
                self.__iDataItem__ = nx_factory.\
                            createNXsignal(None, 'none', storage.__iArray__)
            else :
                self.__iDataItem__ = nx_factory.\
                            createNXDataItem(None, name, storage.__iArray__)
            if not units is None :
                self.__iDataItem__.setUnits(str(units))
        elif hasattr(storage, '__len__') :
            self.storage = Array(storage, shape, dtype)
            if signal :
                self.__iDataItem__ = nx_factory.\
                            createNXsignal(None, 'none', storage.__iArray__)
            else :
                self.__iDataItem__ = nx_factory.\
                            createNXDataItem(None, 'none', self.storage.__iArray__)
            name = 'sd' + str(id(self.__iDataItem__))
            self.__iDataItem__.setShortName(name)
            if not units is None :
                self.__iDataItem__.setUnits(str(units))
        else :
            self.__iDataItem__ = storage
            if not lazy_loading:
                self.storage = Array(storage.getData(skip_flaws))
            else:
                self.storage = None
                lazy_flag = True
            if not units is None :
                ounits = self.__iDataItem__.getUnits()
                if ounits is None or len(ounits) == 0 :
                    self.__iDataItem__.setUnits(str(units))
        self.lazy_loading = lazy_flag
        if title is None :
            title = self.__iDataItem__.getTitle()
        if title is None :
            title = self.__iDataItem__.getShortName()
        self.__iDataItem__.setTitle(str(title))
#            self.name = iDataItem.getShortName()


#    def __add__(self, obj) :
#        arr1 = self.storage
#        arr2 = obj.storage
#        narr = arr1 + arr2
#        di = NcDataItem(self.__iDataItem__)
#        di.setCachedData(narr.__iArray__, 0)
#        nsd = SimpleData(di)
#        nsd.storage = narr
#        return nsd
    
#    def __mul__(self, object):
#        array1 = self.__iDataItem__.getData()
#        array2 = object.__iDataItem__.getData()
#        return SimpleData(array1.getArrayMath().toEltMultiply(array2).getArray())
    
        
#    def __repr__(self):
#        return self.__class__.__name__
    def __set_name__(self, name):
        self.__iDataItem__.setShortName(str(name))
        
    #####################################################################################
    #   Array indexing
    #####################################################################################
    def __getitem__(self, index):
        if self.lazy_loading:
            if type(index) is int :
                if index < 0 :
                    index = self.shape[0] + index
                if index >= self.shape[0] :
                    raise Exception, 'out of range, ' + str(index) + " in " + \
                        str(self.shape[0])
                if self.ndim == 1 :
                    return self.get_value(index)
                else :
                    return self.get_slice(0, index)
            elif type(index) is slice :
                start = index.start
                stop = index.stop
                step = index.step
                if step is None :
                    step = 1
                if step <= 0 :
                    raise ValueError, 'negative step is not supported: step=' + str(step)
                if start is None :
                    start = 0
                if stop is None :
                    stop = self.shape[0]
                if start < 0 :
                    start += self.shape[0]
                    if start < 0 :
                        start = 0
                if start > self.shape[0] :
                    return instance([0], self.dtype)
                if stop > self.shape[0] :
                    stop = self.shape[0]
                if stop <= 0 :
                    stop += self.shape[0]
                    if stop <= 0 :
                        return instance([0], self.dtype)
                origin = [0] * self.ndim
                origin[0] = start
                shape = copy.copy(self.shape)
                shape[0] = int(math.ceil(float(stop - start) / step))
                if (shape[0] <= 0) :
                    return instance([0], self.ndim)
                if step == 1 :
                    section = self.get_section(origin, shape)
                else :
                    stride = [1] * self.ndim
                    stride[0] = step
    #                if stride[0] > 1 :
    #                    shape[0] = int(math.ceil(float(shape[0]) / stride[0]))
                    section = self.get_section(origin, shape, stride)
                return section
            elif type(index) is list :
    #            nshape = copy.copy(self.shape)
    #            nshape[0] = len(index)
    #            narr = instance(nshape, 0, self.dtype)
    #            for i in xrange(len(index)) :
    #                val = index[i]
    #                if val < 0 :
    #                    val += self.shape[0]
    #                    if val < 0 :
    #                        raise ValueError, 'index out of bound: ' + str(index[i]) + \
    #                                ' in ' + str(self.shape[0])
    #                if val >= self.shape[0] :
    #                    raise ValueError, 'index out of bound: ' + str(index[i]) + \
    #                                ' in ' + str(self.shape[0])
    #                if self.ndim == 1 :
    #                    narr[i] = self[index[i]]
    #                else :
    #                    narr[i].copy_from(self[index[i]])
                raise TypeError, 'irregular slicing is not supported'
            elif type(index) is tuple :
                origin = [0] * self.ndim
                shape = copy.copy(self.shape)
                stride = [1] * self.ndim
                if len(index) <= self.ndim :
                    i = 0
                    secflag = len(index) < self.ndim
                    reducedim = 0
                    for item in index :
                        if type(item) is int :
                            if item < 0 :
                                item = self.shape[i] + item
                            origin[i] = item
                            shape[i] = 1
                            if reducedim == i :
                                reducedim = i + 1
                        elif type(item) is slice :
                            secflag = 1
                            start = item.start
                            stop = item.stop
                            step = item.step
                            if step is None :
                                step = 1
                            if step <= 0 :
                                raise ValueError, 'negative step is not supported: step=' + str(step)
                            if start is None :
                                start = 0
                            if stop is None :
                                stop = self.shape[i]
                            if start < 0 :
                                start += self.shape[i]
                                if start < 0 :
                                    start = 0
                            if start > self.shape[i] :
                                return instance([0], self.dtype)
                            if stop > self.shape[i] :
                                stop = self.shape[i]
                            if stop < 0 :
                                stop += self.shape[i]
                                if stop <= 0 :
                                    return instance([0], self.dtype)
                            origin[i] = start
                            stride[i] = step
                            if step > 1 :
                                shape[i] = int(math.ceil(float(stop - start) / step))
                            else :
                                shape[i] = stop - start
                        elif type(item) is list :
                            raise TypeError, 'irregular slicing is not supported'
                        i += 1
                    if secflag :
                        section =  self.get_section(origin, shape, stride)
                        if reducedim > 0 :
                            return section.get_reduced(range(reducedim))
                        else :
                            return section
                    else :
                        return self.get_value(origin)
                else :
                    raise Exception, 'dim=' + str(len(index)) + " in ndim=" + str(self.ndim)
            elif hasattr(index, 'item_iter') :
                if index.dtype is bool :
                    ish = index.shape
                    ash = self.shape
                    if ish == ash :
                        nlen = 0
                        it1 = index.item_iter()
                        while it1.has_next() :
                            if it1.next() :
                                nlen += 1
                        if nlen > 0 :
                            res = instance([nlen], dtype = self.dtype)
                            it1 = index.item_iter()
                            its = self.item_iter()
                            itr = res.item_iter()
                            while it1.has_next() :
                                if it1.next() :
                                    itr.set_next(its.next())
                                else :
                                    its.next()
                            return res
                        else :
                            return instance([0], dtype = self.dtype)
                raise Exception, 'index out of range'
            else :
                raise Exception, 'not supported'
        else:
            nst = self.storage[index]
            if isinstance(nst, Array) :
                return self.__new__(nst)
            else :
                return nst            
            
    def get_slice(self, dim, index):
        if self.lazy_loading:
            origin = [0] * self.ndim
            origin[dim] = index
            shape = self.shape
            shape[dim] = 1
            _arr = self.__iDataItem__.getData(jutils.jintcopy(origin), jutils.jintcopy(shape))
            _arr = _arr.getArrayUtils().reduce(dim).getArray()
            return self.__new__(Array(_arr))
        else:
            return self.__new__(self.storage.get_slice(dim, index))
        
    
    def get_section(self, origin, shape, stride = None): 
        if self.lazy_loading:
            ranges = []
            for i in xrange(len(origin)):
                f = origin[i]
                l = shape[i] + f - 1
                if stride:
                    s = stride[i]
                else:
                    s = 1
                ranges.append(nx_factory.createRange(f, l, s))
            _di = self.__iDataItem__.getSection(ranges)
            _arr = _di.getData()
            return self.__new__(Array(_arr))
        else:
            return self.__new__(self.storage.get_section(origin, shape, stride))
    
    def get_reduced(self, dim = None):
        if self.lazy_loading:
            raise NexusException('not supported in lazy loading mode')
        return self.__new__(self.storage.get_reduced(dim))
        
    def section_iter(self, shape):
        return SimpledataSectionIter(self, shape)
#         self.storage.section_iter(shape)
    
    def take(self, indices, axis=None, out=None, mode='raise'):
        if self.lazy_loading:
            raise NexusException('not supported in lazy loading mode')
        return self.__new__(self.storage.take(indices, axis, out, mode))
#####################################################################################
# Array accessing
#####################################################################################    
    def get_value(self, index) :
        if self.lazy_loading:
#             if type(index) is int:
#                 st = self.get_slice(0, index)
#             elif type(index) is list:
#                 st = self.get_
#             return st.get_value(0)
            if type(index) is int:
                st = self.get_slice(0, index)
                return st.get_value(0)
            elif type(index) is list:
                ndim = self.ndim
                ns = [1] * ndim
                ni = [0] * ndim
                se = self.get_section(index, ns)
                return se.get_value(ni)
        else:
            return self.storage.get_value(index)
    
    def __len__(self):
        if self.lazy_loading:
            return self.shape[0]
        else:
            return len(self.storage)
    
    def __getattr__(self, name):
        if name == 'name' :
            return self.__iDataItem__.getShortName()
        elif name == 'units' :
            return self.__iDataItem__.getUnits()
        elif name == 'title' :
            title = self.__iDataItem__.getTitle()
            if title is None :
                return self.name
            else :
                return title
#         elif name == 'lazy_loading' :
#             return self.__dict__[name]
        elif name == 'ndim' :
            if self.lazy_loading:
                return len(self.__iDataItem__.getDimensionList())
            else :
                return getattr(self.storage, name)
        elif name == 'size' :
            if self.lazy_loading:
                dims = self.__iDataItem__.getDimensionList()
                size = 1
                for dim in dims:
                    size *= dim.getLength()
                return size
            else :
                return getattr(self.storage, name)
        elif name == 'shape' :
            if self.lazy_loading:
                dims = self.__iDataItem__.getDimensionList()
                shape = []
                for dim in dims:
                    shape.append(dim.getLength())
                return shape
            else :
                return getattr(self.storage, name)
        elif name == 'dtype' :
            if self.lazy_loading:
                try :
                    tp = DataType.getType(self.__iDataItem__.getType())
                except :
                    tp = DataType.STRING
                if tp.equals(DataType.INT) :
                    return int
                elif tp.equals(DataType.DOUBLE) :
                    return float
                elif tp.equals(DataType.BOOLEAN) :
                    return bool
                elif tp.equals(DataType.LONG) :
                    return long
                elif tp.equals(DataType.CHAR) :
                    return str
                elif tp.equals(DataType.BYTE) :
                    return int
                elif tp.equals(DataType.SHORT) :
                    return int
                elif tp.equals(DataType.FLOAT) :
                    return float
                else :
                    return object
            else :
                return getattr(self.storage, name)
        else :
            att = self.__iDataItem__.findAttributeIgnoreCase(name)
            if att:
                val = att.getValue()
                if val.getElementType() is String:
                    return str(val)
                else:
                    arr = Array(val)
                    if arr.size == 1 :
                        return arr[0]
        return getattr(self.storage, name)

    def read_all(self):
        if self.lazy_loading:
            self.storage = Array(self.__iDataItem__.getData(self.skip_flaws))
            self.lazy_loading = False
        
    def get_attribute(self, name):
        att = self.__iDataItem__.findAttributeIgnoreCase(name)
        if att:
            val = att.getValue()
            if val.getElementType() is String:
                return str(val)
            else:
                arr = Array(val)
                if arr.size == 1 :
                    return arr[0]
        return None

    def __setattr__(self, name, value):
        if name == 'name' :
#            self.__iDataItem__.setShortName(str(value))
            raise AttributeError, 'name can not be set, try set the title instead'
        elif name == 'units' :
            self.__iDataItem__.setUnits(str(value))
        elif name == 'title' :
            self.__iDataItem__.setTitle(str(value))
        elif name == 'storage' :
            self.__dict__[name] = value
        elif name.startswith('__'):
            self.__dict__[name] = value
        else:
            self.__dict__[name] = value
    
    def set_attribute(self, name, value):
        if type(value) is str:
            self.__iDataItem__.addStringAttribute(name, value)
        else:
            arr = Array(value)
            att = nx_factory.createAttribute(name, arr.__iArray__)
            self.__iDataItem__.addOneAttribute(att)
        
        
    def __iter__(self):
        if (self.ndim > 1) :
            return SimpledataSliceIter(self)
        else :
            return self.item_iter()
        
    def item_iter(self):
        if self.lazy_loading:
            raise NexusException('not supported in lazy loading mode, use read_all() to cache data before calling iter_iter')
        else:
            return self.storage.item_iter()
    
    def set_value(self, index, value):
        if self.lazy_loading:
            raise NexusException('not supported in lazy loading mode, use read_all() to cache data before calling set_value')
        else:
            self.storage.set_value(index, value)
        
#********************************************************************************
#     Array math
#********************************************************************************
    
    def __eq__(self, obj) :
        if obj is None:
            return False
        if isinstance(obj, SimpleData) :
            obj = obj.storage
        res = self.storage.__eq__(obj)
        if isinstance(res, Array) :
            return self.__new__(res)
        else :
            return res
    
    def __ne__(self, obj):
        if obj is None:
            return True
        if isinstance(obj, SimpleData) :
            obj = obj.storage
        res = self.storage.__ne__(obj)
        if isinstance(res, Array) :
            return self.__new__(res)
        else :
            return res

    def __lt__(self, obj) :
        if isinstance(obj, SimpleData) :
            obj = obj.storage
        res = self.storage.__lt__(obj)
        return self.__new__(res)

    def __gt__(self, obj) :
        if isinstance(obj, SimpleData) :
            obj = obj.storage
        res = self.storage.__gt__(obj)
        return self.__new__(res)

    def __le__(self, obj) :
        if isinstance(obj, SimpleData) :
            obj = obj.storage
        res = self.storage.__le__(obj)
        return self.__new__(res)

    def __ge__(self, obj) :
        if isinstance(obj, SimpleData) :
            obj = obj.storage
        res = self.storage.__ge__(obj)
        return self.__new__(res)
        
    def __not__(self):
        raise ValueError, 'The truth value of an array with more than one element is ambiguous. Use a.any() or a.all()'

    def __add__(self, obj):
        if isinstance(obj, SimpleData) :
            obj = obj.storage
        return self.__new__(self.storage + obj)

    def __iadd__(self, obj):
        if isinstance(obj, SimpleData) :
            obj = obj.storage
        self.storage.__iadd__(obj)
        return self

    def __radd__(self, obj):
        return self.__add__(obj)
            
    def __div__(self, obj):
        if isinstance(obj, SimpleData) :
            obj = obj.storage
        return self.__new__(self.storage / obj)
    
    def __rdiv__(self, obj):
        if isinstance(obj, SimpleData) :
            obj = obj.storage
        return self.__new__(self.storage.__rdiv__(obj))
    
    def __mul__(self, obj):
        if isinstance(obj, SimpleData) :
            obj = obj.storage
        return self.__new__(self.storage * obj)
    
    def __rmul__(self, obj):
        return self.__mul__(obj)
        
    def __neg__(self):
        return self * -1
    
    def __sub__(self, obj):
        if isinstance(obj, SimpleData) :
            obj = obj.storage
        return self.__new__(self.storage - obj)
    
    def __rsub__(self, obj):
        if isinstance(obj, SimpleData) :
            obj = obj.storage
        return self.__new__(self.storage.__rsub__(obj))
    
    def __invert__(self):
        return self.__new__(self.storage.__invert__())
    
    def matrix_invert(self):
        return self.__new__(self.storage.matrix_invert())
    
    def matrix_dot(self, obj):
        if isinstance(obj, Array) :
            return self.__new__(self.storage.matrix_dot(obj))
        elif isinstance(obj, SimpleData):
            return self.__new__(self.storage.matrix_dot(obj.storage))
        else :
            raise Exception, 'obj must be a gumpy array or dataset'
        
    def __pow__(self, obj):
        if isinstance(obj, SimpleData) :
            obj = obj.storage
        return self.__new__(self.storage.__pow__(obj))

    def exp(self):
        return self.__new__(self.storage.exp())

    def log10(self):
        return self.__new__(self.storage.log10())
    
    def ln(self):
        return self.__new__(self.storage.ln())

    def sqrt(self):
        return self.__new__(self.storage.sqrt())
        
    def __rpow__(self, obj):
        if isinstance(obj, SimpleData) :
            obj = obj.storage
        return self.__new__(self.storage.__rpow__(obj))
    
    def __mod__(self, obj):
        if isinstance(obj, SimpleData) :
            obj = obj.storage
        return self.__new__(self.storage % obj)

    def __rmod__(self, obj):
        if isinstance(obj, SimpleData) :
            obj = obj.storage
        return self.__new__(self.storage.__rmod__(obj))

    def __sin__(self):
        return self.__new__(self.storage.__sin__())
    
    def __cos__(self):
        return self.__new__(self.storage.__cos__())
    
    def __tan__(self):
        return self.__new__(self.storage.__tan__())
    
    def __arcsin__(self):
        return self.__new__(self.storage.__arcsin__())
    
    def __arccos__(self):
        return self.__new__(self.storage.__arccos__())
    
    def __arctan__(self):
        return self.__new__(self.storage.__arctan__())
    
    def __exp__(self):
        return self.__new__(self.storage.__exp__())

    def __prod__(self, axis = None):
        return self.__new__(self.storage.__prod__(axis))
    
    def max(self, axis = None, out = None):
        if axis is None :
            return self.storage.max()
        else :
            if out is None :
                return self.__new__(self.storage.max(axis))
            else :
                if isinstance(out, SimpleData) :
                    obj = out.storage
                else :
                    obj = out
                self.storage.max(axis, obj)
                return out
    
    def min(self, axis = None, out = None):
        if axis is None :
            return self.storage.min()
        else :
            if out is None :
                return self.__new__(self.storage.min(axis))
            else :
                if isinstance(out, SimpleData) :
                    obj = out.storage
                else :
                    obj = out
                self.storage.min(axis, obj)
                return out
    
    def argmax(self, axis = None):
        return self.storage.argmax(axis)

    def argmin(self, axis = None):
        return self.storage.argmin(axis)
        
    def sum(self, axis = None, dtype = None, out = None):
        if axis is None :
            return self.storage.sum(dtype = dtype)
        else :
            if out is None :
                return self.__new__(self.storage.sum(axis, dtype))
            else :
                return self.storage.sum(axis, dtype, out)
            
    def transpose(self, axes = None):
        return self.__new__(self.storage.transpose(axes))
        
    def compress(self, condition, axis = None, out = None):
        return self.__new__(self.storage.compress(condition, axis, out))

    def clip(self, a_min, a_max, out = None):
        if out is None:
            return self.__new__(self.storage.clip(a_min, a_max))
        else :
            if hasattr(out, 'storage'):
                self.storage.clip(a_min, a_max, out.storage)
            else:
                self.storage.clip(a_min, a_max, out)
            return out
        
    def mean(self, axis = None, dtype = None, out = None):
        if axis is None :
            return self.storage.mean(dtype = dtype)
        else :
            if out is None :
                return self.__new__(self.storage.mean(axis, dtype))
            else :
                return self.storage.mean(axis, dtype, out)
#********************************************************************************
#     Array utilities
#********************************************************************************
    
    def __repr__(self, indent = '', skip = True):
        indent += '           '
        if self.lazy_loading:
            res = 'SimpleData(shape=' + str(self.shape) + ', type=' + self.dtype.__name__ \
                + ', storage=<lazy loading>, \n' \
                + indent + 'title=\'' + self.title + '\''
        else:
            res = 'SimpleData(' + self.storage.__repr__(indent, skip) + ', \n' \
                    + indent + 'title=\'' + self.title + '\''
        if not self.units is None :
            res += ',\n' + indent + 'units=\'' + self.units + '\''
        res += ')'
        return res
    
    def __str__(self, indent = '', skip = True):
        res = 'title: ' + self.title + '\n' + indent
        if self.lazy_loading:
            res += 'shape: ' + str(self.shape) + '\n' + indent
            res += 'type: ' + self.dtype.__name__ + '\n' + indent
            res += 'storage: <lazy loading>\n' + indent
        else:
            if self.dtype is str :
                return indent + self.storage.__str__(indent, skip)
            else:
                res += 'storage: ' + self.storage.__str__(indent + ' ' * 9) 
        if not self.units is None and len(self.units) > 0:
            res += 'units: ' + self.units + '\n' + indent
        return res
    
    def tolist(self):
        return self.storage.tolist()
            
#####################################################################################
#   Array modification
#####################################################################################    

    def __setitem__(self, index, value):
        if isinstance(value, SimpleData) :
            value = value.storage
        self.storage[index] = value
    
    def copy_from(self, value, length = -1):
        if isinstance(value, SimpleData) :
            value = value.storage
        self.storage.copy_from(value, length)
                    
    def fill(self, val):
        self.storage.fill(val)
            
    def put(self, indices, values, mode='raise') :
        self.storage.put(indices, values, mode)
        
#####################################################################################
#   Reinterpreting arrays
#####################################################################################    
    def reshape(self, shape): 
        return self.__new__(self.storage.reshape(shape))
    
    def flatten(self) :
        return self.__new__(self.storage.flatten())

    def view_1d(self):
        return self.__new__(self.storage.view_1d())
    
    def __copy__(self):
        return self.__new__(self.storage.__copy__())

    def __deepcopy__(self):
        return self.__new__(self.storage.__deepcopy__())
    
    def float_copy(self):
        return self.__new__(self.storage.float_copy())

    def positive_float_copy(self):
        return self.__new__(self.storage.positive_float_copy())

    def __new__(self, storage, units = None, parent = None, title = None):
        name = self.name
        if units is None :
            units = self.units
        if title is None :
            title = self.title
        sd = new(storage, name, units, parent, title)
        for att in self.__iDataItem__.getAttributeList():
            sd.__iDataItem__.addOneAttribute(att)
        return sd

    def __dir__(self):
        if self.lazy_loading:
            dirs = ['dtype', 'ndim','shape', 'size']
        else:
            dirs = self.storage.__dir__()
        dirs.append('delete_slice')
        dirs.append('get_attribute')
        dirs.append('get_flawed_indices')
        dirs.append('get_value')
        dirs.append('name')
        dirs.append('read_all')
        dirs.append('set_attribute')
        dirs.append('set_value')
        dirs.append('storage')
        dirs.append('title')
        dirs.append('units')
        return sorted(dirs)
        
    def all(self):
        return self.storage.all()
    
    def any(self):
        return self.storage.any()

    def get_flawed_indices(self):
        return self.__iDataItem__.getFlawedIndexList()
     
    def delete_slice(self, indices):
        slices = []
        if type(indices) is int :
            slices = [indices]
        elif hasattr(indices, '__len__') :
            slices = indices
        else :
            raise AttributeError, 'indices must be either integer value or integer list'
        if len(slices) > 0 :
            narr = array.delete(self.storage, slices, 0)
            oname = self.__iDataItem__.getShortName()
            self.__iDataItem__ = nx_factory.\
                            createNXsignal(None, oname, narr.__iArray__)
            self.storage = narr
    
    def intg(self, axis = None, out = None, keepdims = False):
        if axis is None :
            return self.storage.intg()
        else :
            if out is None :
                return self.__new__(self.storage.intg(axis, out, keepdims))
            else :
                return self.storage.intg(axis, out, keepdims)
        
#####################################################################################
# Simpledata slice iter class
#####################################################################################
class SimpledataSliceIter():
    def __init__(self, data):
        self.data = data
        self.cur_slice = -1
    
    def next(self):
        if self.has_next() :
            self.cur_slice += 1
            arr = self.data.get_slice(0, self.cur_slice)
        else :
            raise StopIteration
        return arr
        
    def curr(self):
        return self.data.get_slice(0, self.cur_slice)
    
    def has_next(self):
        return self.cur_slice < len(self.data) - 1

#####################################################################################
# Simpledata section iter class
#####################################################################################
class SimpledataSectionIter():
    def __init__(self, data, shape):
        self.data = data
        if type(shape) is int :
            shape = [shape]
        if len(shape) > data.ndim :
            raise ValueError, 'expecting sections to be at most in ' + str(data.ndim) + ' dim'
        for i in xrange(len(shape)) :
            if data.shape[data.ndim - 1 - i] % shape[len(shape) - 1 -i] != 0 :
                raise ValueError, 'shape is not comfortable at dim=' + str(data.ndim - 1 - i)
        self.__cur_section_org__ = [0] * data.ndim
        self.__section_iter_shape__ = shape
        self.__last_section_org__ = None
    
    def next(self):
        if self.has_next() :
            nshape = [1] * self.data.ndim
            lssh = len(self.__section_iter_shape__)
            for i in xrange(lssh) :
                nshape[self.data.ndim - 1 - i] = self.__section_iter_shape__[lssh - 1 - i]
            sec = self.data.get_section(self.__cur_section_org__, nshape, None)
            if self.data.ndim > lssh :
                sec = sec.get_reduced(range(self.data.ndim - lssh))
            self.__cur_section_org__[self.data.ndim - 1] += self.__section_iter_shape__[lssh - 1]
            for i in xrange(self.data.ndim) :
                cdim = self.data.ndim - 1 - i
                if self.__cur_section_org__[cdim] >= self.data.shape[cdim] :
                    if cdim > 0 :
                        self.__cur_section_org__[cdim] = 0
                        if i < lssh - 1:
                            gain = self.__section_iter_shape__[lssh - 2 - i]
                        else :
                            gain = 1
                        self.__last_section_org__ = copy.copy(self.__cur_section_org__)
                        self.__cur_section_org__[cdim - 1] += gain
            return sec
        else :
            raise StopIteration
        
    def curr(self):
        if self.__last_section_org__ is None :
            return None
        else :
            return self.data.get_slice(0, self.__last_section_org__)
    
    def has_next(self):
        if self.__cur_section_org__[0] < self.data.shape[0] :
            return True
        else :
            return False
    
#####################################################################################
# Array utilities
#####################################################################################
    
def get_item(obj, id, shape = None, ismatrix = True):
    if isinstance(obj, SimpleData) :
        obj = obj.storage
    return array.get_item(obj, id, shape, ismatrix)
        

#####################################################################################
# Array creation
#####################################################################################    
def zeros(shape, dtype = float): 
    return new(array.zeros(shape, dtype))

def zeros_like(obj):
    name = None
    units = None
    if isinstance(obj, SimpleData) :
        name = obj.name
        units = obj.units
        obj = obj.storage
    return new(array.zeros_like(obj), name, units)

def diagflat(obj, k = 0):
    return new(array.diagflat(obj, k))
        
def ones(shape, dtype = float):
    return new(array.ones(shape, dtype))
    
def ones_like(obj):
    name = None
    units = None
    if isinstance(obj, SimpleData) :
        name = obj.name
        units = obj.units
        obj = obj.storage
    return new(array.ones_like(obj), name, units)

def eye(N, M = None, k = 0, dtype = float):
    return new(array.eye(N, M, k, dtype))
    
def rand(shape, para = None, engine = None, dtype = float):
    return new(array.rand(shape, para, engine, dtype))
    
def instance(shape, init = 0, dtype = float):
    return new(array.instance(shape, init, dtype))

def asarray(obj, dtype = None):
    name = None
    units = None
    if isinstance(obj, SimpleData) :
        name = obj.name
        units = obj.units
        obj = obj.storage
    return new(array.asarray(obj, dtype), name, units)
    
def arange(*args) :
    return new(array.arange(*args))

def linspace(start, end, num, shape = None, endpoint=True, retstep=False):
    return new(array.linspace(start, end, num, shape, endpoint, retstep))

#####################################################################################
# Create array from existing data
#####################################################################################
def take(sdata, indices, axis=None, out=None):
    return sdata.take(indices, axis, out)
    
#####################################################################################
# Joining arrays
#####################################################################################
def append(obj, val, axis = None):
    name = None
    units = None
    if isinstance(obj, SimpleData) :
        name = obj.name
        units = obj.units
        obj = obj.storage
    return new(array.append(obj, val, axis), name, units)

def concatenate(tup, axis = 0):
    return new(array.concatenate(tup, axis))
            
def column_stack(*tup):
    return new(array.column_stack(*tup))

def vstack(*tup):
    return new(array.vstack(*tup))

def hstack(*tup):
    return new(array.hstack(*tup))
    
def dstack(*tup):
    return new(array.dstack(*tup))

#####################################################################################
# Splitting arrays
#####################################################################################
def array_split(obj, indices_or_sections, axis = 0):
    name = None
    units = None
    if isinstance(obj, SimpleData) :
        name = obj.name
        units = obj.units
        obj = obj.storage
    ares = array.array_split(obj, indices_or_sections, axis)
    res = []
    for item in ares :
        res += [new(item, name = name, units = units)]
    return res

def split(obj, indices_or_sections, axis = 0):
    if isinstance(obj, SimpleData) :
        obj = obj.storage
    ares = array.split(obj, indices_or_sections, axis)
    res = []
    for item in ares :
        res += [new(item)]
    return res
        
def dsplit(obj, indices_or_sections):
    if isinstance(obj, SimpleData) :
        obj = obj.storage
    ares = array.dsplit(obj, indices_or_sections)
    res = []
    for item in ares :
        res += [new(item)]
    return res

def hsplit(obj, indices_or_sections):
    if isinstance(obj, SimpleData) :
        obj = obj.storage
    ares = array.hsplit(obj, indices_or_sections)
    res = []
    for item in ares :
        res += [new(item)]
    return res

def vsplit(obj, indices_or_sections):
    if isinstance(obj, SimpleData) :
        obj = obj.storage
    ares = array.vsplit(obj, indices_or_sections)
    res = []
    for item in ares :
        res += [new(item)]
    return res


#####################################################################################
# Enlarging arrays
#####################################################################################
def tile(obj, reps):
    if isinstance(obj, SimpleData) :
        obj = obj.storage
    return array.tile(obj, reps)

def repeat(obj, repeats, axis=None):
    raise TypeError, 'not supported'
    
    
#####################################################################################
# Adding and removing elements
#####################################################################################    
def delete(sdata, obj, axis=None) :
    if isinstance(sdata, SimpleData) :
        sdata = sdata.storage
    return new(array.delete(sdata, obj, axis))

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
def sin(obj):
    return obj.__sin__()

def max(obj, axis = None, out = None):
        if isinstance(obj, SimpleData) :
            obj = obj.storage
        return array.max(obj, axis, out)
    
def min(obj, axis = None, out = None):
        if isinstance(obj, SimpleData) :
            obj = obj.storage
        return array.min(obj, axis, out)
    
#####################################################################################
# Data utilities
#####################################################################################
def new(storage, name = None, units = None, parent = None, title = None):
    iGroup = None
    if not parent is None :
        iGroup = parent.__iNXdata__

    iDataItem = nx_factory.createNXDataItem(iGroup, 'null', \
                                            storage.__iArray__)
    if name is None :
        name = 'sd' + str(id(iDataItem))
    iDataItem.setShortName(name)
    if not units is None :
        iDataItem.setUnits(str(units))
    if not title is None :
        iDataItem.setTitle(str(title))
    return SimpleData(iDataItem)

s_ = array.s_
#def sdata(storage, name = None, parent = None):
#    return new(storage, name, parent)

#####################################################################################
# Numpy standards
#####################################################################################
empty = instance    