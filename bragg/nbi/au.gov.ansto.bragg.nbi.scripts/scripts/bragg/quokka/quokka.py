from gumpy.commons import sics
from gumpy.commons.logger import log
from gumpy.commons.logger import n_logger

from org.gumtree.gumnix.sics.control import ServerStatus
from org.gumtree.gumnix.sics.io import SicsExecutionException

from au.gov.ansto.bragg.quokka.sics import DetectorHighVoltageController
from au.gov.ansto.bragg.quokka.sics import BeamStopController

import sys
import time
import math

from collections import OrderedDict
from datetime import datetime, timedelta

class Enumeration(object):
    def __init__(self, *keys):
        for key in keys:
            if not isinstance(key, str):
                raise TypeError

            setattr(self, key, key)

        self._keys = keys

    def __contains__(self, key):
        return key in self._keys


# safe count rates
LOCAL_RATE_SAFE  =    15.0
GLOBAL_RATE_SAFE = 40000.0

# attenuation values
ATT_VALUES = [330, 300, 270, 240, 210, 180, 150, 120, 90, 60, 30, 0]

SAMPLE_STAGE = Enumeration('fixed', 'manual', 'lookup')
ATTENUATION_ALGO = Enumeration('fixed', 'iterative', 'smart')
MEASUREMENT_MODE = Enumeration('transmission', 'scattering')
ACQUISITION_MODE = Enumeration('unlimited', 'time', 'counts', 'bm_counts', 'ba') # ba: bounded acquisition

ACTION = Enumeration('up', 'down')
GUIDE_CONFIG = Enumeration(
    'ga', 'mt', 'lp', 'lens',
    'p1', 'p1lp', 'p1lens', 'g1',
    'p2', 'g2', 'p3', 'g3', 'p4', 'g4', 'p5', 'g5',
    'p6', 'g6', 'p7', 'g7', 'p8', 'g8', 'p9', 'g9')

dhv1 = DetectorHighVoltageController()
bsList = dict((index, BeamStopController(index)) for index in range(1, 6))

def setConsoleWriter(writter):
    global __CONSOLE_WRITER__
    __CONSOLE_WRITER__ = writter

def slog(text):
    global __CONSOLE_WRITER__
    log(text, __CONSOLE_WRITER__)

class QuokkaState:
    def __init__(self):
        self.env_drive = dict()

        self.sample_stage    = SAMPLE_STAGE.lookup
        self.sample_name     = 'unknown'
        self.sample_position = 1

        self.att_algo  = ATTENUATION_ALGO.fixed
        self.att_angle = 330

        self.meas_mode = MEASUREMENT_MODE.scattering
        self.acq_mode  = ACQUISITION_MODE.unlimited
        self.min_time  = None
        self.max_time  = None
        self.counts    = None
        self.bm_counts = None

state = QuokkaState()

def sleep(secs, dt=0.1):
    # interruptable sleep
    target = datetime.now() + timedelta(seconds=secs)

    while True:
        if target < datetime.now():
            break
        else:
            sics.handleInterrupt()

        if target < datetime.now():
            break
        else:
            time.sleep(dt)

    sics.handleInterrupt()

def waitUntilSicsIs(status, dt=0.2):
    repeat = True
    while repeat:
        sics.handleInterrupt()

        while not sics.getSicsController().getServerStatus().equals(status):
            time.sleep(dt)

        time.sleep(dt)
        repeat = not sics.getSicsController().getServerStatus().equals(status)

    sics.handleInterrupt()

def isInterruptException(e):
    return isinstance(e, SicsExecutionException) and ('Interrupted' in str(e.getMessage()))

def hasTripped():

    def getHistmemTextstatus(name):
        counter = 0
        while True:
            try:
                counter += 1
                return str(sics.run_command('histmem textstatus ' + name))

            except (Exception, SicsExecutionException) as e:
                if isInterruptException(e) or (counter >= 3):
                    raise

                time.sleep(1)

    trp = int(getHistmemTextstatus('detector_protect_num_trip'))
    ack = int(getHistmemTextstatus('detector_protect_num_trip_ack'))

    value = trp != ack
    if value:
        slog('Detector has tripped')
        print >> sys.stderr, 'Detector has tripped'

    return value

def resetTrip(increase_att=True):
    slog('Reset trip')

    result = False
    if increase_att:
        # drive to higher attenuation
        att = getAtt()
        if att < max(ATT_VALUES):
            driveAtt(att + 30)
            result = True
        else:
            raise Exception('unable to increase attenuation in order to reset trip')

    # reset fast shutter
    sics.execute('hset /instrument/detector/reset_trip 1')
    sleep(20) # interruptable

    return result # only true if attenuation has been increased

def initiate(info):
    # parameters
    title        = info.experimentTitle
    pnumber      = str(info.proposalNumber)
    users        = str(info.users)
    sample_stage = str(info.sampleStage)

    # update sics
    sics.set('title', title)
    sics.execute('hset /experiment/experiment_identifier ' + pnumber)

    sics.set('user', users)
    #sics.execute('hset /user/email ' + email) # !!!
    #sics.execute('hset /user/phone ' + phone)

    setSampleStage(sample_stage)

def cleanUp(info):
    slog('Cleaning up ...')
    
    driveToSafeAtt()
    driveToLoadPosition()

def setParameters(info):
    # parameters
    name       = str(info.name).lower()
    parameters = info.parameters

    if name == 'configurationlist':
        pass # configuration list doesn't have any parameters used for acquisition
        
    elif name == 'configuration':
        setupConfiguration(parameters)

    elif name == 'transmission':
        setupMeasurement(parameters, MEASUREMENT_MODE.transmission)

    elif name == 'scattering':
        setupMeasurement(parameters, MEASUREMENT_MODE.scattering)

    elif name == 'samplelist':
        pass # sample list doesn't have any parameters used for acquisition

    elif name == 'sample':
        setupSample(parameters)

    elif name == 'environment':
        setupEnvironment(parameters)

    elif name == 'setpoint':
        setupSetPoint(parameters)

    else:
        slog('unknown parameters: ' + name)

def setupConfiguration(parameters):
    # parameters
    name   = parameters['Name']
    script = parameters['SetupScript']

    # run configuration script
    slog('Set instrument to configuration: ' + name)

    # before instrument can move to new configuration, move to safe attenuation angle
    driveToSafeAtt()

    exec script in globals()

def setupMeasurement(parameters, meas_mode):
    def lookup(parameters, name):
        if name in parameters:
            value = parameters[name]
            if value is not None:
                return int(value)
            else:
                return None # e.g. for ba mode parameters can be set to None

        return None

    # parameters
    script    = parameters['SetupScript']

    att_algo  = str(parameters['AttenuationAlgorithm']).lower()
    att_angle = int(parameters['AttenuationAngle'])

    acq_mode  = ACQUISITION_MODE.ba
    min_time  = lookup(parameters, 'MinTime')
    max_time  = lookup(parameters, 'MaxTime')
    counts    = lookup(parameters, 'Counts')
    bm_counts = lookup(parameters, 'BmCounts')

    # ATTENUATION_ALGO = Enumeration('fixed', 'iterative', 'smart')
    att_algos = dict()
    att_algos["fixed attenuation"]       = ATTENUATION_ALGO.fixed
    att_algos["iterative attenuation"]   = ATTENUATION_ALGO.iterative
    att_algos["smart attenuation"]       = ATTENUATION_ALGO.smart

    # check parameters
    if att_algo not in att_algos:
        raise Exception('unknown attenuation algorithm: ' + att_algo)
    if att_angle not in ATT_VALUES:
        raise Exception('unexpected attenuation angle: %i' % att_angle)
    if meas_mode not in MEASUREMENT_MODE:
        raise Exception('unknown measurement mode: ' + meas_mode)
    if acq_mode not in ACQUISITION_MODE:
        raise Exception('unknown acquisition mode: ' + acq_mode)

    # state (global variable)
    state.att_algo  = att_algos[att_algo]
    state.att_angle = att_angle

    state.meas_mode = meas_mode
    state.acq_mode  = acq_mode
    state.min_time  = min_time
    state.max_time  = max_time
    state.counts    = counts
    state.bm_counts = bm_counts

    if state.meas_mode == MEASUREMENT_MODE.transmission:
        # set transmission flag
        slog('Set instrument to transmission mode')
        sics.set('transmissionflag', 1)
    else:
        # unset transmission flag
        slog('Set instrument to scattering mode')
        sics.set('transmissionflag', 0)

    time.sleep(0.5)

    # before instrument can move to new configuration, move to safe attenuation angle
    driveToSafeAtt()

    # run configuration script
    exec script in globals()

def setupSample(parameters):
    # parameters
    name        = parameters['Name']
    description = parameters['Description']
    thickness   = float(parameters['Thickness'])
    position    = float(parameters['Position'])

    # state
    state.sample_name = name
    state.sample_position = position

    # set sample name and description
    sics.set('samplename', name)
    sics.set('sampledescription', description)
    sics.set('samplethickness', thickness)

    # drive to sample position
    if state.sample_stage != SAMPLE_STAGE.fixed:
        driveToSamplePosition(position)

def setupEnvironment(parameters):
    # parameters
    name  = parameters['Name']
    env   = parameters['ElementPath']
    script_setup = parameters['SetupScript']
    script_drive = parameters['DriveScript']

    # run configuration script
    slog('Prepare instrument for environment: ' + name)

    state.env_drive[env] = script_drive

    exec script_setup in globals()

def setupSetPoint(parameters):
    # parameters
    env   = parameters['ElementRoot']
    value = float(parameters['Value'])
    wait  = int(parameters['WaitPeriod'])

    # run configuration script
    slog('Prepare instrument for next SetPoint')

    exec state.env_drive[env] in globals(), dict(value=value)

    sleep(wait)

def preAcquisition(info):
    # make sure that detector has not tripped already
    if hasTripped():
        driveAtt(max(ATT_VALUES))
        resetTrip(increase_att=False)

    att_algo  = state.att_algo
    att_angle = state.att_angle

    if att_algo == ATTENUATION_ALGO.fixed:
        driveAtt(att_angle)

    elif att_algo == ATTENUATION_ALGO.iterative:
        iterativeAttenuationAlgo(att_angle)

    elif att_algo == ATTENUATION_ALGO.smart:
        smartAttenuationAlgo(att_angle)

    else:
        slog('unexpected attenuation algorithm: ' + att_algo)

def doAcquisition(info):
    def lookup(parameters, name, default):
        if name in parameters:
            value = parameters[name]
            if value is not None:
                return int(value)
            else:
                return None # e.g. for ba mode parameters can be set to None

        return default

    slog('Start %s run on %s (position: %s, sample stage: %s)' % (state.meas_mode, state.sample_name, state.sample_position, state.sample_stage))

    parameters = info.parameters

    acq_mode  = state.acq_mode
    min_time  = lookup(parameters, 'MinTime', state.min_time)
    max_time  = lookup(parameters, 'MaxTime', state.max_time)
    counts    = lookup(parameters, 'TargetDetectorCounts', state.counts)
    bm_counts = lookup(parameters, 'TargetMonitorCounts', state.bm_counts)

    tripCount = 0

    if acq_mode == ACQUISITION_MODE.ba:
        slog('Bound Acquisition (min-time: %s, max-time: %s, counts: %s, bm_counts: %s)' % (min_time, max_time, counts, bm_counts))
        scanBA(min_time, max_time, counts, bm_counts)

        while hasTripped():
            tripCount += 1
            if not resetTrip(increase_att=True):
                break

            slog('Repeat Bound Acquisition ...')
            scanBA(min_time, max_time, counts, bm_counts)

    else:
        if acq_mode == ACQUISITION_MODE.time:
            preset = max_time
        elif acq_mode == ACQUISITION_MODE.counts:
            preset = counts
        elif acq_mode == ACQUISITION_MODE.bm_counts:
            preset = bm_counts
        else:
            preset = None

        slog('Acquisition (mode: %s, preset: %s)' % (acq_mode, preset))
        scan(acq_mode, preset)

        while hasTripped():
            tripCount += 1
            if not resetTrip(increase_att=True):
                break

            slog('Repeat Acquisition ...')
            scan(acq_mode, preset)

    # feedback
    info.filename = getDataFilename()
    if tripCount > 0:
        info.notes = str('trips: %i' % tripCount)

    # print instrument state
    printSettings()

def postAcquisition(info):
    # move to initial attenuation angle
    # this is okay, because all samples within one configuration will start with the same attenuation angle
    # and before the instrument moves to a new configuration, driveToSafeAtt() is called
    att_angle = state.att_angle

    if getAtt() != att_angle:
        driveAtt(att_angle)

def customAction(info):
    # parameters
    action     = str(info.action).lower()
    parameters = info.parameters

    if action == 'getsamplepositions':
        parameters['SamplePositions'] = getSamplePositions()

    elif action == 'drivetoloadposition':
        setSampleStage(str(parameters['SampleStage']))
        driveToLoadPosition()

    elif action == 'drivetosampleposition':
        setSampleStage(str(parameters['SampleStage']))
        driveToSamplePosition(float(parameters['Position']))

    elif action == 'testdrive':
        testDrive(parameters['Script'])

    elif action == 'environmentsetup':
        environmentSetup(parameters['Script'])

    elif action == 'environmentdrive':
        environmentDrive(parameters['Script'], float(parameters['Value']))

    elif action == 'publishfinishtime':
        publishFinishTime(long(parameters['Time']))

    elif action == 'publishtables':
        publishTables(parameters['Tables'])

    else:
        slog('unknown action: ' + info.action)

def setSampleStage(sample_stage):
    slog('Set sample stage to "' + sample_stage + '" ...')

    # SAMPLE_STAGE = Enumeration('fixed', 'manual', 'lookup')
    sample_stages = dict()
    sample_stages["Fixed Position"]               = SAMPLE_STAGE.fixed
    sample_stages["Manual Position"]              = SAMPLE_STAGE.manual
    sample_stages["Rheometer"]                    = SAMPLE_STAGE.lookup
    sample_stages["5 Position Rotating Holder"]   = SAMPLE_STAGE.lookup
    sample_stages["10 Position Holder"]           = SAMPLE_STAGE.lookup
    sample_stages["12 Position Holder"]           = SAMPLE_STAGE.lookup
    sample_stages["20 Position Holder"]           = SAMPLE_STAGE.lookup

    if sample_stage not in sample_stages:
        raise Exception('unknown sample stage configuration: ' + sample_stage)

    state.sample_stage = sample_stages[sample_stage]

def driveToLoadPosition():
    if state.sample_stage == SAMPLE_STAGE.fixed:
        slog('fixed sample stage cannot be driven to load position')
        return

    slog('Driving sample holder to load position ...')
    samx = sics.getSicsController().findDeviceController('samx')

    tolerance     = samx.getChildController('/precision').getValue().getFloatData()
    soft_zero     = samx.getChildController('/softzero').getValue().getFloatData()
    soft_upperlim = samx.getChildController('/softupperlim').getValue().getFloatData()
    hard_upperlim = samx.getChildController('/hardupperlim').getValue().getFloatData()

    if soft_upperlim > hard_upperlim - soft_zero:
        soft_upperlim = hard_upperlim - soft_zero

    checkedDrive('samx', soft_upperlim - tolerance)

    slog('Sample holder is now at load position')

def driveToSamplePosition(position):
    if state.sample_stage == SAMPLE_STAGE.fixed:
        slog('fixed sample stage cannot be driven to %s' % position)
        return

    # drive to position
    slog('Driving sample holder to position %s ...' % position)

    if state.sample_stage == SAMPLE_STAGE.manual:
        checkedDrive('samx', position)
        return

    if state.sample_stage != SAMPLE_STAGE.lookup:
        raise Exception('unexpected sample stage configuration')

    sicsController = sics.getSicsController()
    controller = sicsController.findComponentController('/sample/sampleNum')

    counter = 0
    position = int(position)
    tolerance = 0.1
    while True:
        try:
            counter += 1

            waitUntilSicsIs(ServerStatus.EAGER_TO_EXECUTE)
            sics.handleInterrupt()

            controller.drive(position)
            sics.handleInterrupt()

            break

        except (Exception, SicsExecutionException) as e:
            if isInterruptException(e) or (counter >= 20):
                raise
            
            if abs(controller.getValue(True).getFloatData() - position) <= tolerance:
                break

            slog('Retry driving sampleNum')
            time.sleep(1)

    # wait until
    for counter in xrange(10):
        if abs(controller.getValue(True).getFloatData() - position) > tolerance:
            time.sleep(0.1)
        else:
            break

    slog('Position of sample holder: %s' % controller.getValue(True).getFloatData())

def testDrive(script):
    # run script
    exec script in globals()

def environmentSetup(script):
    # run script
    exec script in globals()

def environmentDrive(script, value):
    # run script
    exec script in globals(), dict(value=value)

def publishFinishTime(time):
    sics.execute('hset /experiment/gumtree_time_estimate %i' % time, 'status')

def publishTables(tables):
    # TableInfo { String getName(); String getContent(); }
    # tables: Iterable<TableInfo>
    for table in tables:
        n_logger.log_table(name=table.getName(), table=table.getContent())

def printSettings():
    msg = '\n'
    msg += '*****  Quokka Instrument Settings  *****\n'
    msg += '\n'
    msg += '           Filename: %s\n' % getDataFilename(throw=False)
    msg += '        Sample Name: %s\n' % getSampleName(throw=False)
    msg += '\n'
    msg += '         Attenuator: %s\n' % getAtt(throw=False)
    msg += '  Entrance Aperture: %s\n' % getEntRotAp(throw=False)
    msg += 'Guide Configuration: %s\n' % getGuideConfig(throw=False)
    msg += '      Sample Number: %s\n' % getSampleNumber(throw=False)
    msg += '  Sample X Position: %s\n' % getSamx(throw=False)
    msg += '        Beam Stop X: %s\n' % getBsx(throw=False)
    msg += '        Beam Stop Z: %s\n' % getBsz(throw=False)
    msg += '  Detector Position: %s\n' % getDetPosition(throw=False)
    msg += '    Detector Offset: %s\n' % getDetOffset(throw=False)
    msg += '\n'
    msg += '****************************************\n'
    slog(msg)

def resolveTrip():
    while hasTripped() and resetTrip(increase_att=True):
        local_rate, global_rate = determineAveragedRates(max_samples=3, log_success=False)
        slog('local rate = %s' % local_rate)
        slog('global rate = %s' % global_rate)

def iterativeAttenuationAlgo(start_angle):
    local_rate  = 0.0
    global_rate = 0.0
    pre_local_rate  = 0.0
    pre_global_rate = 0.0

    start_level = ATT_VALUES.index(start_angle)

    slog('Iterative attenuation algorithm ...')

    # loop from the safe range of attenuation
    for level in xrange(start_level, len(ATT_VALUES)):
        # drive the attenuator
        driveAtt(ATT_VALUES[level])

        # count bin rate
        if not hasTripped():
            local_rate, global_rate = determineAveragedRates(max_samples=5, log_success=False)
            slog('local rate = %s' % local_rate)
            slog('global rate = %s' % global_rate)

        # check if detector has tripped
        if hasTripped():
            resolveTrip()
            break

        # check if rates are too high
        elif (local_rate > LOCAL_RATE_SAFE) or (global_rate > GLOBAL_RATE_SAFE):
            if level > 0:
                # move to higher attenuation
                driveAtt(ATT_VALUES[level - 1])

                if hasTripped():
                    resetTrip(increase_att=False) # after increasing attenuation detector shouldn't trip

                local_rate  = pre_local_rate
                global_rate = pre_global_rate

            break

        # check if within tolerance ([11/06/2015:davidm] 2.8 is a better approximation)
        elif (local_rate >= LOCAL_RATE_SAFE / 2) or (global_rate >= GLOBAL_RATE_SAFE / 2.8):
            slog('exit loop')
            break

        # repeat
        else:
            pre_local_rate  = local_rate
            pre_global_rate = global_rate

    # print info
    slog('Attenuation is set to %i (local rate = %.1f, global rate = %.1f)' % (getAtt(), local_rate, global_rate))

def smartAttenuationAlgo(start_angle):
    slog('Smart attenuation algorithm ...')

    # attenuator thicknesses
    thickness_table = OrderedDict([(0, 0.0), (30, 0.13), (60, 0.326),
                                  (90, 0.49), (120, 0.64), (150, 0.82),
                                  (180, 0.965), (210, 1.115), (240, 1.305),
                                  (270, 1.5), (300, 1.8), (330, 2.5)])

    start_level = ATT_VALUES.index(start_angle)
    if start_level >= len(ATT_VALUES) - 4:
        # it doesn't make sense to run smart attenuation algo from such a low angle
        slog('switching to iterative attenuation algorithm because start angle is sufficiently low ...')
        return iterativeAttenuationAlgo(start_angle)

    # start attenuation
    att1 = ATT_VALUES[start_level]
    att2 = ATT_VALUES[start_level + 1]

    # safety margin (ratio)
    safety_margin = 1.02  # 2 percent

    # first sampling
    driveAtt(att1)
    local_rate1, global_rate1 = determineAveragedRates()
    thickness1 = thickness_table[att1]

    if hasTripped():
        return resolveTrip()

    # second sampling
    driveAtt(att2)
    local_rate2, global_rate2 = determineAveragedRates()
    thickness2 = thicknessTable[attenuation2]

    if hasTripped():
        return resolveTrip()

    # check that statistics is sufficient
    if (local_rate2 < local_rate1 * safety_margin) and (global_rate2 < global_rate1 * safety_margin):
        slog('insufficient statistics - switching to iterative attenuation algorithm ...')
        if (local_rate2 < LOCAL_RATE_SAFE) and (global_rate2 < GLOBAL_RATE_SAFE):
            return iterativeAttenuationAlgo(att2)
        else:
            return iterativeAttenuationAlgo(att1)

    # find safe thickness
    if local_rate2 > local_rate1 * safety_margin:
        # offset should be negative, and means how much thickness can be removed to get to ideal attenuation
        local_ratio = math.log(local_rate2 / local_rate1) / (thickness1 - thickness2)
        local_offset = math.log(local_rate1 / LOCAL_RATE_SAFE) / local_ratio # LOCAL_RATE_SAFE should never be zero

        if local_offset < 0:
            local_thickness_safe = thickness1 + local_offset * 0.90  # 90% for safety
        else:
            local_thickness_safe = thickness1

        slog('local ratio = %s' % local_ratio)
    else:
        local_thickness_safe = 0

    if global_rate2 > global_rate1 * safety_margin:
        global_ratio = math.log(global_rate2 / global_rate1) / (thickness1 - thickness2)
        global_offset = math.log(global_rate1 / GLOBAL_RATE_SAFE) / global_ratio

        if global_offset < 0:
            global_thickness_safe = thickness1 + global_offset * 0.90  # 90% for safety
        else:
            global_thickness_safe = thickness1

        slog('global ratio = %s' % global_ratio)
    else:
        global_thickness_safe = 0

    # take thickest safe attenuator
    thickness_safe = max([0, local_thickness_safe, global_thickness_safe])
    slog('suggested safe thickness = %s' % thickness_safe)

    # find corresponding attenuation angle
    att_safe = 330
    for att, thickness in thickness_table.items():
        if thickness >= thickness_safe:
            att_safe = att
            break

    slog('suggested attenuation angle: %s' % att_safe)

    # drive to suggested attenuation angle and try to improve it if possible
    slog('continue with iterative attenuation algorithm ...')
    return iterativeAttenuationAlgo(att_safe)

def determineAveragedRates(max_samples=60, interval=0.2, timeout=30.0, log_success=True):

    def determineStandardDeviation(sum, sqr_sum, n): # n = sample count
        if n <= 1:
            return float('inf')

        s_sqr = sqr_sum/n - sum*sum/(n*n)
        return math.sqrt(s_sqr)

    def getStudentsFactor(n): # n = sample count
        # 90% confidence
        f = [float('inf'), 6.314, 2.92, 2.353, 2.132, 2.015, 1.943, 1.895, 1.86, 1.833, 1.812, 1.796, 1.782, 1.771, 1.761, 1.753, 1.746, 1.74, 1.734, 1.729, 1.725, 1.721, 1.717, 1.714, 1.711, 1.708, 1.706, 1.703, 1.701, 1.699, 1.697]
        if n <= len(f):
            return f[n - 1] # n to zero based index
        else:
            return f[-1]    # use last element

    def removeOutlier(v_min, v_max, avg, sum2, n):
        # effectively remove min or max value
        if (v_max - avg) > (avg - v_min):
            avg2 = (n * avg - v_max) / (n - 1)
            var2 = (sum2 - v_max * v_max) / (n - 1) - avg2 * avg2
        else:
            avg2 = (n * avg - v_min) / (n - 1)
            var2 = (sum2 - v_min * v_min) / (n - 1) - avg2 * avg2

        return (avg2, math.sqrt(var2)) # var -> std

    if max_samples < 1:
        max_samples = 1

    waitUntilSicsIs(ServerStatus.EAGER_TO_EXECUTE)

    startHistmem()
    try:
        time.sleep(1.0)

        local_rate = getMaxBinRate()
        global_rate = getGlobalMapRate()

        n = 0 # sample count
        local_rate_sum      = 0.0
        global_rate_sum     = 0.0
        local_rate_sqr_sum  = 0.0 # sum of squared rate
        global_rate_sqr_sum = 0.0

        local_rate_min  = +float('inf')
        local_rate_max  = -float('inf')
        global_rate_min = +float('inf')
        global_rate_max = -float('inf')

        local_rate_mean  = 0.0
        global_rate_mean = 0.0

        for i in xrange(max_samples):

            new_local_rate  = getMaxBinRate()
            new_global_rate = getGlobalMapRate()

            start = time.time()
            while (new_local_rate == local_rate) or (new_local_rate == 0) or (new_global_rate == global_rate) or (new_global_rate == 0):
                # check if detector has tripped
                if hasTripped():
                    return 0.0, 0.0

                if time.time() - start >= timeout:
                    raise Exception('Timeout in determineAveragedRates')

                time.sleep(0.5)
                new_local_rate = getMaxBinRate()
                new_global_rate = getGlobalMapRate()

            local_rate  = new_local_rate
            global_rate = new_global_rate
            slog('measurement:  local rate = %10.3f          global rate = %10.3f' % (local_rate, global_rate))

            if local_rate_min > local_rate:
                local_rate_min = local_rate
            if local_rate_max < local_rate:
                local_rate_max = local_rate

            if global_rate_min > global_rate:
                global_rate_min = global_rate
            if global_rate_max < global_rate:
                global_rate_max = global_rate

            n += 1 # increase sample count
            local_rate_sum      += local_rate
            global_rate_sum     += global_rate
            local_rate_sqr_sum  += (local_rate * local_rate)
            global_rate_sqr_sum += (global_rate * global_rate)

            local_rate_mean  = local_rate_sum / n
            global_rate_mean = global_rate_sum / n

            if n > 2:
                # https://de.wikipedia.org/wiki/Vertrauensintervall#Beispiele_f.C3.BCr_ein_Konfidenzintervall

                #local_rate_std  = determineStandardDeviation(local_rate_sum , local_rate_sqr_sum , n)
                #global_rate_std = determineStandardDeviation(global_rate_sum, global_rate_sqr_sum, n)

                # remove outlier
                local_rate_mean , local_rate_std  = removeOutlier(local_rate_min , local_rate_max , local_rate_mean , local_rate_sqr_sum , n)
                global_rate_mean, global_rate_std = removeOutlier(global_rate_min, global_rate_max, global_rate_mean, global_rate_sqr_sum, n)

                factor = getStudentsFactor(n-1) / math.sqrt(n-1)

                local_rate_err  = factor * local_rate_std
                global_rate_err = factor * global_rate_std

                slog('estimation:   local rate = %10.3f+-%-7.3f global rate = %10.3f+-%-7.3f' % (local_rate_mean, local_rate_err, global_rate_mean, global_rate_err))

                local_rate_intvl  = 2 * local_rate_err
                global_rate_intvl = 2 * global_rate_err

                if (local_rate_intvl < interval * local_rate_mean) and (global_rate_intvl < interval * global_rate_mean):
                    if log_success:
                        slog('successful estimation')
                    break

                if (n == max_samples) and log_success:
                    slog('unsuccessful estimation')

    finally:
        stopHistmem()

    return local_rate_mean, global_rate_mean

def checkedDrive(motor, value, useController=False):
    waitUntilSicsIs(ServerStatus.EAGER_TO_EXECUTE)

    if useController:
        controller = sics.getSicsController().findComponentController(motor)
        controller.drive(value)
    else:
        sics.drive(motor, value)

def loggedDrive(name, motor, value, unit, getter, useController=False):
    slog('Driving %s to %s %s ...' % (name, value, unit))
    checkedDrive(motor, value, useController)
    slog('%s is now at %s %s' % (name, getter(), unit))

def getData(getter, throw):
    try:
        return getter()

    except (Exception, SicsExecutionException) as e:
        if throw or isInterruptException(e):
            raise
        else:
            return '???'

def getIntData(path, throw=True, useController=False, useRaw=False):

    def getter():
        if useController:
            controller = sics.getSicsController().findComponentController(path)
            return controller.getValue().getIntData()
        elif useRaw:
            return int(sics.get_raw_value(path))
        else:
            return sics.getValue(path).getIntData()

    return getData(getter, throw)

def getFloatData(path, throw=True, useController=False, useRaw=False):

    def getter():
        if useController:
            controller = sics.getSicsController().findComponentController(path)
            return controller.getValue().getFloatData()
        elif useRaw:
            return float(sics.get_raw_value(path))
        else:
            return sics.getValue(path).getFloatData()

    return getData(getter, throw)

def getStringData(path, throw=True, useController=False, useRaw=False):

    def getter():
        if useController:
            controller = sics.getSicsController().findComponentController(path)
            return controller.getValue().getStringData()
        elif useRaw:
            return str(sics.get_raw_value(path))
        else:
            return sics.getValue(path).getStringData()

    return getData(getter, throw)

def getDataFilename(throw=True):
    name = getStringData('datafilename', throw)
    if len(name) > 17:
        return name[-17:] # only keep the name of the file e.g. QKK0000000.nx.hdf
    else:
        return name

def getMaxBinRate(throw=True):
    return getFloatData('/instrument/detector/max_binrate', throw) # pixel count rate

def getGlobalMapRate(throw=True):
    return getFloatData('/instrument/detector/total_maprate', throw) # global count rate

def getAtt(throw=True):
    return getIntData('att', throw)

def driveAtt(value):
    loggedDrive('attenuator', 'att', value, 'degree', getAtt)

def driveToSafeAtt():
    safe_att = max(ATT_VALUES)

    counter = 0
    while getAtt() != safe_att:
        try:
            counter += 1
            driveAtt(safe_att)

        except (Exception, SicsExecutionException) as e:
            if isInterruptException(e) or (counter >= 5):
                raise

def getBsPosition(id):
    return bsList[id].getPosition().name()

def driveBs(ids, action):
    # wrap input as list
    if not isinstance(ids, list):
        ids = [ids]

    action = str(action)

    # loop through each provided beam stop
    for id in ids:
        if action == ACTION.up:
            slog('Driving bs%s up ...' % id)
            bsList[id].up()
            slog('bs%s is now at %s' % (id, getBsPosition(id)))

        elif action == ACTION.down:
            slog('Driving bs%s down ...' % id)
            bsList[id].down()
            slog('bs%s is now at %s' % (id, getBsPosition(id)))

        else:
            slog('Cannot drive bs%s to %s' % (id, action))

def selBsHelper(beamstop, bx, bz, controller):
    # get command controller
    sicsController = sics.getSicsController()
    commandController = sicsController.findComponentController(controller)

    # configuring command properties
    if bx is not None:
        sics.hset(commandController, '/bx', bx)
    if bz is not None:
        sics.hset(commandController, '/bz', bz)

    sics.hset(commandController, '/bs', beamstop)

    count = 0
    while getIntData(controller + '/bs') != beamstop:
        time.sleep(0.1)
        count += 1
        if count > 100:
            raise Exception('Time out on receiving feedback on beam stop selection')

    count = 0
    while True:
        try:
            count += 1
            commandController.syncExecute()
            break

        except (Exception, SicsExecutionException) as e:
            if isInterruptException(e) or (count >= 20):
                raise

            slog('Retry selecting beam stop %s' % beamstop)
            time.sleep(1)
            waitUntilSicsIs(ServerStatus.EAGER_TO_EXECUTE)

    slog('beam stop is %s' % beamstop)

def selBs(beamstop):
    selBsHelper(beamstop, bx=None, bz=None, controller='/commands/beamstops/selbsn')

def getBsx(throw=True):
    return getFloatData('bsx', throw)

def driveBsx(baseValue, offset):
    loggedDrive('beamstop x', 'bsx', baseValue + offset, 'mm', getBsx)

def driveBsRailIn():
    driveBsx(-64, 0)

def driveBsRailOut():
    driveBsx(-64, 100)

def getBsz(throw=True):
    return getFloatData('bsz', throw)

# Updated by nxi. Removed the second argument: offset
def driveBsz(baseValue):
    loggedDrive('beamstop z', 'bsz', baseValue, 'mm', getBsz)

def selBsxz(beamstop, bx, bz):
    selBsHelper(beamstop, bx, bz, '/commands/beamstops/selbsxz')

def getDetPosition(throw=True):
    return getIntData('det', throw)

def getDetOffset(throw=True):
    return getFloatData('detoff', throw)

def driveDet(position, offset):
    # tolerance: 5mm and 1mm
    position_delta = 5
    offset_delta = 1

    drive_position = abs(position - getDetPosition()) > position_delta
    drive_offset = abs(offset - getDetOffset()) > offset_delta

    # drive det only if we needed to
    if drive_position or drive_offset:
        driveDhv1(ACTION.down)

        if drive_position:
            slog('Driving detector position to %s mm ...' % position)
            checkedDrive('det', position)

        if drive_offset:
            slog('Driving detector offset to %s mm ...' % position)
            checkedDrive('detoff', offset)

    # always drive voltage up even if detector wasn't moved
    driveDhv1(ACTION.up)
    slog('Detector is now at %s mm with offset %s mm' % (getDetPosition(), getDetOffset()))

def getDhv1():
    return dhv1.getValue()

def driveDhv1(action):
    action = str(action)

    # don't drive if it is already in position
    startingValue = getDhv1()
    if (action == ACTION.up and startingValue >= 2350.0) or (action == ACTION.down and startingValue == 0.0):
        slog('dhv1 is now at %s (no action is required)' % startingValue)
        return

    slog('Driving dhv1 %s ...' % action)

    waitUntilSicsIs(ServerStatus.EAGER_TO_EXECUTE)
    if action == ACTION.up:
        dhv1.up()
    elif action == ACTION.down:
        dhv1.down()
    elif action == 'reset':
        dhv1.reset()

    slog('dhv1 is now at %s' % getDhv1())

def getEntRotAp(throw=True):
    return getIntData('srce', throw)

def driveEntRotAp(value):
    loggedDrive('entrance aperture', 'srce', value, 'degree', getEntRotAp)

def getFlipper(throw=True):
    return getIntData('/instrument/flipper/flip_on', throw)

def driveFlipper(value):
    slog('Driving flipper to %s ...' % value)

    # make sure that value is int
    value = int(value)

    counter = 0
    while True:
        try:
            counter += 1

            waitUntilSicsIs(ServerStatus.EAGER_TO_EXECUTE)
            sics.handleInterrupt()

            sics.set('/instrument/flipper/set_flip_on', value)

            waitUntilSicsIs(ServerStatus.EAGER_TO_EXECUTE)
            sics.handleInterrupt()

            if getFlipper() != value:
                raise Exception('unable to set Flipper')

            break

        except (Exception, SicsExecutionException) as e:
            if isInterruptException(e) or (counter >= 20):
                raise

            slog('Retry setting Flipper')
            time.sleep(1)

    slog('Flipper is set to %s' % getFlipper())

def getGuideConfig(throw=True):
    return getStringData('/commands/optics/guide/configuration', throw)

def driveGuide(value):
    if value not in GUIDE_CONFIG:
        slog('[WARNING] unknown guide configuration: ' + value)

    # set target configuration
    sics.set('/commands/optics/guide/configuration', value)

    slog('Moving guide to ' + value)

    sicsController = sics.getSicsController()
    controller = sicsController.findComponentController('/commands/optics/guide')

    # setting of configuration and starting a command are committed to SICS via different communication channels
    # in order to make those in sync, we need to wait for the configuration to settle
    time.sleep(0.5)

    counter = 0
    while True:
        try:
            counter += 1

            waitUntilSicsIs(ServerStatus.EAGER_TO_EXECUTE)
            sics.handleInterrupt()

            controller.syncExecute()
            sics.handleInterrupt()
            break

        except (Exception, SicsExecutionException) as e:
            if isInterruptException(e) or (counter >= 20):
                raise

            slog('Retry moving guide')
            time.sleep(1)

    slog('Guide is moved to ' + getGuideConfig())

def getJulabo(throw=True):
    return getFloatData('/sample/tc1', throw, useController=True)

def driveJulabo(value):
    loggedDrive('Julabo', '/sample/tc1', float(value), 'Kelvin', getJulabo, useController=True)

def getL1(throw=True):
    return getFloatData('l1', throw)

def getL2(throw=True):
    return getFloatData('l2', throw)

def getLambda(throw=True):
    return getFloatData('/instrument/velocity_selector/wavelength_nominal', throw)

def getSampleName(throw=True):
    return getStringData('samplename', throw)

def getSampleNumber(throw=True):
    return getIntData('/sample/sampleNum', throw, useController=True)

def getSamplePositions(throw=True):
    return getIntData('samx posit_count', throw, useRaw=True)

def getSamx(throw=True):
    return getFloatData('samx', throw)

def driveTesla(value, speed, wait_sec):
    slog('Driving Tesla (value: %s, speed: %s) ...' % (value, speed))

    waitUntilSicsIs(ServerStatus.EAGER_TO_EXECUTE)

    sics.execute('oxfordseths on')
    sleep(35) # interruptable sleep
    sics.execute('oxfordsetrate %s' + speed)
    sics.execute('oxfordsetfield %s' + value)
    sleep(float(wait_sec))
    sics.execute('oxfordseths off')
    sleep(35)

def startHistmem():
    slog('Starting histmem ...')

    waitUntilSicsIs(ServerStatus.EAGER_TO_EXECUTE)

    sics.execute('histmem mode unlimited')
    time.sleep(1.0)
    sics.execute('histmem start')

def stopHistmem():
    slog('Stopping histmem ...')

    sics.execute('histmem stop')
    time.sleep(0.5)

def scan(mode, preset):
    controllerPath = '/commands/scan/runscan'

    if mode == ACQUISITION_MODE.unlimited:
        sics.execute('hset ' + controllerPath + '/mode unlimited', 'scan')
    elif mode == ACQUISITION_MODE.time:
        sics.execute('hset ' + controllerPath + '/mode time', 'scan')
    elif mode == ACQUISITION_MODE.counts:
        sics.execute('hset ' + controllerPath + '/mode counts', 'scan')
    elif mode == ACQUISITION_MODE.bm_counts:
        sics.execute('hset ' + controllerPath + '/mode MONITOR_1', 'scan')
    else:
        raise Exception('unsupported scan mode: ' + mode)

    # set preset
    if preset is not None:
        sics.execute('hset ' + controllerPath + '/preset %s' % preset, 'scan')
    elif mode != ACQUISITION_MODE.unlimited:
        raise Exception('preset is required for mode: ' + mode)

    # ensure that ba is disabled
    sics.execute('histmem ba disable', 'scan')

    syncScan(controllerPath)

def scanBA(min_time, max_time, counts, bm_counts):
    def getIntOrDefault(value, default):
        if value is not None:
            return int(value)
        else:
            return default

    controllerPath = '/commands/scan/runscan'

    sics.execute('hset ' + controllerPath + '/mode unlimited', 'scan')

    sics.execute('histmem ba roi total', 'scan')
    sics.execute('histmem ba monitor 1', 'scan')

    sics.execute('histmem ba mintime %i'     % getIntOrDefault(min_time ,  0), 'scan')
    sics.execute('histmem ba maxtime %i'     % getIntOrDefault(max_time , -1), 'scan')
    sics.execute('histmem ba maxdetcount %i' % getIntOrDefault(counts   , -1), 'scan')
    sics.execute('histmem ba maxbmcount %i'  % getIntOrDefault(bm_counts, -1), 'scan')

    try:
        sics.execute('histmem ba enable', 'scan')
        syncScan(controllerPath)

    finally:
        # ensure that ba is disabled afterwards
        sics.execute('histmem ba disable', 'scan')

def syncScan(controllerPath):
    # configuring scan properties
    sics.execute('hset ' + controllerPath + '/datatype ' + 'HISTOGRAM_XY', 'scan')
    sics.execute('hset ' + controllerPath + '/savetype ' + 'save'        , 'scan')
    sics.execute('hset ' + controllerPath + '/force '    + 'true'        , 'scan')

    # dummy scan
    sics.execute('hset ' + controllerPath + '/scan_variable dummy_motor', 'scan')
    sics.execute('hset ' + controllerPath + '/scan_start ' + '0', 'scan')
    sics.execute('hset ' + controllerPath + '/scan_stop '  + '0', 'scan')
    sics.execute('hset ' + controllerPath + '/numpoints '  + '1', 'scan')

    # wait to make the settings settle
    time.sleep(1)
    waitUntilSicsIs(ServerStatus.EAGER_TO_EXECUTE)

    # synchronously run scan
    sicsController = sics.getSicsController()
    scanController = sicsController.findComponentController(controllerPath)
    scanController.syncExecute()
