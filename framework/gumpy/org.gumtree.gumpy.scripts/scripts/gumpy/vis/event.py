from org.jfree.chart import ChartMouseListener


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
