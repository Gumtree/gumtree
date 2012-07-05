from gumpy.commons import jutils
import unittest

class TestJutils(unittest.TestCase):
    
    def test_get_values(self):
        self.assertTrue(jutils.get_inf() != None)
        self.assertTrue(jutils.get_nan() != None)

    def test_array_creation(self):
        self.assertEqual(len(jutils.jdoubles(1)), 1)
        self.assertEqual(len(jutils.jints(1)), 1)
        self.assertEqual(len(jutils.jbooleans(1)), 1)
        self.assertEqual(len(jutils.jbytes(1)), 1)
        self.assertEqual(len(jutils.jchars(1)), 1)
        self.assertEqual(len(jutils.jfloats(1)), 1)
        self.assertEqual(len(jutils.jlongs(1)), 1)
        self.assertEqual(len(jutils.jshorts(1)), 1)

def getSuite():
    return unittest.TestLoader().loadTestsFromTestCase(TestJutils)