from gumpy.nexus.dataset import DatasetFactory
import os

class Script:
    def __init__(self, model):
        self.model = model
 
    def __setattr__(self, name, value):
        if name == 'title' :
            self.model.setTitle(value)
        elif name == 'version' :
            self.model.setVersion(value)
        elif name == 'numColumns':
            self.model.setNumColumns(value)
        else:
            self.__dict__[name] = value
    
    def __getattr__(self, name):
        if name == 'title' :
            return self.model.getTitle()
        elif name == 'version' :
            return self.model.getVersion()
        elif name == 'numColumns':
            return self.model.getNumColumns()
            
    def __str__(self):
        return self.title
    
    def __repr__(self):
        return 'Script' 

df = DatasetFactory()
def load_file(filenames):
    files = []
    for item in filenames :
        ds = df[item]
        files.append(ds)
    return files

def clear():
    df.datasets.clear()
    
