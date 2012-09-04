from gumpy.commons.core import injectObject
from gumpy.commons.swt import swtFunction

from org.eclipse.jface.layout import GridDataFactory
from org.eclipse.jface.layout import GridLayoutFactory
from org.eclipse.swt import SWT

from org.gumtree.gumnix.sics.ui.widgets import DeviceStatusWidget

@swtFunction
def create(parent):
    GridLayoutFactory.swtDefaults().applyTo(parent)
    deviceStatusWidget = DeviceStatusWidget(parent, SWT.NONE)
    deviceStatusWidget\
        .addDevice('/experiment/title', 'Proposal')\
        .addSeparator()\
        .addDevice('/user/name', 'User')\
        .addDevice('/user/email', 'Email')\
        .addDevice('/user/phone', 'Phone')\
        .addSeparator()\
        .addDevice('/sample/name', 'Sample')
    injectObject(deviceStatusWidget)
    GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)\
        .grab(True, True).hint(300, SWT.DEFAULT).applyTo(deviceStatusWidget)
