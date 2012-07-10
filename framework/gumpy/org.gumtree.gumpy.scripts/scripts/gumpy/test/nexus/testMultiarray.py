from gumpy.commons import jutils
from gumpy.nexus import *
from unittest import TestCase
import gumpy.nexus.dataset as np
import sys
import tempfile
import unittest

ndarray = dataset.Dataset
np = dataset
np.choose = np.take
empty = dataset.zeros
array = Array

class TestAttributes(TestCase):

    def setUp(self):
        self.one = arange(10)
        self.two = arange(20, [4, 5])
        self.three = arange(60, [2, 5, 6], float)

    def test_attributes(self):
        self.assertEqual(self.one.shape, [10])
        self.assertEqual(self.two.shape, [4,5])
        self.assertEqual(self.three.shape, [2,5,6])
        self.assertEqual(self.one.ndim, 1)
        self.assertEqual(self.two.ndim, 2)
        self.assertEqual(self.three.ndim, 3)
        self.assertEqual(self.two.size, 20)

    def test_dtypeattr(self):
        self.assertEqual(self.one.dtype, int)
        self.assertEqual(self.three.dtype, float)

    # [Tony] Not working
#    def test_stridesattr(self):
#        x = self.one
#        def make_array(size, offset, strides):
#            return ndarray(x, shape = [size], dtype=int)
#        self.assertEqual(make_array(4, 4, -1), Array([4, 3, 2, 1]))
#        self.failUnlessRaises(ValueError, make_array, 4, 4, -2)
#        self.failUnlessRaises(ValueError, make_array, 4, 2, -1)
#        self.failUnlessRaises(ValueError, make_array, 8, 3, 1)
    
    # [Tony] Not working (not supported exception)
#    def test_fill(self):
#        for t in (int, float, str, long) :
#            x = empty((3,2,1), t)
#            y = empty((3,2,1), t)
#            x.fill(1)
#            y[...] = 1
#            assert_equal(x,y)
#
#        x = array([(0,0.0), (1,1.0)], dtype='i4,f8')
#        x.fill(x[0])
#        self.assertEqual(x['f1'][1], x['f1'][0])

class TestZeroRank(TestCase):
    def setUp(self):
        self.d = array(0), array('x', object)
    
#    def test_ellipsis_subscript(self):
#        a,b = self.d
#        self.failUnlessEqual(a[...], 0)
#        self.failUnlessEqual(b[...], 'x')
#        self.failUnless(a[...] is a)
#        self.failUnless(b[...] is b)
#
#    def test_empty_subscript(self):
#        a,b = self.d
#        self.failUnlessEqual(a[()], 0)
#        self.failUnlessEqual(b[()], 'x')
#        self.failUnless(type(a[()]) is a.dtype.type)
#        self.failUnless(type(b[()]) is str)
#
#    def test_invalid_subscript(self):
#        a,b = self.d
#        self.failUnlessRaises(IndexError, lambda x: x[0], a)
#        self.failUnlessRaises(IndexError, lambda x: x[0], b)
#        self.failUnlessRaises(IndexError, lambda x: x[array([], int)], a)
#        self.failUnlessRaises(IndexError, lambda x: x[array([], int)], b)
#
#    def test_ellipsis_subscript_assignment(self):
#        a,b = self.d
#        a[...] = 42
#        self.failUnlessEqual(a, 42)
#        b[...] = ''
#        self.failUnlessEqual(b.item(), '')
#
#    def test_empty_subscript_assignment(self):
#        a,b = self.d
#        a[()] = 42
#        self.failUnlessEqual(a, 42)
#        b[()] = ''
#        self.failUnlessEqual(b.item(), '')
#
#    def test_invalid_subscript_assignment(self):
#        a,b = self.d
#        def assign(x, i, v):
#            x[i] = v
#        self.failUnlessRaises(IndexError, assign, a, 0, 42)
#        self.failUnlessRaises(IndexError, assign, b, 0, '')
#        self.failUnlessRaises(ValueError, assign, a, (), '')
#
#    def test_constructor(self):
#        x = ndarray(())
#        x[()] = 5
#        self.failUnlessEqual(x[()], 5)
#        y = ndarray((),buffer=x)
#        y[()] = 6
#        self.failUnlessEqual(x[()], 6)

def getSuite():
    return unittest.TestSuite([\
            unittest.TestLoader().loadTestsFromTestCase(TestAttributes),\
            unittest.TestLoader().loadTestsFromTestCase(TestZeroRank)])
