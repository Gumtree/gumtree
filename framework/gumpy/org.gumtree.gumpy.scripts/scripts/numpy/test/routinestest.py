# from gumpy.commons import jutils
from unittest import TestCase
import numpy as np
import sys
import tempfile
import unittest

array = np.array

class TestCreation(TestCase):

    def setUp(self):
        pass

    def test_empty(self):
        e = np.empty([2, 2])
        z = np.zeros([2, 2])
        self.assertEqual(e, z, 'test empty with zeros')

    def test_empty_like(self):
        a = ([1,2,3], [4,5,6])
        emp = np.empty_like(a)
        zer = np.zeros([2,3])
        self.assertEqual(emp, zer, 'test empty like with zeros')

    def test_eye(self):
        e = np.eye(2, dtype=int)
        res = array([[1, 0],
                     [0, 1]])
        self.assertTrue(np.array_equal(e, res), 'eye 2x2 value match')
        self.assertEqual(e.dtype, int, 'eye int type match')
        e = np.eye(3, k=1)
        res = array([[0.,  1.,  0.],
                     [0.,  0.,  1.],
                     [0.,  0.,  0.]])
        self.assertTrue(np.array_equal(e, res), 'eye 3x3 value match')
        self.assertEqual(e.dtype, float, 'eye float type match')
        
    def test_identity(self):
        i = np.identity(3)
        res = array([[1.,  0.,  0.],
                     [0.,  1.,  0.],
                     [0.,  0.,  1.]])
        self.assertTrue(np.array_equal(i, res), 'identity value match')
        
    def test_full(self):
        f = np.full((2, 2), np.inf)
        res = array([[np.inf, np.inf],
                     [np.inf, np.inf]])
        self.assertTrue(np.array_equal(f, res), 'full inf value match')
        f = np.full((2, 2), [1, 2])
        res = array([[1, 2],
                     [1, 2]])
        self.assertTrue(np.array_equal(f, res), 'full array value match')
        
    def test_full_like(self):
        x = np.arange(6, int)
        y = np.full_like(x, 1)
        res = array([1, 1, 1, 1, 1, 1])
        self.assertTrue(np.array_equal(y, res), 'full like value match')

    def test_vander(self):
        x = np.array([1, 2, 3, 5])
        v1 = np.vander(x)
        o1 = array([[  1,   1,   1,   1],
                   [  8,   4,   2,   1],
                   [ 27,   9,   3,   1],
                   [125,  25,   5,   1]])
        self.assertTrue(np.array_equal(v1, o1), 'vander increase = False')
        v2 = np.vander(x, increasing=True)
        o2 = array([[  1,   1,   1,   1],
                   [  1,   2,   4,   8],
                   [  1,   3,   9,  27],
                   [  1,   5,  25, 125]])
        self.assertTrue(np.array_equal(v2, o2), 'vander increase = True')
        
    def test_asfarray(self):
        a = np.asfarray([2, 3])
        self.assertEqual(a.dtype, float, 'asfarray type is float')
        a = np.asarray([2, 3])
        b = np.asfarray(a)
        self.assertEqual(b.dtype, float, 'asfarray from int original, test type is float')
        
    def test_asarray_chkfinite(self):
        a = [1, 2, np.inf]
        self.assertRaises(ValueError, np.asarray_chkfinite, a)
        a = [1, 2]
        b = np.asarray_chkfinite(a, dtype=float)
        b[0] = np.nan
        self.assertRaises(ValueError, np.asarray_chkfinite, b)
        
    def test_diag(self):
        x = np.arange(9).reshape((3,3))
        y = np.diag(x)
        res = array([0, 4, 8])
        self.assertTrue(np.array_equal(y, res), 'diag 1 value match')

        y = np.diag(x, k=1)
        res = array([1, 5])
        self.assertTrue(np.array_equal(y, res), 'diag 2 value match')
         
        y = np.diag(x, k=-1)
        res = array([3, 7])
        self.assertTrue(np.array_equal(y, res), 'diag 3 value match')
         
        y = np.diag(np.diag(x))
        res = array([[0, 0, 0],
                     [0, 4, 0],
                     [0, 0, 8]])
        self.assertTrue(np.array_equal(y, res), 'diag 4 value match')
        
    def test_diag_flat(self):
        x = np.diagflat([[1,2], [3,4]])
        res = array([[1, 0, 0, 0],
                     [0, 2, 0, 0],
                     [0, 0, 3, 0],
                     [0, 0, 0, 4]])
        self.assertTrue(np.array_equal(x, res), 'diag_flat 1 value match')

        x = np.diagflat([1,2], 1)
        res = array([[0, 1, 0],
                     [0, 0, 2],
                     [0, 0, 0]])
        self.assertTrue(np.array_equal(x, res), 'diag_flat 2 value match')
        
    def test_diagonal(self):
        a = np.arange(4).reshape(2,2)
        b = a.diagonal()
        res = array([0, 3])
        self.assertTrue(np.array_equal(b, res), 'diagonal 1 value match')
        
        b = a.diagonal(1)
        res = array([1])
        self.assertTrue(np.array_equal(b, res), 'diagonal 2 value match')

        a = np.arange(8).reshape(2,2,2)
        b = a.diagonal(0, 0, 1)
        res = array([[0, 6],
                     [1, 7]])
        self.assertTrue(np.array_equal(b, res), 'diagonal 3 value match')
        
        a = np.arange(9).reshape(3, 3)
        b = np.fliplr(a).diagonal()  # Horizontal flip
        res = array([2, 4, 6])
        self.assertTrue(np.array_equal(b, res), 'diagonal 4 value match')

        b = np.flipud(a).diagonal()  # Vertical flip
        res = array([6, 4, 2])
        self.assertTrue(np.array_equal(b, res), 'diagonal 5 value match')
        
    def test_creation(self):
        a = np.array([[1, 2], [3, 4], [5, 6]])
        b = np.compress([0, 1], a, axis=0)
        res = array([[3, 4]])
        self.assertTrue(np.array_equal(b, res), 'compress 1 value match')

        b = np.compress([False, True, True], a, axis=0)
        res = array([[3, 4],
                     [5, 6]])
        self.assertTrue(np.array_equal(b, res), 'compress 2 value match')

        b = np.compress([False, True], a, axis=1)
        res = array([[2],
                     [4],
                     [6]])
        self.assertTrue(np.array_equal(b, res), 'compress 3 value match')

        b = np.compress([False, True], a)
        res = array([2])
        self.assertTrue(np.array_equal(b, res), 'compress 4 value match')

    def test_dumps_loads(self):
        a = np.random.rand(2, 3, 4)
        rr = a.dumps()
        b = np.loads(rr)
        self.assertTrue(np.allclose(a, b), 'dumps and loads single array\na={}\nb={}'.format(a, b))
        
    def test_repeat(self):
        r = np.repeat(3, 4)
        res = array([3, 3, 3, 3])
        self.assertTrue(np.array_equal(r, res), 'repeat 1')

        x = np.array([[1,2],[3,4]])
        r = np.repeat(x, 2)
        res = array([1, 1, 2, 2, 3, 3, 4, 4])
        self.assertTrue(np.array_equal(r, res), 'repeat 2')

        r = np.repeat(x, 3, axis=1)
        res = array([[1, 1, 1, 2, 2, 2],
                     [3, 3, 3, 4, 4, 4]])
        self.assertTrue(np.array_equal(r, res), 'repeat 3')

        r = np.repeat(x, [1, 2], axis=0)
        res = array([[1, 2],
                     [3, 4],
                     [3, 4]])
        self.assertTrue(np.array_equal(r, res), 'repeat 4')

    def test_take(self):
        a = [4, 3, 5, 7, 6, 8]
        indices = [0, 1, 4]
        t = np.take(a, indices)
        res = array([4, 3, 6])
        self.assertTrue(np.array_equal(t, res), 'math take 1')

        t = np.take(a, [[0, 1], [2, 3]])
        res = array([[4, 3],
                     [5, 7]])
        self.assertTrue(np.array_equal(t, res), 'math take 2 with axis 1')
        
class TestManipulation(TestCase):
    
    def test_copy(self):
        x = np.array([1, 2, 3])
        y = x
        z = np.copy(x)

        ''' Note that, when we modify x, y changes, but not z:'''
        x[0] = 10
        self.assertTrue(x[0] == y[0], 'copy 1, same object')
        self.assertFalse(x[0] == z[0], 'copy 2, different object')

        
    def test_copyto(self):
        a = np.arange(10)
        b = np.zeros(10)
        np.copyto(b, a)
        self.assertTrue(np.array_equal(a, b), 'test copy all')
        b = np.zeros(10)
        c = np.full(10, False, bool)
        c[5:] = True
        np.copyto(b, a, where = c)
        self.assertTrue(np.array_equal(a[5:], b[5:]), 'test partial copy')
        
    def test_shape(self):
        a = np.arange(24).reshape(2, 3, 4)
        self.assertEqual(np.shape(a), (2, 3, 4), 'shape match')
        
    def test_ravel(self):
        a = np.arange(10)
        b = a.reshape(2, 5)
        c = np.ravel(a)
        self.assertTrue(np.array_equal(a, c), 'ravel function')
        
    def test_moveaxis(self):
        a = np.arange(120).reshape(2,3,4,5)
        b = np.moveaxis(a, 0, 2)
        c = np.moveaxis(a, [0,2,1],[2,1,3])
        co= array([[[[  0,  20,  40], [ 60,  80, 100]],
                    [[  5,  25,  45], [ 65,  85, 105]],
                    [[ 10,  30,  50], [ 70,  90, 110]],
                    [[ 15,  35,  55], [ 75,  95, 115]]],
                   [[[  1,  21,  41], [ 61,  81, 101]],
                    [[  6,  26,  46], [ 66,  86, 106]],
                    [[ 11,  31,  51], [ 71,  91, 111]],
                    [[ 16,  36,  56], [ 76,  96, 116]]],
                   [[[  2,  22,  42], [ 62,  82, 102]],
                    [[  7,  27,  47], [ 67,  87, 107]],
                    [[ 12,  32,  52], [ 72,  92, 112]],
                    [[ 17,  37,  57], [ 77,  97, 117]]],
                   [[[  3,  23,  43], [ 63,  83, 103]],
                    [[  8,  28,  48], [ 68,  88, 108]],
                    [[ 13,  33,  53], [ 73,  93, 113]],
                    [[ 18,  38,  58], [ 78,  98, 118]]],
                   [[[  4,  24,  44], [ 64,  84, 104]],
                    [[  9,  29,  49], [ 69,  89, 109]],
                    [[ 14,  34,  54], [ 74,  94, 114]],
                    [[ 19,  39,  59], [ 79,  99, 119]]]])
        self.assertEqual(b.shape, (3,4,2,5), 'single dim change, shape match expected')
        self.assertEqual(c.shape, (5,4,2,3), 'multi dims change, shape match expected')
        self.assertTrue(np.array_equal(c, co), 'multi dims change, value match expected')
        
    def test_rollaxis(self):
        a = np.ones((3,4,5,6))
        r1 = np.rollaxis(a, 3, 1).shape
        r2 = np.rollaxis(a, 2).shape
        r3 = np.rollaxis(a, 1, 4).shape
        self.assertEqual(r1, (3, 6, 4, 5), 'rollaxis test 1 shape match')
        self.assertEqual(r2, (5, 3, 4, 6), 'rollaxis test 1 shape match')
        self.assertEqual(r3, (3, 5, 6, 4), 'rollaxis test 1 shape match')
        
    def test_swapaxes(self):
        x = np.array([[1,2,3]])
        y = np.swapaxes(x,0,1)
        res = array([[1],
                     [2],
                     [3]])
        self.assertEqual(y, res, 'swap axes 1')
        x = np.array([[[0,1],[2,3]],[[4,5],[6,7]]])
        y = np.swapaxes(x,0,2)
        res = array([[[0, 4],
                      [2, 6]],
                     [[1, 5],
                      [3, 7]]])
        self.assertEqual(y, res, 'swap axes 2')
        
    def test_transpose(self):
        x = np.arange(4).reshape((2,2))
        y = np.transpose(x)
        res = array([[0, 2],
                     [1, 3]])
        self.assertEqual(x, y, 'transpose value match')
        x = np.arange(6).reshape(1, 2, 3)
        y = np.transpose(x, (1, 0, 2))
        self.assertEqual(y.shape, (2, 1, 3), 'transpose 3D shape match')
        res = array([[[0, 1, 2]],
                     [[3, 4, 5]]])
        self.assertTrue(np.array_equal(y, res), 'transpose 3D value match')
        
    def test_atleast_1d(self):
        a = np.atleast_1d(1.0)
        res = array([1.])
        self.assertTrue(np.array_equal(a, res), 'atleast_1d scalar param')
        x = np.arange(9.0).reshape(3,3)
        a = np.atleast_1d(x)
        res = array([[0., 1., 2.],
                     [3., 4., 5.],
                     [6., 7., 8.]])
        self.assertTrue(np.array_equal(a, res), 'atleast_1d array param')
        a = np.atleast_1d(1, [3, 4])
        res = [array([1]), array([3, 4])]
        self.assertTrue(np.array_equal(a[0], res[0]), 'atleast_1d multiple params 1')
        self.assertTrue(np.array_equal(a[1], res[1]), 'atleast_1d multiple params 2')

    def test_atleast_2d(self):
        a = np.atleast_2d(3.0)
        self.assertEqual(a.ndim, 2, 'atleast_2d test ndim match')
        a = np.atleast_2d(1, [1, 2], [[1, 2]])
        res = [array([[1]]), array([[1, 2]]), array([[1, 2]])]
        self.assertTrue(np.array_equal(a[0], res[0]), '2d test 1')
        self.assertTrue(np.array_equal(a[1], res[1]), '2d test 2')
        self.assertTrue(np.array_equal(a[2], res[2]), '2d test 3')

    def test_atleast_3d(self):
        a = np.atleast_3d(3.0)
        self.assertEqual(a.ndim, 3, 'atleast_3d test ndim match')
        x = np.arange(12.0).reshape(4,3)
        a = np.atleast_3d(x).shape
        self.assertEqual(a, (4, 3, 1), 'atleast_3d shape match')
        a = np.atleast_3d([1, 2], [[1, 2]], [[[1, 2]]])
        res = [array([[[1], [2]]]), array([[[1], [2]]]), array([[[1, 2]]])]
        self.assertTrue(np.array_equal(a[0], res[0]), '3d test 1')
        self.assertTrue(np.array_equal(a[1], res[1]), '3d test 2')
        self.assertTrue(np.array_equal(a[2], res[2]), '3d test 3')

    def test_broadcast_to(self):
        x = np.array([1, 2, 3])
        a = np.broadcast_to(x, (3, 3))
        res = array([[1, 2, 3],
                     [1, 2, 3],
                     [1, 2, 3]])
        self.assertTrue(np.array_equal(a, res), 'broadcast_to value match')
        b = np.broadcast_to(x, (3, 3, 3))
        self.assertEqual(b.shape, (3,3,3), 'broadcast_to shape match')
    
    def test_broadcast_arrays(self):
        x = np.array([[1,2,3]])
        y = np.array([[4],[5]])
        a = np.broadcast_arrays(x, y)
        res = [array([[1, 2, 3],
                      [1, 2, 3]]), 
               array([[4, 4, 4],
                      [5, 5, 5]])]
        self.assertTrue(np.array_equal(a[0], res[0]), 'broadcast_arrays value 1 match')
        self.assertTrue(np.array_equal(a[1], res[1]), 'broadcast_arrays value 2 match')
        
    def test_expand_dims(self):
        x = np.array([1, 2])
        y = np.expand_dims(x, axis=0)
        res = array([[1, 2]])
        self.assertTrue(np.array_equal(y, res), 'expand_dims add axis at 0')
        y = np.expand_dims(x, axis=1)
        res = array([[1],
                     [2]])
        self.assertTrue(np.array_equal(y, res), 'expand_dims add axis at 1')
        y = np.expand_dims(x, axis=(0, 1))
        res = array([[[1, 2]]])
        self.assertTrue(np.array_equal(y, res), 'expand_dims add axis at (0, 1)')
        y = np.expand_dims(x, axis=(2, 0))
        res = array([[[1],
                      [2]]])
        self.assertTrue(np.array_equal(y, res), 'expand_dims add axis at (2, 0)')
        
    def test_concatenate(self):
        a = np.array([[1, 2], [3, 4]])
        b = np.array([[5, 6]])
        c = np.concatenate((a, b), axis=0)
        res = array([[1, 2],
                     [3, 4],
                     [5, 6]])
        self.assertTrue(np.array_equal(c, res), 'concatenate 2d value match')
        d = np.concatenate((a, b.T), axis=1)
        res = array([[1, 2, 5],
                     [3, 4, 6]])
        self.assertTrue(np.array_equal(d, res), 'concatenate 2d and T value match')
        e = np.concatenate((a, b), axis=None)
        res = array([1, 2, 3, 4, 5, 6])
        self.assertTrue(np.array_equal(e, res), 'concatenate flat value match')
        
    def test_vstack(self):
        a = np.array([1, 2, 3])
        b = np.array([2, 3, 4])
        c = np.vstack((a,b))
        res = array([[1, 2, 3],
                     [2, 3, 4]])
        self.assertTrue(np.array_equal(c, res), 'vstack 1 value match')
        a = np.array([[1], [2], [3]])
        b = np.array([[2], [3], [4]])
        c = np.vstack((a,b))
        res = array([[1], [2], [3], [2], [3], [4]])
        self.assertTrue(np.array_equal(c, res), 'vstack 2 value match')
        
    def test_hstack(self):
        a = np.array((1,2,3))
        b = np.array((2,3,4))
        c = np.hstack((a,b))
        res = array([1, 2, 3, 2, 3, 4])
        self.assertTrue(np.array_equal(c, res), 'hstack 1 value match')
        a = np.array([[1],[2],[3]])
        b = np.array([[2],[3],[4]])
        c = np.hstack((a,b))
        res = array([[1, 2],
                     [2, 3],
                     [3, 4]])
        self.assertTrue(np.array_equal(c, res), 'hstack 2 value match')
        
    def test_dstack(self):
        a = np.array((1,2,3))
        b = np.array((2,3,4))
        c = np.dstack((a,b))
        res = array([[[1, 2],
                      [2, 3],
                      [3, 4]]])
        self.assertTrue(np.array_equal(c, res), 'dstack 1 value match')
        a = np.array([[1],[2],[3]])
        b = np.array([[2],[3],[4]])
        c = np.dstack((a,b))
        res = array([[[1, 2]],
                     [[2, 3]],
                     [[3, 4]]])
        self.assertTrue(np.array_equal(c, res), 'dstack 2 value match')
        
    def test_stack(self):
        a = np.array([1, 2, 3])
        b = np.array([2, 3, 4])
        c = np.stack((a, b))
        res = array([[1, 2, 3],
                     [2, 3, 4]])
        self.assertTrue(np.array_equal(c, res), 'stack 1 value match')
        c = np.stack((a, b), axis = -1)
        res = array([[1, 2],
                     [2, 3],
                     [3, 4]])
        self.assertTrue(np.array_equal(c, res), 'stack 2 value match')
        arrays = [np.random.randn(3, 4) for _ in range(10)]
        s = np.stack(arrays, axis=0).shape
        self.assertEqual(s, (10, 3, 4), 'stack randn values match')
        
    def test_column_stack(self):
        a = np.array((1,2,3))
        b = np.array((2,3,4))
        c = np.column_stack((a,b))
        res = array([[1, 2],
                     [2, 3],
                     [3, 4]])
        self.assertTrue(np.array_equal(c, res), 'column_stack value match')
        
    def test_split(self):
        x = np.arange(9.0)
        y = np.split(x, 3)
        res = [array([0.,  1.,  2.]), 
               array([3.,  4.,  5.]), 
               array([6.,  7.,  8.])]
        for i in xrange(3):
            self.assertTrue(np.array_equal(y[i], res[i]), 'split value {} match'.format(i))
        x = np.arange(8.0)
        y = np.split(x, [3, 5, 6, 10])
        res = [array([0.,  1.,  2.]),
               array([3.,  4.]),
               array([5.]),
               array([6.,  7.])]
        for i in xrange(4):
            self.assertTrue(np.array_equal(y[i], res[i]), 'split value {} match'.format(i))
        
    def test_array_split(self):
        x = np.arange(8.0)
        y = np.array_split(x, 3)
        res = [array([0.,  1.,  2.]), 
               array([3.,  4.,  5.]), 
               array([6.,  7.])]
        for i in xrange(3):
            self.assertTrue(np.array_equal(y[i], res[i]), 'split value {} match'.format(i))
        x = np.arange(7.0)
        y = np.array_split(x, 3)
        res = [array([0.,  1.,  2.]), 
               array([3.,  4.]), 
               array([5.,  6.])]        
        for i in xrange(3):
            self.assertTrue(np.array_equal(y[i], res[i]), 'split value {} match'.format(i))
        
    def test_dsplit(self):
        x = np.arange(16.0).reshape(2, 2, 4)
        y = np.dsplit(x, 2)
        res = [array([[[ 0.,  1.],
                       [ 4.,  5.]],
                      [[ 8.,  9.],
                       [12., 13.]]]), 
               array([[[ 2.,  3.],
                       [ 6.,  7.]],
                      [[10., 11.],
                       [14., 15.]]])]
        for i in xrange(2):
            self.assertTrue(np.array_equal(y[i], res[i]), 'dsplit 1 value {} match'.format(i))
        y = np.dsplit(x, array([3, 6]))
        res = [array([[[ 0.,   1.,   2.],
                       [ 4.,   5.,   6.]],
                      [[ 8.,   9.,  10.],
                       [12.,  13.,  14.]]]),
               array([[[ 3.],
                       [ 7.]],
                      [[11.],
                       [15.]]]),]
        for i in xrange(2):
            self.assertTrue(np.array_equal(y[i], res[i]), 'dsplit 2 value {} match'.format(i))
        
    def test_hsplit(self):
        x = np.arange(16.0).reshape(4, 4)
        y = np.hsplit(x, 2)
        res = [array([[  0.,   1.],
                      [  4.,   5.],
                      [  8.,   9.],
                      [12.,  13.]]),
               array([[  2.,   3.],
                      [  6.,   7.],
                      [10.,  11.],
                      [14.,  15.]])]
        for i in xrange(2):
            self.assertTrue(np.array_equal(y[i], res[i]), 'hsplit 1 value {} match'.format(i))
        y = np.hsplit(x, array([3 ,6]))
        res = [array([[ 0.,   1.,   2.],
                      [ 4.,   5.,   6.],
                      [ 8.,   9.,  10.],
                      [12.,  13.,  14.]]),
               array([[ 3.],
                      [ 7.],
                      [11.],
                      [15.]])]
        for i in xrange(2):
            self.assertTrue(np.array_equal(y[i], res[i]), 'hsplit 2 value {} match'.format(i))
        x = np.arange(8.0).reshape(2, 2, 2)
        y = np.hsplit(x, 2)
        res = [array([[[0.,  1.]],
                      [[4.,  5.]]]),
               array([[[2.,  3.]],
                      [[6.,  7.]]])]
        for i in xrange(2):
            self.assertTrue(np.array_equal(y[i], res[i]), 'hsplit 3 value {} match'.format(i))

    def test_vsplit(self):
        x = np.arange(16.0).reshape(4, 4)
        y = np.vsplit(x, 2)
        res = [array([[0., 1., 2., 3.],
                      [4., 5., 6., 7.]]), 
               array([[ 8.,  9., 10., 11.],
                      [12., 13., 14., 15.]])]
        for i in xrange(2):
            self.assertTrue(np.array_equal(y[i], res[i]), 'vsplit 1 value {} match'.format(i))
        
        y = np.vsplit(x, array([3, 6]))
        res = [array([[ 0.,  1.,  2.,  3.],
                      [ 4.,  5.,  6.,  7.],
                      [ 8.,  9., 10., 11.]]), 
               array([[12., 13., 14., 15.]])]
        for i in xrange(2):
            self.assertTrue(np.array_equal(y[i], res[i]), 'vsplit 2 value {} match'.format(i))
    
    def test_tile(self):
        a = np.array([0, 1, 2])
        b = np.tile(a, 2)
        res = array([0, 1, 2, 0, 1, 2])
        self.assertTrue(np.array_equal(b, res), 'tile 1 value match')
        c = np.tile(a, (2, 2))
        res = array([[0, 1, 2, 0, 1, 2],
                     [0, 1, 2, 0, 1, 2]])
        self.assertTrue(np.array_equal(c, res), 'tile 2 value match')
        d = np.tile(a, (2, 1, 2))
        res = array([[[0, 1, 2, 0, 1, 2]],
                     [[0, 1, 2, 0, 1, 2]]])
        self.assertTrue(np.array_equal(d, res), 'tile 3 value match')
        
        a = np.array([[1, 2], [3, 4]])
        b = np.tile(a, 2)
        res = array([[1, 2, 1, 2],
                     [3, 4, 3, 4]])
        self.assertTrue(np.array_equal(b, res), 'tile 4 value match')

        c = np.tile(a, (2, 1))
        res = array([[1, 2],
                     [3, 4],
                     [1, 2],
                     [3, 4]])
        self.assertTrue(np.array_equal(c, res), 'tile 5 value match')

        a = np.array([1,2,3,4])
        b = np.tile(a,(4,1))
        res = array([[1, 2, 3, 4],
                     [1, 2, 3, 4],
                     [1, 2, 3, 4],
                     [1, 2, 3, 4]])
        self.assertTrue(np.array_equal(b, res), 'tile 6 value match')
        
    def test_delete(self):
        a = np.array([[1,2,3,4], [5,6,7,8], [9,10,11,12]])
        b = np.delete(a, 1, 0)
        res = array([[ 1,  2,  3,  4],
                     [ 9, 10, 11, 12]])
        self.assertTrue(np.array_equal(b, res), 'delete 1 value match')
        
        b = np.delete(a, np.s_[::2], 1)
        res = array([[ 2,  4],
                     [ 6,  8],
                     [10, 12]])
        self.assertTrue(np.array_equal(b, res), 'delete 2 value match')

        b = np.delete(a, [1,3,5], None)
        res = array([ 1,  3,  5,  7,  8,  9, 10, 11, 12])
        self.assertTrue(np.array_equal(b, res), 'delete 3 value match')
        
    def test_insert(self):
        a = np.array([[1, 1], [2, 2], [3, 3]])
        b = np.insert(a, 1, 5)
        res = array([1, 5, 1, 2, 2, 3, 3])
        self.assertTrue(np.array_equal(b, res), 'insert 1 value match')
        b = np.insert(a, 1, 5, axis=1)
        res = array([[1, 5, 1],
                     [2, 5, 2],
                     [3, 5, 3]])
        self.assertTrue(np.array_equal(b, res), 'insert 2 value match')
        
        b = np.insert(a, [1], [[1],[2],[3]], axis=1)
        res = array([[1, 1, 1],
                     [2, 2, 2],
                     [3, 3, 3]])
        self.assertTrue(np.array_equal(b, res), 'insert 3 value match')
        b = np.insert(a, [1], [[1],[2],[3]], axis=1)
        self.assertTrue(np.array_equal(b, res), 'insert 4 value match')
        
        x = np.arange(8).reshape(2, 4)
        y = np.insert(x, (1, 3), 999, axis=1)
        res = array([[  0, 999,   1,   2, 999,   3],
                     [  4, 999,   5,   6, 999,   7]])
        self.assertTrue(np.array_equal(y, res), 'insert 5 value match')

    def test_append(self):
        a = np.append([1, 2, 3], [[4, 5, 6], [7, 8, 9]])
        res = array([1, 2, 3, 4, 5, 6, 7, 8, 9])
        self.assertTrue(np.array_equal(a, res), 'append 1 value match')
        a = np.append([[1, 2, 3], [4, 5, 6]], [[7, 8, 9]], axis=0)
        res = array([[1, 2, 3],
                     [4, 5, 6],
                     [7, 8, 9]])
        self.assertTrue(np.array_equal(a, res), 'append 2 value match')
        
        a = [[1, 2, 3], [4, 5, 6]]
        obj = [7, 8, 9]
        axis = 0
        self.assertRaises(ValueError, np.append, a, obj, axis)

    def test_resize(self):
        a = np.array([[0,1],[2,3]])
        b = np.resize(a,(2,3))
        res = array([[0, 1, 2],
                     [3, 0, 1]])
        self.assertTrue(np.array_equal(b, res), 'resize 1 value match')
        b = np.resize(a,(1,4))
        res = array([[0, 1, 2, 3]])
        self.assertTrue(np.array_equal(b, res), 'resize 2 value match')
        b = np.resize(a,(2,4))
        res = array([[0, 1, 2, 3],
                     [0, 1, 2, 3]])
        self.assertTrue(np.array_equal(b, res), 'resize 3 value match')

    def test_trim_zeros(self):
        a = np.array((0, 0, 0, 1, 2, 3, 0, 2, 1, 0))
        b = np.trim_zeros(a)
        res = array([1, 2, 3, 0, 2, 1])
        self.assertTrue(np.array_equal(b, res), 'trim zeros 1 value match')
        
    def test_flip(self):
        A = np.arange(8).reshape((2,2,2))
        b = np.flip(A, 0)
        res = array([[[4, 5],
                      [6, 7]],
                     [[0, 1],
                      [2, 3]]])
        self.assertTrue(np.array_equal(b, res), 'flip 1 value match')
        b = np.flip(A, 1)
        res = array([[[2, 3],
                      [0, 1]],
                     [[6, 7],
                      [4, 5]]])
        self.assertTrue(np.array_equal(b, res), 'flip 2 value match')
        b = np.flip(A)
        res = array([[[7, 6],
                      [5, 4]],
                     [[3, 2],
                      [1, 0]]])
        self.assertTrue(np.array_equal(b, res), 'flip 3 value match')
        b = np.flip(A, (0, 2))
        res = array([[[5, 4],
                      [7, 6]],
                     [[1, 0],
                      [3, 2]]])
        self.assertTrue(np.array_equal(b, res), 'flip 4 value match')
        
    def test_fliplr(self):
        A = np.diag([1.,2.,3.])
        b = np.fliplr(A)
        res = array([[0.,  0.,  1.],
                     [0.,  2.,  0.],
                     [3.,  0.,  0.]])
        self.assertTrue(np.array_equal(b, res), 'fliplr 1 value match')
        
    def test_flipud(self):
        A = np.diag([1.0, 2, 3])
        b = np.flipud(A)
        res = array([[0.,  0.,  3.],
                     [0.,  2.,  0.],
                     [1.,  0.,  0.]])
        self.assertTrue(np.array_equal(b, res), 'flipud 1 value match')

        b = np.flipud([1,2])
        res = array([2, 1])
        self.assertTrue(np.array_equal(b, res), 'flipud 2 value match')
        
    def test_roll(self):
        x = np.arange(10)
        y = np.roll(x, 2)
        res = array([8, 9, 0, 1, 2, 3, 4, 5, 6, 7])
        self.assertTrue(np.array_equal(y, res), 'roll 1 value match')

        y = np.roll(x, -2)
        res = array([2, 3, 4, 5, 6, 7, 8, 9, 0, 1])
        self.assertTrue(np.array_equal(y, res), 'roll 2 value match')

        x2 = np.reshape(x, (2,5))
        y = np.roll(x2, 1)
        res = array([[9, 0, 1, 2, 3],
                     [4, 5, 6, 7, 8]])
        self.assertTrue(np.array_equal(y, res), 'roll 3 value match')

        y = np.roll(x2, -1)
        res = array([[1, 2, 3, 4, 5],
                     [6, 7, 8, 9, 0]])
        self.assertTrue(np.array_equal(y, res), 'roll 4 value match')

        y = np.roll(x2, 1, axis=0)
        res = array([[5, 6, 7, 8, 9],
                     [0, 1, 2, 3, 4]])
        self.assertTrue(np.array_equal(y, res), 'roll 5 value match')

        y = np.roll(x2, -1, axis=0)
        res = array([[5, 6, 7, 8, 9],
                     [0, 1, 2, 3, 4]])
        self.assertTrue(np.array_equal(y, res), 'roll 6 value match')

        y = np.roll(x2, 1, axis=1)
        res = array([[4, 0, 1, 2, 3],
                     [9, 5, 6, 7, 8]])
        self.assertTrue(np.array_equal(y, res), 'roll 7 value match')

        y = np.roll(x2, -1, axis=1)
        res = array([[1, 2, 3, 4, 0],
                     [6, 7, 8, 9, 5]])
        self.assertTrue(np.array_equal(y, res), 'roll 8 value match')

    def test_put(self):
        a = np.arange(5)
        np.put(a, [0, 2], [-44, -55])
        res = array([-44,   1, -55,   3,   4])
        self.assertTrue(np.array_equal(a, res), 'put 1')
        
        a = np.arange(5)
        np.put(a, 22, -5, mode='clip')
        res = array([ 0,  1,  2,  3, -5])
        self.assertTrue(np.array_equal(a, res), 'put 2')
        
    def test_ravel(self):
        x = np.array([[1, 2, 3], [4, 5, 6]])
        y = np.ravel(x)
        res = array([1, 2, 3, 4, 5, 6])
        self.assertTrue(np.array_equal(y, res), 'ravel 1')

        y = np.ravel(x.T)
        res = array([1, 4, 2, 5, 3, 6])
        self.assertTrue(np.array_equal(y, res), 'ravel 2')

    def test_reshape(self):
        a = np.arange(6).reshape((3, 2))
        res = array([[0, 1],
                     [2, 3],
                     [4, 5]])
        self.assertTrue(np.array_equal(a, res), 'reshape 1')
        
        r = np.reshape(a, (2, 3))
        res = array([[0, 1, 2],
                     [3, 4, 5]])
        self.assertTrue(np.array_equal(r, res), 'reshape 2')

        r = np.reshape(np.ravel(a), (2, 3))
        res = array([[0, 1, 2],
                     [3, 4, 5]])
        self.assertTrue(np.array_equal(r, res), 'reshape 3')
        
        a = np.array([[1,2,3], [4,5,6]])
        r = np.reshape(a, 6)
        res = array([1, 2, 3, 4, 5, 6])
        self.assertTrue(np.array_equal(r, res), 'reshape 4')
        
        r = np.reshape(a, (3,-1))
        res = array([[1, 2],
                     [3, 4],
                     [5, 6]])
        self.assertTrue(np.array_equal(r, res), 'reshape 5')

    def test_searchsorted(self):
        s = np.searchsorted([1,2,3,4,5], 3)
        self.assertEqual(s, 2, 'searchsorted 1')

        s = np.searchsorted([1,2,3,4,5], 3, side='right')
        self.assertEqual(s, 3, 'searchsorted 2')

        s = np.searchsorted([1,2,3,4,5], [-10, 10, 2, 3])
        res = array([0, 5, 1, 2])
        self.assertTrue(np.array_equal(s, res), 'searchsorted 3')
        
    def test_squeeze(self):
        x = np.array([[[0], [1], [2]]])
        s = np.squeeze(x).shape
        self.assertEqual(s, (3,), 'squeeze 1 shape')

        s = np.squeeze(x, axis=0).shape
        self.assertEqual(s, (3, 1), 'squeeze 2 shape')
        
        s = np.squeeze(x, axis=2).shape
        self.assertEqual(s, (1, 3), 'squeeze 3 shape')

#         x = np.array([[1234]])
#         s = np.squeeze(x)
#         res = array(1234)  # 0d array
#         s = np.squeeze(x).shape
#         self.assertEqual(s, (), 'squeeze 4 shape')
#         s = np.squeeze(x)[()]
#         self.assertEqual(s, 1234, 'squeeze 5')


        
class TestLogic(TestCase):

    def test_array_equal(self):
        a = np.arange(10)
        b = np.arange(10)
        eq = np.array_equal(a, b)
        self.assertTrue(eq, "array_equal failed")
        
    def test_all(self):
        x = np.all([[True,False],[True,True]])
        self.assertFalse(x, 'all false return')
        
        x = np.all([[True,False],[True,True]], axis=0)
        res = array([True, False])
        self.assertTrue(np.array_equal(x, res), 'all axis = 0 return')

        x = np.all([-1, 4, 5])
        self.assertTrue(x, 'all true return')

        x = np.all([1.0, np.nan])
        self.assertTrue(x, 'all true 2 return')

        o=np.array(False)
        z=np.all([-1, 4, 5], out=o)
        res = array(True)
        self.assertTrue(np.array_equal(o, res), 'all with out 1')
        self.assertTrue(np.array_equal(o, z), 'all with out 2')
        
        x=np.arange(24).reshape(2,3,4)
        y=np.all(x, (0, 1))
        res = array([False,  True,  True,  True])
        self.assertTrue(np.array_equal(y, res), 'all multi-axis out 1')
        y=np.all(x, (0, 2))
        res = array([False,  True,  True])
        self.assertTrue(np.array_equal(y, res), 'all multi-axis out 2')
        
        
    
    def test_any(self):
        x = np.any([[True, False], [True, True]])
        self.assertTrue(x, 'any true 1 return')

        x = np.any([[True, False], [False, False]], axis=0)
        res = array([ True, False])
        self.assertTrue(np.array_equal(x, res), 'any with axis 1')

        x = np.any([-1, 0, 5])
        self.assertTrue(x, 'any true 2 return')

        x = np.any(np.nan)
        self.assertTrue(x, 'any true 3 return')

        o = np.array(False)
        z = np.any([-1, 4, 5], out=o)
        self.assertTrue(np.array_equal(o, array(True)), 'all with out 1')
        self.assertTrue(np.array_equal(z, array(True)), 'all with out 2')
        self.assertTrue(z is o, 'all with out 3')

    def test_argmax(self):
        a = np.arange(6).reshape(2,3)
        m = np.argmax(a)
        self.assertEqual(m, 5, 'argmax max at 5')

        m = np.argmax(a, axis=0)
        res = array([1, 1, 1])
        self.assertTrue(np.array_equal(m, res), 'argmax axis 0')
        
        m = np.argmax(a, axis=1)
        res = array([2, 2])
        self.assertTrue(np.array_equal(m, res), 'argmax axis 1')
        

    def test_argmin(self):
        a = np.arange(6).reshape(2,3)
        m = np.argmin(a)
        self.assertEqual(m, 0, 'argmin min at 0')

        m = np.argmin(a, axis=0)
        res = array([0, 0, 0])
        self.assertTrue(np.array_equal(m, res), 'argmin axis 0')
        
        m = np.argmin(a, axis=1)
        res = array([0, 0])
        self.assertTrue(np.array_equal(m, res), 'argmin axis 1')
        
        b = np.arange(6)
        b[4] = 0
        self.assertEqual(np.argmin(b), 0, 'argmin only the first occurrence is returned')
        
    def test_sort(self):
        a = np.array([[1,4],[3,1]])
        np.sort(a)                # sort along the last axis
        res = array([[1, 4],
                     [1, 3]])
        self.assertTrue(np.array_equal(a, res), 'sort 1 axis = -1')

        np.sort(a, axis=None)     # sort the flattened array
        res = array([[1, 1], [3, 4]])
        self.assertTrue(np.array_equal(a, res), 'sort 2 None axis')

        np.sort(a, axis=0)        # sort along the first axis
        res = array([[1, 1],
                     [3, 4]])
        self.assertTrue(np.array_equal(a, res), 'sort 3 axis = 0')

class TestNDArray(TestCase):
    def setUp(self):
        pass
    
    def test_fill(self):
        a = np.array([1, 2])
        a.fill(0)
        res = array([0, 0])
        self.assertTrue(np.array_equal(a, res), 'ndarray fill 1')
        
        a = np.empty(2)
        a.fill(1)
        res = array([1.,  1.])
        self.assertTrue(np.array_equal(a, res), 'ndarray fill 2')
        
    def test_flatten(self):
        a = np.array([[1,2], [3,4]])
        b = a.flatten()
        res = array([1, 2, 3, 4])
        self.assertTrue(np.array_equal(b, res), 'ndarray flatten 1')

    def test_item(self):
        x = array([[2, 2, 6],
                   [1, 3, 6],
                   [1, 0, 1]])
        a = x.item(3)
        self.assertEqual(a, 1, 'ndarray item 0')

        a = x.item(7)
        self.assertEqual(a, 0, 'ndarray item 1')


        a = x.item((0, 1))
        self.assertEqual(a, 2, 'ndarray item 2')

        a = x.item((2, 2))
        self.assertEqual(a, 1, 'ndarray item 3')
        
    def test_itemset(self):
        x = array([[2, 2, 6],
                   [1, 3, 6],
                   [1, 0, 1]])
        x.itemset(4, 0)
        x.itemset((2, 2), 9)
        res = array([[2, 2, 6],
                     [1, 0, 6],
                     [1, 0, 9]])
        self.assertTrue(np.array_equal(x, res), 'ndarray itemset 0')
        
    def test_tolist(self):
        a = np.array([1, 2])
        a_list = list(a)
        res = [1, 2]
        self.assertEqual(a_list, res, 'ndarray tolist 1')
        
        t = type(a_list[0])
        self.assertEqual(t, int, 'ndarray tolist 2 type')
        
        a_tolist = a.tolist()
        res = [1, 2]
        self.assertEqual(a_tolist, res, 'ndarray tolist 3')
        
        t = type(a_tolist[0])
        self.assertEqual(t, int, 'ndarray tolist 4 type')
        
        a = np.array([[1, 2], [3, 4]])
        tl = list(a)
        res = [array([1, 2]), array([3, 4])]
        self.assertTrue(np.array_equal(tl[0], res[0]), 'ndarray tolist 5')
        self.assertTrue(np.array_equal(tl[1], res[1]), 'ndarray tolist 5')
        
        tl = a.tolist()
        res = [[1, 2], [3, 4]]
        self.assertEqual(tl, res, 'ndarray tolist 6 2d')
        
class TestMath(TestCase):
    def setUp(self):
        TestCase.setUp(self)
    
    def test_cumprod(self):
        a = np.array([[1, 2, 3], [4, 5, 6]])
        b = np.cumprod(a, dtype=float)
        res = array([   1.,    2.,    6.,   24.,  120.,  720.])
        self.assertTrue(np.array_equal(b, res), 'math cumprod None axis')
        
        b = np.cumprod(a, axis=0)
        res = array([[ 1,  2,  3],
                     [ 4, 10, 18]])
        self.assertTrue(np.array_equal(b, res), 'math cumprod axis 0')
    
        b = np.cumprod(a,axis=1)
        res = array([[  1,   2,   6],
                     [  4,  20, 120]])
        self.assertTrue(np.array_equal(b, res), 'math cumprod axis 1')

    def test_cumsum(self):
        a = np.array([[1, 2, 3], [4, 5, 6]])
        b = np.cumsum(a, dtype=float)
        res = array([  1.,   3.,   6.,  10.,  15.,  21.])
        self.assertTrue(np.array_equal(b, res), 'math cumsum None axis')
        
        b = np.cumsum(a, axis=0)
        res = array([[1, 2, 3],
                     [5, 7, 9]])
        self.assertTrue(np.array_equal(b, res), 'math cumsum axis 0')
    
        b = np.cumsum(a,axis=1)
        res = array([[ 1,  3,  6],
                     [ 4,  9, 15]])
        self.assertTrue(np.array_equal(b, res), 'math cumsum axis 1')

    def test_dot(self):
        a = np.dot(3, 4)
        self.assertEqual(a, 12, 'math dot scaler multiplication')
        
        a = [[1, 0], [0, 1]]
        b = [[4, 1], [2, 2]]
        c = np.dot(a, b)
        res = array([[4, 1],
                     [2, 2]])
        self.assertTrue(np.array_equal(c, res), 'math dot matrix dot')
        
        a = np.arange(3*4*5*6).reshape((3,4,5,6))
        b = np.arange(3*4*5*6 - 1, -1).reshape((5,4,6,3))
        c = np.dot(a, b)[2,3,2,1,2,2]
        self.assertEqual(c, 499128, 'math dot multi-dimension dot')

    def test_nonzero(self):
        x = np.array([[3, 0, 0], [0, 4, 0], [5, 6, 0]])
        y = np.nonzero(x)
        res = (array([0, 1, 2, 2]), array([0, 1, 0, 1]))
        self.assertTrue(np.array_equal(y[0], res[0]), 'math nonzero 1 0')
        self.assertTrue(np.array_equal(y[1], res[1]), 'math nonzero 1 1')
        
        
#         x[np.nonzero(x)]
#         array([3, 4, 5, 6])
        
    def test_amax(self):
        a = np.arange(4).reshape((2,2))
        m = np.amax(a)           # Maximum of the flattened array
        self.assertEqual(m, 3, 'math amax 1')

        m = np.amax(a, axis=0)   # Maxima along the first axis
        res = array([2, 3])
        self.assertTrue(np.array_equal(m, res), 'math max 2 with axis')

        m = np.amax(a, axis=1)   # Maxima along the second axis
        res = array([1, 3])
        self.assertTrue(np.array_equal(m, res), 'math max 3 with axis')

        m = np.amax(a, initial=3, axis=1)
        res = array([3,  3])
        self.assertTrue(np.array_equal(m, res), 'math max 4 with initial value')

        b = np.arange(5, float)
        b[2] = np.NaN
        m = np.amax(b)
        self.assertEqual(m, 4.0, 'math max 5 with NaN value')

#         m = np.nanmax(b)
#         self.assertEqual(m, 4.0, 'math max 6 with NaN support')

    def test_amin(self):
        a = np.arange(4).reshape((2,2))
        m = np.amin(a)           # Minimum of the flattened array
        self.assertEqual(m, 0, 'math min 1 flat array')

        m = np.amin(a, axis=0)   # Minima along the first axis
        res = array([0, 1])
        self.assertTrue(np.array_equal(m, res), 'math max 2 with axis')

        m = np.amin(a, axis=1)   # Minima along the second axis
        res = array([0, 2])
        self.assertTrue(np.array_equal(m, res), 'math min 3 with axis')

        m = np.amin(a, initial=0, axis=0)
        res = array([0,  0])
        self.assertTrue(np.array_equal(m, res), 'math max 4 with initial')

        b = np.arange(5, float)
        b[2] = np.NaN
        m = np.amin(b)
        self.assertEqual(m, 0.0, 'math min 5 with nan value')

        m = np.amin([[-50], [10]], axis=-1, initial=0)
        res = array([-50,   0])
        self.assertTrue(np.array_equal(m, res), 'math max 6 with axis and initial')

    def test_mean(self):
        a = np.array([[1, 2], [3, 4]])
        m = np.mean(a)
        self.assertEqual(m, 2.5, 'math mean 1')
    
        m = np.mean(a, axis=1)
        res = array([2., 3.])
        self.assertTrue(np.array_equal(m, res), 'math mean 2 with axis')
    
        m = np.mean(a, axis=0)
        res = array([1.5, 3.5])
        self.assertTrue(np.array_equal(m, res), 'math mean 3 with axis')
        
    def test_prod(self):
        p = np.prod([1.,2.])
        self.assertEqual(p, 2.0, 'math prod 1')
        
        p = np.prod([[1.,2.],[3.,4.]])
        self.assertEqual(p, 24.0, 'math prod 2')
        
        p = np.prod([[1.,2.],[3.,4.]], axis=0)
        res = array([ 3., 8.])
        self.assertTrue(np.array_equal(p, res), 'math prod 3 with axis')

        p = np.prod([[1.,2.],[3.,4.]], axis=1)
        res = array([  2.,  12.])
        self.assertTrue(np.array_equal(p, res), 'math prod 4 with axis')
        
        p = np.prod([1, 2], initial=5)
        self.assertEqual(p, 10, 'math prod 5 with initial value')
        
    def test_ptp(self):
        x = np.array([[4, 9, 2, 10],
                      [6, 9, 7, 12]])
        p = np.ptp(x, axis=1)
        res = array([8, 6])
        self.assertTrue(np.array_equal(p, res), 'math ptp 1 with axis')

        p = np.ptp(x, axis=0)
        res = array([2, 0, 5, 2])
        self.assertTrue(np.array_equal(p, res), 'math ptp 2 with axis')

        p = np.ptp(x)
        self.assertEqual(p, 10, 'math ptp 3')
        
        y = np.array([[1, 127],
                      [0, 127],
                      [-1, 127],
                      [-2, 127]])
        p = np.ptp(y, axis=1)
        res = array([ 126,  127, 128, 129])
        self.assertTrue(np.array_equal(p, res), 'math ptp 4')
        
    def test_round(self):
        r = np.around([0.37, 1.64])
        res = array([0.,  2.])
        self.assertTrue(np.array_equal(r, res), 'math round 1')

        r = np.around([0.37, 1.64], decimals=1)
        res = array([0.4,  1.6])
        self.assertTrue(np.array_equal(r, res), 'math round 2')

        r = np.around([.5, 1.5, 2.5, 3.5, 4.5]) # rounds to nearest even value
        res = array([1.,  2.,  3.,  4.,  5.])
        self.assertTrue(np.array_equal(r, res), 'math round 3')
        
        r = np.around([1,2,3,11], decimals=1) # ndarray of ints is returned
        res = array([ 1,  2,  3, 11])
        self.assertTrue(np.array_equal(r, res), 'math round 4')
        
        r = np.around([1,2,3,11], decimals=-1)
        res = array([ 0,  0,  0, 10])
        self.assertTrue(np.array_equal(r, res), 'math round 5')
        
    def test_std(self):
        a = np.array([[1, 2], [3, 4]])
        s = np.std(a)
        self.assertEqual(s, 1.1180339887498949, 'math std 1')
        
        s = np.std(a, axis=0)
        res = array([1.,  1.])
        self.assertTrue(np.array_equal(s, res), 'math std 2 axis 0')
        
        s = np.std(a, axis=1)
        res = array([0.5,  0.5])
        self.assertTrue(np.array_equal(s, res), 'math std 3 axis 1')
        
        a = np.arange(24).reshape(2, 3, 4)
        s = np.std(a, axis = 0)
        res = array([[6., 6., 6., 6.],
                     [6., 6., 6., 6.],
                     [6., 6., 6., 6.]])
        self.assertTrue(np.array_equal(s, res), 'math std 4 axis 0')
        
        s = np.std(a, axis = 1)
        res = array([[3.26598632, 3.26598632, 3.26598632, 3.26598632],
                     [3.26598632, 3.26598632, 3.26598632, 3.26598632]])
        self.assertTrue(np.allclose(s, res), 'math std 5 axis 1')
        
        s = np.std(a, axis = 2)
        res = array([[1.11803399, 1.11803399, 1.11803399],
                     [1.11803399, 1.11803399, 1.11803399]])
        self.assertTrue(np.allclose(s, res), 'math std 6 axis 2')
        
        s = np.std(a, axis = (0,2))
        res = array([6.10327781, 6.10327781, 6.10327781])
        self.assertTrue(np.allclose(s, res), 'math std 6 axis (0,2)')
        
    def test_sum(self):
        s = np.sum([0.5, 1.5])
        self.assertEqual(s, 2.0, 'math sum 1')
        
        s = np.sum([[0, 1], [0, 5]])
        self.assertEqual(s, 6, 'math sum 3')
        
        s = np.sum([[0, 1], [0, 5]], axis=0)
        res = array([0, 6])
        self.assertTrue(np.array_equal(s, res), 'math sum 4 with axis 0')
        
        s = np.sum([[0, 1], [0, 5]], axis=1)
        res = array([1, 5])
        self.assertTrue(np.array_equal(s, res), 'math sum 5 with axis 1')
        
    def test_var(self):
        a = np.array([[1, 2], [3, 4]])
        v = np.var(a)
        self.assertEqual(v, 1.25, 'math var 1')
        
        v = np.var(a, axis=0)
        res = array([1.,  1.])
        self.assertTrue(np.allclose(v, res), 'math var 2 with axis 0')
        
        v = np.var(a, axis=1)
        res = array([0.25,  0.25])
        self.assertTrue(np.allclose(v, res), 'math var 3 with axis 1')
        
    def test_trace(self):
        t = np.trace(np.eye(3))
        self.assertEqual(t, 3.0, 'math trace 1')

        a = np.arange(8).reshape((2,2,2))
        t = np.trace(a)
        res = array([6, 8])
        self.assertTrue(np.array_equal(t, res), 'math trace 2 with 3 dims')

        a = np.arange(24).reshape((2,2,2,3))
        t = np.trace(a)
        self.assertEqual(t.shape, (2, 3), 'math trace 3 check shape')
        res = array([[18, 20, 22],
                     [24, 26, 28]])
        self.assertTrue(np.array_equal(t, res), 'math trace 4 with 4 dims')        

class TestOperator(TestCase):
    
    def setUp(self):
        TestCase.setUp(self)
        
    def test_floordiv(self):
        a = np.arange(12)
        b = a // 3
        res = array([0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3])
        self.assertTrue(np.array_equal(b, res), 'operator floordiv 1 int scaler')
        
        a = np.arange(12.)
        b = np.arange(12., 0, -1)
        c = a // b
        res = array([-0.,  0.,  0.,  0.,  0.,  0.,  1.,  1.,  2.,  3.,  5., 11.])
        self.assertTrue(np.array_equal(c, res), 'operator floordiv 2 float')
        
        b = np.array([4])
        c = a // b
        res = array([-0.,  0.,  0.,  0.,  1.,  1.,  1.,  1.,  2.,  2.,  2.,  2.])
        self.assertTrue(np.array_equal(c, res), 'operator floordiv 3 float')
        
    def test_ifloordiv(self):
        a = np.arange(12.)
        a //= 3
        res = array([-0.,  0.,  0.,  1.,  1.,  1.,  2.,  2.,  2.,  3.,  3.,  3.])
        self.assertTrue(np.array_equal(a, res), 'operator ifloordiv 1 float')
        
    def test_divmod(self):
        a = np.arange(12.)
        q, r = divmod(a, 3)
        qres = array([-0.,  0.,  0.,  1.,  1.,  1.,  2.,  2.,  2.,  3.,  3.,  3.])
        rres = array([0., 1., 2., 0., 1., 2., 0., 1., 2., 0., 1., 2.])
        self.assertTrue(np.array_equal(q, qres), 'operator divmod 1 quotient')
        self.assertTrue(np.array_equal(r, rres), 'operator divmod 2 remainder')
        
    def test_int(self):
        a = np.array([3.3])
        b = int(a)
        self.assertEqual(b, 3, 'operator int 1')
        
        a = np.arange(2.)
        try:
            b = int(a)
            raise 'error in exception test'
        except Exception, e:
            self.assertTrue(isinstance(e, TypeError), 'operator int 2 exception')

    def test_float(self):
        a = np.array([3])
        b = float(a)
        self.assertEqual(type(b), float, 'operator float 1')
        
        a = np.arange(2)
        try:
            b = float(a)
            raise 'error in exception test'
        except Exception, e:
            self.assertTrue(isinstance(e, TypeError), 'operator float 2 exception')

    def test_long(self):
        a = np.array([3])
        b = long(a)
        self.assertEqual(type(b), long, 'operator long 1')
        
        a = np.arange(2)
        try:
            b = long(a)
            raise 'error in exception test'
        except Exception, e:
            self.assertTrue(isinstance(e, TypeError), 'operator long 2 exception')
        
def getSuite():
    return unittest.TestSuite([\
            unittest.TestLoader().loadTestsFromTestCase(TestCreation),\
            unittest.TestLoader().loadTestsFromTestCase(TestNDArray),\
            unittest.TestLoader().loadTestsFromTestCase(TestLogic),\
            unittest.TestLoader().loadTestsFromTestCase(TestManipulation),\
            unittest.TestLoader().loadTestsFromTestCase(TestMath),\
            unittest.TestLoader().loadTestsFromTestCase(TestOperator),\
            ])

def run_test():
#     suite = unittest.TestLoader().loadTestsFromTestCase(TestCreation)
    suite = getSuite()
    unittest.TextTestRunner(verbosity=2).run(suite)
    
def run():
    run_test()