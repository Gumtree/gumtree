from org.gumtree.data.ui.part import PlotView
from org.gumtree.vis.hist2d.color import ColorScale
from org.gumtree.vis.mask import EllipseMask, RectangleMask
from org.gumtree.vis.nexus.utils import NXFactory

color_scale = ColorScale

class Image:
    
    def __init__(self, ds = None, title = None, x_label = None, y_label = None, widget = None):
        if widget is None :
            self.__view__ = PlotView.getNewInstance()
            self.pv = self.__view__.getViewer()
        else :
            self.__view__ = widget
            self.pv = widget.getViewer()
        self.__ds__ = None
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
#        if __ds__.axes != None :
#            if len(__ds__.axes) > 1 :
#                y_axis = __ds__.axes[len(__ds__.axes) - 2]
#                y_label = y_axis.name
#                if not y_axis.units is None :
#                    y_label += ' (' + y_axis.units + ')'
#            if len(__ds__.axes) > 0 :
#                x_axis = __ds__.axes[len(__ds__.axes) - 1]
#                x_label = x_axis.name
#                if not x_axis.units is None :
#                    x_label += ' (' + x_axis.units + ')'
        nxDs = None
        if not ds is None :
            nxDs = ds.__iNXDataset__
        pds = NXFactory.createHist2DDataset(nxDs)
#        if not x_label is None :
#            pds.setXTitle(x_label)
#        if not y_label is None :
#            pds.setYTitle(y_label)
        self.pv.setDataset(pds)
        if not ds is None :
            self.update()
        self.__ds__ = ds
        return pds

    def set_color_scale(self, color_scale):
        self.pv.getPlot().setColorScale(color_scale)
        self.pv.getPlot().updatePlot()
        
    def set_log_scale_on(self, flag):
        self.pv.getPlot().setLogarithmScaleEnabled(flag)
        self.pv.getPlot().updatePlot()
        
    def set_x_flipped(self, flag):
        self.pv.getPlot().setHorizontalAxisFlipped(flag)
        
    def set_y_flipped(self, flag):
        self.pv.getPlot().setVerticalAxisFlipped(flag)
    
    def set_x_range(self, min, max):
        self.pv.getPlot().getXYPlot().getDomainAxis().setRange(min, max)
    
    def set_y_range(self, min, max):
        self.pv.getPlot().getXYPlot().getRangeAxis().setRange(min, max)
        
    def save_as_png(self, filename):
        self.pv.getPlot().saveTo(filename, 'png')
        
    def save_as_jpg(self, filename):
        self.pv.getPlot().saveTo(filename, 'jpg')
    
    def set_bounds(self, x_min, x_max, y_min, y_max):
        self.set_x_range(x_min, x_max)
        self.set_y_range(y_min, y_max)
                    
    def restore_x_range(self):
        self.pv.getPlot().restoreHorizontalBounds()
        
    def restore_y_range(self):
        self.pv.getPlot().restoreVerticalBounds()

    def restore_bounds(self):
        self.pv.getPlot().restoreAutoBounds()
    
    def add_mask(self, x_min, x_max, y_min, y_max, name = None, is_inclusive = True, shape = 'r'):
        x = min(x_min, x_max)
        y = min(y_min, y_max)
        width = abs(x_max - x_min)
        height = abs(y_max - y_min)
        if shape is None or not shape[0].lower() == 'e' :
            mask = RectangleMask(is_inclusive, x, y, width, height)
        else :
            mask = EllipseMask(is_inclusive, x, y, width, height)
        if not name is None :
            mask.setName(name)
        self.pv.getPlot().addMask(mask)
        self.pv.getPlot().repaint()
        return mask
        
    def is_disposed(self):
        return self.pv.isDisposed()
        
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
        self.pv.getPlot().getDataset().update()
        self.restore_bounds()

    def get_dataset(self):
        pds = self.pv.getPlot().getDataset()
        if not pds is None :
            return pds.getNXDataset()
        
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
        if name == 'ds' :
            self.set_dataset(value)
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
            return self.__ds__
        raise AttributeError, name + ' does not exist'

    def __str__(self): 
        res = 'Image 2D'
        if self.title != None :
            res += ': ' + self.title
        return res
    
    def __repr__(self):
        return self.__str__();
    
    def add_mouse_listener(self, listener):
        self.pv.getPlot().addChartMouseListener(listener.__get_jlistener__())

    def remove_mouse_listener(self, listener):
        self.pv.getPlot().removeChartMouseListener(listener.__get_jlistener__())

    def close(self):
        PlotView.closePlotView(self.__view__)

def image(ds = None, title = None, x_label = None, y_label = None):
    return Image(ds, title, x_label, y_label)
