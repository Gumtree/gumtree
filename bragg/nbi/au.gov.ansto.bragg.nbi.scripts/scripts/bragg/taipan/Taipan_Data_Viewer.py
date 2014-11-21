from gumpy.vis.event import MouseListener
from org.gumtree.gumnix.sics.control.events import DynamicControllerListenerAdapter
from java.lang import System, Double
from gumpy.nexus.fitting import Fitting, GAUSSIAN_FITTING
from java.io import File
from gumpy.commons import sics
import math
import traceback
import sys

# Script control setup area
# script info
__script__.title = 'Taipan Data Viewer'
__script__.version = '1.3'
Dataset.__dicpath__ = get_script_path() + '/lib/taipan_path_table'


# Make a new plot
#if not 'Plot4' in globals() or Plot4 is None or Plot4.is_disposed() :
#    Plot4 = GPlot()

# arguments for demostration purpose
g1 = Group('Plot1')
fit = Group('Fitting')
g2 = Group('Plot2')
g3 = Group('Plot3')
#g4 = Group('Plot4')
data_name = Par('string', 'bm2_counts', \
               options = ['bm1_counts', 'bm2_counts'])
normalise = Par('bool', False)
axis_name = Par('string', 'suid')
axis_lock = Par('bool', False, command = 'lock_axis()')
auto_fit = Par('bool', False)
g1.add(data_name, axis_name, axis_lock, normalise, auto_fit)

fit_min = Par('float', 'NaN')
fit_max = Par('float', 'NaN')
act1 = Act('fit_curve()', 'Gaussian Fit Plot1')
peak_pos = Par('float', 'NaN')
FWHM = Par('float', 'NaN')
fit.add(fit_min, fit_max, act1, peak_pos, FWHM)

allow_duplication = Par('bool', False)
act2 = Act('import_to_plot2()', text = 'Import Data Files to Plot2')
to_remove = Par('string', '', options=[])
act3 = Act('remove_curve()', 'Remove selected curve')
plot2_fit_min = Par('float', 'NaN')
plot2_fit_max = Par('float', 'NaN')
plot2_act1 = Act('plot2_fit_curve()', 'Gaussian Fit Plot2')
plot2_peak_pos = Par('float', 'NaN')
plot2_FWHM = Par('float', 'NaN')
act_reset = Act('reset_fitting_plot2()', 'Remove Fitting')
act_remove_all = Act('remove_all_curves()', 'Remove All Curves')
g2.add(allow_duplication, act2, to_remove, act3, plot2_fit_min, plot2_fit_max, 
       plot2_act1, plot2_peak_pos, plot2_FWHM, act_reset, act_remove_all)

act4 = Act('put_peak_pos_to_plot3()', text = 'Add peak position to Plot3')
peak_at = Par('string', '', options=[])
act5 = Act('remove_peak()', text = 'Remove selected peak')
g3.add(act4, peak_at, act5)

#act6 = Act('', 'put_fitting_to_plot4()', text = 'Add fitting curve to Plot4')
#curve_fit = Par('string', '', options=[])
#act7 = Act('', 'remove_fitting_curve()', 'Remove selected fitting')
#g4.add(act6, curve_fit, act7)

def lock_axis():
    if axis_lock.value :
        axis_name.enabled = False
    else:
        axis_name.enabled = True
    
def fit_curve():
    global Plot1
    ds = Plot1.ds
    if len(ds) == 0:
        log('Error: no curve to fit in Plot1.\n')
        return
    for d in ds:
        if d.title == 'fitting':
            Plot1.remove_dataset(d)
    d0 = ds[0]
    fitting = Fitting(GAUSSIAN_FITTING)
    try:
        fitting.set_histogram(d0, fit_min.value, fit_max.value)
        val = peak_pos.value
        if val == val:
            fitting.set_param('mean', val)
        val = FWHM.value
        if val == val:
            fitting.set_param('sigma', math.fabs(val / 2.35482))
        res = fitting.fit()
        res.var[:] = 0
        res.title = 'fitting'
        Plot1.add_dataset(res)
        mean = fitting.params['mean']
        mean_err = fitting.errors['mean']
        FWHM.value = 2.35482 * math.fabs(fitting.params['sigma'])
        FWHM_err = 5.54518 * math.fabs(fitting.errors['sigma'])
        log('POS_OF_PEAK=' + str(mean) + ' +/- ' + str(mean_err))
        log('FWHM=' + str(FWHM.value) + ' +/- ' + str(FWHM_err))
        log('Chi2 = ' + str(fitting.fitter.getQuality()))
        peak_pos.value = fitting.mean
#        print fitting.params
    except:
#        traceback.print_exc(file = sys.stdout)
        log('can not fit\n')
    
def plot2_fit_curve():
    global Plot2
    ds = Plot2.ds
    if len(ds) == 0:
        log('Error: no curve to fit in Plot2.\n')
        return
    for d in ds:
        if d.title == 'fitting':
            Plot2.remove_dataset(d)
    if len(ds) == 1:
        sds = ds[0]
    else:
        sds = Plot2.get_selected_dataset()
        if sds is None :
            open_error('Please select a curve to fit. Right click on the plot to focus on a curve. Or use CTRL + Mouse Click on a curve to select one.')
            return
    fitting = Fitting(GAUSSIAN_FITTING)
    try:
        fitting.set_histogram(sds, plot2_fit_min.value, plot2_fit_max.value)
        val = plot2_peak_pos.value
        if val == val:
            fitting.set_param('mean', val)
        val = plot2_FWHM.value
        if val == val:
            fitting.set_param('sigma', math.fabs(val / 2.35482))
        res = fitting.fit()
        res.var[:] = 0
        res.title = 'fitting'
        Plot2.add_dataset(res)
        mean = fitting.params['mean']
        log('POS_OF_PEAK=' + str(mean))
        log('FWHM=' + str(2.35482 * math.fabs(fitting.params['sigma'])))
        log('Chi2 = ' + str(fitting.fitter.getQuality()))
        plot2_peak_pos.value = fitting.mean
        plot2_FWHM.value = 2.35482 * math.fabs(fitting.params['sigma'])
#        print fitting.params
    except:
#        traceback.print_exc(file = sys.stdout)
        log('can not fit\n')

def reset_fitting_plot2():
    global Plot2
    ds = Plot2.ds
    if len(ds) == 0:
        return
    for d in ds:
        if d.title == 'fitting':
            Plot2.remove_dataset(d)
    plot2_peak_pos.value = Double.NaN
    plot2_FWHM.value = Double.NaN
    
def remove_all_curves():
    global Plot2
    Plot2.clear()
    plot2_fit_min.value = Double.NaN
    plot2_fit_max.value = Double.NaN
    plot2_peak_pos.value = Double.NaN
    plot2_FWHM.value = Double.NaN
    to_remove.options = []
    
def import_to_plot2():
    global Plot2
#    from Experiment import config
    for loc in __selected_files__:
        ds = df[str(loc)]
        if not allow_duplication.value:
            did = str(ds.id)
            if to_remove.options.__contains__(did):
                for item in reversed(Plot2.ds) :
                    if item.title == did :
                        Plot2.remove_dataset(item)
                        rlist = copy(to_remove.options)
                        rlist.remove(did)
                        to_remove.options = rlist
                        break
        dname = str(data_name.value)
        data = ds[dname]
        if dname == 'bm1_counts':
            tname = 'bm1_time'
        else:
            tname = 'bm2_time'
        norm = ds[tname]
        if normalise.value and norm != None and hasattr(norm, '__len__'):
            avg = norm.sum() / len(norm)
            niter = norm.item_iter()
            if niter.next() <= 0:
                niter.set_curr(1)
            data = data / norm * avg
        axis = ds[str(axis_name.value)]
        if data.size > axis.size:
            data = data[:axis.size]
        ds2 = Dataset(data, axes=[axis])
        ds2.title = ds.id
        Plot2.add_dataset(ds2)
        Plot2.x_label = axis_name.value
        Plot2.y_label = dname
        Plot2.title = 'Overlay'
        rlist = copy(to_remove.options)
        rlist.append(str(ds2.title))
        to_remove.options = rlist
        to_remove.value = ds2.title
    
#def put_fitting_to_plot4():
#    global Plot1
#    global Plot4
#    dss = Plot1.ds
#    if dss is None or len(dss) < 2:
#        print 'Error: no fitting curve in Plot1.'
#        return
#    ds = dss[1] + 100 * len(curve_fit.options)
#    ds.title = dss[0].title
#    Plot4.add_dataset(ds)
#    Plot4.x_label = ''
#    Plot4.y_label = 'counts'
#    Plot4.title = 'Fitting Curves'
#    rlist = copy(curve_fit.options)
#    rlist.append(ds.title)
#    curve_fit.options = rlist
    
def remove_curve():
    global Plot2
    if Plot2.ds is None :
        return
    if to_remove.value is None or to_remove.value == '':
        return
    for item in Plot2.ds :
        if item.title == to_remove.value :
            Plot2.remove_dataset(item)
            rlist = copy(to_remove.options)
            rlist.remove(to_remove.value)
            to_remove.options = rlist
            break

#def remove_fitting_curve():
#    global Plot4
#    if Plot4.ds is None :
#        return
#    if curve_fit.value is None or curve_fit.value == '':
#        return
#    for item in Plot4.ds :
#        if item.title == curve_fit.value :
#            Plot4.remove_dataset(item)
#            rlist = copy(curve_fit.options)
#            rlist.remove(curve_fit.value)
#            curve_fit.options = rlist
#            break

def remove_peak():
    global Plot3
    ds = Plot3.ds
    if ds is None or len(ds) == 0:
        log('Warning: no data in Plot3.\n')
        return
    if peak_at.value is None or peak_at.value == '':
        log('Warning: please select the index of the peak to remove.\n')
        return
    ds0 = ds[0]
    idx = int(peak_at.value)
    if ds0.size == 1 and idx == 0:
        Plot3.clear()
        peak_at.options = []
    else:
        nds = delete(ds0, idx)
        Plot3.set_dataset(nds)
        rlist = []
        for i in xrange(nds.size):
            rlist.append(str(i))
        peak_at.options = rlist
    log('peak ' + str(idx) + ' is removed.\n')
    
def put_peak_pos_to_plot3():
    if str(peak_pos.value) == 'nan':
        log('Error: no fitting result is found\n')
        return
    global Plot3
    ds = Plot3.ds
    if ds is None:
        nds = instance([1], init = peak_pos.value)
    else:
        ds0 = ds[0]
        nds = append(ds0, peak_pos.value)
    nds.var[:] = 0
    Plot3.set_dataset(nds)
    Plot3.x_label = ''
    Plot3.y_label = axis_name.value
    Plot3.title = 'Peak Positions'
    rlist = copy(peak_at.options)
    rlist.append(len(rlist))
    peak_at.options = rlist
    peak_at.value = len(rlist) - 1

#class __SaveListener__(DynamicControllerListenerAdapter):
#    
#    def __init__(self):
#        pass
#    
#    def valueChanged(self, controller, newValue):
#        pass
#
#__cur_point__ = -1
#
#def saved(controller, newValue):
#    global __UI__
#    global __cur_point__
#    if pause.value:
#        return
#    newCount = int(newValue.getStringData())
#    axis_name.value = scan_variable_node.getValue().getStringData()
##    __UI__.getScriptExecutor().runScript(cmd)
#    new_point = scanpoint_node.getValue().getIntData()
#    if new_point != __cur_point__ :
#        log('scanpoint=' + str(scanpoint_node.getValue().getIntData() + 1) + '\n', __writer__)
#        __cur_point__ = new_point
#    else:
#        __cur_point__ = -1
#    
#    
#statusListener = __SaveListener__()
#statusListener.valueChanged = saved
#saveCountNode.addComponentListener(statusListener)


# This function is called when pushing the Run button in the control UI.
def __run_script__(dss):
    # Use the provided resources, please don't remove.
    global Plot1
    global Plot2
    global Plot3
    if (dss is None or len(dss) == 0) :
        log('no input datasets\n')
    else :
        for fn in dss:
            df.datasets.clear()
            ds = df[fn]
            dname = str(data_name.value)
            data = ds[dname]
            if dname == 'bm1_counts':
                tname = 'bm1_time'
            else:
                tname = 'bm2_time'
            norm = ds[tname]
            if normalise.value and norm != None and hasattr(norm, '__len__'):
                avg = norm.sum() / len(norm)
                niter = norm.item_iter()
                if niter.next() <= 0:
                    niter.set_curr(1)
                data = data / norm * avg
            if not ds.axes is None and len(ds.axes) > 0: 
                if not axis_lock.value:
                    axis_name.value = ds.axes[0].name
            axis = ds[str(axis_name.value)]
            if not hasattr(data, 'size') :
                data = simpledata.SimpleData([data])
            if not hasattr(axis, 'size') :
                axis = simpledata.SimpleData([axis])
            if data.size > axis.size:
                data = data[:axis.size]
            for i in xrange(data.size):
                if math.fabs(data[i]) > 1e8 :
                    data[i] = float('NaN')
            for i in xrange(axis.size):
                if math.fabs(axis[i]) > 1e8:
                    axis[i] = float('NaN')
            ds2 = Dataset(data, axes=[axis])
            ds2.title = ds.id
            Plot1.set_dataset(ds2)
            Plot1.x_label = axis_name.value
            Plot1.y_label = dname
            Plot1.title = dname + ' vs ' + axis_name.value
            fit_min.value = ds2.axes[0].min()
            fit_max.value = ds2.axes[0].max()
#            Plot1.pv.getPlot().setMarkerEnabled(True)
            if auto_fit.value:
                peak_pos.value = float('NaN')
                FWHM.value = float('NaN')
                fit_curve()
            ds.close()
            
#    print '**** done ****'
    
def __dataset_added__(dsn):
    global __run_script__
    if __script__.title == 'Taipan Live Data':
        __run_script__(dsn)

def __dispose__():
    global Plot1
    global Plot2
    global Plot3
    Plot1.clear()
    Plot2.clear()
    Plot3.clear()
#    saveCountNode.removeComponentListener(statusListener)


#eq = Act('check_eq()', 'Check Equal')
def check_eq():
    ds = Plot2.ds
    d0 = ds[0]
    d1 = ds[1]
    if d0 is d1 :
        print 'True'
    else:
        print 'False'