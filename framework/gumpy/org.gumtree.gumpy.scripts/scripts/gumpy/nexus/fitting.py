from org.gumtree.data.nexus.fitting import Fitter, UserDefinedFitter
from gumpy.nexus.simpledata import arange
from gumpy.nexus.dataset import Dataset
from gumpy.nexus.data import Data

GAUSSIAN_FITTING = 'Gaussian'
LINEAR_FITTING = 'Linear'
QUADRATIC_FITTING = 'Quadratic' 
CUBIC_FITTING = 'Cubic'
GAUSSIAN_LORENTZIAN_FITTING = 'GaussianLorentzian'
POWER_FITTING = 'Power'

class Fitting():
    def __init__(self, name, ndim = 1):
        self.fitter = Fitter.getFitter(name, ndim)
        self.name = name
        self.data = None
        self.axis = None
        self.res = None
        self.x_axis = None
        self.y_axis = None
        
    def set_histogram(self, dataset, xmin = None, xmax = None):
        self.data = dataset
        axes = dataset.axes
        if dataset.ndim == 1:
            if len(axes) > 0:
                self.axis = axes[-1]
            else:
                self.axis = arange(dataset.size)
            if (str(xmin) == 'nan' and str(xmax) == 'nan') or (xmin is None and xmax is None):
                self.fitter.createHistogram(dataset.__iNXdata__)
            elif str(xmax) == 'nan' or xmax is None:
                xmax = self.axis.max()
                self.fitter.createHistogram(dataset.__iNXdata__, float(xmin), float(xmax))
            elif str(xmin) == 'nan' or xmin is None:
                xmin = self.axis.min()
                self.fitter.createHistogram(dataset.__iNXdata__, float(xmin), float(xmax))
            else:
                self.fitter.createHistogram(dataset.__iNXdata__, float(xmin), float(xmax))
#        self.fitter.setParameters()
        elif dataset.ndim == 2:
            if len(axes) >= 2:
                self.y_axis = axes[-2]
                self.x_axis = axes[-1]
            elif len(axes) == 1:
                self.x_axis = axes[0]
                self.y_axis = arange(len(dataset))
            else:
                self.y_axis = arange(dataset.shape[0])
                self.x_axis = arange(dataset.shape[1])
            self.fitter.createHistogram(dataset.__iNXdata__)
        
    def fit(self):
        self.fitter.fit()
        self.res = Dataset(Data(self.fitter.getResult()))
        return self.res
    
    def set_bounds(self, name, lower, higher) :
        self.fitter.setParameterBounds(name, lower, higher);
        
    def fix_param(self, name, is_fixed):
        self.fitter.setParameterFixed(name, is_fixed)
        
    def __getattr__(self, name):
        if name == 'parameters' or name == 'params':
            params = self.fitter.getParameters()
            pars = dict()
            for key in params.keySet():
                pars[str(key)] = params.get(key)
            return pars
        elif name == 'errors' or name == 'errs':
            errors = self.fitter.getFitErrors()
            errs = dict()
            for key in errors.keySet():
                errs[str(key)] = errors.get(key)
            return errs
        else :
            pars = self.params
            if pars.__contains__(name):
                return pars[name]
            else:
                return self.__dict__[name]
            
    def set_param(self, name, value):
        self.fitter.setParameterValue(name, float(value))

class UndefinedFitting(Fitting):
    def __init__(self, function, name = None):
        if name is None:
            name = 'UNDEFINED'
        self.fitter = UserDefinedFitter(name, function)
        self.name = name
        self.data = None
        self.axis = None
        self.res = None
                