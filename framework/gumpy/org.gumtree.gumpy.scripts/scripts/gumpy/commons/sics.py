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

# Asynchronously execute any (adhoc) SICS command (without feedback)
def execute(command, channel_id = 'general'):
    SicsCore.getDefaultProxy().send(command, None, channel_id)
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

def getValue(name):
    controller = getDeviceController(name)
    if (controller == None):
        raise SicsError('Device / Path ' + name + ' not found')
    else:
        return controller.getValue()
    
def getFilename():
    return getValue('/experiment/file_name')
    
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
            controller.drive(float(value))
            break
        except SicsExecutionException, e:
            em = str(e.getMessage())
#            if em.__contains__('Interrupted'):
#                raise e
            if not em.lower().__contains__('time out'):
                raise e
            logger.log('retry driving ' + str(deviceId))
            time.sleep(1)
            while not getSicsController().getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE):
                time.sleep(0.3)
            cnt += 1
    if cnt >= 20:
        raise Exception, 'timeout to drive ' + str(deviceId) + ' to ' + str(value)
    while not getSicsController().getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE):
                time.sleep(0.3)
    handleInterrupt()

# Synchronously drive a number of devices to a given value
# Usage: multiDrive({'my':-10.0, 'mx':-5.0})
def multiDrive(entries):
    runner = MultiDrivableRunner()
    for key in entries:
        drivable = getSicsController().findDeviceController(key)
        runner.addDrivable(drivable, entries[key])
    runner.drive()
    while not getSicsController().getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE):
                time.sleep(0.3)
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