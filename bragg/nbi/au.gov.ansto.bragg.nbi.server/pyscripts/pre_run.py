print '0'
from gumpy.control.script import Script
from gumpy.control.param import Par, Act, Group
print '1'
__model__ = __register__.getScriptModel()
__script__ = Script(__model__)
__script__.title = 'unknown'
__script__.version = 'unknown'
print '2'

Par.__model__ = __model__
Act.__model__ = __model__
Group.__model__ = __model__

print '3'
if '__dispose__' in globals() :
    __dispose__()
print '4'