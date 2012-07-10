'''
Created on 04/02/2011

@author: nxi
'''
from org.gumtree.data.nexus.ui.viewers import NexusViewer
from org.gumtree.data.nexus.ui import NexusBrowserView
from org.gumtree.data.ui.viewers import DatasetChangeListener
from java.io import File
from dataset import Dataset
from gumpy.commons import jutils
import time

class DataBrowser:
    
    __timeup__ = 3
    
    def __init__(self, new_shell = False):
        if new_shell :
            from org.gumtree.data.nexus.ui.viewers import NexusViewer
            self.__viewer__ = NexusViewer.openInNewShell()
            if self.__viewer__ is None :
                raise Exception, 'failed to create the plot in current environment'
        else : 
            from org.gumtree.data.nexus.ui import NexusBrowserView
            view = NexusBrowserView.getInstance()
            if view is None :
#                raise Exception, 'failed to create the plot in current environment'
                from org.gumtree.data.nexus.ui.viewers import NexusViewer
                self.__viewer__ = NexusViewer.openInNewShell()
                if self.__viewer__ is None :
                    raise Exception, 'failed to create the plot in current environment'
            else :
                self.__viewer__ = view.getNexusViewer()
        if jutils.is_jython :
            self.__viewer__.getDatasetBrowser().addDatasetChangeListener(
                                        DatasetListListener(self))
        self.dlist = []
        #self.__databrowser__ = self.__viewer__.getDatasetBrowser()

    def __getitem__(self, index):
#        return Dataset(self.__databrowser__.getDatasets().get(key))
        return self.dlist[index]
    
    def addDataFile(self, file):
        self.__databrowser__.addDataset(File(file).toURI())
        datasets = self.__databrowser__.getDatasets()
        self.dlist.append(Dataset(datasets.get(datasets.size() - 1)))
        
    def refresh(self, dataset):
        self.__viewer__.refresh(dataset.__iNXDataset__)
        self.select(dataset)
        
    def add(self, dataset):
        self.__databrowser__.addDataset(dataset.__iNXDataset__)
#        if self.__databrowser__.getDatasets().size() == 1 :
        self.dlist.append(dataset)
        if len(self.dlist) == 1 :
            self.select(dataset)
        
    def select(self, object):
        if isinstance(object, Dataset) :
            self.__databrowser__.setSelection(object.__iNXDataset__)
        else :
            self.__databrowser__.setSelection(object)
        
    def open(self):
        self.__viewer__ = NexusBrowserView.getInstance().getNexusViewer()
#        self.__databrowser__ = self.__viewer__.getDatasetBrowser()
        
    def __getBrowser__(self):
        seconds = 0
#        if self.__viewer__.getDatasetBrowser() == None :
#            self.open()
        while (self.__viewer__.getDatasetBrowser() == None) & (seconds <= DataBrowser.__timeup__):
            time.sleep(0.1)
            seconds += 0.1
        if seconds > DataBrowser.__timeup__ :
            self.open()
#            print "time up, browser not available"
        if self.__viewer__.getDatasetBrowser() == None :
            raise "time up, browser not available"
        return self.__viewer__.getDatasetBrowser()
    
    def __getattr__(self, name):
        if name == '__databrowser__' :
            return self.__getBrowser__()

    def remove(self, object):
        self.__databrowser__.removeDataset(object.__iNXDataset__)
        self.dlist.remove(object)
        
    def __len__(self): 
        return len(self.dlist)
    
    def __repr__(self):
        return 'Nexus Browser'
    
    def __str__(self):
        return 'Nexus Browser'
    
#db = DataBrowser()
#
#def plot(dataset, title = None):
#    db.open()
#    dataset.set_title(title)
#    db.add(dataset)
#    db.select(dataset)

if jutils.is_jython :
    class DatasetListListener(DatasetChangeListener):
        
        def __init__(self, browser) :
            self.browser = browser
            
        def datasetAdded(self, dataset) :
            self.browser.dlist.append(Dataset(dataset))
        
        def datasetRemoved(self, dataset) :
            tr = None
            for ds in self.browser.dlist :
                if ds.__iNXDataset__ == dataset :
                    tr = ds
            if not tr is None :
                self.browser.dlist.remove(tr)
        
        