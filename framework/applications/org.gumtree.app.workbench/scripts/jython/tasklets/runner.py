from org.eclipse.jface.util import SafeRunnable
from org.gumtree.ui.util import SafeUIRunner

class Runnable(SafeRunnable):
    def run(self):
        self.function(self.mPerspective)
        
    def setFunction(self, function):
        self.function = function
    
    def setMPerspective(self, mPerspective):
        self.mPerspective = mPerspective
