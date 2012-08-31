from org.eclipse.e4.core.contexts import ContextInjectionFactory
from org.eclipse.e4.ui.model.application.ui.basic import MBasicFactory
from org.eclipse.e4.ui.model.application.ui.advanced import MAdvancedFactory
from org.eclipse.jface.util import SafeRunnable
from org.eclipse.ui import PlatformUI

from org.gumtree.ui.util import SafeUIRunner
from org.gumtree.ui.util.workbench import WorkbenchUtils

from time import sleep

###############################################################################
# Global variables
###############################################################################

__mPerspectiveStack__ = None
__mPerspective__ = None
__perspectiveDesc__ = None

###############################################################################
# Helper class and functions
###############################################################################

def swtFunction(function):
    def internalRun(*args, **kwargs):
        class Runnable(SafeRunnable):
            def run(self):
                function(*args, **kwargs)
        SafeUIRunner.asyncExec(Runnable())
    return internalRun

#def inject(function):
#    def internal():
#        __getActivateTasklet__().getEclipseContext()
#        function()
#    return internal

def createPart(function, parent, label, containerData='1000'):
    mPart = MBasicFactory.INSTANCE.createPart()
    mPart.setLabel(label)
    mPart.setContributionURI('bundleclass://org.gumtree.ui/org.gumtree.ui.tasklet.support.DefaultPart')
    mPart.setContainerData(containerData)
    parent.getChildren().add(mPart)
    while mPart.getWidget() == None:
        sleep(0.1)
    @swtFunction
    def prepareWidget(widget):
        # Remove composite from DefaultPart
        widget.getChildren()[0].dispose()
        # Construct widget from function
        function(widget)
    prepareWidget(mPart.getWidget())
    return mPart

@swtFunction
def refreshUI():
    global __mPerspective__
    __mPerspective__.getWidget().layout(True, True)

def configureObject(object):
    activatedTasklet = __getActivateTasklet__()
    ContextInjectionFactory.inject(object, activatedTasklet.getEclipseContext())

###############################################################################
# Internal helper functions
###############################################################################

@swtFunction
def __prepareUI__():
    global __perspectiveDesc__
    originalPerspective = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective()
    perspectiveRegistry = PlatformUI.getWorkbench().getPerspectiveRegistry()
    __perspectiveDesc__ = perspectiveRegistry.createPerspective('tasklet', originalPerspective)
    activatedTasklet = __getActivateTasklet__()
    if not activatedTasklet == None:
        if activatedTasklet.getTasklet().isNewWindow():
            WorkbenchUtils.openEmptyWorkbenchWindow()
    global __mPerspectiveStack__
    __mPerspectiveStack__ = WorkbenchUtils.getActiveMPerspectiveStack()

def __getActivateTasklet__():
    return __executor__.getEngine().get('activatedTasklet')

def __createSinglePart__(mPerspective, createWidgetFunction):
    label = 'Part'
    activatedTasklet = __getActivateTasklet__()
    if not activatedTasklet == None:
        label = activatedTasklet.getTasklet().getLabel()
    mPart = createPart(createWidgetFunction, mPerspective, label)
    # Register parent to tasklet
    if not activatedTasklet == None:
        activatedTasklet.setParentComposite(mPart.getWidget())

def __createPerspective__(mPerspective):
    create(__mPerspective__)
    activatedTasklet = __getActivateTasklet__()
    if not activatedTasklet == None:
        activatedTasklet.setParentComposite(__mPerspective__.getWidget())
    
###############################################################################
# Main function
###############################################################################

def __run__():
    # Prepare UI
    __prepareUI__()
    global __mPerspectiveStack__
    global __perspectiveDesc__
    while __mPerspectiveStack__ == None:
        sleep(0.1)
    # Create model
    global __mPerspective__
    __mPerspective__ = MAdvancedFactory.INSTANCE.createPerspective()
    activatedTasklet = __getActivateTasklet__()
    if not activatedTasklet == None:
        __mPerspective__.setLabel(activatedTasklet.getLabel());
        __mPerspective__.getProperties().put('id', activatedTasklet.getId())
        activatedTasklet.setMPerspective(__mPerspective__)
        activatedTasklet.setPerspective(__perspectiveDesc__)
    __mPerspective__.setElementId(__perspectiveDesc__.getId())
    __mPerspectiveStack__.getChildren().add(__mPerspective__);
    __mPerspectiveStack__.setSelectedElement(__mPerspective__)
    if not activatedTasklet == None:
        if activatedTasklet.getTasklet().isSimpleLayout():
            __createSinglePart__(__mPerspective__, create)
        else:
            __createPerspective__(__mPerspective__)
    # Refresh UI
    refreshUI()
