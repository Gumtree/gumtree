from au.gov.ansto.bragg.nbi.server.restlet import JythonRestlet
from gumpy.vis.wplot import WebPlot

Plot1 = WebPlot(JythonRestlet.getPlot1())
Plot2 = WebPlot(JythonRestlet.getPlot2())
Plot3 = WebPlot(JythonRestlet.getPlot3())

import traceback
import sys
from gumpy.nexus import *
from gumpy.control import param
from gumpy.control import script
#from gumpy.vis.image2d import Image
#from gumpy.vis.plot1d import Plot
#from gumpy.vis.gplot import GPlot, plot
#from gumpy.vis.event import MouseListener
from org.eclipse.core.resources import ResourcesPlugin
from au.gov.ansto.bragg.nbi.server.restlet import JythonModelRegister
from gumpy.commons.logger import log
import time
__register__ = JythonModelRegister.getRegister(__script_model_id__)
#__DATASOURCE__ = __register__.getDataSourceViewer()
#__runner__ = __UI__.getRunner()
#__writer__ = __UI__.getScriptExecutor().getEngine().getContext().getWriter()

__loaded_files__ = []
__selected_files__ = []

def __set_loaded_files__(li):
    global __loaded_files__
    __loaded_files__ = li
    
def __set_selected_files__(li):
    global __selected_files__
    __selected_files__ = li
    __register__.getDataHandler().setSelectedData(li)
    
class ScriptingDatasetFactory(DatasetFactory):
    def __init__(self, path = None, prefix = None, factor = None):
        DatasetFactory.__init__(self, path, prefix, factor)
    
    def __getitem__(self, index):
        global __register__, __selected_files__, __loaded_files__
        if type(index) is int:
            sname = '%(index)07d.nx.hdf' % {'index' : index}
            for key in self.datasets.keys() :
                if key.__contains__(sname) :
                    return self.datasets[str(key)]
            for loc in __loaded_files__ :
                if loc.__contains__(sname) :
                    return self.__getitem__(str(loc))
        else :
            for loc in __loaded_files__ :
                if loc.__contains__(index) :
                    return DatasetFactory.__getitem__(self, str(loc))
            return DatasetFactory.__getitem__(self, index)
        
        
df = ScriptingDatasetFactory()

def noclose():
    print 'not closable'
    
gumtree_root = str(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString())
def get_project_path(pname):
    return str(ResourcesPlugin.getWorkspace().getRoot().getProject(pname).getLocation().toString())

def get_absolute_path(spath):
    return str(ScriptControlViewer.getFullScriptPath(spath))
    
#def load_script(fname):
#    fname = os.path.dirname(__UI__.getScriptFilename()) + '/' + fname
#    __UI__.loadScript(fname)
    
#def confirm(msg): 
#    return __runner__.openConfirm(msg)
#
#def open_warning(msg): 
#    return __runner__.openWarning(msg)
#
#def open_information(msg): 
#    return __runner__.openInformation(msg)
#
def open_error(msg): 
#    return __runner__.openError(msg)
    raise Exception,msg
#
#def open_question(msg): 
#    return __runner__.openQuestion(msg)
#
def selectSaveFolder():
    return __register__.getSavePath()

if '__dispose__' in globals() :
    __dispose__()
    
def auto_run():
    pass

def run_action(act):
    act.set_running_status()
    try:
        exec(act.command)
        act.set_done_status()
#    except Exception, e:
#        raise e
    except:
        act.set_error_status()
        traceback.print_exc(file = sys.stdout)
        raise Exception, 'Error in running <' + act.text + '>'
    
def get_pref_value(name):
    value = __register__.getPreference(name)
    if value == None:
        value = ''
    else:
        value = str(value)
    return value

def set_pref_value(name, value):
    if value == None:
        value = ''
    __register__.setPreference(name, value)
    
def save_pref():
    __register__.savePreferenceStore()

def report_file(name, type = 'save'):
    __register__.reportFileForDownload(type + ":" + name)
    
def __dataset_added__():
    pass

def get_script_path():
    return __register__.getScriptPath()

def get_calibration_path():
    return __register__.getCalibrationPath()

def get_data_path():
    return __register__.getDataPath()

def get_save_path():
    return __register__.getSavePath()