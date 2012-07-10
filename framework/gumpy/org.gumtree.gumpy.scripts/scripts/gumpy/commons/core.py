from java.lang import Class

from org.gumtree.core.service import ServiceUtils

def getService(classname):
    return ServiceUtils.getService(Class.forName(classname))
