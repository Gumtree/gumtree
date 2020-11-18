'''
@author nxi
'''
from nparray import ndarray
from errorhandler import *
from creation import *
import numpy as np


''' Create a copy of the array with a new shape.

    Parameters

        a : array_like
            Array to be reshaped.
            
        newshape : int or tuple of ints
            The new shape should be compatible with the original shape. 
            If an integer, then the result will be a 1-D array of that length. 
            One shape dimension can be -1. In this case, the value is inferred 
            from the length of the array and remaining dimensions.

        order : not supported

    Returns
        reshaped_array : ndarray
            This will be a new copy of the array. 

'''
def reshape(a, newshape, order='C'):
    if np.iterable(newshape):
        newshape = tuple(newshape)
        return asanyarray(a).reshape(*newshape)
    else:
        return asanyarray(a).reshape((newshape))

''' Copies values from one array to another, broadcasting as necessary.

    Raises a TypeError if the casting rule is violated, and if where is 
    provided, it selects which elements to copy.

    Parameters

        dst : ndarray
            The array into which values are copied.

        src : array_like
            The array from which values are copied.

        casting : {'no', 'equiv', 'safe', 'same_kind', 'unsafe'}, optional
            Controls what kind of data casting may occur when copying.

                    'no' means the data types should not be cast at all.

                    'equiv' means only byte-order changes are allowed.

                    'safe' means only casts which can preserve values are allowed.

                    'same_kind' means only safe casts or casts within a kind, like float64 to float32, are allowed.

                    'unsafe' means any data conversions may be done.

        where : array_like of bool, optional

            A boolean array which is broadcasted to match the dimensions of dst, 
            and selects elements to copy from src to dst wherever it contains 
            the value True.

'''
def copyto(dst, src, casting='same_kind', where=True):
    if isinstance(src, ndarray):
        src = src.buffer
    if isinstance(where, ndarray):
        where = where.buffer
    dst.buffer.copy_from(src, where = where)

''' Return the shape of an array.

    Parameters

        a : array_like
            Input array.

    Returns

        shape : tuple of ints
            The elements of the shape tuple give the lengths of 
            the corresponding array dimensions.

'''
def shape(a):
    return asanyarray(a).shape

''' Return a contiguous flattened array.

    A 1-D array, containing the elements of the input, is returned. 
    A copy is made only if needed. The returned array will have the 
    same type as the input array. (for example, a masked array will 
    be returned for a masked array input)

    Parameters

        a : array_like
            Input array. The elements in a are read in the order 
            specified by order, and packed as a 1-D array.
        
        order : not supported

    Returns

        y : array_like
            y is an array of the same subtype as a, with shape (a.size,). 
            Note that matrices are special cased for backward 
            compatibility, if a is a matrix, then y is a 1-D ndarray.

'''
def ravel(a, order='C'):
    arr = asanyarray(a)
    return ndarray(buffer = a.buffer.flatten())

''' Move axes of an array to new positions.

    Other axes remain in their original order.

    Parameters

        a : np.ndarray
            The array whose axes should be reordered.

        source : int or sequence of int
            Original positions of the axes to move. These must be unique.

        destination : int or sequence of int
            Destination positions for each of the original axes. These must also be unique.

    Returns

        result : np.ndarray
            Array with moved axes. This array is a view of the input array.

'''
def moveaxis(a, source, destination):
    return asanyarray(a).moveaxis(source, destination)

''' Roll the specified axis backwards, until it lies in a given position.

    This function continues to be supported for backward compatibility, 
    but you should prefer moveaxis. 

    Parameters

        a : ndarray
            Input array.

        axis : int
            The axis to be rolled. The positions of the other axes do not 
            change relative to one another.

        start : int, optional
            When start <= axis, the axis is rolled back until it lies in 
            this position. When start > axis, the axis is rolled until it 
            lies before this position. The default, 0, results in a "complete" 
            roll. The following table describes how negative values of start 
            are interpreted:

    Returns

        res : ndarray
            A view of a is always returned. 
'''
def rollaxis(a, axis, start=0):
    return moveaxis(a, axis, start)

''' Interchange two axes of an array.

    Parameters

        a : array_like
            Input array.

        axis1 : int
            First axis.
        
        axis2 : int
            Second axis.

    Returns

        a_swapped : ndarray
            a veiw of the original array is returned

'''
def swapaxes(a, axis1, axis2):
    return asanyarray(a).swapaxes(axis1, axis2)
    
''' Reverse or permute the axes of an array; returns the modified array.

    For an array a with two axes, transpose(a) gives the matrix transpose.

    Parameters

        a : array_like
            Input array.

        axes : tuple or list of ints, optional
            If specified, it must be a tuple or list which contains a 
            permutation of [0,1,..,N-1] where N is the number of axes of a. 
            The i'th axis of the returned array will correspond to the axis 
            numbered axes[i] of the input. If not specified, defaults to 
            range(a.ndim)[::-1], which reverses the order of the axes.

    Returns

        p : ndarray
            a with its axes permuted. A view is returned whenever possible.

'''
def transpose(a, axes=None):
    return asanyarray(a).transpose(axes)

''' Convert inputs to arrays with at least one dimension.

    Scalar inputs are converted to 1-dimensional arrays, whilst 
    higher-dimensional inputs are preserved.

    Parameters

        arys1, arys2, ... : array_like
            One or more input arrays.

    Returns

        ret : ndarray
            An array, or list of arrays, each with a.ndim >= 1. 
            Copies are made only if necessary.

'''
def atleast_1d(*arys):
    if len(arys) == 0:
        raise IllegalArgumentError('needs array like input')
    if len(arys) == 1:
        ary = arys[0]
        if isinstance(ary, ndarray):
            return ary
        else:
            return asanyarray(ary)
    else:
        ret = []
        for ary in arys:
            if isinstance(ary, ndarray):
                ret.append(ary)
            else:
                ret.append(asanyarray(ary))
        return ret

''' View inputs as arrays with at least two dimensions.

    Parameters
        arys1, arys2, ... : array_like

            One or more array-like sequences. Non-array inputs are converted to arrays. 
            Arrays that already have two or more dimensions are preserved.

    Returns

        res, res2, ... : ndarray
            An array, or list of arrays, each with a.ndim >= 2. Copies are avoided where 
            possible, and views with two or more dimensions are returned.

'''
def atleast_2d(*arys):
    if len(arys) == 0:
        raise IllegalArgumentError('needs array like input')
    if len(arys) == 1:
        ary = arys[0]
        if not isinstance(ary, ndarray):
            ary = asanyarray(ary)
        if ary.ndim < 2:
            return ary.reshape((1,) + ary.shape)
    else:
        ret = []
        for ary in arys:
            if not isinstance(ary, ndarray):
                ary = asanyarray(ary)
            if ary.ndim < 2:
                ret.append(ary.reshape((1,) + ary.shape))
            else:
                ret.append(ary)
        return ret
    

''' View inputs as arrays with at least three dimensions.

    Parameters

        arys1, arys2, ... : array_like
            One or more array-like sequences. Non-array inputs are converted to arrays. 
            Arrays that already have three or more dimensions are preserved.

    Returns

        res1, res2, ... : ndarray
            An array, or list of arrays, each with a.ndim >= 3. 
            For example, a 1-D array of shape (N,) becomes a view of shape (1, N, 1), 
            and a 2-D array of shape (M, N) becomes a view of shape (M, N, 1).

''' 
def atleast_3d(*arys):
    if len(arys) == 0:
        raise IllegalArgumentError('needs array like input')
    if len(arys) <= 2:
        ary = arys[0]
        if not isinstance(ary, ndarray):
            ary = asanyarray(ary)
        if ary.ndim == 2:
            return ary.reshape(ary.shape + (1,))
        else :
            return ary.reshape((1,) + ary.shape + (1,))
    else:
        ret = []
        for ary in arys:
            if not isinstance(ary, ndarray):
                ary = asanyarray(ary)
            if ary.ndim == 2:
                ret.append(ary.reshape(ary.shape + (1,)))
            elif ary.ndim == 1:
                ret.append(ary.reshape((1,) + ary.shape + (1,)))
            else:
                ret.append(ary)
        return ret

'''

    Broadcast an array to a new shape.

    Parameters

        arrayarray_like
            The array to broadcast.

        shape : tuple
            The shape of the desired array.

        subok : bool, optional
            If True, then sub-classes will be passed-through, otherwise the returned 
            array will be forced to be a base-class array (default).

    Returns

        broadcast : array
            A readonly view on the original array with the given shape. It is typically 
            not contiguous. Furthermore, more than one element of a broadcasted array 
            may refer to a single memory location.

    Raises
        ValueError

            If the array is not compatible with the new shape according to NumPy's 
            broadcasting rules.

'''
def broadcast_to(array, shape, subok=False):
    shape = tuple(shape) if np.iterable(shape) else (shape,)
    array = np.asanyarray(array)
    if not shape and array.shape:
        raise ValueError('cannot broadcast a non-scalar to a scalar array')
    if any(size < 0 for size in shape):
        raise ValueError('all elements of broadcast shape must be non-negative')
#     extras = []
#     it = np.nditer(
#         (array,), flags=['multi_index', 'refs_ok', 'zerosize_ok'] + extras,
#         op_flags=['readonly'], itershape=shape, order='C')
#     with it:
#         # never really has writebackifcopy semantics
#         broadcast = it.itviews[0]
#     result = _maybe_view_as_subclass(array, broadcast)
#     # In a future version this will go away
#     if not readonly and array.flags._writeable_no_warn:
#         result.flags.writeable = True
#         result.flags._warn_on_write = True
    oshape = array.shape
    ishape = (1,) * (len(shape) - array.ndim) + oshape
    res = np.empty(shape, dtype = array.dtype)
    si = res.buffer.section_iter(ishape)
    while si.has_next():
        sec = si.next()
        sec.copy_from(array.buffer)
    return res

''' Broadcast any number of arrays against each other.

    Parameters

        `*args` : array_likes
            The arrays to broadcast.

    Returns

        broadcasted : list of arrays

            Broadcast arrays as new copies.

'''
def broadcast_arrays(*args):
    newargs = ()
    shape = ()
    for arg in args:
        arg = np.asanyarray(arg)
        newargs += (arg,)
        s = arg.shape
        ns = ()
        if len(shape) < len(s):
            ns += s[:len(s) - len(shape)]
            for i in xrange(len(shape)):
                d1 = shape[i]
                d2 = s[i + len(s) - len(shape)]
                nd = d1 if d1 >= d2 else d2
                ns += (nd,)
        else:
            ns += s[:len(shape) - len(s)]
            for i in xrange(len(s)):
                d1 = s[i]
                d2 = shape[i + len(shape) - len(s)]
                nd = d1 if d1 >= d2 else d2
                ns += (nd,)
        shape = ns
    res = []
    for arg in newargs:
        res.append(broadcast_to(arg, shape))
    return res

''' Expand the shape of an array by making a new copy.

    Insert a new axis that will appear at the axis position in the expanded array 
    shape.

    Parameters

        a : array_like
            Input array.

        axis : int or tuple of ints

            Position in the expanded axes where the new axis (or axes) is placed.

    Returns

        result : ndarray
            Copy of a with the number of dimensions increased.

'''
def expand_dims(a, axis):
    a = asanyarray(a)
    if np.iterable(axis):
        list(axis).sort()
        for i in xrange(len(axis)):
            if axis < 0:
                x = axis[i]
                if x + a.ndim + 1 < 0:
                    raise AxisError('absolute value of axis can not be larger than the ndim of a')
                else:
                    axis[i] += a.ndim + 1
        shape = list(a.shape)
        for x in axis:
            shape.insert(x, 1)
    else:
        if axis < 0:
            if axis + a.ndim + 1 < 0:
                raise AxisError('absolute value of axis can not be larger than the ndim of a')
            else:
                axis += a.ndim + 1
        if axis > a.ndim:
            raise AxisError('axis can not be larger than the ndim of a')
        shape = list(a.shape)
        shape.insert(axis, 1)
    return np.reshape(a, shape)

''' Remove single-dimensional entries from the shape of an array. Return 
    an array with the same storage.

    Parameters

        a : array_like
            Input data.

        axis : None or int or tuple of ints, optional
            Selects a subset of the single-dimensional entries in the shape. 
            If an axis is selected with shape entry greater than one, 
            an error is raised.

    Returns

        squeezed : ndarray
            The input array, but with all or a subset of the dimensions of 
            length 1 removed. This is always a itself or a view into a. 
            Note that if all axes are squeezed, the result is a 0d array and 
            not a scalar.

    Raises

        ValueError
            If axis is not None, and an axis being squeezed is not of length 1

'''
def squeeze(a, axis=None):
    return asanyarray(a).squeeze(axis)


''' Join a sequence of arrays along an existing axis.

    Parameters

        arrays : tuple object, with multiple sequences of array_like
            The arrays must have the same shape, except in the dimension corresponding 
            to axis (the first, by default).
            
        axis : int, optional
            The axis along which the arrays will be joined. If axis is None, arrays are 
            flattened before use. Default is 0.

        out : ndarray, optional
            If provided, the destination to place the result. The shape must be correct, 
            matching that of what concatenate would have returned if no out argument 
            were specified.

    Returns

        res : ndarray
            The concatenated array.

'''
def concatenate(arrays, axis=0, out=None):
    narr = ()
    for a in arrays:
        if isinstance(a, ndarray):
            a = a.buffer
        narr += (a,)
    return ndarray(buffer = nxa.concatenate(narr, axis, out))

''' Join a sequence of arrays along a new axis.

    The axis parameter specifies the index of the new axis in the dimensions of 
    the result. For example, if axis=0 it will be the first dimension and if axis=-1 
    it will be the last dimension.

    Parameters

        arrays : sequence of array_like
            Each array must have the same shape.

        axis : int, optional
            The axis in the result array along which the input arrays are stacked.

        out : ndarray, optional
            If provided, the destination to place the result. The shape must be correct, 
            matching that of what stack would have returned if no out argument were 
            specified.

    Returns

        stacked : ndarray
            The stacked array has one more dimension than the input arrays.

'''
def stack(arrays, axis=0, out=None):
    if out is None:
        return ndarray(buffer = nxa.stack(arrays, axis))
    else:
        out = asanyarray(out)
        return ndarray(buffer = nxa.stack(arrays, axis, out.buffer))
#     narrs = ()
#     for arr in arrays:
#         if isinstance(arr, ndarray):
#             narrs += (arr.buffer,)
#         else:
#             narrs += (arr,)
#     return ndarray(buffer = nxa.stack(narrs, axis, out))

''' Stack arrays in sequence vertically (row wise).

    This is equivalent to concatenation along the first axis after 
    1-D arrays of shape (N,) have been reshaped to (1,N). Rebuilds 
    arrays divided by vsplit.

    This function makes most sense for arrays with up to 3 dimensions. 
    For instance, for pixel-data with a height (first axis), width 
    (second axis), and r/g/b channels (third axis). The functions 
    concatenate, stack and block provide more general stacking and 
    concatenation operations.

    Parameters

        tup : sequence of ndarrays
            The arrays must have the same shape along all but the 
            first axis. 1-D arrays must have the same length.

    Returns

        stacked : ndarray
            The array formed by stacking the given arrays, will be at 
            least 2-D.

'''
def vstack(tup):
    nt = ()
    for a in tup:
        if isinstance(a, ndarray):
            nt += (a.buffer,)
        else:
            nt += (a,)
    return ndarray(buffer = nxa.vstack(*nt))

''' Stack arrays in sequence horizontally (column wise).

    This is equivalent to concatenation along the second axis, except for 1-D arrays 
    where it concatenates along the first axis. Rebuilds arrays divided by hsplit.

    This function makes most sense for arrays with up to 3 dimensions. For instance, 
    for pixel-data with a height (first axis), width (second axis), and r/g/b channels 
    (third axis). The functions concatenate, stack and block provide more general 
    stacking and concatenation operations.

    Parameters

        tup : sequence of ndarrays
            The arrays must have the same shape along all but the second axis, except 
            1-D arrays which can be any length.

    Returns

        stacked : ndarray
            The array formed by stacking the given arrays.

'''
def hstack(tup):
    nt = ()
    for a in tup:
        if isinstance(a, ndarray):
            nt += (a.buffer,)
        else:
            nt += (a,)
    return ndarray(buffer = nxa.hstack(*nt))


''' Stack arrays in sequence depth wise (along third axis).

    This is equivalent to concatenation along the third axis after 2-D 
    arrays of shape (M,N) have been reshaped to (M,N,1) and 1-D arrays 
    of shape (N,) have been reshaped to (1,N,1). Rebuilds arrays divided 
    by dsplit.

    This function makes most sense for arrays with up to 3 dimensions. 
    For instance, for pixel-data with a height (first axis), width 
    (second axis), d r/g/b channels (third axis). The functions 
    concatenate, stack and block provide more general stacking and 
    concatenation operations.

    Parameters

        tup : sequence of arrays
            The arrays must have the same shape along all but the 
            third axis. 1-D or 2-D arrays must have the same shape.

    Returns

        stacked : ndarray
            The array formed by stacking the given arrays, will be 
            at least 3-D.

'''
def dstack(tup):
    nt = ()
    for a in tup:
        if isinstance(a, ndarray):
            nt += (a.buffer,)
        else:
            nt += (a,)
    return ndarray(buffer = nxa.dstack(*nt))

''' function block() is not supported '''
def block(arrays):
    raise NotSupportedError()

''' Stack 1-D arrays as columns into a 2-D array.

    Take a sequence of 1-D arrays and stack them as columns to make a 
    single 2-D array. 2-D arrays are stacked as-is, just like with hstack. 
    1-D arrays are turned into 2-D columns first.

    Parameters

        tup : sequence of 1-D or 2-D arrays.
            Arrays to stack. All of them must have the same first dimension.

    Returns

        stacked : 2-D array
            The array formed by stacking the given arrays.

'''
def column_stack(tup):
    return ndarray(buffer = nxa.column_stack(*tup))


''' Split an array into multiple sub-arrays as views into ary.

    Parameters

        ary : ndarray
            Array to be divided into sub-arrays.

        indices_or_sections : int or 1-D array
            If indices_or_sections is an integer, N, the array will be divided into N equal 
            arrays along axis. If such a split is not possible, an error is raised.

            If indices_or_sections is a 1-D array of sorted integers, the entries indicate 
            where along axis the array is split. For example, [2, 3] would, for axis=0, 
            result in

                    ary[:2]

                    ary[2:3]

                    ary[3:]

            If an index exceeds the dimension of the array along axis, an empty sub-array 
            is returned correspondingly.

        axis : int, optional
            The axis along which to split, default is 0.

    Returns

        sub-arrays : list of ndarrays
            A list of sub-arrays as new copies of parts of ary.

    Raises

        ValueError
            If indices_or_sections is given as an integer, but a split does not result in 
            equal division.

'''
def split(ary, indices_or_sections, axis=0):
    ary = asanyarray(ary)
    if isinstance(indices_or_sections, ndarray):
        indices_or_sections = indices_or_sections.tolist()
    res = nxa.split(ary.buffer, indices_or_sections, axis)
    out = []
    for r in res:
        out.append(ndarray(buffer = r))
    return out

''' Split an array into multiple sub-arrays.

    Please refer to the split documentation. The only difference between 
    these functions is that array_split allows indices_or_sections to be 
    an integer that does not equally divide the axis. For an array of 
    length l that should be split into n sections, it returns l % n 
    sub-arrays of size l//n + 1 and the rest of size l//n.

    Parameters

        ary : ndarray
            Array to be divided into sub-arrays.

        indices_or_sections : int or 1-D array
            If indices_or_sections is an integer, N, the array will be divided into N equal 
            arrays along axis. If such a split is not possible, an error is raised.


        axis : int, optional
            The axis along which to split, default is 0.

    Returns

        sub-arrays : list of ndarrays
            A list of sub-arrays as new copies of parts of ary.

'''
def array_split(ary, indices_or_sections, axis=0):
    ary = asanyarray(ary)
    if isinstance(indices_or_sections, ndarray):
        indices_or_sections = indices_or_sections.tolist()
    res = nxa.array_split(ary.buffer, indices_or_sections, axis)
    out = []
    for r in res:
        out.append(ndarray(buffer = r))
    return out

''' Split array into multiple sub-arrays along the 3rd axis (depth).

    Please refer to the split documentation. dsplit is equivalent to split 
    with axis=2, the array is always split along the third axis provided 
    the array dimension is greater than or equal to 3.

    See also

    split

        Split an array into multiple arrays of equal size as new copies.

'''
def dsplit(ary, indices_or_sections):
    ary = asanyarray(ary)
    if isinstance(indices_or_sections, ndarray):
        indices_or_sections = indices_or_sections.tolist()
    res = nxa.dsplit(ary.buffer, indices_or_sections)
    out = []
    for r in res:
        out.append(ndarray(buffer = r))
    return out

''' Split an array into multiple sub-arrays horizontally (column-wise).

    Please refer to the split documentation. hsplit is equivalent to split 
    with axis=1, the array is always split along the second axis regardless 
    of the array dimension.

    See also

    split
        Split an array into multiple arrays of equal size as new copies.

'''
def hsplit(ary, indices_or_sections):
    ary = asanyarray(ary)
    if isinstance(indices_or_sections, ndarray):
        indices_or_sections = indices_or_sections.tolist()
    res = nxa.hsplit(ary.buffer, indices_or_sections)
    out = []
    for r in res:
        out.append(ndarray(buffer = r))
    return out
    
''' Split an array into multiple sub-arrays vertically (row-wise).

    Please refer to the split documentation. vsplit is equivalent to split 
    with axis=0 (default), the array is always split along the first axis 
    regardless of the array dimension.

    See also

        split

            Split an array into multiple arrays of equal size as new copies.

'''
def vsplit(ary, indices_or_sections):
    if not isinstance(ary, ndarray):
        ary = asanyarray(ary)
    if isinstance(indices_or_sections, ndarray):
        indices_or_sections = indices_or_sections.tolist()
    res = nxa.vsplit(ary.buffer, indices_or_sections)
    out = []
    for r in res:
        out.append(ndarray(buffer = r))
    return out

''' Construct an array by repeating A the number of times given by reps.

    If reps has length d, the result will have dimension of max(d, A.ndim).

    If A.ndim < d, A is promoted to be d-dimensional by prepending new axes. 
    So a shape (3,) array is promoted to (1, 3) for 2-D replication, or 
    shape (1, 1, 3) for 3-D replication. If this is not the desired behavior, 
    promote A to d-dimensions manually before calling this function.

    If A.ndim > d, reps is promoted to A.ndim by pre-pending 1's to it. 
    Thus for an A of shape (2, 3, 4, 5), a reps of (2, 2) is treated as (1, 1, 2, 2).

    Note : Although tile may be used for broadcasting, it is strongly recommended 
    to use numpy's broadcasting operations and functions.

    Parameters

        A : array_like
            The input array.

        reps : array_like
            The number of repetitions of A along each axis.

    Returns

        c : ndarray
            The tiled output array.

'''
def tile(A, reps):
    if isinstance(reps, ndarray):
        reps = reps.tolist()
    A = asanyarray(A)
    return ndarray(buffer = nxa.tile(A.buffer, reps))

'''

    Construct an array by repeating A the number of times given by reps.

    If reps has length d, the result will have dimension of max(d, A.ndim).

    If A.ndim < d, A is promoted to be d-dimensional by prepending new axes. 
    So a shape (3,) array is promoted to (1, 3) for 2-D replication, or 
    shape (1, 1, 3) for 3-D replication. If this is not the desired behavior, 
    promote A to d-dimensions manually before calling this function.

    If A.ndim > d, reps is promoted to A.ndim by pre-pending 1's to it. 
    Thus for an A of shape (2, 3, 4, 5), a reps of (2, 2) is treated as (1, 1, 2, 2).

    Note : Although tile may be used for broadcasting, it is strongly recommended 
    to use numpy's broadcasting operations and functions.

    Parameters

        A : array_like
            The input array.
        
        reps : array_like
            The number of repetitions of A along each axis.

    Returns

        c : ndarray
            The tiled output array.

'''
def repeat(a, repeats, axis=None):
    a = asanyarray(a)
    return ndarray(buffer = nxa.repeat(a.buffer, repeats, axis))

''' Return a new array with sub-arrays along an axis deleted. For a one 
    dimensional array, this returns those entries not returned by arr[obj].

    Parameters

        arr : array_like
            Input array.

        obj : slice, int or array of ints
            Indicate indices of sub-arrays to remove along the specified axis.

            Boolean indices are now treated as a mask of elements to remove, 
            rather than being cast to the integers 0 and 1.

        axis : int, optional
            The axis along which to delete the subarray defined by obj. If axis 
            is None, obj is applied to the flattened array.

    Returns

        out : ndarray
            A copy of arr with the elements specified by obj removed. Note that 
            delete does not occur in-place. If axis is None, out is a flattened 
            array.

'''
def delete(arr, obj, axis=None):
    arr = asanyarray(arr)
    return ndarray(buffer = nxa.delete(arr.buffer, obj, axis))

''' Insert values along the given axis before the given indices.

    Parameters

        arr : array_like
            Input array.

        index : int, slice or sequence of ints
            Object that defines the index or indices before which values is inserted.

            Support for multiple insertions when obj is a single scalar or a sequence 
            with one element (similar to calling insert multiple times).
        
        values : array_like
            Values to insert into arr. If the type of values is different from that 
            of arr, values is converted to the type of arr. values should be shaped 
            so that arr[...,obj,...] = values is legal.
        
        axis : int, optional
            Axis along which to insert values. If axis is None then arr is flattened 
            first.

    Returns

        out : ndarray
            A copy of arr with values inserted. Note that insert does not occur 
            in-place: a new array is returned. If axis is None, out is a flattened array.

'''
def insert(arr, index, values, axis=None):
    arr = asanyarray(arr)
    return ndarray(buffer = nxa.insert(arr.buffer, index, values, axis))
    
''' Append values to the end of an array.

    Parameters

        arr : array_like
            Values are appended to a copy of this array.
        
        values : array_like
            These values are appended to a copy of arr. It must be of the 
            correct shape (the same shape as arr, excluding axis). If axis 
            is not specified, values can be any shape and will be flattened 
            before use.
        
        axis : int, optional
            The axis along which values are appended. If axis is not given, 
            both arr and values are flattened before use.

    Returns

        append : ndarray
            A copy of arr with values appended to axis. Note that append does 
            not occur in-place: a new array is allocated and filled. If axis 
            is None, out is a flattened array.

'''
def append(arr, values, axis=None):
    arr = asanyarray(arr)
    return ndarray(buffer = nxa.append(arr.buffer, values, axis))
    
    
'''

    Return a new array with the specified shape.

    If the new array is larger than the original array, then the new array is 
    filled with repeated copies of a. Note that this behavior is different 
    from a.resize(new_shape) which fills with zeros instead of repeated copies 
    of a.

    Parameters

        a : array_like
            Array to be resized.
        
        new_shape : int or tuple of int
            Shape of resized array.

    Returns

        reshaped_array : ndarray
            The new array is formed from the data in the old array, repeated 
            if necessary to fill out the required number of elements. The data 
            are repeated in the order that they are stored in memory.

'''
def resize(a, new_shape):
    a = asanyarray(a)
    return ndarray(buffer = nxa.resize(a.buffer, new_shape))

''' Trim the leading and/or trailing zeros from a 1-D array or sequence.

    Parameters

        filt : 1-D array or sequence
            Input array.

        trim : str, optional
            A string with 'f' representing trim from front and 'b' to trim 
            from back. Default is 'fb', trim zeros from both front and back of 
            the array.

    Returns

        trimmed : 1-D array or sequence
            The result of trimming the input. The input data type is preserved.

'''
def trim_zeros(filt, trim='fb'):
    filt = asanyarray(filt)
    return ndarray(buffer = nxa.trim_zeros(filt.buffer, trim))
    

def unique(ar, return_index=False, return_inverse=False, return_counts=False, axis=None):
    raise NotSupportedError()

''' Reverse the order of elements in an array along the given axis.

    The shape of the array is preserved, but the elements are reordered.

    Parameters

        m : array_like
            Input array.

        axis : None or int or tuple of ints, optional
            Axis or axes along which to flip over. The default, axis=None, 
            will flip over all of the axes of the input array. If axis is 
            negative it counts from the last to the first axis.

            If axis is a tuple of ints, flipping is performed on all of 
            the axes specified in the tuple.

    Returns

        out : array_like
            A view of m with the entries of axis reversed. 

'''
def flip(m, axis=None):
    m = asanyarray(m)
    return ndarray(buffer = m.buffer.flip(axis))

''' Flip array in the left/right direction.

    Flip the entries in each row in the left/right direction. Columns are 
    preserved, but appear in a different order than before.

    Parameters

        m : array_like
            Input array, must be at least 2-D.

    Returns

        f : ndarray
            A view of m with the columns reversed. 

'''
def fliplr(m):
    if m.ndim < 2:
        raise ValueError('array must be at least 2D')
    return flip(m, 1)


''' Flip array in the up/down direction.

    Flip the entries in each column in the up/down direction. Rows are preserved, 
    but appear in a different order than before.

    Parameters

        m : array_like
            Input array.

    Returns

        out : array_like
            A view of m with the rows reversed. 
'''
def flipud(m):
    return flip(m, 0)


''' Roll array elements along a given axis.

    Elements that roll beyond the last position are re-introduced at the first.

    Parameters

        a : array_like
            Input array.

        shift : int or tuple of ints
            The number of places by which elements are shifted. If a tuple, then 
            axis must be a tuple of the same size, and each of the given axes is 
            shifted by the corresponding number. If an int while axis is a tuple 
            of ints, then the same value is used for all given axes.

        axis : int or tuple of ints, optional
            Axis or axes along which elements are shifted. By default, the array 
            is flattened before shifting, after which the original shape is 
            restored.

    Returns

        resndarray

            Output array, with the same shape as a.

'''
def roll(a, shift, axis=None):
    a = asanyarray(a)
    return ndarray(buffer = nxa.roll(a.buffer, shift, axis))

''' Rotate an array by 90 degrees in the plane specified by axes.

    Rotation direction is from the first towards the second axis.

    Parameters

        m : array_like
            Array of two or more dimensions.
        
        k : integer
            Number of times the array is rotated by 90 degrees.

        axes: (2,) array_like

            The array is rotated in the plane defined by the axes. Axes must be different.
            
    Returns

        y : ndarray
            A rotated view of m.

'''
def rot90(m, k=1, axes=(0, 1)):
    m = asanyarray(m)
    return ndarray(buffer = nxa.rot90(m.buffer, k, axes))


''' Replaces specified elements of an array with given values.

    The indexing works on the flattened target array. put is roughly equivalent to:

    a.flat[ind] = v

    Parameters

        a : ndarray
            Target array.

        i : ndarray_like
            Target indices, interpreted as integers.

        v : array_like
            Values to place in a at target indices. If v is shorter than ind it will 
            be repeated as necessary.

        mode{'raise', 'wrap', 'clip'}, optional

            Specifies how out-of-bounds indices will behave.

                'raise' - raise an error (default)

                'wrap' - wrap around

                'clip' - clip to the range

            'clip' mode means that all indices that are too large are replaced by the 
            index that addresses the last element along that axis. Note that this 
            disables indexing with negative numbers. In 'raise' mode, if an exception 
            occurs the target array may still be modified.

'''
def put(a, ind, v, mode='raise'):
    a = np.asanyarray(a)
    a.put(ind, v, mode)
    
''' Return a contiguous flattened array.

    A 1-D array, containing the elements of the input, is returned. 
    A copy is made only if needed.

    Parameters

        a : array_like
            Input array. The elements in a are read in the order specified by order, 
            and packed as a 1-D array.
            
        order : not supported

    Returns

        y : array_like
            y is an array of the same subtype as a, with shape (a.size,). 

'''
def ravel(a, order=None):
    a = np.asanyarray(a)
    return a.ravel()