from gumpy.commons.core import injectObject
from gumpy.commons.swt import swtFunction

from org.eclipse.swt import SWT

from org.gumtree.widgets.swt.forms import JVMStatisticsWidget

@swtFunction
def create(parent):
    widget = JVMStatisticsWidget(parent, SWT.NONE)
    injectObject(widget)
