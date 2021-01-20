'''
    @author: nxi
'''
import numpy as np

def clip(a, a_min, a_max, out=None, **kwargs):
    ''' Clip (limit) the values in an array.
    
        Given an interval, values outside the interval are clipped to the 
        interval edges. For example, if an interval of [0, 1] is specified, 
        values smaller than 0 become 0, and values larger than 1 become 1.
    
        Equivalent to but faster than np.minimum(a_max, np.maximum(a, a_min)).
    
        No check is performed to ensure a_min < a_max.
    
        Parameters
    
            a : array_like
                Array containing elements to clip.
    
            a_min : scalar or array_like or None
                Minimum value. If None, clipping is not performed on lower 
                interval edge. Not more than one of a_min and a_max may be None.
    
            a_max : scalar or array_like or None
                Maximum value. If None, clipping is not performed on upper 
                interval edge. Not more than one of a_min and a_max may be None. 
                If a_min or a_max are array_like, then the three arrays will be 
                broadcasted to match their shapes.
    
            out : ndarray, optional
                The results will be placed in this array. It may be the input 
                array for in-place clipping. out must be of the right shape to 
                hold the output. Its type is preserved.
    
        Returns
    
            clipped_array : ndarray
                An array with the elements of a, but where values < a_min are 
                replaced with a_min, and those > a_max with a_max.
    
    '''
    a = np.asanyarray(a)
    return a.clip(a_min, a_max, out)

def cumprod(a, axis=None, dtype=None, out=None):
    ''' Return the cumulative product of elements along a given axis.
    
        Parameters
    
            a : array_like
                Input array.
                
            axis : int, optional
                Axis along which the cumulative product is computed. 
                By default the input is flattened.
    
            dtype : dtype, optional
                Type of the returned array, as well as of the accumulator 
                in which the elements are multiplied. If dtype is not 
                specified, it defaults to the dtype of a, unless a has an 
                integer dtype with a precision less than that of the 
                default platform integer. In that case, the default 
                platform integer is used instead.
    
            out : ndarray, optional
                Alternative output array in which to place the result. It 
                must have the same shape and buffer length as the expected 
                output but the type of the resulting values will be cast 
                if necessary.
    
        Returns
    
            cumprod : ndarray
    
                A new array holding the result is returned unless out is 
                specified, in which case a reference to out is returned.
    
    '''
    a = np.asanyarray(a)
    return a.cumprod(axis, dtype, out)

def cumsum(a, axis=None, dtype=None, out=None):
    ''' Return the cumulative sum of the elements along a given axis.
    
        Parameters
    
            a : array_like
                Input array.
                
            axis : int, optional
                Axis along which the cumulative sum is computed. The default (None) 
                is to compute the cumsum over the flattened array.
            
            dtype : dtype, optional
                Type of the returned array and of the accumulator in which the 
                elements are summed. If dtype is not specified, it defaults to 
                the dtype of a, unless a has an integer dtype with a precision 
                less than that of the default platform integer. In that case, 
                the default platform integer is used.
            
            out : ndarray, optional
                Alternative output array in which to place the result. It must 
                have the same shape and buffer length as the expected output 
                but the type will be cast if necessary. See ufuncs-output-type 
                for more details.
    
        Returns
    
            cumsum_along_axis : ndarray.
                A new array holding the result is returned unless out is specified, 
                in which case a reference to out is returned. The result has the 
                same size as a, and the same shape as a if axis is not None or a is 
                a 1-d array.
    
    '''
    a = np.asanyarray(a)
    return a.cumsum(axis, dtype, out)

def dot(a, b, out=None):
    ''' Dot product of two arrays. Specifically,
    
            If both a and b are 1-D arrays, it is inner product of vectors (without complex conjugation).
    
            If both a and b are 2-D arrays, it is matrix multiplication, but using matmul or a @ b is preferred.
    
            If either a or b is 0-D (scalar), it is equivalent to multiply and using numpy.multiply(a, b) or a * b is preferred.
    
            If a is an N-D array and b is a 1-D array, it is a sum product over the last axis of a and b.
    
            If a is an N-D array and b is an M-D array (where M>=2), it is a sum product over the last axis of a and the second-to-last axis of b:
    
            dot(a, b)[i,j,k,m] = sum(a[i,j,:] * b[k,:,m])
    
        Parameters
    
            a : array_like
                First argument.
    
            b : array_like
                Second argument.
    
            out : ndarray, optional
                Output argument. This must have the exact kind that would be 
                returned if it was not used. In particular, it must have the 
                right type, must be C-contiguous, and its dtype must be the 
                dtype that would be returned for dot(a,b). This is a 
                performance feature. Therefore, if these conditions are not 
                met, an exception is raised, instead of attempting to be 
                flexible.
    
        Returns
    
            outputndarray
    
                Returns the dot product of a and b. If a and b are both scalars 
                or both 1-D arrays then a scalar is returned; otherwise an array 
                is returned. If out is given, then it is returned.
    
        Raises
    
            ValueError
                If the last dimension of a is not the same size as the 
                second-to-last dimension of b.
    
    '''
    if np.iterable(a):
        a = np.asanyarray(a)
        return a.dot(b, out)
    elif np.iterable(b):
        b = np.asanyarray(b)
        return b.dot(a, out)
    else:
        return a * b

def amax(a, axis=None, out=None, keepdims=None, initial=None, where=None):
    ''' Return the maximum of an array or maximum along an axis.
    
        Parameters
    
            a : array_like
                Input data.
    
            axis : None or int or tuple of ints, optional
                Axis or axes along which to operate. 
                By default, flattened input is used.
    
                If this is a tuple of ints, the maximum is selected over multiple axes, 
                instead of a single axis or all the axes as before.
    
            out : ndarray, optional
                Alternative output array in which to place the result. Must be of the 
                same shape and buffer length as the expected output. See ufuncs-output-type 
                for more details.
    
            keepdims : not supported
            
            initial : scalar, optional
                The minimum value of an output element. Must be present to allow computation 
                on empty slice. See reduce for details.
    
            where : not supported
    
        Returns
    
            amax : ndarray or scalar
                Maximum of a. If axis is None, the result is a scalar value. If axis is given, 
                the result is an array of dimension a.ndim - 1.
    
    '''
    a = np.asanyarray(a)
    return a.max(axis, out, initial = initial)

def amin(a, axis=None, out=None, keepdims=None, initial=None, where=None):
    ''' Return the minimum of an array or minimum along an axis.
    
        Parameters
    
            a : array_like
                Input data.
    
            axis : None or int or tuple of ints, optional
                Axis or axes along which to operate. By default, flattened input is used.
                If this is a tuple of ints, the minimum is selected over multiple axes, 
                instead of a single axis or all the axes as before.
    
            out : ndarray, optional
                Alternative output array in which to place the result. Must be of the same 
                shape and buffer length as the expected output. See ufuncs-output-type for 
                more details.
            
            keepdims : not supported
    
            initial : scalar, optional
                The maximum value of an output element. Must be present to allow computation 
                on empty slice. See reduce for details.
    
            where : not supported
    
        Returns
    
            amin : ndarray or scalar
                Minimum of a. If axis is None, the result is a scalar value. If axis is given, 
                the result is an array of dimension a.ndim - 1.
    
    '''
    a = np.asanyarray(a)
    return a.min(axis, out, initial = initial)

def mean(a, axis=None, dtype=None, out=None, keepdims=None):
    ''' Compute the arithmetic mean along the specified axis.
    
        Returns the average of the array elements. The average is taken over 
        the flattened array by default, otherwise over the specified axis. 
        float64 intermediate and return values are used for integer inputs.
    
        Parameters
    
            a : array_like
                Array containing numbers whose mean is desired. If a is not 
                an array, a conversion is attempted.
                
            axis : None or int or tuple of ints, optional
                Axis or axes along which the means are computed. The default 
                is to compute the mean of the flattened array.
    
                If this is a tuple of ints, a mean is performed over multiple 
                axes, instead of a single axis or all the axes as before.
                
            dtype : data-type, optional
                Type to use in computing the mean. For integer inputs, the 
                default is float64; for floating point inputs, it is the same 
                as the input dtype.
    
            out : ndarray, optional
                Alternate output array in which to place the result. The 
                default is None; if provided, it must have the same shape as 
                the expected output, but the type will be cast if necessary. \
                
            keepdims : not supported
    
        Returns
    
            m : ndarray, see dtype parameter above
                If out=None, returns a new array containing the mean values, 
                otherwise a reference to the output array is returned.
    
    '''
    a = np.asanyarray(a)
    return a.mean(axis, dtype, out, keepdims)

def nonzero(a):
    ''' Return the indices of the elements that are non-zero.
    
        Returns a tuple of arrays, one for each dimension of a, containing the 
        indices of the non-zero elements in that dimension.
        
        Parameters
    
            a : array_like
                Input array.
    
        Returns
    
            tuple_of_arrays : tuple
                Indices of elements that are non-zero.
    
    '''
    a = np.asanyarray(a)
    return a.nonzero()

def prod(a, axis=None, dtype=None, out=None, keepdims=None, initial=1, where=None):
    ''' Return the product of array elements over a given axis.
    
        Parameters
    
            a : array_like
                Input data.
            
            axis : None or int or tuple of ints, optional
                Axis or axes along which a product is performed. The default, axis=None, 
                will calculate the product of all the elements in the input array. 
                If axis is negative it counts from the last to the first axis.
    
                If axis is a tuple of ints, a product is performed on all of the axes 
                specified in the tuple instead of a single axis or all the axes as before.
                
            dtype : dtype, optional
                The type of the returned array, as well as of the accumulator in which 
                the elements are multiplied. The dtype of a is used by default unless a 
                has an integer dtype of less precision than the default platform integer. 
                In that case, if a is signed then the platform integer is used while if 
                a is unsigned then an unsigned integer of the same precision as the 
                platform integer is used.
                
            out : ndarray, optional
                Alternative output array in which to place the result. It must have the 
                same shape as the expected output, but the type of the output values 
                will be cast if necessary.
                
            keepdims : not supported
    
            initial : scalar, optional
                The starting value for this product. 
    
            where : not supported
    
        Returns
    
            product_along_axis : ndarray, see dtype parameter above.
                An array shaped as a but with the specified axis removed. Returns a 
                reference to out if specified.
    
    '''
    a = np.asanyarray(a)
    return a.prod(axis, dtype, out, initial = initial)

def ptp(a, axis=None, out=None, keepdims=None):
    ''' Range of values (maximum - minimum) along an axis.
    
        The name of the function comes from the acronym for 'peak to peak'.
    
        Parameters
    
            a : array_like
                Input values.
    
            axis : None or int or tuple of ints, optional
                Axis along which to find the peaks. By default, flatten the array. 
                axis may be negative, in which case it counts from the last to the 
                first axis.
    
                If this is a tuple of ints, a reduction is performed on multiple 
                axes, instead of a single axis or all the axes as before.
                
            out : array_like
                Alternative output array in which to place the result. It must 
                have the same shape and buffer length as the expected output, but 
                the type of the output values will be cast if necessary.
                
            keepdims : bool, optional
                If this is set to True, the axes which are reduced are left in the 
                result as dimensions with size one. With this option, the result 
                will broadcast correctly against the input array.
    
                If the default value is passed, then keepdims will not be passed 
                through to the ptp method of sub-classes of ndarray, however any 
                non-default value will be. If the sub-class' method does not 
                implement keepdims any exceptions will be raised.
    
        Returns
    
            ptp : ndarray
                A new array holding the result, unless out was specified, in which 
                case a reference to out is returned.
    
    '''
    a = np.asanyarray(a)
    return a.ptp(axis, out)

def around(a, decimals=0, out=None):
    ''' Evenly round to the given number of decimals.
    
        Parameters
    
            a : array_like
                Input data.
    
            decimals : int, optional
                Number of decimal places to round to (default: 0). If decimals is negative, 
                it specifies the number of positions to the left of the decimal point.
            
            out : ndarray, optional
                Alternative output array in which to place the result. It must have the 
                same shape as the expected output, but the type of the output values will 
                be cast if necessary. 
    
        Returns
    
            rounded_array : ndarray
                An array of the same type as a, containing the rounded values. Unless 
                out was specified, a new array is created. A reference to the result is 
                returned.
    
                The real and imaginary parts of complex numbers are rounded separately. 
                The result of rounding a float is a float.
    
    '''
    a = np.asanyarray(a)
    return a.round(decimals, out)

def std(a, axis=None, dtype=None, out=None, ddof=0, keepdims=None):
    ''' Compute the standard deviation along the specified axis.
    
        Returns the standard deviation, a measure of the spread of a distribution, 
        of the array elements. The standard deviation is computed for the flattened 
        array by default, otherwise over the specified axis.
    
        Parameters
    
            a : array_like
                Calculate the standard deviation of these values.
            
            axis : None or int or tuple of ints, optional
                Axis or axes along which the standard deviation is computed. The 
                default is to compute the standard deviation of the flattened array.
    
                If this is a tuple of ints, a standard deviation is performed over 
                multiple axes, instead of a single axis or all the axes as before.
                
            dtype : dtype, optional
                Type to use in computing the standard deviation. For arrays of 
                integer type the default is float64, for arrays of float types 
                it is the same as the array type.
    
            out : ndarray, optional
                Alternative output array in which to place the result. It must 
                have the same shape as the expected output but the type (of the 
                calculated values) will be cast if necessary.
    
            ddof : int, optional
                Means Delta Degrees of Freedom. The divisor used in calculations 
                is N - ddof, where N represents the number of elements. 
                By default ddof is zero.
    
            keepdims : bool, optional
                If this is set to True, the axes which are reduced are left in 
                the result as dimensions with size one. With this option, the 
                result will broadcast correctly against the input array.
    
                If the default value is passed, then keepdims will not be passed 
                through to the std method of sub-classes of ndarray, however any 
                non-default value will be. If the sub-class' method does not 
                implement keepdims any exceptions will be raised.
    
        Returns
    
            standard_deviationndarray, see dtype parameter above.
                If out is None, return a new array containing the standard 
                deviation, otherwise return a reference to the output array.
    
    '''
    a = np.asanyarray(a)
    return a.std(axis, dtype, out, ddof)

def sum(a, axis=None, dtype=None, out=None, keepdims=None, initial=0, where=None):
    ''' Sum of array elements over a given axis.
    
        Parameters
    
            a : array_like
                Elements to sum.
                
            axis : None or int or tuple of ints, optional
                Axis or axes along which a sum is performed. The default, axis=None, 
                will sum all of the elements of the input array. If axis is negative 
                it counts from the last to the first axis.
    
                If axis is a tuple of ints, a sum is performed on all of the axes 
                specified in the tuple instead of a single axis or all the axes as 
                before.
                
            dtype : dtype, optional
                The type of the returned array and of the accumulator in which the 
                elements are summed. The dtype of a is used by default unless a has 
                an integer dtype of less precision than the default platform integer. 
                In that case, if a is signed then the platform integer is used while 
                if a is unsigned then an unsigned integer of the same precision as 
                the platform integer is used.
                
            out : ndarray, optional
                Alternative output array in which to place the result. It must have 
                the same shape as the expected output, but the type of the output 
                values will be cast if necessary.
                
            keepdims : bool, optional
                If this is set to True, the axes which are reduced are left in the 
                result as dimensions with size one. With this option, the result will 
                broadcast correctly against the input array.
    
                If the default value is passed, then keepdims will not be passed 
                through to the sum method of sub-classes of ndarray, however any 
                non-default value will be. If the sub-class' method does not implement 
                keepdims any exceptions will be raised.
                
            initial : scalar, optional
                Starting value for the sum. See reduce for details.
    
            where : not supported
    
        Returns
    
            sum_along_axis : ndarray
                An array with the same shape as a, with the specified axis removed. 
                If a is a 0-d array, or if axis is None, a scalar is returned. If an 
                output array is specified, a reference to out is returned.
    
    '''
    a = np.asanyarray(a)
    return a.sum(axis, dtype, out, initial = initial)

def var(a, axis=None, dtype=None, out=None, ddof=0, keepdims=None):
    ''' Compute the variance along the specified axis.
    
        Returns the variance of the array elements, a measure of the spread of 
        a distribution. The variance is computed for the flattened array by 
        default, otherwise over the specified axis.
    
        Parameters
    
            a : array_like
                Array containing numbers whose variance is desired. If a is not 
                an array, a conversion is attempted.
    
            axis : None or int or tuple of ints, optional
    
                Axis or axes along which the variance is computed. The default 
                is to compute the variance of the flattened array.
    
                If this is a tuple of ints, a variance is performed over multiple 
                axes, instead of a single axis or all the axes as before.
                
            dtype : data-type, optional
                Type to use in computing the variance. For arrays of integer type 
                the default is float64; for arrays of float types it is the same 
                as the array type.
                
            out : ndarray, optional
                Alternate output array in which to place the result. It must have 
                the same shape as the expected output, but the type is cast if 
                necessary.
                
            ddof : int, optional
                "Delta Degrees of Freedom": the divisor used in the calculation 
                is N - ddof, where N represents the number of elements. By default 
                ddof is zero.
            
            keepdims : not support
    
        Returns
    
            variance : ndarray, see dtype parameter above
                If out=None, returns a new array containing the variance; otherwise, 
                a reference to the output array is returned.
    
    '''
    a = np.asanyarray(a)
    return a.var(axis, dtype, out, ddof)

def trace(a, offset=0, axis1=0, axis2=1, dtype=None, out=None):
    ''' Return the sum along diagonals of the array.
    
        If a is 2-D, the sum along its diagonal with the given offset is returned, 
        i.e., the sum of elements a[i,i+offset] for all i.
    
        If a has more than two dimensions, then the axes specified by axis1 and axis2 
        are used to determine the 2-D sub-arrays whose traces are returned. The shape 
        of the resulting array is the same as that of a with axis1 and axis2 removed.
    
        Parameters
    
            a : array_like
                Input array, from which the diagonals are taken.
    
            offset : int, optional
                Offset of the diagonal from the main diagonal. Can be both positive 
                and negative. Defaults to 0.
                
            axis1, axis2 : int, optional
                Axes to be used as the first and second axis of the 2-D sub-arrays 
                from which the diagonals should be taken. Defaults are the first two 
                axes of a.
                
            dtype : dtype, optional
                Determines the data-type of the returned array and of the accumulator 
                where the elements are summed. If dtype has the value None and a is 
                of integer type of precision less than the default integer precision, 
                then the default integer precision is used. Otherwise, the precision 
                is the same as that of a.
                
            out : ndarray, optional
                Array into which the output is placed. Its type is preserved and it 
                must be of the right shape to hold the output.
    
        Returns
    
            sum_along_diagonals : ndarray
                If a is 2-D, the sum along the diagonal is returned. If a has larger 
                dimensions, then an array of sums along diagonals is returned.
    
    '''
    a = np.asanyarray(a)
    return a.trace(offset, axis1, axis2, dtype, out)


def matmul(x1, x2, out=None, dtype=None, subok=True):
    ''' 
    Matrix product of two arrays.

    Parameters

        x1, x2 : array_like
            Input arrays, scalars not allowed.

        out : ndarray, optional
            A location into which the result is stored. If provided, it must have a 
            shape that matches the signature (n,k),(k,m)->(n,m). If not provided or 
            None, a freshly-allocated array is returned.
            
        dtype : dtype, optional
            Determines the data-type of the returned array.
            
        subok : not supported

    Returns

        y : ndarray
            The matrix product of the inputs. This is a scalar only when both x1, x2 
            are 1-d vectors.

    Raises

        ValueError
            If the last dimension of a is not the same size as the second-to-last dimension of b.
            If a scalar value is passed in.

    '''
    x1 = np.asanyarray(x1)
    x2 = np.asanyarray(x2)
    if out is None:
        return np.ndarray(buffer = x1.buffer.matmul(x2.buffer, dtype = dtype))
    else:
        out = asanyarray(out).buffer
        x1.buffer.matmul(x2.buffer, out, dtype = dtype)
        return out
    
''' Counts the number of non-zero values in the array a.

    Parameters

        a : array_like
            The array for which to count non-zeros.
            
        axis : int or tuple, optional
            Axis or tuple of axes along which to count non-zeros. 
            Default is None, meaning that non-zeros will be counted 
            along a flattened version of a.

        keepdims : bool, optional
            If this is set to True, the axes that are counted are 
            left in the result as dimensions with size one. With 
            this option, the result will broadcast correctly against 
            the input array.

    Returns

        count : int or array of int
            Number of non-zero values in the array along a given axis. 
            Otherwise, the total number of non-zero values in the 
            array is returned.

'''
def count_nonzero(a, axis=None, keepdims=False):
    a = np.asanyarray(a)
    return a.count_nonzero(axis, keepdims)