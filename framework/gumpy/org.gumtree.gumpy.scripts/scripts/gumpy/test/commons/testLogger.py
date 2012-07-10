from gumpy.commons import logger
from java.io import StringWriter
from org.python.core import PyFileWriter
import sys
import unittest

class TestLogger(unittest.TestCase):
    
    def setUp(self):
        pass
    
    def test_logger(self):
        # Setup writer
        stringWriter = StringWriter()
        pyFileWriter = PyFileWriter(stringWriter)
        oldWriter = sys.stdout
        sys.stdout = pyFileWriter
        
        # Run
        logger.log('123')
        output = stringWriter.toString()
        sys.stdout = oldWriter
        
        # Assert
        self.assertTrue(output.index('123') > 0)

def getSuite():
    return unittest.TestLoader().loadTestsFromTestCase(TestLogger)
