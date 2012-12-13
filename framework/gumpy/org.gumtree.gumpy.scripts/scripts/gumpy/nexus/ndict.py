def parse(ds):
    root = ds.__iNXroot__
    entry = root.getFirstEntry()
    return parse_group(entry)
    
def parse_group(group):
    ndict = dict()
    for item in group.getDataItemList():
        path = item.getName()
        try:
            path = '$entry' + path[path.index('/'):]
        except:
            pass
        ndict[item.getShortName()] = path
    for sgroup in group.getGroupList():
        ndict.update(parse_group(sgroup))
    return ndict
    
def process(ds, path):
    ndict = parse(ds)
    f = open(path, 'w')
    it = ndict.iteritems()
    space = ' ' * 24
    while True:
        try :
            pair = it.next()
            name = pair[0]
            if len(name) < 24:
                name += space[:24 - len(name)] 
            f.write(name + ' = ' + pair[1] + '\n')
        except:
            break
    f.close()
