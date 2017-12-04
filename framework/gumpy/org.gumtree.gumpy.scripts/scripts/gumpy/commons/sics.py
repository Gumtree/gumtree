import time
import os
import sys
import traceback
from org.gumtree.gumnix.sics.core import SicsCore
from org.gumtree.gumnix.sics.control.controllers import ComponentData
from org.gumtree.gumnix.sics.control.controllers import CommandStatus
from org.gumtree.gumnix.sics.io import SicsCallbackAdapter
from org.gumtree.gumnix.sics.control import ServerStatus
from org.gumtree.gumnix.sics.control import MultiDrivableRunner
from org.gumtree.gumnix.sics.io import SicsExecutionException
from gumpy.commons import logger

# Get SICS controller
def getSicsController():
    return SicsCore.getSicsController()

# Get device controller from path or id
def getDeviceController(deviceId):
    sicsController = getSicsController()
    if (deviceId[0] == '/'):
        # Set hipadaba
        controller = sicsController.findComponentController(deviceId)
    else:
        # Set device
        controller = sicsController.findDeviceController(deviceId)
    return controller

def send_command(command, channel_id = 'general'):
    SicsCore.getDefaultProxy().send(command, None, channel_id)
    
# Asynchronously execute any (adhoc) SICS command (without feedback)
def execute(command, channel_id = 'general'):
    send_command(command, channel_id)
    handleInterrupt()

# Asynchronously set any device or hipadaba node to a given value
def set(name, value):
    controller = getDeviceController(name)
    if (controller == None):
        raise SicsError('Device / Path ' + name + ' not found')
    else:
        controller.setTargetValue(ComponentData.createData(value))
        controller.commitTargetValue(None)
        logger.log('Set ' + name + ' OK')

def setpos(device, value, real_value):
	execute('setpos ' + device + ' ' + str(value) + ' ' + str(real_value))

def getValue(name, refresh = False):
    controller = getDeviceController(name)
    if (controller == None):
        raise SicsError('Device / Path ' + name + ' not found')
    else:
        return controller.getValue(refresh)
    
def getFilename():
    fn = None
    timeout = 5
    count = 0
    while fn is None and count < timeout :
        try:
            fn = getValue('/experiment/file_name', True)
        except SicsError:
            raise
        except:
            time.sleep(0.5)
            count += 0.5
    if fn is None:
        fn = getValue('/experiment/file_name', False)
    return fn
    
# Asynchronously set any hipadaba node to a given value
def hset(parentController, relativePath, value):
    sicsController = getSicsController()
    controller = sicsController.findComponentController(parentController, relativePath);
    controller.setTargetValue(ComponentData.createData(value))
    controller.commitTargetValue(None)

# Asynchronously set (run) any device to a given value
def run(deviceId, value):
    sicsController = getSicsController()
    device = sicsController.findDeviceController(deviceId)
    device.setTargetValue(ComponentData.createData(value))
    device.commitTargetValue(None)
    handleInterrupt()
    logger.log("Run " + device.getPath() + " OK")

def isIdle():
    return getSicsController().getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE)
     
# Synchronously set (drive) any device to a given value
def drive(deviceId, value):
    sicsController = getSicsController()
    controller = getDeviceController(deviceId)
#    controller.drive(float(value))
    cnt = 0
    while cnt < 20:
        try:
            try:
                c_val = controller.getValue().getFloatData()
                tolerance = controller.getChildController('/precision').getValue().getFloatData()
#                 logger.log('current value is ' + str(c_val))
#                 logger.log('precision is ' + str(tolerance))
                if abs(value - c_val) <= tolerance :
                    logger.log(str(deviceId) + ' is already at ' + str(c_val))
                    return
            except:
                pass
            controller.drive(float(value))
            break
        except SicsExecutionException, e:
            em = str(e.getMessage())
#            if em.__contains__('Interrupted'):
#                raise e
            if not em.lower().__contains__('time out'):
                raise e
            handleInterrupt()
            logger.log('retry driving ' + str(deviceId))
            time.sleep(1)
            wait_until_idle()
            cnt += 1
    if cnt >= 20:
        is_done = False
        try:
            cval = controller.getValue(True)
            if abs(float(value) - cval.getFloatData()) <= controller.getChildController("/precision").getValue().getFloatData():
                is_done = True;
        except:
            pass
        if not is_done:
            raise Exception, 'timeout to drive ' + str(deviceId) + ' to ' + str(value)
    wait_until_idle()
    handleInterrupt()

# Synchronously drive a number of devices to a given value
# Usage: multiDrive({'my':-10.0, 'mx':-5.0})
def multiDrive(entries):
    runner = MultiDrivableRunner()
    for key in entries:
        drivable = getSicsController().findDeviceController(key)
        runner.addDrivable(drivable, entries[key])
    runner.drive()
    wait_until_idle()
    handleInterrupt()

def runbmonscan(scan_variable, scan_start, scan_increment, NP, mode, preset, channel):
    runscan('bmonscan', scan_variable, scan_start, scan_increment, NP, mode, preset, channel)

def runhmscan(scan_variable, scan_start, scan_increment, NP, mode, preset, channel):
    runscan('hmscan', scan_variable, scan_start, scan_increment, NP, mode, preset, channel)

def runscan(type, scan_variable, scan_start, scan_increment, NP, mode, preset, channel):
    # Initialisation
    clearInterrupt()
    sicsController = getSicsController()
    scanController = sicsController.findComponentController('/commands/scan/' + type)
    hset(scanController, '/scan_variable', scan_variable)
    hset(scanController, '/scan_start', scan_start)
    hset(scanController, '/scan_increment', scan_increment)
    hset(scanController, '/NP', NP)
    hset(scanController, '/mode', mode)
    hset(scanController, '/preset', preset)
    hset(scanController, '/channel', channel)
    
    # Monitor status
    while(scanController.getCommandStatus().equals(CommandStatus.BUSY)):
        # Don't do anything before scan is ready
        time.sleep(0.1)
    
    # Run scan
    logger.log('Scan started')
    scanController.asyncExecute()
    
    # Monitor initial status change
    timeOut = False
    counter = 0;
    while(scanController.getStatusDirtyFlag() == False):
        time.sleep(0.1)
        counter += 0.1
        if (counter >= 1):
            timeOut = True
            logger.log('Time out on running scan')
            break
            
    # Enter into normal sequence
    if (timeOut == False):
        scanpoint = -1;
        scanPointController = sicsController.findComponentController(scanController, '/feedback/scanpoint')
        countsController = sicsController.findComponentController(scanController, '/feedback/counts')
        logger.log('  NP  ' + '\t' + ' Counts')
        while (scanController.getCommandStatus().equals(CommandStatus.BUSY)):
            currentPoint = scanPointController.getValue().getIntData()
            if ((scanpoint == -1 and  currentPoint == 0) or (scanpoint != -1 and currentPoint != scanpoint)):
                scanpoint = currentPoint
                logger.log('%4d \t %d' % (scanpoint, countsController.getValue().getIntData()))
            time.sleep(0.1)
        logger.log('Scan completed')
    handleInterrupt()

def count(mode, preset):
    # Initialisation
    sicsController = SicsCore.getSicsController()
    countController = sicsController.findComponentController('/commands/monitor/count')
    hset(countController, '/mode', mode)
    hset(countController, '/preset', preset)
    
    # Monitor status
    while(countController.getCommandStatus().equals(CommandStatus.BUSY)):
        # Don't do anything before counter is ready
        time.sleep(0.1)
        
    # Run scan
    logger.log('Count started')
    countController.asyncExecute()
    
    # Monitor initial status change
    timeOut = False
    counter = 0
    while(countController.getStatusDirtyFlag() == False):
        time.sleep(0.1)
        counter += 0.1
        if (counter >= 1):
            timeOut = True
            logger.log('Time out on running count')
            break
            
    # Enter into normal sequence
    if (timeOut == False):
        while (countController.getCommandStatus().equals(CommandStatus.BUSY)):
            time.sleep(0.1)
    handleInterrupt()
    logger.log('Count completed')

def interrupt(channel = None):
    if channel == None:
        SicsCore.getSicsController().interrupt()
    else:
        SicsCore.getDefaultProxy().send('INT1712 3', None, channel)
    logger.log("Sent SICS interrupt")

def isInterrupt():
    return SicsCore.getSicsController().isInterrupted()
    
def clearInterrupt():
    SicsCore.getSicsController().clearInterrupt()
    
def handleInterrupt():
    if isInterrupt():
        clearInterrupt()
        raise Exception, 'SICS interrupted!'
    
def histmem(cmd, mode, preset):
    sicsController = SicsCore.getSicsController()
    histmemController = sicsController.findComponentController('/commands/histogram/histmem')
    hset(histmemController, '/cmd', cmd)
    hset(histmemController, '/mode', mode)
    hset(histmemController, '/preset', preset)
    # give time for hset to finish
    time.sleep(0.2)
    # start histmem command
    logger.log(cmd + ' histogram acquisition') 
    histmemController.asyncExecute()
    
    # Monitor initial status change
    timeOut = False
    counter = 0;
    while(histmemController.getStatusDirtyFlag() == False):
        time.sleep(0.1)
        counter += 0.1
        if (counter >= 1):
            timeOut = True
            logger.log('Time out on running count')
            break
            
    # Enter into normal sequence
    if (timeOut == False):
        while (histmemController.getCommandStatus().equals(CommandStatus.BUSY) or histmemController.getCommandStatus().equals(CommandStatus.STARTING)):
            time.sleep(0.1)
    handleInterrupt()
    logger.log('Count completed')
    
    
class SicsError(Exception):
    def __init__(self, value):
        self.value = value
    def __str__(self):
        return repr(self.value)
    
__time_out__ = 1
class __SICS_Callback__(SicsCallbackAdapter):
    
    def __init__(self, use_full_feedback = False):
        self.__status__ = None
        self.__use_full_feedback__ = use_full_feedback
    
    def receiveError(self, data):
        self.__status__ = data.getString()
        self.setCallbackCompleted(True)
    
    def receiveFinish(self, data):
        self.__status__ = data.getString()
        self.setCallbackCompleted(True)
        
    def receiveReply(self, data):
        try:
            rt = data.getString()
            if self.__use_full_feedback__:
                status = rt
            else :
                if rt.__contains__('='):
                    status = data.getString().split("=")[1].strip()
                elif rt.__contains__(':'):
                    status = data.getString().split(":")[1].strip()
                    if status.__contains__('}'):
                        status = status[:status.index('}')]
                else :
                    status = rt
            self.__status__ = status
            self.setCallbackCompleted(True)
        except:
            self.__status__ = data
            traceback.print_exc(file = sys.stdout)
            self.setCallbackCompleted(True)

def run_command(cmd, use_full_feedback = False):
    call_back = __SICS_Callback__(use_full_feedback)
    SicsCore.getDefaultProxy().send(cmd, call_back)
    acc_time = 0
#    while call_back.__status__ is None and acc_time < 20:
    while call_back.__status__ is None:
        time.sleep(0.2)
        acc_time += 0.2
    if call_back.__status__ is None:
        raise Exception, 'time out in running the command'
    return call_back.__status__

def run_command_timeout(cmd, use_full_feedback = False, timeout = None):
    call_back = __SICS_Callback__(use_full_feedback)
    SicsCore.getDefaultProxy().send(cmd, call_back)
    acc_time = 0
    while call_back.__status__ is None and (timeout is None or acc_time < timeout) :
#    while call_back.__status__ is None:
        time.sleep(0.2)
        acc_time += 0.2
    if call_back.__status__ is None:
        raise Exception, 'time out in running the command'
    return call_back.__status__

def get_raw_value(comm, dtype = float):
    global __time_out__
    __count__ = 0
    comm_str = str(comm)
    while __count__ < __time_out__:
        try:
            item = run_command(comm_str)
            if dtype is str:
                return str(item)
            elif dtype is float:
                return float(item)
            elif dtype is int:
                return int(float(item))
            else:
                return item
        except:
            __count__ += 0.2
            time.sleep(0.2)
    logger.log('time out in running ' + comm_str)
    return None

def get_raw_feedback(comm):
    global __time_out__
    __count__ = 0
    comm_str = str(comm)
    while __count__ < __time_out__:
        try:
            item = run_command(comm_str, True)
            return str(item)
        except:
            __count__ += 0.2
            time.sleep(0.2)
    logger.log('time out in running ' + comm_str)
    return None

def get_base_filename():
    return os.path.basename(str(getFilename()))

def get_stable_value(dev):
    val = None
    while (True):
        controller = getDeviceController(dev)
        new_val = controller.getValue(True).getFloatData()
        if new_val == val :
            return getValue(dev)
        else:
            val = new_val
            time.sleep(1)
        
def getStatus():
    controller = getSicsController()
    if not controller is None:
        t = controller.getServerStatus().getText()
        if t == "UNKNOW":
            return "UNKNOWN"
        else:
            return t
    else:
        return "UNKNOWN"

def get_status():
    controller = getSicsController()
    if not controller is None:
        controller.refreshServerStatus()
        return controller.getServerStatus()
    else:
        return ServerStatus.UNKNOWN

def wait_until_idle():
    controller = getSicsController()
    if not controller is None:
        cnt = 0
        while not controller.getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE):
            time.sleep(0.5)
            cnt += 0.5
            if cnt >= 5:
                controller.refreshServerStatus()
                cnt = 0
    else:
        raise Exception, 'disconnected'
        
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
    value_reached = False
    controller = getDeviceController(device)
    logger.log('start waiting for ' + str(device) + ' to reach ' + str(value))
    if precision is None :
        precision = 0.01
    if interval is None or interval <= 0 :
        interval = 0.2
    if timeout_if_not_change == 0 or timeout_if_not_change is None :
        timeout_if_not_change = float('nan')
    old_val = float('nan')
    update_interval = 5
    update_count = 0
    total_count = 0
    not_change_count = 0
    while not value_reached:
        if update_count >= update_interval :
            try :
                new_val = controller.getValue(True).getFloatData()
                update_count = 0
            except:
                new_val = controller.getValue(False).getFloatData()
        else:
            new_val = controller.getValue(False).getFloatData()
        if abs(new_val - value) <= precision :
            value_reached = True
            break
        else:
            if not_change_count > timeout_if_not_change:
                break
            if abs(new_val - old_val) <= precision:
                not_change_count += interval
            else:
                not_change_count = 0
            old_val = new_val
            update_count += interval
            total_count += interval
            time.sleep(interval)
    if value_reached:
        logger.log(str(device) + ' reached value ' + str(value) + ' in ' + str(total_count) + ' seconds')
    else:
        logger.log(str(device) + ' failed to reach value ' + str(value) + ' in ' + str(total_count) + ' seconds')
