from gumpy.commons.core import injectObject
from gumpy.commons.swt import swtFunction

from org.eclipse.swt import SWT

from org.gumtree.jython.ui import JythonScriptDemoWidget

@swtFunction
def create(parent):
    demoWidget = JythonScriptDemoWidget(parent, SWT.NONE)
    path = 'bundle://au.gov.ansto.bragg.nbi.workbench/scripts/demo/widgets'
    # Basic
    demoWidget.addScript('Basic', 'Label', path + '/basic/Label.py')
    # JavaFX
    demoWidget.addScript('JavaFX', 'Animation', path + '/javafx/Animation.py')
    demoWidget.addScript('JavaFX', 'Pie Chart', path + '/javafx/PieChart.py')
    # SICS
    demoWidget.addScript('SICS', 'Interrupt', path + '/sics/Interrupt.py')
    # Services
    demoWidget.addScript('Services', 'Script Console', path + '/services/ScriptConsole.py')
    # Visualisation
    demoWidget.addScript('Visualisation', '1D Plot', path + '/visualisation/1DPlot.py')
    
    injectObject(demoWidget)
