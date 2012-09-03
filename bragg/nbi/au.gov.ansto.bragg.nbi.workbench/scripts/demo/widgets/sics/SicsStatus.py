from gumpy.commons.core import injectObject
from gumpy.commons.swt import swtFunction

from org.eclipse.swt import SWT

from org.gumtree.gumnix.sics.ui.widgets import SicsStatusWidget
from org.gumtree.ui.util.resource import SharedImage

@swtFunction
def create(parent):
    sicsStatusWidget = SicsStatusWidget(parent, SWT.NONE)
    injectObject(sicsStatusWidget)

@swtFunction
def dispose():
    pass
