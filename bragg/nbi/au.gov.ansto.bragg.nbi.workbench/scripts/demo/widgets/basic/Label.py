from gumpy.commons.swt import swtFunction

from org.eclipse.swt import SWT
from org.eclipse.swt.widgets import Label

@swtFunction
def create(parent):
    label = Label(parent, SWT.NONE, text ='Hello, World!')
