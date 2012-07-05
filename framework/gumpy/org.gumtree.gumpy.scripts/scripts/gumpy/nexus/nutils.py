import os.path

def get_parent_path(pkg_name) :
    items = str(pkg_name).split(os.path.sep)
    d_path = ''
    for i in xrange(len(items) - 1) :
        d_path += items[i] + os.path.sep
    return d_path
    
