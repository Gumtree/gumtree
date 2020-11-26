'''
    @author: nxi
'''

from gumpy.nexus import array as nxa
from errorhandler import *
import pickle
import numpy as np

class ndarray():
    def __init__(self, shape = None, dtype=float, buffer=None, offset=None, strides=None, order=None):
        if buffer is None:
            self.buffer = nxa.instance(shape, dtype = dtype)
        else:
            if isinstance(buffer, ndarray):
                buffer = buffer.buffer
            if strides is None:
                self.buffer = buffer
            else:
                if offset is None:
                    offset = [0] * buffer.ndim
                self.buffer = buffer.get_section(offset, shape, strides)
    
    '''
    T : ndarray
        The transposed array.

    data: buffer
        Python buffer object pointing to the start of the array's data.

    dtype : dtype object
        Data-type of the array's elements.

    flags : not supported
        Information about the memory layout of the array.

    flat : numpy.flatiter object
        A 1-D iterator over the array.

    imag : not supported
        The imaginary part of the array.

    real : not supported
        The real part of the array.

    size : int
        Number of elements in the array.

    itemsize : int
        Length of one array element in bytes.

    nbytes : int
        Total bytes consumed by the elements of the array.

    ndim : int
        Number of array dimensions.

    shape : tuple of ints
        Tuple of array dimensions.

    strides : tuple of ints
        Tuple of bytes to step in each dimension when traversing an array.

    ctypes : not supported

    base : ndarray
        Base object if memory is from some other object.

    '''
    def __getattr__(self, name):
        if name == 'shape':
            return tuple(self.buffer.shape)
        elif name == 'data':
            return self.buffer
        elif name == 'T':
            t = self.buffer.transpose()
            return self._new(buffer = t)
        elif name == 'dtype':
            return self.buffer.dtype
        elif name == 'flags':
            raise NotSupportedError('flags is not supported in Gumpy')
        elif name == 'flat':
            return ndarray(buffer = self.buffer.flatten())
        elif name == 'imag':
            raise NotSupportedError('complex data type is not supported in Gumpy')
        elif name == 'real':
            raise NotSupportedError('complex data type is not supported in Gumpy')
        elif name == 'ndim':
            return self.buffer.ndim
        elif name == 'size':
            return self.buffer.size
        elif name == 'itemsize':
            return self.buffer.itemsize
        elif name == 'nbytes':
            return self.buffer.nbytes
        elif name == 'strides':
            return self.buffer.stride
        elif name == 'ctype':
            raise NotSupportedError('C type is not supported in Gumpy')
        elif name == 'base':
            raise NotSupportedError('memory access is not supported in Gumpy')
        else:
            raise AttributeError('attribute {} not found'.format(name))
        
    def __getitem__(self, index):
        out = self.buffer.__getitem__(index)
        if isinstance(out, nxa.Array):
            return ndarray(None, buffer = out)
        else:
            return out
        
    def __setitem__(self, index, value):
        self.buffer.__setitem__(index, value)
        
    def __str__(self, indent = ''):
        return self.buffer.__str__(indent)
    
    def __repr__(self, indent = '', skip = True, precision = None):
        if precision is None:
            precision = nxa.Array.precision
        out = self.buffer.__repr__(indent, skip = skip, precision = precision)
        return 'a' + out[1:]
        
    def __len__(self):
        return self.shape[0]
    
    def __iter__(self):
        if (self.ndim > 1) :
            return SliceIter(self)
        else :
            return self.buffer.item_iter()
    
    def __copy__(self):
        return self.copy()
        
    def __deepcopy__(self):
        return ndarray(buffer = self.buffer.__deepcopy__())
        
    ############## logic functions ##############
    
    def __eq__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__eq__(obj)) 
    
    def __ne__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__ne__(obj))
        
    def __lt__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__lt__(obj))

    def __le__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__le__(obj))

    def __gt__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__gt__(obj))

    def __ge__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__ge__(obj))

    def __not__(self):
        return ndarray(buffer = self.buffer.__not__())
    
    def __bool__(self):
        return ndarray(buffer = self.buffer.__bool__())
        
    def __or__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__or__(obj))

    def __ior__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        self.buffer.__ior__(obj)
        return self

    def __xor__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__xor__(obj))

    def __ixor__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        self.buffer.__ixor__(obj)
        return self
        
    def __and__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__and__(obj))

    def __iand__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        self.buffer.__iand__(obj)
        return self

    ############## math functions ###############
    def __add__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__add__(obj))

    def __iadd__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        self.buffer.__iadd__(obj)
        return self

    def __radd__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__radd__(obj))

    def __div__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__div__(obj))

    def __floordiv__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__floordiv__(obj))

    def __truediv__(self, obj):
        return self.__div__(obj)

    def __mod__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__mod__(obj))
    
    def __rmod__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__rmod__(obj))
            
    def __divmod__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        out = self.buffer.__divmod__(obj)
        return (ndarray(buffer = out[0]), ndarray(buffer = out[1]))
        
    def __idiv__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        self.buffer.__idiv__(obj)
        return self

    def __imod__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        self.buffer.__imod__(obj)
        return self

    def __itruediv__(self, obj):
        return self.__idiv__(obj)

    def __ifloordiv__(self, obj):
        return self.buffer.__ifloordiv__(obj)
    
    def __rdiv__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__rdiv__(obj))

    def __mul__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__mul__(obj))

    def __imul__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        self.buffer.__imul__(obj)
        return self

    def __rmul__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__rmul__(obj))

    def __neg__(self):
        return ndarray(buffer = self.buffer.__neg__())

    def __pos__(self):
        return ndarray(buffer = self.buffer.__pos__())
    
    def __sub__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__sub__(obj))

    def __isub__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        self.buffer.__isub__(obj)
        return self

    def __rsub__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__rsub__(obj))

    def __invert__(self):
        return ndarray(buffer = self.buffer.__invert__())

    def __abs__(self):
        return ndarray(buffer = self.buffer.__abs__())

    def __pow__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__pow__(obj))

    def __ipow__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        self.buffer.__ipow__(obj)
        return self

    def __rpow__(self, obj):
        if isinstance(obj, ndarray):
            obj = obj.buffer
        return ndarray(buffer = self.buffer.__rpow__(obj))
        
    def __int__(self):
        return self.buffer.__int__()

    def __float__(self):
        return self.buffer.__float__()

    def __long__(self):
        return self.buffer.__long__()
        
    def __matmul__(self, value):
        if isinstance(value, ndarray):
            value = value.buffer
        return self._new(self.buffer.matmul(value))
        
    def _new(self, buffer):
        if np.iterable(buffer) :
            return ndarray(buffer = buffer)
        else:
            return buffer
        
    def item(self, *args):
        return self.buffer.item(*args)
        
    def itemset(self, *args):
        self.buffer.itemset(*args)
        
    def copy(self, subok=True):
        if subok:
            return self._new(buffer = self.buffer.__copy__())
        else:
            return ndarray(buffer = self.buffer.__copy__())

    ''' Dump a pickle of the array to the specified file. The array 
        can be read back with pickle.load or numpy.load.

        Parameters

            file : str or Path
                A string naming the dump file.

    '''        
    def dump(self, file):
        file = open(file, 'wb')
        try:
            pickle.dump(self.dumps(), file)
        finally:
            file.close()
            
    def dumps(self):
        return 'np.' + self.__repr__(skip = False, precision = np.PRECISION)
        
    def all(self, axis=None, out=None, keepdims=False):
        res = self.buffer.all(axis)
        if out is None:
            if keepdims:
                return ndarray(self.shape, bool).fill(res)
            else:
                return res
        else:
            out.fill(res)
            return out

    def any(self, axis=None, out=None, keepdims=False):
        res = self.buffer.any(axis)
        if out is None:
            if keepdims:
                return ndarray(self.shape, bool).fill(res)
            else:
                return res
        else:
            out.fill(res)
            return out
    
    ''' Return the maximum along a given axis. '''
    def max(self, axis=None, out=None, keepdims=None, initial=None, where=None):
        if not out is None:
            out = np.asanyarray(out).buffer
        return self._new(self.buffer.amax(axis, out, initial))

    ''' Return the minimum along a given axis. '''
    def min(self, axis=None, out=None, keepdims=None, initial=None, where=None):
        if not out is None:
            out = np.asanyarray(out).buffer
        return self._new(self.buffer.amin(axis, out, initial))

    def mean(self, axis=None, dtype=None, out=None, keepdims=False):
        if not out is None:
            out = np.asanyarray(out).buffer
        return self._new(self.buffer.mean(axis, dtype, out))
        
    def argmax(self, axis=None, out=None):
        res = self.buffer.argmax(axis)
        if not out is None:
            out.fill(res)
            return out
        else:
            return self._new(res)
        
    def argmin(self, axis=None, out=None):
        res = self.buffer.argmin(axis)
        if not out is None:
            out.fill(res)
            return out
        else:
            return self._new(res)
        
    def cumprod(self, axis=None, dtype=None, out=None):
        return self._new(buffer = self.buffer.cumprod(axis, dtype, out))

    def cumsum(self, axis=None, dtype=None, out=None):
        return self._new(buffer = self.buffer.cumsum(axis, dtype, out))

    def dot(self, b, out = None):
        if np.iterable(b):
            b = np.asanyarray(b).buffer
        if not out is None:
            out = np.asanyarray(out).buffer
        return self._new(buffer = self.buffer.dot(b, out))
        
    def nonzero(self):
        out = self.buffer.nonzero()
        return tuple([ndarray(buffer = x) for x in out])
        
    def sum(self, axis=None, dtype=None, out=None, keepdims=None, initial=0, where=None):
        if not out is None:
            out = np.asanyarray(out).buffer
        return self._new(self.buffer.asum(axis, dtype, out, initial))
        
    def prod(self, axis=None, dtype=None, out=None, keepdims=None, initial=1, where=None):
        if not out is None:
            out = np.asanyarray(out).buffer
        return self._new(self.buffer.prod(axis, dtype, out, initial))
    
    def ptp(self, axis=None, out=None, keepdims=None):
        if not out is None:
            out = np.asanyarray(out).buffer
        return self._new(buffer = self.buffer.ptp(axis, out))
        
    def round(self, decimals=0, out=None):
        if not out is None:
            out = np.asanyarray(out).buffer
        return self._new(self.buffer.round(decimals, out))
    
    def sort(self, axis=-1, reverse=False):
        self.buffer.sort(axis, reverse)
        
    def std(self, axis=None, dtype=None, out=None, ddof=0):
        if not out is None:
            out = np.asanyarray(out).buffer
        return self._new(self.buffer.std(axis, dtype, out, ddof))
        
    def var(self, axis=None, dtype=None, out=None, ddof=0):
        if not out is None:
            out = np.asanyarray(out).buffer
        return self._new(self.buffer.var(axis, dtype, out, ddof))

    ''' Copy of the array, cast to a specified type.
    
        Parameters
    
            dtype : str or dtype
                Typecode or data-type to which the array is cast.
    
            order : not supported
    
            casting : not supported
                
            subok : bool, optional
                If True, then sub-classes will be passed-through (default), 
                otherwise the returned array will be forced to be a base-class 
                array.

            copy : not supported
    
        Returns
    
            arr_t : ndarray
                A new array of the same shape as the input array, with dtype, 
                order given by dtype, order.
    
        Raises
    
            ComplexWarning
                When casting from complex to float or int. To avoid this, 
                one should use a.real.astype(t).

    '''
    def astype(self, dtype, order='K', casting='unsafe', subok=True, copy=True):
        if subok:
            return self._new(buffer = self.buffer.astype(dtype))
        else:
            return ndarray(buffer = self.buffer.astype(dtype))
        
    ''' Return a copy of the array collapsed into one dimension.
    
        Parameters
    
            order : not supported
    
        Returns
    
            y : ndarray
                A copy of the input array, flattened to one dimension.
    
    '''
    def flatten(self, order='C'):
        return ndarray(buffer=self.buffer.flatten())
    
    def ravel(self):
        return self._new(self.buffer.ravel())
        
    def repeat(self, repeats, axis=None):
        return self._new(nxa.repeat(self.buffer, repeats, axis))
        
    def clip(self, min=None, max=None, out=None):
        if out is None:
            return self._new(buffer = self.buffer.clip(min, max))
        else :
            if not isinstance(out, ndarray):
                out = np.asanyarray(out)
            return self._new(buffer = self.buffer.clip(min, max, out.buffer))
    
    def put(self, indices, values, mode='raise'):
        self.buffer.put(indices, values, mode)
        
    def moveaxis(self, source, destination):
        if type(source) is int:
            if destination < 0:
                destination = self.ndim + destination
            dims = range(self.ndim)
            s = dims[source]
            nd = dims[:source] + dims[source + 1:]
            nd.insert(destination, s)
        elif hasattr(source, '__len__'):
            dims = range(self.ndim)
            s = []
            for i in xrange(len(source)):
                s.append([dims[source[i]], destination[i]])
            s = sorted(s, key = lambda g:g[1])
            nd = []
            source.sort()
            start = -1
            for i in source:
                nd += dims[start : i]
                start = i + 1
            nd += dims[start :]
            for g in s:
                nd.insert(g[1], g[0])
        return self._new(buffer=self.buffer.permute(nd))
    
    def swapaxes(self, axis1, axis2):
        nd = range(self.ndim)
        nd[axis1] = axis2
        nd[axis2] = axis1
        return self._new(buffer=self.buffer.permute(nd))
    
    def transpose(self, axes=None):
        if axes != None and len(axes) > 2:
            return self._new(buffer = self.buffer.permute(axes))
        else:
            return self._new(buffer = self.buffer.transpose(axes))
       
    def compress(self, condition, axis=None, out=None):
        if out is None:
            return self._new(buffer = self.buffer.compress(condition, axis))
        else:
            out = np.asanyarray(out)
            return self._new(buffer = self.buffer.compress(condition, axis, out.buffer))
        
    '''
    Fill the array with a scalar value.

    Parameters

       value : scalar
            All elements of a will be assigned this value.
    '''
    def fill(self, value):
        self.buffer.fill(value)
        
    def reshape(self, *shape):
        if len(shape) == 0:
            raise IllegalArgumentError('shape must be provided')
        if len(shape) == 1 :
            if type(shape[0]) is list:
                return self._new(buffer = self.buffer.reshape(shape[0]))
            elif type(shape[0]) is int:
                return self._new(buffer = self.buffer.reshape([shape[0]]))
            else:
                return self._new(buffer = self.buffer.reshape(list(shape[0])))
        else:
            return self._new(buffer = self.buffer.reshape(list(shape)))
        
    def resize(self, *new_size):
        if len(new_size) == 0:
            raise IllegalArgumentError('new size must be provided')
        if len(new_size) == 1 :
            if type(new_size[0]) is list:
                return self._new(buffer = self.buffer.resize(new_size[0]))
            elif type(new_size[0]) is int:
                return self._new(buffer = self.buffer.resize([new_size[0]]))
            else:
                return self._new(buffer = self.buffer.resize(list(new_size[0])))
        else:
            return self._new(buffer = self.buffer.resize(list(new_size)))
        
        
    def squeeze(self, axis = None):
        return ndarray(buffer = self.buffer.squeeze(axis))
        
    def searchsorted(self, v, side='left', sorter=None):
        return self._new(buffer = self.buffer.searchsorted(v, side, sorter))
        
    def diagonal(self, offset=0, axis1=0, axis2=1):
        return ndarray(buffer = self.buffer.diagonal(offset, axis1, axis2))
    
    def trace(self, offset=0, axis1=0, axis2=1, dtype=None, out=None):
        if out is None:
            return self._new(buffer = self.buffer.trace(offset, axis1, axis2, dtype))
        else:
            out = np.asanyarray(out).buffer
            r = self.buffer.trace(offset, axis1, axis2, dtype, out)
            return out            
        
    def tril(self, k=0):
        return ndarray(buffer = self.buffer.tril(k))

    def triu(self, k=0):
        return ndarray(buffer = self.buffer.triu(k))
        
    def take(self, indices, axis=None, out=None, mode=None):
        return self._new(buffer = self.buffer.take(indices, axis, out))
        
    ''' Return the array as an a.ndim-levels deep nested list of Python scalars.

        Return a copy of the array data as a (nested) Python list. Data items are 
        converted to the nearest compatible builtin Python type, via the item 
        function.
        
        If a.ndim is 0, then since the depth of the nested list is 0, it will not 
        be a list at all, but a simple Python scalar.
        
        Parameters
        
            none
        
        Returns
        
            y : object, or list of object, or list of list of object, or 
        
                The possibly nested list of array elements.

    '''
    def tolist(self):
        if self.size == 0:
            return []
        else:
            return self.buffer.tolist()
        
    ''' the following methods were not implemented
    
        argpartition(kth[, axis, kind, order])
    

        argsort([axis, kind, order])
    

        astype(dtype[, order, casting, subok, copy])
    

        byteswap([inplace])
        
        choose(choices[, out, mode])
        
        conj()
        
        conjugate()
        
        getfield(dtype, offset=0)
        
        newbyteorder(new_order='S')
        
        partition(kth, axis=-1, kind='introselect', order=None)

        setfield(val, dtype, offset=0)
        
        setflags(write=None, align=None, uic=None)
        
        tobytes(order='C')
        
        tofile(fid, sep="", format="%s")
        
        view([dtype][, type])
        
    '''

#####################################################################################
# Array slice iter class
#####################################################################################
class SliceIter():
    def __init__(self, array):
        self.array = array
        self.cur_slice = -1
    
    def next(self):
        if self.has_next() :
            self.cur_slice += 1
            arr = ndarray(buffer = self.array.buffer.get_slice(0, self.cur_slice))
        else :
            raise StopIteration
        return arr
        
    def curr(self):
        return ndarray(buffer = self.array.buffer.get_slice(0, self.cur_slice))
    
    def has_next(self):
        return self.cur_slice < len(self.array) - 1
        