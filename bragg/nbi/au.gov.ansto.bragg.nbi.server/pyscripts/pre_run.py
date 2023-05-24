from gumpy.control.script import Script
#from gumpy.control.param import Par, Act, Group
from gumpy.control.param import Par as _Par, Act as _Act, Group as _Group, Tab as _Tab

__model__ = __register__.getScriptModel()
__script__ = Script(__model__)
__script__.title = 'unknown'
__script__.version = 'unknown'

# Par.__model__ = __model__
# Act.__model__ = __model__
# Group.__model__ = __model__

def Par(ptype, default = None, options = None, command = None):
    return _Par(__model__, ptype, default, options, command)

def Act(command, text = 'Run'):
    return _Act(__model__, command, text)

def Group(name):
    return _Group(__model__, name)

def Tab(name):
    return _Tab(__model__, name)

if '__dispose__' in globals() :
    __dispose__()
