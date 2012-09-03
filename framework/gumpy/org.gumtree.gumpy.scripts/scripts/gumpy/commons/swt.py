from org.eclipse.jface.util import SafeRunnable

from org.gumtree.ui.util import SafeUIRunner

def swtFunction(function):
    def internalRun(*args, **kwargs):
        class Runnable(SafeRunnable):
            def run(self):
                function(*args, **kwargs)
        SafeUIRunner.asyncExec(Runnable())
    return internalRun

@swtFunction
def refreshWidget(widget):
    widget.layout(True, True)
