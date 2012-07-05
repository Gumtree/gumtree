from gumpy.lib.odict import OrderedDict
import unittest

class TestODict(unittest.TestCase):
    
    def test_odict(self):
    	d = OrderedDict(((1, 3), (3, 2), (2, 1)))
    	self.assertEqual(d[2], 1)
    	self.assertEqual(d[3], 2)
    	self.assertEqual(len(d), 3)
    
def getSuite():
    return unittest.TestLoader().loadTestsFromTestCase(TestODict)
