from threading import Thread, Timer
import time
import traceback
import sys
from datetime import datetime

INIT_DAEMON_PERIOD = 5

class DaemonThread(Thread):
    def __init__(self, group=None, target=None, name=None, 
        args=(), kwargs=None):
        Thread.__init__(self, group=group, target=target, name=name, 
                        args=args, kwargs=kwargs)
    
    def run(self):
        print('daemon thread created')
#         Timer(sec_to_quarter_hour(), daemon_process).start()
        Timer(INIT_DAEMON_PERIOD, daemon_process).start()
        while True:
            try:
                pass
            except:
                traceback.print_exc(file = sys.stderr)


if __name__ == '__main__':
    DaemonThread().start()
    
    