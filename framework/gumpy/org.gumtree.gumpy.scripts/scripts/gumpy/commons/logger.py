import time

def log(message, writer=None):
    currentTime = time.localtime()
    if writer == None:
        print '[' + time.strftime("%Y-%m-%d %H:%M:%S", time.localtime()) + '] ' + message
    else:
        writer.write('[' + time.strftime("%Y-%m-%d %H:%M:%S", time.localtime()) + '] ' + message)
