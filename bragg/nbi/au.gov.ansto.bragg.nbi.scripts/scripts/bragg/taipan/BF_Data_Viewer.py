from gumpy.vis.event import MouseListener, MaskEventListener, AWTMouseListener
from org.gumtree.vis.mask import RectangleMask
from org.gumtree.gumnix.sics.control.events import DynamicControllerListenerAdapter
from java.lang import System, Double
from gumpy.nexus.fitting import Fitting, GAUSSIAN_FITTING
from java.io import File
from gumpy.commons import sics
from Experiment.lib import export
import math
import traceback
import sys

# Script control setup area
# script info
__script__.title = 'Be Filter Data Viewer'
__script__.version = '3.0'
__script__.numColumns = 1
Dataset.__dicpath__ = get_script_path() + '/lib/taipan_path_table'

SAVED_MASK_PRFN = 'BeFilter.savedMasks'
SAVED_INC_MASK_PRFN = 'BeFilter.savedIncMasks'
SAVED_EXC_MASK_PRFN = 'BeFilter.savedExcMasks'
DS = None
__new_xAxis__ = simpledata.arange(-0.5, 30, 1, float)

__mask_updated__ = False

class RegionEventListener(MaskEventListener):
    
    def __init__(self):
        MaskEventListener.__init__(self)
    
    def mask_added(self, mask):
        pass
            
    def mask_removed(self, mask):
        global DS
        update_mask_list()
        process(DS)
    
    def mask_updated(self, mask):
        global __mask_updated__
        update_mask_list()
        __mask_updated__ = True
        
regionListener = RegionEventListener()

class MousePressListener(AWTMouseListener):
    def __init__(self):
        AWTMouseListener.__init__(self)
    
    def mouse_released(self, event):
        global __mask_updated__
        global DS
        if __mask_updated__ :
            process(DS)
            __mask_updated__ = False
    
mouse_press_listener = MousePressListener()


__inc_masks__ = []
__exc_masks__ = []

def load_mask_pref():
    global __inc_masks__, __exc_masks__, SAVED_INC_MASK_PRFN, \
            SAVED_EXC_MASK_PRFN, SAVED_MASK_PRFN
    s_mask = get_pref_value(SAVED_MASK_PRFN)
    if not s_mask is None and s_mask.strip() != '':
        reg_list.value = s_mask
    reg_list.title = 'mask list'
    reg_list.enabled = False
    inc_masks = get_pref_value(SAVED_INC_MASK_PRFN)
    if not inc_masks is None and inc_masks.strip() != '':
        __inc_masks__ = eval(inc_masks)
    exc_masks = get_pref_value(SAVED_EXC_MASK_PRFN)
    if not exc_masks is None and exc_masks.strip() != '':
        __exc_masks__ = eval(exc_masks)

def update_mask_list():
    if Plot3.ndim > 0:
        reg_list.value = mask2str(Plot3.get_masks())
        inte_masks(Plot3.ds, Plot3.get_masks())
        
def inte_masks(ds, masks):
    global __inc_masks__
    global __exc_masks__
    __inc_masks__ = []
    __exc_masks__ = []
    x_axis = ds.axes[-1]
    y_axis = ds.axes[-2]

    for mask in masks:
        y_iMin = int((mask.minY - y_axis[0]) / (y_axis[-1] - y_axis[0]) \
                     * (y_axis.size - 1))
        if y_iMin < 0 :
            y_iMin = 0
        if y_iMin >= y_axis.size:
            continue
        y_iMax = int((mask.maxY - y_axis[0]) / (y_axis[-1] - y_axis[0]) \
                     * (y_axis.size - 1)) + 1
        if y_iMax < 0:
            continue
        x_iMin = int((mask.minX - x_axis[0]) / (x_axis[-1] - x_axis[0]) \
                     * (x_axis.size - 1))
        if x_iMin < 0:
            x_iMin = 0;
        if x_iMin >= x_axis.size:
            continue
        x_iMax = int((mask.maxX - x_axis[0]) / (x_axis[-1] - x_axis[0]) \
                     * (x_axis.size - 1)) + 1
        if x_iMax < 0:
            continue
        if mask.isInclusive() :
            __inc_masks__.append([x_iMin, x_iMax, y_iMin, y_iMax])
        else:
            __exc_masks__.append([x_iMin, x_iMax, y_iMin, y_iMax])

def str2maskstr(value):
    items = value.split(';')
    res = []
    for item in items:
        name = item[0:item.index('[')];
        rstr = item[item.index('[') + 1 : item.index(']')]
        range = rstr.split(',')
        range.append(name)
        res.append(range)
    return res

def str2mask(value):
    items = value.split(';')
    masks = []
    for item in items:
        name = item[0:item.index('[')];
        rstr = item[item.index('[') + 1 : item.index(']')]
        range = rstr.split(',')
        mask = RectangleMask(True, float(range[0]), float(range[2]), \
                             float(range[1]) - float(range[0]), \
                             float(range[3]) - float(range[2]))
        mask.name = name
        masks.append(mask)
    return masks

def mask2str(masks):
    res = ''
    for mask in masks:
        res += mask.name
        res += '[' + str(mask.minX) + ',' + str(mask.maxX) + ',' \
                + str(mask.minY) + ',' + str(mask.maxY) + ']'
        if masks.indexOf(mask) < len(masks) - 1:
            res += ';'
    return res

# Make a new plot
#if not 'Plot4' in globals() or Plot4 is None or Plot4.is_disposed() :
#    Plot4 = GPlot()

# arguments for demostration purpose
g1 = Group('Plot1')
fit = Group('Fitting')
g2 = Group('Plot2')
g3 = Group('Plot3')
#g4 = Group('Plot4')
data_name = Par('string', 'total_counts', \
               options = ['bm1_counts', 'bm2_counts', 'total_counts'])
data_name.title = 'Select Data'
normalise = Par('bool', False)
axis_name = Par('string', 'suid')
axis_name.title = 'Select Axis'
axis_lock = Par('bool', False, command = 'lock_axis()')
axis_lock.title = 'Lock Axis'
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

#act4 = Act('put_peak_pos_to_plot3()', text = 'Add peak position to Plot3')
#peak_at = Par('string', '', options=[])
#act5 = Act('remove_peak()', text = 'Remove selected peak')
#g3.add(act4, peak_at, act5)

plot3_idx = Par('int', 0, options = [], command = 'jump_to_idx()')
plot3_idx.title = 'select to jump to index'
reg_enabled = Par('bool', True)
reg_enabled.title = 'region enabled'
reg_list = Par('string', '')
g3.add(plot3_idx, reg_enabled, reg_list)

load_mask_pref()

g5 = Group('Export')
exp_act = Act('batch_export()', 'Batch Export')
g5.add(exp_act)

def batch_export():
    from Experiment import config
    dss = __get_selected_files__()
    if dss is None or len(dss) == 0:
        print 'Please select one or more files to export.'
        return
    path = selectSaveFolder()
    if path == None:
        return
    fi = File(path)
    if not fi.exists():
        if not fi.mkdir():
            print 'Error: failed to make directory: ' + path
            return
    eid = int(experiment_id.value)
    exp_folder = path + '/exp' + str(eid)
    fi = File(exp_folder)
    if not fi.exists():
        if not fi.mkdir():
            print 'Error: failed to make directory: ' + exp_folder
            return
    HMM_folder = exp_folder + '/HMMfiles'
    fi = File(HMM_folder)
    if not fi.exists():
        if not fi.mkdir():
            print 'Error: failed to make directory: ' + HMM_folder
            return
    
    count = 0
    for loc in dss:
        
        ds = df[str(loc)]
        if len(__exc_masks__) > 0:
            res = copy(ds.get_reduced())
            for mask in __exc_masks__:
                res[:, mask[2]:mask[3], mask[0]:mask[1]] = 0
        else :
            res = ds.get_reduced(1)
        if len(__inc_masks__) > 0:
            r = dataset.instance(res.shape, dtype=int)
            for mask in __inc_masks__:
                r[:, mask[2]:mask[3], mask[0]:mask[1]] = res[:, mask[2]:mask[3], mask[0]:mask[1]]
        else :
            r = res
        data = r.sum(0)
        if not ds.axes is None and len(ds.axes) > 0: 
            if not axis_lock.value:
                axis_name.value = ds.axes[0].name
        if len(data == 1) :
            ds2 = Dataset(data, axes=[[ds[str(axis_name.value)]]])
        else :
            axis = ds[str(axis_name.value)]
            ds2 = Dataset(data, axes=[axis])
        ds2.title = ds.title

#        count = int(fsn[3:10])
#        new_fname = 'TAIPAN_exp' + ('%(value)04d' % {'value':eid}) + '_scan' + ('%(value)04d' % {'value':count}) + '.dat'
        export.HMM_intensity_export(ds2, ds.bm1_counts, HMM_folder, eid, get_pref_value, reg_list.value)
    print 'done'

def jump_to_idx():
    global DS
    if DS is None:
        return
    dlen = len(DS)
    idx = plot3_idx.value
    if idx < 0: 
        idx = 0
    if idx >= dlen:
        idx = dlen - 1
    print 'plot histogram ' + str(idx) + ' in Plot3'
    Plot3.set_dataset(DS[idx, 0])
    Plot3.title = str(DS.name) + '_' + str(idx)
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
    from Experiment import config
#    dss = __DATASOURCE__.getSelectedDatasets()
    dss = __get_selected_files__()
    for loc in dss:
#        loc = dinfo.getLocation()
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
    if str(peak_pos) == 'nan':
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
    global DS
    global __new_xAxis__
    if (dss is None or len(dss) == 0) :
        log('no input datasets\n')
    else :
        for fn in dss:
            df.datasets.clear()
            ds = df[fn]
            ds.axes[3] = __new_xAxis__
            DS = ds
            process(DS)

            ds.close()
    Plot3.set_awt_mouse_listener(mouse_press_listener)
    Plot3.set_mask_listener(regionListener)
            
def process(ds):
    global __inc_masks__, __exc_masks__
    if ds is None:
        return
    dname = str(data_name.value)
    if dname == 'total_counts' and reg_enabled.value and len(__inc_masks__) + len(__exc_masks__) > 0:
        if len(__exc_masks__) > 0:
            res = copy(ds.get_reduced())
            for mask in __exc_masks__:
                res[:, mask[2]:mask[3], mask[0]:mask[1]] = 0
        else :
            res = ds.get_reduced()
        if len(__inc_masks__) > 0:
            r = dataset.instance(res.shape, dtype=int)
            for mask in __inc_masks__:
                r[:, mask[2]:mask[3], mask[0]:mask[1]] = res[:, mask[2]:mask[3], mask[0]:mask[1]]
        else :
            r = res
        data = r.sum(0)
        if not ds.axes is None and len(ds.axes) > 0: 
            if not axis_lock.value:
                axis_name.value = ds.axes[0].name
        axis = ds[str(axis_name.value)]
    else :
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
    if auto_fit.value:
        peak_pos.value = float('NaN')
        FWHM.value = float('NaN')
        fit_curve()
    dlen = len(ds)
    plot3_idx.options = range(dlen)
    plot3_idx.value = dlen - 1
    Plot3.set_dataset(ds[dlen - 1, 0])
    Plot3.title = str(ds.name) + '_' + str(dlen-1)
    
#    if reg_enabled.value :
    if len(Plot3.get_masks()) == 0:
        if reg_list.value != None and reg_list.value.strip() != '':
            masks = str2maskstr(reg_list.value)
            for mask in masks:
                Plot3.add_mask_2d(float(mask[0]), float(mask[1]), \
                                  float(mask[2]), float(mask[3]), \
                                  mask[4], mask[4].startswith('I'))

    if reg_enabled.value:
        save_mask_prof()

def save_mask_prof():
    global __inc_masks__, __exc_masks__, SAVED_EXC_MASK_PRFN, \
            SAVED_INC_MASK_PRFN, SAVED_MASK_PRFN
    set_pref_value(SAVED_MASK_PRFN , str(reg_list.value))
    set_pref_value(SAVED_INC_MASK_PRFN , str(__inc_masks__))
    set_pref_value(SAVED_EXC_MASK_PRFN , str(__exc_masks__))
    save_pref()
    
    
#    print '**** done ****'

def __dataset_added__(dsn):
    global __run_script__
    if __script__.title == 'Be Filter Live Data':
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