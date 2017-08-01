from gumpy.commons import sics
from gumpy.commons.logger import log

# import everything from bragg/quokka/quokka.py
from bragg.quokka.quokka import *


class ConfigSystem :

    _property_list = ['__is_dirty__',
                       'att',
                       'wavelength',
                       'wavelength_spread',
                       'det',
                       'det_offset',
                       'srce',
                       'guide',
                       'apx',
                       'bs',
                       'beamcenterx',
                       'beamcenterz',
                       ]
    def __init__(self):
        self.__is_dirty__ = False
        self.att = None
        self.wavelength = None
        self.wavelength_spread = None
        self.det = None
        self.det_offset = None
        self.srce = None
        self.guide = None
        self.apx = None
        self.bs = None
        self.beamcenterx = None
        self.beamcenterz = None

    def __setattr__(self, name, value):
        if name == 'att' and value != 330 and not value is None:
            log('att can not be changed; use 330 instead')
            return
        if name == 'bs' and not value is None :
            if value < 1 or value > 6:
                self.clear()
                raise Exception, 'beamstop selection must be an integer from 1 to 6'
        if name == 'guide' :
            if hasattr(value, 'key'):
                value = value.key
        if not ConfigSystem._property_list.__contains__(name):
            raise Exception, 'property not allowed: ' + str(name)
        self.__dict__[name] = value
        if name != '__is_dirty__' and value != None:
            self.__dict__['__is_dirty__'] = True


    def clear(self):
        self.att = None
        self.wavelength = None
        self.wavelength_spread = None
        self.det = None
        self.det_offset = None
        self.srce = None
        self.guide = None
        self.apx = None
        self.bs = None
        self.beamcenterx = None
        self.beamcenterz = None
        self.__is_dirty__ = False

    def multi_drive(self):
        cmd = 'drive'
        if self.need_drive_det():
            cmd += ' det ' + str(self.det)
            if not self.det_offset is None:
                cmd += ' detoff ' + str(self.det_offset)
            else:
                cmd += ' detoff 0'
        if not self.wavelength is None:
            cmd += ' nvs_lambda ' + str(self.wavelength)
        if not self.srce is None:
            cmd += ' srce ' + str(self.srce)
        if not self.apx is None:
            cmd += ' apx ' + str(self.apx)
        if not cmd == 'drive':
            log(cmd)
            res = sics.run_command_timeout(cmd, True, 15*60)
            if not res is None and res.find('Full Stop') >= 0:
                raise Exception, res
            log('finished multi-drive')

    def multi_set(self):
        if not self.wavelength is None:
            log('set wavelength to ' + str(self.wavelength))
            sics.set('/instrument/velocity_selector/wavelength', self.wavelength)
        if not self.wavelength_spread is None:
            log('set wavelength_spread to ' + str(self.wavelength_spread))
            sics.set('/instrument/velocity_selector/wavelength_spread', self.wavelength_spread)
        if not self.beamcenterx is None :
            log('set beam centre x to ' + str(self.beamcenterx))
            sics.set('beamcenterx', self.beamcenterx)
        if not self.beamcenterz is None :
            log('set beam centre z to ' + str(self.beamcenterz))
            sics.set('beamcenterz', self.beamcenterz)

    def need_drive_det(self):
        if self.det is None:
            return False
        else:
            precision = 5
            offsetPrecision = 1
            position = self.det
            offset = self.det_offset
            if offset is None:
                offset = 0
            shouldDrive = (position > getDetPosition() + precision) or (position < getDetPosition() - precision)
            shouldDrive = (shouldDrive) or ((offset > getDetOffset() + offsetPrecision) or (offset < getDetOffset() - offsetPrecision))
            return shouldDrive

    def need_drive_guide(self):
        return self.guide != None and self.guide !=  getGuideConfig()

    def test_dhv1(self, key):
            # Test drive
            startingValue = getDhv1()
            precision = 20
            log('Test drive dhv1 to ' + key + ' ...')
            sics.getSicsController().clearInterrupt()
            sics.execute('dhv1 ' + key)
            # Wait for 6 sec
            time.sleep(6)
            hasInterrupted = sics.getSicsController().isInterrupted() == 1
            # Stop test drive
            log('Stopping test drive')
            sics.execute('INT1712 3')
            time.sleep(1)
            currentValue = getDhv1()
            # Don't go any further if someone has interrrupted the test drive
            if hasInterrupted:
#            log('Test drive was interrupted')
                raise Exception, 'dhv1 ' + key + ' interrupted'
#            print ('currentValue: ' + str(currentValue) + ', startingValue: ' +  str(startingValue));
            # Test if the current value is within the precision (more than half of voltage step size, which is about half of 30V)
            if (startingValue + precision >= currentValue) and (startingValue - precision <= currentValue):
                log('Dhv1 needs to be reset')
                sics.execute('dhv1 reset')
                time.sleep(1)

            # Actual drive
            sics.getSicsController().clearInterrupt()

    def drive(self):
        if self.__is_dirty__ :
            driveAtt(330)
        else:
            return
        try:
            sics.clearInterrupt()
            log('starting configuration')
            if self.need_drive_det():

                ########### below code drop dhv1 and select beam stop together ###########
                precision = 20
                safe_voltage = 800
                working_voltage = 2100
                startingValue = getDhv1()
                if startingValue > safe_voltage:
                    self.test_dhv1('down')
                    startingValue = getDhv1()
                    log('Driving dhv1 to down ...')
                    sics.execute('dhv1 down')
                    time.sleep(1)

                if not self.bs is None:
                    selBs(self.bs)

                if startingValue > safe_voltage:
                    time_count = 0
                    time_out = 30
                    while getDhv1() == startingValue and time_count < time_out:
                        time.sleep(0.5)
                        time_count += 0.5
                    if getDhv1() == startingValue :
                        raise Exception, 'failed to start dropping dhv1'
                    time_count = 0
                    time_out = 600
                    while getDhv1() > safe_voltage and time_count < time_out:
                        time.sleep(0.5)
                        time_count += 0.5
                    if getDhv1() > safe_voltage:
                        raise Exception, 'time out dropping dhv1'
                    log('dhv1 is now down')

                ########### below drive det and other motors ###########
                self.multi_drive()

                ########### below code raise dhv1 and drive guide together ###########
                self.test_dhv1('up')
                startingValue = getDhv1()
                log('Driving dhv1 to up ...')
                sics.execute('dhv1 up')
                time.sleep(1)
                if self.need_drive_guide():
                    driveGuide(self.guide)
                time_count = 0
                time_out = 30
                while getDhv1() == startingValue and time_count < time_out:
                    time.sleep(0.5)
                    time_count += 0.5
                if getDhv1() == startingValue :
                    raise Exception, 'failed to start raising dhv1'
                time_count = 0
                time_out = 600
                while getDhv1() < working_voltage and time_count < time_out:
                    time.sleep(0.5)
                    time_count += 0.5
                if getDhv1() < working_voltage :
                    raise Exception, 'time out raising dhv1'
                log('dhv1 is now up')

                self.multi_set()
            else:
                if not self.bs is None:
                    selBs(self.bs)
                if self.need_drive_guide():
                    driveGuide(self.guide)
                self.multi_drive()
                self.multi_set()
        finally:
            self.clear()
        while sics.getStatus() != 'EAGER TO EXECUTE':
            time.sleep(0.3)
        sics.handleInterrupt()
        log('configuration is finished')
