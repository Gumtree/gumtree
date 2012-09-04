from gumpy.commons.core import injectObject
from gumpy.commons.swt import swtFunction

from org.eclipse.swt import SWT
from org.gumtree.ui.scripting.support import ScriptConsole

@swtFunction
def create(parent):
    scriptingConsole = ScriptConsole(parent, SWT.NONE)
    injectObject(scriptingConsole)
    scriptingConsole.setContentAssistEnabled(True)

