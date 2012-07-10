from java.awt import Color
from org.gumtree.data.ui.part import PlotView
from org.gumtree.vis.mask import RangeMask
from org.gumtree.vis.nexus.utils import NXFactory
from org.gumtree.vis.plot1d import MarkerShape
import copy
from org.jfree.chart import ChartMouseListener

marker_shape = MarkerShape
color = Color

class Plot: 
    
    def __init__(self, ds = None, title = None, x_label = None, y_label = None, widget = None):
        if widget is None :
            self.__view__ = PlotView.getNewInstance()
            self.pv = self.__view__.getViewer()
        else :
            self.__view__ = widget
            self.pv = widget.getViewer()
        self.__datasets__ = []
        self.set_dataset(ds)
        if not title is None:
            self.set_title(title);
        if not x_label is None:
            self.set_x_label(x_label);
        if not y_label is None:
            self.set_y_label(y_label);

                
    def set_title(self, title):
        self.pv.getPlot().setPlotTitle(title)
        
    def set_x_label(self, x_label):
        xyplot = self.pv.getPlot().getXYPlot()
        if not xyplot is None :
            xyplot.getDomainAxis().setLabel(x_label)
    
    def set_y_label(self, y_label):
        xyplot = self.pv.getPlot().getXYPlot()
        if not xyplot is None :
            xyplot.getRangeAxis().setLabel(y_label)
            
    def set_dataset(self, ds):
#        x_axis_array = None
#        error_array = None
        x_label = None
        y_label = None
        title = None
        nxDs = None
        if not ds is None :
            nxDs = ds.__iNXDataset__
            if ds.axes != None :
                if len(ds.axes) > 0 :
                    x_axis = ds.axes[len(ds.axes) - 1]
                    x_label = x_axis.name
                    if not x_axis.units is None :
                        x_label += ' (' + x_axis.units + ')'
            y_label = ds.title
            if not ds.units is None and len(ds.units) > 0 :
                y_label += ' (' + ds.units + ')'
            title = ds.title
#        if not ds.var is None :
#            error_array = ds.var.sqrt().storage.__iArray__
        pds = NXFactory.createSingleXYDataset(title, nxDs)
        if not x_label is None :
            pds.setXTitle(x_label)
        if not y_label is None :
            pds.setYTitle(y_label)
        self.pv.setDataset(pds)
        self.__datasets__ = [ds]
        return pds
    
    def add_dataset(self, ds):
#        x_axis_array = None
#        error_array = None
#        if ds.axes != None :
#            if len(ds.axes) > 0 :
#                x_axis = ds.axes[len(ds.axes) - 1]
#                x_axis_array = x_axis.storage.__iArray__
        title = ds.title
#        if not ds.var is None :
#            error_array = ds.var.sqrt().storage.__iArray__
        series = NXFactory.createNexusSeries(title, ds.__iNXDataset__)
        self.pv.getDataset().addSeries(series)
        self.__datasets__.append(ds)
        return series

    def remove_dataset(self, ds):
        sds = self.pv.getPlot().getDataset()
        for s in sds.getSeries() :
            if s.getNxDataset() is ds.__iNXDataset__ :
                sds.removeSeries(s)
                self.__datasets__.remove(ds)
                break
        
    def set_log_x_on(self, flag):
        self.pv.getPlot().setLogarithmXEnabled(flag)
#        self.pv.getPlot().updatePlot()

    def set_log_y_on(self, flag):
        self.pv.getPlot().setLogarithmYEnabled(flag)

    def set_error_bar_on(self, flag):
        self.pv.getPlot().setErrorBarEnabled(flag)
        
    def set_marker_on(self, flag):
        self.pv.getPlot().setMarkerEnabled(flag)
        
#    def set_marker_filled(self, flag):
#        self.pv.getPlot().setCurveMarkerFilled(flag)
    def set_x_flipped(self, flag):
        self.pv.getPlot().setHorizontalAxisFlipped(flag)
        
    def set_y_flipped(self, flag):
        self.pv.getPlot().setVerticalAxisFlipped(flag)
    
    def set_x_range(self, min, max):
        self.pv.getPlot().getXYPlot().getDomainAxis().setRange(min, max)
    
    def set_y_range(self, min, max):
        self.pv.getPlot().getXYPlot().getRangeAxis().setRange(min, max)
        
    def set_bounds(self, x_min, x_max, y_min, y_max):
        self.set_x_range(x_min, x_max)
        self.set_y_range(y_min, y_max)
                
    def save_as_png(self, filename):
        self.pv.getPlot().saveTo(filename, 'png')
        
    def save_as_jpg(self, filename):
        self.pv.getPlot().saveTo(filename, 'jpg')
    
    def restore_x_range(self):
        self.pv.getPlot().restoreHorizontalBounds()
        
    def restore_y_range(self):
        self.pv.getPlot().restoreVerticalBounds()

    def restore_bounds(self):
        self.restoreAutoBounds()
        
    def add_mask(self, x_min, x_max, name = None, is_inclusive = True):
        mask = RangeMask(is_inclusive)
        if not name is None :
            mask.setName(name)
        mask.setBoundary(min(x_min, x_max), max(x_min, x_max))
        self.pv.getPlot().addMask(mask)
        self.pv.getPlot().repaint()
        return mask
    
    def select_mask(self, obj):
        if type(obj) is str :
            masks = self.pv.getPlot().getMasks()
            for mask in masks :
                if mask.getName() == obj :
                    self.pv.getPlot().setSelectedMask(mask)
                    break
        else :
            self.pv.getPlot().setSelectedMask(obj)
        self.pv.getPlot().repaint()
        
    def remove_mask(self, obj):
        if type(obj) is str :
            masks = self.pv.getPlot().getMasks()
            for mask in masks :
                if mask.getName() == obj :
                    self.pv.getPlot().removeMask(mask)
                    break
        else :
            self.pv.getPlot().removeMask(obj)
        self.pv.getPlot().repaint()
        
    def update(self):
        self.pv.getPlot().updatePlot()

    def set_legend_on(self, flag):
        self.pv.getPlot().getChart().getLegend().setVisible(flag)
        
    def set_color(self, ds, color): 
        series = self.pv.getPlot().getDataset().getSeries()
        for s in series :
            if s.getNxDataset() is ds.__iNXDataset__ :
                self.pv.getPlot().setCurveColor(s, color)
                break
    
    def set_marker_shape(self, ds, shape):
        series = self.pv.getPlot().getDataset().getSeries()
        for s in series :
            if s.getNxDataset() is ds.__iNXDataset__ :
                self.pv.getPlot().setCurveMarkerShape(s, shape)
                break
    
    def set_marker_filled(self, ds, flag):
        series = self.pv.getPlot().getDataset().getSeries()
        for s in series :
            if s.getNxDataset() is ds.__iNXDataset__ :
                self.pv.getPlot().setCurveMarkerFilled(s, flag)
                break
    
    def set_legend_title(self, ds, title):
        series = self.pv.getPlot().getDataset().getSeries()
        for s in series :
            if s.getNxDataset() is ds.__iNXDataset__ :
                s.setKey(title)
                break
    
    def select_dataset(self, ds):
        series = self.pv.getPlot().getDataset().getSeries()
        index = 0
        for s in series :
            if s.getNxDataset() is ds.__iNXDataset__ :
                self.pv.getPlot().setSelectedSeries(index)
                break
            index += 1
    
    def get_datasets(self):
#        pds = self.pv.getPlot().getDataset()
#        dss = []
#        if not pds is None :
#            series = pds.getSeries()
#            for s in series :
#                dss.append(s.getNxDataset())
#        return dss
        return copy.copy(self.__datasets__)
        
    def __setattr__(self, name, value):
        if name == 'title' :
            self.set_title(value)
        if name == 'x_label' :
            self.set_x_label(value)
        if name == 'y_label' :
            self.set_y_label(value)
        if name == 'x_range' :
            self.set_x_range(value[0], value[1])
        if name == 'y_range' :
            self.set_y_range(value[0], value[1])
        if name == 'masks' :
            for mask in value :
                self.pv.getPlot().addMask(mask)
        if name == 'datasets' :
            raise AttributeError, 'can not set datasets to the plot, use set_dataset() or add_dataset() instead.'
        if name == 'ds' :
            raise AttributeError, 'can not set dataset to the plot, use set_dataset() or add_dataset() instead.'
        else :
            self.__dict__[name] = value
            
    def __getattr__(self, name):
        if name == 'title' :
            return self.pv.getPlot().getTitle().getText()
        if name == 'x_label' :
            return self.pv.getPlot().getHorizontalAxis().getLabel()
        if name == 'y_label' :
            return self.pv.getPlot().getVerticalAxis().getLabel()
        if name == 'x_range' :
            axis = self.pv.getPlot().getHorizontalAxis()
            return [axis.getLowerBound(), axis.getUpperBound()]
        if name == 'y_range' :
            axis = self.pv.getPlot().getVerticalAxis()
            return [axis.getLowerBound(), axis.getUpperBound()]
        if name == 'masks' :
            return self.pv.getPlot().getMasks()
        if name == 'ds' :
            if len(self.__datasets__) == 0:
                return None
            else:
                return self.__datasets__[0]
        if name == 'datasets' :
            return copy.copy(self.__datasets__)
        return self.__dict__[name]
#        raise AttributeError, name + ' does not exist'

    def __str__(self): 
        res = 'Plot 1D: '
        if self.title != None :
            res += self.title
        return res
    
    def __repr__(self):
        return self.__str__();
    
    def add_mouse_listener(self, listener):
        self.pv.getPlot().addChartMouseListener(listener.__get_jlistener__())

    def remove_mouse_listener(self, listener):
        self.pv.getPlot().removeChartMouseListener(listener.__get_jlistener__())
    
    def is_disposed(self):
        return self.pv.isDisposed()
    
    def close(self):
        PlotView.closePlotView(self.__view__)
        
def plot(ds = None, title = None, x_label = None, y_label = None):
    return Plot(ds, title, x_label, y_label)

