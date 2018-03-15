import unittest
import time
from org.gumtree.control.core import SicsManager
from org.gumtree.control.events import ISicsCallback
from org.gumtree.control.exception import SicsInterruptException

USE_LOCAL_SERVER = True;
REMOTE_SERVER_ADDRESS = "tcp://ics1-bilby-test.nbi.ansto.gov.au:5555"
REMOTE_PUBLISHER_ADDRESS = "tcp://ics1-bilby-test.nbi.ansto.gov.au:5556"
LOCAL_SERVER_ADDRESS = "tcp://localhost:5555"
LOCAL_PUBLISHER_ADDRESS = "tcp://localhost:5566"
TEST_SCRIPT = """
s1=control.get_controller('s1')
for i in xrange(3):
    s1.drive(i)
"""

if USE_LOCAL_SERVER :
    SicsManager.getSicsProxy(LOCAL_SERVER_ADDRESS, LOCAL_PUBLISHER_ADDRESS)
else:
    SicsManager.getSicsProxy(REMOTE_SERVER_ADDRESS, REMOTE_PUBLISHER_ADDRESS)
            
from gumpy.control import control
import random

class TestControl(unittest.TestCase):
    
    def setUp(self):
        self.proxy = SicsManager.getSicsProxy()
        self.model = SicsManager.getSicsModel()
#         if USE_LOCAL_SERVER :
#             self.proxy = SicsManager.getSicsProxy(LOCAL_SERVER_ADDRESS, LOCAL_PUBLISHER_ADDRESS)
#         else:
#             self.proxy = SicsManager.getSicsProxy(REMOTE_SERVER_ADDRESS, REMOTE_PUBLISHER_ADDRESS)
        pass
        
    def test_initialisation(self):
        self.assertNotEqual(self.proxy, None, "proxy not null")
        self.assertNotEqual(self.model, None, "model not null")

    def test_get_controller(self):
        controller = control.get_controller('dummy_motor')
        self.assertNotEqual(controller, None, "controller not null")

    def test_get_value(self):
        control.drive('dummy_motor', 0)
        val = control.get_value('dummy_motor')
        self.assertEqual(val, 0, "dummy_motor at 0")
            
    def test_send_command(self):
        res = control.send_command('status')
        self.assertEqual("EAGER_TO_EXECUTE", res, "status is EAGER TO EXECUTE")
        
    def test_set_value(self):
        name = 'gumtree_time_estimate'
        val = '100'
        control.set_value(name, val)
        self.assertEqual(control.get_value(name), val, 'assert gumtree_time_estimate value')
        
    def test_run(self):
        pos = random.uniform(0, 100)
        control.run('s1', pos)
        self.assertEqual("DRIVING", control.get_status().getText(), "status is DRIVING")
        time.sleep(2)
        self.assertTrue(abs(pos - control.get_value('s1')) < 0.001, "s1 at " + str(pos))
        
    def test_drive(self):
        pos = random.uniform(0, 100)
        control.drive('s2', pos)
        self.assertTrue(abs(pos - control.get_value('s2')) < 0.001, "s2 at " + str(pos))
        
    def test_get_raw_value(self):
        val = control.get_raw_value('histmem preset')
        self.assertEqual(10, val, "histmem preset 10")
        
    def test_drive_interrupt(self):
        try:
            self.proxy.syncRun("drive dummy_motor 0 interrupt")
        except Exception as e:
            self.assertTrue(isinstance(e, SicsInterruptException), "expecting interrupt exception")
    
    def test_callback(self):
        cb = Callback()
        self.proxy.syncRun("drive dummy_motor 0", cb)
        self.assertTrue(cb.replyReceived, "reply received")
        self.assertTrue(cb.isFinished, "finish received")
        self.assertTrue(not cb.isError, "no error")

    def test_run_script(self):
        exec(TEST_SCRIPT)
        self.assertEqual(control.get_value('s1'), 2., 'assert s1 value to be 2')

class Callback(ISicsCallback):
    
    def __init__(self):
        self.replyReceived = False
        self.isFinished = False
        self.isError = False
        
    def receiveReply(self, data):
        print("reply")
        self.replyReceived = True
        
    def receiveFinish(self, data):
        print("finish")
        self.replyReceived = True
        self.isFinished = True
        
    def receiveError(self, data):
        self.isError = True
    
        
def run_test():
    suite = unittest.TestLoader().loadTestsFromTestCase(TestControl)
    unittest.TextTestRunner(verbosity=2).run(suite)