from java.lang import Class

from org.eclipse.e4.core.contexts import ContextInjectionFactory

from org.gumtree.core.service import ServiceUtils
from org.gumtree.util.eclipse import E4Utils

def getService(classname):
    return ServiceUtils.getService(Class.forName(classname))

def injectObject(object, eclipseContext=E4Utils.getEclipseContext()):
    ContextInjectionFactory.inject(object, eclipseContext)
    
class Injector:
    
    def __init__(self):
        self.eclipseContext = E4Utils.getEclipseContext().createChild()
        self.bindMap = {}
        
    def bind(self, variable, className):
        self.bindMap[variable] = className
        return self
    
    def setEclipseContext(self, eclipseContext):
        self.eclipseContext = eclipseContext
        return self
        
    def inject(self, object):
        for item in self.bindMap:
            value = self.eclipseContext.get(Class.forName(self.bindMap[item]))
            setattr(object, item, value)
        return self
