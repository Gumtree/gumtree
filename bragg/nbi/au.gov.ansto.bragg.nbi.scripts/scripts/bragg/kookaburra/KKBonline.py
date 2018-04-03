# Script control setup area
# script info
__script__.title = 'KKB Realtime Reduction'
__script__.version = '1.0'



from math import sqrt, sin, exp
from datetime import datetime
from bisect import bisect_left
from __builtin__ import max as builtin_max
from __builtin__ import min as builtin_min

# TO ADD A FORTH PLOT
try :
    Plot1.close()
except:
    pass
#Plot_START =  plot()



'''

    INPUT

'''



combine_tube0 = Par('bool', True)
combine_tube0.title = '  Tube 0'

combine_tube1 = Par('bool', True)
combine_tube1.title = '    Tube 1'

combine_tube2 = Par('bool', True)
combine_tube2.title = '    Tube 2'

combine_tube3 = Par('bool', True)
combine_tube3.title = '    Tube 3'

combine_tube4 = Par('bool', True)
combine_tube4.title = '    Tube 4'

'''        
g0 = Group('Select Tube(s) of Interest:')
g0.numColumns = 5
g0.add(combine_tube0, combine_tube1, combine_tube2, combine_tube3, combine_tube4)
'''


use_beammonitor = Par('bool', False)
use_beammonitor.title = 'Use Beam Monitor'

defaultMCR = Par('float', '8500')
defaultMCR.title = 'Default MCR'

TWideMarker = Par('float', '2e-3')
TWideMarker.title = 'TWide Marker (1/A)'

convert2q = Par('bool', True)
convert2q.title = 'Convert to q'

sort_scanvar = Par('bool', True)
sort_scanvar.title = 'Sort Scan Variable'

'''
g1 = Group('Advanced Settings')
g1.numColumns = 3
g1.add(use_beammonitor, defaultMCR, TWideMarker)
'''
       

# SAMPLE INPUT


Thickness = Par('float', 'NaN')
Thickness.title = ''
Thickness.enabled = False
Thickness_Patching = Par('bool', False, command='Thickness.enabled = Thickness_Patching.value')
Thickness_Patching.title = ''
Thickness_FromFile = Par('float', 'NaN')
Thickness_FromFile.title = 'Sample Thickness (mm)'
Thickness_FromFile.enabled = False

SampleName = Par('string', 'patch?')
SampleName.title = ''
SampleName.enabled = False
SampleName_Patching = Par('bool', False, command='SampleName.enabled = SampleName_Patching.value')
SampleName_Patching.title = ''
SampleName_FromFile = Par('string', 'NaN')
SampleName_FromFile.title = 'Sample Name'
SampleName_FromFile.enabled = False

SampleDescr = Par('string', 'patch?')
SampleDescr.title = ''
SampleDescr.enabled = False
SampleDescr_Patching = Par('bool', False, command='SampleDescr.enabled = SampleDescr_Patching.value')
SampleDescr_Patching.title = ''
SampleDescr_FromFile = Par('string', 'NaN')
SampleDescr_FromFile.title = 'Sample Description'
SampleDescr_FromFile.enabled = False

SampleBkg = Par('float', 'NaN')
SampleBkg.title = ''
SampleBkg.enabled = False
SampleBkg_Patching = Par('bool', False, command='SampleBkg.enabled = SampleBkg_Patching.value')
SampleBkg_Patching.title = ''
SampleBkg_FromFile = Par('string', 'NaN')
SampleBkg_FromFile.title = 'Ambient Background'
SampleBkg_FromFile.enabled = False

samIgnorePts = Par('string', '')
samIgnorePts.title = 'Ignore Sample Data Points' 
samIgnorePts.colspan = 1

g2 = Group('SAMPLE')
g2.numColumns = 3
g2.add(SampleName_FromFile,SampleName_Patching,SampleName,
       SampleDescr_FromFile,SampleDescr_Patching,SampleDescr,
       Thickness_FromFile,Thickness_Patching,Thickness,      
       SampleBkg_FromFile,SampleBkg_Patching,SampleBkg,
       samIgnorePts)



def __run_script__(fns):
    global __script__
#    datasets = __DATASOURCE__.getSelectedDatasets()
    datasets = fns
    for sds in datasets:
            ds = Dataset(str(sds))
            SampleName_FromFile.value   = str(ds['entry1/sample/name'             ])
            SampleDescr_FromFile.value  = str(ds['entry1/sample/description'      ])
            Thickness_FromFile.value    = float(ds['entry1/sample/thickness'        ])
            SampleBkg_FromFile.value    = float(ds['entry1/experiment/bkgLevel'     ])
        
#    dsFilePaths = [str(ds.getLocation()) for ds in __DATASOURCE__.getSelectedDatasets()]
    dsFilePaths = fns
        
    if len(dsFilePaths) == 0:
        print 'Warning: no Sample Scans were selected'
        return
        
    # get name of first sample file
    filepath = dsFilePaths[0]
    mtime = os.path.getmtime(filepath)
    mtime_str = datetime.fromtimestamp(int(mtime)).strftime('%Y-%m-%dT%H:%M:%S')
    filename = os.path.basename(filepath)
    filename = filename[:filename.find('.nx.hdf')]
    
#    path = 'V:/shared/KKB Logbook/Temp Plot Data Repository/'    
    rds = df[filepath]
    if len(rds) <= 2:
        try:
            Plot1.set_dataset(arange(1))
            Plot1.title = filename + ': number of scan point is ' + str(len(rds)) + ', I-Q plot is not available.'
            Plot1.x_label = 'q (1/Angstrom)'
            Plot1.y_label = 'intensity (counts/sec)'
            __script__.model.setLastModifiedTimestamp(long((mtime + 1) * 1000))
        finally:
            rds.close()
            df.datasets.clear()
        return
    ds = None
    try:        
        ds = LoadNxHdf(dsFilePaths)
       
        ds.MeasurementTime()
        if sort_scanvar.value:
            ds.SortAngles()    
        ds = RemoveIgnoredRanges(ds, samIgnorePts.value)
        
        if convert2q.value:        
            ds.FindZeroAngle()
            ds.DetermineQVals()
            ds.FindTWideCtr()
        else:
            ds.Qvals = copy(ds.Angle)
                
    #    ds.SaveRaw(path + filename + '.dat')
        
        PlotDataset(Plot1, ds, filename + ': ' + ds.SampleName + ' - ' + str(mtime_str))
        
    #    Plot3.add_y_marker(ds.SampleBkg, 6000, 'blue')    
        Plot1.add_y_marker(ds.SampleBkg, 6000, 'blue')
    
        __script__.model.setLastModifiedTimestamp(long((mtime + 1) * 1000))
    finally:
        if not ds is None:
            ds.__ds__.close()
        df.datasets.clear()


#########################################################################################
# EMPTY SAMPLE COMPARTMENT

empFiles = Par('string', '')
empFiles.title = 'Files' 
empFilesTakeBtn = Act('empFilesTake()', 'Select As Empty Sample Container')

empIgnorePts = Par('string', '')
empIgnorePts.title = 'Ignore Data Points' 

empLevel = Par('float', '0')
empLevel.title = 'Empty Level'
empLevel_Error = Par('float', '0')
empLevel_Error.title = ' Actual Error'
empLevel_Error.enabled = False
empLevel_tailpoints = Par('int', '5')
empLevel_tailpoints.title = 'Tail Points'

g3 = Group('EMPTY SAMPLE CONTAINER')
g3.numColumns = 2
g3.add(empFiles, empFilesTakeBtn,empIgnorePts,  empLevel, 
        empLevel_tailpoints,empLevel_Error)


def empFilesTake():
    fns = None
#    for sds in __DATASOURCE__.getSelectedDatasets():
    for sds in __selected_files__ :
        basename = os.path.basename(str(sds))
        basename = basename[:basename.find('.nx.hdf')]

        if fns is None:
            fns = basename
        else:
            fns += ', ' + basename
            
    if fns is None:
        empFiles.value = ''
    else:
        empFiles.value = fns
    
    tailpoints = int(empLevel_tailpoints.value)
    if tailpoints < 1:
        print 'tail point needs to be greater than 0'
        return
    
    # find empty files
    emFileList = filter(None, str(empFiles.value).split(','))
    for i in xrange(0, len(emFileList)):
        emFileList[i] = emFileList[i].strip()

    emFilePaths = []
    if len(emFileList) != 0:
        for emFile in emFileList:
            found = False
            
            for ds in __selected_files__ :
                dsLocation = str(ds)
                if emFile in dsLocation:
                    if not found:
                        emFilePaths.append(dsLocation)
                        found = True
                    else:
                        print 'Warning: "%s" has multiple matches' % emFile
                        break
                    
            if not found:
                print 'Warning: "%s" was not found' % emFile
          
    if len(emFilePaths) == 0:
        print 'Warning: no Empty Scans were selected'
        return
  
    em = LoadNxHdf(emFilePaths)                           
    em.SortAngles()    
    em = RemoveIgnoredRanges(em, empIgnorePts.value)
    empLevel.value = sum(em.DetCtr[-tailpoints:]) / tailpoints
    empLevel_Error.value = sum(em.ErrDetCtr[-tailpoints:]) / tailpoints   
    em.FindZeroAngle()
    em.DetermineQVals()
    em.MeasurementTime()
    
    filename = os.path.basename(emFilePaths[0])
    filename = filename[:filename.find('.nx.hdf')]
#    path = 'V:/shared/KKB Logbook/Temp Plot Data Repository/'

#    em.SaveRaw(path + filename + '-empty.dat')
    
    global Plot1
    
    #print dir(em)
    PlotDataset(Plot1, em, 'EMPTY: ' + filename)
    for i in xrange(tailpoints):
        x = em.Qvals[-i-1]
        y = em.DetCtr[-i-1]
        Plot1.add_marker(x, y, 'red')
#        Plot3.add_marker(x, y, 'red')
        
    # ADD IGNORED RANGE
    
#    Plot3.add_y_marker(empLevel.value, 6000, 'red')    
    Plot1.add_y_marker(empLevel.value, 6000, 'red')
    
#    PlotTransmissionDataset(Plot1, em, 'EMPTY: ' + filename)
    



# OPTIONAL: AMBIENT BACKGROUND
bkgFiles = Par('string', '')
bkgFiles.title = 'Files' 
bkgFilesTakeBtn = Act('bkgFilesTake()', 'Select As Ambient Background')

bkgIgnorePts = Par('string', '')
bkgIgnorePts.title = 'Ignore Data Points' 

bkgLevel = Par('float', '0.2')
bkgLevel.title = 'Background Level'
bkgLevel.enabled = False
bkgLevel_Error = Par('float', '0')
bkgLevel_Error.title = ' Actual Error'
bkgLevel_Error.enabled = False
bkgLevel_space = Par('label', '')
bkgLevel_space.colspan = 2

g4 = Group('OPTIONAL: RE-DETERMINE AMBIENT BACKGROUND (AB) AND COPY VALUE TO PATCHED AB INPUT WINDOW')
g4.numColumns = 2
g4.add(bkgFiles, bkgFilesTakeBtn, bkgIgnorePts, 
       bkgLevel, bkgLevel_space,bkgLevel_Error)

#steps_label.colspan = 200

def bkgFilesTake():
    fns = None
    for sds in __selected_files__:
        basename = os.path.basename(str(sds))
        basename = basename[:basename.find('.nx.hdf')]

        if fns is None:
            fns = basename
        else:
            fns += ', ' + basename
            
    if fns is None:
        bkgFiles.value = ''
    else:
        bkgFiles.value = fns
    # find background files
    bkgFileList = filter(None, str(bkgFiles.value).split(','))
    for i in xrange(0, len(bkgFileList)):
        bkgFileList[i] = bkgFileList[i].strip()

    bkgFilePaths = []
    if len(bkgFileList) != 0:
        for bkgFile in bkgFileList:
            found = False
            
            for ds in __selected_files__ :
                dsLocation = str(ds)
                if bkgFile in dsLocation:
                    if not found:
                        bkgFilePaths.append(dsLocation)
                        found = True
                    else:
                        print 'Warning: "%s" has multiple matches' % bkgFile
                        break
                    
            if not found:
                print 'Warning: "%s" was not found' % bkgFile
                
    if len(bkgFilePaths) == 0:
        print 'Warning: no Background Scans were selected'
        return
    
    filename = os.path.basename(bkgFilePaths[0])
    
    print 'filname:', filename
    
    bkg = LoadNxHdf(bkgFilePaths)
    bkg.SortAngles()
    bkg = RemoveIgnoredRanges(bkg, bkgIgnorePts.value)
    
    bkgLevel.value = sum(bkg.DetCtr)/len(bkg.Angle)
    bkgLevel_Error.value = sum(bkg.ErrDetCtr)/len(bkg.Angle)
    
    bkg.FindZeroAngle()
    bkg.DetermineQVals()
    
    global Plot1

    PlotDataset(Plot1, bkg, 'Determine background: ' + filename)
#    PlotDataset_log(Plot3, bkg, 'Determine background: ' + filename)
    
    Plot1.add_y_marker(bkgLevel.value, 600, 'green')
#    Plot3.add_y_marker(bkgLevel.value, 600, 'green')
       
#    PlotTransmissionDataset(Plot1, bkg, 'Determine background: ' + filename)
#    PlotMonitorDataset(Plot2, bkg, 'Determine background: ' + filename)    
    
    
g0 = Group('Select Tube(s) of Interest:')
g0.numColumns = 5
g0.add(combine_tube0, combine_tube1, combine_tube2, combine_tube3, combine_tube4)

g1 = Group('Advanced Settings')
g1.numColumns = 3
g1.add(use_beammonitor, defaultMCR, TWideMarker, convert2q, sort_scanvar)


    

def LoadNxHdf(filePaths):

    result = None
    for file in filePaths:
        tmp = ReductionDataset(file)

        if result is None:
            result = tmp
        else:
            result.Append(tmp)
            
    return result

    result = None
    for file in files:
        tmp = ReductionDataset(path + file)

        if result is None:
            result = tmp
        else:
            result.Append(tmp)
            
    return result

class ReductionDataset:
            
    def __init__(self, path):
        
        print ' '
        print 'loading file number:', path
        print ' '
        
        ds = Dataset(path) # df[path]
        # in case of 4d data
        ds.__iDictionary__.addEntry('hmm', 'entry1/data/hmm')
       
        self.__ds__ = ds
        self.Filename   = os.path.basename(path)
            
        self.CountTimes    = list(ds['entry1/instrument/detector/time'])
        self.Bex           = list(ds['entry1/instrument/crystal/bex'])
        #self.Angle         = list(ds['entry1/instrument/crystal/m2om'])
        self.MonCts        = list(ds['entry1/monitor/bm1_counts'])
        self.MonCountTimes = list(ds['entry1/monitor/time']) # NEW
        
        self.ScanVariablename  = str(ds['entry1/instrument/crystal/scan_variable'])
        self.ScanVariable = list(ds['entry1/instrument/crystal/' + self.ScanVariablename])        
        
        self.Angle = copy(self.ScanVariable)
        
        if self.ScanVariablename == 'm2om':
            pass
        else:
            if convert2q.value:
                raise Exception ("Please Untick 'Convert to q'")
                    
        self.DetCts     = [] # more difficult readout
        self.ErrDetCts  = [] # calculated
        self.TransCts   = [] # more difficult readout   
        
        # read parameters from file
     
        self.Wavelength    = float(ds['entry1/instrument/crystal/wavelength'])         
        self.MainDeadTime  = float(ds['entry1/instrument/detector/MainDeadTime' ])
        self.TransDeadTime = float(ds['entry1/instrument/detector/TransDeadTime'])
        self.dOmega        = float(ds['entry1/instrument/crystal/dOmega'])      
        self.gDQv          = float(ds['entry1/instrument/crystal/gDQv'])
        self.ScanVariable  = str(ds['entry1/instrument/crystal/scan_variable'])
        self.TimeStamp     = list(ds['entry1/time_stamp'])
        
              
        # read parameters from file and possibly patch
                
        self.Thick         = TryGet(ds, ['entry1/sample/thickness'                   ], Thickness.value , Thickness_Patching.value ) / 10.0 # mm to cm            
        self.SampleName    = TryGet(ds, ['entry1/sample/name'                        ], SampleName.value , SampleName_Patching.value )
        self.SampleDescr   = TryGet(ds, ['entry1/sample/description'                 ], SampleDescr.value , SampleDescr_Patching.value )
        self.SampleBkg     = TryGet(ds, ['entry1/experiment/bkgLevel'                ], SampleBkg.value , SampleBkg_Patching.value )

    
        
        self.empLevel = empLevel.value
        self.empLevel_Error = empLevel_Error.value
        
        self.defaultMCR = defaultMCR.value
        self.TWideMarker = TWideMarker.value
        self.ActualTime = range(len(self.Angle))
        self.widepoints = 0
        

  
        # tube ids
        tids = []
        if combine_tube0.value:
            tids.append(0)
        if combine_tube1.value:
            tids.append(1)
        if combine_tube2.value:
            tids.append(2)
        if combine_tube3.value:
            tids.append(3)
        if combine_tube4.value:
            tids.append(4)

        # sum selected tubes
        data = zeros(len(self.Angle))
        for tid in tids:
            if ds.hmm.ndim == 4:
                data[:] += ds.hmm[:, 0, :, tid].sum(0) # hmm
            else:
                data[:] += ds.hmm[:, :, tid].sum(0)    # hmm_xy       
                                
        DeadtimeCorrection(data, self.MainDeadTime, self.CountTimes)

        self.DetCts    = list(data)
        self.ErrDetCts = [sqrt(cts) for cts in self.DetCts]

        # transmission counts
        if abs(self.Wavelength - 4.74) < 0.01:
            tid = 10
            #print 'long wavelength'
        elif abs(self.Wavelength - 2.37) < 0.01:
            tid = 9
            #print 'short wavelength'
        else:
            raise Exception('unsupported wavelength')
            
        if ds.hmm.ndim == 4:
            data[:] = ds.hmm[:, 0, :, tid].sum(0) # hmm
        else:
            data[:] = ds.hmm[:, :, tid].sum(0)    # hmm_xy
        
            
        DeadtimeCorrection(data, self.TransDeadTime, self.CountTimes)
        
        self.TransCts  = list(data) 
        
        
        # CONVERT TO COUNTRATES
        self.DetCtr     = range(len(self.DetCts))
        self.ErrDetCtr  = range(len(self.DetCts))
        self.TransCtr   = range(len(self.DetCts))
        self.MonCtr     = range(len(self.DetCts))
                
        
        ctTimes = self.CountTimes
        
        # to ignore all the 0 times in the detector
        
        
        for i in xrange(len(self.DetCts)):    
            if ctTimes[i] < 0.5:                        
                print 'Please Ignore Data Point: ', i+1
                print ''
                ctTimes[i] = ctTimes[i] + 100.0
                        
        for i in xrange(len(ctTimes)):
            ctTime = ctTimes[i]                  
            self.DetCtr[i]     = self.DetCts[i]    / ctTime
            self.ErrDetCtr[i]  = self.ErrDetCts[i] / ctTime
            self.TransCtr[i]   = self.TransCts[i]  / ctTime
    
        ctTimes = self.MonCountTimes
        
        for i in xrange(len(ctTimes)):
            ctTime = ctTimes[i]                  
            self.MonCtr[i]     = self.MonCts[i]    / ctTime
           
        # TAKE ACCOUNT OF BEAMMONITOR
        
        if use_beammonitor.value: 
        
           mcr = self.defaultMCR
           for i in xrange(len(self.Angle)):
               if self.MonCtr[i] <0.0001: #to fix the 0-divison problem
                   self.MonCtr[i] = 1000
               
               f = mcr / self.MonCtr[i]
            
               self.DetCtr[i]    = self.DetCtr[i]    * f
               self.ErrDetCtr[i] = self.ErrDetCtr[i] * f
               self.TransCtr[i]  = self.TransCtr[i]  * f
        
     # CALCULATE TIME OF THE MEASUREMENT
    def MeasurementTime(self):   
        TotalTime = self.TimeStamp[-1] 
        
        h      = TotalTime // 3600
        h_left = TotalTime % 3600
        min    = h_left // 60
        sec    = h_left % 60
        
        self.TotalTime_form = "%02i:%02i:%02i" % (h, min, sec)

        
        print 'Total Run Time: ' + self.TotalTime_form + ' [h:min:sec]'          
        print ''
        
        for i in xrange(len(self.TimeStamp)-1):
            self.ActualTime[i+1] = self.TimeStamp[i+1]-self.TimeStamp[i]
        
    def SortAngles(self):
        info = sorted(enumerate(self.Angle), key=lambda item:item[1])
        
        self.Angle     = [item[1]                 for item in info]
        self.DetCtr    = [self.DetCtr    [item[0]] for item in info]
        self.ErrDetCtr = [self.ErrDetCtr [item[0]] for item in info]
        self.MonCtr    = [self.MonCtr    [item[0]] for item in info]
        self.TransCtr  = [self.TransCtr  [item[0]] for item in info]
        self.CountTimes= [self.CountTimes[item[0]] for item in info]
        self.Bex       = [self.Bex       [item[0]] for item in info]
        self.TimeStamp = [self.TimeStamp [item[0]] for item in info]
        self.ActualTime= [self.ActualTime[item[0]] for item in info]
    
    def Append(self, other): # CHECK MISSING COUNTRATE? CHECK CHECK CHECK!!!
        self.Filename  += ';' + other.Filename
        self.Angle     += other.Angle
        self.Bex       += other.Bex
        self.DetCtr    += other.DetCtr
        self.ErrDetCtr += other.ErrDetCtr
        self.MonCtr    += other.MonCtr
        self.TransCtr  += other.TransCtr
        self.CountTimes+= other.CountTimes
        self.TimeStamp += other.TimeStamp
        self.ActualTime+= other.ActualTime
        
    def KeepOnly(self, toKeep):
        self.Angle     = [self.Angle[i]      for i in toKeep]
        self.DetCtr    = [self.DetCtr[i]     for i in toKeep]
        self.ErrDetCtr = [self.ErrDetCtr[i]  for i in toKeep]
        self.MonCtr    = [self.MonCtr[i]     for i in toKeep]
        self.TransCtr  = [self.TransCtr[i]   for i in toKeep]
        self.CountTimes= [self.CountTimes[i] for i in toKeep]
        self.Bex       = [self.Bex[i]        for i in toKeep]
        self.TimeStamp = [self.TimeStamp[i]  for i in toKeep]
        self.ActualTime= [self.ActualTime[i] for i in toKeep]
    '''        
    def FindZeroAngle(self):
        # find peak
        x = self.Angle
        y = self.DetCtr
        i = y.index(builtin_max(y))

        dy = slopeAt(y, i)
        if dy < 0.0:
            aX = x[i-1]
            bX = x[i  ]
            
            aY = slopeAt(y, i-1)
            bY = dy
        else:
            aX = x[i  ]
            bX = x[i+1]
            
            aY = dy
            bY = slopeAt(y, i+1)
            
        self.PeakAng = (aY*bX - aX*bY) / (aY - bY)
        self.PeakVal = y[i];
        
        print "Peak Angle:", self.PeakAng
        print "I(rock):", self.PeakVal
        return self.PeakAng
    '''
    def FindZeroAngle(self):
        from __builtin__ import max, min, sorted
        
        # input
        x = self.Angle
        y = self.DetCtr

        # limits
        y_max = max(y)
        
        y_low = 0.01 * y_max # find suitable x range
        x_min = max(x)
        x_max = min(x)
        for xi, yi in zip(x, y):
            if yi > y_low:
                if x_min > xi:
                    x_min = xi
                if x_max < xi:
                    x_max = xi

        # sampling
        x_sam = self.linspace(x_min, x_max, num=500)
        y_sam = self.sample(x, y, x_sam)
        
        # normalized cross-correlation
        y_cnv = self.normxcorr(y_sam, y_sam)
        x_cnv = self.linspace(x_min, x_max, num=len(y_cnv))
                    
        # find suitable maximum of y_cnv
        yLevel = 0.5 * y_max
                    
        maxima = self.localmaxima(x_cnv, y_cnv)
        maxima = [m for m in maxima if m[1] > 0.0]                    # ignore negative matches
        maxima = [m for m in maxima if self.sample(x, y, m[0]) > yLevel]   # only consider high y values
        maxima = sorted(maxima, key=lambda m: m[1], reverse=True)     # best fit first
        
        if not maxima:
            self.PeakAng = x[y.index(y_max)]
            self.PeakVal = y_max
        
        else:
            x_cnv_max, y_cnv_max, i_cnv_max = maxima[0]
            self.PeakAng = self.maximumX(x_cnv, y_cnv, i_cnv_max)
            self.PeakVal = y_max
        
        print "Peak Angle:", self.PeakAng
        print "I(rock):", self.PeakVal
        return self.PeakAng

    def linspace(self, start, stop, num):
        r = [0.0] * num
        
        nom = stop - start
        den = num - 1
        for i in xrange(num):
            r[i] = start + (i * nom) / den
        return r
    
    def sample(self, x0, y0, x1):
        from __builtin__ import max, min
        
        if len(x0) != len(y0):
            raise Exception("len(x0) != len(y0)")

        x0_min = min(x0)
        x0_max = max(x0)

        if isinstance(x1, list):
            x1_min = min(x1)
            x1_max = max(x1)
        else:
            x1_min = x1
            x1_max = x1
    
        if len(x0) < 2:
            raise Exception("len(x0) < 2")
        if x0_min >= x0_max:
            raise Exception("x0_min >= x0_max")
        if x1_min < x0_min:
            raise Exception("x1_min < x0_min")
        if x0_max < x1_max:
            raise Exception("x0_max < x1_max")

        i0 = 0
        i1 = 1
        x0i0 = x0[i0]
        y0i0 = y0[i0]
        x0i1 = x0[i1]
        y0i1 = y0[i1]
        
        # in case first x values are equal
        while x0i0 == x0i1:
            i1 += 1
            x0i1 = x0[i1]
            y0i1 = y0[i1]

        try:
            _ = iter(x1)
        except TypeError:
            # not iterable
            while x0i1 < x1:
                x0i0 = x0i1
                y0i0 = y0i1
    
                i1 += 1
                
                x0i1 = x0[i1]
                y0i1 = y0[i1]
    
            return y0i0 + (x1 - x0i0) * (y0i1 - y0i0) / (x0i1 - x0i0)
            
        else:
            # iterable
            y1 = [0.0] * len(x1)
            for j in xrange(len(x1)):
                x1j = x1[j]
                
                while x0i1 < x1j:
                    x0i0 = x0i1
                    y0i0 = y0i1
    
                    i1 += 1
                    
                    x0i1 = x0[i1]
                    y0i1 = y0[i1]

                y1[j] = y0i0 + (x1j - x0i0) * (y0i1 - y0i0) / (x0i1 - x0i0)
    
            return y1
        
    def fwhm(self, x, y, i):
    
        def helper(i, yLevel, J):
            x1 = x[i]
            y1 = y[i]
            
            xRef = x1
    
            for j in J:
                x0 = x1
                y0 = y1
                x1 = x[j]
                y1 = y[j]
                
                if y1 <= yLevel:
                    return (x0 + (x1 - x0) * (yLevel - y0) / (y1 - y0)) - (xRef)
    
            return None

        yLevel = 0.5 * y[i] # half maximum
    
        n = len(y)
        rhs = helper(i, yLevel, xrange(i + 1,  n, +1)) # right hand side
        lhs = helper(i, yLevel, xrange(i - 1, -1, -1)) # left hand side
    
        if rhs is None:
            if lhs is None:
                return np.max(x) - np.min(x)
            else:
                return 2 * lhs
        else:
            if lhs is None:
                return 2 * rhs
            else:
                return lhs + rhs
            
    def normxcorr(self, s, k):
        from __builtin__ import abs
        
        sLen = len(s)
        kLen = len(k)
        cLen = sLen + kLen - 1
    
        c = [0.0] * cLen
        
        for i in xrange(cLen):
            j0 = i - (kLen - 1) if i >= kLen - 1 else 0
            jN = i + 1 if i < sLen - 1 else sLen
    
            s1Sum = 0.0
            s2Sum = 0.0
    
            k1Sum = 0.0
            k2Sum = 0.0
    
            skSum = 0.0
    
            n = jN - j0
            for j in xrange(j0, jN):
                sVal = s[j    ]
                kVal = k[i - j]
    
                s1Sum += sVal
                s2Sum += sVal * sVal
                
                k1Sum += kVal
                k2Sum += kVal * kVal
    
                skSum += sVal * kVal
    
            nom  = skSum - s1Sum * k1Sum / n
            denS = s2Sum - s1Sum * s1Sum / n
            denk = k2Sum - k1Sum * k1Sum / n
            den  = sqrt(denS * denk)
    
            if den > 1e-5 * abs(nom):
                c[i] = nom / den
    
        return c
    
    def localmaxima(self, x, y):
        if len(x) != len(y):
            raise Exception("len(x) != len(y)")
        if len(x) < 3:
            raise Exception("len(x) < 3")
        
        # return list of tuples (x, y, i)
        result = []
    
        y1 = y[0]
        y2 = y[1]
        for i2 in xrange(2, len(y)):
            y0 = y1
            y1 = y2
            y2 = y[i2]
    
            if (y0 < y1) and (y1 > y2):
                result.append((x[i2 - 1], y1, i2 - 1))
            
        return result
    def maximumX(self, x, y, i):
                
        x0 = x[i - 1]
        x1 = x[i    ]
        x2 = x[i + 1]
        
        y0 = y[i - 1]
        y1 = y[i    ]
        y2 = y[i + 1]
        
        y10 = y1 - y0
        y20 = y2 - y0
        
        return -0.5 * ((x2*x2 - x0*x0)*y10 - (x1*x1 - x0*x0)*y20) / ((x1 - x0)*y20 - (x2 - x0)*y10)


    def DetermineQVals(self):
        deg2rad = 3.14159265359 / 180
        f = 4 * 3.14159265359 / self.Wavelength
        
        self.Qvals = [f * sin(deg2rad * (angle - self.PeakAng) / 2) for angle in self.Angle]
         
    def FindTWideCtr(self):

        level = self.TWideMarker
        i0 = bisect_left(self.Qvals, level);
        
        if i0 == len(self.Qvals):
            #print ''
            #print "You don't have data past %f (1/A) - so TWide may not be reliable" % level
            #print ''
            i0 = builtin_max(0, len(self.Qvals) - 5)
  
        sumTransCtr = 0
        points = 0
              
        for i in xrange(i0, len(self.Qvals)):
            sumTransCtr += self.TransCtr[i]
            points = points+1

        self.TWideCtr = sumTransCtr / points
        self.widepoints = points
        print "I(Wide): ", self.TWideCtr, ", ", points,'points used'
        
        
        if self.Qvals[i0] < level:
            print ''
            print "WARNING: You don't have data past %f (1/A) - so TWide may not be reliable" % level
            print ''
 
                                           
    def CorrectData(self, emp):
        self.Emp = emp
        
        dOmega = self.dOmega
        
        samRock = self.PeakVal
        samWide = self.TWideCtr

        empRock = emp.PeakVal
        empWide = emp.TWideCtr

        self.TransRock  = samRock / empRock
        self.TransWide  = samWide / empWide
        
        print '  '
        print "T(rock):       %.4g" % (self.TransRock)
        print "T(wide):       %.4g" % (self.TransWide)
        print "T(sas) = Trock/Twide: %.4g" % (self.TransRock / self.TransWide)
        print '  '
        
        scale = 1.0 / (self.TransWide * self.Thick * dOmega * emp.PeakVal)                
               
        maxq = emp.Qvals[-1]
        for i in xrange(len(self.Qvals)):
            wq = self.Qvals[i]
            if wq < maxq:
                tempI   = interp(wq, emp.Qvals, emp.DetCtr)
                tempErr = interp(wq, emp.Qvals, emp.ErrDetCtr)
            else:
                tempI   = self.empLevel
                tempErr = emp.empLevel_Error # USE THIS!!!
                #tempErr = 0
                #tempErr = 0 ### MISSING NEEDS TO BE CHANGED!!!
 
            detCtr = self.DetCtr[i] - self.SampleBkg
            empCtr = tempI          - self.SampleBkg

            self.DetCtr[i]    = detCtr - self.TransRock * empCtr
            self.ErrDetCtr[i] = sqrt(self.ErrDetCtr[i]**2 + (self.TransRock * tempErr)**2)
            
            
            self.DetCtr[i]    *= scale
            self.ErrDetCtr[i] *= scale                            

    def SaveAbs(self, path):
        LE = '\n'
        with open(path, 'w') as fp:
            fp.write("FILES: " + self.Filename.replace(';',',') + LE)
            fp.write("CREATED: " + datetime.now().strftime("%a, %d %b %Y at %H:%M:%S") + LE)
            fp.write("SAMPLE: " + self.SampleName + '; ' + self.SampleDescr + '; Thickness [cm]: %g' % (self.Thick) + LE)
            fp.write("AMBIENT BACKGROUND: %g" % self.SampleBkg + LE)
            fp.write("EMP LEVEL: %g " % (self.empLevel) + LE) 
            fp.write("EMP FILES: " + self.Emp.Filename.replace(';',',') + "; EMP LEVEL: %.4g " % self.empLevel + LE)
            fp.write("Trock = %.4f; Twide = %.4f; Tsas = %.4f" % (self.TransRock, self.TransWide, self.TransRock / self.TransWide) + LE)
            fp.write("SAM PEAK ANGLE: %.5f ; EMP PEAK ANGLE: %.5f" % (self.PeakAng, self.Emp.PeakAng) + LE)
          
            try:
                pass
                #fp.write("EMP LEVEL: %g ; BKG LEVEL: %g" % (self.empLevel, self.Emp.bkgLevel) + LE)
            except:
                pass
                #fp.write("SAM PEAK ANGLE: %g" % self.PeakAng + LE)
                #fp.write("EMP LEVEL: %g" % self.empLevel + LE)

            # divergence, in terms of Q (1/A) 
            gdqv = self.gDQv
            
            doublepoints = 0
            doublepointsi = []
            
            preQ = float('nan')
            for i in xrange(len(self.Qvals)):
                newQ = self.Qvals[i]
                
                if preQ == newQ:
                    doublepoints +=1
                    doublepointsi.append(i)
                if preQ != newQ:
                    fp.write("%15.6g %15.3f %15.3f %15.6g %15.6g %15.6g" % (newQ, self.DetCtr[i], self.ErrDetCtr[i], -gdqv, -gdqv, -gdqv) + LE)
                    preQ = newQ
        
        print ('Info: removed %i duplicate points in this file: ' % doublepoints) + str(doublepointsi)
                                       
    def SaveRaw(self, path):
        LE = '\n'
        with open(path, 'w') as fp:        
            if convert2q.value:
                fp.write('%15s' % 'q')
            else:
                fp.write('%15s' % self.ScanVariablename)
            fp.write('%15s' % self.Filename.replace('.nx.hdf','')) 
            fp.write('%15s' % 'y_error') 
            fp.write('%15s' % 'data_trans')
            fp.write('%15s' % 'beam_monitor')
            if convert2q.value:
                fp.write('%15s' % 'm2om')
            fp.write('%15s' % 'det_time')
            fp.write('%15s' % 'bex')
            #fp.write('%15s' % 'timestamp_no')
            #fp.write('%15s' % 'actual_time')
            #fp.write('%15s' % 'diff_time')
            fp.write('\n')
            
            
            if convert2q.value:
                fp.write('%15s' % '[1/A]')
            else:
                fp.write('%15s' % '[]')
            fp.write('%15s' % '[c/s]') 
            fp.write('%15s' % '[c/s]') 
            fp.write('%15s' % '[c/s]')
            fp.write('%15s' % '[c/s]')
            if convert2q.value:
                fp.write('%15s' % '[deg]')
            fp.write('%15s' % '[s]')
            fp.write('%15s' % '[mm]')
            #fp.write('%15s' % '[s]')
            #fp.write('%15s' % '[s]')
            #fp.write('%15s' % '[s]')
            fp.write('\n')

            
            
            for i in xrange(len(self.Qvals)):
                fp.write("%15.8g %15.3f %15.3f" % (self.Qvals[i], self.DetCtr[i], self.ErrDetCtr[i]))
                fp.write("%15.3f %15.3f" % (self.TransCtr[i], self.MonCtr[i]))
                if convert2q.value:
                    fp.write("%15f" % (self.Angle[i]))
                    #fp.write("%15f %15d %15f %15f" % (self.CountTimes[i],self.TimeStamp[i],self.ActualTime[i], self.ActualTime[i] - self.CountTimes[i]))
                fp.write("%15f" % (self.CountTimes[i]))
                fp.write("%15f" % (self.Bex[i]))
                fp.write('\n')
            
            fp.write("FILES: " + self.Filename.replace('.nx.hdf','') + ';  ' + LE)         
            fp.write("CREATED: " + datetime.now().strftime("%a, %d %b %Y at %H:%M:%S") + LE)
            fp.write("SAMPLE: " + self.SampleName + LE)
            fp.write("SAMPLE_DESCRIPTION: " + self.SampleDescr + LE) 
            fp.write("SAMPLE_THICKNESS [cm]: %g" % self.Thick + LE)
            fp.write("AMBIENT_BACKGROUND [c/s]: %g" % self.SampleBkg + LE)
            try:
               fp.write("PEAK_ANGLE [deg]: %.5f" % self.PeakAng + LE)
            except:
                pass   
            fp.write("TOTAL_TIME [h:min:sec]: " + self.TotalTime_form + LE)
                
                
def DeadtimeCorrection(counts, deadTime, countTimes):
    # x1 = x0 - (x0 - y*e^cx0) / (1 - cx0)
        
    for i in xrange(len(counts)):
        if countTimes[i] == 0:
            counts[i] = 0
            
        else:
            dtt = deadTime / countTimes[i]
            
            y = counts[i]
            x = y       # initial value
            
            # 4 iterations
            for j in xrange(4):
                x = x - (x - y*exp(dtt * x)) / (1 - dtt * x)
                
            counts[i] = x
    
            #tube[i] = tube[i] * (1 / (1.0 - tube[i] * deadTime / countTimes[i]))        

def TryGet(ds, pathList, default, forceDefault=False):
    if forceDefault:
        return default
    
    for path in pathList:
        try:
            return ds[path]
        except AttributeError:
            pass
        
    return default

def slopeAt(list, i):
    L = 0
    H = len(list) - 1

    yL = list[builtin_max(L, i - 1)]
    yH = list[builtin_min(H, i + 1)]

    return yH - yL

def interp(q, Q, I):
    
    def helper(q, Q, I, k):
        if Q[k - 1] == Q[k]:
            return (I[k - 1] + I[k]) / 2
        else:
            return I[k] + (I[k - 1] - I[k]) / (Q[k - 1] - Q[k]) * (q - Q[k])

    if q <= Q[1]:
        return helper(q, Q, I, 1)
    elif q >= Q[-2]:
        return helper(q, Q, I, -1)
    else:
        return helper(q, Q, I, bisect_left(Q, q))

def RemoveIgnoredRanges(ds, ignorePtsStr):
    indices = GetToKeepFilter(len(ds.Angle), ignorePtsStr)
    if indices is not None:
        ds.KeepOnly(indices)        
    return ds

def GetToKeepFilter(maxCount, ignorePtsStr):
    ignoredRanges = filter(None, str(ignorePtsStr).split(','))
    if len(ignoredRanges) < 1:
        return None
    
    toKeep = range(0, maxCount)
    for ignoredRange in ignoredRanges:
        rangeItems = ignoredRange.split('-')
        if ('' in rangeItems) or (len(rangeItems) < 1) or (len(rangeItems) > 2):
            raise Exception('format in "ignore data points" is incorrect')
        
        # from 1 based to 0 based
        start = int(rangeItems[0]) #- 1
        
        if len(rangeItems) == 1:
            end = start + 1
        elif rangeItems[1]== '*':
            end = maxCount
        else:
            end = float(rangeItems[1])
        
        for point in xrange(start, end):
            if point in toKeep:
                toKeep.remove(point)
                
    return toKeep

def PlotDataset(plot, ds, title):
    data = zeros(len(ds.Qvals))
    data[:]     = ds.DetCtr
    data.var[:] = Array(ds.ErrDetCtr) ** 2 # nice way of cheating for now
    data.title  = title
    axis0       = data.axes[0]
    axis0[:]    = ds.Qvals
    
    plot.set_dataset(data)
    plot.title    = title
    plot.x_label = str(ds.ScanVariablename)
    if convert2q.value:
        plot.x_label = 'q (1/Angstrom)'
    plot.y_label = 'intensity (counts/sec)'
    plot.set_mouse_follower_precision(6,2,2)
    plot.set_log_y_on(True)
    plot.y_range = [0.1, data.max()]
           

