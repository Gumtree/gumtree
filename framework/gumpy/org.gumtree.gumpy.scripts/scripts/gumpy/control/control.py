from org.gumtree.control.core import SicsManager as manager
from org.gumtree.control.core import ServerStatus
from org.gumtree.control.events import ISicsControllerListener, ISicsCallback
from gumpy.commons import logger
import os

SICS_PROXY = manager.getSicsProxy()
# SICS_MODEL = manager.getSicsModel()

# VALIDATOR_PROXY = manager.getValidatorProxy()
# VALIDATOR_MODEL = VALIDATOR_PROXY.getSicsModel()

proxy = SICS_PROXY
# model = SICS_MODEL

def is_connected():
    return proxy.isConnected()

def get_model():
    if proxy.isConnected():
        return proxy.getSicsModel()
    else:
        return None
    
# if '__IS_VALIDATION_MODE__' in globals():
#     print 'in globals'
#     if __IS_VALIDATION_MODE__ :
#         print 'is validation'
#         proxy = VALIDATOR_PROXY
#         model = VALIDATOR_MODEL
# else:
#     print 'not in globals'

def get_proxy():
    return proxy

# def set_validator(flag):
#     global proxy
#     global model
#     if flag:
#         proxy = VALIDATOR_PROXY
#         model = VALIDATOR_MODEL
#     else:
#         proxy = SICS_PROXY
#         model = SICS_MODEL

# class Controller():
#     
#     def __init__(self, jcontroller):
#         self.jcontroller = jcontroller
#         
#     def __getattr__(self, attr):
#         if hasattr(self.jcontroller, attr) :
#             return getattr(self.jcontroller, attr)
#         else :
#             raise AttributeError
    
# Get device controller from path or id
def get_controller(id_or_path):
#     jcontroller = model.findController(id_or_path)
#     if not jcontroller is None :
#         return Controller(jcontroller)
#     else :
#         return None
    c = get_model().findController(id_or_path)
    if c is None :
        raise NameError('controller not found: ' + str(id_or_path))
    return c

def send_command(command):
    return proxy.syncRun(command)
    
# Asynchronously execute any (adhoc) SICS command (without feedback)
def execute(command):
    ret = send_command(command)
    handle_interrupt()
    return ret

# Asynchronously set any device or hipadaba node to a given value
def set_value(name, value):
    controller = get_controller(name)
    if (controller == None):
        raise SicsError('Device / Path ' + name + ' not found')
    else:
        controller.setTargetValue(value)
        controller.commitTargetValue()
        logger.log('Set ' + name + ' OK')

def hset(parentController, relativePath, value):
    controller = get_model().findChildController(parentController, relativePath);
    controller.setTargetValue(value)
    controller.commitTargetValue()

def setpos(device, value, real_value):
	execute('setpos ' + device + ' ' + str(value) + ' ' + str(real_value))

def get_value(name):
    controller = get_controller(name)
    if (controller == None):
        raise SicsError('Device / Path ' + name + ' not found')
    else:
        return controller.getValue()
    
def get_filename():
    return get_value('/experiment/file_name')
    
# Asynchronously set (run) any device to a given value
def run(deviceId, value):
    controller = get_controller(deviceId)
    controller.setTarget(value)
    controller.run()
    handle_interrupt()
    logger.log("run " + controller.getPath() + " OK")

def pause(on_or_off = True):
    flag = None
    if type(on_or_off) is bool :
        flag = on_or_off
    elif type(on_or_off) is int :
        flag = on_or_off > 0
    elif type(on_or_off) is str :
        if on_or_off.lower() == 'on' :
            flag = True
        elif on_or_off.lower() == 'off' :
            flag = False
    if flag == True :
        status = get_status()
        if status != ServerStatus.COUNTING and status != ServerStatus.PAUSED :
            raise SicsError('pause is only available on COUNTING status')
        else :
            execute("pause on")
            logger.log('pause on')
    elif flag == False:
        execute("pause off")
        logger.log('pause off')
    else :
        raise SicsError('illegal argument')
    
def unpause():
    pause(False)
    
def is_idle():
    return proxy.getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE)
     
# Synchronously set (drive) any device to a given value
def drive(deviceId, value):
    controller = get_controller(deviceId)
    controller.setTarget(value)
    controller.drive()
    handle_interrupt()
    logger.log("drive " + controller.getPath() + " OK")

# Synchronously drive a number of devices to a given value
# Usage: multiDrive({'my':-10.0, 'mx':-5.0})
def multi_drive(entries):
    proxy.multiDrive(entries)
    handle_interrupt()

class __ControllerEventHandler__(ISicsControllerListener):
    
    def __init__(self):
        pass
    
    def updateState(self, oldState, newState):
        pass
        
    def updateValue(self, oldValue, newValue):
        pass
    
    def updateEnabled(self, isEnabled):
        pass
    
def bmonscan(scan_variable, scan_start, scan_increment, NP, mode, preset):
    clear_interrupt()
    controller = get_controller('/commands/scan/bmonscan')
    p = dict()
    p['scan_variable'] = scan_variable
    p['scan_start'] = scan_start
    p['scan_increment'] = scan_increment
    p['NP'] = NP
    p['mode'] = mode
    p['preset'] = preset
    __run__(controller, p)

def hmscan(scan_variable, scan_start, scan_increment, NP, mode, preset):
    clear_interrupt()
    controller = get_controller('/commands/scan/hmscan')
    p = dict()
    p['scan_variable'] = scan_variable
    p['scan_start'] = scan_start
    p['scan_increment'] = scan_increment
    p['NP'] = NP
    p['mode'] = mode
    p['preset'] = preset
    __run__(controller, p)

def __run__(controller, pars):
    def log_step(old, new):
        if float(new) >= 0:
            logger.log("scan point " + str(new))
        
    listener = __ControllerEventHandler__()
    listener.updateValue = log_step
    scv = controller.getChild('feedback')
    if scv :
        scv = scv.getChild('scan_variable_value')
    if scv :
        scv.addControllerListener(listener)
    # Run scan
    logger.log('scan started')
    try :
        controller.run(pars, None)
    finally:
        if scv:
            scv.removeControllerListener(listener)
    logger.log('scan completed')
    handle_interrupt()
    
def runscan(scan_variable, scan_start, scan_stop, numpoints, mode, preset, datatype = 'HISTOGRAM_XY', 
            force = 'true', savetype = 'save'):
    # Initialisation
    clear_interrupt()
    controller = get_controller('/commands/scan/runscan')
    p = dict()
    p['scan_variable'] = scan_variable
    p['scan_start'] = scan_start
    p['scan_stop'] = scan_stop
    p['numpoints'] = numpoints
    p['mode'] = mode
    p['preset'] = preset
    p['force'] = force
    p['savetype'] = savetype
    __run__(controller, p)


def count(mode, preset):
    # Initialisation
    clear_interrupt()
    controller = get_controller('/commands/monitor/count')
    p = dict()
    p['mode'] = mode
    p['preset'] = preset
    __run__(controller, p)

def interrupt():
    proxy.interrupt()
    logger.log("Sent SICS interrupt")

def is_interrupted():
    return proxy.isInterrupted()
    
def clear_interrupt():
    proxy.clearInterruptFlag()
    
def handle_interrupt():
    if is_interrupted():
        clear_interrupt()
        raise Exception, 'SICS interrupted!'
    
# def histmem(cmd, mode, preset):
#     clear_interrupt()
#     controller = get_controller('/commands/histogram/histmem')
#     p = dict()
#     p['cmd'] = cmd
#     p['mode'] = mode
#     p['preset'] = preset
#     __run__(controller, p)
    
def histmem(cmd, mode, preset):
    clear_interrupt()
#     execute('histmem mode {}'.format(mode))
#     execute('histmem preset {}'.format(preset))
#     execute('histmem start block')
    controller = get_controller('/commands/scan/runscan')
    p = dict()
    p['cmd'] = cmd
    p['mode'] = mode
    p['preset'] = preset
    __run__(controller, p)
    
    
class SicsError(Exception):
    def __init__(self, value):
        self.value = value
    def __str__(self):
        return repr(self.value)
    
__time_out__ = 1
class __SICS_Callback__(ISicsCallback):
     
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

# def run_command(cmd, use_full_feedback = False):
#     call_back = __SICS_Callback__(use_full_feedback)
#     proxy.send(cmd, call_back)
#     return call_back.__status__

def get_raw_value(cmd, dtype = float):
    res = execute(cmd)
    if "=" in res:
        pair = res.split("=")
        res = pair[-1].strip()
    if dtype == float :
        return float(res)
    else :
        return res

def get_base_filename():
    return os.path.basename(str(get_filename()))

def get_status():
    return proxy.getServerStatus()

def wait_until_idle():
    if proxy.isConnected() :
        while not get_status() == ServerStatus.EAGER_TO_EXECUTE:
            time.sleep(0.5)
#             cnt += 0.5
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
    controller = get_controller(device)
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
                new_val = controller.getValue()
                update_count = 0
            except:
                new_val = controller.getValue()
        else:
            new_val = controller.getValue()
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
