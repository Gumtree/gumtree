###############################################################################
# Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     Tony Lam (Bragg Institute) - initial API and implementation
###############################################################################

from gumpy.commons.logger import log
from gumpy.commons import sics
from bragg.quokka import quokka
from bragg.quokka.quokka import *
from time import sleep
from org.gumtree.util.eclipse import WorkspaceUtils
from xml.etree.ElementTree import Element, SubElement, ElementTree, tostring
import traceback
from time import strftime, localtime
"""
Quokka Workflow module contains helper method to glue the Python code with 
the Quokka scan Java workflow code.
"""

ATT_STATE_MANAGER = 'stateManager'
PYTHON_DRIVERS_PATH = '/SICS/Drivers/Python'

# Check to see if the workflow need to drive the sample stage
def isDriveSampleStage():

    if engineContext.getAttribute(ATT_STATE_MANAGER) != None:
        stateManager = engineContext.getAttribute(ATT_STATE_MANAGER)
        return not stateManager.isFixedSamplePosition()
    
    # Always drive if no hint from the state manager
    return True

# Check against experiment model if transmission run is runnable
def isRunTransmission(runId):

    if engineContext.getAttribute(ATT_STATE_MANAGER) != None:
        stateManager = engineContext.getAttribute(ATT_STATE_MANAGER)
        return stateManager.checkTransmissionRunnable(runId)
    
    # Always run if no hint from the state manager
    return True

# Check against experiment model if scattering run is runnable
def isRunScattering(runId):
    
    if engineContext.getAttribute(ATT_STATE_MANAGER) != None:
        stateManager = engineContext.getAttribute(ATT_STATE_MANAGER)
        return stateManager.checkScatteringRunnable(runId)
    
    # Always run if no hint from the state manager
    return True

# Get the latest preset from the UI
def getScatteringPreset(runId, defaultPreset):
    
    if engineContext.getAttribute(ATT_STATE_MANAGER) != None:
        stateManager = engineContext.getAttribute(ATT_STATE_MANAGER)
        return stateManager.getScatteringPreset(runId)
    
     # Return default if state manager is missing
    return defaultPreset
    
# Update the GumTree experiment model with data file name
def updateTransmissionDetails(runId, dataFile, wavelength=0, att=0, l1=0, l2=0):
    if engineContext.getAttribute(ATT_STATE_MANAGER) != None:
        stateManager = engineContext.getAttribute(ATT_STATE_MANAGER)
        stateManager.setTransmissionDetails(runId, dataFile, float(wavelength), float(att), float(l1), float(l2))

# Update the GumTree experiment model with data file name
def updateScatteringDetails(runId, dataFile, wavelength=0, att=0, l1=0, l2=0):
    if engineContext.getAttribute(ATT_STATE_MANAGER) != None:
        stateManager = engineContext.getAttribute(ATT_STATE_MANAGER)
        stateManager.setScatteringDetails(runId, dataFile, float(wavelength), float(att), float(l1), float(l2))
        
# Update the GumTree experiment model with run state
def updateTransmissionRunState(runId, running):
    if engineContext.getAttribute(ATT_STATE_MANAGER) != None:
        stateManager = engineContext.getAttribute(ATT_STATE_MANAGER)
        stateManager.setRunningTransmission(runId, running)

# Update the GumTree experiment model with run state
def updateScatteringRunState(runId, running):
    if engineContext.getAttribute(ATT_STATE_MANAGER) != None:
        stateManager = engineContext.getAttribute(ATT_STATE_MANAGER)
        stateManager.setRunningScattering(runId, running)
        
def startAcquistion():
    if engineContext.getAttribute(ATT_STATE_MANAGER) != None:
        try:
            stateManager = engineContext.getAttribute(ATT_STATE_MANAGER)
            stateManager.setAcquistionStarted()
        except:
            pass
    
def runQuokkaScan(acquisitionEntries, reserve=False, evnVal=None):
    acquisitionList = list(acquisitionEntries)
    if reserve:
        acquisitionList.reverse()
    isConfigRun = False
    for acquisitionGroup in acquisitionList:
        if acquisitionGroup['type'] == 'sampleEnv':
            runSampleEnvironmentScan(acquisitionGroup)
        if acquisitionGroup['type'] == 'config':
            runMultiConfigScan(acquisitionGroup)
            isConfigRun = True
    if isConfigRun :
        stateManager = engineContext.getAttribute(ATT_STATE_MANAGER)
        if stateManager != None:
            try:
                html = getAcqEntryHtml(list(acquisitionEntries), evnVal)
                print html
                stateManager.exportAcquisitionEntryHtml(html)
            except:
                traceback.print_exc(file=sys.stdout)
                pass        
        
def getAcqEntryHtml(acqEntry, evnVal):
    stateManager = engineContext.getAttribute(ATT_STATE_MANAGER)
    table = Element('table')
    table.set('align', 'center')
    table.set('border', '1')
    table.set('cellpadding', '2')
    table.set('cellspacing', '0')
    table.set('class', 'xmlTable')
    table.set('style', 'table-layout:fixed; width:100%; word-wrap:break-word')
    tr = SubElement(table, 'tr')
    th = SubElement(tr, 'th')
    th.set('colspan', '2')
    if not evnVal is None:
        th.text = evnVal
        
    for configGroup in acqEntry:
        configName = configs[configGroup['target']]['name']
        th = SubElement(tr, 'th')
#        t_idx = configSetting.find('-')
#        th.text = configSetting[t_idx + 1: t_idx + 12]
        th.text = configName
    tr = SubElement(table, 'tr')
    th = SubElement(tr, 'th')
    th.text = 'Pos'
    th.set('style', 'width: 5%;')
    th = SubElement(tr, 'th')
    th.set('style', 'width: 20%;')
    th.text = 'Sample'
    configWidth = 75 / len(acqEntry)
    for i in xrange(len(acqEntry)):
        th = SubElement(tr, 'th')
        th.set('style', 'width: ' + str(configWidth) + '%;')
        th.text = 'Scattering'
    
    sps = acqEntry[0]['contents']
    for i in xrange(len(sps)):
        sp = sps[i]
        tr = SubElement(table, 'tr')
        td = SubElement(tr, 'td')
        td.text = str(sp['sample'])
        td = SubElement(tr, 'td')
        td.text = samples[sp['sample']]['name']
        for configGroup in acqEntry:
            item = configGroup['contents'][i]
            run_id = item['runId']
            td = SubElement(tr, 'td')
            scatt = stateManager.getScatteringDetails(run_id)
            if not scatt is None:
                td.text = scatt
    html = tostring(table)
    span = Element('span')
    span.set('class', 'class_span_tablefoot')
    span.text = 'Scan report created at ' + strftime("%Y-%m-%dT%H:%M:%S", localtime())
    if not evnVal is None:
        span.text += ', ' + evnVal
    html += tostring(span)
    return html
    
def runSampleEnvironmentScan(sampleEvnGroup):
    # Gets the sample environment settings
    evnSetting = sampleEnvironments[sampleEvnGroup['target']]
    controller = evnSetting['controller']
    preset = evnSetting[sampleEvnGroup['setting']]['preset']
    waitTime = evnSetting[sampleEvnGroup['setting']]['wait']
    
    # Drive drivable controller
    deviceController = sics.getDeviceController(controller)
    if (deviceController == None):
        log('Driving controller ' + controller + ' to set point ' + str(preset))
        driverPath = WorkspaceUtils.getFolder(PYTHON_DRIVERS_PATH).getFullPath().toString()
        driverPath = driverPath.replace('/', '.')[1:]
        exec('from ' + driverPath + ' import ' + controller)
        exec(controller + '.drive(preset)')
    else:
        log('Driving controller ' + controller + ' to set point ' + str(preset))
        sics.drive(controller, preset)
    # Stabilisation
    log('Stabilising the controller for ' + str(waitTime) + ' sec')
    sleep(waitTime)
    
    # Recurrsively run (for effectiveness we reverse the configuration on every second run)
    if sampleEvnGroup['setting'] % 2 == 1:
        runQuokkaScan(sampleEvnGroup['contents'], True, controller + '=' + str(preset))
    else:
        runQuokkaScan(sampleEvnGroup['contents'], False, controller + '=' + str(preset))
    
def runMultiConfigScan(configGroup):
    configSetting = configs[configGroup['target']]
    acqEntries = configGroup['contents']
    
    # Check if we need to run transmission
    shouldRunTransmission = False
    for acqEntry in acqEntries:
        shouldRunTransmission = shouldRunTransmission or isRunTransmission(acqEntry['runId'])
    # Check if we need to run scattering
    shouldRunScattering = False
    for acqEntry in acqEntries:
        shouldRunScattering = shouldRunScattering or isRunScattering(acqEntry['runId'])
    # Drive to instrument config
    shouldRunConfiguration = shouldRunTransmission or shouldRunScattering
    if shouldRunConfiguration:
        log('Set instrument to config ' + configSetting['name'])
        configSetting['mainRoutine']()
    else:
        log('Skip driving to configuration ' + configSetting['name'])
        return
    
    # Transmission mode
    if shouldRunTransmission:
        log('Set instrument to transmission mode')
        configSetting['transmissionRoutine']()
        runTransmission(acqEntries, configSetting)
    else:
        log('Skip transmission mode')
    
    # Scattering mode
    if shouldRunScattering:
        log('Set instrument to scattering mode')
        configSetting['scatteringRoutine']()
        runScattering(acqEntries, configSetting)
    else:
        log('Skip scattering mode')
        
    if engineContext.getAttribute(ATT_STATE_MANAGER) != None:
        try:
            stateManager = engineContext.getAttribute(ATT_STATE_MANAGER)
            stateManager.setConfigSetFinished(acqEntries[len(acqEntries) - 1]['runId'])
        except:
            pass
        
def runTransmission(acqEntries, configSetting):
    
    for seqIndex in range(len(acqEntries)):
        acqEntry = acqEntries[seqIndex]
        sampleEntry = samples[acqEntry['sample']]
        
        # Click if runnable
        if isRunTransmission(acqEntry['runId']):
            # Set sample
            quokka.setSample(sampleEntry['position'], sampleEntry['name'], sampleEntry['description'], sampleEntry['thickness'], isDriveSampleStage())
            # Set transmission flag
            sics.set('transmissionflag', 1)
            sleep(0.1)
            # Set attenuation
            # Acquire data
            mode = configSetting['transmissionMode']
            preset = configSetting['transmissionPreset']
            updateTransmissionRunState(acqEntry['runId'], True)
            log('Start transmission run on ' + sampleEntry['name'] + ' ...')
            dataFile = scan(mode, dataType.HISTOGRAM_XY, preset, getForcedScanValue(sampleEntry['type']))
            # Update data file name
            updateTransmissionRunState(acqEntry['runId'], False)
            updateTransmissionDetails(acqEntry['runId'], dataFile[-14:-7], getLambdaValue(), getAttValue(), getL1Value(), getL2Value())
            # Print instrument state
            sleep(0.2)
            quokka.printQuokkaSettings()
            
def runScattering(acqEntries, configSetting):
    
    for seqIndex in range(len(acqEntries)):
        acqEntry = acqEntries[seqIndex]
        sampleEntry = samples[acqEntry['sample']]
        
        # Click if runnable
        if isRunScattering(acqEntry['runId']):
            # Set sample
            quokka.setSample(sampleEntry['position'], sampleEntry['name'], sampleEntry['description'], sampleEntry['thickness'], isDriveSampleStage())
            # Clear transmission flag
            sics.set('transmissionflag', 0)
            sleep(0.1)
            # Set attenuation
            log('Finding safe attenuation value ...')
            startingAttenuation = driveSafeAttenuation(configSetting['manualAttenuationAlgorithm'], configSetting['startingAttenuation'])
            # Acquire data
            mode = configSetting['scatteringMode']
            preset = getScatteringPreset(acqEntry['runId'], acqEntry['preset'])
            updateScatteringRunState(acqEntry['runId'], True)
            log('Start scattering run on ' + sampleEntry['name'] + ' (mode: ' + mode.key + ', preset: ' + str(preset) + ') ... ')
            # Scan (force scan if this is a dark current scan)
            dataFile = scan(mode, dataType.HISTOGRAM_XY, preset, getForcedScanValue(sampleEntry['type']))
            # Update data file name
            updateScatteringRunState(acqEntry['runId'], False)
            updateScatteringDetails(acqEntry['runId'], dataFile[-14:-7], getLambdaValue(), getAttValue(), getL1Value(), getL2Value())
            # Print instrument state
            sleep(0.2)
            # Drive attenuation back safer value
            log('Drive attenuation back to safe value ...')
            if startingAttenuation is None:
                startingAttenuation = 300
            driveAtt(startingAttenuation)
            quokka.printQuokkaSettings()

def getForcedScanValue(type):
    #    if type == 'dark_current':
    #        return 'true'
    #    return 'false'
    # [GUMTREE-421] Set to true to allow pausing on monitor count mode
    return 'true'
    
if __name__ == '__main__':

    print 'This module provides template functions for the Quokka Workflow.'
     
        