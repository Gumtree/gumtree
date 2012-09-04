from gumpy.commons.core import injectObject
from gumpy.commons.swt import swtFunction

from org.eclipse.jface.layout import GridDataFactory
from org.eclipse.jface.layout import GridLayoutFactory
from org.eclipse.swt import SWT

from org.gumtree.gumnix.sics.ui.widgets import ShutterStatusWidget

@swtFunction
def create(parent):
    GridLayoutFactory.swtDefaults().applyTo(parent)
    shutterStatusWidget = ShutterStatusWidget(parent, SWT.NONE)
    injectObject(shutterStatusWidget)
    GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)\
        .grab(True, True).hint(200, 200).applyTo(shutterStatusWidget)