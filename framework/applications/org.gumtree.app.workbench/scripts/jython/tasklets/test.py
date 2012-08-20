from org.eclipse.swt import SWT
from org.eclipse.swt.widgets import Label

def create(parent):
    label = Label(parent, SWT.NONE)
    label.setText('From Jython!')
