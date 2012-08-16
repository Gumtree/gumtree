from java.lang import System

from org.eclipse.ui import PlatformUI

def createPerspective(mPerspective):
    System.out.println(mPerspective)
    workbench = PlatformUI.getWorkbench()
    workbench.openWorkbenchWindow(None)
