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
__mPart__ = None
__perspectiveDesc__ = None
__parentComposite__ = None

###############################################################################
# Helper class
###############################################################################

class FunctionRunnable(SafeRunnable):
    
    def __init__(self, function, context=None):
        self.function = function
        self.context = context
        
    def run(self):
        if (self.context == None):
            self.function()
        else:
            self.function(self.context)

def runUIFunction(function, context=None):
    runnable = FunctionRunnable(function, context)
    SafeUIRunner.asyncExec(runnable)

###############################################################################
# UI thread helper functions
###############################################################################

def __prepareUI__():
    global __mPerspectiveStack__
    __mPerspectiveStack__ = WorkbenchUtils.getActiveMPerspectiveStack()
    originalPerspective = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective()
    perspectiveRegistry = PlatformUI.getWorkbench().getPerspectiveRegistry()
    global __perspectiveDesc__
    __perspectiveDesc__ = perspectiveRegistry.createPerspective('Test', originalPerspective)
    
def __getPartWidget__():
    global __mPart__
    global __parentComposite__
    __parentComposite__ = __mPart__.getWidget()

def __refresh__():
    global __parentComposite__
    __parentComposite__.layout(True, True)

###############################################################################
# Non UI thread helper functions
###############################################################################

def __createParentComposite__():
    # Create model
    global __mPart__
    __mPart__ = MBasicFactory.INSTANCE.createPart()
    activatedTasklet = __executor__.getEngine().get('activatedTasklet')
    tasklet = activatedTasklet.getTasklet()
    if not activatedTasklet == None:
        __mPart__.setLabel(activatedTasklet.getTasklet().getLabel())
    __mPart__.setContributionURI('bundleclass://org.gumtree.ui/org.gumtree.ui.tasklet.support.DefaultPart')
    global __mPerspective__
    __mPerspective__.getChildren().add(__mPart__)
    # Get part widget
    runUIFunction(__getPartWidget__)
    while __parentComposite__ == None:
        sleep(0.1)
    # Register parent to tasklet
    if not activatedTasklet == None:
        activatedTasklet.setParentComposite(__parentComposite__)

###############################################################################
# Main helper function
###############################################################################

def __run__():
    # Prepare UI
    runUIFunction(__prepareUI__)
    while __mPerspectiveStack__ == None:
        sleep(0.1)
    # Create model
    global __mPerspective__
    global __perspectiveDesc__
    __mPerspective__ = MAdvancedFactory.INSTANCE.createPerspective()
    activatedTasklet = __executor__.getEngine().get('activatedTasklet')
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
            __createParentComposite__()
            # Use script
            runUIFunction(create, __parentComposite__)
            # Refresh
            runUIFunction(__refresh__)
        else:
            runUIFunction(create, __mPerspective__)
