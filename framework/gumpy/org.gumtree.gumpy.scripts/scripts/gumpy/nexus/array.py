from gumpy.commons import jutils
from org.gumtree.data import DataType
from org.gumtree.data.utils import FactoryManager, Utilities
from symbol import except_clause
import copy
import math
import random


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
        self.dtype = None
        if hasattr(obj, '__len__') :
#            if len(obj) == 0 :
#                raise Exception, 'empty list not allowed'
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
                self.dtype = int
                for id in xrange(size) :
                    val = get_item(obj, id, rawshape)
                    if type(val) is int :
                        continue
                    elif type(val) is float :
                        tp = DataType.DOUBLE
                        self.dtype = float
                        break
                    elif type(val) is str :
                        tp = DataType.CHAR
                        self.dtype = str
                        break
                    elif type(val) is bool :
                        tp = DataType.BOOLEAN
                        self.dtype = bool
                        break
                    elif type(val) is long :
                        tp = DataType.LONG
                        self.dtype = long
                        break
                    else :
                        tp = DataType.STRING
                        self.dtype = object
                        break
                if self.dtype is str :
                    if not type(obj) is str :
                        tp = DataType.STRING
                        self.dtype = object
            else :
                self.dtype = dtype
                if dtype is int :
                    tp = DataType.INT
                elif dtype is float :
                    tp = DataType.DOUBLE
                elif dtype is long :
                    tp = DataType.LONG
                elif dtype is bool :
                    tp = DataType.BOOLEAN
                elif dtype is str :
                    tp = DataType.CHAR
                else :
                    tp = DataType.STRING
            iArray = gdm_factory.createArray(tp.getPrimitiveClassType(), jshape)
            iter = iArray.getIterator()
            if self.dtype is int :
                for id in xrange(size) :
                    val = get_item(obj, id, rawshape)
                    iter.next().setIntCurrent(val)
            elif self.dtype is float :
                for id in xrange(size) :
                    val = get_item(obj, id, rawshape)
                    iter.next().setDoubleCurrent(float(val))
            elif self.dtype is bool :
                for id in xrange(size) :
                    val = get_item(obj, id, rawshape)
                    iter.next().setBooleanCurrent(bool(val))
            elif self.dtype is long :
                for id in xrange(size) :
                    val = get_item(obj, id, rawshape)
                    iter.next().setLongCurrent(long(val))
            elif self.dtype is str :
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
        self.size = int(iArray.getSize())
        self.ndim = iArray.getRank()
        jshape = iArray.getShape()
        self.shape = []
        for counter in range(self.ndim):
            dim = jshape[counter]
            self.shape.append(dim)
        try :
            tp = DataType.getType(iArray.getElementType())
        except :
            tp = DataType.STRING
        if tp.equals(DataType.INT) :
            self.get_value = self.get_int
            self.set_prime_value = self.set_int_value
            self.dtype = int
        elif tp.equals(DataType.DOUBLE) :
            self.get_value = self.get_float
            self.set_prime_value = self.set_float_value
            self.dtype = float
        elif tp.equals(DataType.BOOLEAN) :
            self.get_value = self.get_bool
            self.set_prime_value = self.set_bool_value
            self.dtype = bool
        elif tp.equals(DataType.LONG) :
            self.get_value = self.get_long
            self.set_prime_value = self.set_long_value
            self.dtype = long
        elif tp.equals(DataType.CHAR) :
            self.get_value = self.get_char
            self.set_prime_value = self.set_char_value
            self.dtype = str
        elif tp.equals(DataType.BYTE) :
            self.get_value = self.get_int
            self.set_prime_value = self.set_int_value
            self.dtype = int
        elif tp.equals(DataType.SHORT) :
            self.get_value = self.get_int
            self.set_prime_value = self.set_int_value
            self.dtype = int
        elif tp.equals(DataType.FLOAT) :
            self.get_value = self.get_float
            self.set_prime_value = self.set_float_value
            self.dtype = float
        else :
            self.get_value = self.get_str
            self.set_prime_value = self.set_str_value
            self.dtype = object
    
#####################################################################################
#   Array indexing
#####################################################################################
    def __getitem__(self, index):
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
                nsize = len(indices)
                if out is None :
                    out = instance([nsize], 0, self.dtype)
                for i in xrange(nsize) :
                    out[i] = afl[indices[i]]
                return out
        elif axis >= self.ndim :
            raise ValueError, 'axis must be within the ndim of the array, ' + str(axis) \
                        + ' in ' + str(self.ndim)
        else :
            tp = ()
            shape = self.shape
            for i in xrange(self.ndim) :
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
                osize = self.shape[axis]
                nshape = copy.copy(self.shape)
                nshape[axis] = nsize
                if out is None :
                    out = instance(nshape, 0, self.dtype)
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
                    if self.ndim == 1 :
                        out[i] = self[indices[i]]
                    else :
                        out.get_slice(axis, i).copy_from(
                            self.get_slice(axis, indices[i]))
                return out
            
#####################################################################################
# Array accessing
#####################################################################################    
    def __len__(self):
        return self.shape[0]
    
#    def __getattr__(self, name):
##        raise AttributeError(name + ' not exists')
#        return None
    
    def __iter__(self):
        if (self.ndim > 1) :
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
            return self.__iArray__.getObject(self.__iArray__.getIndex().set(index)).toString()
        elif type(index) is list :
            return self.__iArray__.getObject(self.__iArray__.getIndex().set(jutils.jintcopy(index))).toString()

    def set_value(self, index, value):
        if type(index) is int :
            i = index
        elif type(index) is list :
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
        ntype = self.dtype
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

    def __or__(self, obj):
        if isinstance(obj, Array) :
            if self.shape != obj.shape :
                raise ValueError, 'dimension does not match'
            res = instance(self.shape, dtype = bool)
            siter = self.item_iter()
            oiter = obj.item_iter()
            riter = res.item_iter()
            try :
                if self.dtype is float :
                    prc = 10 ** (-Array.precision)
                    while True :
                        riter.set_next(abs(siter.next() - oiter.next()) > prc)
                else :
                    while True :
                        riter.set_next(siter.next() > oiter.next())
            except :
                pass
            return res
        else :
            if hasattr(obj, '__len__') :
                return self == Array(obj)
            else :
                res = instance(self.shape, dtype = bool)
            siter = self.item_iter()
            riter = res.item_iter()
            try :
                if self.dtype is float :
                    prc = 10 ** (-Array.precision)
                    while True :
                        riter.set_next(abs(siter.next() - obj) > prc)
                else :
                    while True :
                        riter.set_next(siter.next() > obj)
            except :
                pass
            return res
        
    def count_nonzero(self):
        dtype = self.dtype
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
        
    def transpose(self, axes = None):
        if axes is None:
            dim1 = self.ndim - 1
            dim2 = self.ndim - 2
        elif not hasattr(axes, '__len__'):
            raise Exception, 'axes parameter must be either a list or a tuple with length of 2'
        elif len(axes) != 2 :
            raise Exception, 'axes parameter must be either a list or a tuple with length of 2'
        else :
            dim1 = axes[0]
            dim2 = axes[1]
        if dim2 >= self.ndim :
            raise Exception, 'dimension ' + str(dim2) + ' is not available'
        if dim1 >= self.ndim :
            raise Exception, 'dimension ' + str(dim1) + ' is not available'
        return Array(self.__iArray__.getArrayUtils().transpose(dim1, dim2).getArray())
        
    def compress(self, condition, axis = None, out = None):
        if axis is None:
            osize = self.size
            if osize > len(condition):
                osize = len(condition)
            idx = 0
            storage = []
            it = self.item_iter()
            while idx < osize:
                if condition[idx] :
                    storage.append(it.next())
                else :
                    it.next()
                idx += 1
            return Array(storage, dtype = self.dtype)
        else:
            osize = self.shape[axis]
            if osize > len(condition):
                osize = len(condition)
            nsize = 0
            for i in xrange(osize) :
                if condition[i] :
                    nsize += 1
            if nsize == 0:
                return null
            nshape = copy.copy(self.shape)
            nshape[axis] = nsize
            narr = instance(nshape, dtype = self.dtype)
            idx = 0
            for i in xrange(osize):
                if condition[i] :
                    narr.get_slice(axis, idx).copy_from(self.get_slice(axis, i))
                    idx += 1
            return narr
    
    def clip(self, a_min, a_max, out = None):
        if out is None:
            out = instance(self.shape, dtype = self.dtype)
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
            dsize = self.size
        else :
            dsize = 1
            for i in xrange(self.ndim):
                if i != axis:
                    dsize *= self.shape[i]
        return dsum / dsize
    
    def all(self):
        siter = self.item_iter()
        try :
            while True :
                if not siter.next() :
                    return False
        except :
            pass
        return True
    
    def any(self):
        siter = self.item_iter()
        try :
            while True :
                if siter.next() :
                    return True
        except :
            pass
        return False
    
        
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
#        res = zeros(self.shape, self.__match_type__(obj))
#        riter = res.item_iter()
#        siter = self.item_iter()
#        if hasattr(obj, '__len__') :
#            if len(obj) < self.size :
#                raise Exception, 'resource should have at least ' + \
#                    str(self.size) + ' items, got ' + str(len(obj))
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
#            if len(obj) < self.size :
#                raise Exception, 'resource should have at least ' + \
#                    str(self.size) + ' items, got ' + str(len(obj))
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
    
    def __rdiv__(self, obj):
        res = zeros(self.shape, self.__match_type__(obj, True))
        riter = res.item_iter()
        siter = self.item_iter()
        if hasattr(obj, '__len__') :
            if len(obj) < self.size :
                raise Exception, 'resource should have at least ' + \
                    str(self.size) + ' items, got ' + str(len(obj))
            try :
                rawshape = get_shape(obj)
                for id in xrange(get_size(obj, rawshape)) :
                    nval = siter.next()
                    if nval != 0 :
                        riter.set_next(get_item(obj, id, rawshape) / nval)
                    else :
                        riter.next()
            except StopIteration:
                pass
        else :
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
#        res = zeros(self.shape, self.__match_type__(obj))
#        riter = res.item_iter()
#        siter = self.item_iter()
#        if hasattr(obj, '__len__') :
#            if len(obj) < self.size :
#                raise Exception, 'resource should have at least ' + \
#                    str(self.size) + ' items, got ' + str(len(obj))
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
        return self * -1
    
    def __sub__(self, obj):
        return self.__add__(obj * -1)
#        res = zeros(self.shape, self.__match_type__(obj))
#        riter = res.item_iter()
#        siter = self.item_iter()
#        if hasattr(obj, '__len__') :
#            if len(obj) < self.size :
#                raise Exception, 'resource should have at least ' + \
#                    str(self.size) + ' items, got ' + str(len(obj))
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
        res = zeros(self.shape, self.__match_type__(obj))
        riter = res.item_iter()
        siter = self.item_iter()
        if hasattr(obj, '__len__') :
            if len(obj) < self.size :
                raise Exception, 'resource should have at least ' + \
                    str(self.size) + ' items, got ' + str(len(obj))
            try :
                rawshape = get_shape(obj)
                for id in xrange(get_size(obj, rawshape)) :
                    riter.set_next(get_item(obj, id, rawshape) - siter.next())
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
        if self.dtype is bool :
            res = instance(self.shape, dtype = bool)
            siter = self.item_iter()
            riter = res.item_iter()
            while siter.has_next() :
                riter.set_next(not siter.next())
            return res
        else :
            res = instance(self.shape, dtype = self.dtype)
            siter = self.item_iter()
            riter = res.item_iter()
            while siter.has_next() :
                riter.set_next(~siter.next())
            return res
            
    def __pow__(self, obj):
        res = zeros(self.shape, self.__match_type__(obj))
        riter = res.item_iter()
        siter = self.item_iter()
        if hasattr(obj, '__len__') :
            if len(obj) < self.size :
                raise Exception, 'resource should have at least ' + \
                    str(self.size) + ' items, got ' + str(len(obj))
            try :
                rawshape = get_shape(obj)
                for id in xrange(get_size(obj, rawshape)) :
                    riter.set_next(siter.next() ** get_item(obj, id, rawshape))
            except StopIteration:
                pass
        else :
            try :
                while True :
                    riter.set_next(siter.next() ** obj)
            except StopIteration:
                pass
        return res
    
    def exp(self):
        return Array(self.__iArray__.getArrayMath().toExp().getArray())

    def log10(self):
        return Array(self.__iArray__.getArrayMath().toLog10().getArray())
    
    def ln(self):
        return Array(self.__iArray__.getArrayMath().toLn().getArray())

    def sqrt(self):
        return Array(self.__iArray__.getArrayMath().toSqrt().getArray())
        
    def __rpow__(self, obj):
        res = zeros(self.shape, self.__match_type__(obj))
        riter = res.item_iter()
        siter = self.item_iter()
        if hasattr(obj, '__len__') :
            if len(obj) < self.size :
                raise Exception, 'resource should have at least ' + \
                    str(self.size) + ' items, got ' + str(len(obj))
            try :
                rawshape = get_shape(obj)
                for id in xrange(get_size(obj, rawshape)) :
                    riter.set_next(get_item(obj, id, rawshape) ** siter.next())
            except StopIteration:
                pass
        else :
            try :
                while True :
                    riter.set_next(obj ** self.next())
            except StopIteration:
                pass
        return res
    
    def __mod__(self, obj):
        res = zeros(self.shape, self.__match_type__(obj, True))
        riter = res.item_iter()
        siter = self.item_iter()
        if hasattr(obj, '__len__') :
            if len(obj) < self.size :
                raise Exception, 'resource should have at least ' + \
                    str(self.size) + ' items, got ' + str(len(obj))
            try :
                rawshape = get_shape(obj)
                for id in xrange(get_size(obj, rawshape)) :
                    val = get_item(obj, id, rawshape)
                    if val != 0 :
                        riter.set_next(siter.next() % val)
                    else :
                        riter.next()
            except StopIteration:
                pass                    
        else :
            if obj != 0 :
                try :
                    while True :
                        res.set_next(self.next() % obj)
                except StopIteration:
                    pass
        return res

    def __rmod__(self, obj):
        res = zeros(self.shape, self.__match_type__(obj, True))
        riter = res.item_iter()
        siter = self.item_iter()
        if hasattr(obj, '__len__') :
            if len(obj) < self.size :
                raise Exception, 'resource should have at least ' + \
                    str(self.size) + ' items, got ' + str(len(obj))
            try :
                rawshape = get_shape(obj)
                for id in xrange(get_size(obj, rawshape)) :
                    nval = self.next()
                    if nval != 0 :
                        riter.set_next(get_item(obj, id, rawshape) % nval)
                    else :
                        riter.next()
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
        if self.size is 0 :
            return 0
        res = 1
        siter = self.item_iter()
        try :
            while True :
                res *= siter.next()
        except StopIteration:
            pass
        return res
    
    def max(self, axis = None, out = None):
        if axis is None :
            return self.__iArray__.getArrayMath().getMaximum()
        else :
            if axis >= self.ndim :
                raise ValueError, 'axis index out of stack, ' + str(axis) + \
                                    ' in ' + str(self.ndim)
            asize = self.shape[axis]
            if out is None :
                out = instance([asize], 0, self.dtype)
                for id in xrange(asize) :
                    out[id] = self.get_slice(axis, id).max()
            return out
    
    def min(self, axis = None, out = None):
        if axis is None :
            return self.__iArray__.getArrayMath().getMinimum()
        else :
            if axis >= self.ndim :
                raise ValueError, 'axis index out of stack, ' + str(axis) + \
                                    ' in ' + str(self.ndim)
            asize = self.shape[axis]
            if out is None :
                out = instance([asize], 0, self.dtype)
                for id in xrange(asize) :
                    out[id] = self.get_slice(axis, id).min()
            return out
    
    def sum(self, axis=None, dtype=None, out=None):
        if axis is None :
            s = self.__iArray__.getArrayMath().sum()
            if dtype is int :
                return int(s)
            elif dtype is long :
                return long(s)
            else :
                return s
        else :
            if axis >= self.ndim :
                raise ValueError, 'index out of bound, ' + str(axis) + ' in ' + str(self.ndim)
            nsize = self.shape[axis]
            if out is None :
                out = instance([nsize], 0, dtype = dtype)
            for i in xrange(nsize) :
                out[i] = self.get_slice(axis, i).sum(dtype = dtype)
            return out

#********************************************************************************
#     Array utilities
#********************************************************************************
    
    def __repr__(self, indent = None, skip = True):
        if self.size == 0 :
            return 'Array([], shape=' + str(self.shape) + \
                    ', dtype=' + self.dtype.__name__ + ')'
        if skip :
            skip = self.size > Array.threshold
        amax = 0
        amin = 0
        if self.dtype is int or self.dtype is long :
            amax = self.max()
            amin = self.min()
            if amax < 0 :
                amax = -amin
        if indent is None :
            nindent = ' ' * 6
        else :
            nindent = indent + ' ' * 6;
        return 'Array(' + self.__string__(0, skip, True, indent = nindent, \
                                          max = amax, min = amin) + ')'
    
    def __str__(self, indent = '', skip = True):
        if self.dtype is str :
            return self.__iArray__.toString()
        if self.size == 0 :
            return indent + '[]'
        if skip :
            skip = self.size > Array.threshold
        amax = 0
        amin = 0
        if self.dtype is int or self.dtype is long :
            amax = self.max()
            amin = self.min()
            if amax < 0 :
                amax = -amin
        return self.__string__(0, skip, max = amax, min = amin, indent = indent)
    
    def __string__(self, level, skip = False, sep = False, indent = '', max = 0, min = 0):
        if self.ndim > 1 :
            cmark = ''
            if sep :
                cmark += ','
            cmark += '\n' + indent
            if self.ndim >= 3 :
                cmark += '\n' + indent
            cmark += ' ' * (level + 1)
            abbrv = '...,'
        elif sep or self.dtype is float :
            cmark = ', '
            abbrv = '...'
        else :
            cmark = ' '
            abbrv = '...,'
        close = ']'
        result = '['
        fm = ''
        if self.dtype is int or self.dtype is long:
            if max == 0 :
                max = 1
            sign = 0
            if min < 0 :
                sign = 1
            fm = '%#' + str(int(math.ceil(math.log10(max))) + sign) + 'i'
        elif self.dtype is float :
            fm = '%#.' + str(Array.precision) + 'f'
        else :
            fm = '%s'
        size = self.shape[0]
        if self.ndim <= 1 :
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
                result += self[0].__string__(level + 1, skip, sep, indent, max, min) + \
                    cmark + self[1].__string__(level + 1, skip, sep, indent, max, min) + \
                    cmark + self[2].__string__(level + 1, skip, sep, indent, max, min) + \
                    cmark + abbrv + cmark + \
                            self[size - 3].__string__(level + 1, skip, sep, indent, max, min) + \
                    cmark + self[size - 2].__string__(level + 1, skip, sep, indent, max, min) + \
                    cmark + self[size - 1].__string__(level + 1, skip, sep, indent, max, min) + close
            else :
                for i in xrange(size - 1) :
#                    if i > 0 :
#                        result += '\t'
                    result += self[i].__string__(level + 1, skip, sep,  indent, max, min) + cmark
                result += self[size - 1].__string__(level + 1, skip, sep, indent, max, min) + close
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
            idx = 0
            iter = self.item_iter()
            while iter.has_next():
                nv = iter.next()
                if nv > val:
                    val = nv
                    found = idx
                idx += 1
            return found
        else:
            if axis >= self.ndim:
                raise Exception, 'axis out of range'
            if self.ndim == 1:
                return self.argmax()
            res_shape = copy.copy(self.shape)
            res_shape.pop(axis)
            res = zeros(res_shape, int)
            res_iter = res.item_iter()
            sec_iter_shape = [1] * self.ndim
            sec_iter_shape[axis] = self.shape[axis]
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
            while iter.has_next():
                nv = iter.next()
                if nv < val:
                    val = nv
                    found = idx
                idx += 1
            return found
        else:
            if axis >= self.ndim:
                raise Exception, 'axis out of range'
            if self.ndim == 1:
                return self.argmin()
            res_shape = copy.copy(self.shape)
            res_shape.pop(axis)
            res = zeros(res_shape, int)
            res_iter = res.item_iter()
            sec_iter_shape = [1] * self.ndim
            sec_iter_shape[axis] = self.shape[axis]
            sec_iter = self.section_iter(sec_iter_shape)
            while sec_iter.has_next():
                sec = sec_iter.next()
                res_iter.set_next(sec.argmin())
            return res
        
#####################################################################################
#   Array modification
#####################################################################################    

    def __setitem__(self, index, value):
        if type(index) is int :
            if self.ndim == 1 :
                self.set_value(index, value);
            else :
                self.get_slice(0, index).copy_from(value)
        elif type(index) is slice :
            self.__getitem__(index).copy_from(value)
        elif type(index) is tuple :
            secflag = 0
            if len(index) == self.ndim :
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
    
    def copy_from(self, value, length = -1):
        if value.__class__ is Array :
            Utilities.copyTo(value.__iArray__, self.__iArray__, length)
        elif type(value) is int or type(value) is float or type(value) is long :
            self.fill(value)
        else :
#            try :
#                get_item(value, self.size - 1)
#            except :
#                raise Exception, 'resource should have at least ' + \
#                    str(self.size) + ' items'
            siter = self.item_iter()
            if length <= 0 :
                length = self.size
            elif length > self.size :
                length = self.size
            for id in xrange(length) :
                siter.set_next(get_item(value, id))
                    
    def fill(self, val):
        siter = self.item_iter()
        try :
            while True :
                siter.set_next(val)
        except StopIteration :
            pass
            
    def put(self, indices, values, mode='raise') :
#        v1 = self.view_1d()
        if type(indices) is int :
            if indices >= self.size :
                if mode == 'clip' :
                    indices = self.size - 1
                elif mode == 'wrap' :
                    indices %= self.size
                else :
                    raise ValueError, 'index out of range: ' + str(indices) + ' in ' \
                            + str(self.size)
            if indices < 0 :
                indices += self.size
                if indices < 0 :
                    if mode == 'clip' :
                        indices = 0
                    elif mode == 'wrap' :
                        indices %= self.size
                    else :
                        raise ValueError, 'index out of range: ' + str(indices - self.size) \
                            + ' in ' + str(self.size)
            idx = jutils.jintcopy(get_index_1d_to_nd(indices, self.shape))
            if hasattr(values, '__len__') :
                self.set_value(idx, values[0])
            else :
                self.set_value(idx, values)
        elif type(indices) is slice :
            start = indices.start
            if start is None :
                start = 0
            elif start >= self.size :
                raise ValueError, 'index out of range'
            elif start <= -self.size :
                start = -self.size
            stop = indices.stop
            if stop is None :
                stop = self.size
            elif stop > self.size :
                stop = self.size
            step = indices.step
            if step is None :
                step = 1
            vid = 0
            for i in xrange(start, stop, step) :
                idx = jutils.jintcopy(get_index_1d_to_nd(i, self.shape))
                if hasattr(values, '__len__') :
                    self.set_value(idx, values[vid % len(values)])
                    vid += 1
                else :
                    self.set_value(idx, values)
        elif hasattr(indices, '__len__') :
            vid = 0
            for i in xrange(len(indices)) :
                val = indices[i]
                if val >= self.size :
                    if mode == 'clip' :
                        val = self.size - 1
                    elif mode == 'wrap' :
                        val %= self.size
                    else :
                        raise ValueError, 'index out of range: ' + str(val) + ' in ' \
                                + str(self.size)
                if val < 0 :
                    val += self.size
                    if val < 0 :
                        if mode == 'clip' :
                            val = 0
                        elif mode == 'wrap' :
                            val %= self.size
                        else :
                            raise ValueError, 'index out of range: ' + str(indices[i]) \
                                + ' in ' + str(self.size)
                idx = jutils.jintcopy(get_index_1d_to_nd(val, self.shape))
                if hasattr(values, '__len__') :
                    self.set_value(idx, values[vid % len(values)])
                    vid += 1
                else :
                    self.set_value(idx, values)

#####################################################################################
#   Reinterpreting arrays
#####################################################################################    
    def reshape(self, shape): 
        if type(shape) is list :
            jshape = jutils.jintcopy(shape)
            return Array(self.__iArray__.getArrayUtils().reshape(jshape).getArray())
        else :
            raise Exception, 'unsupported type : ' + str(type(shape))
    
    def flatten(self) :
        shape = [self.size]
        arr = instance(shape, 0, self.dtype)
        arr.copy_from(self)
        return arr

    def view_1d(self):
        ns = [self.size]
        return self.reshape(ns)
    
    def __copy__(self):
        return Array(self.__iArray__.copy())

    def __deepcopy__(self):
        return self.__copy__()
    
    def float_copy(self):
        return Array(Utilities.copyToDoubleArray(self.__iArray__))

    def positive_float_copy(self):
        return Array(Utilities.copyToPositiveDoubleArray(self.__iArray__))
    
    def __dir__(self):
        dirs = []
        dirs.append('copy_from')
        dirs.append('dtype')
        dirs.append('exp')
        dirs.append('fill')
        dirs.append('flatten')
        dirs.append('float_copy')
#        if self.dtype is bool :
#            dirs.append('get_bool')
#            dirs.append('get_bool_current')
#            dirs.append('get_bool_next')
#        elif self.dtype is str :
#            dirs.append('get_char')
#            dirs.append('get_char_current')
#            dirs.append('get_char_next')
#        elif self.dtype is float :
#            dirs.append('get_float')
#            dirs.append('get_float_current')
#            dirs.append('get_float_next')
#        elif self.dtype is int :
#            dirs.append('get_int')
#            dirs.append('get_int_current')
#            dirs.append('get_int_next')
#        elif self.dtype is long :
#            dirs.append('get_long')
#            dirs.append('get_long_current')
#            dirs.append('get_long_next')
#        else :
#            dirs.append('get_str')
#            dirs.append('get_str_current')
#            dirs.append('get_str_next')
        dirs.append('get_reduced')
        dirs.append('get_section')
        dirs.append('get_slice')
        dirs.append('item_iter')
        dirs.append('ln')
        dirs.append('log10')
        dirs.append('max')
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
        dirs.append('view_1d')
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
#            nshape = copy.copy(self.shape)
#            include = range(nshape[0])
#            for i in slices :
#                include.remove(i)
#            if len(include) == nshape[0] :
#                return
#            nshape[0] = len(include)
#            if nshape[0] == 0 :
#                raise AttributeError, 'can not remove all slices from the dataset'
#            narr = instance(nshape, dtype = self.dtype)
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
            if axis >= self.ndim :
                raise Exception, 'index out of bound, ' + str(axis)
        else :
            for i in xrange(len(axis)):
                if axis[i] >= self.ndim:
                    raise Exception, 'index out of bound, ' + str(axis)
        if self.ndim == 1:
            return self.sum()
        sshape = [1] * self.ndim
        if axis_type is int:
            sshape[axis] = self.shape[axis]
        else:
            for i in xrange(len(axis)):
                sshape[axis[i]] = self.shape[axis[i]]
        if keepdims:
            nshape = copy.copy(self.shape)
            if axis_type is int:
                nshape[axis] = 1
            else:
                for i in xrange(len(axis)):
                    nshape[axis[i]] = 1
        else:
            nshape = []
            for i in xrange(self.ndim):
                if axis_type is int:
                    if i != axis:
                        nshape.append(self.shape[i])
                else:
                    if not axis.__contains__(i):
                        nshape.append(self.shape[i])
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
#            if len(axis) >= self.ndim:
#                raise Exception, 'index out of bound'
#            for item in axis:
#                if item >= self.ndim :
#                    raise Exception, str(item) + ' does not exist'
#        else :
#            if axis >= self.ndim :
#                raise Exception, str(item) + ' does not exist'
#        if keepdims:
#            nshape = copy.copy(self.shape)
#            if hasattr(axis, '__len__'):
#                for item in axis:
#                    nshape[item] = 1
#            else:
#                nshape[axis] = 1
#            
#        else:
#            nshape = []
#            if hasattr(axis, '__len__'):
#                for i in xrange(self.ndim):
#                    if not axis.__contains__(i):
#                        nshape.append(self.shape[i])
#            else:
#                for i in xrange(self.ndim):
#                    if axis != i:
#                        nshape.append(self.shape[i])
#            
#        if out is None:
#            pass
        
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
    if isinstance(obj, Array) :
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
    if isinstance(obj, Array) :
        return obj.shape
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
        step = 1
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
    if retstep :
        return step
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
    iter(arr)
    i = 0
    while i < num :
        arr.set_next_value(start + i * step)
        i += 1
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

def concatenate(tup, axis = 0):
    if len(tup) < 2 :
        raise ValueError, 'must have at least 2 arrays'
    dtype = bool
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
    arr = instance(nshp, 0, dtype)
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
    items = []
    ndim = 0
    shape = []
    for item in tup :
        if type(item) is tuple :
            for si in item :
                items.append(si)
                idim = get_ndim(si)
                if idim > ndim :
                    ndim = idim
                    shape = get_shape(si, ndim)
        else :
            items.append(item)
        idim = get_ndim(item)
        if idim > ndim :
            ndim = idim
            shape = get_shape(item, ndim)
    nshape = copy.copy(shape)
    nshape[0] = 0
    allshape = []
    alldim = []
    for item in items :
        idim = get_ndim(item)
        alldim.append(idim)
        ishape = get_shape(item, idim)
        allshape.append(ishape)
        if idim < ndim :
            if ndim - idim > 1 :
                raise ValueError, 'rank does not match, must be same or at most 1 less'
            else :
                if ishape != shape[1:] :
                    raise ValueError, 'shape does not match, must be same except for axis=0'
            nshape[0] += 1
        else :
            if ishape[1:] != shape[1:] :
                raise ValueError, 'shape does not match, must be same except for axis=0'
            nshape[0] += ishape[0]
    dtype = bool
    for item in items :
        itype = get_type(item)
        dtype = __compare_type__(dtype, itype)
    arr = instance(nshape, 0, dtype)
    org = [0] * arr.ndim
    id = 0
    for item in items :
        if alldim[id] < ndim :
            ishape = [1]+ allshape[id]
        else :
            ishape = allshape[id]
        arr.get_section(org, ishape).copy_from(item)
        org[0] += ishape[0]
        id += 1
    return arr

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
        if osize > nsize * indices_or_sections :
            nsize += 1
        org = [0] * arr.ndim
        nshape = copy.copy(arr.shape)
        nshape[axis] = nsize
        for id in xrange(indices_or_sections - 1) :
            res += [arr.get_section(org, nshape)]
            org[axis] += nsize
        nshape[axis] = osize - nsize * (indices_or_sections - 1)
        res += [arr.get_section(org, nshape)]
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
    raise TypeError, 'not supported yet'
    
    
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

#####################################################################################
# Other utility classes
#####################################################################################

class Slice_ :
    def __init__(self) :
        pass
    
    def __getitem__(self, index):
        return index

s_ = Slice_()


#####################################################################################
# Numpy standards
#####################################################################################
empty = instance