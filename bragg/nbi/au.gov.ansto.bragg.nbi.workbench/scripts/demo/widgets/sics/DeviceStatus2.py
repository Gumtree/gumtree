# Device status widget with customised appearance

from gumpy.commons.core import injectObject
from gumpy.commons.swt import swtFunction

from org.eclipse.jface.layout import GridDataFactory
from org.eclipse.jface.layout import GridLayoutFactory
from org.eclipse.swt import SWT

from org.gumtree.gumnix.sics.ui.widgets import DeviceStatusWidget
from org.gumtree.ui.util.resource import SharedImage
from org.gumtree.ui.util.resource import UIResourceManager
from org.gumtree.ui.util.resource import UIResources

@swtFunction
def create(parent):
    GridLayoutFactory.swtDefaults().applyTo(parent)
    # Create icons
    resourceManager = UIResourceManager('au.gov.ansto.bragg.nbi.workbench', parent)
    image1 = resourceManager.createImage('icons/thread_view.gif')
    image2 = resourceManager.createImage('icons/hh1_16x16.png')
    image3 = resourceManager.createImage('icons/hh2_16x16.png')
    # Set background
    parent.setBackgroundImage(SharedImage.CRUISE_BG.getImage())
    # Create and configure widget
    deviceStatusWidget = DeviceStatusWidget(parent, SWT.NONE)
    deviceStatusWidget.setForeground(UIResources.getSystemColor(SWT.COLOR_WHITE))
    deviceStatusWidget\
        .addSeparator()\
        .addDevice('/sample/dummy_motor', 'Dummy Motor', image1)\
        .addSeparator()\
        .addDevice('/monitor/bm1_event_rate', 'Beam Monitor 1', image2)\
        .addDevice('/monitor/bm2_event_rate', 'Beam Monitor 2', image3)\
        .addSeparator()
    injectObject(deviceStatusWidget)
    # Set Layout
    GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)\
        .grab(True, True).hint(300, SWT.DEFAULT).applyTo(deviceStatusWidget)
