import sys
import time
from org.gumtree.service.db import RemoteTextDbService

__global_writer__ = None

def log(message, writer=None):
    global __global_writer__
    currentTime = time.localtime()
    if writer == None:
        if not __global_writer__ is None:
            __global_writer__.write('[' + time.strftime("%Y-%m-%d %H:%M:%S", time.localtime()) + '] ' + message + '\n')
        else:
            print '[' + time.strftime("%Y-%m-%d %H:%M:%S", time.localtime()) + '] ' + message
    else:
        writer.write('[' + time.strftime("%Y-%m-%d %H:%M:%S", time.localtime()) + '] ' + message + '\n')

class Logger():
    
    def __init__(self, writer = None):
        if writer == None:
            self.__writer__ = sys.stdout
        else :
            self.__writer__ = writer
        
    def log(self, message):
        currentTime = time.localtime()
        if self.__writer__ == None:
            print '[' + time.strftime("%Y-%m-%d %H:%M:%S", time.localtime()) + '] ' + message
        else:
            self.__writer__.write(str(self.__writer__) + '[' + time.strftime("%Y-%m-%d %H:%M:%S", time.localtime()) + '] ' + message + '\n')
            
class NotebookLogger():
    
    def __init__(self):
        self.logger = RemoteTextDbService.getInstance()
        
    def log_text(self, text, name = None):
        if name == None:
            name = "Scripting log"
        self.logger.appendTextEntry(name, text)
        
    def log_plot(self, plot, name = None, footer = None):
        if name == None:
            name = "Scripting plot"
        self.logger.appendImageEntry(name, plot.pv.getPlot().getImage(), footer)
        
    def log_table(self, table, name = None):
        if name == None:
            name = "Scripting table"
        self.logger.appendTableEntry(name, table)
        
    def log(self, text, name = None):
        self.log_text(text, name)
        
n_logger = NotebookLogger()