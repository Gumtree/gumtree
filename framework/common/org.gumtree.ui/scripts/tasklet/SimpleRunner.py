from org.eclipse.e4.ui.model.application.ui.basic import MBasicFactory
from org.eclipse.e4.ui.model.application.ui.advanced import MAdvancedFactory
from org.eclipse.jface.util import SafeRunnable
from org.eclipse.ui import PlatformUI

from org.gumtree.ui.tasklet.support import TaskletUtilities
from org.gumtree.ui.util import SafeUIRunner

from time import sleep

###############################################################################
# Global variables
###############################################################################

mPerspectiveStack = None
mPart = None
perspectiveDesc = None
parentComposite = None

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

###############################################################################
# UI thread helper functions
###############################################################################

def prepareUI():
    global mPerspectiveStack
    mPerspectiveStack = TaskletUtilities.getActiveMPerspectiveStack()
    originalPerspective = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective()
    perspectiveRegistry = PlatformUI.getWorkbench().getPerspectiveRegistry()
    global perspectiveDesc
    perspectiveDesc = perspectiveRegistry.createPerspective('Test', originalPerspective)
    
def getPartWidget():
    global mPart
    global parentComposite
    parentComposite = mPart.getWidget()

def refresh():
    global parentComposite
    parentComposite.layout(True, True)

###############################################################################
# Non UI thread helper functions
###############################################################################

def createParentComposite():
    # Create model
    global mPart
    mPart = MBasicFactory.INSTANCE.createPart()
    tasklet = __executor__.getEngine().get('activatedTasklet').getTasklet()
    if not tasklet == None:
        mPart.setLabel(tasklet.getLabel())
    mPart.setContributionURI('bundleclass://org.gumtree.ui/org.gumtree.ui.tasklet.support.DefaultPart')
    mPerspective.getChildren().add(mPart)
    # Get part widget
    runnable = FunctionRunnable(getPartWidget)
    SafeUIRunner.asyncExec(runnable)
    while parentComposite == None:
        sleep(0.1)

###############################################################################
# Main helper function
###############################################################################

def run():
    # Prepare UI
    runnable = FunctionRunnable(prepareUI)
    SafeUIRunner.asyncExec(runnable)
    while mPerspectiveStack == None:
        sleep(0.1)
    # Create model
    global mPerspective
    global perspectiveDesc
    mPerspective = MAdvancedFactory.INSTANCE.createPerspective()
    activatedTasklet = __executor__.getEngine().get('activatedTasklet')
    if not activatedTasklet == None:
        mPerspective.setLabel(activatedTasklet.getLabel());
        mPerspective.getProperties().put('id', activatedTasklet.getId())
    mPerspective.setElementId(perspectiveDesc.getId())
    mPerspectiveStack.getChildren().add(mPerspective);
    mPerspectiveStack.setSelectedElement(mPerspective)
    createParentComposite()
    # Use script
    runnable = FunctionRunnable(create, parentComposite)
    SafeUIRunner.asyncExec(runnable)
    # Refresh
    runnable = FunctionRunnable(refresh)
    SafeUIRunner.asyncExec(runnable)
