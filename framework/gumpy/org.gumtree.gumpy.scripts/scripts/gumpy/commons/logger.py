import sys
import time

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