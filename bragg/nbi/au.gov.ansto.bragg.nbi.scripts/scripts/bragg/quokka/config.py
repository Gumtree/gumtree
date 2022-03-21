from gumpy.commons import sics
from gumpy.commons.logger import log

# import everything from bragg/quokka/quokka.py
from bragg.quokka.quokka import *

from org.gumtree.gumnix.sics.control import ServerStatus

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
        if not self.wavelength is None:
            log('run nvs_lambda ' + str(self.wavelength))
            self.run_nvs_lambda(self.wavelength)
        cmd = 'drive'
        if self.need_drive_det():
            cmd += ' det ' + str(self.det)
            if not self.det_offset is None:
                cmd += ' detoff ' + str(self.det_offset)
            else:
                cmd += ' detoff 0'
#         if not self.wavelength is None:
#             cmd += ' nvs_lambda ' + str(self.wavelength)
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
        if not self.wavelength is None:
            self.check_nvs_lambda(self.wavelength)
                

    def run_nvs_lambda(self, val):
        sics.execute('run nvs_lambda ' + str(val))
        
    def check_nvs_lambda(self, val):
        timeout = 600
        interval = 2
        count = 0
        while count < timeout :
            try:
                cur = sics.get_raw_value('nvs_lambda')
                if sics.isInterrupt():
                    break
#                 pre = sics.get_raw_value('nvs_lambda precision')
                pre = 0.1
                if abs(cur - val) < pre:
                    log('wavelength is ' + str(cur))
                    return True
                else:
                    try:
                        time.sleep(interval)
                        count += interval
                    except KeyboardInterrupt as ei:
                        log('Interrupted')
                        raise Exception, 'interrupted'
                        break;
            except:
                try:
                    time.sleep(interval)
                    count += interval
                except KeyboardInterrupt as ei:
                    log('Interrupted')
                    raise Exception, 'interrupted'
                    break;
        if sics.isInterrupt():
            sics.clearInterrupt()
            raise Exception, 'interrupted'
        sics.execute('stopexe nvs_lambda')
        log('WARNING: timeout in driving nvs_lambda, but choose to continue.')
        
    def multi_set(self):
        if not self.wavelength is None:
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

    def drive(self):
        if self.__is_dirty__ :
            driveAtt(330)
        else:
            return
        try:
            sics.clearInterrupt()
            log('starting configuration')
            
            if not self.bs is None:
                selBs(self.bs)
            if self.need_drive_guide():
                driveGuide(self.guide)
            self.multi_drive()
            self.multi_set()
            
        finally:
            self.clear()
        log('skipping status checking')
#    while not sics.get_status().equals(ServerStatus.EAGER_TO_EXECUTE) :
#            time.sleep(0.3)
        sics.handleInterrupt()
        log('configuration is finished')
