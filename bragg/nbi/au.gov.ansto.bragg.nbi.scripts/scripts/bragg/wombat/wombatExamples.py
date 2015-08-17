# Script control setup area
# script info
__script__.title = 'Wombat Scripting Examples'
__script__.version = '1.0'
Dataset.__dicpath__ = get_script_path() + '/lib/Wombat_path_table'

DS = None

show_dimension_action = Act('show_dimension()', 'Click to Print Dimension Info of Selected Files')
Group('Example 1: Access Selected Dataset').add(show_dimension_action)
def show_dimension():
    li = __get_selected_files__()
    if len(li) == 0:
        open_error('Please select a file from the file source view.')
        return

    for name in li:
        ds = df[name]
        print 'shape of ' + name + ': ' + str(ds.shape)
    
frame_index_arg = Par('int', 0, options = [0], command = 'plot_selected_frame()')
def plot_selected_frame():
    global DS
    if DS is None:
        print 'please click on the button first'
    
    ds_i = DS[frame_index_arg.value]
    Plot1.set_dataset(ds_i)

plot_frame_action = Act('plot_file()', 'Click to Plot the Selected File')
Group('Example 2: Plot Selected Dataset').add(frame_index_arg, plot_frame_action)
def plot_file():
    global DS
    li = __get_selected_files__()
    if len(li) == 0:
        open_error('Please select a file from the file source view.')
        return

    df.datasets.clear()
    DS = df[li[0]]
    frame_index_arg.options = range(len(DS))
    ds_0 = DS[0]
    Plot1.set_dataset(ds_0)
    
    
# This function is called when pushing the Run button in the control UI.
def __run_script__(fns):
    # __get_selected_files__() returns the selected files as a list
    show_dimension()