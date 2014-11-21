from gumpy.vis.event import MouseListener

# Script control setup area
# script info
__script__.title = 'Be Filter Histogram Profile'
__script__.version = '1.0'
Dataset.__dicpath__ = get_script_path() + '/lib/taipan_path_table'

DS = None

class NavMouseListener(MouseListener):
    
    def __init__(self):
        MouseListener.__init__(self)
        
    def on_click(self, event):
        x = event.getX()
        y = event.getY()
        print '(', x, ',', y, ')'
        updatePlot2(y)
        updatePlot3(x)
#            idx = var_jump.options.index(x)
#            ind_jump.value = idx
            

def updatePlot2(y):
    dlen = len(DS)
    fidx = plot1_idx.value
    if fidx < 0 or fidx >= dlen:
        return
    
    d = DS[fidx, 0]
    yaxis = d.axes[0]
    idx = 0
    for v in yaxis:
        if v > y:
            break
        idx += 1
    s = d[idx]
    s.axes[0] = simpledata.arange(30)
    Plot2.set_dataset(s)
    Plot2.x_label = 'x_bin()'
    
def updatePlot3(x):
    dlen = len(DS)
    fidx = plot1_idx.value
    if fidx < 0 or fidx >= dlen:
        return
    
    d = DS[fidx, 0]
    xaxis = d.axes[1]
    idx = 0
    for v in xaxis:
        if v < x:
            break
        idx += 1
    s = d[:, idx].get_reduced()
    Plot3.set_dataset(s)
    
# Use below example to create parameters.
    
g3 = Group('Histogram')
plot1_idx = Par('int', 0, options = [], command = 'jump_to_idx()')
plot1_idx.title = 'select to jump to index'
g3.add(plot1_idx)

g4 = Group('Stack Plot')
offset_val = Par('int', 50)
offset_val.title = 'y offset for each curve'
stack_act = Act('plot_stack()', 'Show Stack Plot in Plot3')
g4.add(offset_val, stack_act)

def plot_stack():
    if DS is None:
        __run_script__(__get_selected_files__())
    dlen = len(DS)
    fidx = plot1_idx.value
    if fidx < 0 or fidx >= dlen:
        return
    
    Plot3.clear()
    time.sleep(3)
    d = DS[fidx, 0]
    for i in xrange(d.shape[1]):
        x = i
        y = d[:, i].get_reduced()
        y += i * offset_val.value
        y.title = d.title + str(x)
        Plot3.add_dataset(y)
        if i == 0:
            Plot3.set_legend_position(None)
    

def jump_to_idx():
    global DS
    if DS is None:
        return
    dlen = len(DS)
    idx = plot1_idx.value
    if idx < 0: 
        idx = 0
    if idx >= dlen:
        idx = dlen - 1
    print 'plot histogram ' + str(idx) + ' in Plot3'
    Plot1.set_dataset(DS[idx, 0])
    Plot1.title = str(DS.name) + '_' + str(idx)
# Use below example to create a new Plot
# Plot4 = Plot(title = 'new plot')

# This function is called when pushing the Run button in the control UI.
def __run_script__(dss):
    # Use the provided resources, please don't remove.
    global Plot1
    global Plot2
    global Plot3
    global DS
    if (dss is None or len(dss) == 0) :
        log('no input datasets\n')
    else :
        for fn in dss:
            df.datasets.clear()
            ds = df[fn]
            DS = ds
            dlen = len(ds)
            plot1_idx.options = range(dlen)
            plot1_idx.value = dlen - 1
            Plot1.set_dataset(ds[dlen - 1, 0])
            Plot1.title = str(ds.name) + '_' + str(dlen-1)
            ds.close()
    Plot1.set_mouse_listener(NavMouseListener())
    
def __dispose__():
    global Plot1
    global Plot2
    global Plot3
    Plot1.clear()
    Plot2.clear()
    Plot3.clear()
