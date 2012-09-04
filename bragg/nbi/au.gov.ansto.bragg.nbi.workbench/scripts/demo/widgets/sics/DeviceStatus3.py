# Device status widget with delayed update (to avoid information overflow)

from gumpy.commons.core import injectObject
from gumpy.commons.swt import swtFunction

from org.eclipse.jface.layout import GridDataFactory
from org.eclipse.jface.layout import GridLayoutFactory
from org.eclipse.swt import SWT

from org.gumtree.gumnix.sics.ui.widgets import DeviceStatusWidget
from org.gumtree.util.messaging import ReducedDelayEventExecutor 

@swtFunction
def create(parent):
    GridLayoutFactory.swtDefaults().applyTo(parent)
    # Set device to update every 5 sec
    global delayEventExecutor
    delayEventExecutor = ReducedDelayEventExecutor(5 * 1000).activate()
    # Create and configure widget
    deviceStatusWidget = DeviceStatusWidget(parent, SWT.NONE, widgetDisposed=dispose)
    deviceStatusWidget.setDelayEventExecutor(delayEventExecutor)
    deviceStatusWidget\
        .addSeparator()\
        .addDevice('/sample/dummy_motor', 'Dummy Motor')\
        .addSeparator()\
        .addDevice('/monitor/bm1_event_rate', 'Beam Monitor 1')\
        .addDevice('/monitor/bm2_event_rate', 'Beam Monitor 2')\
        .addSeparator()
    injectObject(deviceStatusWidget)
    # Set Layout
    GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)\
        .grab(True, True).hint(300, SWT.DEFAULT).applyTo(deviceStatusWidget)

def dispose(event):
    global delayEventExecutor
    delayEventExecutor.deactivate()
