from gumpy.control.script import Script
from gumpy.control.param import Par, Act, Group
__model__ = __register__.getScriptModel()
__script__ = Script(__model__)
__script__.title = 'unknown'
__script__.version = 'unknown'

Par.__model__ = __model__
Act.__model__ = __model__
Group.__model__ = __model__
