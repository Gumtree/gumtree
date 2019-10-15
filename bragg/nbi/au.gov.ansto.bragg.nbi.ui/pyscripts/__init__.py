import traceback
import sys
from gumpy.nexus import *
from gumpy.control import param
from gumpy.control import script
from au.gov.ansto.bragg.nbi.ui.scripting import ScriptPageRegister
from au.gov.ansto.bragg.nbi.ui.scripting.parts import ScriptControlViewer
from gumpy.vis.image2d import Image
from gumpy.vis.plot1d import Plot
from gumpy.vis.gplot import GPlot, plot
from gumpy.vis.event import MouseListener
from org.eclipse.core.resources import ResourcesPlugin
from gumpy.commons.logger import log
from au.gov.ansto.bragg.nbi.ui.scripting.parts import ScriptRunner
from gumpy.commons import sics
import time
__register__ = ScriptPageRegister.getRegister(__script_model_id__)
__UI__ = __register__.getControlViewer()
__DATASOURCE__ = __register__.getDataSourceViewer()
#__model__ = __register__.getScriptModel()
#__script__ = Script(__model__)
#__script__.title = 'unknown'
#__script__.version = 'unknown'
__runner__ = __UI__.getRunner()
__writer__ = __UI__.getScriptExecutor().getEngine().getContext().getWriter()
__err_writer__ = __UI__.getScriptExecutor().getEngine().getContext().getErrorWriter()
def logln(text):
    return log(text, __writer__)
def logErr(text):
    return log(text, __err_writer__)
    
clear = script.clear

class ScriptingDatasetFactory(DatasetFactory):
    def __init__(self, path = None, prefix = None, factor = None):
        DatasetFactory.__init__(self, path, prefix, factor)
    
    def __getitem__(self, index):
        global __DATASOURCE__
        if type(index) is int:
            sname = '%(index)07d.nx.hdf' % {'index' : index}
            for key in self.datasets.keys() :
                if key.__contains__(sname) :
                    return self.datasets[str(key)]
            for dinfo in __DATASOURCE__.getDatasetList():
                loc = dinfo.getLocation()
                if loc.__contains__(sname) :
                    return self.__getitem__(str(loc))
        else :
            return DatasetFactory.__getitem__(self, index)
        
        
df = ScriptingDatasetFactory()

def noclose():
    print 'not closable'
    
if __register__.getPlot1() != None:
    Plot1 = GPlot(widget=__register__.getPlot1())
    Plot1.close = noclose
elif __register__.getObject('Plot1') != None:
    Plot1 = GPlot(widget=__register__.getObject('Plot1'))
    Plot1.close = noclose
if __register__.getPlot2() != None:
    Plot2 = GPlot(widget=__register__.getPlot2())
    Plot2.close = noclose
elif __register__.getObject('Plot2') != None:
    Plot2 = GPlot(widget=__register__.getObject('Plot2'))
    Plot2.close = noclose
if __register__.getPlot3() != None:
    Plot3 = GPlot(widget=__register__.getPlot3())
    Plot3.close = noclose
elif __register__.getObject('Plot3') != None:
    Plot3 = GPlot(widget=__register__.getObject('Plot3'))
    Plot3.close = noclose
    
__INFOTEXT__ = None
if __register__.getInfoViewer() != None:
    __INFOTEXT__ = __register__.getInfoViewer()
    
gumtree_root = str(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString())
def get_project_path(pname):
    return str(ResourcesPlugin.getWorkspace().getRoot().getProject(pname).getLocation().toString())

def get_absolute_path(spath):
    return str(ScriptControlViewer.getFullScriptPath(spath))
    
def load_script(fname):
    fname = os.path.dirname(__UI__.getScriptFilename()) + '/' + fname
    __UI__.loadScript(fname)
    
def confirm(msg): 
    return __runner__.openConfirm(msg)

def open_warning(msg): 
    return __runner__.openWarning(msg)

def open_information(msg): 
    return __runner__.openInformation(msg)

def open_error(msg): 
    return __runner__.openError(msg)

def open_question(msg): 
    return __runner__.openQuestion(msg)

def pause(msg = None):
    if msg is None:
        msg = 'Paused, please press OK to continue.'
    else:
        msg = 'Paused, please press OK to continue.\n' + str(msg)
    return __runner__.openInformation(msg)
    
def selectSaveFolder():
    return __runner__.selectSaveFolder()

def selectSaveFile(ext = None, ws_path = None, fn = None):
    if ext is None:
        ext = []
    return __runner__.selectSaveFile(ext, ws_path, fn)

def selectLoadFile(ext = None, ws_path = None):
    if ext is None:
        ext = []
    return __runner__.selectLoadFile(ext, ws_path)
    
if '__dispose__' in globals() :
    __dispose__()
    
def auto_run():
    pass

def run_action(act):
    logln('pressed on button: ' + str(act.text))
    act.set_running_status()
    try:
        exec(act.command)
        act.set_done_status()
#    except Exception, e:
#        raise e
    except:
        if sics.getSicsController() != None: 
            act.set_interrupt_status()
        act.set_error_status()
        traceback.print_exc(file = sys.stdout)
        raise Exception, 'Error in running <' + act.text + '>'
    if not act.no_interrupt_check == 'True':
        if sics.getSicsController() != None:
            sics.handleInterrupt()
    
def validate_script(script):
    print(script)
    __UI__.validateScript(script)
    
def get_pref_value(name):
    value = __UI__.getPreference(name)
    if value == None:
        value = ''
    else:
        value = str(value)
    return value

def get_prof_value(name):
    return get_pref_value(name)
    
def set_pref_value(name, value):
    if value == None:
        value = ''
    __UI__.setPreference(name, value)
    
def set_prof_value(name, value):
    set_pref_value(name, value)
    
def save_pref():
    __UI__.savePreferenceStore()

def __dataset_added__(fns = None):
    pass

__selected_dataset__ = []
def __dataset_selected__(datasets):
    global __selected_dataset__
    __selected_dataset__ = datasets
    