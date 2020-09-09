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
        self.assertEqual(e.shape, (2, 2))

    def test_empty_like(self):
        a = ([1,2,3], [4,5,6])
        np.empty_like(a)

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

class TestLogic(TestCase):

    def test_array_equal(self):
        a = np.arange(10)
        b = np.arange(10)
        eq = np.array_equal(a, b)
        self.assertTrue(eq, "array_equal failed")
        
class TestNDArray(TestCase):
    def setUp(self):
        pass
    

def getSuite():
    return unittest.TestSuite([\
            unittest.TestLoader().loadTestsFromTestCase(TestCreation),\
            unittest.TestLoader().loadTestsFromTestCase(TestLogic)])

def run_test():
#     suite = unittest.TestLoader().loadTestsFromTestCase(TestCreation)
    suite = getSuite()
    unittest.TextTestRunner(verbosity=2).run(suite)