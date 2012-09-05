from gumpy.commons.core import injectObject
from gumpy.commons.swt import swtFunction

from org.eclipse.jface.layout import GridDataFactory
from org.eclipse.jface.layout import GridLayoutFactory
from org.eclipse.swt import SWT

from org.gumtree.gumnix.sics.widgets.swt import SicsStatusWidget

@swtFunction
def create(parent):
    GridLayoutFactory.swtDefaults().applyTo(parent)
    sicsStatusWidget = SicsStatusWidget(parent, SWT.NONE)
    injectObject(sicsStatusWidget)
    GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)\
        .grab(True, True).hint(200, 50).applyTo(sicsStatusWidget)
