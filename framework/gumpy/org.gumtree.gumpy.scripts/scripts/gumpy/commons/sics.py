import time
import os
import sys
import traceback
from gumpy.commons import logger
from gumpy.control import control 
from org.gumtree.control.events import ISicsControllerListener

class ControllerListener ( ISicsControllerListener ):
        
        def updateValue(oldValue, newValue):
            pass
        
        def updateTarget(oldValue, newValue):
            pass
        
        def updateState(oldState, newState):
            pass
        
        def updateEnabled(isEnabled):
            pass

# Get SICS controller
# def getSicsController():
#     return SicsCore.getSicsController()
def is_connected():
    return control.is_connected()

def sleep(secs, dt=0.1):
    control.sleep(secs, dt)
    
def get_model():
    return control.get_model()

# Get device controller from path or id
def getDeviceController(deviceId):
    return control.get_controller(deviceId)

def send_command(command, channel_id = 'general'):
    control.send_command(command)
    
# Asynchronously execute any (adhoc) SICS command (without feedback)
def execute(command, channel_id = 'general'):
    control.execute(command)

# Asynchronously set any device or hipadaba node to a given value
def set(name, value):
    control.set_value(name, value)
    
def setpos(device, value, real_value):
	execute('setpos ' + device + ' ' + str(value) + ' ' + str(real_value))

def getValue(name, refresh = False):
    return control.get_value(name)
    
def getFilename():
    return control.get_filename()
    
# Asynchronously set any hipadaba node to a given value
def hset(parentController, relativePath, value):
    control.hset(parentController, relativePath, value)

# Asynchronously set (run) any device to a given value
def run(deviceId, value):
    control.run(deviceId, value)

def isIdle():
    return control.is_idle()
     
def drive(deviceId, value):
    control.drive(deviceId, value)

# Synchronously drive a number of devices to a given value
# Usage: multiDrive({'my':-10.0, 'mx':-5.0})
def multiDrive(entries):
    control.multi_drive(entries)
    
def runbmonscan(scan_variable, scan_start, scan_increment, NP, mode, preset, channel):
    control.bmonscan(scan_variable, scan_start, scan_increment, NP, mode, preset)

def runhmscan(scan_variable, scan_start, scan_increment, NP, mode, preset, channel):
    control.hmscan(scan_variable, scan_start, scan_increment, NP, mode, preset)

def runscan(type, scan_variable, scan_start, scan_increment, NP, mode, preset, channel = None):
    # Initialisation
    control.runscan(scan_variable, scan_start, scan_stop, numpoints, mode, preset, datatype = 'HISTOGRAM_XY', 
            force = 'true', savetype = 'save')
    
def count(mode, preset):
    # Initialisation
    control.count(mode, preset)

def interrupt(channel = None):
    control.interrupt()

def isInterrupt():
    return control.is_interrupted()
    
def clearInterrupt():
    control.clear_interrupt()
    
def handleInterrupt():
    control.handle_interrupt()
    
def histmem(cmd, mode, preset):
    control.histmem(cmd, mode, preset)
    
class SicsError(Exception):
    def __init__(self, value):
        self.value = value
    def __str__(self):
        return repr(self.value)
    
def run_command(cmd, use_full_feedback = False):
    send_command(cmd)

def run_command_timeout(cmd, use_full_feedback = False, timeout = None):
    send_command(cmd)
    
def get_raw_value(cmd, dtype = float):
    return control.get_raw_value(cmd, dtype)

def get_raw_feedback(cmd):
    return control.execute(cmd)

def get_base_filename():
    return control.get_base_filename()

def get_stable_value(dev):
    return control.get_value(dev)
        
def getStatus():
    return control.get_status().getText()

def update_status():
    pass

def get_status():
    return control.get_status()

def wait_until_idle():
    control.wait_until_idle()
        
'''
    Make the system wait until a device reaching a given value.
    
    :param device: name or path of the device, e.g., 'samx' or '/sample/samx'.
    :type device: str
    
    :param value: a float number, can't be None
    :type value: float
    
    :param precision: the precision or tolerance of the device. Once the value reaches within the 
                      tolerance, it finishes waiting. Default value is 0.01.
    :type precision: float
    
    :param timeout_if_not_change: the waiting will finish if the device value doesn't change in 
                                  this number of seconds. If set to be None, there will be no
                                  timeout. Default value is None.
    :type timeout_if_not_change: float value in seconds
    
    :param interval: the system will check the device value for every given number of seconds.
                     It can't be None or 0. Default value is 0.2 seconds.
    :type interval: float
    
    :return: a boolean value if the target value has been reached.
'''
def wait_until_value_reached(device, value, precision = 0.01, timeout_if_not_change = None, interval = 0.2):
    control.wait_until_value_reached(device, value, precision, timeout_if_not_change, interval)
