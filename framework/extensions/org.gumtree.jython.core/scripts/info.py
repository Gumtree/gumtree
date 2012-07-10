import __main__
import __builtin__
import inspect as __inspect__
    
def __info__(source):
    infoList = []
    # Append scope
    if (source == ''):
        source = '__main__'
    else:
        source = '__main__' + '.' + source
    # Do dir
    try:
        result = eval('dir(' + source + ')')
        for item in result:
            if (not item.startswith('_')):
                try :
                    # find type
                    itemObject = eval('(' + source + '.' + item + ')')
                    itemType = type(itemObject)
                    argspec = ''
                    if __inspect__.isfunction(itemObject):
                        argspec = __inspect__.getargspec(itemObject)
                    infoList.append([item, itemType, argspec])
                except:
                    pass
    except:
        return infoList
    return infoList
