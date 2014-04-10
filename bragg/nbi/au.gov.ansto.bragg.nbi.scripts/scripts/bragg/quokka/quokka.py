###################################################################################
# Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     David Mannicke, Tony Lam (Bragg Institute) - initial API and implementation
###################################################################################

"""
Quokka module provides helper functions to control the Quokka small angle
neutron scattering instrument.
"""

from gumpy.lib.odict import OrderedDict
from gumpy.commons.logger import log
from gumpy.commons import sics
from gumpy.lib import enum
from org.gumtree.gumnix.sics.core import SicsCore
from org.gumtree.gumnix.sics.control import ServerStatus
from org.gumtree.gumnix.sics.io import SicsExecutionException
from au.gov.ansto.bragg.quokka.sics import DetectorHighVoltageController
from au.gov.ansto.bragg.quokka.sics import BeamStopController
import time
import math

# Constants
DEVICE_SAMX = 'samx'

# safe count rates
local_rateSafe =     15.0
global_rateSafe = 40000.0

# Enums
guideConfig = enum.Enum(\
    'ga', 'mt', 'lp', 'lens', \
    'p1', 'p1lp', 'p1lens', 'g1', \
    'p2', 'g2', 'p3', 'g3', 'p4', 'g4', 'p5', 'g5', \
    'p6', 'g6', 'p7', 'g7', 'p8', 'g8', 'p9', 'g9')
action = enum.Enum('up', 'down')
hmMode = enum.Enum('time', 'monitor')
scanMode = enum.Enum('time', 'monitor', 'unlimited', 'MONITOR_1')
dataType = enum.Enum('HISTOGRAM_XY')
saveType = enum.Enum('save', 'nosave')

# Global variables
# Beam stop controllers
bsList = [BeamStopController(1), \
          BeamStopController(2), \
          BeamStopController(3), \
          BeamStopController(4), \
          BeamStopController(5)]
dhv1 = DetectorHighVoltageController()
attenuationLevels = [330, 300, 270, 240, 210, 180, 150, 120, 90, 60, 30, 0]
devices = {'sampleNum' : '/sample/sampleNum'}

# Synchronous
def setSample(position, name='UNKNOWN', description='UNKNOWN', thickness=0, driveSampleStage=True):
    if driveSampleStage:
        driveSample(position)
    # Set sample name and description
    sics.set('samplename', name)
    sics.set('sampledescription', description)
    sics.set('samplethickness', thickness)

def driveSample(position):
    log('Driving sample holder to position ' + str(position) + ' ...')
    sicsController = sics.getSicsController()
    controller = sicsController.findComponentController(devices['sampleNum'])
#    controller.drive(position)
    cnt = 0
    while cnt < 20:
        try:
            controller.drive(position)
            break
        except SicsExecutionException, e:
            em = str(e.getMessage())
            if em.__contains__('Interrupted'):
                raise e
            time.sleep(0.6)
            log('retry driving sampleNum')
            time.sleep(1)
            while not sicsController.getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE):
                time.sleep(0.3)
            cnt += 1
            sics.handleInterrupt()
    if cnt >= 20:
        raise Exception, 'Time out on running sampleNum'


    log('Sample holder is in position ' + controller.getValue().getStringData())
    
def getSamplePosition():
    sicsController = sics.getSicsController()
    controller = sicsController.findComponentController(devices['sampleNum'])
    return controller.getValue().getIntData()

# Drive the sample hold to load position
def driveToLoadPosition():
    log('Driving sample holder to load position')
    # Find soft upper limit
    upperlimit = sics.getValue('/sample/sample_x/softupperlim').getFloatData()
    hardlimit = sics.getValue('/sample/sample_x/hardupperlim').getFloatData()
    softzero = sics.getValue('/sample/sample_x/softzero').getFloatData()
    if upperlimit > hardlimit - softzero:
        upperlimit = hardlimit - softzero
    # Use the soft lower limit as the load position
    loadPosition = math.floor(upperlimit)
    sics.drive('samx', loadPosition)
    
def getAttValue():
    """ Do something hard
    """
    return sics.getValue('att').getIntData()

def driveAtt(value):
    """ Do something very hard
    """
    while not sics.getSicsController().getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE):
        time.sleep(0.2)

    log('Driving attenuator to ' + str(value) + ' degree ...')
    time.sleep(0.2)
    sics.drive('att', value)
    log('Attenuator is now at ' + str(getAttValue()) + ' degree')

def setSafeAttenuation(startingAttenuation=330):
    local_rate  = 0
    global_rate = 0
    previousLocalRate  = 0
    previousGlobalRate = 0
    startLevel = attenuationLevels.index(startingAttenuation)
    
    # Hack: need to reset and run histmem
    log('set safe attenuation...')
    
    # loop from the safe range of attenuation
    for level in xrange(startLevel, len(attenuationLevels)):
        # drive the attenuator
        driveAtt(attenuationLevels[level])
        
        # count bin rate
        local_rate, global_rate = determineAveragedRates(max_samples=5, log_success=False)
        log('local rate = '  + str(local_rate))
        log('global rate = ' + str(global_rate))
        
        # Too much (check both local and global rate)
        if ((local_rate > local_rateSafe) or (global_rate > global_rateSafe)):
            if (level > 0):
                # [GUMTREE-378] Ensure SICS is ready after count
                while not sics.getSicsController().getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE):
                    time.sleep(0.1)
                # Brings it back to one step
                driveAtt(attenuationLevels[level - 1])
            local_rate  = previousLocalRate
            global_rate = previousGlobalRate
            break
        
        # Within tolerance
        elif (local_rate >= local_rateSafe / 2) or (global_rate >= global_rateSafe / 2):
            log('exit loop')
            break
        
        previousLocalRate  = local_rate
        previousGlobalRate = global_rate
        # Ensure SICS is ready after count
        while not sics.getSicsController().getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE):
            time.sleep(0.1)
    
    # Print info at the end
    log('Attenuation is set to ' + str(getAttValue()) + ' degree with bin rate ' + str(local_rate))

def getDetPosition():
    return sics.getValue('det').getIntData()

def driveDet(position, offset):
    # Tolerance: 5mm and 1mm
    precision = 5
    offsetPrecision = 1
    shouldDrive = (position > getDetPosition() + precision) or (position < getDetPosition() - precision)
    shouldDrive = (shouldDrive) or ((offset > getDetOffsetValue() + offsetPrecision) or (offset < getDetOffsetValue() - offsetPrecision))
    # Drive det only if we needed to
    if shouldDrive:
        driveDhv1(action.down)
        log('Driving detector to ' + str(position) + ' mm ...')
        while not sics.getSicsController().getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE):
            time.sleep(0.1)
        sics.drive('det', position)
        while not sics.getSicsController().getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE):
            time.sleep(0.1)
        log('Driving detector offset to ' + str(position) + ' mm ...')
        driveDetOffset(offset)
        while not sics.getSicsController().getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE):
            time.sleep(0.1)
    # Always set voltage up regardless if it is in position or not
    driveDhv1(action.up)
    log('Detector is now at ' + str(getDetPosition()) + 'mm with offset ' + str(getDetOffsetValue()) + 'mm')

def getDetOffsetValue():
    return sics.getValue('detoff').getFloatData()

def driveDetOffset(value):
    log('Driving detector offset to ' + str(value) + ' ...')
    sics.drive('detoff', value)
    log('Detector offset is now at ' + str(getDetOffsetValue()))

def getL1Value():
    return sics.getValue('l1').getFloatData()

def getL2Value():
    return sics.getValue('l2').getFloatData()

def getLambdaValue():
    try:
        # For SICS after 2011-05
        return sics.getValue('/instrument/velocity_selector/wavelength_nominal').getFloatData()
    except sics.SicsError:
        # For SICS prior to 2011-05
        return sics.getValue('/instrument/velocity_selector/Lambda').getFloatData()

def getAllBsPosition():
    i = 1
    for bs in bsList:
        log('Beamstop ' + str(i) + ' is ' + bs.getPosition().name())
        i += 1
    
def getBsPosition(id):
    return bsList[id - 1].getPosition().name()

# New beam stop commands
def selBs(beamstop):
    # Get command controller
    sicsController = sics.getSicsController()
    commandController = sicsController.findComponentController('/commands/beamstops/selbsn')
    
    # Configuring command properties
    timeout = 10
    count = 0
    sics.hset(commandController, '/bs', beamstop)
    while sics.getValue('/commands/beamstops/selbsn/bs').getIntData() != beamstop:
        time.sleep(0.1)
        count += 0.1
        if count > timeout:
            raise Exception("Time out on receiving feedback on beam stop selection")
    
    cnt = 0
    while cnt < 20:
        try:
            commandController.syncExecute();
            break
        except SicsExecutionException, e:
            em = str(e.getMessage())
            if em.__contains__('Interrupted'):
                raise e
            log('retry selecting beam stop ' + str(beamstop))
            time.sleep(1)
            while not sicsController.getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE):
                time.sleep(0.3)
            cnt += 1
    if cnt >= 20:
        raise Exception, 'timeout selecting beam stop ' + str(beamstop)
    
    log('beam stop is ' + str(beamstop))

def selBsxz(beamstop, bx, bz):
    # Get command controller
    sicsController = sics.getSicsController()
    commandController = sicsController.findComponentController('/commands/beamstops/selbsxz')
    
    # Configuring command properties
    sics.hset(commandController, '/bs', beamstop)
    sics.hset(commandController, '/bx', bx)
    sics.hset(commandController, '/bz', bz)
    
    cnt = 0
    while cnt < 20:
        try:
            commandController.syncExecute();
            break
        except SicsExecutionException, e:
            em = str(e.getMessage())
            if em.__contains__('Interrupted'):
                raise e
            log('retry selecting beam stop ' + str(beamstop))
            time.sleep(1)
            while not sicsController.getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE):
                time.sleep(0.3)
            cnt += 1
    if cnt >= 20:
        raise Exception, 'timeout selecting beam stop ' + str(beamstop)
    
    log('beam stop is ' + str(beamstop))
    
    
def driveBs(ids, action):
    # Wrap input as list
    if type(ids) != type([]):
        ids = [ids]
    # Loop thru each beam stop
    for id in ids:
        if action.key == 'up':
            log('Driving bs' + str(id) + ' up ...')
            bsList[id - 1].up()
            log('bs' + str(id) + ' is now in ' + getBsPosition(id))
        elif action.key == 'down':
            log('Driving bs' + str(id) + ' down ...')
            bsList[id - 1].down()
            log('bs' + str(id) + ' is now in ' + getBsPosition(id))
        else:
            log('Cannot driving bs' + str(id) + ' to ' + str(action))

def driveBsx(baseValue, offset):
    log('Driving beamstop x to ' + str(baseValue + offset) + 'mm ...')
    sics.drive('bsx', baseValue + offset)
    log('Beamstop x is now at ' + str(getBsxValue()) + 'mm')
    
def getBsxValue():
    return sics.getValue('bsx').getFloatData()
    
def driveBsz(value):
    log('Driving beamstop z to ' + str(value) + 'mm ...')
    sics.drive('bsz', value)
    log('Beamstop z is now at ' + str(getBszValue()) + 'mm')

def getBszValue():
    return sics.getValue('bsz').getFloatData()
    
def getBsRailPosition():
    return sics.getValue('bsx').getIntData()

def driveBsRailIn():
    value = -64
    log('Driving beamstop rail in to ' + str(value) + '  ...')
    sics.drive('bsx', value)
    log('Beamstop rail is now at ' + str(getBsRailPosition()))
    
def driveBsRailOut():
    value = -64 + 100
    log('Driving beamstop rail out to ' + str(value) + '  ...')
    sics.drive('bsx', value)
    log('Beamstop rail is now at ' + str(getBsRailPosition()))

def getGuideConfig():
    return sics.getValue('/commands/optics/guide/configuration').getStringData()

def driveGuide(guideConfig):
    # Set configuration
    sics.set('/commands/optics/guide/configuration', guideConfig.key)
    log('Moving guide to ' + guideConfig.key)
    sicsController = SicsCore.getSicsController()
    commandController = sicsController.findComponentController('/commands/optics/guide')
    # Setting of configuration and starting a command are committed to SICS via different communication channels
    # In order to make those in sync, we need to wait for the configuration to be settled.
    time.sleep(0.1)
    
    # Start command now
    cnt = 0
    while cnt < 20:
        try:
            commandController.syncExecute();
            break
        except SicsExecutionException, e:
            em = str(e.getMessage())
            if em.__contains__('Interrupted'):
                raise e
            log('retry moving guide to ' + str(guideConfig.key))
            time.sleep(1)
            while not sicsController.getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE):
                time.sleep(0.3)
            cnt += 1
    if cnt >= 20:
        raise Exception, 'timeout moving guide to ' + str(guideConfig.key)
    log('Guide is moved to ' + getGuideConfig())

def getDhv1Value():
    return dhv1.getValue()

def driveDhv1(action):
    # Don't drive if it is already in position
    startingValue = getDhv1Value()
    if (action.key == 'up' and startingValue >= 2350.0) or (action.key == 'down' and startingValue == 0.0):
        log('dhv1 is now at ' + str(startingValue) + ', no action is required')
        return
    
    # Test drive
    log('Test drive dhv1 to ' + action.key + ' ...')
    sics.getSicsController().clearInterrupt()
    sics.execute('dhv1 ' + action.key)
    # Wait for 6 sec
    time.sleep(6)
    hasInterrupted = sics.getSicsController().isInterrupted() == 1
    # Stop test drive
    log('Stopping test drive')
    sics.execute('INT1712 3')
    time.sleep(1)
    currentValue = getDhv1Value()
    # Don't go any further if someone has interrrupted the test drive
    if hasInterrupted:
#        log('Test drive was interrupted')
        raise Exception
#    print ('currentValue: ' + str(currentValue) + ', startingValue: ' +  str(startingValue));
    # Test if the current value is within the precision (more than half of voltage step size, which is about half of 30V)
    precision = 20
    if (startingValue + precision >= currentValue) and (startingValue - precision <= currentValue):
        log('Dhv1 needs to be reset')
        sics.execute('dhv1 reset')
        time.sleep(1)
    
    # Actual drive
    sics.getSicsController().clearInterrupt()
    log('Driving dhv1 to ' + action.key + ' ...')
    if action.key == 'up':
        dhv1.up()
    elif action.key == 'down':
        dhv1.down()
    elif action.key == 'reset':
        dhv1.reset()
    log('dhv1 is now at ' + str(getDhv1Value()))

def getEntRotApValue():
    return sics.getValue('srce').getIntData()

def driveEntRotAp(value):
    log('Driving entrance aperture to ' + str(value) + ' degree ...')
    sics.drive('srce', value)
    log('Entrance aperture is now at ' + str(getEntRotApValue()) + ' degree')

def getJulaboValue():
    return sics.getValue('/sample/tc1').getFloatData()

def driveJulabo(value):
    tc1 = sics.getSicsController().findComponentController('/sample/tc1')
    tc1.drive(float(value))

def driveHistmem(hmMode, preset):
    sicsController = sics.getSicsController()
    histmemController = sicsController.findComponentController('/commands/histogram/histmem')
    sics.hset(histmemController, '/mode', hmMode.key)
    sics.hset(histmemController, '/preset', preset)
    sics.hset(histmemController, '/cmd', 'start')
    log('Start histmem ...')
    histmemController.syncExecute()
    time.sleep(0.8)
    log('Histmem stopped')

def driveFlipper(value):
    # Clear interrupt flag
    sics.getSicsController().clearInterrupt()
    # Set timeout to 120 sec
    timeout = 120
    counter = 0
    # Set flipper
    sics.set('/instrument/flipper/set_flip_on', value)
    log('Driving flipper to ' + str(value) + "...")
    while True:
        flipperValue = sics.getValue('/instrument/flipper/flip_on').getIntData()
        # Test if value is set 
        if flipperValue == value:
            log('Flipper is set to ' + str(flipperValue))
            return
        else:
            # Check timeout
            if counter >= timeout:
                raise Exception('Failed to set flipper to ' + str(value))
            # Check interrupt
            if sics.getSicsController().isInterrupted() == 1:
                raise Exception('SICS has been interrupted')
            # Otherwise sleep for 1 sec
            counter += 1
            time.sleep(1)

# Gets the maximum pixel count rate 
def getMaxBinRate():
    return sics.getValue('/instrument/detector/max_binrate').getFloatData()

# Gets the global count rate
def getGlobalMapRate():
    return sics.getValue('/instrument/detector/total_maprate').getFloatData()

def getSampleHolderPosition():
    sicsController = SicsCore.getSicsController()
    samx = sicsController.findDeviceController(DEVICE_SAMX)
    return samx.getValue().getFloatData()

# This scan rely on samx
def scan(scanMode, dataType, preset, force='true', saveType=saveType.save):
    
    controllerPath = '/commands/scan/runscan'
    sicsController = sics.getSicsController()
    scanController = sicsController.findComponentController(controllerPath)
    
    # Configuring scan properties
#    sics.hset(scanController, '/scan_variable', DEVICE_SAMX)
#    sics.hset(scanController, '/scan_start', getSampleHolderPosition())
#    sics.hset(scanController, '/scan_stop', getSampleHolderPosition())
    sics.hset(scanController, '/numpoints', 1)
#    # Hack to fix monitor selection in scan
#    if (scanMode.key == 'monitor'):
#        sics.hset(scanController, '/mode', 'MONITOR_1')
#    else:
#        sics.hset(scanController, '/mode', scanMode.key)
    sics.hset(scanController, '/preset', preset)
#    sics.hset(scanController, '/datatype', dataType.key)
#    sics.hset(scanController, '/savetype', saveType.key)
    sics.hset(scanController, '/force', force)

    sics.execute('hset /instrument/dummy_motor 0', 'general')
    
    sics.execute('hset ' + controllerPath + '/scan_variable dummy_motor', 'general')
    sics.execute('hset ' + controllerPath + '/scan_start 0', 'general')
    sics.execute('hset ' + controllerPath + '/scan_stop 0', 'general')
    sics.execute('hset ' + controllerPath + '/numpoints 1', 'general')
    if (scanMode.key == 'monitor'):
        sics.execute('hset ' + controllerPath + '/mode MONITOR_1', 'general')
    else:
        sics.execute('hset ' + controllerPath + '/mode ' + scanMode.key, 'general')
    sics.execute('hset ' + controllerPath + '/preset ' + str(preset), 'general')
    sics.execute('hset ' + controllerPath + '/datatype ' + dataType.key, 'general')
    sics.execute('hset ' + controllerPath + '/savetype ' + saveType.key, 'general')
    sics.execute('hset ' + controllerPath + '/force ' + force, 'general')

    # Wait 1 sec to make the setting settle
    time.sleep(2)
    
    # Synchronously run scan
    scanController.syncExecute()

    # Get output filename
    filenameController = sicsController.findDeviceController('datafilename')
    savedFilename = filenameController.getValue().getStringData()
    log('Saved to ' +  savedFilename)
    return savedFilename

# Functions for setting safe attenuation
thicknessTable = OrderedDict([(0, 0.0), (30, 0.13), (60, 0.326),
                              (90, 0.49), (120, 0.64), (150, 0.82),
                              (180, 0.965), (210, 1.115), (240, 1.305),
                              (270, 1.5), (300, 1.8), (330, 2.5)])
# Drive action
def driveSafeAttenuation(override=False, startingAttenuation=330):
    if override:
        # Use traditional method
        setSafeAttenuation(startingAttenuation)
    else:
        # Find safe attenuation value
        att = findSafeAttenuation(startingAttenuation)
        if att is not None:
            while not sics.getSicsController().getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE):
                time.sleep(0.1)
            driveAtt(att)

def startHistmem():
    # sicsController = sics.getSicsController()
    # histmemController = sicsController.findComponentController('/commands/histogram/histmem')
    # sics.hset(histmemController, '/mode', 'unlimited')
    log('starting histmem...')
    sics.execute('histmem mode unlimited')
    time.sleep(1.0)
    sics.execute('histmem start')

def stopHistmem():
    sicsController = sics.getSicsController()
    histmemController = sicsController.findComponentController('/commands/histogram/histmem')
    log('stopping histmem ...')
    sics.execute('histmem stop')

# determine averaged local and global rates
#   samples: number of samples to create average
#   timeout: maximal time for this function
def determineAveragedRates(max_samples=60, interval=0.2, timeout=30.0, log_success=True):
    
    def determineStandardDeviation(sum, sqr_sum, n): # n = sample count
        if n <= 1:
            return float('inf')
        
        s_sqr = (sqr_sum - sum*sum/n) / (n - 1)
        return math.sqrt(s_sqr)
    
    def getStudentsFacotr(n): # n = sample count
        # 90% confidence
        f = [float('inf'), 6.314, 2.92, 2.353, 2.132, 2.015, 1.943, 1.895, 1.86, 1.833, 1.812, 1.796, 1.782, 1.771, 1.761, 1.753, 1.746, 1.74, 1.734, 1.729, 1.725, 1.721, 1.717, 1.714, 1.711, 1.708, 1.706, 1.703, 1.701, 1.699, 1.697]
        if n <= len(f):
            return f[n - 1] # n to zero based index
        else:
            return f[-1]    # use last element
        return 1
        
    if max_samples < 1:
        max_samples = 1

    while not sics.getSicsController().getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE):
        time.sleep(0.1)
    startHistmem()
    try:
        time.sleep(1.0)

        local_rate = getMaxBinRate()
        global_rate = getGlobalMapRate()

        n = 0 # sample count
        local_rate_sum     = 0.0
        global_rate_sum     = 0.0
        local_rate_sqr_sum = 0.0 # sum of squared rate
        global_rate_sqr_sum = 0.0

        for i in xrange(max_samples):
            
            new_local_rate = getMaxBinRate()
            new_global_rate = getGlobalMapRate()
            
            start = time.time()
            while (new_local_rate == local_rate) or (new_local_rate == 0) or (new_global_rate == global_rate) or (new_global_rate == 0):
                if time.time() - start >= timeout:
                    raise Exception("Timeout in determineAveragedRates")
                time.sleep(0.5)
                new_local_rate = getMaxBinRate()
                new_global_rate = getGlobalMapRate()

            local_rate = new_local_rate
            global_rate = new_global_rate
            log('measurement:  local rate = %10.3f          global rate = %10.3f' % (local_rate, global_rate))

            n += 1 # increase sample count
            local_rate_sum     += local_rate
            global_rate_sum     += global_rate
            local_rate_sqr_sum += (local_rate * local_rate)
            global_rate_sqr_sum += (global_rate * global_rate)
            
            if n > 1:
                # https://de.wikipedia.org/wiki/Vertrauensintervall#Beispiele_f.C3.BCr_ein_Konfidenzintervall
                
                local_rate_mean = local_rate_sum / n
                global_rate_mean = global_rate_sum / n
                
                local_rate_std = determineStandardDeviation(local_rate_sum, local_rate_sqr_sum, n)
                global_rate_std = determineStandardDeviation(global_rate_sum, global_rate_sqr_sum, n)
                
                factor = getStudentsFacotr(n) / math.sqrt(n)
                
                local_rate_err = factor * local_rate_std
                global_rate_err = factor * global_rate_std
                
                log('estimation:   local rate = %10.3f+-%-7.3f global rate = %10.3f+-%-7.3f' % (local_rate_mean, local_rate_err, global_rate_mean, global_rate_err))
                
                local_rate_intvl = 2 * local_rate_err
                global_rate_intvl = 2 * global_rate_err
                
                if (local_rate_intvl < interval * local_rate_mean) and (global_rate_intvl < interval * global_rate_mean):
                    if log_success:
                        log('successful estimation')
                    break;
                
                if (n == max_samples) and log_success:
                    log('unsuccessful estimation')

    finally:
        stopHistmem()
 
    return local_rate_sum/n, global_rate_sum/n # -0.7 to subtract background

# Find the attenuation angle
def findSafeAttenuation(startingAttenuation):
    # Sample at 90 (was 150) and 60 (was 120)
    attenuation1 = 120
    attenuation2 =  90
    # Safety margin (ratio)
    safety_margin = 1.02 # 2%
    
    # First sampling
    while not sics.getSicsController().getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE):
        time.sleep(0.1)
    driveAtt(attenuation1)
        
    local_rate1, global_rate1 = determineAveragedRates()
    thickness1 = thicknessTable[attenuation1]
    
    log('local rate1 = ' + str(local_rate1))
    log('global rate1 = ' + str(global_rate1))
    
    # Second sampling
    while not sics.getSicsController().getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE):
        time.sleep(0.1)
    driveAtt(attenuation2)
        
    local_rate2, global_rate2 = determineAveragedRates()
    thickness2 = thicknessTable[attenuation2]
    
    log('local rate2 = ' + str(local_rate2))
    log('global rate2 = ' + str(global_rate2))
    
    # check that at least local or global rate is sufficient
    if (local_rate2 <= local_rate1 * safety_margin) and (global_rate2 <= global_rate1 * safety_margin):
        log('insufficient statistics - safe attenuation routine is used')
        sics.execute('histmem mode time')
        if (local_rate2 <= local_rateSafe) and (global_rate2 <= global_rateSafe):
            setSafeAttenuation(attenuation2)
            return None # to tell caller that attenuation value is set
        elif (local_rate1 <= local_rateSafe) and (global_rate1 <= global_rateSafe):
            setSafeAttenuation(attenuation1)
            return None # to tell caller that attenuation value is set
        else:
            setSafeAttenuation(startingAttenuation)
            return None # to tell caller that attenuation value is set

    # find safe thickness
    if local_rate2 > local_rate1 * safety_margin:
        local_ratio  = math.log(local_rate2 / local_rate1) / (thickness1 - thickness2)
        local_offset = math.log(local_rate1 / local_rateSafe) / local_ratio
        if local_offset < 0:
            local_thicknessSafe = thickness1 + local_offset
        else:
            local_thicknessSafe = thickness1
        log('local ratio = ' + str(local_ratio))
    else:
        local_thicknessSafe = 0
    
    if global_rate2 > global_rate1 * safety_margin:
        global_ratio  = math.log(global_rate2 / global_rate1) / (thickness1 - thickness2)
        global_offset = math.log(global_rate1 / global_rateSafe) / global_ratio
        if global_offset < 0:
            global_thicknessSafe = thickness1 + global_offset
        else:
            global_thicknessSafe = thickness1
        log('global ratio = ' + str(global_ratio))
    else:
        global_thicknessSafe = 0

    # final safe thickness
    thicknessSafe = max([0, local_thicknessSafe, global_thicknessSafe])
    log('safe thickness = ' + str(thicknessSafe))
    
    # find corresponding attenuation angle
    suggestedAttAngle = 330
    for item in thicknessTable.items():
        attAngle  = item[0]
        thickness = item[1]
        if (thicknessSafe <= thickness) and (suggestedAttAngle > attAngle):
            suggestedAttAngle = attAngle
    
    log('suggested attenutaion angle = ' + str(suggestedAttAngle))
    return suggestedAttAngle
    
# Count simulation - used for debug only
def count(thickness, wavelength): 
    # Initial count rate = 100 count/sec
    countInitial = 100
    # Attenuation factor is 0.01 /mm/A
    attenuationFactor = 0.5
    return countInitial * math.exp(-1 * attenuationFactor * thickness * wavelength)
    
# Prints current instrument settings on Quokka
def printQuokkaSettings():
    datafile = sics.getValue('datafilename').getStringData()
    if ((datafile.find('QKK') != -1) and (datafile.find('.nx.hdf') != -1)):
        runNumber = (datafile.split('QKK')[1].split('.nx.hdf'))[0]
    else:
        runNumber = datafile
    msg = '\n'
    msg += "*****  Quokka Instrument Settings  *****"
    msg += '\n'
    msg +=  "    Last Run Number: " + runNumber
    msg +=  "        Sample Name: " + sics.getValue('samplename').getStringData()
    msg += '\n'
    msg +=  "         Attenuator: %.2f degree" % getAttValue()
    msg +=  " Entrance Aperature: %.2f" % getEntRotApValue()
    msg +=  "Guide Configuration: " + getGuideConfig()
    msg +=  "    Sample Position: " + str(getSamplePosition())
#    print "        Beam Stop 1: " + getBsPosition(1)
#    print "        Beam Stop 2: " + getBsPosition(2)
#    print "        Beam Stop 3: " + getBsPosition(3)
#    print "        Beam Stop 4: " + getBsPosition(4)
#    print "        Beam Stop 5: " + getBsPosition(5)
    msg +=  "        Beam Stop X: %.2f" % getBsxValue()
    msg +=  "        Beam Stop Z: %.2f" % getBszValue()
    msg +=  "  Detector Position: %.2f" %getDetPosition()
    msg +=  "    Detector Offset: %.2f" % getDetOffsetValue()
    msg += '\n'
    msg +=  "****************************************"
    log(msg)
