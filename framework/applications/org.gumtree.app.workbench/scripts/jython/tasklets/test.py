from org.eclipse.swt import SWT
from org.eclipse.swt.widgets import Label

def create(parent):
    tasklet = __executor__.getEngine().get('activatedTasklet').getTasklet()
    label = Label(parent, SWT.NONE)
    label.setText(tasklet.getLabel())
