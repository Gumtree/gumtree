from gumpy.commons.core import injectObject
from gumpy.commons.swt import swtFunction

from org.eclipse.swt import SWT

from org.gumtree.gumnix.sics.ui.widgets import SicsInterruptWidget
from org.gumtree.ui.util.resource import SharedImage

@swtFunction
def create(parent):
    sicsInterruptWidget = SicsInterruptWidget(parent, SWT.NONE)
    sicsInterruptWidget.setBackgroundImage(SharedImage.CRUISE_BG.getImage())
    injectObject(sicsInterruptWidget)
