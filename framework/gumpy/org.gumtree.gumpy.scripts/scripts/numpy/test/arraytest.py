import unittest
import numpy as np
import sys
from decimal import Decimal

class AbstractNumpyTest(unittest.TestCase):
    
    def assertEqual(self, first, second, msg = None):
        if isinstance(first, np.ndarray):
            return self.assertTrue(np.array_equal(first, second), msg)
        else:
            unittest.TestCase.assertEqual(self, first, second, msg)
        

class TestAttributes(AbstractNumpyTest):
    
    def setUp(self):
        self.one = np.arange(10)
        self.two = np.arange(20).reshape(4, 5)
        self.three = np.arange(60, np.float64).reshape(2, 5, 6)

    def test_attributes(self):
        self.assertEqual(self.one.shape, (10,))
        self.assertEqual(self.two.shape, (4, 5))
        self.assertEqual(self.three.shape, (2, 5, 6))
        self.three.shape = (10, 3, 2)
        self.assertEqual(self.three.shape, (10, 3, 2))
        self.three.shape = (2, 5, 6)
#         self.assertEqual(self.one.strides, (self.one.itemsize,))
        num = self.two.itemsize
#         self.assertEqual(self.two.strides, (5*num, num))
        num = self.three.itemsize
#         self.assertEqual(self.three.strides, (30*num, 6*num, num))
        self.assertEqual(self.one.ndim, 1)
        self.assertEqual(self.two.ndim, 2)
        self.assertEqual(self.three.ndim, 3)
        num = self.two.itemsize
        self.assertEqual(self.two.size, 20)
        self.assertEqual(self.two.nbytes, 20*num)
#         self.assertEqual(self.two.itemsize, self.two.dtype.itemsize)
#         self.assertEqual(self.two.base, np.arange(20))

#     def test_dtypeattr(self):
#         self.assertEqual(self.one.dtype, np.dtype(np.int_))
#         self.assertEqual(self.three.dtype, np.dtype(np.float_))
#         self.assertEqual(self.one.dtype.char, 'l')
#         self.assertEqual(self.three.dtype.char, 'd')
#         self.assertTrue(self.three.dtype.str[0] in '<>')
#         self.assertEqual(self.one.dtype.str[1], 'i')
#         self.assertEqual(self.three.dtype.str[1], 'f')

#     def test_int_subclassing(self):
#         # Regression test for https://github.com/numpy/numpy/pull/3526
# 
#         numpy_int = np.int_(0)
# 
#         # int_ doesn't inherit from Python int, because it's not fixed-width
#         self.assertTrue(not isinstance(numpy_int, int))

#     def test_stridesattr(self):
#         x = self.one
# 
#         def make_array(size, offset, strides):
#             return np.ndarray(size, buffer=x, dtype=int,
#                               offset=offset*x.itemsize,
#                               strides=strides*x.itemsize)
# 
#         self.assertEqual(make_array(4, 4, -1), np.array([4, 3, 2, 1]))
#         self.assertRaises(ValueError, make_array, 4, 4, -2)
#         self.assertRaises(ValueError, make_array, 4, 2, -1)
#         self.assertRaises(ValueError, make_array, 8, 3, 1)
#         self.assertEqual(make_array(8, 3, 0), np.array([3]*8))
#         # Check behavior reported in gh-2503:
#         self.assertRaises(ValueError, make_array, (2, 3), 5, np.array([-2, -3]))
#         make_array(0, 0, 10)

#     def test_set_stridesattr(self):
#         x = self.one
# 
#         def make_array(size, offset, strides):
#             try:
#                 r = np.ndarray([size], dtype=int, buffer=x,
#                                offset=offset*x.itemsize)
#             except Exception as e:
#                 raise RuntimeError(e)
#             r.strides = strides = strides*x.itemsize
#             return r
# 
#         self.assertEqual(make_array(4, 4, -1), np.array([4, 3, 2, 1]))
#         self.assertEqual(make_array(7, 3, 1), np.array([3, 4, 5, 6, 7, 8, 9]))
#         self.assertRaises(ValueError, make_array, 4, 4, -2)
#         self.assertRaises(ValueError, make_array, 4, 2, -1)
#         self.assertRaises(RuntimeError, make_array, 8, 3, 1)
#         # Check that the true extent of the array is used.
#         # Test relies on as_strided base not exposing a buffer.
#         x = np.lib.stride_tricks.as_strided(np.arange(1), (10, 10), (0, 0))
# 
#         def set_strides(arr, strides):
#             arr.strides = strides
# 
#         self.assertRaises(ValueError, set_strides, x, (10*x.itemsize, x.itemsize))
# 
#         # Test for offset calculations:
#         x = np.lib.stride_tricks.as_strided(np.arange(10, dtype=np.int8)[-1],
#                                                     shape=(10,), strides=(-1,))
#         self.assertRaises(ValueError, set_strides, x[::-1], -1)
#         a = x[::-1]
#         a.strides = 1
#         a[::2].strides = 2
# 
#         # test 0d
#         arr_0d = np.array(0)
#         arr_0d.strides = ()
#         self.assertRaises(TypeError, set_strides, arr_0d, None)

    def test_fill(self):
        for t in "?bBiulflc":
            x = np.empty((3, 2, 1), t)
            y = np.empty((3, 2, 1), t)
            x.fill(1)
            y[...] = 1
            self.assertEqual(x, y)

    def test_fill_max_uint64(self):
        x = np.empty((3, 2, 1), dtype=np.uint64)
        y = np.empty((3, 2, 1), dtype=np.uint64)
        value = 2**63 - 1
#         value = 1
        y[...] = value
        x.fill(value)
        self.assertEqual(x, y)

#     def test_fill_struct_array(self):
#         # Filling from a scalar
#         x = np.array([(0, 0.0), (1, 1.0)], dtype='i4,f8')
#         x.fill(x[0])
#         self.assertEqual(x['f1'][1], x['f1'][0])
#         # Filling from a tuple that can be converted
#         # to a scalar
#         x = np.zeros(2, dtype=[('a', 'f8'), ('b', 'i4')])
#         x.fill((3.5, -2))
#         self.assertEqual(x['a'], [3.5, 3.5])
#         self.assertEqual(x['b'], [-2, -2])
        
class TestArrayConstruction(AbstractNumpyTest):
    def test_array(self):
        d = np.ones(6)
        r = np.array([d, d])
        self.assertEqual(r, np.ones((2, 6)))

        d = np.ones(6)
        tgt = np.ones((2, 6))
        r = np.array([d, d])
        self.assertEqual(r, tgt)
        tgt[1] = 2
        r = np.array([d, d + 1])
        self.assertEqual(r, tgt)

        d = np.ones(6)
        r = np.array([[d, d]])
        self.assertEqual(r, np.ones((1, 2, 6)))

        d = np.ones(6)
        r = np.array([[d, d], [d, d]])
        self.assertEqual(r, np.ones((2, 2, 6)))

        d = np.ones((6, 6))
        r = np.array([d, d])
        self.assertEqual(r, np.ones((2, 6, 6)))

#         d = np.ones((6, ))
#         r = np.array([[d, d + 1], d + 2], dtype=object)
#         self.assertEqual(len(r), 2)
#         self.assertEqual(r[0], [d, d + 1])
#         self.assertEqual(r[1], d + 2)

        tgt = np.ones((2, 3), dtype=bool)
        tgt[0, 2] = False
        tgt[1, 0:2] = False
        r = np.array([[True, True, False], [False, False, True]])
        self.assertEqual(r, tgt)
        r = np.array([[True, False], [True, False], [False, True]])
        self.assertEqual(r, tgt.T)

    def test_array_empty(self):
        self.assertRaises(TypeError, np.array)

    def test_array_copy_false(self):
        d = np.array([1, 2, 3])
        e = np.array(d, copy=False)
        d[1] = 3
        self.assertEqual(e, [1, 3, 3])
        e = np.array(d, copy=False, order='F')
        d[1] = 4
        self.assertEqual(e, [1, 4, 3])
        e[2] = 7
        self.assertEqual(d, [1, 4, 7])

    def test_array_copy_true(self):
        d = np.array([[1,2,3], [1, 2, 3]])
        e = np.array(d, copy=True)
        d[0, 1] = 3
        e[0, 2] = -7
        self.assertEqual(e, [[1, 2, -7], [1, 2, 3]])
        self.assertEqual(d, [[1, 3, 3], [1, 2, 3]])
        e = np.array(d, copy=True, order='F')
        d[0, 1] = 5
        e[0, 2] = 7
        self.assertEqual(e, [[1, 3, 7], [1, 2, 3]])
        self.assertEqual(d, [[1, 5, 3], [1,2,3]])

#     def test_array_cont(self):
#         d = np.ones(10)[::2]
#         self.assertTrue(np.ascontiguousarray(d).flags.c_contiguous)
#         self.assertTrue(np.ascontiguousarray(d).flags.f_contiguous)
#         self.assertTrue(np.asfortranarray(d).flags.c_contiguous)
#         self.assertTrue(np.asfortranarray(d).flags.f_contiguous)
#         d = np.ones((10, 10))[::2,::2]
#         self.assertTrue(np.ascontiguousarray(d).flags.c_contiguous)
#         self.assertTrue(np.asfortranarray(d).flags.f_contiguous)

class TestAssignment(AbstractNumpyTest):
    
    def test_assignment_broadcasting(self):
        a = np.arange(6).reshape(2, 3)

        # Broadcasting the input to the output
        a[...] = np.arange(3)
        self.assertEqual(a, [[0, 1, 2], [0, 1, 2]])
        a[...] = np.arange(2).reshape(2, 1)
        self.assertEqual(a, [[0, 0, 0], [1, 1, 1]])

        # For compatibility with <= 1.5, a limited version of broadcasting
        # the output to the input.
        #
        # This behavior is inconsistent with NumPy broadcasting
        # in general, because it only uses one of the two broadcasting
        # rules (adding a new "1" dimension to the left of the shape),
        # applied to the output instead of an input. In NumPy 2.0, this kind
        # of broadcasting assignment will likely be disallowed.
        a[...] = np.arange(5, -1, -1).reshape(1, 2, 3)
        self.assertEqual(a, [[5, 4, 3], [2, 1, 0]])
        # The other type of broadcasting would require a reduction operation.

        def assign(a, b):
            a[...] = b

        self.assertRaises(ValueError, assign, a, np.arange(12).reshape(2, 2, 3))

    def test_assignment_errors(self):
        # Address issue #2276
        class C:
            pass
        a = np.zeros(1)

        def assign(v):
            a[0] = v

        self.assertRaises((AttributeError, TypeError), assign, C())
        self.assertRaises(TypeError, assign, [1])

# class TestZeroRank:
#     def setup(self):
#         self.d = np.array(0), np.array('x', object)
# 
#     def test_ellipsis_subscript(self):
#         a, b = self.d
#         self.assertEqual(a[...], 0)
#         self.assertEqual(b[...], 'x')
#         self.assertTrue(a[...].base is a)  # `a[...] is a` in numpy <1.9.
#         self.assertTrue(b[...].base is b)  # `b[...] is b` in numpy <1.9.
# 
#     def test_empty_subscript(self):
#         a, b = self.d
#         self.assertEqual(a[()], 0)
#         self.assertEqual(b[()], 'x')
#         self.assertTrue(type(a[()]) is a.dtype.type)
#         self.assertTrue(type(b[()]) is str)
# 
#     def test_invalid_subscript(self):
#         a, b = self.d
#         self.assertRaises(IndexError, lambda x: x[0], a)
#         self.assertRaises(IndexError, lambda x: x[0], b)
#         self.assertRaises(IndexError, lambda x: x[np.array([], int)], a)
#         self.assertRaises(IndexError, lambda x: x[np.array([], int)], b)
# 
#     def test_ellipsis_subscript_assignment(self):
#         a, b = self.d
#         a[...] = 42
#         self.assertEqual(a, 42)
#         b[...] = ''
#         self.assertEqual(b.item(), '')
# 
#     def test_empty_subscript_assignment(self):
#         a, b = self.d
#         a[()] = 42
#         self.assertEqual(a, 42)
#         b[()] = ''
#         self.assertEqual(b.item(), '')
# 
#     def test_invalid_subscript_assignment(self):
#         a, b = self.d
# 
#         def assign(x, i, v):
#             x[i] = v
# 
#         self.assertRaises(IndexError, assign, a, 0, 42)
#         self.assertRaises(IndexError, assign, b, 0, '')
#         self.assertRaises(ValueError, assign, a, (), '')
# 
#     def test_newaxis(self):
#         a, b = self.d
#         self.assertEqual(a[np.newaxis].shape, (1,))
#         self.assertEqual(a[..., np.newaxis].shape, (1,))
#         self.assertEqual(a[np.newaxis, ...].shape, (1,))
#         self.assertEqual(a[..., np.newaxis].shape, (1,))
#         self.assertEqual(a[np.newaxis, ..., np.newaxis].shape, (1, 1))
#         self.assertEqual(a[..., np.newaxis, np.newaxis].shape, (1, 1))
#         self.assertEqual(a[np.newaxis, np.newaxis, ...].shape, (1, 1))
#         self.assertEqual(a[(np.newaxis,)*10].shape, (1,)*10)
# 
#     def test_invalid_newaxis(self):
#         a, b = self.d
# 
#         def subscript(x, i):
#             x[i]
# 
#         self.assertRaises(IndexError, subscript, a, (np.newaxis, 0))
#         self.assertRaises(IndexError, subscript, a, (np.newaxis,)*50)
# 
#     def test_constructor(self):
#         x = np.ndarray(())
#         x[()] = 5
#         self.assertEqual(x[()], 5)
#         y = np.ndarray((), buffer=x)
#         y[()] = 6
#         self.assertEqual(x[()], 6)
# 
#         # strides and shape must be the same length
#         with pytest.raises(ValueError):
#             np.ndarray((2,), strides=())
#         with pytest.raises(ValueError):
#             np.ndarray((), strides=(2,))
# 
#     def test_output(self):
#         x = np.array(2)
#         self.assertRaises(ValueError, np.add, x, [1], x)
# 
#     def test_real_imag(self):
#         # contiguity checks are for gh-11245
#         x = np.array(1j)
#         xr = x.real
#         xi = x.imag
# 
#         self.assertEqual(xr, np.array(0))
#         self.assertTrue(type(xr) is np.ndarray)
#         self.assertEqual(xr.flags.contiguous, True)
#         self.assertEqual(xr.flags.f_contiguous, True)
# 
#         self.assertEqual(xi, np.array(1))
#         self.assertTrue(type(xi) is np.ndarray)
#         self.assertEqual(xi.flags.contiguous, True)
#         self.assertEqual(xi.flags.f_contiguous, True)

# class TestScalarIndexing(AbstractNumpyTest):
#     def setUp(self):
#         self.d = np.array([0, 1])[0]
# 
#     def test_ellipsis_subscript(self):
#         a = self.d
#         self.assertEqual(a[...], 0)
#         self.assertEqual(a[...].shape, ())
# 
#     def test_empty_subscript(self):
#         a = self.d
#         self.assertEqual(a[()], 0)
#         self.assertEqual(a[()].shape, ())
# 
#     def test_invalid_subscript(self):
#         a = self.d
#         self.assertRaises(IndexError, lambda x: x[0], a)
#         self.assertRaises(IndexError, lambda x: x[np.array([], int)], a)
# 
#     def test_invalid_subscript_assignment(self):
#         a = self.d
# 
#         def assign(x, i, v):
#             x[i] = v
# 
#         self.assertRaises(TypeError, assign, a, 0, 42)
# 
#     def test_newaxis(self):
#         a = self.d
#         self.assertEqual(a[np.newaxis].shape, (1,))
#         self.assertEqual(a[..., np.newaxis].shape, (1,))
#         self.assertEqual(a[np.newaxis, ...].shape, (1,))
#         self.assertEqual(a[..., np.newaxis].shape, (1,))
#         self.assertEqual(a[np.newaxis, ..., np.newaxis].shape, (1, 1))
#         self.assertEqual(a[..., np.newaxis, np.newaxis].shape, (1, 1))
#         self.assertEqual(a[np.newaxis, np.newaxis, ...].shape, (1, 1))
#         self.assertEqual(a[(np.newaxis,)*10].shape, (1,)*10)
# 
#     def test_invalid_newaxis(self):
#         a = self.d
# 
#         def subscript(x, i):
#             x[i]
# 
#         self.assertRaises(IndexError, subscript, a, (np.newaxis, 0))
#         self.assertRaises(IndexError, subscript, a, (np.newaxis,)*50)
# 
#     def test_overlapping_assignment(self):
#         # With positive strides
#         a = np.arange(4)
#         a[:-1] = a[1:]
#         self.assertEqual(a, [1, 2, 3, 3])
# 
#         a = np.arange(4)
#         a[1:] = a[:-1]
#         self.assertEqual(a, [0, 0, 1, 2])
# 
#         # With positive and negative strides
#         a = np.arange(4)
#         a[:] = a[::-1]
#         self.assertEqual(a, [3, 2, 1, 0])
# 
#         a = np.arange(6).reshape(2, 3)
#         a[::-1,:] = a[:, ::-1]
#         self.assertEqual(a, [[5, 4, 3], [2, 1, 0]])
# 
#         a = np.arange(6).reshape(2, 3)
#         a[::-1, ::-1] = a[:, ::-1]
#         self.assertEqual(a, [[3, 4, 5], [0, 1, 2]])
# 
#         # With just one element overlapping
#         a = np.arange(5)
#         a[:3] = a[2:]
#         self.assertEqual(a, [2, 3, 4, 3, 4])
# 
#         a = np.arange(5)
#         a[2:] = a[:3]
#         self.assertEqual(a, [0, 1, 0, 1, 2])
# 
#         a = np.arange(5)
#         a[2::-1] = a[2:]
#         self.assertEqual(a, [4, 3, 2, 3, 4])
# 
#         a = np.arange(5)
#         a[2:] = a[2::-1]
#         self.assertEqual(a, [0, 1, 2, 1, 0])
# 
#         a = np.arange(5)
#         a[2::-1] = a[:1:-1]
#         self.assertEqual(a, [2, 3, 4, 3, 4])
# 
#         a = np.arange(5)
#         a[:1:-1] = a[2::-1]
#         self.assertEqual(a, [0, 1, 0, 1, 2])

class TestCreation(AbstractNumpyTest):
    """
    Test the np.array constructor
    """
    def test_from_attribute(self):
        class x:
            def __array__(self, dtype=None):
                pass

        self.assertRaises(AttributeError, np.array, x())

#     def test_from_string(self):
#         types = np.typecodes['AllInteger'] + np.typecodes['Float']
#         nstr = ['123', '123']
#         result = np.array([123, 123], dtype=int)
#         for type in types:
#             msg = 'String conversion for %s' % type
#             self.assertEqual(np.array(nstr, dtype=type), result, err_msg=msg)

#     def test_void(self):
#         arr = np.array([], dtype='V')
#         self.assertEqual(arr.dtype.kind, 'V')

    def test_zeros(self):
        types = 'if'
        for dt in types:
            d = np.zeros((13,), dtype=dt)
            self.assertEqual(np.count_nonzero(d), 0)
            # true for ieee floats
            self.assertEqual(d.sum(), 0)
            self.assertTrue(not d.any())


#     def test_zeros_obj(self):
#         # test initialization from PyLong(0)
#         d = np.zeros((13,), dtype=object)
#         self.assertEqual(d, [0] * 13)
#         self.assertEqual(np.count_nonzero(d), 0)
# 
#     def test_zeros_obj_obj(self):
#         d = np.zeros(10, dtype=[('k', object, 2)])
#         self.assertEqual(d['k'], 0)


    def test_empty_unicode(self):
        # don't throw decode errors on garbage memory
        for i in range(5, 100, 5):
            d = np.empty(i, dtype='U')
            str(d)


    def test_false_len_sequence(self):
        # gh-7264, segfault for this example
        class C:
            def __getitem__(self, i):
                raise IndexError
            def __len__(self):
                return 42

#         a = (C()) # segfault?
        self.assertRaises(IndexError, np.array, C())

    def test_false_len_iterable(self):
        # Special case where a bad __getitem__ makes us fall back on __iter__:
        class C:
            def __getitem__(self, x):
                raise ValueError
            def __iter__(self):
                return iter(())
            def __len__(self):
                return 2

        a = np.empty(2)
        with self.assertRaises(ValueError):
            a[:] = C()  # Segfault!

    def test_failed_len_sequence(self):
        # gh-7393
        class A:
            def __init__(self, data):
                self._data = data
            def __getitem__(self, item):
#                 return type(self)(self._data[item])
                return self._data[item]
            def __len__(self):
                return len(self._data)

        # len(d) should give 3, but len(d[0]) will fail
        d = A([1,2,3])
        self.assertEqual(len(np.array(d)), 3)

#     def test_array_too_big(self):
#         # Test that array creation succeeds for arrays addressable by intp
#         # on the byte level and fails for too large arrays.
#         buf = np.zeros(100)
# 
#         max_bytes = np.iinfo(np.intp).max
#         for dtype in ["intp", "S20", "b"]:
#             dtype = np.dtype(dtype)
#             itemsize = dtype.itemsize
# 
#             np.ndarray(buffer=buf, strides=(0,),
#                        shape=(max_bytes//itemsize,), dtype=dtype)
#             self.assertRaises(ValueError, np.ndarray, buffer=buf, strides=(0,),
#                           shape=(max_bytes//itemsize + 1,), dtype=dtype)

    def _ragged_creation(self, seq):
        # without dtype=object, the ragged object should raise
#         with assert_warns(np.VisibleDeprecationWarning):
        a = np.array(seq)
        b = np.array(seq, dtype=object)
#         self.assertEqual(a, b)
        return b

#     def test_ragged_ndim_object(self):
#         # Lists of mismatching depths are treated as object arrays
#         a = self._ragged_creation([1, 2, 3])
#         self.assertEqual(a.shape, (3,))
#         self.assertEqual(a.dtype, object)
# 
#         a = self._ragged_creation([1, 2, 3])
#         self.assertEqual(a.shape, (3,))
#         self.assertEqual(a.dtype, object)
# 
#         a = self._ragged_creation([1, 2, 3])
#         self.assertEqual(a.shape, (3,))
#         self.assertEqual(a.dtype, object)

#     def test_ragged_shape_object(self):
#         # The ragged dimension of a list is turned into an object array
#         a = self._ragged_creation([[1, 1], [2], [3]])
#         self.assertEqual(a.shape, (3,))
#         self.assertEqual(a.dtype, object)
# 
#         a = self._ragged_creation([[1], [2, 2], [3]])
#         self.assertEqual(a.shape, (3,))
#         self.assertEqual(a.dtype, object)
# 
#         a = self._ragged_creation([[1], [2], [3, 3]])
#         assert a.shape == (3,)
#         assert a.dtype == object

    def test_array_of_ragged_array(self):
        outer = np.array([None, None])
        outer[0] = outer[1] = np.array([1, 2, 3])
        assert np.array(outer).shape == (2,)
        assert np.array([outer]).shape == (1, 2)

        outer_ragged = np.array([None, None])
        outer_ragged[0] = np.array([1, 2, 3])
        outer_ragged[1] = np.array([1, 2, 3, 4])
        # should both of these emit deprecation warnings?
        assert np.array(outer_ragged).shape == (2,)
        assert np.array([outer_ragged]).shape == (1, 2,)

    def test_deep_nonragged_object(self):
        # None of these should raise, even though they are missing dtype=object
        a = np.array([[[Decimal(1)]]])
        a = np.array([1, Decimal(1)])
        a = np.array([[1], [Decimal(1)]])

class TestMethods(AbstractNumpyTest):

    def test_compress(self):
        tgt = [[5, 6, 7, 8, 9]]
        arr = np.arange(10).reshape(2, 5)
        out = arr.compress([0, 1], axis=0)
        self.assertEqual(out, tgt)

        tgt = [[1, 3], [6, 8]]
        out = arr.compress([0, 1, 0, 1, 0], axis=1)
        self.assertEqual(out, tgt)

        tgt = [[1], [6]]
        arr = np.arange(10).reshape(2, 5)
        out = arr.compress([0, 1], axis=1)
        self.assertEqual(out, tgt)

        arr = np.arange(10).reshape(2, 5)
        out = arr.compress([0, 1])
        self.assertEqual(out, 1)

#     def test_choose(self):
#         x = 2*np.ones((3,), dtype=int)
#         y = 3*np.ones((3,), dtype=int)
#         x2 = 2*np.ones((2, 3), dtype=int)
#         y2 = 3*np.ones((2, 3), dtype=int)
#         ind = np.array([0, 0, 1])
#  
#         A = ind.choose((x, y))
#         self.assertEqual(A, [2, 2, 3])
#  
#         A = ind.choose((x2, y2))
#         self.assertEqual(A, [[2, 2, 3], [2, 2, 3]])
#  
#         A = ind.choose((x, y2))
#         self.assertEqual(A, [[2, 2, 3], [2, 2, 3]])
#  
#         oned = np.ones(1)
#         # gh-12031, caused SEGFAULT
#         self.assertRaises(TypeError, oned.choose,np.void(0), [oned])
#  
#         # gh-6272 check overlap on out
#         x = np.arange(5)
#         y = np.choose([0,0,0], [x[:3], x[:3], x[:3]], out=x[1:4], mode='wrap')
#         self.assertEqual(y, np.array([0, 1, 2]))
 
    def test_prod(self):
        ba = [1, 2, 10, 11, 6, 5, 4]
        ba2 = [[1, 2, 3, 4], [5, 6, 7, 9], [10, 3, 4, 5]]
 
        for ctype in [int, float, long]:
            a = np.array(ba, ctype)
            a2 = np.array(ba2, ctype)
            if ctype in ['1', 'b']:
                self.assertRaises(ArithmeticError, a.prod)
                self.assertRaises(ArithmeticError, a2.prod, axis=1)
            else:
                self.assertEqual(a.prod(axis=0), 26400)
                self.assertEqual(a2.prod(axis=0),
                                   np.array([50, 36, 84, 180], ctype))
                self.assertEqual(a2.prod(axis=-1),
                                   np.array([24, 1890, 600], ctype))
 
    def test_repeat(self):
        m = np.array([1, 2, 3, 4, 5, 6])
        m_rect = m.reshape((2, 3))
 
        A = m.repeat([1, 3, 2, 1, 1, 2])
        self.assertEqual(A, [1, 2, 2, 2, 3,
                         3, 4, 5, 6, 6])
 
        A = m.repeat(2)
        self.assertEqual(A, [1, 1, 2, 2, 3, 3,
                         4, 4, 5, 5, 6, 6])
 
        A = m_rect.repeat([2, 1], axis=0)
        self.assertEqual(A, [[1, 2, 3],
                         [1, 2, 3],
                         [4, 5, 6]])
 
        A = m_rect.repeat([1, 3, 2], axis=1)
        self.assertEqual(A, [[1, 2, 2, 2, 3, 3],
                         [4, 5, 5, 5, 6, 6]])
 
        A = m_rect.repeat(2, axis=0)
        self.assertEqual(A, [[1, 2, 3],
                         [1, 2, 3],
                         [4, 5, 6],
                         [4, 5, 6]])
 
        A = m_rect.repeat(2, axis=1)
        self.assertEqual(A, [[1, 1, 2, 2, 3, 3],
                         [4, 4, 5, 5, 6, 6]])
 
    def test_reshape(self):
        arr = np.array([[1, 2, 3], [4, 5, 6], [7, 8, 9], [10, 11, 12]])
 
        tgt = [[1, 2, 3, 4, 5, 6], [7, 8, 9, 10, 11, 12]]
        self.assertEqual(arr.reshape(2, 6), tgt)
 
        tgt = [[1, 2, 3, 4], [5, 6, 7, 8], [9, 10, 11, 12]]
        self.assertEqual(arr.reshape(3, 4), tgt)
 
        tgt = [[1, 4, 7, 10], [2, 5, 8, 11], [3, 6, 9, 12]]
        self.assertEqual(arr.T.reshape((3, 4)), tgt)
 
    def test_round(self):
        def check_round(arr, expected, *round_args):
            self.assertEqual(arr.round(*round_args), expected)
            # With output array
            out = np.zeros_like(arr)
            res = arr.round(*round_args, out=out)
            self.assertEqual(out, expected)
            self.assertEqual(out, res)
 
        check_round(np.array([1.2, 1.5]), [1, 2])
        check_round(np.array(1.5), 2)
        check_round(np.array([12.2, 15.5]), [10, 20], -1)
        check_round(np.array([12.15, 15.51]), [12.2, 15.5], 1)
        # Complex rounding is not supported
#         check_round(np.array([4.5 + 1.5j]), [4 + 2j])
#         check_round(np.array([12.5 + 15.5j]), [10 + 20j], -1)
 
    def test_squeeze(self):
        a = np.array([[[1], [2], [3]]])
        self.assertEqual(a.squeeze(), [1, 2, 3])
        self.assertEqual(a.squeeze(axis=(0,)), [[1], [2], [3]])
        self.assertRaises(object, a.squeeze, axis=(1,))
        self.assertEqual(a.squeeze(axis=(2,)), [[1, 2, 3]])
 
    def test_transpose(self):
        a = np.array([[1, 2], [3, 4]])
        self.assertEqual(a.transpose(), [[1, 3], [2, 4]])
        self.assertRaises(object, lambda: a.transpose(0))
        self.assertRaises(object, lambda: a.transpose(0, 0))
        self.assertRaises(object, lambda: a.transpose(0, 1, 2))
 
#     def test_sort(self):
        # test ordering for floats and complex containing nans. It is only
        # necessary to check the less-than comparison, so sorts that
        # only follow the insertion sort path are sufficient. We only
        # test doubles and complex doubles as the logic is the same.
 
        # check none value not support
#         msg = "Test real sort order with nans"
#         a = np.array([np.nan, 1, 0])
#         b = np.sort(a)
#         self.assertEqual(b, [0, 1, np.nan], msg)
        # check complex not supported
#         msg = "Test complex sort order with nans"
#         a = np.zeros(9, dtype=np.complex128)
#         a.real += [np.nan, np.nan, np.nan, 1, 0, 1, 1, 0, 0]
#         a.imag += [np.nan, 1, 0, np.nan, np.nan, 1, 0, 1, 0]
#         b = np.sort(a)
#         self.assertEqual(b, a[::-1], msg)
 
    # all c scalar sorts use the same code with different types
    # so it suffices to run a quick check with one type. The number
    # of sorted items must be greater than ~50 to check the actual
    # algorithm because quick and merge sort fall over to insertion
    # sort for small arrays.
# 
    def test_sort_unsigned(self, dtype = None):
        a = np.arange(101, dtype)
        b = np.arange(100, -1, -1, dtype)
        msg = "unsigned scalar sort"
        c = a.copy()
        c.sort()
        self.assertEqual(c, a, msg)
        c = b.copy()
        c.sort()
        self.assertEqual(c, a, msg)

    def test_sort_signed(self, dtype = None):
        a = np.arange(-50, 51, dtype)
        b = np.arange(50, -51, -1, dtype)
        msg = "signed scalar sort"
        c = a.copy()
        c.sort()
        self.assertEqual(c, a, msg)
        c = b.copy()
        c.sort()
        self.assertEqual(c, a, msg)

    def test_sort_axis(self):
        # check axis handling. This should be the same for all type
        # specific sorts, so we only check it for one type and one kind
        a = np.array([[3, 2], [1, 0]])
        b = np.array([[1, 0], [3, 2]])
        c = np.array([[2, 3], [0, 1]])
        d = a.copy()
        d.sort(axis=0)
        self.assertEqual(d, b, "test sort with axis=0")
        d = a.copy()
        d.sort(axis=1)
        self.assertEqual(d, c, "test sort with axis=1")
        d = a.copy()
        d.sort()
        self.assertEqual(d, c, "test sort with default axis")
 
#     def test_sort_degraded(self):
#         # test degraded dataset would take minutes to run with normal qsort
#         d = np.arange(1000000)
#         do = d.copy()
#         x = d
#         # create a median of 3 killer where each median is the sorted second
#         # last element of the quicksort partition
#         while x.size > 3:
#             mid = x.size // 2
#             x[mid], x[-2] = x[-2], x[mid]
#             x = x[:-2]
# 
#         self.assertEqual(np.sort(d), do)
#         self.assertEqual(d[np.argsort(d)], do)
# 
 
#     def test_sort_order(self):
#         # Test sorting an array with fields
#         x1 = np.array([21, 32, 14])
#         x2 = np.array(['my', 'first', 'name'])
#         x3 = np.array([3.1, 4.5, 6.2])
#         r = np.rec.fromarrays([x1, x2, x3], names='id,word,number')
# 
#         r.sort(order=['id'])
#         self.assertEqual(r.id, np.array([14, 21, 32]))
#         self.assertEqual(r.word, np.array(['name', 'my', 'first']))
#         self.assertEqual(r.number, np.array([6.2, 3.1, 4.5]))
# 
#         r.sort(order=['word'])
#         self.assertEqual(r.id, np.array([32, 21, 14]))
#         self.assertEqual(r.word, np.array(['first', 'my', 'name']))
#         self.assertEqual(r.number, np.array([4.5, 3.1, 6.2]))
# 
#         r.sort(order=['number'])
#         self.assertEqual(r.id, np.array([21, 32, 14]))
#         self.assertEqual(r.word, np.array(['my', 'first', 'name']))
#         self.assertEqual(r.number, np.array([3.1, 4.5, 6.2]))
# 
#         assert_raises_regex(ValueError, 'duplicate',
#             lambda: r.sort(order=['id', 'id']))
# 
#         if sys.byteorder == 'little':
#             strtype = '>i2'
#         else:
#             strtype = '<i2'
#         mydtype = [('name', strchar + '5'), ('col2', strtype)]
#         r = np.array([('a', 1), ('b', 255), ('c', 3), ('d', 258)],
#                      dtype=mydtype)
#         r.sort(order='col2')
#         self.assertEqual(r['col2'], [1, 3, 255, 258])
#         self.assertEqual(r, np.array([('a', 1), ('c', 3), ('b', 255), ('d', 258)],
#                                  dtype=mydtype))
# 
#     def test_argsort(self):
#         # all c scalar argsorts use the same code with different types
#         # so it suffices to run a quick check with one type. The number
#         # of sorted items must be greater than ~50 to check the actual
#         # algorithm because quick and merge sort fall over to insertion
#         # sort for small arrays.
# 
#         for dtype in [np.int32, np.uint32, np.float32]:
#             a = np.arange(101, dtype=dtype)
#             b = a[::-1].copy()
#             for kind in self.sort_kinds:
#                 msg = "scalar argsort, kind=%s, dtype=%s" % (kind, dtype)
#                 self.assertEqual(a.copy().argsort(kind=kind), a, msg)
#                 self.assertEqual(b.copy().argsort(kind=kind), b, msg)
# 
#         # test complex argsorts. These use the same code as the scalars
#         # but the compare function differs.
#         ai = a*1j + 1
#         bi = b*1j + 1
#         for kind in self.sort_kinds:
#             msg = "complex argsort, kind=%s" % kind
#             self.assertEqual(ai.copy().argsort(kind=kind), a, msg)
#             self.assertEqual(bi.copy().argsort(kind=kind), b, msg)
#         ai = a + 1j
#         bi = b + 1j
#         for kind in self.sort_kinds:
#             msg = "complex argsort, kind=%s" % kind
#             self.assertEqual(ai.copy().argsort(kind=kind), a, msg)
#             self.assertEqual(bi.copy().argsort(kind=kind), b, msg)
# 
#         # test argsort of complex arrays requiring byte-swapping, gh-5441
#         for endianness in '<>':
#             for dt in np.typecodes['Complex']:
#                 arr = np.array([1+3.j, 2+2.j, 3+1.j], dtype=endianness + dt)
#                 msg = 'byte-swapped complex argsort, dtype={0}'.format(dt)
#                 self.assertEqual(arr.argsort(),
#                              np.arange(len(arr), dtype=np.intp), msg)
# 
#         # test string argsorts.
#         s = 'aaaaaaaa'
#         a = np.array([s + chr(i) for i in range(101)])
#         b = a[::-1].copy()
#         r = np.arange(101)
#         rr = r[::-1]
#         for kind in self.sort_kinds:
#             msg = "string argsort, kind=%s" % kind
#             self.assertEqual(a.copy().argsort(kind=kind), r, msg)
#             self.assertEqual(b.copy().argsort(kind=kind), rr, msg)
# 
#         # test unicode argsorts.
#         s = 'aaaaaaaa'
#         a = np.array([s + chr(i) for i in range(101)], dtype=np.unicode_)
#         b = a[::-1]
#         r = np.arange(101)
#         rr = r[::-1]
#         for kind in self.sort_kinds:
#             msg = "unicode argsort, kind=%s" % kind
#             self.assertEqual(a.copy().argsort(kind=kind), r, msg)
#             self.assertEqual(b.copy().argsort(kind=kind), rr, msg)
# 
#         # test object array argsorts.
#         a = np.empty((101,), dtype=object)
#         a[:] = list(range(101))
#         b = a[::-1]
#         r = np.arange(101)
#         rr = r[::-1]
#         for kind in self.sort_kinds:
#             msg = "object argsort, kind=%s" % kind
#             self.assertEqual(a.copy().argsort(kind=kind), r, msg)
#             self.assertEqual(b.copy().argsort(kind=kind), rr, msg)
# 
#         # test structured array argsorts.
#         dt = np.dtype([('f', float), ('i', int)])
#         a = np.array([(i, i) for i in range(101)], dtype=dt)
#         b = a[::-1]
#         r = np.arange(101)
#         rr = r[::-1]
#         for kind in self.sort_kinds:
#             msg = "structured array argsort, kind=%s" % kind
#             self.assertEqual(a.copy().argsort(kind=kind), r, msg)
#             self.assertEqual(b.copy().argsort(kind=kind), rr, msg)
# 
#         # test datetime64 argsorts.
#         a = np.arange(0, 101, dtype='datetime64[D]')
#         b = a[::-1]
#         r = np.arange(101)
#         rr = r[::-1]
#         for kind in ['q', 'h', 'm']:
#             msg = "datetime64 argsort, kind=%s" % kind
#             self.assertEqual(a.copy().argsort(kind=kind), r, msg)
#             self.assertEqual(b.copy().argsort(kind=kind), rr, msg)
# 
#         # test timedelta64 argsorts.
#         a = np.arange(0, 101, dtype='timedelta64[D]')
#         b = a[::-1]
#         r = np.arange(101)
#         rr = r[::-1]
#         for kind in ['q', 'h', 'm']:
#             msg = "timedelta64 argsort, kind=%s" % kind
#             self.assertEqual(a.copy().argsort(kind=kind), r, msg)
#             self.assertEqual(b.copy().argsort(kind=kind), rr, msg)
# 
#         # check axis handling. This should be the same for all type
#         # specific argsorts, so we only check it for one type and one kind
#         a = np.array([[3, 2], [1, 0]])
#         b = np.array([[1, 1], [0, 0]])
#         c = np.array([[1, 0], [1, 0]])
#         self.assertEqual(a.copy().argsort(axis=0), b)
#         self.assertEqual(a.copy().argsort(axis=1), c)
#         self.assertEqual(a.copy().argsort(), c)
# 
#         # check axis handling for multidimensional empty arrays
#         a = np.array([])
#         a.shape = (3, 2, 1, 0)
#         for axis in range(-a.ndim, a.ndim):
#             msg = 'test empty array argsort with axis={0}'.format(axis)
#             self.assertEqual(np.argsort(a, axis=axis),
#                          np.zeros_like(a, dtype=np.intp), msg)
#         msg = 'test empty array argsort with axis=None'
#         self.assertEqual(np.argsort(a, axis=None),
#                      np.zeros_like(a.ravel(), dtype=np.intp), msg)
# 
#         # check that stable argsorts are stable
#         r = np.arange(100)
#         # scalars
#         a = np.zeros(100)
#         self.assertEqual(a.argsort(kind='m'), r)
#         # complex
#         a = np.zeros(100, dtype=complex)
#         self.assertEqual(a.argsort(kind='m'), r)
#         # string
#         a = np.array(['aaaaaaaaa' for i in range(100)])
#         self.assertEqual(a.argsort(kind='m'), r)
#         # unicode
#         a = np.array(['aaaaaaaaa' for i in range(100)], dtype=np.unicode_)
#         self.assertEqual(a.argsort(kind='m'), r)
# 
#     def test_sort_unicode_kind(self):
#         d = np.arange(10)
#         k = b'\xc3\xa4'.decode("UTF8")
#         self.assertRaises(ValueError, d.sort, kind=k)
#         self.assertRaises(ValueError, d.argsort, kind=k)
# 
    def test_searchsorted(self):
        # test for floats and complex containing nans. The logic is the
        # same for all float types so only test double types for now.
        # The search sorted routines use the compare functions for the
        # array type, so this checks if that is consistent with the sort
        # order.
 
        # check double
        a = np.array([0, 1, np.nan])
        msg = "Test real searchsorted with nans, side='l'"
        b = a.searchsorted(a, side='l')
        self.assertEqual(b, np.arange(3), msg)
        msg = "Test real searchsorted with nans, side='r'"
        b = a.searchsorted(a, side='r')
        self.assertEqual(b, np.arange(1, 4), msg)
        # check keyword arguments
        a.searchsorted(v=1)
        # check double complex
        a = np.zeros(9, dtype=np.complex128)
        a.real += [0, 0, 1, 1, 0, 1, np.nan, np.nan, np.nan]
        a.imag += [0, 1, 0, 1, np.nan, np.nan, 0, 1, np.nan]
        msg = "Test complex searchsorted with nans, side='l'"
        b = a.searchsorted(a, side='l')
        self.assertEqual(b, np.arange(9), msg)
        msg = "Test complex searchsorted with nans, side='r'"
        b = a.searchsorted(a, side='r')
        self.assertEqual(b, np.arange(1, 10), msg)
        msg = "Test searchsorted with little endian, side='l'"
        a = np.array([0, 128], dtype='<i4')
        b = a.searchsorted(np.array(128, dtype='<i4'))
        self.assertEqual(b, 1, msg)
        msg = "Test searchsorted with big endian, side='l'"
        a = np.array([0, 128], dtype='>i4')
        b = a.searchsorted(np.array(128, dtype='>i4'))
        self.assertEqual(b, 1, msg)
 
        # Check 0 elements
        a = np.ones(0)
        b = a.searchsorted([0, 1, 2], 'l')
        self.assertEqual(b, [0, 0, 0])
        b = a.searchsorted([0, 1, 2], 'r')
        self.assertEqual(b, [0, 0, 0])
        a = np.ones(1)
        # Check 1 element
        b = a.searchsorted([0, 1, 2], 'l')
        self.assertEqual(b, [0, 0, 1])
        b = a.searchsorted([0, 1, 2], 'r')
        self.assertEqual(b, [0, 1, 1])
        # Check all elements equal
        a = np.ones(2)
        b = a.searchsorted([0, 1, 2], 'l')
        self.assertEqual(b, [0, 0, 2])
        b = a.searchsorted([0, 1, 2], 'r')
        self.assertEqual(b, [0, 2, 2])
 
        # Test searching unaligned array
        a = np.arange(10)
        aligned = np.empty(a.itemsize * a.size + 1, 'uint8')
        unaligned = aligned[1:].view(a.dtype)
        unaligned[:] = a
        # Test searching unaligned array
        b = unaligned.searchsorted(a, 'l')
        self.assertEqual(b, a)
        b = unaligned.searchsorted(a, 'r')
        self.assertEqual(b, a + 1)
        # Test searching for unaligned keys
        b = a.searchsorted(unaligned, 'l')
        self.assertEqual(b, a)
        b = a.searchsorted(unaligned, 'r')
        self.assertEqual(b, a + 1)
 
        # Test smart resetting of binsearch indices
        a = np.arange(5)
        b = a.searchsorted([6, 5, 4], 'l')
        self.assertEqual(b, [5, 5, 4])
        b = a.searchsorted([6, 5, 4], 'r')
        self.assertEqual(b, [5, 5, 5])
 
        # Test all type specific binary search functions
        types = ''.join((np.typecodes['AllInteger'], np.typecodes['AllFloat'],
                         np.typecodes['Datetime'], '?O'))
        for dt in types:
            if dt == 'M':
                dt = 'M8[D]'
            if dt == '?':
                a = np.arange(2, dtype=dt)
                out = np.arange(2)
            else:
                a = np.arange(0, 5, dtype=dt)
                out = np.arange(5)
            b = a.searchsorted(a, 'l')
            self.assertEqual(b, out)
            b = a.searchsorted(a, 'r')
            self.assertEqual(b, out + 1)
            # Test empty array, use a fresh array to get warnings in
            # valgrind if access happens.
            e = np.ndarray(shape=0, buffer=b'', dtype=dt)
            b = e.searchsorted(a, 'l')
            self.assertEqual(b, np.zeros(len(a), dtype=np.intp))
            b = a.searchsorted(e, 'l')
            self.assertEqual(b, np.zeros(0, dtype=np.intp))
 
#     def test_searchsorted_unicode(self):
#         # Test searchsorted on unicode strings.
# 
#         # 1.6.1 contained a string length miscalculation in
#         # arraytypes.c.src:UNICODE_compare() which manifested as
#         # incorrect/inconsistent results from searchsorted.
#         a = np.array(['P:\\20x_dapi_cy3\\20x_dapi_cy3_20100185_1',
#                       'P:\\20x_dapi_cy3\\20x_dapi_cy3_20100186_1',
#                       'P:\\20x_dapi_cy3\\20x_dapi_cy3_20100187_1',
#                       'P:\\20x_dapi_cy3\\20x_dapi_cy3_20100189_1',
#                       'P:\\20x_dapi_cy3\\20x_dapi_cy3_20100190_1',
#                       'P:\\20x_dapi_cy3\\20x_dapi_cy3_20100191_1',
#                       'P:\\20x_dapi_cy3\\20x_dapi_cy3_20100192_1',
#                       'P:\\20x_dapi_cy3\\20x_dapi_cy3_20100193_1',
#                       'P:\\20x_dapi_cy3\\20x_dapi_cy3_20100194_1',
#                       'P:\\20x_dapi_cy3\\20x_dapi_cy3_20100195_1',
#                       'P:\\20x_dapi_cy3\\20x_dapi_cy3_20100196_1',
#                       'P:\\20x_dapi_cy3\\20x_dapi_cy3_20100197_1',
#                       'P:\\20x_dapi_cy3\\20x_dapi_cy3_20100198_1',
#                       'P:\\20x_dapi_cy3\\20x_dapi_cy3_20100199_1'],
#                      dtype=np.unicode_)
#         ind = np.arange(len(a))
#         self.assertEqual([a.searchsorted(v, 'left') for v in a], ind)
#         self.assertEqual([a.searchsorted(v, 'right') for v in a], ind + 1)
#         self.assertEqual([a.searchsorted(a[i], 'left') for i in ind], ind)
#         self.assertEqual([a.searchsorted(a[i], 'right') for i in ind], ind + 1)
# 
#     def test_searchsorted_with_invalid_sorter(self):
#         a = np.array([5, 2, 1, 3, 4])
#         s = np.argsort(a)
#         self.assertRaises(TypeError, np.searchsorted, a, 0,
#                       sorter=np.array((1, (2, 3)), dtype=object))
#         self.assertRaises(TypeError, np.searchsorted, a, 0, sorter=[1.1])
#         self.assertRaises(ValueError, np.searchsorted, a, 0, sorter=[1, 2, 3, 4])
#         self.assertRaises(ValueError, np.searchsorted, a, 0, sorter=[1, 2, 3, 4, 5, 6])
# 
#         # bounds check
#         self.assertRaises(ValueError, np.searchsorted, a, 4, sorter=[0, 1, 2, 3, 5])
#         self.assertRaises(ValueError, np.searchsorted, a, 0, sorter=[-1, 0, 1, 2, 3])
#         self.assertRaises(ValueError, np.searchsorted, a, 0, sorter=[4, 0, -1, 2, 3])
# 
#     def test_searchsorted_with_sorter(self):
#         a = np.random.rand(300)
#         s = a.argsort()
#         b = np.sort(a)
#         k = np.linspace(0, 1, 20)
#         self.assertEqual(b.searchsorted(k), a.searchsorted(k, sorter=s))
# 
#         a = np.array([0, 1, 2, 3, 5]*20)
#         s = a.argsort()
#         k = [0, 1, 2, 3, 5]
#         expected = [0, 20, 40, 60, 80]
#         self.assertEqual(a.searchsorted(k, side='l', sorter=s), expected)
#         expected = [20, 40, 60, 80, 100]
#         self.assertEqual(a.searchsorted(k, side='r', sorter=s), expected)
# 
#         # Test searching unaligned array
#         keys = np.arange(10)
#         a = keys.copy()
#         np.random.shuffle(s)
#         s = a.argsort()
#         aligned = np.empty(a.itemsize * a.size + 1, 'uint8')
#         unaligned = aligned[1:].view(a.dtype)
#         # Test searching unaligned array
#         unaligned[:] = a
#         b = unaligned.searchsorted(keys, 'l', s)
#         self.assertEqual(b, keys)
#         b = unaligned.searchsorted(keys, 'r', s)
#         self.assertEqual(b, keys + 1)
#         # Test searching for unaligned keys
#         unaligned[:] = keys
#         b = a.searchsorted(unaligned, 'l', s)
#         self.assertEqual(b, keys)
#         b = a.searchsorted(unaligned, 'r', s)
#         self.assertEqual(b, keys + 1)
# 
#         # Test all type specific indirect binary search functions
#         types = ''.join((np.typecodes['AllInteger'], np.typecodes['AllFloat'],
#                          np.typecodes['Datetime'], '?O'))
#         for dt in types:
#             if dt == 'M':
#                 dt = 'M8[D]'
#             if dt == '?':
#                 a = np.array([1, 0], dtype=dt)
#                 # We want the sorter array to be of a type that is different
#                 # from np.intp in all platforms, to check for #4698
#                 s = np.array([1, 0], dtype=np.int16)
#                 out = np.array([1, 0])
#             else:
#                 a = np.array([3, 4, 1, 2, 0], dtype=dt)
#                 # We want the sorter array to be of a type that is different
#                 # from np.intp in all platforms, to check for #4698
#                 s = np.array([4, 2, 3, 0, 1], dtype=np.int16)
#                 out = np.array([3, 4, 1, 2, 0], dtype=np.intp)
#             b = a.searchsorted(a, 'l', s)
#             self.assertEqual(b, out)
#             b = a.searchsorted(a, 'r', s)
#             self.assertEqual(b, out + 1)
#             # Test empty array, use a fresh array to get warnings in
#             # valgrind if access happens.
#             e = np.ndarray(shape=0, buffer=b'', dtype=dt)
#             b = e.searchsorted(a, 'l', s[:0])
#             self.assertEqual(b, np.zeros(len(a), dtype=np.intp))
#             b = a.searchsorted(e, 'l', s)
#             self.assertEqual(b, np.zeros(0, dtype=np.intp))
# 
#         # Test non-contiguous sorter array
#         a = np.array([3, 4, 1, 2, 0])
#         srt = np.empty((10,), dtype=np.intp)
#         srt[1::2] = -1
#         srt[::2] = [4, 2, 3, 0, 1]
#         s = srt[::2]
#         out = np.array([3, 4, 1, 2, 0], dtype=np.intp)
#         b = a.searchsorted(a, 'l', s)
#         self.assertEqual(b, out)
#         b = a.searchsorted(a, 'r', s)
#         self.assertEqual(b, out + 1)
# 
#     def test_searchsorted_return_type(self):
#         # Functions returning indices should always return base ndarrays
#         class A(np.ndarray):
#             pass
#         a = np.arange(5).view(A)
#         b = np.arange(1, 3).view(A)
#         s = np.arange(5).view(A)
#         assert_(not isinstance(a.searchsorted(b, 'l'), A))
#         assert_(not isinstance(a.searchsorted(b, 'r'), A))
#         assert_(not isinstance(a.searchsorted(b, 'l', s), A))
#         assert_(not isinstance(a.searchsorted(b, 'r', s), A))
# 
#     def test_argpartition_out_of_range(self):
#         # Test out of range values in kth raise an error, gh-5469
#         d = np.arange(10)
#         self.assertRaises(ValueError, d.argpartition, 10)
#         self.assertRaises(ValueError, d.argpartition, -11)
#         # Test also for generic type argpartition, which uses sorting
#         # and used to not bound check kth
#         d_obj = np.arange(10, dtype=object)
#         self.assertRaises(ValueError, d_obj.argpartition, 10)
#         self.assertRaises(ValueError, d_obj.argpartition, -11)
# 
#     def test_partition_out_of_range(self):
#         # Test out of range values in kth raise an error, gh-5469
#         d = np.arange(10)
#         self.assertRaises(ValueError, d.partition, 10)
#         self.assertRaises(ValueError, d.partition, -11)
#         # Test also for generic type partition, which uses sorting
#         # and used to not bound check kth
#         d_obj = np.arange(10, dtype=object)
#         self.assertRaises(ValueError, d_obj.partition, 10)
#         self.assertRaises(ValueError, d_obj.partition, -11)
# 
#     def test_argpartition_integer(self):
#         # Test non-integer values in kth raise an error/
#         d = np.arange(10)
#         self.assertRaises(TypeError, d.argpartition, 9.)
#         # Test also for generic type argpartition, which uses sorting
#         # and used to not bound check kth
#         d_obj = np.arange(10, dtype=object)
#         self.assertRaises(TypeError, d_obj.argpartition, 9.)
# 
#     def test_partition_integer(self):
#         # Test out of range values in kth raise an error, gh-5469
#         d = np.arange(10)
#         self.assertRaises(TypeError, d.partition, 9.)
#         # Test also for generic type partition, which uses sorting
#         # and used to not bound check kth
#         d_obj = np.arange(10, dtype=object)
#         self.assertRaises(TypeError, d_obj.partition, 9.)
# 
#     def test_partition_empty_array(self):
#         # check axis handling for multidimensional empty arrays
#         a = np.array([])
#         a.shape = (3, 2, 1, 0)
#         for axis in range(-a.ndim, a.ndim):
#             msg = 'test empty array partition with axis={0}'.format(axis)
#             self.assertEqual(np.partition(a, 0, axis=axis), a, msg)
#         msg = 'test empty array partition with axis=None'
#         self.assertEqual(np.partition(a, 0, axis=None), a.ravel(), msg)
# 
#     def test_argpartition_empty_array(self):
#         # check axis handling for multidimensional empty arrays
#         a = np.array([])
#         a.shape = (3, 2, 1, 0)
#         for axis in range(-a.ndim, a.ndim):
#             msg = 'test empty array argpartition with axis={0}'.format(axis)
#             self.assertEqual(np.partition(a, 0, axis=axis),
#                          np.zeros_like(a, dtype=np.intp), msg)
#         msg = 'test empty array argpartition with axis=None'
#         self.assertEqual(np.partition(a, 0, axis=None),
#                      np.zeros_like(a.ravel(), dtype=np.intp), msg)
# 
#     def test_partition(self):
#         d = np.arange(10)
#         self.assertRaises(TypeError, np.partition, d, 2, kind=1)
#         self.assertRaises(ValueError, np.partition, d, 2, kind="nonsense")
#         self.assertRaises(ValueError, np.argpartition, d, 2, kind="nonsense")
#         self.assertRaises(ValueError, d.partition, 2, axis=0, kind="nonsense")
#         self.assertRaises(ValueError, d.argpartition, 2, axis=0, kind="nonsense")
#         for k in ("introselect",):
#             d = np.array([])
#             self.assertEqual(np.partition(d, 0, kind=k), d)
#             self.assertEqual(np.argpartition(d, 0, kind=k), d)
#             d = np.ones(1)
#             self.assertEqual(np.partition(d, 0, kind=k)[0], d)
#             self.assertEqual(d[np.argpartition(d, 0, kind=k)],
#                                np.partition(d, 0, kind=k))
# 
#             # kth not modified
#             kth = np.array([30, 15, 5])
#             okth = kth.copy()
#             np.partition(np.arange(40), kth)
#             self.assertEqual(kth, okth)
# 
#             for r in ([2, 1], [1, 2], [1, 1]):
#                 d = np.array(r)
#                 tgt = np.sort(d)
#                 self.assertEqual(np.partition(d, 0, kind=k)[0], tgt[0])
#                 self.assertEqual(np.partition(d, 1, kind=k)[1], tgt[1])
#                 self.assertEqual(d[np.argpartition(d, 0, kind=k)],
#                                    np.partition(d, 0, kind=k))
#                 self.assertEqual(d[np.argpartition(d, 1, kind=k)],
#                                    np.partition(d, 1, kind=k))
#                 for i in range(d.size):
#                     d[i:].partition(0, kind=k)
#                 self.assertEqual(d, tgt)
# 
#             for r in ([3, 2, 1], [1, 2, 3], [2, 1, 3], [2, 3, 1],
#                       [1, 1, 1], [1, 2, 2], [2, 2, 1], [1, 2, 1]):
#                 d = np.array(r)
#                 tgt = np.sort(d)
#                 self.assertEqual(np.partition(d, 0, kind=k)[0], tgt[0])
#                 self.assertEqual(np.partition(d, 1, kind=k)[1], tgt[1])
#                 self.assertEqual(np.partition(d, 2, kind=k)[2], tgt[2])
#                 self.assertEqual(d[np.argpartition(d, 0, kind=k)],
#                                    np.partition(d, 0, kind=k))
#                 self.assertEqual(d[np.argpartition(d, 1, kind=k)],
#                                    np.partition(d, 1, kind=k))
#                 self.assertEqual(d[np.argpartition(d, 2, kind=k)],
#                                    np.partition(d, 2, kind=k))
#                 for i in range(d.size):
#                     d[i:].partition(0, kind=k)
#                 self.assertEqual(d, tgt)
# 
#             d = np.ones(50)
#             self.assertEqual(np.partition(d, 0, kind=k), d)
#             self.assertEqual(d[np.argpartition(d, 0, kind=k)],
#                                np.partition(d, 0, kind=k))
# 
#             # sorted
#             d = np.arange(49)
#             self.assertEqual(np.partition(d, 5, kind=k)[5], 5)
#             self.assertEqual(np.partition(d, 15, kind=k)[15], 15)
#             self.assertEqual(d[np.argpartition(d, 5, kind=k)],
#                                np.partition(d, 5, kind=k))
#             self.assertEqual(d[np.argpartition(d, 15, kind=k)],
#                                np.partition(d, 15, kind=k))
# 
#             # rsorted
#             d = np.arange(47)[::-1]
#             self.assertEqual(np.partition(d, 6, kind=k)[6], 6)
#             self.assertEqual(np.partition(d, 16, kind=k)[16], 16)
#             self.assertEqual(d[np.argpartition(d, 6, kind=k)],
#                                np.partition(d, 6, kind=k))
#             self.assertEqual(d[np.argpartition(d, 16, kind=k)],
#                                np.partition(d, 16, kind=k))
# 
#             self.assertEqual(np.partition(d, -6, kind=k),
#                                np.partition(d, 41, kind=k))
#             self.assertEqual(np.partition(d, -16, kind=k),
#                                np.partition(d, 31, kind=k))
#             self.assertEqual(d[np.argpartition(d, -6, kind=k)],
#                                np.partition(d, 41, kind=k))
# 
#             # median of 3 killer, O(n^2) on pure median 3 pivot quickselect
#             # exercises the median of median of 5 code used to keep O(n)
#             d = np.arange(1000000)
#             x = np.roll(d, d.size // 2)
#             mid = x.size // 2 + 1
#             self.assertEqual(np.partition(x, mid)[mid], mid)
#             d = np.arange(1000001)
#             x = np.roll(d, d.size // 2 + 1)
#             mid = x.size // 2 + 1
#             self.assertEqual(np.partition(x, mid)[mid], mid)
# 
#             # max
#             d = np.ones(10)
#             d[1] = 4
#             self.assertEqual(np.partition(d, (2, -1))[-1], 4)
#             self.assertEqual(np.partition(d, (2, -1))[2], 1)
#             self.assertEqual(d[np.argpartition(d, (2, -1))][-1], 4)
#             self.assertEqual(d[np.argpartition(d, (2, -1))][2], 1)
#             d[1] = np.nan
#             assert_(np.isnan(d[np.argpartition(d, (2, -1))][-1]))
#             assert_(np.isnan(np.partition(d, (2, -1))[-1]))
# 
#             # equal elements
#             d = np.arange(47) % 7
#             tgt = np.sort(np.arange(47) % 7)
#             np.random.shuffle(d)
#             for i in range(d.size):
#                 self.assertEqual(np.partition(d, i, kind=k)[i], tgt[i])
#             self.assertEqual(d[np.argpartition(d, 6, kind=k)],
#                                np.partition(d, 6, kind=k))
#             self.assertEqual(d[np.argpartition(d, 16, kind=k)],
#                                np.partition(d, 16, kind=k))
#             for i in range(d.size):
#                 d[i:].partition(0, kind=k)
#             self.assertEqual(d, tgt)
# 
#             d = np.array([0, 1, 2, 3, 4, 5, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
#                           7, 7, 7, 7, 7, 9])
#             kth = [0, 3, 19, 20]
#             self.assertEqual(np.partition(d, kth, kind=k)[kth], (0, 3, 7, 7))
#             self.assertEqual(d[np.argpartition(d, kth, kind=k)][kth], (0, 3, 7, 7))
# 
#             d = np.array([2, 1])
#             d.partition(0, kind=k)
#             self.assertRaises(ValueError, d.partition, 2)
#             self.assertRaises(np.AxisError, d.partition, 3, axis=1)
#             self.assertRaises(ValueError, np.partition, d, 2)
#             self.assertRaises(np.AxisError, np.partition, d, 2, axis=1)
#             self.assertRaises(ValueError, d.argpartition, 2)
#             self.assertRaises(np.AxisError, d.argpartition, 3, axis=1)
#             self.assertRaises(ValueError, np.argpartition, d, 2)
#             self.assertRaises(np.AxisError, np.argpartition, d, 2, axis=1)
#             d = np.arange(10).reshape((2, 5))
#             d.partition(1, axis=0, kind=k)
#             d.partition(4, axis=1, kind=k)
#             np.partition(d, 1, axis=0, kind=k)
#             np.partition(d, 4, axis=1, kind=k)
#             np.partition(d, 1, axis=None, kind=k)
#             np.partition(d, 9, axis=None, kind=k)
#             d.argpartition(1, axis=0, kind=k)
#             d.argpartition(4, axis=1, kind=k)
#             np.argpartition(d, 1, axis=0, kind=k)
#             np.argpartition(d, 4, axis=1, kind=k)
#             np.argpartition(d, 1, axis=None, kind=k)
#             np.argpartition(d, 9, axis=None, kind=k)
#             self.assertRaises(ValueError, d.partition, 2, axis=0)
#             self.assertRaises(ValueError, d.partition, 11, axis=1)
#             self.assertRaises(TypeError, d.partition, 2, axis=None)
#             self.assertRaises(ValueError, np.partition, d, 9, axis=1)
#             self.assertRaises(ValueError, np.partition, d, 11, axis=None)
#             self.assertRaises(ValueError, d.argpartition, 2, axis=0)
#             self.assertRaises(ValueError, d.argpartition, 11, axis=1)
#             self.assertRaises(ValueError, np.argpartition, d, 9, axis=1)
#             self.assertRaises(ValueError, np.argpartition, d, 11, axis=None)
# 
#             td = [(dt, s) for dt in [np.int32, np.float32, np.complex64]
#                   for s in (9, 16)]
#             for dt, s in td:
#                 aae = assert_array_equal
#                 at = assert_
# 
#                 d = np.arange(s, dtype=dt)
#                 np.random.shuffle(d)
#                 d1 = np.tile(np.arange(s, dtype=dt), (4, 1))
#                 map(np.random.shuffle, d1)
#                 d0 = np.transpose(d1)
#                 for i in range(d.size):
#                     p = np.partition(d, i, kind=k)
#                     self.assertEqual(p[i], i)
#                     # all before are smaller
#                     assert_array_less(p[:i], p[i])
#                     # all after are larger
#                     assert_array_less(p[i], p[i + 1:])
#                     aae(p, d[np.argpartition(d, i, kind=k)])
# 
#                     p = np.partition(d1, i, axis=1, kind=k)
#                     aae(p[:, i], np.array([i] * d1.shape[0], dtype=dt))
#                     # array_less does not seem to work right
#                     at((p[:, :i].T <= p[:, i]).all(),
#                        msg="%d: %r <= %r" % (i, p[:, i], p[:, :i].T))
#                     at((p[:, i + 1:].T > p[:, i]).all(),
#                        msg="%d: %r < %r" % (i, p[:, i], p[:, i + 1:].T))
#                     aae(p, d1[np.arange(d1.shape[0])[:, None],
#                         np.argpartition(d1, i, axis=1, kind=k)])
# 
#                     p = np.partition(d0, i, axis=0, kind=k)
#                     aae(p[i, :], np.array([i] * d1.shape[0], dtype=dt))
#                     # array_less does not seem to work right
#                     at((p[:i, :] <= p[i, :]).all(),
#                        msg="%d: %r <= %r" % (i, p[i, :], p[:i, :]))
#                     at((p[i + 1:, :] > p[i, :]).all(),
#                        msg="%d: %r < %r" % (i, p[i, :], p[:, i + 1:]))
#                     aae(p, d0[np.argpartition(d0, i, axis=0, kind=k),
#                         np.arange(d0.shape[1])[None, :]])
# 
#                     # check inplace
#                     dc = d.copy()
#                     dc.partition(i, kind=k)
#                     self.assertEqual(dc, np.partition(d, i, kind=k))
#                     dc = d0.copy()
#                     dc.partition(i, axis=0, kind=k)
#                     self.assertEqual(dc, np.partition(d0, i, axis=0, kind=k))
#                     dc = d1.copy()
#                     dc.partition(i, axis=1, kind=k)
#                     self.assertEqual(dc, np.partition(d1, i, axis=1, kind=k))
# 
#     def assert_partitioned(self, d, kth):
#         prev = 0
#         for k in np.sort(kth):
#             assert_array_less(d[prev:k], d[k], err_msg='kth %d' % k)
#             assert_((d[k:] >= d[k]).all(),
#                     msg="kth %d, %r not greater equal %d" % (k, d[k:], d[k]))
#             prev = k + 1
# 
#     def test_partition_iterative(self):
#             d = np.arange(17)
#             kth = (0, 1, 2, 429, 231)
#             self.assertRaises(ValueError, d.partition, kth)
#             self.assertRaises(ValueError, d.argpartition, kth)
#             d = np.arange(10).reshape((2, 5))
#             self.assertRaises(ValueError, d.partition, kth, axis=0)
#             self.assertRaises(ValueError, d.partition, kth, axis=1)
#             self.assertRaises(ValueError, np.partition, d, kth, axis=1)
#             self.assertRaises(ValueError, np.partition, d, kth, axis=None)
# 
#             d = np.array([3, 4, 2, 1])
#             p = np.partition(d, (0, 3))
#             self.assert_partitioned(p, (0, 3))
#             self.assert_partitioned(d[np.argpartition(d, (0, 3))], (0, 3))
# 
#             self.assertEqual(p, np.partition(d, (-3, -1)))
#             self.assertEqual(p, d[np.argpartition(d, (-3, -1))])
# 
#             d = np.arange(17)
#             np.random.shuffle(d)
#             d.partition(range(d.size))
#             self.assertEqual(np.arange(17), d)
#             np.random.shuffle(d)
#             self.assertEqual(np.arange(17), d[d.argpartition(range(d.size))])
# 
#             # test unsorted kth
#             d = np.arange(17)
#             np.random.shuffle(d)
#             keys = np.array([1, 3, 8, -2])
#             np.random.shuffle(d)
#             p = np.partition(d, keys)
#             self.assert_partitioned(p, keys)
#             p = d[np.argpartition(d, keys)]
#             self.assert_partitioned(p, keys)
#             np.random.shuffle(keys)
#             self.assertEqual(np.partition(d, keys), p)
#             self.assertEqual(d[np.argpartition(d, keys)], p)
# 
#             # equal kth
#             d = np.arange(20)[::-1]
#             self.assert_partitioned(np.partition(d, [5]*4), [5])
#             self.assert_partitioned(np.partition(d, [5]*4 + [6, 13]),
#                                     [5]*4 + [6, 13])
#             self.assert_partitioned(d[np.argpartition(d, [5]*4)], [5])
#             self.assert_partitioned(d[np.argpartition(d, [5]*4 + [6, 13])],
#                                     [5]*4 + [6, 13])
# 
#             d = np.arange(12)
#             np.random.shuffle(d)
#             d1 = np.tile(np.arange(12), (4, 1))
#             map(np.random.shuffle, d1)
#             d0 = np.transpose(d1)
# 
#             kth = (1, 6, 7, -1)
#             p = np.partition(d1, kth, axis=1)
#             pa = d1[np.arange(d1.shape[0])[:, None],
#                     d1.argpartition(kth, axis=1)]
#             self.assertEqual(p, pa)
#             for i in range(d1.shape[0]):
#                 self.assert_partitioned(p[i,:], kth)
#             p = np.partition(d0, kth, axis=0)
#             pa = d0[np.argpartition(d0, kth, axis=0),
#                     np.arange(d0.shape[1])[None,:]]
#             self.assertEqual(p, pa)
#             for i in range(d0.shape[1]):
#                 self.assert_partitioned(p[:, i], kth)
# 
#     def test_partition_cdtype(self):
#         d = np.array([('Galahad', 1.7, 38), ('Arthur', 1.8, 41),
#                    ('Lancelot', 1.9, 38)],
#                   dtype=[('name', '|S10'), ('height', '<f8'), ('age', '<i4')])
# 
#         tgt = np.sort(d, order=['age', 'height'])
#         self.assertEqual(np.partition(d, range(d.size),
#                                         order=['age', 'height']),
#                            tgt)
#         self.assertEqual(d[np.argpartition(d, range(d.size),
#                                              order=['age', 'height'])],
#                            tgt)
#         for k in range(d.size):
#             self.assertEqual(np.partition(d, k, order=['age', 'height'])[k],
#                         tgt[k])
#             self.assertEqual(d[np.argpartition(d, k, order=['age', 'height'])][k],
#                          tgt[k])
# 
#         d = np.array(['Galahad', 'Arthur', 'zebra', 'Lancelot'])
#         tgt = np.sort(d)
#         self.assertEqual(np.partition(d, range(d.size)), tgt)
#         for k in range(d.size):
#             self.assertEqual(np.partition(d, k)[k], tgt[k])
#             self.assertEqual(d[np.argpartition(d, k)][k], tgt[k])
# 
#     def test_partition_unicode_kind(self):
#         d = np.arange(10)
#         k = b'\xc3\xa4'.decode("UTF8")
#         self.assertRaises(ValueError, d.partition, 2, kind=k)
#         self.assertRaises(ValueError, d.argpartition, 2, kind=k)
# 
#     def test_partition_fuzz(self):
#         # a few rounds of random data testing
#         for j in range(10, 30):
#             for i in range(1, j - 2):
#                 d = np.arange(j)
#                 np.random.shuffle(d)
#                 d = d % np.random.randint(2, 30)
#                 idx = np.random.randint(d.size)
#                 kth = [0, idx, i, i + 1]
#                 tgt = np.sort(d)[kth]
#                 self.assertEqual(np.partition(d, kth)[kth], tgt,
#                                    err_msg="data: %r\n kth: %r" % (d, kth))
# 
#     def test_argpartition_gh5524(self):
#         #  A test for functionality of argpartition on lists.
#         d = [6,7,3,2,9,0]
#         p = np.argpartition(d,1)
#         self.assert_partitioned(np.array(d)[p],[1])
# 
#     def test_flatten(self):
#         x0 = np.array([[1, 2, 3], [4, 5, 6]], np.int32)
#         x1 = np.array([[[1, 2], [3, 4]], [[5, 6], [7, 8]]], np.int32)
#         y0 = np.array([1, 2, 3, 4, 5, 6], np.int32)
#         y0f = np.array([1, 4, 2, 5, 3, 6], np.int32)
#         y1 = np.array([1, 2, 3, 4, 5, 6, 7, 8], np.int32)
#         y1f = np.array([1, 5, 3, 7, 2, 6, 4, 8], np.int32)
#         self.assertEqual(x0.flatten(), y0)
#         self.assertEqual(x0.flatten('F'), y0f)
#         self.assertEqual(x0.flatten('F'), x0.T.flatten())
#         self.assertEqual(x1.flatten(), y1)
#         self.assertEqual(x1.flatten('F'), y1f)
#         self.assertEqual(x1.flatten('F'), x1.T.flatten())
# 
# 
#     @pytest.mark.parametrize('func', (np.dot, np.matmul))
#     def test_arr_mult(self, func):
#         a = np.array([[1, 0], [0, 1]])
#         b = np.array([[0, 1], [1, 0]])
#         c = np.array([[9, 1], [1, -9]])
#         d = np.arange(24).reshape(4, 6)
#         ddt = np.array(
#             [[  55,  145,  235,  325],
#              [ 145,  451,  757, 1063],
#              [ 235,  757, 1279, 1801],
#              [ 325, 1063, 1801, 2539]]
#         )
#         dtd = np.array(
#             [[504, 540, 576, 612, 648, 684],
#              [540, 580, 620, 660, 700, 740],
#              [576, 620, 664, 708, 752, 796],
#              [612, 660, 708, 756, 804, 852],
#              [648, 700, 752, 804, 856, 908],
#              [684, 740, 796, 852, 908, 964]]
#         )
# 
# 
#         # gemm vs syrk optimizations
#         for et in [np.float32, np.float64, np.complex64, np.complex128]:
#             eaf = a.astype(et)
#             self.assertEqual(func(eaf, eaf), eaf)
#             self.assertEqual(func(eaf.T, eaf), eaf)
#             self.assertEqual(func(eaf, eaf.T), eaf)
#             self.assertEqual(func(eaf.T, eaf.T), eaf)
#             self.assertEqual(func(eaf.T.copy(), eaf), eaf)
#             self.assertEqual(func(eaf, eaf.T.copy()), eaf)
#             self.assertEqual(func(eaf.T.copy(), eaf.T.copy()), eaf)
# 
#         # syrk validations
#         for et in [np.float32, np.float64, np.complex64, np.complex128]:
#             eaf = a.astype(et)
#             ebf = b.astype(et)
#             self.assertEqual(func(ebf, ebf), eaf)
#             self.assertEqual(func(ebf.T, ebf), eaf)
#             self.assertEqual(func(ebf, ebf.T), eaf)
#             self.assertEqual(func(ebf.T, ebf.T), eaf)
# 
#         # syrk - different shape, stride, and view validations
#         for et in [np.float32, np.float64, np.complex64, np.complex128]:
#             edf = d.astype(et)
#             self.assertEqual(
#                 func(edf[::-1, :], edf.T),
#                 func(edf[::-1, :].copy(), edf.T.copy())
#             )
#             self.assertEqual(
#                 func(edf[:, ::-1], edf.T),
#                 func(edf[:, ::-1].copy(), edf.T.copy())
#             )
#             self.assertEqual(
#                 func(edf, edf[::-1, :].T),
#                 func(edf, edf[::-1, :].T.copy())
#             )
#             self.assertEqual(
#                 func(edf, edf[:, ::-1].T),
#                 func(edf, edf[:, ::-1].T.copy())
#             )
#             self.assertEqual(
#                 func(edf[:edf.shape[0] // 2, :], edf[::2, :].T),
#                 func(edf[:edf.shape[0] // 2, :].copy(), edf[::2, :].T.copy())
#             )
#             self.assertEqual(
#                 func(edf[::2, :], edf[:edf.shape[0] // 2, :].T),
#                 func(edf[::2, :].copy(), edf[:edf.shape[0] // 2, :].T.copy())
#             )
# 
#         # syrk - different shape
#         for et in [np.float32, np.float64, np.complex64, np.complex128]:
#             edf = d.astype(et)
#             eddtf = ddt.astype(et)
#             edtdf = dtd.astype(et)
#             self.assertEqual(func(edf, edf.T), eddtf)
#             self.assertEqual(func(edf.T, edf), edtdf)
# 
#     @pytest.mark.parametrize('func', (np.dot, np.matmul))
#     @pytest.mark.parametrize('dtype', 'ifdFD')
#     def test_no_dgemv(self, func, dtype):
#         # check vector arg for contiguous before gemv
#         # gh-12156
#         a = np.arange(8.0, dtype=dtype).reshape(2, 4)
#         b = np.broadcast_to(1., (4, 1))
#         ret1 = func(a, b)
#         ret2 = func(a, b.copy())
#         self.assertEqual(ret1, ret2)
# 
#         ret1 = func(b.T, a.T)
#         ret2 = func(b.T.copy(), a.T)
#         self.assertEqual(ret1, ret2)
# 
#         # check for unaligned data
#         dt = np.dtype(dtype)
#         a = np.zeros(8 * dt.itemsize // 2 + 1, dtype='int16')[1:].view(dtype)
#         a = a.reshape(2, 4)
#         b = a[0]
#         # make sure it is not aligned
#         assert_(a.__array_interface__['data'][0] % dt.itemsize != 0)
#         ret1 = func(a, b)
#         ret2 = func(a.copy(), b.copy())
#         self.assertEqual(ret1, ret2)
# 
#         ret1 = func(b.T, a.T)
#         ret2 = func(b.T.copy(), a.T.copy())
#         self.assertEqual(ret1, ret2)
# 
#     def test_dot(self):
#         a = np.array([[1, 0], [0, 1]])
#         b = np.array([[0, 1], [1, 0]])
#         c = np.array([[9, 1], [1, -9]])
#         # function versus methods
#         self.assertEqual(np.dot(a, b), a.dot(b))
#         self.assertEqual(np.dot(np.dot(a, b), c), a.dot(b).dot(c))
# 
#         # test passing in an output array
#         c = np.zeros_like(a)
#         a.dot(b, c)
#         self.assertEqual(c, np.dot(a, b))
# 
#         # test keyword args
#         c = np.zeros_like(a)
#         a.dot(b=b, out=c)
#         self.assertEqual(c, np.dot(a, b))
# 
#     def test_dot_type_mismatch(self):
#         c = 1.
#         A = np.array((1,1), dtype='i,i')
# 
#         self.assertRaises(TypeError, np.dot, c, A)
#         self.assertRaises(TypeError, np.dot, A, c)
# 
#     def test_dot_out_mem_overlap(self):
#         np.random.seed(1)
# 
#         # Test BLAS and non-BLAS code paths, including all dtypes
#         # that dot() supports
#         dtypes = [np.dtype(code) for code in np.typecodes['All']
#                   if code not in 'USVM']
#         for dtype in dtypes:
#             a = np.random.rand(3, 3).astype(dtype)
# 
#             # Valid dot() output arrays must be aligned
#             b = _aligned_zeros((3, 3), dtype=dtype)
#             b[...] = np.random.rand(3, 3)
# 
#             y = np.dot(a, b)
#             x = np.dot(a, b, out=b)
#             self.assertEqual(x, y, err_msg=repr(dtype))
# 
#             # Check invalid output array
#             self.assertRaises(ValueError, np.dot, a, b, out=b[::2])
#             self.assertRaises(ValueError, np.dot, a, b, out=b.T)
# 
#     def test_dot_matmul_out(self):
#         # gh-9641
#         class Sub(np.ndarray):
#             pass
#         a = np.ones((2, 2)).view(Sub)
#         b = np.ones((2, 2)).view(Sub)
#         out = np.ones((2, 2))
# 
#         # make sure out can be any ndarray (not only subclass of inputs)
#         np.dot(a, b, out=out)
#         np.matmul(a, b, out=out)
# 
#     def test_dot_matmul_inner_array_casting_fails(self):
# 
#         class A:
#             def __array__(self, *args, **kwargs):
#                 raise NotImplementedError
# 
#         # Don't override the error from calling __array__()
#         self.assertRaises(NotImplementedError, np.dot, A(), A())
#         self.assertRaises(NotImplementedError, np.matmul, A(), A())
#         self.assertRaises(NotImplementedError, np.inner, A(), A())
# 
#     def test_matmul_out(self):
#         # overlapping memory
#         a = np.arange(18).reshape(2, 3, 3)
#         b = np.matmul(a, a)
#         c = np.matmul(a, a, out=a)
#         assert_(c is a)
#         self.assertEqual(c, b)
#         a = np.arange(18).reshape(2, 3, 3)
#         c = np.matmul(a, a, out=a[::-1, ...])
#         assert_(c.base is a.base)
#         self.assertEqual(c, b)
# 
#     def test_diagonal(self):
#         a = np.arange(12).reshape((3, 4))
#         self.assertEqual(a.diagonal(), [0, 5, 10])
#         self.assertEqual(a.diagonal(0), [0, 5, 10])
#         self.assertEqual(a.diagonal(1), [1, 6, 11])
#         self.assertEqual(a.diagonal(-1), [4, 9])
#         self.assertRaises(np.AxisError, a.diagonal, axis1=0, axis2=5)
#         self.assertRaises(np.AxisError, a.diagonal, axis1=5, axis2=0)
#         self.assertRaises(np.AxisError, a.diagonal, axis1=5, axis2=5)
#         self.assertRaises(ValueError, a.diagonal, axis1=1, axis2=1)
# 
#         b = np.arange(8).reshape((2, 2, 2))
#         self.assertEqual(b.diagonal(), [[0, 6], [1, 7]])
#         self.assertEqual(b.diagonal(0), [[0, 6], [1, 7]])
#         self.assertEqual(b.diagonal(1), [[2], [3]])
#         self.assertEqual(b.diagonal(-1), [[4], [5]])
#         self.assertRaises(ValueError, b.diagonal, axis1=0, axis2=0)
#         self.assertEqual(b.diagonal(0, 1, 2), [[0, 3], [4, 7]])
#         self.assertEqual(b.diagonal(0, 0, 1), [[0, 6], [1, 7]])
#         self.assertEqual(b.diagonal(offset=1, axis1=0, axis2=2), [[1], [3]])
#         # Order of axis argument doesn't matter:
#         self.assertEqual(b.diagonal(0, 2, 1), [[0, 3], [4, 7]])
# 
#     def test_diagonal_view_notwriteable(self):
#         a = np.eye(3).diagonal()
#         assert_(not a.flags.writeable)
#         assert_(not a.flags.owndata)
# 
#         a = np.diagonal(np.eye(3))
#         assert_(not a.flags.writeable)
#         assert_(not a.flags.owndata)
# 
#         a = np.diag(np.eye(3))
#         assert_(not a.flags.writeable)
#         assert_(not a.flags.owndata)
# 
#     def test_diagonal_memleak(self):
#         # Regression test for a bug that crept in at one point
#         a = np.zeros((100, 100))
#         if HAS_REFCOUNT:
#             assert_(sys.getrefcount(a) < 50)
#         for i in range(100):
#             a.diagonal()
#         if HAS_REFCOUNT:
#             assert_(sys.getrefcount(a) < 50)
# 
#     def test_size_zero_memleak(self):
#         # Regression test for issue 9615
#         # Exercises a special-case code path for dot products of length
#         # zero in cblasfuncs (making it is specific to floating dtypes).
#         a = np.array([], dtype=np.float64)
#         x = np.array(2.0)
#         for _ in range(100):
#             np.dot(a, a, out=x)
#         if HAS_REFCOUNT:
#             assert_(sys.getrefcount(x) < 50)
# 
#     def test_trace(self):
#         a = np.arange(12).reshape((3, 4))
#         self.assertEqual(a.trace(), 15)
#         self.assertEqual(a.trace(0), 15)
#         self.assertEqual(a.trace(1), 18)
#         self.assertEqual(a.trace(-1), 13)
# 
#         b = np.arange(8).reshape((2, 2, 2))
#         self.assertEqual(b.trace(), [6, 8])
#         self.assertEqual(b.trace(0), [6, 8])
#         self.assertEqual(b.trace(1), [2, 3])
#         self.assertEqual(b.trace(-1), [4, 5])
#         self.assertEqual(b.trace(0, 0, 1), [6, 8])
#         self.assertEqual(b.trace(0, 0, 2), [5, 9])
#         self.assertEqual(b.trace(0, 1, 2), [3, 11])
#         self.assertEqual(b.trace(offset=1, axis1=0, axis2=2), [1, 3])
# 
#     def test_trace_subclass(self):
#         # The class would need to overwrite trace to ensure single-element
#         # output also has the right subclass.
#         class MyArray(np.ndarray):
#             pass
# 
#         b = np.arange(8).reshape((2, 2, 2)).view(MyArray)
#         t = b.trace()
#         assert_(isinstance(t, MyArray))
# 
#     def test_put(self):
#         icodes = np.typecodes['AllInteger']
#         fcodes = np.typecodes['AllFloat']
#         for dt in icodes + fcodes + 'O':
#             tgt = np.array([0, 1, 0, 3, 0, 5], dtype=dt)
# 
#             # test 1-d
#             a = np.zeros(6, dtype=dt)
#             a.put([1, 3, 5], [1, 3, 5])
#             self.assertEqual(a, tgt)
# 
#             # test 2-d
#             a = np.zeros((2, 3), dtype=dt)
#             a.put([1, 3, 5], [1, 3, 5])
#             self.assertEqual(a, tgt.reshape(2, 3))
# 
#         for dt in '?':
#             tgt = np.array([False, True, False, True, False, True], dtype=dt)
# 
#             # test 1-d
#             a = np.zeros(6, dtype=dt)
#             a.put([1, 3, 5], [True]*3)
#             self.assertEqual(a, tgt)
# 
#             # test 2-d
#             a = np.zeros((2, 3), dtype=dt)
#             a.put([1, 3, 5], [True]*3)
#             self.assertEqual(a, tgt.reshape(2, 3))
# 
#         # check must be writeable
#         a = np.zeros(6)
#         a.flags.writeable = False
#         self.assertRaises(ValueError, a.put, [1, 3, 5], [1, 3, 5])
# 
#         # when calling np.put, make sure a
#         # TypeError is raised if the object
#         # isn't an ndarray
#         bad_array = [1, 2, 3]
#         self.assertRaises(TypeError, np.put, bad_array, [0, 2], 5)
# 
#     def test_ravel(self):
#         a = np.array([[0, 1], [2, 3]])
#         self.assertEqual(a.ravel(), [0, 1, 2, 3])
#         assert_(not a.ravel().flags.owndata)
#         self.assertEqual(a.ravel('F'), [0, 2, 1, 3])
#         self.assertEqual(a.ravel(order='C'), [0, 1, 2, 3])
#         self.assertEqual(a.ravel(order='F'), [0, 2, 1, 3])
#         self.assertEqual(a.ravel(order='A'), [0, 1, 2, 3])
#         assert_(not a.ravel(order='A').flags.owndata)
#         self.assertEqual(a.ravel(order='K'), [0, 1, 2, 3])
#         assert_(not a.ravel(order='K').flags.owndata)
#         self.assertEqual(a.ravel(), a.reshape(-1))
# 
#         a = np.array([[0, 1], [2, 3]], order='F')
#         self.assertEqual(a.ravel(), [0, 1, 2, 3])
#         self.assertEqual(a.ravel(order='A'), [0, 2, 1, 3])
#         self.assertEqual(a.ravel(order='K'), [0, 2, 1, 3])
#         assert_(not a.ravel(order='A').flags.owndata)
#         assert_(not a.ravel(order='K').flags.owndata)
#         self.assertEqual(a.ravel(), a.reshape(-1))
#         self.assertEqual(a.ravel(order='A'), a.reshape(-1, order='A'))
# 
#         a = np.array([[0, 1], [2, 3]])[::-1, :]
#         self.assertEqual(a.ravel(), [2, 3, 0, 1])
#         self.assertEqual(a.ravel(order='C'), [2, 3, 0, 1])
#         self.assertEqual(a.ravel(order='F'), [2, 0, 3, 1])
#         self.assertEqual(a.ravel(order='A'), [2, 3, 0, 1])
#         # 'K' doesn't reverse the axes of negative strides
#         self.assertEqual(a.ravel(order='K'), [2, 3, 0, 1])
#         assert_(a.ravel(order='K').flags.owndata)
# 
#         # Test simple 1-d copy behaviour:
#         a = np.arange(10)[::2]
#         assert_(a.ravel('K').flags.owndata)
#         assert_(a.ravel('C').flags.owndata)
#         assert_(a.ravel('F').flags.owndata)
# 
#         # Not contiguous and 1-sized axis with non matching stride
#         a = np.arange(2**3 * 2)[::2]
#         a = a.reshape(2, 1, 2, 2).swapaxes(-1, -2)
#         strides = list(a.strides)
#         strides[1] = 123
#         a.strides = strides
#         assert_(a.ravel(order='K').flags.owndata)
#         self.assertEqual(a.ravel('K'), np.arange(0, 15, 2))
# 
#         # contiguous and 1-sized axis with non matching stride works:
#         a = np.arange(2**3)
#         a = a.reshape(2, 1, 2, 2).swapaxes(-1, -2)
#         strides = list(a.strides)
#         strides[1] = 123
#         a.strides = strides
#         assert_(np.may_share_memory(a.ravel(order='K'), a))
#         self.assertEqual(a.ravel(order='K'), np.arange(2**3))
# 
#         # Test negative strides (not very interesting since non-contiguous):
#         a = np.arange(4)[::-1].reshape(2, 2)
#         assert_(a.ravel(order='C').flags.owndata)
#         assert_(a.ravel(order='K').flags.owndata)
#         self.assertEqual(a.ravel('C'), [3, 2, 1, 0])
#         self.assertEqual(a.ravel('K'), [3, 2, 1, 0])
# 
#         # 1-element tidy strides test (NPY_RELAXED_STRIDES_CHECKING):
#         a = np.array([[1]])
#         a.strides = (123, 432)
#         # If the stride is not 8, NPY_RELAXED_STRIDES_CHECKING is messing
#         # them up on purpose:
#         if np.ones(1).strides == (8,):
#             assert_(np.may_share_memory(a.ravel('K'), a))
#             self.assertEqual(a.ravel('K').strides, (a.dtype.itemsize,))
# 
#         for order in ('C', 'F', 'A', 'K'):
#             # 0-d corner case:
#             a = np.array(0)
#             self.assertEqual(a.ravel(order), [0])
#             assert_(np.may_share_memory(a.ravel(order), a))
# 
#         # Test that certain non-inplace ravels work right (mostly) for 'K':
#         b = np.arange(2**4 * 2)[::2].reshape(2, 2, 2, 2)
#         a = b[..., ::2]
#         self.assertEqual(a.ravel('K'), [0, 4, 8, 12, 16, 20, 24, 28])
#         self.assertEqual(a.ravel('C'), [0, 4, 8, 12, 16, 20, 24, 28])
#         self.assertEqual(a.ravel('A'), [0, 4, 8, 12, 16, 20, 24, 28])
#         self.assertEqual(a.ravel('F'), [0, 16, 8, 24, 4, 20, 12, 28])
# 
#         a = b[::2, ...]
#         self.assertEqual(a.ravel('K'), [0, 2, 4, 6, 8, 10, 12, 14])
#         self.assertEqual(a.ravel('C'), [0, 2, 4, 6, 8, 10, 12, 14])
#         self.assertEqual(a.ravel('A'), [0, 2, 4, 6, 8, 10, 12, 14])
#         self.assertEqual(a.ravel('F'), [0, 8, 4, 12, 2, 10, 6, 14])
# 
#     def test_ravel_subclass(self):
#         class ArraySubclass(np.ndarray):
#             pass
# 
#         a = np.arange(10).view(ArraySubclass)
#         assert_(isinstance(a.ravel('C'), ArraySubclass))
#         assert_(isinstance(a.ravel('F'), ArraySubclass))
#         assert_(isinstance(a.ravel('A'), ArraySubclass))
#         assert_(isinstance(a.ravel('K'), ArraySubclass))
# 
#         a = np.arange(10)[::2].view(ArraySubclass)
#         assert_(isinstance(a.ravel('C'), ArraySubclass))
#         assert_(isinstance(a.ravel('F'), ArraySubclass))
#         assert_(isinstance(a.ravel('A'), ArraySubclass))
#         assert_(isinstance(a.ravel('K'), ArraySubclass))
# 
#     def test_swapaxes(self):
#         a = np.arange(1*2*3*4).reshape(1, 2, 3, 4).copy()
#         idx = np.indices(a.shape)
#         assert_(a.flags['OWNDATA'])
#         b = a.copy()
#         # check exceptions
#         self.assertRaises(np.AxisError, a.swapaxes, -5, 0)
#         self.assertRaises(np.AxisError, a.swapaxes, 4, 0)
#         self.assertRaises(np.AxisError, a.swapaxes, 0, -5)
#         self.assertRaises(np.AxisError, a.swapaxes, 0, 4)
# 
#         for i in range(-4, 4):
#             for j in range(-4, 4):
#                 for k, src in enumerate((a, b)):
#                     c = src.swapaxes(i, j)
#                     # check shape
#                     shape = list(src.shape)
#                     shape[i] = src.shape[j]
#                     shape[j] = src.shape[i]
#                     self.assertEqual(c.shape, shape, str((i, j, k)))
#                     # check array contents
#                     i0, i1, i2, i3 = [dim-1 for dim in c.shape]
#                     j0, j1, j2, j3 = [dim-1 for dim in src.shape]
#                     self.assertEqual(src[idx[j0], idx[j1], idx[j2], idx[j3]],
#                                  c[idx[i0], idx[i1], idx[i2], idx[i3]],
#                                  str((i, j, k)))
#                     # check a view is always returned, gh-5260
#                     assert_(not c.flags['OWNDATA'], str((i, j, k)))
#                     # check on non-contiguous input array
#                     if k == 1:
#                         b = c
# 
#     def test_conjugate(self):
#         a = np.array([1-1j, 1+1j, 23+23.0j])
#         ac = a.conj()
#         self.assertEqual(a.real, ac.real)
#         self.assertEqual(a.imag, -ac.imag)
#         self.assertEqual(ac, a.conjugate())
#         self.assertEqual(ac, np.conjugate(a))
# 
#         a = np.array([1-1j, 1+1j, 23+23.0j], 'F')
#         ac = a.conj()
#         self.assertEqual(a.real, ac.real)
#         self.assertEqual(a.imag, -ac.imag)
#         self.assertEqual(ac, a.conjugate())
#         self.assertEqual(ac, np.conjugate(a))
# 
#         a = np.array([1, 2, 3])
#         ac = a.conj()
#         self.assertEqual(a, ac)
#         self.assertEqual(ac, a.conjugate())
#         self.assertEqual(ac, np.conjugate(a))
# 
#         a = np.array([1.0, 2.0, 3.0])
#         ac = a.conj()
#         self.assertEqual(a, ac)
#         self.assertEqual(ac, a.conjugate())
#         self.assertEqual(ac, np.conjugate(a))
# 
#         a = np.array([1-1j, 1+1j, 1, 2.0], object)
#         ac = a.conj()
#         self.assertEqual(ac, [k.conjugate() for k in a])
#         self.assertEqual(ac, a.conjugate())
#         self.assertEqual(ac, np.conjugate(a))
# 
#         a = np.array([1-1j, 1, 2.0, 'f'], object)
#         self.assertRaises(TypeError, lambda: a.conj())
#         self.assertRaises(TypeError, lambda: a.conjugate())
# 
#     def test__complex__(self):
#         dtypes = ['i1', 'i2', 'i4', 'i8',
#                   'u1', 'u2', 'u4', 'u8',
#                   'f', 'd', 'g', 'F', 'D', 'G',
#                   '?', 'O']
#         for dt in dtypes:
#             a = np.array(7, dtype=dt)
#             b = np.array([7], dtype=dt)
#             c = np.array([[[[[7]]]]], dtype=dt)
# 
#             msg = 'dtype: {0}'.format(dt)
#             ap = complex(a)
#             self.assertEqual(ap, a, msg)
#             bp = complex(b)
#             self.assertEqual(bp, b, msg)
#             cp = complex(c)
#             self.assertEqual(cp, c, msg)
# 
#     def test__complex__should_not_work(self):
#         dtypes = ['i1', 'i2', 'i4', 'i8',
#                   'u1', 'u2', 'u4', 'u8',
#                   'f', 'd', 'g', 'F', 'D', 'G',
#                   '?', 'O']
#         for dt in dtypes:
#             a = np.array([1, 2, 3], dtype=dt)
#             self.assertRaises(TypeError, complex, a)
# 
#         dt = np.dtype([('a', 'f8'), ('b', 'i1')])
#         b = np.array((1.0, 3), dtype=dt)
#         self.assertRaises(TypeError, complex, b)
# 
#         c = np.array([(1.0, 3), (2e-3, 7)], dtype=dt)
#         self.assertRaises(TypeError, complex, c)
# 
#         d = np.array('1+1j')
#         self.assertRaises(TypeError, complex, d)
# 
#         e = np.array(['1+1j'], 'U')
#         self.assertRaises(TypeError, complex, e)
        
def getSuite():
    return unittest.TestSuite([\
            unittest.TestLoader().loadTestsFromTestCase(TestAttributes),\
            unittest.TestLoader().loadTestsFromTestCase(TestArrayConstruction),\
            unittest.TestLoader().loadTestsFromTestCase(TestAssignment),\
#             unittest.TestLoader().loadTestsFromTestCase(TestScalarIndexing),\
            unittest.TestLoader().loadTestsFromTestCase(TestCreation),\
            unittest.TestLoader().loadTestsFromTestCase(TestMethods),\
            ])

def run_test():
#     suite = unittest.TestLoader().loadTestsFromTestCase(TestCreation)
    suite = getSuite()
    unittest.TextTestRunner(verbosity=2).run(suite)
    
def run():
    run_test()