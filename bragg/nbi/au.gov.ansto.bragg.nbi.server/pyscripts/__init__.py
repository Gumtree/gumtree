from gumpy.vis.wplot import WebPlot
from java.lang import System
import zipfile

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

__instrument_id__ = 'gumtree.instrument.id'
#__DATASOURCE__ = __register__.getDataSourceViewer()
#__runner__ = __UI__.getRunner()
#__writer__ = __UI__.getScriptExecutor().getEngine().getContext().getWriter()

__loaded_files__ = []
__selected_files__ = []
__user_files__ = []
__selected_user_files__ = []
__is_user_file_domain__ = False

Plot1 = WebPlot(__register__.getPlot1())
Plot2 = WebPlot(__register__.getPlot2())
Plot3 = WebPlot(__register__.getPlot3())


def __set_loaded_files__(li):
    global __loaded_files__
    __loaded_files__ = li
    
def __set_selected_files__(li):
    global __selected_files__, __selected_user_files__, __is_user_file_domain__
    if __is_user_file_domain__:
        __selected_user_files__ = li
        __register__.getDataHandler().setSelectedData(li)
    else:
        __selected_files__ = li
        __register__.getDataHandler().setSelectedUserFiles(li)

def __set_user_files__(li):
    global __user_files__
    __user_files__ = li

#def __set_selected_user_files__(li):
#    global __selected_user_files__
#    __selected_user_files__ = li
#    __register__.getDataHandler().setSelectedUserFiles(li)
#    
def __append_user_files__(li):
    global __user_files__
    __user_files__ += li
    
def __get_selected_files__():
    global __selected_files__, __selected_user_files__, __is_user_file_domain__
    if __is_user_file_domain__:
        return __selected_user_files__
    else:
        return __selected_files__
    
def __get_loaded_files__():
    global __loaded_files__, __user_files__, __is_user_file_domain__
    if __is_user_file_domain__:
        return __user_files__
    else:
        return __loaded_files__
    
class ScriptingDatasetFactory(DatasetFactory):
    def __init__(self, path = None, prefix = None, factor = None):
        DatasetFactory.__init__(self, path, prefix, factor)
    
    def __getitem__(self, index):
        global __register__
        if type(index) is int:
            sname = '%(index)07d.nx.hdf' % {'index' : index}
            for key in self.datasets.keys() :
                if key.__contains__(sname) :
                    return self.datasets[str(key)]
            for loc in __get_loaded_files__() :
                if loc.__contains__(sname) :
                    return self.__getitem__(str(loc))
        else :
            for loc in __get_loaded_files__() :
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
    uuid = __register__.getUUID()
    if uuid :
        uuid += '_'
    value = __register__.getPreference(uuid + name)
    if value == None:
        value = ''
    else:
        value = str(value)
    return value

def set_pref_value(name, value):
    if value == None:
        value = ''
    uuid = __register__.getUUID()
    if uuid :
        uuid += '_'
    __register__.setPreference(uuid + name, value)
    
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

def get_user_data_path():
    return __register__.getUserPath()
    
def get_data_path():
    global __register__, __is_user_file_domain__
    if __is_user_file_domain__:
        return get_user_data_path()
    else:
        return __register__.getDataPath()

def get_save_path():
    return __register__.getSavePath()

def zip_files(files, zipname, in_user_area = True):
    print 'compressing result in ' + zipname
    if in_user_area:
        zfile = get_user_data_path() + '/' + zipname
    else :
        zfile = selectSaveFolder() + '/' + zipname
    f_out = zipfile.ZipFile(zfile, mode='w')
    for rfn in files:
        try:
            rfn = rfn.replace('\\', '/')
            f_out.write(rfn, arcname = rfn[rfn.rindex('/') + 1 :])
        except:
            print 'failed to zip'
            f_out.close()
    f_out.close()
    __append_user_files__([zfile])
    if in_user_area:
        __register__.reportAddingUserFiles([zfile])
        report_file(zipname, 'user')
    else:
        report_file(zipname, 'save')
    
def snapshot(uuid, obj = None):
    spath = __register__.getStorePath() + '/' + str(uuid)
    
    import java.io as io
    import org.python.util as util
    outFile = io.FileOutputStream(spath)
    outStream = io.ObjectOutputStream(outFile)
    if not obj is None:
        outStream.writeObject(obj)
    outFile.close( )
    
def download_selected_files():
    if len(__get_selected_files__()) == 0:
        return
    full_paths = []
    for f in __get_selected_files__():
        full_paths.append(get_data_path() + '/' + f)
    inst_id = System.getProperty(__instrument_id__)
    if inst_id is None :
        inst_id = 'DATA'
    z_name = inst_id.upper() + '_raw_' + str(int(time.time()))[2:] + '.zip'
    zip_files(full_paths, z_name, False)
    print 'data files have been zipped in ' + z_name
    
def remove_selected_user_files():
    global __selected_user_files__, __user_files__
    
    if len(__selected_user_files__) == 0:
        return
    for f in __selected_user_files__:
        for path in __user_files__:
            if path.__contains__(f) :
                __user_files__.remove(path)
                os.remove(path)
                break

