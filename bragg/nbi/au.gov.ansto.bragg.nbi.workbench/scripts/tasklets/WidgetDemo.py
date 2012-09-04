from gumpy.commons.core import injectObject
from gumpy.commons.swt import swtFunction

from java.net import URI

from org.eclipse.swt import SWT

from org.gumtree.jython.ui import JythonScriptDemoWidget

@swtFunction
def create(parent):
    demoWidget = JythonScriptDemoWidget(parent, SWT.NONE)
    demoWidget.setScriptPath(URI.create('bundle://au.gov.ansto.bragg.nbi.workbench/scripts/demo/widgets'))
    injectObject(demoWidget)
