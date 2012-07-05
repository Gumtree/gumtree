from gumpy.commons import core
from org.eclipse.ui import PlatformUI

class Application:
    
    def __init__(self):
        self.services = Services()
        self.workbench = PlatformUI.getWorkbench()

class Services:
    
    def __init__(self):
        self.serviceManager = core.getService('org.gumtree.core.service.IServiceManager')
        self.scriptingManager = core.getService('org.gumtree.scripting.IScriptingManager')
        self.dataAccessManager = core.getService('org.gumtree.service.dataaccess.IDataAccessManager')
        self.directoryService = core.getService('org.gumtree.service.directory.IDirectoryService')
        self.preferencesManager = core.getService('org.gumtree.service.preferences.IPreferencesManager')
