import sys
import time
from org.gumtree.service.db import RemoteTextDbService, RemoteCatalogService

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
        try:
            self.logger.appendTextEntry(name, text)
        except:
            print 'failed to send text to notebook db'
        
    def log_plot(self, plot, name = None, footer = None):
        if name == None:
            name = "Scripting plot"
        if not plot is None and hasattr(plot, 'pv') :
            if footer is None:
                if hasattr(plot, 'title'):
                    footer = plot.title
            try:
                self.logger.appendImageEntry(name, plot.pv.getPlot().getImage(), footer)
            except:
                print 'failed to send plot to notebook db'
        elif not plot is None and hasattr(plot, 'cache') :
            if footer is None:
                if hasattr(plot, 'title'):
                    footer = plot.title
            try:
                from org.apache.commons.codec.binary import Base64
                from java.lang import String
                self.logger.appendImageEntry(name, String(Base64.encodeBase64(plot.cache.getImageCache())), footer)
            except:
                print 'failed to send plot to notebook db'
            
    def log_table(self, table, name = None):
        if name == None:
            name = "Scripting table"
        try:
            self.logger.appendTableEntry(name, table)
        except:
            print 'failed to send table to notebook db'
        
    def log(self, text, name = None):
        try:
            self.log_text(text, name)
        except:
            print 'failed to send text to notebook db'
        
class CatalogLogger():
    
    def __init__(self):
        self.logger = RemoteCatalogService.getInstance()
        
    def update(self, filename, columns):
        try:
            self.logger.updateEntry(filename, columns)
        except:
            print 'failed to send text to notebook db'
        
n_logger = NotebookLogger()
c_logger = CatalogLogger()