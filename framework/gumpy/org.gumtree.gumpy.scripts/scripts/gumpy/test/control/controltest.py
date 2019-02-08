import unittest
import time
from org.gumtree.control.core import SicsManager
from org.gumtree.control.events import ISicsCallback
from org.gumtree.control.exception import SicsInterruptException

USE_LOCAL_SERVER = False;
REMOTE_SERVER_ADDRESS = "tcp://ics1-bilby-test.nbi.ansto.gov.au:5555"
REMOTE_PUBLISHER_ADDRESS = "tcp://ics1-bilby-test.nbi.ansto.gov.au:5556"
REMOTE_SERVER_ADDRESS = "tcp://137.157.204.8:5555"
REMOTE_PUBLISHER_ADDRESS = "tcp://137.157.204.8:5566"
REMOTE_SERVER_ADDRESS = "tcp://ics3-build.nbi.ansto.gov.au:5555"
REMOTE_PUBLISHER_ADDRESS = "tcp://ics3-build.nbi.ansto.gov.au:5566"
LOCAL_SERVER_ADDRESS = "tcp://localhost:5555"
LOCAL_PUBLISHER_ADDRESS = "tcp://localhost:5566"
VALIDATOR_SERVER_ADDRESS = "tcp://localhost:5577";
VALIDATOR_PUBLISHER_ADDRESS = "tcp://localhost:5588";

TEST_SCRIPT = """
s1=control.get_controller('s1')
for i in xrange(3):
    s1.drive(i)
"""

if USE_LOCAL_SERVER :
    SicsManager.getSicsProxy(LOCAL_SERVER_ADDRESS, LOCAL_PUBLISHER_ADDRESS)
else:
    SicsManager.getSicsProxy(REMOTE_SERVER_ADDRESS, REMOTE_PUBLISHER_ADDRESS)
# SicsManager.getValidatorProxy(VALIDATOR_SERVER_ADDRESS, VALIDATOR_PUBLISHER_ADDRESS)
            
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
        
    def test_01_initialisation(self):
        self.assertNotEqual(self.proxy, None, "proxy not null")
        self.assertNotEqual(self.model, None, "model not null")

    def test_02_get_controller(self):
        controller = control.get_controller('att')
        self.assertNotEqual(controller, None, "controller not null")

    def test_03_get_value(self):
        control.drive('att', 10)
        val = control.get_value('att')
        self.assertTrue(abs(val - 10) < 0.1, "att at 10")
            
    def test_04_send_command(self):
        res = control.send_command('status')
        self.assertEqual("status = Eager to execute commands", res, "status is EAGER TO EXECUTE")
        
    def test_05_set_value(self):
        name = 'gumtree_time_estimate'
        val = '100'
        control.set_value(name, val)
        self.assertEqual(control.get_value(name), val, 'assert gumtree_time_estimate value')
        
    def test_06_run(self):
        m = 'att'
        pos = random.uniform(20, 21)
        control.run(m, pos)
        cs = control.get_status().getText()
        time.sleep(0.2)
        self.assertEqual("DRIVING", cs, "test status is DRIVING, got " + cs)
        control.drive('dummy_motor', 0)
        self.assertTrue(abs(pos - control.get_value(m)) < 0.1, m + " at " + str(pos))
        
    def test_07_drive(self):
        m = 'att'
        pos = random.uniform(20, 21)
        control.drive(m, pos)
        self.assertTrue(abs(pos - control.get_value(m)) < 0.01, m + " at " + str(pos))
        
    def test_08_get_raw_value(self):
        val = control.get_raw_value('dummy_motor')
        self.assertEqual(0., val, "dummy_motor at 0")
        
    def test_09_drive_interrupt(self):
        m = 'att'
        pos = random.uniform(40, 41)
        control.run(m, pos)
        self.proxy.syncRun("INT1712 3")
        time.sleep(0.2)
        self.assertTrue(control.get_proxy().isInterrupted(), 'test if interrupted, got False')
#         except Exception as e:
#             self.assertTrue(isinstance(e, SicsInterruptException), "expecting interrupt exception")
    
    def test_10_callback(self):
        cb = Callback()
        self.proxy.syncRun("drive dummy_motor 0", cb)
        print('call back status ' + str(cb.replyReceived))
        self.assertTrue(cb.replyReceived, "reply received got " + str(cb.replyReceived))
        self.assertTrue(cb.isFinished, "finish received")
        self.assertTrue(not cb.isError, "no error")

    def test_21_run_scan(self):
        control.runscan('att', 20, 22, 3, 'time', 5)
        
        
#     def test_11_run_script(self):
#         exec(TEST_SCRIPT)
#         self.assertEqual(control.get_value('s1'), 2., 'assert s1 value to be 2')
        
    def runTest(self):
        print ('call runTest()')

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