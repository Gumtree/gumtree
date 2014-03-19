from java.awt.event import MouseListener as JavaMouseListener
from org.jfree.chart import ChartMouseListener
from org.gumtree.vis.mask import IMaskEventListener


class MouseListener :
    
    def __init__(self):
        self.jlistener = __MouseListener__(self)
        
    def on_click(self, event):
        pass
            
    def on_double_click(self, event):
        pass
    
    def on_move(self, event):
        pass
        
    def __get_jlistener__(self):
        return self.jlistener

class __MouseListener__(ChartMouseListener): 
    
    def __init__(self, par):
        self.par = par
        
    def chartMouseClicked(self, event) :
        if event.getTrigger().getClickCount() == 2 :
            self.par.on_double_click(event)
        else :
            self.par.on_click(event)
    
    def chartMouseMoved(self, event):
        self.par.on_move(event)

class AWTMouseListener :
    
    def __init__(self):
        self.jlistener = __AWTMouseListener__(self)
        
    def mouse_clicked(self, event):
        pass

    def mouse_pressed(self, event):
        pass

    def mouse_released(self, event):
        pass

    def mouse_entered(self, event):
        pass

    def mouse_exited(self, event):
        pass
        
    def __get_jlistener__(self):
        return self.jlistener

class __AWTMouseListener__(JavaMouseListener): 
    
    def __init__(self, par):
        self.par = par
        
    def mouseClicked(self, event):
        self.par.mouse_clicked(event)

    def mousePressed(self, event):
        self.par.mouse_pressed(event)

    def mouseReleased(self, event):
        self.par.mouse_released(event)

    def mouseEntered(self, event):
        self.par.mouse_entered(event)

    def mouseExited(self, event):
        self.par.mouse_exited(event)


class MaskEventListener():
    def __init__(self):
        self.jlistener = __MaskEventListener__(self)
        
    def mask_added(self, mask):
        pass
            
    def mask_removed(self, mask):
        pass
    
    def mask_updated(self, mask):
        pass
        
    def __get_jlistener__(self):
        return self.jlistener

class __MaskEventListener__(IMaskEventListener):
    
    def __init__(self, par):
        self.par = par
        
    def maskAdded(self, mask):
        self.par.mask_added(mask)

    def maskRemoved(self, mask):
        self.par.mask_removed(mask)

    def maskUpdated(self, mask):
        self.par.mask_updated(mask)