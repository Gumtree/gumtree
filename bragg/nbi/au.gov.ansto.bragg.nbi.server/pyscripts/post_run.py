def set_name(obj, name):
	obj.name = name

for __name__to__test__ in globals().keys() :
	if eval('isinstance(' + __name__to__test__ + ', _Par) or isinstance(' + __name__to__test__ + ', _Act)') :
		eval('set_name(' + __name__to__test__ + ', \'' + __name__to__test__ + '\')')

__model__.fireModelChanged()
if hasattr(__script__, 'dict_path') and __script__.dict_path != None:
	Dataset.__dicpath__ = __script__.dict_path