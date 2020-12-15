import unittest
import numpy as np
import sys

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
#         assert_(self.three.dtype.str[0] in '<>')
#         self.assertEqual(self.one.dtype.str[1], 'i')
#         self.assertEqual(self.three.dtype.str[1], 'f')

#     def test_int_subclassing(self):
#         # Regression test for https://github.com/numpy/numpy/pull/3526
# 
#         numpy_int = np.int_(0)
# 
#         # int_ doesn't inherit from Python int, because it's not fixed-width
#         assert_(not isinstance(numpy_int, int))

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
#         assert_(np.ascontiguousarray(d).flags.c_contiguous)
#         assert_(np.ascontiguousarray(d).flags.f_contiguous)
#         assert_(np.asfortranarray(d).flags.c_contiguous)
#         assert_(np.asfortranarray(d).flags.f_contiguous)
#         d = np.ones((10, 10))[::2,::2]
#         assert_(np.ascontiguousarray(d).flags.c_contiguous)
#         assert_(np.asfortranarray(d).flags.f_contiguous)


def getSuite():
    return unittest.TestSuite([\
            unittest.TestLoader().loadTestsFromTestCase(TestAttributes),\
            unittest.TestLoader().loadTestsFromTestCase(TestArrayConstruction),\
            ])

def run_test():
#     suite = unittest.TestLoader().loadTestsFromTestCase(TestCreation)
    suite = getSuite()
    unittest.TextTestRunner(verbosity=2).run(suite)
    
def run():
    run_test()