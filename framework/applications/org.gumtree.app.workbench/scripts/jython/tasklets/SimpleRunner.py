from org.eclipse.e4.ui.model.application.ui.basic import MBasicFactory
from org.eclipse.jface.util import SafeRunnable

from org.gumtree.ui.tasklet.support import TaskletUtilities
from org.gumtree.ui.util import SafeUIRunner

from time import sleep

mPerspectiveStack = None
mPart = None
parentComposite = None

class FunctionRunnable(SafeRunnable):
    
    def __init__(self, function, context=None):
        self.function = function
        self.context = context
        
    def run(self):
        if (self.context == None):
            self.function()
        else:
            self.function(self.context)

def getPartWidget():
    global mPart
    global parentComposite
    parentComposite = mPart.getWidget()

def refresh():
    global parentComposite
    parentComposite.layout(True, True)

def createParentComposite():
    # Create model
    global mPart
    mPart = MBasicFactory.INSTANCE.createPart()
    mPart.setLabel('Test')
    mPart.setContributionURI('bundleclass://org.gumtree.ui/org.gumtree.ui.tasklet.support.DefaultPart')
    mPerspective.getChildren().add(mPart)
    # Get part widget
    runnable = FunctionRunnable(getPartWidget)
    SafeUIRunner.asyncExec(runnable)
    while parentComposite == None:
        sleep(0.1)

###############################################################################
# Internal test
###############################################################################

def getPerspectiveStack():
    global mPerspectiveStack
    mPerspectiveStack = TaskletUtilities.getActiveMPerspectiveStack()

def getPartWidget():
    global mPart
    global parentComposite
    parentComposite = mPart.getWidget()

def run():
    # Get perspective stack
    runnable = FunctionRunnable(getPerspectiveStack)
    SafeUIRunner.asyncExec(runnable)
    while mPerspectiveStack == None:
        sleep(0.1)
    # Create model
    global mPerspective
    mPerspective = TaskletUtilities.createMPerspective(mPerspectiveStack, 'Test')
    mPerspectiveStack.setSelectedElement(mPerspective)
    createParentComposite()
    # Use script
    runnable = FunctionRunnable(create, parentComposite)
    SafeUIRunner.asyncExec(runnable)
