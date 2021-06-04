from gumpy.commons import jutils
from org.gumtree.data import DataType
from org.gumtree.data.utils import FactoryManager, Utilities
from symbol import except_clause
import copy
import math
import random
import itertools
from java.lang import Double

gdm_factory = FactoryManager().getFactory()

class Array:
    
    precision = 6
    threshold = 1000
    edgeitems = 3
    linewidth = 75
    suppress = False
    nanstr = 'NaN'
    infstr = 'Inf'
    
    def __init__(self, obj, shape = None, dtype = None):
        if type(obj) is int or type(obj) is float or type(obj) is long or type(obj) is bool :
            obj = [obj]
        iArray = obj
        self._dtype = None
        self._itemsize = 0
        if hasattr(obj, '__len__') :
#            if len(obj) == 0 :
#                raise Exception, 'empty list not allowed'
            if obj == []:
                self._size = 0
                self._dtype = float if dtype is None else dtype
                self._shape = [0]
                self._ndim = 1
                self._itemsize = 8
                self.__iArray__ = gdm_factory.createArray(DataType.INT.getPrimitiveClassType(), 
                                                          jutils.jintcopy([0]))
                return
            rawrank = get_ndim(obj)
            rawshape = get_shape(obj, rawrank)
            if shape is None :
                rank = rawrank
                shape = rawshape
            else :
                rank = len(shape)
            size = 1
            for dim in range(rank) :
                size *= shape[dim]
            jshape = jutils.jintcopy(shape)
            tp = DataType.INT
            if dtype is None :
                self._dtype = int
                self._itemsize = 4
                for id in xrange(size) :
                    val = get_item(obj, id, rawshape)
                    if type(val) is int :
                        self._itemsize = 4
                        continue
                    elif type(val) is float :
                        tp = DataType.DOUBLE
                        self._dtype = float
                        self._itemsize = 8
                        break
                    elif type(val) is str :
                        tp = DataType.CHAR
                        self._dtype = str
                        self._itemsize = 2
                        break
                    elif type(val) is bool :
                        tp = DataType.BOOLEAN
                        self._dtype = bool
                        self._itemsize = 1
                        break
                    elif type(val) is long :
                        tp = DataType.LONG
                        self._dtype = long
                        self._itemsize = 8
                        break
                    else :
                        tp = DataType.STRING
                        self._dtype = object
                        self._itemsize = 0
                        break
                if self._dtype is str :
                    if not type(obj) is str :
                        tp = DataType.STRING
                        self._dtype = object
                        self._itemsize = 0
            else :
                self._dtype = dtype
                if dtype is int :
                    tp = DataType.INT
                    self._itemsize = 4
                elif dtype is float :
                    tp = DataType.DOUBLE
                    self._itemsize = 8
                elif dtype is long :
                    tp = DataType.LONG
                    self._itemsize = 8
                elif dtype is bool :
                    tp = DataType.BOOLEAN
                    self._itemsize = 1
                elif dtype is str :
                    tp = DataType.CHAR
                    self._itemsize = 2
                else :
                    tp = DataType.STRING
                    self._itemsize = 0
            iArray = gdm_factory.createArray(tp.getPrimitiveClassType(), jshape)
            iter = iArray.getIterator()
            if self._dtype is int :
                for id in xrange(size) :
                    val = get_item(obj, id, rawshape)
                    iter.next().setIntCurrent(val)
            elif self._dtype is float :
                for id in xrange(size) :
                    val = get_item(obj, id, rawshape)
                    iter.next().setDoubleCurrent(float(val))
            elif self._dtype is bool :
                for id in xrange(size) :
                    val = get_item(obj, id, rawshape)
                    iter.next().setBooleanCurrent(bool(val))
            elif self._dtype is long :
                for id in xrange(size) :
                    val = get_item(obj, id, rawshape)
                    iter.next().setLongCurrent(long(val))
            elif self._dtype is str :
                for id in xrange(size) :
                    val = get_item(obj, id, rawshape)
                    iter.next().setCharCurrent(val)
            else :
                for id in xrange(size) :
                    val = get_item(obj, id, rawshape)
                    iter.next().setObjectCurrent(str(val))
#        else :
#            if not dtype is None :
#                raise TypeError, 'can not assign type to existing storage'
        self.__iArray__ = iArray
        self._size = int(iArray.getSize())
        self._ndim = iArray.getRank()
        jshape = iArray.getShape()
        self._shape = []
        for counter in range(self._ndim):
            dim = jshape[counter]
            self._shape.append(dim)
        try :
            tp = DataType.getType(iArray.getElementType())
        except :
            tp = DataType.STRING
        if tp.equals(DataType.INT) :
            self.get_value = self.get_int
            self.set_prime_value = self.set_int_value
            self._dtype = int
            self._itemsize = 4
        elif tp.equals(DataType.DOUBLE) :
            self.get_value = self.get_float
            self.set_prime_value = self.set_float_value
            self._dtype = float
            self._itemsize = 8
        elif tp.equals(DataType.BOOLEAN) :
            self.get_value = self.get_bool
            self.set_prime_value = self.set_bool_value
            self._dtype = bool
            self._itemsize = 1
        elif tp.equals(DataType.LONG) :
            self.get_value = self.get_long
            self.set_prime_value = self.set_long_value
            self._dtype = long
            self._itemsize = 8
        elif tp.equals(DataType.CHAR) :
            self.get_value = self.get_char
            self.set_prime_value = self.set_char_value
            self._dtype = str
            self._itemsize = 2
        elif tp.equals(DataType.BYTE) :
            self.get_value = self.get_int
            self.set_prime_value = self.set_int_value
            self._dtype = int
            self._itemsize = 4
        elif tp.equals(DataType.SHORT) :
            self.get_value = self.get_int
            self.set_prime_value = self.set_int_value
            self._dtype = int
            self._itemsize = 4
        elif tp.equals(DataType.FLOAT) :
            self.get_value = self.get_float
            self.set_prime_value = self.set_float_value
            self._dtype = float
            self._itemsize = 8
        else :
            self.get_value = self.get_str
            self.set_prime_value = self.set_str_value
            self._dtype = object
            self._itemsize = 0
    
#####################################################################################
#   Array indexing
#####################################################################################
    def __getitem__(self, index):
        if index is Ellipsis:
            return self
        if type(index) is int :
            if index < 0 :
                index = self._shape[0] + index
            if index >= self._shape[0] :
                raise Exception, 'out of range, ' + str(index) + " in " + \
                    str(self._shape[0])
            if self._ndim == 1 :
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
                stop = self._shape[0]
            if start < 0 :
                start += self._shape[0]
                if start < 0 :
                    start = 0
            if start > self._shape[0] :
                return instance([0], self._dtype)
            if stop > self._shape[0] :
                stop = self._shape[0]
            if stop <= 0 :
                stop += self._shape[0]
                if stop <= 0 :
                    return instance([0], self._dtype)
            origin = [0] * self._ndim
            origin[0] = start
            shape = self.shape
            shape[0] = int(math.ceil(float(stop - start) / step))
            if (shape[0] <= 0) :
                return instance([0], self._ndim)
            if step == 1 :
                section = self.get_section(origin, shape)
            else :
                stride = [1] * self._ndim
                stride[0] = step
#                if stride[0] > 1 :
#                    shape[0] = int(math.ceil(float(shape[0]) / stride[0]))
                section = self.get_section(origin, shape, stride)
            return section
        elif hasattr(index, '__iter__') :
            index = tuple(index)
            origin = [0] * self._ndim
            shape = self.shape
            stride = [1] * self._ndim
            if len(index) <= self._ndim :
                i = 0
                secflag = len(index) < self._ndim
                reducedim = 0
                for item in index :
                    if type(item) is int :
                        if item < 0 :
                            item = self._shape[i] + item
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
                            stop = self._shape[i]
                        if start < 0 :
                            start += self._shape[i]
                            if start < 0 :
                                start = 0
                        if start > self._shape[i] :
                            return instance([0], self._dtype)
                        if stop > self._shape[i] :
                            stop = self._shape[i]
                        if stop < 0 :
                            stop += self._shape[i]
                            if stop <= 0 :
                                return instance([0], self._dtype)
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
                raise Exception, 'dim=' + str(len(index)) + " in ndim=" + str(self._ndim)
        elif hasattr(index, 'item_iter') :
            if index.dtype is bool :
                ish = index.shape
                ash = self._shape
                if ish == ash :
                    nlen = 0
                    it1 = index.item_iter()
                    while it1.has_next() :
                        if it1.next() :
                            nlen += 1
                    if nlen > 0 :
                        res = instance([nlen], dtype = self._dtype)
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
                        return instance([0], dtype = self._dtype)
            raise Exception, 'index out of range'
        else :
            raise Exception, 'not supported'
    
#     def __getattribute__(self, name):
#         if some_predicate(name):
#             if name == 'shape':
#                 return self.shape
#             elif name == 'size':
#                 return self._size
#             elif name == 'ndim':
#                 return self._ndim
#             elif name == 'itemsize':
#                 return self._itemsize
#             elif name == 'dtype':
#                 return self._dtype
#         else:
#             return object.__getattribute__(self, name)
        
    def __getattr__(self, name):
        if name == 'shape':
            return copy.copy(self._shape)
        elif name == 'size':
            return self._size
        elif name == 'ndim':
            return self._ndim
        elif name == 'itemsize':
            return self._itemsize
        elif name == 'dtype':
            return self._dtype
        else:
            raise AttributeError('attribute error')
#             return object.__getattribute__(self, name)
                
    def get_slice(self, dim, index):
        return Array(self.__iArray__.getArrayUtils().slice(dim, index).getArray())
    
    def get_section(self, origin, shape, stride = None): 
        jorigin = jutils.jintcopy(origin)
        jshape = jutils.jintcopy(shape)
        if stride is None :
            stride = [1] * len(shape)
        jstride = jutils.jlongcopy(stride)
        return Array(self.__iArray__.getArrayUtils().sectionNoReduce(jorigin, jshape, \
                        jstride).getArray())
    
    def get_reduced(self, dim = None):
        if dim is None :
            return Array(self.__iArray__.getArrayUtils().reduce().getArray())
        elif hasattr(dim, '__len__') :
            autils = self.__iArray__.getArrayUtils()
            for val in sorted(dim, reverse = True) :
                autils = autils.reduce(val)
            return Array(autils.getArray())
        else :
            return Array(self.__iArray__.getArrayUtils().reduce(dim).getArray())
    
    def squeeze(self, axis = None):
        return self.get_reduced(axis)
    
    def item(self, *args):
        if len(args) == 0:
            idx = (0,) * self._ndim
            return self.get_value(idx)
        elif len(args) == 1:
            arg = args[0]
            if hasattr(arg, '__iter__'):
                return self.get_value(arg)
            else:
                return self.get_value(self._1d_to_nd_index(arg))
        else:
            return self.get_value(args)
        
    def itemset(self, *args):
        if len(args) == 0:
            raise ValueError('args can not be empty')
        elif len(args) == 1:
            self.fill(args[0])
        elif len(args) == 2:
            idx = args[0]
            val = args[1]
            if hasattr(idx, '__iter__'):
                return self.set_value(idx, val)
            else:
                return self.set_value(self._1d_to_nd_index(idx), val)
            self.__setitem__(args[0], args[1])
        else:
            raise ValueError('too many args')
        
    def _1d_to_nd_index(self, idx_1d):
        idx_1d = int(idx_1d)
        shape = self._shape
        idx_nd = ()
        for i in xrange(self._ndim - 1):
            cap = sum(shape[i + 1 :])
            idx = idx_1d / cap
            idx_1d = idx_1d - idx * cap
            idx_nd += (idx,)
        idx_nd += (idx_1d,)
        return idx_nd
        
    def section_iter(self, shape):
        return ArraySectionIter(self, shape)
    
    def take(self, indices, axis=None, out=None, mode='raise'):
        if axis is None :
            afl = self.flatten()
            if type(indices) is int or type(indices) is slice :
                if out is None :
                    return afl[indices]
                else :
                    out.copy_from(afl[indices])
                    return out
            elif hasattr(indices, '__len__') :
                nshape = get_shape(indices)
                idx = Array(indices, dtype = int)
                if out is None:
                    out = zeros(idx.shape, self.dtype)
                oi = out.item_iter()
                ii = idx.item_iter()
                try:
                    while True:
                        oi.set_next(afl[ii.next()])
                except StopIteration:
                    pass
                return out
        elif axis >= self._ndim :
            raise ValueError, 'axis must be within the ndim of the array, ' + str(axis) \
                        + ' in ' + str(self._ndim)
        else :
            tp = ()
            shape = self._shape
            for i in xrange(self._ndim) :
                if i == axis :
                    tp += (indices,)
                else :
                    tp += (slice(shape[i]),)
            if type(indices) is int or type(indices) is slice:
                if out is None :
                    return self.__getitem__(tp)
                else :
                    out.copy_from(self.__getitem__(tp))
                    return out
            elif hasattr(indices, '__len__') :
                nsize = len(indices)
                osize = self._shape[axis]
                nshape = self.shape
                nshape[axis] = nsize
                if out is None :
                    out = instance(nshape, 0, self._dtype)
                for i in xrange(nsize) :
                    val = indices[i]
                    if val < 0 :
                        val += osize
                        if val < 0 :
                            if mode == 'clip' :
                                val = 0
                            elif mode == 'wrap' :
                                val = val % osize
                            else :
                                raise ValueError, 'index out of range: ' + str(indices[i]) \
                                        + ' in ' + str(osize)
                    if val >= osize :
                        if mode == 'clip' :
                            val = osize - 1
                        elif mode == 'wrap' :
                            val = val % osize
                        else :
                            raise ValueError, 'index out of range: ' + str(val) \
                                + ' in ' + str(osize)
                    if self._ndim == 1 :
                        out[i] = self[indices[i]]
                    else :
                        out.get_slice(axis, i).copy_from(
                            self.get_slice(axis, indices[i]))
                return out
            
#####################################################################################
# Array accessing
#####################################################################################    
    def __len__(self):
        return self._shape[0]
    
#    def __getattr__(self, name):
##        raise AttributeError(name + ' not exists')
#        return None
    
    def __iter__(self):
        if (self._ndim > 1) :
            return ArraySliceIter(self)
        else :
            return ArrayItemIter(self)
        
    def item_iter(self):
        return ArrayItemIter(self)
    
    #********************************************************************************
    #   type-specific get and set methods
    #********************************************************************************
    def get_float(self, index):
        if type(index) is int :
            return self.__iArray__.getDouble(self.__iArray__.getIndex().set(index))
        elif type(index) is list :
            return self.__iArray__.getDouble(self.__iArray__.getIndex().set(jutils.jintcopy(index)))
    
    def get_int(self, index):
        if type(index) is int :
            return self.__iArray__.getInt(self.__iArray__.getIndex().set(index))
        elif type(index) is list :
            return self.__iArray__.getInt(self.__iArray__.getIndex().set(jutils.jintcopy(index)))
        else :
            return self.__iArray__.getInt(self.__iArray__.getIndex().set(index))
    
    def get_bool(self, index):
        if type(index) is int :
            return self.__iArray__.getBoolean(self.__iArray__.getIndex().set(index))
        elif type(index) is list :
            return self.__iArray__.getBoolean(self.__iArray__.getIndex().set(jutils.jintcopy(index)))

    def get_char(self, index):
        if type(index) is int :
            return self.__iArray__.getChar(self.__iArray__.getIndex().set(index))
        elif type(index) is list :
            return self.__iArray__.getChar(self.__iArray__.getIndex().set(jutils.jintcopy(index)))
    
    def get_long(self, index):
        if type(index) is int :
            return self.__iArray__.getLong(self.__iArray__.getIndex().set(index))
        elif type(index) is list :
            return self.__iArray__.getLong(self.__iArray__.getIndex().set(jutils.jintcopy(index)))

    def get_str(self, index):
        if type(index) is int :
            return str(self.__iArray__.getObject(self.__iArray__.getIndex().set(index)))
        elif type(index) is list :
            return str(self.__iArray__.getObject(self.__iArray__.getIndex().set(jutils.jintcopy(index))))

    def set_value(self, index, value):
        if type(index) is int :
            i = index
        elif type(index) is list or type(index) is tuple:
            i = jutils.jintcopy(index)
        else :
            i = index
        self.set_prime_value(i, value)
        
    def set_float_value(self, index, value):
        self.__iArray__.setDouble(self.__iArray__.getIndex().set(index), float(value))
        
    def set_int_value(self, index, value):
        self.__iArray__.setInt(self.__iArray__.getIndex().set(index), int(value))
    
    def set_bool_value(self, index, value):
        self.__iArray__.setBoolean(self.__iArray__.getIndex().set(index), bool(value))
    
    def set_char_value(self, index, value):
        self.__iArray__.setChar(self.__iArray__.getIndex().set(index), str(value))
    
    def set_long_value(self, index, value):
        self.__iArray__.setLong(self.__iArray__.getIndex().set(index), long(value))
    
    def set_str_value(self, index, value):
        self.__iArray__.setObject(self.__iArray__.getIndex().set(index), str(value))

#********************************************************************************
#     Array math
#********************************************************************************
    def __match_type__(self, obj, fdiv = False):
        ntype = self._dtype
        if ntype is int or ntype is long :
            if fdiv :
                if 3 / 2 > 1 :
                    print '__future__.division'
                    return float
            if isinstance(obj, Array) :
                ntype = obj.dtype
            else :
                if hasattr(obj, '__len__') :
                    rawshape = get_shape(obj)
                    for id in xrange(len(obj)) :
                        if type(get_item(obj, id, rawshape)) is float :
                            ntype = float
                            break
                else :
                    if type(obj) is float :
                        ntype = float
        return ntype
    
    def __eq__(self, obj):
        if obj is None:
            return False
        if isinstance(obj, Array) :
            iarr = self.__iArray__
            oarr = obj.__iArray__
            rarr = iarr.getArrayMath().equalTo(oarr).getArray()
            return Array(rarr)
        else :
            if hasattr(obj, '__len__') :
                return self == Array(obj)
            else :
                iarr = self.__iArray__
                rarr = iarr.getArrayMath().equalTo(obj).getArray()
                return Array(rarr)
    
    def __ne__(self, obj):
        if obj is None:
            return True
        if isinstance(obj, Array) :
            iarr = self.__iArray__
            oarr = obj.__iArray__
            rarr = iarr.getArrayMath().notEqualTo(oarr).getArray()
            return Array(rarr)
        else :
            if hasattr(obj, '__len__') :
                return self != Array(obj)
            else :
                iarr = self.__iArray__
                rarr = iarr.getArrayMath().notEqualTo(obj).getArray()
                return Array(rarr)
    
    def __lt__(self, obj):
        if isinstance(obj, Array) :
            iarr = self.__iArray__
            oarr = obj.__iArray__
            rarr = iarr.getArrayMath().lessThan(oarr).getArray()
            return Array(rarr)
        else :
            if hasattr(obj, '__len__') :
                return self < Array(obj)
            else :
                iarr = self.__iArray__
                rarr = iarr.getArrayMath().lessThan(obj).getArray()
                return Array(rarr)
 
    def __le__(self, obj):
        if isinstance(obj, Array) :
            iarr = self.__iArray__
            oarr = obj.__iArray__
            rarr = iarr.getArrayMath().lessEqualThan(oarr).getArray()
            return Array(rarr)
        else :
            if hasattr(obj, '__len__') :
                return self <= Array(obj)
            else :
                iarr = self.__iArray__
                rarr = iarr.getArrayMath().lessEqualThan(obj).getArray()
                return Array(rarr)

    def __gt__(self, obj):
        if isinstance(obj, Array) :
            iarr = self.__iArray__
            oarr = obj.__iArray__
            rarr = iarr.getArrayMath().largerThan(oarr).getArray()
            return Array(rarr)
        else :
            if hasattr(obj, '__len__') :
                return self > Array(obj)
            else :
                iarr = self.__iArray__
                rarr = iarr.getArrayMath().largerThan(obj).getArray()
                return Array(rarr)

    def __ge__(self, obj):
        if isinstance(obj, Array) :
            iarr = self.__iArray__
            oarr = obj.__iArray__
            rarr = iarr.getArrayMath().largerEqualThan(oarr).getArray()
            return Array(rarr)
        else :
            if hasattr(obj, '__len__') :
                return self >= Array(obj)
            else :
                iarr = self.__iArray__
                rarr = iarr.getArrayMath().largerEqualThan(obj).getArray()
                return Array(rarr)
    
    def __not__(self):
        raise ValueError, 'The truth value of an array with more than one element is ambiguous. Use a.any() or a.all()'

    def __bool__(self):
        raise ValueError, 'The truth value of an array with more than one element is ambiguous. Use a.any() or a.all()'
        
    def __or__(self, obj):
        if isinstance(obj, Array) :
            if self._shape != obj.shape :
                raise ValueError, 'dimension does not match'
            res = instance(self._shape, dtype = bool)
            siter = self.item_iter()
            oiter = obj.item_iter()
            riter = res.item_iter()
            try :
                while True:
                    s = siter.next() 
                    o = oiter.next()
                    riter.set_next(s | o)
            except StopIteration:
                pass
            return res
        else :
            if hasattr(obj, '__len__') :
                return self or Array(obj)
            else :
                res = instance(self._shape, dtype = bool)
                siter = self.item_iter()
                riter = res.item_iter()
                try :
                    while True:
                        riter.set_next(siter.next() | obj)
                except StopIteration:
                    pass
                return res

    def __ior__(self, obj):
        siter = self.item_iter()
        if hasattr(obj, '__len__') :
            if obj.size < self._size :
                raise Exception, 'resource should have at least ' + \
                    str(self._size) + ' items, got ' + str(len(obj))
            oiter = obj.item_iter()
            try :
                while True:
                    siter.set_curr(siter.next() | oiter.next())
            except StopIteration:
                pass
        else :
            try :
                while True :
                    siter.set_curr(siter.next() | obj)
            except StopIteration:
                pass
        return self

    
    def __xor__(self, obj):
        if isinstance(obj, Array) :
            if self._shape != obj.shape :
                raise ValueError, 'dimension does not match'
            res = instance(self._shape, dtype = bool)
            siter = self.item_iter()
            oiter = obj.item_iter()
            riter = res.item_iter()
            try :
                while True:
                    s = siter.next() 
                    o = oiter.next()
                    riter.set_next(s ^ o)
            except StopIteration:
                pass
            return res
        else :
            if hasattr(obj, '__len__') :
                return self or Array(obj)
            else :
                res = instance(self._shape, dtype = bool)
                siter = self.item_iter()
                riter = res.item_iter()
                try :
                    while True:
                        riter.set_next(siter.next() ^ obj)
                except StopIteration:
                    pass
                return res

    def __ixor__(self, obj):
        siter = self.item_iter()
        if hasattr(obj, '__len__') :
            if obj.size < self._size :
                raise Exception, 'resource should have at least ' + \
                    str(self._size) + ' items, got ' + str(len(obj))
            oiter = obj.item_iter()
            try :
                while True:
                    siter.set_curr(siter.next() ^ oiter.next())
            except StopIteration:
                pass
        else :
            try :
                while True :
                    siter.set_curr(siter.next() ^ obj)
            except StopIteration:
                pass
        return self
                
    def __and__(self, obj):
        if isinstance(obj, Array) :
            if self._shape != obj.shape :
                raise ValueError, 'dimension does not match'
            res = instance(self._shape, dtype = bool)
            siter = self.item_iter()
            oiter = obj.item_iter()
            riter = res.item_iter()
            try :
                while True:
                    riter.set_next(siter.next() & oiter.next())
            except StopIteration:
                pass
            return res
        else :
            if hasattr(obj, '__len__') :
                return self or Array(obj)
            else :
                res = instance(self._shape, dtype = bool)
                siter = self.item_iter()
                riter = res.item_iter()
                try :
                    while True:
                        riter.set_next(siter.next() & obj)
                except StopIteration:
                    pass
                return res

    def __iand__(self, obj):
        siter = self.item_iter()
        if hasattr(obj, '__len__') :
            if obj.size < self._size :
                raise Exception, 'resource should have at least ' + \
                    str(self._size) + ' items, got ' + str(len(obj))
            oiter = obj.item_iter()
            try :
                while True:
                    siter.set_curr(siter.next() & oiter.next())
            except StopIteration:
                pass
        else :
            try :
                while True :
                    siter.set_curr(siter.next() & obj)
            except StopIteration:
                pass
        return self
                                
    def count_nonzero(self, axis = None):
        dtype = self._dtype
        if axis is None:
            cnt = 0
            if dtype is float or dtype is int :
                siter = self.item_iter()
                try :
                    while True :
                        if siter.next():
                            cnt += 1
                except :
                    pass
            return cnt
        else:
            if type(axis) is int:
                axis = [axis]
            for i in axis:
                if i >= self._ndim:
                    raise ValueError('axis {} out of range'.format(i))
            if len(axis) == self._ndim:
                return self.count_nonzero()
            oshape = []
            ishape = [1] * self._ndim
            for i in xrange(self._ndim):
                if i in axis:
                    ishape[i] = self._shape[i]
                else:
                    oshape.append(self._shape[i])
            out = zeros(oshape, int)
            oi = out.item_iter()
            si = self.section_iter(ishape)
            while oi.has_next():
                ss = si.next()
                oi.set_next(ss.count_nonzero())
            return out
        
    def nonzero(self):
        out = [[] for i in xrange(self._ndim)]
        iter = self.item_iter()
        try:
            while True:
                val = iter.next()
                if not val == 0:
                    idx = iter.iter.getCounter()
                    for i in xrange(self._ndim) :
                        out[i].append(idx[i])
        except StopIteration, e:
            pass
        for i in xrange(len(out)):
            out[i] = Array(out[i])
        return tuple(out)
        
    def transpose(self, axes = None):
        if axes is None:
            dim1 = self._ndim - 1
            dim2 = self._ndim - 2
        elif not hasattr(axes, '__len__'):
            raise Exception, 'axes parameter must be either a list or a tuple with length of 2'
        elif len(axes) != 2 :
            raise Exception, 'axes parameter must be either a list or a tuple with length of 2'
        else :
            dim1 = axes[0]
            dim2 = axes[1]
        if dim2 >= self._ndim :
            raise Exception, 'dimension ' + str(dim2) + ' is not available'
        if dim1 >= self._ndim :
            raise Exception, 'dimension ' + str(dim1) + ' is not available'
        return Array(self.__iArray__.getArrayUtils().transpose(dim1, dim2).getArray())
        
    def flip(self, axes = None):
        if axes is None:
            axes = range(self._ndim)
        if type(axes) is int:
            return Array(self.__iArray__.getArrayUtils().flip(axes).getArray())
        elif hasattr(axes, '__len__'):
            au = self.__iArray__.getArrayUtils()
            for i in axes:
                au = au.flip(i)
            return Array(au.getArray())
        else:
            raise ValueError('axes must be int or a sequence of int values')
        
#     def compress(self, condition, axis = None, out = None):
#         if axis is None:
#             osize = self._size
#             if osize > len(condition):
#                 osize = len(condition)
#             idx = 0
#             storage = []
#             it = self.item_iter()
#             while idx < osize:
#                 if condition[idx] :
#                     storage.append(it.next())
#                 else :
#                     it.next()
#                 idx += 1
#             return Array(storage, dtype = self._dtype)
#         else:
#             osize = self._shape[axis]
#             if osize > len(condition):
#                 osize = len(condition)
#             nsize = 0
#             for i in xrange(osize) :
#                 if condition[i] :
#                     nsize += 1
#             if nsize == 0:
#                 return null
#             nshape = self.shape
#             nshape[axis] = nsize
#             narr = instance(nshape, dtype = self._dtype)
#             idx = 0
#             for i in xrange(osize):
#                 if condition[i] :
#                     narr.get_slice(axis, idx).copy_from(self.get_slice(axis, i))
#                     idx += 1
#             return narr

    def compress(self, condition, axis=None, out=None):
        size = 0
        for c in condition:
            if c:
                size += 1
        if size == 0:
            raise ValueError('empty selection')
        ct = 0
        if out is None:
            if axis is None:
                shape = [size]
            else:
                shape = self.shape
                ss = self.shape
                shape[axis] = size
                ss[axis] = 1
            out = zeros(shape, self.dtype)
        if axis is None:
            si = self.item_iter()
            oi = out.item_iter()
            for c in condition:
                if c:
                    oi.set_next(si.next())
                else:
                    si.next()
        else:
            si = self.section_iter(ss)
            oi = out.section_iter(ss)
            for c in condition:
                ss = si.next()
                if c:
                    os = oi.next()
                    os.copy_from(ss)
        return out
    
    def clip(self, a_min, a_max, out = None):
        if out is None:
            out = instance(self._shape, dtype = self._dtype)
        it = self.item_iter()
        nit = out.item_iter()
        while it.has_next():
            val = it.next()
            if val < a_min:
                nit.set_next(a_min)
            elif val > a_max:
                nit.set_next(a_max)
            else:
                nit.set_next(val)
        return out
        
    def mean(self, axis = None, dtype = None, out = None):
        dsum = self.sum(axis, dtype, out)
        if axis is None:
            dsize = self._size
        else :
            dsize = 1
            for i in xrange(self._ndim):
                if i != axis:
                    dsize *= self._shape[i]
        return dsum / dsize

    def sqr_dev(self):
        c = self.mean()
        si = self.item_iter()
        sd = 0
        try:
            while True:
                sd += (si.next() - c) ** 2
        except StopIteration:
            pass
        return sd

    def amean(self, axis = None, dtype = None, out = None):
        dsum = self.asum(axis, dtype, out)
        if axis is None:
            dsize = self._size
        else :
            if axis < 0:
                axis = self._ndim + axis
            dsize = self.shape[axis]
        return dsum / dsize

    def std(self, axis=None, dtype=None, out=None, ddof=0):
        if axis is None:
            n = self.size
            if n < 2:
                raise ValueError('variance requires at least two data points')
            if n == ddof:
                raise ValueError('invalid ddof, ddof can not be the size of the array')
            ss = self.sqr_dev()
            pvar = (ss / (n - ddof)) ** 0.5
            if not out is None:
                out[:] = pvar
            else:
                out = pvar
            return out
        elif type(axis) is int:
            if dtype is None:
                dtype = float
            sshape = self.shape
            sshape = sshape[:axis] + sshape[axis+1:]
            ishape = [1] * self._ndim
            ishape[axis] = self._shape[axis]
            if out is None:
                out = zeros(sshape, dtype)
            si = self.section_iter(ishape)
            oi = out.item_iter()
            while si.has_next():
                ss = si.next()
                oi.set_next(ss.std(ddof = ddof))
            return out
        elif hasattr(axis, '__iter__'):
            axis = list(axis)
            if dtype is None:
                dtype = float
            s = self.shape
            sshape = []
            ishape = [1] * self._ndim
            for i in xrange(self._ndim):
                if i in axis:
                    ishape[i] = s[i]
                else:
                    sshape.append(s[i])
            if out is None:
                out = zeros(sshape, dtype)
            si = self.section_iter(ishape)
            oi = out.item_iter()
            while si.has_next():
                ss = si.next()
                oi.set_next(ss.std(ddof = ddof))
            return out
        else:
            raise ValueError, 'invalid axis value, must be int or tuple of int'
            
    def variance(self, axis=None, dtype=None, out=None, ddof=0):
        if axis is None:
            n = self.size
            if n < 2:
                raise ValueError('variance requires at least two data points')
            if n == ddof:
                raise ValueError('invalid ddof, ddof can not be the size of the array')
            ss = self.sqr_dev()
            pvar = ss / (n - ddof)
            if not out is None:
                out[:] = pvar
            else:
                out = pvar
            return out
        elif type(axis) is int:
            if dtype is None:
                dtype = float
            sshape = self.shape
            sshape = sshape[:axis] + sshape[axis+1:]
            ishape = [1] * self._ndim
            ishape[axis] = self._shape[axis]
            if out is None:
                out = zeros(sshape, dtype)
            si = self.section_iter(ishape)
            oi = out.item_iter()
            while si.has_next():
                ss = si.next()
                oi.set_next(ss.variance(ddof = ddof))
            return out
        elif hasattr(axis, '__iter__'):
            axis = list(axis)
            if dtype is None:
                dtype = float
            s = self.shape
            sshape = []
            ishape = [1] * self._ndim
            for i in xrange(self._ndim):
                if i in axis:
                    ishape[i] = s[i]
                else:
                    sshape.append(s[i])
            if out is None:
                out = zeros(sshape, dtype)
            si = self.section_iter(ishape)
            oi = out.item_iter()
            while si.has_next():
                ss = si.next()
                oi.set_next(ss.variance(ddof = ddof))
            return out
        else:
            raise ValueError, 'invalid axis value, must be int or tuple of int'

    def all(self, axis = None):
        if axis is None or self.ndim == 1:
            siter = self.item_iter()
            try :
                while True :
                    if not siter.next() :
                        return False
            except :
                pass
            return True
        else:
            if type(axis) is int:
                axis = (axis,)
            if len(axis) == self._ndim:
                return self.all()
            ish = self.shape
            rsh = []
            for i in xrange(self.ndim):
                if not i in axis:
                    rsh.append(ish[i])
                    ish[i] = 1
            res = zeros(rsh, dtype = bool)
            ri = res.item_iter()
            si = self.section_iter(ish)
            while ri.has_next():
                se = si.next()
                ri.set_next(se.all())
            return res
    
    def any(self, axis = None):
        if axis is None or self.ndim == 1:
            siter = self.item_iter()
            try :
                while True :
                    if siter.next() :
                        return True
            except :
                pass
            return False
        else:
            if type(axis) is int:
                axis = (axis,)
            if len(axis) == self._ndim:
                return self.all()
            ish = self.shape
            rsh = []
            for i in xrange(self.ndim):
                if not i in axis:
                    rsh.append(ish[i])
                    ish[i] = 1
            res = zeros(rsh, dtype = bool)
            ri = res.item_iter()
            si = self.section_iter(ish)
            while ri.has_next():
                se = si.next()
                ri.set_next(se.any())
            return res
    
    def round(self, decimals=0, out=None):
        if out is None:
            out = zeros(self.shape, self.dtype)
        else:
            if out.size != self.size :
                raise ValueError('out size needs to match')
        si = self.item_iter()
        oi = out.item_iter()
        try:
            while True:
                oi.set_next(round(si.next(), decimals))
        except StopIteration:
            pass
        return out
        
    def __add__(self, obj):
        if hasattr(obj, '__len__') :
            if hasattr(obj, '__iArray__') :
                if obj.shape == [1]:
                    return Array(self.__iArray__.getArrayMath().toAdd(obj[0]).getArray())
                iarr = obj.__iArray__
            else :
                iarr = Array(obj).__iArray__
            return Array(self.__iArray__.getArrayMath().toAdd(iarr).getArray())
        else :
            return Array(self.__iArray__.getArrayMath().toAdd(float(obj)).getArray())

#        if isinstance(obj, Array) :
#            return Array(self.__iArray__.getArrayMath().toAdd(
#                                obj.__iArray__).getArray());
#        res = zeros(self._shape, self.__match_type__(obj))
#        riter = res.item_iter()
#        siter = self.item_iter()
#        if hasattr(obj, '__len__') :
#            if len(obj) < self._size :
#                raise Exception, 'resource should have at least ' + \
#                    str(self._size) + ' items, got ' + str(len(obj))
#            try :
#                rawshape = get_shape(obj)
#                for id in xrange(get_size(obj, rawshape)) :
#                    riter.set_next(siter.next() + get_item(obj, id, rawshape))
#            except StopIteration:
#                pass
#        else :
#            try :
#                while True :
#                    riter.set_next(siter.next() + obj)
#            except StopIteration:
#                pass
#        return res

    def __iadd__(self, obj):
        if hasattr(obj, '__len__') :
            if hasattr(obj, '__iArray__') :
                if obj.shape == [1]:
                    self.__iArray__.getArrayMath().add(obj[0])
                    return self
                iarr = obj.__iArray__
            else :
                iarr = Array(obj).__iArray__
            self.__iArray__.getArrayMath().add(iarr)
            return self
        else :
            self.__iArray__.getArrayMath().add(float(obj))
            return self
#        siter = self.item_iter()
#        if hasattr(obj, '__len__') :
#            if len(obj) < self._size :
#                raise Exception, 'resource should have at least ' + \
#                    str(self._size) + ' items, got ' + str(len(obj))
#            try :
#                rawshape = get_shape(obj)
#                for id in xrange(get_size(obj, rawshape)) :
#                    siter.set_curr(siter.next() + get_item(obj, id, rawshape))
#            except StopIteration:
#                pass
#        else :
#            try :
#                while True :
#                    siter.set_curr(siter.next() + obj)
#            except StopIteration:
#                pass
#        return self
    
    def __radd__(self, obj):
        return self.__add__(obj)
            
    def __div__(self, obj):
        if hasattr(obj, '__len__') :
            if hasattr(obj, '__iArray__') :
                if obj.shape == [1]:
                    return Array(self.__iArray__.getArrayMath().toScale(1.0 / obj[0]).getArray())
                iarr = obj.__iArray__
            else :
                iarr = Array(obj).__iArray__
            return Array(self.__iArray__.getArrayMath().toEltDivide(iarr).getArray())
        else :
            return Array(self.__iArray__.getArrayMath().toScale(1.0/obj).getArray())

    def __floordiv__(self, obj):
        if hasattr(obj, '__len__') :
            if hasattr(obj, '__iArray__') :
                if obj.shape == [1]:
                    return self.__floordiv__(obj[0])
            else :
                obj = Array(obj)
            res = instance(self._shape, dtype = self._dtype)
            si = self.item_iter()
            oi = obj.item_iter()
            ri = res.item_iter()
            try:
                while True:
                    ri.set_next(si.next() // oi.next())
            except StopIteration:
                pass
            return res
        else :
            res = instance(self._shape, dtype = self._dtype)
            si = self.item_iter()
            ri = res.item_iter()
            try:
                while True:
                    ri.set_next(si.next() // obj)
            except StopIteration:
                pass
            return res

    def __idiv__(self, obj):
        if hasattr(obj, '__len__') :
            if hasattr(obj, '__iArray__') :
                if obj.shape == [1]:
                    self.__iArray__.getArrayMath().scale(1.0 / obj[0])
                    return self
                iarr = obj.__iArray__
            else :
                iarr = Array(obj).__iArray__
            self.__iArray__.getArrayMath().eltDivide(iarr)
            return self
        else :
            self.__iArray__.getArrayMath().scale(1.0/obj)
            return self
    
    def __ifloordiv__(self, obj):
        if hasattr(obj, '__len__') :
            if hasattr(obj, '__iArray__') :
                if obj.shape == [1]:
                    return self.__ifloordiv__(obj[0])
            else :
                obj = Array(obj)
            si = self.item_iter()
            oi = obj.item_iter()
            try:
                while True:
                    si.set_curr(si.next() // oi.next())
            except StopIteration:
                pass
            return self
        else :
            si = self.item_iter()
            try:
                while True:
                    si.set_curr(si.next() // obj)
            except StopIteration:
                pass
            return self
    
    def __rdiv__(self, obj):
        if hasattr(obj, '__len__') :
            if len(obj) < self._size :
                raise Exception, 'resource should have at least ' + \
                    str(self._size) + ' items, got ' + str(len(obj))
            res = zeros(self._shape, self.__match_type__(obj, True))
            riter = res.item_iter()
            siter = self.item_iter()
            oiter = obj.item_iter()
            try :
                while True:
                    nval = siter.next()
                    if nval != 0 :
                        riter.set_next(oiter.next() / nval)
                    else :
                        riter.next()
                        oiter.next()
            except StopIteration:
                pass
        else :
            res = zeros(self._shape, self.__match_type__(obj, True))
            riter = res.item_iter()
            siter = self.item_iter()
            try :
                while True :
                    nval = siter.next()
                    if nval != 0 :
                        riter.set_next(obj / nval)
                    else :
                        riter.next()
            except StopIteration:
                pass
        return res
    
    def __mul__(self, obj):
        if hasattr(obj, '__len__') :
            if hasattr(obj, '__iArray__') :
                if obj.shape == [1]:
                    return Array(self.__iArray__.getArrayMath().toScale(obj[0]).getArray())
                iarr = obj.__iArray__
            else :
                iarr = Array(obj).__iArray__
            return Array(self.__iArray__.getArrayMath().toEltMultiply(iarr).getArray())
        else :
            return Array(self.__iArray__.getArrayMath().toScale(float(obj)).getArray())
#        res = zeros(self._shape, self.__match_type__(obj))
#        riter = res.item_iter()
#        siter = self.item_iter()
#        if hasattr(obj, '__len__') :
#            if len(obj) < self._size :
#                raise Exception, 'resource should have at least ' + \
#                    str(self._size) + ' items, got ' + str(len(obj))
#            try :
#                rawshape = get_shape(obj)
#                for id in xrange(get_size(obj, rawshape)) :
#                    riter.set_next(siter.next() * get_item(obj, id, rawshape))
#            except StopIteration:
#                pass
#        else :
#            try :
#                while True :
#                    riter.set_next(siter.next() * obj)
#            except StopIteration:
#                pass
#        return res
    
    def __imul__(self, obj):
        if hasattr(obj, '__len__') :
            if hasattr(obj, '__iArray__') :
                if obj.shape == [1]:
                    self.__iArray__.getArrayMath().scale(obj[0])
                    return self
                iarr = obj.__iArray__
            else :
                iarr = Array(obj).__iArray__
            self.__iArray__.getArrayMath().eltMultiply(iarr)
            return self
        else :
            self.__iArray__.getArrayMath().scale(float(obj))
            return self
    
    def __rmul__(self, obj):
        return self.__mul__(obj)
        
    def __neg__(self):
        out = zeros(self._shape, self._dtype)
        si = self.item_iter()
        oi = out.item_iter()
        try:
            while True:
                oi.set_next(-si.next())
        except StopIteration:
            pass
        return out
    
    def __pos__(self):
        out = instance(shape, dtype = self._dtype)
        si = self.item_iter()
        oi = out.item_iter()
        try:
            while True:
                oi.set_next(si.next())
        except StopIteration:
            pass
        return out
        
    def __sub__(self, obj):
        return self.__add__(obj * -1)
#        res = zeros(self._shape, self.__match_type__(obj))
#        riter = res.item_iter()
#        siter = self.item_iter()
#        if hasattr(obj, '__len__') :
#            if len(obj) < self._size :
#                raise Exception, 'resource should have at least ' + \
#                    str(self._size) + ' items, got ' + str(len(obj))
#            try :
#                rawshape = get_shape(obj)
#                for id in xrange(get_size(obj, rawshape)) :
#                    riter.set_next(siter.next() - get_item(obj, id, rawshape))
#            except StopIteration:
#                pass
#        else :
#            try :
#                while True :
#                    riter.set_next(siter.next() - obj)
#            except StopIteration:
#                pass
#        return res
    
    def __isub__(self, obj):
        self.__iadd__(obj * -1)
        return self
    
    def __rsub__(self, obj):
        res = zeros(self._shape, self.__match_type__(obj))
        riter = res.item_iter()
        siter = self.item_iter()
        if hasattr(obj, '__len__') :
            if not isinstance(obj, Array):
                obj = Array(obj)
            if obj.size < self._size :
                raise Exception, 'resource should have at least ' + \
                    str(self._size) + ' items, got ' + str(len(obj))
            oiter = obj.item_iter()
            try :
                while True:
                    riter.set_next(oiter.next() - siter.next())
            except StopIteration:
                pass
        else :
            try :
                while True :
                    riter.set_next(obj - siter.next())
            except StopIteration:
                pass
        return res
    
    def __invert__(self):
#        return Array(self.__iArray__.getArrayMath().toEltInverse().getArray())
        if self._dtype is bool :
            res = instance(self._shape, dtype = bool)
            siter = self.item_iter()
            riter = res.item_iter()
            while siter.has_next() :
                riter.set_next(not siter.next())
            return res
        else :
            res = instance(self._shape, dtype = self._dtype)
            siter = self.item_iter()
            riter = res.item_iter()
            while siter.has_next() :
                riter.set_next(~siter.next())
            return res
            
    def __abs__(self):
        out = instance(shape, dtype = self._dtype)
        si = self.item_iter()
        oi = out.item_iter()
        try:
            while True:
                oi.set_next(abs(si.next()))
        except StopIteration:
            pass
        return out
        
    def matrix_invert(self):
        if self._ndim != 2:
            raise Exception, 'array dimention must be 2, got ' + str(self._ndim)
        if self._shape[0] != self._shape[1]:
            raise Exception, 'array is not square '
        return Array(self.__iArray__.getArrayMath().matInverse().getArray())
    
    def matrix_dot(self, arr):
        if not hasattr(arr, 'ndim'):
            raise Exception, 'argument arr must be a gumpy array'
        if self._ndim != 2:
            raise Exception, 'array dimention must be 2, got ' + str(self._ndim)
        if arr.ndim != 2:
            raise Exception, 'argument array dimention must be 2, got ' + str(arr.ndim)
        if self._shape[1] != arr.shape[0]:
            raise Exception, 'array dimensions do not match'
        return Array(self.__iArray__.getArrayMath().matMultiply(arr.__iArray__).getArray())
    
    def dot(self, b, out = None):
        if not hasattr(b, 'ndim'):
            if out is None:
                return self.__mul__(b)
            else:
                si = self.item_iter()
                oi = out.item_iter()
                oi.set_next(si.next() * b)
                return out
        elif self.ndim == 1 and b.ndim == 1:
            if out is None:
                return self.__mul__(b)
            else:
                si = self.item_iter()
                bi = self.item_iter()
                oi = out.item_iter()
                oi.set_next(si.next() * bi.next())
                return out
        elif self.ndim == 2 and b.ndim == 2:
            if out is None:
                return self.matrix_dot(b)
            else:
                out.copy_from(self.matrix_dot(b))
                return out
        elif b.ndim == 1:
            if self.shape[-1] != b.size:
                raise ValueError('size of b must match the size of the last dimension')
            oshape = self.shape[:-1]
            ishape = [1] * self.ndim
            ishape[-1] = self.shape[-1]
            if out is None:
                dtype = self.dtype
                if b.dtype == float:
                    dtype = float
                out = zeros(oshape, dtype)
            oi = out.item_iter()
            si = self.section_iter(ishape)
            while si.has_next():
                ss = si.next()
                oi.set_next((ss * b).sum())
            return out
        else:
            ashape = self.shape[:-1]
            bshape = b.shape
            bshape = bshape[:-2] + [bshape[-1]]
            oshape = ashape + bshape
            if out is None:
                dtype = self.dtype
                if b.dtype == float:
                    dtype = float
                out = zeros(oshape, dtype)
            aishape = [1] * self.ndim
            aishape[-1] = self.shape[-1]
            bishape = [1] * b.ndim
            bishape[-2] = b.shape[-2]
            ai = self.section_iter(aishape)
            oi = out.item_iter()
            while ai.has_next():
                ss = ai.next()
                bi = b.section_iter(bishape)
                while bi.has_next():
                    bs = bi.next()
                    ssi = ss.item_iter()
                    bsi = bs.item_iter()
                    s = 0
                    while ssi.has_next():
                        s += ssi.next() * bsi.next()
                    oi.set_next(s)
            return out
    
    def matmul(self, b, out = None, dtype = None):
        if not hasattr(b, 'ndim'):
            raise ValueError('matmul: Input operand 1 does not have enough dimensions ...')
        elif self.ndim == 1 and b.ndim == 1:
            if out is None:
                return self.__mul__(b)
            else:
                si = self.item_iter()
                bi = self.item_iter()
                oi = out.item_iter()
                oi.set_next(si.next() * bi.next())
                return out
        elif self.ndim == 2 and b.ndim == 2:
            if out is None:
                return self.matrix_dot(b)
            else:
                out.copy_from(self.matrix_dot(b))
                return out
        elif b.ndim == 1:
            if self.shape[-1] != b.size:
                raise ValueError('size of b must match the size of the last dimension')
            oshape = self.shape[:-1]
            ishape = [1] * self.ndim
            ishape[-1] = self.shape[-1]
            if out is None:
                dtype = self.dtype
                if b.dtype == float:
                    dtype = float
                out = zeros(oshape, dtype)
            oi = out.item_iter()
            si = self.section_iter(ishape)
            while si.has_next():
                ss = si.next()
                oi.set_next((ss * b).sum())
            return out
        else:
            s = self
            sdim = s._ndim
            bdim = b._ndim
            if sdim > bdim:
                b = broadcast_to(b, s.shape[:sdim - bdim] + b.shape)
                odim = sdim
            elif sdim < bdim:
                s = broadcast_to(s, b.shape[:bdim - sdim] + s.shape)
                odim = bdim
#             ashape = self.shape[:-1]
#             bshape = b.shape
#             bshape = bshape[:-2] + [bshape[-1]]
            oshape = s.shape[:sdim - 1] + [b.shape[-1]]
            if out is None:
                dtype = self.dtype
                if b.dtype == float:
                    dtype = float
                out = zeros(oshape, dtype)
            sishape = [1] * (s.ndim - 2) + s.shape[-2:]
            bishape = [1] * (b.ndim - 2) + b.shape[-2:]
            oishape = [1] * (out.ndim - 2) + out.shape[-2:]
            si = s.section_iter(sishape)
            bi = b.section_iter(bishape)
            oi = out.section_iter(oishape)
            while si.has_next():
                ss = si.next().get_reduced(range(s.ndim - 2))
                bs = bi.next().get_reduced(range(b.ndim - 2))
                os = oi.next().get_reduced(range(out.ndim - 2))
                ss.matmul(bs, os)
            return out
        
    def __pow__(self, obj):
        res = zeros(self._shape, self.__match_type__(obj))
        riter = res.item_iter()
        siter = self.item_iter()
        if hasattr(obj, '__len__') :
            if not isinstance(obj, Array):
                obj = Array(obj)
            if obj.size < self._size :
                raise Exception, 'resource should have at least ' + \
                    str(self._size) + ' items, got ' + str(len(obj))
            oiter = obj.item_iter()
            try :
                while True:
                    riter.set_next(siter.next() ** oiter.next())
            except StopIteration:
                pass
        else :
            try :
                while True :
                    riter.set_next(siter.next() ** obj)
            except StopIteration:
                pass
        return res

    def __ipow__(self, obj):
        siter = self.item_iter()
        if hasattr(obj, '__len__') :
            if not isinstance(obj, Array):
                obj = Array(obj)
            if obj.size < self._size :
                raise Exception, 'resource should have at least ' + \
                    str(self._size) + ' items, got ' + str(len(obj))
            oiter = obj.item_iter()
            try :
                while True:
                    siter.set_curr(siter.next() ** oiter.next())
            except StopIteration:
                pass
        else :
            try :
                while True :
                    siter.set_curr(siter.next() ** obj)
            except StopIteration:
                pass
        return self
        
    def exp(self):
        return Array(self.__iArray__.getArrayMath().toExp().getArray())

    def log10(self):
        return Array(self.__iArray__.getArrayMath().toLog10().getArray())
    
    def ln(self):
        return Array(self.__iArray__.getArrayMath().toLn().getArray())

    def sqrt(self):
        return Array(self.__iArray__.getArrayMath().toSqrt().getArray())
        
    def __rpow__(self, obj):
        res = zeros(self._shape, self.__match_type__(obj))
        riter = res.item_iter()
        siter = self.item_iter()
        if hasattr(obj, '__len__') :
            if not isinstance(obj, Array):
                obj = Array(obj)
            if obj.size < self._size :
                raise Exception, 'resource should have at least ' + \
                    str(self._size) + ' items, got ' + str(len(obj))
            oiter = obj.item_iter()
            try :
                while True:
                    riter.set_next(oiter.next() ** siter.next())
            except StopIteration:
                pass
        else :
            try :
                while True :
                    riter.set_next(obj ** siter.next())
            except StopIteration:
                pass
        return res
    
    def __mod__(self, obj):
        res = zeros(self._shape, self.__match_type__(obj, True))
        riter = res.item_iter()
        siter = self.item_iter()
        if hasattr(obj, '__len__') :
            if not isinstance(obj, Array):
                obj = Array(obj)
            if obj.size < self._size :
                raise Exception, 'resource should have at least ' + \
                    str(self._size) + ' items, got ' + str(len(obj))
            oiter = obj.item_iter()
            try :
                while True:
                    val = oiter.next()
                    if val != 0 :
                        riter.set_next(siter.next() % val)
                    else :
                        riter.next()
                        siter.next()
            except StopIteration:
                pass                    
        else :
            if obj != 0 :
                try :
                    while True :
                        riter.set_next(siter.next() % obj)
                except StopIteration:
                    pass
        return res

    def __imod__(self, obj):
        siter = self.item_iter()
        if hasattr(obj, '__len__') :
            if not isinstance(obj, Array):
                obj = Array(obj)
            if obj.size < self._size :
                raise Exception, 'resource should have at least ' + \
                    str(self._size) + ' items, got ' + str(len(obj))
            oiter = obj.item_iter()
            try :
                while True:
                    val = oiter.next()
                    if val != 0 :
                        siter.set_curr(siter.next() % val)
                    else :
                        siter.next()
            except StopIteration:
                pass                    
        else :
            if obj != 0 :
                try :
                    while True :
                        siter.set_curr(siter.next() % obj)
                except StopIteration:
                    pass
        return self
    
    def __rmod__(self, obj):
        res = zeros(self._shape, self.__match_type__(obj, True))
        riter = res.item_iter()
        siter = self.item_iter()
        if hasattr(obj, '__len__') :
            if not isinstance(obj, Array):
                obj = Array(obj)
            if obj.size < self._size :
                raise Exception, 'resource should have at least ' + \
                    str(self._size) + ' items, got ' + str(len(obj))
            oiter = obj.item_iter()
            try :
                while True:
                    nval = self.next()
                    if nval != 0 :
                        riter.set_next(oiter.next() % nval)
                    else :
                        riter.next()
                        oiter.next()
            except StopIteration:
                pass
        else :
            try :
                while True :
                    nval = siter.next()
                    if nval != 0 :
                        riter.set_next(obj % nval)
                    else :
                        riter.next()
            except StopIteration:
                pass
        return res

    def __divmod__(self, obj):
        rem = zeros(self._shape, self.__match_type__(obj, True))
        qt = zeros(self._shape, self.__match_type__(obj, True))
        riter = rem.item_iter()
        qiter = qt.item_iter()
        siter = self.item_iter()
        if hasattr(obj, '__len__') :
            if not isinstance(obj, Array):
                obj = Array(obj)
            if obj.size < self._size :
                raise Exception, 'resource should have at least ' + \
                    str(self._size) + ' items, got ' + str(len(obj))
            oiter = obj.item_iter()
            try :
                while True:
                    val = oiter.next()
                    if val != 0 :
                        q, r = divmod(siter.next(), val)
                        qiter.set_next(q)
                        riter.set_next(r)
                    else :
                        qiter.next()
                        riter.next()
                        siter.next()
            except StopIteration:
                pass                    
        else :
            if obj != 0 :
                try :
                    while True :
                        q, r = divmod(siter.next(), obj)
                        qiter.set_next(q)
                        riter.set_next(r)
                except StopIteration:
                    pass
        return (qt, rem)
    
    def __sin__(self):
        return Array(self.__iArray__.getArrayMath().toSin().getArray())
    
    def __cos__(self):
        return Array(self.__iArray__.getArrayMath().toCos().getArray())
    
    def __tan__(self):
        return Array(self.__iArray__.getArrayMath().toTan().getArray())
    
    def __arcsin__(self):
        return Array(self.__iArray__.getArrayMath().toAsin().getArray())
    
    def __arccos__(self):
        return Array(self.__iArray__.getArrayMath().toAcos().getArray())
    
    def __arctan__(self):
        return Array(self.__iArray__.getArrayMath().toAtan().getArray())
    
    def __exp__(self):
        return Array(self.__iArray__.getArrayMath().toExp().getArray())

    def __prod__(self, axis = None):
        if self._size is 0 :
            return 0
        res = 1
        siter = self.item_iter()
        try :
            while True :
                res *= siter.next()
        except StopIteration:
            pass
        return res
    
#     def prod(self, axis=None, dtype=None, out=None, initial=1):
#         if self._size is 0 :
#             return 0
#         res = initial
#         siter = self.item_iter()
#         try :
#             while True :
#                 res *= siter.next()
#         except StopIteration:
#             pass
#         return res

    def prod(self, axis=None, dtype=None, out=None, initial=1):
        if axis is None :
            if self._size is 0 :
                return initial
            res = initial
            siter = self.item_iter()
            try :
                while True :
                    res *= siter.next()
            except StopIteration:
                pass
            return res
        else :
            if axis >= self._ndim :
                raise ValueError, 'index out of bound, ' + str(axis) + ' in ' + str(self._ndim)
            elif axis < 0:
                axis = self._ndim + axis
            if self._ndim == 1:
                return self.prod(dtype = dtype, initial = initial)
            nshape = self.shape
            nshape = nshape[:axis] + nshape[axis + 1:]
            ishape = [1] * self._ndim
            ishape[axis] = self.shape[axis]
            si = self.section_iter(ishape)
            if dtype is None:
                dtype = self._dtype
            if out is None :
                out = instance(nshape, 0, dtype)
            oi = out.item_iter()
            while oi.has_next():
                ss = si.next()
                oi.set_next(ss.prod(initial = initial))
            return out
        
    def max(self, axis = None, out = None, initial = None):
        if axis is None :
            if initial is None:
                return self.__iArray__.getArrayMath().getMaximum()
            else:
                m = self.__iArray__.getArrayMath().getMaximum()
                return m if m > initial else initial
        else :
            if axis >= self._ndim :
                raise ValueError, 'axis index out of stack, ' + str(axis) + \
                                    ' in ' + str(self._ndim)
            elif axis < 0:
                axis = self._ndim + axis
            asize = self._shape[axis]
            if out is None :
                out = instance([asize], 0, self._dtype)
                for id in xrange(asize) :
                    out[id] = self.get_slice(axis, id).max(initial = initial)
            return out
    
    def amax(self, axis = None, out = None, initial = None):
        if axis is None :
            if initial is None:
                return self.__iArray__.getArrayMath().getMaximum()
            else:
                m = self.__iArray__.getArrayMath().getMaximum()
                return m if m > initial else initial
        else :
            if axis >= self._ndim :
                raise ValueError, 'axis index out of stack, ' + str(axis) + \
                                    ' in ' + str(self._ndim)
            elif axis < 0:
                axis = self._ndim + axis
            nshape = self.shape
            nshape = nshape[:axis] + nshape[axis + 1:]
            ishape = [1] * self._ndim
            ishape[axis] = self.shape[axis]
            si = self.section_iter(ishape)
            if out is None :
                out = instance(nshape, 0, self._dtype)
            oi = out.item_iter()
            while oi.has_next():
                ss = si.next()
                oi.set_next(ss.amax(initial = initial))
            return out
    
    def min(self, axis = None, out = None, initial = None):
        if axis is None :
            if initial is None:
                return self.__iArray__.getArrayMath().getMinimum()
            else:
                m = self.__iArray__.getArrayMath().getMinimum()
                return m if m < initial else initial
        else :
            if axis >= self._ndim :
                raise ValueError, 'axis index out of stack, ' + str(axis) + \
                                    ' in ' + str(self._ndim)
            elif axis < 0:
                axis = self._ndim + axis
            asize = self._shape[axis]
            if out is None :
                out = instance([asize], 0, self._dtype)
                for id in xrange(asize) :
                    out[id] = self.get_slice(axis, id).min(initial = initial)
            return out
    
    def amin(self, axis = None, out = None, initial = None):
        if axis is None :
            if initial is None:
                return self.__iArray__.getArrayMath().getMinimum()
            else:
                m = self.__iArray__.getArrayMath().getMinimum()
                return m if m < initial else initial
        else :
            if axis >= self._ndim :
                raise ValueError, 'axis index out of stack, ' + str(axis) + \
                                    ' in ' + str(self._ndim)
            elif axis < 0:
                axis = self._ndim + axis
            nshape = self.shape
            nshape = nshape[:axis] + nshape[axis + 1:]
            ishape = [1] * self._ndim
            ishape[axis] = self.shape[axis]
            si = self.section_iter(ishape)
            if out is None :
                out = instance(nshape, 0, self._dtype)
            oi = out.item_iter()
            while oi.has_next():
                ss = si.next()
                oi.set_next(ss.amin(initial = initial))
            return out

    def ptp(self, axis=None, out=None):
        if axis is None:
            min = float("inf")
            max = float("-inf")
            si = self.item_iter()
            try:
                while True:
                    v = si.next()
                    if v < min:
                        min = v
                    if v > max:
                        max = v
            except StopIteration:
                pass
            return max - min
        else:
            if type(axis) is int:
                if axis >= self._ndim:
                    raise ValueError('axis out of range')
                if axis < 0:
                    axis = self._ndim + axis
                nshape = self.shape
                nshape = nshape[:axis] + nshape[axis + 1:]
                ishape = [1] * self._ndim
                ishape[axis] = self.shape[axis]
                si = self.section_iter(ishape)
                if out is None :
                    out = instance(nshape, 0, self._dtype)
                oi = out.item_iter()
                while oi.has_next():
                    ss = si.next()
                    oi.set_next(ss.ptp())
                return out
            elif hasattr(axis, '__iter__'):
                axis = list(axis)
                for i in xrange(len(axis)):
                    if axis[i] >= self._ndim:
                        raise ValueError('axis out of range')
                    elif axis[i] < 0:
                        axis[i] = self._ndim + axis
                s = self.shape
                nshape = []
                ishape = [1] * self._ndim
                for i in xrange(self._ndim):
                    if not i in axis:
                        nshape.append(s[i])
                    else:
                        ishape[i] = s[i]
                si = self.section_iter(ishape)
                if out is None :
                    out = instance(nshape, 0, self._dtype)
                oi = out.item_iter()
                while oi.has_next():
                    ss = si.next()
                    oi.set_next(ss.ptp())
                return out
            else:
                raise ValueError('invalid axis argument')
                
        
    def asum(self, axis=None, dtype=None, out=None, initial = 0):
        if axis is None :
            s = initial + self.__iArray__.getArrayMath().sum()
            if dtype is int :
                return int(s)
            elif dtype is long :
                return long(s)
            else :
                return s
        else :
            if axis >= self._ndim :
                raise ValueError, 'index out of bound, ' + str(axis) + ' in ' + str(self._ndim)
#             nsize = self._shape[axis]
#             if out is None :
#                 out = instance([nsize], 0, dtype = dtype)
#             for i in xrange(nsize) :
#                 out[i] = self.get_slice(axis, i).sum(dtype = dtype)
#             return out

            elif axis < 0:
                axis = self._ndim + axis
            nshape = self.shape
            nshape = nshape[:axis] + nshape[axis + 1:]
            ishape = [1] * self._ndim
            ishape[axis] = self.shape[axis]
            si = self.section_iter(ishape)
            if out is None :
                out = instance(nshape, 0, self._dtype)
            oi = out.item_iter()
            while oi.has_next():
                ss = si.next()
                oi.set_next(ss.asum(initial = initial))
            return out
    
    def sum(self, axis=None, dtype=None, out=None, initial=0):
        if axis is None :
            s = initial + self.__iArray__.getArrayMath().sum()
            if dtype is int :
                return int(s)
            elif dtype is long :
                return long(s)
            else :
                return s
        else :
            if axis >= self._ndim :
                raise ValueError, 'index out of bound, ' + str(axis) + ' in ' + str(self._ndim)
            nsize = self._shape[axis]
            if out is None :
                out = instance([nsize], 0, dtype = dtype)
            for i in xrange(nsize) :
                out[i] = self.get_slice(axis, i).sum(dtype = dtype, initial = initial)
            return out

    def searchsorted(self, v, side='left', sorter=None):
        if hasattr(v, '__iter__'):
            out = zeros([len(v)], int)
            for i in xrange(len(v)):
                out[i] = self.searchsorted(v[i], side, sorter)
            return out
        else:
            si = self.item_iter()
            idx = 0
            if side is None:
                side = 'left'
            if side == 'left' or side.lower().startswith('l'):
                if Double.isNaN(v):
                    try:
                        while True:
                            nv = si.next()
                            if Double.isNaN(nv) :
                                return idx
                            else:
                                idx += 1
                    except StopIteration:
                        pass
                else:
                    try:
                        while True:
                            nv = si.next()
                            if nv >= v:
                                return idx
                            else:
                                idx += 1
                    except StopIteration:
                        pass
                return idx
            elif side == 'right' or side.lower().startswith('r'):
                if Double.isNaN(v):
                    try:
                        while True:
                            nv = si.next()
                            if Double.isNaN(nv) :
                                return idx + 1
                            else:
                                idx += 1
                    except StopIteration:
                        pass
                else:
                    try:
                        while True:
                            nv = si.next()
                            if nv > v:
                                return idx
                            else:
                                idx += 1
                    except StopIteration:
                        pass
                    return idx
            else:
                raise ValueError, 'invalid side parameter'
            
    def sort(self, axis=-1, reverse=False):
        if axis is None:
            f = self.flatten()
            r = sorted(f)
            self[:] = r
            return
        if axis < 0:
            axis = self._ndim + axis
        if axis < 0 or axis >= self._ndim:
            raise ValueError, 'axis out of range'
        sshape = [1] * self._ndim
        sshape[axis] = self.shape[axis]
        si = self.section_iter(sshape)
        while si.has_next():
            ss = si.next().get_reduced()
            r = sorted(ss, reverse = reverse)
            ss[:] = r
        
#********************************************************************************
#     Array utilities
#********************************************************************************
    
    def __repr__(self, indent = None, skip = True, precision = None):
        if precision is None:
            precision = Array.precision
        if self._size == 0 :
            return 'Array([], shape=' + str(self._shape) + \
                    ', dtype=' + self._dtype.__name__ + ')'
        if skip :
            skip = self._size > Array.threshold
        amax = 0
        amin = 0
        if self._dtype is int or self._dtype is long :
            amax = self.max()
            amin = self.min()
            if amax < 0 :
                amax = -amin
        if indent is None :
            nindent = ' ' * 6
        else :
            nindent = indent + ' ' * 6;
        return 'Array(' + self.__string__(0, skip, True, indent = nindent, \
                                          max = amax, min = amin, precision = precision) + ')'
    
    def __str__(self, indent = '', skip = True):
        if self._dtype is str :
            return self.__iArray__.toString()
        if self._size == 0 :
            return indent + '[]'
        if skip :
            skip = self._size > Array.threshold
        amax = 0
        amin = 0
        if self._dtype is int or self._dtype is long :
            amax = self.max()
            amin = self.min()
            if amax < 0 :
                amax = -amin
        return self.__string__(0, skip, max = amax, min = amin, indent = indent)
    
    def __string__(self, level, skip = False, sep = False, indent = '', 
                   max = 0, min = 0, precision = None):
        if precision is None:
            precision = Array.precision
        if self._ndim > 1 :
            cmark = ''
            if sep :
                cmark += ','
            cmark += '\n' + indent
            if self._ndim >= 3 :
                cmark += '\n' + indent
            cmark += ' ' * (level + 1)
            abbrv = '...,'
        elif sep or self._dtype is float :
            cmark = ', '
            abbrv = '...'
        else :
            cmark = ' '
            abbrv = '...,'
        close = ']'
        result = '['
        fm = ''
        if self._dtype is int or self._dtype is long:
            if max == 0 :
                max = 1
            sign = 0
            if min < 0 :
                sign = 1
            fm = '%#' + str(int(math.ceil(math.log10(max))) + sign) + 'i'
        elif self._dtype is float :
            fm = '%#.' + str(precision) + 'f'
        else :
            fm = '%s'
        size = self._shape[0]
        if self._ndim <= 1 :
            if skip and size > 6:
                result += (fm % self[0]) + \
                    cmark + (fm % self[1]) + \
                    cmark + (fm % self[2]) + \
                    cmark + abbrv + cmark + (fm % self[size - 3]) + \
                    cmark + (fm % self[size - 2]) + \
                    cmark + (fm % self[size - 1]) + close
            else :
                for i in xrange(size - 1) :
#                    result += str(self[i]) + cmark
                    result += (fm % self[i]) + cmark
                result += (fm % self[size - 1]) + close
            return result
        else :
            if skip and size > 6:
                result += self[0].__string__(level + 1, skip, sep, indent, max, min, precision) + \
                    cmark + self[1].__string__(level + 1, skip, sep, indent, max, min, precision) + \
                    cmark + self[2].__string__(level + 1, skip, sep, indent, max, min, precision) + \
                    cmark + abbrv + cmark + \
                            self[size - 3].__string__(level + 1, skip, sep, indent, max, min, precision) + \
                    cmark + self[size - 2].__string__(level + 1, skip, sep, indent, max, min, precision) + \
                    cmark + self[size - 1].__string__(level + 1, skip, sep, indent, max, min, precision) + close
            else :
                for i in xrange(size - 1) :
#                    if i > 0 :
#                        result += '\t'
                    result += self[i].__string__(level + 1, skip, sep,  indent, max, min, precision) + cmark
                result += self[size - 1].__string__(level + 1, skip, sep, indent, max, min, precision) + close
            return result        
    
    def tolist(self):
        l = []
        for part in self :
            if isinstance(part, Array) :
                l.append(part.tolist())
            else :
                l.append(part)
        return l
            
#####################################################################################
# Returns the indices of the maximum values along an axis.
# Parameters:    
#     axis : int, optional
#        By default, the index is into the flattened array, otherwise along the specified axis.
#
# Returns:    
#    index_array : ndarray of ints
#    Array of indices into the array. It has the same shape as a.shape with the dimension along axis removed.
#####################################################################################
    def argmax(self, axis = None):
        if axis is None:
            val = float("-inf")
            found = -1
            iter = self.item_iter()
            for idx in xrange(self.size):
                nv = iter.next()
                if nv > val:
                    val = nv
                    found = idx
                idx += 1
            return found
        else:
            if axis >= self._ndim:
                raise Exception, 'axis out of range'
            if self._ndim == 1:
                return self.argmax()
            res_shape = self.shape
            res_shape.pop(axis)
            res = zeros(res_shape, int)
            res_iter = res.item_iter()
            sec_iter_shape = [1] * self._ndim
            sec_iter_shape[axis] = self._shape[axis]
            sec_iter = self.section_iter(sec_iter_shape)
            while sec_iter.has_next():
                sec = sec_iter.next()
                res_iter.set_next(sec.argmax())
            return res
        
#####################################################################################
# Returns the indices of the minimum values along an axis.
# Parameters:    
#     axis : int, optional
#        By default, the index is into the flattened array, otherwise along the specified axis.
#
# Returns:    
#    index_array : ndarray of ints
#    Array of indices into the array. It has the same shape as a.shape with the dimension along axis removed.
#####################################################################################
    def argmin(self, axis = None):
        if axis is None:
            val = float("inf")
            found = -1
            idx = 0
            iter = self.item_iter()
            for idx in xrange(self.size):
                nv = iter.next()
                if nv < val:
                    val = nv
                    found = idx
            return found
        else:
            if axis >= self._ndim:
                raise Exception, 'axis out of range'
            if self._ndim == 1:
                return self.argmin()
            res_shape = self.shape
            res_shape.pop(axis)
            res = zeros(res_shape, int)
            res_iter = res.item_iter()
            sec_iter_shape = [1] * self._ndim
            sec_iter_shape[axis] = self._shape[axis]
            sec_iter = self.section_iter(sec_iter_shape)
            while sec_iter.has_next():
                sec = sec_iter.next()
                res_iter.set_next(sec.argmin())
            return res
        
#####################################################################################
#   Array modification
#####################################################################################    

    def __setitem__(self, index, value):
        if index is Ellipsis:
            self.fill(value)
        elif type(index) is int :
            if self._ndim == 1 :
                self.set_value(index, value);
            else :
                self.get_slice(0, index).copy_from(value)
        elif type(index) is slice :
            self.__getitem__(index).copy_from(value)
        elif type(index) is tuple or type(index) is list :
            secflag = 0
            if len(index) == self._ndim :
                for val in index :
                    if not type(val) is int :
                        secflag = 1
            else :
                secflag = 1
            if secflag :
                self.__getitem__(index).copy_from(value)
            else :
                l = []
                for val in index :
                    l.append(val)
                self.set_value(l, value)
        elif hasattr(index, 'ndim') :
            if index.dtype is bool :
                if hasattr(value, '__len__') :
                    self.__iArray__.getArrayMath().setValue(index.__iArray__, value.__iArray__)
                else :
                    self.__iArray__.getArrayMath().setValue(index.__iArray__, value)
            else :
                raise Exception, 'not supported'
        else :
            raise Exception, 'not supported'
    
    def astype(self, dtype):
        if self.dtype == dtype:
            return self
        else:
            out = instance(self.shape, dtype = dtype)
            out.copy_from(self)
            return out
    
    def clip(self, min=None, max=None, out=None):
        if Double.isNaN(min) and Double.isNaN(max):
            if out is None:
                return self
            else:
                out.copy_from(self)
                return out
        if out is None:
            out = zeros(self.shape, self.dtype)
        if Double.isNaN(min):
            si = self.item_iter()
            oi = out.item_iter()
            for i in xrange(self.size):
                n = si.next()
                if n > max:
                    oi.set_next(max)
                else:
                    oi.set_next(n)
        elif Double.isNaN(max):
            si = self.item_iter()
            oi = out.item_iter()
            for i in xrange(self.size):
                n = si.next()
                if n < min:
                    oi.set_next(min)
                else:
                    oi.set_next(n)
        else:
            si = self.item_iter()
            oi = out.item_iter()
            for i in xrange(self.size):
                n = si.next()
                if n > max:
                    oi.set_next(max)
                elif n < min:
                    oi.set_next(min)
                else:
                    oi.set_next(n)
        return out

    def copy_from(self, value, length = -1, where = True):
        if where is True:
            if value.__class__ is Array :
                Utilities.copyTo(value.__iArray__, self.__iArray__, length)
            elif not hasattr(value, '__len__'):
                self.fill(value)
            else :
    #            try :
    #                get_item(value, self._size - 1)
    #            except :
    #                raise Exception, 'resource should have at least ' + \
    #                    str(self._size) + ' items'
                siter = self.item_iter()
                if length <= 0 :
                    length = self._size
                elif length > self._size :
                    length = self._size
                for id in xrange(length) :
                    siter.set_next(get_item(value, id))
        else:
            if value.__class__ is Array:
                if where.__class__ is Array:
                    siter = self.item_iter()
                    fiter = value.item_iter()
                    witer = where.item_iter()
                    if length <= 0 :
                        length = self._size
                    elif length > self._size :
                        length = self._size
                    for id in xrange(length) :
                        if witer.next():
                            siter.set_next(fiter.next())
                        else:
                            siter.next()
                            fiter.next()
                else:
                    siter = self.item_iter()
                    fiter = value.item_iter()
                    if length <= 0 :
                        length = self._size
                    elif length > self._size :
                        length = self._size
                    for id in xrange(length) :
                        if get_item(where, id):
                            siter.set_next(fiter.next())
                        else:
                            siter.next()
                            fiter.next()
            elif type(value) is int or type(value) is float or type(value) is long :
                if where.__class__ is Array:
                    siter = self.item_iter()
                    witer = where.item_iter()
                    if length <= 0 :
                        length = self._size
                    elif length > self._size :
                        length = self._size
                    for id in xrange(length) :
                        if witer.next():
                            siter.set_next(get_item(value, id))
                        else:
                            siter.next()
                else:
                    siter = self.item_iter()
                    if length <= 0 :
                        length = self._size
                    elif length > self._size :
                        length = self._size
                    for id in xrange(length) :
                        if get_item(where, id):
                            siter.set_next(get_item(value, id))
                        else:
                            siter.next()
                    
                    
    def fill(self, val):
        if hasattr(val, '__len__'):
            if self.size == get_size(val):
                self.copy_from(val)
                return
            shape = get_shape(val)
            si = self.section_iter(shape)
            while si.has_next():
                s = si.next()
                s[:] = val
        else:
            siter = self.item_iter()
            try :
                while True :
                    siter.set_next(val)
            except StopIteration :
                pass
            
    def put(self, indices, values, mode='raise') :
#        v1 = self.view_1d()
        if type(indices) is int :
            if indices >= self._size :
                if mode == 'clip' :
                    indices = self._size - 1
                elif mode == 'wrap' :
                    indices %= self._size
                else :
                    raise ValueError, 'index out of range: ' + str(indices) + ' in ' \
                            + str(self._size)
            if indices < 0 :
                indices += self._size
                if indices < 0 :
                    if mode == 'clip' :
                        indices = 0
                    elif mode == 'wrap' :
                        indices %= self._size
                    else :
                        raise ValueError, 'index out of range: ' + str(indices - self._size) \
                            + ' in ' + str(self._size)
            idx = jutils.jintcopy(get_index_1d_to_nd(indices, self._shape))
            if hasattr(values, '__len__') :
                self.set_value(idx, values[0])
            else :
                self.set_value(idx, values)
        elif type(indices) is slice :
            start = indices.start
            if start is None :
                start = 0
            elif start >= self._size :
                raise ValueError, 'index out of range'
            elif start <= -self._size :
                start = -self._size
            stop = indices.stop
            if stop is None :
                stop = self._size
            elif stop > self._size :
                stop = self._size
            step = indices.step
            if step is None :
                step = 1
            vid = 0
            for i in xrange(start, stop, step) :
                idx = jutils.jintcopy(get_index_1d_to_nd(i, self._shape))
                if hasattr(values, '__len__') :
                    self.set_value(idx, values[vid % len(values)])
                    vid += 1
                else :
                    self.set_value(idx, values)
        elif hasattr(indices, '__len__') :
            vid = 0
            for i in xrange(len(indices)) :
                val = indices[i]
                if val >= self._size :
                    if mode == 'clip' :
                        val = self._size - 1
                    elif mode == 'wrap' :
                        val %= self._size
                    else :
                        raise ValueError, 'index out of range: ' + str(val) + ' in ' \
                                + str(self._size)
                if val < 0 :
                    val += self._size
                    if val < 0 :
                        if mode == 'clip' :
                            val = 0
                        elif mode == 'wrap' :
                            val %= self._size
                        else :
                            raise ValueError, 'index out of range: ' + str(indices[i]) \
                                + ' in ' + str(self._size)
                idx = jutils.jintcopy(get_index_1d_to_nd(val, self._shape))
                if hasattr(values, '__len__') :
                    self.set_value(idx, values[vid % len(values)])
                    vid += 1
                else :
                    self.set_value(idx, values)

#####################################################################################
#   Reinterpreting arrays
#####################################################################################    
    def ravel(self):
        return self.flatten()
        
    def reshape(self, shape):
        if type(shape) is int:
            if shape == -1:
                shape = self.size
            shape = [shape]
        elif hasattr(shape, '__iter__'):
            if not type(shape) is list:
                shape = list(shape)
            de = -1
            mul = 1
            for i in xrange(len(shape)):
                if shape[i] == -1:
                    de = i
                else:
                    mul *= shape[i]
            if de >= 0:
                shape[de] = self.size / mul
            jshape = jutils.jintcopy(shape)
            return Array(self.__iArray__.getArrayUtils().reshape(jshape).getArray())
        else :
            raise Exception, 'unsupported type : ' + str(type(shape))

    def permute(self, newshape): 
        jshape = jutils.jintcopy(newshape)
        return Array(self.__iArray__.getArrayUtils().permute(jshape).getArray())
   
    def flatten(self) :
        shape = [self._size]
        arr = instance(shape, 0, self._dtype)
        arr.copy_from(self)
        return arr

    def view_1d(self):
        ns = [self._size]
        return self.reshape(ns)
    
    def __copy__(self):
        return Array(self.__iArray__.copy())

    def __deepcopy__(self):
        return self.__copy__()
    
    def __int__(self):
        if self._size != 1:
            raise TypeError, 'only size-1 arrays can be converted to Python scalars'
        return int(self.get_value(0))

    def __float__(self):
        if self._size != 1:
            raise TypeError, 'only size-1 arrays can be converted to Python scalars'
        return float(self.get_value(0))

    def __long__(self):
        if self._size != 1:
            raise TypeError, 'only size-1 arrays can be converted to Python scalars'
        return long(self.get_value(0))
        
    def float_copy(self):
        return Array(Utilities.copyToDoubleArray(self.__iArray__))

    def positive_float_copy(self):
        return Array(Utilities.copyToPositiveDoubleArray(self.__iArray__))
    
    def __dir__(self):
        dirs = []
        dirs.append('all')
        dirs.append('any')
        dirs.append('argmax')
        dirs.append('argmin')
        dirs.append('clip')
        dirs.append('compress')
        dirs.append('copy_from')
        dirs.append('count_nonzero')
        dirs.append('exp')
        dirs.append('dtype')
        dirs.append('fill')
        dirs.append('flatten')
        dirs.append('float_copy')
        dirs.append('get_reduced')
        dirs.append('get_section')
        dirs.append('get_slice')
        dirs.append('get_value')
        dirs.append('intg')
        dirs.append('item_iter')
        dirs.append('ln')
        dirs.append('log10')
        dirs.append('matrix_dot')
        dirs.append('matrix_invert')
        dirs.append('max')
        dirs.append('mean')
        dirs.append('min')
        dirs.append('ndim')
        dirs.append('positive_float_copy')
        dirs.append('put')
        dirs.append('reshape')
        dirs.append('section_iter')
        dirs.append('set_value')
        dirs.append('shape')
        dirs.append('size')
        dirs.append('sqrt')
        dirs.append('sum')
        dirs.append('take')
        dirs.append('tolist')
        dirs.append('transpose')
        dirs.append('view_1d')
#        if self._dtype is bool :
#            dirs.append('get_bool')
#            dirs.append('get_bool_current')
#            dirs.append('get_bool_next')
#        elif self._dtype is str :
#            dirs.append('get_char')
#            dirs.append('get_char_current')
#            dirs.append('get_char_next')
#        elif self._dtype is float :
#            dirs.append('get_float')
#            dirs.append('get_float_current')
#            dirs.append('get_float_next')
#        elif self._dtype is int :
#            dirs.append('get_int')
#            dirs.append('get_int_current')
#            dirs.append('get_int_next')
#        elif self._dtype is long :
#            dirs.append('get_long')
#            dirs.append('get_long_current')
#            dirs.append('get_long_next')
#        else :
#            dirs.append('get_str')
#            dirs.append('get_str_current')
#            dirs.append('get_str_next')
        return dirs

#    def remove_slices(self, indices):
#        slices = []
#        if type(indices) is int :
#            slices = [indices]
#        elif type(indices) is list :
#            slices = indices
#        else :
#            raise AttributeError, 'indices must be either integer value or integer list'
#        if len(slices) > 0 :
#            nshape = self.shape
#            include = range(nshape[0])
#            for i in slices :
#                include.remove(i)
#            if len(include) == nshape[0] :
#                return
#            nshape[0] = len(include)
#            if nshape[0] == 0 :
#                raise AttributeError, 'can not remove all slices from the dataset'
#            narr = instance(nshape, dtype = self._dtype)
#            counter = 0
#            for i in include :
#                narr[counter] = self[i]
#                counter += 1
#            self.__iArray__ = narr.__iArray__
#            ndata = SimpleData(narr)
#            axis_attr = self.__iDataItem__.getAttribute('axes')
#            self.__iNXdata__.removeDataItem(self.__iDataItem__)
#            self.__iNXdata__.addDataItem(ndata.__iDataItem__)
#            ndata.__iDataItem__.addOneAttribute(axis_attr)
#            self.storage = narr

    def intg(self, axis = None, out = None, keepdims = False):
        if axis is None :
            return self.sum()
        axis_type = type(axis)
        if axis_type is int:
            if axis >= self._ndim :
                raise Exception, 'index out of bound, ' + str(axis)
        else :
            for i in xrange(len(axis)):
                if axis[i] >= self._ndim:
                    raise Exception, 'index out of bound, ' + str(axis)
        if self._ndim == 1:
            return self.sum()
        sshape = [1] * self._ndim
        if axis_type is int:
            sshape[axis] = self._shape[axis]
        else:
            for i in xrange(len(axis)):
                sshape[axis[i]] = self._shape[axis[i]]
        if keepdims:
            nshape = self.shape
            if axis_type is int:
                nshape[axis] = 1
            else:
                for i in xrange(len(axis)):
                    nshape[axis[i]] = 1
        else:
            nshape = []
            for i in xrange(self._ndim):
                if axis_type is int:
                    if i != axis:
                        nshape.append(self._shape[i])
                else:
                    if not axis.__contains__(i):
                        nshape.append(self._shape[i])
        sit = self.section_iter(sshape)
        if out is None:
            out = instance(nshape)
        oit = out.item_iter()
        while sit.has_next():
            oit.set_next(sit.next().sum())
        return out
#        if axis is None :
#            return self.sum(out = out)
#        if hasattr(axis, '__len__'):
#            if len(axis) >= self._ndim:
#                raise Exception, 'index out of bound'
#            for item in axis:
#                if item >= self._ndim :
#                    raise Exception, str(item) + ' does not exist'
#        else :
#            if axis >= self._ndim :
#                raise Exception, str(item) + ' does not exist'
#        if keepdims:
#            nshape = self.shape
#            if hasattr(axis, '__len__'):
#                for item in axis:
#                    nshape[item] = 1
#            else:
#                nshape[axis] = 1
#            
#        else:
#            nshape = []
#            if hasattr(axis, '__len__'):
#                for i in xrange(self._ndim):
#                    if not axis.__contains__(i):
#                        nshape.append(self._shape[i])
#            else:
#                for i in xrange(self._ndim):
#                    if axis != i:
#                        nshape.append(self._shape[i])
#            
#        if out is None:
#            pass
    def diagonal(self, offset=0, axis1=0, axis2=1, out=None):
        if self._ndim < 2:
            raise Exception('diag requires an array of at least two dimensions')
        elif self._ndim == 2:
            if offset == 0:
                s0 = self._shape[0]
                s1 = self._shape[1]
                s = s0 if s0 < s1 else s1
                if not out:
                    out = zeros([s], dtype = self._dtype)
                for i in xrange(s):
                    out[i] = self.get_value([i, i])
            elif offset > 0:
                s0 = self._shape[0]
                s1 = self._shape[1] - offset
                s = s0 if s0 < s1 else s1
                if not out:
                    out = zeros([s], dtype = self._dtype)
                for i in xrange(s):
                    out[i] = self.get_value([i, i + offset])
            else :
                s0 = self._shape[0] + offset
                s1 = self._shape[1]
                s = s0 if s0 < s1 else s1
                if not out:
                    out = zeros([s], dtype = self._dtype)
                for i in xrange(s):
                    out[i] = self.get_value([i - offset, i])
            return out
        else:
            ss = self._shape
            s1 = ss[axis1]
            s2 = ss[axis2]
            shape = [1] * self._ndim
            shape[axis1] = s1
            shape[axis2] = s2
            if offset == 0:
                s = s1 if s1 < s2 else s2
            elif offset > 0:
                s = s1 if s1 < s2 - offset else s2 - offset
            else:
                s = s1 + offset if s1 + offset < s2 else s2
            # os is shape of out, ois is the shape of out iterator shape
            os = []
            ois = []
            to_reduce = []
            for i in xrange(self._ndim):
                if i != axis1 and i != axis2:
                    os.append(self._shape[i])
                    ois.append(1)
                    to_reduce.append(i)
            to_reduce.sort(reverse = True)
            os.append(s)
            out = zeros(os, self._dtype)
            ois.append(s)
            si = self.section_iter(shape)
            oi = out.section_iter(ois)
            while si.has_next():
                sec = si.next()
                osec = oi.next()
                for r in to_reduce:
                    sec = sec.get_reduced(r)
                for r in range(self._ndim - 3, -1, -1):
                    osec = osec.get_reduced(r)
                sec.diagonal(offset, out = osec)
            return out
    
    def trace(self, offset=0, axis1=0, axis2=1, dtype=None, out=None):
        if self._ndim < 2:
            raise Exception('diag requires an array of at least two dimensions')
        elif self._ndim == 2:
            o = 0
            if offset == 0:
                s0 = self._shape[0]
                s1 = self._shape[1]
                s = s0 if s0 < s1 else s1
                for i in xrange(s):
                    o += self.get_value([i, i])
            elif offset > 0:
                s0 = self._shape[0]
                s1 = self._shape[1] - offset
                s = s0 if s0 < s1 else s1
                for i in xrange(s):
                    o += self.get_value([i, i + offset])
            else :
                s0 = self._shape[0] + offset
                s1 = self._shape[1]
                s = s0 if s0 < s1 else s1
                for i in xrange(s):
                    o += self.get_value([i - offset, i])
            if out is None:
                out = o
            else:
                out[:] = o
            return out
        else:
            os = []
            to_reduce = []
            ishape = [1] * self._ndim
            for i in xrange(self._ndim):
                if i != axis1 and i != axis2:
                    os.append(self._shape[i])
                    to_reduce.append(i)
                else:
                    ishape[i] = self._shape[i]
            if out is None:
                if dtype is None:
                    dtype = self._dtype
                out = zeros(os, dtype)
            si = self.section_iter(ishape)
            oi = out.item_iter()
            while si.has_next():
                sec = si.next().get_reduced(to_reduce)
                oi.set_next(sec.trace(offset))
            return out
        
    def tril(self, k=0):
        if self._ndim != 2:
            raise Exception('dimension must be 2')
        arr = zeros(self._shape, self._dtype)
        N, M = self._shape
        if k >= 0:
            for i in xrange(N) :
                l = i + k + 1 if i + k < M else M
                for j in xrange(l):
                    arr[i, j] = self.get_value([i, j])
        else:
            for i in xrange(-k, N):
                l = i + k + 1 if i + k + 1 < M else M
                for j in xrange(l):
                    arr[i, j] = self.get_value([i, j])
        return arr

    def triu(self, k=0):
        if self._ndim != 2:
            raise Exception('dimension must be 2')
        arr = zeros(self._shape, self._dtype)
        N, M = self._shape
        if k >= 0:
            for i in xrange(N) :
                s = i + k
                if s >= M :
                    break
                for j in xrange(s, M):
                    arr[i, j] = self.get_value([i, j])
        else:
            for i in xrange(N):
                s = i + k
                if s >= M :
                    break
                l = s if s >= 0 else 0
                for j in xrange(l, M):
                    arr[i, j] = self.get_value([i, j])
        return arr

    def cumprod(self, axis=None, dtype=None, out=None):
        if dtype is None:
            dtype = self.dtype
        if axis is None:
            if out is None:
                out = zeros([self.size], dtype)
            si = self.item_iter()
            oi = out.item_iter()
            v = 1
            while si.has_next():
                v *= si.next()
                oi.set_next(v)
            return out
        else:
            shape = self.shape
            if out is None:
                out = zeros(shape, dtype)
            ishape = copy.copy(shape)
            ishape[axis] = 1
            si = self.section_iter(ishape)
            oi = out.section_iter(ishape)
            fs = ones(ishape, dtype)
            while si.has_next():
                ss = si.next()
                os = oi.next()
                os[:] = fs * ss
                fs = os
            return out
            
    def cumsum(self, axis=None, dtype=None, out=None):
        if dtype is None:
            dtype = self.dtype
        if axis is None:
            if out is None:
                out = zeros([self.size], dtype)
            si = self.item_iter()
            oi = out.item_iter()
            v = 0
            while si.has_next():
                v += si.next()
                oi.set_next(v)
            return out
        else:
            shape = self.shape
            if out is None:
                out = zeros(shape, dtype)
            ishape = copy.copy(shape)
            ishape[axis] = 1
            si = self.section_iter(ishape)
            oi = out.section_iter(ishape)
            fs = zeros(ishape, dtype)
            while si.has_next():
                ss = si.next()
                os = oi.next()
                os[:] = fs + ss
                fs = os
            return out
            
        
#####################################################################################
# Array slice iter class
#####################################################################################
class ArraySliceIter():
    def __init__(self, array):
        self.array = array
        self.cur_slice = -1
    
    def next(self):
        if self.has_next() :
            self.cur_slice += 1
            arr = self.array.get_slice(0, self.cur_slice)
        else :
            raise StopIteration
        return arr
        
    def curr(self):
        return self.array.get_slice(0, self.cur_slice)
    
    def has_next(self):
        return self.cur_slice < len(self.array) - 1
        
#####################################################################################
# Array value iter class
#####################################################################################
class ArrayItemIter():
    def __init__(self, array):
        if array.size == 0:
            self.iter = iter(array.__iArray__)
            self.next = self.iter.next
            return
        self.iter = array.__iArray__.getIterator()
        dtype = array.dtype
        if dtype is int :
            self.next = self.__next_int__
            self.curr = self.__curr_int__
            self.set_next = self.__set_next_int__
            self.set_curr = self.__set_curr_int__
        elif dtype is float :
            self.next = self.__next_float__
            self.curr = self.__curr_float__
            self.set_next = self.__set_next_float__
            self.set_curr = self.__set_curr_float__
        elif dtype is long :
            self.next = self.__next_long__
            self.curr = self.__curr_long__
            self.set_next = self.__set_next_long__
            self.set_curr = self.__set_curr_long__
        elif dtype is bool :
            self.next = self.__next_bool__
            self.curr = self.__curr_bool__
            self.set_next = self.__set_next_bool__
            self.set_curr = self.__set_curr_bool__
        elif dtype is str :
            self.next = self.__next_char__
            self.curr = self.__curr_char__
            self.set_next = self.__set_next_char__
            self.set_curr = self.__set_curr_char__
        else :
            self.next = self.__next_str__
            self.curr = self.__curr_str__
            self.set_next = self.__set_next_str__
            self.set_curr = self.__set_curr_str__
    
    def __next_bool__(self):
        if self.iter.hasNext() :
            return self.iter.getBooleanNext()
        else :
            raise StopIteration()

    def __next_int__(self):
        if self.iter.hasNext() :
            return self.iter.getIntNext()
        else :
            raise StopIteration()

    def __next_float__(self):
        if self.iter.hasNext() :
            return self.iter.getDoubleNext()
        else :
            raise StopIteration()

    def __next_char__(self):
        if self.iter.hasNext() :
            return self.iter.getCharNext()
        else :
            raise StopIteration()

    def __next_long__(self):
        if self.iter.hasNext() :
            return self.iter.getLongNext()
        else :
            raise StopIteration()

    def __next_str__(self):
        if self.iter.hasNext() :
            return self.iter.getObjectNext().toString()
        else :
            raise StopIteration()
    
    def __curr_bool__(self):
        return self.iter.getBooleanCurrent()

    def __curr_int__(self):
        return self.iter.getIntCurrent()

    def __curr_float__(self):
        return self.iter.getDoubleCurrent()

    def __curr_long__(self):
        return self.iter.getLongCurrent()

    def __curr_char__(self):
        return self.iter.getCharCurrent()

    def __curr_str__(self):
        return self.iter.getObjectCurrent().toString()

    def __set_next_int__(self, value):
        if self.iter.hasNext() :
            self.iter.next().setIntCurrent(int(value))
        else :
            raise StopIteration()
    
    def __set_next_float__(self, value):
        if self.iter.hasNext() :
            self.iter.next().setDoubleCurrent(float(value))
        else :
            raise StopIteration()
    
    def __set_next_bool__(self, value):
        if self.iter.hasNext() :
            self.iter.next().setBooleanCurrent(bool(value))
        else :
            raise StopIteration()
    
    def __set_next_long__(self, value):
        from java.math import BigInteger
        if self.iter.hasNext() :
            self.iter.next().setLongCurrent(long(value))
        else :
            raise StopIteration()
    
    def __set_next_char__(self, value):
        if self.iter.hasNext() :
            self.iter.next().setCharCurrent(str(value))
        else :
            raise StopIteration()
    
    def __set_next_str__(self, value):
        if self.iter.hasNext() :
            self.iter.next().setObjectCurrent(str(value))
        else :
            raise StopIteration()

    def __set_curr_int__(self, value):
        self.iter.setIntCurrent(int(value))
    
    def __set_curr_float__(self, value):
        self.iter.setFloatCurrent(float(value))
    
    def __set_curr_bool__(self, value):
        self.iter.setBooleanCurrent(bool(value))
    
    def __set_curr_long__(self, value):
        self.iter.setLongCurrent(long(value))
    
    def __set_curr_char__(self, value):
        self.iter.setCharCurrent(str(value))
    
    def __set_curr_str__(self, value):
        self.iter.setObjectCurrent(str(value))
        
    def has_next(self):
        return self.iter.hasNext()

    def get_index(self):
        idx = self.iter.getCounter()
        return [i for i in idx]

#####################################################################################
# Array section iter class
#####################################################################################
class ArraySectionIter():
    def __init__(self, array, shape):
        self.array = array
        if type(shape) is int :
            shape = [shape]
        if len(shape) > array.ndim :
            raise ValueError, 'expecting sections to be at most in ' + str(array.ndim) + ' dim'
        for i in xrange(len(shape)) :
            if array.shape[array.ndim - 1 - i] % shape[len(shape) - 1 -i] != 0 :
                raise ValueError, 'shape is not comfortable at dim=' + str(array.ndim - 1 - i)
        self.__cur_section_org__ = [0] * array.ndim
        self.__section_iter_shape__ = shape
        self.__last_section_org__ = None
    
    def next(self):
        if self.has_next() :
            nshape = [1] * self.array.ndim
            lssh = len(self.__section_iter_shape__)
            for i in xrange(lssh) :
                nshape[self.array.ndim - 1 - i] = self.__section_iter_shape__[lssh - 1 - i]
            sec = self.array.get_section(self.__cur_section_org__, nshape, None)
            if self.array.ndim > lssh :
                sec = sec.get_reduced(range(self.array.ndim - lssh))
            self.__cur_section_org__[self.array.ndim - 1] += self.__section_iter_shape__[lssh - 1]
            for i in xrange(self.array.ndim) :
                cdim = self.array.ndim - 1 - i
                if self.__cur_section_org__[cdim] >= self.array.shape[cdim] :
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
            return self.array.get_slice(0, self.__last_section_org__)
    
    def has_next(self):
        if self.__cur_section_org__[0] < self.array.shape[0] :
            return True
        else :
            return False


#####################################################################################
# Array utilities
#####################################################################################
def get_ndim(obj, dep = 0) :
    if dep > 7 :
        return 0
    if isinstance(obj, Array) :
        return obj.ndim
    if hasattr(obj, '__len__') :
        if len(obj) == 0 :
            return 0
        if obj[0] is obj :
            return 0
        if type(obj) is str :
            if dep is 0 :
                return 1
            else :
                return 0
        rank = get_ndim(obj[0], dep + 1) + 1
        return rank
    else :
        return 0

def get_type(obj):
    if hasattr(obj, 'dtype'):
        return obj.dtype
    else :
        if type(obj) is str :
            return str
        else :
            if hasattr(obj, '__len__') :
                _dtype = bool
                for item in obj :
                    _dtype = __compare_type__(get_type(item), _dtype)
                return _dtype
            else :
                return type(obj)
                    
def __compare_type__(t1, t2):
    v1 = __value_type__(t1)
    v2 = __value_type__(t2)
    if v1 >= v2 :
        return t1
    else :
        return t2

def __value_type__(t):
    if t is bool :
        return 100
    elif t is int :
        return 200
    elif t is long :
        return 300
    elif t is float :
        return 400
    elif t is str :
        return 500
    else :
        return 1000

def __is_matrix__(obj):
    if hasattr(obj, '__len__') :
        size = 0
        haslen = None
        for val in obj :
            if haslen is None :
                haslen = hasattr(val, '__len__')
            else :
                if haslen ^ hasattr(val, '__len__') :
                    return False
            if size == 0 :
                size = get_size(val, None, False)
            else :
                if size != get_size(val, None, False) :
                    return False
            if not __is_matrix__(val) :
                return False
    return True
    
def get_shape(obj, rank = -1):
    if hasattr(obj, '_shape') :
        return list(obj.shape)
    if rank < 0 :
        rank = get_ndim(obj)
        return get_shape(obj, rank)
    if rank is 0 :
        return []
    return [len(obj)] + get_shape(obj[0], rank - 1)

def get_size(obj, shape = None, ismatrix = True): 
    if isinstance(obj, Array) :
        return obj.size
    if not hasattr(obj, '__len__') :
        return 1
    if ismatrix :
        if shape is None :
            shape = get_shape(obj)
        if len(shape) == 0 or shape[0] is 0 :
            return 0
        size = 1
        for val in shape :
            size *= val
        return size
    else :
        size = 0
        for val in obj :
            size += get_size(val, None, ismatrix)
        return size
    
def get_item(obj, id, shape = None, ismatrix = True):
    if ismatrix :
        if shape is None :
            shape = get_shape(obj)
        if len(shape) > 1 :
            tshape = shape[1:]
            tsize = 1
            for val in tshape :
                tsize *= val
            fid = id / tsize
            tid = id % tsize
            return get_item(obj[fid], tid, tshape)
        else :
            return obj[id]
    else :
        if hasattr(obj, '__len__') :
            start = 0
            for val in obj :
                end = start + get_size(val, None, ismatrix)
                if id < end :
                    return get_item(val, id, None, ismatrix)
                start = end
        else :
            return obj
        
def get_index_1d_to_nd(i, shape):
    if len(shape) == 1 :
        if i < shape[0] :
            return [i]
        else :
            raise ValueError, 'index out of range'
    else :
        tshape = shape[1:]
        tsize = 1
        for val in tshape :
            tsize *= val
        fid = i / tsize
        if fid >= shape[0] :
            raise ValueError, 'index out of range, ' + str(fid) + ' in ' + shape[0]
        tid = i % tsize
        return [fid] + get_index_1d_to_nd(tid, tshape)

#####################################################################################
# Array creation
#####################################################################################    
def zeros(shape, dtype = float): 
    if type(shape) is int :
        shape = [shape]
    tp = DataType.DOUBLE
    if dtype is int :
        tp = DataType.INT
    elif dtype is long :
        tp = DataType.LONG
    elif dtype is bool :
        tp = DataType.BOOLEAN
    jshape = jutils.jintcopy(shape)
    iArray = gdm_factory.createArray(tp.getPrimitiveClassType(), jshape)
    return Array(iArray)

def zeros_like(array):
    return instance(array.shape, 0, array.dtype)

'''
Create a two-dimensional array with the flattened input as a diagonal.
Parameters:    
obj : array_like
    Input data, which is flattened and set as the k-th diagonal of the output.
k : int, optional
    Diagonal to set; 0, the default, corresponds to the "main" diagonal, a positive (negative) k giving the number of the diagonal above (below) the main.
Returns:    
out : ndarray    
    The 2-D output array.
Examples
>>> np.diagflat([[1,2], [3,4]])
array([[1, 0, 0, 0],
       [0, 2, 0, 0],
       [0, 0, 3, 0],
       [0, 0, 0, 4]])

>>> np.diagflat([1,2], 1)
array([[0, 1, 0],
       [0, 0, 2],
       [0, 0, 0]])
'''
def diagflat(obj, k = 0):
    if hasattr(obj, 'item_iter') and hasattr(obj, 'size'):
        dim = obj.size + abs(k)
        arr = zeros([dim, dim])
        oiter = obj.item_iter()
        if k == 0:
            idx = 0
            while True:
                try:
                    arr[idx, idx] = oiter.next()
                    idx += 1
                except:
                    break
        elif k > 0:
            idx = 0
            while True:
                try:
                    arr[idx, idx + k] = oiter.next()
                    idx += 1
                except:
                    break
        else:
            k = -k
            idx = 0
            while True:
                try:
                    arr[idx + k, idx] = oiter.next()
                    idx += 1
                except:
                    break
        return arr
    elif type(obj) is list :
        ndim = get_ndim(obj)
        if ndim > 1:
            obj = list(itertools.chain(*obj))
        dim = len(obj) + abs(k)
        arr = zeros([dim, dim])
        if k == 0:
            idx = 0
            for item in obj :
                arr[idx, idx] = item
                idx += 1
        elif k > 0:
            idx = 0
            for item in obj :
                arr[idx, idx + k] = item
                idx += 1
        else:
            k = -k
            idx = 0
            for item in obj :
                arr[idx + k, idx] = item
                idx += 1
        return arr

def tri(N, M = None, k = 0, dtype = float):
    if M is None :
        M = N
    arr = instance([N, M], dtype = dtype)
    if k >= 0:
        for i in xrange(N) :
            l = i + k + 1 if i + k < M else M
            for j in xrange(l):
                arr[i, j] = 1
    else:
        for i in xrange(-k, N):
            l = i + k + 1 if i + k + 1 < M else M
            for j in xrange(l):
                arr[i, j] = 1
    return arr

def vander(x, N=None, increasing=False):
    l = len(x)
    if N is None:
        N = l
    arr = zeros([l, N], x.dtype)
    if increasing:
        rid = 0
        for row in arr :
            v = x[rid]
            ri = iter(row)
            cid = 0
            while ri.has_next():
                ri.set_next(v ** cid)
                cid += 1
            rid += 1
    else:
        rid = 0
        for row in arr :
            v = x[rid]
            ri = iter(row)
            cid = N - 1
            while ri.has_next():
                ri.set_next(v ** cid)
                cid -= 1
            rid += 1
    return arr
    
        
def ones(shape, dtype = float):
    if type(shape) is int :
        shape = [shape]
    tp = DataType.DOUBLE
    if dtype is int :
        tp = DataType.INT
    elif dtype is long :
        tp = DataType.LONG
    elif dtype is bool :
        tp = DataType.BOOLEAN
    jshape = jutils.jintcopy(shape)
    array = Array(gdm_factory.createArray(tp.getPrimitiveClassType(), jshape))
    array.fill(1)
    return array

def ones_like(array):
    return instance(array.shape, 1, array.dtype)

def eye(N, M = None, k = 0, dtype = float):
    if M is None :
        M = N
    arr = instance([N, M], dtype = dtype)
    for i in xrange(N) :
        if i + k >= 0 and i + k < M :
            arr[i, i + k] = 1
    return arr
    
def rand(shape, para = None, engine = None, dtype = float):
    arr = zeros(shape, dtype)
    if engine is None :
        if para is None :
            engine = random.random
        else :
            engine = random.uniform
    aiter = arr.item_iter()
    if para is None :
        try :
            while True :
                aiter.set_next(engine())
        except StopIteration:
            pass
    else :
        comd = 'engine('
        if hasattr(para, '__len__') :
            for item in para :
                comd += str(item) + ","
            comd +=")"
        else :
            comd += str(para) + ")"
        print comd
        try :
            while True :
                aiter.set_next(eval(comd))
        except StopIteration:
            pass
    return arr
    
def instance(shape, init = 0, dtype = float):
    if shape == 0 or shape == [0]:
        return Array([], dtype = dtype)
    if type(shape) is int :
        shape = [shape]
    if dtype is float :
        tp = DataType.DOUBLE
    elif dtype is int :
        tp = DataType.INT
    elif dtype is long :
        tp = DataType.LONG
    elif dtype is bool :
        tp = DataType.BOOLEAN
    elif dtype is str :
        tp = DataType.CHAR
    elif dtype is object :
        tp = DataType.STRING
    elif dtype is None :
        tp = DataType.DOUBLE
    else :
        raise TypeError, 'data type not supported - ' + str(dtype)
    jshape = jutils.jintcopy(shape)
    array = Array(gdm_factory.createArray(tp.getPrimitiveClassType(), jshape))
    if init != 0 :
        array.fill(init)
    return array

def asarray(obj, dtype = None):
    return Array(obj, None, dtype)
    
def arange(*args) :
    l = len(args)
    if l < 1 :
        raise TypeError, 'arange() expects at least 1 argument, got 0'
    if l > 5 :
        raise TypeError, 'arange() expects at most 4 arguments, got ' + str(l)
    datatype = None
    nums = []
    shape = None
    for obj in args :
        if isinstance(obj, int) or isinstance(obj, float) :
            nums.append(obj)
        elif type(obj) is type :
            datatype = obj
        elif hasattr(obj, '__len__') :
            shape = obj
    ll = len(nums)
    if ll < 1 :
        raise TypeError, 'arange() expects at least 1 numeric argument, got 0'
    if ll > 3 :
        raise TypeError, 'arange() expects the 4th argument to be type, got a numeric value'
    if ll is 1 :
        start = 0
        end = nums[0]
        step = 1
    elif ll is 2 :
        start = nums[0]
        end = nums[1]
        if start < end:
            step = 1
        elif start > end:
            step = -1
        else:
            raise ValueError, 'can not create empty array'
    else :
        start = nums[0]
        end = nums[1]
        step = nums[2]
        if step == 0 :
            raise TypeError, 'arrange() can not take 0 as step value'
    _dtype = int
    if type(start) is float or type(end) is float or type(step) is float :
        _dtype = float
    if datatype is None :
        datatype = _dtype
    size = (float(end) - start) / step
    if size - int(size) > (10 ** -Array.precision) :
        size += 1
    size = int(size)
    if shape is None :
        shape = [size]
    else :
        _size = 1
        for val in shape :
            _size *= val
        if _size != size :
            raise TypeError, 'size of the array does not match with the given shape, \
                should be ' + str(size) + ', but ' + str(_size) + ' in the shape'
    arr = instance(shape, dtype = datatype)
    aiter = arr.item_iter()
    val = float(start)
    if step > 0 :
        while val - end < - 10 ** -Array.precision :
            aiter.set_next(val)
            val += step
    else :
        while val - end > 10 ** -Array.precision :
            aiter.set_next(val)
            val += step
    return arr

def linspace(start, end, num, shape = None, endpoint=True, retstep=False):
    if num < 1 :
        raise TypeError, 'number of step must be at least 1'
    if num == 1 :
        step = end - start
    else :
        if endpoint :
            step = (float(end) - start) / int(num - 1)
        else :
            step = (float(end) - start) / int(num)
    if shape is None :
        shape = [num]
    else :
        stype= type(shape)
        if stype is int or stype is long :
            shape = [shape]
        elif stype is float :
            shape = [int(shape)]
        _size = 1
        for val in shape :
            _size *= val
        if _size != num :
            raise TypeError, 'size of the array does not match with the given shape, \
                should be ' + str(num) + ', but ' + str(_size) + ' in the shape'
    arr = instance(shape, dtype = float)
    ai = arr.item_iter()
    i = 0
    while i < num :
        ai.set_next(start + i * step)
        i += 1
    if retstep :
        return (arr, step)
    else:
        return arr

#####################################################################################
# Create array from existing data
#####################################################################################
def take(arr, indices, axis=None, out=None):
    return arr.take(indices, axis, out)
    
#####################################################################################
# Joining arrays
#####################################################################################
def append(obj, val, axis = None):
    dtype = __compare_type__(get_type(obj), get_type(val))
    osize = get_size(obj)
    vsize = get_size(val)
    if axis is None :
#        isoa = isinstance(obj, Array)
#        isva = isinstance(val, Array)
        size = osize + vsize
        arr = instance([size], 0, dtype)
        arr.copy_from(obj, osize)
        arr[osize : size].copy_from(val, vsize)
        return arr
    else :
        odim = get_ndim(obj)
        vdim = get_ndim(val)
        if odim != vdim :
            raise ValueError, 'they must have the same dimension: ' + str(odim) +' vs ' + str(vdim)
        if axis >= odim :
            raise ValueError, 'bad axis argument to swapaxes: ' + str(axis) + \
                ', expecting axis < ' + str(odim)
        oshp = get_shape(obj, odim)
        vshp = get_shape(val, vdim)
        nshp = []
        for id in xrange(odim) :
            if id != axis :
                if (oshp[id] != vshp[id]) :
                    raise ValueError, 'array dimensions must agree except for d_' + str(axis)
                nshp.append(oshp[id])
            else :
                nshp.append(oshp[id] + vshp[id])
        arr = instance(nshp, 0, dtype)
        if axis == 0 :
            arr[0:oshp[0]].copy_from(obj)
            arr[oshp[0] : nshp[0]].copy_from(val)
        else :
            org = [0] * arr.ndim
            arr.get_section(org, oshp).copy_from(obj)
            org[axis] = oshp[axis]
            arr.get_section(org, vshp).copy_from(val)
        return arr

def concatenate(tup, axis = 0, out = None):
    if len(tup) < 2 :
        raise ValueError, 'must have at least 2 arrays'
    dtype = bool
    if axis is None:
        size = 0
        sizes = []
        for obj in tup :
            dtype = __compare_type__(get_type(obj), dtype)
            s = get_size(obj)
            size += s
            sizes.append(s)
        if out is None:
            arr = instance([size], 0, dtype)
        else:
            arr = out
        start = 0
        for i in xrange(len(tup)):
            obj = tup[i]
            cs = sizes[i]
            arr[start : start + cs].copy_from(obj)
            start += cs
    else:
        odim = -1
        for obj in tup :
            dtype = __compare_type__(get_type(obj), dtype)
            if odim < 0 :
                odim = get_ndim(obj)
            else :
                if odim != get_ndim(obj) :
                    raise ValueError, 'they must have the same dimension'
        if axis >= odim :
            raise ValueError, 'bad axis argument to swapaxes: ' + str(axis) + \
                ', expecting axis < ' + str(odim)
        oshp = None
        shapes = []
        alen = []
        for obj in tup :
            if oshp is None :
                oshp = get_shape(obj, odim)
                nshp = copy.copy(oshp)
                alen.append(oshp[axis])
                shapes.append(oshp)
            else :
                vshp = get_shape(obj, odim)
                shapes.append(vshp)
                for id in xrange(odim) :
                    if id == axis :
                        nshp[id] += vshp[id]
                        alen.append(vshp[id])
                    else :
                        if (oshp[id] != vshp[id]) :
                            raise ValueError, 'array dimensions must agree except for d_' + str(id)
        if out is None:
            arr = instance(nshp, 0, dtype)
        else:
            arr = out
        if axis == 0 :
            id = 0
            start = 0
            for obj in tup :
                arr[start : start + alen[id]].copy_from(obj)
                start += alen[id]
                id += 1
        else :
            org = [0] * arr.ndim
            id = 0
            for obj in tup :
                arr.get_section(org, shapes[id]).copy_from(obj)
                org[axis] += alen[id]
                id += 1
    return arr

def resize(a, new_shape):
    res = zeros(new_shape, a.dtype)
    ai = a.item_iter()
    ri = res.item_iter()
    if res.size > a.size:
        while ri.has_next():
            if not ai.has_next():
                ai = a.item_iter()
            ri.set_next(ai.next())
    else:
        while ri.has_next():
            ri.set_next(ai.next())
    return res

def trim_zeros(filt, trim='fb'):
    first = 0
    trim = trim.upper()
    if 'F' in trim:
        for i in filt:
            if i != 0.:
                break
            else:
                first = first + 1
    last = len(filt)
    if 'B' in trim:
        for i in xrange(last):
            if filt[last - i - 1] != 0.:
                break
        last = last - i
    return filt[first:last]

def flip(a, axis=None):
    return a.flip(axis)

def roll(a, shift, axis = None):
    res = zeros(a.shape, a.dtype)
    if axis is None or a.ndim == 1:
        shift = shift % a.size
        ai = a.item_iter()
        ri = res.item_iter()
        if shift > 0:
            for i in xrange(shift):
                ri.next()
            for i in xrange(a.size - shift):
                ri.set_next(ai.next())
            ri = res.item_iter()
            for i in xrange(shift):
                ri.set_next(ai.next())
        else:
            for i in xrange(shift):
                ai.next()
            for i in xrange(a.size - shift):
                ri.set_next(ai.next())
            ai = a.item_iter()
            for i in xrange(shift):
                ri.set_next(ai.next())
    else:
        shape = a.shape
        size = shape[axis]
        shift = shift % size
        sr = []
        sa = []
        for i in xrange(a.ndim):
            if i != axis:
                sr.append(slice(None, None, 1))
                sa.append(slice(None, None, 1))
            else :
                sr.append(1)
                sa.append(1)
        if shift > 0:
            for i in xrange(shift):
                sr[axis] = i
                sa[axis] = size - shift + i
                res[sr] = a[sa]
            for i in xrange(size - shift):
                sr[axis] = shift + i
                sa[axis] = i
                res[sr] = a[sa]
        else:
            for i in xrange(size - shift):
                sr[axis] = i
                sa[axis] = shift + i
                res[sr] = a[sa]
            for i in xrange(shift):
                sr[axis] = size - shift + i
                sa[axis] = i
                res[sr] = a[sa]
    return res
            
def rot90(m, k=1, axes=(0, 1)):
    if m.ndim < 2:
        raise ValueError('m must be at least 2D')
    k = k % 4
    if k == 0:
        return copy.copy(m)
    elif k == 1:
        msh = m.shape
        rsh = m.shape
        rsh[axes[0]] = msh[axes[1]]
        rsh[axes[1]] = msh[axes[0]]
        res = zeros(rsh, m.dtype)
        len = msh[axes[0]]
        asl = []
        rsl = []
        for i in xrange(m.ndim):
            if i == axes[0]:
                rsl.append(1)
                asl.append(slice(None, None, 1))
            elif i == axes[1]:
                rsl.append(slice(None, None, 1))
                asl.append(1)
            else:
                rsl.append(slice(None, None, 1))
                asl.append(slice(None, None, 1))
        for i in xrange(len):
            rsl[axes[0]] = i
            asl[axes[1]] = len - 1 - i
            res[rsl] = m[asl]
    elif k == 2:
        pass
    return res
        
    
def stack(tup, axis = 0, out = None):
    length = -1
    shape = None
    dtype = bool
    for item in tup :
        itype = get_type(item)
        dtype = __compare_type__(dtype, itype)
        if shape is None :
            shape = get_shape(item)
            
#             if len(shape) == 1:
#                 if axis == 0:
#                     shape = [1] + shape
#                 elif axis == 1 or axis == -1:
#                     shape = shape + [1]
#                 else:
#                     raise ValueError, 'out of bounds for array of dimension 2'
        else:
            s = get_shape(item)
#             if len(s) == 1:
#                 if axis == 0:
#                     s = [1] + s
#                 elif axis == 1 or axis == -1:
#                     s = s + [1]
#                 else:
#                     raise ValueError, 'out of bounds for array of dimension 2'
            if shape != s :
                raise ValueError, 'all items must have the same shape'
    if axis < 0:
        axis += len(shape) + 1
    if axis < 0 or axis > len(shape):
        raise ValueError, 'axis out of bounds of dimensions'
    nsh = shape[:axis] + [len(tup)] + shape[axis:]
#     for i in xrange(len(shape)):
#         if i == axis:
#             nsh.append(shape[i] * len(tup))
#         else:
#             nsh.append(shape[i])
    if out is None:
        arr = instance(nsh, 0, dtype)
    else:
        arr = out
    id = 0
    for item in tup :
        idx = ()
        for i in xrange(len(nsh)):
            if i == axis:
                idx += (id,)
            else:
                idx += (slice(0, nsh[i]),)
        arr[idx].copy_from(item)
        id += 1
    return arr
  
def column_stack(*tup):
    items = []
    for item in tup :
        if type(item) is tuple :
            for si in item :
                items.append(si)
        else :
            items.append(item)
    length = -1
    for item in items :
        if length < 0 :
            length = len(item)
        elif len(item) != length :
            raise ValueError, 'all items must have the same length'
    dtype = bool
    for item in items :
        itype = get_type(item)
        dtype = __compare_type__(dtype, itype)
    arr = instance([length, len(items)], 0, dtype)
    id = 0
    for item in items :
        arr[:, id].copy_from(item)
        id += 1
    return arr

def vstack(*tup):
    if len(tup) < 2 :
        raise ValueError, 'must have at least 2 arrays'
    dtype = bool
    oshp = None
    shapes = []
    alen = []
    for obj in tup :
        dtype = __compare_type__(get_type(obj), dtype)
        if oshp is None :
            oshp = get_shape(obj)
            if len(oshp) == 1:
                oshp = [1] + oshp
            nshp = copy.copy(oshp)
            alen.append(oshp[0])
            shapes.append(oshp)
        else :
            vshp = get_shape(obj)
            if len(vshp) == 1:
                vshp = [1] + vshp
            shapes.append(vshp)
            alen.append(vshp[0])
            nshp[0] += vshp[0]
    arr = instance(nshp, 0, dtype)
    id = 0
    start = 0
    for obj in tup :
        arr[start : start + alen[id]].copy_from(obj)
        start += alen[id]
        id += 1
    return arr            
#     items = []
#     ndim = 0
#     shape = []
#     for item in tup :
#         if type(item) is tuple :
#             for si in item :
#                 items.append(si)
#                 idim = get_ndim(si)
#                 if idim > ndim :
#                     ndim = idim
#                     shape = get_shape(si, ndim)
#         else :
#             items.append(item)
#         idim = get_ndim(item)
#         if idim > ndim :
#             ndim = idim
#             shape = get_shape(item, ndim)
#     nshape = copy.copy(shape)
#     nshape[0] = 0
#     allshape = []
#     alldim = []
#     for item in items :
#         idim = get_ndim(item)
#         alldim.append(idim)
#         ishape = get_shape(item, idim)
#         allshape.append(ishape)
#         if idim < ndim :
#             if ndim - idim > 1 :
#                 raise ValueError, 'rank does not match, must be same or at most 1 less'
#             else :
#                 if ishape != shape[1:] :
#                     raise ValueError, 'shape does not match, must be same except for axis=0'
#             nshape[0] += 1
#         else :
#             if ishape[1:] != shape[1:] :
#                 raise ValueError, 'shape does not match, must be same except for axis=0'
#             nshape[0] += ishape[0]
#     dtype = bool
#     for item in items :
#         itype = get_type(item)
#         dtype = __compare_type__(dtype, itype)
#     arr = instance(nshape, 0, dtype)
#     org = [0] * arr.ndim
#     id = 0
#     for item in items :
#         if alldim[id] < ndim :
#             ishape = [1]+ allshape[id]
#         else :
#             ishape = allshape[id]
#         arr.get_section(org, ishape).copy_from(item)
#         org[0] += ishape[0]
#         id += 1
#     return arr

def hstack(*tup):
    items = []
    ndim = 0
    allshape = []
    for item in tup :
        if type(item) is tuple :
            for si in item :
                items.append(si)
                idim = get_ndim(si)
                if ndim == 0 :
                    ndim = idim
                else :
                    if ndim != idim :
                        raise ValueError, 'rank does not match, must be the same'
                shape = get_shape(si, idim)
                allshape.append(shape)
        else :
            items.append(item)
            idim = get_ndim(item)
            if ndim == 0 :
                ndim = idim
            else :
                if ndim != idim :
                    raise ValueError, 'rank does not match, must be the same'
            shape = get_shape(item, idim)
            allshape.append(shape)
    nshape = copy.copy(shape)
    if ndim == 1 :
        nshape[0] = 0
        for sitem in allshape :
            nshape[0] += sitem[0]
    else :
        nshape[1] = 0
        for sitem in allshape :
            nshape[1] += sitem[1]
            for id in xrange(ndim) :
                if id != 1 :
                    if sitem[id] != shape[id] :
                        raise ValueError, 'shape does not match, must be same except for axis=1'
    dtype = bool
    for item in items :
        itype = get_type(item)
        dtype = __compare_type__(dtype, itype)
    arr = instance(nshape, 0, dtype)
    org = [0] * arr.ndim
    if ndim == 1 :
        chid = 0
    else :
        chid = 1
    id = 0
    for item in items :
        ishape = allshape[id]
        arr.get_section(org, ishape).copy_from(item)
        org[chid] += ishape[chid]
        id += 1
    return arr
    
def dstack(*tup):
    items = []
    ndim = 0
    allshape = []
    for item in tup :
        if type(item) is tuple :
            for si in item :
                items.append(si)
                idim = get_ndim(si)
                if ndim == 0 :
                    ndim = idim
                else :
                    if ndim != idim :
                        raise ValueError, 'rank does not match, must be the same'
                shape = get_shape(si, idim)
                allshape.append(shape)
        else :
            items.append(item)
            idim = get_ndim(item)
            if ndim == 0 :
                ndim = idim
            else :
                if ndim != idim :
                    raise ValueError, 'rank does not match, must be the same'
            shape = get_shape(item, idim)
            allshape.append(shape)
    if ndim == 1 :
        nshape = [1, shape[0], len(items)]
        for sitem in allshape :
            if sitem != shape :
                raise ValueError, 'shape does not match, must be same except for axis=2'
    elif ndim == 2 :
        nshape = shape + [len(items)]
        for sitem in allshape :
            if sitem != shape :
                raise ValueError, 'shape does not match, must be same except for axis=2'
    else :
        nshape = copy.copy(shape)
        nshape[2] = 0
        for sitem in allshape :
            nshape[2] += sitem[2]
            for id in xrange(ndim) :
                if id != 2 :
                    if sitem[id] != shape[id] :
                        raise ValueError, 'shape does not match, must be same except for axis=2'
    dtype = bool
    for item in items :
        itype = get_type(item)
        dtype = __compare_type__(dtype, itype)
    arr = instance(nshape, 0, dtype)
    org = [0] * arr.ndim
    id = 0
    if ndim == 1 :
        for item in items :
            ishape = [1] + allshape[id] + [1]
            arr.get_section(org, ishape).copy_from(item)
            org[2] += 1
            id += 1
    elif ndim == 2 :
        for item in items :
            ishape = allshape[id] + [1]
            arr.get_section(org, ishape).copy_from(item)
            org[2] += 1
            id += 1
    else :
        for item in items :
            ishape = allshape[id]
            arr.get_section(org, ishape).copy_from(item)
            org[2] += ishape[2]
            id += 1
    return arr

    
#####################################################################################
# Splitting arrays
#####################################################################################
def array_split(arr, indices_or_sections, axis = 0):
    if axis >= arr.ndim :
        raise ValueError, 'axis must be within the ndim of the array, ' + str(axis) \
                    + ' in ' + str(arr.ndim)
    res = []
    osize = arr.shape[axis]
    if type(indices_or_sections) is int :
        nsize = osize / indices_or_sections
        rem = osize - nsize * indices_or_sections
        org = [0] * arr.ndim
        nshape = copy.copy(arr.shape)
        for id in xrange(indices_or_sections) :
            if rem > 0:
                nshape[axis] = nsize + 1
                rem -= 1
            else:
                nshape[axis] = nsize
            res += [arr.get_section(org, nshape)]
            org[axis] += nshape[axis]
#         nshape[axis] = osize - nsize * (indices_or_sections - 1)
#         res += [arr.get_section(org, nshape)]
    elif hasattr(indices_or_sections, '__len__') :
        org = [0] * arr.ndim
        nshape = copy.copy(arr.shape)
        start = 0
        for item in indices_or_sections :
            if item > 0 :
                if item >= osize :
                    nshape[axis] = osize - start
                else :
                    nshape[axis] = item - start
                start = item
                res += [arr.get_section(org, nshape)]
                org[axis] += nshape[axis]
        if start < osize :
            nshape[axis] = osize - start
            res += [arr.get_section(org, nshape)]
    else :
        raise ValueError, 'expecting either int value or list at arg[1]'
    return res

def split(arr, indices_or_sections, axis = 0):
    if axis >= arr.ndim :
        raise ValueError, 'axis must be within the ndim of the array, ' + str(axis) \
                    + ' in ' + str(arr.ndim)
    if type(indices_or_sections) is int :
        osize = arr.shape[axis]
        if osize != osize / indices_or_sections * indices_or_sections :
            raise ValueError, 'array split does not result in an equal division'
    return array_split(arr, indices_or_sections, axis)
        
def dsplit(arr, indices_or_sections):
    if arr.ndim < 3 :
        raise ValueError, 'array must have at least 3 dimensions, got ' + str(arr.ndim)
    return array_split(arr, indices_or_sections, 2)

def hsplit(arr, indices_or_sections):
    if arr.ndim == 1 :
        return array_split(arr, indices_or_sections, 0)
    return array_split(arr, indices_or_sections, 1)

def vsplit(arr, indices_or_sections):
    return array_split(arr, indices_or_sections, 0)


#####################################################################################
# Enlarging arrays
#####################################################################################
def tile(obj, reps):
    ndim = get_ndim(obj)
    shape = get_shape(obj)
    if hasattr(reps, '__len__') :
        if ndim == 0 :
            nshape = reps
        else :
            lshape = len(shape)
            lreps = len(reps)
            if lshape > lreps :
                lnshape = lshape
            else :
                lnshape = lreps
            nshape = [1] * lnshape
            for i in xrange(lshape) :
                nshape[lnshape - 1 - i] *= shape[lshape - 1 - i]
            for i in xrange(lreps) :
                nshape[lnshape - 1 - i] *= reps[lreps - 1 - i]
    else :
        if ndim == 0 :
            nshape = [reps]
        else :
            nshape = copy.copy(shape)
            nshape[len(nshape) - 1] *= reps
    dtype = get_type(obj)
    arr = instance(nshape, 0, dtype)
    if ndim == 0 :
        arr.fill(obj)
    else :
        sit = arr.section_iter(shape)
        while sit.has_next() :
            sit.next().copy_from(obj)
    return arr

def repeat(obj, repeats, axis=None):
    if not isinstance(obj, Array):
        if hasattr(obj, '__iter__'):
            obj = Array(obj)
        else:
            obj = Array([obj])
    if axis is None:
        osize = obj.size
        if type(repeats) is int:
            nsize = osize * repeats
            res = zeros([nsize], obj.dtype)
            ri = res.item_iter()
            oi = obj.item_iter()
            for i in xrange(osize):
                val = oi.next()
                for j in xrange(repeats):
                    ri.set_next(val)
            return res
        else:
            nsize = sum(repeats)
            res = zeros([nsize], obj.dtype)
            ri = res.item_iter()
            oi = obj.item_iter()
            for i in xrange(osize):
                val = oi.next()
                for j in xrange(repeats[i]):
                    ri.set_next(val)
            return res
    else:
        oshape = obj.shape
#         olen = len(obj)
        if type(repeats) is int:
            olen = oshape[axis]
            nlen = olen * repeats
            ns = copy.copy(oshape)
            ss = copy.copy(oshape)
            ns[axis] = nlen
            ss[axis] = 1
            res = zeros(ns, obj.dtype)
            ri = res.section_iter(ss)
            oi = obj.section_iter(ss)
            for i in xrange(olen):
                val = oi.next()
                for j in xrange(repeats):
                    r = ri.next()
                    r[:] = val
            return res
        else:
            nlen = sum(repeats)
            rlen = len(repeats)
            ns = copy.copy(oshape)
            ss = copy.copy(oshape)
            ns[axis] = nlen
            ss[axis] = 1
            res = zeros(ns, obj.dtype)
            ri = res.section_iter(ss)
            oi = obj.section_iter(ss)
            for i in xrange(rlen):
                val = oi.next()
                for j in xrange(repeats[i]):
                    r = ri.next()
                    r[:] = val
            return res
        
    
#####################################################################################
# Adding and removing elements
#####################################################################################    
def delete(arr, obj, axis=None) :
    if axis is None :
        afl = arr.flatten()
        if type(obj) is int :
            if obj < 0 :
                obj = obj + afl.size
            elif obj >= afl.size :
                return afl
            if afl.size == 1 and obj == 0 :
                return instance([0], 0, arr.dtype)
            narr = instance([afl.size - 1], 0, arr.dtype)
            if obj == 0 :
                return narr[1:]
            elif obj == afl.size - 1 :
                narr.copy_from(arr)
                return narr
            else :
                narr[:obj].copy_from(arr[:obj])
                narr[obj:].copy_from(arr[obj + 1:])
                return narr
        elif type(obj) is slice :
            orange = range(afl.size)
            ndel = orange[obj]
            for val in ndel :
                orange.remove(val)
            nshape = [len(orange)]
            narr = instance(nshape, 0, arr.dtype)
            for i in xrange(len(orange)) :
                narr[i] = arr[orange[i]]
            return narr
        elif hasattr(obj, '__len__') :
            cobj = copy.copy(obj)
            nrange = range(arr.size)
            for val in cobj :
                if val < 0 :
                    val += arr.size
                nrange.remove(val)
            narr = instance([len(nrange)], 0, arr.dtype)
            for i in xrange(narr.size) :
                narr[i] = afl[nrange[i]]
            return narr
    elif axis >= arr.ndim :
        raise ValueError, 'axis must be within the ndim of the array, ' + str(axis) \
                    + ' in ' + str(arr.ndim)
    else :
        if type(obj) is int :
            oshape = arr.shape
            if obj >= oshape[axis] :
                raise ValueError, 'invalid entry at the axis ' + str(axis) \
                        + ', index out of range' + str(obj) + ' in ' \
                        + str(oshape[axis])
            elif obj < 0 :
                obj += oshape[axis]
            if obj < 0 :
                raise ValueError, 'invalid entry'
            if oshape[axis] == 1 :
                nshape = copy.copy(oshape)
                nshape[axis] = 0
                return instance(nshape, 0, arr.dtype)
            else :
                nshape = copy.copy(oshape)
                nshape[axis] = oshape[axis] - 1
                narr = instance(nshape, 0, arr.dtype)
                org = [0] * arr.ndim
                sshape = copy.copy(oshape)
                if obj > 0 :
                    sshape[axis] = obj
                    narr.get_section(org, sshape).copy_from(\
                            arr.get_section(org, sshape))
                if obj < oshape[axis] - 1 :
                    org[axis] = obj
                    sshape[axis] = oshape[axis] - obj - 1
                    oorg = [0] * arr.ndim
                    oorg[axis] = obj + 1
                    narr.get_section(org, sshape).copy_from(\
                            arr.get_section(oorg, sshape))
                else :
                    narr.copy_from(arr) 
                return narr
        elif type(obj) is slice:
            oshape = arr.shape
            osize = oshape[axis]
            nrange = range(osize)
            ndel = nrange[obj]
            for val in ndel :
                nrange.remove(val)
            nshape = copy.copy(oshape)
            nshape[axis] = len(nrange)
            narr = instance(nshape, 0, arr.dtype)
            for i in xrange(len(nrange)) :
                if arr.ndim == 1 :
                    narr[i] = arr[nrange[i]]
                else :
                    narr.get_slice(axis, i).copy_from(
                        arr.get_slice(axis, nrange[i]))
            return narr
        elif hasattr(obj, '__len__') :
            oshape = arr.shape
            osize = oshape[axis]
            nrange = range(osize)
            for val in obj :
                if val < 0 :
                    val += osize
                nrange.remove(val)
            nshape = copy.copy(oshape)
            nshape[axis] = len(nrange)
            narr = instance(nshape, 0, arr.dtype)
            for i in xrange(len(nrange)) :
                if arr.ndim == 1 :
                    narr[i] = arr[nrange[i]]
                else :
                    narr.get_slice(axis, i).copy_from(
                        arr.get_slice(axis, nrange[i]))
            return narr

def insert(arr, index, values, axis=None):
    if axis is None:
        osize = arr.size
        if type(index) is int:
            if hasattr(values, '__len__'):
                dim = get_ndim(values)
                if dim > 1:
                    values = Array(values)
                nsize = osize + len(values)
                res = zeros([nsize], arr.dtype)
                oi = arr.item_iter()
                ri = res.item_iter()
                vi = iter(values)
                for i in xrange(index):
                    ri.set_next(oi.next())
                for i in xrange(len(values)):
                    ri.set_next(vi.next())
                for i in xrange(index, osize):
                    ri.set_next(oi.next())
                return res
            else:
                nsize = osize + 1
                res = zeros([nsize], arr.dtype)
                oi = arr.item_iter()
                ri = res.item_iter()
                for i in xrange(index):
                    ri.set_next(oi.next())
                ri.set_next(values)
                for i in xrange(index, osize):
                    ri.set_next(oi.next())
                return res
        elif hasattr(index, '__len__'):
            if hasattr(values, '__len__'):
                nsize = osize + len(index)
                res = zeros([nsize], arr.dtype)
                oi = arr.item_iter()
                ri = res.item_iter()
                start = 0
                for i in xrange(len(index)):
                    for j in xrange(start, index[i]):
                        ri.set_next(oi.next())
                    ri.set_next(values[i])
                    start = index[i]
                for i in xrange(start, osize):
                    ri.set_next(oi.next())
                return res
            else:
                nsize = osize + len(index)
                res = zeros([nsize], arr.dtype)
                oi = arr.item_iter()
                ri = res.item_iter()
                start = 0
                for i in xrange(len(index)):
                    for j in xrange(start, index[i]):
                        ri.set_next(oi.next())
                    ri.set_next(values)
                    start = index[i]
                for i in xrange(start, osize):
                    ri.set_next(oi.next())
                return res
    else:
        oshape = arr.shape
        odim = arr.ndim
        if type(index) is int:
            if hasattr(values, '__len__'):
                values = Array(values)
                nshape = copy.copy(oshape)
                ishape = copy.copy(oshape)
                ishape[axis] = 1
                olen = oshape[axis]
                nshape[axis] = oshape[axis] + 1
                res = zeros(nshape, arr.dtype)
                oi = arr.section_iter(ishape)
                ri = res.section_iter(ishape)
                for i in xrange(index):
                    os = oi.next()
                    rs = ri.next()
                    rs.copy_from(os)
                rs = ri.next()
                rs.copy_from(values)
                for i in xrange(index, olen):
                    os = oi.next()
                    rs = ri.next()
                    rs.copy_from(os)
                return res
            else:
                nshape = copy.copy(oshape)
                ishape = copy.copy(oshape)
                ishape[axis] = 1
                olen = oshape[axis]
                nshape[axis] = oshape[axis] + 1
                res = zeros(nshape, arr.dtype)
                oi = arr.section_iter(ishape)
                ri = res.section_iter(ishape)
                for i in xrange(index):
                    os = oi.next()
                    rs = ri.next()
                    rs.copy_from(os)
                rs = ri.next()
                rs.fill(values)
                for i in xrange(index, olen):
                    os = oi.next()
                    rs = ri.next()
                    rs.copy_from(os)
                return res
        elif hasattr(index, '__len__'):
            if hasattr(values, '__len__'):
                values = Array(values)
                vshape = values.shape
                vdim = values.ndim
                nshape = copy.copy(oshape)
                ishape = copy.copy(oshape)
                ishape[axis] = 1
                olen = oshape[axis]
#                 if vdim == odim:
#                     nlen = vshape[axis]
#                 elif vdim == odim - 1:
#                     nlen = 1
                nlen = len(index)
                nshape[axis] = oshape[axis] + nlen
                res = zeros(nshape, arr.dtype)
                oi = arr.section_iter(ishape)
                ri = res.section_iter(ishape)
                
                if values.ndim == arr.ndim:
                    if vshape[axis] == nlen:
                        vi = values.section_iter(ishape)
                        start = 0
                        for i in xrange(len(index)):
                            for j in xrange(start, index[i]):
                                os = oi.next()
                                rs = ri.next()
                                rs.copy_from(os)
                            rs = ri.next()
                            vs = vi.next()
                            rs.copy_from(vs)
                            start = index[i]
                        for i in xrange(start, olen):
                            os = oi.next()
                            rs = ri.next()
                            rs.copy_from(os)
                    elif vshape[axis] == 1:
                        start = 0
                        for i in xrange(len(index)):
                            for j in xrange(start, index[i]):
                                os = oi.next()
                                rs = ri.next()
                                rs.copy_from(os)
                            rs = ri.next()
                            rs.copy_from(values)
                            start = index[i]
                        for i in xrange(start, olen):
                            os = oi.next()
                            rs = ri.next()
                            rs.copy_from(os)
                elif values.ndim == 1 and len(values) == nlen:
                    start = 0
                    for i in xrange(len(index)):
                        for j in xrange(start, index[i]):
                            os = oi.next()
                            rs = ri.next()
                            rs.copy_from(os)
                        rs = ri.next()
                        rs.copy_from(values[i])
                        start = index[i]
                    for i in xrange(start, olen):
                        os = oi.next()
                        rs = ri.next()
                        rs.copy_from(os)                     
                elif values.ndim == arr.ndim - 1:
                    start = 0
                    for i in xrange(len(index)):
                        for j in xrange(start, index[i]):
                            os = oi.next()
                            rs = ri.next()
                            rs.copy_from(os)
                        rs = ri.next()
                        rs.copy_from(values)
                        start = index[i]
                    for i in xrange(start, olen):
                        os = oi.next()
                        rs = ri.next()
                        rs.copy_from(os)   
                return res
            else:
                nshape = copy.copy(oshape)
                ishape = copy.copy(oshape)
                ishape[axis] = 1
                olen = oshape[axis]
                nshape[axis] = oshape[axis] + len(index)
                res = zeros(nshape, arr.dtype)
                oi = arr.section_iter(ishape)
                ri = res.section_iter(ishape)
                start = 0
                for i in xrange(len(index)):
                    for j in xrange(start, index[i]):
                        os = oi.next()
                        rs = ri.next()
                        rs.copy_from(os)
                    rs = ri.next()
                    rs.fill(values)
                    start = index[i]
                for i in xrange(start, olen):
                    os = oi.next()
                    rs = ri.next()
                    rs.copy_from(os)
                return res
        
def sort(arr, axis=-1, reverse=False):
    out = instance(arr.shape, dtype = arr.dtype)
    if axis is None:
        f = arr.flatten()
        r = sorted(f)
        out[:] = r
        return out
    if axis < 0:
        axis = arr._ndim + axis
    if axis < 0 or axis >= arr._ndim:
        raise ValueError, 'axis out of range'
    sshape = [1] * arr._ndim
    sshape[axis] = arr.shape[axis]
    si = out.section_iter(sshape)
    while si.has_next():
        ss = si.next().get_reduced()
        r = sorted(ss, reverse = reverse)
        ss[:] = r
    return out
                
#####################################################################################
# Array modification
#####################################################################################
def put(arr, indices, values, mode='raise') :
    arr.put(indices, values, mode)
#####################################################################################
# Array output
#####################################################################################
def tolist(arr):
    return arr.tolist()
            
#####################################################################################
# Array math
#####################################################################################
def exp(obj):
    return obj.exp()
    
def log10(obj):
    return obj.log10()
    
def ln(obj):
    return obj.ln()

def sqrt(obj):
    return obj.sqrt()

def sin(obj):
    return obj.__sin__()

def max(arr, axis = None, out = None):
        if isinstance(arr, Array) :
            return arr.max(axis, out)
        else :
            raise TypeError, 'type not supported: ' + str(type(arr))
    
def min(arr, axis = None, out = None):
    if isinstance(arr, Array) :
        arr.min(axis, out)
    else :
        raise TypeError, 'type not supported: ' + str(type(arr))

def array_equal(a1, a2, equal_nan=False):
    if a1.shape != a2.shape:
        return False
    a1i = a1.item_iter()
    a2i = a2.item_iter()
    if equal_nan:
        while a1i.has_next():
            x = a1i.next()
            y = a2i.next()
            if x != y:
                if Double.isNaN(x) and Double.isNaN(y) :
                    continue
                else:
                    return False
    else:
        while a1i.has_next():
            if a1i.next() == a2i.next():
                continue
            else:
                return False
    return True

def allclose(a, b, rtol=1e-05, atol=1e-08, equal_nan=False):
    if a.shape != b.shape:
        return False
    ai = a.item_iter()
    bi = b.item_iter()
    if equal_nan:
        while ai.has_next():
            x = ai.next()
            y = bi.next()
            if x != y:
                if Double.isNaN(x) and Double.isNaN(y) :
                    continue
                if abs(x - y) > (atol + rtol * abs(y)) :
                    continue
                else:
                    return False
    else:
        while ai.has_next():
            x = ai.next()
            y = bi.next()
            if x != y:
                if abs(x - y) < (atol + rtol * abs(y)) :
                    continue
                else:
                    return False
    return True

def broadcast_to(array, shape):
    oshape = array.shape
    ishape = [1] * (len(shape) - array.ndim) + oshape
    res = zeros(shape, dtype = array.dtype)
    si = res.section_iter(ishape)
    while si.has_next():
        sec = si.next()
        sec.copy_from(array)
    return res
    
#####################################################################################
# Other utility classes
#####################################################################################

class Slice_ :
    def __init__(self) :
        pass
    
    def __getitem__(self, index):
        return index

s_ = Slice_()

class NexusException(Exception):
    def __init__(self, t = ''):
        self.t = t
        
    def __str__(self, *args, **kwargs):
        return str(self.t)

#####################################################################################
# Numpy standards
#####################################################################################
empty = instance