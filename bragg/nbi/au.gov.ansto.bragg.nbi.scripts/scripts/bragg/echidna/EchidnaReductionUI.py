"""
Echidna Reduction Main GUI.  Please be aware of the following conventions when
changing this file:

(1) 1D Dataset titles must be unique to allow simple selection in the GUI. The format
used here is '<filenumber>_<filetime>', where filetime is the time of day that
this dataset was plotted.  For synthetic datasets (e.g. summed or subtracted)
for which there are no filenumbers, the filenames with the appropriate operator are
used.

(2) Details on the precise data reduction settings should be stored in the file
metadata, which can be interrogated if the origin of a plot is uncertain.

(3) The axis names are not arbitrary and, in addition to those defined by the
NeXuS standard, should be chosen from the following predefined set: 
'Two theta'
'd-spacing'
These names are used by the d-spacing conversion to check which representation
the dataset is currently in.
"""
# Script control setup area
__script__.title     = 'ECH Reduction'
__script__.version   = '1.0'
Dataset.__dicpath__ = get_script_path() + '/lib/Echidna_path_table'
# For direct access to the selected filenames
#__datasource__ = __register__.getDataSourceViewer()
# Git will embed the blob here
__UI_gitversion = "$Id$"

''' User Interface '''
# Plot Helper: always Plot2 to Plot 3
# At the top for convenience

run_button = Act('__run_script__(__get_selected_files__())', 'Reduce Selected File')

plh_copy = Act('plh_copy_proc()', 'Press to Copy Plot 2 to Plot 3')
copy_acc = Par('bool','False')
copy_acc.title = 'Auto copy to Plot 3'
Group('Copy Datasets').add(plh_copy,copy_acc)

# Output Folder
#out_folder = Par('file')
#out_folder.dtype = 'folder'
output_xyd = Par('bool','False')
output_xyd.title = "XYD"
#output_cif = Par('bool','True')   #always True
#output_cif.title = "CIF"
output_fxye = Par('bool','True')
output_fxye.title = "GSAS FXYE"
output_stem = Par('string','reduced')
output_stem.title = "Include in filename:"
Group('Output Format').add(output_xyd,output_fxye)
Group('Output Filename: ECH00NNNNN_+...').add(output_stem)
# Normalization
# We link the normalisation sources to actual dataset locations right here, right now
norm_table = {'Monitor 1':'bm1_counts','Monitor 2':'bm2_counts',
              'Monitor 3':'bm3_counts','Total counts':'total_counts',
              'Detector time':'detector_time'}
norm_apply     = Par('bool', 'True')
norm_apply.title = 'Apply'
norm_reference = Par('string', 'Monitor 3', options = norm_table.keys())
norm_reference.title = 'Source'
norm_target    = 'auto'
norm_plot = Act('plot_norm_proc()','Plot')
norm_plot_all = Act('plot_all_norm_proc()','Plot all')
Group('Normalization').add(norm_apply, norm_reference,norm_plot_all,norm_plot)

# Background Correction
bkg_apply = Par('bool', 'False')
bkg_apply.title = 'Apply'
bkg_map   = Par('file', '')
bkg_map.title = 'Bkg File'
bkg_map.ext = '*.hdf'
bkg_show  = Act('bkg_show_proc()', 'Show') 
Group('Background Correction').add(bkg_apply, bkg_map, bkg_show)

# Vertical Tube Correction
vtc_apply = Par('bool', 'True')
vtc_apply.title = 'Apply'
vtc_file  = Par('file', '', options = ['', 'Vertical_offsets_2014_04_28.txt'])
vtc_file.title = 'VTC file'
vtc_file.ext = '*.txt,*.*'
vtc_show  = Act('vtc_show_proc()', 'Show') 
Group('Vertical Tube Correction').add(vtc_apply, vtc_file, vtc_show)

# Efficiency Correction
eff_apply = Par('bool', 'True')
eff_apply.title = 'Apply'
eff_map   = Par('file', '', options = ['', 'eff_2014_04_28.cif'])
eff_map.title = 'Efficiency File'
eff_map.ext = '*.*'
eff_show  = Act('eff_show_proc()', 'Show') 
Group('Efficiency Correction').add(eff_apply, eff_map, eff_show)

# Horizontal Tube Correction
htc_apply = Par('bool', 'True')
htc_apply.title = 'Apply'
htc_file  = Par('file', '', options = ['', 'echidna.ang'])
htc_file.title = 'HTC file'
htc_file.ext = '*.ang,*.*'
htc_show  = Act('htc_show_proc()', 'Show') 
Group('Horizontal Tube Correction').add(htc_apply, htc_file, htc_show)

# Assemble (note this is before rescaling)
asm_drop_frames = Par('string')
asm_drop_frames.title = 'Remove frames:'
Group('Assemble frames').add(asm_drop_frames)

# Vertical Integration (note that gain recalc will include vertical integration)
vig_lower_boundary = Par('int', '0')
vig_lower_boundary.title = 'Lower limit'
vig_upper_boundary = Par('int', '127')
vig_upper_boundary.title = 'Upper limit'
vig_apply_rescale  = Par('bool', 'False')
vig_apply_rescale.title = 'Rescale'
vig_rescale_target = Par('float', '10000.0')
vig_rescale_target.title = 'Rescale target:'
vig_cluster = Par('string','Sum',options=['Sum','Merge','None'])
vig_cluster.title = 'Treatment of close points:'
Group('Vertical Integration').add(vig_lower_boundary, vig_upper_boundary, vig_cluster, vig_apply_rescale, vig_rescale_target)

# Recalculate gain
regain_apply = Par('bool','False')
regain_apply.title = 'Apply'
regain_iterno = Par('int','5')
regain_iterno.title = 'Iterations'
Group('Recalculate Gain').add(regain_apply,regain_iterno)


# Allow summation of plots
plh_sum = Act('plh_sum_proc()','Sum datasets')
plh_sum_type = Par('string','Ideal',options=['Ideal','Cluster','Merge Only'])
plh_cluster = Par('float','0.03')
plh_file = Par('file','')
Group('Sum 1D datasets in Plot 3').add(plh_file,plh_sum_type,plh_cluster,plh_sum)

# Delete plots
plh_dataset = Par('string', 'All', options = ['All'])
plh_delete  = Act('plh_delete_proc()', 'Delete')
Group('Delete 1D Datasets').add(plh_dataset, plh_delete)

# Plot settings
ps_plotname = Par('string','Plot 2',options=['Plot 2','Plot 3'])
ps_dspacing = Par('bool',False,command='dspacing_change()')
Group('Plot settings').add(ps_plotname,ps_dspacing)

# Storage for efficiency map
if not 'eff_map_cache' in globals():
    eff_map_cache = {}
    
''' New global plots'''
#if 'Plot4' not in globals() or 'Disposed' in str(Plot4.pv):
#    Plot4 = Plot(title='Chi-squared behaviour')
#    Plot4.close = noclose
#if 'Plot5' not in globals() or 'Disposed' in str(Plot5.pv):
#    Plot5 = Plot(title='Final gain values')
#    Plot5.close = noclose

''' Button Actions '''

def show_helper(filename, plot, pre_title = ''):
    if filename:
        
        ds = Dataset(str(filename))
        
        if ds.ndim == 4:
            plot.set_dataset(ds[0, 0])
            plot.title = ds.title + " (first frame)"
        elif ds.ndim == 3:
            plot.set_dataset(ds[0])
            plot.title = ds.title + " (first frame)"
        else:
            plot.set_dataset(ds)
        
        if pre_title:
            plot.title = pre_title + plot.title
            
    else:
        print 'no valid filename was specified'
            
# Plot normalisation info
def plot_norm_proc():
    plot_norm_master()

def plot_all_norm_proc():
    """Plot all normalisation values found in file"""
    plot_norm_master(all_mons=True)

def plot_norm_master(all_mons = False):
#    dss = __datasource__.getSelectedDatasets()
    dss = __get_selected_files__()
    if Plot2.ds:
        remove_list = copy(Plot2.ds)  #otherwise dynamically changes
        for ds in remove_list:
            Plot2.remove_dataset(ds)  #clear doesn't work
    for fn in dss:
        loc = fn
        dset = df[str(loc)]
        print 'Dataset %s' % os.path.basename(str(loc))
        for monitor_loc in norm_table.keys():
            if all_mons or monitor_loc == str(norm_reference.value):
                norm_source = norm_table[monitor_loc]
                plot_data = Dataset(getattr(dset,norm_source))
                if norm_apply.value or all_mons:
                    ave_val = plot_data.sum()/len(plot_data)
                    plot_data = plot_data/ave_val
                plot_data.title = os.path.basename(str(loc))+':' + str(monitor_loc) + '_'
                send_to_plot(plot_data,Plot2,add=True)
        
# show Background Correction Map
def bkg_show_proc():
    show_helper(bkg_map.value, Plot1, "Background Map: ")

# show Efficiency Correction Map 
def eff_show_proc():
    from Reduction import reduction
    eff_map_canonical = get_calibration_path() + '/' + eff_map.value
    if eff_map_canonical[0:5] != 'file:':
        eff_map_canonical = 'file:' + eff_map_canonical
    if not eff_map_canonical in eff_map_cache:
        eff_map_cache[eff_map_canonical] = reduction.read_efficiency_cif(eff_map_canonical)
    else:
        print 'Found in cache ' + `eff_map_cache[eff_map_canonical]`
    Plot1.clear()
    Plot1.set_dataset(eff_map_cache[eff_map_canonical][0])
    Plot1.title = 'Efficiency map'  #add info to this title!

# For HDF files
# show_helper(eff_map.value, Plot1, "Efficiency Map: ")

# show Vertical Tube Correction
def vtc_show_proc():
    if vtc_file.value:
        f = None
        try:
            # open file
            f  = open(get_calibration_path() + '/' + str(vtc_file.value), 'r')
            ds = zeros(128, int)
            
            # read file
            for line in f:
                if type(line) is str:
                    line = line.strip()
                    if (len(line) > 0) and not line.startswith('#'):
                        items = line.split()
                        if len(items) == 2:
                            x_index  = int(items[0]) - 1 # from [1..128] to [0..127]
                            offset   = int(items[1])
                            
                            ds[x_index]     = offset
                            ds.var[x_index] = 0
                            
            # show result
            ds.title = 'Vertical Tube Correction'
            
            # show plot
            Plot3.set_dataset(ds)

        finally:
            if f != None:
                f.close()
    else:
        print 'no valid filename was specified'
# show Horizontal Tube Correction
def htc_show_proc():
    global Plot3
    if htc_file.value:
        f = None
        try:
            # open file
            f  = open(get_calibration_path() + '/' + str(htc_file.value), 'r')
            ds = zeros(128)
            
            # read file
            index = 0
            for line in f:
                if type(line) is str:
                    line = line.strip()
                    if (len(line) > 0) and not line.startswith('#'):
                        ds[index]     = float(line)
                        ds.var[index] = 0
                        index         += 1
                            
            # show result
            ds.title = 'Horizontal Tube Correction'
            
            # show plot
            Plot3.set_dataset(ds)

        finally:
            if f != None:
                f.close()
    else:
        print 'no valid filename was specified'

def plh_copy_proc():
    # We copy from Plot 2 to Plot 3 only
    print 'Test printing from button actions'
    src = 'Plot 2'
    dst = 'Plot 3'
    
    plots = {'Plot 1': Plot1, 'Plot 2': Plot2, 'Plot 3': Plot3}

    src_plot = plots[src]
    dst_plot = plots[dst]
    
    src_ds = src_plot.ds
    if type(src_ds) is not list:
        print 'source plot does not contain 1D datasets'
        return
    
    dst_ds = dst_plot.ds
    if type(dst_ds) is not list:
        dst_ds = []
    
    dst_ds_ids = [id(ds) for ds in dst_ds]
    
    for ds in src_ds:
        if id(ds) not in dst_ds_ids:
            send_to_plot(ds,dst_plot,add=True,add_timestamp=False)

def plh_plot_changed():
    
    target = str(plh_plot.value)
    
    plots = {'Plot 1': Plot1, 'Plot 2': Plot2, 'Plot 3': Plot3}
    
    if not target in plots:
        print 'specify source plot'
        plh_dataset.options = []
        return
    
    target_plot = plots[target]
    target_ds   = target_plot.ds
    target_list = ['All']
    
    if (type(target_ds) is not list) or (len(target_ds) == 0):
        print 'target plot does not contain 1D datasets'
        plh_dataset.options = []
        return
    
    for ds in target_ds:
        target_list.append(ds.title)
    
    plh_dataset.options = target_list
    plh_dataset.value   = 'All'

def plh_delete_proc():
    # Plot 3 hard-coded for simplicity
    target  = 'Plot 3'
    dataset = str(plh_dataset.value)
    
    plots = {'Plot 1': Plot1, 'Plot 2': Plot2, 'Plot 3': Plot3}
    
    if not target in plots:
        print 'specify source plot'
        plh_dataset.options = []
        return
    
    target_plot = plots[target]
    target_ds   = copy(target_plot.ds)
    
    if (type(target_ds) is not list) or (len(target_ds) == 0):
        print 'target plot does not contain 1D datasets'
        plh_dataset.options = []
        return
    
    if dataset == 'All':
        for ds in target_ds:
            target_plot.remove_dataset(ds)
        plh_dataset.options = []
    else:
        for ds in target_ds:
            if ds.title == dataset:
                target_plot.remove_dataset(ds)
        target_list = ['All']
        for ds in target_plot.ds:
            target_list.append(ds.title)
        plh_dataset.options = target_list
        plh_dataset.value   = 'All'

def plh_sum_proc():
    """Sum all datasets contained in plot 3"""
    from Reduction import reduction
    from Formats import output
    filename = str(plh_file.value)
    datasets = Plot3.ds
    approach = str(plh_sum_type.value)
    if approach == 'Ideal':
        newds = reduction.sum_datasets(datasets)
        send_to_plot(newds,Plot2,add=False)
    else:
        newds = reduction.merge_datasets(datasets)
        if approach == 'Cluster':
            cluster = float(plh_cluster.value)
            if cluster > 0:
                newds = reduction.debunch(newds,cluster)
        send_to_plot(newds,Plot2,add=False)
        # Write to file
    if filename != '':
            output.write_cif_data(newds,filename)
            if output_xyd.value:
                output.write_xyd_data(newds,filename)
            if output_fxye.value:
                output.write_fxye_data(newds,filename)

def dspacing_change():
    """Toggle the display of d spacing on the horizontal axis"""
    global Plot2,Plot3
    from Reduction import reduction
    plot_table = {'Plot 2':Plot2, 'Plot 3':Plot3}
    target_plot = plot_table[str(ps_plotname.value)]
    if target_plot.ds is None:
        return
    # Preliminary check we are not displaying something
    # irrelevant, e.g. monitor counts
    for ds in target_plot.ds:
        if ds.axes[0].name not in ['Two theta','d-spacing']:
            print 'Not converting, have axis = %s' % ds.axes[0].name
            return
    change_dss = copy(target_plot.ds)
    # Check to see what change is required
    need_d_spacing = ps_dspacing.value
    # target_plot.clear() causes problems; use 'remove' instead
    # need to set the xlabel by hand due to gplot bug
    if need_d_spacing: target_plot.x_label = 'd-spacing (Angstroms)'
    elif not need_d_spacing: target_plot.x_label = 'Two theta (Degrees)'
    target_plot.y_label = 'Intensity'
    for ds in change_dss:
        current_axis = ds.axes[0].name
        print '%s has axis %s' % (ds.title,current_axis)
        if need_d_spacing:    
            result = reduction.convert_to_dspacing(ds)
        elif not need_d_spacing:
            result = reduction.convert_to_twotheta(ds)
        if result == 'Changed':
            target_plot.remove_dataset(ds)
            target_plot.add_dataset(ds)

# The preference system: 
def load_user_prefs(prefix = ''):
    """Load preferences, optionally prepending the value of
    prefix in the preference search.  This is typically used
    to load an alternative set of preferences"""
    # Run through our parameters, looking for the corresponding
    # preferences
    try:
        p = globals().keys()
    except AttributeError:
        p = globals().scope_keys()
    for name in p:
        if eval('isinstance('+ name + ',Par)'):
            execstring = name + '.value = "' + get_pref_value(prefix+name) + '"'
            try:
                exec execstring in globals()
            except:
                print 'Failure setting %s to %s' % (name,str(get_pref_value(prefix+name)))
            print 'Set %s to %s' % (name,str(eval(name+'.value')))

def save_user_prefs(prefix=''):
    """Save user preferences, optionally prepending the value of
    prefix to the preferences. This prefix is typically used to
    save an alternative set of preferences.  Return lists of values
    as ASCII strings for logging purposes"""
    print 'In save user prefs'
    prof_names = []
    prof_vals = []
    # sneaky way to get all the preferences
    try: 
        p = globals().keys()
    except AttributeError:
        p = globals().scope_keys()
    for name in p:
        if eval('isinstance('+ name + ',Par)'):
            prof_val = str(eval(name + '.value'))
            set_pref_value(prefix+name,prof_val)
            print 'Set %s to %s' % (prefix+name,str(get_pref_value(prefix+name)))
            prof_names.append(name)
            prof_vals.append(prof_val)
    save_pref()
    return prof_names,prof_vals        

''' Script Actions '''

# This function is called when pushing the Run button in the control UI.
def __run_script__(fns):
    
    from Reduction import reduction,AddCifMetadata
    from os.path import basename
    from os.path import join
    import os,re
    import time           #how fast are we going?
    from Formats import output
    
    elapsed = time.clock()
    print 'Started working at %f' % (time.clock()-elapsed)
    df.datasets.clear()
    
    # save user preferences
    prof_names,prof_values = save_user_prefs()

    # store current Git versions for data output
    code_versions = {"GUI":__UI_gitversion[4:-1],
                     "Reduction library":reduction.gitversion[4:-1]}
    # check input
    if (fns is None or len(fns) == 0) :
        print 'no input datasets'
        return

    # pre-check that we can write the result
#    output_destination = out_folder.value
    output_destination = get_save_path()
    if output_xyd.value or output_fxye.value:
        if not os.access(output_destination,os.W_OK):
            open_error("Unable to write to folder %s" % output_destination)
            return
    # check if input needs to be normalized
    if norm_apply.value:
        # norm_ref is the source of information for normalisation
        # norm_tar is the value norm_ref should become,
        # by multiplication.  If 'auto', the maximum value of norm_ref
        # for the first dataset is used, otherwise any number may be entered.
        norm_ref = str(norm_reference.value)
        if norm_ref.strip() == '':
            open_error("You have asked to apply normalisation but not specified any normalisation reference")
            return
        norm_tar = str(norm_target).lower()

        # check if normalization target needs to be determined
        if len(norm_tar) == 0:
            norm_ref = None
            norm_tar = None
            print 'WARNING: no reference for normalization was specified'
        elif norm_tar == 'auto':
            # set flag
            norm_tar = -1
            # iterate through input datasets
            location = norm_table[norm_ref]     
            print 'utilized reference value for "' + norm_ref + '" is:', norm_tar
            
        # use provided reference value
        else:
            norm_tar = float(norm_tar)
            
    else:
        norm_ref = None
        norm_tar = None
    
    # check if bkg-map needs to be loaded
    if bkg_apply.value:
        if not bkg_map.value:
            bkg = None
            print 'WARNING: no bkg-map was specified'
        else:
            bkg = Dataset(str(bkg_map.value))
    else:
        bkg = None
    
    # check if eff-map needs to be loaded
    if eff_apply.value:
        if not eff_map.value:
            eff = None
            print 'WARNING: no eff-map was specified'
        else:
            eff_map_canonical = get_calibration_path() + '/' + eff_map.value
            if eff_map_canonical[0:5] != 'file:':
                eff_map_canonical = 'file:' + eff_map_canonical
            if not eff_map_canonical in eff_map_cache:
                try:
                    eff_map_cache[eff_map_canonical] = reduction.read_efficiency_cif(eff_map_canonical)
                except:
                    open_error("Failed to read efficiency file %s" % eff_map_canonical)
                    return
            else:
                print 'Found in cache ' + `eff_map_canonical`
        eff = eff_map_cache[eff_map_canonical]
    else:
        eff = None
    
    # check if vertical tube correction needs to be loaded
    if vtc_apply.value:
        if not vtc_file.value:
            vtc = None
            print 'WARNING: no vtc-file was specified'
        else:
            vtc = get_calibration_path() + '/' + str(vtc_file.value)
    else:
        vtc = None
    # check if horizontal tube correction needs to be loaded
    if htc_apply.value:
        if not htc_file.value:
            htc = None
            print 'WARNING: no htc-file was specified'
        else:
            htc = get_calibration_path() + '/' + str(htc_file.value)
    else:
        htc = None
    
    reduced_files = []
    # iterate through input datasets
    # note that the normalisation target (an arbitrary number) is set by
    # the first dataset unless it has already been specified.
    for fn in fns:
        # load dataset
        ds = df[fn]
        # extract basic metadata
        ds = AddCifMetadata.extract_metadata(ds,codeversions=code_versions)
        AddCifMetadata.store_reduction_preferences(ds,prof_names,prof_values)
        # remove redundant dimensions and convert to floating point
        rs = ds.get_reduced()*1.0
        rs.copy_cif_metadata(ds)
        # check if normalized is required 
        if norm_ref:
            ds,norm_tar = reduction.applyNormalization(rs, reference=norm_table[norm_ref], target=norm_tar)
        else:
            ds = rs
        if bkg:
            ds = reduction.getBackgroundCorrected(ds, bkg, norm_table[norm_ref], norm_tar)
        
        print 'Finished normalisation, background subtraction at %f' % (time.clock()-elapsed)
        # check if vertical tube correction is required
        if vtc:
            ds = reduction.getVerticallyCorrected(ds, vtc)
        print 'Finished vertical offset correction at %f' % (time.clock()-elapsed)
        # check if efficiency correction is required
        if eff:
            ds = reduction.getEfficiencyCorrected(ds, eff)
        
        print 'Finished efficiency correction at %f' % (time.clock()-elapsed)
        # Before fiddling with axes, get the ideal stepsize
        stepsize = reduction.get_stepsize(ds)
        print 'Ideal stepsize determined to be %f' % stepsize
        # check if horizontal tube correction is required
        if htc:
            ds = reduction.getHorizontallyCorrected(ds, htc)

        print 'Finished horizontal correction at %f' % (time.clock()-elapsed)
        # Stitching. If we are recalculating gain, this is purely for
        # informational purposes. We don't want to take the 100x time penalty of
        # multiplying a 2D array by the gain factor for each tube, so we
        # stitch on a 1D array after doing the gain re-refinement.
        if ds.ndim > 2:
            stitched = reduction.getStitched(ds,ignore=str(asm_drop_frames.value))
        # Display dataset
        print 'Finished stitching at %f' % (time.clock()-elapsed)
        Plot1.set_dataset(stitched)
        Plot1.title = stitched.title
        # check if we are recalculating gain 
        if regain_apply.value:
           bottom = int(vig_lower_boundary.value)
           top = int(vig_upper_boundary.value)
           cs,gain,esds,chisquared,no_overlaps = reduction.do_overlap(ds,regain_iterno.value,bottom=bottom,top=top,
                                                          exact_angles=htc,drop_frames=str(asm_drop_frames.value))
           if cs is not None:
               print 'Have new gains at %f' % (time.clock() - elapsed)
               fg = Dataset(gain)
               fg.var = esds
           # set horizontal axis (ideal values)
#               Plot4.set_dataset(Dataset(chisquared))   #chisquared history
#               Plot5.set_dataset(fg)   #final gain plot
           else:
               open_error("Cannot do gain recalculation as the scan ranges do not overlap.")
               return
        if not vig_apply_rescale.value:
            norm_const = -1.0
        else:
            norm_const = float(vig_rescale_target.value)
        # set the cluster value
        if str(vig_cluster.value) in ['Merge','Sum']:
            cluster = (stepsize * 0.6,str(vig_cluster.value))  #60 percent of ideal
        else:
            cluster = (0.0,'None')
        if not regain_apply.value:  #already done
            final_result = reduction.getVerticalIntegrated(stitched, axis=0, normalization=norm_const,
                                                 cluster=cluster,bottom = int(vig_lower_boundary.value),
                                                 top=int(vig_upper_boundary.value))
            print 'Finished vertical integration at %f' % (time.clock()-elapsed)
        else:
            if str(vig_cluster.value) == 'Sum':  #simulate a sum for the gain recalculated value
                cs *= no_overlaps
                info_string = "\nFinal values were multiplied by %d to simulate summation of individual points." % no_overlaps
                cs.add_metadata("_pd_proc_info_data_reduction",info_string,append=True)
            final_result = cs
        # Display reduced dataset
        send_to_plot(final_result,Plot2)
        if copy_acc.value:   #user wants us to accumulate it
            plh_copy_proc()
        # Output datasets
        # Calculate inserted string: %s for sample name, %t for temperature
        stem = str(output_stem.value)
        stem = re.sub(r'[^\w+=()*^@~:{}\[\].%-]','_',stem)
        if '%s' in stem:
             samplename = final_result.harvest_metadata("CIF")['_pd_spec_special_details']
             name_front = samplename.split()[0]
             stem = stem.replace('%s',name_front)
        if '%t' in stem:
             temperature = 'Unknown_temperature'
             stem = stem.replace('%t',temperature)
        print 'Filename stem is now ' + stem
        filename_base = join(get_save_path(),basename(str(fn))[:-7] + '_' + stem)
        if output_xyd.value or output_fxye.value:  #write CIF if other files written
            output.write_cif_data(final_result,filename_base)
            reduced_files.append(filename_base + '.cif')
        if output_xyd.value:
            output.write_xyd_data(final_result,filename_base,codeversions=code_versions)
            reduced_files.append(filename_base + '.xyd')
        if output_fxye.value:
            output.write_fxye_data(final_result,filename_base,codeversions=code_versions)
            reduced_files.append(filename_base + '.xye')
        
        # ds.save_copy(join(str(out_folder.value), 'reduced_' + basename(str(fn))))
        print 'Finished writing data at %f' % (time.clock()-elapsed)
        
    if len(reduced_files) > 0:
        zip_files(reduced_files, 'Echidna_rd_' + str(int(time.time()))[2:] + '.zip')
        
''' Utility functions for plots '''
def send_to_plot(dataset,plot,add=False,change_title=True,add_timestamp=True):
    """This routine appends a timestamp to the dataset title
    in order to keep uniqueness of the title for later 
    identification purposes. It also maintains plot
    consistency in terms of displaying d-spacing."""
    from datetime import datetime
    from Reduction import reduction
    if add_timestamp:
        timestamp = datetime.now().strftime("%H:%M:%S")
        dataset.title = dataset.title + timestamp
    # Check d-spacing status
    if plot.ds:
        axis_name = ''
        try:
            axis_name = plot.ds[0].axes[0].name
        except IndexError:
            pass
        if axis_name == 'd-spacing':
            reduction.convert_to_dspacing(dataset)
        elif axis_name == 'Two theta':
            reduction.convert_to_twotheta(dataset)
    if add:
        plot.add_dataset(dataset)
    else:
        plot.set_dataset(dataset)
    if change_title:
        plot.title = dataset.title
    #Update any widgets that keep a track of the plots
    if plot == Plot3:   #Delete only operates on plot 3
        curves = ['All'] + map(lambda a:a.title,plot.ds)
        plh_dataset.options = curves
        plh_dataset.value = 'All'

# dispose
def __dispose__():
    global Plot1,Plot2,Plot3
    Plot1.clear()
    Plot2.clear()
    Plot3.clear()


''' Quick-Fix '''

def run_action(act):
    import sys
    act.set_running_status()
    try:
        exec(act.command)
        act.set_done_status()
    except:
        act.set_error_status()
        traceback.print_exc(file = sys.stdout)
        raise Exception, 'Error in running <' + act.text + '>'

''' Execute this each time it is loaded to reload user preferences '''

load_user_prefs()
