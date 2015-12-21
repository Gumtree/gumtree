from gumpy.lib import enum
from gumpy.commons import sics
import time
from gumpy.lib import enum
from org.gumtree.gumnix.sics.control import ServerStatus
from gumpy.commons.logger import log

def att_pos(val = None):
    if not val is None :
        sics.drive('att', val)
    return sics.get_raw_value('att')

def nguide(val1 = '', val2 = '', val3 = ''):
    return sics.get_raw_feedback('nguide ' + str(val1) + ' ' + str(val2) + ' ' + str(val3))

def sapmot(val = None):
    if not val is None:
        sics.run_command('pdrive sapmot ' + str(val))
    return sics.get_raw_value('posname sapmot', str)

def samx(val = None):
    if not val is None :
        sics.drive('samx', val)
    return sics.get_raw_value('samx') 

def samz(val = None):
    if not val is None :
        sics.drive('samz', val)
    return sics.get_raw_value('samz')

def som(val = None):
    if not val is None :
        sics.drive('som', val)
    return sics.get_raw_value('som')
 
def sample(val = None):
    sampleMap = {
           0 : 250.000,
           1 : 210.500,
           2 : 168.375, 
           3 : 126.250,
           4 : 84.125,
           5 : 42.000,
           6 : -39.700,
           7 : -81.875,
           8 : -124.000,
           9 : -166.125,
           10 : -208.250,
           11 : -250.000
           }
    if not val is None :
        if val <=0 or val > 10:
            raise Exception, 'sample number not supported, must be within 1 to 10, got ' + str(val)
        else:
            sics.drive('samx', sampleMap[round(val)])
    raw = sics.get_raw_value('samx')
    samNum = -1;
    for i in xrange(len(sampleMap)) :
        if raw > sampleMap[i] :
            if i > 0 :
                samNum = i - (raw - sampleMap[i]) / (sampleMap[i - 1] - sampleMap[i])
            break
    if samNum < 0.05 or samNum > 10.95:
        samNum = -1
    return round(samNum, 1)

def det(val = None):
    if not val is None :
        sics.run_command('dhv down')
        sics.drive('det', val)
        sics.run_command('dhv up')
    return sics.get_raw_value('det')

def curtaindet(val = None):
    if not val is None :
        sics.run_command('dhv down')
        sics.drive('curtaindet', val)
        sics.run_command('dhv up')
    return sics.get_raw_value('curtaindet')

def curtainl(val = None):
    if not val is None :
        sics.run_command('dhv down')
        sics.drive('curtainl', val)
        sics.run_command('dhv up')
    return sics.get_raw_value('curtainl')

def curtainr(val = None):
    if not val is None :
        sics.run_command('dhv down')
        sics.drive('curtainr', val)
        sics.run_command('dhv up')
    return sics.get_raw_value('curtainr')

def curtainu(val = None):
    if not val is None :
        sics.run_command('dhv down')
        sics.drive('curtainu', val)
        sics.run_command('dhv up')
    return sics.get_raw_value('curtainu')

def curtaind(val = None):
    if not val is None :
        sics.run_command('dhv down')
        sics.drive('curtaind', val)
        sics.run_command('dhv up')
    return sics.get_raw_value('curtaind')

def bs3(val):
    if not val is None:
        if val.lower() == 'in':
            sics.drive('bs3', 65)
        elif val.lower() == 'out':
            sics.drive('bs3', 0)
    cur = sics.get_raw_value('bs3')
    if cur >= 63 and cur <= 67:
        return 'IN'
    else :
        return 'OUT'
    
def bs4(val):
    if not val is None:
        if val.lower() == 'in':
            sics.drive('bs4', 65)
        elif val.lower() == 'out':
            sics.drive('bs4', 0)
    cur = sics.get_raw_value('bs4')
    if cur >= 63 and cur <= 67:
        return 'IN'
    else :
        return 'OUT'

def bs5(val):
    if not val is None:
        if val.lower() == 'in':
            sics.drive('bs5', 65)
        elif val.lower() == 'out':
            sics.drive('bs5', 0)
    cur = sics.get_raw_value('bs5')
    if cur >= 63 and cur <= 67:
        return 'IN'
    else :
        return 'OUT'
    

hmMode = enum.Enum('timer', 'monitor')
scanMode = enum.Enum('time', 'count', 'monitor', 'unlimited', 'MONITOR_1')
dataType = enum.Enum('HISTOGRAM_XYT')
saveType = enum.Enum('save', 'nosave')

# This scan rely on samx
def scan(deviceName, start, stop, numpoints, scanMode, dataType, preset, force='true', saveType=saveType.save):
    
    controllerPath = '/commands/scan/runscan'
    sicsController = sics.getSicsController()
    scanController = sicsController.findComponentController(controllerPath)
    
    sics.execute('hset ' + controllerPath + '/scan_variable ' + deviceName, 'scan')
    sics.execute('hset ' + controllerPath + '/scan_start ' + str(start), 'scan')
    sics.execute('hset ' + controllerPath + '/scan_stop ' + str(stop), 'scan')
    sics.execute('hset ' + controllerPath + '/numpoints ' + str(numpoints), 'scan')
    if (scanMode.key == 'monitor'):
        sics.execute('hset ' + controllerPath + '/mode MONITOR_1', 'scan')
    else:
        sics.execute('hset ' + controllerPath + '/mode ' + scanMode.key, 'scan')
    sics.execute('hset ' + controllerPath + '/preset ' + str(preset), 'scan')
    sics.execute('hset ' + controllerPath + '/datatype ' + dataType.key, 'scan')
    sics.execute('hset ' + controllerPath + '/savetype ' + saveType.key, 'scan')
    sics.execute('hset ' + controllerPath + '/force ' + force, 'scan')

    # repeat until successful
    while True:

        time.sleep(1)
        while not sics.getSicsController().getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE):
            time.sleep(0.1)
            
        scanController.syncExecute()
        break

    # Get output filename
    filenameController = sicsController.findDeviceController('datafilename')
    savedFilename = filenameController.getValue().getStringData()
    log('Saved to ' +  savedFilename)
    return savedFilename

def count(mode, dataType, preset, force='true', saveType=saveType.save):
    
    controllerPath = '/commands/scan/runscan'
    sicsController = sics.getSicsController()
    scanController = sicsController.findComponentController(controllerPath)
    
    sics.execute('hset ' + controllerPath + '/scan_variable ' + deviceName, 'scan')
    sics.execute('hset ' + controllerPath + '/scan_start 0', 'scan')
    sics.execute('hset ' + controllerPath + '/scan_stop 0', 'scan')
    sics.execute('hset ' + controllerPath + '/numpoints 1', 'scan')
    if (scanMode.key == 'monitor'):
        sics.execute('hset ' + controllerPath + '/mode MONITOR_1', 'scan')
    else:
        sics.execute('hset ' + controllerPath + '/mode ' + scanMode.key, 'scan')
    sics.execute('hset ' + controllerPath + '/preset ' + str(preset), 'scan')
    sics.execute('hset ' + controllerPath + '/datatype ' + dataType.key, 'scan')
    sics.execute('hset ' + controllerPath + '/savetype ' + saveType.key, 'scan')
    sics.execute('hset ' + controllerPath + '/force ' + force, 'scan')

    # repeat until successful
    while True:

        time.sleep(1)
        while not sics.getSicsController().getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE):
            time.sleep(0.1)
            
        scanController.syncExecute()
        break

    # Get output filename
    filenameController = sicsController.findDeviceController('datafilename')
    savedFilename = filenameController.getValue().getStringData()
    log('Saved to ' +  savedFilename)
    return savedFilename

def scan10(sample_position, collect_time, sample_name = None):
    if sample_position < 1 or sample_position > 10:
        raise Exception, 'Invalid sample position, scan not run. Choose a position between 1 and 10 inclusive.'
    else:
        if not sample_name is None:
            sics.execute('samplename ' + str(sample_name))
        cur_samx = samx()
        time.sleep(1)
        log("Collection time set to " + str(collect_time) + " seconds")
#        sics.execute('histmem mode time')
#        sics.execute('histmem preset ' +  str(collect_time))
#        time.sleep(1)
        log("Data collection for sample " + sample_name + " started")
#        sics.execute("histmem start")
#        sicsController = sics.getSicsController()
#        while not sicsController.getServerStatus().equals(ServerStatus.COUNTING):
#                time.sleep(0.3)
#        while not sicsController.getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE):
#                time.sleep(0.3)
#        time.sleep(1)
#        sics.execute('newfile HISTOGRAM_XYT')
#        time.sleep(1)
#        log("Saving data")
#        sics.execute('save')
        scan('samx', cur_samx, cur_samx, 1, scanMode.time, dataType.HISTOGRAM_XYT, collect_time)
        time.sleep(2)
        log(sics.get_base_filename() + ' updated')
        log("Scan completed")
        if not sample_name is None:
            sics.execute('samplename {}')
