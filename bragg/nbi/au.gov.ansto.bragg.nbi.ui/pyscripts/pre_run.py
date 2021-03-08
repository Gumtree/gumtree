from gumpy.control.script import Script
from gumpy.control.param import Par, Act, Group, Tab
__model__ = __register__.getScriptModel()
__script__ = Script(__model__)
__script__.title = '/'
__script__.version = '/'

Par.__model__ = __model__
Act.__model__ = __model__
Group.__model__ = __model__
Tab.__model__ = __model__

if '__dispose__' in globals() :
    __dispose__()
