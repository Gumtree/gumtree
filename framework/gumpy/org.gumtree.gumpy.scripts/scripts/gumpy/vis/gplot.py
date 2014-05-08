from java.awt import Color
from org.gumtree.data.ui.part import PlotView
from org.gumtree.vis.hist2d.color import ColorScale
from org.gumtree.vis.mask import EllipseMask, RectangleMask, RangeMask
from org.gumtree.vis.nexus.utils import NXFactory
from org.gumtree.vis.plot1d import LegendPosition
from org.jfree.chart import ChartMouseListener
from gumpy.vis.event import __AWTMouseListener__

color_scale = ColorScale

class GPlot:
    
    def __init__(self, ds = None, title = None, x_label = None, y_label = None, widget = None):
        if widget is None :
            self.__view__ = PlotView.getNewInstance()
            self.pv = self.__view__.getViewer()
        else :
            self.__view__ = widget
            self.pv = widget.getViewer()
        self.__ds__ = None
        self.ndim = 0
        if not ds is None :
            self.set_dataset(ds)
        if not title is None:
            self.set_title(title);
        if not x_label is None:
            self.set_x_label(x_label);
        if not y_label is None:
            self.set_y_label(y_label);
                
    def set_title(self, title):
        self.pv.getPlot().setPlotTitle(title)
        
    def set_tab_title(self, tab_title):
        self.__view__.setViewTitle(tab_title)
        
    def set_x_label(self, x_label):
        xyplot = self.pv.getPlot().getXYPlot()
        if not xyplot is None :
            xyplot.getDomainAxis().setLabel(x_label)
    
    def set_y_label(self, y_label):
        xyplot = self.pv.getPlot().getXYPlot()
        if not xyplot is None :
            xyplot.getRangeAxis().setLabel(y_label)
            
    def clear(self): 
        self.pv.getPlotComposite().clear()
        self.__ds__ = None
        
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
        pds = None
        x_label = None
        y_label = None
        if not ds is None :
            nxDs = ds.__iNXDataset__
        if ds.ndim == 2 :
            pds = NXFactory.createHist2DDataset(nxDs)
            self.__ds__ = ds
            self.ndim = 2
        elif ds.ndim == 1 :
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
            pds = NXFactory.createSingleXYDataset(title, nxDs)
            if not x_label is None :
                pds.setXTitle(x_label)
            if not y_label is None :
                pds.setYTitle(y_label)
            self.__ds__ = [ds]
            self.ndim = 1
        if not pds is None :
            self.pv.setDataset(pds)
            if not ds is None :
                self.update()
        return pds

    def add_dataset(self, ds):
        if self.__ds__ is None or self.__ds__ == []:
            self.set_dataset(ds)
            return
        if self.ndim == 1 :
            title = ds.title
            series = NXFactory.createNexusSeries(title, ds.__iNXDataset__)
            self.pv.getDataset().addSeries(series)
            self.__ds__.append(ds)
            return series
        else :
            raise AttributeError, 'not supported for this type of plot: ' + str(self.ndim) + '-dimension'

    def remove_dataset(self, ds):
        if self.ndim == 1 :
            sds = self.pv.getPlot().getDataset()
            for s in sds.getSeries() :
                if s.getNxDataset() is ds.__iNXDataset__ :
                    sds.removeSeries(s)
                    for i in xrange(len(self.__ds__)):
                        if self.__ds__[i] is ds:
                            self.__ds__.__delitem__(i)
                            break
                    break
        else :
            raise AttributeError, 'not supported for this type of plot: ' + str(self.ndim) + '-dimension'

    def __get_NXseries__(self, ds):
        if self.ndim == 1 :
            sds = self.pv.getPlot().getDataset()
            for s in sds.getSeries() :
                if s.getNxDataset() is ds.__iNXDataset__ :
                    return s
            return None
        else :
            raise AttributeError, 'not supported for this type of plot: ' + str(self.ndim) + '-dimension'

    def set_log_x_on(self, flag):
        if self.ndim == 1 :
            self.pv.getPlot().setLogarithmXEnabled(flag)
        else :
            raise AttributeError, 'not supported for this type of plot: ' + str(self.ndim) + '-dimension'

    def set_log_y_on(self, flag):
        if self.ndim == 1 :
            self.pv.getPlot().setLogarithmYEnabled(flag)
        else :
            raise AttributeError, 'not supported for this type of plot: ' + str(self.ndim) + '-dimension'

    def set_error_bar_on(self, flag):
        if self.ndim == 1 :
            self.pv.getPlot().setErrorBarEnabled(flag)
        else :
            raise AttributeError, 'not supported for this type of plot: ' + str(self.ndim) + '-dimension'
        
    def set_marker_on(self, flag):
        if self.ndim == 1 :
            self.pv.getPlot().setMarkerEnabled(flag)
        else :
            raise AttributeError, 'not supported for this type of plot: ' + str(self.ndim) + '-dimension'
        
    def set_color_scale(self, color_scale):
        if self.ndim == 2 :
            self.pv.getPlot().setColorScale(color_scale)
            self.pv.getPlot().updatePlot()
        else :
            raise ValueError, 'not supported for this type of plot: ' + str(self.ndim) + '-dimension'
        
    def set_log_scale_on(self, flag):
        if self.ndim == 2 :
            self.pv.getPlot().setLogarithmScaleEnabled(flag)
            self.pv.getPlot().updatePlot()
        else :
            raise ValueError, 'not supported for this type of plot: ' + str(self.ndim) + '-dimension'
        
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

    def set_legend_on(self, flag):
        if self.ndim == 1 :
            self.pv.getPlot().getChart().getLegend().setVisible(flag)
        else :
            raise ValueError, 'not supported for this type of plot: ' + str(self.ndim) + '-dimension'
        
    def set_color(self, ds, color): 
        if self.ndim == 1 :
            series = self.pv.getPlot().getDataset().getSeries()
            for s in series :
                if s.getNxDataset() is ds.__iNXDataset__ :
                    self.pv.getPlot().setCurveColor(s, color)
                    break
        else :
            raise ValueError, 'not supported for this type of plot: ' + str(self.ndim) + '-dimension'
    
    def set_marker_shape(self, ds, shape):
        if self.ndim == 1 :
            series = self.pv.getPlot().getDataset().getSeries()
            for s in series :
                if s.getNxDataset() is ds.__iNXDataset__ :
                    self.pv.getPlot().setCurveMarkerShape(s, shape)
                    break
        else :
            raise ValueError, 'not supported for this type of plot: ' + str(self.ndim) + '-dimension'
    
    def set_marker_filled(self, ds, flag):
        if self.ndim == 1 :
            series = self.pv.getPlot().getDataset().getSeries()
            for s in series :
                if s.getNxDataset() is ds.__iNXDataset__ :
                    self.pv.getPlot().setCurveMarkerFilled(s, flag)
                    break
        else :
            raise ValueError, 'not supported for this type of plot: ' + str(self.ndim) + '-dimension'
    
    def set_legend_title(self, ds, title):
        if self.ndim == 1 :
            series = self.pv.getPlot().getDataset().getSeries()
            for s in series :
                if s.getNxDataset() is ds.__iNXDataset__ :
                    s.setKey(title)
                    break
        else :
            raise ValueError, 'not supported for this type of plot: ' + str(self.ndim) + '-dimension'
    
    def select_dataset(self, ds):
        if self.ndim == 1 :
            series = self.pv.getPlot().getDataset().getSeries()
            index = 0
            for s in series :
                if s.getNxDataset() is ds.__iNXDataset__ :
                    self.pv.getPlot().setSelectedSeries(index)
                    break
                index += 1
        else :
            raise ValueError, 'not supported for this type of plot: ' + str(self.ndim) + '-dimension'

    def get_selected_dataset(self):
        if self.ndim == 1 :
            id = self.pv.getPlot().getSelectedCurveIndex()
            dss = self.ds
            if id >= 0 and len(dss) > id:
                return dss[id]
            else:
                return None
        else :
            raise ValueError, 'not supported for this type of plot: ' + str(self.ndim) + '-dimension'
                            
    def restore_x_range(self):
        self.pv.getPlot().restoreHorizontalBounds()
        
    def restore_y_range(self):
        self.pv.getPlot().restoreVerticalBounds()

    def restore_bounds(self):
        self.pv.getPlot().restoreAutoBounds()
    
    def add_mask_2d(self, x_min, x_max, y_min, y_max, name = None, is_inclusive = True, shape = 'r'):
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

    def add_mask_1d(self, x_min, x_max, name = None, is_inclusive = True):
        mask = RangeMask(is_inclusive)
        if not name is None :
            mask.setName(name)
        mask.setBoundary(min(x_min, x_max), max(x_min, x_max))
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
        
    def get_masks(self):
        if self.pv.getPlot():
            return self.pv.getPlot().getMasks();
        else :
            return []
        
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
        if self.ndim == 2 :
            self.pv.getPlot().getDataset().update()
            self.restore_bounds()

    def get_dataset(self):
#        pds = self.pv.getPlot().getDataset()
#        if not pds is None :
#            return pds.getNXDataset()
        return self.ds
        
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
            if self.pv.getPlot():
                return self.pv.getPlot().getTitle().getText()
            else :
                return 'empty'
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
        if self.ndim == 2 :
            res = 'Image 2D'
        elif self.ndim == 1 :
            res = 'Plot 1D'
        else:
            res = 'Plot'
        if self.title != None :
            res += ': ' + self.title
        return res
    
    def __repr__(self):
        return self.__str__();
    
    def add_mouse_listener(self, listener):
        self.pv.getPlot().addChartMouseListener(listener.__get_jlistener__())

    def add_awt_mouse_listener(self, listener):
        self.pv.getPlot().addMouseListener(listener.__get_jlistener__())
        
    def set_awt_mouse_listener(self, listener):
        lts = self.pv.getPlot().getMouseListeners()
        to_rm = []
        for lt in lts:
            if isinstance(lt, __AWTMouseListener__):
                self.pv.getPlot().removeMouseListener(lt)
        self.add_awt_mouse_listener(listener)
        
    def set_mouse_listener(self, listener):
        listeners = self.pv.getPlot().getListeners(ChartMouseListener)
        for lsn in listeners:
            self.pv.getPlot().removeChartMouseListener(lsn)
        self.add_mouse_listener(listener)
        
    def add_mask_listener(self, listener):
        self.pv.getPlot().addMaskEventListener(listener.__get_jlistener__())
        
    def set_mask_listener(self, listener):
        self.pv.getPlot().getMaskEventListeners().clear()
        self.add_mask_listener(listener)
        
    def remove_mouse_listener(self, listener):
        self.pv.getPlot().removeChartMouseListener(listener.__get_jlistener__())

    def remove_awt_mouse_listener(self, listener):
        self.pv.getPlot().removeMouseListener(listener.__get_jlistener__())

    def remove_mask_listener(self, listener):
        self.pv.getPlot().removeMaskEventListener(listener.__get_jlistener__())

    def close(self):
        PlotView.closePlotView(self.__view__)
        
    # Add markers on horizontal axis. 
    # pos: positions on horizontal axis. Can be either a double value or a list of double values. If height is 0, it will draw a line through the whole y range. 
    # height: length of the marker in pixel
    # color: color of the marker (optional)
    def add_x_marker(self, pos, height, color = None):
        if color is None:
            cc = None
        else:
            cc = __get_color__(color)
        if height is None:
            height = 0
        if hasattr(pos, '__len__') :
            for item in pos :
                self.pv.getPlot().addDomainAxisMarker(item, height, cc)
        else:
            self.pv.getPlot().addDomainAxisMarker(pos, height, cc)
    
    def remove_x_marker(self, pos):
        if hasattr(pos, '__len__') :
            for item in pos :
                self.pv.getPlot().removeDomainAxisMarker(item)
        else:
            self.pv.getPlot().removeDomainAxisMarker(pos)
        
    def clear_x_markers(self):
        self.pv.getPlot().clearDomainAxisMarkers()
        
    def clear_y_markers(self):
        self.pv.getPlot().clearRangeAxisMarkers()
        
    def clear_markers(self):
        self.pv.getPlot().clearMarkers()

    # Add markers on vertical axis. 
    # pos: positions on vertical axis. Can be either a double value or a list of double values. If width is 0, it will draw a line through the whole x range. 
    # height: length of the marker in pixel
    # color: color of the marker (optional)
    def add_y_marker(self, pos, width, color = None):
        if color is None:
            cc = None
        else:
            cc = __get_color__(color)
        if width is None:
            width = 0
        if hasattr(pos, '__len__') :
            for item in pos :
                self.pv.getPlot().addRangeAxisMarker(item, width, cc)
        else :
            self.pv.getPlot().addRangeAxisMarker(pos, width, cc)
     
    def remove_y_marker(self, pos):
        if hasattr(pos, '__len__') :
            for item in pos :
                self.pv.getPlot().removeRangeAxisMarker(item)
        else:
            self.pv.getPlot().removeRangeAxisMarker(pos)
        
    # Add markers in the plot. 
    # x, y: coordinate of the marker in double values.
    # color: color of the marker (optional)
    def add_marker(self, x, y, color = None):
        if color is None:
            cc = None
        else:
            cc = __get_color__(color)
        if hasattr(x, '__len__') and hasattr(y, '__len__') and len(x) == len(y):
            for i in xrange(len(x)) :
                self.pv.getPlot().addMarker(x[i], y[i], cc)
        else :
            self.pv.getPlot().addMarker(x, y, cc)

    def remove_marker(self, x, y):
        if hasattr(x, '__len__') and hasattr(y, '__len__') and len(x) == len(y):
            for i in xrange(len(x)) :
                self.pv.getPlot().removeMarker(x[i], y[i])
        else :
            self.pv.getPlot().removeMarker(x, y)
        
    def set_legend_position(self, pos):
        jpos = LegendPosition.NONE
        try :
            if pos != None:
                jpos = LegendPosition.valueOf(pos.upper())
        except:
            print 'failed to parse ' + str(pos)
        self.pv.getPlot().setLegendPosition(jpos)
    
def __get_color__(name):
    res = None
    try:
        res = eval('Color.' + name.upper())
    except:
        res = Color.getColor(name)
    if name != None and res is None:
        print 'can not interpret color ' + name
    return res
    
def plot(ds = None, title = None, x_label = None, y_label = None):
    return GPlot(ds, title, x_label, y_label)
