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
from au.gov.ansto.bragg.quokka.sics import DetectorHighVoltageController
from au.gov.ansto.bragg.quokka.sics import BeamStopController
import time
import math

# Constants
DEVICE_SAMX = 'samx'

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
        except:
            sics.handleInterrupt()
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
    log('Driving attenuator to ' + str(value) + ' degree ...')
    sics.drive('att', value)
    log('Attenuator is now at ' + str(getAttValue()) + ' degree')

def setSafeAttenuation(startingAttenuation=330):
    # Variables
    localRateLimit = 10
    globalRateLimit = 25000
    #rateUpdateTime = 5
    hmPreset = 5 #rateUpdateTime * 2
    localRate = 0
    globalRate = 0
    previousLocalRate = 0
    previousGlobalRate = 0
    startLevel = attenuationLevels.index(startingAttenuation)
    
    # Hack: need to reset and run histmem
    log('set safe attenuation...')
    scan(scanMode.time, dataType.HISTOGRAM_XY, hmPreset, 'true', saveType.nosave)
    time.sleep(1)
    
    # loop from the safe range of attenuation
    for level in xrange(startLevel, len(attenuationLevels)):
        # drive the attenuator
        driveAtt(attenuationLevels[level])
        
        # count bin rate
        driveHistmem(hmMode.time, hmPreset)
        localRate = getMaxBinRate()
        globalRate = getTotalMapRate()
        log('Current rate: local = ' + str(localRate) + ', global = ' + str(globalRate))
        checkDetHealth()
        # Too much (check both local and global rate)
        if ((localRate > localRateLimit) or (globalRate > globalRateLimit)):
            if (level > 0):
                # [GUMTREE-378] Ensure SICS is ready after count
                while not sics.getSicsController().getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE):
                    time.sleep(0.1)
                # Brings it back to one step
                driveAtt(attenuationLevels[level - 1])
            localRate = previousLocalRate
            globalRate = previousGlobalRate
            break
        # Within tolerance
        elif (localRate >= localRateLimit / 2) and (localRate <= localRateLimit):
            break
        previousLocalRate = localRate
        previousGlobalRate = globalRate
        # Ensure SICS is ready after count
        while not sics.getSicsController().getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE):
            time.sleep(0.1)
    
    # Print info at the end
    log('Attenuation is set to ' + str(getAttValue()) + ' degree with bin rate ' + str(localRate))

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
    # Synchronously run command
    commandController.syncExecute()

def selBsxz(beamstop, bx, bz):
    # Get command controller
    sicsController = sics.getSicsController()
    commandController = sicsController.findComponentController('/commands/beamstops/selbsxz')
    
    # Configuring command properties
    sics.hset(commandController, '/bs', beamstop)
    sics.hset(commandController, '/bx', bx)
    sics.hset(commandController, '/bz', bz)
    
    # Synchronously run command
    commandController.syncExecute()
    
    
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
    commandController.syncExecute();
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
def getTotalMapRate():
    return sics.getValue('/instrument/detector/total_maprate').getFloatData()

def getSampleHolderPosition():
    sicsController = SicsCore.getSicsController()
    samx = sicsController.findDeviceController(DEVICE_SAMX)
    return samx.getValue().getFloatData()

# This scan rely on samx
def scan(scanMode, dataType, preset, force='true', saveType=saveType.save):
    
    sicsController = sics.getSicsController()
    scanController = sicsController.findComponentController('/commands/scan/runscan')
    
    # Configuring scan properties
    sics.hset(scanController, '/scan_variable', DEVICE_SAMX)
    sics.hset(scanController, '/scan_start', getSampleHolderPosition())
    sics.hset(scanController, '/scan_stop', getSampleHolderPosition())
    sics.hset(scanController, '/numpoints', 1)
    # Hack to fix monitor selection in scan
    if (scanMode.key == 'monitor'):
        sics.hset(scanController, '/mode', 'MONITOR_1')
    else:
        sics.hset(scanController, '/mode', scanMode.key)
    sics.hset(scanController, '/preset', preset)
    sics.hset(scanController, '/datatype', dataType.key)
    sics.hset(scanController, '/savetype', saveType.key)
    sics.hset(scanController, '/force', force)
    
    # Wait 1 sec to make the setting settle
    time.sleep(1)
    
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
        att, startingAttenuation = findSafeAttenuation(startingAttenuation)
        if att is not None:
            while not sics.getSicsController().getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE):
                time.sleep(0.1)
            driveAtt(att)
            
    return startingAttenuation

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
    log('stoping histmem ...')
    sics.execute('histmem stop')

# determine averaged local and total rates
#   samples: number of samples to create average
#   timeout: maximal time for this function
def determineAveragedRates(samples=5, timeout=30.0):

    while not sics.getSicsController().getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE):
        time.sleep(0.1)
    startHistmem()
    try:
        time.sleep(1.0)

        local_rate = getMaxBinRate()
        total_rate = getTotalMapRate()

        accumulated_local_rate = 0.0
        accumulated_total_rate = 0.0

        start = time.time()
        n = max([samples, 1])
        for i in xrange(n):
            
            new_local_rate = getMaxBinRate()
            new_total_rate = getTotalMapRate()
            
            while (new_local_rate == local_rate) or (new_local_rate == 0) or (new_total_rate == total_rate) or (new_total_rate == 0):
                if time.time() - start >= timeout:
                    raise Exception("Timeout in determineAveragedRates")
                time.sleep(0.5)
                new_local_rate = getMaxBinRate()
                new_total_rate = getTotalMapRate()

            local_rate = new_local_rate
            total_rate = new_total_rate
            log('local rate: ' + str(local_rate))
            log('total rate: ' + str(total_rate))

            accumulated_local_rate += local_rate
            accumulated_total_rate += total_rate

    finally:
        stopHistmem()
 
    return accumulated_local_rate/n, accumulated_total_rate/n # -0.7 to subtract background

# Find the attenuation angle
def findSafeAttenuation(startingAttenuation):
    # Safe count rate
    local_rateSafe =     5.0
    total_rateSafe = 15000.0
    # Sample at 90 (was 150) and 60 (was 120)
    attenuation1 = 180
    attenuation2 = 150
    # Safety margin (ratio)
    safety_margin = 1.02 # 2%
    
    # First sampling
    while not sics.getSicsController().getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE):
        time.sleep(0.1)
    driveAtt(attenuation1)
        
    local_rate1, total_rate1 = determineAveragedRates()
    thickness1 = thicknessTable[attenuation1]
    
    log('local rate1 = ' + str(local_rate1))
    log('total rate1 = ' + str(total_rate1))
    
    # Second sampling
    while not sics.getSicsController().getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE):
        time.sleep(0.1)
    driveAtt(attenuation2)
        
    local_rate2, total_rate2 = determineAveragedRates()
    thickness2 = thicknessTable[attenuation2]
    
    log('local rate2 = ' + str(local_rate2))
    log('total rate2 = ' + str(total_rate2))
    
    # check that at least local or total rate is sufficient
    if (local_rate2 <= local_rate1 * safety_margin) and (total_rate2 <= total_rate1 * safety_margin):
        log('insufficient statistics - safe attenuation routine is used')
        sics.execute('histmem mode time')
        if (local_rate2 <= local_rateSafe) and (total_rate2 <= total_rateSafe):
            setSafeAttenuation(attenuation2)
            return None, attenuation1 # to tell caller that attenuation value is set
        elif (local_rate1 <= local_rateSafe) and (total_rate1 <= total_rateSafe):
            setSafeAttenuation(attenuation1)
            return None, attenuation1 # to tell caller that attenuation value is set
        else:
            setSafeAttenuation(startingAttenuation)
            return None, startingAttenuation # to tell caller that attenuation value is set

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
    
    if total_rate2 > total_rate1 * safety_margin:
        total_ratio  = math.log(total_rate2 / total_rate1) / (thickness1 - thickness2)
        total_offset = math.log(total_rate1 / total_rateSafe) / total_ratio
        if total_offset < 0:
            total_thicknessSafe = thickness1 + total_offset
        else:
            total_thicknessSafe = thickness1
        log('total ratio = ' + str(total_ratio))
    else:
        total_thicknessSafe = 0

    # final safe thickness
    thicknessSafe = max([0, local_thicknessSafe, total_thicknessSafe])
    log('safe thickness = ' + str(thicknessSafe))
    
    # find corresponding attenuation angle
    suggestedAttAngle = 330
    for item in thicknessTable.items():
        attAngle  = item[0]
        thickness = item[1]
        if (thicknessSafe <= thickness) and (suggestedAttAngle > attAngle):
            suggestedAttAngle = attAngle
    
    log('suggested attenutaion angle = ' + str(suggestedAttAngle))
    return suggestedAttAngle, attenuation1
    
# Check the health of our detector
def checkDetHealth():
    #if getTotalMapRate() <= 0.0:
    #    raise Exception("There is no reading from the detector.")
    pass

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
    if (datafile == 'UNKNOWN'):
        runNumber = datafile
    else:
        runNumber = (datafile.split('QKK')[1].split('.nx.hdf'))[0]
    print
    print "*****  Quokka Instrument Settings  *****"
    print
    print "    Last Run Number: " + runNumber
    print "        Sample Name: " + sics.getValue('samplename').getStringData()
    print
    print "         Attenuator: %.2f degree" % getAttValue()
    print " Entrance Aperature: %.2f" % getEntRotApValue()
    print "Guide Configuration: " + getGuideConfig()
    print "    Sample Position: " + str(getSamplePosition())
#    print "        Beam Stop 1: " + getBsPosition(1)
#    print "        Beam Stop 2: " + getBsPosition(2)
#    print "        Beam Stop 3: " + getBsPosition(3)
#    print "        Beam Stop 4: " + getBsPosition(4)
#    print "        Beam Stop 5: " + getBsPosition(5)
    print "        Beam Stop X: %.2f" % getBsxValue()
    print "        Beam Stop Z: %.2f" % getBszValue()
    print "  Detector Position: %.2f" %getDetPosition()
    print "    Detector Offset: %.2f" % getDetOffsetValue()
    print
    print "****************************************"
