import traceback
import sys
from gumpy.nexus import *
from gumpy.control import param
from gumpy.control import script
from gumpy.control.param import Par
from gumpy.control.param import Act
from gumpy.control.param import Group
from gumpy.control.script import *
from au.gov.ansto.bragg.nbi.ui.scripting import ScriptPageRegister
from gumpy.vis.image2d import Image
from gumpy.vis.plot1d import Plot
from gumpy.vis.gplot import GPlot
from gumpy.vis.event import MouseListener
from org.eclipse.core.resources import ResourcesPlugin
from gumpy.commons.logger import log
from au.gov.ansto.bragg.nbi.ui.scripting.parts import ScriptRunner
from gumpy.commons import sics
import time
__register__ = ScriptPageRegister.getRegister(__script_model_id__)
__UI__ = __register__.getControlViewer()
__DATASOURCE__ = __register__.getDataSourceViewer()
__model__ = __register__.getScriptModel()
__script__ = Script(__model__)
__script__.title = 'unknown'
__script__.version = 'unknown'
__runner__ = __UI__.getRunner()
__writer__ = __UI__.getScriptExecutor().getEngine().getContext().getWriter()
def logln(text):
    log(text, __writer__)
clear = script.clear
Par.__model__ = __model__
Act.__model__ = __model__
Group.__model__ = __model__
df = script.df
Plot1 = GPlot(widget=__register__.getPlot1())
Plot2 = GPlot(widget=__register__.getPlot2())
Plot3 = GPlot(widget=__register__.getPlot3())
gumtree_root = str(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString())
def noclose():
    print 'not closable'
    
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

def selectSaveFolder():
    return __runner__.selectSaveFile()

Plot1.close = noclose
Plot2.close = noclose
Plot3.close = noclose

if '__dispose__' in globals() :
    __dispose__()
    
def auto_run():
    pass

def run_action(act):
    act.set_running_status()
    try:
        exec(act.command)
        act.set_done_status()
    except Exception, e:
        act.set_interrupt_status()
        raise Exception, e.message
    except:
        act.set_error_status()
        traceback.print_exc(file = sys.stdout)
        raise Exception, 'Error in running <' + act.text + '>'
    sics.handleInterrupt()
    
def slog(text):
    global __file_logger__
    logln(text + '\n')
    try:
        tsmp = strftime("[%Y-%m-%d %H:%M:%S]", localtime())
        __file_logger__.write(tsmp + ' ' + text + '\n')
        __file_logger__.flush()
    except:
        print 'failed to log'