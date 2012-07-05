from gumpy.commons import core
import unittest

class TestCore(unittest.TestCase):
    
    def test_get_service(self):
        scriptingManager = core.getService('org.gumtree.scripting.IScriptingManager')
        self.assertTrue(len(scriptingManager.getAllEngineFactories()) > 0)
    
def getSuite():
   return unittest.TestLoader().loadTestsFromTestCase(TestCore)
