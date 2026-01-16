from org.gumtree.control.core import SicsManager as manager
from org.gumtree.control.core import ServerStatus
from org.gumtree.control.model.PropertyConstants import ControllerState
from org.gumtree.control.events import ISicsControllerListener, ISicsCallback
from org.gumtree.control.model import SicsModelUtils
from org.gumtree.control.events import SicsCallbackAdapter
from org.gumtree.control.batch import SicsMessageAdapter as MessageAdapter
from org.gumtree.control.events import SicsControllerAdapter as ControllerAdapter
from org.gumtree.control.events import SicsProxyListenerAdapter as ProxyAdapter
from gumpy.commons import logger
import os
from datetime import datetime, timedelta
import time

SICS_PROXY = manager.getSicsProxy()
# SICS_MODEL = manager.getSicsModel()

_enable_node = '/OUTPUT_STAGE_ENABLE'
# VALIDATOR_PROXY = manager.getValidatorProxy()
# VALIDATOR_MODEL = VALIDATOR_PROXY.getSicsModel()

proxy = SICS_PROXY
# model = SICS_MODEL

def is_connected():
    return proxy.isConnected()

def get_model():
#     if proxy.isConnected():
#         return proxy.getSicsModel()
#     else:
#         return None
    return proxy.getSicsModel()
    
# if '__IS_VALIDATION_MODE__' in globals():
#     print 'in globals'
#     if __IS_VALIDATION_MODE__ :
#         print 'is validation'
#         proxy = VALIDATOR_PROXY
#         model = VALIDATOR_MODEL
# else:
#     print 'not in globals'

def print_reply(obj):
    logger.log(obj.getString())
    
def print_error(obj):
    logger.log(obj.getSting())

class SicsCallback(SicsCallbackAdapter):
    
    def receiveReply(self, obj):
        print_reply(obj)
        
    def receiveError(self, obj):
        print_error('Error: ' + str(obj))
    
_callback = SicsCallbackAdapter()


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
#     if c is None :
#         raise NameError('controller not found: ' + str(id_or_path))
    return c

def sleep(secs, dt=0.1):
    target = datetime.now() + timedelta(seconds=secs)

    while True:
        if target < datetime.now():
            break
        else:
            handle_interrupt()
            time.sleep(dt)

    handle_interrupt()
    
def async_command(command, reset_intt = True, callback = _callback):
    if reset_intt:
        clear_interrupt()
    proxy.asyncRun(command, callback)
    
def send_command(command, reset_intt = True, callback = _callback):
    if reset_intt:
        clear_interrupt()
    return proxy.syncRun(command, callback)
    
# Asynchronously execute any (adhoc) SICS command (without feedback)
def execute(command, callback = _callback):
    ret = send_command(command, callback = callback)
#     handle_interrupt()
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
    clear_interrupt()
    controller = get_controller(deviceId)
    if (controller == None):
        raise SicsError('Device / Path ' + deviceId + ' not found')
    controller.setTarget(value)
    controller.run()
#     handle_interrupt()
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
    clear_interrupt()
    controller = get_controller(deviceId)
    if (controller == None):
        raise SicsError('Device / Path ' + deviceId + ' not found')
    controller.setTarget(value)
    controller.drive()
    handle_interrupt()
    logger.log("drive " + controller.getPath() + " OK")

# Synchronously drive a number of devices to a given value
# Usage: multiDrive({'my':-10.0, 'mx':-5.0})
def multi_drive(entries):
    clear_interrupt()
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
    if (controller == None):
        raise SicsError('Model path /commands/scan/bmonscan not found')
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
    if (controller == None):
        raise SicsError('Model path /commands/scan/hmscan not found')
    p = dict()
    p['scan_variable'] = scan_variable
    p['scan_start'] = scan_start
    p['scan_increment'] = scan_increment
    p['NP'] = NP
    p['mode'] = mode
    p['preset'] = preset
    __run__(controller, p)

def __run__(controller, pars, step_cmd = None, save_cmd = None):
    def step_changed(old, new):
        if not step_cmd is None:
            step_cmd()
        
    def save_changed(old, new):
        try:
            if float(new) > 0 and not save_cmd is None:
                save_cmd()
        except:
            pass
            
    step_listener = __ControllerEventHandler__()
    step_listener.updateValue = step_changed
    scv = controller.getChild('feedback')
    if scv :
        scv = scv.getChild('scan_variable_value')
    if scv :
        scv.addControllerListener(step_listener)
    save_listener = __ControllerEventHandler__()
    save_listener.updateValue = save_changed
    save_controller = get_controller('/experiment/save_count')
    if save_controller:
        save_controller.addControllerListener(save_listener)
    # Run scan
    logger.log('scan started')
    clear_interrupt()
    try :
        controller.run(pars, None)
    finally:
        if scv:
            scv.removeControllerListener(step_listener)
        if save_controller:
            save_controller.removeControllerListener(save_listener)
    logger.log('scan completed')
#     handle_interrupt()
    
def runscan(scan_variable, scan_start, scan_stop, numpoints, mode, preset, datatype = 'HISTOGRAM_XY', 
            force = 'true', savetype = 'save', step_cmd = None, save_cmd = None):
    # Initialisation
    controller = get_controller('/commands/scan/runscan')
    controller = get_controller(deviceId)
    if (controller == None):
        raise SicsError('Model path /commands/scan/runscan not found')
    p = dict()
    p['scan_variable'] = scan_variable
    p['scan_start'] = scan_start
    p['scan_stop'] = scan_stop
    p['numpoints'] = numpoints
    p['mode'] = mode
    p['preset'] = preset
    p['force'] = force
    p['savetype'] = savetype
    __run__(controller, p, step_cmd, save_cmd)


def count(mode, preset):
    # Initialisation
    controller = get_controller('/commands/monitor/count')
    if (controller == None):
        raise SicsError('Model path /commands/monitor/count not found')
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
    controller = get_controller('/commands/histogram/histmem')
    if (controller == None):
        raise SicsError('Model path /commands/histogram/histmem not found')
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
    if dtype is str:
        return str(res)
    elif dtype is float:
        return float(res)
    elif dtype is int:
        return int(float(res))
    else:
        return res

def get_base_filename():
    return os.path.basename(str(get_filename()))

def get_status():
    return proxy.getServerStatus()

def get_drivables():
    arr = SicsModelUtils.getSicsDrivableIds()
    res = []
    for i in xrange(len(arr)):
        item = arr[i]
        if not item is None:
            res.append(item)
    return res

def wait_until_idle():
    if proxy.isConnected() :
        while not get_status() == ServerStatus.EAGER_TO_EXECUTE:
            sleep(0.5)
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
    if (controller == None):
        raise SicsError('Device / Path ' + device + ' not found')
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
            sleep(interval)
        handle_interrupt()
    if value_reached:
        logger.log(str(device) + ' reached value ' + str(value) + ' in ' + str(total_count) + ' seconds')
    else:
        logger.log(str(device) + ' failed to reach value ' + str(value) + ' in ' + str(total_count) + ' seconds')

def get_ms_channels(tc):
    channels = []
    for child in tc.getChildren():
        if not child.getChild('OUTPUT_STAGE_ENABLE') is None:
            channels.append(child)
    return channels 
    
def drive_ms(id, value, controller_name = 'tc1'):
    enable_ms(id, print_all = False)
    entries = dict()
    if type(id) is list or type(id) is tuple:
        for i in xrange(len(id)):
            did = id[i]
            dname = controller_name + '_' + 'MEER{0:02d}'.format(did) + '_ObjectTemp_LOOP_0_TARGET'
            if type(value) is list or type(value) is tuple:
                dval = value[i]
            else:
                dval = value
            entries[dname] = dval
        print("multi_drive " + str(entries))
        multi_drive(entries)
    else :
        did = controller_name + '_' + 'MEER{0:02d}'.format(id) + '_ObjectTemp_LOOP_0_TARGET'
        print("drive {} {}".format(did, value))
        drive(did, value)

def drive_all_ms(value, controller_name = 'tc1'):
    enable_all_ms(print_all = False)
    entries = dict()
    if type(value) is list or type(value) is tuple:
        for i in xrange(len(value)):
            dname = controller_name + '_' + 'MEER{0:02d}'.format(i + 1) + '_ObjectTemp_LOOP_0_TARGET'
            dval = value[i]
            entries[dname] = dval
        print("multi_drive " + str(entries))
        multi_drive(entries)
    else :
        tc = get_controller('/sample/' + controller_name)
        if (tc == None):
            raise SicsError('/sample/' + controller_name + ' not found')
        num = len(get_ms_channels(tc))
        for i in xrange(num):
            dname = controller_name + '_' + 'MEER{0:02d}'.format(i + 1) + '_ObjectTemp_LOOP_0_TARGET'
            entries[dname] = value
        print("multi_drive " + str(entries))
        multi_drive(entries)

def run_ms(id, value, controller_name = 'tc1'):
    enable_ms(id, print_all = False)
    if type(id) is list or type(id) is tuple:
        for i in xrange(len(id)):
            did = id[i]
            dname = controller_name + '_' + 'MEER{0:02d}'.format(did) + '_ObjectTemp_LOOP_0_TARGET'
            if type(value) is list or type(value) is tuple:
                dval = value[i]
            else:
                dval = value
            print("run " + dname + ' ' + str(dval))
            run(dname, dval)
    else :
        did = controller_name + '_' + 'MEER{0:02d}'.format(id) + '_ObjectTemp_LOOP_0_TARGET'
        print("run " + did + ' ' + str(value))
        run(did, value)

def run_all_ms(value, controller_name = 'tc1'):
    enable_all_ms(print_all = False)
    if type(value) is list or type(value) is tuple:
        for i in xrange(len(value)):
            dname = controller_name + '_' + 'MEER{0:02d}'.format(i + 1) + '_ObjectTemp_LOOP_0_TARGET'
            dval = value[i]
            print("run " + dname + ' ' + str(dval))
            run(dname, dval)
    else :
        tc = get_controller('/sample/' + controller_name)
        if (tc == None):
            raise SicsError('/sample/' + controller_name + ' not found')
        num = len(get_ms_channels(tc))
        for i in xrange(num):
            dname = controller_name + '_' + 'MEER{0:02d}'.format(i + 1) + '_ObjectTemp_LOOP_0_TARGET'
            print("run " + dname + ' ' + str(value))
            run(dname, value)

def get_ms(meer_id, controller_name = 'tc1'):
#     tc = get_controller('/sample/' + controller_name)
#     if tc is None :
#         raise Exception(controller_name + ' not found')
    dpath = '/sample/' + controller_name + '/MEER{0:02d}'.format(meer_id) + '/ObjectTemp_LOOP_0_SENSOR'
    dc = get_controller(dpath)
    if (dc == None):
        raise SicsError(dpath + ' not found')
    return dc.getValue()

def get_ms_tolerance(meer_id, controller_name = 'tc1'):
#     tc = get_controller('/sample/' + controller_name)
#     if tc is None :
#         raise Exception(controller_name + ' not found')
    dpath = '/sample/' + controller_name + '/MEER{0:02d}'.format(meer_id) + '/ObjectTemp_LOOP_0_TARGET'
    res = get_raw_value('hgetprop ' + dpath + ' tolerance')
    if res is None :
        raise Exception(dpath + ' tolerance not found')
    return res

def set_ms_tolerance(meer_id, value, controller_name = 'tc1'):
    if type(meer_id) is list or type(id) is tuple:
        for i in xrange(len(meer_id)):
            did = meer_id[i]
            dpath = '/sample/' + controller_name + '/MEER{0:02d}'.format(did) + '/ObjectTemp_LOOP_0_TARGET'
            if type(value) is list or type(value) is tuple:
                dval = value[i]
            else:
                dval = value
            cmd = "hsetprop " + dpath + ' tolerance ' + str(dval)
            print(cmd)
            async_command(cmd)
    else :
        dpath = '/sample/' + controller_name + '/MEER{0:02d}'.format(meer_id) + '/ObjectTemp_LOOP_0_TARGET'
        cmd = "hsetprop " + dpath + ' tolerance ' + str(value)
        print(cmd)
        async_command(cmd)

def set_all_ms_tolerance(value, controller_name = 'tc1'):
    if type(value) is list or type(value) is tuple:
        for i in xrange(len(value)):
            dpath = '/sample/' + controller_name + '/MEER{0:02d}'.format(i + 1) + '/ObjectTemp_LOOP_0_TARGET'
            dval = value[i]
            cmd = "hsetprop " + dpath + ' tolerance ' + str(dval)
            print(cmd)
            async_command(cmd)
    else :
        tc = get_controller('/sample/' + controller_name)
        if (tc == None):
            raise SicsError('/sample/' + controller_name + ' not found')
        num = len(get_ms_channels(tc))
        for i in xrange(num):
            dpath = '/sample/' + controller_name + '/MEER{0:02d}'.format(i + 1) + '/ObjectTemp_LOOP_0_TARGET'
            cmd = "hsetprop " + dpath + ' tolerance ' + str(value)
            print(cmd)
            async_command(cmd)
            
def enable_ms(meer_id, controller_name = 'tc1', print_all = True):
    if type(meer_id) is list or type(id) is tuple:
        for i in xrange(len(meer_id)):
            did = meer_id[i]
            ipath = '/sample/' + controller_name + '/MEER{0:02d}'.format(did) + _enable_node
            rbv = get_controller(ipath)
            if rbv == None:
                raise SicsError(ipath + ' not found')
            if rbv.getValue() <= 0 :
                dpath = '/sample/' + controller_name + '/MEER{0:02d}'.format(did) + _enable_node
                cmd = "hset " + dpath + ' 1'
                print(cmd)
                async_command(cmd)
            elif print_all :
                print('/MEER{0:02d}'.format(did) + ' already enabled')
    else :
        ipath = '/sample/' + controller_name + '/MEER{0:02d}'.format(meer_id) + _enable_node
        rbv = get_controller(ipath)
        if rbv == None:
            raise SicsError(ipath + ' not found')
        if rbv.getValue() <= 0 :
            dpath = '/sample/' + controller_name + '/MEER{0:02d}'.format(meer_id) + _enable_node
            cmd = "hset " + dpath + ' 1'
            print(cmd)
            async_command(cmd)
        elif print_all :
            print('/MEER{0:02d}'.format(meer_id) + ' already enabled')

def enable_all_ms(controller_name = 'tc1', print_all = True):
    tc = get_controller('/sample/' + controller_name)
    if tc is None :
        raise SicsError(controller_name + ' not found')
    num = len(get_ms_channels(tc))
    for i in xrange(num):
        ipath = '/sample/' + controller_name + '/MEER{0:02d}'.format(i + 1) + _enable_node
        rbv = get_controller(ipath)
        if rbv == None:
            raise SicsError(ipath + ' not found')
        if rbv.getValue() <= 0 :
            dpath = '/sample/' + controller_name + '/MEER{0:02d}'.format(i + 1) + _enable_node
            cmd = "hset " + dpath + ' 1'
            print(cmd)
            async_command(cmd)
        elif print_all :
            print('/MEER{0:02d}'.format(i + 1) + ' already enabled')
            
def disable_ms(meer_id, controller_name = 'tc1', print_all = True):
    if type(meer_id) is list or type(id) is tuple:
        for i in xrange(len(meer_id)):
            did = meer_id[i]
            ipath = '/sample/' + controller_name + '/MEER{0:02d}'.format(did) + _enable_node
            rbv = get_controller(ipath)
            if rbv == None:
                raise SicsError(ipath + ' not found')
            if rbv.getValue() >= 1 :
                dpath = '/sample/' + controller_name + '/MEER{0:02d}'.format(did) + _enable_node
                cmd = "hset " + dpath + ' 0'
                print(cmd)
                async_command(cmd)
            elif print_all :
                print('/MEER{0:02d}'.format(did) + ' already disabled')
    else :
        ipath = '/sample/' + controller_name + '/MEER{0:02d}'.format(meer_id) + _enable_node
        rbv = get_controller(ipath)
        if rbv == None:
            raise SicsError(ipath + ' not found')
        if rbv.getValue() >= 1 :
            dpath = '/sample/' + controller_name + '/MEER{0:02d}'.format(meer_id) + _enable_node
            cmd = "hset " + dpath + ' 0'
            print(cmd)
            async_command(cmd)
        elif print_all :
            print('/MEER{0:02d}'.format(meer_id) + ' already disabled')

def disable_all_ms(controller_name = 'tc1', print_all = True):
    tc = get_controller('/sample/' + controller_name)
    if tc is None :
        raise SicsError(controller_name + ' not found')
    num = len(get_ms_channels(tc))
    for i in xrange(num):
        ipath = '/sample/' + controller_name + '/MEER{0:02d}'.format(i + 1) + _enable_node
        rbv = get_controller(ipath)
        if rbv == None:
            raise SicsError(ipath + ' not found')
        if rbv.getValue() >= 1 :
            dpath = '/sample/' + controller_name + '/MEER{0:02d}'.format(i + 1) + _enable_node
            cmd = "hset " + dpath + ' 0'
            print(cmd)
            async_command(cmd)
        elif print_all :
            print('/MEER{0:02d}'.format(i + 1) + ' already disabled')
